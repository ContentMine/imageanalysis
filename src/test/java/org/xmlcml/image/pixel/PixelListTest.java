package org.xmlcml.image.pixel;

import org.junit.Assert;
import org.junit.Test;

public class PixelListTest {

	@Test
	public void testRemoveMinorIslands() {
		PixelList pixelList = new PixelList();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				pixelList.add(new Pixel(i, j));
			}
		}
		pixelList.add(new Pixel(10, 10));
		
		pixelList.add(new Pixel(20, 20));
		pixelList.add(new Pixel(20, 21));
		
		pixelList.add(new Pixel(30, 30));
		pixelList.add(new Pixel(30, 31));
		pixelList.add(new Pixel(30, 32));
		
		PixelIsland island = new PixelIsland(pixelList);
		
		Assert.assertEquals("pixels", 22, pixelList.size());
		pixelList.removeMinorIslands(1);
		Assert.assertEquals("pixels", 21, pixelList.size());
		pixelList.removeMinorIslands(2);
		Assert.assertEquals("pixels", 19, pixelList.size());
		pixelList.removeMinorIslands(3);
		Assert.assertEquals("pixels", 16, pixelList.size());
		
	}


}
