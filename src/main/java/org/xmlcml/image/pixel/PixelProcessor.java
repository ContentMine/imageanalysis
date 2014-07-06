package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
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
	
	private PixelIslandList pixelIslandList;
	private int maxIsland;
	private BufferedImage image;
	private List<PixelGraph> pixelGraphList;
	private File outputDir;

	private boolean superThinning;
//	private Thinning thinning = 

	
	public PixelProcessor(BufferedImage image) {
		this.image = image;
		setDefaults();
	}
	
	private void setDefaults() {
		outputDir = new File("target/misc1/");
		maxIsland = 6;
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
		if (pixelIslandList == null && image != null) {
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
		return outputDir;
	}

	
	public void debug() {
		System.err.println("pixelIslandList   "+pixelIslandList);
		System.err.println("maxIsland         "+maxIsland);
		System.err.println("pixelGraphList    "+pixelGraphList);
	}

}
