package org.xmlcml.image.compound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
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

}
