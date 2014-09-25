package org.xmlcml.image.pixel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xmlcml.image.pixel.PixelNucleus.PixelJunctionType;
import org.xmlcml.image.pixel.nucleus.CrossNucleus;
import org.xmlcml.image.pixel.nucleus.DotNucleus;
import org.xmlcml.image.pixel.nucleus.EightPlusPixelNucleus;
import org.xmlcml.image.pixel.nucleus.FivePixelNucleus;
import org.xmlcml.image.pixel.nucleus.FourPixelNucleus;
import org.xmlcml.image.pixel.nucleus.SixSevenPixelNucleus;
import org.xmlcml.image.pixel.nucleus.TerminalNucleus;
import org.xmlcml.image.pixel.nucleus.ThreeWayNucleus;

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

	private PixelNucleusList fourWayJunctionList;
//	private PixelNucleusList fivePixelJunctionList;
//	private PixelNucleusList sixSevenPixelJunctionList;
	private PixelNucleusList eightPlusPixelJunctionList;
	private PixelNucleusList dotJunctionList;
//	private PixelNucleusList doubleYJunctionList;
	private PixelNucleusList terminalJunctionList;
	private PixelNucleusList threeWayJunctionList;
//	private PixelNucleusList tJunctionList;
//	private PixelNucleusList yJunctionList;
	
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
	private PixelSet unusedPixelSet;
	
	public PixelNucleusFactory(PixelIsland island) {
		LOG.trace("MAKING FACTORY from "+island.hashCode()+"; "+island.pixelList.size());
		this.island = island;
		island.setNucleusFactory(this);
		indexJunctions();
	}
	
	public PixelIsland getPixelIsland() {
		return island;
	}
	
	public PixelNucleusList getOrCreateDotJunctionList() {
		if (dotJunctionList == null) {
			dotJunctionList = new PixelNucleusList();
			addPixelNuclei(DotNucleus.class, dotJunctionList);
		}
		return dotJunctionList;
	}

	public PixelNucleusList getOrCreateTerminalJunctionList() {
		if (terminalJunctionList == null) {
			terminalJunctionList = new PixelNucleusList();
			addPixelNuclei(TerminalNucleus.class, terminalJunctionList);
		}
		return terminalJunctionList;
	}

	public PixelNucleusList getOrCreateThreeWayJunctionList() {
		if (threeWayJunctionList == null) {
			threeWayJunctionList = new PixelNucleusList();
			addPixelNuclei(ThreeWayNucleus.class, threeWayJunctionList);
		}
		return threeWayJunctionList;
	}

	public PixelNucleusList getOrCreateFourWayJunctionList() {
		if (fourWayJunctionList == null) {
			fourWayJunctionList = new PixelNucleusList();
		}
		return fourWayJunctionList;
	}
	
	public PixelNucleusList getOrCreateEightPlusPixelJunctionList() {
		if (eightPlusPixelJunctionList == null) {
			eightPlusPixelJunctionList = new PixelNucleusList();
		}
		return eightPlusPixelJunctionList;
	}
	
	private void addPixelNuclei(Class<? extends PixelNucleus> nucleusClass, PixelNucleusList junctionList) {
		for (PixelNucleus nucleus : allNucleusList) {
			if (nucleus.getClass().equals(nucleusClass)) {
				junctionList.add(nucleus);
			}
		}
	}

	private void indexJunctions() {
		ensureNucleusByPixelMap();
		getOrCreateNucleusList();
		ensureJunctionLists();
		for (PixelNucleus nucleus : allNucleusList) {
//			indexNucleusTypeAndAddToLists(nucleus);
			for (Pixel pixel : nucleus.getPixelList()) {
				nucleusByPixelMap.put(pixel, nucleus);
			}
		}
		return;
	}

	private void ensureJunctionLists() {
		dotJunctionList = new PixelNucleusList();
		terminalJunctionList = new PixelNucleusList();
		threeWayJunctionList = new PixelNucleusList();
		fourWayJunctionList = new PixelNucleusList();
		eightPlusPixelJunctionList = new PixelNucleusList();
	}
	
	@Deprecated
	public PixelNucleusList getOrCreateNucleusListOld() {
		if (allNucleusList == null) {
			allNucleusList = new PixelNucleusList();
			LOG.trace(this.hashCode()+"; NucleusList pixelList:"+island.pixelList.size());
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
//				allNucleusList.mergeTouchingNuclei();
			}
			LOG.trace("Created nucleusList: "+allNucleusList.toString());
		}
		return allNucleusList;
	}
	
	public PixelNucleusList getOrCreateNucleusList() {
		if (allNucleusList == null) {
			allNucleusList = new PixelNucleusList();
			unusedPixelSet = new PixelSet(island.pixelList);
			makeDotAndTerminalNuclei();
			makeNonTerminalNuclei();
			LOG.trace("Created nucleusList: "+allNucleusList.size());
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
				LOG.trace("created non-terminal nucleus: px: "+pixel+"; n: "+nucleus.hashCode()+"; "+nucleus.getJunctionType()+"; "+nucleus.getPixelList());
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
		PixelSet seedSet = new PixelSet();
		seedSet.add(seed);
		PixelSet usedSet = new PixelSet();
		while (!seedSet.isEmpty()) {
			Pixel pixel = seedSet.next();
			seedSet.remove(pixel);
			PixelList neighbours = pixel.getOrCreateNeighbours(island);
			LOG.trace("next pixel: "+pixel+"; "+neighbours);
			addNeighboursWith3orMoreNeighbours(island, seedSet, usedSet, neighbours);
			usedSet.add(pixel);
		}
		PixelList nucleusPixelList = new PixelList(new ArrayList<Pixel>(usedSet));
		LOG.trace("making nucleus "+nucleusPixelList);
		PixelNucleus nucleus = createSubtypedNucleus(nucleusPixelList);
		if (nucleus == null) {
			throw new RuntimeException("NULL NUCLEUS: "+seed+"; "+nucleusPixelList);
		}
		nucleus.addAll(usedSet);
		return nucleus;
	}

	private void addNeighboursWith3orMoreNeighbours(PixelIsland island, PixelSet seedSet, PixelSet used, PixelList neighbours) {
		for (Pixel neighbour : neighbours) {
			if (neighbour.getOrCreateNeighbours(island).size() > 2) {
				if (!used.contains(neighbour)) {
					seedSet.add(neighbour);
					LOG.trace("added "+neighbour+" to "+seedSet);
				}
				used.add(neighbour);
				LOG.trace("used "+used);
			}
		}
		LOG.trace("======added neighbours: "+used);
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

	//====================subtyped Nuclei==========
	
	PixelNucleus createSubtypedNucleus(PixelList pixelList) {
		PixelNucleus newNucleus = null;
		Pixel centrePixel = null;
		// DOT
		if (pixelList.size() == 1) {
			newNucleus = process1PixelNuclei(pixelList);

		} else if (pixelList.size() == 3) {
			newNucleus = process3PixelNuclei(pixelList);
			
		} else if (pixelList.size() == 4) {
			newNucleus = process4PixelNuclei(pixelList, centrePixel);
			
		} else if (pixelList.size() == 5) {
			newNucleus = process5PixelNuclei(pixelList, centrePixel);
			
		} else if (pixelList.size() == 6 || pixelList.size() == 7) {
			newNucleus = new SixSevenPixelNucleus(centrePixel, pixelList, island);
			
		} else if (pixelList.size() >= 8) {
			newNucleus = new EightPlusPixelNucleus(centrePixel, pixelList, island);
			
		}
		
		return newNucleus;
	}

	private PixelNucleus process5PixelNuclei(PixelList pixelList, Pixel centrePixel) {
		PixelNucleus newNucleus = null;
		int corner = CrossNucleus.getCrossCentre(centrePixel, pixelList, island);
		if (corner != -1) {
			newNucleus = new CrossNucleus(centrePixel, pixelList, island);
		} else {
			newNucleus = new FivePixelNucleus(centrePixel, pixelList, island);
		}
		return newNucleus;
	}

	private PixelNucleus process4PixelNuclei(PixelList pixelList, Pixel centrePixel) {
		PixelNucleus newNucleus = null;
		if (isFilledT(centrePixel, pixelList, island)) {
			newNucleus = new ThreeWayNucleus(centrePixel, pixelList, island);
		} else {
			LOG.error("UNKNOWN 4 PIXEL NUCLEUS");
			newNucleus = new FourPixelNucleus(centrePixel, pixelList, island);
		}
		return newNucleus;
	}

	private PixelNucleus process3PixelNuclei(PixelList pixelList) {
		PixelNucleus newNucleus = null;
		Pixel centrePixel;
		int corner = getRightAngleCorner(pixelList);
		if (corner != -1) {
			centrePixel = pixelList.get(corner);
			newNucleus = new ThreeWayNucleus(centrePixel, pixelList, island);
		} else {
			LOG.error("UNKNOWN 3 PIXEL NUCLEUS");
		}
		return newNucleus;
	}

	private PixelNucleus process1PixelNuclei(PixelList pixelList) {
		PixelNucleus newNucleus = null;
		Pixel centrePixel;
		centrePixel = pixelList.get(0);
		if (centrePixel.getOrthogonalNeighbours(island).size() 
				+ centrePixel.getDiagonalNeighbours(island).size() == 0) {
			newNucleus = new DotNucleus(centrePixel, pixelList, island);
			LOG.trace("made DOT");
			
		} else if (centrePixel.getOrthogonalNeighbours(island).size() 
				+ centrePixel.getDiagonalNeighbours(island).size() == 1) {
			newNucleus = new TerminalNucleus(centrePixel, pixelList, island);
			LOG.debug("made TERMINAL");
			
		} else if (isNickedT(centrePixel, island)) {
			newNucleus = new ThreeWayNucleus(centrePixel, pixelList, island);
			LOG.trace("made NICKED_T");
			
		} else {
			LOG.error("UNKNOWN SINGLE PIXEL NUCLEUS: "+pixelList+"; "+pixelList.get(0).getOrCreateNeighbours(island));
		}
		return newNucleus;
	}

	private int getRightAngleCorner(PixelList pixelList) {
		int rightAngleCorner = -1;
		for (int i = 0; i < 3; i++) {
			int j = (i + 1) % 3;
			int k = (j + 1) % 3;
			if (Pixel.isRightAngle(pixelList.get(i), pixelList.get(j),
					pixelList.get(k))) {
				rightAngleCorner = i;
				break;
			}
		}
		return rightAngleCorner;
	}

	/** symmetric about vertical stem.
	 * 
	 * @return
	 */
	private boolean isNickedT(Pixel centrePixel, PixelIsland island) {
		if (centrePixel != null) {
			PixelList diagonalNeighbours = centrePixel.getDiagonalNeighbours(island);
			PixelList orthogonalNeighbours = centrePixel.getOrthogonalNeighbours(island);
			if (diagonalNeighbours.size() == 2 && orthogonalNeighbours.size() == 1) {
				return true;
			}
		}
		return false;
	}

	private boolean isFilledT(Pixel centrePixel, PixelList pixelList, PixelIsland pixelIsland) {
		for (Pixel pixel : pixelList) {
			if (pixel.getOrthogonalNeighbours(island).size() == 3) {
				if (centrePixel != null) {
					throw new RuntimeException("Not a filled TJunction "+this);
				}
				centrePixel = pixel;
			}
		}
		return (centrePixel != null);
	}

	/** two diagonal Y's joined by stems.
	 * 
	 *  +
	 * ++
	 *   ++
	 *   +
	 *   
	 *   centre pixel will be randomly one of the two central pixels
	 * @return
	 */
//	public boolean isDoubleYJunction() {
//		if (getJunctionType() == null) {
//			if (pixelList.size() == 6) {
//				PixelList centres = new PixelList();
//				for (Pixel pixel : pixelList) {
//					PixelList orthNeighbours = pixel.getOrthogonalNeighbours(island);
//					if (orthNeighbours.size() == 2 && 
//							orthNeighbours.get(0).isDiagonalNeighbour(orthNeighbours.get(1)))  {
//						centres.add(pixel);
//					}
//				}
//				if (centres.size() == 2 && centres.get(0).isDiagonalNeighbour(centres.get(1))) {
//					centrePixel = centres.get(0); // arbitrary but?
//					setJunctionType(PixelJunctionType.DOUBLEY);
//				}
//			}
//		}
//		return PixelJunctionType.DOUBLEY.equals(getJunctionType());
//	}

}
