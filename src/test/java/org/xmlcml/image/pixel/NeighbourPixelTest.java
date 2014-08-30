package org.xmlcml.image.pixel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Int2;

/** finds nodes by connectivity after super thinning/
 * 
 * @author pm286
 *
 */
public class NeighbourPixelTest {
	private final static Logger LOG = Logger.getLogger(NeighbourPixelTest.class);

	private static PixelIsland DOT1;
	private static PixelIsland LINE2; // 2-pixel Line
	private static PixelIsland LINE4; // 4-pixel Line with diagonal bend
	private static PixelIsland YORTH4; // 4-pixel Y-junction with symmetry orthogonal
	private static PixelIsland YORTH7; // 7-pixel Y-junction with symmetry orthogonal
	private static PixelIsland YDIAG4; // 4-pixel Y-junction with symmetry diagonal
	private static PixelIsland YDIAG7; // 7-pixel Y-junction with symmetry diagonal
	private static PixelIsland T4;     // 4-pixel T-junction
	private static PixelIsland T7;     // 7-pixel T-junction
	private static PixelIsland T10;     // 7-pixel T-junction
	private static PixelIsland XORTH5;     // 5-pixel orthogonal cross
	private static PixelIsland XORTH9;     // 9-pixel orthogonal cross
	private static PixelIsland XDIAG5;     // 5-pixel diagonal cross
	private static PixelIsland TT6;     // 2 T-junctions joined by arms
	private static PixelIsland TT10;     // 2 T-junctions joined by arms (extended)
	private static PixelIsland A12;     // capital A with Y-nodes
	private static PixelIsland A14;     // capital A with T-junctions 
	private static PixelIsland B15;     // capital B with 2 junctions 
	private static PixelIsland E14;     // capital E
	private static PixelIsland H16;     // capital H
	private static PixelIsland O16;     // capital O
	private static PixelIsland P12;     // capital P
	private static PixelIsland Q19;     // capital Q
	private static PixelIsland R15;     // capital R
	
	static {
		makeDOT1();
		makeLINE2();
		makeLINE4();
		makeYORTH4();
		makeYORTH7();
		makeYDIAG4();
		makeYDIAG7();
		makeT4();
		makeT7();
		makeT10();
		makeX5();
		makeXORTH9();
		makeXDIAG5();
		makeTT6();
		makeTT10();
		makeA12();
		makeA14();
		makeB15();
		makeE14();
		makeH16();
		makeO16(); 
		makeP12();
		makeQ19();
		makeR15();
		
	}

	private static void makeDOT1() {
		DOT1 = new PixelIsland();
		DOT1.addPixel(new Pixel(0, 0));
	}

	private static void makeLINE2() {
		LINE2 = new PixelIsland();
		LINE2.addPixel(new Pixel(0, 0));
		LINE2.addPixel(new Pixel(0, 1));
	}

	private static void makeLINE4() {
		LINE4 = new PixelIsland();
		LINE4.setDiagonal(true);
		LINE4.addPixel(new Pixel(0, 0));
		LINE4.addPixel(new Pixel(0, 1));
		LINE4.addPixel(new Pixel(1, 2));
		LINE4.addPixel(new Pixel(2, 2));
	}

	private static void makeYORTH4() {
		YORTH4 = new PixelIsland();
		YORTH4.setDiagonal(true);
		YORTH4.addPixel(new Pixel(0, -1)); // stem
		YORTH4.addPixel(new Pixel(0, 0)); // centre of Y
		YORTH4.addPixel(new Pixel(-1, 1));
		YORTH4.addPixel(new Pixel(1, 1));
	}

	private static void makeYORTH7() {
		YORTH7 = new PixelIsland();
		YORTH7.setDiagonal(true);
		YORTH7.addPixel(new Pixel(0, -1)); // stem
		YORTH7.addPixel(new Pixel(0, -2)); // stem
		YORTH7.addPixel(new Pixel(0, 0)); // centre of Y
		YORTH7.addPixel(new Pixel(-1, 1));
		YORTH7.addPixel(new Pixel(-2, 2));
		YORTH7.addPixel(new Pixel(1, 1));
		YORTH7.addPixel(new Pixel(1, 2));
	}

