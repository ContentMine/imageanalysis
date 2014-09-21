package org.xmlcml.image.pixel;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;

/**
 * connected Nodes.
 * 
 * @author pm286
 * 
 */
public class PixelNucleus {

	private static Logger LOG = Logger.getLogger(PixelNucleus.class);

	public enum PixelJunctionType {
		CROSS,
		DOUBLEY,
		FILLEDT,
		NICKEDT,
		TERMINAL,
		Y,
	}
	private PixelIsland island;
	private Real2 centre;
	private Pixel centrePixel;
	private PixelList pixelList;
	private int rightAngleCorner;
	private PixelJunctionType junctionType;
	private PixelNode pixelNode;  // the node that coresponds to centrePixel
	// list of pixels protruding from nucleus // normally in edges
	// pixles can be removed from this list (but not from the nucleus)
	private PixelList spikePixelList;

	public PixelNucleus(PixelIsland island) {
		this.island = island;
		setDefaults();
	}

	private void setDefaults() {
		this.rightAngleCorner = -1;
	}

	public boolean contains(PixelNode node) {
		return (this.pixelNode != null && pixelNode.equals(node));
	}

	/** size in pixels.
	 * 
	 * @return
	 */
	public int size() {
		return pixelList.size();
	}

	public static void drawNucleusSet(Set<PixelNucleus> nucleusSet, SVGG g,
			double rad) {
		for (PixelNucleus nucleus : nucleusSet) {
			nucleus.drawCentre(g, rad);
		}
	}

	private void drawCentre(SVGG g, double rad) {
		Real2 centre = this.getCentre();
		SVGCircle circle = new SVGCircle(centre.plus(new Real2(0.5, 0.5)), rad);
		circle.setOpacity(0.4);
		circle.setFill("magenta");
		g.appendChild(circle);
	}

	/** get coordinates of centre of nucleus.
	 * 
	 * @return
	 */
	public Real2 getCentre() {
		Real2 centre = null;
		if (centrePixel == null) {
			Real2Array real2Array = new Real2Array();
			for (Pixel pixel : pixelList) {
				real2Array.add(new Real2(pixel.getInt2()));
			}
			centre = real2Array.getMean();
		} else {
			centre = new Real2(centrePixel.getInt2());
		}
		return centre;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (pixelNode != null) {
			sb.append("node: "+pixelNode + "; ");
		}
		sb.append("{");
		if (centre != null) {
			sb.append(centre + ";");
		}
		ensurePixelList();
		sb.append(pixelList.toString());
		sb.append("}");
		return sb.toString();
	}

//	public JunctionSet getJunctionSet() {
//		return junctionSet;
//	}

//	@Deprecated
//	public Pixel getCentrePixelOld() {
//		centrePixel = null;
//		PixelList pixelList = island.getPixelList();
//		if (pixelList.size() > 0) {
//			double dist0 = Integer.MAX_VALUE;
//			getCentre();
//			for (Pixel pixel : pixelList) {
//				double dist = centre.getDistance(new Real2(pixel.getInt2()));
//				if (dist < dist0) {
//					dist0 = dist;
//					centrePixel = pixel;
//				}
//			}
//		}
//		return centrePixel;
//	}
	
	public Pixel getCentrePixel() {
		return centrePixel;
	}

	public void add(Pixel pixel) {
		ensurePixelList();
		if (!pixelList.contains(pixel)) {
			pixelList.add(pixel);
		}
	}

	private void ensurePixelList() {
		if (pixelList == null) {
			pixelList = new PixelList();
		}
	}

