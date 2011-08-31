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

package org.opentripplanner.api.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.routing.contraction.ContractionHierarchySet;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.impl.ContractionPathServiceImpl;
import org.opentripplanner.routing.impl.ContractionRoutingServiceImpl;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.opentripplanner.routing.impl.SingletonPathServiceFactoryImpl;
import org.opentripplanner.routing.impl.StreetVertexIndexServiceImpl;

    public class RouteFindTask implements Runnable{
    	public ContractionPathServiceImpl pathservice;
        public ContractionHierarchySet graph;
        public Request request;
        public Integer starting_loc;
        public String outputDir;
        public HashMap<Integer, String> tazLocations;
        
        public RouteFindTask(Integer starting_loc, String outputDir,  HashMap<Integer, String> tazLocations, ContractionHierarchySet graph, 
        		String date, String time, Double walk) throws IOException, ClassNotFoundException{
        	this.starting_loc=starting_loc;
        	this.outputDir=outputDir;
        	this.tazLocations=tazLocations;
        	this.graph=graph;
        	initRequest(date, time, walk);
        }
        public void run(){
        	TAZ nextTaz = new TAZ(starting_loc+"",1);
    		for(Integer ending_loc:tazLocations.keySet()){
    			try {
               
    				//If the route is to and from the same TAZ, signal default Itinerary
    				if(starting_loc.intValue() == ending_loc.intValue()){
    					nextTaz.addMapping(ending_loc+"", new Itinerary());
    				}else{
    					String start = tazLocations.get(starting_loc);
    					String end = tazLocations.get(ending_loc);
              
    					//Trigger execution of finding fastest route
    					Response current = execute(start, end);
                
    					//Extract minimum itinerary length
    					List<Itinerary> itins = current.getPlan().itinerary;
    					Itinerary min = itins.get(0);
                	
    					//Add the mapping to the current TAZ
    					nextTaz.addMapping(ending_loc+"", min);
    				}
    			}catch(NullPointerException ex){
    			}catch (Exception e) {
					e.printStackTrace();
				}
    		}
    		//Output the TAZ to the XML directory
    		try {
				nextTaz.toXML(outputDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		System.out.println("XML Written");
        }
        
        //Generate a trip request, execute it, and wait for response of fastest route
        public Response execute (String fromPlace, String toPlace) throws IOException, ClassNotFoundException{
            
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
        
        //Initialize pathfinding/routefinding objects
        public void initRequest(String date, String time, double walk){
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
    }