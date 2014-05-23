package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.image.compound.PixelList;

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
	private PixelIsland island;
	private Map<Pixel, Junction> junctionByPixelMap;
	private Map<Pixel, TerminalNode> terminalNodeByPixelMap;
	private Set<Pixel> usedNonNodePixelSet;
	private SortedNodeSet activeNodeSet;
	private Map<Junction, PixelNucleus> nucleusByJunctionMap;
	private Set<PixelNucleus> nucleusSet;

	private static long timecounter0;
	private static long timecounter1;
	private static long timecounter2;

	public PixelGraph(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
	}

	/**
	 * creates graph and fills it.
	 * 
	 * @param island
	 * @return
	 */
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
	 */
	public static PixelGraph createGraph(PixelList pixelList, PixelIsland island) {
		PixelGraph graph = new PixelGraph(pixelList, island);
		LOG.trace("before graph");
		graph.createGraph();
		LOG.trace("after graph");
		return graph;
	}

	private void createGraph() {
		if (edges == null) {
			edges = new ArrayList<PixelEdge>();
			nodes = new ArrayList<PixelNode>();
			usedNonNodePixelSet = new HashSet<Pixel>();
			getTerminalNodeSet();
			getJunctionSet();
			createPixelNuclei();
			LOG.trace("pixelList: " + pixelList.size());
			removeExtraneousPixelsFromNuclei();
			LOG.trace("pixelList: " + pixelList.size());
			createPixelNuclei(); // recompute with thinner graph
			LOG.trace("junctions " + junctionSet.size());
			removeExtraneousJunctionsFromNuclei();
			LOG.trace("junctions " + junctionSet.size());
			if (pixelList.size() == 0) {
				throw new RuntimeException("no pixels in island");
			}
			if (pixelList.size() == 1) {
				// single pixel - conventionally a cycle of 1
				PixelCycle cycle = new PixelCycle(pixelList.get(0), island);
				this.add(cycle);
			} else if (this.terminalNodeSet.size() == 0) {
				if (this.junctionSet.size() == 0) { // a circle?
					PixelCycle cycle = this.createCycle();
					if (cycle == null) {
						throw new RuntimeException(
								"Cannot create a single cycle");
					}
					this.add(cycle);
				} else {
					// start at arbitrary end node
					Junction start = (Junction) junctionSet.iterator().next();
					this.createEdges(start);
				}
			} else {
				// start at arbitrary end node
				TerminalNode start = (TerminalNode) terminalNodeSet.iterator()
						.next();
				this.createEdges(start);
			}
		}
	}

	private void removeExtraneousPixelsFromNuclei() {
		for (PixelNucleus nucleus : nucleusSet) {
			nucleus.removeExtraneousPixels();
		}
		junctionSet = null;
		getJunctionSet();
	}

	private void removeExtraneousJunctionsFromNuclei() {
		for (PixelNucleus nucleus : nucleusSet) {
			List<Junction> junctions = nucleus.removeExtraneousJunctions();
			junctionSet.removeAll(junctions);
		}
	}

	/**
	 * similar to floodfill
	 * 
	 * add locally connected nodes.
	 */
	private Map<Junction, PixelNucleus> createPixelNuclei() {
		nucleusByJunctionMap = new HashMap<Junction, PixelNucleus>();
		Set<PixelNode> unusedNodes = new HashSet<PixelNode>();
		nucleusSet = new HashSet<PixelNucleus>();
		unusedNodes.addAll(junctionSet.getList());
		while (!unusedNodes.isEmpty()) {
			LOG.trace("unused " + unusedNodes.size());
			// new nucleus, find next unused Junction
			Junction nextNode = (Junction) unusedNodes.iterator().next();
			unusedNodes.remove(nextNode);
			Set<Junction> nucleusNodeSet = new HashSet<Junction>();
			nucleusNodeSet.add(nextNode);
			PixelNucleus nucleus = new PixelNucleus(island);
			nucleusSet.add(nucleus);
			while (!nucleusNodeSet.isEmpty()) {
				LOG.trace("nucleus " + nucleusNodeSet.size() + " "
						+ nucleusNodeSet);
				nextNode = nucleusNodeSet.iterator().next();
				nucleus.add(nextNode);
				nucleusByJunctionMap.put(nextNode, nucleus);
				nucleusNodeSet.remove(nextNode);
				List<Junction> neighbourJunctions = getNeighbourJunctions(nextNode);
				for (Junction neighbourJunction : neighbourJunctions) {
					if (!nucleus.contains(neighbourJunction)) {
						nucleusNodeSet.add(neighbourJunction);
						unusedNodes.remove(neighbourJunction);
					}
				}
			}
		}
		return nucleusByJunctionMap;
	}

	public Map<Junction, PixelNucleus> getNucleusByJunctionMap() {
		return nucleusByJunctionMap;
	}

	/**
	 * take next unused neighbour as second pixel in edge
	 * 
	 * @param start
	 *            // seems to be surperfluous
	 */
	private void createEdges(PixelNode start) {
		this.getNextUnusedNonNodeNeighbour(start);
		createEdges();
	}

	private void createEdges() {
		edges = new ArrayList<PixelEdge>();
		activeNodeSet = new SortedNodeSet();
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
				throw new RuntimeException("BUG duplicate edge");
			}
			lastEdge = edge;

			add(edge);
			addNonNodePixelsInEdgeToNonNodeUsedSet(edge);
			removeEndNodesIfNoUnusedNeighbours(edge);
		}
	}

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

	private PixelEdge createEdge(PixelNode startNode) {
		PixelEdge edge = null;
		Pixel nextPixel = this.getNextUnusedNonNodeNeighbour(startNode);
		if (nextPixel != null) {
			edge = iterateWhile2Connected(startNode.getCentrePixel(), nextPixel);
		}
		return edge;
	}

	private PixelCycle createCycle() {
		if (!checkAllAre2Connected()) {
			throw new RuntimeException("should be only 2-connected");
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
		Long time0 = System.currentTimeMillis();
		PixelList neighbours = current.getNeighbours(island);
		Long time1 = System.currentTimeMillis();
		neighbours.remove(last);
		Long time2 = System.currentTimeMillis();
		timecounter0 += (time1 - time0);
		timecounter1 += (time2 - time1);
		next = neighbours.size() == 1 ? neighbours.get(0) : null;
		Long time3 = System.currentTimeMillis();
		timecounter2 += (time3 - time2);
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

	private PixelEdge iterateWhile2Connected(Pixel startPixel,
			Pixel currentPixel) {
		PixelEdge edge = new PixelEdge(island);
		PixelNode startNode = getPixelNode(startPixel);
		edge.addStartNode(startNode);
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
					edge.addEndNode(nextNode);
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
		createGraph();
		if (junctionSet == null) {
			junctionSet = new JunctionSet();
			junctionByPixelMap = new HashMap<Pixel, Junction>();
			for (Pixel pixel : pixelList) {
				Junction junction = Junction.createJunction(pixel, island);
				if (junction != null) {
					junctionSet.add(junction);
					junctionByPixelMap.put(pixel, junction);
				}
			}
		}
		return junctionSet;
	}

	public TerminalNodeSet getTerminalNodeSet() {
		createGraph();
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

	private boolean checkAllAre2Connected() {
		boolean connected = true;
		for (Pixel pixel : pixelList) {
			PixelList neighbours = pixel.getNeighbours(island);
			if (neighbours.size() != 2) {
				connected = false;
				LOG.error("not a single cycle " + pixel + "/" + neighbours);
			}
		}
		return connected;
	}

	public List<PixelEdge> getEdges() {
		return edges;
	}

	public List<PixelNode> getNodes() {
		return nodes;
	}

	public String toString() {
		String s = "";
		s += "; edges: " + (edges == null ? "none" : edges.toString());
		s += "; nodes: " + (nodes == null ? "none" : nodes.toString());
		s += "; cycle: " + (cycle == null ? "none" : cycle.toString());
		return s;
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

	public List<Junction> getNeighbourJunctions(Junction junction) {
		List<Junction> junctionList = new ArrayList<Junction>();
		PixelList neighbours = junction.getNeighbours();
		if (neighbours != null) {
			for (Pixel neighbour : neighbours) {
				PixelNode pixelNode = getPixelNode(neighbour);
				if (pixelNode instanceof Junction) {
					junctionList.add((Junction) pixelNode);
				}
			}
		}
		return junctionList;
	}

	public PixelList getPixelList() {
		return pixelList;
	}

	public Set<PixelNucleus> getNucleusSet() {
		return nucleusSet;
	}

	public void createAndDrawGraph(SVGG g) {
		JunctionSet junctionSet = getJunctionSet();
		Junction.drawJunctions(junctionSet, g);
		TerminalNodeSet endNodeSet = getTerminalNodeSet();
		TerminalNode.drawEndNodes(endNodeSet, g);
		Set<PixelNucleus> nucleusSet = getNucleusSet();
		PixelNucleus.drawSet(nucleusSet, g);
	}

}
