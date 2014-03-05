package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Util;
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
	

	@Test
	public void testCorrelateFontsAll() throws Exception {
		List<File> helvticaFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFont.HELVETICA_DIR, new String[]{"png"}, false));
		List<File> genericFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFont.GENERIC_DIR, new String[]{"png"}, false));
		for (File helvFile : helvticaFiles) {
			BufferedImage helvImage = ImageIO.read(helvFile);
			int width = helvImage.getWidth();
			int height = helvImage.getHeight();
			String helvName = helvFile.getName();
			if (helvName.startsWith("_")) continue; // skip large images
			GrayCharacter helvChar = GrayCharacter.readGrayImage(helvImage);
			double corrmax = -999;
			String titlemax = null;
			for (File genFile : genericFiles) {
				BufferedImage genImage = ImageIO.read(genFile);
				BufferedImage scaledGenImage = Scalr.resize(genImage, Method.ULTRA_QUALITY, Mode.FIT_EXACT, width,
			            height);
				String genName = genFile.getName();
				GrayCharacter genChar = GrayCharacter.readGrayImage(scaledGenImage);
				String title = helvName+"_"+genName;
				double cor = helvChar.correlateGray(genChar, null/*title*/, true);
				if (cor > corrmax) {
					corrmax = cor;
					titlemax = title;
				}
			}
			System.out.println(Util.format(corrmax, 2)+": "+titlemax);
		}
	}
}
