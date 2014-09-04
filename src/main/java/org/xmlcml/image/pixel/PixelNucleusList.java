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

	public void doTJunctionThinning(PixelIsland island) {
		for (PixelNucleus nucleus : this) {
			nucleus.doTJunctionThinning(island);
		}
	}

	public int size() {
		ensureList();
		return list.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (PixelNucleus nucleus : this) {
			sb.append(nucleus.toString());
		}
		sb.append("}");
		return sb.toString();
	}
	

}
