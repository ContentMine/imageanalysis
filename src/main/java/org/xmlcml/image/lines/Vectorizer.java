package org.xmlcml.image.lines;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.image.processing.FloodFill;
import org.xmlcml.image.processing.PixelIsland;

/** vectorizes pixel maps
 * 
 * @author pm286
 *
 */
public class Vectorizer {

	private File file;
	private BufferedImage inputImage;
	private FloodFill floodFill;
	private boolean diagonalFlood;
	private double dpTolerance = 0.5;
	private DouglasPeucker douglasPeucker;

	public List<PixelIsland> createIslands() throws IOException {
		FloodFill floodFill = new FloodFill(inputImage);
		floodFill.setDiagonal(true);
		floodFill.fill();
		List<PixelIsland> islandList = floodFill.getPixelIslandList();
		return islandList;
	}

	public void readFile(File file) throws IOException {
		this.file = file;
		readImage(ImageIO.read(file));
	}

	public void readImage(BufferedImage image) {
		this.inputImage = image;
	}

	public FloodFill createFloodFill() {
		floodFill = new FloodFill(inputImage);
		floodFill.setDiagonal(this.diagonalFlood);
		floodFill.fill();
		return floodFill;
	}

	public List<SVGLine> segment(PixelPath pixelPath) {
		List<SVGLine> lineList = new ArrayList<SVGLine>();
		douglasPeucker = new DouglasPeucker(dpTolerance);
		return lineList;
	}
}
