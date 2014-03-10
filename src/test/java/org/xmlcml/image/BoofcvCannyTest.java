package org.xmlcml.image;

import georegression.struct.line.LineParametric2D_F32;
import georegression.struct.line.LineSegment2D_F32;
import georegression.struct.point.Point2D_I32;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;

import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSingleBand;
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
		LOG.debug("edgeContours: "+edgeContours.size());
		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
		// Note that you are only interested in external contours.
		List<Contour> contours = BinaryImageOps.contour(edgeImage, 8, null);
		LOG.debug("contours: "+contours.size());
		SVGG g = new SVGG();
		for (Contour contour : contours) {
			int size = contour.external.size();
			SVGG gg = new SVGG();
			g.appendChild(gg);
			for (int i = 0; i < size; i++) {
				int j = (i+1) % size;
				Point2D_I32 pointi = contour.external.get(i);
				Real2 pi = new Real2(pointi.x, pointi.y);
				Point2D_I32 pointj = contour.external.get(j);
				Real2 pj = new Real2(pointj.x, pointj.y);
				SVGLine line = new SVGLine(pi, pj);
				line.setFill("red");
				line.setWidth(0.5);
				gg.appendChild(line);
			}
			System.out.println();
		}
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/countours.svg"));
 
		// display the results
		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, null);
		BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours,null,
				gray.width,gray.height,null);
		BufferedImage visualEdgeContour = VisualizeBinaryData.renderExternal(contours, null,
				gray.width, gray.height, null);
		ImageUtil.writeImageQuietly(visualCannyContour, "target/visualCannyContour.png");
 
		ShowImages.showWindow(visualBinary,"Binary Edges from Canny");
		ShowImages.showWindow(visualCannyContour,"Canny Trace Graph");
		ShowImages.showWindow(visualEdgeContour,"Contour from Canny Binary");
		Thread.sleep(100000);
	}
	// adjusts edge threshold for identifying pixels belonging to a line
	private static final float edgeThreshold = 25;
	// adjust the maximum number of found lines in the image
	private static final int maxLines = 10;
 
	/**
	 * Detects lines inside the image using different types of Hough detectors
	 *
	 * @param image Input image.
	 * @param imageType Type of image processed by line detector.
	 * @param derivType Type of image derivative.
	 */
	public static<T extends ImageSingleBand, D extends ImageSingleBand>
			void detectLines( BufferedImage image , 
							  Class<T> imageType ,
							  Class<D> derivType )
	{
		// convert the line into a single band image
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType );
 
		// Comment/uncomment to try a different type of line detector
		DetectLineHoughPolar<T,D> detector = FactoryDetectLineAlgs.houghPolar(3, 30, 2, Math.PI / 180,
				edgeThreshold, maxLines, imageType, derivType);
//		DetectLineHoughFoot<T,D> detector = FactoryDetectLineAlgs.houghFoot(3, 8, 5, edgeThreshold,
//				maxLines, imageType, derivType);
//		DetectLineHoughFootSubimage<T,D> detector = FactoryDetectLineAlgs.houghFootSub(3, 8, 5, edgeThreshold,
//				maxLines, 2, 2, imageType, derivType);
 
		List<LineParametric2D_F32> found = detector.detect(input);
 
		// display the results
		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
 
		ShowImages.showWindow(gui,"Found Lines");
	}
 
	/**
	 * Detects segments inside the image
	 *
	 * @param image Input image.
	 * @param imageType Type of image processed by line detector.
	 * @param derivType Type of image derivative.
	 */
	public static<T extends ImageSingleBand, D extends ImageSingleBand>
	void detectLineSegments( BufferedImage image ,
							 Class<T> imageType ,
							 Class<D> derivType )
	{
		// convert the line into a single band image
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType );
 
		// Comment/uncomment to try a different type of line detector
		DetectLineSegmentsGridRansac<T,D> detector = FactoryDetectLineAlgs.lineRansac(40, 30, 2.36, true, imageType, derivType);
 
		List<LineSegment2D_F32> found = detector.detect(input);
 
		// display the results
		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLineSegments(found);
		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
 
		ShowImages.showWindow(gui,"Found Line Segments");
	}
 
	public static void detectLines() {
		BufferedImage input = UtilImageIO.loadImage("../data/evaluation/simple_objects.jpg");
 
		detectLines(input,ImageUInt8.class,ImageSInt16.class);
 
		// line segment detection is still under development and only works for F32 images right now
		detectLineSegments(input, ImageFloat32.class, ImageFloat32.class);
	}

	public void testDetectLines() {
		detectLines();
	}
}
