package org.xmlcml.image.text.fonts;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.xmlcml.euclid.Util;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.processing.PixelIsland;
import org.xmlcml.image.processing.PixelIslandList;
import org.xmlcml.image.processing.PixelProcessor;
import org.xmlcml.image.text.CharacterMatchList;
import org.xmlcml.image.text.GrayCharacter;

public class ReferenceFont {

	private final static Logger LOG = Logger.getLogger(ReferenceFont.class);

	public final static String[] WP_CHARS = { "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z", "graveup", "Aup", "ringup",
			"Aup", "acuteup", "Eup", "hatup", "Iup", "a", "b", "c", "d", "e",
			"f", "g", "h", "dot", "i", "dot", "j", "k", "l", "m", "n", "o",
			"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "gravelow",
			"alow", "ringlow", "alow", "acutelow", "elow", "hatlow", "ilow",
			"amp", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "lbrak",
			"dollar", "pound", "period", "comma", "exclam", "query", "rbrak", };

	private Map<Integer, PixelIslandList> pixelIslandListByCodePointMap;
	private Map<Integer, BufferedImage> imageByCodePointMap;
	private Map<Integer, GrayCharacter> grayByCodePointMap;

	private String name;

	public ReferenceFont() {

	}

	public ReferenceFont(String name) {
		this();
		this.name = name;
	}

	/**
	 * splits large image into indivdiual pngs.
	 * 
	 * will binarize image.
	 * 
	 * Currently fails on glyphs with multiple islands such as accented chars,
	 * colons, etc.
	 * 
	 * @param imageWithGlyphs
	 */
	public static List<File> createWikpediaFontPngs(File imageWithGlyphs,
			File outputDirectory) throws Exception {
		LOG.debug(imageWithGlyphs + " " + imageWithGlyphs.exists());
		BufferedImage image = ImageIO.read(imageWithGlyphs);
		PixelProcessor pixelProcessor = new PixelProcessor(image);
		PixelIslandList islands = pixelProcessor.getOrCreatePixelIslandList();
		List<File> fileList = new ArrayList<File>();
		int i = 0;
		for (PixelIsland island : islands) {
			File imageFile = new File(outputDirectory, "_" + i + ".png");
			BufferedImage newImage = island.createImage(image.getType());
			if (newImage != null) {
				fileList.add(imageFile);
				ImageUtil.writeImageQuietly(newImage, imageFile);
			}
			i++;
		}
		return fileList;
	}

