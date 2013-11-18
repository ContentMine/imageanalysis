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
	
	public final static File PROCESSING_DIR = new File(IMAGE_DIR, "processing");
	public static final File HISTOGRAM_JPG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.jpg");
	public static final File HISTOGRAM_PNG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.png");
	
}
