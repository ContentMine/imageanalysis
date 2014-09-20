package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private PixelIsland island;
	private PixelEdgeList edgeList;
	private PixelNodeList nodeList;
	private Map<Pixel, PixelNucleus> nucleusByPixelMap;
	private Map<PixelNode, PixelNucleus> nucleusByNodeMap;
	private Map<PixelNucleus, PixelNode> nodeByNucleusMap;
	private Map<Pixel, PixelNode> nodeByPixelMap;
	private PixelList spikePixelList;
	private Map<Pixel, PixelNucleus> nucleusBySpikePixelMap;
	
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
			getOrCreateNucleusList();
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
			getOrCreateNucleusList();
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
			getOrCreateNucleusList();
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
			getOrCreateNucleusList();
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
			getOrCreateNucleusList();
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
		ensureNucleusByPixelMap();
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
	
	public PixelNucleusList getOrCreateNucleusList() {
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
						LOG.trace("created nucleus: "+pixel+"; "+nucleus+"; "+nucleus.hashCode());
						allNucleusList.add(nucleus);
					}
				}
			}
			LOG.trace("Created nucleusList: "+allNucleusList.toString());
		}
		return allNucleusList;
	}
	
	private void ensureNucleusByPixelMap() {
		if (nucleusByPixelMap == null) {
			nucleusByPixelMap = new HashMap<Pixel, PixelNucleus>();
		}
	}
	
	private void ensureNodeByPixelMap() {
		if (nodeByPixelMap == null) {
			nodeByPixelMap = new HashMap<Pixel, PixelNode>();
		}
	}
	
	private void ensureNodeByNucleusMap() {
		if (nodeByNucleusMap == null) {
			nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
		}
	}
	
	private void ensureNucleusByNodeMap() {
		if (nucleusByNodeMap == null) {
			nucleusByNodeMap = new HashMap<PixelNode, PixelNucleus>();
		}
	}
	
	public Map<Pixel, PixelNucleus> populateMaps() {
		ensureNucleusByPixelMap();
		ensureNucleusByNodeMap();
		ensureNodeByPixelMap();
		ensureNodeByNucleusMap();
		for (PixelNucleus nucleus : allNucleusList) {
			PixelNode node = nucleus.getNode();
			nodeByNucleusMap.put(nucleus, node);
			nucleusByNodeMap.put(node, nucleus);
			PixelList pixelList = nucleus.getPixelList();
			for (Pixel pixel : pixelList) {
				nodeByPixelMap.put(pixel, node);
			}
		}
		return nucleusByPixelMap;
	}

	public PixelNodeList getOrCreateNodeListFromNuclei() {
		if (nodeList == null) {
			getOrCreateNucleusList();
			nodeList = new PixelNodeList();
			for (PixelNucleus pixelNucleus : allNucleusList) {
				PixelNode nucleusNode = pixelNucleus.getNode();
				nucleusNode.setIsland(this.island);
				nodeList.add(nucleusNode);
			}
		}
		return nodeList;
	}

	public void setIsland(PixelIsland pixelIsland) {
		this.island = pixelIsland;
	}

	PixelNucleus getNucleusByPixel(Pixel pixel) {
		populateMaps();
		return this.nucleusByPixelMap.get(pixel);
	}

	PixelNucleus getNucleusByNode(PixelNode node) {
		populateMaps();
		return this.nucleusByNodeMap.get(node);
	}

	PixelNode getNodeByPixel(Pixel pixel) {
		populateMaps();
		return this.nodeByPixelMap.get(pixel);
	}
	
	PixelNode getNodeByNucleus(PixelNucleus nucleus) {
		populateMaps();
		return this.nodeByNucleusMap.get(nucleus);
	}
	
//	private PixelNode createNode(Pixel pixel) {
//		PixelNode node = null;
//		PixelNucleus nucleus = getNucleusByPixel(pixel);
//		if (nucleus != null) {
//			node = getNodeByNucleus(nucleus);
//			if (node == null) {
//				node = new PixelNode(pixel, island);
//				nodeByNucleusMap.put(nucleus, node);
//				nucleusByNodeMap.put(node, nucleus);
//			}
//		}
//		if (node != null && node.getCentrePixel() == null) {
//			node.setCentrePixel(pixel);
//		}
//		return node;
//	}

