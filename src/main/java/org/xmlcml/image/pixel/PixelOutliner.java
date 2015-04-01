package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.image.pixel.IntLine.ChangeDirection;

public class PixelOutliner {

	private static Logger LOG = Logger.getLogger(PixelOutliner.class);

	private final static ChangeDirection[] DIRECTIONS = { ChangeDirection.LEFT,
			ChangeDirection.AHEAD, ChangeDirection.RIGHT };

	private PixelList pixelList;
	private int maxIter = 1000; // for testing and checking
	private List<IntLine> lineList;
	private PixelList usedPixels;
	private List<SVGPolygon> polygonList;
	private boolean failedConverge;
	private int minPolySize = 20;

	public PixelOutliner(PixelList pixelList) {
		this.pixelList = pixelList;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	/**
	 * create outline starting at Northern extreme pixel.
	 * 
	 * goes horizontally East/right and continues clockwise round pixelList.
	 * 
	 * @return
	 */
	public List<SVGPolygon> createOutline() {
		polygonList = new ArrayList<SVGPolygon>();
		while (pixelList.size() > 0) {
			PixelList extremes = pixelList.findExtremePixels();
			Pixel startPixel = extremes.get(0);
			SVGPolygon polygon = iterateClockwiseRoundPerimeter(startPixel);
			if (polygon.size() > minPolySize) {
				polygonList.add(polygon);
				LOG.debug("poly size " + polygon.size() + " pixelList "
						+ pixelList.size());
			}
			for (Pixel pixel : usedPixels) {
				while (pixelList.remove(pixel)) {
					
				}
			}
			if (failedConverge) {
				LOG.error("FAILED TO CONVERGE");
				break;
			}
		}
		LOG.debug("polygons " + polygonList.size());
		return polygonList;
	}

	public List<SVGPolygon> getPolygonList() {
		return polygonList;
	}

	private SVGPolygon iterateClockwiseRoundPerimeter(Pixel startPixel) {
		failedConverge = false;
		lineList = new ArrayList<IntLine>();
		usedPixels = new PixelList();
		Int2 current = startPixel.getInt2();
		Int2 next = new Int2(current);
		next.incrementX();
		IntLine line = new IntLine(current, next, startPixel.getInt2(),
				startPixel);
		int count = 0;
		while (!next.equals(current)) {
			lineList.add(line);
			IntLine nextLine = createNextLine(line);
			if (nextLine == null) {
				throw new RuntimeException("no line found");
			}
			Pixel currentPixel = nextLine.getCurrentPixel();
			if (currentPixel != null) {
				usedPixels.add(currentPixel);
			} else {
				LOG.error("null current pixel");
			}
			line = nextLine;
			if (nextLine.equals(lineList.get(0))) {
				break;
			}
			if (count++ >= maxIter) {
				LOG.error("failed to converge after " + count);
				failedConverge = true;
				break;
			}
		}
		return getSVGPolygon();
	}

	/**
	 * creates line from current position to next.
	 * 
	 * Iterates through left, ahead, right and breaks after first which provide
	 * pixel on the right
	 * 
	 * @param line
	 * @return
	 */
	private IntLine createNextLine(IntLine line) {
		LineDirection dir = line.getDirection();
		IntLine nextLine = null;
		for (ChangeDirection change : DIRECTIONS) {
			LineDirection newDirection = dir.getNewDirection(change);
			nextLine = line.getNextLine(line.getDirection(), newDirection,
					change, pixelList);
			if (nextLine != null) {
				break;
			}
		}
		if (nextLine == null) {
			throw new RuntimeException("Failed to find next line");
		}
		return nextLine;
	}

	public SVGPolygon getSVGPolygon() {
		// createOutline();
		Real2Array r2a = new Real2Array();
		for (IntLine line : lineList) {
			Real2 midPoint = line.getMidPoint();
			r2a.add(midPoint);
		}
		SVGPolygon polygon = new SVGPolygon(r2a);
		polygon.setStrokeWidth(0.1);
		polygon.setFill("none");
		return polygon;
	}

	public void setMinPolySize(int size) {
		this.minPolySize = size;
	}

}
