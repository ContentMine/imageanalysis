package org.xmlcml.image.processing;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class OtsuBinarizationTest {

	@Test
	public void testBinarize() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MALTORYZINE_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/maltoryzineBinary.png"));        
	}

	@Test
	public void testBinarize1() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_CANNY_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/moleculeCannyBinarize.png"));        
	}
	

	@Test
	public void testBinarizeMolecule() throws IOException {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.MOLECULE_20131119_A_JPG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/molecule20131119Binary.png"));        
	}
}
