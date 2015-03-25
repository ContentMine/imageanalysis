package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.pixel.PixelComparator.ComparatorType;

/**
 * container for a lit of pixels. 
 * can have additional attributes such as colour or value.
 * 
 * @author pm286
 *
 */
public class PixelList implements Iterable<Pixel> {

	final static Logger LOG = Logger.getLogger(PixelList.class);

	// these may not be needed
	private static final String START_STRING = ":";
	private static final String END_STRING = ":";
	public final static Pattern COORD_PATTERN = Pattern.compile("\\((\\d+),(\\d+)\\)");
	private static final String DEFAULT_FILL = "red";

	private List<Pixel> list;
	private Real2Array points;
	private PixelIsland island;
	private Map<Int2, Pixel> pixelByCoordinateMap;

	Int2Range bbox;
	
	public PixelList() {
		ensureList();
		ensurePixelByCoordinateMap();
	}

	private void ensureList() {
		if (list == null) {
			list = new ArrayList<Pixel>();
		}
	}
	
	public PixelList(Collection<Pixel> pixelCollection) {
		ensureList();
		Iterator<Pixel> iterator = pixelCollection.iterator();
		while (iterator.hasNext()) {
			this.add(iterator.next());
		}
	}

	public PixelList(PixelList list) {
		this(list.getList());
	}

	public Pixel getPixelByCoordinate(Int2 coord) {
		ensurePixelByCoordinateMap();
		return pixelByCoordinateMap.get(coord);
	}

	private void ensurePixelByCoordinateMap() {
		if (pixelByCoordinateMap == null) {
			pixelByCoordinateMap = new HashMap<Int2, Pixel>();
			for (Pixel pixel : this) {
				if (pixel == null) {
					throw new RuntimeException("null pixel");
				}
				pixelByCoordinateMap.put(pixel.getInt2(), pixel);
			}
		}
	}

