package org.xmlcml.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ThinningService;

public class Util {

	public static BufferedImage thin(BufferedImage image) {
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		return image;
	}

	public static BufferedImage binarize(BufferedImage image) {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.setImage(image);
		otsuBinarize.toGray();
		otsuBinarize.binarize();
		image = otsuBinarize.getBinarizedImage();
		return image;
	}
	/** extracts a subimage translated to 0,0.
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage clipSubImage(BufferedImage image, Int2Range boundingBox) {
		IntRange xRange = boundingBox.getXRange();
		IntRange yRange = boundingBox.getYRange();
		int xMin = xRange.getMin();
		int yMin = yRange.getMin();
		int w = xRange.getRange();
		int h = yRange.getRange();
		Rectangle rect = new Rectangle(xMin, yMin, w, h);
//		Raster r = image.getData(rect);
		BufferedImage subImage = new BufferedImage(w, h, image.getType());
//		subImage.setData(r);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = image.getRGB(i + xMin, j + yMin);
				subImage.setRGB(i, j,  rgb);
			}
		}
		return subImage;
	}

}
