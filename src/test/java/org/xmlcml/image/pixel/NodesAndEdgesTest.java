package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageProcessor;
import org.xmlcml.image.ImageUtil;

public class NodesAndEdgesTest {



	private ImageProcessor DEFAULT_PROCESSOR;

	private final static Logger LOG = Logger.getLogger(NodesAndEdgesTest.class);
	
	private final static PixelIsland A12 = NeighbourPixelTest.A12;
	private final static PixelIsland A14 = NeighbourPixelTest.A14;

	@Before
	public void setup() {
		DEFAULT_PROCESSOR = new ImageProcessor();
	}
	
	@Test
	public void testGetNodePixelList() {
		PixelList nodePixelList = A12.getNodePixelList();
		Assert.assertEquals("A12 nodes", 4, nodePixelList.size());
	}
	
	@Test
	public void testTJunctionThinning() {
		PixelIsland a14a = new PixelIsland(A14);
		a14a.setDiagonal(true);
//		debug(a12a);
		PixelList pixelList = a14a.getPixelList();
		Assert.assertEquals(14, pixelList.size());
		Assert.assertEquals("A12a 0 neighbours", 0, a14a.getPixelsWithNeighbourCount(0).size());
		Assert.assertEquals("A12a 1 neighbours", 2, a14a.getPixelsWithNeighbourCount(1).size());
		Assert.assertEquals("A12a 2 neighbours", 4, a14a.getPixelsWithNeighbourCount(2).size());
		Assert.assertEquals("A12a 3 neighbours", 6, a14a.getPixelsWithNeighbourCount(3).size());
		Assert.assertEquals("A12a 4 neighbours", 2, a14a.getPixelsWithNeighbourCount(4).size());
		
		PixelNucleusList nucleusList = a14a.doTJunctionThinning();
		Assert.assertEquals(2, nucleusList.size());
		PixelList newPixelList = a14a.getPixelList();
		Assert.assertEquals(12, newPixelList.size());
//		LOG.debug("======");
//		debug(a12a);
		Assert.assertEquals("A12a 0 neighbours", 0, a14a.getPixelsWithNeighbourCount(0).size());
		Assert.assertEquals("A12a 1 neighbours", 2, a14a.getPixelsWithNeighbourCount(1).size());
		Assert.assertEquals("A12a 2 neighbours", 8, a14a.getPixelsWithNeighbourCount(2).size());
		Assert.assertEquals("A12a 3 neighbours", 2, a14a.getPixelsWithNeighbourCount(3).size());
		Assert.assertEquals("A12a 4 neighbours", 0, a14a.getPixelsWithNeighbourCount(4).size());
	}

	/** create PixelNodes.
	 * 
	 */
	@Test
	public void testNodes() {
		PixelNodeList nodeList = A12.createNodeList();//  getNodes();
		Assert.assertEquals("nodes", 4, nodeList.size());
		Assert.assertNotNull(nodeList.getPixelNode(new Pixel(0,0)));
		Assert.assertNotNull(nodeList.getPixelNode(new Pixel(1,2)));
		Assert.assertNotNull(nodeList.getPixelNode(new Pixel(3,2)));
		Assert.assertNotNull(nodeList.getPixelNode(new Pixel(4,0)));
	}