	public boolean canTouch(Pixel pix) {
		for (Pixel pixel : this.pixelList) {
			if (pixel.isDiagonalNeighbour(pix)
					|| pixel.isOrthogonalNeighbour(pix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * list of pixels which can be removed as part of superthinning of nuclei.
	 * 
	 * @return
	 */
	public PixelList getSuperthinRemovablePixelList() {
		PixelList removableList = new PixelList();
		for (Pixel pixel : pixelList) {
			if (pixel.isTjunctionCentre(island)) {
				removableList.add(pixel);
			} else {

			}
		}
		return removableList;
	}

	void doTJunctionThinning(PixelIsland island) {
		PixelList removables = getSuperthinRemovablePixelList();
		island.removePixels(removables);
	}

	public boolean isTerminalJunction() {
		if (junctionType == null) {
			if (pixelList.size() == 1) {
				centrePixel = pixelList.get(0);
				if (centrePixel.getOrthogonalNeighbours(island).size() 
						+ centrePixel.getDiagonalNeighbours(island).size() == 1) {
					junctionType = PixelJunctionType.TERMINAL;
					LOG.trace("Terminal centre: "+centrePixel+" ; "+centrePixel.getOrCreateNeighbours(island));
				}
			}
		}
		return PixelJunctionType.TERMINAL.equals(junctionType);
	}

	public boolean isCrossJunction() {
		if (junctionType == null) {
			if (pixelList.size() == 5) {
				int corner = getCrossCentre();
				if (corner != -1) {
					junctionType = PixelJunctionType.CROSS;
				}
			}
		}
		return PixelJunctionType.CROSS.equals(junctionType);
	}

	private int getCrossCentre() {
		centrePixel = null;
		int pixelNumber = -1;
		for (int i = 0; i < 5; i++) {
			Pixel pixel = pixelList.get(i);
			if (pixel.getOrthogonalNeighbours(island).size() == 4) {
				if (centrePixel != null) {
					throw new RuntimeException("Bad cross: " + this);
				}
				centrePixel = pixel;
				pixelNumber = i;
			}
		}
		return pixelNumber;
	}

	public boolean isTJunction() {
		if (junctionType == null) {
			if (pixelList.size() == 1) {
				if(isNickedT()) {
					junctionType = PixelJunctionType.NICKEDT; 
				}
			} else if (pixelList.size() == 4) {
				if (isFilledT()) {
					junctionType = PixelJunctionType.FILLEDT; 
				}
			}
		}
		return PixelJunctionType.NICKEDT.equals(junctionType) ||
				PixelJunctionType.FILLEDT.equals(junctionType);
	}

	/** symmetric about vertical stem.
	 * 
	 * @return
	 */
	private boolean isNickedT() {
		boolean isT = false;
		if (centrePixel == null) {
			if (pixelList.size() == 1) {
				centrePixel = pixelList.get(0);
				LOG.trace("nicked T "+centrePixel);
			}
		}
		if (centrePixel != null) {
			PixelList diagonalNeighbours = centrePixel.getDiagonalNeighbours(island);
			PixelList orthogonalNeighbours = centrePixel.getOrthogonalNeighbours(island);
			if (diagonalNeighbours.size() == 2 && orthogonalNeighbours.size() == 1) {
				isT = true;
			}
		}
		return isT;
	}

	private boolean isFilledT() {
		boolean isT = false;
		centrePixel = null;
		for (Pixel pixel : pixelList) {
			if (pixel.getOrthogonalNeighbours(island).size() == 3) {
				if (centrePixel != null) {
					throw new RuntimeException("Not a filled TJunction "+this);
				}
				centrePixel = pixel;
			}
		}
		if (centrePixel != null) {
			isT = true;
			LOG.trace("Filled T: "+centrePixel);
		}
		return isT;
	}

	/** 3 connected pixels in rightangle.
	 * 
	 * The first cross is the corner returned by getRightAngleCorner();
	 * 
	 * + +
	 * +
	 * 
	 * @return
	 */
	public boolean isYJunction() {
		if (junctionType == null) {
			if (pixelList.size() == 3) {
				int corner = getRightAngleCorner();
				if (corner != -1) {
					centrePixel = pixelList.get(corner);
					junctionType = PixelJunctionType.Y;
				}
			}
		}
		return PixelJunctionType.Y.equals(junctionType);
	}
	
	/** two diagonal Y's joined by stems.
	 * 
	 *  +
	 * ++
	 *   ++
	 *   +
	 *   
	 *   centre pixel will be randomly one of the two central pixels
	 * @return
	 */
	public boolean isDoubleYJunction() {
		if (junctionType == null) {
			if (pixelList.size() == 6) {
				PixelList centres = new PixelList();
				for (Pixel pixel : pixelList) {
					PixelList orthNeighbours = pixel.getOrthogonalNeighbours(island);
					if (orthNeighbours.size() == 2 && 
							orthNeighbours.get(0).isDiagonalNeighbour(orthNeighbours.get(1)))  {
						centres.add(pixel);
					}
				}
				if (centres.size() == 2 && centres.get(0).isDiagonalNeighbour(centres.get(1))) {
					centrePixel = centres.get(0); // arbitrary but?
					junctionType = PixelJunctionType.DOUBLEY;
				}
			}
		}
		return PixelJunctionType.DOUBLEY.equals(junctionType);
	}

	private int getRightAngleCorner() {
		rightAngleCorner = -1;
		for (int i = 0; i < 3; i++) {
			int j = (i + 1) % 3;
			int k = (j + 1) % 3;
			if (Pixel.isRightAngle(pixelList.get(i), pixelList.get(j),
					pixelList.get(k))) {
				rightAngleCorner = i;
				break;
			}
		}
		return rightAngleCorner;
	}

	public boolean rearrangeYJunction(PixelIsland island) {
		Pixel rightAnglePixel = pixelList.get(rightAngleCorner);
		int p0 = (rightAngleCorner + 1) % 3;
		Pixel startPixel0 = pixelList.get(p0);
		int p1 = (p0 + 1) % 3;
		Pixel startPixel1 = pixelList.get(p1);
		LOG.trace("pixel0 " + startPixel0 + " neighbours "
				+ startPixel0.getOrCreateNeighbours(island));
		LOG.trace("pixel1 " + startPixel1 + " neighbours "
				+ startPixel1.getOrCreateNeighbours(island));
		LOG.trace("right angle " + rightAnglePixel + " neighbours "
				+ rightAnglePixel.getOrCreateNeighbours(island));
		if (movePixel(startPixel0, startPixel1, rightAnglePixel, island)) {
			return true;
		}
		if (movePixel(startPixel1, startPixel0, rightAnglePixel, island)) {
			return true;
		}
		return false;
	}

	private boolean movePixel(Pixel pixel0, Pixel pixel1,
			Pixel rightAnglePixel, PixelIsland island) {
		Int2 right = rightAnglePixel.getInt2();
		Int2 p0 = pixel0.getInt2();
		Int2 p1 = pixel1.getInt2();
		Int2 vector = right.subtract(p0);
		Int2 new2 = p1.plus(vector);
		Pixel newPixel = new Pixel(new2.getX(), new2.getY());
		PixelList neighbours = newPixel.getOrCreateNeighbours(island);
		LOG.trace("new " + newPixel + "neighbours: " + neighbours);
		if (neighbours.size() != 3)
			return false; // we still have to remove old pixel

		PixelList oldNeighbours = pixel1.getOrCreateNeighbours(island); // before
																		// removal
		island.remove(pixel1);
		newPixel.clearNeighbours();
		island.addPixel(newPixel);
		PixelList newPixelNeighbours = newPixel.getOrCreateNeighbours(island);
		LOG.trace("new " + newPixel + "neighbours: " + newPixelNeighbours);
		for (Pixel oldNeighbour : oldNeighbours) {
			oldNeighbour.clearNeighbours();
			PixelList oldNeighbourNeighbours = oldNeighbour
					.getOrCreateNeighbours(island);
			LOG.trace("old " + oldNeighbour + "neighbours: "
					+ oldNeighbourNeighbours);
		}
		return true;
	}

	public PixelList getPixelList() {
		ensurePixelList();
		return pixelList;
	}

	public PixelJunctionType getJunctionType() {
		return junctionType;
	}

	public void set(PixelNode node) {
		if (this.pixelNode != null) {
			throw new RuntimeException();
		}
		this.pixelNode = node;
	}

	public PixelNode getNode() {
		if (pixelNode == null) {
			if (centrePixel == null) {
				LOG.error("Null centre pixel ; pixelList "+pixelList);
				for (Pixel pixel : pixelList) {
					LOG.trace(pixel+"; "+pixel.getOrCreateNeighbours(island));
				}
			} else {
				pixelNode = new PixelNode(centrePixel, island);
			}
		}
		return pixelNode;
	}

	/** all non-nucleus pixels connected to nucleus pixels are added as neighbours.
	 * 
	 * This effectively creates a shell of linkable pixels suitable for edges.
	 * 
	 * @return
	 */
	public PixelList createSpikePixelList() {
		spikePixelList = new PixelList();
		for (Pixel pixel : pixelList) {
			PixelList neighbours = pixel.createNeighbourList(island);
			for (Pixel neighbour : neighbours) {
				if (!pixelList.contains(neighbour)) {
					spikePixelList.add(neighbour);
				}
			}
		}
		return spikePixelList;
	}

	/** get bounding box for nucleus.
	 * 
	 * @return bounding box (null if no pixels)
	 */
	public Int2Range getBoundingBox() {
		Int2Range bbox = null;
		PixelList pixelList = getPixelList();
		if (pixelList.size() > 0) {
			bbox = pixelList.getIntBoundingBox();
		}
		return bbox;
	}

	/** NYI
	 * 
	 * @param nucleusj
	 * @return
	 */
	PixelNucleus merge(PixelNucleus nucleus) {
		throw new RuntimeException("NYI");
	}

	/** adds all pixels.
	 * 
	 * pixelSet is not altered
	 * 
	 * @param pixelSet
	 */
	public void addAll(PixelSet pixelSet) {
		Iterator<Pixel> iterator = pixelSet.iterator();
		while (iterator.hasNext()) {
			this.add(iterator.next());
		}
	}


}