	private static void makeYDIAG4() {
		YDIAG4 = new PixelIsland();
		YDIAG4.setDiagonal(true);
		YDIAG4.addPixel(new Pixel(1, 1)); // stem
		YDIAG4.addPixel(new Pixel(0, 0)); // centre of Y
		YDIAG4.addPixel(new Pixel(0, -1));
		YDIAG4.addPixel(new Pixel(-1, 0));
	}

	private static void makeYDIAG7() {
		YDIAG7 = new PixelIsland();
		YDIAG7.setDiagonal(true);
		YDIAG7.addPixel(new Pixel(1, 1)); // stem
		YDIAG7.addPixel(new Pixel(2, 2)); // stem
		YDIAG7.addPixel(new Pixel(0, 0)); // centre of Y
		YDIAG7.addPixel(new Pixel(0, -1));
		YDIAG7.addPixel(new Pixel(0, -2));
		YDIAG7.addPixel(new Pixel(-1, 0));
		YDIAG7.addPixel(new Pixel(-2, 0));
	}

	private static void makeT4() {
		T4 = new PixelIsland();       // simplest T
		T4.setDiagonal(true);
		T4.addPixel(new Pixel(1, 0)); // stem
		T4.addPixel(new Pixel(0, 0)); // centre of T
		T4.addPixel(new Pixel(0, -1));
		T4.addPixel(new Pixel(-1, 0));
	}

	private static void makeT7() {
		T7 = new PixelIsland();      // T extended by 1
		T7.setDiagonal(true);
		T7.addPixel(new Pixel(1, 0)); // stem
		T7.addPixel(new Pixel(2, 0)); // stem
		T7.addPixel(new Pixel(0, 0)); // centre of T
		T7.addPixel(new Pixel(0, -1));
		T7.addPixel(new Pixel(0, -2));
		T7.addPixel(new Pixel(-1, 0));
		T7.addPixel(new Pixel(-2, 0));
	}

	private static void makeT10() {
		T10 = new PixelIsland();      // T extended by 2
		T10.setDiagonal(true);
		T10.addPixel(new Pixel(1, 0)); // stem
		T10.addPixel(new Pixel(2, 0)); // stem
		T10.addPixel(new Pixel(3, 0)); // stem
		T10.addPixel(new Pixel(0, 0)); // centre of T
		T10.addPixel(new Pixel(0, -1));
		T10.addPixel(new Pixel(0, -2));
		T10.addPixel(new Pixel(0, -3));
		T10.addPixel(new Pixel(-1, 0));
		T10.addPixel(new Pixel(-2, 0));
		T10.addPixel(new Pixel(-3, 0));
	}

	private static void makeX5() {
		XORTH5 = new PixelIsland();      // Orthogonal cross
		XORTH5.setDiagonal(true);
		XORTH5.addPixel(new Pixel(0, 0)); // centre of X
		XORTH5.addPixel(new Pixel(0, 1));
		XORTH5.addPixel(new Pixel(0, -1));
		XORTH5.addPixel(new Pixel(1, 0));
		XORTH5.addPixel(new Pixel(-1, 0));
	}

	private static void makeXORTH9() {
		XORTH9 = new PixelIsland();      // Orthogonal cross extended 1
		XORTH9.setDiagonal(true);
		XORTH9.addPixel(new Pixel(0, 0)); // centre of X
		XORTH9.addPixel(new Pixel(0, 1));
		XORTH9.addPixel(new Pixel(0, 2));
		XORTH9.addPixel(new Pixel(0, -1));
		XORTH9.addPixel(new Pixel(0, -2));
		XORTH9.addPixel(new Pixel(1, 0));
		XORTH9.addPixel(new Pixel(2, 0));
		XORTH9.addPixel(new Pixel(-1, 0));
		XORTH9.addPixel(new Pixel(-2, 0));
	}

	private static void makeXDIAG5() {
		XDIAG5 = new PixelIsland();      // Diagonal cross
		XDIAG5.setDiagonal(true);
		XDIAG5.addPixel(new Pixel(0, 0)); // centre of X
		XDIAG5.addPixel(new Pixel(1, 1));
		XDIAG5.addPixel(new Pixel(-1, -1));
		XDIAG5.addPixel(new Pixel(1, -1));
		XDIAG5.addPixel(new Pixel(-1, 1));
	}