	@Test
	public void testCreateEdge() {
		PixelIsland island = A12;
		PixelNodeList nodeList = island.createNodeList();//  getNodes()
		Assert.assertEquals("nodes", 4, nodeList.size());
		PixelEdgeList edgeList = island.createEdges();
		Assert.assertEquals("edges", 4, edgeList.size());
		PixelEdgeList edges = edgeList.getEdges(new Pixel(0,0), new Pixel(1,2));
		Assert.assertEquals(1, edges.size());
		Assert.assertEquals("{(0,0)(0,1)(1,2)}/[(0,0)(1,2)]", edges.get(0).toString());
		Assert.assertEquals("{(0,0)(0,1)(1,2)}/[(0,0)(1,2)]", edgeList.getEdges(new Pixel(1,2), new Pixel(0,0)).get(0).toString());
		Assert.assertEquals("{(3,2)(4,1)(4,0)}/[(3,2)(4,0)]", edgeList.getEdges(new Pixel(3,2), new Pixel(4,0)).get(0).toString());
		Assert.assertEquals("", 2, edgeList.getEdges(new Pixel(1,2), new Pixel(3,2)).size());
		String s = edgeList.getEdges(new Pixel(1,2), new Pixel(3,2)).toString();
		Assert.assertTrue(
				s.contains("{(1,2)(0,3)(1,4)(2,4)(3,4)(4,3)(3,2)}/[(1,2)(3,2)]") &&
				s.contains("{(1,2)(2,2)(3,2)}/[(1,2)(3,2)]"));
		for (PixelEdge edge : edgeList) {
			LOG.trace(edge);
		}
	}

