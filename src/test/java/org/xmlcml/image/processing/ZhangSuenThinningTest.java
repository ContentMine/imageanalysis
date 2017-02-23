package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.graphics.image.ImageIOUtil;
import org.xmlcml.image.Fixtures;

public class ZhangSuenThinningTest {


	@Test
	public void testMolecule() throws IOException {
	       BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
	       Thinning thinningService = new ZhangSuenThinning(image);
	       thinningService.doThinning();
	       image = thinningService.getThinnedImage();
	       File thinnedPng = ImageIOUtil.writeImageQuietly(image, "target/thinning/zhangSuenMaltoryzine.png");
	       Assert.assertTrue(thinnedPng.exists());
	}

	// ==========================================================================
	
}
