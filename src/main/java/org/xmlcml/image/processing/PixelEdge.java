package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.image.compound.PixelList;
import org.xmlcml.image.lines.DouglasPeucker;

public class PixelEdge {

	private final static Logger LOG = Logger.getLogger(PixelEdge.class);
	
	private List<PixelNode> nodes;
	private PixelList pixelList; // pixels in order
	private PixelIsland island;

	private SVGPolyline polyline;

	public PixelEdge(PixelIsland island) {
		this.island = island;
		this.pixelList = new PixelList();
		this.nodes = new ArrayList<PixelNode>();
	}

	/** adds node and pixel contained within it.
	 * 
	 * @param node
	 * @param pos 0 or 1
	 */
	public void addNode(PixelNode node, int pos) {
		if (this.nodes.size() != pos) {
			LOG.error("Cannot add node");
		} else if (node == null) {
			LOG.error("Cannot add null node");
		} else {
			nodes.add(node);
			node.addEdge(this);
			LOG.trace("size "+nodes.size());
		}
	}
	
	public void addPixel(Pixel pixel) {
		pixelList.add(pixel);
	}
	
	public void addPixelList(PixelList pixelList) {
		this.pixelList.addAll(pixelList);
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

	public PixelNode getPixelNode(int i) {
		return (i < 0 || i >= nodes.size()) ? null : nodes.get(i);
	}
	
	public void removeNodes() {
		while (nodes != null && nodes.size() > 0) {
			nodes.remove(0);
		}
	}
	
	public String toString() {
		String s = ""+pixelList+"/"+nodes;
		return s;
	}

	public boolean equalsIgnoreOrder(String listString) {
		boolean equals = pixelList.toString().equals(listString);
		if (!equals) {
			PixelList newList = new PixelList(pixelList);
			newList.reverse();
			equals = newList.toString().equals(listString);
		}
		return equals;
	}

	public SVGPolyline getOrCreateSegments() {
		if (polyline == null) {
			DouglasPeucker douglasPeucker = new DouglasPeucker(2.0);
			Real2Array points = pixelList.getReal2Array();
			Real2Array pointArray = douglasPeucker.reduceToArray(points);
			polyline = new SVGPolyline(pointArray);
		}
		return polyline;
	}

	public PixelNode getOtherNode(PixelNode pixelNode) {
		if (nodes.size() != 2) {
			return null;
		} else if (nodes.get(0).equals(pixelNode)) {
			return nodes.get(1);
		} else if (nodes.get(1).equals(pixelNode)) {
			return nodes.get(0);
		} else {
			return null;
		}
	}

}
