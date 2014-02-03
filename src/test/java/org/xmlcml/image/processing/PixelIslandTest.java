package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.lines.DouglasPeucker;
import org.xmlcml.image.lines.PixelPath;

public class PixelIslandTest {

	public final static Logger LOG = Logger.getLogger(PixelIslandTest.class);
	
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
//		island.cleanChains();
//		Assert.assertEquals("size", 28, island.size());
	}
	
	@Test
	public void testgetTerminalMaltoryzine0() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIsland island = floodFill.getPixelIslandList().get(0);
		Assert.assertEquals("size", 492, island.size());
//		island.cleanChains();
//		Assert.assertEquals("size", 478, island.size());
	}

	@Test
	public void testCreateSeveralPixelIslands() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		List<PixelIsland> islandList = floodFill.getPixelIslandList();
		Assert.assertEquals("islands", 5, islandList.size());
		int[] islands =   {492, 33,  25,  29,  25};
		int[] terminals = {6,   2,   2,   2,   2};
		int[] count2 =    {409, 12,  23,  27,  23};
		int[] count3 =    {74,  18,  0,   0,   0};
		int[] count4 =    {3,   1,   0,   0,   0};
		int[] count5 =    {0,   0,   0,   0,   0};
		for (int i = 0; i < islandList.size(); i++) {
			PixelIsland island = islandList.get(i);
			Assert.assertEquals("island "+i, islands[i], island.size());
			Assert.assertEquals("terminal "+i, terminals[i], island.getTerminalPixels().size());
			Assert.assertEquals("2-nodes "+i, count2[i], island.getNodesWithNeighbours(2).size());
			Assert.assertEquals("3-nodes "+i, count3[i], island.getNodesWithNeighbours(3).size());
			Assert.assertEquals("4-nodes "+i, count4[i], island.getNodesWithNeighbours(4).size());
			Assert.assertEquals("5-nodes "+i, count5[i], island.getNodesWithNeighbours(5).size());
		}
	
		islandList.get(0).flattenNuclei();
		islandList.get(1).flattenNuclei();
		islandList.get(2).flattenNuclei();
		islandList.get(3).flattenNuclei();
		islandList.get(4).flattenNuclei();
		
		
		PixelIsland island1 = islandList.get(1);
		List<Pixel> pixelList = island1.getPixelList();
		for (Pixel pixel : pixelList) {
			LOG.trace("pixel "+pixel.getInt2());
		}
		Pixel pixel32 = pixelList.get(32);
		Assert.assertEquals("pixel32", new Int2(206, 66), pixel32.getInt2());
		island1.createSpanningTree(pixel32);
	
	}
	
	@Test
	public void testCreateLinePixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.LINE_PNG);
		List<PixelPath> pixelPaths = island.createPixelPathList();
		Assert.assertEquals("paths", 1, pixelPaths.size());
		DouglasPeucker douglasPeucker = new DouglasPeucker(2.0);
		List<Real2> reduced = douglasPeucker.reduce(pixelPaths.get(0).getPoints());
		LOG.debug(reduced);
	}

	@Test
	public void testCreateZigzagPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.ZIGZAG_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/zigzag.svg");
	}

	@Test
	public void testCreateHexagonPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.HEXAGON_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/hexagon.svg");
	}

	@Test
	public void testCreateBranch0PixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.BRANCH0_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 3, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/branch0.svg");
	}

	@Test
	public void testCreateMaltoryzine0PixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.MALTORYZINE0_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 6, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/maltoryzine0.svg");
	}

	@Test
	public void testCreateMaltoryzinePixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.MALTORYZINE_THINNED_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 6, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/maltoryzine.svg");
	}

	/** this one has a terminal nucleus
	 * 
	 * [point: (50,59); neighbours:  (51,59) (50,58) (49,58); marked:, 
	 *  point: (50,58); neighbours:  (49,58) (51,59) (50,59); marked:,
	 *  point: (51,59); neighbours:  (50,59) (52,60) (50,58); marked:]
	 *  
	 *  (49,58) is marked as a spike but not part of the nucleus. 
	 *  
	 *  also (52,60) and (53,60)
	 *  Finally (54,61) is a terminal and is identified as such.
	 *  
	 *  The search starts at (54,61) and terminates correctly at (49,58)
	 *  
	 * @throws IOException
	 */
	@Test
	public void testCreateTerminalPixelPaths() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.TERMINAL_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 1, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/terminalnode.svg");
	}

	/** this one has 2 terminal nuclei
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateTerminalsPixelPaths() throws IOException{
		PixelIsland island = createFirstPixelIsland(Fixtures.TERMINALS_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 6, segmentArrayList.size());
		SVGSVG svg = island.debugSVG("target/terminalnodes.svg");
	}


	/** this one has a terminal nucleus
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateBranchPixelPaths() throws IOException {
		PixelIsland island = createFirstPixelIsland(Fixtures.BRANCH_PNG);
		List<Real2Array> segmentArrayList = island.createSegments();
		Assert.assertEquals("segmentArray", 3, segmentArrayList.size());
		debug(segmentArrayList);
		SVGSVG svg = island.debugSVG("target/branch.svg");
	}

	// =====================================================
	
	private void debug(List<Real2Array> segmentArrayList) {
		for (Real2Array coords : segmentArrayList) {
			System.out.println(coords);
		}
	}

	private PixelIsland createFirstPixelIsland(File file) throws IOException {
		return PixelIsland.createPixelIsland(ImageIO.read(file)).get(0);
	}


}
