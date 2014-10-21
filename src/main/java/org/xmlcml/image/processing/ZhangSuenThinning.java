package org.xmlcml.image.processing;

	 
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.xmlcml.euclid.Int2;
 
/**
 *
 * @author nayef
 */
public class ZhangSuenThinning extends Thinning {
 
    private static final String LEFT_TOP = "lt";
	private static final String RIGHT_BOTTOM = "rb";

	public ZhangSuenThinning(BufferedImage image) {
    	super(image);
    }


	public ZhangSuenThinning() {
		super();
	}

	@Override
	public void doThinning() {
    	boolean hasChange;
    	List<Int2> pointsToChange = new LinkedList<Int2>();
        do {
 
            hasChange = false;
            hasChange = iterateOverPoints(hasChange, pointsToChange, RIGHT_BOTTOM);
            hasChange = iterateOverPoints(hasChange, pointsToChange, LEFT_TOP);
        } while (hasChange);
    }


	private boolean iterateOverPoints(boolean hasChange, List<Int2> pointsToChange, String trbl) {
		for (int y = 1; y + 1 < binary.length; y++) {
		    for (int x = 1; x + 1 < binary[y].length; x++) {
		        int sumCyclicChanges = getSumCyclicChanges(y, x);
		        int neighbourSum = getNeighbourSum(y, x);
				boolean neighbourFunction = extracted(y, x, sumCyclicChanges,
						neighbourSum);
		        boolean triangles = false;
		        if (LEFT_TOP.equals(trbl)) {
		        	triangles = leftTopTriangles(y, x);
		        } else if (RIGHT_BOTTOM.equals(trbl)) {
		        	triangles = rightBottomTriangles(y, x);
		        }
				if ( neighbourFunction && triangles) {
		            pointsToChange.add(new Int2(x, y));
		            hasChange = true;
		        }
		    }
		}
		resetChangedPointsToZeroAndClearList(pointsToChange);
		return hasChange;
	}


	private boolean extracted(int y, int x, int sumCyclicChanges,
			int neighbourSum) {
		boolean neighbourFunction = binary[y][x]==1 && 2 <= neighbourSum && neighbourSum <= 6 && sumCyclicChanges == 1;
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
