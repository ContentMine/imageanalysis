package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.image.lines.PixelPath;
import org.xmlcml.image.processing.Pixel.Marked;

/** connected list of pixels.
 * 
 * It is possible to traverse all pixels without encountering "gaps". May contain "holes"
 * (e.g letter "O").If there are objects within the hole (e.g. "copyright" 0x00A9 which has "C" inside a circle)
 * they may be initially be in a separate island - we may coordinate this later
 * 
 * Islands can consist of:
 * <ul> 
 * <li>A single pixel</li<
 * <li>A connected chain of pixels (with 2 terminal pixels</li<
 * <li>A tree of pixels with braching nodes (3-8 connected, but likely 3)</li<
 * <li>The above with nuclei (ganglia) in chains or nodes. The nuclei arise from incomplete thinning and 
 * are to be reduced to single pixels or chains while retaining connectivity</li<
 * </ul>
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
	private List<Nucleus> nucleusList;
	private List<Pixel> terminalPixels;
	private Map<Pixel, Nucleus> nucleusMap;

	private Set<Pixel> usedPixels;

	private Set<Nucleus> usedNuclei;

	private List<PixelPath> pixelPaths;
	
	public PixelIsland() {
	}
	
	public PixelIsland(List<Pixel> pixelList) {
		this(pixelList, false);
	}

	/** 
	 * 
	 * @param pixelList
	 * @param diagonal were diagonal neighbours allowed in creating the pixelList?
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
		throw new RuntimeException("NYI");
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
		terminalPixels = getNodesWithNeighbours(1);
		
		return terminalPixels;
	}

	public List<Pixel> getNodesWithNeighbours(int neighbourCount) {
		List<Pixel> nodePixels = new ArrayList<Pixel>();
		for (Pixel pixel : pixelList) {
			int nCount = getNeighbourCount(pixel);
			if (neighbourCount == nCount) {
				nodePixels.add(pixel);
			}
		}
		return nodePixels;
	}

	private int getNeighbourCount(Pixel pixel) {
		return pixel.getNeighbours(this).size();
	}
	
	/** for start of spanningTree or other traversals.
	 * 
	 * If there are terminal pixels get the first one.
	 * else get the first pixel. This may not be reproducible.
	 * 
	 * @return first pixel or null for empty island (which shouldn't happen)
	 */
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

	/** 
	 * this may be obsolete.
	 * 
	 * @param removePixels
	 */
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
		LOG.debug(toRemove);
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

	public List<Nucleus> getNucleusList() {
		nucleusList = new ArrayList<Nucleus>();
		nucleusMap = new HashMap<Pixel, Nucleus>();
		Set<Pixel> multiplyConnectedPixels = new HashSet<Pixel>();
		for (int i = 3; i <= 8; i++) {
			multiplyConnectedPixels.addAll(getNodesWithNeighbours(i));
		}
		while (multiplyConnectedPixels.size() > 0) {
			Nucleus nucleus = makeNucleus(multiplyConnectedPixels);
			nucleusList.add(nucleus);
			LOG.debug("nucl size: "+nucleus.size()+" spikes: "+nucleus.getSpikeSet());
		}
		return nucleusList;
	}

	private Nucleus makeNucleus(Set<Pixel> multiplyConnectedPixels) {
		Nucleus nucleus = new Nucleus(this);
		Stack<Pixel> pixelStack = new Stack<Pixel>();
		Pixel pixel = multiplyConnectedPixels.iterator().next();
		removeFromSetAndPushOnStack(multiplyConnectedPixels, pixelStack, pixel);
		while(!pixelStack.isEmpty()) {
			pixel = pixelStack.pop();
			nucleus.add(pixel);
			nucleusMap.put(pixel, nucleus);
			List<Pixel> neighbours = pixel.getNeighbours(this);
			for (Pixel neighbour : neighbours) {
				if (!nucleus.contains(neighbour) && multiplyConnectedPixels.contains(neighbour)) {
					removeFromSetAndPushOnStack(multiplyConnectedPixels, pixelStack, neighbour);
				}
			}
		}
		return nucleus;
	}

	private void removeFromSetAndPushOnStack(Set<Pixel> multiplyConnectedPixels,
			Stack<Pixel> pixelStack, Pixel pixel) {
		pixelStack.push(pixel);
		multiplyConnectedPixels.remove(pixel);
	}

	public void flattenNuclei() {
		List<Nucleus> nucleusList = this.getNucleusList();
		for (Nucleus nucleus : nucleusList) {
			nucleus.ensureFlattened(3);
		}
	}

	public void debug(int nucleusCount) {
		List<Nucleus> nucleusList = getNucleusList();
		Assert.assertEquals("nucleusList",  nucleusCount, nucleusList.size());
		for (Nucleus nucleus : nucleusList) {
			int spikeSize = nucleus.getSpikeSet().size();
			if (spikeSize == 1) {
				SpanningTreeTest.LOG.debug("terminus nucleus "+nucleus.size());
			} else if (spikeSize == 1) {
				SpanningTreeTest.LOG.debug("branch nucleus "+nucleus.size()+" spikes "+spikeSize);
			}
		}
	}

	public void processAndDebugNuclei(int count) {
		debug(count);
		flattenNuclei();
	}

	public List<PixelPath> createPixelPathList() {
		pixelPaths = new ArrayList<PixelPath>();
		getNucleusList();
		LOG.debug("nucleus list "+nucleusList.size());
		processTerminals();
		return pixelPaths;
	}

	private void processTerminals() {
		getTerminalPixels();
		Set<Pixel> usedTerminalPixels = new HashSet<Pixel>();
		for (int i = 0; i < terminalPixels.size(); i++) {
			Pixel terminal = terminalPixels.get(i);
			if (usedTerminalPixels.contains(terminal)) {
				continue;
			}
			usedTerminalPixels.add(terminal);
			PixelPath pixelPath = findTerminalOrBranch(terminal);
			usedTerminalPixels.add(pixelPath.getLastPixel());
			pixelPaths.add(pixelPath);
		}
	}

	private PixelPath findTerminalOrBranch(Pixel terminalPixel) {
		PixelPath pixelPath = new PixelPath();
		usedPixels = new HashSet<Pixel>();
		usedNuclei = new HashSet<Nucleus>();
		Pixel currentPixel = terminalPixel;
		while (true) {
			usedPixels.add(currentPixel);
			pixelPath.add(currentPixel);
			Pixel nextPixel = null;
			Nucleus nucleus = nucleusMap.get(currentPixel);
			if (nucleus != null) {
				nextPixel = processNucleusAndGetNextPixel(nucleus, currentPixel);
			} else {
				nextPixel = getNextPixel(currentPixel);
			}
			if (nextPixel == null) {
				LOG.debug("end terminalOrBranch");
				break;
			} else {
				currentPixel = nextPixel;
				LOG.debug("next: "+nextPixel.getInt2());
			}
		}
		return pixelPath;
	}

	private Pixel getNextPixel(Pixel pixel) {
		LOG.trace(pixel);
		List<Pixel> neighbours = pixel.getNeighbours(this);
		List<Pixel> unusedPixels = new ArrayList<Pixel>();
		for (Pixel neighbour : neighbours) {
			if (!usedPixels.contains(neighbour)) {
				unusedPixels.add(neighbour);
			}
		}
		int size = unusedPixels.size();
		if (size == 0) {
//			LOG.debug(neighbours);
			LOG.debug("Found terminal"); // temp
		} else if (size > 1) {
			throw new RuntimeException("Cannot find unique next pixel: "+size);
		}
		return size == 0 ? null : unusedPixels.get(0);
	}

	/** "jumps over" 2-spike nucleus to "other side"
	 * 
	 * 
	 * marks as used previous neighbour of nextPixel
	 * 
	 * @param nucleus
	 * @param currentPixel
	 * @return nextPixel far side of nucleus
	 */
	private Pixel processNucleusAndGetNextPixel(Nucleus nucleus, Pixel currentPixel) {
		Pixel nextPixel = null;
		Set<Pixel> spikeSetCopy = new HashSet<Pixel>(nucleus.getSpikeSet());
		if (spikeSetCopy.size() <= 1) {
			throw new RuntimeException("Spike set cannot have < 2 ");
		} else if (spikeSetCopy.size() == 2) {
			spikeSetCopy.removeAll(getUsedNeighbours(currentPixel));
			if (spikeSetCopy.size() != 1) {
				throw new RuntimeException("BUG: should have removed pixel");
			}
			nextPixel = spikeSetCopy.iterator().next();
			for (Pixel neighbour : nextPixel.getNeighbours(this)) {
				if (nucleus.contains(neighbour)) {
					usedPixels.add(neighbour);
				}
			}
			LOG.debug("Skipped 2-spike Nucleus from "+currentPixel.getInt2()+" to "+nextPixel.getInt2());
		} else {
			// treat as terminal
			if (!nucleus.getSpikeSet().removeAll(currentPixel.getNeighbours(this))) {
				throw new RuntimeException("Failed to remove");
			}
			nextPixel = null;
		}
		return nextPixel;
	}

	private Set<Pixel> getUsedNeighbours(Pixel currentPixel) {
		Set<Pixel> usedNeighbours = new HashSet<Pixel>();
		List<Pixel> neighbours = currentPixel.getNeighbours(this);
		for (Pixel neighbour : neighbours) {
			if (usedPixels.contains(neighbour)) {
				usedNeighbours.add(neighbour);
			}
		}
		return usedNeighbours;
	}
}
