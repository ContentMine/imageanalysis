package org.xmlcml.image.tess;

import java.io.FileOutputStream;

import org.junit.Test;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.image.Fixtures;

public class SVGGeneratorTest {
	
	@Test
	public void testHtml2SVG() throws Exception {
		SVGGenerator svgGenerator = new SVGGenerator();
		svgGenerator.readHtml(Fixtures.JOURNAL_HTML);
		SVGUtil.debug(svgGenerator.getSVG(), new FileOutputStream("target/journal.svg"), 1);
	}
}
