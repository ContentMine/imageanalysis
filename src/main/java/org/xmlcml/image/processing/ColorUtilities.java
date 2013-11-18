package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;

/** not yet working
 * 
 * @author pm286
 *
 */
public class ColorUtilities {

    private BufferedImage colorFrame;
    private int width;
    private int height;
    
    private BufferedImage grayFrame = 
        new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    
	public void filter1() {
	   BufferedImageOp grayscaleConv = 
	      new ColorConvertOp(colorFrame.getColorModel().getColorSpace(), 
	                         grayFrame.getColorModel().getColorSpace(), null);
	   grayscaleConv.filter(colorFrame, grayFrame);
	}
	
	// OR
	protected void filter() {       
        WritableRaster raster = grayFrame.getRaster();

        for(int x = 0; x < raster.getWidth(); x++) {
            for(int y = 0; y < raster.getHeight(); y++){
                int argb = colorFrame.getRGB(x,y);
                int r = (argb >> 16) & 0xff;
                int g = (argb >>  8) & 0xff;
                int b = (argb      ) & 0xff;

                int l = (int) (.299 * r + .587 * g + .114 * b);
                raster.setSample(x, y, 0, l);
            }
        }
    }
}
