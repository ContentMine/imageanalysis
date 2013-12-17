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
	 
public class OtsuBinarize {
 
    private BufferedImage original, grayscale, binarized;
	private int[] histograms;
	private BufferedImage lum;
	private int threshold;

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
        otsuBinarize.writeImage(file);        
 
    }

	public void read(File file) throws IOException {
		original = ImageIO.read(file);
	}

    public void writeImage(File file) throws IOException {
    	String filename = file.getAbsolutePath();
    	String type = filename.substring(filename.length()-3, filename.length());
        ImageIO.write(binarized, type, file);
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
 
        lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
 
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
                lum.setRGB(i, j, newPixel);
 
            }
        }
 
        return lum;
 
    }
 
    // Get binary treshold using Otsu's method
    private int otsuTreshold() {
 
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
 
        int threshold = otsuTreshold();
 
        binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
 
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
 
                // Get pixels
                red = new Color(original.getRGB(i, j)).getRed();
                int alpha = new Color(original.getRGB(i, j)).getAlpha();
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

 
}

