package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class ColorUtilitiesTest {
	
	@Test
	public void testFlipBlackWhite() throws IOException {
		BufferedImage image  = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
		ColorUtilities.flipWhiteBlack(image);
		ImageIO.write(image, "png", new File("target/maltoryzineFlipped.png"));
	}
}
