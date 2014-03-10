package org.xmlcml.image.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGImage;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.image.GrayCharacterTest;
import org.xmlcml.image.ImageUtil;
import org.xmlcml.image.processing.PixelIsland;
import org.xmlcml.image.processing.PixelIslandList;
import org.xmlcml.image.processing.PixelIslandList.Operation;
import org.xmlcml.image.text.fonts.ReferenceFont;
import org.xmlcml.image.text.fonts.ReferenceFontManager;

public class GrayCharacterMatcher {

	private final static Logger LOG = Logger.getLogger(GrayCharacterMatcher.class);

	private boolean binarize;
	private boolean scale;
	private boolean overlay;
	private int maxPixels;
	private File testImageFile;
	private ReferenceFont referenceFont;
	private double minCorrelation;
	private ReferenceFontManager fontManager = new ReferenceFontManager();
	private BufferedImage testImage;
	private CharacterMatchList matchList;
	private boolean plotSingleChar;
	private boolean createSVG;
	private double currentCorrelation;

	private SVGG g;

	public GrayCharacterMatcher() {
		setDefaults();
	}
	
	private void setDefaults() {
		setMinimumCorrelation(0.20);
		setReferenceFont(fontManager.getFont(ReferenceFontManager.HELVETICA));
		setMaximumPixels(1000);
		setOverlay(true);
		setScale(true);
		setBinarize(true);
		setCreateSVG(false);
		setPlotSingleChar(true);

	}
	public void setCreateSVG(boolean b) {
		this.createSVG = b;
	}

	public void setPlotSingleChar(boolean b) {
		this.plotSingleChar = b;
	}

	public void setMinimumCorrelation(double d) {
		this.minCorrelation = d;
	}
	public void setReferenceFont(ReferenceFont referenceFont) {
		this.referenceFont = referenceFont;		
	}
	
	public void setTestImage(File imageFile) throws IOException {
		if (imageFile == null) {
			throw new IOException("null testFile");
		}
		if (imageFile.isDirectory() || !imageFile.exists()) {
			throw new IOException("Cannot find image file "+imageFile);
		}
		this.testImageFile = imageFile;
		this.testImage = ImageIO.read(testImageFile);
	}
	/** set the maximum size of object being examined.
	 * 
	 * @param maxPixels pixel area (i.i. width * height)
	 */
	public void setMaximumPixels(int maxPixels) {
		this.maxPixels = maxPixels;
	}
	/** attempt to fit images by centre of gravity.
	 * 
	 * @param b
	 */
	public void setOverlay(boolean b) {
		this.overlay = b;
	}
	/** attempt to scale images to same geometric size.
	 * 
	 * In principle this can address aspect ratio, etc.
	 * 
	 * @param b
	 */
	public void setScale(boolean b) {
		this.scale = b;
	}
	/** binarize the testImage.
	 * 
	 * @param b
	 */
	public void setBinarize(boolean b) {
		this.binarize = b;
	}
	
	/** match the images.
	 * 
	 * This does a pixel by pixel match giving a heuristic coefficient.
	 * 
	 */
	public void match() {
		BufferedImage testImage0 = (binarize) ? ImageUtil.binarize(testImage) : testImage;
		PixelIslandList pixelIslandList = PixelIslandList.createPixelIslandList(testImage, Operation.BINARIZE);
		g = new SVGG();
		for (PixelIsland island : pixelIslandList) {
			BufferedImage image = island.createImage(testImage.getType());
			if (image == null ||
				image.getWidth() * image.getHeight() > maxPixels) continue;
			GrayCharacter grayCharacter = GrayCharacter.readGrayImage(image);
			this.matchList = getBestCharacters(grayCharacter, minCorrelation);
			if (createSVG) {
				addSVGBox(island, g, image);
			}
		}
	}
	
	public CharacterMatchList getBestCharacters(GrayCharacter gray, double minCorrelation) {
		CharacterMatchList matchList = new CharacterMatchList();
		Map<Integer, PixelIslandList> pixelIslandListByCodePointMap = referenceFont.getPixelIslandListByCodePointMap();
		this.currentCorrelation = -999;
		for (Integer codePoint : pixelIslandListByCodePointMap.keySet()) {
			LOG.trace("codePoint: "+codePoint);
			GrayCharacter refGray = referenceFont.getGrayCharacter(codePoint);
			boolean scale = true;
			double corr = refGray.correlateGray(gray, null, overlay, scale);
			LOG.trace("corr: "+corr);
			if (corr > minCorrelation) {
				matchList.add(refGray, corr);
			}
			if (corr > currentCorrelation) {
				currentCorrelation = corr;
			}
		}
		LOG.trace(pixelIslandListByCodePointMap.size()+" m "+matchList.size());
		return matchList;
	}


	
	private void addSVGBox(PixelIsland island, SVGG g, BufferedImage image) {
		Real2Range bbox = island.getBoundingBox();
		SVGRect rect = new SVGRect(bbox);
		rect.setStrokeWidth(0.5);
		rect.setStroke("red");
		g.appendChild(rect);
		annotateBox(g, rect);
		SVGImage svgImage = new SVGImage();
		svgImage.readImageDataIntoSrcValue(image, "image/png");
		svgImage.setTransform(new Transform2(new Vector2(rect.getXY())));
		g.appendChild(svgImage);
	}

	private void annotateBox(SVGG g, SVGRect rect) {
		Integer codePoint = matchList.getBestCodePoint();
		if (codePoint != null) {
			String toPlot = (plotSingleChar) ? String.valueOf((char)(int)codePoint) :
				this.matchList.getAllCharacters();
			SVGText text = new SVGText(rect.getXY(), toPlot);
			text.setFontSize(10.0);
			text.setFill("black");
			g.appendChild(text);
		}
	}

	public double getCorrelation() {
		return currentCorrelation;
	}

	public SVGG getSVGG() {
		return g;
	}
	
}