	private static void makeTT6() {
		TT6 = new PixelIsland();      // 2 T-junctions joined by arms
		TT6.setDiagonal(true);
		TT6.addPixel(new Pixel(0, 0)); 
		TT6.addPixel(new Pixel(1, 0)); // first centre
		TT6.addPixel(new Pixel(1, 1)); // first stem
		TT6.addPixel(new Pixel(2, 0)); // second centre
		TT6.addPixel(new Pixel(2, -1));// second stem
		TT6.addPixel(new Pixel(3, 0));
	}

	private static void makeTT10() {
		TT10 = new PixelIsland();      // 2 T-junctions joined by arms
		TT10.setDiagonal(true);
		TT10.addPixel(new Pixel(-1, 0)); 
		TT10.addPixel(new Pixel(0, 0)); 
		TT10.addPixel(new Pixel(1, 0)); // first centre
		TT10.addPixel(new Pixel(1, 1)); // first stem
		TT10.addPixel(new Pixel(1, 2)); // first stem
		TT10.addPixel(new Pixel(2, 0)); // second centre
		TT10.addPixel(new Pixel(2, -1));// second stem
		TT10.addPixel(new Pixel(2, -2));// second stem
		TT10.addPixel(new Pixel(3, 0));
		TT10.addPixel(new Pixel(4, 0));
	}

	private static void makeA12() {
		A12 = new PixelIsland(); // capital A with Y-junctions
		A12.setDiagonal(true);
		A12.addPixel(new Pixel(0, 0)); // left leg
		A12.addPixel(new Pixel(0, 1)); // left leg
		A12.addPixel(new Pixel(1, 2)); // left node
		A12.addPixel(new Pixel(2, 2)); // bar
		A12.addPixel(new Pixel(3, 2)); // right node
		A12.addPixel(new Pixel(4, 1)); // right leg
		A12.addPixel(new Pixel(4, 0)); // right leg
		A12.addPixel(new Pixel(0, 3)); // arch
		A12.addPixel(new Pixel(1, 4)); // arch
		A12.addPixel(new Pixel(2, 4)); // arch
		A12.addPixel(new Pixel(3, 4)); // arch
		A12.addPixel(new Pixel(4, 3)); // arch
	}

	private static void makeA14() {
		A14 = new PixelIsland(); // capital A with Y-junctions
		A14.setDiagonal(true);
		A14.addPixel(new Pixel(0, 0)); // left leg
		A14.addPixel(new Pixel(0, 1)); // left leg
		A14.addPixel(new Pixel(0, 2)); // left T centre
		A14.addPixel(new Pixel(1, 2)); // left node
		A14.addPixel(new Pixel(2, 2)); // bar
		A14.addPixel(new Pixel(3, 2)); // right node
		A14.addPixel(new Pixel(4, 2)); // right T centre
		A14.addPixel(new Pixel(4, 1)); // right leg
		A14.addPixel(new Pixel(4, 0)); // right leg
		A14.addPixel(new Pixel(0, 3)); // arch
		A14.addPixel(new Pixel(1, 4)); // arch
		A14.addPixel(new Pixel(2, 4)); // arch
		A14.addPixel(new Pixel(3, 4)); // arch
		A14.addPixel(new Pixel(4, 3)); // arch
	}

	private static void makeE14() {
		E14 = new PixelIsland(); // capital E
		E14.setDiagonal(true);
		E14.addPixel(new Pixel(0, 0)); // centre
		E14.addPixel(new Pixel(0, 1)); // vert
		E14.addPixel(new Pixel(0, 2)); // vert
		E14.addPixel(new Pixel(1, 3)); // corner
		E14.addPixel(new Pixel(2, 3)); // level
		E14.addPixel(new Pixel(3, 3)); // level
		E14.addPixel(new Pixel(1, 0)); // middle
		E14.addPixel(new Pixel(2, 0)); // middle
		E14.addPixel(new Pixel(3, 0)); // middle
		E14.addPixel(new Pixel(0, -1)); // vert
		E14.addPixel(new Pixel(0, -2)); // vert
		E14.addPixel(new Pixel(1, -3)); // corner
		E14.addPixel(new Pixel(2, -3)); // level
		E14.addPixel(new Pixel(3, -3)); // level
	}

