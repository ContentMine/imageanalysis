package org.xmlcml.image.processing;

import java.util.Set;

public abstract class PixelNode {

	private Pixel centrePixel; // pixel 1

	protected PixelNode() {
		
	}
	
	protected PixelNode(Pixel pixel) {
		this.centrePixel = pixel;
	}
	
	public Pixel getCentrePixel() {
		return centrePixel;
	}

	public Pixel getNextUnusedNeighbour(Set<Pixel> unused, PixelIsland island) {
		for (Pixel neighbour : centrePixel.getNeighbours(island)) {
			if (!unused.contains(neighbour)) {
				return neighbour;
			}
		}
		return null;
	}

}
