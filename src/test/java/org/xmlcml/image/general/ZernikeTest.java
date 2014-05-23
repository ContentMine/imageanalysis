package org.xmlcml.image.general;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.compound.PixelList;
import org.xmlcml.image.processing.Pixel;
import org.xmlcml.image.processing.PixelIslandList;

public class ZernikeTest {

	public final static Logger LOG = Logger.getLogger(ZernikeTest.class);
	
	@Test
	// mainly to make sure new version is consistent
	public void testZernikeOrig() throws Exception {
		Assert.assertTrue("image", Fixtures.IMAGE_DIR.exists());
		Assert.assertTrue("general", Fixtures.GENERAL_DIR.exists());
		Assert.assertTrue("refFont", Fixtures.REFFONT_DIR.exists());
		BufferedImage image = ImageIO.read(new File(Fixtures.REFFONT_DIR, "65.png"));
		PixelIslandList pixelIslandList = PixelIslandList.createPixelIslandList(image);
		PixelList pixelList = pixelIslandList.getPixelList();
		Assert.assertEquals("pixels", 332, pixelList.size());
		Real2Array real2Array = Pixel.createReal2Array(pixelList);
		Assert.assertEquals("pixels", 332, real2Array.size());
		RealArray realArray = real2Array.getXArray();
		LOG.trace(realArray);
		double[] xarray = realArray.getArray();		
		double[] yarray = real2Array.getYArray().getArray();
		int npoints = xarray.length;
		int order = 1;
		ZernikeMomentsOrig.Complex[] complexArray = ZernikeMomentsOrig.zer_mmts(order, xarray, yarray, npoints);
		Assert.assertEquals("ncomplex", 2, complexArray.length);
		for (ZernikeMomentsOrig.Complex complex : complexArray) {
			LOG.trace(complex);
		}
	}
	
	@Test
	public void testZernikeOrig1() throws Exception {
		int order = 1;
		File file = new File(Fixtures.REFFONT_DIR, "65.png");
		ZernikeMomentsOrig.Complex[] complexArray = calculateComplex(order, file);
		for (ZernikeMomentsOrig.Complex complex : complexArray) {
			LOG.trace(complex);
		}
		complexArray = calculateComplex(2, file);
		for (ZernikeMomentsOrig.Complex complex : complexArray) {
			LOG.trace(complex);
		}
		for (int i = 65; i <= 126; i++) {
			file = new File(Fixtures.REFFONT_DIR, i+".png");
			debugMoments(file, 2, ""+(char)i);
		}
		debugMoments(new File(Fixtures.REFFONT_DIR, "18.png"), 2, "serifB");
	}

	private void debugMoments(File file, int order, String title) throws IOException {
		ZernikeMomentsOrig.Complex[] complexArray;
		if (file.exists()) {
			complexArray = calculateComplex(order, file);
			for (ZernikeMomentsOrig.Complex complex : complexArray) {
				LOG.trace(title+": "+complex);
			}
//			System.out.println();
		}
	}
	
	private ZernikeMomentsOrig.Complex[] calculateComplex(int order, File imageFile) throws IOException {
		BufferedImage image = ImageIO.read(imageFile);
		PixelIslandList pixelIslandList = PixelIslandList.createPixelIslandList(image);
		PixelList pixelList = pixelIslandList.getPixelList();
		Real2Array real2Array = Pixel.createReal2Array(pixelList);
		double[] xarray = real2Array.getXArray().getArray();		
		double[] yarray = real2Array.getYArray().getArray();
		ZernikeMomentsOrig.Complex[] complexArray = ZernikeMomentsOrig.zer_mmts(order, xarray, yarray, xarray.length);
		return complexArray;
	}
}
