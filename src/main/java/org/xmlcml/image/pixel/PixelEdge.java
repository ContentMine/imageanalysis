package org.xmlcml.image.pixel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.image.geom.DouglasPeucker;

public class PixelEdge {

	private final static Logger LOG = Logger.getLogger(PixelEdge.class);
	
	private static Pattern EDGE_PATTERN = Pattern.compile("\\{([^\\}]*)\\}\\/\\[([^\\]]*)\\]");

	private PixelNodeList nodes;
	private PixelList pixelList; // pixels in order
	private PixelIsland island;
	private PixelSegmentList segmentList;
	private String id;
	private PixelGraph pixelGraph;
	private SVGG svgg;

	public PixelEdge(PixelIsland island) {
		this.island = island;
		this.pixelList = new PixelList();
		this.nodes = new PixelNodeList();
	}

	public PixelEdge(PixelGraph pixelGraph) {
		this.pixelGraph = pixelGraph;
	}

	/** adds node and pixel contained within it.
	 * 
	 * @param node
	 * @param pos 0 or 1
	 */
	public void addNode(PixelNode node, int pos) {
		ensureNodes();
		if (this.nodes.size() != pos) {
			LOG.trace("Cannot add node");
		} else if (node == null) {
			LOG.trace("Cannot add null node");
		} else {
			nodes.add(node);
			node.addEdge(this);
			LOG.trace("size "+nodes.size());
		}
	}
	
	private void ensureNodes() {
		if (nodes == null) {
			nodes = new PixelNodeList();
		}
	}

	public void addPixel(Pixel pixel) {
		ensurePixelList();
		pixelList.add(pixel);
	}
	
	private void ensurePixelList() {
		if (pixelList == null) {
			pixelList = new PixelList();
		}
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
	public PixelNodeList getNodes() {
		return nodes;
	}
	
	/** gets pixel from list.
	 * 
	 * @param i
	 * @return null if no list or i is outside range
	 */
	public Pixel get(int i) {
		return (pixelList == null || size() == 0 || i < 0 || i >= size()) ? null : pixelList.get(i);
	}
	
	public Pixel getFirst() {
		return get(0);
	}

	public Pixel getLast() {
		return get(size() - 1);
	}
	
	public int size() {
		return (pixelList == null) ? 0 : pixelList.size();
	}

	public PixelNode getPixelNode(int i) {
		return (nodes == null || i < 0 || i >= nodes.size()) ? null : nodes.get(i);
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

	public PixelSegmentList getOrCreateSegmentList(double tolerance) {
		if (segmentList == null) {
			DouglasPeucker douglasPeucker = new DouglasPeucker(tolerance);
			Real2Array points = pixelList.getReal2Array();
			Real2Array pointArray = douglasPeucker.reduceToArray(points);
			segmentList = new PixelSegmentList(pointArray);
		}
		return segmentList;
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

	public Pixel getNearestPixelToMidPoint(Real2 midPoint) {
		Pixel midPixel = null;
		double distMin = Double.MAX_VALUE;
		for (Pixel pixel :pixelList) {
			if (midPixel == null) {
				midPixel = pixel;
			} else {
				Real2 xy = new Real2(pixel.getInt2());
				double dist = midPoint.getDistance(xy);
				if (dist < distMin) {
					distMin = dist;
					midPixel = pixel;
				}
			}
		}
		return midPixel;
	}

	public SVGG createPixelSVG(String colour) {
		SVGG g = new SVGG();
		for (Pixel pixel : pixelList) {
			SVGRect rect = pixel.getSVGRect(1, colour);
			g.appendChild(rect);
		}
		return g;
	}

	/** some edges are zero or one pixels and return to same node.
	 * 
	 * (Algorithm needs mending)
	 * 
	 * examples:
	 * {(19,48)}/[(19,48), (19,48)]
	 * {(22,36)}/[(23,36), (23,36)]
	 * {(29,30)}/[(29,31), (29,31)]
	 * {(29,29)}/[(29,31), (29,31)]
	 * 
	 * 
	 * @return
	 */
	public boolean isZeroCircular() {
		boolean circular = false;
		if (nodes.size() == 0) {
			circular = pixelList.size() <= 1;
		} else if (nodes.size() == 2) {
			circular = pixelList.size() <= 1;
		}
		return circular;
	}

	public SVGG createLineSVG() {
		SVGG g = new SVGG();
		if (getFirst() != null && getLast() != null) {
			Real2 firstXY = new Real2(getFirst().getInt2()).plus(new Real2(0.5, 0.5));
			Real2 lastXY = new Real2(getLast().getInt2()).plus(new Real2(0.5, 0.5));
			SVGLine line = new SVGLine(firstXY, lastXY);
			line.setWidth(0.5);
			g.appendChild(line);
		}
		return g;
	}

	public Real2 getMidPoint() {
		Pixel first = getFirst();
		Real2 firstXY = first == null ? null : new Real2(first.getInt2());
		Pixel last = getLast();
		Real2 lastXY = last == null ? null : new Real2(last.getInt2());
		Real2 mid = (lastXY == null || firstXY == null) ? null : firstXY.getMidPoint(lastXY);
		return mid;
	}

	public Pixel getNearestPixelToMidPoint() {
		Real2 midPoint = getMidPoint();
		return midPoint == null ? null : getNearestPixelToMidPoint(midPoint);
		
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/** creates edge from string representation.
	 * 
	 * {(2,0)(1,0)(0,1)(-1,2)(0,3)(0,4)}/[(2,0)(0,4)]
	 * 
	 * @param edge
	 * @return
	 */
	public static PixelEdge createEdge(String edgeS, PixelIsland island) {
		PixelEdge edge = null;
		if (edgeS != null) {
			Matcher matcher = EDGE_PATTERN.matcher(edgeS);
			if (matcher.matches()) {
				edge = new PixelEdge(island);
				String pixelListS = matcher.group(1);
				edge.pixelList = PixelList.createPixelList(pixelListS, island);
				LOG.trace("pixelList "+edge.pixelList);
				String nodeListS = matcher.group(2);
				edge.nodes = PixelNodeList.createNodeList(nodeListS, island);
				LOG.trace("nodeList "+edge.nodes);
			}
		}
		return edge;
	}

	public SVGG getOrCreateSVG() {
		if (svgg == null) {
			svgg = new SVGG();
			svgg.appendChild(pixelList.plotPixels("blue"));
		}
		return svgg;
	}
}
