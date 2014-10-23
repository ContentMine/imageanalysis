package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.image.ImageParameters;
import org.xmlcml.image.ImageUtil;

/**
 * connected list of pixels.
 * 
 * It is possible to traverse all pixels without encountering "gaps". May
 * contain "holes" (e.g letter "O").If there are objects within the hole (e.g.
 * "copyright" 0x00A9 which has "C" inside a circle) they may be initially be in
 * a separate island - we may coordinate this later
 * 
 * Islands can consist of:
 * <ul>
 * <li>A single pixel</li<
 * <li>A connected chain of pixels (with 2 terminal pixels</li<
 * <li>A tree of pixels with braching nodes (3-8 connected, but likely 3)</li<
 * <li>The above with nuclei (ganglia) in chains or nodes. The nuclei arise from
 * incomplete thinning and are to be reduced to single pixels or chains while
 * retaining connectivity</li<
 * </ul>
 * 
 * @author pm286
 * 
 */
public class PixelIsland implements Iterable<Pixel> {

	private final static Logger LOG = Logger.getLogger(PixelIsland.class);

	private static final int NEIGHBOUR8 = -1;

	PixelIslandList islandList;
	PixelList pixelList; // these may have original coordinates
	boolean allowDiagonal = false;
//	boolean allowDiagonal = true;
	private Int2Range int2range;
	private Int2 leftmostCoord;
	Map<Int2, Pixel> pixelByCoordMap; // find pixel or null
	private PixelList terminalPixels;

	private String pixelColor = "red";
	private PixelSet cornerSet;
//	private ImageParameters parameters;
	private PixelList emptyPixelList;
	private PixelList singleHoleList;
	private PixelNucleusFactory nucleusFactory;
	private PixelList orthogonalStubList;
	private PixelGraph pixelGraph;

	private SVGG svgg;
	private String id;
	
	public PixelIsland() {
		ensurePixelList();
	}

	@Deprecated // shallow copy
	public PixelIsland(PixelList pixelList) {
		this(pixelList, false);
	}
	
	public static PixelIsland createSeparateIslandWithClonedPixels(PixelList pixelList, boolean diagonal) {
		PixelIsland cloneIsland = new PixelIsland();
		for (Pixel pixel : pixelList) {
			Pixel clonePixel = new Pixel(pixel);
			clonePixel.setIsland(cloneIsland);
			cloneIsland.addPixelWithoutComputingNeighbours(clonePixel);
		}
		cloneIsland.setDiagonal(diagonal);
		return cloneIsland;
	}

	private void ensurePixelList() {
		if (pixelList == null) {
			this.pixelList = new PixelList();
		}
	}

	/**
	 * 
	 * @param pixelList
	 * @param diagonal
	 *            were diagonal neighbours allowed in creating the pixelList?
	 */
	@Deprecated //(shallow copy, not always what was wanted)
	public PixelIsland(PixelList pixelList, boolean diagonal) {
		this.pixelList = pixelList;
		this.allowDiagonal = diagonal;
		pixelList.setIsland(this);
		createMapAndRanges();
	}

	public PixelIsland(PixelIsland island) {
		this(island.getPixelList());
		this.allowDiagonal = island.allowDiagonal;
		this.islandList = island.islandList;
	}

	public Real2Range getBoundingBox() {
		ensurePixelList();
		Real2Range r2r = new Real2Range();
		for (Pixel pixel : pixelList) {
			r2r.add(new Real2(pixel.getInt2()));
		}
		return r2r;
	}

	public Int2Range getIntBoundingBox() {
		ensurePixelList();
		Int2Range i2r = new Int2Range();
		for (Pixel pixel : pixelList) {
			i2r.add(pixel.getInt2());
		}
		return i2r;
	}

	public void addPixelAndComputeNeighbourNeighbours(Pixel pixel) {
		ensurePixelList();
		this.pixelList.add(pixel);
		createMapAndRanges(pixel);
		// FIXME - why???
		pixel.createNeighbourNeighbourList(this);
	}

	public void addPixelWithoutComputingNeighbours(Pixel pixel) {
		ensurePixelList();
		this.pixelList.add(pixel);
	}

	@Deprecated // does not control neighbour list
	public void addPixel(Pixel pixel) {
		
		ensurePixelList();
		this.pixelList.add(pixel);
		createMapAndRanges(pixel);
		// FIXME - why???
		pixel.createNeighbourNeighbourList(this);
	}

