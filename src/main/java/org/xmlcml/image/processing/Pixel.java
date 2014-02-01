package org.xmlcml.image.processing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;

public class Pixel {

	private final static Logger LOG = Logger.getLogger(Pixel.class);
	
	enum Marked {
		ALL,
		UNUSED,
		USED,
	};
	
	Point point;
	private List<Pixel> neighbourList;
	PixelIsland island;
	private Marked marked = null;

	public Pixel(Point p) {
		this.point = p;
	}

	public Int2 getInt2() {
		return point == null ? null : new Int2(point.x, point.y);
	}

	public List<Pixel> getNeighbours(PixelIsland island) {
		this.island = island;
		ensureNeighbours();
		return neighbourList;
	}

	public List<Pixel> getNeighbours(Marked marked) {
		ensureNeighbours();
//		LOG.debug("neighbours "+neighbourList.size());
		List<Pixel> markedList = new ArrayList<Pixel>();
		for (Pixel pixel : neighbourList) {
			if (pixel.isMarked(marked) || Marked.ALL.equals(marked)) {
				markedList.add(pixel);
			}
		}
		return markedList;
	}

	public boolean isMarked(Marked m) {
		return this.marked == null ? false : this.marked.equals(m);
	}
	
	public void setMarked(Marked m) {
		this.marked = m;
	}

	private void ensureNeighbours() {
		if (neighbourList == null) {
			neighbourList = new ArrayList<Pixel>();
			List<Int2> coordList = calculateNeighbourCoordList();
			for (Int2 coord : coordList) {
				Pixel pixel = island.getPixelByCoordMap().get(coord);
				if (pixel != null) {
					neighbourList.add(pixel);
				}
			}
		}
	}

	private List<Int2> calculateNeighbourCoordList() {
		List<Int2> coordList = new ArrayList<Int2>();
		coordList.add(new Int2(point.x + 1, point.y));
		coordList.add(new Int2(point.x - 1, point.y));
		if (island.allowDiagonal) {
			coordList.add(new Int2(point.x + 1, point.y + 1));
			coordList.add(new Int2(point.x - 1, point.y + 1));
		}
		coordList.add(new Int2(point.x, point.y + 1));
		coordList.add(new Int2(point.x, point.y - 1));
		if (island.allowDiagonal) {
			coordList.add(new Int2(point.x + 1, point.y - 1));
			coordList.add(new Int2(point.x - 1, point.y - 1));
		}
		return coordList;
	}

	public void setIsland(PixelIsland island) {
		this.island = island;
	}

	/**
	Point point;
	private List<Pixel> neighbourList;
	PixelIsland island;
	private Marked marked = null;
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("point: "+((point == null) ? "null" : this.getInt2()));
		sb.append("; neighbours: ");
		if (neighbourList == null) {
			sb.append("null");
		} else {
			for (Pixel neighbour : neighbourList) {
				sb.append(" "+neighbour.getInt2()+" "+neighbour.getMarked());
			}
		}
//		sb.append("island: "+island+"\n");
		sb.append("; marked: "+marked);
		return sb.toString();
	}

	private Marked getMarked() {
		return marked;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((marked == null) ? 0 : marked.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pixel other = (Pixel) obj;
		if (marked != other.marked)
			return false;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		return true;
	}

	public void remove() {
		throw new RuntimeException("NYI");
	}
	
}
