package org.xmlcml.image.lines;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.xmlcml.image.AMIContour;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

public class AMICanny {

	public AMICanny() {
		
	}

	public List<Contour> extractContours(String filename) {
		
		BufferedImage image = UtilImageIO.loadImage(filename);
		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
		// It has also been configured to save the trace as a graph.  This is the graph created while performing
		// hysteresis thresholding.
		CannyEdge<ImageUInt8,ImageSInt16> canny = FactoryEdgeDetectors.canny(2,true, true, ImageUInt8.class, ImageSInt16.class);
		// The edge image is actually an optional parameter.  If you don't need it just pass in null
//		canny.process(gray,0.8f,0.9f,null);
//		canny.process(gray,0.1f,0.3f,null);
		canny.process(gray,0.1f,0.3f,edgeImage);	
		// First get the contour created by canny
		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);
		return contours;
	}
	

	public List<AMIContour> extractAMIContours(String filename) {
		List<Contour> contours = extractContours(filename);
		List<AMIContour> amiContours = new ArrayList<AMIContour>();
		for (Contour contour : contours) {
			AMIContour amiContour = new AMIContour(contour);
			amiContours.add(amiContour);
		}
		return amiContours;
	}

}
