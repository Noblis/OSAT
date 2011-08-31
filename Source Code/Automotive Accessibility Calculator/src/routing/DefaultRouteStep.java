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


package routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mapping.Compass;
import mapping.Point;
import mapping.RoadSegment;


/**
 * A step along the path, consisting of several segments with the same name.
 */
public class DefaultRouteStep implements RouteStep {
	
	//API does not seem to show specification for data input so lists were created to track data
	private ArrayList<RoadSegment> segments = new ArrayList<RoadSegment>();
	private ArrayList<Integer> directions = new ArrayList<Integer>();

	//Adds a road segment to the current step
	public void addRoadSegment(RoadSegment r, int way){
		segments.add(r);
		directions.add(way);
	}

	//Return street name of entire route
	public String streetName(){
		return segments.get(0).fullName();
	}

	//Return starting point of route
	public Point startPoint(){
		if(directions.get(0) == 1)
			return segments.get(0).getFromPoint();  
		return segments.get(0).getToPoint();
	}

	//Return ending point of route
	public Point endPoint(){
		if(directions.get(0) == 1)
			return segments.get(segments.size() - 1).getToPoint();
		return segments.get(segments.size() - 1).getFromPoint();
	}

	//Return segments which comprise current step
	public List<RoadSegment> segments(){
		return segments;
	}

	//Returns the initial direction of the first road segment
	public Direction initialDirection(){
		Direction d = Compass.getDirection(segments.get(0), directions.get(0));
		return d;
	}

	//Returns the final direction of the last road segment
	public Direction finalDirection(){
		Direction d = Compass.getDirection(segments.get(segments.size() - 1), directions.get(segments.size() - 1));
		return d;
	}

	//Iterates through segments and sums the distance
	public double distance(){
		double d = 0.0;
		Iterator<RoadSegment> iter = segments.iterator();
		while(iter.hasNext()){
			RoadSegment r = iter.next();
			d += RoutePlanner.distanceInMiles(r.getFromPoint(), r.getToPoint());
		}
		return d;
	}

	//Iterates through segments and sums the time for each segment
	public double estimatedTime(){
		double time = 0.0;
		Iterator<RoadSegment> iter = segments.iterator();
		while(iter.hasNext()){
			RoadSegment r = iter.next();
			time += RoutePlanner.distanceInMiles(r.getFromPoint(), r.getToPoint()) / RoutePlanner.getSpeedLimit(r) * 60;
		}
		return time;
	}

}
