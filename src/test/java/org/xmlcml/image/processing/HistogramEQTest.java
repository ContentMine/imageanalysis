package org.xmlcml.image.processing;

import java.io.IOException;

import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageUtil;

public class HistogramEQTest {

	@Test
	public void testHistogram() throws IOException {
    	HistogramEqualization histogramEQ = new HistogramEqualization();
        histogramEQ.readImage(Fixtures.HISTOGRAM_PNG);
        histogramEQ.histogramEqualization();
        ImageUtil.writeImageQuietly(histogramEQ.getEqualized(), "target/histogram/histogram.png");

	}
	@Test
	public void testMolecule() throws IOException {
    	HistogramEqualization histogramEQ = new HistogramEqualization();
        histogramEQ.readImage(Fixtures.MOLECULE_20131119_JPG);
        histogramEQ.histogramEqualization();
        ImageUtil.writeImageQuietly(histogramEQ.getEqualized(), "target/histogram/molecule.png");

	}

}
