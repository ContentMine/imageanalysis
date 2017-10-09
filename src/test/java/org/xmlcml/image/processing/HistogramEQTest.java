package org.xmlcml.image.processing;

import java.io.IOException;

import org.junit.Test;
import org.xmlcml.graphics.svg.util.ImageIOUtil;
import org.xmlcml.image.Fixtures;

public class HistogramEQTest {

	/** reference test from wikipedia.
	 * 
	 * @throws IOException
	 */
	@Test
	
	public void testHistogram() throws IOException {
    	HistogramEqualization histogramEQ = new HistogramEqualization();
        histogramEQ.readImage(Fixtures.HISTOGRAM_PNG);
        histogramEQ.histogramEqualization();
        ImageIOUtil.writeImageQuietly(histogramEQ.getEqualized(), "target/histogram/histogram.png");

	}
	/** histogram on photograph.
	 * 
	 * not sure this is worth it. Brings up the background too much
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMoleculePhotograph() throws IOException {
    	HistogramEqualization histogramEQ = new HistogramEqualization();
        histogramEQ.readImage(Fixtures.MOLECULE_20131119_JPG);
        histogramEQ.histogramEqualization();
        ImageIOUtil.writeImageQuietly(histogramEQ.getEqualized(), "target/histogram/molecule.png");

	}

}
