package org.xmlcml.image.processing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public abstract class Thinning {

	protected BufferedImage image;
	protected int[][] binaryImage;
	protected boolean hasChange;
	
	public Thinning(BufferedImage image) {
    	createBinary(image);
	}
	
	public Thinning() {
		// TODO Auto-generated constructor stub
	}

	private void createBinary(BufferedImage image) {
		this.image = image;
	    binaryImage = copyImageToBinary(image);
	}
	public int[][] getBinaryImage() {
		return binaryImage;
	}
	public BufferedImage getThinnedImage() {
	    copyBinaryToImage(image, binaryImage);
		return image;
	}
	
	public static int[][] copyImageToBinary(BufferedImage image) {
		int[][] imageData = new int[image.getHeight()][image.getWidth()];
		for (int y = 0; y < imageData.length; y++) {
	       for (int x = 0; x < imageData[y].length; x++) {
	
	           if (image.getRGB(x, y) == Color.BLACK.getRGB()) {
	               imageData[y][x] = 1;
	           } else {
	               imageData[y][x] = 0;
	
	           }
	       }
	   }
		return imageData;
	}
	
	public static void copyBinaryToImage(BufferedImage image, int[][] imageData) {
		for (int y = 0; y < imageData.length; y++) {
	
	           for (int x = 0; x < imageData[y].length; x++) {
	
	               if (imageData[y][x] == 1) {
	                   image.setRGB(x, y, Color.BLACK.getRGB());
	
	               } else {
	                   image.setRGB(x, y, Color.WHITE.getRGB());
	               }
	
	
	           }
	       }
	}

	protected abstract int[][] doThinning();

}
