package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;

/** connected list of pixels.
 * 
 * It is possible to traverse all pixels without encountering "gaps". May contain "holes"
 * (e.g letter "O").If there are objects within the hole (e.g. "copyright" 0x00A9 which has "C" inside a circle)
 * they may be initially be in a separate island - we may coordinate this later
 * 
 * @author pm286
 *
 */
public class PixelIsland {

	private List<Pixel> pixelList;
	boolean allowDiagonal = false;
	private Int2Range int2range;
	private Int2 leftmostCoord;
	Map<Int2, Pixel> pixelByCoordMap;
	
	public PixelIsland() {
		this.pixelList = new ArrayList<Pixel>();
	}
	
	public PixelIsland(List<Pixel> pixelList) {
		this.pixelList = pixelList;
	}

	/** 
	 * 
	 * @param pixelList
	 * @param diagonal wer diagnal neighbours allowed in creating the pixelList?
	 */
	public PixelIsland(List<Pixel> pixelList, boolean diagonal) {
		this(pixelList);
		this.allowDiagonal = diagonal;
		indexPixelsByCoordinate();
	}
	
	public void addPixel(Pixel pixel) {
		this.pixelList.add(pixel);
		addPixelMetadata(pixel);
	}
	

	private void indexPixelsByCoordinate() {
		pixelByCoordMap = new HashMap<Int2, Pixel>();
		int2range = new Int2Range();
		leftmostCoord = null;
		for (Pixel pixel : pixelList) {
			addPixelMetadata(pixel);
		}
	}

	private void addPixelMetadata(Pixel pixel) {
		Int2 int2 = pixel.getInt2();
		pixelByCoordMap.put(pixel.getInt2(), pixel);
		int2range.add(int2);
		if (leftmostCoord == null || leftmostCoord.getX() < int2.getX()) {
			leftmostCoord = int2;
		}
		pixel.setIsland(this);
	}

	public int size() {
		return this.pixelList.size();
	}

	public SpanningTree createSpanningTree() {
		SpanningTree spanningTree = new SpanningTree(this);
		spanningTree.start(leftmostCoord);
		return spanningTree;
	}
	
}
