package org.xmlcml.image.compound;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.processing.Pixel;
import org.xmlcml.image.processing.PixelIsland;

/**
 * container for a lit of pixels. 
 * can have additional attributes such as colour or value.
 * 
 * @author pm286
 *
 */
public class PixelList implements Iterable<Pixel> {

	private List<Pixel> list;
	private Real2Array points;
	
	public PixelList() {
		init();
	}

	private void init() {
		list = new ArrayList<Pixel>();
	}
	
	public PixelList(Collection<Pixel> pixelCollection) {
		init();
		list.addAll(pixelCollection);
	}

	public PixelList(PixelList list) {
		this(list.getList());
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
	
	public void add(Pixel pixel) {
		if (list == null) {
			init();
		}
		list.add(pixel);
	}
	
	public List<Pixel> getList() {
		return list;
	}

	public int size() {
		return list == null ? 0 : list.size();
	}

	public void addAll(PixelList pixelList) {
		this.list.addAll(pixelList.getList());
	}

	public boolean contains(Pixel pixel) {
		return list != null && list.contains(pixel);
	}

	public boolean remove(Pixel pixel) {
		if (list != null) {
			return list.remove(pixel);
		}
		return false;
	}

	public PixelList getPixelsTouching(PixelList list1, PixelIsland island) {
		Set<Pixel> used = new HashSet<Pixel>();
		PixelList touchingList = new PixelList();
		if (list1 != null && list1.size() > 0) {
			int value = list1.get(0).getValue();
			for (Pixel pixel : list) {
				PixelList neighbours = pixel.getNeighbours(island);
				for (Pixel neighbour : neighbours) {
					if (neighbour.getValue() == value && !used.contains(neighbour)) {
						used.add(neighbour);
						touchingList.add(neighbour);
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
		StringBuilder sb = new StringBuilder("{");
		for (Pixel pixel : this) {
			sb.append(pixel.toString());
		}
		sb.append("}");
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

}
