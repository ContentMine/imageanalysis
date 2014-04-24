package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;

import org.junit.Test;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageUInt8;

public class BoofCVTest {

	@Test
	public void testBinarize() {
		BufferedImage image = UtilImageIO.loadImage("src/test/resources/org/xmlcml/image/processing/postermol.png");
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
	}

}
