package org.xmlcml.image.pixel;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageParameters;
import org.xmlcml.image.pixel.PixelIslandComparator.ComparatorType;


/**
 * holds PixelNodes and PixelEdges for pixelIsland
 * 
 * a graph may be a number of PixelNodes and PixelEdges. A single cycle will have a 
 * single PixelNode located in an arbitrary position
 * 
 * @author pm286
 * 
 */
public class PixelGraph {

	private static final String NODE_PREFIX = "zn";

	private final static Logger LOG = Logger.getLogger(PixelGraph.class);

	private static final Angle ANGLE_EPS = new Angle(0.03, Units.RADIANS);
	public static String[] COLOURS = new String[] {"red", "green", "pink", "cyan", "orange", "blue", "yellow"};

	private PixelEdgeList pixelEdgeList;
	private PixelNodeList pixelNodeList;
	private PixelList pixelList;
	PixelIsland island;
	private Map<Pixel, PixelNode> nodeByPixelMap;
	private Map<PixelNode, PixelNucleus> nucleusByPixelNodeMap;
	private Set<PixelNucleus> nucleusSet;
	private Map<Pixel, PixelNucleus> nucleusByPixelMap;
	private PixelSet oneConnectedSet;
	private PixelSet twoConnectedSet;
	private PixelSet threeConnectedSet;
	private PixelSet multiConnectedSet;
	private PixelSet zeroConnectedSet;

	private Map<PixelNucleus, PixelNode> nodeByNucleusMap;
	private SVGG svgGraph;
	private ImageParameters parameters;


	private PixelGraph() {
		
	}
	
	public PixelGraph(PixelIsland island) {
		this(island.getPixelList(), island);
	}
	
	private PixelGraph(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
		this.createNodesAndEdges();
	}

	/** creates graph without pixels
	 * 
	 * @return
	 */
	public static PixelGraph createEmptyGraph() {
		return new PixelGraph();
	}

	/**
	 * creates graph and fills it.
	 * 
	 * @param island
	 * @return
	 */
	public static PixelGraph createGraph(PixelIsland island) {
		PixelGraph pixelGraph = null;
		if (island != null) {
			pixelGraph = new PixelGraph(island.getPixelList(), island);
		}
		return pixelGraph;
	}

	void createNodesAndEdges() {
		if (pixelEdgeList == null) {
			pixelEdgeList = new PixelEdgeList();
			pixelNodeList = new PixelNodeList();
			createEdges();
			createNodes();
//			addMissedPixels();
			LOG.error("MEND createNodesAndEdges");
		}
	}

	private void createNodes() {
		Set<PixelNode> nodeSet = new HashSet<PixelNode>();
		nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
		for (PixelEdge edge : pixelEdgeList) {
			Pixel pixel0 = edge.getPixelList().get(0);
			createAndAddNode(nodeSet, edge, pixel0, 0);
			Pixel pixelLast = edge.getPixelList().last();
			createAndAddNode(nodeSet, edge, pixelLast, 1);
		}
		pixelNodeList.addAll(nodeSet);
	}

	private void createAndAddNode(Set<PixelNode> nodeSet, PixelEdge edge,
			Pixel pixel, int end) {
		PixelNode node = createNode(pixel);
		if (node == null) {
			LOG.trace("null node");
		} else {
			// add serial
//			node.setId("nn"+pixel+nodeSet.size());
			node.setId("nn"+pixel);
			edge.addNode(node, end);
			nodeSet.add(node);
//			usedNonNodePixelSet.add(node.getCentrePixel());
		}
	}

	private PixelNode createNode(Pixel pixel) {
		PixelNode node = null;
		ensureNucleusByPixelMap();
		PixelNucleus nucleus = nucleusByPixelMap.get(pixel);
		if (nucleus != null) {
			node = nodeByNucleusMap.get(nucleus);
			if (node == null) {
				node = new PixelNode(pixel);
				nodeByNucleusMap.put(nucleus, node);
			}
		}
		if (node != null && node.getCentrePixel() == null) {
			node.setCentrePixel(pixel);
		}
		return node;
	}

	private void ensureNucleusByPixelMap() {
		if (nucleusByPixelMap == null) {
			nucleusByPixelMap = new HashMap<Pixel, PixelNucleus>();
		}
	}

	public Map<PixelNode, PixelNucleus> getNucleusByJunctionMap() {
		return nucleusByPixelNodeMap;
	}

