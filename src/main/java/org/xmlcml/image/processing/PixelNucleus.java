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

	public PixelNucleus(PixelIsland island) {
		junctionSet = new JunctionSet();
		externallyConnectedJunctionSet = new JunctionSet();
		externalPixelSet = new HashSet<Pixel>();
		this.island = island;
	}

	public void add(Junction junction) {
		junctionSet.add(junction);
		PixelList externalPixels = junction.getNonJunctionPixels();
		if (externalPixels.size() > 0) {
			externalPixelSet.addAll(externalPixels.getList());
			externallyConnectedJunctionSet.add(junction);
		}
	}

	public boolean contains(Junction junction) {
		return junctionSet.contains(junction);
	}

	public int size() {
		return junctionSet.size();
	}

	public static void drawSet(Set<PixelNucleus> nucleusSet, SVGG g) {
		for (PixelNucleus nucleus : nucleusSet) {
			SVGCircle circle = new SVGCircle(nucleus.getCentre().plus(
					new Real2(0.5, 0.5)), 5.);
			circle.setOpacity(0.4);
			circle.setFill("magenta");
			g.appendChild(circle);
		}
	}

	private Real2 getCentre() {
		Real2Array real2Array = new Real2Array();
		for (PixelNode junction : junctionSet) {
			real2Array.add(new Real2(junction.getCentrePixel().getInt2()));
		}
		return real2Array.getMean();
	}

	/**
	 * clean nuclei of various sizes
	 * 
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
		} else if (size() == 5) { // corner?
			tidyNucleus5();
		} else {
			tidyNucleusLarge();
		}
	}

	/**
	 * clean nuclei of various sizes
	 * 
	 * @return nodes to be deleted
	 */
	public List<Junction> removeExtraneousJunctions() {
		List<Junction> junctionList = new ArrayList<Junction>();
		if (size() == 1) {
			// do nothing
		} else if (size() == 2) {
		} else if (size() == 3) { // maybe a triangle
		} else if (size() == 4) { // maybe a rhombus
			junctionList.addAll(tidyTJunction());
		} else if (size() == 5) { // corner?
		} else {
			LOG.debug("Cannot tidy large nucleus "+this);
		}
		return junctionList;
	}

	private void tidyNucleus2() {
		List<PixelNode> junctionNodes = junctionSet.getList();
		Set<Pixel> neighbours = getCommonNeighbours(junctionNodes);
		island.removePixels(new PixelList(neighbours));
		LOG.trace("removed " + neighbours);
	}

	/**
	 * remove any nodes which bridge an isolated junction pair. A X* // remove X
	 * * B
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
		LOG.debug("3-Nucleus NYI "+this);
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

	private List<Junction> tidyTJunction() {
		List<Junction> removedJunctionList = new ArrayList<Junction>();
		Real2 centre = Pixel.getCentre(junctionSet.getPixelList());
		Junction centreJunction = findCentreJunction(centre);
		if (centreJunction == null) {
			LOG.debug("not T Junction: "+this);
			return removedJunctionList;
		}
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
					removedJunctionList = new ArrayList<Junction>();
					break;
				}
				LOG.trace("removed Junction "+junction);
				junctionSet.remove(junction);
				removedJunctionList.add((Junction)junction);
			}
		}
		LOG.trace("removed: "+removedJunctionList.size());
		return removedJunctionList;
	}

	private Junction findCentreJunction(Real2 centre) {
		Junction centreJunction = null;
		double minDist = 0.4; // final dist should be 0.25
		for (PixelNode junction : junctionSet) {
			double dist = new Real2(junction.getCentrePixel().getInt2())
					.getDistance(centre);
			if (dist < minDist) {
				centreJunction = (Junction) junction;
			}
		}
		return centreJunction;
	}

	private void tidyNucleus5() {
		LOG.debug("5-Nucleus NYI "+this);
	}

	private void tidyNucleusLarge() {
		LOG.debug("large-Nucleus NYI "+this);
	}
	
	public String toString() {
		String s = "";
		for (PixelNode junction : junctionSet) {
			s += junction + "; ";
		}
		return s;
	}

}
