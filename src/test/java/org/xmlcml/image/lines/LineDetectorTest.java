package org.xmlcml.image.lines;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xmlcml.image.Fixtures;

public class LineDetectorTest {

	@Test
	public void testLineDetector() throws IOException {
		AbstractDetector detector = new LineDetector();
		detector.readImageFile(Fixtures.TREE_PNG);
		detector.writeFile(new File("target/tree.png"));
		detector.writeSvg(new File("target/tree.svg"));
	}
	
	@Test
	public void testLineDetector1() throws IOException {
		AbstractDetector detector = new LineDetector();
		detector.readImageFile(Fixtures.TREE1_PNG);
		detector.writeFile(new File("target/tree1.png"));
		detector.writeSvg(new File("target/tree1.svg"));
	}
	
}
