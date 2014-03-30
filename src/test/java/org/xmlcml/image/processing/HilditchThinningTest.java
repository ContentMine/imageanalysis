package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

public class HilditchThinningTest {


	@Test
	public void testMolecule() throws IOException {
	       BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
	       Thinning thinningService = new HilditchThinning(image);
	       thinningService.doThinning();
	       image = thinningService.getThinnedImage();
	       File thinnedPng = ImageUtil.writeImageQuietly(image, "target/thin/maltoryzineHilditch.png");
	       Assert.assertTrue(thinnedPng.exists());
	}

	// ==========================================================================
	
}
