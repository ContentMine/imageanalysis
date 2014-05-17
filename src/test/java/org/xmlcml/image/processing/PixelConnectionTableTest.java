package org.xmlcml.image.processing;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.compound.PixelList;

public class PixelConnectionTableTest {

	private final static Logger LOG = Logger.getLogger(PixelConnectionTableTest.class);
	
	@Test
	public void testSingleCycle() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		// simple diamond cycle
		island.addPixel(new Pixel(1,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(-1,0));
		island.addPixel(new Pixel(0, -1));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		Assert.assertNotNull(table);
		PixelCycle cycle = table.getCycle();
		Assert.assertNotNull(cycle);
		PixelEdge edge = cycle.getEdge();
		Assert.assertNotNull(edge);
		PixelList list = edge.getPixelList();
		Assert.assertNotNull(list);
		Assert.assertEquals("cycle", "{(1,0)(0,1)(-1,0)(0,-1)}", list.toString());
		List<PixelNode> nodes = edge.getPixelNodes();
		Assert.assertEquals(0, nodes.size());
	}
	
	@Test
	public void test2Nodes() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		// simple diamond cycle
		island.addPixel(new Pixel(1,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(-1,2));
		island.addPixel(new Pixel(0, 3));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		Assert.assertNotNull(table);
		LOG.debug(table);
	}
}
