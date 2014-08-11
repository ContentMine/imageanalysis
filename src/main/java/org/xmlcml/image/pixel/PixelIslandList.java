package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.image.ImageParameters;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.pixel.PixelIslandComparator.ComparatorType;
import org.xmlcml.image.processing.Thinning;
import org.xmlcml.image.processing.ZhangSuenThinning;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * container for collection of PixelIslands.
 * 
 * @author pm286
 * 
 */
public class PixelIslandList implements Iterable<PixelIsland> {

	private static final int MAXPIXEL = 6000;
	private final static Logger LOG = Logger.getLogger(PixelIslandList.class);

	public enum Operation {
		BINARIZE, DEHYPOTENUSE, THIN
	}

	private List<PixelIsland> list;
	private String pixelColor;
	private SVGG svgg;
	private boolean debug = false;
	private PixelProcessor pixelProcessor;
	private ImageParameters parameters;
	
	public PixelIslandList() {
		list = new ArrayList<PixelIsland>();
		init();
	}

	public PixelIslandList(List<PixelIsland> newList) {
		this.list = newList;
		init();
	}
	
	private void init() {
	}

	public PixelIslandList(Collection<PixelIsland> collection) {
		this(new ArrayList<PixelIsland>(collection));
	}
	
	public void setPixelProcessor(PixelProcessor pixelProcessor) {
		this.pixelProcessor = pixelProcessor;
	}

	public int size() {
		return list.size();
	}

	@Override
	public Iterator<PixelIsland> iterator() {
		return list.iterator();
	}

	public PixelIsland get(int i) {
		return list.get(i);
	}

	public void add(PixelIsland pixelIsland) {
		list.add(pixelIsland);
	}

	public List<PixelIsland> getList() {
		return list;
	}

	/**
	 * find all separated islands.
	 * 
	 * creates a FloodFill and extracts Islands from it. diagonal set to true
	 * 
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public static PixelIslandList createSuperThinnedPixelIslandList(BufferedImage image) {
		PixelProcessor pixelProcessor = new PixelProcessor(image);
		PixelIslandList islandList = pixelProcessor.getOrCreatePixelIslandList();
		islandList.removeStepsSortAndReverse();
		return islandList;
	}

	public PixelIslandList smallerThan(Real2 box) {
		List<PixelIsland> newList = new ArrayList<PixelIsland>();
		for (PixelIsland island : list) {
			Real2Range bbox = island.getBoundingBox();
			if (bbox.getXRange().getRange() < box.getX()
					&& bbox.getYRange().getRange() < box.getY()) {
				newList.add(island);
			} else {
				LOG.trace("omitted " + bbox);
			}
		}
		return new PixelIslandList(newList);
	}

	/**
	 * create list of islands falling within dimension ranges.
	 * 
	 * if island dimensions (width, height) fit with x/ySizeRange add to list
	 * 
	 * @param xSizeRange
	 *            range of xSizes inclusive
	 * @param ySizeRange
	 *            range of ySizes inclusive
	 * @return
	 */
	public PixelIslandList isContainedIn(RealRange xSizeRange,
			RealRange ySizeRange) {
		List<PixelIsland> newList = new ArrayList<PixelIsland>();
		for (PixelIsland island : list) {
			if (island.fitsWithin(xSizeRange, ySizeRange)) {
				newList.add(island);
			}
		}
		return new PixelIslandList(newList);
	}

	public Multimap<Integer, PixelIsland> createCharactersByHeight() {
		Multimap<Integer, PixelIsland> map = ArrayListMultimap.create();
		for (PixelIsland island : list) {
			Integer height = (int) island.getBoundingBox().getYRange()
					.getRange();
			map.put(height, island);
		}
		return map;
	}

	public double correlation(int i, int j) {
		return list.get(i).binaryIslandCorrelation(list.get(j), i + "-" + j);
	}

	public SVGG plotPixels() {
		return plotPixels(null); // may change this
	}

