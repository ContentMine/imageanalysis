package org.xmlcml.image.lines.hough;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGLine;
 
/** 
 * 
 * Informed in part by code from Olly Oechsle, University of Essex, Date: 13-Mar-2008 
 * @author pm286
 * @version 1.0 
 */ 
public class HoughLine { 

	private static final String H = "H";
	private static final String V = "V";

	private final static Logger LOG = Logger.getLogger(HoughLine.class);
	
    public static final int NULL = 0;
    
	protected double theta; 
    protected double r;
	private int height;
	private int width;
	private float centerX;
	private float centerY;

	private int houghSize;
	private int houghColour;
	private int segmentColour;
	private BufferedImage image;

	private Int2 minPoint;
	private Int2 maxPoint;
	private int newColour;
	private Real2Array pointArray;
	private double maxSeparation;
	private List<Segment> segmentList;
	private Segment segment;
	private Double minSegmentLength;
	private int lineSpread;

    /** 
     * Initialises the hough line 
     */ 
    public HoughLine(double theta, double r) { 
        this.theta = theta; 
        this.r = r; 
        initialize();
    } 
 
	private void initialize() {
		maxSeparation = 2.0;
        minSegmentLength = 5.0;
		lineSpread = 2;
	}

    /** 
     * Draws the line on the image of your choice with the RGB colour of your choice. 
     */ 
    public void draw(BufferedImage image, int houghColour, int segmentColour) { 
 
    	this.image = image;
        this.houghColour = houghColour;
        this.segmentColour = segmentColour;
        height = image.getHeight(); 
        width = image.getWidth(); 
 
        houghSize = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
 
        centerX = width / 2; 
        centerY = height / 2; 
 
		pointArray = new Real2Array();

		int xy = -1;
        if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) { 
            drawVertical(); 
            xy = 1;
        } else { 
            drawHorizontal(); 
            xy = 0;
        } 
        createSegments(xy);
    }

	private void createSegments(int xy) {
		Real2 lastPoint = null;
		pointArray.sortAscending(xy);
		for (Real2 point : pointArray) {
			double dist = point.getDistance(lastPoint);
			if (lastPoint == null || Double.isNaN(dist) || dist < maxSeparation ) {
				addPointToSegment(point);
			} else {
				endSegment();
			}
			lastPoint = point;
		}
		endSegment();
	}

	private void endSegment() {
		ensureSegmentList();
		if (segment != null) {
			Double length = segment.getLength();
			if (length != null && length > minSegmentLength) {
				segment.normalize();
				segmentList.add(segment);
			}
		}
		segment = null;
	}

	private void addPointToSegment(Real2 point) {
		if (segment == null) {
			segment = new Segment();
		}
		segment.addPoint(point);
	}

	private void drawHorizontal() {
		for (int dy = -lineSpread; dy <= lineSpread; dy++){
			for (int x = 0; x < width; x++) { 
			    int y = (int) ((((r - houghSize) - ((x - centerX) * Math.cos(theta))) / Math.sin(theta)) + centerY) + dy; 
			    if (y < height && y >= 0) { 
			        processPoint(x, y, H);
			    } 
			}
		}
	}

	private void drawVertical() {
		for (int dx = -lineSpread; dx <= lineSpread; dx++){
			for (int y = 0; y < height; y++) { 
			    int x = (int) ((((r - houghSize) - ((y - centerY) * Math.sin(theta))) / Math.cos(theta)) + centerX) + dx; 
			    if (x < width && x >= 0) { 
			        processPoint(x, y, V);
			    } 
			}
		}
	}

	private void processPoint(int x, int y, String hv) {
		setRGB(x, y); 
		addPointToExtractedLine(x, y, hv);
	}

	private void setRGB(int x, int y) {
		newColour = NULL;
		if (segmentColour == NULL) {
			image.setRGB(x, y, houghColour);
		} else {
			int currentColor = image.getRGB(x, y);
			if (false) {
			} else if (currentColor == segmentColour) {
				newColour = segmentColour;
			} else if (currentColor == Color.WHITE.getRGB()) {
				newColour = segmentColour;
			} else if (currentColor == Color.BLACK.getRGB()) {
				newColour = houghColour;
			} else if (currentColor == houghColour) {
			} else if (currentColor == segmentColour) {
			} else {
				throw new RuntimeException("?? "+currentColor);
			}
			image.setRGB(x, y, newColour);
		}
	}

	private void addPointToExtractedLine(int x, int y, String hv) {
		if (newColour == segmentColour) {
			pointArray.add(new Real2(x, y));
			Int2 i2 = new Int2(x, y);
			if (minPoint == null || maxPoint == null) {
				minPoint = i2;
				maxPoint = i2;
			} else {
				if (H.equals(hv) && i2.getX() < minPoint.getX() ||
				    V.equals(hv) && i2.getY() < minPoint.getY()) {
					minPoint = i2;
				} else if (H.equals(hv) && i2.getX() > maxPoint.getX() ||
						   V.equals(hv) && i2.getY() > maxPoint.getY()) {
					maxPoint = i2;
				}
			}
		}
	}
	
	public Int2 getMinPoint() {return minPoint;}
	public Int2 getMaxPoint() {return maxPoint;}

	public List<Segment> getSegments() {
		ensureSegmentList();
		return segmentList;
	}

	private void ensureSegmentList() {
		if (segmentList == null) {
			segmentList = new ArrayList<Segment>();
		}
	}

} 
