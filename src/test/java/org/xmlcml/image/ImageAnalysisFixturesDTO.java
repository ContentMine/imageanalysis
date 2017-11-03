package org.xmlcml.image;

import java.io.File;

public class ImageAnalysisFixturesDTO {
	
    public int[][] expectedRingSizes;
    public int[] nodes;
    public int[] edges;
    public int[] outlines;
    public String dir;
    public String inname;
    public File outdir;
    public File indir ;
    public File imageFile;
    public int islandCount;
	public int mainIslandCount;
	public int[] pixelRingListCount;

}
