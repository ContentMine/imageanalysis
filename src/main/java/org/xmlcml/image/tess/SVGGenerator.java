package org.xmlcml.image.tess;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.html.HtmlElement;
import org.xmlcml.html.HtmlSpan;
import org.xmlcml.html.util.HtmlUtil;


public class SVGGenerator {

	private static final String BBOX = "bbox";
	private static final double FONT_SCALE = 1.5;
	
	private SVGSVG svgsvg;

	public SVGGenerator() {
		
	}

	/**
<span class='ocrx_word' id='word_2' title="bbox 1173 17 1582 66">Passeriformes</span>
	 * @param htmlFile
	 */
	public void readHtml(File htmlFile) {
		HtmlElement element = null;
		try {
			element = HtmlUtil.readAndCreateElement(htmlFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (element != null) {
			svgsvg = new SVGSVG();
			svgsvg.setWidth(2500.);
			svgsvg.setHeight(2500.);
			List<HtmlSpan> spanList = HtmlSpan.extractSpans(HtmlUtil.getQueryHtmlElements(
					element, ".//h:span[not(h:span)]"));
			for (HtmlSpan span : spanList) {
				String title = span.getTitle();
				if (title == null) {
					throw new RuntimeException("null title");
				}
				List<String> tt = Arrays.asList(title.split("\\s+"));
				if (tt.size() != 5 || !(BBOX.equals(tt.get(0)))) {
					throw new RuntimeException("bad title: "+title);
				}
				Real2 xy0 = new Real2(tt.get(1)+" "+tt.get(2));
				Real2 xy1 = new Real2(tt.get(3)+" "+tt.get(4));
				Real2Range bbox =new Real2Range(xy0, xy1);
				Double xRange = bbox.getXRange().getRange();
				Double yRange = bbox.getYRange().getRange();
				boolean vertical = yRange > xRange;
				double fontSize =  (vertical) ? xRange * FONT_SCALE : yRange * FONT_SCALE ;
				SVGText text = new SVGText(xy0, span.getValue());
				text.setFontSize(fontSize);
				if (vertical) {
					SVGG g = new SVGG();
					svgsvg.appendChild(g);
					g.appendChild(text);
					Transform2 t2 = Transform2.getRotationAboutPoint(new Angle(Math.PI / 2.), xy0);
					g.setTransform(t2);
					
				} else {
					svgsvg.appendChild(text);
				}
			}
		}
	}
	
	public SVGSVG getSVG() {
		return svgsvg;
	}
}
