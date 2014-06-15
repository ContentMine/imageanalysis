package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntSet;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.compound.PixelList;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class BoofCVTest {

	private static Logger LOG = Logger.getLogger(BoofCVTest.class);

	private static final String IMAGE_PROCESSING = "src/test/resources/org/xmlcml/image/processing/";
	private File BOOFCV_OUT_DIR;
	
	
	@Before
	public void setUp() {
		BOOFCV_OUT_DIR = new File("target/boofcv/");
		BOOFCV_OUT_DIR.mkdirs();
	}
	@Test
	public void testInt2() {
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"postermol.png");
		ImageUInt8 imageInt2 = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		BufferedImage outImage = ConvertBufferedImage.convertTo(imageInt2,null);
		UtilImageIO.saveImage(outImage, new File(BOOFCV_OUT_DIR, "postermol.png").toString());
	}

	@Test
	public void testBinarize() {
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"postermol.png");
		ImageUInt8 input = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 binary = new ImageUInt8(input.getWidth(), input.getHeight());
//		Creates a binary image by thresholding the input image. Binary must be of type ImageUInt8.
		BufferedImage binaryImage = null;
		for (int i = 10; i < 150; i+= 10) {
			ThresholdImageOps.threshold(input, binary, i, true);
			binaryImage = VisualizeBinaryData.renderBinary(binary,null);
			UtilImageIO.saveImage(binaryImage, new File(BOOFCV_OUT_DIR, "postermolBinary"+i+".png").toString());
		}
		int best = 70;
		ThresholdImageOps.threshold(input, binary, best, true);
//		Apply an erode operation on the binary image, writing over the original image reference.
		ImageUInt8 erode8 = BinaryImageOps.erode8(binary,null);
		erode8 = BinaryImageOps.erode8(erode8,null);
		outputBinary(erode8, new File(BOOFCV_OUT_DIR, "postermolErode"+best+"_8.png"));
		
		ImageUInt8 output = new ImageUInt8(input.getWidth(), input.getHeight());
//		Apply an erode operation on the binary image, saving results to the output binary image.
//		BinaryImageOps.erode8(binary,output);
//		Apply an erode operation with a 4-connect rule.
		BinaryImageOps.erode4(binary,output);
		ImageUInt8 erode4 = BinaryImageOps.erode4(binary,null);
		outputBinary(erode4, new File(BOOFCV_OUT_DIR, "postermolErode"+best+"_4.png"));
//		int numBlobs = BinaryImageOps.labelBlobs4(binary,blobs);
//		Detect and label blobs in the binary image using a 4-connect rule. blobs is an image of type ImageSInt32.
//		BufferedImage visualized = VisualizeBinaryData.renderLabeled(blobs, numBlobs, null);
//		Renders the detected blobs in a colored image.
		BufferedImage outputImage = VisualizeBinaryData.renderBinary(output,null);
//		Renders the binary image as a black white image.		
	}
	
	@Test
	public void testMoreBinary() {
		// load and convert the image into a usable format
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"postermol.png");
 
		// convert into a usable format
		ImageFloat32 input = ConvertBufferedImage.convertFromSingle(image, null, ImageFloat32.class);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
		ImageSInt32 label = new ImageSInt32(input.width,input.height);
 
		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);
 
		// create a binary image by thresholding
		ThresholdImageOps.threshold(input,binary,(float)mean,true);
 
		// remove small blobs through erosion and dilation
		// The null in the input indicates that it should internally declare the work image it needs
		// this is less efficient, but easier to code.
		ImageUInt8 filtered = BinaryImageOps.erode8(binary,null);
		filtered = BinaryImageOps.dilate8(filtered, null);
		outputBinary(filtered, new File(BOOFCV_OUT_DIR, "dilate8.png"));
 
		// Detect blobs inside the image using an 8-connect rule
		List<Contour> contours = BinaryImageOps.contour(filtered, 8, label);
 
		// colors of contours
		int colorExternal = 0xFFFFFF;
		int colorInternal = 0xFF2020;
 
		// display the results
		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, null);
		BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, null);
		BufferedImage visualLabel = VisualizeBinaryData.renderLabeled(label, contours.size(), null);
		BufferedImage visualContour = VisualizeBinaryData.renderContours(contours,colorExternal,colorInternal,
				input.width,input.height,null);
 
		// these are not suitable for tests
		
//		ShowImages.showWindow(visualBinary,"Binary Original");
//		ShowImages.showWindow(visualFiltered,"Binary Filtered");
//		ShowImages.showWindow(visualLabel,"Labeled Blobs");
//		ShowImages.showWindow(visualContour,"Contours");
	}
 
	@Test
	public void testBlobs() {
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"postermol.png");
		ImageUInt8 input = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 binary = new ImageUInt8(input.getWidth(), input.getHeight());
