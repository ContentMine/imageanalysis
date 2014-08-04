package org.xmlcml.image;

public class ImageParameters extends AbstractParameters {

	private double segmentTolerance;
	private String stroke;
	private double lineWidth;
	private String fill;

	public ImageParameters() {
		setDefaults();
	}
	
	private void setDefaults() {
		segmentTolerance = 2.0;
		stroke = "green";
		lineWidth = 1.0;
		fill = "none";
	}
	
	public double getSegmentTolerance() {
		return segmentTolerance;
	}

	public void setSegmentTolerance(double tolerance) {
		this.segmentTolerance = tolerance;
	}

	public String getStroke() {
		return stroke;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public String getFill() {
		return fill;
	}

}
