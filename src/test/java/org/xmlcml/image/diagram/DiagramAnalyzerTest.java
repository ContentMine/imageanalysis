package org.xmlcml.image.diagram;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.util.ColorStore;
import org.xmlcml.graphics.svg.util.ImageIOUtil;
import org.xmlcml.graphics.svg.util.ColorStore.ColorizerType;
import org.xmlcml.image.ImageAnalysisFixtures;
import org.xmlcml.image.ImageProcessor;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.colour.ColorAnalyzer;
import org.xmlcml.image.pixel.PixelEdge;
import org.xmlcml.image.pixel.PixelGraph;
import org.xmlcml.image.pixel.PixelIsland;
import org.xmlcml.image.pixel.PixelIslandList;
import org.xmlcml.image.pixel.PixelList;
import org.xmlcml.image.pixel.PixelRingList;
import org.xmlcml.image.pixel.PixelSegment;
import org.xmlcml.image.pixel.PixelSegmentList;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import boofcv.io.image.UtilImageIO;
import junit.framework.Assert;

public class DiagramAnalyzerTest {
	private static final Logger LOG = Logger.getLogger(DiagramAnalyzerTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public static File TARGET_DIR = new File("target");
	public static String FUNNEL = "funnel";
	public static File TARGET_FUNNEL = new File(TARGET_DIR, FUNNEL+"/");
	public static String ELECTRONIC = "electronic";
	public static File TARGET_ELECTRONIC = new File(TARGET_DIR, ELECTRONIC+"/");
	
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
		SVGSVG.wrapAndWriteAsSVG(pixelIsland.createSVG(), new File(TARGET_FUNNEL, filenames[0] + ".svg"));

	}

	@Test
	public void testDrainSource() {
		String filename = "drainsource.png";
		String [] filenames = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.ELECTRONIC_DIR, filenames[0] + "." + filenames[1]);
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		Assert.assertEquals(115, pixelIslandList.size());
		PixelIsland pixelIsland = pixelIslandList.get(0);
		PixelGraph graph = new PixelGraph(pixelIsland);
		graph.compactCloseNodes(3);
		LOG.debug(graph);
	}

	@Test
	/**
	 * Single line, segmented OK
	 */
	public void testDrainSource1() {
		String filename = "drainsource1.png";
		String [] filenames = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.ELECTRONIC_DIR, filenames[0] + "." + filenames[1]);
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		Assert.assertEquals(11, pixelIslandList.size());
		for (int i = 0; i < pixelIslandList.size(); i++) {
			PixelIsland pixelIsland = pixelIslandList.get(i);
			SVGSVG.wrapAndWriteAsSVG(pixelIsland.createSVG(), new File(TARGET_ELECTRONIC, filenames[0] + "."+i+".svg"));
		}
		// extract segments
		extractSegments(pixelIslandList, 0);
		extractSegments(pixelIslandList, 1);
		
		// analyze pixels
		int nvalues = 4; // i.e. 16-bit color
		nvalues = 2;
		BufferedImage image = UtilImageIO.loadImage(imageFile.toString());
		image = ImageUtil.flattenImage(image, nvalues);
		ImageIOUtil.writeImageQuietly(image, new File(TARGET_ELECTRONIC, filenames[0] + "."+"colors"+".png"));

	}

	@Test
	/** messy, because linewidth not analyzed.
	 * 
	 */
	public void testDrainSource2() {
		String filename = "drainsource2.png";
		String [] filenames = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.ELECTRONIC_DIR, filenames[0] + "." + filenames[1]);
		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
		Assert.assertEquals(40, pixelIslandList.size());
		// smaller islands are characters
		for (int i = 0; i < pixelIslandList.size(); i++) {
			PixelIsland pixelIsland = pixelIslandList.get(i);
			SVGSVG.wrapAndWriteAsSVG(pixelIsland.createSVG(), new File(TARGET_ELECTRONIC, filenames[0] + "."+i+".svg"));
		}
		// extract segments
		extractSegments(pixelIslandList, 0);
		extractSegments(pixelIslandList, 1);
	}
	
	@Test
	/** tricolor diagram
	 * 
	 */
	public void testTricolor() {
		String filename = "tricolor.png";
		String [] filenames = filename.split("\\.");
		File imageFile = new File(ImageAnalysisFixtures.BIO_DIR, filenames[0] + "." + filenames[1]);
		int nvalues = 4; // i.e. 16-bit color
		nvalues = 2;
		BufferedImage image = UtilImageIO.loadImage(imageFile.toString());
		image = ImageUtil.flattenImage(image, nvalues);
		ImageIOUtil.writeImageQuietly(image, new File("target/bio/"+filenames[0]+"/poster.png"));
		
		ColorAnalyzer colorAnalyzer = new ColorAnalyzer(image);
		Multiset<Integer> set = colorAnalyzer.createColorSetNew();
		for (Entry<Integer> entry : set.entrySet()) {
			int ii = ((int) entry.getElement()) & 0x00ffffff;
			// uncomment for debug
//			System.out.println(Integer.toHexString(ii)+"  "+entry.getCount()); 
		}
//		ImageIOUtil.writeImageQuietly(image, new File("target/bio/filenames[0]/poster.png"));

		DiagramAnalyzer diagramAnalyzer = new DiagramAnalyzer();
		diagramAnalyzer.getOrCreateGraphList(imageFile);
		PixelIslandList pixelIslandList = diagramAnalyzer.getOrCreatePixelIslandList();
//		Assert.assertEquals(40, pixelIslandList.size());
		for (int i = 0; i < pixelIslandList.size(); i++) {
			PixelIsland pixelIsland = pixelIslandList.get(i);
			SVGSVG.wrapAndWriteAsSVG(pixelIsland.createSVG(), new File(TARGET_ELECTRONIC, filenames[0] + "."+i+".svg"));
		}
		// extract segments
		extractSegments(pixelIslandList, 0);
		extractSegments(pixelIslandList, 1);
	}


	//======================================
	
	private void extractSegments(PixelIslandList pixelIslandList, int serial) {
		PixelEdge edge = pixelIslandList.get(serial).getOrCreateGraph().getOrCreateEdgeList().get(0);
		PixelEdge edge1 = edge.cyclise();
		edge = edge1 == null ? edge : edge1;
		LOG.debug("edge "+edge);
		LOG.debug("cycle "+edge.isCyclic());
		LOG.debug("node "+edge.getNodes().size()+"; "+edge.getNodes());
		PixelSegmentList segmentList = edge.getOrCreateSegmentList(1.0);
		LOG.debug("S: "+segmentList.size()+"; "+segmentList);
		SVGElement g = segmentList.getOrCreateSVG();
		for (PixelSegment segment : segmentList) {
			plotPoint(g, segment, 0);
		}
		plotPoint(g, segmentList.getLast(), 1);
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_ELECTRONIC, "edge" + "."+serial+".svg"));
	}

	private void plotPoint(SVGElement g, PixelSegment segment, int serial) {
		SVGLine line = segment.getSVGLine();
		SVGCircle c = new SVGCircle(line.getXY(serial), 3.0);
		g.appendChild(c);
	}
	
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

