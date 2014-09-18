package org.xmlcml.image.pixel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/** an intermediate object which identifies and manages PixelNucleus's and PixelNodes.
 * 
 * PixelNodes can exist independently of PixelGraph (though normally they are then used 
 * to create a graph). The node can be either:
 * <ul>
 *   <li>Dot (a single isolated pixel)</li>
 *   <li>terminal (a pixel with only one neighbour)</li>
 *   <li>nucleus (a pixel with 3 or more connections)</li>
 * </ul> 
 * 
 * Because the details of the nucleus matter, the node is described by a PixelNucleus and
 * a centre Pixel. The precise details of PixelNucleus depend on what thinning algorithm has 
 * been applied. 
 * 
 * PixelNucleusCollection is used to search thinned islands for possible nodes, and their
 * nuclei. In some uses, the nucleus is the primary object, in others the node, so maps 
 * between the two are given. A nucleus can only hold one node; normally this is the "central" 
 * node; in rare cases there may be no centre and a convention is used.
 * 
 * @author pm286
 *
 */
public class PixelNucleusFactory {
	
	private final static Logger LOG = Logger.getLogger(PixelNucleusFactory.class);

	private PixelNucleusList crossJunctionList;
	private PixelNucleusList doubleYJunctionList;
	private PixelNucleusList terminalJunctionList;
	private PixelNucleusList tJunctionList;
	private PixelNucleusList yJunctionList;
	private PixelNucleusList allNucleusList;
//	private PixelList pixelList;
	private PixelIsland island;
	private PixelNodeList nodeList;
	private Map<Pixel, PixelNucleus> nucleusByPixelMap;
	private Map<PixelNode, PixelNucleus> nucleusByNodeMap;
	private Map<PixelNucleus, PixelNode> nodeByNucleusMap;
	
	public PixelNucleusFactory(PixelIsland island) {
		this.island = island;
		indexJunctions();
		
	}
	
	public PixelIsland getPixelIsland() {
		return island;
	}
	
	public PixelNucleusList getOrCreateCrossJunctionList() {
		if (crossJunctionList == null) {
			crossJunctionList = new PixelNucleusList();
			getOrCreatePixelNucleusList();
			for (PixelNucleus nucleus : allNucleusList) {
				if (nucleus.isCrossJunction()) {
					crossJunctionList.add(nucleus);
				}
			}
		}
		return crossJunctionList;
	}
	public PixelNucleusList getOrCreateDoubleYJunctionList() {
		if (doubleYJunctionList == null) {
			doubleYJunctionList = new PixelNucleusList();
			getOrCreatePixelNucleusList();
			for (PixelNucleus nucleus : allNucleusList) {
				if (nucleus.isDoubleYJunction()) {
					doubleYJunctionList.add(nucleus);
				}
			}
		}
		return doubleYJunctionList;
	}
	public PixelNucleusList getOrCreateTerminalJunctionList() {
		if (terminalJunctionList == null) {
			terminalJunctionList = new PixelNucleusList();
			getOrCreatePixelNucleusList();
			for (PixelNucleus pixelNucleus : allNucleusList) {
				if (pixelNucleus.isTerminalJunction()) {
					terminalJunctionList.add(pixelNucleus);
				}
			}
		}
		return terminalJunctionList;
	}
	public PixelNucleusList getOrCreateTJunctionList() {
		if (tJunctionList == null) {
			tJunctionList = new PixelNucleusList();
			getOrCreatePixelNucleusList();
			for (PixelNucleus pixelNucleus : allNucleusList) {
				if (pixelNucleus.isTJunction()) {
					tJunctionList.add(pixelNucleus);
				}
			}
		}
		return tJunctionList;
	}
	public PixelNucleusList getOrCreateYJunctionList() {
		if (yJunctionList == null) {
			yJunctionList = new PixelNucleusList();
			getOrCreatePixelNucleusList();
			for (PixelNucleus pixelNucleus : allNucleusList) {
				if (pixelNucleus.isYJunction()) {
					yJunctionList.add(pixelNucleus);
				}
			}
		}
		return yJunctionList;
	}
	
