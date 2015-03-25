package org.xmlcml.image.processing;

	 
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xmlcml.euclid.Int2;
 
/**
 *
 * @author nayef
 */
public class ZhangSuenThinning extends Thinning {
 
    private static final String LEFT_TOP = "lt";
	private static final String RIGHT_BOTTOM = "rb";
	
	private Set<Int2> blackPixels = new HashSet<Int2>();

	public ZhangSuenThinning(BufferedImage image) {
    	super(image);
    }


	public ZhangSuenThinning() {
		super();
	}

	@Override
	public void doThinning() {
    	boolean hasChange = false;
        blackPixels.clear();
        hasChange = iterateOverPoints(RIGHT_BOTTOM, true);
        hasChange |= iterateOverPoints(LEFT_TOP, true);
        while (hasChange) {
 
            hasChange = false;
            hasChange = iterateOverPoints(RIGHT_BOTTOM, false);
            hasChange |= iterateOverPoints(LEFT_TOP, false);
        }
    }


	private boolean iterateOverPoints(String trbl, boolean all) {
		List<Int2> pointsToChange = new ArrayList<Int2>();
		if (all) {
			for (int y = 1; y + 1 < binary.length; y++) {
			    for (int x = 1; x + 1 < binary[y].length; x++) {
			        processPoint(trbl, pointsToChange, y, x);
			        if (binary[y][x] == 1) {
			        	blackPixels.add(new Int2(x, y));
			        }
			    }
			}
		} else {
			Iterator<Int2> it = blackPixels.iterator();
			while (it.hasNext()) {
				Int2 pixel = it.next();
		        boolean changed = processPoint(trbl, pointsToChange, pixel.getY(), pixel.getX());
		        if (changed) {
		        	it.remove();
		        }
			}
		}
		boolean hasChange = (pointsToChange.size() > 0);
		resetChangedPointsToZeroAndClearList(pointsToChange);
		return hasChange;
	}


	private boolean processPoint(String trbl, List<Int2> pointsToChange, int y, int x) {
		int sumCyclicChanges = getSumCyclicChanges(y, x);
		int neighbourSum = getNeighbourSum(y, x);
		boolean neighbourContrast = getNeighbourContrast(y, x, sumCyclicChanges, neighbourSum);
		boolean triangles = false;
		if (LEFT_TOP.equals(trbl)) {
			triangles = leftTopTriangles(y, x);
		} else if (RIGHT_BOTTOM.equals(trbl)) {
			triangles = rightBottomTriangles(y, x);
		}
		if (neighbourContrast && triangles) {
		    boolean pathologicalCaseCheck = checkForPathologicalCase(y, x);
		    if (!pathologicalCaseCheck) {
		    	Int2 coord = new Int2(x, y);
		        pointsToChange.add(coord);
		        return true;
		    }
		}
		return false;
	}

	private boolean checkForPathologicalCase(int y, int x) {
		try {
			if (binary[y + 1][x] == 0) {
				return false;
			} else if (binary[y][x + 1] == 0) {
				return false;
			} else if (binary[y + 1][x + 1] == 0) {
				return false;
			} else if (binary[y + 2][x] == 1) {
				return false;
			} else if (binary[y + 2][x + 1] == 1) {
				return false;
			} else if (binary[y + 2][x + 2] == 1) {
				return false;
			} else if (binary[y + 1][x + 2] == 1) {
				return false;
			} else if (binary[y][x + 2] == 1) {
				return false;
			} else if (binary[y - 1][x - 1] == 1) {
				return false;
			} else if (binary[y - 1][x] == 1) {
				return false;
			} else if (binary[y - 1][x + 1] == 1) {
				return false;
			} else if (binary[y - 1][x + 2] == 1) {
				return false;
			} else if (binary[y][x - 1] == 1) {
				return false;
			} else if (binary[y + 1][x - 1] == 1) {
				return false;
			} else if (binary[y + 2][x - 1] == 1) {
				return false;
			}
			return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	private boolean getNeighbourContrast(int y, int x, int sumCyclicChanges,
			int neighbourSum) {
		boolean neighbourFunction = binary[y][x]==1 && 3 <= neighbourSum && neighbourSum <= 6 && sumCyclicChanges == 1;
		return neighbourFunction;
	}

	private boolean leftTopTriangles(int y, int x) {
		return (binary[y - 1][x] * binary[y][x + 1] * binary[y][x - 1] == 0)
		    && (binary[y - 1][x] * binary[y + 1][x] * binary[y][x - 1] == 0);
	}


	private boolean rightBottomTriangles(int y, int x) {
		return (binary[y - 1][x] * binary[y][x + 1] * binary[y + 1][x] == 0)
		    && (binary[y][x + 1] * binary[y + 1][x] * binary[y][x - 1] == 0);
	}

	private void resetChangedPointsToZeroAndClearList(List<Int2> pointsToChange) {
		for (Int2 point : pointsToChange) {
		    binary[point.getY()][point.getX()] = 0;
		}
        pointsToChange.clear();
	}
 

}
