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
public class EndNode extends PixelNode {

	private final static Logger LOG = Logger.getLogger(EndNode.class);

	private Pixel node; // pixel 1
	private Pixel neighbour;
	
	public EndNode() {
		
	}

	public EndNode(Pixel node, Pixel neighbour) {
		this.node = node;
		this.neighbour = neighbour;
	}

	public static void drawEndNodes(Set<EndNode> endNodeSet, SVGG g) {
		for (EndNode endNode : endNodeSet) {
			SVGCircle circle = new SVGCircle(new Real2(endNode.getNode().getInt2()).plus(new Real2(0.5, 0.5)), 3.);
			circle.setOpacity(0.4);
			circle.setFill("orange");
			g.appendChild(circle);
		}
	}

	public Pixel getNode() {
		return node;
	}

	public Pixel getNeighbour() {
		return neighbour;
	}
	

}