	private void createMapAndRanges() {
		ensurePixelList();
		for (Pixel pixel : pixelList) {
			createMapAndRanges(pixel);
		}
	}
	
	void ensurePopulatedMapAndRanges() {
		ensurePixelByCoordMap();
		if (pixelByCoordMap.size() == 0) {
			createMapAndRanges(pixelList);
		}
	}

	private void createMapAndRanges(PixelList pixelList) {
		for (Pixel pixel : pixelList) {
			createMapAndRanges(pixel);
		}
	}
	private void createMapAndRanges(Pixel pixel) {
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
		ensurePixelList();
		return this.pixelList.size();
	}

	public Pixel getPixelByCoord(Int2 coord) {
		ensurePopulatedMapAndRanges();
		Pixel pixel = getPixelByCoordMap().get(coord);
		return pixel;
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

	public PixelList getPixelList() {
		ensurePixelList();
		return pixelList;
	}

	PixelList getTerminalPixels() {
		terminalPixels = getPixelsWithNeighbourCount(1);
		return terminalPixels;
	}

	public PixelList getPixelsWithNeighbourCount(int neighbourCount) {
		PixelList pixels = new PixelList();
		for (Pixel pixel : pixelList) {
			int nCount = getNeighbourCount(pixel);
			if (neighbourCount == nCount) {
				pixels.add(pixel);
			}
		}
		return pixels;
	}

	private int getNeighbourCount(Pixel pixel) {
		return pixel.getOrCreateNeighbours(this).size();
	}

	public void setDiagonal(boolean diagonal) {
		this.allowDiagonal = diagonal;
	}

	/**
	 * private List<Pixel> pixelList; boolean allowDiagonal = false; private
	 * Int2Range int2range; private Int2 leftmostCoord; Map<Int2, Pixel>
	 * pixelByCoordMap;
	 * 
	 * @param pixel
	 */
	public void remove(Pixel pixel) {
		if (pixelList.remove(pixel)) {
			// leaves int2range and laftmostCoord dirty
			int2range = null;
			leftmostCoord = null;
			Int2 coord = pixel.getInt2();
			pixelByCoordMap.remove(coord);
			pixel.removeFromNeighbourNeighbourList(this);
			pixel.clearNeighbours();
			
		}
	}

	/**
	 * remove steps and leave diagonal connections.
	 * 
	 * A step is: 1-2 ..3-4
	 * 
	 * where 2 and 3 have 3 connections (including diagonals and no other
	 * neighbours)
	 * 
	 * we want to remove either 2 or 3
	 * 
	 * @return pixels removed
	 */
	public PixelSet removeSteps() {
		PixelSet removed = new PixelSet();
		for (Pixel pixel : pixelList) {
			if (removed.contains(pixel)) {
				continue;
			}
			PixelList pixelNeighbours = pixel.getOrCreateNeighbours(this);
			if (pixelNeighbours.size() == 3) { // could be step or tJunction
				for (int i = 0; i < pixelNeighbours.size(); i++) {
					Pixel pi = pixelNeighbours.get(i);
					if (pi.isOrthogonalNeighbour(pixel)) {
						int j = (i + 1) % 3;
						Pixel pj = pixelNeighbours.get(j);
						int k = (i + 2) % 3;
						Pixel pk = pixelNeighbours.get(k);
						if (pj.isKnightsMove(pk, pi)) {
							removed.add(pixel);
							LOG.debug("removed: " + pixel);
							// this.remove(pixel);
						}
					}
				}
			}
		}
		for (Pixel pixel : removed) {
			this.remove(pixel);
		}
		return removed;
	}

	public SVGG createSVG() {
		SVGG g = new SVGG();
		for (Pixel pixel : pixelList) {
			g.appendChild(pixel.getSVGRect());
		}
		return g;
	}

	public void setPixelColor(String color) {
		this.pixelColor = color;
	}

	/**
	 * plost pixels as rectangles filled with pixelColor.
	 * 
	 * @return
	 */
	public SVGG plotPixels() {
		return PixelIsland.plotPixels(this.getPixelList(), this.pixelColor);
	}

	public static SVGG plotPixels(PixelList pixelList, String pixelColor) {
		SVGG g = new SVGG();
		LOG.trace("pixelList " + pixelList.size());
		for (Pixel pixel : pixelList) {
			SVGRect rect = pixel.getSVGRect();
			rect.setFill(pixelColor);
			LOG.trace(rect.getBoundingBox());
			g.appendChild(rect);
		}
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
		boolean include = ((width <= wmax && width >= wmin) && (height <= hmax && height >= hmin));
		return include;
	}

	/**
	 * computes correlations and outputs images.
	 * 
	 * @param island2
	 *            must be binarized
	 * @param title
	 *            if not null creates title.svg
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
		LOG.trace(xrange + " " + yrange);
		double score = 0.;
		File file = new File("target/correlate/");
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
				} else if (pixel1 == null && pixel2 == null) {
					score++;
				} else {
					score--;
				}
			}
		}
		if (title != null) {
			File filex = new File(file, title + ".svg");
			filex.getParentFile().mkdirs();
			SVGSVG.wrapAndWriteAsSVG(g, filex);
		}
		return score / (xrange * yrange);
	}

	private SVGRect addRect(Int2 i2, String color) {
		double x = i2.getX();
		double y = i2.getY();
		SVGRect rect = new SVGRect(new Real2(x, y), new Real2(x + 1, y + 1));
		rect.setStroke("none");
		rect.setFill(color);
		return rect;
	}

	/**
	 * clips rectangular image from rawImage corresponding to this.
	 * 
	 * WARNING. still adjusting inclusive/exclusive clip
	 * 
	 * @param rawImage
	 *            to clip from
	 * @return image in raw image with same bounding box as this
	 */
	public BufferedImage clipSubimage(BufferedImage rawImage) {
		Int2Range i2r = getIntBoundingBox();
		// may have clipped 1 pixel too much...
		IntRange ix = i2r.getXRange();
		IntRange iy = i2r.getYRange();
		i2r = new Int2Range(new IntRange(ix.getMin(), ix.getMax() + 1),
				new IntRange(iy.getMin(), iy.getMax() + 1));
		BufferedImage subImage = ImageUtil.clipSubImage(rawImage, i2r);
		return subImage;
	}

	/** creates RGB image.
	 * 
	 * @return
	 */
	public BufferedImage createImage() {
		return createImage(BufferedImage.TYPE_INT_RGB);
	}

	public BufferedImage createImage(int imageType) {
		Int2Range bbox = this.getIntBoundingBox();
		int xmin = bbox.getXRange().getMin();
		int ymin = bbox.getYRange().getMin();
		int w = bbox.getXRange().getRange() + 1; // this was a bug
		int h = bbox.getYRange().getRange() + 1; // and this
		BufferedImage image = null;
		if (w == 0 || h == 0) {
			LOG.trace("zero pixel image");
			return image;
		}
		image = new BufferedImage(w, h, imageType);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				image.setRGB(i, j, 0xffffff);
			}
		}
		int wrote = 0;
		for (Pixel pixel : this.getPixelList()) {
			Int2 xy = pixel.getInt2();
			int x = xy.getX() - xmin;
			int y = xy.getY() - ymin;
			// System.out.println(xy+" "+bbox+" "+w+" "+h+" "+x+" "+y);
			if (x < w && y < h) {
				image.setRGB(x, y, 0);
				wrote++;
			} else {
				LOG.error("Tried to write pixel outside image area "+xy);
			}
		}
		LOG.trace("created image, size: " + pixelList.size()+" "+wrote);
		return image;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("pixels " + ((pixelList == null) ? null : pixelList.size()));
		sb.append("; int2range " + int2range);
		return sb.toString();
	}

	public void removePixels(PixelList pixelList) {
		for (Pixel pixel : pixelList) {
			this.remove(pixel);
		}
	}

	public String getPixelColor() {
		return pixelColor;
	}

	public List<PixelIsland> findPixelLakes() {
		throw new RuntimeException("NYI");
	}
	
	public void findRidge() {
		markEdges();
	}

	/** mark all pixels which have an exposure to the outside.
	 * 
	 * set value to ffffff (white) by default and 1 if < 8 neighbours
	 * 
	 */
	public void markEdges() {
		for (Pixel pixel : pixelList) {
			pixel.setValue(NEIGHBOUR8);
			PixelList neighbours = pixel.getOrCreateNeighbours(this);
			int size = neighbours.size();
			if (size < 8) {
				pixel.setValue(1);
			}
		}
	}

	/** get list of all pixels with value v.
	 * 
	 * @param v
	 * @return
	 */
	public PixelList getPixelsWithValue(int v) {
		PixelList valueList = new PixelList();
		for (Pixel pixel : pixelList) {
			if (pixel.getValue() == v) {
				valueList.add(pixel);
			}
		}
		return valueList;
	}

	/** for each pixel of value v in list (ring) increment neighbours.
	 * 
	 * @param startPixels
	 * @param v
	 * @return
	 */
	public PixelList growFrom(PixelList startPixels, int v) {
		PixelList growList = new PixelList();
		for (Pixel start : startPixels) {
			if (start.getValue() != v) {
				throw new RuntimeException("bad pixel " + start.getValue());
			}
			PixelList neighbours = start.getOrCreateNeighbours(this);
			for (Pixel neighbour : neighbours) {
				if (neighbour.getValue() == NEIGHBOUR8) {
					neighbour.setValue(v + 1);
					growList.add(neighbour);
				}
			}
		}
		return growList;
	}
	/** find rings round rideg.
	 * 
	 * @return
	 */
	public PixelRingList createOnionRings() {
		PixelRingList onionRings = new PixelRingList();
		onionRings.setIsland(this);
		setDiagonal(true);
		findRidge();
		PixelList list = getPixelsWithValue(1);
		int ring = 1;
		while (list.size() > 0) {
			onionRings.add(list);
			list = growFrom(list, ring++);
		}
		return onionRings;
	}

	public Pixel get(int i) {
		return pixelList == null || i < 0 || i >= pixelList.size() ? null
				: pixelList.get(i);
	}

	/** removes the pixels from an incompletely thinned island.
	 * 
	 */
	public void removeStepsIteratively() {
		while (true) {
			PixelSet removed = removeSteps();
			if (removed.size() == 0) {
				break;
			}
		}
	}

	public Iterator<Pixel> iterator() {
		return pixelList.iterator();
	}

	/**
	 * plots pixels onto SVGG with current (or default) colour.
	 * 
	 * @return
	 */
	@Deprecated
	public SVGG getSVGG() {
		return plotPixels(pixelList, pixelColor);
	}

	/**
	 * plots pixels onto SVGG with current (or default) colour.
	 * 
	 * @return
	 */
	public SVGG getOrCreateSVGG() {
		if (svgg == null) {
			svgg = plotPixels(pixelList, pixelColor);
		}
		return svgg;
	}

	public void removeCorners() {
		while (true) {
			makeCornerSet();
			if (cornerSet.size() == 0)
				break;
			removeCornerSet();
		}
	}

	/**
	 * this may be better or complementary to triangles;
	 * 
	 * finds all corners of form
	 * 
	 * ++ +
	 * 
	 */
	private PixelSet makeCornerSet() {
		cornerSet = new PixelSet();
		for (Pixel pixel : this) {
			PixelList orthogonalNeighbours = pixel
					.getOrthogonalNeighbours(this);
			// two orthogonal at right angles?
			if (orthogonalNeighbours.size() == 2) {
				Pixel orthNeigh0 = orthogonalNeighbours.get(0);
				Pixel orthNeigh1 = orthogonalNeighbours.get(1);
				// corner?
				if (orthNeigh0.isDiagonalNeighbour(orthNeigh1)) {
					PixelList diagonalNeighbours = pixel
							.getDiagonalNeighbours(this);
					// is this a diagonal Y-junction?
					boolean add = true;
					for (Pixel diagonalNeighbour : diagonalNeighbours) {
						if (diagonalNeighbour.isKnightsMove(orthNeigh0)
								&& diagonalNeighbour.isKnightsMove(orthNeigh1)) {
							LOG.trace("skipped diagonal Y Junction: "
									+ diagonalNeighbour + "/" + pixel + "/"
									+ orthNeigh0 + "//" + orthNeigh1);
							add = false;
							break; // Y-junction
						}
					}
					if (add) {
						cornerSet.add(pixel);
					}
				}
			}
		}
		return cornerSet;
	}

	/**
	 * removes all corners not next to each other.
	 * 
	 * in some cases may not take the same route and so may give different
	 * answers but the result should always have no corners.
	 */
	public void removeCornerSet() {
		ensureCornerSet();
		while (!cornerSet.isEmpty()) {
			Pixel pixel = cornerSet.iterator().next();
			PixelList neighbours = pixel.getOrCreateNeighbours(this);
			// remove neighbours from set - if they are corners, if not no-op
			for (Pixel neighbour : neighbours) {
				cornerSet.remove(neighbour);
			}
			cornerSet.remove(pixel);
			this.remove(pixel);
		}
	}

	private void ensureCornerSet() {
		if (cornerSet == null) {
			makeCornerSet();
		}
	}

	public PixelGraph getOrCreateGraph() {
		PixelGraph graph = new PixelGraph(this);
		return graph;
	}

	/** sets all pixels in island to black in image
	 * 
	 * subtracts offset
	 * 
	 * @param image
	 * @param xy0 offest to subtract
	 * @param y0
	 */
	void setToBlack(BufferedImage image, Int2 xy0) {
		for (Pixel pixel : getPixelList()) {
			pixel.setToBlack(image, xy0);
		}
	}

	/** get all pixels which could be nodes.
	 * 
	 * At preset this is all except those with 2 connections
	 * 
	 * @return
	 */
	public PixelList getNucleusCentrePixelList() {
		PixelList pixels = new PixelList();
		for (Pixel pixel : pixelList) {
			int neighbourCount = getNeighbourCount(pixel);
			if (neighbourCount != 2) {
				pixels.add(pixel);
			}
		}
		return pixels;
	}

	/** remove orthogonal stub
	 *
	 * single pixel on a surface
	 * 
	 * @return list of pixels removed
	 */
	public PixelList trimOrthogonalStubs() {
		getOrCreateOrthogonalStubList();
		for (Pixel stub : orthogonalStubList) {
			remove(stub);
		}
		return orthogonalStubList;
	}
	
	/** fill empty single pixel.
	 *
	 * "hole" must have 4 orthogonal neighbours
	 */
	public PixelList fillSingleHoles() {
		if (singleHoleList == null) {
			singleHoleList = new PixelList();
			PixelList emptyList = getEmptyPixels();
			for (Pixel pixel : emptyList) {
				PixelList neighbours = this.getFilledOrthogonalNeighbourPixels(pixel);
				if (neighbours.size() == 4) {
					Pixel newPixel = new Pixel(pixel.getInt2().getX(), pixel.getInt2().getY());
					singleHoleList.add(newPixel);
					this.addPixelAndComputeNeighbourNeighbours(newPixel);
				}
			}
		}
		return singleHoleList;
	}

	private PixelList getFilledOrthogonalNeighbourPixels(Pixel pixel) {
		Int2 coord = pixel == null ? null : pixel.getInt2();
		PixelList filledList = null;
		if (coord != null) {
			filledList = new PixelList();
			ensurePixelByCoordMap();
			addPixel(filledList, pixelByCoordMap.get(coord.subtract(new Int2(1,0))));
			addPixel(filledList, pixelByCoordMap.get(coord.subtract(new Int2(0,1))));
			addPixel(filledList, pixelByCoordMap.get(coord.plus(new Int2(1,0))));
			addPixel(filledList, pixelByCoordMap.get(coord.plus(new Int2(0,1))));

		}
		if (filledList.size() > 0) {
			LOG.trace("filled "+filledList.size());
		}
		return filledList;
	}

	/** add pixel if not null
	 * 
	 * @param list
	 * @param pixel
	 */
	private void addPixel(PixelList list, Pixel pixel) {
		if (pixel != null) {
			list.add(pixel);
		}
		
	}

	public PixelList getEmptyPixels() {
		if (emptyPixelList == null) {
			emptyPixelList = new PixelList();
			Int2Range box = this.getIntBoundingBox();
			IntRange xRange = box.getXRange();
			int xmin = xRange.getMin();
			int xmax = xRange.getMax();
			IntRange yRange = box.getYRange();
			int ymin = yRange.getMin();
			int ymax = yRange.getMax();
			Map<Int2, Pixel> pixelByCoordMap = getPixelByCoordMap();
			for (int i = xmin; i <= xmax; i++) {
				for (int j = ymin; j <= ymax; j++) {
					Pixel pixel = pixelByCoordMap.get(new Int2(i, j));
					if (pixel == null) {
						emptyPixelList.add(new Pixel(i, j));
					}
				}
			}
			LOG.trace("empty "+emptyPixelList.size());
		}
		return emptyPixelList;
	}

	public void doSuperThinning() {
		removeSteps();
		doTJunctionThinning();
	}

	public boolean getDiagonal() {
		return allowDiagonal;
	}

	public void recomputeNeighbours() {
		for (Pixel pixel : this) {
			pixel.recomputeNeighbours(this);
		}
	}

	/** does island contain a pixel?
	 * 
	 * crude
	 * 
	 * @param pixel
	 * @return
	 */
	public boolean contains(Pixel pixel) {
		ensurePixelList();
		for (Pixel pixel0 : pixelList) {
			if (pixel.equals(pixel0)) {
				return true;
			}
		}
		return false;
	}

	public void trimCornerPixels() {
		boolean a = allowDiagonal;
		PixelList connected5List = getPixelsWithNeighbourCount(5);
		if (connected5List .size() > 0) {
			for (Pixel pixel : connected5List) {
				if (pixel.form5Corner(this)) {
					this.remove(pixel);
				}
			}
		}
	}
	
	/** adds translation to all pixels
	 * 
	 * @param translation
	 */
	public PixelIsland createTranslate(Int2 translation) {
		throw new RuntimeException("NYI");
	}

	public SVGG createSVGFromEdges() {
		SVGG g = new SVGG();
		SVGText text = new SVGText(new Real2(20., 20.), "SVG Edges NYI");
		return g;
	}

	/** superthin the nucleus by removing 3-coordinated and higher pixels.
	 * 
	 * @return the removed pixels
	 */
	public PixelNucleusList doTJunctionThinning() {
		getOrCreateNucleusFactory();
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		nucleusList.doTJunctionThinning(this);
		return nucleusList;
	}

	public PixelList getOrCreateOrthogonalStubList() {
		if (orthogonalStubList == null) {
			orthogonalStubList = new PixelList();
			for (Pixel pixel : this) {
				if (isOrthogonalStub(pixel)) {
					orthogonalStubList.add(pixel);
				}
			}
		}
		return orthogonalStubList;
	}

	public PixelNucleusFactory getOrCreateNucleusFactory() {
		if (nucleusFactory == null) {
			nucleusFactory = new PixelNucleusFactory(this);
			nucleusFactory.setIsland(this);
		}
		
		return nucleusFactory;
	}


	public PixelNodeList createNodeList() {
		return getOrCreateNucleusFactory().getOrCreateNodeListFromNuclei();
	}

	public PixelEdgeList createEdgeList() {
		return getOrCreateNucleusFactory().createPixelEdgeListFromNodeList();
	}

