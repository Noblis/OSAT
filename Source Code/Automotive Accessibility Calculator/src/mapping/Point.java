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

import java.awt.geom.Point2D;

/**
 * A latitude-longitude point on the earth's surface. A positive latitude lies
 * in the northern hemisphere, and a negative longitude lies in the western
 * hemisphere.
 */
public final class Point extends Point2D.Double {

   public Point(int latitudeE6, int longitudeE6) {
      super(longitudeE6/1.0E6, latitudeE6/1.0E6);
   }
   
   public Point(double latitude, double longitude) {
      super(longitude, latitude);
   }
   

   /**
    * Returns this point's latitude in degrees.
    * 
    * @return this point's latitude in degrees
    */
   public double getLatitude() {
      return getY();
   }

   /**
    * Returns this point's longitude in degrees.
    * 
    * @return this point's longitude in degrees
    */
   public double getLongitude() {
      return getX();
   }

   /**
    * Returns a formatted string representation of this point.
    * 
    * @return a string representation of this point
    */
   @Override
   public String toString() {
      return String.format("[%f%s, %f%s]", // \u00b0 does not show up in Windows
               Math.abs(getLongitude()), getLongitude() > 0 ? "E" : "W",
               Math.abs(getLatitude()), getLatitude() > 0 ? "N" : "S");
   }
   
   public double compareTo(Point p){
	   double nX = p.getX() - getX();
	   double nY = p.getY() - getY();
	   return nX*nX + nY*nY;
	   
   }
}