	/** creates edges without drawing
	 * 
	 */
	public PixelEdgeList createEdges() {
		createConnectedPixelSets();
		pixelEdgeList = new PixelEdgeList();
		int serial = 0;
		while (!twoConnectedSet.isEmpty()) {
			Iterator<Pixel> iterator = twoConnectedSet.iterator();
			Pixel current = iterator.next();
			LOG.trace("current "+current.toString() + " 2con: " + twoConnectedSet);
			PixelEdge edge = getEdgeFrom2ConnectedPixels(current);
			edge.setId("ee"+(serial++));
			LOG.trace("added "+edge.toString());
			pixelEdgeList.add(edge);
//			usedNonNodePixelSet.addAll(edge.getPixelList().getList());
		}
		return pixelEdgeList;
	}

	/** starts at 2-connected pixel in twoConnectedSet and traverses chain in both directions.
	 * 
	 * starting point can be arbitrary (picked from set of 2-connected pixels).
	 * removes pixels as they are incorporated into edges.
	 * 
	 * @param current
	 * @return
	 */
	private PixelEdge getEdgeFrom2ConnectedPixels(Pixel current) {
		twoConnectedSet.remove(current);
		PixelEdge edge = new PixelEdge(island);
		PixelList neighbours = current.getOrCreateNeighbours(island);
		PixelList list0 = traverseTillNon2Connected(neighbours.get(0), current);
		if (list0.isCycle()) {
			edge.addPixelList(list0);
			LOG.trace("CYCLE");
			return edge;
		} else {
			// lists started in different directions so reverse this and add starting point
			list0.reverse();
			list0.add(current);
			// go in other direction
			PixelList list1 = traverseTillNon2Connected(neighbours.get(1), current);
			// merge
			list0.addAll(list1);
			edge.addPixelList(list0);
			// find nodes
			/** not yet ...
			ensureNucleusByPixelMap();
			PixelList pixelList = edge.getPixelList();
			PixelNucleus nucleus0 = nucleusByPixelMap.get(pixelList.get(0));
			PixelNucleus nucleus1 = nucleusByPixelMap.get(pixelList.last());
			LOG.debug("NUCLEI "+nucleus0+ "/" +nucleus1);
			*/
			
			return edge;
		}
	}
	
	/** starts at current 2-connected pixel and traverses down chain.
	 * 
	 * Direction is the neighbour of current which is NOT avoidMe
	 * 
	 * @param current stating pixel (should be 2-connected)
	 * @param avoidMe pixel in branch to avoid
	 * @return ordered list of pixels starting at current.
	 */
	private PixelList traverseTillNon2Connected(Pixel current, Pixel avoidMe) {
		Pixel last = avoidMe;
		PixelList pixelList = new PixelList();
		while (current != null) {
			boolean stillActive = twoConnectedSet.contains(current);
			if (current.is2ConnectedAny(island)) {
				if (stillActive) {
					twoConnectedSet.remove(current);
					Pixel next = current.getNextNeighbourIn2ConnectedChain(last);
					pixelList.add(current);
					last = current;
					current = next;
				} else {
					// a cycle without any nodes
					pixelList.add(current);
					break;
				}
			} else {
				pixelList.add(current);
				break;
			}
		}
		return pixelList;
	}

	private void drawConnectedPixels(int serial) {
		String[] color = {"red", "blue", "green", "magenta", "cyan"};
		createConnectedPixelSets();
		SVGG gg = new SVGG();
		drawPixels(serial, color, gg, 0, twoConnectedSet);
		drawPixels(serial, color, gg, 1, oneConnectedSet);
		drawPixels(serial, color, gg, 2, threeConnectedSet);
		drawPixels(serial, color, gg, 3, multiConnectedSet);
		SVGSVG.wrapAndWriteAsSVG(gg, new File("target/plot/onetwothree"+serial+".svg"));
	}

	private void createConnectedPixelSets() {
		createMultiConnectedDiagonalPixelSet();
		zeroConnectedSet = createConnectedDiagonalPixelSet(0);
		oneConnectedSet = createConnectedDiagonalPixelSet(1);
		twoConnectedSet = createConnectedDiagonalPixelSet(2);
		threeConnectedSet = createConnectedDiagonalPixelSet(3);
		LOG.trace("connected "+zeroConnectedSet.size());
		LOG.trace("1connected "+oneConnectedSet.size());
		LOG.trace("2connected "+twoConnectedSet.size());
		LOG.trace("3connected "+threeConnectedSet.size());
		LOG.trace("Multiconnected "+multiConnectedSet.size());
	}

