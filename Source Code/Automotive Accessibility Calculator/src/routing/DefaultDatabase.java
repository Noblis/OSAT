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
import java.util.HashMap;
import java.util.Iterator;

import mapping.Point;
import mapping.RoadSegment;




/**
 * An implementation of {@link Database} consisting of road segments from the
 * TIGER/Line database from the US Census. The TIGER/Line data consists of a
 * separate set of files for each county in the US. In the file of Type 1, each
 * record represents a segment of a one-dimensional feature such as a road,
 * river, or political boundary. It is a road if its 3-character Census Feature
 * Class Code (CFCC) begins with 'A'.
 */
public class DefaultDatabase implements Database {
/*Create database to store information, mapping tlid to each road segment */
protected HashMap<Integer, RoadSegment> data = new HashMap<Integer, RoadSegment>();
   /**
    * Creates a database from the given source.
    * 
    * @param source
    *           the database source
    * @param supplementary
    *           the supplementary address ranges
    */
   public DefaultDatabase(ArrayList<String> source) {
	   //Loop through source, properly parsing and storing information
      for(String s : source){
    	  String cfcc = s.substring(55, 58);
    	  if(cfcc.charAt(0) == 'A'){
    		int tlid = parseTextInt(s, 7, 16);
    		String prefix = s.substring(17, 19).trim();
    		String name = s.substring(19, 49).trim();
    		String postfix = s.substring(49, 55).trim();
    		int fromLong = parseTextInt(s, 190, 200);
    		int fromLat = parseTextInt(s, 200, 209);
    		int toLong = parseTextInt(s, 209, 219);
    		int toLat = parseTextInt(s, 219, 228); 
    		
    		//Construct a road segment for the given line of data
    		RoadSegment r = new RoadSegment(tlid, new Point(fromLat, fromLong), new Point(toLat, toLong), prefix, name, postfix, cfcc);
    		
    		//Ultimately put the finished road segment into database mapped to its tlid
    		data.put(tlid, r); 
    		
    	  }
      }
   }
   
   public DefaultDatabase(HashMap<Integer, RoadSegment> database){
	   data = database;
   }
   
   
   /**
    * Helper method to reduce code repetition and parse out an integer from a string
    */
   protected int parseTextInt(String s, int from, int to){
	   return Integer.parseInt(s.substring(from, to).replace('+', ' ').trim());
   }

   /**
    * {@inheritDoc}
    */
   public Iterator<RoadSegment> iterator() {
      return data.values().iterator();
   }

   /**
    * {@inheritDoc}
    */
   public int size() {
      return data.size();
   }
   
   //Finds the nearest point in database with relation to p
   public Point findNearestCoordinate(Point p){
	   Iterator<RoadSegment> itr = this.iterator();
	   double min = Double.MAX_VALUE;
	   Point low = null;
	   while(itr.hasNext()){
		   RoadSegment r = itr.next();
		   Point nLow = r.getClosestDistance(p);
		   double dist = Math.sqrt(nLow.compareTo(p));
		   if(dist < min){
			   min = dist;
			   low = nLow;
		   }
	   }
	   return low;
   }
   
   //Finds the nearest roadsegment to a point, useful for geocoding
   public RoadSegment findNearestRoad(Point p){
	   RoadSegment minRoad = null;
	   Iterator<RoadSegment> itr = this.iterator();
	   double min = Double.MAX_VALUE;
	   while(itr.hasNext()){
		   RoadSegment r = itr.next();
		   Point nLow = r.getClosestDistance(p);
		   double dist = Math.sqrt(nLow.compareTo(p));
		   if(dist < min){
			   min = dist;
			   minRoad = r;
		   }
	   }
	   return minRoad;
	   
   }

   /**
    * {@inheritDoc}
    */
   public RoadSegment lookupByTlid(int tlid) {
      return data.get(tlid);
   }

   /**
    * {@inheritDoc}
    */
   public Database query(DatabaseFilter filter) {
	  //Create new database for segments which match the filter
      HashMap<Integer, RoadSegment> containing = new HashMap<Integer, RoadSegment>();
      for(RoadSegment r : data.values())
    	  if(filter.accept(r))
    		  containing.put(r.getTlid(), r);	//Once accepted, adds segment to matching database
		//System.out.println("Your query matched " + containing.size() + " record(s)");
      return new DefaultDatabase(containing);
   }
   
   
   //Generic method which returns all elements in the database if it contains less then 7 elements
   //Otherwise it notes that there are too many to list to reduce cluster.
   public String toString(){
	   String toReturn = "";
	  if(data.size() > 7)
		   return "....too many to list....\n";
	  for(RoadSegment r: data.values())
		  toReturn += r.toString() + "\n";
	  return toReturn;
   }
   
}

