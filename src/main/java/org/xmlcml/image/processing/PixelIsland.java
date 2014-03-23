package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.lines.PixelPath;

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
	
	private List<Pixel> pixelList; // these may have original coordinates
	boolean allowDiagonal = false;
	private Int2Range int2range;
	private Int2 leftmostCoord;
	Map<Int2, Pixel> pixelByCoordMap; // find pixel or null
//	private List<Pixel> removeList;
	private List<Nucleus> nucleusList;
	private List<Pixel> terminalPixels;
	private Map<Pixel, Nucleus> nucleusMap;
	private Set<Pixel> usedPixels;
//	private Set<Nucleus> usedNuclei;
	private List<PixelPath> pixelPaths;
	private double tolerance = 1.5;

	private List<Real2Array> segmentArrayList;

	private Set<Triangle> triangleSet;
	
	public PixelIsland() {
		this.pixelList = new ArrayList<Pixel>();
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
	
	public Real2Range getBoundingBox() {
		Real2Range r2r = new Real2Range();
		for (Pixel pixel : pixelList) {
			r2r.add(new Real2(pixel.getInt2()));
		}
		return r2r;
	}

	public Int2Range getIntBoundingBox() {
		Int2Range i2r = new Int2Range();
		for (Pixel pixel : pixelList) {
			i2r.add(pixel.getInt2());
		}
		return i2r;
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
		List<Pixel> terminalSpikedList = getTerminalSpikes();
		if (terminalSpikedList.size() > 0) {
			terminalPixels.addAll(terminalSpikedList);
			LOG.trace("adding pseudo-terminals: "+terminalSpikedList);
		}
		return terminalPixels;
	}

	/** finds spikes on nuclei that are actually terminals.
	 * 
	 * Example:
	 * 1 2
	 *   3 4
	 *      5 6
	 *         7
	 *         
	 * 1234 should be a nucleus but it may be a 3-nucleus (2,3,4) with a pseudo-spike 1
	 * return it to the nucleus and remove it from the spike set and label it as
	 * a terminal.
	 * 
	 * Believe the algorithm is (a) find nuclei (b) find spikeset (c) see if any spikes have
	 * 2-neighbours, both in nucleus
	 * 
	 * @return
	 */
	private List<Pixel> getTerminalSpikes() {
		List<Pixel> terminalList = new ArrayList<Pixel>();
//		Pixel terminalSpike = null;
		if (nucleusList != null) {
			for (Nucleus nucleus : nucleusList) {
				Set<Pixel> spikeSet = nucleus.getSpikeSet();
				for (Pixel spike : spikeSet) {
					List<Pixel> spikeNeighbours = spike.getNeighbours(this);
					if (has2NeighboursInNucleus(nucleus, spikeNeighbours)) {
						terminalList.add(spike);
					}
				}
			}
		}
		return terminalList;
	}

	private boolean has2NeighboursInNucleus(Nucleus nucleus, List<Pixel> spikeNeighbours) {
		boolean terminal = false;
		if (spikeNeighbours.size() == 2) {
			terminal = true;
			for (Pixel neighbour : spikeNeighbours) {
				if (!nucleus.contains(neighbour)) {
					terminal = false;
				}
			}
		}
		return terminal;
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
//	private void findNucleiAndMarkToRemove(boolean removePixels) {
//		List<Triangle> lastTriangleList = null;
//		Type type = null;
//		Type lastType = null;
//		removeList = new ArrayList<Pixel>();
//		for (Pixel pixel : pixelList) {
//			type = Type.NULL;
//			List<Pixel> neighbourList = pixel.getNeighbours(Marked.ALL);
//			List<Triangle> triangleList = scanTriangles(pixel, neighbourList);
//			if (neighbourList.size() == 1) {
//				type = Type.TERMINAL;
//			} else if (neighbourList.size() == 2) {
//				type = Type.CHAIN2;
//				if (triangleList.size() == 1) {
//					type = Type.BEND2;
//				}
//			} else if (neighbourList.size() == 3) {
//				if (triangleList.size() == 1) {
//					type = Type.CHAIN3;
//				} else if (triangleList.size() == 2) {
//					type = Type.DEMILOZENGE;
//					if (lastType.equals(Type.DEMILOZENGE)) {
//						tidyLozenge(lastTriangleList, triangleList);
//					}
//			} else if (triangleList.size() == 3) {
//					type = Type.IMPOSSIBLE;
//				}
//			} else if (neighbourList.size() == 4) {
//				if (triangleList.size() == 1) {
//					type = Type.NODE4;
//				} else if (triangleList.size() == 2) {
//					type = Type.BILOZENGE;
//				} else if (triangleList.size() == 3) {
//					type = Type.IMPOSSIBLE;
//				} else if (triangleList.size() == 4) {
//					type = Type.NODE4;
//				} else if (triangleList.size() >= 5) {
//					type = Type.IMPOSSIBLE;
//				}
//			} else if (neighbourList.size() >= 5) {
//				type = Type.NODE5;
//			}
//			LOG.trace(type);
//			lastType = type;
//			lastTriangleList = triangleList;
//		}
//		if (removePixels) {
//			this.removePixels();
//		}
//
//	}

//	private void tidyLozenge(List<Triangle> lastTriangleList, List<Triangle> triangleList) {
//		Set<Pixel> lastUnion = getUnion(lastTriangleList);
//		Set<Pixel> thisUnion = getUnion(triangleList);
//		if (!lastUnion.equals(thisUnion)) {
//			throw new RuntimeException("mismatched union");
//		}
//		if (lastUnion.size() != 4) {
//			throw new RuntimeException("bad union size: "+lastUnion.size());
//		}
//		Set<Pixel> lastIntersection = getIntersection(lastTriangleList);
//		Set<Pixel> thisIntersection = getIntersection(triangleList);
//		if (!lastIntersection.equals(thisIntersection)) {
//			throw new RuntimeException("mismatched intersection");
//		}
//		if (lastIntersection.size() != 2) {
//			throw new RuntimeException("bad intersection size: "+lastIntersection.size());
//		}
//		LOG.trace("Lozenge");
//		Pixel toRemove = lastIntersection.iterator().next();
//		LOG.trace(toRemove);
//		removeList.add(toRemove);
//	}

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

//	private Set<Pixel> getUnion(List<Triangle> triangleList) {
//		Set<Pixel> unionSet = triangleList.get(0).addAll(triangleList.get(1));
//		return unionSet;
//	}
//
//	private Set<Pixel> getIntersection(List<Triangle> triangleList) {
//		Set<Pixel> intersectionSet = triangleList.get(0).retainAll(triangleList.get(1));
//		return intersectionSet;
//	}

//	private List<Triangle> scanTriangles(Pixel pixel, List<Pixel> neighbourList) {
//		List<Triangle> triangleList = new ArrayList<Triangle>();
//		int count = neighbourList.size();
//		for (int i = 0; i < count - 1; i++) {
//			for (int j = i+1; j < count; j++ ) {
//				if (scanTriangle(neighbourList, i, j)) {
//					triangleList.add(new Triangle(pixel, neighbourList.get(i), neighbourList.get(j)));
//				}
//			}
//		}
//		return triangleList;
//	}

	/** look for two neighbours that form a triangle.
	 * 
	 * @param neighbourList
	 * @param i first neighbour pixel
	 * @param j second neighbour pixel
	 * @return Int2(i,j) if they form a triangle, else null
	 */
//	private boolean scanTriangle(List<Pixel> neighbourList, int i, int j) {
//		if (i >= 0 && i < neighbourList.size() && 
//			j >= 0 && j < neighbourList.size() &&
//			i != j) {
//			Pixel pixeli = neighbourList.get(i);
//			Pixel pixelj = neighbourList.get(j);
//			if (pixeli.getNeighbours(Marked.ALL).contains(pixelj)) {
//				return true;
//			}
//		} 
//		return false;
//	}

//	private void removePixels() {
//		for (Pixel pixel : removeList) {
//			LOG.trace("remove: "+pixel.getInt2());
//			remove(pixel);
//		}
//	}

//	private void cleanChains() {
//		this.findNucleiAndMarkToRemove(true);
//		this.findNucleiAndMarkToRemove(false);
//	}

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
			LOG.trace("nucl "+nucleus.toString());
		}
		LOG.debug("nuclei: "+nucleusList.size());
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

	void flattenNuclei() {
		List<Nucleus> nucleusList = this.getNucleusList();
		int maxIterations = 3;
		for (Nucleus nucleus : nucleusList) {
			nucleus.ensureFlattened(maxIterations);
		}
	}

	void debug() {
		List<Nucleus> nucleusList = getNucleusList();
		for (Nucleus nucleus : nucleusList) {
			int spikeSize = nucleus.getSpikeSet().size();
			if (spikeSize == 1) {
				LOG.debug("terminus nucleus "+nucleus.size());
			} else if (spikeSize == 1) {
				LOG.debug("branch nucleus "+nucleus.size()+" spikes "+spikeSize);
			}
		}
	}

	public List<PixelPath> createPixelPathListStartingAtTerminals() {
		if (pixelPaths == null) {
			pixelPaths = new ArrayList<PixelPath>();
			removeHypotenuses();
			getNucleusList();
			LOG.trace("nucleus list "+nucleusList.size());
			getTerminalPixels();
			createPixelPathsStartingAtTerminals();
		}
		return pixelPaths;
	}

	/** remove any diagonal neighbours where other connecting pixels exist.
	 * 
	 * In 1.2
	 *    ..3
	 *      
	 * the neighbours might be 1-2 2-3 1-3, remove the 1-3
	 * 
	 * in 1.2
	 *    ....3
	 *        
	 * the enighbours are 1-2 and 2-3 - leave them
	 * 
	 */
	void removeHypotenuses() {
		createTriangleSet();
		LOG.trace("triangle "+triangleSet);
	}

	private void createTriangleSet() {
		triangleSet = new HashSet<Triangle>();
		for (Pixel pixel : pixelList) {
			Set<Triangle> triangleSet0 = pixel.getTriangles(this);
			triangleSet.addAll(triangleSet0);
		}
	}

	private void createPixelPathsStartingAtTerminals() {
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
//		usedNuclei = new HashSet<Nucleus>();
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
				LOG.trace("end terminalOrBranch");
				if (nucleus != null) {
					pixelPath.addFinalNucleus(nucleus);
				}
				break;
			} else {
				currentPixel = nextPixel;
				LOG.trace("next: "+nextPixel.getInt2());
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
//			LOG.trace(neighbours);
			LOG.trace("Found terminal"); // temp
		} else if (size > 1) {
			LOG.trace("Cannot find unique next pixel: "+size); // could be terminal in nucleus
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
			LOG.error("Spike set cannot have < 2 ");
			return null;
		} else if (spikeSetCopy.size() == 2) {
			spikeSetCopy.removeAll(getUsedNeighbours(currentPixel));
			if (spikeSetCopy.size() != 1) {
				LOG.error("BUG: should have removed pixel");
			}
			nextPixel = spikeSetCopy.iterator().next();
			for (Pixel neighbour : nextPixel.getNeighbours(this)) {
				if (nucleus.contains(neighbour)) {
					usedPixels.add(neighbour);
				}
			}
			LOG.trace("Skipped 2-spike Nucleus from "+currentPixel.getInt2()+" to "+nextPixel.getInt2());
		} else {
			// treat as terminal
			if (!nucleus.getSpikeSet().removeAll(currentPixel.getNeighbours(this))) {
				LOG.error("Failed to remove");
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

	public SVGG createSVGFromPixelPaths(boolean pixels) {
		SVGG gg = new SVGG();
		if (!pixels) {
			createPixelPathListStartingAtTerminals();
		}
		if (pixels || pixelPaths.size() == 0) {
			gg = plotPixels(pixelList);
		} else {
			for (PixelPath pixelPath : pixelPaths) {
				SVGG g = pixelPath.createSVGG();
				g.setStrokeWidth(0.5);
				gg.appendChild(g);
			}
		}
		return gg;
	}

	public SVGG plotPixels() {
		return PixelIsland.plotPixels(this.getPixelList());
	}
	
	public static SVGG plotPixels(List<Pixel> pixelList) {
		SVGG g = new SVGG();
		LOG.debug("pixelList "+pixelList.size());
		for (Pixel pixel : pixelList) {
			SVGRect rect = pixel.getSVGRect();
			LOG.trace(rect.getBoundingBox());
			g.appendChild(rect);
		}
		return g;
	}

	public List<Real2Array> createSegments(double tolerance) {
		createPixelPathListStartingAtTerminals();
		segmentArrayList = new ArrayList<Real2Array>();
		for (PixelPath pixelPath : pixelPaths) {
			Real2Array segmentArray = new Real2Array(pixelPath.createDouglasPeucker(tolerance));
			segmentArrayList.add(segmentArray);
		}
		return segmentArrayList;
	}

	public SVGG debugSVG(String filename) {
		SVGG g = createSVGFromPixelPaths(true);
		SVGSVG.wrapAndWriteAsSVG(g, new File(filename));
		return g;
	}

	public boolean fitsWithin(RealRange xSizeRange, RealRange ySizeRange) {
		double wmax = xSizeRange.getMax();
		double wmin = xSizeRange.getMin();
		double hmax = ySizeRange.getMax();
		double hmin = ySizeRange.getMin();
		Real2Range ibox = getBoundingBox();
		double width = ibox.getXRange().getRange();
		double height = ibox.getYRange().getRange();
		boolean include = ((width <= wmax && width >= wmin) &&
			(height <= hmax && height >= hmin));
		return include;
	}

	/** computes correlations and outputs images.
	 * 
	 * @param island2 must be binarized
	 * @param title if not null creates title.svg
	 * @return correlation
	 */
	public double binaryIslandCorrelation(PixelIsland island2, String title) {
		Int2Range bbox1 = this.getIntBoundingBox();
		Int2Range bbox2 = island2.getIntBoundingBox();
		int xRange1 = bbox1.getXRange().getRange();
		int yRange1 = bbox1.getYRange().getRange();
		int xMin1 = bbox1.getXRange().getMin();
		int yMin1 = bbox1.getYRange().getMin();
		int xRange2 = bbox2.getXRange().getRange();
		int yRange2 = bbox2.getYRange().getRange();
		int xMin2 = bbox2.getXRange().getMin();
		int yMin2 = bbox2.getYRange().getMin();
		int xrange = Math.max(xRange1, xRange2);
		int yrange = Math.max(yRange1, yRange2);
		LOG.trace(xrange+" "+yrange);
		double score = 0.;
		SVGG g = new SVGG();
		for (int i = 0; i < xrange; i++) {
			int x1 = xMin1 + i;
			int x2 = xMin2 + i;
			for (int j = 0; j < yrange; j++) {
				int y1 = yMin1 + j;
				int y2 = yMin2 + j;
				Int2 i2 = new Int2(x1, y1);
				Pixel pixel1 = pixelByCoordMap.get(i2);
				Pixel pixel2 = island2.pixelByCoordMap.get(new Int2(x2, y2));
				if (pixel1 != null) {
					g.appendChild(addRect(i2, "red"));
				}
				if (pixel2 != null) {
					g.appendChild(addRect(i2, "blue"));
				}
				if (pixel1 != null && pixel2 != null) {
					g.appendChild(addRect(i2, "purple"));
					score++;
				} else if(pixel1 == null && pixel2 == null) {
					score++;
				} else {
					score--;
				}
			}
		}
		if (title != null) {
			File file = new File("target/"+title+".svg");
			file.getParentFile().mkdirs();
			SVGSVG.wrapAndWriteAsSVG(g, file);
		}
		return score / (xrange * yrange);
	}

	private SVGRect addRect(Int2 i2, String color) {
		double x = i2.getX();
		double y = i2.getY();
		SVGRect rect = new SVGRect(new Real2(x, y), new Real2(x+1, y+1));
		rect.setStroke("none");
		rect.setFill(color);
		return rect;
	}

	/** clips rectangular image from rawImage corresponding to this.
	 * 
	 * WARNING. still adjusting inclusive/exclusive clip
	 * 
	 * @param rawImage to clip from
	 * @return image in raw image with same bounding box as this
	 */
	public BufferedImage clipSubimage(BufferedImage rawImage) {
		Int2Range i2r = getIntBoundingBox();
		// may have clipped 1 pixel too much...
		IntRange ix = i2r.getXRange();
		IntRange iy = i2r.getYRange();
		i2r = new Int2Range(new IntRange(ix.getMin(), ix.getMax()+1), new IntRange(iy.getMin(), iy.getMax()+1));
		BufferedImage subImage = ImageUtil.clipSubImage(rawImage, i2r);
		return subImage;
	}

	public BufferedImage createImage(int imageType) {
		Int2Range bbox = this.getIntBoundingBox();
		int xmin = bbox.getXRange().getMin();
		int ymin = bbox.getYRange().getMin();
		int w = bbox.getXRange().getRange();
		int h = bbox.getYRange().getRange();
		BufferedImage image = null;
		if (w == 0 || h == 0) {
			LOG.trace("zero pixel image");
			return image;
		}
		image = new BufferedImage(w, h, imageType);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				image.setRGB(i, j, 0xffffffff);
			}
		}
		for (Pixel pixel : this.getPixelList()) {
			Int2 xy = pixel.getInt2();
			int x = xy.getX() - xmin;
			int y = xy.getY() - ymin;
//			System.out.println(xy+" "+bbox+" "+w+" "+h+" "+x+" "+y);
			if (x < w && y < h) {
				image.setRGB(x, y, 0xff000000);
			}
		}
		return image;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("pixels "+((pixelList == null) ? null : pixelList.size()));
		sb.append("; int2range "+int2range);
		return sb.toString();
	}

	public void removePixels(PixelPath pixelPath) {
		List<Pixel> pixelList = pixelPath.getPixelList();
		for (Pixel pixel : pixelList) {
			this.remove(pixel);
		}
	}

	public List<SVGPolyline> createPolylinesAndRemoveUsedPixels(double epsilon) {
		List<PixelPath> pixelPaths = this.createPixelPathListStartingAtTerminals();
		LOG.debug("pixelPaths: "+pixelPaths.size());
		List<SVGPolyline> polylineList = new ArrayList<SVGPolyline>();
		for (PixelPath pixelPath : pixelPaths) {
			SVGPolyline polyline = pixelPath.createPolyline(epsilon);
			removePixels(pixelPath);
			polylineList.add(polyline);
		}
		return polylineList;
	}

	public void setPixelPaths(List<PixelPath> pixelPaths) {
		this.pixelPaths = pixelPaths;
	}

	public List<SVGPolyline> createPolylinesIteratively(double dpEpsilon, int maxiter) {
		List<SVGPolyline> polylineList = new ArrayList<SVGPolyline>();
		while (maxiter-- > 0) {
			LOG.debug("pixels: "+getPixelList().size());
			setPixelPaths(null);
			List<SVGPolyline> polylineList0 = createPolylinesAndRemoveUsedPixels(dpEpsilon);
			if (polylineList0.size() == 0) {
				// this seems to break the cycles OK on first example
				break;
			}
			polylineList.addAll(polylineList0);
			LOG.debug("pixels after: "+getPixelList().size()+" polylines "+polylineList0.size());
		}
		if (maxiter == 0) {
			throw new RuntimeException("couldn't analyze pixelIsland");
		}
		return polylineList;
	}
	
	
}
