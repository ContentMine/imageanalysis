package org.xmlcml.image.pixel;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;

/** end of pixel path.
 * 
 * Node is only 1-connected.
 * 
 * @author pm286
 *
 */
@Deprecated
public class TerminalNode extends PixelNode {

	private final static Logger LOG = Logger.getLogger(TerminalNode.class);

	private Pixel neighbour;
	
	public TerminalNode() {
	}

	public TerminalNode(Pixel pixel, Pixel neighbour) {
		super(pixel, null);
		this.neighbour = neighbour;
	}

	public static void drawEndNodes(TerminalNodeSet endNodeSet, SVGG g, double rad) {
		for (PixelNode endNode : endNodeSet) {
			SVGCircle circle = new SVGCircle(new Real2(endNode.getCentrePixel().getInt2()).plus(new Real2(0.5, 0.5)), rad);
			circle.setOpacity(0.4);
			circle.setFill("orange");
			g.appendChild(circle);
		}
	}

	public Pixel getNeighbour() {
		return neighbour;
	}
	

}