	private static void makeH16() {
		H16 = new PixelIsland(); // capital H
		H16.setDiagonal(true);
		H16.addPixel(new Pixel(0, 0)); // centre
		H16.addPixel(new Pixel(0, 1)); // vert
		H16.addPixel(new Pixel(0, 2)); // vert
		H16.addPixel(new Pixel(0, 3)); // end
		H16.addPixel(new Pixel(0, -1)); // vert
		H16.addPixel(new Pixel(0, -2)); // vert
		H16.addPixel(new Pixel(0, -3)); // end
		H16.addPixel(new Pixel(1, 0)); // level
		H16.addPixel(new Pixel(2, 0)); // level
		H16.addPixel(new Pixel(3, 0)); // level
		H16.addPixel(new Pixel(3, 1)); // vert
		H16.addPixel(new Pixel(3, 2)); // vert
		H16.addPixel(new Pixel(3, 3)); // end
		H16.addPixel(new Pixel(3, -1)); // vert
		H16.addPixel(new Pixel(3, -2)); // vert
		H16.addPixel(new Pixel(3, -3)); // end
	}

	private static void makeO16() {
		O16 = new PixelIsland(); // capital O
		O16.setDiagonal(true);
		O16.addPixel(new Pixel(-3, 0)); 
		O16.addPixel(new Pixel(-3, 1)); 
		O16.addPixel(new Pixel(-2, 2)); 
		O16.addPixel(new Pixel(-1, 3)); 
		O16.addPixel(new Pixel(0, 3)); 
		O16.addPixel(new Pixel(1, 3)); 
		O16.addPixel(new Pixel(2, 2)); 
		O16.addPixel(new Pixel(3, 1)); 
		O16.addPixel(new Pixel(3, 0)); 
		O16.addPixel(new Pixel(3, -1)); 
		O16.addPixel(new Pixel(2, -2)); 
		O16.addPixel(new Pixel(1, -3)); 
		O16.addPixel(new Pixel(0, -3)); 
		O16.addPixel(new Pixel(-1, -3)); 
		O16.addPixel(new Pixel(-2, -2)); 
		O16.addPixel(new Pixel(-3, -1));
	}

	private static void makeP12() {
		P12 = new PixelIsland(); // capital P
		P12.setDiagonal(true);
		P12.addPixel(new Pixel(0, 0)); // centre
		P12.addPixel(new Pixel(0, 1)); // vert
		P12.addPixel(new Pixel(0, 2)); // vert
		P12.addPixel(new Pixel(1, 3)); // corner
		P12.addPixel(new Pixel(2, 3)); // level
		P12.addPixel(new Pixel(3, 2)); // corner
		P12.addPixel(new Pixel(3, 1)); // vert
		P12.addPixel(new Pixel(2, 0)); // corner
		P12.addPixel(new Pixel(1, 0)); // bridge
		P12.addPixel(new Pixel(0, -1)); // vert
		P12.addPixel(new Pixel(0, -2)); // vert
		P12.addPixel(new Pixel(0, -3)); // corner
	}

	private static void makeQ19() {
		Q19 = new PixelIsland(); // capital Q
		Q19.setDiagonal(true);
		Q19.addPixel(new Pixel(-3, 0)); 
		Q19.addPixel(new Pixel(-3, 1)); 
		Q19.addPixel(new Pixel(-2, 2)); 
		Q19.addPixel(new Pixel(-1, 3)); 
		Q19.addPixel(new Pixel(0, 3)); 
		Q19.addPixel(new Pixel(1, 3)); 
		Q19.addPixel(new Pixel(2, 2)); 
		Q19.addPixel(new Pixel(3, 1)); 
		Q19.addPixel(new Pixel(3, 0)); 
		Q19.addPixel(new Pixel(3, -1)); 
		Q19.addPixel(new Pixel(2, -2)); 
		Q19.addPixel(new Pixel(1, -3)); 
		Q19.addPixel(new Pixel(0, -3)); 
		Q19.addPixel(new Pixel(-1, -3)); 
		Q19.addPixel(new Pixel(-2, -2)); 
		Q19.addPixel(new Pixel(-3, -1)); 
		Q19.addPixel(new Pixel(-1, -1)); // tail
		Q19.addPixel(new Pixel(-3, -3)); // tail
		Q19.addPixel(new Pixel(-4, -4)); // tail
	}

