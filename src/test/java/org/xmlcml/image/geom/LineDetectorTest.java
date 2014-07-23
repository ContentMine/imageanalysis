package org.xmlcml.image.geom;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.image.Fixtures;
import org.xmlcml.image.geom.AbstractDetector;
import org.xmlcml.image.geom.LineDetector;

public class LineDetectorTest {

	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testLineDetector() throws IOException {
		AbstractDetector detector = new LineDetector();
		detector.readImageFile(Fixtures.TREE_PNG);
		detector.writeFile(new File("target/tree.png"));
		detector.writeSvg(new File("target/tree.svg"));
	}
	
	@Test
	@Ignore // file gone missing (maybe in earlier version)
	public void testLineDetector1() throws IOException {
		AbstractDetector detector = new LineDetector();
		detector.readImageFile(Fixtures.TREE1_PNG);
		detector.writeFile(new File("target/tree1.png"));
		detector.writeSvg(new File("target/tree1.svg"));
	}
	
}
