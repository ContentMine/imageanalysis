package org.xmlcml.image.lines.hough;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealMatrix;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.lines.hough.BinaryHough;

public class BinaryHoughTest {

	private static final Logger LOG = Logger.getLogger(BinaryHoughTest.class);
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
