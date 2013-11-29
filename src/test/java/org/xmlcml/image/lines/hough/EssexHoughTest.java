package org.xmlcml.image.lines.hough;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.ColorUtilities;

import ac.essex.ooechs.imaging.commons.edge.hough.HoughLine;
import ac.essex.ooechs.imaging.commons.edge.hough.HoughTransform;

public class EssexHoughTest {


	private static final Logger LOG = Logger.getLogger(EssexHoughTest.class);

	@Test
	public void testMaltoryzineBinary() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.MALTORYZINE_BINARY_PNG); 
//	    binarizeImage(image, 1,255);
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(211, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), -1); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineBinaryHough.png"));
	}

	@Test
	public void testMaltoryzineFlipped() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MALTORYZINE_FLIPPED_PNG); 
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(41, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), HoughLine.NULL); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineBinaryHough.png"));
	}


	@Test
	public void testMoleculeCanny1() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MOLECULE_CANNY_1_PNG); 
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    int threshold = 30;
	    Vector<HoughLine> lines = h.getLines(threshold); 
	    Assert.assertEquals(650, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), HoughLine.NULL); 
	    } 
	    ImageIO.write(image, "png", new File("target/moleculeCanny1.png"));
	}

	@Test
	public void testMoleculeBinaryCanny1() throws IOException {
	    BufferedImage image = ImageIO.read(Fixtures.MOLECULE_BINARY_CANNY_1_PNG); 
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    int threshold = 80;
	    Vector<HoughLine> lines = h.getLines(threshold); 
//	    Assert.assertEquals(49, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), HoughLine.NULL); 
	    } 
	    ImageIO.write(image, "png", new File("target/moleculeBinaryCannyHough80.png"));
	}
	@Test
	public void testEthane() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.ETHANE_PNG); 
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    Vector<HoughLine> lines = h.getLines(30); 
	    Assert.assertEquals(58, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), HoughLine.NULL); 
	    } 
	    ImageIO.write(image, "png", new File("target/ethane.png"));
	}

	@Test
	public void testMaltoryzineThinnedFlipped() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG); 
		ColorUtilities.flipWhiteBlack(image);
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    // this seems fairly critical
	    Vector<HoughLine> lines = h.getLines(25); 
	    Assert.assertEquals(15, lines.size());
	    // draw the lines back onto the image 
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), HoughLine.NULL); 
	    } 
	    ImageIO.write(image, "png", new File("target/maltoryzineThinnedHough.png"));
	}

	@Test
	public void testMaltoryzineSegments() throws IOException {
	    BufferedImage image = javax.imageio.ImageIO.read(Fixtures.MALTORYZINE_THINNED_PNG); 
		ColorUtilities.flipWhiteBlack(image);
	    HoughTransform h = new HoughTransform(image); 
	    h.addPoints(); 
	    // this seems fairly critical
	    Vector<HoughLine> lines = h.getLines(25); 
	    Assert.assertEquals(15, lines.size());
	    // draw the lines back onto the image and outline where overlap
	    SVGG g = new SVGG();
	    for (int j = 0; j < lines.size(); j++) { 
	        HoughLine line = lines.elementAt(j); 
	        line.draw(image, Color.RED.getRGB(), Color.YELLOW.getRGB()); 
	        List<SVGLine> segmentList = line.getSegments();
	        LOG.debug(segmentList.size());
	        Int2 min = line.getMinPoint();
	        Int2 max = line.getMaxPoint();
	        SVGLine svgLine = new SVGLine(
	        		new Real2(min.getX(), min.getY()),
	        		new Real2(max.getX(), max.getY())
	        		);
	        svgLine.setStrokeWidth(1.4);
	        svgLine.setStroke("red");
	        g.appendChild(svgLine);
	        for (SVGLine segment : segmentList) {
	        	g.appendChild(segment);
	        }
	    } 
	    SVGSVG.wrapAndWriteAsSVG(g, new File("target/maltoryzineLines.svg"));
	    ImageIO.write(image, "png", new File("target/maltoryzineThinnedHough.png"));
	    h.resetToBlack(Color.RED.getRGB());
	    h.resetToBlack(Color.YELLOW.getRGB());
	    ImageIO.write(image, "png", new File("target/maltoryzineThinnedHoughNew.png"));
	}


	// =======================================


}
