package org.xmlcml.image.geom;

import java.awt.Point;
import java.util.ArrayList;

/**
 * class to Implement Convex Hull Algorithms in 2D-Space.
 */
public class ConvexHullExtra {

//public ArrayList<Line2>  ConvexHull_BruteForce(ArrayList<Point2> points)
//{
//    boolean convexLine = false;
//    Vector2 a = new Vector2();
//    Vector2 b = new Vector2();
//    
//    //arraylist of type 'Line' holding alllines of Convex hull
//    ArrayList<Line2> ConvexLines = new ArrayList<Line2>();
//
//    /* Brute Force : iterate over all combination in Set of Points and
//     * check if all points are to the right of Line ij (clock-wise picking).
//     */
//  for(int i=0; i<points.size() ; i++)
//     for(int j=0; j<points.size() ;j++)
//         if( i != j) {
//             convexLine = true;
//             a.Set(points.get(j), points.get(i));
//
//          for(int k=0; k<points.size() ;k++)
//           if( k!= i && k!= j)
//           {
//               b.Set(points.get(k), points.get(i));
//               
//              if(Vector2.ToRight(a,b))
//              {
//                 convexLine = false;
//                 break;
//              }
//           }
//
//          if(convexLine)
//           ConvexLines.add(new Line2(points.get(i),points.get(j)));
//         }
//
//  return ConvexLines;
//}
//public Stack<Point2>  ConvexHull_GrahamScan(ArrayList<Point2> points)
//{
//
//    if(points.size()<=0)
//        return new Stack<Point2>();
//
//   ArrayList<Point2> InputPoints = new ArrayList<Point2>(points);
//   Point2 p0 = InputPoints.get(0);
//   // stack to hold points in order they are added to convex hull descending.
//   Stack<Point2> sortedpoints = new Stack<Point2>();
//   
//   /* choose point P0 with smallest Y value in case of tie choose mostleft */
//   for(int i=0;i<points.size();i++)
//      if(p0.Y > points.get(i).Y || (p0.Y == points.get(i).Y && p0.X > points.get(i).X))
//          p0 = points.get(i);
//
//   
//   InputPoints.remove(p0);
//
//   /* Sort points according to Polar angle */
//   java.util.Collections.sort(InputPoints, new GrahamPointSorting(p0));
//
//   // add first three points.
//   sortedpoints.push(p0);
//   sortedpoints.push(InputPoints.get(0));
//   sortedpoints.push(InputPoints.get(1));
//   
//   for(int i=2;i<InputPoints.size();i++)
//   {
//       while(sortedpoints.size() > 2)
//       {
//         if(!Point2.LeftTurn(sortedpoints.get(sortedpoints.size()-2), sortedpoints.peek(), InputPoints.get(i)))
//             sortedpoints.pop();
//         else
//             break;
//       }
//
//       sortedpoints.push(InputPoints.get(i));
//   }
//
//
//   return sortedpoints;
//}
//public ArrayList<Point2> ConvexHull_JarvisMarch(ArrayList<Point2> points)
//{
//
//    int crnt,next=-1,root=-1;
//    Vector2 a = new Vector2();
//    Vector2 b = new Vector2();
//    //boolean[] vpoints = new boolean[points.size()];
//
//    //arraylist of type 'Line' holding alllines of Convex hull
//    ArrayList<Point2> ConvexPoints = new ArrayList<Point2>();
//
//    if(points.size()<= 0)
//        return ConvexPoints;
//
//    //choose point with lowest X-value.
//    crnt = 0;
//    for(int i=0;i<points.size();i++)
//        if(points.get(i).X<points.get(crnt).X || (points.get(i).X==points.get(crnt).X && points.get(i).Y<points.get(crnt).Y))
//            crnt = i;
//
//    //add first point in convex hull.
//    ConvexPoints.add(points.get(crnt));
//    root = crnt;
//
//    while(true)
//    {
//       next = -1;
//       for(int i=0;i<points.size();i++)
//       {
//         if(i == crnt)
//            continue;
//
//         if(next == -1)
//         {
//             next = i;
//             a.Set(points.get(next), points.get(crnt));
//             continue;
//         }
//         
//         
//         b.Set(points.get(i),points.get(crnt));
//
//         if(Vector2.ToRight(a, b))
//         {
//           next = i;
//           a.Set(points.get(next), points.get(crnt));
//         }
//       }
//
//       //break condition when no next point to go to.
//       if(next == -1 || next == root)
//          break;
//      
//       //vpoints[next] = true;
//       ConvexPoints.add(points.get(next));
//       crnt = next;
//    }
//
//
//    return ConvexPoints;
//}
//}
//
//
// class GrahamPointSorting implements java.util.Comparator<Point2> {
//
//
//  Point2 p0 ;
//  Vector2 a,b;
//
//  
//  public GrahamPointSorting(Point2 P0)
//  {
//      p0 = P0;
//      a = new Vector2();
//      b = new Vector2();
//  }
//  public int compare(Point2 p1, Point2 p2) {
//
//     a.Set(p1, p0);
//     b.Set(p2, p0);
//
//    if(a.Angle() > b.Angle())
//        return 1;
//    else
//        if(a.Angle() < b.Angle())
//            return -1;
//
//    if(a.norm() > b.norm())
//       return 1;
//    else
//       return -1;
//      
// }
	
