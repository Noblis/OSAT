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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import mapping.Edge;
import mapping.Node;
import mapping.Point;
import mapping.RoadSegment;



public class RoutePlanner  {

	//Establish private data fields
	private Database road_database;

	
	private Dijkstra dijkstra_calculator;

	//Constructor which initializes important private fields
	public RoutePlanner(Database db) throws FileNotFoundException{
		road_database = db;
		
		dijkstra_calculator = new Dijkstra();
				
		Iterator<RoadSegment> itr = road_database.iterator();
				
		while(itr.hasNext()){

			RoadSegment curr = itr.next();

			Node src = new Node(new HashSet<Edge>(), null, curr.getFromPoint(), Double.POSITIVE_INFINITY);
			Node checkSrc = dijkstra_calculator.getNode(curr.getFromPoint());
			Node snk = new Node(new HashSet<Edge>(), null, curr.getToPoint(), Double.POSITIVE_INFINITY);
			Node checkSnk = dijkstra_calculator.getNode(curr.getToPoint());
			
			Node sink = null;
			Node source = null;

			double distance = distanceInMiles(curr.getFromPoint(), curr.getToPoint());
			double weight = distance/getSpeedLimit(curr);

			//If the distance is the same as the weight, then the current segment is not a road
			if(distance != weight){

				if(checkSnk != null)
					sink = checkSnk;
				else
					sink = snk;
				if(checkSrc != null)
					source = checkSrc;
				else
					source = src;

				dijkstra_calculator.addNode(sink);
				dijkstra_calculator.addNode(source);
				
				//If you want to account for one way streets, start here and adjust as necessary
				try{
						dijkstra_calculator.addEdge(source, sink, distance, weight, curr, 1);
						dijkstra_calculator.addEdge(sink, source, distance, weight, curr, -1);
					}
				catch(Exception e){}
			}
		}
	}

	//Times are abridged to account for traffic
	public static int getSpeedLimit(RoadSegment r){
		             //65  55  45  35  25  20
		int speed[] = {30, 27, 22, 18, 13, 11, 1};
		return speed[(Integer.parseInt(r.getCfcc().substring(1))/10) - 1];	   
	}

	//Finds the distance in miles by using the spherical law of cosines
	public static double distanceInMiles(Point a, Point b){
		double r = 6371;
		double dist = Math.acos(Math.sin(a.getLatitude()* Math.PI /180.)*Math.sin(b.getLatitude()* Math.PI /180.) + 
		                  Math.cos(a.getLatitude() * Math.PI /180.)*Math.cos(b.getLatitude()* Math.PI /180.) *
		                  Math.cos(b.getLongitude()* Math.PI /180.-a.getLongitude()* Math.PI /180.)) * r;
		return dist * 0.621371192 ;

	}
	
	//Reset dijkstra object to remove shortest path tree
	public void reset(){
		dijkstra_calculator.reset();
	}

	
	//Find the shortest path between two points
	public List<RouteStep> getRoute(Point from, Point to) throws RouteFailureException {

		Dijkstra d = new Dijkstra();
		d.setNodePool(dijkstra_calculator.getNodePool());
		
		
		//Find coordinates within graph
		Node src = d.getNode(from);
		
		Node snk = d.getNode(to);
		

		Stack<Edge> fEdges ;
		fEdges = d.getShortestPath(src, snk);

		return createRoute(fEdges);
		
	}

	private ArrayList<RouteStep> createRoute(Stack<Edge> edges){
		
		if(edges.size() == 0){
			return new ArrayList<RouteStep>();
		}
		
		ArrayList<RouteStep> route = new ArrayList<RouteStep>();

		//Reset map highlighting so that the route can be shown alone

		DefaultRouteStep curr = new DefaultRouteStep();

		//Pop off the first edge and add it to current RouteStep
		Edge e = edges.pop();
		curr.addRoadSegment(e.getRoadSegment(), e.getWay());

		//Continues to pop off edges until none are left
		while(!edges.isEmpty()){
			e = edges.pop();
			
			//Parses edge data and checks whether it is still the same road
			String currentName = curr.streetName().trim();
			String newName = e.getRoadSegment().fullName().trim();
			if(currentName.equalsIgnoreCase(newName))
				curr.addRoadSegment(e.getRoadSegment(), e.getWay());
			else{

				//If it is not the same road, adds the current routestep, and creates a new one
				route.add(curr);
				curr = new DefaultRouteStep();
				curr.addRoadSegment(e.getRoadSegment(), e.getWay());
			}	
		}
		return route;
	}
}
