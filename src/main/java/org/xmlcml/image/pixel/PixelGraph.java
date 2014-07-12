package org.xmlcml.image.pixel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.euclid.Vector3;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGSVG;


/**
 * holds PixelNodes and PixelEdges for pixelIsland
 * 
 * a graph may either be a single PixelCycle or may be a number of PixelNodes
 * and PixelEdges
 * 
 * @author pm286
 * 
 */
public class PixelGraph {

	private final static Logger LOG = Logger.getLogger(PixelGraph.class);

	private List<PixelEdge> edges;
	private List<PixelNode> nodes;
	private PixelCycle cycle;
	private PixelList pixelList;
	private JunctionSet junctionSet;
	private TerminalNodeSet terminalNodeSet;
	PixelIsland island;
	private Map<Pixel, JunctionNode> junctionByPixelMap;
	private Map<Pixel, TerminalNode> terminalNodeByPixelMap;
	private Set<Pixel> usedNonNodePixelSet;
	private SortedPixelNodeSet activeNodeSet;
	private Map<JunctionNode, PixelNucleus> nucleusByJunctionMap;
	private Set<PixelNucleus> nucleusSet;

	private Map<Pixel, PixelNucleus> nucleusByPixelMap;
	private Set<Pixel> oneConnectedSet;
	private Set<Pixel> twoConnectedSet;
	private Set<Pixel> threeConnectedSet;
	private Set<Pixel> multiConnectedSet;
	private Set<Pixel> zeroConnectedSet;

	private Map<PixelNucleus, PixelNode> nodeByNucleusMap;
	
	private SVGG svgGraph;

	private PixelGraph() {
		
	}
	
