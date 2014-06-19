package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.image.compound.PixelList;

/**
 * connected Nodes.
 * 
 * @author pm286
 * 
 */
public class PixelNucleus {

	private static Logger LOG = Logger.getLogger(PixelNucleus.class);

	private JunctionSet junctionSet;
	private JunctionSet externallyConnectedJunctionSet;
	private Set<Pixel> externalPixelSet;
	private PixelIsland island;

	private Real2 centre;

	private Pixel centrePixel;

	private PixelList pixelList;

	public PixelNucleus(PixelIsland island) {
		junctionSet = new JunctionSet();
		externallyConnectedJunctionSet = new JunctionSet();
		externalPixelSet = new HashSet<Pixel>();
		this.island = island;
	}

	public void add(JunctionNode junction) {
		junctionSet.add(junction);
		PixelList externalPixels = junction.getNonJunctionPixels();
		if (externalPixels.size() > 0) {
			externalPixelSet.addAll(externalPixels.getList());
			externallyConnectedJunctionSet.add(junction);
		}
	}

//	public PixelList getPixelList() {
//		if (pixelList == null) {
//			for (PixelNode junction : junctionSet) {
//				PixelList junctionPixels = junction.getCentrePixel()
//			}
//		}
//	}
	
	public boolean contains(PixelNode junction) {
		return junctionSet.contains(junction);
	}

	public int size() {
		return junctionSet.size();
	}

	public static void drawNucleusSet(Set<PixelNucleus> nucleusSet, SVGG g, double rad) {
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
				Pixel pixel = ((JunctionNode)junction).getCentrePixel();
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
			LOG.trace("Cannot tidy large nucleus "+this);
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
		LOG.debug("after: "+island.size());
		return neighbours;
	}

	/**
	 * find any nodes which bridge an isolated junction pair. 
	 * A X
	 *   B
	 * // remove X
	 * 
	 * @param junctionNodes
	 * @return
	 */
	private Set<Pixel> getCommonNeighbours(List<PixelNode> junctionNodes) {
		
		Set<Pixel> commonNeighbourSet = new HashSet<Pixel>();
		for (int i = 0; i < junctionNodes.size() - 1; i++) {
			PixelNode nodei = junctionNodes.get(i);
			Set<Pixel> neighboursi = new HashSet<Pixel>(nodei.getCentrePixel()
					.getNeighbours(island).getList());
			for (int j = i + 1; j < junctionNodes.size(); j++) {
				PixelNode nodej = junctionNodes.get(j);
				Set<Pixel> neighboursj = new HashSet<Pixel>(nodej
						.getCentrePixel().getNeighbours(island).getList());
				if (neighboursj.retainAll(neighboursi)) {
					commonNeighbourSet.addAll(neighboursj);
				}
			}
		}
		return commonNeighbourSet;
	}

	private void tidyNucleus3() {
		LOG.trace("3-Nucleus NYI "+this);
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
		LOG.trace("removed junctions: "+removedJunctionList.size());
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
			LOG.trace("removed: "+removedJunctionList.size());
		}
		return removedJunctionList;
	}

	/** process all Y-junctions in nucleus.
	 * normally only one.
	 * 
	 * there are two types:
	 * 
	 * axial
	 *   +     +
	 *    +   +
	 *     + +
	 *      +
	 *      +
	 *      +
	 *      
	 * diag
	 *   +     
	 *    +   
	 *     + 
	 *      +++
	 *      +
	 *      +
	 * 
	 * @param centreJunction
	 * @return
	 */
	private List<JunctionNode> processYJunctions() {
		List<JunctionNode> removedJunctionList = new ArrayList<JunctionNode>();
		List<PixelNode> junctionList = junctionSet.getList();
		for (PixelNode junction0 : junctionList) {
			JunctionNode junction = (JunctionNode) junction0; // should use better generics :-(
			PixelList diagonalPixels = junction.getDiagonalNeighbours(island);
			PixelList orthogonalPixels = junction.getOrthogonalNeighbours(island);
			if (diagonalPixels.size() == 1 && orthogonalPixels.size() == 2) {
				junction.processDiagonal(diagonalPixels.get(0), orthogonalPixels);
			} else if (diagonalPixels.size() == 2 && orthogonalPixels.size() == 1) {
				junction.processOrthogonal(orthogonalPixels.get(0), diagonalPixels);
			} else {
				LOG.debug("not a Y-junction");
			}
			if (junction.isYJunction()) {
				removedJunctionList.add(junction);
			}
		}
		return removedJunctionList;
	}

	/** process all T-junctions in nucleus.
	 * normally only one.
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
				if (i2 == null) continue;
				Int2 inext = centreJunction.getCentrePixel().getInt2();
				if (inext == null) continue;
				Int2 dist = i2.subtract(inext);
				int manhattan = Math.abs(dist.getX()) + Math.abs(dist.getY());
				if (manhattan != 1) {
					LOG.debug("not T-junction");
					removedJunctionList = new ArrayList<JunctionNode>();
					break;
				}
				LOG.trace("removed Junction "+junction);
				junctionSet.remove(junction);
				removedJunctionList.add((JunctionNode)junction);
			}
		}
		return removedJunctionList;
	}

//	/** process all Y-junctions in nucleus.
//	 * normally only one.
//	 * 
//	 * @param centreJunction
//	 * @return
//	 */
//	private List<Junction> processYJunctions(Junction centreJunction) {
//		List<Junction> removedJunctionList = new ArrayList<Junction>();
//		List<PixelNode> junctionList = junctionSet.getList();
//		for (PixelNode junction : junctionList) {
//			if (!junction.equals(centreJunction)) {
//				Int2 i2 = junction.getCentrePixel().getInt2();
//				if (i2 == null) continue;
//				Int2 inext = centreJunction.getCentrePixel().getInt2();
//				if (inext == null) continue;
//				Int2 dist = i2.subtract(inext);
//				int manhattan = Math.abs(dist.getX()) + Math.abs(dist.getY());
//				if (manhattan != 1) {
//					LOG.debug("not T-junction");
//					removedJunctionList = new ArrayList<Junction>();
//					break;
//				}
//				LOG.trace("removed Junction "+junction);
//				junctionSet.remove(junction);
//				removedJunctionList.add((Junction)junction);
//			}
//		}
//		return removedJunctionList;
//	}

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
		LOG.trace("large-Nucleus NYI "+this);
	}
	
	public String toString() {
		String s = "";
		for (PixelNode junction : junctionSet) {
			s += junction + "; ";
		}
		return s;
	}

	public JunctionSet getJunctionSet() {
		return junctionSet;
	}

	public Pixel getCentrePixel() {
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

}
