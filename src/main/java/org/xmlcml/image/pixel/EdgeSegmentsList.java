package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.image.pixel.PixelComparator.ComparatorType;

public class EdgeSegmentsList  implements Iterable<EdgeSegments> {

	private List<EdgeSegments> list;
	private SVGG svgg;
	private PixelGraph graph;

	public EdgeSegmentsList() {
		ensureList();
	}
	
	public EdgeSegmentsList(PixelGraph graph) {
		this.graph = graph;
	}

	public Iterator<EdgeSegments> iterator() {
		ensureList();
		return list.iterator();
	}

	private void ensureList() {
		if (list == null) {
			list = new ArrayList<EdgeSegments>();
		}
	}

	public void add(EdgeSegments edgeSegments) {
		ensureList();
		list.add(edgeSegments);
	}

	public int size() {
		ensureList();
		return list.size();
	}

	public List<EdgeSegments> getList() {
		ensureList();
		return list;
	}

	/** gets edges regardless of node order.
	 * NYI
	 * @param pixelNode0
	 * @param pixelNode1
	 * @return edgeList
	 */
	public EdgeSegmentsList getEdges(PixelNode pixelNode0, PixelNode pixelNode1) {
		throw new RuntimeException("sort NYI");
//		EdgeSegmentsList edgeList = new EdgeSegmentsList(); 
//		for (EdgeSegments edge : this) {
//			Pixel nodePixel0 = edge.getPixelNode(0).getCentrePixel();
//			Pixel nodePixel1 = edge.getPixelNode(1).getCentrePixel();
//			if ( (nodePixel0.equals(pixel0) && nodePixel1.equals(pixel1)) ||
//			     (nodePixel0.equals(pixel1) && nodePixel1.equals(pixel0)) ) {
//				edgeList.add(edge);
//			}
//		}
//		return edgeList;
	}

	public EdgeSegments get(int i) {
		ensureList();
		return list.get(i);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (EdgeSegments edge : this) {
			sb.append(edge.toString());
		}
		return sb.toString();
	}

	public boolean contains(EdgeSegments edge) {
		ensureList();
		return list.contains(edge);
	}

	public boolean remove(EdgeSegments edge) {
		ensureList();
		return list.remove(edge);
	}

	public SVGElement getOrCreateSVG() {
		if (svgg == null) {
			svgg = new SVGG();
			for (EdgeSegments edge : this) {
				svgg.appendChild(edge.getOrCreateSVG());
			}
		}
		return svgg;
	}
	
	/**
	 * sorts by comparator.
	 * 
	 * only one direction
	 * 
	 */
	public void sort(ComparatorType comparatorType) {
//		Collections.sort(list, new EdgeSegmentsComparator(comparatorType));
		throw new RuntimeException("sort NYI");
	}

	
}