	public int[][] createBinary() {
		int[][] binary = null;
		Int2Range bbox = this.getIntBoundingBox();
		int xmin = bbox.getXRange().getMin();
		int ymin = bbox.getYRange().getMin();
		int w = bbox.getXRange().getRange() + 1; // this was a bug
		int h = bbox.getYRange().getRange() + 1; // and this
		
		binary = new int[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				binary[i][j] = 0;
			}
		}
		for (Pixel pixel : this) {
			Int2 xy = pixel.getInt2();
			int x = xy.getX() - xmin;
			int y = xy.getY() - ymin;
			if (x < w && y < h) {
				binary[x][y] = 1;
			} else {
				LOG.error("Tried to write pixel outside image area "+xy);
			}
		}
		return binary;
	}
	
	@Override
	public Iterator<Pixel> iterator() {
		return list.iterator();
	}
	
	public Pixel get(int i) {
		return (list == null || i < 0 || i >= list.size()) ? null : list.get(i);
	}
	
	public Pixel last() {
		return (list == null || list.size() == 0 ) ? null : list.get(list.size() - 1);
	}
	
	public void addFromSameIsland(Pixel pixel) {
		add(pixel, true);
	}
	
	public void add(Pixel pixel) {
		add(pixel, false);
	}
	
	private void add(Pixel pixel, boolean check) {
		ensureList();
		if (check) {
			checkFromSameIsland(pixel);
		}
		list.add(pixel);
		addToMap(pixel);
	}

	private void addToMap(Pixel pixel) {
		ensurePixelByCoordinateMap();
		this.pixelByCoordinateMap.put(pixel.getInt2(), pixel);
	}

	private void checkFromSameIsland(Pixel pixel) {
		PixelIsland island = pixel.getIsland();
		if (this.island == null) {
			this.island = island;
		} else if (island == null || !this.island.equals(island)) {
			throw new RuntimeException("change of island not allowed: "+this.island+"=>"+island);
		}
	}
	
	public List<Pixel> getList() {
		return list;
	}

	public int size() {
		return list == null ? 0 : list.size();
	}

	public void addAllFromSameIsland(PixelList pixelList) {
		ensureList();
		for (Pixel pixel : pixelList) {
			addFromSameIsland(pixel);
		}
	}

	public void addAll(PixelList pixelList) {
		ensureList();
		for (Pixel pixel : pixelList) {
			add(pixel);
		}
	}

	public boolean contains(Pixel pixel) {
		return list != null && pixelByCoordinateMap.get(pixel.getInt2()) != null;
	}

	public boolean remove(Pixel pixel) {
		if (list != null) {
			ensurePixelByCoordinateMap();
			pixelByCoordinateMap.remove(pixel.getInt2());
			return list.remove(pixel);
		}
		return false;
	}

	public PixelList getPixelsTouching(PixelList list1) {
		PixelSet used = new PixelSet();
		PixelList touchingList = new PixelList();
		if (this.island != null && list1 != null && this.island.equals(list1.getIsland())) {
			if (list1.size() > 0) {
				int value = list1.get(0).getValue();
				for (Pixel pixel : list) {
					PixelList neighbours = pixel.getOrCreateNeighbours(island);
					for (Pixel neighbour : neighbours) {
						if (neighbour.getValue() == value && !used.contains(neighbour)) {
							used.add(neighbour);
							touchingList.add(neighbour);
						}
					}
				}
			}
		}
		return touchingList;
	}
	
	/** plots pixels as squares
	 * 
	 * @param g if null creates one
	 * @param fill colour
	 * @return
	 */
	public SVGG plotPixels(SVGG g, String fill) {
		if (g == null) {
			g = new SVGG();
		}
		for (Pixel pixel : this) {
			SVGRect rect = pixel.getSVGRect();
			rect.setFill(fill);
			g.appendChild(rect);
		}
		return g;
	}

	/** plots pixels as squares
	 * 
	 * @param g if null creates one
	 * @param fill colour
	 * @return
	 */
	public SVGG getOrCreateSVG() {
		SVGG g = new SVGG();
		for (Pixel pixel : this) {
			SVGRect rect = pixel.getSVGRect();
			rect.setFill(DEFAULT_FILL);
			g.appendChild(rect);
		}
		return g;
	}

	/** as plotPixels but creates SVGG
	 * 
	 * @param fill
	 * @return
	 */
	public SVGG plotPixels(String fill) {
		return plotPixels(null, fill);
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (list.size() == 0) sb.append(START_STRING);
		for (Pixel pixel : this) {
			sb.append(pixel.toString());
		}
		if (list.size() == 0) sb.append(END_STRING);
		return sb.toString();
	}

	public void reverse() {
		Collections.reverse(list);
	}

	/** create PixelList from all pixels with given value.
	 * 
	 * @param image1
	 * @param colorValue
	 * @return
	 */
	public static PixelList createPixelList(BufferedImage image, int colorValue) {
		PixelList list = new PixelList();
		int width = image.getWidth();
		int height = image.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j) & 0x00ffffff;
				if (rgb == colorValue) {
					list.add(new Pixel(i, j));
				}
			}
		}
		return list;
	}
	
	/** draw pixelList.
	 * 
	 * @param file if not null write to file
	 * @param fill colour
	 * @return SVGG container
	 */
	public SVGG draw(File file, String fill) {
		SVGG g = new SVGG();
		plotPixels(g, fill);
		if (file != null) {
			SVGSVG.wrapAndWriteAsSVG(g, file);
		}
		return g;
		
	}

	public boolean isCycle() {
		boolean isCycle = false;
		int size = size();
		if (size > 0) {
			isCycle = (get(0).equals(get(size - 1)));
		}
		return isCycle;
	}

	public Real2Array getReal2Array() {
		points = new Real2Array();
		for (Pixel pixel : list) {
			Real2 point = new Real2(pixel.getInt2());
			points.add(point);
		}
		return points;
	}

	/** finds all pixels in list Before pixel.
	 * 
	 * produces list in reverse order including both ends
	 * 
	 * @param pixel
	 * @return null if no list or pixel not in list
	 */
	public PixelList getPixelsBefore(Pixel pixel) {
		int mid = this.indexOf(pixel);
		PixelList pixelList = getPixelList(mid, -1, -1);
		return pixelList;
	}

	/** finds all pixels in list After pixel.
	 * 
	 * produces list in same order including both ends
	 * 
	 * @param pixel
	 * @return null if no list or pixel not in list
	 */
	public PixelList getPixelsAfter(Pixel pixel) {
		int mid = this.indexOf(pixel);
		PixelList pixelList = getPixelList(mid, size(), 1);
		return pixelList;
	}

	private PixelList getPixelList(int start, int end, int delta) {
		PixelList pixelList = new PixelList();
		for (int i = start; i != end; i += delta) {
			Pixel pixel = this.get(i);
			pixelList.add(pixel);
		}
		return pixelList;
	}

	/** gets index of pixel in list;
	 * 
	 * @param pixel
	 * @return -1 if not found 
	 */
	public int indexOf(Pixel pixel) {
		return list == null ? -1 : list.indexOf(pixel);
	}

	public Pixel getOther(Pixel pixel) {
		Pixel other = null;
		if (this.size() == 2) {
			if (this.get(0).equals(pixel)) {
				other = this.get(1);
			} else if (this.get(1).equals(pixel)) {
				other = this.get(0);
			}
		}
		return other;
	}

	/** do all pixels have the same X-or-Y coordinate?
	 * 
	 * @param xy if 0 use X else Y
	 * @return true if list is empty else reports identity of coordinates
	 */
	public boolean hasSameCoords(int xy) {
		if (size() > 1) {
			int coord0 = getCoordinate(list.get(0), xy);
			for (Pixel pixel : this) {
				int coord = getCoordinate(pixel, xy);
				if (coord != coord0) return false;
			}
		}
		return true;
	}

	private int getCoordinate(Pixel pixel, int xy) {
		Int2 int2 = pixel.getInt2();
		return (xy == 0) ? int2.getX() : int2.getY();
	}

	/** create bounding box of list.
	 * 
	 * @return null if empty list;
	 */
	public Int2Range getIntBoundingBox() {
		Int2Range box = null;
		if (list != null && list.size() > 0) {
			box = new Int2Range();
			for (Pixel pixel : this) {
				box.add(pixel.getInt2());
			}
		}
		return box;
	}

	public void addAll(PixelSet set) {
		ensureList();
		this.list.addAll(set);
	}

	public PixelIsland getPixelIsland() {
		return island;
	}

	public void add(int i, Pixel pixel) {
		ensureList();
		this.list.add(i, pixel);
	}

	public Pixel penultimate() {
		ensureList();
		return list.size() <= 1 ? null : list.get(list.size() - 2); 
	}

	/** creates from list of coords.
	 * 
	 * @param pixelListS e.g. (1,2)(3,4)(4,5)
	 * @param island
	 * @return
	 */
	public static PixelList createPixelList(String pixelListS, PixelIsland island) {
		PixelList pixelList = null;
		if (pixelListS != null) {
			pixelList = new PixelList();
			Matcher matcher = COORD_PATTERN.matcher(pixelListS);
			int start = 0;
			while (matcher.find(start)) {
				start = matcher.end();
				String xs = matcher.group(1);
				String ys = matcher.group(2);
				Pixel pixel = new Pixel(new Integer(xs), new Integer(ys));
				pixel.island = island;
				pixelList.add(pixel);
			}
		}
		return pixelList;
	}

	/** gets pixel closest to centroid.
	 * 
	 * for wiggly line might not be the halfway point
	 * 
	 * @return
	 */
	public Pixel getCentralPixel() {
		Pixel centrePixel = null;
		Real2 centre = getCentreCoordinate();
		if (centre != null) {
			centrePixel = getClosestPixel(centre);
		}
		return centrePixel;
	}

	/** get pixel closest to point.
	 * 
	 * uses real Euclidean distance
	 * 
	 * if two pixels have same distance , returns the first in the list
	 * 
	 * @param point
	 * @return closest pixel or null
	 */
	public Pixel getClosestPixel(Real2 point) {
		Pixel centrePixel = null;
		Double distance = null;
		for (Pixel pixel : this) {
			double d = point.getDistance(new Real2(pixel.getInt2()));
			if (distance == null || distance > d) {
				distance = d;
				centrePixel = pixel;
			}
		}
		return centrePixel;
	}

	private Real2 getCentreCoordinate() {
		return size() == 0 ? null : getReal2Array().getMean();
	}


	/**
	 * sorts Y first, then X.
	 * 
	 */
	public void sortYX() {
		Collections.sort(list, new PixelComparator(ComparatorType.TOP, ComparatorType.LEFT));
	}

	/** find all neighbours not in current set.
	 * 
	 * Effectively the next outwards and inwards shell. Most commonly applied
	 * to Nuclei when there is no inside.
	 * 
	 * uses PixelShell.
	 * 
	 * @return
	 */
	public PixelList getOrCreateNeighbours() {
		PixelShell pixelShell = new PixelShell(this);
		return new PixelList(pixelShell.getExpandedSetWithoutSeed());
	}

	public PixelIsland getIsland() {
		this.checkFromSameIsland(this.get(0));
		return island;
	}
	
	/** Finds pixels with orthogonal contacts to another PixelList
	 * 
	 * Typical usage is pixels in a PixelRing touching an inner or outer
	 * Hopefully the orthogonal nature creates a thinned result.
	 * 
	 * @param firstRingPixels
	 * @param island
	 * @return
	 */

	public PixelList getPixelsWithOrthogonalContactsTo(PixelList firstRingPixels, PixelIsland island) {
		PixelSet firstRingSet = new PixelSet(firstRingPixels);
		PixelList touchingPixelList = new PixelList();
		for (Pixel pixel : this) {
			PixelList orthogonalNeighbours = pixel.getOrthogonalNeighbours(island);
			for (Pixel neighbour : orthogonalNeighbours) {
				if (firstRingSet.contains(neighbour)) {
					touchingPixelList.add(pixel);
					break;
				}
			}
		}
		return touchingPixelList;
	}

	/** removes islands in list as small or smaller than limit
	 * only renoves from PixelList (does not delete the actual Pixel
	 * 
	 * @param size maximum size of islands to removeß
	 */
	public void removeMinorIslands(int size) {
		if (island == null) {
			throw new RuntimeException("PixelList island must be non-null");
		}
		if (size > 3) {
			throw new RuntimeException("Cannot treat islands > 3 pixels at present");
		}
		PixelList deleteList = new PixelList();
		if (size >= 1) {
			addAllSinglePixelsToDeleteList(deleteList);
		}
		PixelList terminalList = island.getPixelsWithNeighbourCount(1);
		
		if (size >= 2) {
			addAllDominoesToDeleteList(terminalList, deleteList);
		}
		if (size >= 3) {
			addAllTriominoesToDeleteList(terminalList, deleteList);
		}
		if (deleteList.size() > 0) {
			LOG.trace("deleted "+deleteList.size());
		}
		removeAll(deleteList.getList());
	}

	private void addAllSinglePixelsToDeleteList(PixelList deleteList) {
		PixelList list = island.getPixelsWithNeighbourCount(0);
		deleteList.addAll(list);
	}

	private void addAllDominoesToDeleteList(PixelList terminalList, PixelList deleteList) {
		for (Pixel pixel : terminalList) {
			Pixel neighbour = pixel.getOrCreateNeighbours(island).get(0);
			if (terminalList.contains(neighbour) && terminalList.contains(pixel)) {
				if (!deleteList.contains(pixel) && !deleteList.contains(neighbour)) {
					deleteList.add(pixel);
					deleteList.add(neighbour);
				}
			}
		}
	}

	private void addAllTriominoesToDeleteList(PixelList terminalList, PixelList deleteList) {
		// linear
		PixelList twoConnectedList = island.getPixelsWithNeighbourCount(2);
		for (Pixel pixel : twoConnectedList) {
			PixelList neighbours = pixel.getOrCreateNeighbours(island);
			Pixel neighbour0 = neighbours.get(0);
			Pixel neighbour1 = neighbours.get(1);
			if (terminalList.contains(neighbour0) && terminalList.contains(neighbour1)) {
				if (!deleteList.contains(pixel)) {
					deleteList.add(pixel);
					deleteList.add(neighbour0);
					deleteList.add(neighbour1);
				}
			}
		}
		// triangle NYI
	}

	public void setIsland(PixelIsland island) {
		this.island = island;
	}

	public PixelList findExtremePixels() {
		PixelList extremeList = new PixelList();

		Pixel eastPixel = null;
		Pixel westPixel = null;
		this.sortYX();
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		for (Pixel pixel : list) {
			int x = pixel.getInt2().getX();
			// Eastern
			if (x > xmax) {
				eastPixel = pixel;
				xmax = x;
			}
			// Western
			if (x < xmin) {
				westPixel = pixel;
				xmin = x;
			}
		}
		extremeList.add(list.get(0));
		extremeList.add(eastPixel);
		extremeList.add(list.get(list.size() - 1)); // last
		extremeList.add(westPixel);
		return extremeList;
	}

	private void removeAll(List<Pixel> smallList) {
		ensurePixelByCoordinateMap();
		list.removeAll(smallList);
		for (Pixel pixel : smallList) {
			pixelByCoordinateMap.remove(pixel.getInt2());
		}
	}

//	public void removeAll(PixelList pixelList) {
//		if (pixelList != null) {
//			list.removeAll(pixelList.getList());
//		}
//	}


}
