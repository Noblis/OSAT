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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.Place;


/**
 * 
 * 
 * Primary class for handling all TAZ XML files, can be loaded
 * and outputed via this class.
 * 
 * 
 *
 */
public class TAZ {
    
    String taz;
    String censusbg; //added for census tract block group indentifier
    //Stores an Itinerary for public transit trips for each TAZ
    HashMap<String, Itinerary> mappings;
    
    double acres;
    
    double area;
    
    
    //Employment/Housing/Population data stored as a double for each TAZ
    HashMap<Integer, Double> employment = new HashMap<Integer, Double>() ;
    
    HashMap<Integer, Double> population = new HashMap<Integer, Double>() ;
    
    HashMap<Integer, Double> households = new HashMap<Integer, Double>() ;
    
    //Driving time associated with driving from this taz to every other one, and the double driving time
    HashMap<String, Double> drivingTime;
    
    //Used by RouteFind, sets all variables to default, clear, values
    public TAZ (String zone, int dummy){
        
        mappings = new HashMap<String, Itinerary>();
        
        drivingTime = new HashMap<String, Double>();

        taz = zone;
        defaultExcelData();
    }
    /*
    //for RouteFind only
    public TAZ (Integer zone, String censusbg){
        
        mappings = new HashMap<String, Itinerary>();
        
        drivingTime = new HashMap<String, Double>();

        taz = zone+"";
        this.censusbg=censusbg;
        defaultExcelData();
    }*/
  
    //If a path is specified, parse data and load it
    public TAZ (String filename){
        mappings = new HashMap<String, Itinerary>();
        
        drivingTime = new HashMap<String, Double>();

        //Parses path and loads each value into object
        SAXBuilder build = new SAXBuilder();
        Document d = null;
        try {
            d = build.build(new File(filename));
        } catch (Exception e) {
            System.out.println("Failed load");
        } 

        Element root = d.getRootElement();
        Element TAZ = root.getChild("zone");
        if(TAZ.getAttribute("bg")!=null){
        	taz = TAZ.getAttributeValue("bg");
        }else{
        	taz = TAZ.getAttributeValue("taz");
        }
        acres = Double.parseDouble(root.getChild("acres").getAttributeValue("val"));
        area = Double.parseDouble(root.getChild("area").getAttributeValue("val"));
        
        Element popData = root.getChild("population_data");
        Element empData = root.getChild("employment_data");
        Element housData = root.getChild("household_data");
        List<Element> popChild = popData.getChildren("year_projection");
        List<Element> empChild = empData.getChildren("year_projection");
        List<Element> houseChild = housData.getChildren("year_projection");
        
        for(Element e : popChild){
            int year = Integer.parseInt(e.getAttributeValue("year"));
            double val = Double.parseDouble(e.getChild("population").getAttributeValue("val"));
            population.put(year, val);
        }
        
        for(Element e: empChild){
            int year = Integer.parseInt(e.getAttributeValue("year"));
            double val = Double.parseDouble(e.getChild("employment").getAttributeValue("val"));
            employment.put(year, val);
        }
         
        for(Element e  : houseChild){
            int year = Integer.parseInt(e.getAttributeValue("year"));
            double val = Double.parseDouble(e.getChild("households").getAttributeValue("val"));
            households.put(year, val);
        }
        
        Element dest = root.getChild("destinations");
        List<Element> destinations = dest.getChildren();
        Iterator<Element> itr = destinations.iterator();
        
        //Create an itinerary to store for each TAZ
        while(itr.hasNext()){
            Element e = itr.next();
            Itinerary i = new Itinerary();
            i.duration = Long.parseLong(e.getChild("total_time").getAttributeValue("val"));

            i.walkDistance = Double.parseDouble(e.getChild("walk_distance").getAttributeValue("val"));

            i.walkTime = Long.parseLong(e.getChild("walk_time").getAttributeValue("val"));

            i.waitingTime = Long.parseLong(e.getChild("wait_time").getAttributeValue("val"));

            i.transitTime = Long.parseLong(e.getChild("transit_time").getAttributeValue("val"));

            i.transfers = Integer.parseInt(e.getChild("transfers").getAttributeValue("val"));

            i.startTime = new Date(e.getChild("start_time").getAttributeValue("val"));

            i.endTime = new Date(e.getChild("end_time").getAttributeValue("val"));
            
            String taz = e.getAttributeValue("taz");

            try{
            Double time = Double.parseDouble(e.getChild("drive_time").getAttributeValue("val"));
            
            drivingTime.put(taz, time);
            }
            catch(Exception exc){
                drivingTime.put(taz, 0.);
            }
            
            
            mappings.put(taz, i);
        }
        
    }
    
    public String getTAZ(){
        return taz;
    }
    
    private void defaultExcelData(){
        acres = 0.;
        area = 0.;
        employment = new HashMap<Integer, Double>();
        
        population = new HashMap<Integer, Double>();
        
        households = new HashMap<Integer, Double>();
    }
    
    
    public void setAcres(double a){
        acres = a;
    }
    
