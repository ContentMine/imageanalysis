package org.xmlcml.image.processing;

import java.util.Set;

import org.xmlcml.euclid.Int2;

public abstract class PixelNode implements Comparable<PixelNode> {

	private Pixel centrePixel; // pixel 1

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
			Int2 xy21 = (centrePixel1 == null) ? null : centrePixel1.getInt2();
			Int2 xy20 = (centrePixel == null) ? null : centrePixel.getInt2();
			compare = new Integer(xy20.getY()).compareTo(new Integer(xy21.getY()));
			if (compare == 0) {
				compare = new Integer(xy20.getX()).compareTo(new Integer(xy21.getX()));
			}
		}
		return compare;
	}
	
	public Pixel getNextUnusedNeighbour(Set<Pixel> unused, PixelIsland island) {
		for (Pixel neighbour : centrePixel.getNeighbours(island)) {
			if (!unused.contains(neighbour)) {
				return neighbour;
			}
		}
		return null;
	}
	
	public String toString() {
		return centrePixel.toString();
	}

}
