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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import routing.GeoDatabase;
import routing.RoutePlanner;

import mapping.Point;
import mapping.RoadSegment;


/**
 * 
 * Not associated with Tiger/Line Router, used for generating a file containing
 * street names of associated centroid file, at the current state not all of them
 * will be included because of a lack of road name
 */

public class GeoEncoder {
	public static void main(String [] args) throws IOException{
		
	//Select the Tiger/Line Directory	
	JFileChooser fc = new JFileChooser();
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
	int returnVal = -1;
	returnVal = fc.showDialog(null, "TIGER Directory");
	if(returnVal == 1)
		System.exit(0);

	String directory = fc.getSelectedFile().getPath() + "\\";

	File dir = new File(directory);
	File[] files = dir.listFiles(new RTFilter());
	while(files.length != 2){
		JOptionPane.showMessageDialog(null, "Directory Failed");
		returnVal = fc.showDialog(null, "Select");
		if(returnVal == 1){
			System.exit(0);
		}
		directory = fc.getSelectedFile().getPath() + "\\";
		dir = new File(directory);
		files = dir.listFiles(new RTFilter());
	}

	String pathOne;
	String pathTwo;

	if(files[0].getPath().endsWith(".RT1")){
		pathOne = files[0].getPath();
		pathTwo = files[1].getPath();
	}
	else{
		pathOne = files[1].getPath();
		pathTwo = files[0].getPath();
	}

	final ArrayList<String> rt1 = new ArrayList<String>();
	final ArrayList<String> rt2 = new ArrayList<String>();

	BufferedReader bufRead = new BufferedReader(new FileReader(pathOne));

	String line = bufRead.readLine();
	while (line != null){
		rt1.add(line);
		line = bufRead.readLine();
	}

	bufRead.close();	

	bufRead = new BufferedReader(new FileReader(pathTwo));
	line = bufRead.readLine();
	while (line != null){
		rt2.add(line);
		line = bufRead.readLine();
	}
	GeoDatabase db = new GeoDatabase(rt1, rt2);
	RoutePlanner plan = new RoutePlanner(db);
	System.out.println("Database initialized");
	
	//Specify KML Centroid data input
	JFileChooser forInp = new JFileChooser();
	int choice = forInp.showDialog(null, "KML Centroids");
	
	//Currently just outputs text to the directory specified below
	PrintWriter pw = new PrintWriter(new File("C:/near.txt"));
	if(choice == 0){
		File f = forInp.getSelectedFile();
		Scanner reader = null;
		HashMap<Integer,Point> coords = new HashMap<Integer, Point>();
		HashMap<Integer, Double> travel = new HashMap<Integer, Double>();
			reader = new Scanner(f);
			while(reader.hasNext()){
				Integer taz = Integer.parseInt(reader.nextLine());
				String[] latLong = reader.nextLine().split(",");
				Point centroid = new Point(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));
				
				//Finds the nearest road segment and prints it
				RoadSegment r = db.findNearestRoad(centroid);
				System.out.println(r.toString());

				if(r.fullName().length() > 2){
					RoadSegment road = db.findNearestRoad(centroid);
					pw.println(taz + " - " + road.fullName() + " - " + centroid.getLatitude() + " - " + centroid.getLongitude());
					
				}
			}
			pw.flush();
			pw.close();
		
	}
	}

}
