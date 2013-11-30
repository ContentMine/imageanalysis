package org.xmlcml.image.lines.hough;

import org.xmlcml.euclid.Bivariate;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGLine;

/**
 * line segment from Hough analysis.
 * 
 * @author pm286
 *
 */
public class Segment {

	private Real2Array pointArray;
	
	public Segment() {
		pointArray = new Real2Array();
	}
	public void addPoint(Real2 point) {
		pointArray.add(point);
	}
	public Double getLength() {
		return (pointArray.size() <= 1) ? null : pointArray.get(0).getDistance(pointArray.getLastPoint());
	}
	public SVGLine getSVGLine() {
		return (pointArray == null) ? null : new SVGLine(pointArray.get(0), pointArray.getLastElement());
	}
	/**
	 * remove end points if they appear to curve
	 */
	public void normalize() {
		Bivariate bivariate = new Bivariate(pointArray);
		RealArray residuals = bivariate.getNormalizedResiduals();
		System.out.println(bivariate.getCorrelationCoefficient()+" "+residuals+" ");
	}

}
