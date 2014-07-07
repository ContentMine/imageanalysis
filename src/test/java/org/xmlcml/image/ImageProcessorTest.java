package org.xmlcml.image;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xmlcml.image.processing.ZhangSuenThinning;

public class ImageProcessorTest {

	private ImageProcessor PROCESSOR;
	
	@Before
	public void setUp() {
		this.PROCESSOR  = new ImageProcessor();
	}
	
	/** should give a help/usage().
	 * 
	 */
	@Test
	public void testUsage() {
		String[] args = {};
		ArgIterator argIterator = new ArgIterator(args);
		PROCESSOR.parseArgs(argIterator);		
	}
	
	/** verify defaults
	 * 
	 */
	@Test
	public void testCommandLineDefaults() {
		String[] args = {"-d"};
		ArgIterator argIterator = new ArgIterator(args);
		PROCESSOR.parseArgs(argIterator);		
		Assert.assertTrue("debug", PROCESSOR.getDebug());
		Assert.assertNull("base", PROCESSOR.getBase());
		Assert.assertNull("inputFile", PROCESSOR.getInputFile());
		Assert.assertEquals("outputDir", "target", PROCESSOR.getOutputDir().toString());
		Assert.assertTrue("binarization", PROCESSOR.getBinarize());
		Assert.assertEquals("threshold", 128, PROCESSOR.getThreshold());
		Assert.assertEquals("thinning", ZhangSuenThinning.class, PROCESSOR.getThinning().getClass());
	}
	
	/** try all args.
	 * 
	 */
	@Test
	public void testCommandLine() {
		String[] args = {"-i", "fred", "-o", "sue", "-b", "-d", "-t", "180", "-v", "z"};
		ArgIterator argIterator = new ArgIterator(args);
		PROCESSOR.parseArgs(argIterator);		
		Assert.assertNotNull("base", PROCESSOR.getBase());
		Assert.assertEquals("base", "fred", PROCESSOR.getBase());
		Assert.assertEquals("inputFile", "fred", PROCESSOR.getInputFile().toString());
		Assert.assertNotNull("outputDir", PROCESSOR.getOutputDir());
		Assert.assertEquals("inputFile", "sue", PROCESSOR.getOutputDir().toString());
		Assert.assertTrue("binarization", PROCESSOR.getBinarize());
		Assert.assertTrue("debug", PROCESSOR.getDebug());
		Assert.assertEquals("threshold", 180, PROCESSOR.getThreshold());
		Assert.assertEquals("thinning", ZhangSuenThinning.class, PROCESSOR.getThinning().getClass());
	}
	
	
	/** some errors
	 * 
	 */
	@Test
	public void testCommandLineErrors() {
		String[] args = {"-i", "-q", "-o", "sue", "-b", "dummy", "-t", "x160", "-v", "junk"};
		ArgIterator argIterator = new ArgIterator(args);
		PROCESSOR.parseArgs(argIterator);		
		Assert.assertNull("base", PROCESSOR.getBase());
		Assert.assertNull("inputFile", PROCESSOR.getInputFile());
		Assert.assertNotNull("outputDir", PROCESSOR.getOutputDir());
		Assert.assertEquals("inputFile", "sue", PROCESSOR.getOutputDir().toString());
		Assert.assertTrue("binarization", PROCESSOR.getBinarize());
		Assert.assertFalse("debug", PROCESSOR.getDebug());
		Assert.assertEquals("threshold", 128, PROCESSOR.getThreshold());
		Assert.assertEquals("thinning", ZhangSuenThinning.class, PROCESSOR.getThinning().getClass());
	}
}
