package org.xmlcml.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Util;
import org.xmlcml.image.text.GrayCharacter;

public class GrayCharacterTest {
	private static final String GENERIC_DIR = "src/main/resources/org/xmlcml/image/text/fonts/generic";
	private final static Logger LOG = Logger.getLogger(GrayCharacterTest.class);

	@Test
	public void testGrayCharacter() throws Exception {
		GrayCharacter character = new GrayCharacter(new File(Fixtures.CHAR_DIR, "A10.png"));
	}

	@Test
	public void testCorrelate() throws Exception {
		GrayCharacter character = new GrayCharacter(new File(Fixtures.CHAR_DIR, "A10.png"));
		GrayCharacter characterA = new GrayCharacter(new File(Fixtures.TEXT_DIR, "testA.png"));
		character.correlateGray(character, "coor", false);
		character.correlateGray(characterA, "coor0", false);
		character.correlateGray(characterA, "coor1", true);
	}
	
	@Test
	/** correlates images using overlay.
	 * 
	 * images include bold A, normal A and a G
	 * @throws Exception
	 */
	public void testCorrelateMany() throws Exception {
		RealSquareMatrix corrMat = generateCorrelationMatrixFromFiles(new File(Fixtures.TEXT_DIR, "A10"));
		LOG.trace(corrMat.format(1));
	}

	private RealSquareMatrix generateCorrelationMatrixFromFiles(File dir) throws IOException {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			LOG.trace(files[i].getName());
		}
		RealSquareMatrix corrMat = generateCorrelationMatrix(files);
		return corrMat;
	}
		
		  
    @Test
    public void testCluster() throws Exception {
		analyzeCluster(new File(Fixtures.TEXT_DIR, "A10"), 48);
		analyzeCluster(Fixtures.CHAR_DIR, 47);
    }

    @Test
    public void testClusterAll() throws Exception {
		analyzeCluster(new File(Fixtures.TEXT_DIR, "allchars"), 58);
		analyzeCluster(new File(Fixtures.TEXT_DIR, "rawChars/5"), 58);
    }

	private void analyzeCluster(File dir, int dist0) throws IOException {
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.toString().endsWith(".png");
			}
			
		});
		if (files.length ==0) {
			LOG.error("no files in: "+dir);
			return;
		}
		LOG.error("NYI");
		/**
		String[] names = getFilenames(files);
		LOG.trace(names.length);
		double[][] distances = getDistances(files);
		LOG.trace("made distances");
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering(distances, names,
		        new AverageLinkageStrategy());
		LOG.trace("made cluster");
		cluster.toConsole(0);
        int dist = (int) cluster.getTotalDistance();
        Assert.assertEquals(dist0, dist);
		distances = getDistances(files);
		LOG.trace("reran distamces for timing");
		*/
	}

	private double[][] getDistances(File[] files) throws IOException {
		int n = files.length;
		double[][] distances = new double[n][n];
		RealSquareMatrix corrMat = generateCorrelationMatrix(files);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				distances[i][j] = 1.0/(0.001+Math.abs(corrMat.elementAt(i, j)));
			}
		}
		return distances;
	}

	private String[] getFilenames(File[] files) {
		String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName().replaceAll(".png",  "");
		}
		return names;
	}

	private RealSquareMatrix generateCorrelationMatrix(File[] files)
			throws IOException {
		int n = files.length;
		GrayCharacter[] gray = new GrayCharacter[n];
		for (int i = 0; i < files.length; i++) {
			gray[i] = new GrayCharacter(files[i]);
		}
		LOG.trace("made gray");
		RealSquareMatrix corrMat = new RealSquareMatrix(n);
		for (int i = 0; i < files.length; i++) {
			for (int j = i; j < files.length; j++) {
// writing images is performance hit				
//				double corr = gray[i].correlateGray(gray[j], i+"_"+j, /*true*/false);
				double corr = gray[i].correlateGray(gray[j], null, /*true*/false);
				corrMat.setElementAt(i,  j,  corr);
				corrMat.setElementAt(j,  i,  corr);
			}
		}
		return corrMat;
	}
	
//	@Test
//	public void testTrimCharacter() throws Exception {
//		BufferedImage image = ImageIO.read(new File(GENERIC_DIR, "_latin_capital_letter_a.png"));
//		// debug code
//		GrayCharacter grayCharacter = GrayCharacter.readGrayImage(image);
//		BufferedImage grayImage = grayCharacter.getGrayImage();
//		BufferedImage clipImage = grayCharacter.trimEdgesWhite(250);
//		ImageIO.write(clipImage, "png", new File("target/clipA.png"));
//		Assert.assertEquals("height", 52, clipImage.getHeight());
//		Assert.assertEquals("width", 50, clipImage.getWidth());
//	}

	@Test
	public void testScaleCharacter() throws Exception {
		BufferedImage image = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		int width = 20;
		int height = 10;
		BufferedImage bimage = Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_EXACT, width,
	            height);
		ImageIO.write(bimage, "png", new File("target/rescaledA10.png"));
	}

	@Test
	public void testScaleCapitalA() throws Exception {
		BufferedImage refImage = ImageIO.read(new File(Fixtures.CHAR_DIR, "A10.png"));
		int width = refImage.getWidth();
		int height = refImage.getHeight();
		BufferedImage newImage = ImageIO.read(new File(GENERIC_DIR, "latin_capital_letter_a.png"));
		BufferedImage newScaledImage = Scalr.resize(newImage, Method.ULTRA_QUALITY, Mode.FIT_EXACT, width,
	            height);
		ImageIO.write(newScaledImage, "png", new File("target/rescaledCapitalA.png"));
		GrayCharacter refGray = GrayCharacter.readGrayImage(refImage);
		//GrayCharacter.debugGray(refGray.getGrayImage());
		GrayCharacter newGray = GrayCharacter.readGrayImage(newScaledImage);
		//GrayCharacter.debugGray(newGray.getGrayImage());
		double corr = refGray.correlateGray(newGray, "ref-grayA", true);
		Assert.assertEquals("corr ", 0.59, Util.format(corr, 2), 0.01);
		
	}
}
