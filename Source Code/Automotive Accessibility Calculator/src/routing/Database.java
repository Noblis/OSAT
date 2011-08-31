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

import mapping.Point;
import mapping.RoadSegment;


/**
 * A database of TIGER/Line data representing road segments, publicly available
 * online from the US Census Bureau. Each entity in the database is uniquely
 * identified by its TIGER/Line id (TLID). 
 */
public interface Database extends Iterable<RoadSegment> {

   /**
    * Returns the {@code RoadSegment} with the given TIGER/Line id, or {@code
    * null} if there is no such segment.
    */
   public RoadSegment lookupByTlid(int tlid);

   /**
    * Returns the number of road segments in this database.
    */
   public int size();

   /**
    * Returns a sub-database consisting of all records matching a query as
    * specified by the given {@link DatabaseFilter}.
    */
   public Database query(DatabaseFilter filter);
   
   /**
    * Finds the nearest Point in database with relation to p.
    */
   public Point findNearestCoordinate(Point p);
}
