package org.xmlcml.image.pixel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.image.pixel.PixelNucleus.PixelJunctionType;

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
	private PixelNucleusList fivePixelJunctionList;
	private PixelNucleusList sixPixelJunctionList;
	private PixelNucleusList sevenPixelJunctionList;
	private PixelNucleusList eightOrMorePixelJunctionList;
	private PixelNucleusList dotJunctionList;
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
//	private Set<Pixel> terminalPixels;
	private PixelSet unusedPixelSet;
	
	public PixelNucleusFactory(PixelIsland island) {
		this.island = island;
		island.setNucleusFactory(this);
		indexJunctions();
	}
	
	public PixelIsland getPixelIsland() {
		return island;
	}
	
	public PixelNucleusList getOrCreateCrossJunctionList() {
		if (crossJunctionList == null) {
			crossJunctionList = new PixelNucleusList();
		}
		return crossJunctionList;
	}
	public PixelNucleusList getOrCreateDoubleLJunctionList() {
		if (sixPixelJunctionList == null) {
			sixPixelJunctionList = new PixelNucleusList();
		}
		return sixPixelJunctionList;
	}
	public PixelNucleusList getOrCreateDoubleYJunctionList() {
		if (doubleYJunctionList == null) {
			doubleYJunctionList = new PixelNucleusList();
		}
		return doubleYJunctionList;
	}
	public PixelNucleusList getOrCreateTerminalJunctionList() {
		if (terminalJunctionList == null) {
			terminalJunctionList = new PixelNucleusList();
		}
		return terminalJunctionList;
	}
	public PixelNucleusList getOrCreateTJunctionList() {
		if (tJunctionList == null) {
			tJunctionList = new PixelNucleusList();
		}
		return tJunctionList;
	}
	
	public PixelNucleusList getOrCreateYJunctionList() {
		if (yJunctionList == null) {
			yJunctionList = new PixelNucleusList();
		}
		return yJunctionList;
	}
	
	public PixelNucleusList getOrCreateFivePixelJunctionList() {
		if (fivePixelJunctionList == null) {
			fivePixelJunctionList = new PixelNucleusList();
		}
		return fivePixelJunctionList;
	}
	
	public PixelNucleusList getOrCreateSevenPixelJunctionList() {
		if (sevenPixelJunctionList == null) {
			sevenPixelJunctionList = new PixelNucleusList();
		}
		return sevenPixelJunctionList;
	}
	
	public PixelNucleusList getOrCreateEightOrMorePixelJunctionList() {
		if (eightOrMorePixelJunctionList == null) {
			eightOrMorePixelJunctionList = new PixelNucleusList();
		}
		return eightOrMorePixelJunctionList;
	}
	
	private void indexJunctions() {
		ensureNucleusByPixelMap();
		getOrCreateNucleusList();
		ensureJunctionLists();
		for (PixelNucleus nucleus : allNucleusList) {
			indexNucleusTypeAndAddToLists(nucleus);
			for (Pixel pixel : nucleus.getPixelList()) {
				nucleusByPixelMap.put(pixel, nucleus);
			}
		}


	}

	private void indexNucleusTypeAndAddToLists(PixelNucleus nucleus) {
		// in order of size 
		// zero
		if (nucleus.isDotJunction()) {
			dotJunctionList.add(nucleus);
			// one
		} else if (nucleus.isTerminalJunction()) {
			terminalJunctionList.add(nucleus);
			// threeway
		} else if (nucleus.isYJunction()) {
			yJunctionList.add(nucleus);
		} else if (nucleus.isTJunction()) {
			tJunctionList.add(nucleus);
			// cross (fourway)
		} else if (nucleus.isCrossJunction()) {
			crossJunctionList.add(nucleus);
			// probably 3-way
		} else if (nucleus.isFivePixelJunction()) {
			fivePixelJunctionList.add(nucleus);

			// fourway
		} else if (nucleus.isDoubleYJunction()) {
			doubleYJunctionList.add(nucleus);
			// three
		} else if (nucleus.isSixPixelJunction()) {
			sixPixelJunctionList.add(nucleus);
		} else if (nucleus.isSevenPixelJunction()) {
			sevenPixelJunctionList.add(nucleus);
			// others
		} else if (nucleus.isEightOrMorePixelJunction()) {
			eightOrMorePixelJunctionList.add(nucleus);
			// others (probably thinning errors)
		} else {
			LOG.error("Unknown nucleus: "+nucleus);
		}
	}

	private void ensureJunctionLists() {
		crossJunctionList = new PixelNucleusList();
		dotJunctionList = new PixelNucleusList();
		doubleYJunctionList = new PixelNucleusList();
		fivePixelJunctionList = new PixelNucleusList();
		sixPixelJunctionList = new PixelNucleusList();
		sevenPixelJunctionList = new PixelNucleusList();
		eightOrMorePixelJunctionList = new PixelNucleusList();
		tJunctionList = new PixelNucleusList();
		terminalJunctionList = new PixelNucleusList();
		yJunctionList = new PixelNucleusList();
	}
		
	public PixelNucleusList getThreewayJunctionList() {
		PixelNucleusList threewayList = new PixelNucleusList();
		threewayList.addAll(getOrCreateTJunctionList());
		threewayList.addAll(getOrCreateYJunctionList());
		return threewayList;
	}
	
	@Deprecated
	public PixelNucleusList getOrCreateNucleusListOld() {
		if (allNucleusList == null) {
			allNucleusList = new PixelNucleusList();
			LOG.debug(this.hashCode()+"; NucleusList pixelList:"+island.pixelList.size());
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
						LOG.debug("created nucleus: "+pixel+"; "+nucleus+"; "+nucleus.hashCode());
						allNucleusList.add(nucleus);
					}
				}
				allNucleusList.mergeTouchingNuclei();
			}
			LOG.debug("Created nucleusList: "+allNucleusList.toString());
		}
		return allNucleusList;
	}
	
	public PixelNucleusList getOrCreateNucleusList() {
		if (allNucleusList == null) {
			allNucleusList = new PixelNucleusList();
			unusedPixelSet = new PixelSet(island.pixelList);
			makeDotAndTerminalNuclei();
			makeNonTerminalNuclei();
			LOG.trace("Created nucleusList: "+allNucleusList.toString());
		}
		return allNucleusList;
	}

	private void makeNonTerminalNuclei() {
		while (!unusedPixelSet.isEmpty()) {
			Pixel pixel = unusedPixelSet.next();
			unusedPixelSet.remove(pixel);
			PixelList neighbours = pixel.getOrCreateNeighbours(island);
			if (neighbours.size() > 2) {
				PixelNucleus nucleus = makeNucleusFromSeed(pixel, island);
				allNucleusList.add(nucleus);
				unusedPixelSet.removeAll(nucleus.getPixelList().getList());
				LOG.trace("created nucleus: "+pixel+"; "+nucleus+"; "+nucleus.hashCode());
			} else if (neighbours.size() == 2 || neighbours.size() == 0) {
				// skip these
			} else {
				throw new RuntimeException("Should have processed this: "+pixel+"; ");
			}
		}
	}
	
	private void makeDotAndTerminalNuclei() {
		PixelList dotPixelList = get0ConnectedPixelList();
		for (Pixel pixel : dotPixelList) {
			unusedPixelSet.remove(pixel);
			PixelNucleus nucleus = new PixelNucleus(island);
			nucleus.setJunctionType(PixelJunctionType.DOT);
			nucleus.add(pixel);
			allNucleusList.add(nucleus);
		}
		PixelList terminalPixelList = get1ConnectedPixelList();
		for (Pixel pixel : terminalPixelList) {
			unusedPixelSet.remove(pixel);
			PixelNucleus nucleus = new PixelNucleus(island);
			nucleus.setJunctionType(PixelJunctionType.TERMINAL);
			nucleus.add(pixel);
			allNucleusList.add(nucleus);
		}
	}

	/** list of 0-connected pixels.
	 * 
	 * @return
	 */
	public PixelList get0ConnectedPixelList() {
		PixelList dots = new PixelList();
		for (Pixel pixel : island.getPixelList()) {
			if (pixel.getOrCreateNeighbours(island).size() == 0) {
				dots.add(pixel);
			}
		}
		return dots;
	}

	/** list of 1-connected pixels.
	 * 
	 * @return
	 */
	public PixelList get1ConnectedPixelList() {
		PixelList terminals = new PixelList();
		for (Pixel pixel : island.getPixelList()) {
			if (pixel.getOrCreateNeighbours(island).size() == 1) {
				terminals.add(pixel);
			}
		}
		return terminals;
	}

	private PixelNucleus makeNucleusFromSeed(Pixel seed, PixelIsland island) {
		PixelNucleus nucleus = new PixelNucleus(island);
		PixelSet seedSet = new PixelSet();
		seedSet.add(seed);
		PixelSet usedSet = new PixelSet();
		while (!seedSet.isEmpty()) {
			Pixel pixel = seedSet.next();
			seedSet.remove(pixel);
			usedSet.add(pixel);
			PixelList neighbours = pixel.getOrCreateNeighbours(island);
			addNeighboursWith3orMoreNeighbours(island, seedSet, usedSet, neighbours);
		}
		nucleus.addAll(usedSet);
		return nucleus;
	}

	private void addNeighboursWith3orMoreNeighbours(PixelIsland island, PixelSet set, PixelSet used, PixelList neighbours) {
		for (Pixel neighbour : neighbours) {
			if (neighbour.getOrCreateNeighbours(island).size() > 2) {
				if (!used.contains(neighbour)) {
					set.add(neighbour);
				}
			}
		}
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
	
	public Map<Pixel, PixelNucleus> ensurePopulatedMaps() {
		ensureNucleusByPixelMap();
		ensureNucleusByNodeMap();
		ensureNodeByPixelMap();
		ensureNodeByNucleusMap();
		if (nucleusByPixelMap.size() == 0) {
			for (PixelNucleus nucleus : allNucleusList) {
				PixelNode node = nucleus.getNode();
				nodeByNucleusMap.put(nucleus, node);
				nucleusByNodeMap.put(node, nucleus);
				PixelList pixelList = nucleus.getPixelList();
				for (Pixel pixel : pixelList) {
					nodeByPixelMap.put(pixel, node);
					nucleusByPixelMap.put(pixel, nucleus);
				}
			}
		}
		return nucleusByPixelMap;
	}

	public PixelNodeList getOrCreateNodeListFromNuclei() {
		if (nodeList == null) {
			getOrCreateNucleusList();
			nodeList = new PixelNodeList();
			for (PixelNucleus nucleus : allNucleusList) {
				PixelNode nucleusNode = nucleus.getNode();
				if (nucleusNode == null) {
					LOG.debug("Null node for nucleus:" + nucleus);
				} else {
					nucleusNode.setIsland(this.island);
					nodeList.add(nucleusNode);
				}
			}
		}
		return nodeList;
	}

	public void setIsland(PixelIsland pixelIsland) {
		this.island = pixelIsland;
	}

	PixelNucleus getNucleusByPixel(Pixel pixel) {
		ensurePopulatedMaps();
		return this.nucleusByPixelMap.get(pixel);
	}

	PixelNucleus getNucleusByNode(PixelNode node) {
		ensurePopulatedMaps();
		return this.nucleusByNodeMap.get(node);
	}

	PixelNode getNodeByPixel(Pixel pixel) {
		ensurePopulatedMaps();
		return this.nodeByPixelMap.get(pixel);
	}
	
	PixelNode getNodeByNucleus(PixelNucleus nucleus) {
		ensurePopulatedMaps();
		return this.nodeByNucleusMap.get(nucleus);
	}
	
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
			LOG.trace("made spikePixelList");
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
		PixelList line = null;
		if (neighbours.size() > 2) {
			LOG.debug("spike too many neighbours: "+spike+";"+spike.getOrCreateNeighbours(island)+"; "+nucleus);
		} else if (neighbours.size() != 2) {
			LOG.debug("spike too few neighbours:"+spike+";"+spike.getOrCreateNeighbours(island)+"; "+nucleus);
		} else {
			int nucleusNeighbourIndex = -1;
			if (getNucleusByPixel(neighbours.get(0)) != null) {
				nucleusNeighbourIndex = 0;
			} else if (getNucleusByPixel(neighbours.get(1)) != null) {
				nucleusNeighbourIndex = 1;
			} else {
				LOG.error("No neighbour in nucleus");
			}
			if (nucleusNeighbourIndex != -1) {
				line = findLine(neighbours.get(nucleusNeighbourIndex), spike);
			}
		}
		return line;


	}

	private PixelList findLine(Pixel lastPixel, Pixel thisPixel) {
		PixelList line = new PixelList();
		line.add(lastPixel);
		while (true) {
			line.add(thisPixel);
			// have we hit another nucleus?
			if (thisPixel == null || getNucleusByPixel(thisPixel) != null) {
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
				LOG.error("Cannot find nucleus for edge end pixel: "+pixel);
			} else {
				node = nucleus.getNode();
				Pixel centrePixel = node.getCentrePixel();
				if (centrePixel == null) {
					LOG.error("null centrePixel for: "+node+"; "+nucleus);
				} else if (pixel.equals(centrePixel)) {
					// this is fine
				} else if (!pixel.isNeighbour(centrePixel)) {
					LOG.error("edgeEnd: "+pixel+" is not joined to node "+node);
				}
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
		if (edgeList.size() == 0) {
			createNodesAndEdges();
		}
		return edgeList;
	}

	public void createNodesAndEdges() {
		PixelList spikeList = getOrCreateSpikePixelList();
		PixelSet spikeSet = new PixelSet(spikeList);
		LOG.trace("made spikeSet");
		int maxCount = 1000000;
		while (!spikeSet.isEmpty() && maxCount-- > 0) {
			getNextSpikeTraceEdgeAndDeleteBothSpikeEnds(spikeSet);
		}
		LOG.trace("createdEdges");
	}

	private void getNextSpikeTraceEdgeAndDeleteBothSpikeEnds(PixelSet spikeSet) {
		Pixel pixel = spikeSet.next();
		PixelNucleus nucleus = getNucleusBySpikePixel(pixel);
		spikeSet.remove(pixel);
		PixelList line = findLine(nucleus, pixel);
		if (line == null) {
			LOG.debug("null line");
		} else {
			Pixel lastSpike = line.penultimate();
			spikeSet.remove(lastSpike);
			PixelEdge edge = createEdgeFromLine(line);
			if (edge == null) {
				throw new RuntimeException("cannot create edge: null");
			}
			addEdge(edge);
		}
	}

	public PixelNucleusList getOrCreateYXSortedNucleusList(double tolerance) {
		getOrCreateNucleusList();
		allNucleusList.sortYX(tolerance);
		return allNucleusList;
	}

	public PixelList createYXSortedSpikePixelList() {
		PixelList spikePixelList = createSpikePixelList();
		spikePixelList.sortYX();
		return spikePixelList;
	}

}