	private void drawPixels(int serial, String[] color, SVGG gg, int col1, PixelSet set) {
		PixelList pixelList;
		Set<String> ss;
		pixelList = new PixelList();
		pixelList.addAll(set);
		if (pixelList.size() > 1) {
			SVGG g = pixelList.draw(null, color[(serial + col1) % color.length]);
			gg.appendChild(g);
		}
	}

	private PixelSet createConnectedDiagonalPixelSet(int neighbours) {
		island.setDiagonal(true);
		PixelSet connectedSet = new PixelSet();
		for (Pixel pixel : pixelList) {
			pixel.clearNeighbours();
			if (pixel.isConnectedAny(island, neighbours)) {
				connectedSet.add(pixel);
			}
		}
		return connectedSet;
	}
	
	private PixelSet createMultiConnectedDiagonalPixelSet() {
		island.setDiagonal(true);
		multiConnectedSet = new PixelSet();
		for (Pixel pixel : pixelList) {
			pixel.clearNeighbours();
			for (int conn = 4; conn <= 8; conn++) {
				if (pixel.isConnectedAny(island, conn)) {
					multiConnectedSet.add(pixel);
				}
			}
		}
		return multiConnectedSet;
	}
	
	/**
	 * gets next pixel in chain.
	 * 
	 * @param current
	 * @param last
	 * @param island
	 * @return next pixel or null if no more or branch
	 */
	static Pixel getNextUnusedInEdge(Pixel current, Pixel last,
			PixelIsland island) {
		Pixel next = null;
		PixelList neighbours = current.getOrCreateNeighbours(island);
		neighbours.remove(last);
		next = neighbours.size() == 1 ? neighbours.get(0) : null;
		Long time3 = System.currentTimeMillis();
		return next;
	}

	public PixelNode getPixelNode(Pixel pixel) {
		return pixel == null ? null : nodeByPixelMap.get(pixel);
	}

	public PixelEdgeList getPixelEdgeList() {
		return pixelEdgeList;
	}

	public PixelNodeList getPixelNodeList() {
		return pixelNodeList;
	}

	public PixelList getPixelList() {
		return pixelList;
	}

	public String toString() {
		String s = "";
		s += "; edges: " + (pixelEdgeList == null ? "none" : pixelEdgeList.toString());
		s += "; nodes: " + (pixelNodeList == null ? "none" : pixelNodeList.toString());
		return s;
	}

	public Set<PixelNucleus> getNucleusSet() {
		return nucleusSet;
	}

	public PixelNodeList getPossibleRootNodes1() {
		PixelNodeList pixelNodeList = new PixelNodeList();
		for (PixelNode node : pixelNodeList) {
			PixelEdgeList edgeList = node.getEdges();
			if (edgeList.size() == 1) {
				PixelEdge edge = edgeList.get(0);
				SVGPolyline polyline = edge == null ? new SVGPolyline() : edge.getOrCreateSegmentedPolyline(parameters.getSegmentTolerance());
				if (polyline.size() == 1) {
					pixelNodeList.add(node);
				}
			}
		}
		return pixelNodeList;
	}

	/** get root pixel as middle of leftmost internode edge.
	 * 
	 *  where mid edge is vertical.
	 *  
	 * @return
	 */
	public PixelNode getRootPixelNodeFromExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = getExtremeEdge(comparatorType);
		if (extremeEdge == null) {
			throw new RuntimeException("Cannot find extreme edge for "+comparatorType);
		}
		LOG.debug("extreme "+extremeEdge+"; nodes "+extremeEdge.getPixelNodes().size());
		
		Pixel midPixel = extremeEdge.getNearestPixelToMidPoint();
		PixelNode rootNode = new PixelNode(midPixel, null);
		PixelList neighbours = midPixel.getOrCreateNeighbours(island);
		if (neighbours.size() != 2) {
			throw new RuntimeException("Should have exactly 2 neighbours "+neighbours.size());
		}

		PixelEdgeList pixelEdgeList = splitEdge(extremeEdge, midPixel, rootNode);
		this.addEdge(pixelEdgeList.get(0));
		this.addEdge(pixelEdgeList.get(1));
		this.addNode(rootNode);
		this.removeEdge(extremeEdge);
				
