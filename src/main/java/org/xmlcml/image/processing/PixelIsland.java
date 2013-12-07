package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.image.processing.Pixel.Marked;

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

	private enum Type {
		BEND2,
		BILOZENGE,
		CHAIN2,
		CHAIN3,
		DEMILOZENGE,
		IMPOSSIBLE,
		NODE4,
		NODE5,
		NULL,
		TERMINAL,
	}
	private final static Logger LOG = Logger.getLogger(PixelIsland.class);
	
	private List<Pixel> pixelList;
	boolean allowDiagonal = false;
	private Int2Range int2range;
	private Int2 leftmostCoord;
	Map<Int2, Pixel> pixelByCoordMap;

	private List<Pixel> removeList;
	
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
			LOG.trace(neighbourCount);
			if (neighbourCount == 1) {
				terminalPixels.add(pixel);
			}
		}
		return terminalPixels;
	}
	
	public Pixel getStartPixel() {
		Pixel start = null;
		List<Pixel> terminalList = getTerminalPixels();
		if (terminalList.size() > 0) {
			start = terminalList.get(0);
		} else if (pixelList.size() > 0) {
			start = pixelList.get(0);
		}
		return start;
	}

	public void setDiagonal(boolean diagonal) {
		this.allowDiagonal = diagonal;
	}

	public void findNucleiAndMarkToRemove(boolean removePixels) {
		List<Triangle> lastTriangleList = null;
		Type type = null;
		Type lastType = null;
		removeList = new ArrayList<Pixel>();
		for (Pixel pixel : pixelList) {
			type = Type.NULL;
			List<Pixel> neighbourList = pixel.getNeighbours(Marked.ALL);
			List<Triangle> triangleList = scanTriangles(pixel, neighbourList);
			if (neighbourList.size() == 1) {
				type = Type.TERMINAL;
			} else if (neighbourList.size() == 2) {
				type = Type.CHAIN2;
				if (triangleList.size() == 1) {
					type = Type.BEND2;
				}
			} else if (neighbourList.size() == 3) {
				if (triangleList.size() == 1) {
					type = Type.CHAIN3;
				} else if (triangleList.size() == 2) {
					type = Type.DEMILOZENGE;
					if (lastType.equals(Type.DEMILOZENGE)) {
						tidyLozenge(lastTriangleList, triangleList);
					}
			} else if (triangleList.size() == 3) {
					type = Type.IMPOSSIBLE;
				}
			} else if (neighbourList.size() == 4) {
				if (triangleList.size() == 1) {
					type = Type.NODE4;
				} else if (triangleList.size() == 2) {
					type = Type.BILOZENGE;
				} else if (triangleList.size() == 3) {
					type = Type.IMPOSSIBLE;
				} else if (triangleList.size() == 4) {
					type = Type.NODE4;
				} else if (triangleList.size() >= 5) {
					type = Type.IMPOSSIBLE;
				}
			} else if (neighbourList.size() >= 5) {
				type = Type.NODE5;
			}
			LOG.trace(type);
			lastType = type;
			lastTriangleList = triangleList;
		}
		if (removePixels) {
			this.removePixels();
		}

	}

	private void tidyLozenge(List<Triangle> lastTriangleList, List<Triangle> triangleList) {
		Set<Pixel> lastUnion = getUnion(lastTriangleList);
		Set<Pixel> thisUnion = getUnion(triangleList);
		if (!lastUnion.equals(thisUnion)) {
			throw new RuntimeException("mismatched union");
		}
		if (lastUnion.size() != 4) {
			throw new RuntimeException("bad union size: "+lastUnion.size());
		}
		Set<Pixel> lastIntersection = getIntersection(lastTriangleList);
		Set<Pixel> thisIntersection = getIntersection(triangleList);
		if (!lastIntersection.equals(thisIntersection)) {
			throw new RuntimeException("mismatched intersection");
		}
		if (lastIntersection.size() != 2) {
			throw new RuntimeException("bad intersection size: "+lastIntersection.size());
		}
		LOG.debug("Lozenge");
		Pixel toRemove = lastIntersection.iterator().next();
		removeList.add(toRemove);
	}

	/**
	private List<Pixel> pixelList;
	boolean allowDiagonal = false;
	private Int2Range int2range;
	private Int2 leftmostCoord;
	Map<Int2, Pixel> pixelByCoordMap;
	 * @param pixel
	 */
	private void remove(Pixel pixel) {
		if (pixelList.remove(pixel)) {
			// leaves int2range and laftmostCoord dirty
			int2range = null;
			leftmostCoord = null;
			pixelByCoordMap.remove(pixelByCoordMap.get(pixel.getInt2()));
			removeFromNeighbourNeighbourList(pixel);
		}
	}

	private void removeFromNeighbourNeighbourList(Pixel pixel) {
		List<Pixel> neighbours = pixel.getNeighbours(this);
		for (Pixel neighbour : neighbours) {
			neighbour.getNeighbours(this).remove(pixel);
		}
	}

	private Set<Pixel> getUnion(List<Triangle> triangleList) {
		Set<Pixel> unionSet = triangleList.get(0).addAll(triangleList.get(1));
		return unionSet;
	}

	private Set<Pixel> getIntersection(List<Triangle> triangleList) {
		Set<Pixel> intersectionSet = triangleList.get(0).retainAll(triangleList.get(1));
		return intersectionSet;
	}

	private List<Triangle> scanTriangles(Pixel pixel, List<Pixel> neighbourList) {
		List<Triangle> triangleList = new ArrayList<Triangle>();
		int count = neighbourList.size();
		for (int i = 0; i < count - 1; i++) {
			for (int j = i+1; j < count; j++ ) {
				if (scanTriangle(neighbourList, i, j)) {
					triangleList.add(new Triangle(pixel, neighbourList.get(i), neighbourList.get(j)));
				}
			}
		}
		return triangleList;
	}

	/** look for two neighbours that form a triangle.
	 * 
	 * @param neighbourList
	 * @param i first neighbour pixel
	 * @param j second neighbour pixel
	 * @return Int2(i,j) if they form a triangle, else null
	 */
	private boolean scanTriangle(List<Pixel> neighbourList, int i, int j) {
		if (i >= 0 && i < neighbourList.size() && 
			j >= 0 && j < neighbourList.size() &&
			i != j) {
			Pixel pixeli = neighbourList.get(i);
			Pixel pixelj = neighbourList.get(j);
			if (pixeli.getNeighbours(Marked.ALL).contains(pixelj)) {
				return true;
			}
		} 
		return false;
	}

	public void removePixels() {
		for (Pixel pixel : removeList) {
			LOG.debug("remove: "+pixel.getInt2());
			remove(pixel);
		}
	}

	public void cleanChains() {
		this.findNucleiAndMarkToRemove(true);
		this.findNucleiAndMarkToRemove(false);
	}
}
