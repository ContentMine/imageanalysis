package org.xmlcml.image.pixel.nucleus;

import org.xmlcml.image.pixel.Pixel;
import org.xmlcml.image.pixel.PixelIsland;
import org.xmlcml.image.pixel.PixelList;
import org.xmlcml.image.pixel.PixelNucleus;

public class CrossNucleus extends PixelNucleus {

	public CrossNucleus(Pixel centrePixel, PixelList pixelList, PixelIsland island) {
		super(centrePixel, pixelList, island);
	}

	public static int getCrossCentre(Pixel centrePixel, PixelList pixelList, PixelIsland island) {
		centrePixel = null;
		int pixelNumber = -1;
		for (int i = 0; i < 5; i++) {
			Pixel pixel = pixelList.get(i);
			if (pixel.getOrthogonalNeighbours(island).size() == 4) {
				if (centrePixel != null) {
					throw new RuntimeException("Bad cross: " + pixelList);
				}
				centrePixel = pixel;
				pixelNumber = i;
			}
		}
		return pixelNumber;
	}


}
