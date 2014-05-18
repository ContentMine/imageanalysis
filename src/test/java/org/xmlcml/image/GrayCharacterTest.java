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
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Util;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.processing.PixelIsland;
import org.xmlcml.image.processing.PixelIslandList;
import org.xmlcml.image.text.CharacterMatchList;
import org.xmlcml.image.text.GrayCharacter;
import org.xmlcml.image.text.fonts.ReferenceFont;
import org.xmlcml.image.text.fonts.ReferenceFontManager;

public class GrayCharacterTest {

	static final ReferenceFontManager FONT_MANAGER = new ReferenceFontManager();

	private static final String GENERIC_DIR = "src/main/resources/org/xmlcml/image/text/fonts/generic";
	public final static Logger LOG = Logger.getLogger(GrayCharacterTest.class);

	@Test
	public void testGrayCharacter() throws Exception {
		GrayCharacter character = new GrayCharacter(new File(Fixtures.CHAR_DIR,
				"65.png"));
	}

	@Test
	public void testCorrelate() throws Exception {
		GrayCharacter character = new GrayCharacter(new File(Fixtures.CHAR_DIR,
				"65.png"));
		GrayCharacter characterA = new GrayCharacter(new File(
				Fixtures.TEXT_DIR, "testA.png"));
		boolean scale = false;
		character.correlateGray(character, "coor", false, scale);
		character.correlateGray(characterA, "coor0", false, scale);
		character.correlateGray(characterA, "coor1", true, scale);
	}

	@Test
	/** correlates images using overlay.
	 * 
	 * images include bold A, normal A and a G
	 * @throws Exception
	 */
	public void testCorrelateMany() throws Exception {
		RealSquareMatrix corrMat = generateCorrelationMatrixFromFiles(new File(
				Fixtures.TEXT_DIR, "65"));
		LOG.trace(corrMat.format(1));
	}

