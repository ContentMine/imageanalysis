package util;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/** from
 * http://code.google.com/p/savitzky-golay-filter/source/browse/trunk/src/mr/go/sgfilter/RamerDouglasPeuckerFilter.java?r=15
 * Licence unclear
 * @author pm286
 *
 */

/**
 * Filters data using Ramer-Douglas-Peucker algorithm with specified tolerance
 * 
 * @author Rze&zacute;nik
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm">Ramer-Douglas-Peucker
 *      algorithm</a>
 */
public class RamerDouglasPeucker {

	private double epsilon;

	/**
	 * 
	 * @param epsilon
	 *            epsilon in Ramer-Douglas-Peucker algorithm (maximum distance
	 *            of a point in data between original curve and simplified
	 *            curve)
	 * @throws IllegalArgumentException
	 *             when {@code epsilon <= 0}
	 */
	public RamerDouglasPeucker(double epsilon) {
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon nust be > 0");
		}
		this.epsilon = epsilon;
	}

	public double[] filter(double[] data) {
		return ramerDouglasPeuckerFunction(data, 0, data.length - 1);
	}

	/**
	 * 
	 * @return {@code epsilon}
	 */
	public double getEpsilon() {
		return epsilon;
	}

	protected double[] ramerDouglasPeuckerFunction(double[] points,
			int startIndex, int endIndex) {
		double dmax = 0;
		int idx = 0;
		double a = endIndex - startIndex;
		double b = points[endIndex] - points[startIndex];
		double c = -(b * startIndex - a * points[startIndex]);
		double norm = sqrt(pow(a, 2) + pow(b, 2));
		for (int i = startIndex + 1; i < endIndex; i++) {
			double distance = abs(b * i - a * points[i] + c) / norm;
			if (distance > dmax) {
				idx = i;
				dmax = distance;
			}
		}
		if (dmax >= epsilon) {
			double[] recursiveResult1 = ramerDouglasPeuckerFunction(points,
					startIndex, idx);
			double[] recursiveResult2 = ramerDouglasPeuckerFunction(points,
					idx, endIndex);
			double[] result = new double[(recursiveResult1.length - 1)
					+ recursiveResult2.length];
			System.arraycopy(recursiveResult1, 0, result, 0,
					recursiveResult1.length - 1);
			System.arraycopy(recursiveResult2, 0, result,
					recursiveResult1.length - 1, recursiveResult2.length);
			return result;
		} else {
			return new double[] { points[startIndex], points[endIndex] };
		}
	}

	/**
	 * 
	 * @param epsilon
	 *            maximum distance of a point in data between original curve and
	 *            simplified curve
	 */
	public void setEpsilon(double epsilon) {
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon nust be > 0");
		}
		this.epsilon = epsilon;
	}
	
	public static void main(String[] args) {
		double epsilon = 0.16;
		RamerDouglasPeucker rdp = new RamerDouglasPeucker(epsilon);
		double[] data = new double[] {
				0.1, 0.11,
				0.5, 0.55,
				0.95, 0.9,
				1.3, 1.4
		};
		double[] data1 = rdp.filter(data);
		for (int i = 0; i < data1.length; i++) {
//			System.out.println(data1[i]);
		}
	}

}
