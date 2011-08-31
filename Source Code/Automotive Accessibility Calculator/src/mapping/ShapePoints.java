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


//Class to store information about roads using latitude and longitudes
//Data is previously parsed
public class ShapePoints {
	private double lon;
	private double lat;
	public ShapePoints(int lon, int lat) {
		this.lon = lon/1.0E6;
		this.lat = lat/1.0E6;
	}
	
	//Returns the latitude
	public double getLat(){
		return lat;
	}
	
	//Returns the longitude
	public double getLon(){
		return lon;
	}
	
}
