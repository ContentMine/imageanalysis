package org.xmlcml.image.text;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.xmlcml.euclid.RealArray;

/** various simple machine learning or stats
 *

 * @author pm286
 *
 */
	 
public class KNN {  // Written by: Yancy Vance Paredes
	 
    public static void main(String[] args) throws FileNotFoundException {

    	/**
7 7 0
7 4 0
3 4 1
1 4 1	    	 */
    	KNN knn = new KNN();
    	knn.setK(3);
    	knn.readPoint(0, new RealArray(new double[]{7., 7.}));
    	knn.readPoint(0, new RealArray(new double[]{7., 4.}));
    	knn.readPoint(1, new RealArray(new double[]{3., 4.}));
    	knn.readPoint(1, new RealArray(new double[]{1., 4.}));
    	knn.readInstance(new RealArray(new double[]{9., 12.}));
    	knn.classify();
    	
    }

	private int K;
	private List<DataObject> dataList;
	private int nAttrib;
	private DataObject inst;

	private void setK(int k) {
		this.K = k;
	}

	    
	public void readPoint(double classification, RealArray features) {
	 	if (dataList == null) {
	        dataList = new ArrayList<DataObject>();
	        nAttrib = features.size();
	    }
	 	DataObject ob = new DataObject(nAttrib);
 
        for(int j = 0; j < nAttrib; j++) {
            ob.attrib[j] = features.get(j);
    	}
 
            // Read the classification of the object
        ob.c = classification;
        dataList.add(ob);
	}
	
	public void readInstance(RealArray array) {
        inst = new DataObject(nAttrib);
        for(int j = 0; j < nAttrib; j++) {
            inst.attrib[j] = array.get(j);
        }
	}

	public void classify() {
        // Compute for the Distance of all the Test Data
        for(int i = 0; i < dataList.size(); i++)
            for(int j = 0; j < nAttrib; j++)
                dataList.get(i).dist += Math.pow(dataList.get(i).attrib[j]-inst.attrib[j], 2);
 
        // Sort all the test data according to distance
        Collections.sort(dataList);
 
        //for(int i = 0; i < nTestData; i++)
        //  System.out.println(data.elementAt(i));
 
        // Rank all the K neighbors
        RealArray gModes = new RealArray();
        double val = dataList.get(0).dist;
        for(int i = 0, rank = 1; i < dataList.size() && rank < K; i++) {
            if(val < dataList.get(i).dist) rank++;
 
            gModes.addElement(dataList.get(i).c);
            //System.out.println(data[i] + " " + rank);
        }
 
        // Classify the new object
        // If the classification is qualitative, use the MODE
        inst.c = getMode(gModes);        // Find the mode of the neighbors
 
        // If the classification is quantitative, use the AVERAGE
        //inst.c = getAverage(gMode);       // Find the average of the neighbors
 
        System.out.println("The new object is classified as: ");
        System.out.println(inst);
 
        // add to the training data
        //data.add(inst);
    }
	 
    public static double getMode(RealArray doubles) {
        HashMap<Double, Integer> dict = new HashMap<Double, Integer>();
        for(int i = 0; i < doubles.size(); i++) {
            double d = doubles.get(i);
 
            if(dict.containsKey(d)) {
                dict.put(d, dict.get(d)+1);
            } else {
                dict.put(d, 1);
            }
        }
 
        //System.out.println(dict);
        Double maxMode = null;
        int maxCount = 0;
        Set<Double> keys = dict.keySet();
        for(Double d : keys) {
            //System.out.println("Key: " + d + " : " + dict.get(d));
            int tCount = dict.get(d);
            if(tCount > maxCount) {
                maxCount = tCount;
                maxMode = d;
                //System.out.println("\tSetting mode to " + d);
            }
        }
 
        return maxMode.doubleValue();
    }
	 
    public static double getAverage(RealArray doubles) {
        double sum = 0.0;
 
        for(int i = 0; i < doubles.size(); i++) {
            sum = sum + doubles.elementAt(i);
        }
 
        return sum / doubles.size();
    }
	 
}
	 
class DataObject implements Comparable<DataObject> {
	 
    public double attrib[];
    public double dist, c;
 
    public DataObject(int nAttrib) {
        this.attrib = new double[nAttrib];
        this.dist = 0;
        this.c = 0;
    }
 
    public int compareTo(DataObject ob) {
        return Double.compare(this.dist, ob.dist);
    }
 
    public String toString() {
        String out = new String();
 
        for(int i = 0; i < attrib.length; i++)
            out = out + attrib[i] + " ";
 
        return out + this.c;
    }
	 
}
