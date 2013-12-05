package org.xmlcml.image.processing;

import java.util.List;

import org.xmlcml.euclid.Int2;

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
	}

	public void start(Int2 coord) {
		Pixel currentPixel = island.pixelByCoordMap.get(coord);
		List<Pixel> unusedNeighbours = currentPixel.getNeighbours(Pixel.Marked.UNUSED);
	}
}
