package org.xmlcml.image.processing;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/** analyzes images for colours.
 * 
 * Typically attempts to find blocks of color and separate different regions.
 * 
 * @author pm286
 *
 */
/**
valuable approach to restricting numbers of colours
http://stackoverflow.com/questions/4057475/rounding-colour-values-to-the-nearest-of-a-small-set-of-colours
http://stackoverflow.com/questions/7530627/hcl-color-to-rgb-and-backward
https://en.wikipedia.org/wiki/Color_quantization

 * 
 * @author pm286
 *
 */
public class ColourAnalyzer {

	private BufferedImage image;
	private int height;
	private int width;
	private Multiset<Integer> colorSet;
	private Int2 xRange;
	private Int2Range xyRange;
	private boolean fourbits;

	public ColourAnalyzer(Image image) {
		this.image = (BufferedImage) image;
		this.height = image.getHeight(null);
		this.width = image.getWidth(null);
		this.colorSet = HashMultiset.create();
		this.xyRange = new Int2Range(new IntRange(0, width), new IntRange(0, height));
	}
	
	public Multiset<Integer> createColorSet() {
		IntRange xRange = xyRange.getXRange();
		IntRange yRange = xyRange.getYRange();
		for (int jy = yRange.getMin(); jy <= yRange.getMax(); jy++) {
			for (int ix = xRange.getMin(); ix <= xRange.getMax(); ix++) {
				int color = image.getRGB(ix, jy);
				if (fourbits) {
					color = color & 0xF0F0F0;
				}
				colorSet.add(color);
			}
		}
		return colorSet;
	}

	public Multiset<Integer> createColorSetNew() {
		for (int jy = 0; jy < image.getHeight(); jy++) {
			for (int ix = 0; ix < image.getWidth(); ix++) {
				int color = image.getRGB(ix, jy);
				String s = Integer.toHexString(color);
				if (!s.equals("ffffffff") && !s.equals("ff3366ff")) {
//					System.out.println(">>"+s);
				}
				colorSet.add(color);
			}
		}
		return colorSet;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Multiset<Integer> getColorSet() {
		createColorSetNew();
		return colorSet;
	}

	public void setXYRange(Int2Range xyRange) {
		this.xyRange = xyRange;
	}

	/** map all colours onto 4 bits.
	 * 
	 * @param b
	 */
	public void set4Bits(boolean b) {
		this.fourbits = b;
	}
	
}
