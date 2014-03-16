package org.xmlcml.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.lines.BoofcvCanny;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

public class BoofcvCannyTest {
	
	private final static Logger LOG = Logger.getLogger(BoofcvCannyTest.class);
	/**
	 * Demonstration of the Canny edge detection algorithm.  In this implementation the output can be a binary image and/or
	 * a graph describing each contour.
	 *
	 * @author Peter Abeles
	 */

	@Test
	public void testCanny() throws Exception {
		BufferedImage image = UtilImageIO.loadImage(new File(Fixtures.TEXT_DIR, "1471-2148-14-31-2-l.jpg").getAbsolutePath());
 
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
 
		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8,ImageSInt16> canny = FactoryEdgeDetectors.canny(2,true, true, ImageUInt8.class, ImageSInt16.class);
 
		// The edge image is actually an optional parameter.  If you don't need it just pass in null
		canny.process(gray,0.1f,0.3f,edgeImage);
 
		// First get the contour created by canny
		List<EdgeContour> edgeContours = canny.getContours();
		Assert.assertEquals("edgeContours: ", 772, edgeContours.size());
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, 8, null);
		Assert.assertEquals("contours", 379, contours.size());
		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, null);
		BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours,null,
				gray.width,gray.height,null);
		BufferedImage visualEdgeContour = VisualizeBinaryData.renderExternal(contours, null,
				gray.width, gray.height, null);
		new File("target/contours").mkdirs();
		ImageUtil.writeImageQuietly(visualCannyContour, "target/contours/_cannyContour.png");
		ImageUtil.writeImageQuietly(visualBinary, "target/contours/_binary.png");
		ImageUtil.writeImageQuietly(visualEdgeContour, "target/contours/_dgeContour.png");
	}

	@Test
	public void testCanny1() throws Exception {
		BufferedImage image = UtilImageIO.loadImage(new File(Fixtures.TEXT_DIR, "1471-2148-14-31-2-l.jpg").getAbsolutePath());
 
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
 
		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8,ImageSInt16> canny = FactoryEdgeDetectors.canny(2,true, true, ImageUInt8.class, ImageSInt16.class);
 
		// The edge image is actually an optional parameter.  If you don't need it just pass in null
		canny.process(gray,0.1f,0.3f,edgeImage);
 
		// First get the contour created by canny
		List<EdgeContour> edgeContours = canny.getContours();
		LOG.debug("edgeContours: "+edgeContours.size());
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, 8, null);
		Assert.assertEquals("contours", 379, contours.size());
		SVGG g = new SVGG();
		int i = 0;
		new File("target/contours").mkdirs();
		for (Contour contour : contours) {
			int size = contour.external.size();
			if (size > 200) {
				AMIContour amiContour = new AMIContour(contour);
				double width = 1.0;
				SVGG gg = amiContour.createExternalContourSVG("yellow", width);
				g.appendChild(gg);
				gg = new SVGG(gg);
				SVGSVG.wrapAndWriteAsSVG(gg, new File("target/contours/"+i+".svg"));
				
 				AMIContour reducedContour = amiContour.reduceExternal(0.1);
				width = 0.5;
				SVGG ggred = reducedContour.createExternalContourSVG("blue", width);
				if (getMaxLineLength(ggred) < 5.0) continue;
				SVGLine line = SVGLine.extractSelfAndDescendantLines(ggred).get(0);
				SVGCircle circle = new SVGCircle(line.getXY(0), 4.0);
				circle.setFill("red");
				g.appendChild(circle);
				g.appendChild(ggred);
				i++;
			}
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/contours/_all.svg"));
	}

	private double getMaxLineLength(SVGG ggred) {
		List<SVGLine> lines = SVGLine.extractSelfAndDescendantLines(ggred);
		double maxlen = 0;
		for (SVGLine line : lines) {
			double len = line.getLength();
			if (len > maxlen) {
				maxlen = len;
			}
		}
		return maxlen;
	}

	@Test
	public void testContour() {
		SVGElement contour = SVGElement.readAndCreateSVG(new File(Fixtures.LINES_DIR, "contours/13.svg"));
		contour.debug("contour");
		List<SVGLine> lines = SVGLine.extractSelfAndDescendantLines(contour);
		Assert.assertEquals("lines", 477, lines.size());	
	}

	@Test
	public void testMoleculeCanny() {
		String filename = new File(Fixtures.LINES_DIR, "maltoryzine.png").getAbsolutePath();
		BoofcvCanny canny = new BoofcvCanny();
		List<AMIContour> amiContours = canny.extractAMIContours(filename);
		Assert.assertEquals("contours", 6, amiContours.size());
		SVGG g = new SVGG();
		String[] colors = {"red", "green", "blue", "yellow", "orange", "magenta"};
		int i = 0;
		for (AMIContour amiContour : amiContours) {
//			SVGG gg = amiContour.createExternalContourSVG(colors[i], 1);
			LOG.debug(amiContour);
			AMIContour contourr = amiContour.reduceExternal(1);
			LOG.debug(contourr);
			g.appendChild(contourr.createExternalContourSVG(colors[i], 1));
			i++;
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/maltoryzine.svg"));
	}

}
