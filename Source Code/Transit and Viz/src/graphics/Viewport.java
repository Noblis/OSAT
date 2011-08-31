/*
Copyright (c) 2011 Noblis, Inc.
*/

/*Unless explicitly acquired and licensed from Licensor under another license, the contents of this 
file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent versions as 
allowed by the RPL, and You may not copy or use this file in either source code or executable
form, except in compliance with the terms and conditions of the RPL.

All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT WARRANTY 
OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH WARRANTIES, 
INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, 
QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language governing rights and 
limitations under the RPL. */

package graphics;

import java.awt.Shape;


import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;


/**
 * 
 * Takes the union of all shapes in order to find the furthest points
 * north, south, east and west
 *
 */
public class Viewport {
	public static Rectangle2D boundingBox(HashMap<String, Shape> shapes) {

	      Iterator<Shape> itr = shapes.values().iterator();
	      Rectangle2D tot = itr.next().getBounds2D();
	      while(itr.hasNext())
	    	  Rectangle2D.union(tot, itr.next().getBounds2D(), tot);
	      return tot;
	   }

}