	public ArrayList<Point> quickHull(ArrayList<Point> points) {
		ArrayList<Point> convexHull = new ArrayList<Point>();
		if (points.size() < 3) {
			return (ArrayList<Point>) points.clone();
		}
		// find extremals
		int minPoint = -1, maxPoint = -1;
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).x < minX) {
				minX = points.get(i).x;
				minPoint = i;
			}
			if (points.get(i).x > maxX) {
				maxX = points.get(i).x;
				maxPoint = i;
			}
		}
		Point a = points.get(minPoint);
		Point b = points.get(maxPoint);
		convexHull.add(a);
		convexHull.add(b);
		points.remove(a);
		points.remove(b);

		ArrayList<Point> leftSet = new ArrayList<Point>();
		ArrayList<Point> rightSet = new ArrayList<Point>();

		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (pointLocation(a, b, p) == -1) {
				leftSet.add(p);
			} else {
				rightSet.add(p);
			}
		}
		hullSet(a, b, rightSet, convexHull);
		hullSet(b, a, leftSet, convexHull);

		return convexHull;
	}

	public void hullSet(Point a, Point b, ArrayList<Point> set,
			ArrayList<Point> hull) {
		int insertPosition = hull.indexOf(b);
		if (set.size() == 0) {
			return;
		}
		if (set.size() == 1) {
			Point p = set.get(0);
			set.remove(p);
			hull.add(insertPosition, p);
			return;
		}
		int dist = Integer.MIN_VALUE;
		int furthestPoint = -1;
		for (int i = 0; i < set.size(); i++) {
			Point p = set.get(i);
			int distance = distance(a, b, p);
			if (distance > dist) {
				dist = distance;
				furthestPoint = i;
			}
		}
		Point p = set.get(furthestPoint);
		set.remove(furthestPoint);
		hull.add(insertPosition, p);

		// Determine who's to the left of AP
		ArrayList<Point> leftSetAP = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point m = set.get(i);
			if (pointLocation(a, p, m) == 1) {
				// set.remove(M);
				leftSetAP.add(m);
			}
		}

		// Determine who's to the left of PB
		ArrayList<Point> leftSetPB = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point m = set.get(i);
			if (pointLocation(p, b, m) == 1) {
				// set.remove(M);
				leftSetPB.add(m);
			}
		}
		hullSet(a, p, leftSetAP, hull);
		hullSet(p, b, leftSetPB, hull);
	}

	public int distance(Point a, Point b, Point c) {
		int abx = b.x - a.x;
		int aby = b.y - a.y;
		int num = abx * (a.y - c.y) - aby * (a.x - c.x);
		if (num < 0) {
			num = -num;
		}
		return num;
	}

	public int pointLocation(Point a, Point b, Point p) {
		int cp1 = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x);
		return (cp1 > 0) ? 1 : -1;
	}
		  
}