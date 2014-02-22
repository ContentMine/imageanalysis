package org.xmlcml.image.lines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlcml.euclid.Real2;

public class DouglasPeuckerTest {
	@Test
	public void testLine() {
		DouglasPeucker douglasPeucker = new DouglasPeucker(1.0);
		List<Real2> points = new ArrayList<Real2>();
		points.add(new Real2(1.0, 11.0));
		points.add(new Real2(2.0, 12.0));
		points.add(new Real2(3.0, 13.0));
		points.add(new Real2(4.0, 14.0));
		points.add(new Real2(5.0, 15.0));
		List<Real2> reducedList = douglasPeucker.reduce(points);
		Assert.assertEquals("reduce", 2, reducedList.size());
		Assert.assertEquals("point0", "(1.0,11.0)", reducedList.get(0).toString());
		Assert.assertEquals("point0", "(5.0,15.0)", reducedList.get(1).toString());
	}

	@Test
	public void testNoChange() {
		DouglasPeucker douglasPeucker = new DouglasPeucker(0.01);
		double d = 0.1;
		List<Real2> points = new ArrayList<Real2>();
		points.add(new Real2(1.0-d, 11.0));
		points.add(new Real2(2.0+d, 12.0));
		points.add(new Real2(3.0-d, 13.0));
		points.add(new Real2(4.0+d, 14.0));
		points.add(new Real2(5.0-d, 15.0));
		List<Real2> reducedList = douglasPeucker.reduce(points);
		Assert.assertEquals("reduce", 5, reducedList.size());
		Assert.assertEquals("point0", "(0.9,11.0)", reducedList.get(0).toString());
		Assert.assertEquals("point0", "(2.1,12.0)", reducedList.get(1).toString());
		Assert.assertEquals("point0", "(2.9,13.0)", reducedList.get(2).toString());
		Assert.assertEquals("point0", "(4.1,14.0)", reducedList.get(3).toString());
		Assert.assertEquals("point4", "(4.9,15.0)", reducedList.get(4).toString());
	}

	@Test
	public void testStraightened() {
		DouglasPeucker douglasPeucker = new DouglasPeucker(0.1);
		double d = 0.01;
		List<Real2> points = new ArrayList<Real2>();
		points.add(new Real2(1.0-d, 11.0));
		points.add(new Real2(2.0+d, 12.0));
		points.add(new Real2(3.0-d, 13.0));
		points.add(new Real2(4.0+d, 14.0));
		points.add(new Real2(5.0-d, 15.0));
		List<Real2> reducedList = douglasPeucker.reduce(points);
		Assert.assertEquals("reduce", 2, reducedList.size());
		Assert.assertEquals("point0", "(0.99,11.0)", reducedList.get(0).toString());
		Assert.assertEquals("point1", "(4.99,15.0)", reducedList.get(1).toString());
	}

	@Test
	public void testBend() {
		DouglasPeucker douglasPeucker = new DouglasPeucker(0.1);
		double d = 0.01;
		List<Real2> points = new ArrayList<Real2>();
		points.add(new Real2(1.0-d, 11.0));
		points.add(new Real2(2.0+d, 12.0));
		points.add(new Real2(3.0-d, 13.0));
		points.add(new Real2(2.0+d, 14.0));
		points.add(new Real2(1.0-d, 15.0));
		List<Real2> reducedList = douglasPeucker.reduce(points);
		Assert.assertEquals("reduce", 3, reducedList.size());
		Assert.assertEquals("point0", "(0.99,11.0)", reducedList.get(0).toString());
		Assert.assertEquals("point1", "(2.99,13.0)", reducedList.get(1).toString());
		Assert.assertEquals("point2", "(0.99,15.0)", reducedList.get(2).toString());
	}
	
	@Test
	public void testLinesFromPixels() throws IOException {
//		Vectorizer vectorizer = new Vectorizer();
//		vectorizer.readFile(Fixtures.MALTORYZINE_THINNED_PNG);
//		List<PixelIsland> islandList = vectorizer.createIslands();
//		Assert.assertEquals("islands", 5, islandList.size());
//		vectorizer.segment(islandList.get(1));
//		int[] islands =   {492, 33,  25,  29,  25};
	}
}
