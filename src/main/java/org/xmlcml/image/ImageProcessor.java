package org.xmlcml.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.image.pixel.PixelIslandList;
import org.xmlcml.image.pixel.PixelProcessor;
import org.xmlcml.image.processing.Thinning;
import org.xmlcml.image.processing.ZhangSuenThinning;

/** transforms an image independently of future use.
 * 
 * may use a variety of ImageUtil routines
 * 
 *  * NOTE: setters return ImageProcessor so they can be chained, e.g.
 *     ImageProcessor processor = new ImageProcessor().setThreshold(190).setThinning(null);

 * 
 * @author pm286
 *
 */
public class ImageProcessor {

	private static final int DEFAULT_THRESHOLD = 128;

	private static final Logger LOG = Logger.getLogger(ImageProcessor.class);

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
	private PixelProcessor pixelProcessor;

	public ImageProcessor() {
	}
	
	public ImageProcessor(BufferedImage image) {
		this.image = image;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setBinarize(boolean binarize) {
		this.binarize = binarize;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
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

	/** sets threshold.
	 * 
	 * this assumes an image with white background and black lines and characters.
	 * 
	 * if the antialising bleeds between characters, set the threshold low. Thus
	 * in 
	 * @param threshold
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
		this.binarize = true;
	}

	
	public BufferedImage processImageFile(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			throw new RuntimeException("Image file is null/missing/directory: "+file);
		}
		try {
			image = ImageIO.read(file);
			processImage(image);
			return image;
		} catch (Exception e) {
			throw new RuntimeException("bad image: "+file, e);
		}
	}

	public BufferedImage processImage(BufferedImage img) {
		this.setImage(img);
		if (debug) {
			String filename =  TARGET+"/"+base+"/"+RAW_IMAGE_PNG;
			ImageUtil.writeImageQuietly(this.image, filename);
			System.err.println("wrote raw image file: "+filename);
		}
		if (this.binarize) {
			this.image = ImageUtil.boofCVBinarization(this.image, threshold);
			if (debug) {
				String filename =  TARGET+"/"+base+"/"+BINARIZED_PNG;
				ImageUtil.writeImageQuietly(this.image, filename);
				System.err.println("wrote binarized file: "+filename);
			}
		}
		if (thinning != null) {
			image = ImageUtil.thin(this.image, thinning);
			if (debug) {
				String filename =  TARGET+"/"+base+"/"+THINNED_PNG;
				ImageUtil.writeImageQuietly(this.image, filename);
				System.err.println("wrote thinned file: "+filename);
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

	/** creates default processor.
	 * 
	 * currently sets binarize=true, thinning=ZhangSuenThinning(), threshold=128
	 * But use getters to query actual values
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessor() {
		ImageProcessor imageProcessor = new ImageProcessor();
		imageProcessor.setThinning(new ZhangSuenThinning());
		imageProcessor.setBinarize(true);
		imageProcessor.setThreshold(DEFAULT_THRESHOLD);
		return imageProcessor;
	}

	/** creates default processor and processes image.
	 * 
	 * currently sets binarize=true, thinning=ZhangSuenThinning(), threshold=128
	 * But use getters to query actual values
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessorAndProcess(BufferedImage image) {
		ImageProcessor imageProcessor = ImageProcessor.createDefaultProcessor();
		imageProcessor.processImage(image);
		return imageProcessor;
	}

	/** creates default processor and processes image.
	 * 
	 * uses createDefaultProcessorAndProcess(BufferedImage image)
	 * 
	 * @return
	 */
	public static ImageProcessor createDefaultProcessorAndProcess(File imageFile) {
		if (imageFile == null || !imageFile.exists() || imageFile.isDirectory()) {
			throw new RuntimeException("Cannot find/open file "+imageFile);
		} else {
			try {
				return ImageProcessor.createDefaultProcessorAndProcess(ImageIO.read(imageFile));
			} catch (Exception e) {
				throw new RuntimeException("Cannot read image file: "+imageFile, e);
			}
		} 
	}

	public String getBase() {
		return base;
	}

	public void readAndProcessFile(File file) {
		this.setInputFile(file);
		processImageFile(file);
		
	}

	private void setInputFile(File file) {
		this.inputFile = file;
	}

	public void debug() {
		System.err.println("threshold: "+threshold);
		pixelProcessor.debug();
	}

	public PixelProcessor getPixelProcessor() {
		return this.pixelProcessor;
	}

	/** creates a default ImageProcessor and immediately processes Image.
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
		ensurePixelProcessor();
		// this is messy - the super thinning should have been done earlier
		return pixelProcessor.getOrCreatePixelIslandList(thinning != null);
	}

	public PixelProcessor ensurePixelProcessor() {
		if (pixelProcessor == null) {
			pixelProcessor = new PixelProcessor(image);
		}
		return pixelProcessor;
	}
}
