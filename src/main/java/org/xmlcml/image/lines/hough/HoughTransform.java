package org.xmlcml.image.lines.hough;
	 
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
	 
/** 
 * <p> 
 * Informed in part by code from Olly Oechsle, University of Essex, Date: 13-Mar-2008 
 * <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm">http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm</a> 
 * </p> 
 * 
 * @author pm286
 */ 
 
public class HoughTransform extends Thread { 
 
    final int neighbourhoodSize = 4; 
    final int maxTheta = 180; 
    final double thetaStep = Math.PI / maxTheta; 
    protected int width, height; 
    protected int[][] houghArray; 
    protected float centerX, centerY; 
    protected int houghSize; 
    protected int doubleSize; 
    protected int numPoints; 
    
    private double[] sinCache; 
    private double[] cosCache;
	private BufferedImage image; 
 
    public HoughTransform(BufferedImage image) {
    	this.image = image;
    	this.width = image.getWidth();
    	this.height = image.getHeight();
        initialise(); 
        
    }

	/** 
     * Initialises the hough array. Called by the constructor so you don't need to call it 
     * yourself, however you can use it to reset the transform if you want to plug in another 
     * image (although that image must have the same width and height) 
     */ 
    public void initialise() { 
 
        houghSize = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
        // Double the height of the hough array to cope with negative r values 
        doubleSize = 2 * houghSize; 
        houghArray = new int[maxTheta][doubleSize]; 
        centerX = width / 2; 
        centerY = height / 2; 
        numPoints = 0; 
 
        sinCache = new double[maxTheta]; 
        cosCache = sinCache.clone(); 
        for (int t = 0; t < maxTheta; t++) { 
            double realTheta = t * thetaStep; 
            sinCache[t] = Math.sin(realTheta); 
            cosCache[t] = Math.cos(realTheta); 
        } 
    } 
 
    /** 
     * Adds points from an image. The image is assumed to be greyscale black and white, so all pixels that are 
     * not black are counted as edges. The image should have the same dimensions as the one passed to the constructor. 
     */ 
    public void addPoints() { 
 
        // Now find edge points and update the hough array 
        for (int x = 0; x < width; x++) { 
            for (int y = 0; y < height; y++) { 
                // Find non-black pixels 
                if ((image.getRGB(x, y) & 0x000000ff) != 0) { 
                    addPoint(x, y); 
                } 
            } 
        } 
    } 
 
    /** 
     * Adds a single point to the hough transform. You can use this method directly 
     * if your data isn't represented as a buffered image. 
     */ 
    public void addPoint(int x, int y) { 
 
        for (int t = 0; t < maxTheta; t++) { 
            int r = (int) (((x - centerX) * cosCache[t]) + ((y - centerY) * sinCache[t])); 
            // this copes with negative values of r 
            r += houghSize; 
 
            if (r < 0 || r >= doubleSize) continue; 
            houghArray[t][r]++; 
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
        boolean foundPeak = false;
        for (int t = 0; t < maxTheta; t++) { 
            loop: 
            for (int r = neighbourhoodSize; r < doubleSize - neighbourhoodSize; r++) { 
                if (houghArray[t][r] > threshold) { 
                    int peak = houghArray[t][r]; 
 
                    foundPeak = findPeak(t, r, peak); 
                    if (foundPeak) {
                    	continue;
                    }
//                	if (foundPeak) {
//                		continue loop;
//                	}
                    
                    double theta = t * thetaStep; 
                    lines.add(new HoughLine(theta, r)); 
                } 
            } 
//        	if (foundPeak) continue loop;
        } 
 
        return lines; 
    }

	private boolean findPeak(int t, int r, int peak) {
		boolean breakout = false;
		// Check that this peak is indeed the local maxima 
		for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) { 
		    for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) { 
		        int dt = t + dx; 
		        int dr = r + dy; 
		        if (dt < 0) dt = dt + maxTheta; 
		        else if (dt >= maxTheta) dt = dt - maxTheta; 
		        if (houghArray[dt][dr] > peak) { 
		            // found a bigger point nearby, skip 
//                                continue loop; 
		        	breakout = true;
		        } 
		    } 
		    if (breakout) break;
		}
		return breakout;
	} 
 
    /** 
     * Gets the highest value in the hough array 
     */ 
    public int getHighestValue() { 
        int max = 0; 
        for (int t = 0; t < maxTheta; t++) { 
            for (int r = 0; r < doubleSize; r++) { 
                if (houghArray[t][r] > max) { 
                    max = houghArray[t][r]; 
                } 
            } 
        } 
        return max; 
    } 
 
    /** 
     */ 
    public BufferedImage getHoughArrayImage() { 
        int max = getHighestValue(); 
        BufferedImage image = new BufferedImage(maxTheta, doubleSize, BufferedImage.TYPE_INT_ARGB); 
        for (int t = 0; t < maxTheta; t++) { 
            for (int r = 0; r < doubleSize; r++) { 
                double value = 255 * ((double) houghArray[t][r]) / max; 
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
 
    public static void main(String[] args) throws Exception { 
		String filename = "foo.png"; 
        testExample(filename); 
    }

	private static void testExample(String filename) throws IOException {
        BufferedImage image = ImageIO.read(new File(filename)); 
        HoughTransform h = new HoughTransform(image); 
        h.addPoints(); 
        int threshold = 30;
        List<HoughLine> lines = h.getLines(threshold); 
        for (int j = 0; j < lines.size(); j++) { 
            HoughLine line = lines.get(j); 
            line.draw(image, Color.RED.getRGB(), -1); 
        }
	} 
 

} 
