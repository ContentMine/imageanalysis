package org.xmlcml.image.lines.old;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealMatrix;
import org.xmlcml.image.processing.ColorUtilities;

public class BinaryHough {

	private static final Logger LOG = Logger.getLogger(BinaryHough.class);
	
	private BufferedImage bufferedImage;
	private Real2 origin;
	private int angleCols; 
	private int distRows;
	private int imageCols;
	private int imageRows;
	private int numData;

	private int maxIntensity; 

	public BinaryHough() {
		initialize();
	}
	private void initialize() {
		setOrigin(new Real2(0.0, 0.0));
		angleCols = 60;
		distRows = 100;
		maxIntensity = (255+255+255)/2;
	}
	
	public void readImage(File file) throws IOException {
		if (file == null || !file.exists() || file.isDirectory()) {
			throw new IOException("File not found or is directory: "+file);
		}
		bufferedImage = ImageIO.read(file);
		imageCols = bufferedImage.getWidth();
		imageRows = bufferedImage.getHeight();
	}
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setOrigin(Real2 origin) {
		this.origin = origin;
	}
	
	public RealMatrix calculateAccumulatorMatrix() {
		Accumulator accumulator = new Accumulator(angleCols, distRows);
		
		Raster raster = bufferedImage.getRaster();
		numData = raster.getNumDataElements();
		for (int irow = 0; irow < imageRows; irow++) {
			for (int jcol = 0; jcol < imageCols; jcol++) {
				int[] pix = new int[numData];
				pix = raster.getPixel(jcol, irow, pix);
				int value = ColorUtilities.getValue(pix);
				if (value < maxIntensity) { //i.e.black
					LOG.trace(value);
					accumulator.add(jcol, irow);
				}
			}
		}
		return null;
	}
	
	
}
