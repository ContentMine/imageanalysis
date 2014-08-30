package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class PixelNucleusList implements Iterable<PixelNucleus> {

	private final static Logger LOG = Logger.getLogger(PixelNucleusList.class);
	private List<PixelNucleus> list;

	public PixelNucleusList() {
		ensureList();
	}
	
	@Override
	public Iterator<PixelNucleus> iterator() {
		ensureList();
		return list.iterator();
	}
	
	public void add(PixelNucleus nucleus) {
		ensureList();
		list.add(nucleus);
	}

	private void ensureList() {
		if (list == null) {
			list = new ArrayList<PixelNucleus>();
		}
	}

	public void superthin(PixelIsland island) {
		for (PixelNucleus nucleus : this) {
			PixelList removables = nucleus.getSuperthinRemovablePixelList();
			LOG.trace("remove "+removables);
			island.removePixels(removables);
//			removables.removeFrom(island);
		}
	}

	public int size() {
		ensureList();
		return list.size();
	}
	
	

}
