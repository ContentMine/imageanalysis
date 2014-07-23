package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.image.ArgIterator;
import org.xmlcml.image.ImageProcessor;
import org.xmlcml.image.processing.Thinning;

/** manager for all pixel operations.
 * 
 * often contained within ImageProcessor
 * 
 * used to be PixelIslandList but that became over-complex.
 * Also the pixelIslandList might be null but this shouldn't be.
 * 
 * NOTE: setters return PixelProcessor so they can be chained, e.g.
 *     PixelProcessor processor = new PixelProcessor(image).setMaxisland(3).setOutputDir(file)
 * 
 * @author pm286
 *
 */
public class PixelProcessor {

	private final static Logger LOG = Logger.getLogger(PixelProcessor.class);

	public static final String ISLAND = "-y";
	public static final String ISLAND1 = "--island";

	private PixelIslandList pixelIslandList;
	private int maxIsland;
	private List<PixelGraph> pixelGraphList;
//	private boolean superThinning;
	private boolean debug;
	private ImageProcessor imageProcessor;
	private BufferedImage image;
	private File outputDir;
	private int island;

	
	public PixelProcessor(ImageProcessor imageProcessor) {
		this.imageProcessor = imageProcessor;
		setDefaults();
	}
	
	public PixelProcessor(BufferedImage image) {
		this.image = image;
	}

	public void setDefaults() {
//		outputDir = new File("target/misc1/");
		this.setMaxIsland(getDefaultMaxIsland());
		this.setIsland(-1); // because 0 is a valid island
	}
	
	private int getDefaultMaxIsland() {
		return 3;
	}

	public BufferedImage getImage() {
		if (image == null) {
			if (imageProcessor != null) {
				image = imageProcessor.getImage();
			}
		}
		return image;
	}
	/** messy.
	 * If we have set thinning to null, then we don't use superthinning
	 * @param thinning
	 * @return
	 */
	public PixelIslandList getOrCreatePixelIslandList(Thinning thinning) {
		return getOrCreatePixelIslandList((thinning != null));
	}

	/** get PixelIslandList.
	 * 
	 * create it if it doesn't exist.
	 * 
	 * this default is NOT to superthin after creating islands
	 * 
	 * @return
	 */
	public PixelIslandList getOrCreatePixelIslandList() {
		return getOrCreatePixelIslandList(false);
	}
		
	/** get pixelIslandList.
	 * 
	 * if not existing, do floodfill on pixels
	 * 
	 * if superthinning (ie. cleaning thisck edges and pixelNuclei) is required carry it
	 * out here. Ideally it shold be with the Z-S thinning, but the code is written for pixels after 
	 * pixelIslands are created.
	 * 
	 * @param superThinning 
	 * @return
	 */
	public PixelIslandList getOrCreatePixelIslandList(boolean superThinning) {
		if (pixelIslandList == null && getImage() != null) {
			FloodFill floodFill = new FloodFill(this.image);
			floodFill.setDiagonal(true);
			floodFill.fill();
			pixelIslandList = floodFill.getPixelIslandList();
			LOG.trace("after floodfill islands: "+pixelIslandList.size());
			if (superThinning) {
				pixelIslandList.removeStepsSortAndReverse();
			}
			pixelIslandList.setPixelProcessor(this);
		}
		return pixelIslandList;
	}

	public PixelProcessor setMaxIsland(int maxIsland) {
		this.maxIsland = maxIsland;
		return this;
	}

	public int getMaxIsland() {
		return maxIsland;
	}

	public PixelProcessor setOutputDir(File outputDir) {
		this.outputDir = outputDir;
		return this;
	}

	public File getOutputDir() {
		if (outputDir == null) {
			outputDir = imageProcessor.getOutputDir();
		}
		return outputDir;
	}


	public boolean processArg(ArgIterator argIterator) {
		boolean found = true;
		String arg = argIterator.getCurrent();
		if (false) {
			
		} else if (arg.equals(ImageProcessor.DEBUG) || arg.equals(ImageProcessor.DEBUG1)) {
			this.debug = true;
		} else if (arg.equals(PixelProcessor.ISLAND) || arg.equals(PixelProcessor.ISLAND1)) {
			Integer value = argIterator.getSingleIntegerValue();
			if (value != null) {
				setIsland(value);
			}
			
		} else {
			found = false;
		}
		return found;
	}

	private void setIsland(int island) {
		this.island = island;
	}

	public int getIsland() {
		return island;
	}

	public void debug() {
		System.err.println("pixelIslandList   "+pixelIslandList);
		System.err.println("maxIsland         "+maxIsland);
		System.err.println("pixelGraphList    "+pixelGraphList);
	}

}
