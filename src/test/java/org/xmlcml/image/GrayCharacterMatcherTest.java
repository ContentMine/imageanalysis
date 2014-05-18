package org.xmlcml.image;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Util;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.text.GrayCharacterMatcher;
import org.xmlcml.image.text.fonts.ReferenceFontManager;

public class GrayCharacterMatcherTest {

	static final ReferenceFontManager FONT_MANAGER = new ReferenceFontManager();

	@Test
	@Ignore // rather expensive as a test
	public void testCorrelateBMCAgainstHelvetica() throws Exception {
		GrayCharacterMatcher matcher = new GrayCharacterMatcher();
		matcher.setMinimumCorrelation(0.20);
		matcher.setReferenceFont(FONT_MANAGER.getFont(ReferenceFontManager.HELVETICA));
		matcher.setTestImage(new File(Fixtures.TEXT_DIR, "1471-2148-14-20-test.jpg"));
		matcher.setMaximumPixels(1000);
		matcher.setOverlay(true);
		matcher.setScale(true);
		matcher.setBinarize(true);
		matcher.setPlotSingleChar(true);
		matcher.setCreateSVG(true);
//		matcher.setCreateSVG(false);
		matcher.match();
		double correlation = matcher.getCorrelation();
		Assert.assertEquals("correlation", 0.36, Util.format(correlation, 2), 0.01);
		SVGG g = matcher.getSVGG();
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/1471-2148-14-20.svg"));
	}
	
	@Test
	public void testC() {
		
	}

}