//	private Map<PixelNucleus, PixelNode> getPopulatedNodeByNucleusMap() {
//		if (nodeByNucleusMap == null) {
//			nodeByNucleusMap = new HashMap<PixelNucleus, PixelNode>();
//		}
//		if (nodeByNucleusMap.size() == 0) {
//			for (PixelNucleus nucleus : allNucleusList) {
//				PixelNode node = nucleus.getPixelNode();
//				nodeByNucleusMap.put(nucleus, node);
//			}
//		}
//		return nodeByNucleusMap;
//	}
//
//	Map<PixelNode, PixelNucleus> getPopulatedNucleusByNodeMap() {
//		if (nucleusByNodeMap == null) {
//			nucleusByNodeMap = new HashMap<PixelNode, PixelNucleus>();
//		}
//		if (nucleusByNodeMap.size() == 0) {
//			for (PixelNucleus nucleus : allNucleusList) {
//				PixelNode node = nucleus.getPixelNode();
//				nucleusByNodeMap.put(node, nucleus);
//			}
//		}
//		return nucleusByNodeMap;
//	}

	public PixelList getOrCreateSpikePixelList() {
		if (spikePixelList == null) {
			getOrCreateNucleusList();
			spikePixelList = createSpikePixelList();
		}
		return spikePixelList;
	}
	
	/** creates an unordered list of all spike pixels.
	 * 
	 * also indexes nuclei by spikes in nucleusBySpikePixelMap
	 * 
	 * @return
	 */
	PixelList createSpikePixelList() {
		nucleusBySpikePixelMap = new HashMap<Pixel, PixelNucleus>();
		PixelList allSpikeList = new PixelList();
		for (PixelNucleus nucleus : allNucleusList) {
			PixelList spikePixelList = nucleus.createSpikePixelList();
			for (Pixel spikePixel : spikePixelList) {
				nucleusBySpikePixelMap.put(spikePixel, nucleus);
			}
			LOG.trace("spikes "+spikePixelList);
			allSpikeList.addAll(spikePixelList);
		}
		LOG.trace("===== all "+allSpikeList);
		return allSpikeList;
	}

	public PixelNucleus getNucleusBySpikePixel(Pixel pixel) {
		if (nucleusBySpikePixelMap == null) {
			createSpikePixelList();
		}
		return nucleusBySpikePixelMap.get(pixel);
	}

	public PixelEdgeList createPixelEdgeListFromNodeList() {
		getOrCreateNodeListFromNuclei();
		PixelEdgeList edgeList = new PixelEdgeList();
		getOrCreateSpikePixelList();
		return edgeList;
	}

	public PixelList findLine(PixelNucleus nucleus, Pixel spike) {
		PixelList neighbours = spike.getOrCreateNeighbours(island);
		if (neighbours.size() != 2) {
			throw new RuntimeException("spike must have two neighbours");
		}
		int nucleusNeighbourIndex = -1;
		if (getNucleusByPixel(neighbours.get(0)) != null) {
			nucleusNeighbourIndex = 0;
		} else if (getNucleusByPixel(neighbours.get(1)) != null) {
			nucleusNeighbourIndex = 1;
		} else {
			throw new RuntimeException("No neighbour in nucleus");
		}
		Pixel nextPixel = neighbours.get(1 - nucleusNeighbourIndex);
		PixelList line = findLine(neighbours.get(nucleusNeighbourIndex), spike);
		return line;


	}

	private PixelList findLine(Pixel lastPixel, Pixel thisPixel) {
		PixelList line = new PixelList();
		line.add(lastPixel);
		while (true) {
			line.add(thisPixel);
			// have we hit another nucleus?
			if (getNucleusByPixel(thisPixel) != null) {
				break;
			}
			Pixel nextPixel = thisPixel.getNextNeighbourIn2ConnectedChain(lastPixel);
			lastPixel = thisPixel;
			thisPixel = nextPixel;
		}
		return line;
	}

	public PixelEdge createEdgeFromLine(PixelList line) {
		PixelEdge edge = new PixelEdge(island);
		edge.addPixelList(line);
		addNodeToEdge(line, edge, 0);
		addNodeToEdge(line, edge, 1);
		return edge;
	}

	/** finds node and adds to edge
	 * 
	 * @param line of pixels
	 * @param edge to add to
	 * @param nodePos 0/1 start/end of line
	 */
	private void addNodeToEdge(PixelList line, PixelEdge edge, int nodePos) {
		int pixelPos = (nodePos == 0) ? 0 : line.size() - 1;
		PixelNode node = getNodeByLookupOrThroughNucleus(line.get(pixelPos));
		if (node != null) {
			edge.addNode(node, nodePos);
		}
	}

	private PixelNode getNodeByLookupOrThroughNucleus(Pixel pixel) {
		PixelNode node = getNodeByPixel(pixel);
		if (node == null) {
			PixelNucleus nucleus = getNucleusByPixel(pixel);
			if (nucleus == null) {
				throw new RuntimeException("Cannot find nucleus for edge end pixel: "+pixel);
			}
			node = nucleus.getNode();
			Pixel centrePixel = node.getCentrePixel();
			if (centrePixel == null || !pixel.isNeighbour(centrePixel)) {
				throw new RuntimeException("node is not joined to edge: "+pixel);
			}
		}
		return node;
	}

	public void addEdge(PixelEdge edge) {
		ensureEdgeList();
		edgeList.add(edge);
	}

	private void ensureEdgeList() {
		if (edgeList == null) {
			edgeList = new PixelEdgeList();
		}
	}

	public PixelEdgeList getEdgeList() {
		ensureEdgeList();
		return edgeList;
	}

	public void createNodesAndEdges() {
		PixelList spikeList = getOrCreateSpikePixelList();
		PixelSet spikeSet = new PixelSet(spikeList);
		int maxCount = 1000000;
		while (!spikeSet.isEmpty() && maxCount-- > 0) {
			getNextSpikeTraceEdgeAndDeleteBothSpikeEnds(spikeSet);
		}
	}

	private void getNextSpikeTraceEdgeAndDeleteBothSpikeEnds(PixelSet spikeSet) {
		Pixel pixel = spikeSet.next();
		PixelNucleus nucleus = getNucleusBySpikePixel(pixel);
		spikeSet.remove(pixel);
		PixelList line = findLine(nucleus, pixel);
		Pixel lastSpike = line.penultimate();
		spikeSet.remove(lastSpike);
		PixelEdge edge = createEdgeFromLine(line);
		if (edge == null) {
			throw new RuntimeException("null edge");
		}
		addEdge(edge);
	}

}
