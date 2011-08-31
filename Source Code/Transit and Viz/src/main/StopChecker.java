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

package main;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import calculations.KMLReader;




/**
 * 
 * Not used for any part of the GUI, just a helper file to look at the number of transit
 * stops per acreage in each TAZ. Serves as an example file of how one might use
 * the number of transit stops available in a region to determine whether or not
 * to include it in the analysis
 *
 */
public class StopChecker {
	
	static HashMap<String, Shape> shapes;
public static void main(String [] args) throws FileNotFoundException{
        
        Scanner reader = new Scanner(new File("C:/stops.txt"));
        KMLReader kml = new KMLReader("C:/Documents and Settings/M29455/Desktop/King County/kingCounty.kml");
        shapes = kml.getPolygons();
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        reader.nextLine();
        while(reader.hasNext()){
            String line = reader.nextLine();
            String[] data = line.split(",");
            double lat = Double.parseDouble(data[data.length-3]);
            double lon = Double.parseDouble(data[data.length-2]);
            String taz = getTAZ(lat, lon);
            if(count.containsKey(taz))
            	count.put(taz, count.get(taz) + 1);
            else
            	count.put(taz, 1);
        }
        
        
        reader = new Scanner(new File("C:/acreage.txt"));
        HashMap<Integer, Double> acres = new HashMap<Integer, Double>();
        while(reader.hasNext()){
        	String line = reader.nextLine();
        	String[] data = line.split("\t");
        	int taz = Integer.parseInt(data[0]);
        	double acre = Double.parseDouble(data[3]);
        	System.out.println(acre);
        	acres.put(taz, acre);
        }
        HashMap<String, Double> standardize = new HashMap<String, Double>();
        for(String i : count.keySet()){
        		standardize.put(i, count.get(i)/acres.get(i) * 10000);
        }
        
        for(int i = 1; i <= 530 ; i++){
        	if(standardize.containsKey(i)){
        		if(standardize.get(i)<3)
        			System.out.print(i +",");
        	}
        	else if(acres.get(i) >25000){
        		System.out.print(i + ",");
        	}
        		
        }
        
        
        
        
    }

	static String getTAZ(double lon, double lat){
		Point2D.Double p = new Point2D.Double(lat, lon);
		for(String i: shapes.keySet()){
			Shape s = shapes.get(i);
			if(s.contains(p)){
				return i;
			}
		}
		return "-1";
	}

}
