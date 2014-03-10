package org.xmlcml.image.text;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.EuclidRuntimeException;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealMatrix;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.image.ImageUtil;

public class GrayCharacter {

	private final static Logger LOG = Logger.getLogger(GrayCharacter.class);

	public enum RowCol {
		R,
		C,
	}
	
	private BufferedImage grayImage;
	private double total;
	private double sum;
	private Integer codePoint; // unicode point

	private GrayCharacter() {
		
	}

	private void setImage(BufferedImage image) {
		if (image == null) {
			throw new RuntimeException("null image: check files exists and is image");
		}
		this.grayImage = ImageUtil.binarizeToGray(image);
	}
	
	/** deep copy constructor.
	 * creates new copied image
	 * @param gray2
	 */
	public GrayCharacter(GrayCharacter gray2) {
		BufferedImage image2 = gray2.grayImage;
		int height = image2.getHeight();
		int width = image2.getWidth();
		grayImage = new BufferedImage(width, height, image2.getType());
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				grayImage.setRGB(i, j, image2.getRGB(i, j));
			}
		}
		this.codePoint = gray2.codePoint;
	}

	public GrayCharacter(File file) throws IOException {
		BufferedImage image = ImageIO.read(file);
		if (image == null) {
			throw new RuntimeException("null image for: "+file);
		}
		setImage(image);
		codePoint = getCodePointFromFilename(file);
	}

	private static Integer getCodePointFromFilename(File file) {
		String base = FilenameUtils.getBaseName(file.getPath());
		Integer codePoint = null;
		try {
			codePoint = new Integer(base);
		} catch (Exception e) {
			throw new RuntimeException("cannot create as codePoint: "+file);
		}
		return codePoint;
	}

	/** correlate characters
	 * 
	 * copies character2 and optionally shifts centre to overlay this
	 * 
	 * @param character2
	 * @param title if not null draw overlap // FIXME
	 * @param overlay if true shift v=centre of copy
	 * @return
	 */
	public double correlateGray(GrayCharacter character2, String title, boolean overlay, boolean scale) {
		double cor = 0.0;
		GrayCharacter newCharacter2 = new GrayCharacter(character2);
		if (scale) {
			newCharacter2.grayImage = ImageUtil.scaleImage(this.getWidth(), this.getHeight(), newCharacter2.grayImage);
		}
		Real2 centre = this.getCentre();
		LOG.trace("centre "+centre);
		Real2 centre2 = newCharacter2.getCentre();
		Real2 offset = centre2.subtract(centre);
		LOG.trace("off "+offset);
		if (overlay) {
			newCharacter2.shiftOrigin(offset);
		}
		calculateCorrelation(title, newCharacter2);
		LOG.trace(sum+"/"+total);
		cor = sum / total;
		LOG.trace(cor);
		return cor;
		
	}

	public void shiftOrigin(Real2 offset) {
		this.grayImage = ImageUtil.shiftImage(grayImage, offset.getX(), offset.getY());
	}

	public void scaleAndInterpolate(int newRows, int newCols) {
		this.grayImage = ImageUtil.scaleAndInterpolate(grayImage, newRows, newCols);
	}

	private void calculateCorrelation(String title, GrayCharacter newCharacter2) {
		int xrange = Math.min(this.getWidth(), newCharacter2.getWidth());
		int yrange = Math.min(this.getHeight(), newCharacter2.getHeight());
		SVGG g = new SVGG();
		total = 0;
		sum = 0;
		for (int x = 0; x < xrange; x++) {
			for (int y = 0; y < yrange; y++) {
				int gray = ImageUtil.getGray(this.grayImage.getRGB(x,y));
				int gray2 = ImageUtil.getGray(newCharacter2.grayImage.getRGB(x,y));
				addGrayComponentsToTotals(newCharacter2, gray, gray2);
				if (title != null) {
					drawPixel(g, x, y, gray, gray2);
				}
			}
		}
		if (title != null) {
			File corrPixels = new File("target/corrPixels/");
			corrPixels.mkdirs();
			SVGSVG.wrapAndWriteAsSVG(g, new File(corrPixels, title+".svg"));
		}
	}
	
	private void addGrayComponentsToTotals(GrayCharacter character2, int gray, int gray2) {
		int diff = Math.abs(gray2 - gray);
		int max = Math.max(gray, gray2);
		total += max;
		int score = max - 2 * diff;
		sum += score;
	}

	private void drawPixel(SVGG g, int x, int y, int gray, int gray2) {
		SVGRect rect = new SVGRect((double)x, (double)y, 1.0, 1.0);
		Color color = new Color(255-gray, 0, 255-gray2);
		String colorS = "#"+Integer.toHexString(color.getRGB()).substring(2);
		rect.setFill(colorS);
		rect.setStroke("none");
		g.appendChild(rect);
	}

	/** get weighted centre of image.
	 * 
	 * center weighted by gray intensity of each pixel
	 * 
	 * @return weighted centre
	 */
	public Real2 getCentre() {
		Real2 centre = new Real2(0.0, 0.0);
		double sumGray = 0.0;
		for (int x = 0; x < grayImage.getWidth(); x++) {
			for (int y = 0; y < grayImage.getHeight(); y++) {
				int gray = ImageUtil.getGray(grayImage.getRGB(x,y));
				if (gray < 0) {
					throw new RuntimeException("bad gray value "+Integer.toHexString(gray));
				}
				LOG.trace("g "+gray);
				centre.plusEquals(new Real2(x * gray, y * gray));
				sumGray += gray;
			}
		}
		centre.multiplyEquals(1./sumGray);
		return centre;
	}

	private int getHeight() {
		return grayImage.getHeight();
	}

	private int getWidth() {
		return grayImage.getWidth();
	}

	/** maybe should use java filters? LookupTable? */
	
	public BufferedImage trimEdgesWhite(int maxVal) {
		//GrayCharacter.debugGray(grayImage);
		IntMatrix matrix = ImageUtil.getGrayMatrix(grayImage);
		LOG.trace("gray>> "+matrix);
		IntMatrix subMatrix = trimEdges(maxVal, matrix);
		grayImage = ImageUtil.putGrayMatrix(subMatrix);
		return grayImage;
	}

	private IntMatrix trimEdges(int maxVal, IntMatrix matrix) {
		IntMatrix intMatrix = matrix.elementsInRange(new IntRange(0, maxVal));
		LOG.trace("delete "+intMatrix);
		LOG.trace(intMatrix.getRows()+" "+intMatrix.getCols());
		int lowCol = getExtreme(RowCol.C, 0, intMatrix, 1) + 1;
		int hiCol = getExtreme(RowCol.C, intMatrix.getCols() - 1, intMatrix, -1) - 1;
		int lowRow = getExtreme(RowCol.R, 0, intMatrix, 1) + 1;
		int hiRow = getExtreme(RowCol.R, intMatrix.getRows() - 1, intMatrix, -1) - 1;
		LOG.trace("delete edges up to: "+lowRow+" "+hiRow+" "+lowCol+" "+hiCol);
		IntMatrix subMatrix = matrix.extractSubMatrixData(lowRow,  hiRow,  lowCol,  hiCol);
		return subMatrix;
	}

	private int getExtreme(RowCol rc, int istart, IntMatrix intMatrix, int delta) {
		int limit = (RowCol.C.equals(rc)) ? intMatrix.getRows() : intMatrix.getCols();
		int irow = istart;
		for (; irow != limit; irow += delta) {
			try {
				IntArray arr = (RowCol.C.equals(rc)) ? intMatrix.extractColumnData(irow) : intMatrix.extractRowData(irow);
				if (arr.getMax() > 0) {
					irow -= delta;
					break;
				}
			} catch (EuclidRuntimeException e) {
				return -1;
			} catch (ArrayIndexOutOfBoundsException e) {
				return -1;
			}
		}
		return irow;
	}

	private void setToWhite(int maxVal, RealMatrix matrix) {
		int width = grayImage.getWidth();
		int height = grayImage.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (matrix.elementAt(i, j) > maxVal) {
					matrix.setElementAt(i, j, 255);
				}
			}
		}
	}

	public static void debugGray(BufferedImage image) {
		for (int j = 0; j < image.getHeight(); j++) {
			System.out.print(j+">> ");
			for (int i = 0; i < image.getWidth(); i++) {
				int col = image.getRGB(i, j);
				int gray = ImageUtil.getGray(col);
				System.out.print(" "+/*Integer.toHexString(col)+"("+*/gray/*+")"*/);
			}
			System.out.println();
		}
	}

	public BufferedImage getGrayImage() {
		return grayImage;
	}

	public static GrayCharacter readGrayImage(BufferedImage image) {
		GrayCharacter grayCharacter = new GrayCharacter();
		grayCharacter.setGrayImage(image);
		return grayCharacter;
	}

	private void setGrayImage(BufferedImage image) {
		this.grayImage = image;
	}

	public Integer getCodePoint() {
		return codePoint;
	}

	public void setCodePoint(Integer codePoint) {
		this.codePoint = codePoint;
	}

	public static GrayCharacter clipCharacterFromImage(File testFile, Int2Range box) throws IOException {
		BufferedImage testImage = ImageIO.read(testFile);
		BufferedImage subImage = ImageUtil.clipSubImage(testImage, box);
		GrayCharacter grayImage = GrayCharacter.readGrayImage(subImage);
		return grayImage;
	}
	
	

}
