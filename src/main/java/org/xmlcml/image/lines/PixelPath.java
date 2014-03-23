package org.xmlcml.image.lines;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.image.processing.Nucleus;
import org.xmlcml.image.processing.Pixel;
import org.xmlcml.image.processing.PixelIsland;

/** a non-branching path of pixels.
 * 
 * Either a curve with two ends or a closed path
 * 
 * @author pm286
 *
 */
public class PixelPath {

	private final static Logger LOG = Logger.getLogger(PixelPath.class);
	
	private PixelIsland island;
	private List<Real2> rawPoints;
	private List<Pixel> pixelList;
	private List<Real2> segmentPoints;
	private Nucleus finalNucleus;
	private Nucleus startNucleus;

	public PixelPath() {
		pixelList = new ArrayList<Pixel>();
	}
	
	/** reads a PixelIsland with only one line.
	 * temporary. Will be generated from the island.
	 * 
	 * throws error if more than two termini (i.e. branches)
	 * @param pixelIsland
	 */
	void readPixels(PixelIsland island) {
		this.island = island;
		if (island.getTerminalPixels().size() == 0) {
			createCycle();
		} else if (island.getTerminalPixels().size() == 2) {
			createLine();
		} else {
			this.island = null;
			throw new RuntimeException("Must be cycle or line: no branches");
		}
	}

	private void createLine() {
		throw new RuntimeException("NYI");
	}

	private void createCycle() {
		throw new RuntimeException("NYI");
	}

	public void add(Pixel pixel) {
		pixelList.add(pixel);
	}

	public Pixel getFirstPixel() {
		return pixelList.size() == 0 ? null : pixelList.get(0);
	}

	public Pixel getLastPixel() {
		return (pixelList.size() == 0) ? null : pixelList.get(pixelList.size() - 1);
	}
	
	public List<Real2> getPoints() {
		if (rawPoints == null) {
			rawPoints = new ArrayList<Real2>();
			for (Pixel pixel : pixelList) {
				Int2 int2 = pixel.getInt2();
				rawPoints.add(new Real2(int2.getX(), int2.getY()));
			}
			if (finalNucleus != null) {
				Real2 centre = finalNucleus.getCentre();
				// don't add as causes instability; needs further work
//				rawPoints.add(centre);
				LOG.trace("CC "+centre+" "+rawPoints);
			}
		}
		return rawPoints;
	}

	public SVGG createSVGG() {
		SVGG g = new SVGG();
		List<Real2> pointsToDraw = (segmentPoints != null) ? segmentPoints : rawPoints;
		if (pointsToDraw != null) {
			for (int i = 1; i < pointsToDraw.size(); i++) {
				SVGLine line = new SVGLine(pointsToDraw.get(i - 1), pointsToDraw.get(i));
				g.appendChild(line);
			}
		} else {
			g = PixelIsland.plotPixels(pixelList);
		}
		return g;
	}

	public List<Real2> createDouglasPeucker(double tolerance) {
		if (segmentPoints == null) {
			DouglasPeucker douglasPeucker = new DouglasPeucker(tolerance);
			segmentPoints = douglasPeucker.reduce(getPoints());
		}
		return segmentPoints;
	}

	public void addFinalNucleus(Nucleus nucleus) {
		this.finalNucleus = nucleus;
	}

	public void addStartNucleus(Nucleus nucleus) {
		this.startNucleus = nucleus;
	}

	public List<Pixel> getPixelList() {
		return this.pixelList;
	}


	public SVGPolyline createPolyline(double epsilon) {
		List<Real2> points = createDouglasPeucker(epsilon);
		SVGPolyline polyline = new SVGPolyline(new Real2Array(points));
		return polyline;
	}
}