	public static ReferenceFont createFont(File directory, String name) {
		ReferenceFont font = new ReferenceFont(name);
		try {
			List<File> files = new ArrayList<File>(FileUtils.listFiles(
					directory, new String[] { "png" }, false));
			for (File file : files) {
				BufferedImage image = ImageIO.read(file);
				String base = FilenameUtils.getBaseName(file.getName());
				Integer codePoint = null;
				try {
					codePoint = new Integer(base);
				} catch (Exception e) {
					throw new RuntimeException("filename base must be integer "
							+ file);
				}
				if (image == null) {
					throw new RuntimeException("null " + base + " +image");
				}
				if (image.getWidth() * image.getHeight() <= 1) {
					LOG.debug("single/zero image: " + base);
				} else {
					font.addCharacter(codePoint, image);
					// debug
					ImageUtil.writeImageQuietly(image, new File(new File(
							"target/refFont/"), base + ".png"));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot read images: ", e);
		}
		return font;
	}

	public void addCharacter(Integer codePoint, PixelIslandList pixelIslandList) {
		ensurePixelIslandListByCodePoint();
		getPixelIslandListByCodePointMap().put(codePoint, pixelIslandList);
	}

	private void ensurePixelIslandListByCodePoint() {
		if (getPixelIslandListByCodePointMap() == null) {
			pixelIslandListByCodePointMap = new HashMap<Integer, PixelIslandList>();
		}
	}

	public PixelIslandList getPixelIsland(String charx) {
		ensurePixelIslandListByCodePoint();
		return getPixelIslandListByCodePointMap().get(charx);
	}

	public void addCharacter(Integer codePoint, BufferedImage image) {
		ensureImageByCharacter();
		imageByCodePointMap.put(codePoint, image);
		ensureGrayByCodePoint();
		GrayCharacter gray = GrayCharacter.readGrayImage(image);
		gray.setCodePoint(codePoint);
		getGrayByCodePointMap().put(codePoint, gray);
		addPixelIslandList(codePoint, image);
	}

	private void addPixelIslandList(Integer codePoint, BufferedImage image) {
		ensurePixelIslandListByCodePoint();
		PixelProcessor pixelProcessor = new PixelProcessor(image);
		PixelIslandList pixelIslandList = pixelProcessor
				.getOrCreatePixelIslandList();
		this.getPixelIslandListByCodePointMap().put(codePoint, pixelIslandList);
	}

	private void ensureImageByCharacter() {
		if (imageByCodePointMap == null) {
			imageByCodePointMap = new HashMap<Integer, BufferedImage>();
		}
	}

	private void ensureGrayByCodePoint() {
		if (getGrayByCodePointMap() == null) {
			grayByCodePointMap = new HashMap<Integer, GrayCharacter>();
		}
	}

	public BufferedImage getImage(Integer charx) {
		ensureImageByCharacter();
		return imageByCodePointMap.get(charx);
	}

	public static void compareImagesAgainstReferences(List<File> refFiles,
			List<File> testFiles, boolean plot) throws IOException {
		for (File refFile : refFiles) {
			BufferedImage refImage = ImageIO.read(refFile);
			int width = refImage.getWidth();
			int height = refImage.getHeight();
			String refName = refFile.getName();
			if (refName.startsWith("_"))
				continue; // skip large images
			GrayCharacter refChar = GrayCharacter.readGrayImage(refImage);
			double corrmax = -999;
			String titlemax = null;
			File testFilex = null;
			for (File testFile : testFiles) {
				BufferedImage genImage = ImageIO.read(testFile);
				BufferedImage scaledGenImage = ImageUtil.scaleImage(width,
						height, genImage);
				String genName = testFile.getName();
				GrayCharacter genChar = GrayCharacter
						.readGrayImage(scaledGenImage);
				String title = refName + "_" + genName;
				String plotTitle = (plot) ? title : null;
				boolean scale = false;
				double cor = refChar.correlateGray(genChar, plotTitle, true,
						scale);
				if (cor > corrmax) {
					corrmax = cor;
					titlemax = title;
					testFilex = testFile;
				}
			}
			testFiles.remove(testFilex);
			LOG.trace("cor " + Util.format(corrmax, 2) + ": " + titlemax);
		}
	}

	public static BufferedImage scaleImage(int width, int height,
			BufferedImage genImage) {
		BufferedImage scaledGenImage = Scalr.resize(genImage,
				Method.ULTRA_QUALITY, Mode.FIT_EXACT, width, height);
		return scaledGenImage;
	}

	@Deprecated
	public CharacterMatchList getBestCharacters(GrayCharacter gray,
			double minCorrelation) {
		CharacterMatchList matchList = new CharacterMatchList();
		boolean overlay = true;
		if (getPixelIslandListByCodePointMap() == null) {
			LOG.error("null font ?? " + name + " " + this.hashCode());
			throw new RuntimeException();
		} else if (getPixelIslandListByCodePointMap().size() == 0) {
			LOG.error("empty font ?? " + name + " " + this.hashCode());
			throw new RuntimeException();
		}
		for (Integer codePoint : getPixelIslandListByCodePointMap().keySet()) {
			LOG.trace("codePoint: " + codePoint);
			GrayCharacter refGray = getGrayByCodePointMap().get(codePoint);
			boolean scale = true;
			double corr = refGray.correlateGray(gray, null, overlay, scale);
			LOG.trace("corr: " + corr);
			if (corr > minCorrelation) {
				matchList.add(refGray, corr);
			}
		}
		return matchList;
	}

	public String getName() {
		return name;
	}

	public Map<Integer, GrayCharacter> getGrayByCodePointMap() {
		return grayByCodePointMap;
	}

	/**
	 * gets grayCharcter by value of character.
	 * 
	 * @param value
	 *            (e.g. "A")
	 * @return
	 */
	public GrayCharacter getGrayByCharacterValue(String value) {
		Integer codePoint = value.codePointAt(0);
		return (codePoint == null) ? null : grayByCodePointMap.get(codePoint);
	}

	public double correlateAndPlot(boolean overlay, boolean scale,
			GrayCharacter grayImageTest, String character) {
		GrayCharacter refGrayChar = getGrayByCharacterValue(character);
		ImageUtil.writeImageQuietly(refGrayChar.getGrayImage(), new File(
				"target/ref" + character + ".png"));
		double corr = refGrayChar.correlateGray(grayImageTest, "gray-"
				+ character, overlay, scale);
		return corr;
	}

	public Map<Integer, PixelIslandList> getPixelIslandListByCodePointMap() {
		return pixelIslandListByCodePointMap;
	}

	public GrayCharacter getGrayCharacter(Integer codePoint) {
		return this.grayByCodePointMap == null ? null : grayByCodePointMap
				.get(codePoint);
	}

}
