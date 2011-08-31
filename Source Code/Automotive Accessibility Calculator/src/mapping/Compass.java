/*
Copyright (c) 2011 Noblis, Inc.
Unless explicitly acquired and licensed from Licensor under another license, the contents of this 
file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent versions as 
allowed by the RPL, and You may not copy or use this file in either source code or executable
form, except in compliance with the terms and conditions of the RPL.

All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT WARRANTY 
OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, 
INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, 
QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language governing rights and 
limitations under the RPL. 
*/

package mapping;

import routing.RouteStep.Direction;

//Helper class to find the direction of a road segment based on it's direction
public class Compass {
	
	private static Point start;
	private static Point end;
	
	//Provide a static method by which other classes can access method to get the direction of a segment
	public static Direction getDirection(RoadSegment r, int dir){

		//Based on the direction, alters starting and ending points
		if(dir == 1){
			start = r.getFromPoint();
			end = r.getToPoint();
		}
		else{
			start = r.getToPoint();
			end = r.getFromPoint();
		}

		//Calculates the vector difference in the two points
		double dX = end.getX() - start.getX();
		double dY = end.getY() - start.getY();

		//Uses arc tangent to find the angle
		double findDeg = Math.atan(dY/dX);
		
		//Alter angle accordingly to each quadrant
		if(dX < 0){
			findDeg += Math.PI;
		}
		else if(findDeg < 0){
			findDeg += 2*Math.PI;
		}
		
		//Divide by pi/8 so as to create 8 sectors, and use a lookup table to return proper quadrant direction
		int quad = (int) (findDeg / (Math.PI / 8));
		Direction[] d = {Direction.E, Direction.NE, Direction.NE, Direction.N, Direction.N, 
				Direction.NW, Direction.NW, Direction.W, Direction.W, Direction.SW, 
				Direction.SW, Direction.S, Direction.S, Direction.SE, Direction.SE, Direction.E};
		return d[quad];
	}
	

}
