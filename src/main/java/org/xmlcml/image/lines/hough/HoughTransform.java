package org.xmlcml.image.lines.hough;
	 
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
	 
/** 
 * <p> 
 * Informed in part by code from Olly Oechsle, University of Essex, Date: 13-Mar-2008 
 * <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm">http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm</a> 
 * </p> 
 * 
 * @author pm286
 */ 
 
public class HoughTransform { 

	private final static Logger LOG = Logger.getLogger(HoughTransform.class);
	
    final int neighbourhoodSize = 4; 
    final int maxTheta = 180; 
    final double thetaStep = Math.PI / maxTheta; 
    protected int width, height; 
    protected int[][] thetaRMatrix; 
    protected float centerX, centerY; 
    protected int houghSize; 
    protected int doubleSize; 
    protected int numPoints; 
	private BufferedImage image; 
 
    public HoughTransform(BufferedImage image) {
    	this.image = image;
        initialise(); 
        
    }

    private void initialise() { 
 
    	this.width = image.getWidth();
    	this.height = image.getHeight();
        houghSize = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
        // Double the height of the hough array to cope with negative r values 
        doubleSize = 2 * houghSize; 
        thetaRMatrix = new int[maxTheta][doubleSize]; 
        centerX = width / 2; 
        centerY = height / 2; 
        numPoints = 0; 
    } 
 
    /** 
     * The image is assumed to be greyscale black and white, so all pixels that are 
     * not black are counted as edges.*/ 
    public void addPoints() { 
 
        for (int x = 0; x < width; x++) { 
            for (int y = 0; y < height; y++) { 
                // Find non-black pixels 
                if ((image.getRGB(x, y) & 0x000000ff) != 0) { 
                    addPointToAccumulator(x, y); 
                } 
            } 
        } 
    } 
 
    /** 
     * Increments accumulator. 
     */ 
    public void addPointToAccumulator(int x, int y) { 
 
        for (int t = 0; t < maxTheta; t++) { 
            int r = (int) (((x - centerX) * Math.cos(thetaStep * t)) + ((y - centerY) * Math.sin(thetaStep * t)) ); 
            // this copes with negative values of r 
            r += houghSize; 
 
            if (r < 0 || r >= doubleSize) continue; 
            thetaRMatrix[t][r]++; 
        } 
 
        numPoints++; 
    } 
 
    /** 
     * extracts the lines
     * 
     * @param percentageThreshold The percentage threshold above which lines are determined from the hough array 
     */ 
    public List<HoughLine> getLines(int threshold) { 
        List<HoughLine> lines = new ArrayList<HoughLine>(20); 
        if (numPoints == 0) return lines; 
 
        // Search for local peaks above threshold to draw 
        for (int t = 0; t < maxTheta; t++) { 
            for (int r = neighbourhoodSize; r < doubleSize - neighbourhoodSize; r++) { 
                if (thetaRMatrix[t][r] > threshold) { 
                    if (!findBiggerNeighbouringPeak(t, r)) {
	                    lines.add(new HoughLine(t * thetaStep, r)); 
                    }
                } 
            } 
        } 
 
        return lines; 
    }

	private boolean findBiggerNeighbouringPeak(int theta0, int r0) {
		boolean foundBiggerPeak = false;
		for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) { 
		    for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) { 
		        int r = r0 + dy; 
		        int t = normalizeTheta(theta0 + dx);
		        if (thetaRMatrix[t][r] > thetaRMatrix[theta0][r0]) { 
		        	foundBiggerPeak = true;
		        } 
		    } 
		    if (foundBiggerPeak) break;
		}
		return foundBiggerPeak;
	}

	private int normalizeTheta(int t) {
		if (t < 0) {
			t = t + maxTheta; 
		} else if (t >= maxTheta) {
			t = t - maxTheta; 
		}
		return t;
	} 
 
    /** 
     * Gets the highest value in the hough array 
     */ 
    public int getHighestValueInAccumulator() { 
        int max = 0; 
        for (int t = 0; t < maxTheta; t++) { 
            for (int r = 0; r < doubleSize; r++) { 
                if (thetaRMatrix[t][r] > max) { 
                    max = thetaRMatrix[t][r]; 
                } 
            } 
        } 
        return max; 
    } 
 
    /** 
     */ 
    public BufferedImage createBufferedImageFromAccumulator() { 
        int max = getHighestValueInAccumulator(); 
        BufferedImage image = new BufferedImage(maxTheta, doubleSize, BufferedImage.TYPE_INT_ARGB); 
        for (int t = 0; t < maxTheta; t++) { 
            for (int r = 0; r < doubleSize; r++) { 
                double value = 255 * ((double) thetaRMatrix[t][r]) / max; 
                int v = 255 - (int) value; 
                int c = new Color(v, v, v).getRGB(); 
                image.setRGB(t, r, c); 
            } 
        } 
        return image; 
    }

	public void resetToBlack(int colorToReplace) {
        for (int x = 0; x < width; x++) { 
            for (int y = 0; y < height; y++) { 
            	if (image.getRGB(x, y) == colorToReplace) {
            		image.setRGB(x, y, Color.BLACK.getRGB());
            	}
            }
        }
	} 
 
} 
