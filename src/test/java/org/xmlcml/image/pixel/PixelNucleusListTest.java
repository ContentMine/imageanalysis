package org.xmlcml.image.pixel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class PixelNucleusListTest {
	
	private final static Logger LOG = Logger.getLogger(PixelNucleusListTest.class);

	@Test
	public void testPixelNucleusListDot() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOT_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 1, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListLine() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_LINE_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 2, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(1,0)}}{{(0,3)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListCycle() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_CYCLE_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 0, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_Y_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 4, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}{{(0,3)}}{{(-3,-3)}}{{(3,-3)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListDoubleY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOUBLE_Y_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 6, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,2)}}{{(3,5)}}{{(-3,5)}}{{(0,-2)}}{{(3,-5)}}{{(-3,-5)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testPixelNucleusListSpikedHexagon() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		Assert.assertEquals("nucleusList", 6, nucleusList.size());
		Assert.assertEquals("nucleusList: ", "{{{(0,0)}}{{(0,2)}}{{(1,4)}}{{(-1,4)}}{{(-4,7)}}{{(4,7)}}}", 
				nucleusList.toString());
	}

	@Test
	public void testSpikesDot() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOT_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
	}

	@Test
	public void testSpikesLine() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_LINE_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
	}

	@Test
	public void testSpikesCycle() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_CYCLE_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
	}

	@Test
	public void testSpikesY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_Y_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
		for (int i = 0; i < nucleusList.size(); i++) {
			PixelNucleus nucleus = nucleusList.get(i);
			PixelList pixelList = nucleus.createSpikePixelList();
			LOG.debug("nucleus "+i+"; "+nucleus+"; "+pixelList);
		}
	}

	@Test
	public void testSpikesDoubleY() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_DOUBLE_Y_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
	}

	@Test
	public void testSpikesSpikedHexagon() {
		PixelNucleusFactory nucleusFactory = new PixelNucleusFactory(Fixtures.CREATE_TRISPIKED_HEXAGON_ISLAND());
		PixelNucleusList nucleusList = nucleusFactory.getOrCreatePixelNucleusList();
	}


}
