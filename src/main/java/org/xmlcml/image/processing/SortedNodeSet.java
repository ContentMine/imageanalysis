package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/** Set of nodes sorted in order of nodes.
 * 
 * sort based on ordering of centre pixels
 * 
 * @author pm286
 *
 */
public class SortedNodeSet implements Iterable<PixelNode> {

	protected TreeSet<PixelNode> sortedSet;
	protected List<PixelNode> list;
	
	public SortedNodeSet() {
		this.sortedSet = new TreeSet<PixelNode>();
	}
	
	public Iterator<PixelNode> iterator() {
		return sortedSet.iterator();
	}
	
	public int size() {
		return sortedSet.size();
	}
	
	public PixelNode get(int i) {
		getList();
		return (i < 0 || i >= list.size()) ? null : list.get(i);
	}
	
	public List<PixelNode> getList() {
		if (list == null) {
			list = new ArrayList<PixelNode>();
			Iterator<PixelNode> iterator = iterator();
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}
		}
		return list;
	}
	
	public void add(PixelNode node) {
		sortedSet.add(node);
		this.list = null;
	}

	public void addAll(List<PixelNode> list) {
		this.sortedSet.addAll(list);
	}

	public boolean isEmpty() {
		return sortedSet.isEmpty();
	}

	public void remove(PixelNode node) {
		sortedSet.remove(node);
	}
	
	public String toString() {
		return sortedSet.toString();
	}
	
}
