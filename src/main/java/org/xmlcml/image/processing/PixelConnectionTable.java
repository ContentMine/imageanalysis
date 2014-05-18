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
public class PixelConnectionTable {

	private final static Logger LOG = Logger
			.getLogger(PixelConnectionTable.class);

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

	private Set<PixelNode> activeNodeSet;

	public PixelConnectionTable(PixelList pixelList, PixelIsland island) {
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
	public static PixelConnectionTable createConnectionTable(PixelIsland island) {
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
	public static PixelConnectionTable createConnectionTable(
			PixelList pixelList, PixelIsland island) {
		PixelConnectionTable table = new PixelConnectionTable(pixelList, island);
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
	 * @param start
	 */
	private void createEdges(PixelNode start) {
		Pixel nextPixel = start.getNextUnusedNeighbour(usedNonNodePixelSet, island);
		createEdges();
	}

	private void createEdges() {
		edges = new ArrayList<PixelEdge>();
		activeNodeSet = new HashSet<PixelNode>();
		activeNodeSet.addAll(terminalNodeSet.getList());
		activeNodeSet.addAll(junctionSet.getList());
		while (!activeNodeSet.isEmpty()) {
			PixelNode startNode = activeNodeSet.iterator().next();
			LOG.debug("used0: " + usedNonNodePixelSet);
			PixelEdge edge = createEdge(startNode);
			if (edge == null) {
				LOG.debug("null edge from: "+startNode);
				activeNodeSet.remove(startNode);
				continue;
			}
			add(edge);
			LOG.debug(edge);
			LOG.debug("nodeSet: " + activeNodeSet);
			addNonNodePixelsInEdgeToNonNodeUsedSet(edge);
			removeEndNodesIfNoUnusedNeighbours(edge);
			LOG.debug("usedNonNodePixels: " + usedNonNodePixelSet);
		}
	}

	private void removeEndNodesIfNoUnusedNeighbours(PixelEdge edge) {
		List<PixelNode> nodes = edge.getPixelNodes();
		for (PixelNode node : nodes) {
			if (node.getNextUnusedNeighbour(usedNonNodePixelSet, island) == null) {
				LOG.debug("inactivated node: "+node);
				activeNodeSet.remove(node);
			}
		}
	}

	private List<Pixel> addNonNodePixelsInEdgeToNonNodeUsedSet(PixelEdge edge) {
		// mark all non-node pixels in edge as used
		List<Pixel> edgePixelList = new ArrayList<Pixel>(edge.getPixelList().getList());
		edgePixelList.remove(edgePixelList.get(edgePixelList.size()-1)); // remove last pixel
		edgePixelList.remove(0);
		usedNonNodePixelSet.addAll(edgePixelList);
		return edgePixelList;
	}

	private PixelEdge createEdge(PixelNode startNode) {
		PixelEdge edge = null;
		Pixel nextPixel = startNode.getNextUnusedNeighbour(usedNonNodePixelSet, island);
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
		PixelList neighbours = current.getNeighbours(island);
		neighbours.remove(last);
		Pixel next = neighbours.size() == 1 ? neighbours.get(0) : null;
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

	private PixelEdge iterateWhile2Connected(Pixel start, Pixel current) {
		PixelEdge edge = new PixelEdge(island);
		PixelNode startNode = getPixelNode(start);
		edge.addStartNode(startNode);;
		LOG.debug("startNode: "+startNode);
		PixelNode lastNode = startNode;
		while (true) {
//			usedNonNodePixelSet.add(current); // don't add end pixel
			Pixel nextPixel = PixelConnectionTable.getNextUnusedInEdge(current,
					start, island);
			LOG.trace("current " + current + " next " + nextPixel + "/"
					+ usedNonNodePixelSet + "/");
			edge.addPixel(start);
			PixelNode nextNode = getPixelNode(nextPixel);
			if (nextNode != null && nextNode != startNode) {
				LOG.debug("nextNode: " + nextNode);
//				break;
			} else if (nextPixel == null || usedNonNodePixelSet.contains(nextPixel)) {
				edge.addPixel(current);
				edge.addEndNode(lastNode);
				break;
			}
			start = current;
			current = nextPixel;
			lastNode = nextNode;
		}
		return edge;
	}

	public PixelNode getPixelNode(Pixel pixel) {
		PixelNode node = junctionByPixelMap.get(pixel);
		if (node == null) {
			node = terminalNodeByPixelMap.get(pixel);
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

}
