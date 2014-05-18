package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.text.fonts.ReferenceFont;
import org.xmlcml.image.text.fonts.ReferenceFontManager;

public class ReferenceFontTest {
	
	static final ReferenceFontManager FONT_MANAGER = new ReferenceFontManager();
	
	private final static Logger LOG = Logger.getLogger(ReferenceFontTest.class);

	@Test
	@Ignore // fix generic font character file names
	public void testCreateFonts() throws Exception {
		Assert.assertTrue(ReferenceFontManager.HELVETICA_DIR.exists());
		Assert.assertTrue(ReferenceFontManager.HELVETICA_DIR.isDirectory());
		BufferedImage image = FONT_MANAGER.getFont(ReferenceFontManager.HELVETICA).getImage(65);
		Assert.assertNotNull(image);
		BufferedImage image1 = FONT_MANAGER.getFont(ReferenceFontManager.GENERIC).getImage(65);
		Assert.assertNotNull(image1);
	}


	@Test
	@Ignore
	public void testCorrelateFonts() throws Exception {
		BufferedImage image0 = FONT_MANAGER.getFont(ReferenceFontManager.HELVETICA).getImage(65);
		GrayCharacter character0 = GrayCharacter.readGrayImage(image0);
		BufferedImage image1 = FONT_MANAGER.getFont(ReferenceFontManager.GENERIC).getImage(65);
		GrayCharacter character1 = GrayCharacter.readGrayImage(image1);
		double cor = character0.correlateGray(character1, "A0-1", true, false);
		LOG.debug(cor);
	}
	

	/** correlates fileformat sansserif glyphs with WPHelv.
	 * 
	 * explores the range of correlation coeffs.
	 * 
	 * Because the characters are all scaled to same size small glyphs may be given extra weight.
	 * Thus ' may end up as a solidus. This is only a problem for isolated characters - if we know they are
	 * all the same font we can scale to constant size and baseline (e.g. descender)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrelateFontsAll() throws Exception {
		List<File> helvticaFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.HELVETICA_DIR, new String[]{"png"}, false));
		List<File> genericFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.GENERIC_DIR, new String[]{"png"}, false));
		boolean plot = false;
		ReferenceFont.compareImagesAgainstReferences(helvticaFiles, genericFiles, plot);
	}
	
	@Test
	public void testCorrelateHelveticAndBold() throws Exception {
		List<File> refFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.HELVETICA_DIR, new String[]{"png"}, false));
		List<File> genericFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.HELVETICA_BOLD_DIR, new String[]{"png"}, false));
		boolean plot = false;
		ReferenceFont.compareImagesAgainstReferences(refFiles, genericFiles, plot);
	}

	@Test
	public void testCorrelateHelveticaTimes() throws Exception {
		List<File> refFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.HELVETICA_DIR, new String[]{"png"}, false));
		List<File> genericFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.TIMES_NEW_ROMAN_DIR, new String[]{"png"}, false));
		boolean plot = false;
		ReferenceFont.compareImagesAgainstReferences(refFiles, genericFiles, plot);
	}

	@Test
	@Ignore // takes too long
	public void testCorrelateHelveticaMonospace() throws Exception {
		List<File> refFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.HELVETICA_DIR, new String[]{"png"}, false));
		List<File> genericFiles = new ArrayList<File>(FileUtils.listFiles(ReferenceFontManager.MONOSPACE_DIR, new String[]{"png"}, false));
		boolean plot = true;
		ReferenceFont.compareImagesAgainstReferences(refFiles, genericFiles, plot);
	}

	@Test
	public void testCreateWikpediaHelevticaPngs() throws Exception {
		File glyphFile = new File(ReferenceFontManager.FONT_DIR, "_helveticaBold.png");
		File outdir = new File("target/glyphs/");
		outdir.delete();
		outdir.mkdirs();
		ReferenceFont.createWikpediaFontPngs(glyphFile, outdir);
		Assert.assertTrue("glyphs", FileUtils.listFiles(outdir, new String[]{"png"}, false).size() > 90);
	}
	
	@Test
	public void testCreateWikpediaTimesPngs() throws Exception {
		File glyphFile = new File(ReferenceFontManager.FONT_DIR, "_timesNewRoman.gif");
		File outdir = new File("target/glyphs/");
		outdir.delete();
		outdir.mkdirs();
		ReferenceFont.createWikpediaFontPngs(glyphFile, outdir);
		Assert.assertEquals("glyphs", 98, FileUtils.listFiles(outdir, new String[]{"png"}, false).size());
	}
	
	@Test
	public void testCreateWikpediaMonospacePngs() throws Exception {
		File glyphFile = new File(ReferenceFontManager.FONT_DIR, "_monospace.png");
		File outdir = new File("target/glyphs/");
		outdir.delete();
		outdir.mkdirs();
		ReferenceFont.createWikpediaFontPngs(glyphFile, outdir);
		Assert.assertEquals("glyphs", 98, FileUtils.listFiles(outdir, new String[]{"png"}, false).size());
	}
	
	private void translate(File dir) throws IOException {
		List<File> files = new ArrayList<File>(FileUtils.listFiles(dir, new String[]{"png"}, false));
		for (File file : files) {
			String base = FilenameUtils.getBaseName(file.getName());
//			System.out.println(base);
			String s = null;
			if (base.length() == 1) {
				int charx = base.charAt(0);
				if (charx >= 'a' && charx <= 'z' || charx >= '0' && charx <= '9') {
					s = String.valueOf((int)charx);
				} else {
					s = "+++"+base;
				}
			} else if (base.length() == 2) {
				if (base.charAt(0) == base.charAt(1)) {
					s = String.valueOf((int)(base.charAt(0) - 'a' + 'A'));
				} else {
					s = base;
				}
			} else if (base.equals("rbrak")) {
				s = String.valueOf((int)')');
			} else if (base.equals("lbrak")) {
				s = String.valueOf((int)'(');
			} else if (base.equals("dollar")) {
				s = String.valueOf((int)'$');
			} else if (base.equals("amp")) {
				s = String.valueOf((int)'&');
			} else if (base.equals("comma")) {
				s = String.valueOf((int)',');
			} else if (base.equals("rsquare")) {
				s = String.valueOf((int)']');
			} else if (base.equals("lsquare")) {
				s = String.valueOf((int)'[');
			} else if (base.startsWith("_")) {
			} 
			if (s != null) {
				File outfile = new File(file.getParentFile(), s+".png");
				FileUtils.copyFile(file, outfile);
			}
			System.out.println(base+" "+s);
		}
	}
}
