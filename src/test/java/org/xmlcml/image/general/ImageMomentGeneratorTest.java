package org.xmlcml.image.general;

import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.moments.ImageMomentGenerator;

public class ImageMomentGeneratorTest {

	@Test
	public void testImageMomentGenerator() throws Exception {
		ImageMomentGenerator imageMomentGenerator = new ImageMomentGenerator();
		imageMomentGenerator.readImage(ImageIO.read(new File(Fixtures.REFFONT_DIR, "65.png")));
		imageMomentGenerator.readImage(ImageIO.read(new File(Fixtures.REFFONT_DIR, "66.png")));
		imageMomentGenerator.readImage(ImageIO.read(new File(Fixtures.REFFONT_DIR, "71.png")));
	}
}
