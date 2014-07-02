package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.image.ImageUtil;

/** transforms an image independently of future use.
 * 
 * may use a variety of ImageUtil routines
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

	public ImageProcessor() {
		
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

	public void setThinning(Thinning thinning) {
		this.thinning = thinning;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
		this.binarize = true;
	}

	
	public BufferedImage processImageFile(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			throw new RuntimeException("Image file is null/missing/directory: "+file);
		}
		try {
			BufferedImage image = ImageIO.read(file);
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
}
