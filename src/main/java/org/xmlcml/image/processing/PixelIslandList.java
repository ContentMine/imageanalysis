package org.xmlcml.image.processing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.compound.PixelList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/** container for collection of PixelIslands.
 * 
 * @author pm286
 *
 */
public class PixelIslandList implements Iterable<PixelIsland> {

	private final static Logger LOG = Logger.getLogger(PixelIslandList.class);
	public enum Operation {
		BINARIZE,
		THIN
	}
	
	private List<PixelIsland> list;
	private BufferedImage thinnedImage;
	private String pixelColor;
	
	public PixelIslandList() {
		list = new ArrayList<PixelIsland>();
	}
	
	public PixelIslandList(List<PixelIsland> newList) {
		this.list = newList;
	}
	
	public PixelIslandList(Collection<PixelIsland> collection) {
		this(new ArrayList<PixelIsland>(collection));
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

	/** creates islands 
	 * 
	 * @param file of image
	 * @param operations BINARIZE and THIN
	 * @return island list
	 * @throws IOException
	 */
	public static PixelIslandList createPixelIslandList(File file, Operation... operations) throws IOException {
		BufferedImage image = ImageIO.read(file);
		return createPixelIslandList(image, operations);
	}

	/** creates PixelIslands ffrom iamge using floodfill.
	 * 
	 * @param image
	 * @param operations optionally BINARIZE and THIN (maybe better done elsewhere)
	 * @return
	 * @throws IOException
	 */
	public static PixelIslandList createPixelIslandList(BufferedImage image,
			Operation... operations) {
		List<Operation> opList = Arrays.asList(operations);
		LOG.trace("pre-bin");
		if (opList.contains(Operation.BINARIZE)) {
			image = ImageUtil.binarize(image);
		}
	    if (opList.contains(Operation.THIN)) {
			image = ImageUtil.zhangSuenThin(image);
	    }
		LOG.trace("postbin ");
		PixelIslandList islands = PixelIslandList.createPixelIslandList(image);
		return islands;
	}

	/** find all separated islands.
	 *  
	 * creates a FloodFill and extracts Islands from it.
	 * diagonal set to true
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public static PixelIslandList createPixelIslandList(BufferedImage image) {
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIslandList islandList = floodFill.getPixelIslandList();
		return islandList;
	}
	

	public PixelIslandList smallerThan(Real2 box) {
		List<PixelIsland> newList = new ArrayList<PixelIsland>();
		for (PixelIsland island : list) {
			Real2Range bbox = island.getBoundingBox();
			if (bbox.getXRange().getRange() < box.getX() && bbox.getYRange().getRange() < box.getY()) {
				newList.add(island);
			} else {
				LOG.trace("omitted "+bbox);
			}
		}
		return new PixelIslandList(newList);
	}

	/** create list of islands falling within dimension ranges.
	 *  
	 *  if island dimensions (width, height) fit with x/ySizeRange add to
	 *  list
	 * @param xSizeRange range of xSizes inclusive
	 * @param ySizeRange range of ySizes inclusive
	 * @return
	 */
	public PixelIslandList isContainedIn(RealRange xSizeRange, RealRange ySizeRange) {
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
			Integer height = (int) island.getBoundingBox().getYRange().getRange();
			map.put(height, island);
		}
		return map;
	}

	public double correlation(int i, int j) {
		return list.get(i).binaryIslandCorrelation(list.get(j), i+"-"+j);
	}

	public static PixelIslandList thinFillAndGetPixelIslandList(BufferedImage image0, Thinning thinning) {
		BufferedImage image = ImageUtil.thin(image0, thinning);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIslandList islandList = floodFill.getPixelIslandList();
		islandList.setThinnedImage(image);
		return islandList;
	}

	private void setThinnedImage(BufferedImage image) {
		this.thinnedImage = image;
	}
	
	public BufferedImage getThinnedImage() {
		return thinnedImage;
	}

	public SVGG plotPixels() {
		return plotPixels(null); // may change this
	}
	
	public SVGG plotPixels(Transform2 t2) {
		SVGG g = new SVGG();
		for (PixelIsland island : this) {
			String saveColor = island.getPixelColor();
			island.setPixelColor(this.pixelColor);
			SVGG gg = 	island.plotPixels();
			if (t2 != null) gg.setTransform(t2);
			island.setPixelColor(saveColor);
			g.appendChild(gg);
		}
		return g;
	}
	
	public void setPixelColor(String color) {
		this.pixelColor = color;
	}
	
	public List<List<SVGPolyline>> createPolylinesIteratively(double dpEpsilon, int maxiter) {
		List<List<SVGPolyline>> polylineListList = new ArrayList<List<SVGPolyline>>();
		for (PixelIsland island : this) {
			List<SVGPolyline> polylineList = island.createPolylinesIteratively(dpEpsilon, maxiter);
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

	/** create a list of list of rings.
	 * 
	 * @param outputFile if not null file to write SVG to 
	 * @return
	 */
	public List<RingList> createRingListList(File outputFile) {
		List<RingList> ringListList = new ArrayList<RingList>();
		SVGG gg = null;
		if (outputFile != null) {
			gg = new SVGG();
		}
		for (PixelIsland island : this) {
			RingList ringList = island.createRingsAndPlot(gg, new String[]{"orange", "green", "blue", "red", "cyan"} );
			ringListList.add(ringList);
		}
		if (outputFile != null) {
			SVGSVG.wrapAndWriteAsSVG(gg, outputFile);
		}
		return ringListList;
	}

	/** creates list of rings.
	 * 
	 * @return list of rings
	 */
	public List<RingList> createRingListList() {
		return createRingListList(null);
	}
}
