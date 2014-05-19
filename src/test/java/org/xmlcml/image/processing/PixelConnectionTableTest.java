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
		PixelGraph table = PixelGraph.createGraph(island);
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
		PixelGraph table = PixelGraph.createGraph(island);
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
		Assert.assertTrue(edge.equalsIgnoreOrder("{(1,0)(0,1)(-1,2)(0,3)}"));
	}
	
	@Test
	// Y-shaped tree
	// FIXME - depends on start point
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
		PixelGraph table = PixelGraph.createGraph(island);
		PixelCycle cycle = table.getCycle();
		Assert.assertNull(cycle);
		List<PixelEdge> edgeList = table.getEdges();
		Assert.assertEquals(3, edgeList.size()); 
		Assert.assertEquals("{(-3,-3)(-2,-2)(-1,-1)(0,0)}/[(-3,-3), (0,0)]", edgeList.get(0).toString());
		Assert.assertEquals("{(3,-3)(2,-2)(1,-1)(0,0)}/[(3,-3), (0,0)]", edgeList.get(1).toString());
		Assert.assertEquals("{(0,0)(0,1)(0,2)(0,3)}/[(0,0), (0,3)]", edgeList.get(2).toString());
		// 1 junction
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals("[(0,0)]", junctionSet.toString()); 
		// text 3 terminals
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals("[(-3,-3), (3,-3), (0,3)]", terminalSet.toString()); 
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
		PixelGraph table = PixelGraph.createGraph(island);
		List<PixelEdge> edgeList = table.getEdges();
		Assert.assertEquals(5, edgeList.size()); 
		Assert.assertEquals("{(-3,-5)(-2,-4)(-1,-3)(0,-2)}/[(-3,-5), (0,-2)]", edgeList.get(0).toString());
		Assert.assertEquals("{(3,-5)(2,-4)(1,-3)(0,-2)}/[(3,-5), (0,-2)]", edgeList.get(1).toString());
		Assert.assertEquals("{(0,-2)(0,-1)(0,0)(0,1)(0,2)}/[(0,-2), (0,2)]", edgeList.get(2).toString());
		Assert.assertEquals("{(0,2)(-1,3)(-2,4)(-3,5)}/[(0,2), (-3,5)]", edgeList.get(3).toString());
		Assert.assertEquals("{(0,2)(1,3)(2,4)(3,5)}/[(0,2), (3,5)]", edgeList.get(4).toString());
		// 2 junction
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals("[(0,-2), (0,2)]", junctionSet.toString()); 
		// text 4 terminals
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals("[(-3,-5), (3,-5), (-3,5), (3,5)]", terminalSet.toString()); 
	}
	
	@Test
	/** hexagon with 3 spikes
	 *       X
	 *       X
	 *       X
	 *      X X
	 *      X X
	 *     X X X
	 *    X     X
	 *   X       X
	 */
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
		island.addPixel(new Pixel(-3,6));
		island.addPixel(new Pixel(3,6));
		island.addPixel(new Pixel(-4,7));
		island.addPixel(new Pixel(4,7));
		PixelGraph table = PixelGraph.createGraph(island);
		List<PixelEdge> edgeList = table.getEdges();
		Assert.assertEquals(6, edgeList.size()); 
		Assert.assertEquals("{(0,0)(0,1)(0,2)}/[(0,0), (0,2)]", edgeList.get(0).toString());
		Assert.assertEquals("{(0,2)(-1,3)(-1,4)}/[(0,2), (-1,4)]", edgeList.get(1).toString());
		Assert.assertEquals("{(0,2)(1,3)(1,4)}/[(0,2), (1,4)]", edgeList.get(2).toString());
		Assert.assertEquals("{(-1,4)(-2,5)(-3,6)(-4,7)}/[(-1,4), (-4,7)]", edgeList.get(3).toString());
		Assert.assertEquals("{(-1,4)(0,5)(1,4)}/[(-1,4), (1,4)]", edgeList.get(4).toString());
		Assert.assertEquals("{(1,4)(2,5)(3,6)(4,7)}/[(1,4), (4,7)]", edgeList.get(5).toString());
		JunctionSet junctionSet = table.getJunctionSet();
		Assert.assertNotNull(junctionSet);
		Assert.assertEquals("[(0,2), (-1,4), (1,4)]", junctionSet.toString()); 
		TerminalNodeSet terminalSet = table.getTerminalNodeSet();
		Assert.assertNotNull(terminalSet);
		Assert.assertEquals("[(0,0), (-4,7), (4,7)]", terminalSet.toString()); 
	}

}
