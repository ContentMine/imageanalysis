package org.xmlcml.image.pixel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class PixelNucleusFactoryAndSpikesTest {
	
	public final static Logger LOG = Logger.getLogger(PixelNucleusFactoryAndSpikesTest.class);

	@Test
	public void testPixelNucleusListDot() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOT_ISLAND());
		Assert.assertEquals("island", "{(0,0)}",
				Fixtures.CREATE_DOT_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 1, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListLine() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_LINE_ISLAND());
		Assert.assertEquals("island", "{(2,0)(1,0)(0,1)(-1,2)(0,3)(0,4)}",
				Fixtures.CREATE_LINE_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 2, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(2,0)}}{{(0,4)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListCycle() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_CYCLE_ISLAND());
		Assert.assertEquals("island", "{(1,0)(0,1)(-1,0)(0,-1)}",
				Fixtures.CREATE_CYCLE_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 0, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_Y_ISLAND());
		Assert.assertEquals("island", "{(0,0)(0,1)(0,2)(0,3)(-1,-1)(-2,-2)(-3,-3)(1,-1)(2,-2)(3,-3)}",
				Fixtures.CREATE_Y_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 4, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}{{(0,3)}}{{(-3,-3)}}{{(3,-3)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListDoubleY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOUBLE_Y_ISLAND());
		Assert.assertEquals("island", "{(0,0)(0,1)(0,2)(1,3)(-1,3)(2,4)(-2,4)(3,5)(-3,5)(0,-1)(0,-2)(1,-3)(-1,-3)(2,-4)(-2,-4)(3,-5)(-3,-5)}",
				Fixtures.CREATE_DOUBLE_Y_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 6, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,2)}}{{(3,5)}}{{(-3,5)}}{{(0,-2)}}{{(3,-5)}}{{(-3,-5)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListSpikedHexagon() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
		Assert.assertEquals("island", "{(0,0)(0,1)(0,2)(1,3)(-1,3)(1,4)(-1,4)(-2,5)(0,5)(2,5)(-3,6)(3,6)(-4,7)(4,7)}",
				Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 6, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}{{(0,2)}}{{(1,4)}}{{(-1,4)}}{{(-4,7)}}{{(4,7)}}}", 
				nucleusList.toString());
	}

	@Test
	/**
	 * ++    ++
	 *   ++++
	 *    +
	 *    +
	 *    +
	 */
	public void testPixelNucleusListT() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_T_ISLAND());
		Assert.assertEquals("island", "{(-1,-1)(0,0)(1,0)(2,1)(3,1)(4,1)(5,1)(6,0)(7,0)(3,2)(3,3)(3,4)(3,5)}",
				Fixtures.CREATE_T_ISLAND().getPixelList().toString());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("nucleusList", 4, nucleusList.size());
		Assert.assertEquals("nucleusList: ", ""
				+ "{{{(-1,-1)}}"
				+ "{{(2,1)(3,1)(4,1)(3,2)}}"
				+ "{{(7,0)}}"
				+ "{{(3,5)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testSpikesDot() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOT_ISLAND());
		Assert.assertEquals("dot", "{}", nucleusFactory.createSpikePixelList().toString());
	}

	@Test
	public void testSpikesLine() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_LINE_ISLAND());
		Assert.assertEquals("line", "{(1,0)(0,3)}", nucleusFactory.createSpikePixelList().toString());
	}

	@Test
	public void testSpikesCycle() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_CYCLE_ISLAND());
		Assert.assertEquals("cycle", "{}", nucleusFactory.createSpikePixelList().toString());
	}

	@Test
	public void testSpikesY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_Y_ISLAND());
		Assert.assertEquals("Y", "{(0,1)(1,-1)(-1,-1)(0,2)(-2,-2)(2,-2)}", nucleusFactory.createSpikePixelList().toString());
	}

	@Test
	public void testSpikesDoubleY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOUBLE_Y_ISLAND());
		Assert.assertEquals("doubleY", "{(1,3)(-1,3)(0,1)(2,4)(-2,4)(0,-1)(1,-3)(-1,-3)(2,-4)(-2,-4)}", 
				nucleusFactory.createSpikePixelList().toString());
	}

	@Test
	public void testSpikesSpikedHexagon() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreateNucleusList();
		Assert.assertEquals("spikedHexagon", "{(0,1)(1,3)(-1,3)(0,1)(2,5)(0,5)(1,3)(0,5)(-2,5)(-1,3)(-3,6)(3,6)}", 
				nucleusFactory.createSpikePixelList().toString());
	}
	
	@Test
	public void testSpikesT() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_T_ISLAND());
		Assert.assertEquals("T", "{(0,0)(1,0)(5,1)(3,3)(6,0)(3,4)}",     // ?????
				nucleusFactory.createSpikePixelList().toString());
	}

	// -----------
	@Test
	public void testJoinSpikesDot() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_DOT_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesLine() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_LINE_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "{(2,0)(1,0)(0,1)(-1,2)(0,3)(0,4)}/[(2,0)(0,4)]", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesCycle() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_CYCLE_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesY() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_Y_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "{(0,0)(0,1)(0,2)(0,3)}/[(0,0)(0,3)]"
				+ "{(0,0)(-1,-1)(-2,-2)(-3,-3)}/[(0,0)(-3,-3)]"
				+ "{(0,0)(1,-1)(2,-2)(3,-3)}/[(0,0)(3,-3)]", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesDoubleY() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_DOUBLE_Y_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "{(0,-2)(0,-1)(0,0)(0,1)(0,2)}/[(0,-2)(0,2)]"
				+ "{(0,-2)(-1,-3)(-2,-4)(-3,-5)}/[(0,-2)(-3,-5)]"
				+ "{(0,-2)(1,-3)(2,-4)(3,-5)}/[(0,-2)(3,-5)]"
				+ "{(0,2)(-1,3)(-2,4)(-3,5)}/[(0,2)(-3,5)]"
				+ "{(0,2)(1,3)(2,4)(3,5)}/[(0,2)(3,5)]", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesSpikedHexagon() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "{(0,2)(0,1)(0,0)}/[(0,2)(0,0)]"
				+ "{(-1,4)(-1,3)(0,2)}/[(-1,4)(0,2)]"
				+ "{(1,4)(1,3)(0,2)}/[(1,4)(0,2)]"
				+ "{(1,4)(0,5)(-1,4)}/[(1,4)(-1,4)]"
				+ "{(4,7)(3,6)(2,5)(1,4)}/[(4,7)(1,4)]"
				+ "{(-4,7)(-3,6)(-2,5)(-1,4)}/[(-4,7)(-1,4)]", 
				factory.getEdgeList().toString());
	}

	@Test
	public void testJoinSpikesT() {
		PixelNucleusFactory factory = new PixelNucleusFactory(Fixtures.CREATE_T_ISLAND());
	    factory.createNodesAndEdges();
		Assert.assertEquals("edges", "{(3,5)(3,4)(3,3)(3,2)}/[(3,5)(3,1)]{(2,1)(1,0)(0,0)(-1,-1)}/[(3,1)(-1,-1)]{(7,0)(6,0)(5,1)(4,1)}/[(7,0)(3,1)]", 
				factory.getEdgeList().toString());
	}

	
	// ====================================
	
	private void debugSpikes(PixelNucleusList nucleusList) {
		for (int i = 0; i < nucleusList.size(); i++) {
			PixelNucleus nucleus = nucleusList.get(i);
			PixelList nucleusPixelList = nucleus.createSpikePixelList();
			LOG.trace(nucleusPixelList);
		}
		LOG.trace("========");
	}

	public void debugIterator(PixelSet spikeSet) {
		int maxCount = 1000000;
		PixelSet set = new PixelSet(spikeSet);
		while (!set.isEmpty() && maxCount-- > 0) {
			Pixel pixel = set.next();
			set.remove(pixel);
			LOG.debug("it> "+pixel);
		}
		LOG.debug(spikeSet.toString());
	}


}
