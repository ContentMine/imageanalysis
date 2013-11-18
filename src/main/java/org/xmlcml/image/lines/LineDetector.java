package org.xmlcml.image.lines;

import java.awt.image.Raster;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.processing.ColorUtilities;

/**heuristic linedetector from PMR.
 * 
 * @author pm286
 *
 */
public class LineDetector extends AbstractDetector {

	private static final Logger LOG = Logger.getLogger(LineDetector.class);
	
	public final static Integer MINLEN = 20;

	private static final int CUTOFF = 375;

	enum Direction {
		HORIZ,
		VERT
	}
	
	private Raster raster;
	private int cols;
	private int rows;
	private int numData;
	private IntArray colArray;
	private IntArray rowArray;

	public LineDetector() {
		
	}
	
	@Override
	protected void process() {
		getRasterParameters();
		getRowAndColumnProjections();
		getHorizontalAndVerticalSegments();
	}

	private void getHorizontalAndVerticalSegments() {
		svg = new SVGSVG();
		drawGrid(colArray, Direction.VERT);
		drawGrid(rowArray, Direction.HORIZ);
	}
	
	private void drawGrid(IntArray array, Direction dir) {
		int rasterSize = dir.equals(Direction.HORIZ) ? raster.getWidth() : raster.getHeight();
		System.out.println(rasterSize+" "+array.size());
		int[] pix = new int[numData];
		Real2 start = null;
		Real2 end = null;
		for (int i = 0; i < array.size(); i++) {
			if (array.elementAt(i) > MINLEN) {
				SVGLine line = null;
				for (int j = 0; j < rasterSize; j++) {
					pix = dir.equals(Direction.HORIZ) ? raster.getPixel(j, i, pix) : raster.getPixel(i, j, pix);
					int value = ColorUtilities.getValue(pix);
					if (value  < CUTOFF) {
						if (line == null) { // new line
							line = new SVGLine();
							start = dir.equals(Direction.HORIZ) ?
									new Real2((double)j, (double)i) : new Real2((double) i , (double)j); 
						}
					} else {
						if (line != null) { // end line
							end = dir.equals(Direction.HORIZ) ?
									new Real2((double) j, (double)i) : new Real2((double) i , (double) j); 
							line = new SVGLine(start, end);
							line.setStroke((dir.equals(Direction.HORIZ)) ? "red" : "blue");
							svg.appendChild(line);
							line = null;
						}
					}
				}
//				System.out.println(line.toXML());
//				svg.appendChild(line);
			}
		}
	}

	private void getRowAndColumnProjections() {
		colArray = new IntArray(cols);
		rowArray = new IntArray(rows);
		for (int irow = 0; irow < rows; irow++) {
			for (int jcol = 0; jcol < cols; jcol++) {
				int[] pix = new int[numData];
				pix = raster.getPixel(jcol, irow, pix);
				int value = ColorUtilities.getValue(pix);
				if (value < CUTOFF) { // black
					colArray.incrementElementAt(jcol);
					rowArray.incrementElementAt(irow);
				}
			}
		}
		LOG.debug(colArray);
		LOG.debug(rowArray);
	}

	private void getRasterParameters() {
		raster = inputImage.getRaster();
		cols = raster.getWidth();
		rows = raster.getHeight();
		numData = raster.getNumDataElements();
		LOG.debug(" w "+ cols +" h "+ rows +" num " + numData);
	}
}
