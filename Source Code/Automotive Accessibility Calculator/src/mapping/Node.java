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


import java.util.HashSet;
import java.lang.Comparable;


//Helper class to store Node data within a graph
public class Node implements Comparable<Node>{
	
	//Establish public and private data fields
	public double dist = Double.POSITIVE_INFINITY;
	private Edge incoming;
	private HashSet<Edge> outgoing;
	private Point latLong;
	private int identity;
	static int id = 1;
	
	//Constructor to init fields
	public Node(HashSet<Edge> edges, Edge inc, Point loc, double dis){
		outgoing = edges;
		incoming = inc;
		latLong = loc;
		dist = dis;
		identity = Node.getNewID();
	}
	

	/*                                       Getter and setter methods to access data                                  */
	public Point getLocation(){
		return latLong;
	}
	

	public double getDistance(){
		return dist;
	}
	
	public void setDistance(double d){
		dist = d;
	}
	
	public void setIncoming(Edge i){
		incoming = i;
	}
	
	public Edge getIncoming(){
		return incoming;
	}
	
	public void setPoint(Point p){
		latLong = p;
	}
	
	
	public int getId(){
		return identity;
	}
	
	public void addEdge(Edge e){
		outgoing.add(e);
	}
	
	static int getNewID() {
		return id++;
	}
	
	public HashSet<Edge> getEdges(){
		return outgoing;
	}
	
	public void reset(){
		incoming = null;
		dist = Double.POSITIVE_INFINITY;
	}

	
	//Implement comparable interface so it can be used in a priority queue
	public int compareTo(Node n) {
		if(dist > n.getDistance())
			return 1;
		else if(dist <  n.getDistance())
			return -1;
		else if(id < n.getId())
			return 1;
		else if(id > n.getId())
			return -1;
		else 
			return 0;
	}
	


}
