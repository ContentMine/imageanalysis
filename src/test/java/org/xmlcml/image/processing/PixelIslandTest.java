package org.xmlcml.image.processing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class PixelIslandTest {

	List<Pixel> lineList = Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(1, 5)), 
			});

	/**
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * ++
	 * +
	 */
	List<Pixel> longTList = Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(2, 3)), 
			});

	@Test
	public void testAddPixel() {
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
	public void testAddPixelWithDiagonal() {
		boolean diagonal = true;
		PixelIsland island = new PixelIsland(longTList, diagonal);
		List<Pixel> n0 = longTList.get(0).getNeighbours(island);
		Assert.assertEquals("0", 1, n0.size());
		List<Pixel> n1 = longTList.get(1).getNeighbours(island);
		Assert.assertEquals("1", 3, n1.size());
		List<Pixel> n2 = longTList.get(2).getNeighbours(island);
		Assert.assertEquals("2", 3, n2.size());
		List<Pixel> n3 = longTList.get(3).getNeighbours(island);
		Assert.assertEquals("3", 2, n3.size());
		List<Pixel> n4 = longTList.get(4).getNeighbours(island);
		Assert.assertEquals("3", 3, n4.size());
	}
	
	@Test
	public void testgetTerminalPixels() {
		PixelIsland island = new PixelIsland(lineList);
		List<Pixel> terminalPixels = island.getTerminalPixels();
	}
	
}
