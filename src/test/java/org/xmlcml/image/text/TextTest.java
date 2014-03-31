package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ZhangSuenThinning;

public class TextTest {
	private final static Logger LOG = Logger.getLogger(TextTest.class);

	@Test
	public void testThin() throws Exception {
    	OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(Fixtures.NRRL_PNG);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), new File("target/textthin/NRRL.binarize.png"));
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageUtil.writeImageQuietly(image, "target/textthin/NRRL.thin.png");
		
	}
	
	@Test
	public void testThinNO2() throws Exception {
		binarizeThin(Fixtures.NO2, "NO2");
	}
	
	@Test
	public void testThinCyclopiazonic() throws Exception {
		binarizeThin("Cyclopiazonic", true);
	}
	
	@Test
	public void testThinDithiobicycle() throws Exception {
		binarizeThin("dithiobicycle", false);
	}
	
	@Test
	public void testThinNumbers() throws Exception {
		binarizeThin("numbers", false);
	}
	
	@Test
	public void testThinSugar() throws Exception {
		binarizeThin("sugar", false);
	}

	@Test
	public void testThinYaxis() throws Exception {
		binarizeThin("yaxis", false);
	}

	@Test
	public void testNaringin() throws Exception {
		binarizeThin("naringin", false);
	}

	

	@Test
	public void testSubPixel() throws Exception {
		BufferedImage bImage = ImageIO.read(new File(Fixtures.TEXT_DIR, "mini.png"));
		AntiColour[] anti = new AntiColour[24];
		for (int j = 12; j < Math.min(26, bImage.getHeight()); j++) {
			for (int i = 18; i < bImage.getWidth(); i++) {
				anti[i] = new AntiColour(bImage, i, j);
				LOG.trace(i+"/"+j+"/"+anti[i].ared+"/"+anti[i].agreen+"/"+anti[i].ablue+"   ");
			}
//			System.out.println();
			AntiColour[] newAnti = new AntiColour[24];
			for (int i = 19; i < bImage.getWidth()-1; i++) {
				AntiColour left = anti[i].getLeft();
				AntiColour right = anti[i].getRight();
//				System.out.print(i+"/"+j+" / ");
//				System.out.print(left.ared+"/"+left.agreen+"/"+left.ablue+"   ");
//				System.out.print(anti[i].ared+"/"+anti[i].agreen+"/"+anti[i].ablue+"   ");
//				System.out.print(right.ared+"/"+right.agreen+"/"+right.ablue+"   ");
//				System.out.println();
			}
		}
	}
	
	@Test
	@Ignore // seem to have lost image
	public void testSubPixelC() throws Exception {
		BufferedImage bImage = ImageIO.read(new File(Fixtures.TEXT_DIR, "C.png"));
		AntiColour[] anti = new AntiColour[24];
		for (int j = 4; j < Math.min(18, bImage.getHeight()); j++) {
			for (int i = 0; i < bImage.getWidth(); i++) {
				anti[i] = new AntiColour(bImage, i, j);
				LOG.debug(i+"/"+j+" "+anti[i].ared+"/"+anti[i].agreen+"/"+anti[i].ablue+"   ");
			}
			LOG.debug("");
		}
	}
	
	// =============================================

	private void binarizeThin(File imageFile, String abbrev) throws IOException {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(imageFile);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), new File("target/thin/"+abbrev+".binarize.png"));
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageUtil.writeImageQuietly(image, new File("target/binarizeThin/"+abbrev+".thin.png"));
	}
	
	private void binarizeThin(String abbrev, boolean sharp) throws IOException {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
        otsuBinarize.read(new File(Fixtures.TEXT_DIR, abbrev+".png"));
        otsuBinarize.toGray();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), "target/binarizeThin/"+abbrev+".gray.png");
        if (sharp) {
	        otsuBinarize.sharpenGray();
	        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), "target/binarizeThin/"+abbrev+".sharp.png");
        }
        otsuBinarize.binarize();
        ImageUtil.writeImageQuietly(otsuBinarize.getCurrent(), "target/binarizeThin/"+abbrev+".binarize.png");
        BufferedImage image = otsuBinarize.getBinarizedImage();
        ZhangSuenThinning thinningService = new ZhangSuenThinning(image);
        thinningService.doThinning();
        image = thinningService.getThinnedImage();
        ImageUtil.writeImageQuietly(image, "target/binarizeThin/"+abbrev+".thin.png");
	}
	
	
}
