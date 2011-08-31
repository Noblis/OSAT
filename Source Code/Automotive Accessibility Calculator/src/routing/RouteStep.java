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

import java.util.List;

import mapping.Point;
import mapping.RoadSegment;



/**
 * Describes one step in an IthacaQuest route. The step may consist of several
 * segments with the same name.
 */
public interface RouteStep {

   /**
    * Returns the name of the street. All segments in this {@code RouteStep}
    * should have the same name.
    * 
    * @return the name of the street
    */
   String streetName();

   /**
    * Returns the start point of the {@code RouteStep}.
    * 
    * @return the start point
    */
   Point startPoint();

   /**
    * Returns the end point of the {@code RouteStep}.
    * 
    * @return the end point
    */
   Point endPoint();

   /**
    * Returns a list of {@code RoadSegment} objects in this {@code RouteStep}.
    * The segments are listed in the order that they occur from the start point
    * to the end point.
    * 
    * @return a list of all {@code RoadSegment} objects in this step
    */
   List<RoadSegment> segments();

   /**
    * Returns the initial direction, that is, the direction when moving from the
    * start point to the first shape point if it exists or to the end point if
    * not.
    * 
    * @return the initial direction
    */
   Direction initialDirection();

   /**
    * Returns the final direction, that is, the direction when moving to the end
    * point from the last shape point if it exists or from the start point if
    * not.
    * 
    * @return the final direction
    */
   Direction finalDirection();

   /**
    * Returns the distance in miles along this step.
    * 
    * @return the distance
    */
   double distance();

   /**
    * Returns the estimated time to drive along this step taking distance and
    * speed limits into account.
    * 
    * @return the estimated time
    */
   double estimatedTime();
         
   /**
    * The compass directions.
    */
   
   
   public enum Direction {
      E, NE, N, NW, W, SW, S, SE;
      

      /**
       * Returns the opposite direction of this direction, e.g. SW -> NE.
       * 
       * @return the opposite direction
       */
      public Direction opposite() {
    	  return (values())[((this.ordinal() + 4)%8)];  
      }

      /**
       * Returns a {@code String} command telling which way to turn to go from
       * this direction to the given direction.
       * 
       * @return a command describing the direction to turn
       */
      public String getTurn(Direction next) {
    	 String output[] = {"Continue STRAIGHT", "Bear LEFT", "Turn LEFT", "Turn SHARP LEFT", "U-TURN", "Turn SHARP RIGHT", "Turn RIGHT", "Bear RIGHT"};
    	 return output[(next.ordinal() + 8 - ordinal()) % 8];
      }
      
      /**
       * Turn penalties
       */
      public double getTurnTime (Direction next){
    	  int val = (next.ordinal() + 8 - ordinal()) % 8;
    	  if(val == 0)
    		  return .75;
    	  else if(val <5)
    		  return 1.5;
    	  else
    		  return 1;
      }
   }
}
