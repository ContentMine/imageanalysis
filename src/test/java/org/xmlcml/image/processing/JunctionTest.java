package org.xmlcml.image.processing;

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
import org.xmlcml.image.compound.PixelList;

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

	@Test
	@Ignore // fails with new connection table
	// FIXME
	public void testFindJunctions() throws IOException {
		PixelIsland island = createIsland1();
		PixelGraph connectionTable = PixelGraph.createGraph(island);
		Assert.assertNull("00", Junction.createJunction(p1_00, island));
		Assert.assertNotNull("10", Junction.createJunction(p1_10, island));
		Assert.assertNull("20", Junction.createJunction(p1_20, island));
		
		// check all this
//		Assert.assertNull("11", Junction.createJunction(p1_11, island));
//		Set<Junction> junctionSet = connectionTable.findJunctions();
//		Assert.assertEquals("set", 1, junctionSet.size());
//		Junction junction = junctionSet.toArray(new Junction[0])[0];
//		Assert.assertEquals("centre", island.get(1), junction.getCentre());
//		Assert.assertEquals("stem", island.get(3), junction.getStem());
//		Assert.assertEquals("neighbours", 2, junction.getNeighbours().size());
//		Assert.assertTrue("neighbours", junction.getNeighbours().contains(new Pixel(0, 0)));
//		Assert.assertTrue("neighbours", junction.getNeighbours().contains(new Pixel(2, 0)));
	}

	@Test
	@Ignore // fails with new connection table
	// FIXME
	public void testFindJunctionsInPlot() throws IOException {
		PixelIsland island = PixelIslandList.thinFillAndGetPixelIslandList(
				ImageIO.read(new File(G002_DIR, "points.png")), new ZhangSuenThinning()).get(0);
		island.removeStepsIteratively();
		PixelGraph table = PixelGraph.createGraph(island);
		SVGG g = new SVGG();
		g.appendChild(island.createSVG());
		JunctionSet junctionSet = table.getJunctionSet();
		for (PixelNode tJunction : junctionSet) {
			SVGCircle circle = new SVGCircle(new Real2(tJunction.getCentrePixel().getInt2()).plus(new Real2(0.5, 0.5)), 3.);
			circle.setOpacity(0.2);
			g.appendChild(circle);
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/plot/tjunctions.svg"));
	}
}