	public SVGG plotPixels(Transform2 t2) {
		SVGG g = new SVGG();
		for (PixelIsland island : this) {
			String saveColor = island.getPixelColor();
			island.setPixelColor(this.pixelColor);
			SVGG gg = island.plotPixels();
			if (t2 != null)
				gg.setTransform(t2);
			island.setPixelColor(saveColor);
			g.appendChild(gg);
		}
		return g;
	}

	public void setPixelColor(String color) {
		this.pixelColor = color;
	}

	public List<List<SVGPolyline>> createPolylinesIteratively(double dpEpsilon,
			int maxiter) {
		List<List<SVGPolyline>> polylineListList = new ArrayList<List<SVGPolyline>>();
		for (PixelIsland island : this) {
			List<SVGPolyline> polylineList = island.createPolylinesIteratively(
					dpEpsilon, maxiter);
			polylineListList.add(polylineList);
		}
		return polylineListList;
	}

	public PixelList getPixelList() {
		PixelList pixelList = new PixelList();
		if (list != null) {
			for (PixelIsland island : list) {
				PixelList pList = island.getPixelList();
				pixelList.addAll(pList);
			}
		}
		return pixelList;
	}

//	/**
//	 * create a list of list of rings.
//	 * 
//	 * @param outputFile
//	 *            if not null file to write SVG to
//	 * @return
//	 */
//	@Deprecated
//	public List<PixelRingList> createRingListList(File outputFile) {
//		List<PixelRingList> ringListList = new ArrayList<PixelRingList>();
//		SVGG gg = null;
//		if (outputFile != null) {
//			gg = new SVGG();
//		}
//		for (PixelIsland island : this) {
//			PixelRingList ringList = island.createOnionRings();
//			ringListList.add(ringList);
//			ringList.plotPixels(gg, new String[] {
//					"orange", "green", "blue", "red", "cyan" });
//		}
//		if (outputFile != null) {
//			SVGSVG.wrapAndWriteAsSVG(gg, outputFile);
//		}
//		return ringListList;
//	}

	public List<PixelRingList> createRingListList() {
		List<PixelRingList> ringListList = new ArrayList<PixelRingList>();
		for (PixelIsland island : this) {
			PixelRingList ringList = island.createOnionRings();
			ringListList.add(ringList);
		}
		return ringListList;
	}

	public void sortX() {
		Collections.sort(list, new PixelIslandComparator(ComparatorType.LEFT, ComparatorType.TOP));
	}

	/** sorts Y first, then X.
	 * 
	 */
	public void sortYX() {
		Collections.sort(list, new PixelIslandComparator(ComparatorType.TOP, ComparatorType.LEFT));
	}

	public void sortSize() {
		Collections.sort(list, new PixelIslandComparator(ComparatorType.SIZE));
	}

	public Real2Range getBoundingBox() {
		Real2Range boundingBox = new Real2Range();
		for (PixelIsland island : list) {
			boundingBox.plusEquals(island.getBoundingBox());
		}
		return boundingBox;
	}

	/**
	 * translates pixelIslandLIst to origin and creates image.
	 * 
	 * @return
	 */
	public BufferedImage createImageAtOrigin() {
		return createImageAtOrigin(this.getBoundingBox());

	}

	public BufferedImage createImageAtOrigin(Real2Range bbox) {
		BufferedImage image = null;
		if (bbox.getXRange() != null || bbox.getYRange() != null) {
			int x0 = (int) (double) bbox.getXMin();
			int y0 = (int) (double) bbox.getYMin();
			int width = (int) bbox.getXRange().getRange() + 1;
			int height = (int) bbox.getYRange().getRange() + 1;
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			clearImage(width, height, image);
			for (PixelIsland pixelIsland : this) {
				pixelIsland.setToBlack(image, new Int2(x0, y0));
			}
		}
		return image;
	}

