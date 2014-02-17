package org.xmlcml.image.lines.hough.old;

import org.xmlcml.euclid.Angle;

import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealArray;

/** 
 * calculates the perpendicular distances from the origin to a set of lines through a point.
 * 
 * Through a point P are calculated lines at regular angular intervals. For each the signed
 * perpendicular distance is calculated.
 * 
 * @author pm286
 *
 */
public class HoughDistanceCalculator {

	private Real2 point;
	private Angle startAngle;
	private Angle stepSize;
	private int angleCount;
	private RealArray distanceArray;
	private static final Real2 ORIGIN = new Real2(0.0, 0.0);

	public HoughDistanceCalculator() {
		
	}
	public void setPoint(Real2 point) {
		this.point = point;
	}
	public void setStartAngle(Angle angle) {
		this.startAngle = angle;
		
	}
	public void setStepSize(Angle angle) {
		this.stepSize = angle;
	}
	public void setAngleCount(int angleCount) {
		this.angleCount = angleCount;
	}
	public RealArray getDistanceArray() {
		return distanceArray;
	}

	public RealArray calculateDistanceArray() {
		distanceArray = new RealArray();
		Angle angle = startAngle;
		for (int i = 0; i < angleCount; i++) {
			Double dist = Real.normalize(getPerpendicularDistance(angle, point), 1);
			distanceArray.addElement(dist);
			angle = angle.plus(stepSize);
		}
		return distanceArray;
	}
	private Double getPerpendicularDistance(Angle angle, Real2 point) {
		Line2 line = createLine(angle, point);
		return line.getSignedDistanceFromPoint(ORIGIN);
	}
	private Line2 createLine(Angle angle, Real2 point) {
		Real2 offset = new Real2(angle.cos(), angle.sin());
		return new Line2(point, point.plus(offset));
	}
	
}
