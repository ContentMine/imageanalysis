package org.xmlcml.image.processing;

import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;
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
public class Junction extends PixelNode {
	
	private final static Logger LOG = Logger.getLogger(Junction.class);

	private enum Type {
		FOURPLUS, // four or more - may be reducible
		TJUNCTION, // centre pixel with 3 orthogonal neighbours
		UNKNOWN,
		YJUNCTION, // Y-shaped - three geometries
	}

	private Pixel centre; // pixel 1
	private Pixel stem; // pixel 4 of TJUNCTION
	private PixelList neighbours;
	private Type type = Type.UNKNOWN;
	
	public Junction() {
		
	}

	public Junction(Pixel centre, Pixel stem) {
		this.centre = centre;
		this.stem  = stem;
	}

	/** creates a Junction from a centre pixel.
	 * 
	 * (needs redoing if additional diagonal neighbours)
	 * @param centre
	 * @return null if not a Junction
	 */
	public static Junction createJunction(Pixel centre, PixelIsland island) {
		Junction junction = null;
		if (centre != null) {
			PixelList neighbours = new PixelList(centre.getNeighbours(island));
			if (neighbours.size() == 3) {
				Pixel stem = Junction.getStem(centre, neighbours, island);
				junction = new Junction(centre, stem);
				if (stem != null) {
					neighbours.remove(stem);
					junction.setType(Junction.Type.TJUNCTION);
				} else {
					junction.setType(Junction.Type.YJUNCTION);
				}
			} else if (neighbours.size() > 3) {
				junction = new Junction(centre, null);
				junction.setType(Junction.Type.FOURPLUS);
			}
		}
		return junction;
	}

	private void setType(Type type) {
		this.type  = type;
	}

	private static Pixel getStem(Pixel centre, PixelList neighbours, PixelIsland island) {
		if (getStem(neighbours.get(0), 0, neighbours, island)) return neighbours.get(0);
		if (getStem(neighbours.get(1), 1, neighbours, island)) return neighbours.get(1);
		if (getStem(neighbours.get(2), 2, neighbours, island)) return neighbours.get(2);
		return null;
	}

	private static boolean getStem(Pixel stem, int i, PixelList neighbours, PixelIsland island) {
		int j = (i+1) % 3;
		int k = (i+2) % 3;
		Pixel pj = neighbours.get(j);
		Pixel pk = neighbours.get(k);
//		PixelList stemNeighbours = stem.getNeighbours(island);
//		return stemNeighbours.contains(p1) && stemNeighbours.contains(p2);
		return stem.isDiagonalNeighbour(pk) && stem.isDiagonalNeighbour(pj);
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
	
	public static void drawJunctions(Set<Junction> tJunctionSet, SVGG g) {
		for (Junction tJunction : tJunctionSet) {
			SVGCircle circle = new SVGCircle(new Real2(tJunction.getCentre().getInt2()).plus(new Real2(0.5, 0.5)), 3.);
			circle.setOpacity(0.4);
			circle.setFill("blue");
			g.appendChild(circle);
		}
	}
	

}
