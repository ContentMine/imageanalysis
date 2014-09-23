package org.xmlcml.image.pixel;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.ImageProcessor;

/** useful examples which act as demo and regression tests.
 * 
 * @author pm286
 *
 */
public class ExamplesTest {

	private ImageProcessor DEFAULT_PROCESSOR;

	private final static Logger LOG = Logger.getLogger(ExamplesTest.class);
	
	@Before
	public void setup() {
		DEFAULT_PROCESSOR = new ImageProcessor();
	}

	/** Skeleton of a molecular structure diagram (without element symbols)
	 * 
	 * one large island representing 12 nodes and 12 edges.
	 * 
	 * then 4 smaller islands which are straight lines defining double bonds
	 * 
	 */
	@Test
	public void testMaltoryzineEdges() {
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(Fixtures.MALTORYZINE_BINARY_PNG);
		PixelIslandList pixelIslandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		SVGSVG.wrapAndWriteAsSVG(pixelIslandList.getOrCreateSVGG(), new File("target/examples/maltoryzine.svg"));
		pixelIslandList.sortBySizeDescending();
		PixelIsland largest = pixelIslandList.get(0);
		SVGSVG.wrapAndWriteAsSVG(largest.getOrCreateSVGG(), new File("target/examples/maltoryzineLargest.svg"));
		// largest island is 12-node, 12-edge framework
		PixelNucleusFactory factory = new PixelNucleusFactory(pixelIslandList.get(0));
		Assert.assertEquals("nuclei", ""
				+ "[{(45,34)}{(106,34)}{(98,124)}{(113,34)}{(45,154)}{(238,57)}{(45,130)(45,131)(46,130)(44,130)}{(78,112)(77,111)(77,112)}{(45,56)(46,56)(45,55)}{(113,56)(112,57)(113,57)}{(78,74)(77,74)(77,75)(76,74)}{(106,58)(107,57)(106,56)(106,57)}]",
				factory.getOrCreateNucleusList().toString());
		factory.createNodesAndEdges();
		// 12 edges between nodes. Some have kinks
		Assert.assertEquals("edges", ""
				+ "(106,56)(106,55)(106,54)(106,53)(106,52)(106,51)(106,50)(106,49)(106,48)(106,47)(106,46)(106,45)(106,44)(106,43)(106,42)(106,41)(106,40)(106,39)(106,38)(106,37)(106,36)(106,35)(106,34)/[<(106,57)><(106,34)>](113,57)(114,58)(115,59)(116,59)(117,60)(118,61)(119,61)(120,62)(121,62)(122,63)(123,63)(124,64)(125,65)(126,65)(127,66)(128,66)(129,67)(130,68)(131,68)(132,69)(133,69)(134,70)(135,70)(136,71)(137,72)(138,72)(139,73)(140,73)(141,74)(142,74)(143,74)(144,74)(145,73)(146,73)(147,72)(148,71)(149,71)(150,70)(151,70)(152,69)(153,68)(154,68)(155,67)(156,67)(157,66)(158,66)(159,65)(160,64)(161,64)(162,63)(163,63)(164,62)(165,61)(166,61)(167,60)(168,60)(169,59)(170,59)(171,58)(172,57)(173,57)(174,56)(175,56)(176,57)(177,57)(178,58)(179,58)(180,59)(181,60)(182,60)(183,61)(184,61)(185,62)(186,63)(187,63)(188,64)(189,64)(190,65)(191,65)(192,66)(193,67)(194,67)(195,68)(196,68)(197,69)(198,69)(199,70)(200,71)(201,71)(202,72)(203,72)(204,73)(205,74)(206,74)(207,74)(208,74)(209,73)(210,73)(211,72)(212,72)(213,71)(214,70)(215,70)(216,69)(217,69)(218,68)(219,68)(220,67)(221,66)(222,66)(223,65)(224,65)(225,64)(226,64)(227,63)(228,62)(229,62)(230,61)(231,61)(232,60)(233,59)(234,59)(235,58)(236,58)(237,57)(238,57)/[<(113,57)><(238,57)>](46,130)(47,130)(48,129)(49,129)(50,128)(51,127)(52,127)(53,126)(54,126)(55,125)(56,125)(57,124)(58,123)(59,123)(60,122)(61,122)(62,121)(63,121)(64,120)(65,119)(66,119)(67,118)(68,118)(69,117)(70,117)(71,116)(72,115)(73,115)(74,114)(75,114)(76,113)(77,112)/[<(45,130)><(77,112)>](78,74)(79,74)(80,73)(81,73)(82,72)(83,72)(84,71)(85,70)(86,70)(87,69)(88,69)(89,68)(90,68)(91,67)(92,66)(93,66)(94,65)(95,65)(96,64)(97,63)(98,63)(99,62)(100,62)(101,61)(102,61)(103,60)(104,59)(105,59)(106,58)/[<(77,74)><(106,57)>](98,124)(97,123)(96,123)(95,122)(94,122)(93,121)(92,120)(91,120)(90,119)(89,119)(88,118)(87,117)(86,117)(85,116)(84,116)(83,115)(82,115)(81,114)(80,113)(79,113)(78,112)/[<(98,124)><(77,112)>](77,111)(77,110)(77,109)(77,108)(77,107)(77,106)(77,105)(77,104)(77,103)(77,102)(77,101)(77,100)(77,99)(77,98)(77,97)(77,96)(77,95)(77,94)(77,93)(77,92)(77,91)(77,90)(77,89)(77,88)(77,87)(77,86)(77,85)(77,84)(77,83)(77,82)(77,81)(77,80)(77,79)(77,78)(77,77)(77,76)(77,75)/[<(77,112)><(77,74)>](107,57)(108,57)(109,57)(110,56)(111,57)(112,57)/[<(106,57)><(113,57)>](44,130)(43,129)(42,129)(41,128)(40,128)(39,127)(38,126)(37,126)(36,125)(35,125)(34,124)(33,124)(32,123)(31,122)(30,122)(29,121)(28,121)(27,120)(26,120)(25,119)(24,118)(23,118)(22,117)(21,117)(20,116)(19,116)(18,115)(17,114)(16,114)(15,113)(14,113)(13,112)(13,111)(13,110)(13,109)(13,108)(13,107)(13,106)(13,105)(13,104)(13,103)(13,102)(13,101)(13,100)(13,99)(13,98)(13,97)(13,96)(13,95)(13,94)(13,93)(13,92)(13,91)(13,90)(13,89)(13,88)(13,87)(13,86)(13,85)(13,84)(13,83)(13,82)(13,81)(13,80)(13,79)(13,78)(13,77)(13,76)(13,75)(14,74)(15,74)(16,73)(17,72)(18,72)(19,71)(20,71)(21,70)(22,69)(23,69)(24,68)(25,68)(26,67)(27,66)(28,66)(29,65)(30,65)(31,64)(32,64)(33,63)(34,62)(35,62)(36,61)(37,61)(38,60)(39,60)(40,59)(41,58)(42,58)(43,57)(44,57)(45,56)/[<(45,130)><(45,56)>](113,56)(113,55)(113,54)(113,53)(113,52)(113,51)(113,50)(113,49)(113,48)(113,47)(113,46)(113,45)(113,44)(113,43)(113,42)(113,41)(113,40)(113,39)(113,38)(113,37)(113,36)(113,35)(113,34)/[<(113,57)><(113,34)>](45,34)(45,35)(45,36)(45,37)(45,38)(45,39)(45,40)(45,41)(45,42)(45,43)(45,44)(45,45)(45,46)(45,47)(45,48)(45,49)(45,50)(45,51)(45,52)(45,53)(45,54)(45,55)/[<(45,34)><(45,56)>](45,154)(45,153)(45,152)(45,151)(45,150)(45,149)(45,148)(45,147)(45,146)(45,145)(45,144)(45,143)(45,142)(45,141)(45,140)(45,139)(45,138)(45,137)(45,136)(45,135)(45,134)(45,133)(45,132)(45,131)/[<(45,154)><(45,130)>](46,56)(47,57)(48,57)(49,58)(50,59)(51,59)(52,60)(53,60)(54,61)(55,62)(56,62)(57,63)(58,63)(59,64)(60,64)(61,65)(62,66)(63,66)(64,67)(65,67)(66,68)(67,68)(68,69)(69,70)(70,70)(71,71)(72,71)(73,72)(74,73)(75,73)(76,74)/[<(45,56)><(77,74)>]",
				factory.getEdgeList().toString());

		// edges without nodes
		String[] edges = {
				"",  // tested already
				"(20,79)(19,80)(19,81)(19,82)(19,83)(19,84)(19,85)(19,86)(19,87)(19,88)(19,89)(19,90)(19,91)(19,92)(19,93)(19,94)(19,95)(19,96)(19,97)(19,98)(19,99)(19,100)(19,101)(19,102)(19,103)(19,104)(19,105)(19,106)(19,107)/[<(20,79)><(19,107)>]",
				"(206,66)(205,66)(204,65)(203,65)(202,64)(201,64)(200,63)(199,62)(198,62)(197,61)(196,61)(195,60)(194,60)(193,59)(192,58)(191,58)(190,57)(189,57)(188,56)(187,55)(186,55)(185,54)(184,54)(183,53)(182,53)(181,52)(180,51)(179,51)/[<(206,66)><(179,51)>]",
				"(46,123)(47,122)(48,121)(49,121)(50,120)(51,120)(52,119)(53,119)(54,118)(55,117)(56,117)(57,116)(58,116)(59,115)(60,115)(61,114)(62,113)(63,113)(64,112)(65,112)(66,111)(67,111)(68,110)(69,109)(70,109)/[<(46,123)><(70,109)>]",
				"(46,64)(47,65)(48,65)(49,66)(50,66)(51,67)(52,68)(53,68)(54,69)(55,69)(56,70)(57,70)(58,71)(59,72)(60,72)(61,73)(62,73)(63,74)(64,74)(65,75)(66,76)(67,76)(68,77)(69,77)(70,78)/[<(46,64)><(70,78)>]",
				};
		for (int i = 1; i < pixelIslandList.size(); i++) {
			PixelIsland island = pixelIslandList.get(i);
			SVGSVG.wrapAndWriteAsSVG(largest.getOrCreateSVGG(), new File("target/examples/maltoryzineIsland"+i+".svg"));
			factory = new PixelNucleusFactory(island);
		    factory.createNodesAndEdges();
			Assert.assertEquals("edges "+i, edges[i], factory.getEdgeList().toString());
		}
	}
	

