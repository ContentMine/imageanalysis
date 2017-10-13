package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.image.geom.DouglasPeucker;

/** a list of PixelSegments.
 * 
 * Generally created from a path using Douglas Peucker segmentation.
 * 
 * @author pm286
 *
 */
public class EdgeSegments implements List<PixelSegment> {

	private final static Logger LOG = Logger.getLogger(EdgeSegments.class);
	private List<PixelSegment> segmentList;
	private Real2Array real2Array;
	private List<SVGLine> svgLineList;
	private SVGG g;
	
	public EdgeSegments() {
		super();
	}

	public EdgeSegments(List<PixelSegment> segmentList) {
		super();
		this.segmentList = segmentList;
	}
	
	public EdgeSegments(Real2Array pointArray) {
		ensureSegmentList();
		if (pointArray == null) {
			throw new RuntimeException("Null pointArray");
		}
		for (int i = 0; i < pointArray.size() - 1; i++) {
			PixelSegment segment = new PixelSegment(pointArray.get(i), pointArray.get(i+1));
			this.add(segment);
		}
	}

	public List<PixelSegment> getSegmentList() {
		return segmentList;
	}

	public void setSegmentList(List<PixelSegment> segmentList) {
		this.segmentList = segmentList;
	}

	private void ensureSegmentList() {
		if (segmentList == null) {
			segmentList = new ArrayList<PixelSegment>();
		}
	}
	

	/**
	 * @return
	 * @see java.util.List#size()
	 */
	public int size() {
		ensureSegmentList();
		return segmentList.size();
	}

	/**
	 * @return
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		ensureSegmentList();
		return segmentList.isEmpty();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		ensureSegmentList();
		return segmentList.contains(o);
	}

	/**
	 * @return
	 * @see java.util.List#iterator()
	 */
	public Iterator<PixelSegment> iterator() {
		ensureSegmentList();
		return segmentList.iterator();
	}

	/**
	 * @return
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		ensureSegmentList();
		return segmentList.toArray();
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		ensureSegmentList();
		return segmentList.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(PixelSegment e) {
		ensureSegmentList();
		return segmentList.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		ensureSegmentList();
		return segmentList.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		ensureSegmentList();
		return segmentList.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends PixelSegment> c) {
		ensureSegmentList();
		return segmentList.addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends PixelSegment> c) {
		ensureSegmentList();
		return segmentList.addAll(index, c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		ensureSegmentList();
		return segmentList.removeAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		ensureSegmentList();
		return segmentList.retainAll(c);
	}

	/**
	 * 
	 * @see java.util.List#clear()
	 */
	public void clear() {
		ensureSegmentList();
		segmentList.clear();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		ensureSegmentList();
		return segmentList.equals(o);
	}

	/**
	 * @return
	 * @see java.util.List#hashCode()
	 */
	public int hashCode() {
		ensureSegmentList();
		return segmentList.hashCode();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public PixelSegment get(int index) {
		ensureSegmentList();
		return segmentList.get(index);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public PixelSegment set(int index, PixelSegment element) {
		ensureSegmentList();
		return segmentList.set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, PixelSegment element) {
		ensureSegmentList();
		segmentList.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public PixelSegment remove(int index) {
		ensureSegmentList();
		return segmentList.remove(index);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		ensureSegmentList();
		return segmentList.indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		ensureSegmentList();
		return segmentList.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<PixelSegment> listIterator() {
		ensureSegmentList();
		return segmentList.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<PixelSegment> listIterator(int index) {
		ensureSegmentList();
		return segmentList.listIterator(index);
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public List<PixelSegment> subList(int fromIndex, int toIndex) {
		ensureSegmentList();
		return segmentList.subList(fromIndex, toIndex);
	}

	@Override
	public String toString() {
		ensureSegmentList();
		return "PixelSegmentList [segmentList=" + segmentList + "]";
	}

	public Real2Array getReal2Array() {
		ensureSegmentList();
		if (real2Array == null) {
			real2Array = new Real2Array();
			if (segmentList.size() > 0) {
				for (int i = 0; i < segmentList.size(); i++) {
					Real2 point = segmentList.get(i).getPoint(0);
					real2Array.add(point);
				}
				real2Array.add(segmentList.get(segmentList.size() - 1).getPoint(1));
			}
		}
		return real2Array;
	}

	public Angle getSignedAngleOfDeviation() {
		Angle angle = new Angle(0.0);
		if (size() > 1) {
			Line2 line0 = this.get(0).getEuclidLine();
			Line2 line1 = this.get(size() - 1).getEuclidLine();
			angle = line0.getAngleMadeWith(line1);
		}
		angle.setRange(Angle.Range.SIGNED);
		return angle;
	}

	public PixelSegment getLast() {
		return size() == 0 ? null : get(size() - 1);
	}

	public void setStroke(String stroke) {
		ensureSegmentList();
		for (PixelSegment segment : this) {
			segment.setStroke(stroke);
		}
	}

	public void setWidth(double lineWidth) {
		ensureSegmentList();
		for (PixelSegment segment : this) {
			segment.setWidth(lineWidth);
		}
	}

	public void setFill(String fill) {
		ensureSegmentList();
		for (PixelSegment segment : this) {
			segment.setFill(fill);
		}
	}

	public SVGG getOrCreateSVG() {
		getSVGLineList();
		if (g == null) {
			g = new SVGG();
			g.setStrokeWidth(1.0); // alter later
			for (SVGLine line : svgLineList) {
				g.appendChild(line.copy());
			}
		}
		return g;
	}

	public static EdgeSegments createSegmentList(PixelList pixelList, double tolerance) {
		EdgeSegments segmentList = null;
		if (pixelList != null) {
			Real2Array points = pixelList.getReal2Array();
			DouglasPeucker douglasPeucker = new DouglasPeucker(tolerance);
			Real2Array newPoints = douglasPeucker.reduceToArray(points);
			segmentList = new EdgeSegments(newPoints);
		}
		return segmentList;
	}

	public List<SVGLine> getSVGLineList() {
		if (svgLineList == null) {
			svgLineList = new ArrayList<SVGLine>();
			if (segmentList != null) {
				for (PixelSegment segment : segmentList) {
					svgLineList.add(segment.getSVGLine());
				}
			}
		}
		return svgLineList;
	}

	public SVGG getSVGG() {
		SVGG g = new SVGG();
		for (PixelSegment segment : segmentList) {
			g.appendChild(segment.getSVGLine());
		}
		return g;
	}
}
