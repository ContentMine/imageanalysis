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

public class SpanningTreeTest {

	List<Pixel> lineList = Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(1, 5)), 
			});
	

	/**
	 * Tests the pixels below.
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * +++
	 * +
	 * +
	 */
	List<Pixel> tList = Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(1, 5)), 
			new Pixel(new Point(2, 3)), 
			new Pixel(new Point(3, 3)), 
			});
		

	@Test
	public void testLineEnd() {
		PixelIsland island = new PixelIsland(lineList);
		Pixel startPixel = lineList.get(0);
		System.out.println(startPixel);
		island.createSpanningTree(startPixel);
	}
	
	@Test
	public void testSpanningTree() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(1);
		island.createSpanningTree();

	}

}
