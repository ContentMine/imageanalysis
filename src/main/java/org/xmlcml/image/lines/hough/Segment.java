package org.xmlcml.image.lines.hough;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Bivariate;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.Util;
import org.xmlcml.graphics.svg.SVGLine;

/**
 * line segment from Hough analysis.
 * 
 * @author pm286
 *
 */
public class Segment {

	private final static Logger LOG = Logger.getLogger(Segment.class);
	
	private Real2Array pointArray;
	private Bivariate bivariate;
	private SVGLine svgLine;
	
	// cutoff for wiggly ends
	private double sdCutoff = 2.3;
	
	public Segment() {
		pointArray = new Real2Array();
	}
	public void addPoint(Real2 point) {
		pointArray.add(point);
	}
	public Double getLength() {
		return (pointArray.size() <= 1) ? null : pointArray.get(0).getDistance(pointArray.getLastElement());
	}
	public SVGLine getSVGLine() {
		if (svgLine == null) {
			svgLine = (pointArray == null) ? null : new SVGLine(pointArray.get(0), pointArray.getLastElement());
		}
		return svgLine;
	}
	/**
	 * remove end points if they appear to curve
	 */
	public void normalize() {
		LOG.debug("P "+pointArray);
		bivariate = new Bivariate(pointArray);
		RealArray normalizedResiduals = bivariate.getNormalizedResiduals();
		Double cc = Util.format(bivariate.getCorrelationCoefficient(), 3);
		LOG.debug(Util.format(bivariate.getCorrelationCoefficient(), 3));
		pointArray = trimWigglyEnds(pointArray, normalizedResiduals);
		LOG.debug("PPP "+pointArray);
	}
	
	
	private Real2Array trimWigglyEnds(Real2Array pointArray, RealArray normalizedResiduals) {
		int lowLimit = getLowlimit(pointArray, normalizedResiduals);
		int hiLimit = getHighLimit(pointArray, normalizedResiduals);
		LOG.debug(lowLimit+":"+hiLimit);
		svgLine = null;
		RealArray xarr = pointArray.getXArray();
		RealArray yarr = pointArray.getYArray();
		Real2Array pointArray0 = new Real2Array(
			new RealArray(xarr, lowLimit+1, hiLimit-1),
			new RealArray(yarr, lowLimit+1, hiLimit-1)
			); 
		return pointArray0;
	}
	private int getHighLimit(Real2Array pointArray, RealArray normalizedResiduals) {
		int hiLimit = pointArray.size();
		for (int i = pointArray.size()-1; i >= 0; i--) {
			if (Math.abs(normalizedResiduals.elementAt(i)) < sdCutoff) {
				break;
			}
			hiLimit = i;
		}
		return hiLimit;
	}
	private int getLowlimit(Real2Array pointArray, RealArray normalizedResiduals) {
		int lowLimit = -1;
		for (int i = 0; i < pointArray.size(); i++) {
			if (Math.abs(normalizedResiduals.elementAt(i)) < sdCutoff) {
				break;
			}
			lowLimit = i;
		}
		return lowLimit;
	}
	
	public Bivariate getBivariate() {
		return bivariate;
	}

}