    public void setArea(double a){
        area = a;
    }
    
    public void addDrivingData(HashMap<String, Double> dists){
        drivingTime = dists;
    }
    
    public void addEmploymentData(HashMap<Integer, Double> emps){
        employment = emps;
    }
    
    public void addHousingData(HashMap<Integer, Double> hous){
        households = hous;
    }
    
    public void addPopulationData(HashMap<Integer, Double> pop){
        population = pop;
    }
    
    public void addMapping(String zone, Itinerary r){
        if(!zone.equals(taz))
            mappings.put(zone, r);
        
        //DEFAULT ITINERARY VALUES
        else{
            r.startTime = new Date(2010,6,16,8,55,0);
            r.endTime = new Date(2010,6,16,9,0,0);
            r.duration = 300000;
            r.walkTime = 300000;
            mappings.put(zone, r);
        }
    }
    
    public Itinerary getMapping(int zone){
        return mappings.get(zone);
    }
    public HashMap<String,Itinerary> getMappings(){
    	return mappings;
    }
    
    public void removeMapping(int zone){
        mappings.remove(zone);
    }
    
    public double getAcres(){
        return acres;
    }
    
    public double getArea(){
        return area;
    }
    
    public double getDriveTime(String zone){
        return drivingTime.get(zone);
    }
    
    public void setDriveTime(String zone, double time){
        drivingTime.put(zone, time);
    }
    
    public double getHouseholds(int year){
        return (households.get(year) != null) ? households.get(year) : Double.NaN;
    }
    
    public double getPopulation(int year){
        return (population.get(year) != null) ? population.get(year) : Double.NaN;
    }
    
    public double getEmployment(int year){
        return (employment.get(year) != null) ? employment.get(year) : Double.NaN;
    }
    
    public Date getStart(String zone){
        Itinerary r = mappings.get(zone);
        return r.startTime;
    }
    
    public Date getEnd(String zone){
        Itinerary r = mappings.get(zone);
        return r.endTime;
    }
    
    public long getTransitTime(String zone){
        Itinerary r = mappings.get(zone);
        return r.transitTime;
    }
    
    public long getWalkTime(String zone){
        Itinerary r = mappings.get(zone);
        return r.walkTime;
    }
    
    public double getWalkDistance(String zone){
        Itinerary r = mappings.get(zone);
        return r.walkDistance;
    }
    
    public long getWaitTime(String zone){
        Itinerary r = mappings.get(zone);
        return r.waitingTime;
    }
    
    public void setTimes(String zone, long walk, long wait, long transit, long total){
        Itinerary r = mappings.get(zone);
        r.duration = total;
        r.waitingTime = wait;
        r.walkTime = walk ;
        r.transitTime = transit;
    }
    
    public void setTransitDuration(String zone, long total){
        Itinerary r = mappings.get(zone);
        r.duration = total;
    }
    
    public void setWalkTime(String zone, long walk){
        Itinerary r = mappings.get(zone);
        r.walkTime = walk;
    }
    
    public void setWalkDistance(String zone, double walk){
        Itinerary r = mappings.get(zone);
        r.walkDistance = walk;
    }
        
    public int getTransfers(String zone){
        Itinerary r = mappings.get(zone);
        return r.transfers;
    }
    
    public long getTotalTime(String zone){
        Itinerary r = mappings.get(zone);
        return (r != null)? r.duration: -1;
    }
    
    public List<Leg> getLegs(String zone){
        Itinerary r = mappings.get(zone);
        return r.legs;
    }
    
    public int getNumDestinations(){
        return mappings.size();
    }
    
    public int getNumYearData(){
        return population.size();
    }
    
    //Usually not used in typical operation, can be used during RouteFind to look at formatted output
    public void printLegs(String zone){
        List<Leg> legs = getLegs(zone);
        Iterator<Leg> itr = legs.iterator();
        while(itr.hasNext()){
            Leg l = itr.next();
            System.out.println("Mode: " + l.mode);
            System.out.println("Route: " + l.route);
            Place f = l.from;
            System.out.println("From: " + f.name + " Stop: " + f.stopId);
            System.out.println("Starting From: " + l.startTime);
            Place t = l.to;
            System.out.println("To: " + t.name + " Stop: " + t.stopId);
            System.out.println("Get there at: " + l.endTime);
            System.out.println("Distance: " + l.distance);
        }
    }
    
    //Not used in typical operation, can be used in RouteFind
    public void findLongestRoute(){
        Set<String> keys = mappings.keySet();
        Iterator<String> itr = keys.iterator();
        long max = 0;
        String spot = "-1";
        while(itr.hasNext()){
            String i = itr.next();
            long t = getTotalTime(i);
            if(t > max){
                spot = i;
                max = t;
            }
        }
        
        System.out.println("Furthest away TAZ: " + spot);
        System.out.println("Total Time: " + getTotalTime(spot));
        System.out.println("Wait Time: " + getWaitTime(spot));
        System.out.println("Walk Time: " + getWalkTime(spot));
        
    }
    