//	public void rearrangeYJunctions() {
//		getOrCreateNucleusFactory();
//		PixelNucleusList yJunctionList = nucleusFactory.getOrCreateYJunctionList();
//		for (PixelNucleus yJunction : yJunctionList) {
//			LOG.trace("rearrange Y "+yJunction);
//			if (yJunction.rearrangeYJunction(this)) {
//				LOG.trace("rearranged Y junction: "+yJunction);
//			}
//		}
//	}

	private boolean isOrthogonalStub(Pixel pixel) {
		PixelList neighbours = pixel.getOrCreateNeighbours(this);
		if (neighbours.size() == 3) {
			LOG.trace("3 neighbours "+pixel+"; neighbours "+neighbours);
			if (neighbours.hasSameCoords(0) || neighbours.hasSameCoords(1)) {
				LOG.trace("orthogonal stub "+pixel+"; "+neighbours);
				return true;
			}
		}
		return false;
	}

	private void drawPixels(int serial, String[] color, SVGG gg, int col1, PixelSet set) {
		PixelList pixelList;
		Set<String> ss;
		pixelList = new PixelList();
		pixelList.addAll(set);
		if (pixelList.size() > 1) {
			SVGG g = pixelList.draw(null, color[(serial + col1) % color.length]);
			gg.appendChild(g);
		}
	}

	public void setNucleusFactory(PixelNucleusFactory factory) {
		this.nucleusFactory = factory;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setIslandList(PixelIslandList islandList) {
		this.islandList = islandList;
	}

	public ImageParameters getParameters() {
		getIslandList();
		return islandList.getParameters();
	}

	PixelIslandList getIslandList() {
		if (islandList == null) {
			throw new RuntimeException("Island must have IslandList");
		}
		return islandList;
	}

	/** removes minorIslands 
	 * does not reset maps yet...
	 * 
	 * @param size
	 */
	public void removeMinorIslands(int size) {
		pixelList.setIsland(this);
		pixelList.removeMinorIslands(size);
	}


//	/** create rings of pixels starting at the outside.
//	 * 
//	 * list.get(0) is outermost ring.
//	 * list.get(1) touches it...
//	 * @return
//	 */
//	public List<PixelList> createNestedRings() {
//		setDiagonal(true);
//		findRidge();
//		int value = 1;
//		List<PixelList> pixelListList = new ArrayList<PixelList>();
//		PixelList list = getPixelsWithValue(value);
//		while (list.size() > 0) {
//			pixelListList.add(list);
//			list = growFrom(list, value);
//			value++;
//		}
//		return pixelListList;
//	}

}
