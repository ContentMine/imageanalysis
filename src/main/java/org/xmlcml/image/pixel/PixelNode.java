package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;

public abstract class PixelNode implements Comparable<PixelNode> {

	Pixel centrePixel; // pixel 1
	private List<PixelEdge> edgeList;
	private String label;

	protected PixelNode() {
	}
	
	protected PixelNode(Pixel pixel) {
		this.centrePixel = pixel;
	}
	
	public Pixel getCentrePixel() {
		return centrePixel;
	}

	/** compare Y values of centrePixels then X.
	 * if Y values are equal compare X
	 * 
	 */
	public int compareTo(PixelNode node1) {
		int compare = -1;
		if (node1 != null) {
			Pixel centrePixel1 = node1.getCentrePixel();
			compare = this.centrePixel.compareTo(centrePixel1);
		}
		return compare;
	}
	
	public String toString() {
		getCentrePixel();
		return (centrePixel == null) ? "?" : String.valueOf(centrePixel);
	}

	public PixelList getDiagonalNeighbours(PixelIsland island) {
		return centrePixel.getDiagonalNeighbours(island);
	}

	public PixelList getOrthogonalNeighbours(PixelIsland island) {
		return centrePixel.getOrthogonalNeighbours(island);
	}

	public void addEdge(PixelEdge pixelEdge) {
		ensureEdgeList();
		this.edgeList.add(pixelEdge);
	}

	private void ensureEdgeList() {
		if (edgeList == null) {
			edgeList = new ArrayList<PixelEdge>();
		}
	}

	public List<PixelEdge> getEdges() {
		ensureEdgeList();
		return edgeList;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public boolean removeEdge(PixelEdge edge) {
		return edgeList.remove(edge);
	}

	public SVGG createSVG(double rad) {
		SVGG g = new SVGG();
		SVGCircle circle = new SVGCircle(new Real2(centrePixel.getInt2()).plus(new Real2(0.5, 0.5)), rad);
		g.appendChild(circle);
		circle.setFill("none");
		return g;
	}
}
