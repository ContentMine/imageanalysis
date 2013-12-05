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
	}
	
	public PixelIsland(List<Pixel> pixelList) {
		this(pixelList, false);
	}

	/** 
	 * 
	 * @param pixelList
	 * @param diagonal wer diagnal neighbours allowed in creating the pixelList?
	 */
	public PixelIsland(List<Pixel> pixelList, boolean diagonal) {
		this.pixelList = pixelList;
		this.allowDiagonal = diagonal;
		indexPixelsAndUpdateMetadata();
	}
	
	public void addPixel(Pixel pixel) {
		this.pixelList.add(pixel);
		addPixelMetadata(pixel);
	}
	

	private void indexPixelsAndUpdateMetadata() {
		for (Pixel pixel : pixelList) {
			addPixelMetadata(pixel);
		}
	}

	private void addPixelMetadata(Pixel pixel) {
		ensureInt2Range();
		ensurePixelByCoordMap();
		Int2 int2 = pixel.getInt2();
		pixelByCoordMap.put(int2, pixel);
		int2range.add(int2);
		if (leftmostCoord == null || leftmostCoord.getX() < int2.getX()) {
			leftmostCoord = int2;
		}
		pixel.setIsland(this);
	}

	private void ensureInt2Range() {
		if (this.int2range == null) {
			int2range = new Int2Range();
		}
	}

	public int size() {
		return this.pixelList.size();
	}

	public SpanningTree createSpanningTree(Pixel pixel) {
		SpanningTree spanningTree = new SpanningTree(this);
		spanningTree.start(pixel);
		return spanningTree;
	}

	public void createSpanningTree() {
		this.getTerminalPixels();
	}
	
	public Map<Int2, Pixel> getPixelByCoordMap() {
		ensurePixelByCoordMap();
		return pixelByCoordMap;
	}

	private void ensurePixelByCoordMap() {
		if (pixelByCoordMap == null) {
			pixelByCoordMap = new HashMap<Int2, Pixel>();
		}
	}

	public List<Pixel> getPixelList() {
		return pixelList;
	}

	public List<Pixel> getTerminalPixels() {
		List<Pixel> terminalPixels = new ArrayList<Pixel>();
		for (Pixel pixel : pixelList) {
			int neighbourCount = pixel.getNeighbours(this).size();
			System.out.println(neighbourCount);
			if (neighbourCount == 1) {
				terminalPixels.add(pixel);
			}
		}
		return terminalPixels;
	}

	
}
