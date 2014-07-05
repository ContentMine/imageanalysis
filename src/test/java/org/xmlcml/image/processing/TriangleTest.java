package org.xmlcml.image.processing;

import java.awt.Point;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.euclid.Int2;

public class TriangleTest {

	private final static Logger LOG = Logger.getLogger(TriangleTest.class);
	
	private PixelIsland island;
	private Pixel[] pixel;

	@Before
	public void setUp() {
		island = new PixelIsland();
		island.setDiagonal(true);
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
		Triangle triangle = Triangle.createTriangle(pixel[0], pixel[1], pixel[2], island);
		Pixel rightAngle = triangle.findRightAnglePixel();
		Assert.assertEquals(pixel[0], rightAngle);
	}
	
	@Test
	public void testTriangleDiagonal() {
		Triangle triangle = Triangle.createTriangle(pixel[0], pixel[1], pixel[2], island);
		List<Pixel> diagonal = triangle.getDiagonal();
		Assert.assertEquals(2, diagonal.size());
		Assert.assertTrue(diagonal.contains(pixel[1]));
		Assert.assertTrue(diagonal.contains(pixel[2]));
		PixelList neighbours0 = pixel[0].getNeighbours(island);
		PixelList neighbours1 = pixel[1].getNeighbours(island);
		PixelList neighbours2 = pixel[2].getNeighbours(island);
		Assert.assertTrue("n0 ", neighbours0.contains(pixel[1]) && neighbours0.contains(pixel[2]));
		Assert.assertTrue("n1 ", neighbours1.contains(pixel[0]) && neighbours0.contains(pixel[2]));
		Assert.assertTrue("n2 ", neighbours2.contains(pixel[0]) && neighbours0.contains(pixel[1]));
	}
	
	@Test
	public void testRemoveDiagonal() {
		LOG.trace("p0 "+pixel[0]);
		LOG.trace("p1 "+pixel[1]);
		LOG.trace("p2 "+pixel[2]);
		Triangle triangle = Triangle.createTriangle(pixel[0], pixel[1], pixel[2], island);
		LOG.trace("p0 "+pixel[0]);
		LOG.trace("p1 "+pixel[1]);
		LOG.trace("p2 "+pixel[2]);
		triangle.removeDiagonalNeighbours();
		PixelList neighbours0 = pixel[0].getNeighbours(island);
		PixelList neighbours1 = pixel[1].getNeighbours(island);
		PixelList neighbours2 = pixel[2].getNeighbours(island);
		LOG.trace("n0 "+neighbours0);
		LOG.trace("n1 "+neighbours1);
		LOG.trace("n2 "+neighbours2);
		Assert.assertTrue("nn0 ", neighbours0.contains(pixel[1]) && neighbours0.contains(pixel[2]));
		Assert.assertTrue("nn1 ", neighbours1.contains(pixel[0]) && !neighbours1.contains(pixel[2]));
		Assert.assertTrue("nn2 ", neighbours2.contains(pixel[0]) && !neighbours2.contains(pixel[1]));
	}
	
	
	@Test
	public void testTriangleNonDiagonal() {
		
		pixel[2].setInt2(new Int2(1,-1));
		pixel[0].clearNeighbours();
		pixel[1].clearNeighbours();
		pixel[2].getNeighbours(island);
		Triangle triangle = Triangle.createTriangle(pixel[0], pixel[1], pixel[2], island);
		LOG.trace("triangle: "+triangle);
		List<Pixel> diagonal = triangle.getDiagonal();
		LOG.trace("diag "+diagonal);
		Assert.assertNull(diagonal);
	}
	
}
