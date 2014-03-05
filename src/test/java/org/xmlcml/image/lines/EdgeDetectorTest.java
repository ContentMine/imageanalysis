package org.xmlcml.image.lines;

import gibara.CannyEdgeDetector;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class EdgeDetectorTest {

	private final static Logger LOG = Logger.getLogger(CannyEdgeDetector.class);
	
	@Test
	@Ignore // ImageIO rejects image as wrong type
	public void testEdgeDetector() throws IOException {
		AbstractDetector detector = new EdgeDetector();
		detector.readImageFile(Fixtures.TREE_PNG);
		detector.writeFile(new File("target/tree.png"));
	}
	
	@Test
	@Ignore // file missing
	public void testEdgeDetector1() throws IOException {
		AbstractDetector detector = new EdgeDetector();
		detector.readImageFile(Fixtures.TREE1_PNG);
		detector.writeFile(new File("target/tree1.png"));
	}
	
	@Test
	@Ignore //takes rather long
	public void testMoleculeCanny() throws IOException {
		AbstractDetector detector = new EdgeDetector();
		detector.readImageFile(Fixtures.MOLECULE_20131119_JPG);
		detector.writeFile(new File("target/moleculeCanny.png"));
	}
	
	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testMoleculeBinary() throws IOException {
		AbstractDetector detector = new EdgeDetector();
		detector.readImageFile(Fixtures.MOLECULE_BINARY_CANNY_1_BMP);
//		detector.readImageFile(Fixtures.MOLECULE_BINARY_CANNY_1_PNG);
		detector.writeFile(new File("target/moleculeBinaryCanny1Hough.png"));
	}
	
	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testCannyOld() throws IOException {
		CannyEdgeDetector detector = new CannyEdgeDetector();
		detector.setLowThreshold(0.5f);
		detector.setHighThreshold(1f);
		BufferedImage img = null;
		File file = Fixtures.TREE_PNG;
		try {
			img = ImageIO.read(file);
		} catch (Exception e) {
			throw new RuntimeException("Cannot read: "+file+" "+e);
		}
		detector.setSourceImage(img);
		try {
			detector.process();
		} catch (Exception e) {
			LOG.error("Cannot process: "+file+" e");
			return;
		}
		BufferedImage edges = detector.getEdgesImage();
		if (edges != null) {
			File outdir = new File("junk");
			outdir.mkdirs();
			File outfile = new File(outdir, file.getName()+".png");
	//		LOG.debug("wrote: "+outfile);
			ImageIO.write(edges, "png", outfile);
		}
	}
	
}
