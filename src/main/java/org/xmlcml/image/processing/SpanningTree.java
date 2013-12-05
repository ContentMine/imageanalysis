package org.xmlcml.image.processing;

import java.util.List;

import org.xmlcml.euclid.Int2;
import org.xmlcml.image.processing.Pixel.Marked;

/** spanning tree based on contiguous pixels.
 * 
 * Developed for use on skeletonized (thinned) PixelIslands
 * 
 * @author pm286
 *
 */
public class SpanningTree {

	private PixelIsland island;

	public SpanningTree(PixelIsland island) {
		this.island = island;
		markIsland(Pixel.Marked.UNUSED);
	}

	private void markIsland(Marked unused) {
		for (Pixel pixel : island.getPixelList()) {
			
		}
	}

	public void start(Pixel pixel) {
		Pixel currentPixel = pixel;
		List<Pixel> unusedNeighbours = currentPixel.getNeighbours(Pixel.Marked.UNUSED);
	}
}
