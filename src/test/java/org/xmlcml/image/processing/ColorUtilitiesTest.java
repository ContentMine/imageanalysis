package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

public class ColorUtilitiesTest {
	
	@Test
	public void testFlipBlackWhite() throws IOException {
		BufferedImage image  = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
		ColorUtilities.flipWhiteBlack(image);
		ImageUtil.writeImageQuietly(image, new File("target/colourutils/maltoryzineFlipped.png"));
	}
}
