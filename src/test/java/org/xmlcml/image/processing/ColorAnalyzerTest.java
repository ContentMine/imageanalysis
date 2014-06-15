package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.image.Fixtures;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/** classifies the colours used in a diagram.
 * 
 * @author pm286
 *
 */
public class ColorAnalyzerTest {

	private final static Logger LOG = Logger.getLogger(ColorAnalyzerTest.class);
	
	@Test
	public void countColours() throws Exception {
		BufferedImage image = ImageIO.read(new File(Fixtures.TEXT_DIR, "phylo.jpg"));
		ColourAnalyzer analyzer = new ColourAnalyzer(image);
		analyzer.setXYRange(new Int2Range(new IntRange(0, 300), new IntRange(50, 300)));
		LOG.trace(analyzer.getWidth()+"/"+analyzer.getHeight());
		analyzer.set4Bits(true);
		Multiset<Integer> colorSet = analyzer.createColorSet();
		for (Entry entry : colorSet.entrySet()) {
			if (entry.getCount() < 10) continue;
			int ll = (Integer)entry.getElement();
			HueChromaLuminance hcl = HueChromaLuminance.createHCLfromRGB(ll);
			//System.out.println(hcl+" "+Integer.toHexString(ll)+" "+entry.getCount());
		}
//		List<Integer> colorList = new ArrayList<Integer>(colorSet);
//		Collections.sort(colorList);
//		for (Integer color : colorList) {
//			System.out.println(Integer.toHexString(color));
//		}
	}
}
