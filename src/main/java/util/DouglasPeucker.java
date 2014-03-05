package util;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Real2;


	/*
	*** Ramer Douglas Peucker

	The Ramer-Douglas�Peucker algorithm is an algorithm for reducing the number of points in a curve that is approximated by a series of points. 
	It does so by "thinking" of a line between the first and last point in a set of points that form the curve. 
	It checks which point in between is farthest away from this line. 
	If the point (and as follows, all other in-between points) is closer than a given distance 'epsilon', it removes all these in-between points. 
	If on the other hand this 'outlier point' is farther away from our imaginary line than epsilon, the curve is split in two parts. 
	The function is recursively called on both resulting curves, and the two reduced forms of the curve are put back together.

	1) From the first point up to and including the outlier
	2) The outlier and the remaining points.


	*** Bad implementations on the web
	On the web I found many Ramer Douglas Peucker implementations, but most of the top results on google contained bugs. 
	Even the original example on Wikipedia was BAD! 
	The bugs were ranging from bad calculation of the perpendicular distance of a point to a line (often they contained a devide by zero error for vertical lines), 
	to discarding points that should not be removed at all. 
	To see this in action, just try running the algorithm on it's own result with the same epsilon, 
	many implementations will keep on reducing more and more points until there is no spline left. 
	A correct implementation of RDP will remove *all* points that it can remove given a certain epsilon in the first run.

	I hope that by looking at this source code for my Ramer Douglas Peucker implementation you will be able to get a correct reduction of your dataset.

	@licence Feel free to use it as you please, a mention of my name is always nice.

	Marius Karthaus
	http://www.LowVoice.nl

	 * 
	 */

public class DouglasPeucker {
	
	List<Real2> properRDP(List<Real2> points, double epsilon) {
	    Real2 firstPoint=points.get(0);
	    Real2 lastPoint=points.get(points.size()-1);
	    if (points.size()<3){
	        List<Real2> r2list = new ArrayList<Real2>();
	        for (Real2 r : points) {
	        	r2list.add(r);
	        }
	        return r2list;
	    }
	    int index=-1;
	    double dist=0;
	    for (int i=1;i<points.size()-1;i++){
	        double cDist=findPerpendicularDistance(points.get(i),firstPoint,lastPoint);
	        if (cDist>dist){
	            dist=cDist;
	            index=i;
	        }
	    }
	    if (dist > epsilon) {
	        // iterate
	        List<Real2> l1=slice(points, 0, index+1);
	        List<Real2> l2=slice(points, index);
	        List<Real2> r1=properRDP(l1,epsilon);
	        List<Real2> r2=properRDP(l2,epsilon);
	        // concat r2 to r1 minus the end/startpoint that will be the same
	        List<Real2> rs=concat(slice(r1, 0,r1.size()-1), r2);
	        return rs;
	    } else {
//	        return new Real2Array(firstPoint,lastPoint);
	    	List<Real2> r2a = new ArrayList<Real2>();
	    	r2a.add(firstPoint);
	    	r2a.add(lastPoint);
	    	return r2a;
	    }
	}
	    
	private List<Real2> concat(List<Real2> r1, List<Real2> r2) {
		List<Real2> newList = new ArrayList<Real2>();
		for (Real2 r : r1) {
			newList.add(r);
		}
		for (Real2 r : r2) {
			newList.add(r);
		}
		return newList;
	}

	private List<Real2> slice(List<Real2> points, int i, int j) {
		List<Real2> newList = new ArrayList<Real2>();
		for(int ii = i; ii <= j; ii++) {
			newList.add(points.get(ii));
		}
		return newList;
	}

	private List<Real2> slice(List<Real2> points, int i) {
		return slice(points, i, points.size()-1);
	}


	double findPerpendicularDistance(Real2 p, Real2 p1, Real2 p2) {
	    // if start and end point are on the same x the distance is the difference in X.
	    double result;
	    double slope;
	    double intercept;
	    if (p1.getX()==p2.getX()){
	        result=Math.abs(p.getX()-p1.getX());
	    }else{
	        slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
	        intercept = p1.getY() - (slope * p1.getX());
	        result = Math.abs(slope * p.getX() - p.getY() + intercept) / Math.sqrt(Math.pow(slope, 2) + 1);
	    }
	   
	    return result;
	}

	public static void main(String[] args) {
		double epsilon = 0.09;
		DouglasPeucker dp = new DouglasPeucker();
		List<Real2> points = new ArrayList<Real2>();
		points.add(new Real2(0.1, 0.11));
		points.add(new Real2(0.5, 0.55));
		points.add(new Real2(0.95, 0.9));
		points.add(new Real2(1.3, 1.4));
		List<Real2> points1 = dp.properRDP(points, epsilon);
		for (int i = 0; i < points1.size(); i++) {
//			System.out.println(points1.get(i));
		}
	}
}
