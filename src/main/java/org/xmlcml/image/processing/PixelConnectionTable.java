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

/** holds PixelNodes and PixelEdges for pixelIsland
 * 
 * a graph may either be a single PixelCycle or may be a number of PixelNodes
 * and PixelEdges
 * 
 * @author pm286
 *
 */
public class PixelConnectionTable {
	
	private final static Logger LOG = Logger.getLogger(PixelConnectionTable.class);
	
	private List<PixelEdge> edges;
	private List<PixelNode> nodes;
	private PixelCycle cycle;
	private PixelList pixelList;
	private Set<Junction> junctionSet;
	private Set<TerminalNode> endNodeSet;
	private PixelIsland island;
	private Map<Pixel, Junction> junctionByPixelMap;
	private Map<Pixel, TerminalNode> endNodeByPixelMap;
//	private Set<Pixel> unused;
	private Set<Pixel> usedPixelSet;

	public PixelConnectionTable(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
	}

	public static PixelConnectionTable createConnectionTable(PixelIsland island) {
		return island == null ? null : createConnectionTable(island.getPixelList(), island);
	}
	
	public static PixelConnectionTable createConnectionTable(PixelList pixelList, PixelIsland island) {
		return new PixelConnectionTable(pixelList, island);
	}
	
	private void fillConnectionTable() {
		if (this.edges == null) {
			this.edges = new ArrayList<PixelEdge>();
			this.nodes = new ArrayList<PixelNode>();
			usedPixelSet = new HashSet<Pixel>();
			this.getEndNodeSet();
			this.getJunctionSet();
			if (pixelList.size() == 0) {
				throw new RuntimeException("no pixels in island");
			}
			if (pixelList.size() == 1) {
				// single pixel
				PixelCycle cycle = new PixelCycle(pixelList.get(0), island);
				this.add(cycle);
			} else if (this.endNodeSet.size() == 0) {
				if (this.junctionSet.size() == 0) { // a circle?
					PixelCycle cycle = this.createCycle();
					if (cycle == null) {
						throw new RuntimeException("Cannot create a single cycle");
					}
					this.add(cycle);
				} else {
					// start at arbitrary end node
					Junction start = junctionSet.iterator().next();
					this.createEdges(start);
				}
			} else {
				// start at arbitrary end node
				TerminalNode start = endNodeSet.iterator().next();
				this.createEdges(start);
			}
		}
	}

	/** take next unused neighbour as second pixel in edge
	 * 
	 * @param start
	 */
	private void createEdges(PixelNode start) {
		Pixel nextPixel = start.getNextUnusedNeighbour(usedPixelSet, island);
		createEdges(start, nextPixel);
	}

	private void createEdges(PixelNode start, Pixel nextPixel) {
		edges = new ArrayList<PixelEdge>();
		Stack<PixelNode> nodeStack = new Stack<PixelNode>();
		nodeStack.add(start);
		while (!nodeStack.isEmpty()) {
			start = nodeStack.pop();
			PixelEdge edge = createEdge(start, nextPixel);
			// mark all pixels in edge as used
			usedPixelSet.addAll(edge.getPixelList().getList());
			PixelNode last = edge.getPixelNodes().get(1);
			nextPixel = last.getNextUnusedNeighbour(usedPixelSet, island);
			if (nextPixel != null) {
				nodeStack.push(last);
			}
		}
	}

	private PixelEdge createEdge(PixelNode startNode, Pixel nextPixel) {
		PixelEdge edge = iterateWhile2Connected(startNode.getCentrePixel(), nextPixel);
		return edge;
	}

	private PixelCycle createCycle() {
		if (!checkAllAre2Connected()) {
			throw new RuntimeException("should be only 2-connected");
		}
		
		Pixel last = pixelList.get(0);
		LOG.trace(last);
		usedPixelSet.add(last);
		Pixel current = last.getNeighbours(island).get(0); // arbitrary direction
		PixelEdge edge = iterateWhile2Connected(last, current);
		PixelCycle cycle = new PixelCycle(edge);
		return cycle;
	}

	/** gets next pixel in chain.
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

	private PixelEdge iterateWhile2Connected(Pixel last, Pixel current) {
		PixelEdge edge = new PixelEdge(island);
		while (true) {
			usedPixelSet.add(current);
			Pixel nextPixel = PixelConnectionTable.getNextUnusedInEdge(current, last, island);
			LOG.debug("current "+current+" next "+nextPixel+"/"+usedPixelSet+"/");
			edge.addPixel(last);
			PixelNode nextNode = getNode(nextPixel);
			if (nextNode != null) {
				LOG.debug("next: "+nextNode);
			} else if (nextPixel == null || usedPixelSet.contains(nextPixel)) {
				edge.addPixel(current);
				break;
			}
			last = current;
			current = nextPixel;
		}
		return edge;
	}

	private PixelNode getNode(Pixel pixel) {
		PixelNode node = junctionByPixelMap.get(pixel);
		if (node == null) {
			node = endNodeByPixelMap.get(pixel);
		}
		return node;
	}

	public Set<Junction> getJunctionSet() {
		fillConnectionTable();
		if (junctionSet == null) {
			junctionSet = new HashSet<Junction>();
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

	public Set<TerminalNode> getEndNodeSet() {
		fillConnectionTable();
		if (endNodeSet == null) {
			endNodeSet = new HashSet<TerminalNode>();
			endNodeByPixelMap = new HashMap<Pixel, TerminalNode>();
			for (Pixel pixel : pixelList) {
				PixelList neighbours = pixel.getNeighbours(island);
				if (neighbours.size() == 1) {
					TerminalNode endNode = new TerminalNode(pixel, neighbours.get(0));
					endNodeSet.add(endNode);
					endNodeByPixelMap.put(pixel, endNode);
				}
			}
		}
		return endNodeSet;
	}
	
	private boolean checkAllAre2Connected() {
		boolean connected = true;
		for (Pixel pixel : pixelList) {
			PixelList neighbours = pixel.getNeighbours(island);
			if (neighbours.size() != 2) {
				connected = false;
				LOG.error("not a single cycle "+pixel+"/"+neighbours);
			}
		}
		return connected;
	}

	public String toString() {
		String s = "";
		s += "; edges: "+(edges == null ? "none" : edges.toString());
		s += "; nodes: "+(nodes == null ? "none" : nodes.toString());
		s += "; cycle: "+(cycle == null ? "none" : cycle.toString());
		return s;
	}
	

	
}
