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
import java.util.Set;
import javax.swing.JTextField;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.routing.contraction.ContractionHierarchySet;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.ContractionHierarchySerializationLibrary;
import org.opentripplanner.routing.impl.ContractionPathServiceImpl;
import org.opentripplanner.routing.impl.ContractionRoutingServiceImpl;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;
import org.opentripplanner.api.ws.PlanGenerator;
import org.opentripplanner.routing.impl.SingletonPathServiceFactoryImpl;


/**
 * 
 * Abridged version of Planner which finds the fastest public transit route between points
 * 
 */
public class RouteFind {

    static ContractionPathServiceImpl pathservice;
    static JTextField from, to, date, time;
    static ContractionHierarchySet graph;
    static Request request;

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        //Open up a frame, to allow the user to specify which files/directories to use
        ConfigFrame config = new ConfigFrame();
    }
   
    //Called by the ConfigFrame to iniate the route finding process
     static void route(String graphPath, String centroidKml, String DATE, String ARRIVE_TIME, double maxWalking, String outputDir, int skip) throws IOException, ClassNotFoundException{
        
         //Load the graph of roads and transit stops
         graph = ContractionHierarchySerializationLibrary.readGraph(new File(graphPath));
         System.out.println(graph.getGraph().getVertices().size());
         //Parse and load centroids from KML file
        SAXBuilder builder = new SAXBuilder();
        Document centroid_doc = null;
        try {
            centroid_doc = builder.build(new File(centroidKml));
        } catch (JDOMException e) {
            System.out.println("Bad File");
        }

        Element root_element = centroid_doc.getRootElement();
        Element folder_element = root_element.getChild("Document").getChild("Folder");
        List<Element> markers = folder_element.getChildren("Placemark");
        Iterator<Element> centroid_elements = markers.iterator();
        HashMap<Integer, String> tazLocations = new HashMap<Integer, String>();
        HashMap<Integer, String> censusTractBGmap = new HashMap<Integer,String>();

        while(centroid_elements.hasNext()){
            Element curr = centroid_elements.next();
            String TAZ = curr.getChildText("name");
            int taz = Integer.parseInt(TAZ);
            Element point = curr.getChild("Point");
            String coord = point.getChildText("coordinates");
            String[]coords = coord.split(",");
            String toPrint = coords[1]+","+coords[0];
            tazLocations.put(taz, toPrint);
            
            //added to make sure census tract block group identifier is written
            String desc = curr.getChildText("description");
            String[] s;
            
            if(desc != null){ //works only for census tract/block groups
            	if(desc.split(" ").length == 3){
            		s=desc.split(" ");
            		censusTractBGmap.put(taz, s[1]+s[2]); //7 character identifier of a bg
            	}
            }//end
        }

        //Set the initial parameters of a request
        initRequest(DATE, ARRIVE_TIME, maxWalking);
        
        Set<Integer> locs = tazLocations.keySet();
        Iterator<Integer> starting_taz = locs.iterator();
        
        //CHANGE THIS VALUE IF YOU WANT TO SKIP TO A PARTICULAR POINT IN OPERATION
        for(int s = 0; s < skip; s++)
            starting_taz.next();
        
        //Iterates through each TAZ and finds the shortest route to every other TAZ
        //boolean oncethru = false;
        //String s="";
        while(starting_taz.hasNext()){
        	//if(oncethru){
        	//	System.out.println(s);
        	//	System.exit(1);
        	//}
            Integer starting_loc = starting_taz.next();
            //s=s+starting_loc+"\n";
            TAZ nextTaz = new TAZ(censusTractBGmap.get(starting_loc),1); //edited to get census tract block group identifier written
            
            Iterator<Integer> ending_taz = locs.iterator();
            //Iterator<Integer> ending_taz_tmp = locs.iterator();
            while(ending_taz.hasNext()){
            	//Integer end_loc_tmp = ending_taz_tmp.next();
                try {
                    Integer ending_loc = ending_taz.next();
                    
                    //If the route is to and from the same TAZ, signal default Itinerary
                    if(starting_loc.intValue() == ending_loc.intValue()){
                        nextTaz.addMapping(censusTractBGmap.get(ending_loc), new Itinerary());
                        if(ending_taz.hasNext()){
                            ending_loc = ending_taz.next();
                        }
                        else
                            break;
                    }
                    String start = tazLocations.get(starting_loc);
                    String end = tazLocations.get(ending_loc);
                  
                    //Trigger execution of finding fastest route
                    Response current = execute(start, end);
                    
                    //Extract minimum itinerary length
                    List<Itinerary> itins = current.getPlan().itinerary;
                    Itinerary min = itins.get(0);
                    
                    //Add the mapping to the current TAZ
                    nextTaz.addMapping(censusTractBGmap.get(ending_loc), min);
                }
                catch(NullPointerException ex){
                //	s=s+end_loc_tmp+"\n";
                //	oncethru=true;
                }
            }
            
            //Output the TAZ to the XML directory
            nextTaz.toXML(outputDir);
            System.out.println("XML Written");
        }

        System.out.println("DONE");
        System.exit(0);

    }
    
     //Initialize pathfinding/routefinding objects
    static void initRequest(String date, String time, double walk){
        request = new Request();
        
        GraphServiceImpl graphService = new GraphServiceImpl();
        graphService.setContractionHierarchySet(graph);
        
        ContractionRoutingServiceImpl rout = new ContractionRoutingServiceImpl();
        rout.setGraphService(graphService);

        StreetVertexIndexServiceImpl streetVert = new StreetVertexIndexServiceImpl();
        streetVert.setGraphService(graphService);
        streetVert.setup();

        pathservice = new ContractionPathServiceImpl();
        pathservice.setGraphService(graphService);
        pathservice.setIndexService(streetVert);
        pathservice.setRoutingService(rout);


        request.setDateTime(date, time);
        request.setWheelchair(false);
        request.setArriveBy(true);
        request.setMaxWalkDistance(walk);

        OptimizeType optimize = OptimizeType.QUICK;
        request.setOptimize(optimize);
        TraverseModeSet modes = new TraverseModeSet("TRANSIT,WALK");
        request.setModes(modes);

    }

    //Generate a trip request, execute it, and wait for response of fastest route
    static Response execute (String fromPlace, String toPlace) throws IOException, ClassNotFoundException{
        
        request.setFrom(fromPlace);
        request.setTo(toPlace);

        Response response = new Response(request);
        try {
        	SingletonPathServiceFactoryImpl spsfi=new SingletonPathServiceFactoryImpl();
        	spsfi.setPathService(pathservice);
        	PlanGenerator generator = new PlanGenerator(request, spsfi);
        	
            TripPlan plan = generator.generate();
         
            response.setPlan(plan);
        }
        catch(Exception e){e.printStackTrace();}
        return response;
    } 
}