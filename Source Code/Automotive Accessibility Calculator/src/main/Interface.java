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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import mapping.Point;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.opentripplanner.api.ws.TAZ;

import routing.Database;
import routing.GeoDatabase;
import routing.RouteFailureException;
import routing.RoutePlanner;
import routing.RouteStep;
import routing.RouteStep.Direction;



public class Interface {

	static RoutePlanner route_planner;
	static Database road_database;



	public static void main (String [] args) throws Exception {

		//Sets Look and Feel to the operating system L&F, not necessary
		UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());

		//Asks the user to specify directories, which then calls calculateAutoTimes, otherwise
		//the program exits without further operation
		ConfigFrame get_settings = new ConfigFrame();

	}


	static void calculateAutoTimes(String inputDir, String outputDir, String tigerDir, String kmlPath){

		// Filters directory for Tiger files ending with .RT1 and .RT2
		File[] files = extractTigerFiles(new JFileChooser(), tigerDir);

		String pathRT1;
		String pathRT2;

		// Select proper path to each file, order not originally determined
		if(files[0].getPath().toLowerCase().endsWith(".rt1")){
			pathRT1 = files[0].getPath();
			pathRT2 = files[1].getPath();
		}
		else{
			pathRT1 = files[1].getPath();
			pathRT2 = files[0].getPath();
		}

		// Create data structures to hold the contents of each file
		ArrayList<String> rt1 = new ArrayList<String>();
		ArrayList<String> rt2 = new ArrayList<String>();

		// Populate local database with road segments, if this fails
		// the program exits and closes.
		try {
			initDatabase(pathRT1, pathRT2, rt1, rt2);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Database Initialization Failed. Exiting");
			System.exit(0);
		}


		// Open file containing centroid points and contain data structures
		// in order to hold coordinates as well as travel times for a route from
		// a TAZ to the same TAZ.
		File kml_file = new File(kmlPath);
		HashMap<Integer,Point> centroid_coordinates = new HashMap<Integer, Point>();
		HashMap<Integer, Double> self_travel_times = new HashMap<Integer, Double>();



		//Finds centroids in graph and determines travel times for routes to and from the same
		//TAZ, then uses data to find all routes between each point
		try {
			gatherPoints(kml_file, centroid_coordinates, self_travel_times);

			Set<Integer> centroid_keys = centroid_coordinates.keySet();
			Iterator<Integer> keyItr = centroid_keys.iterator();

			findAllRoutes(outputDir, inputDir, centroid_coordinates, self_travel_times,
					keyItr);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Faulty File, Exiting");
			e1.printStackTrace();
			System.exit(0);
		}

	}



	private static void findAllRoutes(final String outputDir,
			final String inputDir, HashMap<Integer, Point> coords,
			HashMap<Integer, Double> travel, Iterator<Integer> keyItr)
	throws RouteFailureException, JDOMException, IOException {
		while(keyItr.hasNext()){
			
			//Iterates through each key, which contains all TAZs and calculates the
			//trip time to every other TAZ
			Integer from = keyItr.next();
			Iterator<Integer> compItr = coords.keySet().iterator();
			HashMap<Integer, Double> times = new HashMap<Integer, Double>();
			
			//Computes the route times to every other TAZ from the current TAZ
			iterateThroughDestinations(coords, travel, from,
					compItr, times);
			
			//Resets the shortest path tree
			route_planner.reset();
			
			//Append the driving data to the correct XML file
			TAZ nTaz = new TAZ(inputDir + "TAZ-" + from + ".xml");
			for(Integer zone : times.keySet()){
				nTaz.setDriveTime(zone, times.get(zone));
			}
			nTaz.toXML(outputDir);

		}
	}



	private static void iterateThroughDestinations(
			HashMap<Integer, Point> centroid_coordinates,
			HashMap<Integer, Double> self_route_times, Integer from,
			Iterator<Integer> destination_iterator, HashMap<Integer, Double> computed_route_times)
	throws RouteFailureException {
		
		//Iterates through every TAZ finding trip times
		while(destination_iterator.hasNext()){
			Integer to = destination_iterator.next();
			System.out.println(from + "-" + to);
			
			//If the trip is to and from the same TAZ, only put the distance to the nearest road
			//as the total trip time, otherwise calculate shortest path
			if(from.intValue() != to.intValue()){
				Point from_coordinate = centroid_coordinates.get(from);
				Point to_coordinate = centroid_coordinates.get(to);
				
				double duration = 0.;
				List<RouteStep> steps = route_planner.getRoute(from_coordinate, to_coordinate);
				
				//Compute turn penalties
				Direction finalDir = null;
				if(steps.size() > 0)
					finalDir = steps.get(0).initialDirection();
				for(RouteStep r : steps){
					Direction init = r.initialDirection();
					duration += r.estimatedTime() + finalDir.getTurnTime(init);
					finalDir = r.finalDirection();
				}
				
				//If duration was 0, possibly ran into an error which is usually caused by a road segment not
				//being part of the graph and needs to be moved
				if(duration == 0)
					JOptionPane.showMessageDialog(null, "Check Points " + from + " " + to + ", one may need to be moved closer to a street. If the points are just close, ignore.");
				
				computed_route_times.put(to, duration + self_route_times.get(from) + self_route_times.get(to));
			}
			else{
				//Use the pre-computed TAZ to TAZ route time as the result
				computed_route_times.put(to, self_route_times.get(from));
			}

		}
	}



	//Parse KML file and retrieve coordinates of each centroid
	private static void gatherPoints(File f, HashMap<Integer, Point> abridged_centroids,
			HashMap<Integer, Double> self_route_times) throws JDOMException,
			IOException {
		
		//Parsing KML
		SAXBuilder sax = new SAXBuilder();
		Document kml = sax.build(f);
		Element rt = kml.getRootElement();

		Element doc = rt.getChild("Document");
		List<Element> markers = doc.getChildren("Placemark");
		Iterator<Element> elems = markers.iterator();
		while(elems.hasNext()){
			Element curr = elems.next();
			String TAZ = curr.getChildText("name");
			Element point = curr.getChild("Point");
			String coord = point.getChildText("coordinates");
			String[]latLong = coord.split(",");
			Integer taz = Integer.parseInt(TAZ);
			
			//Parse the lat and long coordinate of the TAZ and find the point nearest to it
			//within the road segment graph and calculate the time to that point and store as the
			//TAZ to TAZ route time
			Point centroid = new Point(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0]));
			Point nearest = road_database.findNearestCoordinate(centroid);
			abridged_centroids.put(taz, nearest);
			self_route_times.put(taz, RoutePlanner.distanceInMiles(centroid, nearest)/10. * 60.);

		}
	}




	private static void initDatabase(String pathOne, String pathTwo,
			final ArrayList<String> rt1, final ArrayList<String> rt2)
	throws FileNotFoundException, IOException {
		
		//Open up RT1 Tiger file and retrieve each line
		BufferedReader bufRead = new BufferedReader(new FileReader(pathOne));

		String line = bufRead.readLine();
		while (line != null){
			rt1.add(line);
			line = bufRead.readLine();
		}

		bufRead.close();	

		//Open up RT2 Tiger file and retrieve each line
		bufRead = new BufferedReader(new FileReader(pathTwo));
		line = bufRead.readLine();
		while (line != null){
			rt2.add(line);
			line = bufRead.readLine();
		}
		bufRead.close();
		
		road_database = new GeoDatabase(rt1, rt2);
		route_planner = new RoutePlanner(road_database);
		System.out.println("Database Initialized...");
	}



	private static File[] extractTigerFiles(JFileChooser fc, String directory) {
		int returnVal;
		
		//Open directory and extract RT1 and RT2 Tiger files
		File dir = new File(directory);
		File[] files = dir.listFiles(new RTFilter());
		
		//If the file length is less then 2 then the files are not present
		while(files.length != 2){
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			JOptionPane.showMessageDialog(null, "Directory Failed");
			returnVal = fc.showDialog(null, "Select TIGER/Line File");
			if(returnVal == 1){
				System.exit(0);
			}
			directory = fc.getSelectedFile().getPath() + "\\";
			dir = new File(directory);
			files = dir.listFiles(new RTFilter());
		}
		
		return files;
	}

}



