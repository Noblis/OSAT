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

import java.awt.Shape;
import java.util.ArrayList;
import java.awt.geom.*;

/**
 * An implementation of {@link RoadSegment}.
 * 
 * @see RoadSegment
 */
public class RoadSegment {
   
   /**
    * Creates a road segment. The tlid, from, to, prefix, name, and suffix for
    * this segment are supplied to the constructor. The address ranges are added
    * separately using the {@link #add(AddressRange)} method.
    * 
    * @param tlid
    *           the TIGER/Line id of the segment
    * @param from
    *           the initial coordinates of the road segment
    * @param to
    *           the final coordinates of the road segment
    * @param prefix
    *           the prefix of the road (N, S, E, W, NW, etc.)
    * @param name
    *           the name of the road
    * @param suffix
    *           the suffix of the road (St, Ave, Blvd, etc.)
    */
	
	//Create private local variables to track data
	private boolean highlight = false;
	private int tlid;
	private Point from;
	private Point to;
	private String prefix;
	private String name;
	private String suffix; 
	private String cfcc;
	private ArrayList<ShapePoints> points = new ArrayList<ShapePoints>();
	
   public RoadSegment(int tlid, Point from, Point to, String prefix, String name, String suffix, String cfcc) {
      this.tlid = tlid;
      this.from = from;
      this.to = to;
      this.prefix = prefix;
      this.name = name;
      this.suffix = suffix;
      this.cfcc = cfcc;
      
   }

   /********************************Begin Get and Set Methods*****************************************/
   /**
    * {@inheritDoc}
    */
   public int getTlid() {
      return tlid;
   }

   /**
    * {@inheritDoc}
    */
   public Point getFromPoint() {
      return from;
   }

   /**
    * {@inheritDoc}
    */
   public Point getToPoint() {
     return to;
   }

   /**
    * {@inheritDoc}
    */
   
   public String getPrefix() {
      return prefix;
   }

   /**
    * {@inheritDoc}
    */
   
   public String getName() {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   
   public String getSuffix() {
      return suffix;
   }
   
   
   //Returns the code corresponding to segment
   public String getCfcc()  {
	   return cfcc;
   }
   
   //Adds a new turning point in segment
   public void add(ShapePoints sp){
	   points.add(sp);
   }
   
   //Returns shape of the segment including all turning points
   public Shape getPath(){
	   Path2D.Double path = new Path2D.Double();
	   path.moveTo(from.getLongitude(), from.getLatitude());
	   for(ShapePoints p: points)
		   path.lineTo(p.getLon(), p.getLat());
	   
	   path.lineTo(to.getLongitude(), to.getLatitude());
	   
	   
	   return path;
   }
	   
   //Sets whether the road should be highlighted or not
   public void highlight(boolean h){
	   highlight = h;
   }
   
   //Returns whether segment is hilighted
   public boolean getHighlight(){
	   return highlight;
   }
	   
   /**************************************************************************************************/
   
   //Return the point on the road segment which is closest to the point p
   public Point getClosestDistance(Point p){
	   double min = Double.MAX_VALUE;
	   double startDist = Math.sqrt(p.compareTo(from));
	   Point low = null;
	   if(startDist < min){
		   min = startDist;
		   low = from;
	   }
	   double toDist = Math.sqrt(p.compareTo(to));
	   if(toDist < min){
		   min = toDist;
		   low = to;
	   }
	   for(ShapePoints s : points){
		   double dist = Math.sqrt(Math.pow(s.getLat() - p.getLatitude(), 2) + Math.pow(s.getLon() * p.getLongitude(), 2));
		   if(dist < min){
			   min = dist;
			   low = new Point(s.getLat(), s.getLon());
		   }
	   }
	   return low;
   }

   /**
    * {@inheritDoc}
    */
   public String fullName() {
      return prefix + " " + name + " " + suffix;
   }

   /**
    * Generic code to display road segment information, using proper formating
    */
   public String toString() {
      String toReturn = "TLID: " + tlid + ", From: " + from + ", To: " + to;
      toReturn += " , " + prefix + " " + name + " " + suffix + " ";
      return toReturn;
   }
}
