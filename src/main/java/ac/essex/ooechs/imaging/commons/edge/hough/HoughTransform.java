package ac.essex.ooechs.imaging.commons.edge.hough;
	 
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
	 
/** 
 * <p/> 
 * Java Implementation of the Hough Transform.<br /> 
 * Used for finding straight lines in an image.<br /> 
 * by Olly Oechsle 
 * </p> 
 * <p/> 
 * Note: This class is based on original code from:<br /> 
 * <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm">http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm</a> 
 * </p> 
 * <p/> 
 * If you represent a line as:<br /> 
 * x cos(theta) + y sin (theta) = r 
 * </p> 
 * <p/> 
 * ... and you know values of x and y, you can calculate all the values of r by going through 
 * all the possible values of theta. If you plot the values of r on a graph for every value of 
 * theta you get a sinusoidal curve. This is the Hough transformation. 
 * </p> 
 * <p/> 
 * The hough tranform works by looking at a number of such x,y coordinates, which are usually 
 * found by some kind of edge detection. Each of these coordinates is transformed into 
 * an r, theta curve. This curve is discretised so we actually only look at a certain discrete 
 * number of theta values. "Accumulator" cells in a hough array along this curve are incremented 
 * for X and Y coordinate. 
 * </p> 
 * <p/> 
 * The accumulator space is plotted rectangularly with theta on one axis and r on the other. 
 * Each point in the array represents an (r, theta) value which can be used to represent a line 
 * using the formula above. 
 * </p> 
 * <p/> 
 * Once all the points have been added should be full of curves. The algorithm then searches for 
 * local peaks in the array. The higher the peak the more values of x and y crossed along that curve, 
 * so high peaks give good indications of a line. 
 * </p> 
 * 
 * @author Olly Oechsle, University of Essex 
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
        for (int t = 0; t < maxTheta; t++) { 
            loop: 
            for (int r = neighbourhoodSize; r < doubleSize - neighbourhoodSize; r++) { 
                if (houghArray[t][r] > threshold) { 
                    int peak = houghArray[t][r]; 
 
                    // Check that this peak is indeed the local maxima 
                    for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) { 
                        for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) { 
                            int dt = t + dx; 
                            int dr = r + dy; 
                            if (dt < 0) dt = dt + maxTheta; 
                            else if (dt >= maxTheta) dt = dt - maxTheta; 
                            if (houghArray[dt][dr] > peak) { 
                                // found a bigger point nearby, skip 
                                continue loop; 
                            } 
                        } 
                    } 
                    double theta = t * thetaStep; 
                    lines.add(new HoughLine(theta, r)); 
                } 
            } 
        } 
 
        return lines; 
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
     * Gets the hough array as an image, in case you want to have a look at it. 
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
		String filename = "/home/ooechs/Desktop/vase.png"; 
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
