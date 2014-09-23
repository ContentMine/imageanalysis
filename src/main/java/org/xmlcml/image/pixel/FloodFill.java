package org.xmlcml.image.pixel;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

public class FloodFill {

	// informed by
	// http://stackoverflow.com/questions/2783204/flood-fill-using-a-stack

	private BufferedImage image;
	private boolean[][] painted;
	private boolean diagonal = false;
	private PixelIslandList islandList;
//	private Integer width;
//	private Integer height;

	public FloodFill(BufferedImage image) {
		this.image = image;
//		this.width = image.getWidth();
//		this.height = image.getHeight();
	}

	// NOT YET WRITTEN
	// FIXME
	public FloodFill(PixelIslandList islandList) {
		throw new RuntimeException("NYI");
	}

	private boolean isBlack(int posX, int posY) {
		if (image != null) {
			int color = image.getRGB(posX, posY);
			int brightness = (color & 0xFF) + ((color >> 2) & 0xFF)
					+ ((color >> 4) & 0xFF);
			brightness /= 3;
			return brightness < 128;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("ERROR: Pass filename as argument.");
			return;
		}
		String filename = args[0];
		BufferedImage image = ImageIO.read(new File(filename));
		FloodFill floodFill = new FloodFill(image);
		floodFill.fill();
	}

	public void fill() {
		painted = new boolean[image.getHeight()][image.getWidth()];

		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				addNextUnpaintedBlack(i, j);
			}
		}

	}

	private void addNextUnpaintedBlack(int i, int j) {
		if (isBlack(j, i) && !painted[i][j]) {

			Queue<Point> queue = new LinkedList<Point>();
			queue.add(new Point(j, i));

			PixelList pixelList = new PixelList();
			while (!queue.isEmpty()) {
				Point p = queue.remove();
				if (isInsideImage(p)) {
					if (!painted[p.y][p.x] && isBlack(p.x, p.y)) {
						painted[p.y][p.x] = true;
						Pixel pixel = new Pixel(p);
						pixelList.add(pixel);
						addNewPoints(queue, p);
					}
				}
			}
			add(new PixelIsland(pixelList));
		}
	}

	private void add(PixelIsland island) {
		ensureIslandList();
		islandList.add(island);
	}

	private void ensureIslandList() {
		if (islandList == null) {
			islandList = new PixelIslandList();
		}
	}
	
	public PixelIslandList getIslandList() {
		ensureIslandList();
		for (PixelIsland island : islandList) {
			island.setDiagonal(diagonal);
		}
		return islandList;
	}

	private boolean isInsideImage(Point p) {
		return (p.x >= 0) && (p.x < image.getWidth() &&
				(p.y >= 0) && (p.y < image.getHeight()));
	}

	private void addNewPoints(Queue<Point> queue, Point p) {
		queue.add(new Point(p.x + 1, p.y));
		queue.add(new Point(p.x - 1, p.y));
		queue.add(new Point(p.x, p.y + 1));
		queue.add(new Point(p.x, p.y - 1));
		if (diagonal) {
			queue.add(new Point(p.x + 1, p.y + 1));
			queue.add(new Point(p.x - 1, p.y + 1));
			queue.add(new Point(p.x - 1, p.y - 1));
			queue.add(new Point(p.x + 1, p.y - 1));
		}
	}

	public void setDiagonal(boolean b) {
		this.diagonal = b;
	}

}
