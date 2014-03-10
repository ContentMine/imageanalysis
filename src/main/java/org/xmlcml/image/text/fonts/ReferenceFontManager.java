package org.xmlcml.image.text.fonts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** manages reference fonts.
 * 
 * @author pm286
 *
 */
public class ReferenceFontManager {


	public final static File FONT_DIR = new File("src/main/resources/org/xmlcml/image/text/fonts");
	
	public static final String GENERIC = "Generic";
	public final static File GENERIC_DIR = new File(FONT_DIR, "generic");
	public static final String HELVETICA = "Helvetica";
	public final static File HELVETICA_DIR = new File(FONT_DIR, "helvetica");
	public static final String HELVETICA_BOLD = "HelveticaBold";
	public static final File HELVETICA_BOLD_DIR = new File(FONT_DIR, "helveticaBold/");
	public static final String MONOSPACE = "Monospace";
	public static final File MONOSPACE_DIR = new File(FONT_DIR, "monospace/");
	public static final String TIMES_NEW_ROMAN = "TimesNewRoman";
	public static final File TIMES_NEW_ROMAN_DIR = new File(FONT_DIR, "times/");
	
	private Map<String, ReferenceFont> fontMap;
	private HashMap<String, File> directoryMap;

	public ReferenceFontManager() {
		init();
	}

	/** some fonts to start with.
	 * 
	 * later we should read from config file
	 * 
	 */
	private void init() {
		directoryMap = new HashMap<String, File>();
		directoryMap.put(GENERIC, GENERIC_DIR);
		directoryMap.put(HELVETICA, HELVETICA_DIR);
		directoryMap.put(HELVETICA_BOLD, HELVETICA_BOLD_DIR);
		directoryMap.put(MONOSPACE, MONOSPACE_DIR);
		directoryMap.put(TIMES_NEW_ROMAN, TIMES_NEW_ROMAN_DIR);
	}
	
	void add(ReferenceFont font) {
		ensureFontList();
		fontMap.put(font.getName(), font);
	}

	private void ensureFontList() {
		if (fontMap == null) {
			fontMap = new HashMap<String, ReferenceFont>();
		}
	}
	
	public ReferenceFont getFont(String name) {
		ensureFontList();
		ReferenceFont font = fontMap.get(name);
		if (font == null) {
			File directory = directoryMap.get(name);
			if (directory != null) {
				font = ReferenceFont.createFont(directory, name);
				add(font);
			}
		}
		return font;
	}

}
