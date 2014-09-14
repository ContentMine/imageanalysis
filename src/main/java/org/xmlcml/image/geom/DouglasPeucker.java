package org.xmlcml.image.geom;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;

/**
 * Reduces the number of points in a shape using the Douglas-Peucker algorithm. <br>
 * From:
 * http://www.phpriot.com/articles/reducing-map-path-douglas-peucker-algorithm/4<br>
 * Ported from PHP to Java. "marked" array added to optimize.
 * 
 * @author M.Kergall
 */
public class DouglasPeucker {

	private double tolerance;
	private boolean[] marked;
	private List<Real2> shape;
	private List<Real2> newShape;
	private double maxDeviation;
	private int indexOfMaxDeviation;

	public DouglasPeucker(double tolerance) {
		this.tolerance = tolerance;
	}
	/**
	 * Reduce the number of points in a shape using the Douglas-Peucker
	 * algorithm
	 * 
	 * @param shape
	 *            The shape to reduce
	 * @return the reduced shape
	 */
	public List<Real2> reduce(List<Real2> shape) {
		this.shape = shape;
		int n = shape.size();
		if (n < 3) {
			return shape;
		}

		marked = new boolean[n];
											// marked as "true"
		for (int i = 1; i < n - 1; i++) {
			marked[i] = false;
		}
		// first and last points
		marked[0] = true;
		marked[n - 1] = true;

		douglasPeuckerReduction(0, n - 1);

		newShape = createNewShapeFromMarked();
		return newShape;
	}
	
	private List<Real2> createNewShapeFromMarked() {
		newShape = new ArrayList<Real2>(); 
		for (int i = 0; i < shape.size(); i++) {
			if (marked[i]) {
				newShape.add(shape.get(i));
			}
		}
		return newShape;
	}

	/**
	 * Reduce the points in shape between the specified first and last index.
	 * Mark the points to keep in marked[]
	 * 
//	 * @param shape
//	 *            The original shape
//	 * @param marked
//	 *            The points to keep (marked as true)
//	 * @param tolerance
//	 *            The tolerance to determine if a point is kept
	 * @param firstIdx
	 *            The index in original shape's point of the starting point for
	 *            this line segment
	 * @param lastIdx
	 *            The index in original shape's point of the ending point for
	 *            this line segment
	 */
	private void douglasPeuckerReduction(int firstIdx, int lastIdx) {
		// overlapping indexes
		if (lastIdx <= firstIdx + 1) {
			return;
		}

		int idxMax = findMaximallyDeviatingPoint(shape, firstIdx, lastIdx);

		if (maxDeviation > tolerance) {
			marked[indexOfMaxDeviation] = true;
			douglasPeuckerReduction(firstIdx, idxMax);
			douglasPeuckerReduction(idxMax, lastIdx);
		}
	}
	private void douglasPeuckerReductionOld(int firstIdx, int lastIdx) {
		// overlapping indexes
		if (lastIdx <= firstIdx + 1) {
			return;
		}

		findMaximallyDeviatingPoint(shape, firstIdx, lastIdx);

		if (maxDeviation > tolerance) {
			marked[indexOfMaxDeviation] = true;
			douglasPeuckerReduction(firstIdx, indexOfMaxDeviation);
			douglasPeuckerReduction(indexOfMaxDeviation, lastIdx);
		}
	}
	private int findMaximallyDeviatingPoint(List<Real2> shape, int firstIdx, int lastIdx) {
		maxDeviation = 0.0;
		indexOfMaxDeviation = 0;

		Real2 firstPoint = shape.get(firstIdx);
		Real2 lastPoint = shape.get(lastIdx);

		for (int idx = firstIdx + 1; idx < lastIdx; idx++) {
			Real2 point = shape.get(idx);

			double distance = orthogonalDistance(point, firstPoint, lastPoint);
			// the point with the greatest distance
			if (distance > maxDeviation) {
				maxDeviation = distance;
				indexOfMaxDeviation = idx;
			}
		}
		return indexOfMaxDeviation;
	}

	/**
	 * Calculate the orthogonal distance from the line joining the lineStart and
	 * lineEnd points to point
	 * 
	 * @param point
	 * @param lineStart
	 * @param lineEnd
	 * @return distance
	 */
	private double orthogonalDistance(Real2 point, Real2 lineStart,
			Real2 lineEnd) {
		double area = Math.abs(
				(lineStart.getY() * lineEnd.getX() 
				+ lineEnd.getY() * point.getX() 
				+ point.getY() * lineStart.getX() 
				- lineEnd.getY() * lineStart.getX()
				- point.getY() * lineEnd.getX() 
				- lineStart.getY() * point.getX()
				) / 2.0);

		double bottom = Math.hypot(
				lineStart.getY() - lineEnd.getY(),
				lineStart.getX() - lineEnd.getX());

		return (area / bottom * 2.0);
	}
	public Real2Array reduceToArray(Real2Array real2Array) {
		 List<Real2> real2List = reduce(real2Array.getList());
		return new Real2Array(real2List);
	}
}
