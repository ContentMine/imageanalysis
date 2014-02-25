package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ThinningService;

public class ThinnedTextTest {

	@Test
	public void testHelvetica() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.HELVETICA_PNG);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/helvetica.png"));
	}

	@Test
	public void testHelveticaBold() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.HELVETICA_BOLD_PNG);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/helveticaBold.png"));
	}

	@Test
	public void testMonospaced() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.MONOSPACE_PNG);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/monospace.png"));
	}

	@Test
	public void testTimes() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.TIMES_GIF);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/times.png"));
	}

	@Test
	public void testLucida() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.LUCIDA_PNG);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/lucida.png"));
	}

	@Test
	// this example is rubbish
	public void testTimesRoman() throws Exception {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.read(Fixtures.TIMESROMAN_JPG);
		otsuBinarize.toGray();
		otsuBinarize.binarize();
		BufferedImage image = otsuBinarize.getBinarizedImage();
		ImageIO.write(image, "png", new File("target/timesroman0.png"));
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/timesroman.png"));
	}

	@Test
	// badly antialised
	public void testGibbonTree() throws Exception {
		BufferedImage image = ImageIO.read(Fixtures.GIBBONS_PNG);
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/gibbons.png"));
	}

	@Test
	// poor
	public void testGraph() throws Exception {
		BufferedImage image = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"bmcgraph.jpg"));
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/bmcgraph.png"));
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
		ImageIO.write(image, "png", new File("target/"+original+"binary.png"));
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		ImageIO.write(image, "png", new File("target/"+original+".png"));
	}

}
