package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.image.compound.PixelList;

/** a cyclic set of pixels without defined nodes.
 * 
 * Might have corners and bends (e.g. be rounded box, square )
 * A single pixel is arbitrarily defined as a cycle rather than a Node
 * 
 * A pixel is arbitrarily selected as origin pixel. It can be reset later if there is special symmetry (NYI)
 * 
 * @author pm286
 *
 */
public class PixelCycle {

	final static Logger LOG = Logger.getLogger(PixelCycle.class);
	Pixel origin;
	PixelIsland island;
	PixelList cycleList;
	PixelList pixelList;

	/** special case of single pixel.
	 * 
	 * @param pixel
	 */
	public PixelCycle(Pixel pixel) {
		this.origin = pixel;
		init();
	}

	private void init() {
		this.cycleList = new PixelList();
	}

	public PixelCycle(PixelList pixelList, PixelIsland island) {
		this.pixelList = pixelList;
		this.island = island;
		this.origin = pixelList.get(0);
		init();
	}

	void checkAllAre2Connected() {
		for (Pixel pixel : pixelList) {
			PixelList neighbours = pixel.getNeighbours(island);
				if (neighbours.size() != 2) {
				throw new RuntimeException("not a single cycle "+pixel+"/"+neighbours);
			}
		}
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
	
	public PixelList getCycleList() {
		return cycleList;
	}
	
	public Pixel getOrigin() {
		return this.origin;
	}
	
	public String toString() {
		String s = "origin: "+origin.toString();
		s += "; cycle: "+cycleList;
		return s;
	}

}