	public PixelGraph(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
//		this.createEdgesNew();
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
	@Deprecated 
	public static PixelGraph createGraph(PixelIsland island) {
		return island == null ? null : createGraph(island.getPixelList(),
				island);
	}

	/**
	 * creates graph and fills it.
	 * 
	 * @param pixelList
	 * @param island
	 * @return
	 * 
	 */
	@Deprecated 
	public static PixelGraph createGraph(PixelList pixelList, PixelIsland island) {
		PixelGraph graph = new PixelGraph(pixelList, island);
		graph.createGraph();
		return graph;
	}
//
//	public PixelGraph createGraphNewxx() {
//		PixelGraph graph = new PixelGraph(this.getPixelList(), this);
//		graph.createNodesAndEdges();
//		return graph;
//	}

	@Deprecated
	private void createGraph() {
		if (edges == null) {
			edges = new ArrayList<PixelEdge>();
			nodes = new ArrayList<PixelNode>();
			usedNonNodePixelSet = new HashSet<Pixel>();
			getTerminalNodeSet();
			getJunctionSet();
			getOrCreateNucleusSetAndMap();
			removeExtraneousPixelsFromNuclei();
			getOrCreateNucleusSetAndMap(); // recompute with thinner graph
			removeExtraneousJunctionsFromNuclei();
			tidyNucleiIntoNodes();
			createGraphComponents();
		}
	}

	SVGG createNodesAndEdges() {
		if (edges == null) {
			edges = new ArrayList<PixelEdge>();
			nodes = new ArrayList<PixelNode>();
			getTerminalNodeSet();
			getJunctionSet();
			makeNucleusMap();
			usedNonNodePixelSet = new HashSet<Pixel>();
			svgGraph = new SVGG();
			createEdgesNew(svgGraph);
			createNodes(svgGraph);
//			SVGSVG.wrapAndWriteAsSVG(g, new File("target/plot/lines"+serial+".svg"));
		}
		return svgGraph;
	}

	private void createNodes(SVGG g) {
		Set<PixelNode> nodeSet = new HashSet<PixelNode>();
		nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
		for (PixelEdge edge : edges) {
			createAndAddNode(g, nodeSet, edge, edge.getPixelList().get(0), 0);
			createAndAddNode(g, nodeSet, edge, edge.getPixelList().last(), 1);
		}
		nodes.addAll(nodeSet);
	}

	private void createAndAddNode(SVGG g, Set<PixelNode> nodeSet, PixelEdge edge,
			Pixel pixel, int end) {
		PixelNode node = createNode(pixel, g);
		if (node == null) {
			LOG.trace("null node");
		} else {
			edge.addNode(node, end);
			nodeSet.add(node);
		}
	}

	private PixelNode createNode(Pixel pixel, SVGG g ) {
		PixelNode node = null;
		PixelNucleus nucleus = nucleusByPixelMap.get(pixel);
		if (nucleus != null) {
			node = nodeByNucleusMap.get(nucleus);
			if (node == null) {
				node = new NucleusNode(nucleus);
				nodeByNucleusMap.put(nucleus, node);
			}
		}
		if (node == null) {
			node = terminalNodeByPixelMap.get(pixel);
		}
		return node;
	}

	/** messy as nuclei may contain several proto-junctions in JunctionSet;
	 * 
	 */
	@Deprecated  // probably
	private void tidyNucleiIntoNodes() {
		JunctionSet junctionSetNew = new JunctionSet();
		for (PixelNucleus nucleus : getNucleusSet()) {
			JunctionSet jSet = nucleus.getJunctionSet();
			for (PixelNode junction0 : jSet) {
				JunctionNode junction = (JunctionNode)junction0;
				if (junction.isYJunction()) {
					junctionSetNew.add((JunctionNode)junction);
				}
			}
		}
		junctionSet = junctionSetNew;
	}

	@Deprecated
	private void tidyNucleiIntoNodesNew() {
		junctionSet = new JunctionSet();
		for (PixelNucleus nucleus : getNucleusSet()) {
//			Real2 centre = nucleus.getCentre();
			Pixel centrePixel = nucleus.getCentrePixel();
			JunctionNode junction = new JunctionNode(centrePixel, null);
			junctionSet.add(junction);
		}
//		PixelList twoConnectedSet = get2ConnectedPixels();
	}

	@Deprecated // BUT need to manage cycles and singleton pixels
	private void createGraphComponents() {
		if (pixelList.size() == 0) {
			throw new RuntimeException("no pixels in island");
		}
		if (pixelList.size() == 1) {
			createSinglePixelCycle();
		} else if (this.terminalNodeSet.size() == 0) {
			if (this.junctionSet.size() == 0) { // a circle?
				createAndAddCycle();
			} else {
				createEdgesatArbitraryStart();
			}
		} else {
			// start at arbitrary end node
			TerminalNode start = (TerminalNode) terminalNodeSet.iterator().next();
			this.createEdges(start);
		}
	}

	private void createSinglePixelCycle() {
		// single pixel - conventionally a cycle of 1
		PixelCycle cycle = new PixelCycle(pixelList.get(0), island);
		this.add(cycle);
	}

	@Deprecated
	private void createEdgesatArbitraryStart() {
		// start at arbitrary end node
		JunctionNode start = (JunctionNode) junctionSet.iterator().next();
		this.createEdges(start);
	}

	@Deprecated
	private void createAndAddCycle() {
		PixelCycle cycle = this.createCycle();
		if (cycle == null) {
			throw new RuntimeException(
					"Cannot create a single cycle");
		}
		this.add(cycle);
	}

	@Deprecated
	private void removeExtraneousPixelsFromNuclei() {
		for (PixelNucleus nucleus : getNucleusSet()) {
			nucleus.removeExtraneousPixels();
		}
		junctionSet = null;
		getJunctionSet();
	}

	@Deprecated
	private void removeExtraneousJunctionsFromNuclei() {
		for (PixelNucleus nucleus : getNucleusSet()) {
			List<JunctionNode> junctions = nucleus.removeExtraneousJunctions();
			junctionSet.removeAll(junctions);
		}
	}

	/**
	 * similar to floodfill
	 * 
	 * maybe unnecessary as we are treating nuclei as unstructured
	 * 
	 * add locally connected nodes.
	 */
	private Map<JunctionNode, PixelNucleus> getOrCreateNucleusSetAndMap() {
		if (nucleusSet == null) {
			nucleusByJunctionMap = new HashMap<JunctionNode, PixelNucleus>();
			Set<PixelNode> unusedNodes = new HashSet<PixelNode>();
			nucleusSet = new HashSet<PixelNucleus>();
			if (junctionSet != null) {
				unusedNodes.addAll(junctionSet.getList());
			}
			createNucleiFromJunctionSet(unusedNodes);
		}
		return nucleusByJunctionMap;
	}

	private void createNucleiFromJunctionSet(Set<PixelNode> unusedNodes) {
		while (!unusedNodes.isEmpty()) {
			LOG.trace("unused " + unusedNodes.size());
			// new nucleus, find next unused Junction
			JunctionNode nextNode = (JunctionNode) unusedNodes.iterator().next();
			unusedNodes.remove(nextNode);
			Set<JunctionNode> nucleusNodeSet = new HashSet<JunctionNode>();
			nucleusNodeSet.add(nextNode);
			PixelNucleus nucleus = new PixelNucleus(island);
			getNucleusSet().add(nucleus);
			// do we need this?
			addNucleustoNucleusByJunctionMap(unusedNodes, nucleusNodeSet, nucleus);
		}
	}

	private void addNucleustoNucleusByJunctionMap(Set<PixelNode> unusedNodes,
			Set<JunctionNode> nucleusNodeSet, PixelNucleus nucleus) {
		JunctionNode nextNode;
		while (!nucleusNodeSet.isEmpty()) {
			LOG.trace("nucleus " + nucleusNodeSet.size() + " "
					+ nucleusNodeSet);
			nextNode = nucleusNodeSet.iterator().next();
			nucleus.add(nextNode);
			nucleusByJunctionMap.put(nextNode, nucleus);
			nucleusNodeSet.remove(nextNode);
			List<JunctionNode> neighbourJunctions = getNeighbourJunctions(nextNode);
			for (JunctionNode neighbourJunction : neighbourJunctions) {
				if (!nucleus.contains(neighbourJunction)) {
					nucleusNodeSet.add(neighbourJunction);
					unusedNodes.remove(neighbourJunction);
				}
			}
		}
	}

	public Map<JunctionNode, PixelNucleus> getNucleusByJunctionMap() {
		return nucleusByJunctionMap;
	}

	/**
	 * take next unused neighbour as second pixel in edge
	 * 
	 * @param start
	 *            // seems to be surperfluous
	 */
	@Deprecated
	private void createEdges(PixelNode start) {
		this.getNextUnusedNonNodeNeighbour(start);
		createEdges();
	}

	@Deprecated
	private void createEdges() {
		edges = new ArrayList<PixelEdge>();
		activeNodeSet = new SortedPixelNodeSet();
		activeNodeSet.addAll(terminalNodeSet.getList());
		activeNodeSet.addAll(junctionSet.getList());
		LOG.trace("sets: " + terminalNodeSet + "\n" + junctionSet + "\n"
				+ activeNodeSet);
		PixelEdge lastEdge = null;
		while (!activeNodeSet.isEmpty()) {
			PixelNode startNode = activeNodeSet.iterator().next();
			PixelEdge edge = createEdge(startNode);
			LOG.trace("created edge: ");
			if (edge == null) {
				LOG.trace("null edge from: " + startNode);
				activeNodeSet.remove(startNode);
				continue;
			} else if (lastEdge != null
					&& edge.toString().equals(lastEdge.toString())) {
				LOG.error("BUG duplicate edge: "+edge);
				activeNodeSet.remove(startNode);
				continue;
//				break;
			}
			lastEdge = edge;
			add(edge);
			addNonNodePixelsInEdgeToNonNodeUsedSet(edge);
			removeEndNodesIfNoUnusedNeighbours(edge);
		}
	}

	private void createEdgesNew(SVGG g) {
		createConnectedPixelSets();
		edges = new ArrayList<PixelEdge>();
		
		while (!twoConnectedSet.isEmpty()) {
			Iterator<Pixel> iterator = twoConnectedSet.iterator();
			Pixel current = iterator.next();
			PixelEdge edge = getEdgeFrom2ConnectedPixels(current);
			edges.add(edge);
			SVGLine line = drawLine(edge);
			g.appendChild(line);
		}
		LOG.trace("edges "+edges.size());

	}

	void createEdgesNew() {
		createConnectedPixelSets();
		edges = new ArrayList<PixelEdge>();
		while (!twoConnectedSet.isEmpty()) {
			Iterator<Pixel> iterator = twoConnectedSet.iterator();
			Pixel current = iterator.next();
			PixelEdge edge = getEdgeFrom2ConnectedPixels(current);
			edges.add(edge);
		}
	}

	private SVGLine drawLine(PixelEdge edge) {
		PixelList pixelList = edge.getPixelList();
		LOG.trace("PIXELS: "+pixelList.size());
		Int2 xy0 = pixelList.get(0).getInt2();
		Int2 xy1 = pixelList.get(pixelList.size() - 1).getInt2();
		SVGLine line = new SVGLine(new Real2(xy0), new Real2(xy1));
		line.setWidth(2.0);
		line.setStroke("magenta");
		line.setFill("red");
		return line;
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
		PixelList neighbours = current.getNeighbours(island);
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
	
//	private void ensureNucleusByPixelMap() {
//		if (nucleusByPixelMap == null) {
//			makeNucleusMap();
//		}
//	}

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

	@Deprecated
	private PixelEdge getEdge(Pixel current) {
		oneConnectedSet.remove(current);
		PixelEdge edge = new PixelEdge(island);
		edge.addPixel(current);
		Pixel last = null;
		while (true) {
			if (current.is1ConnectedAny(island) && last != null) {
				oneConnectedSet.remove(current);
				break;
			} else if (current.is1ConnectedAny(island)) {
				// first
				Pixel next = current.getNeighbours(island).get(0);
				last = current;
				current = next;
			} else if (!current.is2ConnectedAny(island)) {
				break;
			} else {
				Pixel next = current.getNextNeighbourIn2ConnectedChain(last);
				edge.addPixel(next);
				last = current;
				current = next;
			}
		}
		return edge;
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

	private void drawPixels(int serial, String[] color, SVGG gg, int col1, Set<Pixel> set) {
		PixelList pixelList;
		pixelList = new PixelList(set);
		if (pixelList.size() > 1) {
			SVGG g = pixelList.draw(null, color[(serial + col1) % color.length]);
			gg.appendChild(g);
		}
	}

	@Deprecated
	private void removeEndNodesIfNoUnusedNeighbours(PixelEdge edge) {
		List<PixelNode> nodes = edge.getPixelNodes();
		for (PixelNode node : nodes) {
			if (node == null) {
				throw new RuntimeException("BUG null node: " + nodes.size());
			}
			if (this.getNextUnusedNonNodeNeighbour(node) == null) {
				activeNodeSet.remove(node);
				LOG.trace("inactivated node: " + node + " / " + activeNodeSet);
			}
		}
	}

	@Deprecated
	private List<Pixel> addNonNodePixelsInEdgeToNonNodeUsedSet(PixelEdge edge) {
		// mark all non-node pixels in edge as used
		List<Pixel> edgePixelList = new ArrayList<Pixel>(edge.getPixelList()
				.getList());
		edgePixelList.remove(edgePixelList.get(edgePixelList.size() - 1)); // remove
																			// last
																			// pixel
		edgePixelList.remove(0);
		usedNonNodePixelSet.addAll(edgePixelList);
		return edgePixelList;
	}

	@Deprecated
	private PixelEdge createEdge(PixelNode startNode) {
		PixelEdge edge = null;
		Pixel nextPixel = this.getNextUnusedNonNodeNeighbour(startNode);
		if (nextPixel != null) {
			edge = iterateWhile2Connected(startNode.getCentrePixel(), nextPixel);
		}
		return edge;
	}

	private Set<Pixel> createConnectedDiagonalPixelSet(int neighbours) {
		island.setDiagonal(true);
		Set<Pixel> connectedSet = new HashSet<Pixel>();
		for (Pixel pixel : pixelList) {
			pixel.clearNeighbours();
			if (pixel.isConnectedAny(island, neighbours)) {
				connectedSet.add(pixel);
			}
		}
		return connectedSet;
	}
	
//	private Set<Pixel> create2ConnectedDiagonalPixelSet() {
//		island.setDiagonal(true);
//		twoConnectedSet = new HashSet<Pixel>();
//		for (Pixel pixel : pixelList) {
//			pixel.clearNeighbours();
//			if (pixel.isConnectedAny(island, 2)) {
//				twoConnectedSet.add(pixel);
//			}
//		}
//		return twoConnectedSet;
//	}
//	
//	private Set<Pixel> create3ConnectedDiagonalPixelSet() {
//		island.setDiagonal(true);
//		threeConnectedSet = new HashSet<Pixel>();
//		for (Pixel pixel : pixelList) {
//			pixel.clearNeighbours();
//			if (pixel.isConnectedAny(island, 3)) {
//				threeConnectedSet.add(pixel);
//			}
//		}
//		return threeConnectedSet;
//	}
	
	private Set<Pixel> createMultiConnectedDiagonalPixelSet() {
		island.setDiagonal(true);
		multiConnectedSet = new HashSet<Pixel>();
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
	
//	private Set<Pixel> createConnectedDiagonalPixelSet(int neighbourCount) {
//		island.setDiagonal(true);
//		connectedSet = new HashSet<Pixel>();
//		for (Pixel pixel : pixelList) {
//			pixel.clearNeighbours();
//			if (pixel.isConnectedAny(island, neighbourCount)) {
//				connectedSet.add(pixel);
//			}
//		}
//		return connectedSet;
//	}
	
	@Deprecated
	private PixelCycle createCycle() {
		if (!checkAllAre2Connected()) {
			LOG.debug("should be only 2-connected");
		}

		Pixel last = pixelList.get(0);
		LOG.trace(last);
		usedNonNodePixelSet.add(last);
		Pixel current = last.getNeighbours(island).get(0); // arbitrary
															// direction
		PixelEdge edge = iterateWhile2Connected(last, current);
		edge.removeNodes(); // cycles don't have nodes
		PixelCycle cycle = new PixelCycle(edge);
		return cycle;
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
		PixelList neighbours = current.getNeighbours(island);
		neighbours.remove(last);
		next = neighbours.size() == 1 ? neighbours.get(0) : null;
		Long time3 = System.currentTimeMillis();
		return next;
	}

	private void add(PixelEdge edge) {
		if (!edges.contains(edge)) {
			edges.add(edge);
		}
	}

	private void add(PixelNode node) {
		if (!nodes.contains(node)) {
			nodes.add(node);
		}
	}

	private void add(PixelCycle cycle) {
		if (this.cycle != null) {
			throw new RuntimeException("Cannot add cycle twice");
		}
		this.cycle = cycle;
	}

	public PixelCycle getCycle() {
		createGraph();
		return cycle;
	}

	@Deprecated
	private PixelEdge iterateWhile2Connected(Pixel startPixel,
			Pixel currentPixel) {
		PixelEdge edge = new PixelEdge(island);
		PixelNode startNode = getPixelNode(startPixel);
		edge.addNode(startNode, 0);
		while (true) {
			Pixel nextPixel = PixelGraph.getNextUnusedInEdge(currentPixel,
					startPixel, island);
			edge.addPixel(startPixel);
			PixelNode nextNode = getPixelNode(nextPixel);
			if ((nextNode != null && nextNode != startNode)
					|| (nextPixel == null || usedNonNodePixelSet
							.contains(nextPixel))) {
				LOG.trace("nextNode: " + nextNode);
				edge.addPixel(currentPixel);
				if (nextNode != null) {
					edge.addNode(nextNode, 1);
					edge.addPixel(nextPixel);
				} else {
					LOG.trace("null next Node");
				}
				break;
			}
			startPixel = currentPixel;
			currentPixel = nextPixel;
		}
		return edge;
	}

	public PixelNode getPixelNode(Pixel pixel) {
		PixelNode node = null;
		if (pixel != null) {
			node = junctionByPixelMap.get(pixel);
			if (node == null) {
				node = terminalNodeByPixelMap.get(pixel);
			}
		}
		return node;
	}

	public JunctionSet getJunctionSet() {
		createNodesAndEdges();
		if (junctionSet == null) {
			junctionSet = new JunctionSet();
			junctionByPixelMap = new HashMap<Pixel, JunctionNode>();
			for (Pixel pixel : pixelList) {
				JunctionNode junction = JunctionNode.createJunction(pixel, island);
				if (junction != null) {
					junctionSet.add(junction);
					junctionByPixelMap.put(pixel, junction);
				}
			}
		}
		return junctionSet;
	}

	public TerminalNodeSet getTerminalNodeSet() {
		createNodesAndEdges();
		if (terminalNodeSet == null) {
			terminalNodeSet = new TerminalNodeSet();
			terminalNodeByPixelMap = new HashMap<Pixel, TerminalNode>();
			for (Pixel pixel : pixelList) {
				PixelList neighbours = pixel.getNeighbours(island);
				if (neighbours.size() == 1) {
					TerminalNode endNode = new TerminalNode(pixel,
							neighbours.get(0));
					terminalNodeSet.add(endNode);
					terminalNodeByPixelMap.put(pixel, endNode);
				}
			}
		}
		return terminalNodeSet;
	}

	@Deprecated
	private boolean checkAllAre2Connected() {
		boolean connected = true;
		for (Pixel pixel : pixelList) {
			connected = pixel.is2ConnectedAny(island);
			if (!connected) break;
		}
		return connected;
	}

	public List<PixelEdge> getEdges() {
		return edges;
	}

	public List<PixelNode> getNodes() {
		return nodes;
	}

	/**
	 * get lowest unused neighbour pixel.
	 * 
	 * iterates over neighbours to find lowest unused pixel (pixel.compareTo())
	 * 
	 * @param pixelNode
	 *            TODO
	 * @param used
	 * @param island
	 * @return
	 */
	@Deprecated
	public Pixel getNextUnusedNonNodeNeighbour(PixelNode pixelNode) {
		Pixel lowest = null;
		for (Pixel neighbour : pixelNode.centrePixel.getNeighbours(island)) {
			if (getPixelNode(neighbour) == null
					&& !usedNonNodePixelSet.contains(neighbour)) {
				if (lowest == null) {
					lowest = neighbour;
				} else if (neighbour.compareTo(lowest) < 0) {
					lowest = neighbour;
				}
			}
		}
		return lowest;
	}

	public List<JunctionNode> getNeighbourJunctions(JunctionNode junction) {
		List<JunctionNode> junctionList = new ArrayList<JunctionNode>();
		PixelList neighbours = junction.getNeighbours();
		if (neighbours != null) {
			for (Pixel neighbour : neighbours) {
				PixelNode pixelNode = getPixelNode(neighbour);
				if (pixelNode instanceof JunctionNode) {
					junctionList.add((JunctionNode) pixelNode);
				}
			}
		}
		return junctionList;
	}

	public PixelList getPixelList() {
		return pixelList;
	}

	public void createAndDrawGraph(SVGG g) {
		JunctionSet junctionSet = getJunctionSet();
		JunctionNode.drawJunctions(junctionSet, g, 2.);
		TerminalNodeSet endNodeSet = getTerminalNodeSet();
		TerminalNode.drawEndNodes(endNodeSet, g, 1.5);
		if (getNucleusSet() == null) {
			makeNucleusMap();
		}
		PixelNucleus.drawNucleusSet(getNucleusSet(), g, 5.);
	}

	public void createAndDrawGraphEdges(SVGG g) {
		JunctionSet junctionSet = getJunctionSet();
		if (junctionSet.size() > 0) {
			LOG.trace("JunctionSet: "+junctionSet);
		}
		JunctionNode.drawJunctions(junctionSet, g, 5.);
		TerminalNodeSet endNodeSet = getTerminalNodeSet();
		TerminalNode.drawEndNodes(endNodeSet, g, 3.);
		if (getNucleusSet() == null) {
			makeNucleusMap();
		}
		if (nucleusSet != null) {
			if (nucleusSet.size() > 0) {LOG.trace("NucleusSet: "+nucleusSet);}
			PixelNucleus.drawNucleusSet(nucleusSet, g, 10.);
		}
		createEdgesNew(g);
		LOG.trace("edges: "+edges.size()+ edges);
	}

	void makeNucleusMap() {
		getOrCreateNucleusSetAndMap();
		if (nucleusByPixelMap == null) {
			nucleusByPixelMap = new HashMap<Pixel, PixelNucleus>();
			for (PixelNucleus nucleus : getNucleusSet()) {
				JunctionSet junctionSet = nucleus.getJunctionSet();
				for (PixelNode pixelNode : junctionSet) {
					JunctionNode junction = (JunctionNode) pixelNode;
					for (Pixel neighbour : junction.getNeighbours()) {
						nucleusByPixelMap.put(neighbour, nucleus);
					}
					nucleusByPixelMap.put(junction.getCentrePixel(), nucleus);
				}
			}
		}
		LOG.trace("nucleusMap "+nucleusByPixelMap);
	}

	public String toString() {
		String s = "";
		s += "; edges: " + (edges == null ? "none" : edges.toString());
		s += "; nodes: " + (nodes == null ? "none" : nodes.toString());
		s += "; cycle: " + (cycle == null ? "none" : cycle.toString());
		return s;
	}

	public Set<PixelNucleus> getNucleusSet() {
		return nucleusSet;
	}

	public List<PixelNode> getPossibleRootNodes1() {
		List<PixelNode> pixelNodeList = new ArrayList<PixelNode>();
		for (PixelNode node : nodes) {
			List<PixelEdge> edgeList = node.getEdges();
			if (edgeList.size() == 1) {
				PixelEdge edge = edgeList.get(0);
				if (edge.getOrCreateSegmentedPolyline().size() == 1) {
					pixelNodeList.add(node);
				}
			}
		}
		return pixelNodeList;
	}

	public List<PixelNode> getPossibleRootNodes3() {
		List<PixelNode> pixelNodeList = new ArrayList<PixelNode>();
		for (PixelNode node : nodes) {
			List<PixelEdge> edgeList = node.getEdges();
			if (edgeList.size() == 3) {
//				if (allVectorProductsParallel(node, edgeList)) {
//					pixelNodeList.add(node);
//				}
				if (allInOneSemicircle(node, edgeList)) {
					pixelNodeList.add(node);
				}
			}
		}
		return pixelNodeList;
	}

	/** assume node in middle of 3-segment path.
	 * 
	 * @return
	 */
	public List<PixelNode> getPossibleRootNodes2() {
		List<PixelNode> pixelNodeList = new ArrayList<PixelNode>();
		PixelEdge rootEdge = null;
		PixelNode midNode = null;
		for (PixelEdge edge : edges) {
			LOG.trace(edge.getPixelNodes());
			SVGPolyline polyline = edge.getOrCreateSegmentedPolyline();
			Angle deviation = polyline.getSignedAngleOfDeviation();
			if (Math.abs(deviation.getRadian()) < 2.0) continue;
			LOG.trace("POLY "+polyline.getLineList().get(0)+"/"+polyline.getLineList().get(polyline.size() - 1)+"/"+deviation);
			if (polyline.size() == 3) {
				SVGLine midline = polyline.getLineList().get(1);
				Pixel midPixel = edge.getNearestPixelToMidPoint(midline.getMidPoint());
				midNode = new JunctionNode(midPixel, null);
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
		this.edges.add(edge);
	}

	private void removeEdgeFromNode(PixelNode node, PixelEdge edge) {
		if (node != null) {
			node.removeEdge(edge);
		}
		edges.remove(edge);
	}

	private boolean allInOneSemicircle(PixelNode node, List<PixelEdge> edgeList) {
		Vector3[] vector3 = create3Vector3s(node, edgeList);
		Angle[] angle = new Angle[3];
		for (int i = 0; i < 3; i++) {
			angle[i] = vector3[i].getAngleMadeWith(vector3[(i + 1) % 3]);
		}
		Integer ii = null;
		if (Real.isEqual(angle[0].getRadian() + angle[1].getRadian(),  angle[2].getRadian(), 0.01)) {
			ii = 2;
		} else if (Real.isEqual(angle[0].getRadian() + angle[2].getRadian(),  angle[1].getRadian(), 0.01)) {
			ii = 1;
		} else if (Real.isEqual(angle[1].getRadian() + angle[2].getRadian(),  angle[0].getRadian(), 0.01)) {
			ii = 0;
		}
		if (ii != null) {
			LOG.trace(angle[0]+"/"+angle[1]+"/"+angle[2]);
		}
		return ii != null;
	}

	private Vector3[] create3Vector3s(PixelNode node, List<PixelEdge> edgeList) {
		Real2 xy0 = new Real2(node.getCentrePixel().getInt2());
		Vector3 vector3[] = new Vector3[3];
		for (int i = 0; i < 3; i++) {
			PixelNode otherNode = edgeList.get(i).getOtherNode(node);
			Real2 otherxy = new Real2(otherNode.getCentrePixel().getInt2());
			Vector2 vector = new Vector2(otherxy.subtract(xy0));
			vector3[i] = new Vector3(vector.getX(), vector.getY(), 0.0);
		}
		return vector3;
	}

	public void addNode(PixelNode node) {
		ensureNodes();
		if (!nodes.contains(node)) {
			nodes.add(node);
		}
	}

	public void addEdge(PixelEdge edge) {
		ensureEdges();
		if (!edges.contains(edge)) {
			edges.add(edge);
			addNode(edge.getPixelNode(0));
			addNode(edge.getPixelNode(1));
		}
	}

	private void ensureNodes() {
		if (nodes == null) {
			nodes = new ArrayList<PixelNode>();
		}
	}

	private void ensureEdges() {
		if (edges == null) {
			edges = new ArrayList<PixelEdge>();
		}
	}

	public void numberTerminalNodes() {
		int i = 0;
		for (PixelNode node : getNodes()) {
			if (node instanceof TerminalNode) {
				node.setLabel("n" + i);
			}
			i++;
		}
	}

	public SVGG getSVG() {
		return svgGraph;
	}

	public SVGG drawEdgesAndNodes() {
		String[] colour = {"red", "green", "pink", "cyan", "orange", "blue", "gray", "yellow"};
		SVGG g = new SVGG();
		for (int i = 0; i < edges.size(); i++) {
			String col = colour[i % colour.length];
			PixelEdge edge = edges.get(i);
			SVGG edgeG = edge.getSVG(col);
			edgeG.setFill(col);
			g.appendChild(edgeG);
		}
		for (int i = 0; i < nodes.size(); i++) {
			String col = colour[i % colour.length];
			PixelNode node = nodes.get(i);
			SVGG nodeG = node.getSVG(1.0);
			nodeG.setStroke(col);
			nodeG.setStrokeWidth(0.3);
			nodeG.setOpacity(0.5);
			nodeG.setFill("none");
			g.appendChild(nodeG);
		}
		return g;
	}

}