	private RealSquareMatrix generateCorrelationMatrixFromFiles(File dir)
			throws IOException {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			LOG.trace(files[i].getName());
		}
		RealSquareMatrix corrMat = generateCorrelationMatrix(files);
		return corrMat;
	}

	@Test
	@Ignore // not fully implemented
	public void testCluster() throws Exception {
		analyzeCluster(new File(Fixtures.TEXT_DIR, "65"), 48);
		analyzeCluster(Fixtures.CHAR_DIR, 47);
	}

	@Test
	@Ignore // not fully implemented
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
		if (files.length == 0) {
			LOG.error("no files in: " + dir);
			return;
		}
		LOG.error("NYI");
		
		/**
		 * String[] names = getFilenames(files); LOG.trace(names.length);
		 * double[][] distances = getDistances(files);
		 * LOG.trace("made distances"); ClusteringAlgorithm alg = new
		 * DefaultClusteringAlgorithm(); Cluster cluster =
		 * alg.performClustering(distances, names, new
		 * AverageLinkageStrategy()); LOG.trace("made cluster");
		 * cluster.toConsole(0); int dist = (int) cluster.getTotalDistance();
		 * Assert.assertEquals(dist0, dist); distances = getDistances(files);
		 * LOG.trace("reran distamces for timing");
		 */
	}

	private double[][] getDistances(File[] files) throws IOException {
		int n = files.length;
		double[][] distances = new double[n][n];
		RealSquareMatrix corrMat = generateCorrelationMatrix(files);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				distances[i][j] = 1.0 / (0.001 + Math.abs(corrMat.elementAt(i,
						j)));
			}
		}
		return distances;
	}

	private String[] getFilenames(File[] files) {
		String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName().replaceAll(".png", "");
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
				// double corr = gray[i].correlateGray(gray[j], i+"_"+j,
				// /*true*/false);
				boolean scale = false;
				double corr = gray[i].correlateGray(gray[j], null, /* true */
						false, scale);
				corrMat.setElementAt(i, j, corr);
				corrMat.setElementAt(j, i, corr);
			}
		}
		return corrMat;
	}

	@Test
	public void testScaleCharacter() throws Exception {
		BufferedImage image = ImageIO
				.read(new File(Fixtures.CHAR_DIR, "65.png"));
		int width = 20;
		int height = 10;
		BufferedImage bimage = Scalr.resize(image, Method.ULTRA_QUALITY,
				Mode.FIT_EXACT, width, height);
		ImageUtil.writeImageQuietly(bimage, "target/gray/rescaled65.png");
	}

	@Test
	public void testScaleCapitalA() throws Exception {
		BufferedImage refImage = ImageIO.read(new File(Fixtures.CHAR_DIR,
				"65.png"));
		int width = refImage.getWidth();
		int height = refImage.getHeight();
		BufferedImage newImage = ImageIO.read(new File(GENERIC_DIR, "65.png"));
		BufferedImage newScaledImage = Scalr.resize(newImage,
				Method.ULTRA_QUALITY, Mode.FIT_EXACT, width, height);
		ImageUtil.writeImageQuietly(newScaledImage,
				"target/gray/rescaledCapitalA.png");
		GrayCharacter refGray = GrayCharacter.readGrayImage(refImage);
		GrayCharacter newGray = GrayCharacter.readGrayImage(newScaledImage);
		double corr = refGray.correlateGray(newGray, "ref-grayA", true, false);
		Assert.assertEquals("corr ", 0.59, Util.format(corr, 2), 0.01);

	}

	@Test
	public void testCorrelatePixelIslandsAgainstReference() throws Exception {
		double corrMin = 0.50;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.MONOSPACE);
		BufferedImage testImage = ImageIO.read(new File(
				ReferenceFontManager.MONOSPACE_DIR, "65.png"));
		PixelIslandList pixelIslandList = PixelIslandList
				.createPixelIslandList(testImage);
		for (PixelIsland island : pixelIslandList) {
			BufferedImage image = island.createImage(testImage.getType());
			if (image == null) {
				LOG.error("null image " + island);
			} else {
				ImageUtil.writeImageQuietly(image, new File(
						"target/reffont/test/65.png"));
				LOG.trace(image.getWidth() + "/" + image.getHeight());
				GrayCharacter grayCharacter = GrayCharacter
						.readGrayImage(image);
				CharacterMatchList matchList = referenceFont.getBestCharacters(
						grayCharacter, corrMin);
				LOG.trace("matchList: " + matchList);
			}
		}
	}

	@Test
	@Ignore
	public void testCorrelateMonospaceMonospace() throws Exception {
		double corrMin = 0.20;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.MONOSPACE);
		// BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
		// "bmcgraph.jpg"));
		BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"monospace.png"));
		SVGG g = referenceFont.extractAndPlotCharacters(corrMin, testImage,
				1000);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/monospace.svg"));

	}

	@Test
	@Ignore
	public void testCorrelateHelveticaAgainstMonospace() throws Exception {
		double corrMin = 0.20;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.MONOSPACE);
		BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"helvetica.png"));
		SVGG g = referenceFont.extractAndPlotCharacters(corrMin, testImage,
				1000);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/helvetica.svg"));

	}

	@Test
	@Ignore
	public void testCorrelateBMCwFont() throws Exception {
		double corrMin = 0.20;
		// ReferenceFont referenceFont =
		// FONT_MANAGER.getFont(ReferenceFontManager.HELVETICA);
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.TIMES_NEW_ROMAN);
		// BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
		// "1471-2148-14-32-2-l.jpg"));
		BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"1471-2148-14-31-2-l.jpg"));
		SVGG g = referenceFont.extractAndPlotCharacters(corrMin, testImage,
				1000);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/bmcHelvetica.svg"));

	}

	@Test
	@Ignore
	public void testCorrelateBMCAgainstHelvetica() throws Exception {
		double corrMin = 0.20;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.HELVETICA);
		// BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
		// "bmcgraph.jpg"));
		BufferedImage testImage = ImageIO.read(new File(Fixtures.TEXT_DIR,
				"1471-2148-14-20-test.jpg"));
		SVGG g = referenceFont.extractAndPlotCharacters(corrMin, testImage,
				1000);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/1471-2148-14-20.svg"));

	}

	@Test
	public void testC() {

	}

	@Test
	public void testCorrelateEightVsTimes() throws Exception {
		double corrMin = 0.20;
		boolean overlay = true;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.TIMES_NEW_ROMAN);
		File testFile = new File(Fixtures.TEXT_DIR, "1471-2148-14-31-2-l.jpg");
		Int2Range box = new Int2Range(new IntRange(352, 360), new IntRange(28,
				43)).getInt2RangeExtendedInX(1, 1)
				.getInt2RangeExtendedInY(1, 1);
		GrayCharacter grayImage8 = GrayCharacter.clipCharacterFromImage(
				testFile, box);

		boolean scale = true;
		double corr = referenceFont.correlateAndPlot(overlay, scale,
				grayImage8, "8");
		LOG.trace("gray-8 " + corr);
		corr = referenceFont.correlateAndPlot(overlay, scale, grayImage8, "A");
		LOG.trace("gray-A " + corr);

	}

	@Test
	public void testCorrelateSixVsHelvetica() throws Exception {
		double corrMin = 0.20;
		boolean overlay = true;
		ReferenceFont referenceFont = FONT_MANAGER
				.getFont(ReferenceFontManager.HELVETICA);
		File testFile = new File(Fixtures.TEXT_DIR, "1471-2148-14-20-test.jpg");
		// Int2Range box = new Int2Range(new IntRange(99, 114), new
		// IntRange(166, 188)).
		// getInt2RangeExtendedInX(1, 1).getInt2RangeExtendedInY(1, 1);
		Int2Range box = new Int2Range(new IntRange(99, 114), new IntRange(166,
				188));
		GrayCharacter grayImage6 = GrayCharacter.clipCharacterFromImage(
				testFile, box);

		boolean scale = true;
		double corr = referenceFont.correlateAndPlot(overlay, scale,
				grayImage6, "6");
		LOG.trace("gray-6 " + corr);
		corr = referenceFont.correlateAndPlot(overlay, scale, grayImage6, "3");
		LOG.trace("gray-3 " + corr);

	}

}
