package org.xmlcml.image.colour;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

public class RGBMatrixTest {
	private static final Logger LOG = Logger.getLogger(RGBMatrixTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test 
	/** rgb values
	 * 
	 */
	public void testRGB() throws IOException {
		BufferedImage  image = ImageIO.read(new File(Fixtures.COMPOUND_DIR, "journal.pone.0095816.g002.png"));
		RGBMatrix rgbMatrix = RGBMatrix.extractMatrix(image);
		IntMatrix red = rgbMatrix.getMatrix(ImageUtil.RED);
		rgbMatrix.invertRgb();
		red = rgbMatrix.getMatrix(ImageUtil.RED);
//		RGBMatrix.debug(red);
	}
	
	/** apply simple sharpening function to image.
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testSharpenImage() throws IOException {
		BufferedImage newImage = null;
		IntArray array = new IntArray(new int[]{1, 1, 1});
		array = ImageUtil.SHARPEN_ARRAY;
//		array = ImageUtil.IDENT_ARRAY;
//		array = ImageUtil.DOUBLE_ARRAY;
//		array = ImageUtil.SMEAR_ARRAY;
		array = ImageUtil.EDGE_ARRAY;
		BufferedImage  image = ImageIO.read(new File(Fixtures.COMPOUND_DIR, "journal.pone.0095816.g002.png"));
		if (image != null) {
			RGBMatrix rgbMatrix = RGBMatrix.extractMatrix(image);
			RGBMatrix rgbMatrix1 = rgbMatrix.applyFilter(array);
//			rgbMatrix1 = rgbMatrix;
			newImage = rgbMatrix1.createImage(image.getType());
		}
		ImageUtil.writeImageQuietly(newImage, new File("target/sharpen/sharpened.png"));
	}



}
