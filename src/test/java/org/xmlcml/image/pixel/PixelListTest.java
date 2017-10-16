package org.xmlcml.image.pixel;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageAnalysisFixtures;
import org.xmlcml.image.diagram.DiagramAnalyzer;

public class PixelListTest {

	private final static Logger LOG = Logger.getLogger(PixelListTest.class);
	
	private static PixelList CREATE_TEST_ISLAND() {
		PixelList pixelList = new PixelList();
		pixelList.add(new Pixel(1, 11));
		pixelList.add(new Pixel(2, 11));
		pixelList.add(new Pixel(4, 11));
		pixelList.add(new Pixel(5, 11));
		pixelList.add(new Pixel(1, 12));
		pixelList.add(new Pixel(2, 12));
		pixelList.add(new Pixel(4, 12));
		pixelList.add(new Pixel(1, 13));
		pixelList.add(new Pixel(3, 13));
		pixelList.add(new Pixel(5, 13));
		pixelList.add(new Pixel(1, 14));
		pixelList.add(new Pixel(5, 14));
		pixelList.add(new Pixel(2, 15));
		pixelList.add(new Pixel(3, 15));
		pixelList.add(new Pixel(4, 15));
		pixelList.add(new Pixel(5, 16));
		return pixelList;
	}

	static PixelList CREATE_DIAMOND() {
		PixelList pixelList = new PixelList();
		pixelList.add(new Pixel(2, 12));
		pixelList.add(new Pixel(1, 13));
		pixelList.add(new Pixel(2, 14));
		pixelList.add(new Pixel(3, 15));
		pixelList.add(new Pixel(4, 16));
		pixelList.add(new Pixel(5, 15));
		pixelList.add(new Pixel(5, 14));
		pixelList.add(new Pixel(6, 13));
		pixelList.add(new Pixel(5, 12));
		pixelList.add(new Pixel(4, 11));
		pixelList.add(new Pixel(3, 11));
		return pixelList;
	}

	static PixelList CREATE_TWO_ISLANDS() {
		PixelList pixelList = CREATE_DIAMOND();
		pixelList.add(new Pixel(12, 2));
		pixelList.add(new Pixel(11, 3));
		pixelList.add(new Pixel(12, 4));
		pixelList.add(new Pixel(13, 5));
		pixelList.add(new Pixel(14, 4));
		pixelList.add(new Pixel(15, 5));
		pixelList.add(new Pixel(15, 4));
		pixelList.add(new Pixel(16, 3));
		pixelList.add(new Pixel(15, 2));
		pixelList.add(new Pixel(14, 1));
		pixelList.add(new Pixel(13, 1));
		return pixelList;
	}

	static PixelList CREATE_DIAMOND_SPIRO() {
		PixelList spiro = CREATE_DIAMOND();
		spiro.add(new Pixel(1,16));
		spiro.add(new Pixel(2,16));
		return spiro;
	}


	@Test
	public void testRemoveMinorIslands() {
		PixelList pixelList = new PixelList();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				pixelList.add(new Pixel(i, j));
			}
		}
		pixelList.add(new Pixel(10, 10));
		
		pixelList.add(new Pixel(20, 20));
		pixelList.add(new Pixel(20, 21));
		
		pixelList.add(new Pixel(30, 30));
		pixelList.add(new Pixel(30, 31));
		pixelList.add(new Pixel(30, 32));
		
		PixelIsland island = new PixelIsland(pixelList);
		
		Assert.assertEquals("pixels", 22, pixelList.size());
		pixelList.removeMinorIslands(1);
		Assert.assertEquals("pixels", 21, pixelList.size());
		pixelList.removeMinorIslands(2);
		Assert.assertEquals("pixels", 19, pixelList.size());
		pixelList.removeMinorIslands(3);
		Assert.assertEquals("pixels", 16, pixelList.size());
		
	}
	
	@Test
	public void testCreateBinary() {
		PixelList list = new PixelList();
		list.add(new Pixel(1, 10));
		list.add(new Pixel(2, 10));
		list.add(new Pixel(3, 10));
		list.add(new Pixel(1, 11));
		list.add(new Pixel(1, 12));
		list.add(new Pixel(2, 12));
		list.add(new Pixel(2, 13));
		int[][] binary = list.createBinary();
		IntMatrix matrix = new IntMatrix(binary);
		Assert.assertEquals("{3,4}\n(1,1,1,0)\n(1,0,1,1)\n(1,0,0,0)", matrix.toString());
		PixelList list1 = new PixelList();
	}
	
	@Test
	public void testCreateInterior() {
		PixelList pixelList = CREATE_TEST_ISLAND();
		
		int[][] binary = pixelList.createBinary();
		IntMatrix matrix = new IntMatrix(binary);
		Assert.assertEquals("{5,6}\n(1,1,1,1,0,0)\n(1,1,0,0,1,0)\n(0,0,1,0,1,0)\n(1,1,0,0,1,0)\n(1,0,1,1,0,1)", matrix.toString());
		
		PixelListFloodFill pixelListFloodFill = new PixelListFloodFill(pixelList);
		PixelList interiorPixelList = pixelListFloodFill.createInteriorPixelList();
		Assert.assertEquals("interior", 5, interiorPixelList.size());
		SVGSVG.wrapAndWriteAsSVG(interiorPixelList.getOrCreateSVG(), new File("target/pixels/interiorTest.svg"));
		
	}

	@Test
	public void testCreateOutline() {
		PixelList diamond = CREATE_DIAMOND();
		SVGSVG.wrapAndWriteAsSVG(diamond.getOrCreateSVG(), new File("target/pixels/diamondTest.svg"));
		PixelList extremes = diamond.findExtremePixels();
		Assert.assertEquals("extremes", "(3,11)(6,13)(4,16)(1,13)", extremes.toString());
		
	}

	@Test
	public void testAnalyzeOutline() {
		PixelList diamond = CREATE_DIAMOND();
		PixelOutliner outliner = new PixelOutliner(diamond);
		outliner.createOutline();
	}

	@Test
	public void testCreateCurvature() {
		String filename = "crossing1.png";
		String [] filenames = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.FUNNEL_DIR, filenames[0] + "." + filenames[1]);
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		Assert.assertEquals(4, pixelIslandList.size());
		PixelIsland pixelIsland = pixelIslandList.get(0);
		PixelGraph graph = new PixelGraph(pixelIsland);
		graph.compactCloseNodes(3);
		LOG.debug(graph);
		PixelNodeList nodeList = graph.getOrCreateNodeList();
		PixelEdgeList edgeList = graph.getOrCreateEdgeList();
		for (PixelEdge edge : edgeList) {
			RealArray curvature = edge.getPixelList().createCurvature();
			LOG.debug("curve:"+curvature);
		}
	}

}
