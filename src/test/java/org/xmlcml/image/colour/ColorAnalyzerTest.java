package org.xmlcml.image.colour;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.colour.ColourAnalyzer;
import org.xmlcml.image.colour.HueChromaLuminance;

import boofcv.io.image.UtilImageIO;

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
	
	@Test
		public void testPosterize() {
			int nvalues = 4; // i.e. 16-bit color
			nvalues = 2;
			BufferedImage image = UtilImageIO.loadImage(new File(Fixtures.PROCESSING_DIR, "phylo.jpg").toString());
			ImageUtil.flattenImage(image, nvalues);
			ColourAnalyzer colorAnalyzer = new ColourAnalyzer(image);
			Multiset<Integer> set = colorAnalyzer.createColorSetNew();
			for (Entry<Integer> entry : set.entrySet()) {
				int ii = ((int) entry.getElement()) & 0x00ffffff;
				// uncomment for debug
	//			System.out.println(Integer.toHexString(ii)+"  "+entry.getCount()); 
			}
			/**
			    ff  26      BLUE
			  7f7f  351     cyan
			7f7f7f  34019   grey
			7fffff  80      cyanw
			  7fff  49      cyan
			    7f  2301    blue
			7fff7f  586     greenw
			7f7fff  102006  cyanw
			7f007f  27      magenta
			     0  25578   BLACK
			ffff7f  40      yellow
			ff7fff  37      magenta
			7f0000  2863    red
			  7f00  489     green
			ff7f7f  1562    redw
			7f7f00  1676    yellow
			ffffff  937110  white
			*/
	
			ImageUtil.writeImageQuietly(image, new File("target/posterize.png"));
		}

	@Test
	public void testPosterize22249() throws IOException {
		testPosterize0("22249");
	}

	@Test
	public void testPosterize36933() throws IOException {
		testPosterize0("36933");
	}

	@Test
	public void testPosterizeSpect2() throws IOException {
		testPosterize0("spect2");
	}

	@Test
	public void testPosterizeSpect5() throws IOException {
		testPosterize0("spect5");
	}

	@Test
	public void testPosterizeMadagascar() throws IOException {
		testPosterize0("madagascar");
	}

	private void testPosterize0(String filename) throws IOException {
		ColourAnalyzer colorAnalyzer = new ColourAnalyzer();
		colorAnalyzer.readImage(new File(Fixtures.PROCESSING_DIR, filename+".png"));
		colorAnalyzer.setStartPlot(1);
		colorAnalyzer.setMaxPixelSize(1000000);
		colorAnalyzer.setIntervalCount(4);
		colorAnalyzer.setEndPlot(15);
		colorAnalyzer.setMinPixelSize(300);
		colorAnalyzer.flattenImage();
		colorAnalyzer.setOutputDirectory(new File("target/"+filename));
		colorAnalyzer.analyzeFlattenedColours();
	}

	

}