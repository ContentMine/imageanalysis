package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class ZhangSuenThinningTest {

//	@Test
//	public void testThinning() throws IOException {
//		
//	    BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
//	    ZhangSuenThinning thinning = new ZhangSuenThinning(image);
//	    thinning.appplyThinning();
//	    thinning.getImage();
//	    thinning.writeDataToImage(image);
//	
//	    ImageIO.write(image, "png", new File("target/thinnedMaltoryzine.png"));
//	}
	 
//	@Test
//	public void testNayef() throws IOException {
////	       BufferedImage image = ImageIO.read(new File("/home/nayef/Desktop/bw.jpg"));
//	       BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
//
//	       int[][] imageData = new int[image.getHeight()][image.getWidth()];
//	       
//	       copyImageToBinary(image, imageData);
//
//	       ThinningService thinningService = new ThinningService();
//	    
//	       thinningService.doZhangSuenThinning(imageData);
//	        
//	       copyBinaryToImage(image, imageData);
//
//	       ImageIO.write(image, "png", new File("target/thinnedMaltoryzine.png"));
//	}

	@Test
	public void testMolecule() throws IOException {
	       BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG);
	       ThinningService thinningService = new ThinningService(image);
	       thinningService.doThinning();
	       image = thinningService.getThinnedImage();
	       File thinned = new File("target/thinnedMaltoryzine.png");
	       ImageIO.write(image, "png", thinned);
	       Assert.assertTrue(thinned.exists());
	}

	// ==========================================================================
	
//	private void copyBinaryToImage(BufferedImage image, int[][] imageData) {
//		for (int y = 0; y < imageData.length; y++) {
//
//	           for (int x = 0; x < imageData[y].length; x++) {
//
//	               if (imageData[y][x] == 1) {
//	                   image.setRGB(x, y, Color.BLACK.getRGB());
//
//	               } else {
//	                   image.setRGB(x, y, Color.WHITE.getRGB());
//	               }
//
//
//	           }
//	       }
//	}
//
//	private void copyImageToBinary(BufferedImage image, int[][] imageData) {
//		for (int y = 0; y < imageData.length; y++) {
//	           for (int x = 0; x < imageData[y].length; x++) {
//
//	               if (image.getRGB(x, y) == Color.BLACK.getRGB()) {
//	                   imageData[y][x] = 1;
//	               } else {
//	                   imageData[y][x] = 0;
//
//	               }
//	           }
//	       }
//	}
}