	private static void makeR15() {
		R15 = new PixelIsland(); // capital R
		R15.setDiagonal(true);
		R15.addPixel(new Pixel(0, 0)); // centre
		R15.addPixel(new Pixel(0, 1)); // vert
		R15.addPixel(new Pixel(0, 2)); // vert
		R15.addPixel(new Pixel(1, 3)); // corner
		R15.addPixel(new Pixel(2, 3)); // level
		R15.addPixel(new Pixel(3, 2)); // corner
		R15.addPixel(new Pixel(3, 1)); // vert
		R15.addPixel(new Pixel(2, 0)); // corner
		R15.addPixel(new Pixel(1, 0)); // bridge
		R15.addPixel(new Pixel(0, -1)); // vert
		R15.addPixel(new Pixel(0, -2)); // vert
		R15.addPixel(new Pixel(1, -3)); // corner
		R15.addPixel(new Pixel(2, -3)); // level
		R15.addPixel(new Pixel(3, -3)); // corner
		R15.addPixel(new Pixel(4, -3)); // vert
	}

	private static void makeB15() {
		B15 = new PixelIsland(); // capital B
		B15.setDiagonal(true);
		B15.addPixel(new Pixel(0, 0)); // centre
		B15.addPixel(new Pixel(0, 1)); // vert
		B15.addPixel(new Pixel(0, 2)); // vert
		B15.addPixel(new Pixel(1, 3)); // corner
		B15.addPixel(new Pixel(2, 3)); // level
		B15.addPixel(new Pixel(3, 2)); // corner
		B15.addPixel(new Pixel(3, 1)); // vert
		B15.addPixel(new Pixel(2, 0)); // corner
		B15.addPixel(new Pixel(1, 0)); // bridge
		B15.addPixel(new Pixel(0, -1)); // vert
		B15.addPixel(new Pixel(0, -2)); // vert
		B15.addPixel(new Pixel(1, -3)); // corner
		B15.addPixel(new Pixel(2, -3)); // level
		B15.addPixel(new Pixel(3, -2)); // corner
		B15.addPixel(new Pixel(3, -1)); // vert
	}

	@Test
	public void testDot() {
		Assert.assertEquals(1, DOT1.getPixelList().size());
		Assert.assertEquals(1, DOT1.getPixelsWithNeighbours(0).size());
		Assert.assertEquals(0, DOT1.getPixelsWithNeighbours(1).size());
	}
	
	@Test
	public void testLine2Size() {
		Assert.assertEquals(2, LINE2.getPixelList().size());
	}
	
