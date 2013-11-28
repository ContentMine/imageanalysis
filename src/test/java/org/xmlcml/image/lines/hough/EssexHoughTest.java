package org.xmlcml.image.lines.hough;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.ColorUtilities;

import ac.essex.ooechs.imaging.commons.edge.hough.HoughLine;
import ac.essex.ooechs.imaging.commons.edge.hough.HoughTransform;

public class EssexHoughTest {


	@Test
	public void testMaltoryzineBinary() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG); 
//	    binarizeImage(image, 1,255);
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(211, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineBinaryHough.png"));
	}

	@Test
	public void testMaltoryzineFlipped() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_FLIPPED_PNG); 
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(41, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineBinaryHough.png"));
	}


	@Test
	public void testMoleculeCanny1() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MOLECULE_CANNY_1_PNG); 
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    int threshold = 30;
	    Vector<HoughLine> lines = h.getLines(threshold); 
	    Assert.assertEquals(650, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/moleculeCanny1.png"));
	}

	@Test
	public void testMoleculeBinaryCanny1() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MOLECULE_BINARY_CANNY_1_PNG); 
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    int threshold = 80;
	    Vector<HoughLine> lines = h.getLines(threshold); 
//	    Assert.assertEquals(49, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/moleculeBinaryCannyHough80.png"));
	}
	@Test
	public void testEthane() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.ETHANE_PNG); 
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(58, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/ethane.png"));
	}

	@Test
	public void testMaltoryzineThinnedFlipped() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG); 
		ColorUtilities.flipWhiteBlack(image);
//	    binarizeImage(image, 1,255);
	    HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
	    h.addPoints(image); 
	    // this seems fairly critical
	    Vector<HoughLine> lines = h.getLines(25); 
	    Assert.assertEquals(15, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB()); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineThinnedHough.png"));
	}


	// =======================================


}
