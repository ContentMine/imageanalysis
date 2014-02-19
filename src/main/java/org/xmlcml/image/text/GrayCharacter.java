package org.xmlcml.image.text;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Util;

public class GrayCharacter {

	private final static Logger LOG = Logger.getLogger(GrayCharacter.class);
	
	private BufferedImage image;
	private int width;
	private int height;

	private double total;

	private double sum;

	GrayCharacter(BufferedImage image) {
		this.image = Util.toGray(image);
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	public double correlateGray(GrayCharacter character2, String title) {
		double cor = 0.0;
		Real2 centre = this.getCentre();
		Real2 centre2 = character2.getCentre();
		Real2 offset = centre2.subtract(centre);
		if (Math.abs(offset.getX()) >= 1.0 || Math.abs(offset.getY()) >= 1.0) {
			LOG.debug("Shift > 1.0, skipping"); 
		} else {
			int xrange = Math.min(width, character2.getWidth());
			int yrange = Math.min(this.getHeight(), character2.getHeight());
			SVGG g = new SVGG();
			total = 0;
			sum = 0;
			for (int x = 0; x < xrange; x++) {
				for (int y = 0; y < yrange; y++) {
					int gray = Util.getGray(this.image.getRGB(x,y));
					int gray2 = Util.getGray(character2.image.getRGB(x,y));
					addGrayComponentsToTotals(character2, gray, gray2);
					if (title != null) {
						drawPixel(g, x, y, gray, gray2);
					}
				}
			}
			if (title != null) {
				SVGSVG.wrapAndWriteAsSVG(g, new File("target/corrGreen"+title+".svg"));
			}
			cor = sum / total;
		}
		return cor;
		
	}
	
	private void addGrayComponentsToTotals(GrayCharacter character2, int gray, int gray2) {
		int diff = Math.abs(gray2 - gray);
		int max = Math.max(gray, gray2);
		total += max;
		int score = max - 2 * diff;
		sum += score;
	}

	private void drawPixel(SVGG g, int x, int y, int gray, int gray2) {
		SVGRect rect = new SVGRect((double)x, (double)y, 1.0, 1.0);
		Color color = new Color(255-gray, 0, 255-gray2);
		String colorS = "#"+Integer.toHexString(color.getRGB()).substring(2);
		rect.setFill(colorS);
		rect.setStroke("none");
		g.appendChild(rect);
	}

	/** get weighted centre of image.
	 * 
	 * center weighted by gray intensity of each pixel
	 * 
	 * @return weighted centre
	 */
	public Real2 getCentre() {
		Real2 centre = new Real2(0.0, 0.0);
		double sumGray = 0.0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int gray = Util.getGray(image.getRGB(x,y));
				if (gray < 0) {
					throw new RuntimeException("bad gray value "+Integer.toHexString(gray));
				}
				centre.plusEquals(new Real2(x * gray, y * gray));
				sumGray += gray;
			}
		}
		centre.multiplyEquals(1./sumGray);
		return centre;
	}

	private int getHeight() {
		return height;
	}

	private int getWidth() {
		return width;
	}

}
