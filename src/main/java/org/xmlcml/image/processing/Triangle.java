package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Triangle {

	private Pixel[] pixel = new Pixel[3];
	private Set<Pixel> set = new HashSet<Pixel>();
	private PixelIsland island;

	Triangle (Pixel pixel0, Pixel pixel1, Pixel pixel2, PixelIsland island) {
		this.island = island;
		this.pixel[0] = pixel0;
		this.pixel[1] = pixel1;
		this.pixel[2] = pixel2;
		set.addAll(Arrays.asList(pixel));
	}

	/** returns a Triangle if points for right-angled isosceles.
	 * 
	 * @param pixel0
	 * @param pixel1
	 * @param pixel2
	 * @param island
	 * @return null if not RH isosceles
	 */
	public static Triangle createTriangle(Pixel pixel0, Pixel pixel1, Pixel pixel2, PixelIsland island) {
		Triangle triangle = new Triangle(pixel0, pixel1, pixel2, island);
		return triangle.createSet() ? triangle : null;
	}

	private boolean createSet() {
		boolean created = false;
		List<Pixel> neighbours0 = pixel[0].getNeighbours(island);
		List<Pixel> neighbours1 = pixel[1].getNeighbours(island);
		if (neighbours0.contains(pixel[1]) && neighbours0.contains(pixel[2]) && neighbours1.contains(pixel[2])) {
			set.addAll(Arrays.asList(pixel));
			created = true;
		}
		return created;
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
	
	public Pixel findRightAnglePixel() {
		for (int i = 0; i < 3; i++) {
			Pixel pixelj = pixel[(i+1)%3];
			Pixel pixelk = pixel[(i+2)%3];
			if (
				(pixelj.getInt2().getX() != pixelk.getInt2().getX()) &&
				(pixelj.getInt2().getY() != pixelk.getInt2().getY())) {
				return pixel[i];
			}
		}
		return null;
	}
	
	public List<Pixel> getDiagonal() {
		List<Pixel> diagonal = null;
		Pixel rightAngle = findRightAnglePixel();
		if (rightAngle != null) {
			diagonal = new ArrayList<Pixel>(Arrays.asList(set.toArray(new Pixel[0])));
			diagonal.remove(rightAngle);
		}
		return diagonal;
	}
}