		return rootNode;
	}

	private void removeEdge(PixelEdge edge) {
		pixelEdgeList.remove(edge);
		
	}

	private PixelEdgeList splitEdge(PixelEdge edge, Pixel midPixel,
			PixelNode rootNode) {
		
		PixelEdgeList pixelEdgeList = new PixelEdgeList();
		PixelNodeList nodes = edge.getPixelNodes();
		if (nodes.size() != 2) {
			LOG.error("Should have exactly 2 extremeNodes found "+nodes.size());
			return pixelEdgeList;
		}
		
		PixelList edgePixelList = edge.getPixelList();
		PixelList beforePixelList = edgePixelList.getPixelsBefore(midPixel);
		PixelList afterPixelList = edgePixelList.getPixelsAfter(midPixel);
		
		Pixel beforePixelLast = beforePixelList.last();
		Pixel afterPixelLast = afterPixelList.last();
		if (!beforePixelLast.equals(beforePixelList.last())) {
			beforePixelList.add(beforePixelLast);
		}
		if (!afterPixelLast.equals(afterPixelList.last())) {
			afterPixelList.add(afterPixelLast);
		}
		
		PixelEdge edge0 = createEdge(rootNode, nodes.get(0), beforePixelList);
		pixelEdgeList.add(edge0);
		PixelEdge edge1 = createEdge(rootNode, nodes.get(1), afterPixelList);
		pixelEdgeList.add(edge1);
		
		return pixelEdgeList;
	}

	private PixelEdge createEdge(PixelNode splitNode, PixelNode newEndNode, PixelList pixelList) {
		PixelEdge edge = new PixelEdge(island);
		edge.addNode(splitNode, 0);
		edge.addNode(newEndNode, 1);
		edge.addPixelList(pixelList);
		return edge;
	}

	private PixelEdge getExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = null;
		double extreme = Double.MAX_VALUE;
		for (PixelEdge edge : pixelEdgeList) {
			LOG.trace(edge);
			SVGPolyline polyLine = edge.getOrCreateSegmentedPolyline(parameters.getSegmentTolerance());
			LOG.trace("PL "+polyLine.size()+"  /  "+polyLine.getReal2Array());
			// look for goal post edge
			if (polyLine.size() != 3) {
				continue;
			}
			Line2 crossbar = polyLine.createLineList().get(1).getEuclidLine();
			Real2 midPoint = crossbar.getMidPoint();
			// LHS
			if (ComparatorType.LEFT.equals(comparatorType) && crossbar.isVertical(ANGLE_EPS)) {
				if (midPoint.getX() < extreme) {
					extreme = midPoint.getX();
					extremeEdge = edge;
					LOG.trace("edge "+midPoint);
				}
			// RHS
			} else if (ComparatorType.RIGHT.equals(comparatorType) && crossbar.isVertical(ANGLE_EPS)) {
				if (midPoint.getX() > extreme) {
					extreme = midPoint.getX();
					extremeEdge = edge;
				}
			// TOP
			} else if (ComparatorType.TOP.equals(comparatorType) && crossbar.isHorizontal(ANGLE_EPS)) {
				if (midPoint.getY() < extreme) {
					extreme = midPoint.getY();
					extremeEdge = edge;
				}
			// BOTTOM
			} else if (ComparatorType.BOTTOM.equals(comparatorType) && crossbar.isHorizontal(ANGLE_EPS)) {
				if (midPoint.getY() > extreme) {
					extreme = midPoint.getY();
					extremeEdge = edge;
				}
			}
		}
		return extremeEdge;
	}

	/** assume node in middle of 3-segment path.
	 * 
	 * @return
	 */
	public PixelNodeList getPossibleRootNodes2() {
		PixelNodeList pixelNodeList = new PixelNodeList();
		PixelEdge rootEdge = null;
		PixelNode midNode = null;
		for (PixelEdge edge : pixelEdgeList) {
			LOG.trace(edge.getPixelNodes());
			SVGPolyline polyline = edge.getOrCreateSegmentedPolyline(parameters.getSegmentTolerance());
			Angle deviation = polyline.getSignedAngleOfDeviation();
			if (Math.abs(deviation.getRadian()) < 2.0) continue;
			LOG.trace("POLY "+polyline.getLineList().get(0)+"/"+polyline.getLineList().get(polyline.size() - 1)+"/"+deviation);
			if (polyline.size() == 3) {
				SVGLine midline = polyline.getLineList().get(1);
				Pixel midPixel = edge.getNearestPixelToMidPoint(midline.getMidPoint());
				midNode = new PixelNode(midPixel, null);
				pixelNodeList.add(midNode);
				rootEdge = edge;
			}
		}
		if (pixelNodeList.size() == 1) {
			PixelNode rootNode = pixelNodeList.get(0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 1);
		}
		return pixelNodeList;
	}

	private void removeOldEdgeAndAddNewEdge(PixelNode rootNode, PixelEdge rootEdge, int nodeNum) {
		PixelNode childNode = rootEdge.getPixelNode(nodeNum);
		this.removeEdgeFromNode(childNode, rootEdge);
		addNewEdge(rootNode, childNode);
	}

	private void addNewEdge(PixelNode node0, PixelNode node1) {
		PixelEdge edge = new PixelEdge(null);
		if (node0 != null) {
	 		edge.addNode(node0, 0);
			node0.addEdge(edge);
		}
		if (node1 != null) {
			edge.addNode(node1, 1);
			node1.addEdge(edge);
		}
		this.pixelEdgeList.add(edge);
	}

	private void removeEdgeFromNode(PixelNode node, PixelEdge edge) {
		if (node != null) {
			node.removeEdge(edge);
		}
		pixelEdgeList.remove(edge);
	}

	public void addNode(PixelNode node) {
		ensureNodes();
		if (!pixelNodeList.contains(node)) {
			pixelNodeList.add(node);
		}
	}

	public void addEdge(PixelEdge edge) {
		ensureEdges();
		if (!pixelEdgeList.contains(edge)) {
			pixelEdgeList.add(edge);
			addNode(edge.getPixelNode(0));
			addNode(edge.getPixelNode(1));
		}
	}

	private void ensureNodes() {
		if (pixelNodeList == null) {
			pixelNodeList = new PixelNodeList();
		}
	}

	private void ensureEdges() {
		if (pixelEdgeList == null) {
			pixelEdgeList = new PixelEdgeList();
		}
	}

	public void numberTerminalNodes() {
		int i = 0;
		for (PixelNode node : getPixelNodeList()) {
			if (node instanceof TerminalNode) {
//				node.setLabel(NODE_PREFIX + i);
//			}
			Pixel pixel = node.getCentrePixel();
			Int2 int2 = pixel == null ? null : pixel.getInt2();
			Integer x = (int2 == null) ? null : int2.getX();
			Integer y = (int2 == null) ? null : int2.getY();
			if (x == null || y == null) {
				node.setLabel("N"+i);
			} else {
				node.setLabel(x+"_"+y);
			}
			}
			i++;
		}
	}

	public SVGG drawEdgesAndNodes(String[] colours) {
		SVGG g = new SVGG();
		SVGG rawPixelG = pixelList.plotPixels("magenta");
		g.appendChild(rawPixelG);
		drawEdges(colours, g);
		drawNodes(colours, g);
		return g;
	}

	public void drawNodes(String[] colours, SVGG g) {
		for (int i = 0; i < pixelNodeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelNode node = pixelNodeList.get(i);
			if (node != null) {
				SVGG nodeG = node.createSVG(1.0);
				nodeG.setStroke(col);
				nodeG.setStrokeWidth(0.1);
				nodeG.setOpacity(0.5);
				nodeG.setFill("none");
				g.appendChild(nodeG);
			}
		}
	}

	public void drawEdges(String[] colours, SVGG g) {
		for (int i = 0; i < pixelEdgeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelEdge edge = pixelEdgeList.get(i);
			SVGG edgeG = edge.createPixelSVG(col);
			edgeG.setFill(col);
			g.appendChild(edgeG);
			SVGG lineG = edge.createLineSVG();
			lineG.setFill(col);
			g.appendChild(lineG);
		}
	}

	public void setParameters(ImageParameters parameters) {
		this.parameters = parameters;
	}

	public PixelNode createRootNodeEmpirically(ComparatorType rootPosition) {
		PixelNode rootNode = null;
		PixelNodeList rootNodes = getPossibleRootNodes1();
		Collections.sort(rootNodes.getList());
		if (rootNodes.size() > 0) {
			rootNode = rootNodes.get(0);
	//			if (debug) {
	//				LOG.trace("found root: " + rootNode);
	//			}
		} else {
			try {
				rootNode = getRootPixelNodeFromExtremeEdge(rootPosition);
			} catch (RuntimeException e) {
					throw(e);
			}
		}
		return rootNode;
	}

	public ImageParameters getParameters() {
		return parameters;
	}

	/** creates segmented lines from pixels adds them to edges and draws them.
	 *  
	 *  uses parameters to 
	 * @return
	 */
	public SVGG createSegmentedEdges() {
		SVGG g = new SVGG();
		for (PixelEdge edge: pixelEdgeList) {
			SVGPolyline segments = edge.getOrCreateSegmentedPolyline(parameters.getSegmentTolerance());
			segments.setStroke(parameters.getStroke());
			segments.setWidth(parameters.getLineWidth());
			segments.setFill(parameters.getFill());
			g.appendChild(segments);
		}
		return g;
	}

	public void createAndDrawGraph(SVGG g) {
		LOG.error("createAndDrawGraph NYI");
	}

}
