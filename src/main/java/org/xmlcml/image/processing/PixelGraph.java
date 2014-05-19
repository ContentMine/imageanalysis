package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
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

	private final static Logger LOG = Logger
			.getLogger(PixelGraph.class);

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

	public PixelGraph(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
	}

	/**
	 * creates table and fills it.
	 * 
	 * @param island
	 * @return
	 */
	public static PixelGraph createGraph(PixelIsland island) {
		return island == null ? null : createConnectionTable(
				island.getPixelList(), island);
	}

	/**
	 * creates table and fills it.
	 * 
	 * @param pixelList
	 * @param island
	 * @return
	 */
	public static PixelGraph createConnectionTable(
			PixelList pixelList, PixelIsland island) {
		PixelGraph table = new PixelGraph(pixelList, island);
		table.fillConnectionTable();
		return table;
	}

	private void fillConnectionTable() {
		if (this.edges == null) {
			this.edges = new ArrayList<PixelEdge>();
			this.nodes = new ArrayList<PixelNode>();
			usedNonNodePixelSet = new HashSet<Pixel>();
			this.getTerminalNodeSet();
			this.getJunctionSet();
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

	/**
	 * take next unused neighbour as second pixel in edge
	 * 
	 * @param start // seems to be surperfluous
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
		LOG.debug(terminalNodeSet+"\n"+junctionSet+"\n"+activeNodeSet);
		PixelEdge lastEdge = null;
		while (!activeNodeSet.isEmpty()) {
			PixelNode startNode = activeNodeSet.iterator().next();
			LOG.trace("used0: " + usedNonNodePixelSet);
			PixelEdge edge = createEdge(startNode);
			if (edge == null) {
				LOG.trace("null edge from: " + startNode);
				activeNodeSet.remove(startNode);
				continue;
			} else if (lastEdge != null && edge.toString().equals(lastEdge.toString())) {
				throw new RuntimeException("BUG duplicate edge");
			}
			lastEdge = edge;
			add(edge);
			LOG.debug(">" + edge);
			LOG.trace("nodeSet: " + activeNodeSet);
			addNonNodePixelsInEdgeToNonNodeUsedSet(edge);
			removeEndNodesIfNoUnusedNeighbours(edge);
			LOG.trace("usedNonNodePixels: " + usedNonNodePixelSet);
		}
	}

	private void removeEndNodesIfNoUnusedNeighbours(PixelEdge edge) {
		List<PixelNode> nodes = edge.getPixelNodes();
		for (PixelNode node : nodes) {
			if (node == null) {
				throw new RuntimeException("BUG null node: "+nodes.size());
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
	static Pixel getNextUnusedInEdge(Pixel current, Pixel last, PixelIsland island) {
		Pixel next = null;
//		if (current != null) {
			PixelList neighbours = current.getNeighbours(island);
			neighbours.remove(last);
			next = neighbours.size() == 1 ? neighbours.get(0) : null;
//		}
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
		fillConnectionTable();
		return cycle;
	}

	private PixelEdge iterateWhile2Connected(Pixel startPixel, Pixel currentPixel) {
		PixelEdge edge = new PixelEdge(island);
		PixelNode startNode = getPixelNode(startPixel);
		edge.addStartNode(startNode);
		LOG.debug("*** startNode: " + startNode+" current "+currentPixel);
		while (true) {
			Pixel nextPixel = PixelGraph.getNextUnusedInEdge(currentPixel,
					startPixel, island);
			LOG.trace("current " + currentPixel + " next " + nextPixel + "/"
					+ usedNonNodePixelSet + "/");
			edge.addPixel(startPixel);
			PixelNode nextNode = getPixelNode(nextPixel);
			if ((nextNode != null && nextNode != startNode) ||
				(nextPixel == null || usedNonNodePixelSet.contains(nextPixel))) {
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
		LOG.debug("***");
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
		fillConnectionTable();
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
		fillConnectionTable();
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

	/** get lowest unused neighbour pixel.
	 * 
	 * iterates over neighbours to find lowest unused pixel (pixel.compareTo())
	 * 
	 * @param pixelNode TODO
	 * @param used
	 * @param island
	 * @return
	 */
	public Pixel getNextUnusedNonNodeNeighbour(PixelNode pixelNode) {
		Pixel lowest = null;
		for (Pixel neighbour : pixelNode.centrePixel.getNeighbours(island)) {
			if (getPixelNode(neighbour) == null && !usedNonNodePixelSet.contains(neighbour)) {
				if (lowest == null) {
					lowest = neighbour;
				} else if (neighbour.compareTo(lowest) < 0) {
					lowest = neighbour;
				}
			}
		}
		return lowest;
	}

}
