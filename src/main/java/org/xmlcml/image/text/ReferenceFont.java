package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.xmlcml.image.processing.PixelIsland;

public class ReferenceFont {

	private final static Logger LOG = Logger.getLogger(ReferenceFont.class);
	
	public final static String[] WP_CHARS = {
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", 
		"graveup", "Aup", "ringup", "Aup", "acuteup", "Eup", "hatup", "Iup",
		"a", "b", "c", "d", "e", "f", "g", "h", "dot", "i", "dot", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", 
		"gravelow", "alow", "ringlow", "alow", "acutelow", "elow", "hatlow", "ilow",
		"amp", 
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
		"lbrak", "dollar", "pound", "period", "comma", "exclam", "query", "rbrak",
	};
	
	public final static File FONT_DIR = new File("src/main/resources/org/xmlcml/image/text/fonts");
	public final static File GENERIC_DIR = new File(FONT_DIR, "generic");
	public static ReferenceFont GENERIC = null;
//	public final static File WP_GENERIC_PNG = new File(GENERIC_DIR, "generic.png");
	
	public final static File HELVETICA_DIR = new File(FONT_DIR, "helvetica");
	public static ReferenceFont HELVETICA = null;
	public final static File WP_HELVETICA_PNG = new File(HELVETICA_DIR, "helvetica.png");
	
	public final static ReferenceFont WP_HELVETICA_BOLD = null;
	
	static {
		GENERIC = ReferenceFont.createFont(GENERIC_DIR, "Generic");
		HELVETICA = ReferenceFont.createFont(HELVETICA_DIR, "Helvetica");
	}

	private static void trimGeneric() {
		List<File> files = new ArrayList<File>(FileUtils.listFiles(GENERIC_DIR, null, false));
		for (File file : files) {
			if (!file.getName().endsWith("png")) continue;
			try {
				BufferedImage image = ImageIO.read(file);
				if (image == null) {
					throw new RuntimeException(" failed on: "+file);
				}
				GrayCharacter gray = GrayCharacter.readGrayImage(image);
				BufferedImage trim = gray.trimEdgesWhite(250);
				ImageIO.write(trim, "png", new File("target/trim/"+file.getName()));
			} catch (Exception e) {
				throw new RuntimeException("Failed "+file, e);
			}
		}
	}
	
	private Map<String, PixelIsland> pixelIslandByCharacterMap;
	private Map<String, BufferedImage> imageByCharacterMap;

	private String name;
	
	public ReferenceFont() {
		
	}

	public ReferenceFont(String name) {
		this();
		this.name = name;
	}

//	/** obsolete as cannot easily deduce character order in image */
//	@Deprecated
//	public static ReferenceFont createFont(String[] chars, File imageFile) {
//		// /Users/pm286/workspace/imageanalysis/src/main/resources/image/text/fonts/helvetica/helvetica.png 
//		ReferenceFont font = null;
//		try {
//			LOG.trace(imageFile.getAbsoluteFile()+" "+imageFile.exists());
//			BufferedImage image = ImageIO.read(imageFile);
//			PixelIslandList pixelIslandList = PixelIslandList.createPixelIslandList(image);
//			if (pixelIslandList.size() != chars.length) {
////				throw new RuntimeException("pixelIslands ("+pixelIslandList.size()+") don't match chars ("+chars.length+")");
//			}
//			// because characters have fuzzy edges we can't sort precisely
//			Collections.sort(pixelIslandList.getList(), new PixelIslandComparator(
//					PixelIslandComparator.ComparatorType.TOP, PixelIslandComparator.ComparatorType.LEFT));
//
//			// this will be omitted when each font is labelled
//			new File("target/subimage/").mkdirs();
//			for (int i = 0; i < pixelIslandList.size(); i++) {
//				BufferedImage subimage = pixelIslandList.get(i).clipSubimage(image);
//				ImageIO.write(subimage, "png", new File("target/subimage/c"+i+".png"));
//				pixelIslandList.get(i).debugSVG("target/charxxx"+i+".svg");
////				font.addCharacter(chars[i], pixelIslandList.get(i));
//			}
//			font = new ReferenceFont();
//			for (int i = 0; i < chars.length; i++) {
//				font.addCharacter(chars[i], pixelIslandList.get(i));
//			}
//		} catch (Exception e) {
//			throw new RuntimeException("Cannot create font: "+imageFile, e);
//		}
//		return font;
//	}
	
	public static ReferenceFont createFont(File directory, String name) {
		ReferenceFont font = new ReferenceFont(name);
		if (GENERIC_DIR.equals(directory)) {
			trimGeneric();
		}
		try {
			List<File> files = new ArrayList<File>(FileUtils.listFiles(directory, new String[]{"png"}, false));
			for (File file : files) {
				BufferedImage image = ImageIO.read(file);
				String filename = file.getName();
				filename = filename.replaceAll("\\..*", ""); // strip suffix
				if (filename.length() == 3 && filename.endsWith("up")) {
					filename = filename.substring(0, 1).toUpperCase();
				}
				font.addCharacter(filename, image);
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot read images: ", e);
		}
		return font;
	}

	public void addCharacter(String charx, PixelIsland pixelIsland) {
		ensurePixelIslandByCharacter();
		pixelIslandByCharacterMap.put(charx, pixelIsland);
	}

	private void ensurePixelIslandByCharacter() {
		if (pixelIslandByCharacterMap == null) {
			pixelIslandByCharacterMap = new HashMap<String, PixelIsland>();
		}
	}

	public PixelIsland getPixelIsland(String charx) {
		ensurePixelIslandByCharacter();
		return pixelIslandByCharacterMap.get(charx);
	}
	
	public void addCharacter(String charx, BufferedImage pimage) {
		ensureImageByCharacter();
		imageByCharacterMap.put(charx, pimage);
	}

	private void ensureImageByCharacter() {
		if (imageByCharacterMap == null) {
			imageByCharacterMap = new HashMap<String, BufferedImage>();
		}
	}

	public BufferedImage getImage(String charx) {
		ensureImageByCharacter();
		return imageByCharacterMap.get(charx);
	}
	

}
