package org.xmlcml.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.image.pixel.PixelIslandList;
import org.xmlcml.image.pixel.PixelProcessor;
import org.xmlcml.image.processing.Thinning;
import org.xmlcml.image.processing.ZhangSuenThinning;

/**
 * transforms an image independently of future use.
 * 
 * may use a variety of ImageUtil routines
 * 
 * * NOTE: setters return ImageProcessor so they can be chained, e.g.
 * ImageProcessor processor = new
 * ImageProcessor().setThreshold(190).setThinning(null);
 * 
 * 
 * @author pm286
 *
 */
public class ImageProcessor {

	private static final int DEFAULT_THRESHOLD = 128;

	private static final Logger LOG = Logger.getLogger(ImageProcessor.class);

	public static final String DEBUG = "-d";
	public static final String DEBUG1 = "--debug";
	public static final String INPUT = "-i";
	public static final String INPUT1 = "--input";
	public static final String OUTPUT = "-o";
	public static final String OUTPUT1 = "--output";
	public static final String BINARIZE = "-b";
	public static final String BINARIZE1 = "--binarize";
	public static final String THRESH = "-t";
	public static final String THRESH1 = "--threshold";
	public static final String THINNING = "-v";
	public static final String THINNING1 = "--thinning";

	private static final String BINARIZED_PNG = "binarized.png";
	private static final String RAW_IMAGE_PNG = "rawImage.png";
	private static final String TARGET = "target";
	private static final String THINNED_PNG = "thinned.png";

	private String base;
	private boolean binarize;
	private boolean debug;
	private BufferedImage image;
	private Thinning thinning;
	private int threshold;
	private File inputFile;
	private File outputDir;
	private PixelProcessor pixelProcessor;
	private ImageParameters parameters;
	private PixelIslandList pixelIslandList = null;

	public ImageProcessor() {
		setDefaults();
		clearVariables();
	}

	public ImageProcessor(BufferedImage image) {
		this();
		this.image = image;
	}

	public void setDefaults() {
		ensurePixelProcessor();

		pixelProcessor.setDefaults();
		this.setThreshold(getDefaultThreshold());
		this.setThinning(new ZhangSuenThinning());
		this.setOutputDir(getDefaultOutputDirectory());
		this.setBinarize(true);
		this.setThreshold(DEFAULT_THRESHOLD);
	}

	public void clearVariables() {
		pixelProcessor.clearVariables();

		image = null;
		inputFile = null;
		// outputDir = null;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setBinarize(boolean binarize) {
		this.binarize = binarize;
	}

