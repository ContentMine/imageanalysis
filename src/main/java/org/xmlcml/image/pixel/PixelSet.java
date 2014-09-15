package org.xmlcml.image.pixel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PixelSet implements Collection<Pixel> {

	private Set<Pixel> pixelSet;
	
	public PixelSet() {
		ensurePixelSet();
	}

	public PixelSet(PixelSet set) {
		this.pixelSet = set.pixelSet;
	}

	private void ensurePixelSet() {
		if (pixelSet == null) {
			pixelSet = new HashSet<Pixel>(); 
		}
	}

	public boolean add(Pixel pixel) {
		ensurePixelSet();
		return pixelSet.add(pixel);
	}

	public boolean contains(Pixel pixel) {
		ensurePixelSet();
		return pixelSet.contains(pixel);
	}

	public int size() {
		ensurePixelSet();
		return pixelSet.size();
	}

	public boolean isEmpty() {
		ensurePixelSet();
		return pixelSet.isEmpty();
	}

	public Iterator<Pixel> iterator() {
		ensurePixelSet();
		return pixelSet.iterator();
	}

	public void remove(Pixel pixel) {
		ensurePixelSet();
		pixelSet.remove(pixel);
	}

	public void addAll(PixelList list) {
		ensurePixelSet();
		pixelSet.addAll(list.getList());
	}

	@Override
	public boolean contains(Object o) {
		ensurePixelSet();
		return pixelSet.contains(o);
	}

	@Override
	public Object[] toArray() {
		ensurePixelSet();
		return pixelSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		ensurePixelSet();
		return pixelSet.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		ensurePixelSet();
		return pixelSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		ensurePixelSet();
		return pixelSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Pixel> c) {
		ensurePixelSet();
		return pixelSet.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		ensurePixelSet();
		return pixelSet.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		ensurePixelSet();
		return pixelSet.retainAll(c);
	}

	@Override
	public void clear() {
		ensurePixelSet();
		pixelSet.clear();
	}
}
