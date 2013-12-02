package org.xmlcml.image.lines.hough.old;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealMatrix;

public class Accumulator {

	private int angleCols;
	private int distRows;
	private RealMatrix matrix;
	
	public Accumulator(int angleCols, int distRows) {
		this.setAngleSteps(angleCols);
		this.setDistanceSteps(distRows);
	}
	
	public void setAngleSteps(int angleCols) {
		this.angleCols = angleCols;
		
	}
	public void setDistanceSteps(int distRows) {
		this.distRows = distRows;
	}

	/** add a point
	 * 
	 * @param jcol
	 * @param irow
	 */
	public void add(int jcol, int irow) {
		Real2 xy = getXY(jcol, irow);
	}

	private Real2 getXY(int jcol, int irow) {
		// TODO Auto-generated method stub
		return null;
	}

}
