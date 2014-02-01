package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


/** a group of 3- or more- connected pixels.
 * 
 * @author pm286
 *
 */
public class Nucleus {
	
	private final static Logger LOG = Logger.getLogger(Nucleus.class);

	private Set<Pixel> pixelSet;
	private Set<Pixel> spikeSet;
	private PixelIsland island;
	private Set<Pixel> thinnedSet;
	private List<Pixel> centrePixelList;
	private Pixel centre;
	private List<Shell> shellList;
	
	public Nucleus(PixelIsland island) {
		this.island = island;
		pixelSet = new HashSet<Pixel>();
	}

	public void add(Pixel pixel) {
		pixelSet.add(pixel);
	}

	public int size() {
		return pixelSet.size();
	}
	
	public boolean contains(Pixel pixel) {
		return pixelSet.contains(pixel);
	}
	
	/** set of "spikes" from nucleus.
	 * 
	 * these are pixels with with exactly 2 neighbours which are attached to nucleus.
	 * They are likely to be the start of the edges between nuclei (nodes)
	 * @return
	 */
	public Set<Pixel> getSpikeSet() {
		spikeSet = new HashSet<Pixel>();
		Iterator<Pixel> iterator = pixelSet.iterator();
		while (iterator.hasNext()) {
			Pixel pixel = iterator.next();
			List<Pixel> neighbourList = pixel.getNeighbours(island);
			for (Pixel neighbour : neighbourList) {
				if (neighbour.getNeighbours(island).size() == 2) {
					spikeSet.add(neighbour);
				}
			}
		}
		return spikeSet;
	}
	
	/** returns iteration required to create overlapping shells.
	 * 
	 * @param maxIterations
	 * @return
	 */
	public int ensureFlattened(int maxIterations) {
		int iteration = 0;
		getSpikeSet();
		if (spikeSet.size() != pixelSet.size() && spikeSet.size() > 0) {
			shellList = createShellListFromSpikeSet();
			boolean anded = false;
			for (; iteration < maxIterations; iteration++) {
				for (Shell shell : shellList) {
					shell.expandOnePixelFromCurrent();
				}
				
				thinnedSet = new HashSet<Pixel>(shellList.get(0).getExpandedSet());
				// remove shells 1... from 0
				for (int j = 1; j < shellList.size(); j++) {
					Shell shell = shellList.get(j);
					thinnedSet.retainAll(shell.getExpandedSet());
					if (thinnedSet.size() != 0) {
						LOG.debug("all shells overlap on iteration "+iteration+ "; size "+thinnedSet.size()+" spikes "+spikeSet.size());
						extractCentre();
						anded = true;
						break;
					}
				}
				if (anded) break;
			}
			if (!anded) {
				LOG.error("failed to AND all spikesets");
			}
		}
		return iteration;
	}

	private void extractCentre() {
		centre = null;
		centrePixelList = new ArrayList<Pixel>(thinnedSet);
		if (centrePixelList.size() == 1) {
			// probably a terminal
			if (shellList.size() == 1) {
				centre = centrePixelList.get(0);
			} else {
				throw new RuntimeException("cannot interpret single pixel centre");
			}
		} else if (centrePixelList.size() == 2) {
			if (shellList.size() == 2) {
				if (areNeighbours(0, 1)) {
					centre = centrePixelList.get(0);
				} else {
					throw new RuntimeException("two thinned pixels are not neighbours");
				}
			}
		} else if (centrePixelList.size() == 3) {
			if (shellList.size() == 3) {
				if (areNeighbours(0, 1) && areNeighbours(0, 2) && areNeighbours(1,2)) {
					centre = centrePixelList.get(0); // as good as any
				} else {
					throw new RuntimeException("3 thinned pixels are not a triangle");
				}
			} else {
				throw new RuntimeException("3 thinned pixels from shellList "+shellList.size());
			}
		} else if (centrePixelList.size() == 4) {
			if (shellList.size() == 2) {
				// * *
				//   * *   pattern
				centrePixelList.remove(shellList.get(0));
				centrePixelList.remove(shellList.get(1));
				// remove one of remaining actual pixels
				centrePixelList.get(0).remove();
				LOG.debug("thinned centre: "+centrePixelList.size()+" too complex, taking first");
				centre = centrePixelList.get(0); // as good as any
			} else if (shellList.size() == 3) {
				centrePixelList.remove(shellList.get(0));
				centrePixelList.remove(shellList.get(1));
				centrePixelList.remove(shellList.get(3));
				// remove one of remaining actual pixels
				centrePixelList.get(0).remove();
				throw new RuntimeException("3 thinned pixels from shellList "+shellList.size());
			} else if (shellList.size() == 4) {
				centre = centrePixelList.get(0); // as good as any
			} else {
				throw new RuntimeException("cannot interpret 4 nucleus");
			}
		} else if (centrePixelList.size() == 4) {
		}
		int size = centrePixelList.size() ;
		for (int i = 0; i < size - 1; i++) {
			Pixel pixeli = centrePixelList.get(i);
			for (int j = i + 1; j < size; j++) {
				Pixel pixelj = centrePixelList.get(j);
				System.out.println(i + " " + j + " "+pixeli.getInt2()+"; "+pixelj.getInt2()+" "+areNeighbours(i, j));
			}
		}

	}

	boolean areNeighbours(int i, int j) {
		return centrePixelList.get(i).getNeighbours(island).contains(centrePixelList.get(j));
	}

	private List<Shell> createShellListFromSpikeSet() {
		List<Shell> shellList = new ArrayList<Shell>(); 
		for (Pixel pixel : spikeSet) {
			Shell shell = new Shell(pixel, island);
			shellList.add(shell);
		}
		return shellList;
	}
}
