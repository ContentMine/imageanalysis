package org.xmlcml.image.compound;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.PixelIsland;
import org.xmlcml.image.processing.PixelIslandList;
import org.xmlcml.image.processing.PixelIslandList.Operation;
import org.xmlcml.image.processing.RingList;
import org.xmlcml.image.processing.ZhangSuenThinning;

import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageUInt8;

public class PlotTest {
	
	private final static Logger LOG = Logger.getLogger(PlotTest.class);
	
	private final static File G002_DIR = new File(Fixtures.COMPOUND_DIR, "journal.pone.0095565.g002");
	private File PLOT_OUT_DIR;

	@Before
	public void setUp() {
		PLOT_OUT_DIR = new File("target/plot/");
		PLOT_OUT_DIR.mkdirs();
	}

	@Test
	public void testPlot() throws IOException {
		BufferedImage image = UtilImageIO.loadImage(new File(G002_DIR, "g002.png").toString());
		ImageUInt8 inputImage = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		UtilImageIO.saveImage(ConvertBufferedImage.convertTo(inputImage,null), new File(PLOT_OUT_DIR, "plotEcho.png").toString());

		ImageUInt8 binary = new ImageUInt8(inputImage.getWidth(), inputImage.getHeight());
		int threshold = 220; // axes are quite light gray
		ThresholdImageOps.threshold(inputImage, binary, threshold, false);
		BufferedImage outImage = VisualizeBinaryData.renderBinary(binary,null);
		UtilImageIO.saveImage(outImage, new File(PLOT_OUT_DIR, "plotBinary.png").toString());
		
		
		PixelIslandList islands = PixelIslandList.createPixelIslandList(new File(G002_DIR, "g002.png"));
		Assert.assertEquals("plot islands", 91, islands.size());
		
		PixelIslandList axes = PixelIslandList.createPixelIslandList(new File(G002_DIR, "axes.png"));
		Assert.assertEquals("axes", 1, axes.size());
		
		PixelIslandList errorbar = PixelIslandList.createPixelIslandList(new File(G002_DIR, "errorbar.png"));
		Assert.assertEquals("errorbar", 1, errorbar.size());
		PixelIslandList xvalues = PixelIslandList.createPixelIslandList(new File(G002_DIR, "xnumbers.png"));
		Assert.assertEquals("xvalues", 11, xvalues.size());
		PixelIslandList xtitle = PixelIslandList.createPixelIslandList(new File(G002_DIR, "xtitle.png"));
		Assert.assertEquals("xtitle", 33, xtitle.size());
		PixelIslandList yvalues = PixelIslandList.createPixelIslandList(new File(G002_DIR, "ynumbers.png"));
		Assert.assertEquals("yvalues", 9, yvalues.size()); 
		PixelIslandList ytitle = PixelIslandList.createPixelIslandList(new File(G002_DIR, "ytitle.png"));
		Assert.assertEquals("ytitle", 33, ytitle.size()); 
	}

	@Test
	public void testAxes0() throws IOException {
		PixelIsland axes = PixelIslandList.createPixelIslandList(new File(G002_DIR, "axes.png")).get(0);
		Assert.assertEquals("pixels", 7860, axes.size());
		PixelIsland axesThin = new PixelIsland(axes);
		axesThin.setDiagonal(true);
		axesThin.findRidge();
		PixelList edgeList = axesThin.getPixelsWithValue(1);
		Assert.assertEquals("1:", 3938, edgeList.size());
		PixelList list2 = axesThin.growFrom(edgeList, 1);
		Assert.assertEquals("2:", 3922, list2.size());
		PixelList list3 = axesThin.growFrom(list2, 2);
		Assert.assertEquals("3:", 0, list3.size());
	}

