package org.xmlcml.image.lines;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.image.processing.Pixel;
import org.xmlcml.image.processing.PixelIsland;

/** a non-branching path of pixels.
 * 
 * Either a curve with two ends or a closed path
 * 
 * @author pm286
 *
 */
public class PixelPath {

	private Object island;
	private List<Real2> points;
	private List<Pixel> pixelList;

	public PixelPath() {
		pixelList = new ArrayList<Pixel>();
	}
	
	/** reads a PixelIsland with only one line.
	 * temporary. Will be generated from the island.
	 * 
	 * throws error if more than two termini (i.e. branches)
	 * @param pixelIsland
	 */
	void readPixels(PixelIsland island) {
		this.island = island;
		if (island.getTerminalPixels().size() == 0) {
			createCycle();
		} else if (island.getTerminalPixels().size() == 2) {
			createLine();
		} else {
			this.island = null;
			throw new RuntimeException("Must be cycle or line: no branches");
		}
	}

	private void createLine() {
		throw new RuntimeException("NYI");
	}

	private void createCycle() {
		throw new RuntimeException("NYI");
	}

	public void add(Pixel pixel) {
		pixelList.add(pixel);
	}

	public Pixel getFirstPixel() {
		return pixelList.size() == 0 ? null : pixelList.get(0);
	}

	public Pixel getLastPixel() {
		return pixelList.size() == 0 ? null : pixelList.get(pixelList.size() - 1);
	}
	
	public List<Real2> getPoints() {
		if (points == null) {
			points = new ArrayList<Real2>();
			for (Pixel pixel : pixelList) {
				Int2 int2 = pixel.getInt2();
				points.add(new Real2(int2.getX(), int2.getY()));
			}
		}
		return points;
	}

	
}
