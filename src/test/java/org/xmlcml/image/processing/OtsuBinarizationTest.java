package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

/** probably superseded by BoofCV
 * 
 * @author pm286
 *
 */
	
@Ignore // not using this ; replaced by BoofCV
public class OtsuBinarizationTest {

	@Test
	public void testBinarize1() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_CANNY_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), new File("target/moleculeCannyBinarize.png"));        
	}
	

	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testBinarizeMolecule() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_20131119_A_JPG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), new File("target/molecule20131119Binary.png"));        
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
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), new File("target/largePhylo.png"));        
	}
	
	

}
