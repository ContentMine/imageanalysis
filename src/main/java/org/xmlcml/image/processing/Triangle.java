package org.xmlcml.image.processing;

import java.util.HashSet;
import java.util.Set;

public class Triangle {

	private Pixel pixel;
	private Pixel pixel2;
	private Pixel pixel3;
	private Set<Pixel> set = new HashSet<Pixel>();
	
	public Triangle(Pixel pixel, Pixel pixel2, Pixel pixel3) {
		this.pixel = pixel;
		this.pixel2 = pixel2;
		this.pixel3 = pixel3;
		set.add(pixel);
		set.add(pixel2);
		set.add(pixel3);
	}
	
	@Override 
	public boolean equals(Object o) {
		boolean equals = false;
		if (o != null && o instanceof Triangle) {
			Triangle triangle = (Triangle) o;
			equals = set.equals(triangle.set);
		}
		return equals;
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	public Set<Pixel> addAll(Triangle triangle) {
		Set<Pixel> union = new HashSet<Pixel>(set);
		union.addAll(triangle.set);
		return union;
	}
	
	public Set<Pixel> retainAll(Triangle triangle) {
		Set<Pixel> retained = new HashSet<Pixel>(set);
		retained.retainAll(triangle.set);
		return retained;
	}
}
