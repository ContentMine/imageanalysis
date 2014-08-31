package org.xmlcml.image.pixel;


/** set of junctions
 * 
 * @author pm286
 *
 */
public class JunctionSet extends SortedPixelNodeSet {

	public JunctionSet() {
		super();
	}
	
	public String toString() {
		getList();
		String s = list.toString();
		return s;
	}


}
