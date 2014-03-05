package org.xmlcml.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.image.text.GrayCharacter;

public class ImageUtilTest {

	@Test
	/** clip rectangle out of image.
	 * 
	 * @throws IOException
	 */
	public void testClipSubImage() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		Rectangle rect = new Rectangle(20, 50, 60, 85); // x0, y0, w, h
		Raster raster = image.getData(rect);
		Assert.assertEquals(60, raster.getWidth());
		Assert.assertEquals(85, raster.getHeight());
		BufferedImage subImage = new BufferedImage(raster.getWidth(), raster.getHeight(), image.getType());
		subImage.setData(raster);
		ImageIO.write(subImage, "png", new File("target/subImage.png"));
	}
	
	@Test
	/** clip a rectangle out of an image.
	 * 
	 * @throws IOException
	 */
	public void testClipSub() throws IOException {
		BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG);
		Int2Range boundingBox = new Int2Range(new IntRange(20, 80), new IntRange(50, 135));
		BufferedImage subImage = ImageUtil.clipSubImage(image, boundingBox);
		ImageIO.write(subImage, "png", new File("target/subImage1.png"));
	}

	@Test
	/** check a few pixels to identify the gray values.
	 * 
	 * @throws IOException
	 */
	public void testGray() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.PROCESSING_DIR, "colors.png"));
		Assert.assertNotNull("not null", image);
		BufferedImage imageg = ImageUtil.binarizeToGray(image);
		Assert.assertNotNull("not nullg", imageg);
		Assert.assertEquals(252, imageg.getRGB(10,0) & 0xff);
		Assert.assertEquals(167, imageg.getRGB(10,140) & 0xff);
		Assert.assertEquals(168, imageg.getRGB(30,140) & 0xff);
		Assert.assertEquals(63, imageg.getRGB(30,135) & 0xff);
		Assert.assertEquals(70, imageg.getRGB(30,130) & 0xff);
		Assert.assertEquals(252, imageg.getRGB(30,120) & 0xff);
		ImageIO.write(imageg, "png", new File("target/gray.png"));
	}

	@Test
	/** shifts image by dx dy.
	 * 
	 */
	public void testReadGrayImage() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		IntMatrix matrix = ImageUtil.getGrayMatrix(image);
//		System.out.println(matrix);
	}

	@Test
	/** shifts image by dx dy.
	 * 
	 */
	public void testShiftGrayImage() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		BufferedImage shiftedImage = ImageUtil.shiftImage(image, 0.1, 0.2);
		ImageIO.write(shiftedImage, "png", new File("target/shiftedImage.png"));
	}

	@Test
	/** scales image.
	 * 
	 */
	public void testScaleAndInterpolate() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		BufferedImage shiftedImage = ImageUtil.scaleAndInterpolate(image, 17, 13);
		ImageIO.write(shiftedImage, "png", new File("target/scaledImage.png"));
	}

	@Test
	/** shifts image to  centre.
	 * 
	 */
	public void testShiftGrayImageToCentre() throws IOException {
		BufferedImage image = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		GrayCharacter character = GrayCharacter.readGrayImage(image);
		Real2 centre = character.getCentre();
//		LOG.debug(centre);
		BufferedImage shiftedImage = ImageUtil.shiftImage(image, 4-centre.getX(), 5-centre.getY());
		ImageIO.write(shiftedImage, "png", new File("target/shiftedImage1.png"));
	}


}
