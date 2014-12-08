package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntMatrix;

/** used for filling a PixelList exterior and interior.
 * 
 * @author pm286
 *
 */
public class PixelListFloodFill {

	private final static Logger LOG = Logger.getLogger(PixelListFloodFill.class);
	
	private static Int2[] NEIGHBOURS = 
		{new Int2(0,1), new Int2(1,0), new Int2(0,-1), new Int2(-1,0)};
	
	private PixelList pixelList;
	private int[][] externalGrid;
	private Stack<Int2> stack;
	private int xmin;
	private int ymin;
	private int expandedWidth;
	private int expandedHeight;
	private Int2Range int2bBox;

	public PixelListFloodFill(PixelList pixelList) {
		this.pixelList = pixelList;
		this.fillExteriorOfGrid();
	}
	
	/** writes a white boundary round each edge to help floodfill.
	 * 
	 * @param pixelList TODO
	 * @return
	 */
	public int[][] createGridWithWhiteBorders() {
		int [][] grid = null;
		int2bBox = pixelList.getIntBoundingBox();
		xmin = int2bBox.getXRange().getMin();
		ymin = int2bBox.getYRange().getMin();
		// the 1 is the fencepost; 2 is for the new borders
		expandedWidth = int2bBox.getXRange().getRange() + 1 + 2;
		expandedHeight = int2bBox.getYRange().getRange() + 1 + 2;
		
		grid = createGridWithUnused();
		fillGridWithUsedPixelList(grid);
		return grid;
	}

	private void fillGridWithUsedPixelList(int[][] binaryBox) {
		for (Pixel pixel : pixelList) {
			Int2 xy = pixel.getInt2();
			int x = xy.getX() - xmin + 1;
			int y = xy.getY() - ymin + 1;
			if (x < expandedWidth && y < expandedHeight) {
				binaryBox[x][y] = 1;
			} else {
				PixelList.LOG.error("Tried to write pixel outside image area "+xy);
			}
		}
	}

	private int[][] createGridWithUnused() {
		int[][] grid = new int[expandedWidth][expandedHeight];
		for (int i = 0; i < expandedWidth; i++) {
			for (int j = 0; j < expandedHeight; j++) {
				grid[i][j] = 0;
			}
		}
		return grid;
	}

	/** fills from edge inwards.
	 * 
	 * requires a white border
	 * starts at 0,0 - returns all pixels outside the PixelList. Then these
	 * will be used to mark pixels which do not fill the PixelList
	 * @param pixelList TODO
	 * @return
	 */
	public int[][] fillExteriorOfGrid() {
		externalGrid = createGridWithWhiteBorders();
		Int2 int2 = new Int2(0,0);
		stack = new Stack<Int2>();
		this.pushOntoStackAndMarkPixel(externalGrid, int2);
		while (!stack.isEmpty()) {
			Int2 next = stack.pop();
			List<Int2> nextList = getUnusedNeighbours(next);
			for (Int2 pixel : nextList) {
				pushOntoStackAndMarkPixel(externalGrid, pixel);
			}
		}
		return externalGrid;
	}

	private void pushOntoStackAndMarkPixel(int[][] externalGrid, Int2 int2) {
		stack.push(int2);
		int x = int2.getX();
		int y = int2.getY();
		externalGrid[x][y] = 1;
	}

	/**
	 * 
	 * @param pixel TODO
	 * @return unused neighbours
	 */
	private List<Int2> getUnusedNeighbours(Int2 pixel) {
		List<Int2> list = new ArrayList<Int2>();
		for (Int2 offset : NEIGHBOURS) {
			Int2 neighbour = pixel.plus(offset);
			int x = neighbour.getX();
			int y = neighbour.getY();
			if (x >=0 && x < externalGrid.length  &&
				y >=0 && y < externalGrid[0].length ) {
				if (externalGrid[x][y] == 0) {
					list.add(neighbour);
				}
			}
		}
		return list;
	}

	public PixelList createInteriorPixelList() {
		PixelList filledList = new PixelList();
		for (int i = 0; i < expandedWidth; i++) {
			for (int j = 0; j < expandedHeight; j++) {
				if (externalGrid[i][j] == 0) {
					filledList.add(new Pixel(i, j));
				}
			}
		}
		return filledList;
	}

}
