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

package org.opentripplanner.api.ws;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.routing.contraction.ContractionHierarchySet;
import org.opentripplanner.routing.impl.ContractionHierarchySerializationLibrary;

/**
 * 
 * Abridged version of Planner which finds the fastest public transit route between points
 * 
 */
public class RouteFind2 {
	
	static ContractionHierarchySet graph;
    //static JTextField from, to, date, time;
    static Request request;
    static String date;
    static String arrive;
    static Double walk;
       
    public static void main(String [] args) throws IOException, ClassNotFoundException {
        //Open up a frame, to allow the user to specify which files/directories to use
        ConfigFrame config = new ConfigFrame();
    }
   
    //Called by the ConfigFrame to iniate the route finding process
    static void route(String graphPath, String centroidKml, String DATE, String ARRIVE_TIME, double maxWalking, String outputDir) throws IOException, ClassNotFoundException, InterruptedException{
        
    	//Load the graph of roads and transit stops 
        graph = ContractionHierarchySerializationLibrary.readGraph(new File(graphPath));
        System.out.println(graph.getGraph().getVertices().size());

         
        //Parse and load centroids from KML file
        SAXBuilder builder = new SAXBuilder();
        Document centroid_doc = null;
        try{
            centroid_doc = builder.build(new File(centroidKml));
        }catch (JDOMException e) {
            System.out.println("Bad File");
        }

        Element root_element = centroid_doc.getRootElement();
        Element folder_element = root_element.getChild("Document").getChild("Folder");
        List<Element> markers = folder_element.getChildren("Placemark");
        Iterator<Element> centroid_elements = markers.iterator();
        HashMap<Integer, String> tazLocations = new HashMap<Integer, String>();

        while(centroid_elements.hasNext()){
            Element curr = centroid_elements.next();
            String TAZ = curr.getChildText("name");
            int taz = Integer.parseInt(TAZ);
            Element point = curr.getChild("Point");
            String coord = point.getChildText("coordinates");
            String[]coords = coord.split(",");
            String toPrint = coords[1]+","+coords[0];
            tazLocations.put(taz, toPrint);
        }

        //Set the initial parameters of a request
        date = DATE;
        arrive=ARRIVE_TIME;
        walk=maxWalking;
        
       
        
		//break into threads for each starting_loc
		int numThreads=Runtime.getRuntime().availableProcessors()*2;
		System.out.println("Threads: "+numThreads);
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
       
		//CHANGE THIS VALUE IF YOU WANT TO SKIP TO A PARTICULAR POINT IN OPERATION        
        int skipTo=0;
        int skipped=0;
        
		//Iterates through each TAZ and finds the shortest route to every other TAZ
        for(Integer starting_loc:tazLocations.keySet()){
        	if(skipped<skipTo){
        		System.out.println("Skipping");
        		skipped++;
        	}else{
        		exec.execute(new RouteFindTask(starting_loc,outputDir,tazLocations,graph,date,arrive,walk));
        	}
        }
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        System.out.println("DONE");
        System.exit(0);
    }
    


}

