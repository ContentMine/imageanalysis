package org.xmlcml.image;

public class ImageParameters extends AbstractParameters {

	private double segmentTolerance;

	public ImageParameters() {
		setDefaults();
	}
	
	private void setDefaults() {
		segmentTolerance = 2.0;
	}
	
	public double getSegmentTolerance() {
		return segmentTolerance;
	}

	public void setSegmentTolerance(double tolerance) {
		this.segmentTolerance = tolerance;
	}

}