	private void clearImage(int width, int height, BufferedImage image) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				image.setRGB(i, j, 0x00ffffff);
			}
		}
	}

	public SVGG getSVGG() {
		if (this.svgg == null) {
			this.svgg = new SVGG();
			for (PixelIsland pixelIsland : list) {
				svgg.appendChild(pixelIsland.getSVGG());
			}
		}
		return svgg;
	}

	/**
	 * reverses order of list.
	 * 
	 */
	public void reverse() {
		Collections.reverse(list);
	}

	/**
	 * removes all unnecessary steps while keeping minimum connectivity.
	 * 
	 */
	public void removeStepsIteratively() {
		for (PixelIsland island : list) {
//			island.removeStepsIteratively();
			// may be better...
			island.removeCorners();
			LOG.trace("after remove corners "+island.size());
		}
	}

	/**
	 * gets sizes of islands in current order.
	 * 
	 * @return
	 */
	public IntArray getSizes() {
		IntArray array = new IntArray(list.size());
		for (int i = 0; i < list.size(); i++) {
			array.setElementAt(i, list.get(i).size());
		}
		return array;
	}

	public void removeStepsSortAndReverse() {
		LOG.trace("removing steps; current Pixel size()"+this.getPixelList().size());
		removeStepsIteratively();
		createCleanIslandList();
		this.
		LOG.trace("sort and reverse");
		sortSize();
		reverse();
		LOG.trace("finish");
	}

	private void createCleanIslandList() {
		List<PixelIsland> newIslandList = new ArrayList<PixelIsland>();
		for (PixelIsland island : this) {
			PixelIsland newIsland = new PixelIsland(island.getPixelList());
			newIslandList.add(newIsland);
			this.list = newIslandList;
		}
	}

	@Deprecated
	public List<PixelGraph> analyzeEdgesAndPlot() throws IOException {
		List<PixelGraph> pixelGraphList = new ArrayList<PixelGraph>();
		removeStepsSortAndReverse();
		File outputDir = pixelProcessor.getOutputDir();
		outputDir.mkdirs();
		ImageUtil.writeImageQuietly(createImageAtOrigin(), new File(outputDir, "cleaned.png"));
		// main tree
		SVGG g = new SVGG();
		for (int i = 0; i < Math.min(size(), pixelProcessor.getMaxIsland()); i++) {
			LOG.debug("============ island "+i+"=============");
			PixelIsland island = get(i);
			BufferedImage image1 = island.createImage();
			if (image1 == null) continue;
			ImageUtil.writeImageQuietly(image1, new File(outputDir, "cleaned"+i+".png"));
			g.appendChild(island.createSVG());
			PixelGraph graph = island.createGraph();
			graph.setParameters(parameters);
			List<PixelEdge> edgeList = graph.createEdges();
			for (PixelEdge edge : edgeList) {
				g.appendChild(edge.createPixelSVG("red"));
			}
			pixelGraphList.add(graph);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File(outputDir,"graphAndChars.svg"));
		return pixelGraphList;
	}

	public List<PixelGraph> createGraphList() {
		List<PixelGraph> pixelGraphList = new ArrayList<PixelGraph>();
		removeStepsSortAndReverse();
		// main tree
		for (int i = 0; i < Math.min(size(), pixelProcessor.getMaxIsland()); i++) {
			PixelIsland island = get(i);
			PixelGraph graph = island.createGraph();
			graph.setParameters(parameters);
			pixelGraphList.add(graph);
		}
		return pixelGraphList;
	}

	public void debug() {
//		System.err.println("maxIsland:    "+this.maxIsland);
	}

	/** create PixelIslandList from String.
	 * 
	 * @param size of font
	 * @param string to write
	 * @param font font
	 * @return
	 */
	public static PixelIslandList createPixelIslandListFromString(double size, String string, String font) {
		SVGText text = new SVGText(new Real2(size/2., 3.*size/2.), string);
		text.setFontFamily(font);
		text.setFontSize(size);
		int height = (int) ( text.getFontSize() * 2.);
		int width = (int) ( text.getFontSize() * 2.);
		BufferedImage image = text.createImage(width, height);
		Thinning thinning = new ZhangSuenThinning(image);
		thinning.doThinning();
		image = thinning.getThinnedImage();
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		return pixelIslandList;
	}

	public ImageParameters getParameters() {
		return parameters;
	}

	public void setParameters(ImageParameters parameters) {
		this.parameters = parameters;
	}


}
