package org.xmlcml.image.processing;

import java.awt.Color;
import java.awt.image.BufferedImage;

public abstract class Thinning {

	protected BufferedImage image;
	protected int[][] binary;
	
	public Thinning(BufferedImage image) {
    	createBinary(image);
	}
	
	public Thinning() {
	}

	public void createBinary(BufferedImage image) {
		this.image = image;
	    binary = copyImageToBinary(image);
	}
	
	public void setBinary(int[][] b) {
		if (b != null && b[0] != null) {
			int ly = b.length;
			int lx = b[0].length;
			this.binary = new int[ly][lx];
			for (int y = 0; y < ly; y++) {
		       for (int x = 0; x < lx; x++) {
		    	   binary[y][x] = b[y][x];
		       }
			}
		}
	}
	
	public int[][] getBinary() {
		return binary;
	}
	public BufferedImage getThinnedImage() {
	    copyBinaryToImage(image, binary);
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

	/** creates a thinned array 
	 * 
	 */
	public abstract void doThinning();

	protected int getNeighbourSum(int y, int x) {
	
	    return binary[y - 1][x] + binary[y - 1][x + 1] + binary[y][x + 1]
	            + binary[y + 1][x + 1] + binary[y + 1][x] + binary[y + 1][x - 1]
	            + binary[y][x - 1] + binary[y - 1][x - 1];
	}

	/**
	 * traverses neighbours in a cycle. Adds 1 for any 0 pixel whose next neighbour is 1.
	 * minimum sum is 0 (00000000 or 11111111), maximum is 4 (01010101) 
	 * otherwise 1,2,3 also possible
	 * 
	 * 
	 * @param y
	 * @param x
	 * @return
	 */
	protected int getSumCyclicChanges(int y, int x) {
	
	    int count = 0;
	    //p2 p3
	    if (binary[y - 1][x] == 0 && binary[y - 1][x + 1] == 1) {
	        count++;
	    }
	    //p3 p4
	    if (binary[y - 1][x + 1] == 0 && binary[y][x + 1] == 1) {
	        count++;
	    }
	    //p4 p5
	    if (binary[y][x + 1] == 0 && binary[y + 1][x + 1] == 1) {
	        count++;
	    }
	    //p5 p6
	    if (binary[y + 1][x + 1] == 0 && binary[y + 1][x] == 1) {
	        count++;
	    }
	    //p6 p7
	    if (binary[y + 1][x] == 0 && binary[y + 1][x - 1] == 1) {
	        count++;
	    }
	    //p7 p8
	    if (binary[y + 1][x - 1] == 0 && binary[y][x - 1] == 1) {
	        count++;
	    }
	    //p8 p9
	    if (binary[y][x - 1] == 0 && binary[y - 1][x - 1] == 1) {
	        count++;
	    }
	    //p9 p2
	    if (binary[y - 1][x - 1] == 0 && binary[y - 1][x] == 1) {
	        count++;
	    }
	
	    return count;
	}


}
