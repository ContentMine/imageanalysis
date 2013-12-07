package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.Pixel.Marked;

public class PixelIslandTest {

	private final static Logger LOG = Logger.getLogger(PixelIslandTest.class);
	
	@Test
	@Ignore // non-deterministic?

	public void testAddPixel() {
		List<Pixel> longTList = Fixtures.LONG_T_LIST;
		PixelIsland island = new PixelIsland(longTList);
		List<Pixel> n0 = longTList.get(0).getNeighbours(island);
		Assert.assertEquals("0", 1, n0.size());
		List<Pixel> n1 = longTList.get(1).getNeighbours(island);
		Assert.assertEquals("1", 2, n1.size());
		List<Pixel> n2 = longTList.get(2).getNeighbours(island);
		Assert.assertEquals("2", 3, n2.size());
		List<Pixel> n3 = longTList.get(3).getNeighbours(island);
		Assert.assertEquals("3", 1, n3.size());
		List<Pixel> n4 = longTList.get(4).getNeighbours(island);
		Assert.assertEquals("3", 1, n4.size());
	}
	
	/**
	 * Tests the pixels below.
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * ++
	 * +
	 */
	@Test
	@Ignore // non-deterministic?
	public void testAddPixelWithDiagonal() {
		boolean diagonal = true;
		List<Pixel> longTList = Fixtures.LONG_T_LIST;
		PixelIsland island = new PixelIsland(longTList, diagonal);
		List<Pixel> n0 = longTList.get(0).getNeighbours(island);
		Assert.assertEquals("0", 1, n0.size());
		List<Pixel> n1 = longTList.get(1).getNeighbours(island);
		Assert.assertEquals("1", 2, n1.size());
		List<Pixel> n2 = longTList.get(2).getNeighbours(island);
		Assert.assertEquals("2", 3, n2.size());
		List<Pixel> n3 = longTList.get(3).getNeighbours(island);
		Assert.assertEquals("3", 1, n3.size());
		List<Pixel> n4 = longTList.get(4).getNeighbours(island);
		Assert.assertEquals("4", 1, n4.size());
	}
	
	@Test
	public void testgetTerminalPixels() {
		List<Pixel> lineList = Fixtures.LINE_LIST;
		PixelIsland island = new PixelIsland(lineList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 2, terminalPixels.size());
	}
	
	@Test
	public void testgetTerminalPixelsL() {
		List<Pixel> lList = Fixtures.L_LIST;
		PixelIsland island = new PixelIsland(lList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 2, terminalPixels.size());
	}
	
	@Test
	public void testgetTerminalPixelsT() {
		List<Pixel> tList = Fixtures.T_LIST;
		PixelIsland island = new PixelIsland(tList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
		Assert.assertEquals("terminal", 3, terminalPixels.size());
		Assert.assertEquals("0", "(1,1)",  terminalPixels.get(0).getInt2().toString());
		Assert.assertEquals("0", "(1,5)",  terminalPixels.get(1).getInt2().toString());
		Assert.assertEquals("0", "(3,3)",  terminalPixels.get(2).getInt2().toString());
	}
	
	@Test
	public void testgetTerminalMaltoryzine1() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(1);
		Assert.assertEquals("size", 33, island.size());
		island.cleanChains();
		Assert.assertEquals("size", 28, island.size());
	}
	
	@Test
	public void testgetTerminalMaltoryzine0() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(0);
		Assert.assertEquals("size", 492, island.size());
		island.cleanChains();
		Assert.assertEquals("size", 478, island.size());
	}
	
}
