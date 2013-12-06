package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class SpanningTreeTest {

	private final static Logger LOG = Logger.getLogger(SpanningTreeTest.class);

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
		LOG.debug(island.size());
		for (Pixel pixel : island.getPixelList()) {
			LOG.debug(pixel.toString());
		}
		island.createSpanningTree(island.getPixelList().get(32));

	}

}
