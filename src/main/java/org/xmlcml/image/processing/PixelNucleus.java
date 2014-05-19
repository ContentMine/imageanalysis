package org.xmlcml.image.processing;

import java.util.HashSet;
import java.util.Set;

import org.xmlcml.image.compound.PixelList;

/** connected Nodes.
 * 
 * @author pm286
 *
 */
public class PixelNucleus {

	private Set<Junction> junctionSet;
	private Set<Junction> externallyConnectedJunctionSet;
	private Set<Pixel> externalPixelSet;
	
	public PixelNucleus() {
		junctionSet = new HashSet<Junction>();
		externallyConnectedJunctionSet = new HashSet<Junction>();
		externalPixelSet = new HashSet<Pixel>();
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
}
