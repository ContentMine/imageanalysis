package org.xmlcml.image.pixel.nucleus;

import org.xmlcml.image.pixel.Pixel;
import org.xmlcml.image.pixel.PixelIsland;
import org.xmlcml.image.pixel.PixelList;
import org.xmlcml.image.pixel.PixelNucleus;

/** probably shouldn't exist.
 * 
 * shouldn't be created and should be flattenable.
 * 
 * @author pm286
 *
 */
public class TwoWayNucleus extends PixelNucleus {

	public TwoWayNucleus(Pixel centrePixel, PixelList pixelList, PixelIsland island) {
		super(centrePixel, pixelList, island);
	}
	
}
