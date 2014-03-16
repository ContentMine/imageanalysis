package org.xmlcml.image;

import georegression.struct.point.Point2D_I32;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.image.lines.DouglasPeucker;

import boofcv.alg.filter.binary.Contour;


/** wraps a Boofcv contour and provides display.
 * 
 * @author pm286
 *
 */
public class AMIContour {

	private Contour contour;
	private Real2Array externalPoints;
	private List<Real2Array> internalPointsList;
	private DouglasPeucker douglasPeucker;

	public AMIContour(Contour contour) {
		this.contour = contour;
		init();
	}
	
	public AMIContour(List<Real2> reduced) {
		externalPoints = new Real2Array(reduced);
		internalPointsList = new ArrayList<Real2Array>(); // empty list
	}

	private void init() {
		this.externalPoints = createReal2Points(contour.external);
		this.internalPointsList = new ArrayList<Real2Array>();
		for (List<Point2D_I32> pointsList : contour.internal) {
			Real2Array realPoints = createReal2Points(pointsList);
			internalPointsList.add(realPoints);
		}
	}

	private Real2Array createReal2Points(List<Point2D_I32> points) {
		Real2Array realPoints = new Real2Array();
		for (Point2D_I32 p : points) {
			realPoints.add(new Real2(p.x, p.y));
		}
		return realPoints;
	}
	
	public AMIContour reduceExternal(double tolerance) {
		douglasPeucker = new DouglasPeucker(tolerance);
		List<Real2> reduced = reduce(externalPoints);
		AMIContour amiContour = new AMIContour(reduced);
		return amiContour;
	}

	/** create a list of the internal contours.
	 * 
	 * this may not be the best way to do this as we separate inner and outer.
	 * 
	 * @param tolerance
	 * @return
	 */
	public List<AMIContour> reduceInternal(double tolerance) {
		douglasPeucker = new DouglasPeucker(tolerance);
		List<AMIContour> amiContourList = new ArrayList<AMIContour>();
		for (Real2Array points : internalPointsList) {
			AMIContour amiContour = new AMIContour(points.getList());
			amiContourList.add(amiContour);
		}
		return amiContourList;
	}

	private List<Real2> reduce(Real2Array real2Array) {
		List<Real2> points = real2Array.getList();
		points = douglasPeucker.reduce(points);
		return points;
	}

	public Real2Array getExternalPoints() {
		return externalPoints;
	}

	public List<Real2Array> getInternalPointsList() {
		return internalPointsList;
	}

	public SVGG createExternalContourSVG(String color, double width) {
		SVGG gg = new SVGG();
		int size = externalPoints.size();
		for (int i = 0; i < size; i++) {
			int j = (i+1) % size;
			Real2 pi = getExternalPoints().get(i);
			Real2 pj = getExternalPoints().get(j);
			SVGLine line = new SVGLine(pi, pj);
			line.setStroke(color);
			line.setWidth(width);
			gg.appendChild(line);
		}
		return gg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("external: "+externalPoints.size()+"; internal contours "+internalPointsList.size()+" ");
		for (Real2Array internal : internalPointsList) {
			sb.append(internal.size());
		}
		return sb.toString();
	}
}
