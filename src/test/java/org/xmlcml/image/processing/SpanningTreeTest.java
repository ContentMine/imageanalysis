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


	@Test
	public void testLineEnd() {
		List<Pixel> lineList = Fixtures.LINE_LIST;
		PixelIsland island = new PixelIsland(lineList);
		Pixel startPixel = lineList.get(0);
		island.createSpanningTree(startPixel);
	}
	
	@Test
	public void testTList() {
		List<Pixel> lineList = Fixtures.T_LIST;
		PixelIsland island = new PixelIsland(lineList);
		Pixel startPixel = lineList.get(0);
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
