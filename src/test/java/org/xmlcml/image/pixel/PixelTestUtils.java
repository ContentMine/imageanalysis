package org.xmlcml.image.pixel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.xmlcml.euclid.IntArray;

public class PixelTestUtils {
	
	private static Logger LOG = Logger.getLogger(PixelTestUtils.class);

	public static void assertNeighbourCounts(PixelIslandList pixelIslandList, IntArray[] neighbourCounts) {
		int i = 0;
		for (PixelIsland island : pixelIslandList) {
			if (i >= neighbourCounts.length) break;
			PixelTestUtils.assertNeighbourCounts("island_"+i, island, neighbourCounts[i]);
			i++;
		}
		
	}

	public static void assertNodeCounts(PixelIslandList pixelIslandList, IntArray[] nodeCounts) {
		int i = 0;
		for (PixelIsland island : pixelIslandList) {
			if (i >= nodeCounts.length) break;
			PixelTestUtils.assertNodeCounts("island_"+i, island, nodeCounts[i++]);
		}
		
	}

	/** compares neighbours against expected.

	 * first element is pixel count.
	 * @param msg
	 * @param island
	 * @param neighbourCounts 1st element is 0-neighbour count, 2nd is 1-neighbour
	 */
	public static void assertNeighbourCounts(String msg, PixelIsland island, IntArray neighbourCounts) {
		// compare pixel counts
		IntArray neighbourCountArray = new IntArray();
		for (int i = 1; i < neighbourCounts.size(); i++) {
			int neighbourCount = i - 1;
			PixelList pixelsWithNNeighbours = island.getPixelsWithNeighbourCount(neighbourCount);
			if (neighbourCount == 4) {
				debugPixelsWithNeighbourCount(island, neighbourCount);
			}
			neighbourCountArray.addElement(pixelsWithNNeighbours.size());
		}
		String msg0 = neighbourCountArray.toString();
		Assert.assertEquals(msg0+"; "+msg+"; pixels", neighbourCounts.elementAt(0),island.size());
		// compare neighbour counts
		for (int i = 1; i < neighbourCounts.size(); i++) {
			int neighbours = i - 1;
			int expected = neighbourCounts.elementAt(i);
			if (expected >= 0) {
				Assert.assertEquals(msg0+"; "+msg+"; neighbours_"+neighbours, expected, 
						neighbourCountArray.elementAt(neighbours));
			}
		}
		
	}

	public static void debugPixelsWithNeighbourCount(PixelIsland island, int neighbourCount) {
		PixelList pixelsWithNNeighbours = island.getPixelsWithNeighbourCount(neighbourCount);
		LOG.trace("Pixels with "+neighbourCount+" neighbours; "+pixelsWithNNeighbours);
		for (Pixel pixelWithNNeighbours : pixelsWithNNeighbours) {
			LOG.trace("..."+pixelWithNNeighbours.getOrCreateNeighbours(island));
		}
	}


	/** compares nodes against expected.

	 * @param msg
	 * @param island
	 * @param nodeCounts 0th eelement is 0-brnach node count, 1st element is 1-branch ...
	 */
	public static void assertNodeCounts(String msg, PixelIsland island, IntArray nodeCounts) {
		// compare pixel counts
//		IntArray neighbourCountArray = new IntArray();
//		for (int i = 1; i < nodeCounts.size(); i++) {
//			int neighbours = i - 1;
//			neighbourCountArray.addElement(island.getPixelsWithNeighbours(neighbours).size());
//		}
//		String msg0 = neighbourCountArray.toString();
//		Assert.assertEquals(msg0+"; "+msg+"; pixels", nodeCounts.elementAt(0),island.size());
//		// compare neighbour counts
//		for (int i = 1; i < nodeCounts.size(); i++) {
//			int neighbours = i - 1;
//			int expected = nodeCounts.elementAt(i);
//			if (expected >= 0) {
//				Assert.assertEquals(msg0+"; "+msg+"; neighbours_"+neighbours, expected, 
//						neighbourCountArray.elementAt(neighbours));
//			}
//		}
		
	}


}
