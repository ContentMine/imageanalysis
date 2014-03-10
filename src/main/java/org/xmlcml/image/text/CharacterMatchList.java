package org.xmlcml.image.text;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Util;

public class CharacterMatchList {

	private List<Double> scoreList;
	private List<Integer> codePointList;
	
	public CharacterMatchList() {
		scoreList = new ArrayList<Double>();
		codePointList = new ArrayList<Integer>();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < scoreList.size(); i++) {
			sb.append(codePointList.get(i)+": "+Util.format(scoreList.get(i), 2)+"; ");
		}
		return sb.toString();
	}

	public void add(GrayCharacter refGray, double corr) {
		scoreList.add(corr);
		codePointList.add(refGray.getCodePoint());
	}

	public int size() {
		return codePointList.size();
	}

	public Integer getCodePoint(int i) {
		return codePointList.get(i);
	}

	public Integer getBestCodePoint() {
		Integer best = null;
		double corr = -999;
		if (codePointList != null) {
			for (int i = 0; i < codePointList.size(); i++) {
				if (scoreList.get(i) > corr) {
					best = codePointList.get(i);
					corr = scoreList.get(i);
				}
			}
		}
		return best;
	}

	public String getAllCharacters() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < codePointList.size(); i++) {
			sb.append((char)(int)codePointList.get(i)+"/");
		}
		return sb.toString();
	}
}
