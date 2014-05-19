package org.xmlcml.image.processing;

import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.image.compound.PixelList;

/** end of pixel path.
 * 
 * Node is only 1-connected.
 * 
 * @author pm286
 *
 */
public class TerminalNode extends PixelNode {

	private final static Logger LOG = Logger.getLogger(TerminalNode.class);

	private Pixel neighbour;
	
	public TerminalNode() {
		
	}

	public TerminalNode(Pixel node, Pixel neighbour) {
		super(node);
		this.neighbour = neighbour;
	}

	public static void drawEndNodes(TerminalNodeSet endNodeSet, SVGG g) {
		for (PixelNode endNode : endNodeSet) {
			SVGCircle circle = new SVGCircle(new Real2(endNode.getCentrePixel().getInt2()).plus(new Real2(0.5, 0.5)), 3.);
			circle.setOpacity(0.4);
			circle.setFill("orange");
			g.appendChild(circle);
		}
	}

	public Pixel getNeighbour() {
		return neighbour;
	}
	

}
