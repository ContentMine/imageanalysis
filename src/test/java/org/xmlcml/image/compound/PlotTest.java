package org.xmlcml.image.compound;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.Pixel;
import org.xmlcml.image.processing.PixelIsland;
import org.xmlcml.image.processing.PixelIslandList;

import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageUInt8;

public class PlotTest {
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
	public void testAxes() throws IOException {
		PixelIsland axes = PixelIslandList.createPixelIslandList(new File(G002_DIR, "axes.png")).get(0);
		Assert.assertEquals("pixels", 7860, axes.size());
		PixelIsland axesThin = new PixelIsland(axes);
		axesThin.setDiagonal(true);
		axesThin.findRidge();
		List<Pixel> edgeList = axesThin.getPixelsWithValue(1);
		Assert.assertEquals("1:", 3938, edgeList.size());
		List<Pixel> list2 = axesThin.growFrom(edgeList, 1);
		Assert.assertEquals("2:", 3922, list2.size());
		List<Pixel> list3 = axesThin.growFrom(list2, 2);
		Assert.assertEquals("3:", 0, list3.size());
	}

	@Test
	public void testErrorBar() throws IOException {
		PixelIsland errorbar = PixelIslandList.createPixelIslandList(new File(G002_DIR, "errorbar.png")).get(0);
		Assert.assertEquals("pixels", 285, errorbar.size());
		PixelIsland barThin = new PixelIsland(errorbar);
		barThin.setDiagonal(true);
		barThin.findRidge();
		List<Pixel> edgeList = barThin.getPixelsWithValue(1);
		Assert.assertEquals("1:", 181, edgeList.size());
		List<Pixel> list2 = barThin.growFrom(edgeList, 1);
		Assert.assertEquals("2:", 49, list2.size());
		List<Pixel> list3 = barThin.growFrom(list2, 2);
		Assert.assertEquals("3:", 34, list3.size());
		List<Pixel> list4 = barThin.growFrom(list3, 3);
		Assert.assertEquals("4:", 18, list4.size());
		List<Pixel> list5 = barThin.growFrom(list4, 4);
		Assert.assertEquals("5:", 3, list5.size());
		List<Pixel> list6 = barThin.growFrom(list5, 5);
		Assert.assertEquals("6:", 0, list6.size());
		
	}

}
