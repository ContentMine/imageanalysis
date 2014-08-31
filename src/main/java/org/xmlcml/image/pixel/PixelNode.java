package org.xmlcml.image.pixel;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;

public class PixelNode implements Comparable<PixelNode> {

	private final static Logger LOG = Logger.getLogger(PixelNode.class);
	
	Pixel centrePixel; // pixel 1
	private PixelEdgeList edgeList;
	private String label;
	private String id;
	private Set<Pixel> unusedNeighbours;

	protected PixelNode() {
	}
	
	public PixelNode(Pixel pixel) {
		this.centrePixel = pixel;
	}
	
	public PixelNode(Pixel pixel, PixelIsland island) {
		this(pixel);
		addNeighboursToUnusedSet(pixel, island);
	}

	private void addNeighboursToUnusedSet(Pixel pixel, PixelIsland island) {
		ensureUnusedNeighbours();
		unusedNeighbours.addAll(pixel.getNeighbours(island).getList());
	}
	
	public Pixel getCentrePixel() {
		return centrePixel;
	}

	/** compare Y values of centrePixels then X.
	 * if Y values are equal compare X
	 * 
	 */
	public int compareTo(PixelNode node1) {
		int compare = -1;
		if (node1 != null) {
			Pixel centrePixel1 = node1.getCentrePixel();
			compare = this.centrePixel.compareTo(centrePixel1);
		}
		return compare;
	}
	
	public String toString() {
		getCentrePixel();
		StringBuilder sb = new StringBuilder((id == null) ? "" : id+" ");
		sb.append((label == null) ? "" : " "+label+" ");
		sb.append((centrePixel == null) ? "?" : String.valueOf(centrePixel));
		return sb.toString();
	}

	public PixelList getDiagonalNeighbours(PixelIsland island) {
		return centrePixel.getDiagonalNeighbours(island);
	}

	public PixelList getOrthogonalNeighbours(PixelIsland island) {
		return centrePixel.getOrthogonalNeighbours(island);
	}

	public void addEdge(PixelEdge pixelEdge) {
		ensureEdgeList();
		this.edgeList.add(pixelEdge);
	}

	private void ensureEdgeList() {
		if (edgeList == null) {
			edgeList = new PixelEdgeList();
		}
	}

	public PixelEdgeList getEdges() {
		ensureEdgeList();
		return edgeList;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public boolean removeEdge(PixelEdge edge) {
		return edgeList.remove(edge);
	}

	public SVGG createSVG(double rad) {
		SVGG g = new SVGG();
		SVGCircle circle = new SVGCircle(new Real2(centrePixel.getInt2()).plus(new Real2(0.5, 0.5)), rad);
		g.appendChild(circle);
		circle.setFill("none");
		return g;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public Set<Pixel> getUnusedNeighbours() {
		ensureUnusedNeighbours();
		return unusedNeighbours;
	}

	private void ensureUnusedNeighbours() {
		if (unusedNeighbours == null) {
			unusedNeighbours = new HashSet<Pixel>();
		}
	}

	public boolean hasMoreUnusedNeighbours() {
		return getUnusedNeighbours().size() > 0;
	}

	/** gets an unusued neighbour and removes from unusedNeighbours set.
	 * 
	 * @return
	 */
	public Pixel getNextUnusedNeighbour() {
		ensureUnusedNeighbours();
		Pixel nextUnused = unusedNeighbours.iterator().next();
		unusedNeighbours.remove(nextUnused);
		return nextUnused;
	}

	public void removeUnusedNeighbour(Pixel neighbour) {
		ensureUnusedNeighbours();
		unusedNeighbours.remove(neighbour);
		LOG.trace(this+" removed: "+neighbour+" unused: "+unusedNeighbours);
	}
}
