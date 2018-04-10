package org.xmlcml.image.colour;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.graphics.svg.util.ImageIOUtil;
import org.xmlcml.image.ImageAnalysisFixtures;

public class ColorUtilitiesTest {
	
	@Test
	public void testFlipBlackWhite() throws IOException {
		BufferedImage image  = ImageIO.read(ImageAnalysisFixtures.MALTORYZINE_BINARY_PNG);
		ColorUtilities.flipWhiteBlack(image);
		ImageIOUtil.writeImageQuietly(image, new File("target/colourutils/maltoryzineFlipped.png"));
	}
}
