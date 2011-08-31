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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Stack;

import mapping.Edge;
import mapping.Node;
import mapping.Point;
import mapping.RoadSegment;




/**
 * A directed graph representation for doing shortest path computations. There
 * is a set of labeled nodes and a set of labeled directed edges. Each edge has
 * two nodes as endpoints, a <i>source</i> and a <i>sink</i>, and each node may
 * be the source of zero or more edges and the sink of zero or more edges.
 */
class Dijkstra {

	private HashMap<Point, Node> nodePool = new HashMap<Point, Node>();
	
	//Following two variables used to speed up execution
	private HashMap<Node, Boolean> nodeVals = new HashMap<Node, Boolean>();
	private HashSet<Point> pqCop;

	//Simply adds a node to the current pool, and returns it for reference
	public Node addNode(Node n) {
		nodePool.put(n.getLocation(), n);
		nodeVals.put(n, true);
		return n;
	}

	//Adds an edge between two nodes in the pool
	public void addEdge(Node a, Node b, double length, double weight,
			RoadSegment source, int way) throws Exception {

		//Checks whether nodes are valid
		if (nodeVals.get(a) == null || nodeVals.get(b) == null){
			throw new Exception();
		}
		else
			a.addEdge(new Edge(a, b, length, weight, source, way));
	}

	//Helper method to see if a current point is already connected to a node
	public Node getNode(Point p) {
		return nodePool.get(p);
	}

	public HashMap<Point, Node> getNodePool() {
		return nodePool;
	}

	public void setNodePool(HashMap<Point, Node> np){
		nodePool = np;
	}
	
	//Resets the nodes within the pool
	public Dijkstra reset(){
		for(Node n : nodePool.values()){
			n.setIncoming(null);
			n.setDistance(Double.POSITIVE_INFINITY);
		}
		return this;
	}


	//Primary method which calculates the shortest weighted path
	public Stack<Edge> getShortestPath(Node src, Node snk) throws RouteFailureException {

		
		//Uses a priority queue to keep track of distances from the source
		PriorityQueue<Node> pq = new PriorityQueue<Node>();
		pqCop = new HashSet<Point>();
		//Removes the source and sets it's distance to 0
		nodePool.remove(src.getLocation());
		src.setDistance(0);
		nodePool.put(src.getLocation(), src);
		
		//Adds all nodes to the priority queue, with the source at the top
		pq.addAll(nodePool.values());


		//Continues until all nodes are evaluated
		while (!pq.isEmpty()) {
			//Takes the node which is of the least distance from the source
			Node curr = pq.poll();
			pqCop.add(curr.getLocation());
			//If the distance is infinity, then there is no path to the point
			if (curr.getDistance() == Double.POSITIVE_INFINITY){
				break;
			}
			else {

				//Iterates through each of the edges of the current least node
				HashSet<Edge> edges = curr.getEdges();
				Iterator<Edge> itr = edges.iterator();
				while (itr.hasNext()) {
					Edge e = itr.next();

					//Assuming the edges contain nodes still in the queue, it re-adds them with the
					//adjusted distances from the source if it is less then then the current distance to the source
					if (!pqCop.contains(e.getSink())) {
						double distance = curr.getDistance() + e.getWeight();
						if (distance < e.getSink().getDistance()) {
							Node toChange = e.getSink();
							pq.remove(toChange);
							toChange.setDistance(distance);
							toChange.setIncoming(e);
							pq.add(toChange);
						}
					}
				}
			}
		}
		Stack<Edge> edges = new Stack<Edge>();
		Edge e = nodePool.get(snk.getLocation()).getIncoming();

		//Using the sink of the route, iterates backwards pushing edges onto stack
		while (e != null) {
			edges.push(e);
			Edge next = e.getSource().getIncoming();
			e = next;
			
		}
		return edges;
	}
}
