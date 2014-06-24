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
	private PixelIsland island;
	private PixelEdge edge;

	/** special case of single pixel.
	 * 
	 * @param pixel
	 */
	public PixelCycle(Pixel pixel, PixelIsland island) {
		this.edge = new PixelEdge(island);
		TerminalNode node = new TerminalNode(pixel, null);
		edge.addNode(node, 0);
		edge.addNode(node, 1);
	}

	/** special case of single pixel.
	 * 
	 * @param pixel
	 */
	public PixelCycle(PixelEdge edge) {
		this.edge = edge;
	}

	public Pixel getOrigin() {
		return this.edge.get(0);
	}
	
	public PixelEdge getEdge() {
		return edge;
	}
	
	public String toString() {
		String s = "origin: "+getOrigin().toString();
		return s;
	}

}
