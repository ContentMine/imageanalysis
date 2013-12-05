package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.xmlcml.image.processing.Pixel.Marked;

/** spanning tree based on contiguous pixels.
 * 
 * Developed for use on skeletonized (thinned) PixelIslands
 * 
 * @author pm286
 *
 */
public class SpanningTree {

	private final static Logger LOG = Logger.getLogger(SpanningTree.class);
	
	private PixelIsland island;
	private Queue<Pixel> queue;

	private HashMap<Pixel, List<Pixel>> listByPixelMap;

	public SpanningTree(PixelIsland island) {
		this.island = island;
		markIsland(Pixel.Marked.UNUSED);
	}
	
	private void markIsland(Marked unused) {
		for (Pixel pixel : island.getPixelList()) {
			pixel.setMarked(Marked.UNUSED);
		}
	}

	public void start(Pixel start) {
		queue = new LinkedList<Pixel>();
		listByPixelMap = new HashMap<Pixel, List<Pixel>>();
		
		addPixelToQueue(start);
		
		while (!queue.isEmpty()) {
			Pixel pixel = queue.remove();
			pixel.setMarked(Marked.USED);
			
			List<Pixel> list = listByPixelMap.get(pixel);
			LOG.debug(pixel.getInt2());
			
			List<Pixel> neighbours = pixel.getNeighbours(Pixel.Marked.UNUSED);
			if (neighbours.size() == 0) {
				// finished
			}
				
//FIXME
			for (Pixel neighbour : neighbours) {
				queue.add(neighbour);
			}
		}
	}

	private void addPixelToQueue(Pixel start) {
		queue.add(start);
		List list = new ArrayList<Pixel>();
		listByPixelMap.put(start, list);
	}
}
