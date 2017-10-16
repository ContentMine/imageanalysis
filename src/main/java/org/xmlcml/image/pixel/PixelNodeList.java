package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.image.pixel.PixelComparator.ComparatorType;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class PixelNodeList implements Iterable<PixelNode> {

	private final static Logger LOG = Logger.getLogger(PixelNodeList.class);
	private List<PixelNode> nodeList;
	private SVGG svgg;
	
	public PixelNodeList() {
		ensureList();
	}
	
	public PixelNodeList(PixelNodeList nodeList) {
		this.nodeList = new ArrayList<PixelNode>(nodeList.nodeList);
	}

	/**
	 * creates from output string, such as (11,22)(33,44)
	 * 
	 * @param nodeListS
	 * @param island null if cannot create
	 * @return
	 */
	public static PixelNodeList createNodeList(String nodeListS, PixelIsland island) {
		PixelNodeList nodeList = null;
		if (nodeListS != null) {
			PixelList pixelList = PixelList.createPixelList(nodeListS, island);
			if (pixelList == null || pixelList.size() != 2) {
				throw new RuntimeException("Bad coordinate string for 2 nodes: "+nodeListS);
			}
			nodeList = new PixelNodeList();
			nodeList.add(new PixelNode(pixelList.get(0), island));
			nodeList.add(new PixelNode(pixelList.get(1), island));
		}
		return nodeList;
	}

	private void ensureList() {
		if (nodeList == null) {
			nodeList = new ArrayList<PixelNode>();
		}
	}

	public Iterator<PixelNode> iterator() {
		ensureList();
		return nodeList.iterator();
	}

	/**  gets node by coordinates.
	 * 
	 * @param int2
	 * @return
	 */
	public PixelNode getPixelNode(Int2 int2) {
		if (int2 != null) {
			for (PixelNode node : nodeList) {
				Int2 coord = node.getCentrePixel().getInt2(); 
				if (int2.equals(coord)) return node;
			}
		}
		return null;
	}
	
	/**  gets node by pixel.
	 * 
	 * @param pixel
	 * @return node or null
	 */
	public PixelNode getPixelNode(Pixel pixel) {
		if (pixel != null) {
			for (PixelNode node : nodeList) {
				if (pixel.equals(node.getCentrePixel())) return node;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (PixelNode node : this) {
			sb.append(node.toString());
		}
		sb.append("]");
		return sb.toString();
	}

	public void add(PixelNode node) {
		ensureList();
		nodeList.add(node);
	}

	public int size() {
		ensureList();
		return nodeList.size();
	}

	public PixelNode get(int i) {
		ensureList();
		return (i < 0 || i >= nodeList.size()) ? null : nodeList.get(i);
	}

	public void remove(int i) {
		ensureList();
		if (i < 0 || i >= nodeList.size()) nodeList.remove(i);
	}

	public void addAll(Collection<PixelNode> nodeSet) {
		ensureList();
		nodeList.addAll(nodeSet);
	}

	public boolean contains(PixelNode node) {
		ensureList();
		return nodeList.contains(node);
	}

	public List<PixelNode> getList() {
		ensureList();
		return nodeList;
	}

	/**
	 * sorts by comparator.
	 * 
	 * only one direction
	 * 
	 */
	public void sort(ComparatorType comparatorType) {
		Collections.sort(nodeList, new PixelNodeComparator(comparatorType));
	}

	/**
	 * sorts X first, then Y.
	 * 
	 */
	public void sortXY() {
		Collections.sort(nodeList, new PixelNodeComparator(ComparatorType.LEFT,
				ComparatorType.TOP));
	}

	/**
	 * sorts Y first, then X.
	 * 
	 */
	public void sortYX() {
		Collections.sort(nodeList, new PixelNodeComparator(ComparatorType.TOP,
				ComparatorType.LEFT));
	}

	public SVGG getOrCreateSVG() {
		if (svgg == null) {
			svgg = new SVGG();
			for (PixelNode node : this) {
				svgg.appendChild(node.getOrCreateSVG());
			}
		}
		return svgg;
	}

	public boolean remove(PixelNode node) {
		ensureList();
		return nodeList.remove(node);
	}

	/** replace existing node.
	 * 
	 * @param inode
	 * @param newNode
	 * @return
	 */
	public PixelNode set(int inode, PixelNode newNode) {
		return nodeList.set(inode, newNode);
	}

	public Multiset<PixelNode> getMultipleNodes() {
		Multiset<PixelNode> multipleNodeSet = HashMultiset.create();
		for (PixelNode node : this) {
			multipleNodeSet.add(node);
		}
		
		Set<Entry<PixelNode>> singletons = new HashSet<Entry<PixelNode>>();
		for (Entry<PixelNode> e : multipleNodeSet.entrySet()) {
			if (e.getCount() <= 1) {
				singletons.add(e);
			}
		}
		for (Entry<PixelNode> e : singletons) {
			multipleNodeSet.remove(e.getElement());
		}
		return multipleNodeSet;
	}

}
