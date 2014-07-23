package org.xmlcml.image.pixel;


public class NucleusNode extends PixelNode {

	private PixelNucleus nucleus;
	
	public NucleusNode() {
		
	}
	
	public NucleusNode(PixelNucleus nucleus) {
		this.nucleus = nucleus;
		getCentrePixel();
	}
	
	public PixelNucleus getNucleus() {
		return nucleus;
	}
	
	@Override
	public Pixel getCentrePixel() {
		centrePixel = (nucleus == null) ? null : nucleus.getCentrePixel();
		return centrePixel;
	}
}