	@Test
	public void testErrorBar() throws IOException {
		PixelIsland errorbar = PixelIslandList.createPixelIslandList(new File(G002_DIR, "errorbar.png"), Operation.BINARIZE).get(0);
		Assert.assertEquals("pixels", 222, errorbar.size());
		PixelIsland errorIsland = new PixelIsland(errorbar);
		SVGG g = new SVGG();
		errorIsland.setDiagonal(true);
		errorIsland.findRidge();
		PixelList list1 = errorIsland.getPixelsWithValue(1);
		Assert.assertEquals("1:", 131, list1.size());
		list1.plotPixels(g, "orange");
		PixelList list2 = errorIsland.growFrom(list1, 1);
		Assert.assertEquals("2:", 46, list2.size());
		list2.plotPixels(g, "green");
		PixelList list3 = errorIsland.growFrom(list2, 2);
		Assert.assertEquals("3:", 30, list3.size());
		list3.plotPixels(g, "blue");
		PixelList list4 = errorIsland.growFrom(list3, 3);
		Assert.assertEquals("4:", 14, list4.size());
		list4.plotPixels(g, "red");
		PixelList list5 = errorIsland.growFrom(list4, 4);
		Assert.assertEquals("5:", 1, list5.size());
		list5.plotPixels(g, "cyan");
		PixelList list6 = errorIsland.growFrom(list5, 5);
		Assert.assertEquals("6:", 0, list6.size());
		
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/plot/errorbar.svg"));

		PixelList list12 = list2.getPixelsTouching(list1, errorIsland);
		list12.plotPixels(g, "yellow");
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/plot/errorbar1.svg"));
		
		SVGG gg = new SVGG();
		PixelIslandList thinned = PixelIslandList.thinFillAndGetPixelIslandList(ImageIO.read(new File(G002_DIR, "errorbar.png")), new ZhangSuenThinning());
		thinned.get(0).createRingsAndPlot(gg, RingList.DEFAULT_COLOURS);
		SVGSVG.wrapAndWriteAsSVG(gg, new File("target/plot/errorbar2.svg"));
	}
	
	@Test
	public void testAxes1() throws IOException {
		PixelIsland axes = PixelIslandList.createPixelIslandList(new File(G002_DIR, "axes.png"), Operation.BINARIZE).get(0);
		Assert.assertEquals("pixels", 7860, axes.size());
		SVGG gg = new SVGG();
		RingList ringList = axes.createRingsAndPlot(gg, new String[]{"orange", "green"} );
		assertSizes(ringList, new int[]{3938, 3922});
		SVGSVG.wrapAndWriteAsSVG(gg, new File("target/plot/axes.svg"));
	}

	@Test
	public void testXTitle() throws IOException {
		PixelIslandList titleChars = PixelIslandList.createPixelIslandList(new File(G002_DIR, "xtitle.png"), Operation.BINARIZE);
		Assert.assertEquals("characters", 33, titleChars.size());
		List<RingList> characterList = titleChars.createRingListList(new File("target/plot/xtitle.svg"));
		SVGG gg = new SVGG();
		PixelIslandList thinned = PixelIslandList.thinFillAndGetPixelIslandList(ImageIO.read(new File(G002_DIR, "xtitle.png")), new ZhangSuenThinning());
		thinned.createRingListList(new File("target/plot/xtitle2.svg"));
	}

	@Test
	public void testXNumbers() throws IOException {
		PixelIslandList xNumbers = PixelIslandList.createPixelIslandList(new File(G002_DIR, "xnumbers.png"), Operation.BINARIZE);
		Assert.assertEquals("characters", 11, xNumbers.size());
		xNumbers.createRingListList(new File("target/plot/xnumbers.svg"));
	}

	@Test
	public void testYTitle() throws IOException {
		PixelIslandList yTitle = PixelIslandList.createPixelIslandList(new File(G002_DIR, "ytitle.png"), Operation.BINARIZE);
		Assert.assertEquals("characters", 32, yTitle.size());
		yTitle.createRingListList(new File("target/plot/ytitle.svg"));
	}

