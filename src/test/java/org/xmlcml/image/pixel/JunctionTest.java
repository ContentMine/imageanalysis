package org.xmlcml.image.pixel;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.pixel.Pixel;
import org.xmlcml.image.pixel.PixelIsland;
import org.xmlcml.image.pixel.PixelList;

public class JunctionTest {

	public final static Logger LOG = Logger.getLogger(JunctionTest.class);
	
	private final static File G002_DIR = new File(Fixtures.COMPOUND_DIR, "journal.pone.0095565.g002");
	private PixelIsland island1;
	private Pixel p1_00;
	private Pixel p1_10;
	private Pixel p1_20;
	private Pixel p1_11;

	private PixelIsland createIsland1() {
		island1 = new PixelIsland();
		p1_00 = new Pixel(0,0);
		p1_10 = new Pixel(1,0);
		p1_20 = new Pixel(2,0);
		p1_11 = new Pixel(1,1);
		island1.addPixel(p1_00);
		island1.addPixel(p1_10);
		island1.addPixel(p1_20);
		island1.addPixel(p1_11);
		island1.setDiagonal(true);
		return island1;
	}

	
	@Test
	public void testcreateIsland() throws IOException {
		PixelIsland island = createIsland1();
		Assert.assertEquals("island", 4, island.size());
		PixelList n00 = p1_00.getNeighbours(island1);
		Assert.assertEquals("n00", "{(1,0)(1,1)}", n00.toString());
		PixelList n10 = p1_10.getNeighbours(island1);
		Assert.assertEquals("n10", "{(2,0)(0,0)(1,1)}", n10.toString());
		PixelList n20 = p1_20.getNeighbours(island1);
		Assert.assertEquals("n20", "{(1,0)(1,1)}", n20.toString());
		PixelList n11 = p1_11.getNeighbours(island1);
		Assert.assertEquals("n11", "{(1,0)(2,0)(0,0)}", n11.toString());
	}

}
