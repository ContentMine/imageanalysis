package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ThinningService;

public class TextTest {

	@Test
	public void testThin() throws Exception {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.NRRL_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/NRRL.binarize.png"));
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ThinningService thinningService = new ThinningService(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageIO.write(image, "png", new File("target/NRRL.thin.png"));
		
	}
	
	@Test
	public void testThinNO2() throws Exception {
		binarizeThin(Fixtures.NO2, "NO2");
	}
	
	@Test
	public void testThinCyclopiazonic() throws Exception {
		binarizeThin("Cyclopiazonic");
	}
	
	@Test
	public void testThinDithiobicycle() throws Exception {
		binarizeThin("dithiobicycle");
	}
	
	@Test
	public void testThinNumbers() throws Exception {
		binarizeThin("numbers");
	}
	
	@Test
	public void testThinSugar() throws Exception {
		binarizeThin("sugar");
	}
	
	// =============================================

	private void binarizeThin(File imageFile, String abbrev) throws IOException {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(imageFile);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/"+abbrev+".binarize.png"));
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ThinningService thinningService = new ThinningService(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageIO.write(image, "png", new File("target/"+abbrev+".thin.png"));
	}
	
	private void binarizeThin(String abbrev) throws IOException {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(new File(Fixtures.TEXT_DIR, abbrev+".png"));
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        otsuBinarize.writeImage(new File("target/"+abbrev+".binarize.png"));
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ThinningService thinningService = new ThinningService(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageIO.write(image, "png", new File("target/"+abbrev+".thin.png"));
	}
	
	
}
