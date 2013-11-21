package org.xmlcml.image.lines;

import gibara.CannyEdgeDetector;

import java.awt.image.BufferedImage;

public class EdgeDetector extends AbstractDetector {

	private CannyEdgeDetector detector;

	public EdgeDetector() {
		init();
	}
	
	private void init() {
		detector = new CannyEdgeDetector();
		detector.setLowThreshold(0.5f);
		detector.setHighThreshold(1f);
	}
		
	private BufferedImage getEdges() {
		return outputImage;
	}
	
	protected void process() {
		detector.setSourceImage(inputImage);
		try {
			detector.process();
		} catch (Exception e) {
			throw new RuntimeException("Cannot process: "+file, e);
		}
		outputImage = detector.getEdgesImage();
	}
}
