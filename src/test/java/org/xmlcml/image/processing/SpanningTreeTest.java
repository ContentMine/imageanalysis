package org.xmlcml.image.processing;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.compound.PixelList;

/** not yet very active.
 * 
 * probably obsoleteed by ConnectionTable.
 * 
 * @author pm286
 *
 */
public class SpanningTreeTest {

	public final static Logger LOG = Logger.getLogger(SpanningTreeTest.class);

	@Test
	@Ignore
	public void testLineEnd() {
		PixelList lineList = Fixtures.LINE_LIST;
		PixelIsland island = new PixelIsland(lineList);
		Pixel startPixel = lineList.get(0);
		SpanningTree spanningTree = island.createSpanningTree(startPixel);
		Assert.assertNotNull("spanningTree", spanningTree);
	}
	
	@Test
	@Ignore
	public void testTList() {
		PixelList lineList = Fixtures.T_LIST;
		PixelIsland island = new PixelIsland(lineList);
		Pixel startPixel = lineList.get(0);
		SpanningTree spanningTree = island.createSpanningTree(startPixel);
		Assert.assertNotNull("spanningTree", spanningTree);
	}

}
