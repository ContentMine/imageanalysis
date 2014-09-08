package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		Y,
	}
	private JunctionSet junctionSet;
	private JunctionSet externallyConnectedJunctionSet;
	private Set<Pixel> externalPixelSet;
	private PixelIsland island;

	private Real2 centre;
	private Pixel centrePixel;
	private PixelList pixelList;

	private int rightAngleCorner;
	private PixelJunctionType junctionType;

	public PixelNucleus(PixelIsland island) {
		junctionSet = new JunctionSet();
		externallyConnectedJunctionSet = new JunctionSet();
		externalPixelSet = new HashSet<Pixel>();
		this.island = island;
		setDefaults();
	}

	private void setDefaults() {
		this.rightAngleCorner = -1;
	}

	public void add(JunctionNode junction) {
		junctionSet.add(junction);
		PixelList externalPixels = junction.getNonJunctionPixels();
		if (externalPixels.size() > 0) {
			externalPixelSet.addAll(externalPixels.getList());
			externallyConnectedJunctionSet.add(junction);
		}
	}

	public boolean contains(PixelNode junction) {
		return junctionSet.contains(junction);
	}

	public int size() {
		return junctionSet.size();
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

	public Real2 getCentre() {
		if (centre == null) {
			Real2Array real2Array = new Real2Array();
			for (PixelNode junction : junctionSet) {
				Pixel pixel = ((JunctionNode) junction).getCentrePixel();
				real2Array.add(new Real2(pixel.getInt2()));
			}
			centre = real2Array.getMean();
		}
		return centre;
	}

	/**
	 * clean nuclei of various sizes
	 * 
	 * this may be obsolete
	 */
	public void removeExtraneousPixels() {
		if (size() == 1) {
			// do nothing
		} else if (size() == 2) { // maybe a corner
			tidyNucleus2();
		} else if (size() == 3) { // maybe a triangle
			tidyNucleus3();
		} else if (size() == 4) { // maybe a diamond
			tidyDiamond();
		} else {
			tidyNucleusLarge();
		}
	}

	/**
	 * clean nuclei of various sizes
	 * 
	 * @return nodes to be deleted
	 */
	public List<JunctionNode> removeExtraneousJunctions() {
		List<JunctionNode> junctionList = new ArrayList<JunctionNode>();
		if (size() == 1) {
			// do nothing
		} else if (size() == 2) {
		} else if (size() == 3) { // maybe a triangle
			junctionList.addAll(tidyYJunction());
		} else if (size() == 4) { // maybe a rhombus
			junctionList.addAll(tidyTJunction());
		} else if (size() == 5) { // corner?
		} else {
			LOG.trace("Cannot tidy large nucleus " + this);
		}
		return junctionList;
	}

	private void tidyNucleus2() {
		Set<Pixel> neighbours = remove2ConnectedNeighbours();
		LOG.debug("removed from nucleus2 " + neighbours);
	}

	private Set<Pixel> remove2ConnectedNeighbours() {
		List<PixelNode> junctionNodes = junctionSet.getList();
		Set<Pixel> neighbours = getCommonNeighbours(junctionNodes);
		LOG.debug(island.size());
		island.removePixels(new PixelList(neighbours));
		LOG.debug("after: " + island.size());
		return neighbours;
	}

	/**
	 * find any nodes which bridge an isolated junction pair. A X B // remove X
	 * 
	 * @param junctionNodes
	 * @return
	 */
	private Set<Pixel> getCommonNeighbours(List<PixelNode> junctionNodes) {

		Set<Pixel> commonNeighbourSet = new HashSet<Pixel>();
		for (int i = 0; i < junctionNodes.size() - 1; i++) {
			PixelNode nodei = junctionNodes.get(i);
			Set<Pixel> neighboursi = new HashSet<Pixel>(nodei.getCentrePixel()
					.getOrCreateNeighbours(island).getList());
			for (int j = i + 1; j < junctionNodes.size(); j++) {
				PixelNode nodej = junctionNodes.get(j);
				Set<Pixel> neighboursj = new HashSet<Pixel>(nodej
						.getCentrePixel().getOrCreateNeighbours(island)
						.getList());
				if (neighboursj.retainAll(neighboursi)) {
					commonNeighbourSet.addAll(neighboursj);
				}
			}
		}
		return commonNeighbourSet;
	}

	private void tidyNucleus3() {
		LOG.trace("3-Nucleus NYI " + this);
	}

	/**
	 * A12 34 B
	 * 
	 * remove 2 or 3 arbitrarily
	 * 
	 * @return
	 */
	private boolean tidyDiamond() {
		boolean tidy = false;
		if (externallyConnectedJunctionSet.size() == 2) {
			List<PixelNode> external = externallyConnectedJunctionSet.getList();
			LOG.debug("diamond external connections: " + external);
		}
		return tidy;
	}

	private List<JunctionNode> tidyYJunction() {
		Real2 centre = Pixel.getCentre(junctionSet.getPixelList());
		JunctionNode centreJunction = findCentreJunction(centre);
		List<JunctionNode> removedJunctionList = new ArrayList<JunctionNode>();
		LOG.trace("YJunction");
		removedJunctionList = processYJunctions();
		LOG.trace("removed junctions: " + removedJunctionList.size());
		return removedJunctionList;
	}

	private List<JunctionNode> tidyTJunction() {
		Real2 centre = Pixel.getCentre(junctionSet.getPixelList());
		JunctionNode centreJunction = findCentreJunction(centre);
		List<JunctionNode> removedJunctionList = new ArrayList<JunctionNode>();
		if (centreJunction == null) {
			LOG.debug("YJunctions should not be here");
		} else {
			removedJunctionList = processTJunctions(centreJunction);
			LOG.trace("removed: " + removedJunctionList.size());
		}
		return removedJunctionList;
	}

	/**
	 * process all Y-junctions in nucleus. normally only one.
	 * 
	 * there are two types:
	 * 
	 * axial + + + + + + + + +
	 * 
	 * diag + + + +++ + +
	 * 
	 * @param centreJunction
	 * @return
	 */
	@Deprecated
	private List<JunctionNode> processYJunctions() {
		List<JunctionNode> removedJunctionList = new ArrayList<JunctionNode>();
		List<PixelNode> junctionList = junctionSet.getList();
		for (PixelNode junction0 : junctionList) {
			JunctionNode junction = (JunctionNode) junction0; // should use
																// better
																// generics :-(
			PixelList diagonalPixels = junction.getDiagonalNeighbours(island);
			PixelList orthogonalPixels = junction
					.getOrthogonalNeighbours(island);
			if (diagonalPixels.size() == 1 && orthogonalPixels.size() == 2) {
				junction.processDiagonal(diagonalPixels.get(0),
						orthogonalPixels);
			} else if (diagonalPixels.size() == 2
					&& orthogonalPixels.size() == 1) {
				junction.processOrthogonal(orthogonalPixels.get(0),
						diagonalPixels);
			} else {
				LOG.debug("not a Y-junction");
			}
			if (junction.isYJunction()) {
				removedJunctionList.add(junction);
			}
		}
		return removedJunctionList;
	}

	/**
	 * process all T-junctions in nucleus. normally only one.
	 * 
	 * @param centreJunction
	 * @return
	 */
	private List<JunctionNode> processTJunctions(JunctionNode centreJunction) {
		List<JunctionNode> removedJunctionList = new ArrayList<JunctionNode>();
		List<PixelNode> junctionList = junctionSet.getList();
		for (PixelNode junction : junctionList) {
			if (!junction.equals(centreJunction)) {
				Int2 i2 = junction.getCentrePixel().getInt2();
				if (i2 == null)
					continue;
				Int2 inext = centreJunction.getCentrePixel().getInt2();
				if (inext == null)
					continue;
				Int2 dist = i2.subtract(inext);
				int manhattan = Math.abs(dist.getX()) + Math.abs(dist.getY());
				if (manhattan != 1) {
					LOG.debug("not T-junction");
					removedJunctionList = new ArrayList<JunctionNode>();
					break;
				}
				LOG.trace("removed Junction " + junction);
				junctionSet.remove(junction);
				removedJunctionList.add((JunctionNode) junction);
			}
		}
		return removedJunctionList;
	}

	// /** process all Y-junctions in nucleus.
	// * normally only one.
	// *
	// * @param centreJunction
	// * @return
	// */
	// private List<Junction> processYJunctions(Junction centreJunction) {
	// List<Junction> removedJunctionList = new ArrayList<Junction>();
	// List<PixelNode> junctionList = junctionSet.getList();
	// for (PixelNode junction : junctionList) {
	// if (!junction.equals(centreJunction)) {
	// Int2 i2 = junction.getCentrePixel().getInt2();
	// if (i2 == null) continue;
	// Int2 inext = centreJunction.getCentrePixel().getInt2();
	// if (inext == null) continue;
	// Int2 dist = i2.subtract(inext);
	// int manhattan = Math.abs(dist.getX()) + Math.abs(dist.getY());
	// if (manhattan != 1) {
	// LOG.debug("not T-junction");
	// removedJunctionList = new ArrayList<Junction>();
	// break;
	// }
	// LOG.trace("removed Junction "+junction);
	// junctionSet.remove(junction);
	// removedJunctionList.add((Junction)junction);
	// }
	// }
	// return removedJunctionList;
	// }

	private JunctionNode findCentreJunction(Real2 centre) {
		JunctionNode centreJunction = null;
		double minDist = 0.4; // final dist should be 0.25
		for (PixelNode junction : junctionSet) {
			double dist = new Real2(junction.getCentrePixel().getInt2())
					.getDistance(centre);
			if (dist < minDist) {
				centreJunction = (JunctionNode) junction;
			}
		}
		return centreJunction;
	}

	private void tidyNucleusLarge() {
		LOG.trace("large-Nucleus NYI " + this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (PixelNode junction : junctionSet) {
			sb.append(junction + "; ");
		}
		sb.append("{");
		if (centre != null) {
			sb.append(centre + ";");
		}
		if (pixelList != null) {
			sb.append(pixelList.toString());
		}
		sb.append("}");
		return sb.toString();
	}

	public JunctionSet getJunctionSet() {
		return junctionSet;
	}

	@Deprecated
	public Pixel getCentrePixelOld() {
		centrePixel = null;
		PixelList pixelList = island.getPixelList();
		if (pixelList.size() > 0) {
			double dist0 = Integer.MAX_VALUE;
			getCentre();
			for (Pixel pixel : pixelList) {
				double dist = centre.getDistance(new Real2(pixel.getInt2()));
				if (dist < dist0) {
					dist0 = dist;
					centrePixel = pixel;
				}
			}
		}
		return centrePixel;
	}
	
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

}
