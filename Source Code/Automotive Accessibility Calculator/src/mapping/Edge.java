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



//Helper class to keep track of Edge data within a graph
public class Edge {
	
	private Node source;
	private Node sink;
	private double length;
	private double weight;
	private RoadSegment road;
	private int way;
	
	//Constructor to init private fields
	public Edge(Node src, Node snk, double lng, double wgt, RoadSegment r, int w){
		source = src;
		sink = snk;
		length = lng;
		weight = wgt;
		road = r;
		way = w;
	}
	
	/*                                                   Getter and Setter Methods                                               */
	public Node getSource(){
		return source;
	}
	
	public Node getSink(){
		return sink;
	}
	
	public double getLength(){
		return length;
	}
	
	public double getWeight(){
		return weight;
	}
	
	public RoadSegment getRoadSegment(){
		return road;
	}
	
	public int getWay(){
		return way;
	}


}