	@Test
	public void testCharacter65() {
		File png65 = new File(Fixtures.HELVETICA_MAIN_DIR, "65.png");
		Assert.assertTrue(png65.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(png65);
		image = ImageUtil.addBorders(image, 1, 1, 0/*0xffffff*/);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		PixelIsland island = pixelIslandList.getLargestIsland();
		island.setDiagonal(true);
		SVGSVG.wrapAndWriteAsSVG(island.getSVGG(), new File("target/nodesEdges/character65.svg"));
		Assert.assertEquals("node 0", 0, island.getPixelsWithNeighbourCount(0).size());
		Assert.assertEquals("node 1", 4, island.getPixelsWithNeighbourCount(1).size()); //?
		Assert.assertEquals("node 2", 66, island.getPixelsWithNeighbourCount(2).size());
		Assert.assertEquals("node 3", 14, island.getPixelsWithNeighbourCount(3).size());
		Assert.assertEquals("node 4", 4, island.getPixelsWithNeighbourCount(4).size());
		Assert.assertEquals("node 5", 0, island.getPixelsWithNeighbourCount(5).size());
		PixelNodeList nodeList = island.createNodeList();
		for (PixelNode node : nodeList) {
			LOG.debug(node);
		}
	}

	@Test
	public void testCharacterHelveticaNeighbourCounts() {
		File helvetica = new File(Fixtures.FONTS_MAIN_DIR, "_helvetica.png");
		Assert.assertTrue(helvetica.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(helvetica);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getSVGG(), new File("target/nodesEdges/helvetica.svg"));
		pixelIslandList.sortYX(5.0);
		Assert.assertEquals("islands", 96, pixelIslandList.size());
		for (int i = 1; i < 96; i++) {
			SVGSVG.wrapAndWriteAsSVG(pixelIslandList.get(i).getSVGG(), new File("target/nodesEdges/helvetica"+i+".svg"));
		}
		
		PixelTestUtils.assertNeighbourCounts(pixelIslandList,
				new IntArray[] {
				new IntArray(new int[]{1442, 0,0,1442,0,0,0,0,0,0}), // box // 0
				new IntArray(new int[]{70, 0,2,66,2,0,0,0,0,0}), // A // 
				new IntArray(new int[]{102, 0,0,100,2,0,0,0,0,0}), // B
				new IntArray(new int[]{68, 0,2,66,0,0,0,0,0,0}), // C
				new IntArray(new int[]{88, 0,0,88,0,0,0,0,0,0}), // D
				new IntArray(new int[]{84, 0,3,80,1,0,0,0,0,0}), // E  // 5
				new IntArray(new int[]{64, 0,3,60,1,0,0,0,0,0}), // F
				new IntArray(new int[]{88, 0,3,84,1,0,0,0,0,0}), // G ??
				new IntArray(new int[]{76, 0,4,70,2,0,0,0,0,0}), // H
				new IntArray(new int[]{28, 0,2,26,0,0,0,0,0,0}), // I // no serif
				new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // J // 10
				new IntArray(new int[]{71, 0,4,65,2,0,0,0,0,0}), // K 2 Y-junctions
				new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // L
				new IntArray(new int[]{108, 0,2,106,0,0,0,0,0,0}), // M
				new IntArray(new int[]{81, 0,3,77,1,0,0,0,0,0}), // N
				new IntArray(new int[]{6, 0,2,4,0,0,0,0,0,0}), // accent // 15 grave
				new IntArray(new int[]{85, 0,0,85,0,0,0,0,0,0}), // O
				new IntArray(new int[]{75, 0,1,73,1,0,0,0,0,0}), // P
				new IntArray(new int[]{92, 0,2,88,2,0,0,0,0,0}), // Q  2 T-junctions // 18
				new IntArray(new int[]{94, 0,2,90,2,0,0,0,0,0}), // R
				new IntArray(new int[]{77, 0,2,75,0,0,0,0,0,0}), // S  // 20
				new IntArray(new int[]{50, 0,3,46,1,0,0,0,0,0}), // T
				new IntArray(new int[]{70, 0,2,68,0,0,0,0,0,0}), // U
				new IntArray(new int[]{58, 0,2,56,0,0,0,0,0,0}), // V
				new IntArray(new int[]{106, 0,3,102,1,0,0,0,0,0}), // W // small tail on central peak
				new IntArray(new int[]{60, 0,4,54,2,0,0,0,0,0}), // X // 1 Y-junction // 25
				new IntArray(new int[]{48, 0,3,44,1,0,0,0,0,0}), // Y
				new IntArray(new int[]{66, 0,2,64,0,0,0,0,0,0}), // Z
				new IntArray(new int[]{70, 0,2,66,2,0,0,0,0,0}), // A accented
				
				new IntArray(new int[]{19, 0,0,19,0,0,0,0,0,0}), // accent ring // 29
				new IntArray(new int[]{5, 0,2,3,0,0,0,0,0,0}), // accent acute // 30
				new IntArray(new int[]{9, 0,2,7,0,0,0,0,0,0}), // accent hat // 31
				new IntArray(new int[]{15, 0,2,13,0,0,0,0,0,0}), // accent tilde // 32
				new IntArray(new int[]{72, 0,2,68,2,0,0,0,0,0}), // accented A // 33
				new IntArray(new int[]{85, 0,3,81,1,0,0,0,0,0}), // accented E // 34
				new IntArray(new int[]{28, 0,2,26,0,0,0,0,0,0}), // accented I // 35
				new IntArray(new int[]{86, 0,0,86,0,0,0,0,0,0}), // accented O // 36
				// these are sorted by the TOP of the box
				new IntArray(new int[]{70, 0,1,68,1,0,0,0,0,0}), // b // 37 
				new IntArray(new int[]{70, 0,1,68,1,0,0,0,0,0}), // d // 38
				new IntArray(new int[]{38, 0,4,29,0,5,0,0,0,0}), // f // 39
				new IntArray(new int[]{61, 0,3,57,1,0,0,0,0,0}), // h // 40
				new IntArray(new int[]{1,  1,0,0,0,0,0,0,0,0}), // dot // 41
				new IntArray(new int[]{1,  1,0,0,0,0,0,0,0,0}), // dot // 42
				new IntArray(new int[]{59, 0,4,53,2,0,0,0,0,0}), // k // 43 
				new IntArray(new int[]{29, 0,2,27,0,0,0,0,0,0}), // l // 44 
				new IntArray(new int[]{70, 0,2,66,2,0,0,0,0,0}), // a // 45
				new IntArray(new int[]{48, 0,2,46,0,0,0,0,0,0}), // c // 46
				new IntArray(new int[]{70, 0,2,66,2,0,0,0,0,0}), // e // 47 // 1 Y junct
				new IntArray(new int[]{84, 0,1,82,1,0,0,0,0,0}), // g  //48
				new IntArray(new int[]{20, 0,2,18,0,0,0,0,0,0}), // i // 49
				new IntArray(new int[]{31, 0,2,29,0,0,0,0,0,0}), // j // 50
				new IntArray(new int[]{82, 0,4,76,2,0,0,0,0,0}), // m // 51 // 2 Yjunct
				new IntArray(new int[]{51, 0,3,47,1,0,0,0,0,0}), // n // 52 // Y junct
				
				new IntArray(new int[]{6,  0,2,4,0,0,0,0,0,0}), // grave // 53
				new IntArray(new int[]{18, 0,0,18,0,0,0,0,0,0}), // ring // 54
				new IntArray(new int[]{5,  0,2,3,0,0,0,0,0,0}), // acute // 55
				new IntArray(new int[]{36, 0,4,27,0,5,0,0,0,0}), // t // 56 // one cross => 5 4-ccords
				new IntArray(new int[]{11, 0,2,9,0,0,0,0,0,0}), // hat // 57
				new IntArray(new int[]{12, 0,2,10,0,0,0,0,0,0}), // tilde // 58
				new IntArray(new int[]{88, 0,2,82,4,0,0,0,0,0}), // ampersand // 59
				new IntArray(new int[]{58, 0,0,58,0,0,0,0,0,0}), // o // 60
				new IntArray(new int[]{72, 0,2,68,2,0,0,0,0,0}), // p // 61
				new IntArray(new int[]{71, 0,2,67,2,0,0,0,0,0}), // q // 62
				new IntArray(new int[]{27, 0,3,23,1,0,0,0,0,0}), // r // 63
				new IntArray(new int[]{55, 0,2,53,0,0,0,0,0,0}), // s // 64
				new IntArray(new int[]{52, 0,3,48,1,0,0,0,0,0}), // u // 65
				new IntArray(new int[]{40, 0,2,38,0,0,0,0,0,0}), // v // 66
				new IntArray(new int[]{73, 0,3,69,1,0,0,0,0,0}), // w // 67
				new IntArray(new int[]{46, 0,4,40,2,0,0,0,0,0}), // x // 68
				new IntArray(new int[]{53, 0,3,49,1,0,0,0,0,0}), // y // 69
				new IntArray(new int[]{47, 0,2,45,0,0,0,0,0,0}), // z // 70
				new IntArray(new int[]{69, 0,2,65,2,0,0,0,0,0}), // a no accent // 71
				new IntArray(new int[]{69, 0,2,65,2,0,0,0,0,0}), // a no accent // 72
				new IntArray(new int[]{71, 0,2,67,2,0,0,0,0,0}), // e no accent // 73
				new IntArray(new int[]{20, 0,2,18,0,0,0,0,0,0}), // i no accent // 74
				new IntArray(new int[]{58, 0,0,58,0,0,0,0,0,0}), // o no accent // 75
				
				new IntArray(new int[]{35, 0,3,31,1,0,0,0,0,0}), // 1 // 76
				new IntArray(new int[]{63, 0,2,61,0,0,0,0,0,0}), // 2 // 77
				new IntArray(new int[]{66, 0,3,62,1,0,0,0,0,0}), // 3 // 78
				new IntArray(new int[]{62, 0,3,53,1,5,0,0,0,0}), // 4 // 79
				new IntArray(new int[]{67, 0,2,65,0,0,0,0,0,0}), // 5 // 80
				new IntArray(new int[]{77, 0,1,75,1,0,0,0,0,0}), // 6 // 81
				new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // 7 // 82
				new IntArray(new int[]{84, 0,0,82,2,0,0,0,0,0}), // 8 // 83
				new IntArray(new int[]{79, 0,1,74,3,0,0,1,0,0}), // 9 // 84 // mess, correct it
				new IntArray(new int[]{71, 0,0,71,0,0,0,0,0,0}), // 0 // 85
				new IntArray(new int[]{41, 0,2,39,0,0,0,0,0,0}), // ( // 86
				new IntArray(new int[]{106, 0,4,86,6,10,0,0,0,0}), // $ // 87
				new IntArray(new int[]{68, 0,4,62,2,0,0,0,0,0}), // pound // 88
				new IntArray(new int[]{21, 0,2,19,0,0,0,0,0,0}), // shriek // 89
				new IntArray(new int[]{36, 0,2,34,0,0,0,0,0,0}), // query // 90
				new IntArray(new int[]{40, 0,2,38,0,0,0,0,0,0}), // ) // 91
				new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
				new IntArray(new int[]{9, 0,2,7,0,0,0,0,0,0}), // , // 93
				new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
				new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
				
			}
		);
	}

	@Test
	public void testCharacterHelveticaNodeCounts() {
		File helvetica = new File(Fixtures.FONTS_MAIN_DIR, "_helvetica.png");
		Assert.assertTrue(helvetica.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(helvetica);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		pixelIslandList.sortYX(5.0);
		PixelTestUtils.assertNodeCounts(pixelIslandList,
			new IntArray[] {
			new IntArray(new int[]{1442, 0,0,1442,0,0,0,0,0,0}), // box // 0
			new IntArray(new int[]{70, 0,2,64,4,0,0,0,0,0}), // A // has a Y-junction
			new IntArray(new int[]{102, 0,0,100,2,0,0,0,0,0}), // B
			new IntArray(new int[]{68, 0,2,66,0,0,0,0,0,0}), // C
			new IntArray(new int[]{88, 0,0,88,0,0,0,0,0,0}), // D
			new IntArray(new int[]{84, 0,3,80,1,0,0,0,0,0}), // E  
			new IntArray(new int[]{64, 0,3,60,1,0,0,0,0,0}), // F
			new IntArray(new int[]{88, 0,3,82,3,0,0,0,0,0}), // G ??
			new IntArray(new int[]{76, 0,4,70,2,0,0,0,0,0}), // H
			new IntArray(new int[]{28, 0,2,26,0,0,0,0,0,0}), // I // no serif
			new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // J // 10
			new IntArray(new int[]{71, 0,4,61,6,0,0,0,0,0}), // K 2 Y-junctions
			new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // L
			new IntArray(new int[]{108, 0,2,106,0,0,0,0,0,0}), // M
			new IntArray(new int[]{81, 0,3,75,3,0,0,0,0,0}), // N
			new IntArray(new int[]{6, 0,2,4,0,0,0,0,0,0}), // accent // 15 grave
			new IntArray(new int[]{85, 0,0,85,0,0,0,0,0,0}), // O
			new IntArray(new int[]{75, 0,1,73,1,0,0,0,0,0}), // P
			new IntArray(new int[]{92, 0,2,88,2,0,0,0,0,0}), // Q  2 T-junctions // 18
			new IntArray(new int[]{94, 0,2,90,2,0,0,0,0,0}), // R
			new IntArray(new int[]{77, 0,2,75,0,0,0,0,0,0}), // S  // 20
			new IntArray(new int[]{50, 0,3,46,1,0,0,0,0,0}), // T
			new IntArray(new int[]{70, 0,2,68,0,0,0,0,0,0}), // U
			new IntArray(new int[]{58, 0,2,56,0,0,0,0,0,0}), // V
			new IntArray(new int[]{106, 0,3,102,1,0,0,0,0,0}), // W // small tail on central peak
			new IntArray(new int[]{60, 0,4,52,4,0,0,0,0,0}), // X // 1 Y-junction // 25
			new IntArray(new int[]{48, 0,3,44,1,0,0,0,0,0}), // Y
			new IntArray(new int[]{66, 0,2,64,0,0,0,0,0,0}), // Z
			new IntArray(new int[]{70, 0,2,65,2,0,0,0,0,0}), // A accented // 28
			
			new IntArray(new int[]{19, 0,0,19,0,0,0,0,0,0}), // accent ring // 29
			new IntArray(new int[]{5, 0,2,3,0,0,0,0,0,0}), // accent acute // 30
			new IntArray(new int[]{9, 0,2,7,0,0,0,0,0,0}), // accent hat // 31
			new IntArray(new int[]{15, 0,2,13,0,0,0,0,0,0}), // accent tilde // 32
			new IntArray(new int[]{72, 0,2,66,4,0,0,0,0,0}), // accented A // 33
			new IntArray(new int[]{85, 0,3,81,1,0,0,0,0,0}), // accented E // 34
			new IntArray(new int[]{28, 0,2,26,0,0,0,0,0,0}), // accented I // 35
			new IntArray(new int[]{86, 0,0,86,0,0,0,0,0,0}), // accented O // 36
			// these are sorted by the TOP of the box
			new IntArray(new int[]{70, 0,1,66,3,0,0,0,0,0}), // b // 37 
			new IntArray(new int[]{70, 0,1,68,1,0,0,0,0,0}), // d // 38
			new IntArray(new int[]{38, 0,4,29,0,5,0,0,0,0}), // f // 39
			new IntArray(new int[]{61, 0,3,55,3,0,0,0,0,0}), // h // 40
			new IntArray(new int[]{1,  1,0,0,0,0,0,0,0,0}), // dot // 41
			new IntArray(new int[]{1,  1,0,0,0,0,0,0,0,0}), // dot // 42
			new IntArray(new int[]{59, 0,4,51,4,0,0,0,0,0}), // k // 43 
			new IntArray(new int[]{29, 0,2,27,0,0,0,0,0,0}), // l // 44 
			new IntArray(new int[]{70, 0,2,64,4,0,0,0,0,0}), // a // 45
			new IntArray(new int[]{48, 0,2,46,0,0,0,0,0,0}), // c // 46
			new IntArray(new int[]{70, 0,2,64,4,0,0,0,0,0}), // e // 47 // 1 Y junct
			new IntArray(new int[]{84, 0,1,82,1,0,0,0,0,0}), // g  //48
			new IntArray(new int[]{20, 0,2,18,0,0,0,0,0,0}), // i // 49
			new IntArray(new int[]{31, 0,2,29,0,0,0,0,0,0}), // j // 50
			new IntArray(new int[]{82, 0,4,72,6,0,0,0,0,0}), // m // 51 // 2 Yjunct
			new IntArray(new int[]{51, 0,3,45,3,0,0,0,0,0}), // n // 52 // Y junct
			
			new IntArray(new int[]{6,  0,2,4,0,0,0,0,0,0}), // grave // 53
			new IntArray(new int[]{18, 0,0,18,0,0,0,0,0,0}), // ring // 54
			new IntArray(new int[]{5,  0,2,3,0,0,0,0,0,0}), // acute // 55
			new IntArray(new int[]{36, 0,4,27,0,5,0,0,0,0}), // t // 56 // one cross => 5 4-ccords
			new IntArray(new int[]{11, 0,2,9,0,0,0,0,0,0}), // hat // 57
			new IntArray(new int[]{12, 0,2,10,0,0,0,0,0,0}), // tilde // 58
			new IntArray(new int[]{88, 0,2,80,6,0,0,0,0,0}), // ampersand // 59
			new IntArray(new int[]{58, 0,0,58,0,0,0,0,0,0}), // o // 60
			new IntArray(new int[]{72, 0,2,68,2,0,0,0,0,0}), // p // 61
			new IntArray(new int[]{71, 0,2,67,2,0,0,0,0,0}), // q // 62
			new IntArray(new int[]{27, 0,3,23,1,0,0,0,0,0}), // r // 63
			new IntArray(new int[]{55, 0,2,53,0,0,0,0,0,0}), // s // 64
			new IntArray(new int[]{52, 0,3,48,1,0,0,0,0,0}), // u // 65
			new IntArray(new int[]{40, 0,2,38,0,0,0,0,0,0}), // v // 66
			new IntArray(new int[]{73, 0,3,67,3,0,0,0,0,0}), // w // 67
			new IntArray(new int[]{46, 0,4,40,2,0,0,0,0,0}), // x // 68
			new IntArray(new int[]{53, 0,3,49,1,0,0,0,0,0}), // y // 69
			new IntArray(new int[]{47, 0,2,45,0,0,0,0,0,0}), // z // 70
			new IntArray(new int[]{69, 0,2,63,4,0,0,0,0,0}), // a no accent // 71
			new IntArray(new int[]{69, 0,2,63,4,0,0,0,0,0}), // a no accent // 72
			new IntArray(new int[]{71, 0,2,65,4,0,0,0,0,0}), // e no accent // 73
			new IntArray(new int[]{20, 0,2,18,0,0,0,0,0,0}), // i no accent // 74
			new IntArray(new int[]{58, 0,0,58,0,0,0,0,0,0}), // o no accent // 75
			
			new IntArray(new int[]{35, 0,3,31,1,0,0,0,0,0}), // 1 // 76
			new IntArray(new int[]{63, 0,2,61,0,0,0,0,0,0}), // 2 // 77
			new IntArray(new int[]{66, 0,3,62,1,0,0,0,0,0}), // 3 // 78
			new IntArray(new int[]{62, 0,3,53,1,5,0,0,0,0}), // 4 // 79
			new IntArray(new int[]{67, 0,2,65,0,0,0,0,0,0}), // 5 // 80
			new IntArray(new int[]{77, 0,1,73,3,0,0,0,0,0}), // 6 // 81
			new IntArray(new int[]{45, 0,2,43,0,0,0,0,0,0}), // 7 // 82
			new IntArray(new int[]{84, 0,0,80,4,0,0,0,0,0}), // 8 // 83
			new IntArray(new int[]{78, 0,1,74,3,0,0,0,0,0}), // 9 // 84
			new IntArray(new int[]{71, 0,0,71,0,0,0,0,0,0}), // 0 // 85
			new IntArray(new int[]{41, 0,2,39,0,0,0,0,0,0}), // ( // 86
			new IntArray(new int[]{106, 0,4,86,6,10,0,0,0,0}), // $ // 87
			new IntArray(new int[]{68, 0,4,60,4,0,0,0,0,0}), // pound // 88
			new IntArray(new int[]{21, 0,2,19,0,0,0,0,0,0}), // shriek // 89
			new IntArray(new int[]{36, 0,2,34,0,0,0,0,0,0}), // query // 90
			new IntArray(new int[]{40, 0,2,38,0,0,0,0,0,0}), // ) // 91
			new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
			new IntArray(new int[]{9, 0,2,7,0,0,0,0,0,0}), // , // 93
			new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
			new IntArray(new int[]{1, 1,0,0,0,0,0,0,0,0}), // dot // 92
			
		}
				);
	}

	
	@Test
	public void testTrimStubs() {
		// too large
		File helvetica = new File(Fixtures.TEST_HELVETICA, "char57.png");
		Assert.assertTrue(helvetica.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(helvetica);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getSVGG(), new File("target/nodesEdges/char57.svg"));
	}

	// ==============================
	
	@Test
	public void testCharacterHelveticaMini() {
		File helvetica = new File(Fixtures.TEXT_DIR, "fonts/helvetica/minihelvetica.png");
		Assert.assertTrue(helvetica.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(helvetica);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getSVGG(), new File("target/nodesEdges/minihelveticatj.svg"));
	}

	@Test
	public void testCharacterTimes() {
		File timesRoman = new File(Fixtures.FONTS_MAIN_DIR, "_timesNewRoman.gif");
		Assert.assertTrue(timesRoman.exists());
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(timesRoman);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getSVGG(), new File("target/nodesEdges/timesNewRoman.svg"));
	}

	
	

}
