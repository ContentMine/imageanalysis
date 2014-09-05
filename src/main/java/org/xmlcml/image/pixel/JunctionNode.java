package org.xmlcml.image.pixel;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;

/** T-junctions in pixelIsland.
 * 
 * of form:
 * <code>
 *  2-1-3
 *  ..4
 *  </code>
 *  
 *  where 1 is exactly 3-connected and 4 is 3- or 4-connected and 2 and 3 are 2- or 3-connected
 *  
 *  This logic is probably obsolete
 *  
 * @author pm286
 *
 */
public class JunctionNode extends PixelNode {
	
	private final static Logger LOG = Logger.getLogger(JunctionNode.class);

	private enum Type {
		FOURPLUS, // four or more - may be reducible
		TJUNCTION, // centre pixel with 3 orthogonal neighbours
		UNKNOWN,
		YJUNCTION, // Y-shaped - three geometries
	}

	private Pixel stem; // pixel 4 of TJUNCTION
	PixelList neighbours;
	private Type type = Type.UNKNOWN;
	private boolean isDiagonalY;
	private boolean isOrthogonalY;
	
	public JunctionNode() {
		
	}

	public JunctionNode(Pixel centre, Pixel stem) {
		super(centre);
		this.stem  = stem;
	}

	/** creates a Junction from a centre pixel.
	 * 
	 * (needs redoing if additional diagonal neighbours)
	 * @param centre
	 * @return null if not a Junction
	 */
	public static JunctionNode createJunction(Pixel centre, PixelIsland island) {
		JunctionNode junction = null;
		if (centre != null) {
			PixelList neighbours = new PixelList(centre.getOrCreateNeighbours(island));
			if (neighbours.size() == 3) {
				Pixel stem = JunctionNode.getStem(centre, neighbours, island);
				junction = new JunctionNode(centre, stem);
				if (stem != null) {
					neighbours.remove(stem);
					junction.setType(Type.TJUNCTION);
				} else {
					junction.setType(Type.YJUNCTION);
				}
			} else if (neighbours.size() > 3) {
				junction = new JunctionNode(centre, null);
				junction.setType(Type.FOURPLUS);
			}
			if (junction != null) {
				junction.setNeighbours(neighbours);
			}
		}
		return junction;
	}

	private void setNeighbours(PixelList neighbours) {
		this.neighbours = new PixelList();
		this.neighbours.addAll(neighbours);
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

	public Pixel getStem() {
		return stem;
	}
	
	public PixelList getNeighbours() {
		return neighbours;
	}
	
	public static void drawJunctions(JunctionSet junctionSet, SVGG g, double size) {
		for (PixelNode junction : junctionSet) {
			LOG.trace("DrawJunctions");
			Pixel centrePixel = junction.getCentrePixel();
			SVGCircle circle = new SVGCircle(new Real2(centrePixel.getInt2()).plus(new Real2(0.5, 0.5)), size);
			if (((JunctionNode)junction).isYJunction()) {
				LOG.debug("ISY");
				circle.setStrokeWidth(1.);
			}
			circle.setOpacity(0.2);
			circle.setFill("yellow");
			g.appendChild(circle);
		}
	}

	public PixelList getNonJunctionPixels() {
		PixelList nonJunctionPixelList = new PixelList();
		for (Pixel neighbour : neighbours) {
			nonJunctionPixelList.add(neighbour);
		}
		return nonJunctionPixelList;
	}

	boolean processDiagonal(Pixel diagonalPixel, PixelList orthogonalPixels) {
		isDiagonalY = false;
		if (orthogonalPixels.size() == 2 
				&& !diagonalPixel.isOrthogonalNeighbour(orthogonalPixels.get(0)) 
				&& !diagonalPixel.isOrthogonalNeighbour(orthogonalPixels.get(1))) {
				isDiagonalY = true;
			}
		return isDiagonalY;
	}

	boolean processOrthogonal(Pixel orthogonalPixel, PixelList diagonalPixels) {
		isOrthogonalY = false;
		if (diagonalPixels.size() == 2) {
			Pixel diag0 = diagonalPixels.get(0);
			Pixel diag1 = diagonalPixels.get(1);
			Int2 delta = diag0.getInt2().subtract(diag1.getInt2());
			if ((Math.abs(delta.getX()) == 2 && Math.abs(delta.getY()) == 0) ||
					(Math.abs(delta.getX()) == 0 || Math.abs(delta.getY()) == 2)) {
				if (orthogonalPixel.isKnightsMove(diag0) 
				    && orthogonalPixel.isKnightsMove(diag1)) {
					isOrthogonalY = true;
				}
			} else {
				LOG.debug("bad diag");
			}
		}
		return isOrthogonalY;
	}

	public boolean isYJunction() {
		return isDiagonalY || isOrthogonalY;
	}
}
