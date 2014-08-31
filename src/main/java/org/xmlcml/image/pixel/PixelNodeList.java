package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;

public class PixelNodeList implements Iterable<PixelNode> {

	private final static Logger LOG = Logger.getLogger(PixelNodeList.class);
	private List<PixelNode> nodeList;
	
	public PixelNodeList() {
		ensureList();
	}
	
	private void ensureList() {
		if (nodeList == null) {
			nodeList = new ArrayList<PixelNode>();
		}
	}

	@Override
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
		return nodeList.get(i);
	}

	public void remove(int i) {
		ensureList();
		nodeList.remove(i);
	}

	public void addAll(Set<PixelNode> nodeSet) {
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

}
