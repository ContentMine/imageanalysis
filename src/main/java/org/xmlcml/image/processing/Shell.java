package org.xmlcml.image.processing;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/** coordination shellm radiating out from pixel.
 * 
 * @author pm286
 *
 */
public class Shell {

	private final static Logger LOG = Logger.getLogger(Shell.class);
	private Pixel pixel;
	private Set<Pixel> expandedShell;
	private PixelIsland island;

	public Shell(Pixel pixel, PixelIsland island) {
		this.pixel = pixel;
		this.island = island;
		expandedShell = new HashSet<Pixel>();
		expandedShell.add(pixel);
	}

	public void expandOnePixelFromCurrent() {
		PixelList shellPixelList = new PixelList(expandedShell);
		for (Pixel pixel : shellPixelList) {
			PixelList neightbourList = pixel.getNeighbours(island);
			for (Pixel neighbour  : neightbourList) {
				expandedShell.add(neighbour);
			}
			LOG.trace("expanded to "+expandedShell.size());
		}
	}

	public Set<Pixel> getExpandedSet() {
		return expandedShell;
	}
}
