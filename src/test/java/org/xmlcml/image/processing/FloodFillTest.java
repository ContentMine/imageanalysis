package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class FloodFillTest {
	private static final Logger LOG = Logger.getLogger(FloodFillTest.class);

	@Test
	public void testBinaryMaltoryzine() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.fill();
		Assert.assertEquals("pixelIslands", 5, floodFill.getPixelIslandList().size());
	}
	
	@Test
	public void testThinnedMaltoryzineDiagonal() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		Assert.assertEquals("pixelIslands", 5, floodFill.getPixelIslandList().size());
		for (PixelIsland pixelIsland : floodFill.getPixelIslandList()) {
			LOG.debug(pixelIsland.size());
		}
	}
	
	
	@Test
	public void testThinnedMaltoryzineNoDiagnal() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.fill();
		Assert.assertEquals("pixelIslands", 202, floodFill.getPixelIslandList().size());
	}
	
	
}
