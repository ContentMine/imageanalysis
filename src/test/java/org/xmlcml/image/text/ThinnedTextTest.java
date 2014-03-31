package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ZhangSuenThinning;

public class ThinnedTextTest {

	private static final String TARGET_FONTS = "target/fonts/";

	@Test
	public void testHelvetica() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.HELVETICA_PNG);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"helvetica.png");
	}

	@Test
	public void testHelveticaBold() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.HELVETICA_BOLD_PNG);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"helveticaBold.png");
	}

	@Test
	public void testMonospaced() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MONOSPACE_PNG);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"monospace.png");
	}

	@Test
	public void testTimes() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.TIMES_GIF);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"times.png");
	}

	@Test
	public void testLucida() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.LUCIDA_PNG);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"lucida.png");
	}

	@Test
	// this example is rubbish
	@Ignore
	public void testTimesRoman() throws Exception {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.read(Fixtures.TIMESROMAN_JPG);
		otsuBinarize.toGray();
		otsuBinarize.binarize();
		BufferedImage image = otsuBinarize.getBinarizedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"timesroman0.png");
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"timesroman.png");
	}

	@Test
	// badly antialised
	public void testGibbonTree() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.GIBBONS_PNG);
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"gibbons.png");
	}

	@Test
	// poor
	public void testGraph() throws Exception {
		BufferedImage image = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"bmcgraph.jpg"));
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+"bmcgraph.png");
	}

	@Test
	// poor
	public void testGraph1() throws Exception {
		binarizethin("bmcgraph.jpg");
//		binarizethin("bmcgraph1.jpg");
		binarizethin("bmcgraph2.jpg"); //small
	}

	@Test
	public void testGraph2() throws Exception {
		binarizethin("phylo.jpg");
	}

	private void binarizethin(String original) throws IOException {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.read(new File(Fixtures.TEXT_DIR, original));
		otsuBinarize.toGray();
		otsuBinarize.binarize();
		BufferedImage image = otsuBinarize.getBinarizedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+original+"binary.png");
		ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageUtil.writeImageQuietly(image, TARGET_FONTS+original+".png");
	}

}
