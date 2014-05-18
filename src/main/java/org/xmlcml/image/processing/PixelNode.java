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
			compare = this.centrePixel.compareTo(centrePixel1);
		}
		return compare;
	}
	
	/** get lowest unused neighbour pixel.
	 * 
	 * iterates over neighbours to find lowest unused pixel (pixel.compareTo())
	 * 
	 * @param used
	 * @param island
	 * @return
	 */
	public Pixel getNextUnusedNeighbour(Set<Pixel> used, PixelIsland island) {
		Pixel lowest = null;
		for (Pixel neighbour : centrePixel.getNeighbours(island)) {
			if (!used.contains(neighbour)) {
				if (lowest == null) {
					lowest = neighbour;
				} else if (neighbour.compareTo(lowest) < 0) {
					lowest = neighbour;
				}
			}
		}
		return lowest;
	}
	
	public String toString() {
		return centrePixel.toString();
	}

}
