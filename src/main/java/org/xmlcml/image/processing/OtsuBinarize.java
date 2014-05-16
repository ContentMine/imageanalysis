package org.xmlcml.image.processing;

	/**
	 * Image binarization - Otsu algorithm
	 *
	 * Author: Bostjan Cigan (http://zerocool.is-a-geek.net)
	 *
	 */
	 
	import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.xmlcml.image.ImageUtil;
	 
public class OtsuBinarize {
 
    private BufferedImage original, grayscale, binarized, current;
	private int[] histograms;
	private BufferedImage grayImage;
	private int threshold;
	private BufferedImage sharp;

    public OtsuBinarize() {
    }
    
    public static void main(String[] args) throws IOException {

    	OtsuBinarize otsuBinarize = new OtsuBinarize();
    	
        File original_f = new File(args[0]+".jpg");
        String output_f = args[0]+"_bin";
        otsuBinarize.original = ImageIO.read(original_f);
        otsuBinarize.toGray();
        otsuBinarize.binarize();
        File file = new File(args[0]+"out.jpg");
        ImageUtil.writeImageQuietly(otsuBinarize.current, file);        
 
    }

	public void read(File file) throws IOException {
		setImage(ImageIO.read(file));
	}

    // Return histogram of grayscale image
    private int[] imageHistogram() {
 
        histograms = new int[256];
 
        for(int i=0; i<histograms.length; i++) histograms[i] = 0;
 
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
                int red = new Color(original.getRGB (i, j)).getRed();
                histograms[red]++;
            }
        }
 
        return histograms;
 
    }
 
    // The luminance method
    public BufferedImage toGray() {
 
        int alpha, red, green, blue;
        int newPixel;
 
        grayImage = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
 
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
 
                // Get pixels by R, G, B
                alpha = new Color(original.getRGB(i, j)).getAlpha();
                red = new Color(original.getRGB(i, j)).getRed();
                green = new Color(original.getRGB(i, j)).getGreen();
                blue = new Color(original.getRGB(i, j)).getBlue();
 
                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                // Return back to original format
                newPixel = colorToRGB(alpha, red, red, red);
 
                // Write pixels into image
                grayImage.setRGB(i, j, newPixel);
 
            }
        }
 
        current = grayImage;
        return grayImage;
 
    }
    
    public void sharpenGray() {
        sharp = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        for(int i=1; i<grayImage.getWidth()-1; i++) {
            for(int j=0; j<grayImage.getHeight(); j++) {
            	Color col0 = new Color(grayImage.getRGB(i-1, j));
            	int gray0 = col0.getRed();
            	Color col1 = new Color(grayImage.getRGB(i, j));
            	int gray1 = col1.getRed();
            	Color col2 = new Color(grayImage.getRGB(i+1, j));
            	int gray2 = col2.getRed();
            	int grayNew =  -gray0 + 3*gray1 -gray2;
            	if (grayNew < 0 ) {
            		grayNew = 0;
            	} else if (grayNew > 255) {
            		grayNew = 255;
            	}
            	int col = colorToRGB(col0.getAlpha(), grayNew, grayNew, grayNew);
            	sharp.setRGB(i,  j,  col); 
            }
        }
        current = sharp;
    }
 
    // Get binary threshold using Otsu's method
    private int otsuThreshold() {
 
        int[] histogram = imageHistogram();
        int total = original.getHeight() * original.getWidth();
 
        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];
 
        float sumB = 0;
        int wB = 0;
        int wF = 0;
 
        float varMax = 0;
        threshold = 0;
 
        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;
 
            if(wF == 0) break;
 
            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
 
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
 
            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
 
        return threshold;
 
    }
 
    public BufferedImage binarize() {
 
        int red;
        int newPixel;
 
        int threshold = otsuThreshold();
 
//        binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        binarized = new BufferedImage(current.getWidth(), current.getHeight(), current.getType());
 
        for(int i=0; i<current.getWidth(); i++) {
            for(int j=0; j<current.getHeight(); j++) {
 
                // Get pixels
                red = new Color(current.getRGB(i, j)).getRed();
                int alpha = new Color(current.getRGB(i, j)).getAlpha();
                if(red > threshold) {
                    newPixel = 255;
                }
                else {
                    newPixel = 0;
                }
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel);
 
            }
        }
 
        current = binarized;
        return binarized;
 
    }
 
    // Convert R, G, B, Alpha to standard 8 bit
    private static int colorToRGB(int alpha, int red, int green, int blue) {
 
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;
 
        return newPixel;
 
    }

	public BufferedImage getBinarizedImage() {
		return binarized;
	}

	public void setImage(BufferedImage bImage) {
		this.original = bImage;
		current = original;
	}

	public BufferedImage getGrayImage() {
		return grayImage;
	}

	public BufferedImage getCurrent() {
		return current;
	}

 
}

