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
import org.xmlcml.image.ImageUtil;

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
			image = ImageUtil.thin(image);
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

	public static PixelIslandList thinFillAndGetPixelIslandList(BufferedImage image0) {
		BufferedImage image = ImageUtil.thin(image0);
		FloodFill floodFill = new FloodFill(image);
		floodFill.setDiagonal(true);
		floodFill.fill();
		PixelIslandList islandList = floodFill.getPixelIslandList();
		return islandList;
	}

}
