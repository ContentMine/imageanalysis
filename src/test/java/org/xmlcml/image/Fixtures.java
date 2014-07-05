package org.xmlcml.image;

import java.awt.Point;
import java.io.File;
import java.util.Arrays;

import org.xmlcml.image.pixel.Pixel;
import org.xmlcml.image.pixel.PixelList;

public class Fixtures {

	public final static File RESOURCE_DIR = new File("src/test/resources");
	public final static File IMAGE_DIR = new File(RESOURCE_DIR, "org/xmlcml/image");
	
	public final static File JOURNAL_HTML = new File(IMAGE_DIR, "journal.html");
	
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
	
	public final static File PROCESSING_DIR = new File(IMAGE_DIR, "processing");
	public static final File HISTOGRAM_JPG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.jpg");
	public static final File HISTOGRAM_PNG = new File(PROCESSING_DIR, "300px-Unequalized_Hawkes_Bay_NZ.png");
	public static final File MOLECULE_20131119_JPG = new File(PROCESSING_DIR, "IMG_20131119_180112.jpg");
	public static final File MOLECULE_CANNY_PNG = new File(PROCESSING_DIR, "moleculeCanny.png");
	public static final File MALTORYZINE_BINARY_PNG = new File(PROCESSING_DIR, "maltoryzineBinary.png");
	public static final File MALTORYZINE_FLIPPED_PNG = new File(PROCESSING_DIR, "maltoryzineFlipped.png");
	public static final File MALTORYZINE_THINNED_PNG = new File(PROCESSING_DIR, "thinnedMaltoryzine.png");
	public static final File BRANCH0_PNG = new File(PROCESSING_DIR, "branch0.png");
	public static final File BRANCH_PNG = new File(PROCESSING_DIR, "branch.png");
	public static final File HEXAGON_PNG = new File(PROCESSING_DIR, "hexagon.png");
	public static final File LINE_PNG = new File(PROCESSING_DIR, "line.png");
	public static final File MALTORYZINE0_PNG = new File(PROCESSING_DIR, "maltoryzine0.png");
	public static final File TERMINAL_PNG = new File(PROCESSING_DIR, "terminalnode.png");
	public static final File TERMINALS_PNG = new File(PROCESSING_DIR, "terminalnodes.png");
	public static final File ZIGZAG_PNG = new File(PROCESSING_DIR, "zigzag.png");


	public final static File TEXT_DIR = new File(IMAGE_DIR, "text");
	public static final File NRRL_PNG = new File(TEXT_DIR, "NRRL.png");
	public static final File NO2 = new File(TEXT_DIR, "NO2.png");
	public static final File GIBBONS_PNG = new File(TEXT_DIR, "gibbons.png");
	public static final File HELVETICA_PNG = new File(TEXT_DIR, "helvetica.png");
	public static final File HELVETICA_BOLD_PNG = new File(TEXT_DIR, "helveticaBold.png");
	public static final File LUCIDA_PNG = new File(TEXT_DIR, "lucida.png");
	public static final File MONOSPACE_PNG = new File(TEXT_DIR, "monospace.png");
	public static final File TIMESROMAN_JPG = new File(TEXT_DIR, "timesroman.jpg");
	public static final File TIMES_GIF = new File(TEXT_DIR, "times.gif");
	public static final File LARGE_PHYLO_JPG = new File(Fixtures.LINES_DIR, "1471-2148-13-93-1-l.jpg");

	public static final File GENERAL_DIR = new File(IMAGE_DIR, "general");
	public static final File REFFONT_DIR = new File(GENERAL_DIR, "refFont");
	public static final File COMPOUND_DIR = new File(IMAGE_DIR, "compound");


	
	public static PixelList LINE_LIST = new PixelList(Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(1, 5)), 
			}));
	
	public static PixelList DIAG_LIST = new PixelList(Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(2, 2)),
			new Pixel(new Point(3, 3)), 
			new Pixel(new Point(4, 4)), 
			new Pixel(new Point(5, 5)), 
			}));
	

	/**
	 * Tests the pixels below.
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * +++
	 * +
	 * +
	 */
	public static PixelList T_LIST = new PixelList(Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(1, 5)), 
			new Pixel(new Point(2, 3)), 
			new Pixel(new Point(3, 3)), 
			}));
		
	public static PixelList L_LIST = new PixelList(Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(2, 3)), 
			new Pixel(new Point(3, 3)), 
			}));

	/**
	 * 
	 * X is right Y is down 
	 * 
	 * +
	 * +
	 * ++
	 * +
	 */
	public static PixelList LONG_T_LIST = new PixelList(Arrays.asList(new Pixel[] {
			new Pixel(new Point(1, 1)), 
			new Pixel(new Point(1, 2)),
			new Pixel(new Point(1, 3)), 
			new Pixel(new Point(1, 4)), 
			new Pixel(new Point(2, 3)), 
			}));
	
	public static File CHAR_DIR = new File(Fixtures.TEXT_DIR, "chars");

	
}