    //Not used in typical operation, can be used in RouteFind
    public void findLongestWait(){
        Set<String> keys = mappings.keySet();
        Iterator<String> itr = keys.iterator();
        long max = -1;
        String spot = "-1";
        while(itr.hasNext()){
            String i = itr.next();
            long t = getWaitTime(i);
            if(t > max){
                spot = i;
                max = t;
            }
        }
        
        System.out.println("Longest wait TAZ: " + spot);
        System.out.println("Total Time: " + getTotalTime(spot));
        System.out.println("Wait Time: " + getWaitTime(spot));
        System.out.println("Walk Time: " + getWalkTime(spot));
        
    }
    
    //Outputs all data stored in the object to the specified directory
    public void toXML(String dir) throws IOException{
        Element root = new Element("TAZ");
        Element zone = new Element("zone");
        
        //edited to get census tract block group identifier written in
        if(censusbg != null){
            zone.setAttribute("bg", censusbg);
        }else{
            zone.setAttribute("taz", taz+"");
        }
        
        Element area = new Element("area");
        area.setAttribute("val", this.area+"");
        
        Element acres = new Element("acres");
        acres.setAttribute("val", this.acres+"");
        
        Element popData = new Element("population_data");
        
        Set<Integer> ys = population.keySet();
        Iterator<Integer> ysItr = ys.iterator();
        while(ysItr.hasNext()){
            int nYear = ysItr.next();
            Element datum = new Element("year_projection");
            datum.setAttribute("year", nYear + "");
            Element pop = new Element("population");
            pop.setAttribute("val", population.get(nYear) + "");
            datum.addContent(pop);
            popData.addContent(datum);
        }

        Element empData = new Element("employment_data");
        
        Set<Integer> es = employment.keySet();
        Iterator<Integer> esItr = es.iterator();
        while(esItr.hasNext()){
            int nYear = esItr.next();
            Element datum = new Element("year_projection");
            datum.setAttribute("year", nYear + "");
            Element pop = new Element("employment");
            pop.setAttribute("val", employment.get(nYear) + "");
            datum.addContent(pop);
            empData.addContent(datum);
        }
        
        Element housData = new Element("household_data");
        
        Set<Integer> hs = households.keySet();
        Iterator<Integer> hsItr = hs.iterator();
        while(hsItr.hasNext()){
            int nYear = hsItr.next();
            Element datum = new Element("year_projection");
            datum.setAttribute("year", nYear + "");
            Element hous = new Element("households");
            hous.setAttribute("val", households.get(nYear) + "");
            datum.addContent(hous);
            housData.addContent(datum);
        }
        
        Element destinations = new Element("destinations");

        
        Set<String> keys = mappings.keySet();
        Iterator<String> k = keys.iterator();
        while(k.hasNext()){
            String curTaz = k.next();
            //Itinerary i = mappings.get(curTaz);
            
            Element dest = new Element("destination");
            dest.setAttribute("taz", curTaz+"");
            
            Element tTime = new Element("total_time");
            tTime.setAttribute("val",getTotalTime(curTaz) + "");
            Element walkTime = new Element("walk_time");
            walkTime.setAttribute("val", getWalkTime(curTaz) + "");
            Element waitTime = new Element("wait_time");
            waitTime.setAttribute("val",getWaitTime(curTaz) + "");
            Element walkDist = new Element("walk_distance");
            walkDist.setAttribute("val", getWalkDistance(curTaz) +"");
            Element transTime = new Element("transit_time");
            transTime.setAttribute("val",getTransitTime(curTaz) + "");
            Element transfers = new Element("transfers");
            transfers.setAttribute("val", getTransfers(curTaz) + "");
            Element startTime = new Element("start_time");
            startTime.setAttribute("val", getStart(curTaz) + "");
            Element endTime = new Element("end_time");
            endTime.setAttribute("val", getEnd(curTaz) + "");
            
            Element drivingTimes = new Element("drive_time");
            drivingTimes.setAttribute("val", drivingTime.get(curTaz) + "");
            
            dest.addContent(tTime);
            dest.addContent(walkTime);
            dest.addContent(walkDist);
            dest.addContent(waitTime);
            dest.addContent(transTime);
            dest.addContent(transfers);
            dest.addContent(startTime);
            dest.addContent(endTime);
            dest.addContent(drivingTimes);
            
            destinations.addContent(dest);
                
                
            }
        root.addContent(zone);
        root.addContent(area);
        root.addContent(acres);
        root.addContent(popData);
        root.addContent(empData);
        root.addContent(housData);
        root.addContent(destinations);
        
        XMLOutputter output = new XMLOutputter();
        output.setFormat(Format.getPrettyFormat());
        if(dir.endsWith("/"))
            dir = dir.substring(0, dir.length()-1);
        FileWriter writer = null;
        try {
            writer = new FileWriter(dir + "/TAZ-"+taz+".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document d = new Document(root);
        try {
            output.output(d, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.flush();
        writer.close();
        
        }
    
    
        
    }
    
    