	@Test
	public void testYNumbers() throws IOException {
		PixelIslandList yNumbers = PixelIslandList.createPixelIslandList(new File(G002_DIR, "ynumbers.png"), Operation.BINARIZE);
		yNumbers.createRingListList(new File("target/plot/ynumbers.svg"));
		Assert.assertEquals("characters", 9, yNumbers.size());
	}

	@Test
	public void testPoints() throws IOException {
		PixelIslandList points = PixelIslandList.createPixelIslandList(new File(G002_DIR, "points.png"), Operation.BINARIZE);
		points.createRingListList(new File("target/plot/points.svg"));
		Assert.assertEquals("points", 4, points.size());
		PixelIslandList thinned = PixelIslandList.thinFillAndGetPixelIslandList(ImageIO.read(new File(G002_DIR, "points.png")), new ZhangSuenThinning());
		thinned.createRingListList(new File("target/plot/points2.svg"));
	}

	@Test
	public void testNodes() throws IOException {
		PixelIslandList points = PixelIslandList.createPixelIslandList(new File(G002_DIR, "points.png"), Operation.BINARIZE);
		points.createRingListList(new File("target/plot/points.svg"));
		Assert.assertEquals("points", 4, points.size());
		PixelIslandList thinned = PixelIslandList.thinFillAndGetPixelIslandList(ImageIO.read(new File(G002_DIR, "points.png")), new ZhangSuenThinning());
		thinned.createRingListList(new File("target/plot/points2.svg"));
	}
	// characters should be done in another project (bring in JavaOCR)
	
	@Test
	// this one is very weak
	@Ignore
	public void test0095375() throws IOException {
		plotRingsAndThin(new File(Fixtures.COMPOUND_DIR, "journal.pone.0095375.g003.png"), 
				new File("target/plot/0093575.svg"),
				new File("target/plot/0093575_2.svg"));
	}

	@Test
	@Ignore // out of memory (chop up diagram?
	public void test0095807ManySubDiagrams() throws IOException {
		plotRingsAndThin(new File(Fixtures.COMPOUND_DIR, "journal.pone.0095807.g003.png"), 
				new File("target/plot/0095807.svg"),
				new File("target/plot/0095807_2.svg"));
	}

	@Test
	public void test0095816MultiColourAndSuperscripts() throws IOException {
		plotRingsAndThin(new File(Fixtures.COMPOUND_DIR, "journal.pone.0095816.g002.png"), 
				new File("target/plot/0095816.svg"),
				new File("target/plot/0095816_2.svg"));
	}

	@Test
	public void test004179BarChart() throws IOException {
		plotRingsAndThin(new File(Fixtures.COMPOUND_DIR, "journal.pone.0094179.g008.png"), 
				new File("target/plot/0094179_8.svg"),
				new File("target/plot/0094179_8_2.svg"));
	}

	@Test
	public void test004179CirclePlots() throws IOException {
		plotRingsAndThin(new File(Fixtures.COMPOUND_DIR, "journal.pone.0094179.g002.png"), 
				new File("target/plot/0094179_2.svg"),
				new File("target/plot/0094179_2_2.svg"));
	}

	// =========================
	

	private void plotRingsAndThin(File infile, File outfile1, File outfile2) throws IOException {
		PixelIslandList plot = PixelIslandList.createPixelIslandList(infile, Operation.BINARIZE);
		LOG.debug("plot size"+plot.size());
		plot.createRingListList(outfile1);
		PixelIslandList thinned = PixelIslandList.thinFillAndGetPixelIslandList(ImageIO.read(infile), new ZhangSuenThinning());
		thinned.createRingListList(outfile2);
	}

	
	
	private static void assertSizes(RingList ringList, int[] sizes) {
		Assert.assertNotNull("ringList", ringList);
		Assert.assertNotNull("sizes", sizes);
		Assert.assertEquals("ring count", sizes.length, ringList.size());
	}

}
