package org.xmlcml.image.colour;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.IntSet;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.pixel.PixelList;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/** analyzes images for colours.
 * 
 * Typically attempts to find blocks of color and separate different regions.
 * 
 * @author pm286
 *
 */
/**
valuable approach to restricting numbers of colours
http://stackoverflow.com/questions/4057475/rounding-colour-values-to-the-nearest-of-a-small-set-of-colours
http://stackoverflow.com/questions/7530627/hcl-color-to-rgb-and-backward
https://en.wikipedia.org/wiki/Color_quantization

 * 
 * @author pm286
 *
 */
public class ColourAnalyzer {

	public final static Logger LOG = Logger.getLogger(ColourAnalyzer.class);
	private BufferedImage currentImage;
	private BufferedImage inputImage;
	private BufferedImage flattenedImage;
	private int height;
	private int width;
	private Multiset<Integer> colorSet;
	private Int2 xRange;
	private Int2Range xyRange;
	private boolean fourbits;
	private File outputDirectory;
	private File inputFile;
	private int intervalCount;
	private IntSet sortedFrequencyIndex;
	private IntArray colorValues;
	private IntArray colorCounts;
	private List<PixelList> pixelListList;
	private int maxPixelSize = 100000;
	private int minPixelSize = 600;
	private int startPlot = 1;
	private int endPlot = 100;

	@Deprecated
	public ColourAnalyzer(Image image) {
		this.inputImage = (BufferedImage) image;
		this.height = image.getHeight(null);
		this.width = image.getWidth(null);
		this.colorSet = HashMultiset.create();
		this.xyRange = new Int2Range(new IntRange(0, width), new IntRange(0, height));
		this.currentImage = inputImage;
	}
	
	public ColourAnalyzer() {
	}

	/**
	 * @deprecated
	 * @return
	 */
	public Multiset<Integer> createColorSet() {
		IntRange xRange = xyRange.getXRange();
		IntRange yRange = xyRange.getYRange();
		for (int jy = yRange.getMin(); jy <= yRange.getMax(); jy++) {
			for (int ix = xRange.getMin(); ix <= xRange.getMax(); ix++) {
				int color = inputImage.getRGB(ix, jy);
				if (fourbits) {
					color = color & 0xF0F0F0;
				}
				colorSet.add(color);
			}
		}
		return colorSet;
	}

	public Multiset<Integer> createColorSetNew() {
		this.colorSet = HashMultiset.create();
		for (int jy = 0; jy < currentImage.getHeight(); jy++) {
			for (int ix = 0; ix < currentImage.getWidth(); ix++) {
				int color = currentImage.getRGB(ix, jy);
				colorSet.add(color);
			}
		}
		return colorSet;
	}

	public BufferedImage getInputImage() {
		return inputImage;
	}

	public BufferedImage getCurrentImage() {
		return currentImage;
	}

	public BufferedImage getFlattenedImage() {
		return flattenedImage;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setXYRange(Int2Range xyRange) {
		this.xyRange = xyRange;
	}

	/** map all colours onto 4 bits.
	 * 
	 * @param b
	 * @deprecated
	 */
	public void set4Bits(boolean b) {
		this.fourbits = b;
	}
	
	public void setStartPlot(int start) {
		this.startPlot = start;
	}

	public void setEndPlot(int end) {
		this.endPlot = end;
	}
	
	public void setMinPixelSize(int minPixel) {
		this.minPixelSize = minPixel;
	}

	public void setMaxPixelSize(int maxPixel) {
		this.maxPixelSize = maxPixel;
	}

	public void readImage(File file) throws IOException {
		this.inputFile = file;
		if (!file.exists()) {
			throw new IOException("Image file does not exist: "+inputFile);
		}
		inputImage = ImageIO.read(inputFile);
		if (inputImage == null) {
			throw new RuntimeException("Image file could not be read: "+inputFile);
		}
		currentImage = inputImage;
	}
	
	public File getInputFile() {
		return inputFile;
	}

	public void setIntervalCount(int nvals) {
		this.intervalCount = nvals;
	}

	public void flattenImage() {
		this.flattenedImage = ImageUtil.flattenImage(currentImage, intervalCount);
		currentImage = flattenedImage;
	}
	
	public void analyzeFlattenedColours() {
		createColorSetNew();
		createSortedFrequencies();
		createSortedPixelLists();
		writePixelListsAsSVG();
		writeMainImage("main.png");
	}

	private void writeMainImage(String outputName) {
		ImageUtil.writeImageQuietly(currentImage, new File(outputDirectory, outputName));
	}

	private void writePixelListsAsSVG() {
		for (int i = 0; i < pixelListList.size(); i++) {
			PixelList pixelList = pixelListList.get(i);
			int size = pixelList.size();
			LOG.trace("size "+size);
			if (size <= maxPixelSize && size >= minPixelSize) {
				if (i >= startPlot && i <= endPlot) {
					SVGG g = new SVGG();
					String hexColor = Integer.toHexString(colorValues.elementAt(i));
					hexColor = "000000"+hexColor;
					hexColor = hexColor.substring(hexColor.length() - 6);
					pixelList.plotPixels(g, "#"+hexColor);
					SVGSVG.wrapAndWriteAsSVG(g, new File(outputDirectory, i+"_"+hexColor+".svg"));
				}
			}
		}
	}

	private void createSortedPixelLists() {
		pixelListList = new ArrayList<PixelList>();
		for (int i = 0; i < colorValues.size(); i++) {
			int colorValue = colorValues.elementAt(i);
			int colorCount = colorCounts.elementAt(i);
			String hex = Integer.toHexString(colorValue);
			PixelList pixelList = PixelList.createPixelList(currentImage, colorValue);
			pixelListList.add(pixelList);
		}
	}

	private void createSortedFrequencies() {
		colorValues = new IntArray();
		colorCounts = new IntArray();
		for (Entry<Integer> entry : colorSet.entrySet()) {
			int ii = ((int) entry.getElement()) & 0x00ffffff;
			colorValues.addElement(ii);
			colorCounts.addElement(entry.getCount());
			int size = colorValues.size();
		}
		this.sortedFrequencyIndex = colorCounts.indexSortDescending();
		colorCounts = colorCounts.getReorderedArray(sortedFrequencyIndex);
		colorValues = colorValues.getReorderedArray(sortedFrequencyIndex);
//		IntArray cc = new IntArray();
//		IntArray cv = new IntArray();
//		for (int i = 0; i < sortedFrequencyIndex.size(); i++) {
//			cc.addElement(colorCounts.elementAt(sortedFrequencyIndex.elementAt(i)));
//			cv.addElement(colorValues.elementAt(sortedFrequencyIndex.elementAt(i)));
//			cv.
//		}
//		colorValues = cv;
//		colorCounts = cc;
	}

	public void setOutputDirectory(File file) {
		this.outputDirectory = file;
		outputDirectory.mkdirs();
		
	}
	
}
