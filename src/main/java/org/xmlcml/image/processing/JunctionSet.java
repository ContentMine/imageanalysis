package org.xmlcml.image.processing;

/** set of junctions
 * 
 * @author pm286
 *
 */
public class JunctionSet extends SortedNodeSet {

	public JunctionSet() {
		super();
	}
	
	public String toString() {
		getList();
		String s = list.toString();
		return s;
	}
	
}
