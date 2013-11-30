package org.xmlcml.image.lines.hough;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealMatrix;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.lines.old.BinaryHough;
import org.xmlcml.image.lines.old.HoughDistanceCalculator;

public class BinaryHoughTest {

	private static final Logger LOG = Logger.getLogger(BinaryHoughTest.class);
	
	@Test
	/** used for perpendicular in Hough.
	 * 
	 * modelled on Wikipedia https://en.wikipedia.org/wiki/Hough_transform#example 2013-11-18
	 * Their angle is rotated 90 degrees and distance is opposite sign
	 * @throws IOException
	 */
	public void testPointDataBins() throws IOException {
		BinaryHough hough = new BinaryHough();
		HoughDistanceCalculator distanceCalculator = new HoughDistanceCalculator();
		distanceCalculator.setPoint(new Real2(40.0, 70.0));
		distanceCalculator.setStartAngle(new Angle(0.0, Units.RADIANS));
		distanceCalculator.setStepSize(new Angle(Math.PI/6.0, Units.RADIANS));
		distanceCalculator.setAngleCount(6);
		RealArray distArray = distanceCalculator.calculateDistanceArray();
		LOG.trace(distArray);
		Assert.assertEquals("distances", "(-70.0,-40.6,-0.4,40.0,69.6,80.6)", distArray.toString());
	}

	@Test
	public void testLineData() throws IOException {
		BinaryHough hough = new BinaryHough();
		hough.readImage(Fixtures.ETHANE_PNG);
		BufferedImage bufferedImage = hough.getBufferedImage();
		Assert.assertNotNull(bufferedImage);
		Assert.assertEquals("width", 58, bufferedImage.getWidth());
		Assert.assertEquals("width", 16, bufferedImage.getHeight());
	}
	
	@Test
	public void testLine() throws IOException {
		BinaryHough hough = new BinaryHough();
		hough.readImage(Fixtures.ETHANE_PNG);
		RealMatrix accumulator = hough.calculateAccumulatorMatrix();
		
	}
	
	@Test
	public void test20Lines() throws IOException {
		BinaryHough hough = new BinaryHough();
		hough.readImage(Fixtures.MALTORYZINE_PNG);
//		List<SVGLine> lineList = hough.findLines();
//		Assert.assertEquals("lines", 20, lineList.size());
		
	}
	
}
