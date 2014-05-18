package org.xmlcml.image.processing;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
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
		// single cycle, test its properties
		PixelCycle cycle = table.getCycle();
		Assert.assertNotNull(cycle);
		PixelEdge edge = cycle.getEdge();
		Assert.assertNotNull(edge);
		PixelList list = edge.getPixelList();
		Assert.assertNotNull(list);
		Assert.assertEquals("cycle", "{(1,0)(0,1)(-1,0)(0,-1)}", list.toString());
		List<PixelNode> nodes = edge.getPixelNodes();
		Assert.assertEquals(0, nodes.size());
		// text zero junctions
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals(0, junctionSet.size());
		// text zero terminals
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals(0, terminalSet.size());
	}
	
	@Test
	// simple line
	public void test2Nodes() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		// simple wiggly line
		island.addPixel(new Pixel(1,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(-1,2));
		island.addPixel(new Pixel(0, 3));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		PixelCycle cycle = table.getCycle();
		Assert.assertNull(cycle);
		// no junctions
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals(0, junctionSet.size());
		// text 2 terminals
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals(2, terminalSet.size());
		// terminal 0
		TerminalNode node0 = (TerminalNode) terminalSet.get(0);
		Assert.assertNotNull(node0);
		Pixel pixel0 = node0.getCentrePixel();
		Assert.assertEquals("(1,0)", pixel0.toString());
		Assert.assertEquals(node0, table.getPixelNode(pixel0));
		// terminal 1
		TerminalNode node1 = (TerminalNode) terminalSet.get(1);
		Assert.assertNotNull(node1);
		Pixel pixel1 = node1.getCentrePixel();
		Assert.assertEquals("(0,3)", pixel1.toString());
		Assert.assertEquals(node1, table.getPixelNode(pixel1));
		// 
		List<PixelEdge> edges = table.getEdges();
		Assert.assertEquals(1, edges.size());
		PixelEdge edge = edges.get(0);
		PixelList edgePixelList = edge.getPixelList();
		Assert.assertNotNull(edgePixelList);
		Assert.assertEquals(4, edgePixelList.size());
		Assert.assertEquals("{(1,0)(0,1)(-1,2)(0,3)}", edgePixelList.toString());
	}
	
	@Test
	// Y-shaped tree
	public void test3Terminals() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		island.addPixel(new Pixel(0,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(0,2));
		island.addPixel(new Pixel(0,3));
		island.addPixel(new Pixel(-1, -1));
		island.addPixel(new Pixel(-2, -2));
		island.addPixel(new Pixel(-3, -3));
		island.addPixel(new Pixel(1, -1));
		island.addPixel(new Pixel(2, -2));
		island.addPixel(new Pixel(3, -3));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		PixelCycle cycle = table.getCycle();
		Assert.assertNull(cycle);
		List<PixelEdge> edgeList = table.getEdges();
		Assert.assertEquals(3, edgeList.size()); 
		// 1 junction
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals(1, junctionSet.size()); 
		Junction junction = (Junction) junctionSet.get(0);
		Assert.assertNotNull(junction);
		Pixel pixelj = junction.getCentrePixel();
		Assert.assertEquals("(0,0)", pixelj.toString());
		// text 2 terminals
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals(3, terminalSet.size());
		// terminal 0
		TerminalNode node0 = (TerminalNode) terminalSet.get(0);
		Assert.assertNotNull(node0);
		Pixel pixel0 = node0.getCentrePixel();
		Assert.assertEquals("(-3,-3)", pixel0.toString());
		Assert.assertEquals(node0, table.getPixelNode(pixel0));
		// terminal 1
		TerminalNode node1 = (TerminalNode) terminalSet.get(1);
		Assert.assertNotNull(node1);
		Pixel pixel1 = node1.getCentrePixel();
		Assert.assertEquals("(3,-3)", pixel1.toString());
		Assert.assertEquals(node1, table.getPixelNode(pixel1));
		// 
		List<PixelEdge> edges = table.getEdges();
		Assert.assertEquals(3, edges.size());
		PixelEdge edge = edges.get(0);
		PixelList edgePixelList = edge.getPixelList();
		Assert.assertNotNull(edgePixelList);
		Assert.assertEquals(4, edgePixelList.size());
//		Assert.assertEquals("{(1,0)(0,1)(0,2)(0,3)}", edgePixelList.toString());
		for (PixelEdge edgex : edges) {
			LOG.debug(edgex);
		}
	}
	
	@Test
	// 2 Y's joined
	public void test1122TetramethylEthane() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		island.addPixel(new Pixel(0,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(0,2));
		island.addPixel(new Pixel(1,3));
		island.addPixel(new Pixel(-1,3));
		island.addPixel(new Pixel(2,4));
		island.addPixel(new Pixel(-2,4));
		island.addPixel(new Pixel(3,5));
		island.addPixel(new Pixel(-3,5));
		island.addPixel(new Pixel(0,-1));
		island.addPixel(new Pixel(0,-2));
		island.addPixel(new Pixel(1,-3));
		island.addPixel(new Pixel(-1,-3));
		island.addPixel(new Pixel(2,-4));
		island.addPixel(new Pixel(-2,-4));
		island.addPixel(new Pixel(3,-5));
		island.addPixel(new Pixel(-3,-5));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		PixelCycle cycle = table.getCycle();
		Assert.assertNull(cycle);
		List<PixelEdge> edges = table.getEdges();
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertEquals(2, junctionSet.size());
		LOG.debug("junctions: "+junctionSet);
		for (PixelEdge edge : edges) {
			LOG.debug(edge);
		}
	}
	
	@Test
	// hexagon with 3 spikes // fails
	@Ignore
	public void test135TrimethylBenzene() {
		PixelIsland island = new PixelIsland();
		island.setDiagonal(true);
		island.addPixel(new Pixel(0,0));
		island.addPixel(new Pixel(0,1));
		island.addPixel(new Pixel(0,2));
		island.addPixel(new Pixel(1,3));
		island.addPixel(new Pixel(-1,3));
		island.addPixel(new Pixel(1,4));
		island.addPixel(new Pixel(-1,4));
		island.addPixel(new Pixel(-2,5));
		island.addPixel(new Pixel(0,5));
		island.addPixel(new Pixel(2,5));
		island.addPixel(new Pixel(-3,3));
		island.addPixel(new Pixel(3,3));
		island.addPixel(new Pixel(-4,4));
		island.addPixel(new Pixel(4,4));
		PixelConnectionTable table = PixelConnectionTable.createConnectionTable(island);
		PixelCycle cycle = table.getCycle();
		Assert.assertNull(cycle);
	}

}
