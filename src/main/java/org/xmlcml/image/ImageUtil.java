package org.xmlcml.image;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealMatrix;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.processing.OtsuBinarize;
import org.xmlcml.image.processing.ThinningService;

public class ImageUtil {
	private final static Logger LOG = Logger.getLogger(ImageUtil.class);

	public static BufferedImage thin(BufferedImage image) {
		ThinningService thinningService = new ThinningService(image);
		thinningService.doThinning();
		image = thinningService.getThinnedImage();
		return image;
	}

	public static BufferedImage binarize(BufferedImage image) {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.setImage(image);
		otsuBinarize.toGray();
		otsuBinarize.binarize();
		image = otsuBinarize.getBinarizedImage();
		return image;
	}

	public static BufferedImage toGray(BufferedImage image) {
		OtsuBinarize otsuBinarize = new OtsuBinarize();
		otsuBinarize.setImage(image);
		otsuBinarize.toGray();
		image = otsuBinarize.getGrayImage();
		return image;
	}
	
	/** extracts a subimage translated to 0,0.
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage clipSubImage(BufferedImage image, Int2Range boundingBox) {
		IntRange xRange = boundingBox.getXRange();
		IntRange yRange = boundingBox.getYRange();
		int xMin = xRange.getMin();
		int yMin = yRange.getMin();
		int w = xRange.getRange();
		int h = yRange.getRange();
		Rectangle rect = new Rectangle(xMin, yMin, w, h);
//		Raster r = image.getData(rect);
		BufferedImage subImage = new BufferedImage(w, h, image.getType());
//		subImage.setData(r);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = image.getRGB(i + xMin, j + yMin);
				subImage.setRGB(i, j,  rgb);
			}
		}
		return subImage;
	}

	public static double correlateGray(BufferedImage image,
			BufferedImage image2, String title) {
		double cor = 0.0;
		int xrange = Math.min(image.getWidth(), image2.getWidth());
		int yrange = Math.min(image.getHeight(), image2.getHeight());
		SVGG g = new SVGG();
		double total = 0;
		double sum = 0;
		Real2 centre = new Real2(0.0, 0.0);
		Real2 centre2 = new Real2(0.0, 0.0);
		double sumGray = 0.0;
		double sumGray2 = 0.0;
		for (int i = 0; i < xrange; i++) {
			for (int j = 0; j < yrange; j++) {
				int gray = getGray(image.getRGB(i,j));
				int gray2 = getGray(image2.getRGB(i,j));
				if (gray < 0 || gray2 < 0) {
					throw new RuntimeException("bad gray value "+Integer.toHexString(gray)+" "+Integer.toHexString(gray2));
				}
				int diff = Math.abs(gray - gray2);
				int max = Math.max(gray, gray2);
				total += max;
				int score = max - 2 * diff;
				sum += score;
				SVGRect rect = new SVGRect((double)i, (double)j, 1.0, 1.0);
				Color color = new Color(255-gray, 0, 255-gray2);
				String colorS = "#"+Integer.toHexString(color.getRGB()).substring(2);
				rect.setFill(colorS);
				rect.setStroke("none");
				g.appendChild(rect);
				centre.plusEquals(new Real2(i * gray, j * gray));
				sumGray += gray;
				centre2.plusEquals(new Real2(i * gray2, j * gray2));
				sumGray2 += gray2;
			}
		}
//		double scale = 1./(double)(xrange * yrange);
		centre.multiplyEquals(1./sumGray);
		centre2.multiplyEquals(1./sumGray2);
		if (title != null) {
			LOG.debug(centre.format(1)+" >> "+centre2.format(1)+" "+centre.subtract(centre2).format(1));
			File file = new File("target/corrGreen/"+title+".svg");
			file.getParentFile().mkdirs();
			SVGSVG.wrapAndWriteAsSVG(g, file);
		}
		cor = sum / total;
		return cor;
		
	}

	/** gets gray value.
	 * range 0-> ff
	 * @param rgb assumed to be grayscale (r==g==b)
	 * @return gray or -1 if not a gray color
	 */
	public static int getGray(int rgb) {
		int r = rgb & 0x00ff0000 / (256*256);
		int g = rgb & 0x0000ff00 / 256;
		int b = rgb & 0x000000ff;
		return (r == g && g == b) ? r : -1;
	}

	/** extracts matrix from grayImage.
	 * 
	 * @param image
	 * @return matrix (null if not a gray image)
	 */
	public static RealMatrix getGrayMatrix(BufferedImage image) {
		int cols = image.getWidth();
		int rows = image.getHeight();
		RealMatrix matrix = new RealMatrix(rows, cols, 0.0);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int gray = ImageUtil.getGray(image.getRGB(j, i));
				matrix.setElementAt(i,  j, (double) gray);
			}
		}
		return matrix;
	}

	/** creates image from RealMatrix..
	 * 
	 * @param matrix values must be 0<=val<=255
	 * @return images values are clipped to 0<v<255 without warning 
	 */
	public static BufferedImage putGrayMatrix(RealMatrix matrix) {
		int cols = matrix.getCols();
		int rows = matrix.getRows();
		LOG.debug("rc "+rows+" "+cols);
		BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				LOG.debug(i+" "+j);
				int gray = (int) matrix.elementAt(i, j);
				if (gray < 0) {
					gray = 0;
				} else if (gray > 255) {
					gray = 255;
				}
				int grayColor = 256*256*gray + 256 * gray + gray; 
				image.setRGB(j, i, grayColor);
			}
		}
		return image;
	}

	public static BufferedImage shiftImage(BufferedImage image, double deltax, double deltay) {
		RealMatrix matrix = ImageUtil.getGrayMatrix(image);
		RealMatrix shiftedMatrix = matrix.createMatrixWithOriginShifted(deltax, deltay);
		BufferedImage shiftedImage = ImageUtil.putGrayMatrix(shiftedMatrix);
		return shiftedImage;
	}
	

}
