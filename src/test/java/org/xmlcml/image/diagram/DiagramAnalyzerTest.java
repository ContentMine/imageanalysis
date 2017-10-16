package org.xmlcml.image.diagram;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.util.ColorStore;
import org.xmlcml.graphics.svg.util.ColorStore.ColorizerType;
import org.xmlcml.image.ImageAnalysisFixtures;
import org.xmlcml.image.ImageProcessor;
import org.xmlcml.image.pixel.PixelGraph;
import org.xmlcml.image.pixel.PixelIsland;
import org.xmlcml.image.pixel.PixelIslandList;
import org.xmlcml.image.pixel.PixelList;
import org.xmlcml.image.pixel.PixelRingList;

import junit.framework.Assert;

public class DiagramAnalyzerTest {
	private static final Logger LOG = Logger.getLogger(DiagramAnalyzerTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static String FUNNEL = "funnel";
	public static File TARGET_FUNNEL = new File("target", FUNNEL+"/");
	
	@Test
	public void testFunnelSegments() {
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		String filename[] = (FUNNEL + "1.gif").split("\\.");
//		String filename[] = "funnel2.jpg".split("\\.");
//		String filename[] = "funnel3.png".split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.FUNNEL_DIR, filename[0] + "." + filename[1]);
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		Assert.assertEquals("islands",  14, pixelIslandList.size());
		SVGG g = new SVGG();
		// pixels
		Iterator<String> iterator = ColorStore.getColorIterator(ColorizerType.CONTRAST);
		int[] sizes = new int[] {3560,71,48,48,48,48,47,47,47,47,47,47,47,47};
		int[] nodeCounts = new int[] {33,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		int[] edgeCounts = new int[] {36,4,1,1,1,1,1,1,1,1,1,1,1,1};
		int[][] edgeSegmentCounts = new int[][] {
			new int[]{1,2,1,1,1,2,1,1,1,2,1,1,1,4,1,6,4,3,5,1,4,2,6,2,5,3,3,3,3,3,3,3,3,5,8,5},
			new int[]{1,1,4,3},
			new int[]{8},
	        new int[]{9},
	        new int[]{8},
	        new int[]{9},
	        new int[]{8},
	        new int[]{8},
	        new int[]{8},
	        new int[]{8},
	        new int[]{9},
	        new int[]{8},
	        new int[]{8},
	        new int[]{10},
			};
		Boolean[] cyclic = new Boolean[] {false,false,true,true,true,true,true,true,true,true,true,true,true,true};
		for (int isl = 0; isl < pixelIslandList.size(); isl++) {
			PixelIsland island = pixelIslandList.get(isl);
			PixelGraph graph = new PixelGraph(island);
			Assert.assertEquals("island", sizes[isl], island.size());
			Assert.assertEquals("nodes", nodeCounts[isl], graph.getOrCreateNodeList().size());
			Assert.assertEquals("edges", edgeCounts[isl], graph.getOrCreateEdgeList().size());
			graph.doEdgeSegmentation();
			Assert.assertEquals("cyclic "+isl, cyclic[isl], graph.isSingleCycle());
			SVGG gg = graph.normalizeSVGElements();
			g.appendChild(gg);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_FUNNEL, filename[0] + ".segments1.svg"));
	}

	@Test
	public void testFunnelSegments2a() {
		for (String filename : new String[]{/*"funnel1.gif", */"funnel3.png"}) {
			DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
			thinAndElements(filename, diagramAnalyzer);
		}
	}

	private void thinAndElements(String filename, DiagramAnalyzer diagramAnalyzer) {
		String filefix[] = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.FUNNEL_DIR, filefix[0] + "." + filefix[1]);
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		SVGG g = new SVGG();
		// pixels
		for (int isl = 0; isl < pixelIslandList.size(); isl++) {
			PixelIsland island = pixelIslandList.get(isl);
			PixelGraph graph = new PixelGraph(island);
			graph.doEdgeSegmentation();
			graph.isSingleCycle();
			SVGG gg = graph.normalizeSVGElements();
			g.appendChild(gg);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_FUNNEL, filefix[0] + ".segments2a.svg"));
	}

	@Test
	public void testFunnelPixelRings() {
		for (String filename : new String[] {
				"funnel1.gif","funnel2.jpg","funnel3.png"		}) {
			String [] filenames = filename.split("\\.");
			File imageFile = new File(ImageAnalysisFixtures.FUNNEL_DIR, filenames[0] + "." + filenames[1]);
			SVGG g = plotRings(imageFile);
			SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_FUNNEL, filenames[0] + ".rings.svg"));
		}
	}

	@Test
	public void testCrossing() {
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
		Assert.assertEquals(4,  graph.getOrCreateNodeList().size());
		Assert.assertEquals(5,  graph.getOrCreateEdgeList().size());
		PixelIslandList newIslandLists = graph.resolveCyclicCrossing();
		SVGSVG.wrapAndWriteAsSVG(pixelIsland.createSVG(), new File(TARGET_FUNNEL, filenames[0] + ".crossing.svg"));

	}


// ==================================
	
	private SVGG plotRings(File imageFile) {
		ImageProcessor imageProcessor = ImageProcessor.createDefaultProcessor();
		imageProcessor.setThinning(null);
		imageProcessor.readAndProcessFile(imageFile);
		PixelIslandList islandList = imageProcessor.getOrCreatePixelIslandList();
		SVGG g = new SVGG();
		// pixels
		Iterator<String> iterator = ColorStore.getColorIterator(ColorizerType.CONTRAST);
		for (PixelIsland island : islandList) {
			PixelRingList pixelRingList = island.getOrCreateInternalPixelRings();
			for (PixelList pixelList : pixelRingList) {
				SVGG gg = pixelList.getOrCreateSVG();
				gg.setCSSStyle("stroke-width:1.0;stroke:"+iterator.next()+";");
//				g.appendChild(gg);
			}
			PixelList outline = pixelRingList.getOuterPixelRing();
			if (outline != null) {
				SVGG gg = outline.getOrCreateSVG();
				gg.setCSSStyle("stroke-width:0.2;stroke:"+"black"+"; fill: none;");
				g.appendChild(gg);
			}
		}
		return g;
	}
	
	
}

