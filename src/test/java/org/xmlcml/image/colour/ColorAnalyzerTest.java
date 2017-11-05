package org.xmlcml.image.colour;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.graphics.svg.util.ImageIOUtil;
import org.xmlcml.image.ImageAnalysisFixtures;
import org.xmlcml.image.ImageUtil;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import boofcv.io.image.UtilImageIO;

/** classifies the colours used in a diagram.
 * 
 * @author pm286
 *
 */
public class ColorAnalyzerTest {

	private final static Logger LOG = Logger.getLogger(ColorAnalyzerTest.class);
	
//	@Test
//	public void countColors() throws Exception {
//		BufferedImage image = ImageIO.read(new File(ImageAnalysisFixtures.TEXT_DIR, "phylo.jpg"));
//		ColorAnalyzer analyzer = new ColorAnalyzer(image);
//		analyzer.setXYRange(new Int2Range(new IntRange(0, 300), new IntRange(50, 300)));
//		LOG.trace(analyzer.getWidth()+"/"+analyzer.getHeight());
//		analyzer.set4Bits(true);
//		Multiset<Integer> colorSet = analyzer.createColorSet();
//		for (Entry entry : colorSet.entrySet()) {
//			if (entry.getCount() < 10) continue;
//			int ll = (Integer)entry.getElement();
//			HueChromaLuminance hcl = HueChromaLuminance.createHCLfromRGB(ll);
//			//System.out.println(hcl+" "+Integer.toHexString(ll)+" "+entry.getCount());
//		}
////		List<Integer> colorList = new ArrayList<Integer>(colorSet);
////		Collections.sort(colorList);
////		for (Integer color : colorList) {
////			System.out.println(Integer.toHexString(color));
////		}
//	}
	
	@Test
		public void testPosterize() {
			int nvalues = 4; // i.e. 16-bit color
			nvalues = 2;
			BufferedImage image = UtilImageIO.loadImage(new File(ImageAnalysisFixtures.PROCESSING_DIR, "phylo.jpg").toString());
			ImageUtil.flattenImage(image, nvalues);
			ColorAnalyzer colorAnalyzer = new ColorAnalyzer(image);
			Multiset<RGBColor> set = colorAnalyzer.getOrCreateColorSet();
			for (Entry<RGBColor> entry : set.entrySet()) {
//				System.out.println(entry+"  "+entry.getCount()); 
			}
			ImageIOUtil.writeImageQuietly(image, new File("target/posterize.png"));
		}

	@Test
	/** LONG
	 * 
	 * @throws IOException
	 */
	@Ignore // LONG
	public void testPosterizePhylo() throws IOException {
		testPosterize0("phylo");
	}

	@Test
	@Ignore
	public void testPosterize22249() throws IOException {
		testPosterize0("22249");
	}

	@Test
	@Ignore
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
		ColorAnalyzer colorAnalyzer = new ColorAnalyzer();
		colorAnalyzer.readImage(new File(ImageAnalysisFixtures.PROCESSING_DIR, filename+".png"));
		colorAnalyzer.setOutputDirectory(new File("target/"+filename));
		colorAnalyzer.defaultPosterize();
	}

	

}
