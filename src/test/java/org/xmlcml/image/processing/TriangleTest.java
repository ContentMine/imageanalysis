package org.xmlcml.image.processing;

import java.awt.Point;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TriangleTest {

	private PixelIsland island;
	private Pixel[] pixel;

	@Before
	public void setUp() {
		island = new PixelIsland();
		pixel = new Pixel[3];
		pixel[0] = new Pixel(new Point(0,0));
		pixel[1] = new Pixel(new Point(0,1));
		pixel[2] = new Pixel(new Point(1,0));
		island.addPixel(pixel[0]);
		island.addPixel(pixel[1]);
		island.addPixel(pixel[2]);
		
	}
	
	@Test
	public void testTriangleRightAngle() {
		Triangle triangle = new Triangle(pixel[0], pixel[1], pixel[2], island);
		Pixel rightAngle = triangle.findRightAnglePixel();
		Assert.assertEquals(rightAngle, pixel[0]);
	}
	
	@Test
	public void testTriangleDiagonal() {
		Triangle triangle = new Triangle(pixel[0], pixel[1], pixel[2], island);
		List<Pixel> diagonal = triangle.getDiagonal();
		Assert.assertEquals(2, diagonal.size());
		Assert.assertTrue(diagonal.contains(pixel[1]));
		Assert.assertTrue(diagonal.contains(pixel[2]));
	}
	
}
