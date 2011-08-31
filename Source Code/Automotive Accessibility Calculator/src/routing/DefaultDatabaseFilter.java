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

import mapping.RoadSegment;

public class DefaultDatabaseFilter implements DatabaseFilter{
	
	//Private locals to keep track of data
	private String prefix;
	private String street;
	private String suffix;
	private boolean written;

	public DefaultDatabaseFilter (int leading, String pre, String name, String post, boolean writ){
		prefix = pre;
		street = name;
		suffix = post;
		written = writ;
	}
	public boolean accept(RoadSegment road){
		//If the street does not match the database perfectly or is not contained within it returns false
		//Street name is most important element
		if(street != null)
			if(!road.getName().equalsIgnoreCase(street) && !road.getName().contains(street))
				return false; 
		
		
		//Checks whether the prefix matches up assuming it is entered
		if(prefix != null)
			if(!road.getPrefix().equalsIgnoreCase(prefix))
				return false;
		
		//Checks both whether a suffix is present and also if it was written as a name, allows for 
		//the program to interpret as an extension of the name
		if (suffix != null)
			if(! road.getSuffix().equalsIgnoreCase(suffix) && (!road.getName().equalsIgnoreCase(street + " " + suffix) || written))
				return false;
		return true;
	}
}