	@Test
	public void testLine2Connections() {
		//debug(LINE2);
		Assert.assertEquals("LINE2 0 neighbours", 0, LINE2.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("LINE2 1 neighbours", 2, LINE2.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("LINE2 2 neighbours", 0, LINE2.getPixelsWithNeighbours(2).size());
	}
	
	@Test
	public void testLINE4() {
		//debug(LINE4);
		Assert.assertEquals("LINE4 1 neighbours", 2, LINE4.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("LINE4 2 neighbours", 2, LINE4.getPixelsWithNeighbours(2).size());
	}
	
	
	@Test
	public void testYORTH4() {
		//debug(YORTH4);
		Assert.assertEquals("YORTH4 0 neighbours", 0, YORTH4.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("YORTH4 1 neighbours", 3, YORTH4.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("YORTH4 2 neighbours", 0, YORTH4.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("YORTH4 3 neighbours", 1, YORTH4.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("YORTH4 4 neighbours", 0, YORTH4.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testYORTH7() {
		//debug(YORTH7);
		Assert.assertEquals("YORTH7 0 neighbours", 0, YORTH7.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("YORTH7 1 neighbours", 3, YORTH7.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("YORTH7 2 neighbours", 3, YORTH7.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("YORTH7 3 neighbours", 1, YORTH7.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("YORTH7 4 neighbours", 0, YORTH7.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testYDIAG4() {
		//debug(YDIAG4);
		Assert.assertEquals("YDIAG4 0 neighbours", 0, YDIAG4.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("YDIAG4 1 neighbours", 1, YDIAG4.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("YDIAG4 2 neighbours", 2, YDIAG4.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("YDIAG4 3 neighbours", 1, YDIAG4.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("YDIAG4 4 neighbours", 0, YDIAG4.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testYDIAG7() {
		//debug(YDIAG7);
		Assert.assertEquals("YDIAG7 0 neighbours", 0, YDIAG7.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("YDIAG7 1 neighbours", 3, YDIAG7.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("YDIAG7 2 neighbours", 1, YDIAG7.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("YDIAG7 3 neighbours", 3, YDIAG7.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("YDIAG7 4 neighbours", 0, YDIAG7.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testT4() {
		//debug(T4);
		Assert.assertEquals("T4 0 neighbours", 0, T4.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("T4 1 neighbours", 0, T4.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("T4 2 neighbours", 2, T4.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("T4 3 neighbours", 2, T4.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("T4 4 neighbours", 0, T4.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testT7() {
		//debug(T7);
		Assert.assertEquals("T7 0 neighbours", 0, T7.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("T7 1 neighbours", 3, T7.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("T7 2 neighbours", 0, T7.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("T7 3 neighbours", 3, T7.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("T7 4 neighbours", 1, T7.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testT10() {
		//debug(T10);
		Assert.assertEquals("T10 0 neighbours", 0, T10.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("T10 1 neighbours", 3, T10.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("T10 2 neighbours", 3, T10.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("T10 3 neighbours", 3, T10.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("T10 4 neighbours", 1, T10.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testXORTH5() {
		//debug(XORTH5);
		Assert.assertEquals("XORTH5 0 neighbours", 0, XORTH5.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("XORTH5 1 neighbours", 0, XORTH5.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("XORTH5 2 neighbours", 0, XORTH5.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("XORTH5 3 neighbours", 4, XORTH5.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("XORTH5 4 neighbours", 1, XORTH5.getPixelsWithNeighbours(4).size());
	}

	public void testXORTH9() {
		//debug(XORTH9);
		Assert.assertEquals("XORTH9 0 neighbours", 0, XORTH9.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("XORTH9 1 neighbours", 4, XORTH9.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("XORTH9 2 neighbours", 0, XORTH9.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("XORTH9 3 neighbours", 4, XORTH9.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("XORTH9 4 neighbours", 1, XORTH9.getPixelsWithNeighbours(4).size());
	}

	@Test
	public void testXDIAG5() {
		//debug(XDIAG5);
		Assert.assertEquals("XDIAG5 0 neighbours", 0, XDIAG5.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("XDIAG5 1 neighbours", 4, XDIAG5.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("XDIAG5 2 neighbours", 0, XDIAG5.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("XDIAG5 3 neighbours", 0, XDIAG5.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("XDIAG5 4 neighbours", 1, XDIAG5.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testTT6() {
		//debug(TT6);
		Assert.assertEquals("TT6 0 neighbours", 0, TT6.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("TT6 1 neighbours", 0, TT6.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("TT6 2 neighbours", 2, TT6.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("TT6 3 neighbours", 2, TT6.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("TT6 4 neighbours", 2, TT6.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testTT10() {
		//debug(TT10);
		Assert.assertEquals("TT10 0 neighbours", 0, TT10.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("TT10 1 neighbours", 4, TT10.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("TT10 2 neighbours", 0, TT10.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("TT10 3 neighbours", 2, TT10.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("TT10 4 neighbours", 4, TT10.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testA12() {
		//debug(A12);
		Assert.assertEquals("A12 0 neighbours", 0, A12.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("A12 1 neighbours", 2, A12.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("A12 2 neighbours", 8, A12.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("A12 3 neighbours", 2, A12.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("A12 4 neighbours", 0, A12.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testA14() {
		//debug(A14);
		Assert.assertEquals("A14 0 neighbours", 0, A14.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("A14 1 neighbours", 2, A14.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("A14 2 neighbours", 4, A14.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("A14 3 neighbours", 6, A14.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("A14 4 neighbours", 2, A14.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testB16() {
		//debug(B16);
		Assert.assertEquals("B16 0 neighbours", 0, B15.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("B16 1 neighbours", 0, B15.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("B16 2 neighbours", 10, B15.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("B16 3 neighbours", 4, B15.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("B16 4 neighbours", 1, B15.getPixelsWithNeighbours(4).size());
	}
	
	@Test
	public void testE14() {
		//debug(E14);
		Assert.assertEquals("E14 0 neighbours", 0, E14.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("E14 1 neighbours", 3, E14.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("E14 2 neighbours", 7, E14.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("E14 3 neighbours", 3, E14.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("E14 4 neighbours", 1, E14.getPixelsWithNeighbours(4).size());
	}
	
	public void testH16() {
		//debug(H16);
		Assert.assertEquals("H16 0 neighbours", 0, H16.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("H16 1 neighbours", 4, H16.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("H16 2 neighbours", 4, H16.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("H16 3 neighbours", 6, H16.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("H16 4 neighbours", 2, H16.getPixelsWithNeighbours(4).size());
	}
	
	public void testO16() {
		//debug(O16);
		Assert.assertEquals("O16 0 neighbours", 0, O16.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("O16 1 neighbours", 0, O16.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("O16 2 neighbours", 16, O16.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("O16 3 neighbours", 0, O16.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("O16 4 neighbours", 0, O16.getPixelsWithNeighbours(4).size());
	}
	
	public void testP12() {
		//debug(P12);
		Assert.assertEquals("P12 0 neighbours", 0, P12.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("P12 1 neighbours", 1, P12.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("P12 2 neighbours", 7, P12.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("P12 3 neighbours", 3, P12.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("P12 4 neighbours", 1, P12.getPixelsWithNeighbours(4).size());
	}
	
	public void testQ19() {
		//debug(Q19);
		Assert.assertEquals("Q19 0 neighbours", 0, Q19.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("Q19 1 neighbours", 2, Q19.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("Q19 2 neighbours", 16, Q19.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("Q19 3 neighbours", 0, Q19.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("Q19 4 neighbours", 1, Q19.getPixelsWithNeighbours(4).size());
	}
	
	public void testR15() {
		//debug(R15);
		Assert.assertEquals("R15 0 neighbours", 0, R15.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("R15 1 neighbours", 2, R15.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("R15 2 neighbours", 8, R15.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("R15 3 neighbours", 4, R15.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("R15 4 neighbours", 1, R15.getPixelsWithNeighbours(4).size());
	}
	
	public void testGetNodePixelList() {
		PixelList nodePixelList = A12.getNodePixelList();
		Assert.assertEquals("A12 nodes", 4, nodePixelList.size());
	}
	
	@Test
	public void testSuperthin() {
		PixelIsland a12a = new PixelIsland(A14);
		a12a.setDiagonal(true);
//		debug(a12a);
		PixelList pixelList = a12a.getPixelList();
		Assert.assertEquals(14, pixelList.size());
		Assert.assertEquals("A12a 0 neighbours", 0, a12a.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("A12a 1 neighbours", 2, a12a.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("A12a 2 neighbours", 4, a12a.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("A12a 3 neighbours", 6, a12a.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("A12a 4 neighbours", 2, a12a.getPixelsWithNeighbours(4).size());
		
		PixelNucleusList nucleusList = a12a.superthin();
		Assert.assertEquals(2, nucleusList.size());
		PixelList newPixelList = a12a.getPixelList();
		Assert.assertEquals(12, newPixelList.size());
//		LOG.debug("======");
//		debug(a12a);
		Assert.assertEquals("A12a 0 neighbours", 0, a12a.getPixelsWithNeighbours(0).size());
		Assert.assertEquals("A12a 1 neighbours", 2, a12a.getPixelsWithNeighbours(1).size());
		Assert.assertEquals("A12a 2 neighbours", 8, a12a.getPixelsWithNeighbours(2).size());
		Assert.assertEquals("A12a 3 neighbours", 2, a12a.getPixelsWithNeighbours(3).size());
		Assert.assertEquals("A12a 4 neighbours", 0, a12a.getPixelsWithNeighbours(4).size());
	}

	/** create PixelNodes.
	 * 
	 */
	@Test
	public void testNodes() {
		PixelNodeList nodeList = A12.getNodeList();//  getNodes();
		Assert.assertEquals("nodes", 4, nodeList.size());
		Set<String> nodeStringSet = new HashSet<String>();
		for (PixelNode node : nodeList) {
			nodeStringSet.add(node.getCentrePixel().getInt2().toString());
		}
		Assert.assertTrue(nodeStringSet.contains("(0,0)"));
		Assert.assertTrue(nodeStringSet.contains("(1,2)"));
		Assert.assertTrue(nodeStringSet.contains("(3,2)"));
		Assert.assertTrue(nodeStringSet.contains("(4,0)"));
		//
	}

	@Test
	public void testCreateEdge() {
		PixelNodeList nodeList = A12.getNodeList();//  getNodes();
		PixelNode node00 = nodeList.getPixelNode(new Int2(0,0));
//		PixelEdge edge0 = A12.getEdge(node00);
		
		PixelNode node40 = nodeList.getPixelNode(new Int2(4,0));
	}

	
	

	// ===================================
	
	private void debug(PixelIsland island) {
		for (Pixel node : island.getPixelList()) {
			LOG.debug(" "+node+" : "+node.getNeighbours(island));
		}
	}
	
}