	private void indexJunctions() {
		indexNucleusListByPixels(getOrCreateCrossJunctionList());
		indexNucleusListByPixels(getOrCreateDoubleYJunctionList());
		indexNucleusListByPixels(getOrCreateTerminalJunctionList());
		indexNucleusListByPixels(getOrCreateTJunctionList());
		indexNucleusListByPixels(getOrCreateYJunctionList());
	}
		
	private void indexNucleusListByPixels(PixelNucleusList nucleusList) {
		ensurePixelNucleusByPixelMap();
		for (PixelNucleus nucleus : nucleusList) {
			PixelList pixels = nucleus.getPixelList();
			for (Pixel pixel : pixels) {
				nucleusByPixelMap.put(pixel, nucleus);
			}
		}
	}
	
	public PixelNucleusList getThreewayJunctionList() {
		PixelNucleusList threewayList = new PixelNucleusList();
		threewayList.addAll(getOrCreateTJunctionList());
		threewayList.addAll(getOrCreateYJunctionList());
		return threewayList;
	}
	
	public PixelNucleusList getOrCreatePixelNucleusList() {
		if (allNucleusList == null) {
			allNucleusList = new PixelNucleusList();
			for (Pixel pixel : island.pixelList) {
				boolean added = false;
				if (pixel.getOrCreateNeighbours(island).size() != 2) {
					for (PixelNucleus nucleus : allNucleusList) {
						if (nucleus.canTouch(pixel)) {
							nucleus.add(pixel);
							added = true;
							break;
						}
					}
					if (!added) {
						PixelNucleus nucleus = new PixelNucleus(island);
						nucleus.add(pixel);
						LOG.trace("created nucleus: "+pixel+"; "+nucleus);
						allNucleusList.add(nucleus);
					}
				}
			}
			
			LOG.trace("Created nucleusList: "+allNucleusList.toString());
		}
		return allNucleusList;
	}
	
	private void ensurePixelNucleusByPixelMap() {
		if (nucleusByPixelMap == null) {
			nucleusByPixelMap = new HashMap<Pixel, PixelNucleus>();
		}
	}
	public Map<Pixel, PixelNucleus> getPixelNucleusByPixelMap() {
		ensurePixelNucleusByPixelMap();
		return nucleusByPixelMap;
	}

	public PixelNodeList getOrCreateNodeList() {
		if (nodeList == null) {
			getOrCreatePixelNucleusList();
			nodeList = new PixelNodeList();
			for (PixelNucleus pixelNucleus : allNucleusList) {
				PixelNode nucleusNode = pixelNucleus.getPixelNode();
				nucleusNode.setIsland(this.island);
				nodeList.add(nucleusNode);
			}
		}
		return nodeList;
	}

	public void setIsland(PixelIsland pixelIsland) {
		this.island = pixelIsland;
	}

	private PixelNucleus getPixelNucleus(Pixel pixel) {
		return this.getPixelNucleusByPixelMap().get(pixel);
	}

	private PixelNode createNode(Pixel pixel) {
		PixelNode node = null;
		PixelNucleus nucleus = getPixelNucleus(pixel);
		if (nucleus != null) {
			node = getPopulatedNodeByNucleusMap().get(nucleus);
			if (node == null) {
				node = new PixelNode(pixel, island);
				nodeByNucleusMap.put(nucleus, node);
				nucleusByNodeMap.put(node, nucleus);
			}
		}
		if (node != null && node.getCentrePixel() == null) {
			node.setCentrePixel(pixel);
		}
		return node;
	}

	private Map<PixelNucleus, PixelNode> getPopulatedNodeByNucleusMap() {
		if (nodeByNucleusMap == null) {
			nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
		}
		if (nodeByNucleusMap.size() == 0) {
			for (PixelNucleus nucleus : allNucleusList) {
				PixelNode node = nucleus.getPixelNode();
				nodeByNucleusMap.put(nucleus, node);
			}
		}
		return nodeByNucleusMap;
	}

	Map<PixelNode, PixelNucleus> getPopulatedNucleusByNodeMap() {
		if (nucleusByNodeMap == null) {
			nucleusByNodeMap = new HashMap<PixelNode, PixelNucleus>();
		}
		if (nucleusByNodeMap.size() == 0) {
			for (PixelNucleus nucleus : allNucleusList) {
				PixelNode node = nucleus.getPixelNode();
				nucleusByNodeMap.put(node, nucleus);
			}
		}
		return nucleusByNodeMap;
	}
	
}
