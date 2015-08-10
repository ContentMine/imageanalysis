package org.xmlcml.image.pixel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.image.geom.DouglasPeucker;

public class PixelEdge {

	private final static Logger LOG = Logger.getLogger(PixelEdge.class);
	
	private static Pattern EDGE_PATTERN = Pattern.compile("\\{([^\\}]*)\\}\\/\\[([^\\]]*)\\]");

	private PixelNodeList nodeList;
	private PixelList pixelList; // pixels in order
	private PixelIsland island;
	private PixelSegmentList segmentList;
	private String id;
	private PixelGraph pixelGraph;
	private SVGG svgg;

	private Int2Range boundingBox;

	public PixelEdge(PixelIsland island) {
		this.island = island;
		this.pixelList = new PixelList();
		this.nodeList = new PixelNodeList();
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
		if (this.nodeList.size() != pos) {
			LOG.trace("Cannot add node");
		} else if (node == null) {
			LOG.trace("Cannot add null node");
		} else {
			nodeList.add(node);
			node.addEdge(this);
			LOG.trace("size "+nodeList.size());
		}
	}
	
	private void ensureNodes() {
		if (nodeList == null) {
			nodeList = new PixelNodeList();
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
		return nodeList;
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
		return (nodeList == null || i < 0 || i >= nodeList.size()) ? null : nodeList.get(i);
	}
	
	public void removeNodes() {
		while (nodeList != null && nodeList.size() > 0) {
			nodeList.remove(0);
		}
	}
	
	public String toString() {
		String s = "pixelList: "+pixelList+"; nodeList: "+nodeList;
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
		return getOrCreateSegmentList(tolerance, null, null);
	}
	
	public PixelSegmentList getOrCreateSegmentList(double tolerance, Integer cornerFindingWindow, Double relativeCornernessThresholdForCornerAggregation) {
		if (segmentList == null) {
			boolean improvedDouglasPeucker = cornerFindingWindow != null && relativeCornernessThresholdForCornerAggregation != null;
			DouglasPeucker douglasPeucker = (improvedDouglasPeucker ? new DouglasPeucker(tolerance, cornerFindingWindow, relativeCornernessThresholdForCornerAggregation) : new DouglasPeucker(tolerance));
			Real2Array points = pixelList.getReal2Array();
			if (nodeList == null || nodeList.size() != 2) {
				throw new RuntimeException("segmentation requires 2 nodes");
			}
			boolean isCyclic = nodeList.get(0).getInt2().equals(nodeList.get(1).getInt2());
			Real2Array pointArray = douglasPeucker.reduceToArray(points);
			if (isCyclic) {
				Real2 point0 = pointArray.get(0);
				pointArray.setElement(pointArray.size() - 1, new Real2(point0));
			}
			LOG.trace(pointArray);
			segmentList = new PixelSegmentList(pointArray);
		}
		return segmentList;
	}

	public PixelNode getOtherNode(PixelNode pixelNode) {
		if (nodeList.size() != 2) {
			return null;
		} else if (nodeList.get(0).equals(pixelNode)) {
			return nodeList.get(1);
		} else if (nodeList.get(1).equals(pixelNode)) {
			return nodeList.get(0);
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
		if (nodeList.size() == 0) {
			circular = pixelList.size() <= 1;
		} else if (nodeList.size() == 2) {
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
				edge.nodeList = PixelNodeList.createNodeList(nodeListS, island);
				LOG.trace("nodeList "+edge.nodeList);
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
	
	public Int2Range getInt2BoundingBox() {
		if (boundingBox == null) {
			boundingBox = pixelList == null ? null : pixelList.getIntBoundingBox();
		}
		return boundingBox;
	}

	public Pixel getClosestPixel(Real2 point) {
		return (pixelList == null || pixelList.size() == 0) ? null : pixelList.getClosestPixel(point);
	}

	public boolean removeNode(PixelNode node) {
		return nodeList.remove(node);
	}

}
