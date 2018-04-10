package org.xmlcml.image.pixel;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.xmlcml.euclid.JodaDate;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;

/** writes pixels to SVG file.
 * crude
 * might integrate with logger
 * 
 * @author pm286
 *
 */
public class PixelDebugger {
	private static final Logger LOG = Logger.getLogger(PixelDebugger.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static void debugToRandomFile(PixelList pixelList, File debugDir) {
		String s = new DateTime().toString().replaceAll("/", "_");
		debugDir.mkdirs();
		SVGSVG.wrapAndWriteAsSVG(pixelList.getOrCreateSVG(), new File(debugDir, s+".svg"));
	}
}
