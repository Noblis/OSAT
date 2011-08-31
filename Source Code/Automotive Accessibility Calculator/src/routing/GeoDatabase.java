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

import mapping.RoadSegment;
import mapping.ShapePoints;


public class GeoDatabase extends DefaultDatabase{
	
	//Rather then having shape points parse the information, GeoDatabase
	//Parses out each latitude and longitude matching it to the database
	public GeoDatabase(ArrayList<String> source, ArrayList<String> shapePoints){
		super(source);
		for(String s : shapePoints){
			int tlid = super.parseTextInt(s, 7, 16);
			RoadSegment r = super.data.get(tlid);
			for(int c = 18; c < 191; c = c + 19){
			   int fLong = super.parseTextInt(s, c, c+10);
			   int fLat = super.parseTextInt(s, c+10, c+19);
			   if(fLat == 0)
				   break;
			   if(r != null)
			      r.add(new ShapePoints(fLong, fLat));
			}
		}		
			
    }
		
}


