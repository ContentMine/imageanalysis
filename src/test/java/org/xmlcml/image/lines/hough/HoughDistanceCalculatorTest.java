package org.xmlcml.image.lines.hough;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealArray;

public class HoughDistanceCalculatorTest {

	private static final Logger LOG = Logger.getLogger(HoughDistanceCalculatorTest.class);

	@Test
	/** used for perpendicular in Hough.
	 * 
	 * modelled on Wikipedia https://en.wikipedia.org/wiki/Hough_transform#example 2013-11-18
	 * Their angle is rotated 90 degrees and distance is opposite sign
	 * @throws IOException
	 */
	public void testPointData() throws IOException {
		HoughDistanceCalculator distanceCalculator = new HoughDistanceCalculator();
		distanceCalculator.setPoint(new Real2(40.0, 70.0));
		distanceCalculator.setStartAngle(new Angle(0.0, Units.RADIANS));
		distanceCalculator.setStepSize(new Angle(Math.PI/6.0, Units.RADIANS));
		distanceCalculator.setAngleCount(6);
		RealArray distArray = distanceCalculator.calculateDistanceArray();
		LOG.debug(distArray);
		Assert.assertEquals("distances", "(-70.0,-40.6,-0.4,40.0,69.6,80.6)", distArray.toString());
	}

}
