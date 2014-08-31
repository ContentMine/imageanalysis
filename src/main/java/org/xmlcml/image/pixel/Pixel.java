package org.xmlcml.image.pixel;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGRect;

public class Pixel {

	private final static Logger LOG = Logger.getLogger(Pixel.class);
	
	enum Marked {
		ALL,
		UNUSED,
		USED,
	};
	
	Point point;
	private PixelList neighbourList;
	PixelIsland island;
	private Marked marked = null;
	int value = 0;

	public Pixel(Point p) {
		this.point = p;
	}

	public Pixel(int x, int y) {
		this(new Point(x, y));
	}

	public Int2 getInt2() {
		return point == null ? null : new Int2(point.x, point.y);
	}

	public PixelList getNeighbours(PixelIsland island) {
		this.island = island;
		ensureNeighbours();
		return neighbourList;
	}

	public PixelList getNeighbours(Marked marked) {
		ensureNeighbours();
//		LOG.debug("neighbours "+neighbourList.size());
		PixelList markedList = new PixelList();
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
			neighbourList = new PixelList();
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
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(""+((point == null) ? "null" : this.getInt2()));
		return sb.toString();
	}

	public String neighboursString() {
		StringBuilder sb = new StringBuilder();
		sb.append("; neighbours: ");
		if (neighbourList == null) {
			sb.append("null");
		} else {
			for (Pixel neighbour : neighbourList) {
				sb.append(" "+neighbour.getInt2()+" "+neighbour.getMarked());
			}
		}
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
		throw new RuntimeException("remove pixel NYI");
	}

	public void setInt2(Int2 int2) {
		point = new Point(int2.getX(), int2.getY());
	}

	public Set<PixelTriangle> getTriangles(PixelIsland island) {
		Set<PixelTriangle> triangleSet = new HashSet<PixelTriangle>();
		PixelList neighbours = this.getNeighbours(island);
		for (int i = 0; i < neighbours.size() - 1; i++) {
			for (int j = i+1; j < neighbours.size(); j++) {
				PixelTriangle triangle = PixelTriangle.createTriangle(this, neighbours.get(i), neighbours.get(j), island);
				if (triangle != null ) {
					triangleSet.add(triangle);
				}
			}
		}
		return triangleSet;
	}

	public void clearNeighbours() {
		this.neighbourList = null;
	}

	public SVGRect getSVGRect() {
		return getSVGRect(1, "red");
	}

	public SVGRect getSVGRect(int size, String color) {
		SVGRect rect = new SVGRect(
				new Real2(this.point.getX(), this.point.getY()),
				new Real2(this.point.getX() + size, this.point.getY() + size));
		rect.setFill(color);
		rect.setStroke(null);
		return rect;
	}

	public static Real2Array createReal2Array(PixelList pixelList) {
		Real2Array array = null;
		if (pixelList != null) {
			array = new Real2Array();
			for (Pixel pixel : pixelList) {
				array.add(new Real2(pixel.getInt2()));
			}
		}
		return array;
	}

	public void setValue(int v) {
		this.value = v;
	}

	public int getValue() {
		return value;
	}

	public boolean isOrthogonalNeighbour(Pixel pixel) {
		Int2 vector = this.getInt2().subtract(pixel.getInt2());
		return Math.abs(vector.getX()) + Math.abs(vector.getY()) == 1;
	}

	public boolean isDiagonalNeighbour(Pixel pixel) {
		Int2 vector = this.getInt2().subtract(pixel.getInt2());
		return Math.abs(vector.getX()) == 1 && Math.abs(vector.getY()) == 1;
	}

	public boolean isKnightsMove(Pixel pixel) {
		Int2 vector = this.getInt2().subtract(pixel.getInt2());
		return Math.abs(vector.getX()) + Math.abs(vector.getY()) == 3 &&
				Math.abs(Math.abs(vector.getX()) - Math.abs(vector.getY())) == 1;
	}

	public boolean isKnightsMove(Pixel centre, Pixel target) {
		boolean b1 = this.isOrthogonalNeighbour(centre) && centre.isDiagonalNeighbour(target);
		boolean b2 = target.isOrthogonalNeighbour(centre) && centre.isDiagonalNeighbour(this);
		return b1 || b2;
	}

	/** compares pixels by x,y values.
	 * 
	 * first compares Y and if equal then X
	 * 
	 * @param pixel1
	 * @return -1 if this.Int2 < pixel1.Int2, 1 if this.Int2 > pixel1.Int2, else 0
	 */
	public int compareTo(Pixel pixel1) {
		Int2 xy0 = this.getInt2();
		Int2 xy1 = pixel1.getInt2();
		int compare = new Integer(xy0.getY()).compareTo(new Integer(xy1.getY()));
		if (compare == 0) {
			compare = new Integer(xy0.getX()).compareTo(new Integer(xy1.getX()));
		}
		return compare;
	}
	
	PixelList getDiagonalNeighbours(PixelIsland island) {
		PixelList neighbours = getNeighbours(island);
		PixelList pixelList = new PixelList();
		for (Pixel neighbour : neighbours) {
			if (isDiagonalNeighbour(neighbour)) {
				pixelList.add(neighbour);
			}
		}
		return pixelList;
	}

	PixelList getOrthogonalNeighbours(PixelIsland island) {
		PixelList neighbours = getNeighbours(island);
		PixelList pixelList = new PixelList();
		for (Pixel neighbour : neighbours) {
			if (isOrthogonalNeighbour(neighbour)) {
				pixelList.add(neighbour);
			}
		}
		return pixelList;
	}

	boolean isConnectedAny(PixelIsland island, int neighbourCount) {
		PixelList neighbours = getNeighbours(island);
		boolean connected = (neighbours.size() != neighbourCount) ? false : true;
		return connected;
	}

	boolean is1ConnectedAny(PixelIsland island) {
		PixelList neighbours = getNeighbours(island);
		boolean connected = (neighbours.size() != 1) ? false : true;
		return connected;
	}

	boolean is2ConnectedAny(PixelIsland island) {
		PixelList neighbours = getNeighbours(island);
		boolean connected = (neighbours.size() != 2) ? false : true;
		return connected;
	}

	/** get mass centre of pixels with units weights.
	 * 
	 * @param pixels
	 * @return mean or null if pixels is null or size 0
	 */
	public static Real2 getCentre(Collection<Pixel> pixels) {
		Real2 centre = null;
		if (pixels != null && pixels.size() > 0) {
			Real2Array coords = new Real2Array();
			for (Pixel pixel : pixels) {
				coords.add(new Real2(pixel.getInt2()));
			}
			centre = coords.getMean();
		}
		return centre;
	}

	public Pixel getNextNeighbourIn2ConnectedChain(Pixel last) {
		Pixel next = null;
		if (this.is2ConnectedAny(island)) {
			PixelList neighbours = this.getNeighbours(island);
			next = (neighbours.get(0) == last) ? neighbours.get(1) : neighbours.get(0);
		}
		return next;
	}

	/** sets pixel in image to black.
	 * 
	 * subtracts offset
	 * 
	 * @param image
	 * @param x0 x offset 
	 * @param y0 y offset
	 */
	public void setToBlack(BufferedImage image, Int2 xy0) {
		Int2 int2 = getInt2();
		int i = int2.getX() - xy0.getX();
		int j = int2.getY() - xy0.getY();
		image.setRGB(i, j, 0x00000000);
	}

	public boolean isTjunctionCentre(PixelIsland island) {
		return this.getOrthogonalNeighbours(island).size() == 3 && 
				this.getDiagonalNeighbours(island).size() == 0;
	}

}
