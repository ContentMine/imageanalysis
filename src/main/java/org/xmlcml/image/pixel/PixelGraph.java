package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.image.ImageParameters;
import org.xmlcml.image.pixel.PixelComparator.ComparatorType;
import org.xmlcml.image.pixel.PixelNucleus.PixelJunctionType;


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

	private PixelEdgeList edgeList; 
	private PixelNodeList nodeList; 
	private PixelList pixelList;
	private PixelIsland island;
	private Stack<PixelNode> nodeStack;
	private PixelNode rootNode;
	
	private PixelGraph() {
		
	}
	
	public PixelGraph(PixelIsland island) {
		this(island.getPixelList(), island);
	}
	
	public PixelGraph(PixelList list) {
		this.island = list.getIsland();
		createGraph(list);
	}
	
	/** all pixels have to belong to island
	 * 
	 * @param pixelList
	 * @param island
	 */
	public PixelGraph(PixelList pixelList, PixelIsland island) {
		this.island = island;
		createGraph(pixelList);
	}

	private void createGraph(PixelList pixelList) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
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
		if (edgeList == null) {
			PixelNucleusFactory nucleusFactory = getNucleusFactory();
			edgeList = nucleusFactory.createPixelEdgeListFromNodeList();
		}
	}

	private void createNodeList() {
		nodeList = getNucleusFactory().getOrCreateNodeListFromNuclei();
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

	public PixelNodeList getNodeList() {
		if (island != null) {
			nodeList = getNucleusFactory().getOrCreateNodeListFromNuclei();
		} else {
			ensureNodes();
		}
		return nodeList;
	}

	public PixelList getPixelList() {
		return pixelList;
	}

	public String toString() {
		getEdgeList();
		StringBuilder sb = new StringBuilder();
		sb.append("; edges: " + (edgeList == null ? "none" : edgeList.size()+"; "+edgeList.toString()));
		sb.append("\n     ");
		sb.append("nodes: " + (nodeList == null ? "none" : nodeList.size()+"; "+nodeList.toString()));
		return sb.toString();
	}

	/** get root pixel as middle of leftmost internode edge.
	 * 
	 *  where mid edge is vertical.
	 *  
	 * @return
	 */
	public PixelNode getRootNodeFromExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = getExtremeEdge(comparatorType);
		if (extremeEdge == null) {
			throw new RuntimeException("Cannot find extreme edge for "+comparatorType);
		}
		LOG.trace("extreme "+extremeEdge+"; nodes "+extremeEdge.getNodes().size());
		
		Pixel midPixel = extremeEdge.getNearestPixelToMidPoint();
		rootNode = splitEdgeAndInsertNewNode(extremeEdge, midPixel);
				
		return rootNode;
	}
	
	public PixelNode getRootPixelNode() {
		return rootNode;
	}

	public PixelNode splitEdgeAndInsertNewNode(PixelEdge oldEdge, Pixel midPixel) {
		PixelNode midNode = new PixelNode(midPixel, this);
		PixelList neighbours = midPixel.getOrCreateNeighbours(island);
		if (neighbours.size() != 2) {
			throw new RuntimeException("Should have exactly 2 neighbours "+neighbours.size());
		}

		PixelEdgeList edgeList = splitEdge(oldEdge, midPixel, midNode);
		this.addEdge(edgeList.get(0));
		this.addEdge(edgeList.get(1));
		this.addNode(midNode);
		this.removeEdge(oldEdge);
		return midNode;
	}

	private void removeEdge(PixelEdge edge) {
		edgeList.remove(edge);
		PixelNodeList nodeList = edge.getNodes();
		for (PixelNode node : nodeList) {
			node.removeEdge(edge);
		}
	}

	private void removeNode(PixelNode node) {
		nodeList.remove(node);
		PixelEdgeList edgeList = node.getEdges();
		for (PixelEdge edge : edgeList) {
			edge.removeNode(node);
		}
	}

	private PixelEdgeList splitEdge(PixelEdge edge, Pixel midPixel, PixelNode midNode) {
		
		PixelEdgeList edgeList = new PixelEdgeList();
		PixelNodeList nodes = edge.getNodes();
		if (nodes.size() != 2) {
			LOG.error("Should have exactly 2 extremeNodes found "+nodes.size());
			return edgeList;
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
		
		PixelEdge edge0 = createEdge(midNode, nodes.get(0), beforePixelList);
		edgeList.add(edge0);
		PixelEdge edge1 = createEdge(midNode, nodes.get(1), afterPixelList);
		edgeList.add(edge1);
		
		return edgeList;
	}

	private PixelEdge createEdge(PixelNode splitNode, PixelNode newEndNode, PixelList pixelList) {
		PixelEdge edge = new PixelEdge(island);
		edge.addNode(splitNode, 0);
		edge.addNode(newEndNode, 1);
		edge.addPixelList(pixelList);
		return edge;
	}

	@Deprecated
	private PixelEdge getExtremeEdge(ComparatorType comparatorType) {
		PixelEdge extremeEdge = null;
		double extreme = Double.MAX_VALUE;
		for (PixelEdge edge : edgeList) {
			LOG.trace(edge);
			PixelSegmentList segmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			LOG.trace("PL "+segmentList.size()+"  /  "+segmentList.getReal2Array());
			// look for goal post edge
			if (segmentList.size() != 3) {
				continue;
			}
			Line2 crossbar = segmentList.get(1).getEuclidLine();
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
	public PixelNodeList getPossibleRootNodes() {
		PixelNodeList nodeList = new PixelNodeList();
		PixelEdge rootEdge = null;
		PixelNode midNode = null;
		for (PixelEdge edge : edgeList) {
			LOG.trace(edge.getNodes());
			PixelSegmentList segmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			Angle deviation = segmentList.getSignedAngleOfDeviation();
			if (Math.abs(deviation.getRadian()) < 2.0) continue;
			LOG.trace("POLY "+segmentList.get(0)+"/"+segmentList.getLast()+"/"+deviation);
			if (segmentList.size() == 3) {
				SVGLine midline = segmentList.get(1).getSVGLine();
				Pixel midPixel = edge.getNearestPixelToMidPoint(midline.getMidPoint());
				midNode = new PixelNode(midPixel, this);
				nodeList.add(midNode);
				rootEdge = edge;
			}
		}
		if (nodeList.size() == 1) {
			rootNode = nodeList.get(0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 0);
			removeOldEdgeAndAddNewEdge(rootNode, rootEdge, 1);
		}
		return nodeList;
	}

	private void removeOldEdgeAndAddNewEdge(PixelNode rootNode, PixelEdge rootEdge, int nodeNum) {
		PixelNode childNode = rootEdge.getPixelNode(nodeNum);
		this.removeEdgeFromNode(childNode, rootEdge);
		addNewEdge(rootNode, childNode);
	}

	private void addNewEdge(PixelNode node0, PixelNode node1) {
		PixelEdge edge = new PixelEdge(this);
		if (node0 != null) {
	 		edge.addNode(node0, 0);
			node0.addEdge(edge);
		}
		if (node1 != null) {
			edge.addNode(node1, 1);
			node1.addEdge(edge);
		}
		this.edgeList.add(edge);
	}

	private void removeEdgeFromNode(PixelNode node, PixelEdge edge) {
		if (node != null) {
			node.removeEdge(edge);
		}
		edgeList.remove(edge);
	}

	public void addNode(PixelNode node) {
		ensureNodes();
		if (!nodeList.contains(node)) {
			nodeList.add(node);
		}
	}

	public void addEdge(PixelEdge edge) {
		ensureEdges();
		if (!edgeList.contains(edge)) {
			edgeList.add(edge);
			addNode(edge.getPixelNode(0));
			addNode(edge.getPixelNode(1));
		}
	}

	private void ensureNodes() {
		if (nodeList == null) {
			nodeList = new PixelNodeList();
		}
	}

	private void ensureEdges() {
		if (edgeList == null) {
			edgeList = new PixelEdgeList();
		}
	}

	public void numberTerminalNodes() {
		int i = 0;
		for (PixelNode node : getNodeList()) {
//			if (node != instanceof TerminalNode) {
				node.setLabel(NODE_PREFIX + i);
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
		ensureNodeList();
		for (int i = 0; i < nodeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelNode node = nodeList.get(i);
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
		for (int i = 0; i < edgeList.size(); i++) {
			String col = colours[i % colours.length];
			PixelEdge edge = edgeList.get(i);
			SVGG edgeG = edge.createPixelSVG(col);
			edgeG.setFill(col);
			g.appendChild(edgeG);
			SVGG lineG = edge.createLineSVG();
			lineG.setFill(col);
			g.appendChild(lineG);
		}
	}

	@Deprecated
	public PixelNode createRootNodeEmpirically(ComparatorType rootPosition) {
		throw new RuntimeException("MEND or KILL");
//		PixelNode rootNode = null;
//		PixelNodeList rootNodes = getPossibleRootNodes(null);
//		Collections.sort(rootNodes.getList());
//		if (rootNodes.size() > 0) {
//			rootNode = rootNodes.get(0);
//		} else {
//			try {
//				rootNode = getRootNodeFromExtremeEdge(rootPosition);
//			} catch (RuntimeException e) {
//					throw(e);
//			}
//		}
//		return rootNode;
	}

	public ImageParameters getParameters() {
		return getIsland().getParameters();
	}

	private PixelIsland getIsland() {
		if (island == null) {
			throw new RuntimeException("Island is required");
		}
		return island;
	}

	/** creates segmented lines from pixels adds them to edges and draws them.
	 *  
	 *  uses parameters to 
	 * @return
	 */
	public SVGG createSegmentedEdges() {
		SVGG g = new SVGG();
		for (PixelEdge edge: edgeList) {
			PixelSegmentList pixelSegmentList = edge.getOrCreateSegmentList(getParameters().getSegmentTolerance());
			pixelSegmentList.setStroke(getParameters().getStroke());
			pixelSegmentList.setWidth(getParameters().getLineWidth());
			pixelSegmentList.setFill(getParameters().getFill());
			g.appendChild(pixelSegmentList.getOrCreateSVG());
		}
		return g;
	}

	public void createAndDrawGraph(SVGG g) {
		PixelEdgeList edgeList = getEdgeList();
		PixelNodeList nodeList = getNodeList();
		for (PixelNode node : nodeList) {			
			String color = node.getEdges().size() == 0 ? "red" : "green";
			SVGG gg = node.createSVG(2.0, color);
			gg.setOpacity(0.3);
			g.appendChild(gg);
		}
		for (PixelEdge edge : edgeList) {
			SVGG gg = edge.createLineSVG();
			gg.setFill("blue");
			gg.setStroke("purple");
			gg.setStrokeWidth(2.0);
			g.appendChild(gg);
		}
//		SVGText text = new SVGText(new Real2(300, 300), "createAndDrawGraph NYI");
//		text.setFontSize(50.);
//		g.appendChild(text);
//		LOG.error("createAndDrawGraph NYI");
	}

	private PixelNode getNode(Pixel pixel) {
		for (PixelNode node : nodeList) {
			if (pixel.equals(node.getCentrePixel())) {
				return node;
			}
		}
		return null;
	}

	public PixelEdge createEdge(PixelNode node) {
		PixelEdge edge = null;
		if (node.hasMoreUnusedNeighbours()) {
			Pixel neighbour = node.getNextUnusedNeighbour();
			edge = createEdge(node, neighbour);
		}
		return edge;
	}

	public PixelEdge createEdge(PixelNode node, Pixel next) {
		PixelEdge edge = new PixelEdge(this);
		Pixel current = node.getCentrePixel();
		edge.addNode(node, 0);
		LOG.trace("start "+node);
		node.removeUnusedNeighbour(next);
		edge.addPixel(current);
		edge.addPixel(next);
		while (true) {
			PixelList neighbours = next.getOrCreateNeighbours(island);
			if (neighbours.size() != 2) {
				break;
			}
			Pixel next0 = neighbours.getOther(current);
			edge.addPixel(next0);
			current = next;
			next = next0;
			LOG.trace(current);
		}
		LOG.trace("end "+next);
		PixelNode node2 = getNode(next);
		if (node2 == null) {
			throw new RuntimeException("cannot find node for pixel: "+next);
		}
		node2.removeUnusedNeighbour(current);
		edge.addNode(node2, 1);
		return edge;
	}

	private Stack<PixelNode> createNodeStack() {
		createNodeList();
		nodeStack = new Stack<PixelNode>();
		for (PixelNode node : nodeList) {
			nodeStack.push(node);
		}
		return nodeStack;
	}

	private PixelNucleusFactory getNucleusFactory() {
		if (island == null) {
			throw new RuntimeException("Island must not be null");
		}
		return island.getOrCreateNucleusFactory();
	}

	public PixelNucleusList getPixelNucleusList() {
		return getNucleusFactory().getOrCreateNucleusList();
	}

	public PixelEdgeList getEdgeList() {
		if (island != null) {
			edgeList = getNucleusFactory().getEdgeList();
		} else {
			ensureEdges();
		}
		return edgeList;
	}

	public void numberAllNodes() {
		ensureNodeList();
		int i = 0;
		for (PixelNode node : nodeList) {
			node.setLabel("n" + (i++));
		}
	}

	public void addCoordsToNodes() {
		ensureNodeList();
		for (PixelNode node : nodeList) {
			Int2 coord = node.getInt2();
			if (coord != null) {
				String label = String.valueOf(coord).replaceAll("[\\(\\)]", "").replaceAll(",","_");
				node.setLabel(label);
			}
		}
	}

	private void ensureNodeList() {
		if (nodeList == null) {
			nodeList = new PixelNodeList();
		}
	}

	public void debug() {
		LOG.debug("graph...");
		for (PixelNode node : this.getNodeList()) {
			LOG.debug("n> "+ node.toString());
			for (PixelEdge edge : node.getEdges()) {
				LOG.debug("  e: "+edge.getNodes());
			}
		}
	}
	
	public <T> PixelTree<T> getPixelTree() {
		//return getFirstTree(new ArrayList<PixelNode>());
		List<PixelTree<T>> tree = getPixelTrees(1);
		return tree.get(0);
	}
	
	public <T> List<PixelTree<T>> getPixelTrees() {
		return getPixelTrees(Integer.MAX_VALUE);
	}

	public <T> List<PixelTree<T>> getPixelTrees(int n) {
		getEdgeList();
		List<PixelTree<T>> trees = new ArrayList<PixelTree<T>>();
		int i = 0;
		for (PixelNode node : getNodeList()) {
			PixelTree<T> tree = new PixelTree<T>();
			if (node.getEdges().size() == 0) {
				if (node.getNucleus().getJunctionType() == PixelJunctionType.DOT) {
					tree.addEdgelessNode(node);
					trees.add(tree);
				}
				continue;
			}
			trees.add(tree);
			for (PixelEdge e : node.getEdges()) {
				addLineToTree(tree, e, node);
			}
			i++;
			if (i == n) {
				break;
			}
		}
		return trees;
		/*List<PixelNode> nodes = new ArrayList<PixelNode>();
		PixelTree<T> tree1 = getFirstTree(nodes);
		PixelTree<T> tree2 = new PixelTree<T>();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			PixelNode node = nodes.get(i);
			for (PixelEdge e : node.getEdges()) {
				addLineToTree(tree2, e, node);
			}
		}
		List<PixelTree<T>> trees = new ArrayList<PixelTree<T>>();
		trees.add(tree1);
		trees.add(tree2);
		return trees;*/
		/*PixelNode firstNode;
		try {
			firstNode = getNucleusFactory().getOrCreateTerminalJunctionList().get(0).getNode();//getNodeList().get(0);
		} catch (IndexOutOfBoundsException e) {
			firstNode = getNucleusFactory().getOrCreateNucleusList().get(0).getNode();
		}
		PixelEdgeList edges = getEdgeList();
		Set<PixelEdge> setOfEdges = new HashSet<PixelEdge>(edges.getList());
		//Pixel firstSpike = getNucleusFactory().getOrCreateSpikePixelList().get(0);
		PixelTree<T> tree = new PixelTree<T>();
		addLineToTree(tree, setOfEdges.iterator().next(), firstNode, );
		while (setOfEdges.size() > 0) {
			addLineToTree(tree, setOfEdges.iterator().next(), firstNode, );
		}*/
	}

	//private <T> PixelTree<T> getFirstTree(List<PixelNode> nodes) {
		//getEdgeList();
		/*PixelTree<T> tree = new PixelTree<T>();
		for (PixelNucleus terminal : getNucleusFactory().getOrCreateTerminalJunctionList()) {
			if (terminal.getNode().getEdges().size() == 1) {
				addLineToTree(tree, terminal.getNode().getEdges().get(0), terminal.getNode());
				nodes.add(terminal.getNode());
			} else {
				System.out.println("Error: tricky nucleus");
			}
		}
		for (PixelNucleus other : getNucleusFactory().getOrCreateNucleusList()) {
			if (other instanceof DotNucleus) {
				tree.addEdgelessNode(other.getNode());
				//nodes.add(other.getNode());
			} else if (other.getNode().getEdges().size() > 0) {
				//nodes.add(other.getNode());
				for (PixelEdge e : other.getNode().getEdges()) {
					addLineToTree(tree, e, other.getNode());
				}
			} else {
				System.out.println("Error: tricky nucleus");
			}
		}
		return tree;*/
	//}

	private <T> void addLineToTree(PixelTree<T> tree, PixelEdge edge, PixelNode startNode) {
		if (tree.edgeEncountered(edge)) {
			return;
		}
		/*PixelNucleusFactory nucleusFactory = getNucleusFactory();
		PixelList line = nucleusFactory.findLine(nucleusFactory.getNucleusBySpikePixel(firstSpike), firstSpike);*/
		tree.addPixelsFromEdge(startNode, edge);
		PixelNode endNode = edge.getOtherNode(startNode);
		if (!tree.nodeEncountered(endNode)) {
			Set<PixelEdge> edges = new LinkedHashSet<PixelEdge>(endNode.getEdges().getList());
			for (PixelEdge e : edges) {
				if (!tree.edgeEncountered(e)) {
					addLineToTree(tree, e, endNode);
				}
			}
		}
		//nucleusFactory.getNucleusByPixel(line.get(line.size())).createSpikePixelList()
	}

	public void tidyNodesAndEdges(int largestSmallEdgeAllowed) {
		getEdgeList();
		getNodeList();
		tidyEdges(largestSmallEdgeAllowed);
		tidyNodes();
	}

	private void tidyEdges(int largestSmallEdgeAllowed) {
		List<PixelEdge> smallEdges = new ArrayList<PixelEdge>();
		for (PixelEdge edge : edgeList) {
			if (edge.getNodes().size() != 2) {
				LOG.trace("Edge with missing node or nodes: " + edge.getNodes().size());
			} else {
				PixelNode first = edge.getNodes().get(0);
				PixelNode last = edge.getNodes().get(1);
				if ((first.getEdges().size() == 1 || last.getEdges().size() == 1) && edge.size() <= largestSmallEdgeAllowed) {
					smallEdges.add(edge);
				}
			}
		}
		for (PixelEdge e : smallEdges) {
			removeEdge(e);
		}
	}

	private void tidyNodes() {
		LOG.trace("Nodes: " + nodeList.size());
		PixelNodeList copyList = new PixelNodeList(nodeList);
		for (PixelNode node : copyList) {
			remove2ConnectedNode(node);
			if (node.getEdges().size() == 0) {
				removeNode(node);
			}
		}
		LOG.trace("Nodes after: " + nodeList.size());
	}

	private void remove2ConnectedNode(PixelNode node) {
		if (node.getEdges().size() == 2) {
			PixelEdge edge0 = node.getEdges().get(0);
			PixelEdge edge1 = node.getEdges().get(1);
			if (edge0 != edge1) {
				PixelNode node0 = edge0.getOtherNode(node);
				PixelNode node1 = edge1.getOtherNode(node);
				LOG.trace("Others: " + node0 + ", " + node1);
				PixelEdge edge01 = createEdgeWithoutPixels(node0, node1);
				Pixel first0 = edge0.getFirst();
				Pixel first1 = edge1.getFirst();
				Pixel last0 = edge0.getLast();
				Pixel last1 = edge1.getLast();
				double dist1 = first0.getInt2().getEuclideanDistance(node.getInt2());
				double dist2 = last0.getInt2().getEuclideanDistance(node.getInt2());
				double dist3 = first1.getInt2().getEuclideanDistance(node.getInt2());
				double dist4 = last1.getInt2().getEuclideanDistance(node.getInt2());
				if (dist1 < dist2) {
					Collections.reverse(edge0.getPixelList().getList());
				}
				if (dist4 < dist3) {
					Collections.reverse(edge1.getPixelList().getList());
				}
				edge01.addPixelList(edge0.getPixelList());
				edge01.addPixelList(edge1.getPixelList());
				addEdge(edge01);
				removeEdge(edge0);
				removeEdge(edge1);
				removeNode(node);
				LOG.trace("Removed: " + node);
			}
		}
	}

	private PixelEdge createEdgeWithoutPixels(PixelNode node0, PixelNode node1) {
		PixelEdge pixelEdge = new PixelEdge(this);
		pixelEdge.addNode(node0, 0);
		pixelEdge.addNode(node1, 1);
		return pixelEdge;
		
	}

}