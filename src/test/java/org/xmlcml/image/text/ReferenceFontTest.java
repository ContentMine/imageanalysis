package org.xmlcml.image.text;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.processing.PixelIsland;

public class ReferenceFontTest {
	
	private final static Logger LOG = Logger.getLogger(ReferenceFontTest.class);
	@Test
	@Ignore
	public void testCreateHelveticaIslands() throws Exception {
		Assert.assertTrue(ReferenceFont.WP_HELVETICA_PNG.exists());
		Assert.assertFalse(ReferenceFont.WP_HELVETICA_PNG.isDirectory());
		BufferedImage image = ImageIO.read(ReferenceFont.WP_HELVETICA_PNG);
		PixelIsland island = ReferenceFont.HELVETICA.getPixelIsland("A");
		Assert.assertNotNull(island);
	}

	@Test
	public void testCreateFonts() throws Exception {
		Assert.assertTrue(ReferenceFont.HELVETICA_DIR.exists());
		Assert.assertTrue(ReferenceFont.HELVETICA_DIR.isDirectory());
		BufferedImage image = ReferenceFont.HELVETICA.getImage("A");
		Assert.assertNotNull(image);
		BufferedImage image1 = ReferenceFont.GENERIC.getImage("latin_capital_letter_a");
		Assert.assertNotNull(image1);
	}


	@Test
	public void testCorrelateFonts() throws Exception {
		BufferedImage image0 = ReferenceFont.HELVETICA.getImage("A");
		GrayCharacter character0 = GrayCharacter.readGrayImage(image0);
		BufferedImage image1 = ReferenceFont.GENERIC.getImage("latin_capital_letter_a");
		GrayCharacter character1 = GrayCharacter.readGrayImage(image1);
		double cor = character0.correlateGray(character1, "A0-1", true);
		LOG.debug(cor);
	}
}