	@Test
	/** extracts SVGLines from pixels of molecule.
	 * 
	 */
	public void testExtractMaltoryzineSVGLines() {
		double tolerance = 2.0; //this takes care of the bends
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(Fixtures.MALTORYZINE_BINARY_PNG);
		PixelIslandList islandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		islandList.sortBySizeDescending();
		SVGG svgg = new SVGG();
		for (PixelIsland island : islandList) {
			PixelNucleusFactory factory = new PixelNucleusFactory(island);
			PixelEdgeList edgeList = factory.getEdgeList();
			for (PixelEdge edge : edgeList) {
				PixelSegmentList segmentList = PixelSegmentList.createSegmentList(edge.getPixelList(), tolerance);
				SVGG g = segmentList.getSVGG();
				svgg.appendChild(g);
			}
		}
		SVGSVG.wrapAndWriteAsSVG(svgg, new File("target/examples/maltoryzine.svg"));
	}
	
	/** simple phylotree. Here we are just counting nodes and edges.
	 * 
	 */
	@Test
	public void testExtractPhyloTree() {
		BufferedImage image = DEFAULT_PROCESSOR.processImageFile(Fixtures.PHYLO_14811_2_PNG);
		PixelIslandList islandList = PixelIslandList.createSuperThinnedPixelIslandList(image);
		islandList.sortBySizeDescending();
		PixelNucleusFactory factory = new PixelNucleusFactory(islandList.get(0));
		PixelEdgeList edgeList = factory.getEdgeList();
		Assert.assertEquals("edges", 17, edgeList.size());
		Assert.assertEquals("nodes", 18, factory.getOrCreateNodeListFromNuclei().size());
		SVGSVG.wrapAndWriteAsSVG(edgeList.getOrCreateSVG(), new File("target/phylo/14811_2.svg"));
	}
	
}
