package org.xmlcml.image.processing;

import org.apache.log4j.Logger;
import org.xmlcml.image.compound.PixelList;

/** T-junctions in pixelIsland.
 * 
 * of form:
 * <code>
 *  2-1-3
 *  ..4
 *  </code>
 *  
 *  where 1 is exactly 3-connected and 4 is 3- or 4- connected and 1 and 3 are 2- or 3-connected
 * @author pm286
 *
 */
public class TJunction {
	
	private final static Logger LOG = Logger.getLogger(TJunction.class);

	private Pixel centre; // pixel 1
	private Pixel stem; // pixel 4
	private PixelList neighbours;
	
	public TJunction() {
		
	}

	public TJunction(Pixel centre, Pixel stem) {
		this.centre = centre;
		this.stem  = stem;
	}

	/** creates a TJunction from a centre pixel.
	 * 
	 * (needs redoing if additional diagonal neighbours)
	 * @param centre
	 * @return null if not a TJunction
	 */
	public static TJunction createTJunction(Pixel centre, PixelIsland island) {
		TJunction junction = null;
		if (centre != null) {
			PixelList neighbours = new PixelList(centre.getNeighbours(island));
			if (neighbours.size() == 3) {
				Pixel stem = TJunction.getStem(centre, neighbours, island);
				if (stem != null) {
					neighbours.remove(stem);
					junction = new TJunction(centre, stem);
				}
			}
		}
		return junction;
	}

	private static Pixel getStem(Pixel centre, PixelList neighbours, PixelIsland island) {
		if (getStem(neighbours.get(0), 0, neighbours, island)) return neighbours.get(0);
		if (getStem(neighbours.get(1), 1, neighbours, island)) return neighbours.get(1);
		if (getStem(neighbours.get(2), 2, neighbours, island)) return neighbours.get(2);
		return null;
	}

	private static boolean getStem(Pixel stem, int i, PixelList neighbours, PixelIsland island) {
		int n1 = (i+1) % 3;
		int n2 = (i+2) % 3;
		Pixel p1 = neighbours.get(n1);
		Pixel p2 = neighbours.get(n2);
		PixelList stemNeighbours = stem.getNeighbours(island);
		return stemNeighbours.contains(p1) && stemNeighbours.contains(p2);
	}

	public Pixel getCentre() {
		return centre;
	}

	public Pixel getStem() {
		return stem;
	}
	
	public PixelList getNeighbours() {
		return neighbours;
	}
}
