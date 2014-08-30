package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	public void add(PixelNode node) {
		ensureList();
		nodeList.add(node);
	}

	public int size() {
		ensureList();
		return nodeList.size();
	}

}
