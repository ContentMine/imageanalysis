package org.xmlcml.image;

import java.io.File;

public class Fixtures {

	public final static File RESOURCE_DIR = new File("src/test/resources");
	public final static File IMAGE_DIR = new File(RESOURCE_DIR, "org/xmlcml/image");
	
	public final static File TESS_DIR = new File(IMAGE_DIR, "tess");
	public final static File JOURNAL_HTML = new File(TESS_DIR, "journal.html");
	
	public final static File LINES_DIR = new File(IMAGE_DIR, "lines");
	public final static File TREE_PNG = new File(LINES_DIR, "tree.png");
	public final static File TREE1_PNG = new File(LINES_DIR, "tree1.png");
	public static final File ETHANE_PNG = new File(LINES_DIR, "ethane.png");
	public static final File ETHANE1_PNG = new File(LINES_DIR, "ethane1.png");
	public static final File ETHENE_PNG = new File(LINES_DIR, "ethene.png");
	public static final File PROPANE_PNG = new File(LINES_DIR, "propane.png");
	public static final File MALTORYZINE_PNG = new File(LINES_DIR, "maltoryzine.png");
	public static final File ETHANE_CANNY_0_PNG = new File(LINES_DIR, "ethaneCanny0.png");
	public static final File ETHANE_CANNY_1_PNG = new File(LINES_DIR, "ethaneCanny1.png");
	public static final File ETHANE_CANNY_2_PNG = new File(LINES_DIR, "ethaneCanny2.png");
	public static final File MOLECULE_CANNY_1_PNG = new File(LINES_DIR, "moleculeCanny1.png");
	public static final File MOLECULE_20131119_A_JPG = new File(LINES_DIR, "IMG_20131119a.jpg");
	public static final File MOLECULE_20131119_BINARY_PNG = new File(LINES_DIR, "molecule20131119Binary.png");
	public static final File MOLECULE_BINARY_CANNY_1_PNG = new File(LINES_DIR, "moleculeBinaryCanny1.png");
	public static final File MOLECULE_BINARY_CANNY_1_BMP = new File(LINES_DIR, "moleculeBinaryCanny1.bmp");
	public static final File MALTORYZINE_THINNED_PNG = new File(LINES_DIR, "thinnedMaltoryzine.png");
	
	public final static File PROCESSING_DIR = new File(IMAGE_DIR, "processing");
	public static final File HISTOGRAM_JPG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.jpg");
	public static final File HISTOGRAM_PNG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.png");
	public static final File MOLECULE_20131119_JPG = new File(PROCESSING_DIR, "IMG_20131119_180112.jpg");
	public static final File MOLECULE_CANNY_PNG = new File(PROCESSING_DIR, "moleculeCanny.png");
	public static final File MALTORYZINE_BINARY_PNG = new File(PROCESSING_DIR, "maltoryzineBinary.png");
	public static final File MALTORYZINE_FLIPPED_PNG = new File(PROCESSING_DIR, "maltoryzineFlipped.png");
	
}
