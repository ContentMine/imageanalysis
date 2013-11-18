package org.xmlcml.image.processing;

import java.io.IOException;

import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class HistogramEQTest {

	@Test
	public void testHistogram() throws IOException {
    	HistogramEqualization histogramEQ = new HistogramEqualization();
//        histogramEQ.readImage(Fixtures.HISTOGRAM_JPG);
        histogramEQ.readImage(Fixtures.HISTOGRAM_PNG);
        histogramEQ.histogramEqualization();
        histogramEQ.writeImage("target/histogram.png");

	}
}
