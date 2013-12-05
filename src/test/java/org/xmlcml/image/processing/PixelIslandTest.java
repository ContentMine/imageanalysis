package org.xmlcml.image.processing;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class PixelIslandTest {
	
	@Test
	public void testSpanningTree() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(1);
		SpanningTree spanningTree = island.createSpanningTree();
		
	}
	

	@Test
	public void testAddPixel() {
		PixelIsland island = new PixelIsland();
		island.addPixel(new Pixel(new Point(1, 1)));
	}
}