	public boolean getBinarize() {
		return binarize;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean getDebug() {
		return debug;
	}

	public void setImage(BufferedImage img) {
		this.image = img;
	}

	public BufferedImage getImage() {
		return this.image;
	}

	public void setThinning(Thinning thinning) {
		this.thinning = thinning;
	}

	/**
	 * sets threshold.
	 * 
	 * this assumes an image with white background and black lines and
	 * characters.
	 * 
	 * if the antialising bleeds between characters, set the threshold low. Thus
	 * in
	 * 
	 * @param threshold
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
		this.binarize = true;
	}

	public BufferedImage processImageFile(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			throw new RuntimeException("Image file is null/missing/directory: "
					+ file);
		}
		try {
			this.inputFile = file;
			image = ImageIO.read(file);
			processImage(image);
			return image;
		} catch (Exception e) {
			throw new RuntimeException("bad image: " + file, e);
		}
	}

	public File getInputFile() {
		return inputFile;
	}

	public BufferedImage processImage(BufferedImage img) {
		this.setImage(img);
		if (debug) {
			String filename = TARGET + "/" + base + "/" + RAW_IMAGE_PNG;
			ImageUtil.writeImageQuietly(this.image, filename);
			System.err.println("wrote raw image file: " + filename);
		}
		if (this.binarize) {
			this.image = ImageUtil.boofCVBinarization(this.image, threshold);
			if (debug) {
				String filename = TARGET + "/" + base + "/" + BINARIZED_PNG;
				ImageUtil.writeImageQuietly(this.image, filename);
				System.err.println("wrote binarized file: " + filename);
			}
		}
		if (thinning != null) {
			image = ImageUtil.thin(this.image, thinning);
			if (debug) {
				String filename = TARGET + "/" + base + "/" + THINNED_PNG;
				ImageUtil.writeImageQuietly(this.image, filename);
				System.err.println("wrote thinned file: " + filename);
			}
		}
		return this.image;
	}

	public Thinning getThinning() {
		return thinning;
	}

	public int getThreshold() {
		return threshold;
	}

	/**
	 * creates default processor.
	 * 
	 * currently sets binarize=true, thinning=ZhangSuenThinning(), threshold=128
	 * But use getters to query actual values
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessor() {
		ImageProcessor imageProcessor = new ImageProcessor();
		// defaults are standard
		return imageProcessor;
	}

	/**
	 * creates default processor and processes image.
	 * 
	 * currently sets binarize=true, thinning=ZhangSuenThinning(), threshold=128
	 * But use getters to query actual values
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessorAndProcess(
			BufferedImage image) {
		ImageProcessor imageProcessor = ImageProcessor.createDefaultProcessor();
		imageProcessor.processImage(image);
		return imageProcessor;
	}

	/**
	 * creates default processor and processes image.
	 * 
	 * uses createDefaultProcessorAndProcess(BufferedImage image)
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessorAndProcess(File imageFile) {
		if (imageFile == null || !imageFile.exists() || imageFile.isDirectory()) {
			throw new RuntimeException("Cannot find/open file " + imageFile);
		} else {
			try {
				return ImageProcessor.createDefaultProcessorAndProcess(ImageIO
						.read(imageFile));
			} catch (Exception e) {
				throw new RuntimeException("Cannot read image file: "
						+ imageFile, e);
			}
		}
	}

	public String getBase() {
		if (base == null && inputFile != null) {
			base = FilenameUtils.getBaseName(inputFile.toString());
		}
		return base;
	}

	private static int getDefaultThreshold() {
		return DEFAULT_THRESHOLD;
	}

	public void readAndProcessFile(File file) {
		this.setInputFile(file);
		processImageFile(file);

	}

	public void setInputFile(File file) {
		this.inputFile = file;
	}

	public BufferedImage processImageFile() {
		if (image == null) {
			if (inputFile == null || !inputFile.exists()) {
				throw new RuntimeException("File does not exist: " + inputFile);
			}
			if (getBase() == null) {
				setBase(FilenameUtils.getBaseName(inputFile.toString()));
			}
			try {
				image = ImageIO.read(inputFile);
				LOG.trace("read image " + image);
			} catch (Exception e) {
				throw new RuntimeException("Cannot find/read imagefile: "
						+ inputFile, e);
			}
		}
		if (image != null) {
			image = processImage(image);
		}
		LOG.trace("image " + image);
		return image;
	}

	public void debug() {
		System.err.println("input:     "
				+ ((inputFile == null) ? "null" : inputFile.getAbsolutePath()));
		System.err.println("output:    "
				+ ((outputDir == null) ? "null" : outputDir.getAbsolutePath()));
		System.err.println("threshold: " + threshold);
		System.err.println("thinning:  " + thinning);
		pixelProcessor.debug();
	}

	public PixelProcessor getPixelProcessor() {
		return this.pixelProcessor;
	}

	/**
	 * creates a default ImageProcessor and immediately processes Image.
	 * 
	 * @param image
	 * @return
	 */
	public static ImageProcessor readAndProcess(BufferedImage image) {
		ImageProcessor imageProcessor = new ImageProcessor(image);
		imageProcessor.processImage(image);
		return imageProcessor;
	}

	public PixelIslandList getOrCreatePixelIslandList() {
		if (pixelIslandList == null) {
			ensurePixelProcessor();
			// this is messy - the super thinning should have been done earlier
			pixelIslandList = pixelProcessor
					.getOrCreatePixelIslandList(thinning != null);
			if (pixelIslandList == null) {
				throw new RuntimeException("Could not create pixelIslandList");
			}
			pixelIslandList.setParameters(this.parameters);
			LOG.trace("pil " + pixelIslandList);
		}
		return pixelIslandList;
	}

	public PixelProcessor ensurePixelProcessor() {
		ensureParameterObject();
		if (pixelProcessor == null) {
			pixelProcessor = new PixelProcessor(this);
			// new Exception("ppex ").printStackTrace();
			pixelProcessor.setParameters(this.parameters);
		}
		return pixelProcessor;
	}

	private void ensureParameterObject() {
		if (this.parameters == null) {
			parameters = new ImageParameters();
		}
	}

	public static File getDefaultOutputDirectory() {
		return new File(TARGET);
	}

	public void setOutputDir(File file) {
		if (file == null) {
			throw new RuntimeException("Null output directory");
		}
		this.outputDir = file;
		outputDir.mkdirs();
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void usage() {
		System.err.println("  imageanalysis options:");
		System.err.println("       " + INPUT + " " + INPUT1
				+ "        input file (directory not yet supported)");
		System.err.println("       " + OUTPUT + " " + OUTPUT1
				+ "        output directory; def="
				+ getDefaultOutputDirectory());
		System.err.println("       " + BINARIZE + " " + BINARIZE1
				+ "        set binarize on");
		System.err.println("       " + DEBUG + " " + DEBUG1
				+ "        set debug on");
		System.err.println("       " + THRESH + " " + THRESH1
				+ "    threshold (default: " + getDefaultThreshold() + ")");
		System.err.println("       " + THINNING + " " + THINNING1
				+ "    thinning ('none', 'z' (ZhangSuen))");
	}

	protected void parseArgs(ArgIterator argIterator) {
		if (argIterator.size() == 0) {
			usage();
		} else {
			while (argIterator.hasNext()) {
				if (debug) {
					LOG.debug(argIterator.getCurrent());
				}
				parseArgAndAdvance(argIterator);
			}
		}
		if (debug) {
			this.debug();
		}
	}

	public boolean parseArgAndAdvance(ArgIterator argIterator) {
		boolean found = true;
		ensurePixelProcessor();
		String arg = argIterator.getCurrent();
		if (debug) {
			LOG.debug(arg);
		}
		if (false) {

		} else if (arg.equals(ImageProcessor.DEBUG)
				|| arg.equals(ImageProcessor.DEBUG1)) {
			debug = true;
			argIterator.setDebug(true);
			argIterator.next();

		} else if (arg.equals(BINARIZE) || arg.equals(BINARIZE1)) {
			this.setBinarize(true);
			argIterator.next();
		} else if (arg.equals(INPUT) || arg.equals(INPUT1)) {
			String value = argIterator.getSingleValue();
			if (value != null) {
				setInputFile(new File(value));
			}
		} else if (arg.equals(OUTPUT) || arg.equals(OUTPUT1)) {
			String value = argIterator.getSingleValue();
			if (value != null) {
				setOutputDir(new File(value));
			}
		} else if (arg.equals(THINNING) || arg.equals(THINNING1)) {
			String value = argIterator.getSingleValue();
			if (value != null) {
				setThin(value);
			}
		} else if (arg.equals(THRESH) || arg.equals(THRESH1)) {
			Integer value = argIterator.getSingleIntegerValue();
			if (value != null) {
				setThreshold(value);
			}
		} else {
			found = pixelProcessor.processArg(argIterator);
			if (!found) {
				LOG.debug("skipped unknown token: " + argIterator.getLast());
				argIterator.next();
			}
		}
		return found;
	}

	private void setThin(String thinningS) {
		if (thinningS == null) {
			throw new RuntimeException(
					"no thinning argument [for none use 'none']");
		} else if (thinningS.equalsIgnoreCase("none")) {
			setThinning(null);
		} else if (thinningS.equalsIgnoreCase("z")) {
			setThinning(new ZhangSuenThinning());
		} else {
			LOG.error("unknown thinning argument: " + thinningS);
		}
	}

	public static void main(String[] args) throws Exception {
		ImageProcessor imageProcessor = new ImageProcessor();
		ArgIterator argIterator = new ArgIterator(args);
		imageProcessor.processArgsAndRun(argIterator);
	}

	private void processArgsAndRun(ArgIterator argIterator) {
		if (argIterator.size() == 0) {
			this.usage();
		} else {
			this.parseArgs(argIterator);
			this.runCommands();
		}
	}

	void runCommands() {
		ensurePixelProcessor();
		if (this.image == null) {
			if (inputFile != null) {
				processImageFile();
			} else {
				throw new RuntimeException("no image file to process");
			}
		} else {
			processImage(image);
		}
		PixelIslandList islandList = pixelProcessor
				.getOrCreatePixelIslandList();
		LOG.trace("islandList " + islandList.size());
	}

	public void parseArgs(String[] args) {
		ArgIterator argIterator = new ArgIterator(args);
		while (argIterator.hasNext()) {
			parseArgAndAdvance(argIterator);
		}
	}

	public void parseArgsAndRun(String[] args) {
		this.parseArgs(args);
		this.runCommands();
	}

	public ImageParameters getParameters() {
		return parameters;
	}

	public void setParameters(ImageParameters parameters) {
		this.parameters = parameters;
	}

}
