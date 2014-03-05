package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
	private Stack<Pixel> stack;

	private HashMap<Pixel, List<Pixel>> listByPixelMap;

	public SpanningTree(PixelIsland island) {
		this.island = island;
		markIsland(Pixel.Marked.UNUSED);
	}
	
	private void markIsland(Marked unused) {
		for (Pixel pixel : island.getPixelList()) {
			pixel.getNeighbours(island);
			LOG.trace("Neigh: "+pixel.getNeighbours(island));
		}
		for (Pixel pixel : island.getPixelList()) {
			pixel.setMarked(Marked.UNUSED);
		}
	}

	public void start(Pixel start) {
		stack = new Stack<Pixel>();
		listByPixelMap = new HashMap<Pixel, List<Pixel>>();
		
		processNode(start);
	}

	private void processNode(Pixel start) {
		addPixelToStack(start);
		
		while (!stack.isEmpty()) {
			Pixel pixel = stack.pop();
			pixel.setMarked(Marked.USED);
			LOG.trace("popped: "+pixel.getInt2());
			
			List<Pixel> neighbours = pixel.getNeighbours(Pixel.Marked.UNUSED);
			if (neighbours.size() == 0) {
				processTerminalNode(pixel);
			} else if (neighbours.size() == 1) {
				// continue down list
				continueDownListTillNode(neighbours.get(0));
			} else {
				addNeighboursToStack(neighbours);
			}
		}
	}

	private void addNeighboursToStack(List<Pixel> neighbours) {
		for (Pixel neighbour : neighbours) {
			addPixelToStack(neighbour);
		}
	}

	private void continueDownListTillNode(Pixel pixel) {
		LOG.trace("2connected "+pixel.getInt2());
		List<Pixel> neighbours = new ArrayList<Pixel>();
		while (pixel != null) {
			pixel.setMarked(Marked.USED);
			LOG.trace("connecting: "+pixel.getInt2());
			neighbours = pixel.getNeighbours(Pixel.Marked.UNUSED);
			if (neighbours.size() != 1) {
				break;
			}
			pixel = neighbours.get(0);
		}
		addNeighboursToStack(neighbours);
	}

	private void processTerminalNode(Pixel pixel) {
		LOG.debug("terminal NYI: "+pixel.getInt2());
	}

	private void addPixelToStack(Pixel pixel) {
		stack.add(pixel);
		List<Pixel> list = new ArrayList<Pixel>();
		listByPixelMap.put(pixel, list);
	}
}
