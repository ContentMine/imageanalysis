package org.xmlcml.image.pixel;

import java.awt.Graphics2D;
import java.awt.geom.Line2D.Double;

import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGRect;

/** holds a linear segment of a path extracted from a PixelIsland.
 * 
 * Generally created by a segmentation algorithm such as Douglas Peucker.
 * Primarily holds an SVGLine
 * 
 * @author pm286
 *
 */
public class PixelSegment {

	private SVGLine line;
	
	public PixelSegment() {
		
	}

	public PixelSegment(Real2 point0, Real2 point1) {
		if (point0 == null || point1 == null) {
			throw new RuntimeException("Null points in segment: "+point0+";"+point1);
		}
		line = new SVGLine(point0, point1);
	}

	/**
	 * @param x12
	 * @param serial
	 * @see org.xmlcml.graphics.svg.SVGLine#setXY(org.xmlcml.euclid.Real2, int)
	 */
	public void setXY(Real2 x12, int serial) {
		line.setXY(x12, serial);
	}

	/**
	 * @return
	 * @see org.xmlcml.graphics.svg.SVGLine#getLine2()
	 */
	public Double getLine2() {
		return line.getLine2();
	}

	/**
	 * @param g2d
	 * @see org.xmlcml.graphics.svg.SVGElement#draw(java.awt.Graphics2D)
	 */
	public void draw(Graphics2D g2d) {
		line.draw(g2d);
	}

	/**
	 * @param fill
	 * @see org.xmlcml.graphics.svg.GraphicsElement#setFill(java.lang.String)
	 */
	public void setFill(String fill) {
		line.setFill(fill);
	}

	/**
	 * @param stroke
	 * @see org.xmlcml.graphics.svg.GraphicsElement#setStroke(java.lang.String)
	 */
	public void setStroke(String stroke) {
		line.setStroke(stroke);
	}

	/**
	 * @param width
	 * @see org.xmlcml.graphics.svg.SVGLine#setWidth(double)
	 */
	public void setWidth(double width) {
		line.setWidth(width);
	}

	/**
	 * @param stroke
	 * @param fill
	 * @param strokeWidth
	 * @param opacity
	 * @return
	 * @see org.xmlcml.graphics.svg.SVGElement#drawBox(java.lang.String, java.lang.String, double, double)
	 */
	public SVGRect drawBox(String stroke, String fill, double strokeWidth,
			double opacity) {
		return line.drawBox(stroke, fill, strokeWidth, opacity);
	}

	/**
	 * @return
	 * @see nu.xom.Element#toXML()
	 */
	public final String toXML() {
		return line.toXML();
	}

	@Override
	public String toString() {
		return "PixelSegment [line=" + line + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PixelSegment other = (PixelSegment) obj;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		return true;
	}

	public SVGLine getLine() {
		return line;
	}

	public void setLine(SVGLine line) {
		this.line = line;
	}

	public Real2 getPoint(int i) {
		return (line == null || i < 0 || i > 1) ? null : line.getXY(i);
	}

	public Line2 getEuclidLine() {
		return line == null ? null : line.getEuclidLine();
	}
	
}