//		Creates a binary image by thresholding the input image. Binary must be of type ImageUInt8.
		BufferedImage binaryImage = null;
		for (int i = 10; i < 150; i+= 10) {
			ThresholdImageOps.threshold(input, binary, i, true);
			binaryImage = VisualizeBinaryData.renderBinary(binary,null);
			UtilImageIO.saveImage(binaryImage, new File(BOOFCV_OUT_DIR, "postermolBinary"+i+".png").toString());
		}
		int best = 70;
		ThresholdImageOps.threshold(input, binary, best, true);
		ImageSInt32 blobs = new ImageSInt32();
//		??
//		int numBlobs = BinaryImageOps.labelBlobs4(binary,blobs);
//		Detect and label blobs in the binary image using a 4-connect rule. blobs is an image of type ImageSInt32.
//		BufferedImage visualized = VisualizeBinaryData.renderLabeled(blobs, numBlobs, null);	
	}
	
	@Test
	public void testPosterize() {
		int nvalues = 4; // i.e. 16-bit color
		nvalues = 2;
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"phylo.jpg");
		ImageUtil.flattenImage(image, nvalues);
		ColourAnalyzer colorAnalyzer = new ColourAnalyzer(image);
		Multiset<Integer> set = colorAnalyzer.getColorSet();
		for (Entry<Integer> entry : set.entrySet()) {
			int ii = ((int) entry.getElement()) & 0x00ffffff;
			// uncomment for debug
//			System.out.println(Integer.toHexString(ii)+"  "+entry.getCount()); 
		}
		/**
		    ff  26      BLUE
		  7f7f  351     cyan
		7f7f7f  34019   grey
		7fffff  80      cyanw
		  7fff  49      cyan
		    7f  2301    blue
		7fff7f  586     greenw
		7f7fff  102006  cyanw
		7f007f  27      magenta
		     0  25578   BLACK
		ffff7f  40      yellow
		ff7fff  37      magenta
		7f0000  2863    red
		  7f00  489     green
		ff7f7f  1562    redw
		7f7f00  1676    yellow
		ffffff  937110  white
		*/

		ImageUtil.writeImageQuietly(image, new File("target/posterize.png"));
	}
	
	@Test
	public void testPosterize22249() {
		int nvalues = 4; // i.e. 16-bit color
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"22249.png");
		BufferedImage image1 = ImageUtil.flattenImage(image, nvalues);
		ColourAnalyzer colorAnalyzer = new ColourAnalyzer(image1);
		Multiset<Integer> set = colorAnalyzer.getColorSet();
		LOG.trace(set.entrySet().size());
		IntArray colorValues = new IntArray();
		IntArray colorCount = new IntArray();
		for (Entry<Integer> entry : set.entrySet()) {
			int ii = ((int) entry.getElement()) & 0x00ffffff;
			colorValues.addElement(ii);
			colorCount.addElement(entry.getCount());
		}
		IntSet index = colorCount.indexSortDescending();
		for (int ii = 0; ii < index.size(); ii++) {
			int idx = index.elementAt(ii);
			int colorValue = colorValues.elementAt(idx);
			String hex = Integer.toHexString(colorValue);
			PixelList pixelList = PixelList.createPixelList(image1, colorValue);
			// plot some images
			if (ii > 0 && ii < 4) {
				SVGG g = new SVGG();
				pixelList.plotPixels(g, "#"+hex);
				SVGSVG.wrapAndWriteAsSVG(g, new File("target/"+hex+".svg"));
			}
			LOG.trace("size "+pixelList.size());
		}

		ImageUtil.writeImageQuietly(image1, new File("target/22249.png"));
	}
	
	@Test
	public void testPosterize36933() {
		int nvalues = 4; // i.e. 16-bit color
		BufferedImage image = UtilImageIO.loadImage(IMAGE_PROCESSING+"36933.png");
		BufferedImage imageNew = ImageUtil.flattenImage(image, nvalues);
		ColourAnalyzer colorAnalyzer = new ColourAnalyzer(imageNew);
		Multiset<Integer> set = colorAnalyzer.getColorSet();
		for (Entry<Integer> entry : set.entrySet()) {
			int ii = ((int) entry.getElement()) & 0x00ffffff;
			System.out.println(Integer.toHexString(ii)+"  "+entry.getCount());
		}

		ImageUtil.writeImageQuietly(imageNew, new File("target/36933.png"));
	}
	

	// ====================================
	
	private static void outputBinary(ImageUInt8 image, File file) {
		BufferedImage binaryImage = VisualizeBinaryData.renderBinary(image,null);
		UtilImageIO.saveImage(binaryImage, file.toString());
	}
	
	

	
}
