package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.image.compound.PixelList;

public class PixelEdge {

	private final static Logger LOG = Logger.getLogger(PixelEdge.class);
	
	private List<PixelNode> nodes;
	private PixelList pixelList; // pixels in order
	private PixelIsland island;

	public PixelEdge(PixelIsland island) {
		this.island = island;
		this.pixelList = new PixelList();
		this.nodes = new ArrayList<PixelNode>();
	}

	/** adds start node and pixel contained within it.
	 * 
	 * @param node
	 */
	public void addStartNode(PixelNode node) {
		if (this.nodes.size() != 0) {
			throw new RuntimeException("Cannot add start node");
		}
		nodes.add(node);
	}
	
	/** adds endNode and pixel contained within it.
	 * 
	 * @param node
	 */
	public void addEndNode(PixelNode node) {
		if (this.nodes.size() == 0 || this.nodes.size() > 1) {
			throw new RuntimeException("Cannot add end node");
		}
		nodes.add(node);
	}
	
	public void addPixel(Pixel pixel) {
		pixelList.add(pixel);
	}
	
	public PixelList getPixelList() {
		return pixelList;
	}
	
	/** gets pixelNodes at end of edge.
	 * 
	 * normally 2; but for single cycles there are no nodes.
	 * 
	 * @return
	 */
	public List<PixelNode> getPixelNodes() {
		return nodes;
	}
	
	/** gets pixel from list.
	 * 
	 * @param i
	 * @return null if no list or i is outside range
	 */
	public Pixel get(int i) {
		return pixelList.size() == 0 ? null : pixelList.get(i);
	}
}
