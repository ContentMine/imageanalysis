package org.xmlcml.image.colour;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.IntSet;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.util.ImageIOUtil;
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
public class ColorAnalyzer {

	private static final Logger LOG = Logger.getLogger(ColorAnalyzer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static final String COUNT = "count";
	private static final String AVERAGE = "average";
	private static final String MINPIXEL = "minpixel";
	private static final String MAXPIXEL = "maxpixel";
	
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
	private int minPixelSize = 100;
	private int startPlot = 1;
	private int endPlot = 100;
	private int count = 0;
	private boolean flatten;

/**
 * 	
//		ColourAnalyzer colorAnalyzer = new ColourAnalyzer();
//		colorAnalyzer.readImage(new File(Fixtures.PROCESSING_DIR, filename+".png"));
//		colorAnalyzer.setStartPlot(1);
//		colorAnalyzer.setMaxPixelSize(1000000);
//		colorAnalyzer.setIntervalCount(4);
//		colorAnalyzer.setEndPlot(15);
//		colorAnalyzer.setMinPixelSize(300);
//		colorAnalyzer.flattenImage();
//		colorAnalyzer.setOutputDirectory(new File("target/"+filename));
//		colorAnalyzer.analyzeFlattenedColours();
 */
	@Deprecated
	public ColorAnalyzer(Image image) {
		setInputImage(image);
		this.height = image.getHeight(null);
		this.width = image.getWidth(null);
		this.colorSet = HashMultiset.create();
		this.xyRange = new Int2Range(new IntRange(0, width), new IntRange(0, height));
	}

	public void setInputImage(Image image) {
		this.inputImage = (BufferedImage) image;
		this.currentImage = inputImage;
	}
	
	public ColorAnalyzer() {
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
	
	public BufferedImage sharpenImage(BufferedImage image) {
		BufferedImage newImage = null;
		RGBMatrix rgbMatrix = RGBMatrix.extractMatrix(image);
		RGBMatrix filtered = rgbMatrix.applyFilter(ImageUtil.SHARPEN_ARRAY);
		return newImage;
	}

	public void flattenImage() {
		ImageIOUtil.writeImageQuietly(currentImage, new File("target/flatten/before.png"));
		this.flattenedImage = ImageUtil.flattenImage(currentImage, intervalCount);
		currentImage = flattenedImage;
		ImageIOUtil.writeImageQuietly(currentImage, new File("target/flatten/after.png"));
	}
	
	public void analyzeFlattenedColours() {
		createColorSetNew();
		createSortedFrequencies();
		createSortedPixelLists();
		if (outputDirectory != null) {
			writePixelListsAsSVG();
			writeMainImage("main.png");
		}
	}

	private void writeMainImage(String outputName) {
		ImageIOUtil.writeImageQuietly(currentImage, new File(outputDirectory, outputName));
	}

	private void writePixelListsAsSVG() {
		for (int i = 0; i < pixelListList.size(); i++) {
			String hexColorS = Integer.toHexString(colorValues.elementAt(i));
			hexColorS = "000000"+hexColorS;
			hexColorS = hexColorS.substring(hexColorS.length() - 6);
			PixelList pixelList = pixelListList.get(i);
			int size = pixelList.size();
			if (size <= maxPixelSize && size >= minPixelSize) {
				if (i >= startPlot && i <= endPlot) {
					SVGG g = new SVGG();
					pixelList.plotPixels(g, "#"+hexColorS);
					// use maximum values for width as we don't want to shift origin
					int xmax = pixelList.getIntBoundingBox().getXRange().getMax();
					int ymax = pixelList.getIntBoundingBox().getYRange().getMax();
					SVGSVG.wrapAndWriteAsSVG(g, new File(outputDirectory, i+"_"+hexColorS+".svg"), xmax, ymax);
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

	public void defaultPosterize() {
		setStartPlot(1);
		setMaxPixelSize(1000000);
		setIntervalCount(4);
		setEndPlot(15);
		setMinPixelSize(300);
		flattenImage();
		analyzeFlattenedColours();
	}

	public void parse(List<String> values) {
		int ival = 0;
		while (ival < values.size()) {
			String value = values.get(ival++);
			if (COUNT.equalsIgnoreCase(value)) {
				count = Integer.parseInt(values.get(ival++));
				this.setIntervalCount(count);
			} else if (AVERAGE.equalsIgnoreCase(value)) {
				flatten = true;
			} else if (MINPIXEL.equalsIgnoreCase(value)) {
				int minPixel = Integer.parseInt(values.get(ival++));
				this.setMinPixelSize(minPixel);
			} else if (MAXPIXEL.equalsIgnoreCase(value)) {
				int maxPixel = Integer.parseInt(values.get(ival++));
				this.setMaxPixelSize(maxPixel);
			} else {
				throw new RuntimeException("unknown arg/param in ColorAnalyzer: "+value);
			}
		}
	}

	public void run() {
		if (flatten) {
			currentImage = ImageUtil.averageImageColors(currentImage);
		}
		this.flattenImage();
		this.analyzeFlattenedColours();

	}
	
}
