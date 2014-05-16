package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	private List<PixelEdge> edges;
	private List<PixelNode> nodes;
	private PixelCycle cycle;
	private PixelList pixelList;
	private Set<Junction> junctionSet;
	private Set<EndNode> endNodeSet;
	private PixelIsland island;

	public PixelConnectionTable(PixelList pixelList, PixelIsland island) {
		if (pixelList == null) {
			throw new RuntimeException("null pixelList");
		}
		this.pixelList = pixelList;
		this.island = island;
		init();
	}

	private void init() {
		this.edges = new ArrayList<PixelEdge>();
		this.nodes = new ArrayList<PixelNode>();
	}
	
	public static PixelConnectionTable createConnectionTable(PixelIsland island) {
		return island == null ? null : createConnectionTable(island.getPixelList(), island);
	}
	
	public static PixelConnectionTable createConnectionTable(PixelList pixelList, PixelIsland island) {
		return new PixelConnectionTable(pixelList, island);
	}
	
//	public void add(PixelEdge edge) {
//		if (!edges.contains(edge)) {
//			edges.add(edge);
//		}
//	}
//	
//	public void add(PixelNode node) {
//		if (!nodes.contains(node)) {
//			nodes.add(node);
//		}
//	}
	
	public void add(PixelCycle cycle) {
		if (this.cycle != null) {
			throw new RuntimeException("Cannot add cycle twice");
		}
		this.cycle = cycle;
	}

	public PixelCycle getCycle() {
		return cycle;
	}

	public void makeTable(PixelNode start) {
		
	}
	
	public String toString() {
		String s = edges.toString();
		s += nodes.toString();
		s += cycle;
		return s;
	}

	public PixelCycle createCycle(PixelIsland island) {
		PixelCycle cycle = new PixelCycle(pixelList, island);
		processCycle(cycle);
		return cycle;
	}

	void processCycle(PixelCycle pixelCycle) {
		pixelCycle.checkAllAre2Connected();
		
		Set<Pixel> unused = new HashSet<Pixel>();
		unused.addAll(pixelCycle.pixelList.getList());
		Pixel last = pixelCycle.origin;
		PixelCycle.LOG.trace(last);
		unused.remove(last);
		pixelCycle.cycleList = new PixelList();
		pixelCycle.cycleList.add(last);
		Pixel current = last.getNeighbours(pixelCycle.island).get(0); // arbitrary direction
		iterateWhile2Connected(pixelCycle, unused, last, current);
		PixelCycle.LOG.trace(pixelCycle.cycleList+"/"+unused);
		if (unused.size() > 0) {
			throw new RuntimeException("Not a simple cycle , did not process: "+unused);
		}
	}

	private void iterateWhile2Connected(PixelCycle pixelCycle, Set<Pixel> unused,
			Pixel last, Pixel current) {
		while (true) {
			unused.remove(current);
			pixelCycle.cycleList.add(current);
			Pixel next = PixelCycle.getNextUnusedInEdge(current, last, pixelCycle.island);
			PixelCycle.LOG.trace("current "+current+" next "+next+"/"+unused+"/"+pixelCycle.cycleList);
			if (next == null || !unused.contains(next) || isNode(next)) {
				break;
			}
			last = current;
			current = next;
		}
	}

	private boolean isNode(Pixel next) {
		throw new RuntimeException("NYI");
	}

	public Set<Junction> findJunctions() {
		if (junctionSet == null) {
			junctionSet = new HashSet<Junction>();
			for (Pixel pixel : pixelList) {
				Junction junction = Junction.createJunction(pixel, island);
				if (junction != null) {
					junctionSet.add(junction);
				}
			}
		}
		return junctionSet;
	}

	public Set<EndNode> findEndNodes() {
		if (endNodeSet == null) {
			endNodeSet = new HashSet<EndNode>();
			for (Pixel pixel : pixelList) {
				PixelList neighbours = pixel.getNeighbours(island);
				if (neighbours.size() == 1) {
					EndNode endNode = new EndNode(pixel, neighbours.get(0));
					endNodeSet.add(endNode);
				}
			}
		}
		return endNodeSet;
	}
	
	public void fillConnectionTable() {
		this.endNodeSet = this.findEndNodes();
		this.junctionSet = this.findJunctions();
		if (pixelList.size() == 0) {
			throw new RuntimeException("no pixels in island");
		}
		if (pixelList.size() == 1) {
			// single pixel
			PixelCycle cycle = new PixelCycle(pixelList.get(0));
			this.add(cycle);
		} else if (this.endNodeSet.size() == 0) {
			if (this.junctionSet.size() == 0) { // a circle?
				PixelCycle cycle = this.createCycle(island);
				if (cycle == null) {
					throw new RuntimeException("Cannot create a single cycle");
				}
				this.add(cycle);
			} else {
				// find a node to start with
				this.makeTable(this.junctionSet
						.iterator().next());
			}
		} else {
			// start at arbitrary end node
			this.makeTable(this.endNodeSet.iterator()
					.next());
		}
	}

	
}
