package org.xmlcml.image.colour;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.IntSet;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
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
	private File outputDirectory;
	private File inputFile;
	private int intervalCount;
	private IntSet sortedFrequencyIndex;
	private IntArray colorValues;
	private IntArray colorCounts;
	private Int2Range xyRange;
	private List<PixelList> pixelListList;
	private int maxPixelSize = 100000;
	private int minPixelSize = 100;
	private int startPlot = 1;
	private int endPlot = 100;
	private int count = 0;
	private boolean flatten;
	private Multiset<RGBColor> colorSet;
	private RGBNeighbourMap rgbNeighbourMap;
	private ColorFrequenciesMap colorFrequenciesMap;

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
	public ColorAnalyzer(Image image) {
		readImage(image);
	}

	public void readImage(Image image) {
		clearVariables();
		setInputImage(image);
		this.height = image.getHeight(null);
		this.width = image.getWidth(null);
		getOrCreateColorSet();
		this.xyRange = new Int2Range(new IntRange(0, width), new IntRange(0, height));
	}

	private void clearVariables() {
		currentImage = null;
		inputImage = null;
		flattenedImage = null;
		height = 0;
		width = 0;
		xyRange = null;
		outputDirectory = null;
		inputFile = null;
		intervalCount = 0;
		sortedFrequencyIndex = null;
		colorValues = null;
		colorCounts = null;
		pixelListList = null;
		count = 0;
		flatten = false;
		colorSet = null;
		rgbNeighbourMap = null;
		colorFrequenciesMap = null;
	}

	public void setInputImage(Image image) {
		this.inputImage = (BufferedImage) image;
		this.currentImage = inputImage;
	}
	
	public ColorAnalyzer() {
	}

	public Multiset<RGBColor> getOrCreateColorSet() {
		if (colorSet == null || colorSet.size() == 0) {
			this.colorSet = HashMultiset.create();
			for (int jy = 0; jy < currentImage.getHeight(); jy++) {
				for (int ix = 0; ix < currentImage.getWidth(); ix++) {
					RGBColor color = new RGBColor(currentImage.getRGB(ix, jy));
					colorSet.add(color);
				}
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
		getOrCreateColorSet();
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
		for (Entry<RGBColor> entry : colorSet.entrySet()) {
			int ii = entry.getElement().getRGB();
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
	
	public SVGG createColorFrequencyPlot() {
		Multiset<RGBColor> set = this.getOrCreateColorSet();
		SVGG g = new SVGG();
		double x0 = 10.;
		double y0 = 10.;
		double ydelta = 20.;
		double x = x0;
		double y = y0;
		double xscale = 50.;
		double yscale = 0.9;
		double height = ydelta * yscale;
		double fontSize = ydelta * 0.75;
		double strokeWidth = 0.5;
		double fontOffset = ydelta * 0.75;
		String stroke = "black";
		List<Entry<RGBColor>> rgb = RGBColor.createRGBListSortedByCount(set);
		for (Entry<RGBColor> entry : rgb) {			// uncomment for debug
			double percent = (100. * (double) entry.getCount() / (double) set.size()); 
			RGBColor color = entry.getElement();
			if (!color.getHex().equals("#ffffff")) {
				y = plotRectangleAndText(g, ydelta, x, y, xscale, height, fontSize, strokeWidth, fontOffset, stroke, percent,
						color.getHex());
			}
		}
		return g;
	}

	private double plotRectangleAndText(SVGG g, double ydelta, double x, double y, double xscale, double height, double fontSize,
			double strokeWidth, double fontOffset, String stroke, double percent, String color) {
		double width = percent * xscale;
		SVGRect rect = new SVGRect(x, y, width, height);
		SVGText text = new SVGText(new Real2(x, y + fontOffset), color);
		text.setFontSize(fontSize);
		text.setFontWeight("bold");
		text.setFill("white");
		text.setStrokeWidth(strokeWidth);
		text.setStroke(stroke);
		text.setFontFamily("monospace");
		
		rect.setFill(color);
		rect.setStrokeWidth(strokeWidth);
		rect.setStroke(stroke);
		y += ydelta;
		g.appendChild(rect);
		g.appendChild(text);
		return y;
	}

	public RGBNeighbourMap getOrCreateNeighbouringColorMap() {
		if (rgbNeighbourMap == null) {
			getOrCreateColorFrequenciesMap();
			rgbNeighbourMap = new RGBNeighbourMap(colorSet);
//			LOG.debug("size "+rgbNeighbourMap.size());
//			LOG.debug("Keys "+rgbNeighbourMap.keySet());
		}
		return rgbNeighbourMap;
	}

	/** frequencies of colours.
	 * count indexed by rgbValue
	 * @return
	 */
	public ColorFrequenciesMap getOrCreateColorFrequenciesMap() {
		if (colorFrequenciesMap == null) {
			colorFrequenciesMap = ColorFrequenciesMap.createMap(colorSet);
		}
		return colorFrequenciesMap;
	}

	public BufferedImage mergeMinorColours(BufferedImage image) {
		readImage(image);
		getOrCreateNeighbouringColorMap();
//		Set<RGBColor> rgbNeighbourKeys = rgbNeighbourMap.keySet();
//		List<RGBColor> rgbNeighbourKeyList = new ArrayList<RGBColor>(rgbNeighbourKeys);
		BufferedImage newImage = ImageUtil.deepCopy(image);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				RGBColor rgbColor = new RGBColor(image.getRGB(i, j));
				RGBColor rgbColor1 = rgbNeighbourMap.getMoreFrequentRGBNeighbour(colorFrequenciesMap, rgbColor);
				newImage.setRGB(i, j, rgbColor1.getRGB());
			}
		}
		return newImage;
	}

	/** extracts the image corresponding to the color.
	 * all other colors are set to WHITE
	 * 
	 * @param color
	 * @return
	 */
	public BufferedImage getImage(RGBColor color) {
		BufferedImage newImage = ImageUtil.deepCopy(inputImage);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				RGBColor rgbColor = new RGBColor(inputImage.getRGB(i, j));
				if (!rgbColor.equals(color)) {
					newImage.setRGB(i, j, RGBColor.HEX_WHITE);
				}
			}
		}
		return newImage;
	}

	/** output all pixels as black unless white.
	 * 
	 * @return
	 */
	public BufferedImage getBinaryImage() {
		BufferedImage newImage = ImageUtil.deepCopy(inputImage);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				RGBColor rgbColor = new RGBColor(inputImage.getRGB(i, j));
				if (rgbColor.equals(RGBColor.RGB_WHITE)) {
					newImage.setRGB(i, j, RGBColor.HEX_WHITE);
				} else {
					newImage.setRGB(i, j, RGBColor.HEX_BLACK);
				}
			}
		}
		return newImage;
	}



}
