package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

/** manager for all pixel operations.
 * 
 * often contained within ImageProcessor
 * 
 * used to be PixelIslandList but that became over-complex.
 * Also the pixelIslandList might be null but this shouldn't be.
 * 
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

	
	public PixelProcessor() {
		init();
	}
	
	private void init() {
		outputDir = new File("target/misc1/");
		maxIsland = 6;
	}
	
	public PixelIslandList getPixelIslandList() {
		if (pixelIslandList == null) {
			FloodFill floodFill = new FloodFill(this.image);
			floodFill.setDiagonal(true);
			floodFill.fill();
			pixelIslandList = floodFill.getPixelIslandList();
			LOG.debug("after floodfill islands: "+pixelIslandList.size());
//			pixelIslandList.setImage(image);
			pixelIslandList.removeStepsSortAndReverse();
		}
		return pixelIslandList;
	}

	public void setMaxIsland(int maxIsland) {
		this.maxIsland = maxIsland;
	}

	public int getMaxIsland() {
		return maxIsland;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	
	public void debug() {
		System.err.println("pixelIslandList   "+pixelIslandList);
		System.err.println("maxIsland         "+maxIsland);
		System.err.println("pixelGraphList    "+pixelGraphList);
	}

}
