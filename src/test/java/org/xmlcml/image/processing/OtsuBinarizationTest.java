package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

public class OtsuBinarizationTest {

	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testBinarize() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MALTORYZINE_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.current, new File("target/maltoryzineBinary.png"));        
	}

	@Test
	public void testBinarize1() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_CANNY_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.current, new File("target/moleculeCannyBinarize.png"));        
	}
	

	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testBinarizeMolecule() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_20131119_A_JPG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.current, new File("target/molecule20131119Binary.png"));        
	}
	
	@Test
	public void testBinarizeJPEG() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.LARGE_PHYLO_JPG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        BufferedImage image = otsuBinarize.getBinarizedImage();
        Assert.assertEquals(1705, image.getHeight());
        Assert.assertEquals(1200, image.getWidth());
        ImageUtil.writeImageQuietly(otsuBinarize.current, new File("target/largePhylo.png"));        
	}
	
	

}
