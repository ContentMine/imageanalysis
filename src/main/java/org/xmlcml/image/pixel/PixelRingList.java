package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;

public class PixelRingList implements Iterable<PixelList> {

	public static final String[] DEFAULT_COLOURS = {"red", "cyan", "orange", "green", "magenta", "blue"};
	private List<PixelList> ringList;
	
	public PixelRingList() {
		init();
	}

	private void init() {
		ringList = new ArrayList<PixelList>();
	}
	
	public PixelRingList(Collection<PixelList> pixelCollection) {
		init();
		ringList.addAll(pixelCollection);
	}

	@Override
	public Iterator<PixelList> iterator() {
		return ringList.iterator();
	}
	
	public PixelList get(int i) {
		return (ringList == null || i < 0 || i >= ringList.size()) ? null : ringList.get(i);
	}
	
	public void add(PixelList pixelList) {
		if (ringList == null) {
			init();
		}
		ringList.add(pixelList);
	}
	
	public List<PixelList> getList() {
		return ringList;
	}

	public int size() {
		return ringList == null ? 0 : ringList.size();
	}

	public void addAll(PixelRingList RingList) {
		this.ringList.addAll(RingList.getList());
	}

	public boolean contains(PixelList pixelList) {
		return ringList != null && ringList.contains(pixelList);
	}

	public boolean remove(PixelList pixelList) {
		if (ringList != null) {
			return ringList.remove(pixelList);
		}
		return false;
	}

	/** plots rings in different colours
	 * 
	 * cycles through the colours if not enough.
	 * 
	 * @param gg SVGG to which everything is drawn; if null create one
	 * @param fill list of colours; if null, defaults are used
	 */
	public SVGG plotPixels(SVGG gg, String[] fill) {
		if (gg != null) {
			gg = new SVGG();
		}
		if (fill == null) {
			fill = DEFAULT_COLOURS;
		}
		int i = 0;
		
		for (PixelList pixelList : this) {
			SVGG g = pixelList.plotPixels(fill[i]);
			gg.appendChild(g);
			i = (i + 1) % fill.length;
		}
		return gg;
	}
	
	/** plot rings with defaults
	 * 
	 */
	public SVGG plotPixels() {
		return plotPixels(null, null);
	}

//	/**
//	 * creates a list of onion rings.
//	 * 
//	 * the pixels are organized as 1-pixel-thick rings from the outside
//	 * 
//	 * @param gg
//	 *            if not null plots the rings within gg
//	 * @param colours
//	 * @return
//	 */
//	@Deprecated
//	public void createRingsAndPlot(SVGG gg, String[] colours) {
//		SVGG g = new SVGG();
//		if (gg != null) {
//			int i = 0;
//			for (PixelList ring : this) {
//				ring.plotPixels(g, colours[i]);
//				if (g.getParent() != null) {
//					g.detach();
//				}
//				gg.appendChild(g);
//				i = (i + 1) % colours.length;
//			}
//		}
//	}

}
