package org.xmlcml.image;

import java.awt.image.BufferedImage;

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

}
