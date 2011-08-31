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

package preotp;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class PreOTPDriver {

	/**
	 * @param args - filepath to configuration file
	 */
	public static boolean split, keepTAZ, manualAdjust;
	public static int roundToPow; 		//rounds to the nearest 10^roundToPow
	public static String tazpath; 		//path to the TAZ KML file
	public static String bgpath; 		//path to the BG KML file
	public static String outputpath; 	//path of output KML file containing polygons after clipping/filtering 
	public static double areaFilter;	//fraction of smallest TAZ/BG to use as area threshold
	public static double maxDistanceToStop;
	public static double smallestArea;
	
	public static HashMap<Integer,GraphNode> TAZmap = new HashMap<Integer,GraphNode>(); 		//mapping of nodeIDs to TAZs
	public static HashMap<Integer,GraphNode> BGmap = new HashMap<Integer,GraphNode>();			//mapping of nodeIDs to BGs
	public static TreeMap<Integer,GraphNode> Outputmap = new TreeMap<Integer,GraphNode>(); 	//mapping of nodeIDs to split BGs
	public static HashSet<Point2D.Double> GTFSstops = new HashSet<Point2D.Double>();
	public static ArrayList<Integer> removeList = new ArrayList<Integer>();
	public static HashMap<Integer,Point2D.Double> adjustList = new HashMap<Integer,Point2D.Double>();
	
	public static void main(String[] args) {
			
		long start = System.currentTimeMillis();
		readConfig(args[0]);								//read in parameters from configuration file
		readKML(tazpath,true); readKML(bgpath,false);		//read in KML files, round coordinates, populate hash maps
		if(split){
			adjustPoints();										//remove duplicate points resulting from rounding
			buildAdj(TAZmap,true);								//build adjacencies among TAZs
			buildAdj(BGmap,false);								//build adjacencies among BGs
			buildInterAdj();									//build adjacencies between TAZs and BGs
			polygonClip();										//clip polygons
		}else{
			if(keepTAZ){
				copyToOutputMap(true);
			}else{
				copyToOutputMap(false);
			}
		}
		if(manualAdjust){
			manualAdjustment();									//manually adjust from reading config XML
			filter();											//filter based on distance to transit stops
			renumber();											//renumber polygons since some numbers were removed
		}
		
		writeToKML(Outputmap, outputpath, false);			//write remaining polygons to KML file
		System.out.println("Total: "+(double)(System.currentTimeMillis()-start)/1000 +"secs");
	}
	public static void copyToOutputMap(boolean taz){
		if(taz){
			for(Integer z:TAZmap.keySet()){
				Outputmap.put(z, TAZmap.get(z));
			}
		}else{
			for(Integer z:BGmap.keySet()){
				Outputmap.put(z, BGmap.get(z));
			}
		}
	}

	public static void renumber(){
		int count = 0;
		TreeMap<Integer, GraphNode> tmp = new TreeMap<Integer, GraphNode>();
		for(Integer z:Outputmap.keySet()){
			Outputmap.get(z).setNodeID(count);
			tmp.put(count, Outputmap.get(z));
			count++;
		}
		Outputmap=tmp;
	}
	public static void filter(){
		ArrayList<Integer> filterout = new ArrayList<Integer>();
		for(Integer z:Outputmap.keySet()){
			if(!closeToStop(Outputmap.get(z).getCentroid())){
				filterout.add(z);
			}
		}
		for(int z=0;z<filterout.size();z++){
			Outputmap.remove(filterout.get(z));
		}
	}
	public static void manualAdjustment(){
		for(int z=0;z<removeList.size();z++){
			Outputmap.remove(removeList.get(z));
		}
		for(Integer z:adjustList.keySet()){
			Outputmap.get(z).setCentroid(adjustList.get(z));
		}
	}
	public static boolean closeToStop(Point2D.Double pt){
		
		Iterator<Point2D.Double> iter = GTFSstops.iterator();
		while(iter.hasNext()){
			if(pt.distance(iter.next()) <maxDistanceToStop){
				return true;
			}
		}
		return false;
	}
	public static void readStops(String path){
		try{
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String transitstop;
			String[] split;
			transitstop=br.readLine();
			split=transitstop.split(",");
			int latIndex=0, lonIndex=0;
			for(int z=0;z<split.length;z++){
				if(split[z].equals("stop_lon")){
					lonIndex=z;
				}
				if(split[z].equals("stop_lat")){
					latIndex=z;
				}
			}
			while((transitstop = br.readLine()) != null){
				if(transitstop.contains("\"")){
					boolean between=false;
					for(int y=0;y<transitstop.length();y++){
						if(transitstop.charAt(y)=='"'){
							between=!between;
						}else if(transitstop.charAt(y)==',' && between){
							transitstop = transitstop.substring(0,y)+transitstop.substring(y+1,transitstop.length());
							y--;
						}
					}
				}
				split = transitstop.split(",");
				Point2D.Double stopLocation = new Point2D.Double(Double.parseDouble(split[lonIndex]),Double.parseDouble(split[latIndex]));
				GTFSstops.add(stopLocation);
			}
			in.close();
		}catch (Exception e){
			System.err.println(e);
		}
		System.out.println("GTFS stops read: "+GTFSstops.size());
	}
	
	public static void readConfig(String path){
		SAXBuilder build = new SAXBuilder(); Document d = null;
		try {d = build.build(new File(path));}catch(Exception e){e.printStackTrace();}	
		Element root = d.getRootElement();
		split = Integer.parseInt(root.getChild("split").getText())==1;
		keepTAZ = Integer.parseInt(root.getChild("keepTAZ").getText())==1;
		manualAdjust = Integer.parseInt(root.getChild("manualadjust").getText())==1;
		tazpath = root.getChild("kmlpath_taz").getText();
		bgpath = root.getChild("kmlpath_bg").getText();
		maxDistanceToStop = Double.parseDouble(root.getChild("maxDist").getText());
		roundToPow = Integer.parseInt(root.getChild("round").getText());
		areaFilter = Double.parseDouble(root.getChild("areaFilter").getText());
		outputpath = root.getChild("output").getText();
		
		List<Element> gtfs = root.getChildren("gtfs");
		Iterator<Element> iter = gtfs.iterator();
		while(iter.hasNext()){
			readStops(iter.next().getText());
		}
		
		List<Element> removals = root.getChildren("remove");
		if(removals !=null){
			iter = removals.iterator();
			while(iter.hasNext()){
				removeList.add(Integer.parseInt(iter.next().getText()));
			}
		}
		
		List<Element> adjustments = root.getChildren("adjust");
		if(adjustments != null){
			Element e; String[] s;
			iter = adjustments.iterator();
			while(iter.hasNext()){
				e=iter.next();
				s=e.getText().split(",");
				adjustList.put(Integer.parseInt(e.getAttributeValue("id")), new Point2D.Double(Double.parseDouble(s[0]),Double.parseDouble(s[1])));
			}
		}
		
	}
	
	public static void polygonClip(){
		
		//find smallest TAZ/BG
		smallestArea=Double.MAX_VALUE;
		for(Integer z:TAZmap.keySet()){
			if(TAZmap.get(z).getArea()<smallestArea){
				smallestArea=TAZmap.get(z).getArea();
			}
		}
		for(Integer z:BGmap.keySet()){
			if(BGmap.get(z).getArea()<smallestArea){
				smallestArea=BGmap.get(z).getArea();
			}
		}
		
		GraphNode bg_gn,taz_gn;
		ArrayList<Point2D.Double> subject=new ArrayList<Point2D.Double>(); //BG
		ArrayList<Point2D.Double> clip=new ArrayList<Point2D.Double>(); //TAZ
		ArrayList<Point2D.Double> intersections=new ArrayList<Point2D.Double>();

		ArrayList<Point2D.Double> entrances=new ArrayList<Point2D.Double>(); //entering intersections
		ArrayList<Point2D.Double> exits=new ArrayList<Point2D.Double>(); //exiting intersections
		ArrayList<ArrayList<Point2D.Double>> clipped=new ArrayList<ArrayList<Point2D.Double>>(); //result of clipping
		double x_int, x1, x2, x3, x4, y_int, y1, y2, y3, y4;
		Line2D.Double bg_line=new Line2D.Double();
		Line2D.Double taz_line=new Line2D.Double();
		SortedMap<Integer,ArrayList<Point2D.Double>> intersects_clip=new TreeMap<Integer,ArrayList<Point2D.Double>>();
		int id_count=0;
		boolean first_intersection=true; //true-enter/false-exit
		
		long start = System.currentTimeMillis();

		for(Integer z:BGmap.keySet()){ //for every BG

			for(int y=0;y<BGmap.get(z).getAdjTAZ().size();y++){ //clip it with every TAZ it borders
				
				while(existParallelLines(z,y)){} //adjust points to avoid vertical lines, collinear lines, possible intersections near endpoints of segments
				
				bg_gn=BGmap.get(z);
				taz_gn=TAZmap.get(bg_gn.getAdjTAZ().get(y));
				
				for(int x=0;x<taz_gn.getPolygon().size();x++){//copy elements into clip
					clip.add(taz_gn.getPolygon().get(x));
				}
				for(int x=0;x<bg_gn.getPolygon().size()-1;x++){ //check every BG line segment
					int crossings=0; //number of intersections with a TAZ line segment for one BG line segment
					if(!subject.contains(bg_gn.getPolygon().get(x))){
						subject.add(bg_gn.getPolygon().get(x)); //can build subject list (BG) now, whereas clip list (TAZ) will have to wait
					}
					bg_line.setLine(bg_gn.getPolygon().get(x).getX(),bg_gn.getPolygon().get(x).getY(),bg_gn.getPolygon().get(x+1).getX(),bg_gn.getPolygon().get(x+1).getY());
					for(int w=0;w<taz_gn.getPolygon().size()-1;w++){ //against every TAZ line segment
						taz_line.setLine(taz_gn.getPolygon().get(w).getX(),taz_gn.getPolygon().get(w).getY(),taz_gn.getPolygon().get(w+1).getX(),taz_gn.getPolygon().get(w+1).getY());
						
						//if they intersect, find intersection
						if(bg_line.intersectsLine(taz_line)){ 
							x1=bg_line.x1; x2=bg_line.x2; x3=taz_line.x1; x4=taz_line.x2;
							y1=bg_line.y1; y2=bg_line.y2; y3=taz_line.y1; y4=taz_line.y2;
							x_int=determinant(determinant(x1, y1, x2, y2), (x1 - x2), determinant(x3, y3, x4, y4), (x3 - x4))/determinant(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
							y_int=determinant(determinant(x1, y1, x2, y2), (y1 - y2), determinant(x3, y3, x4, y4), (y3 - y4))/determinant(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
							Point2D.Double tmp = new Point2D.Double(x_int,y_int);
							
							//add intersection to subject list
							if(crossings>0){ //if there are more than one crossing, order is important
								boolean added=false;
								for(int v=crossings;v>0;v--){									
									if(tmp.distance(bg_gn.getPolygon().get(x))<subject.get(subject.size()-v).distance(bg_gn.getPolygon().get(x))){
										if(!subject.contains(tmp)){
											subject.add(subject.size()-v,tmp);
										}
										intersections.add(intersections.size()-v,tmp);
										added=true;
										break;
									}
								}
								if(!added){
									subject.add(tmp);
									intersections.add(tmp);
								}
							}else{
								if(!subject.contains(tmp)){
									subject.add(tmp);
								}
								intersections.add(tmp);
							}
							
							//add intersections to a holding list that will be combined with TAZ vertices to make clip list
							if(intersects_clip.containsKey(w+1)){ //order matters if there are multiple crossings
								for(int v=0;v<intersects_clip.get(w+1).size();v++){
									if(intersects_clip.get(w+1).contains(tmp)){
										break;
									}
									if(intersects_clip.get(w+1).get(v).distance(taz_gn.getPolygon().get(w))>tmp.distance(taz_gn.getPolygon().get(w))){
										intersects_clip.get(w+1).add(v,tmp);
										break;
									}
								}
								if(!intersects_clip.get(w+1).contains(tmp)){
									intersects_clip.get(w+1).add(tmp);
								}
							}else{
								intersects_clip.put(w+1, new ArrayList<Point2D.Double>());
								intersects_clip.get(w+1).add(tmp);
							}
							crossings++;
						}
					}
				}
				boolean warning = false;
				if(intersections.size()%2==1){
					//System.out.println("Intersections: "+intersections.size() +" "+bg_gn.getNodeID()+" "+bg_gn.getAdjTAZ().indexOf(taz_gn.getNodeID()));
					warning = true;
				}
				
				//determine whether first intersection is an entrance of exit
				if(intersections.size()>1){ 
					Point2D.Double prevPt;
					int find_inter=0;
					while(!intersections.contains(subject.get(find_inter))){ //find index of first intersection in subject
						find_inter++;
					}
					if(find_inter==0){
						find_inter=subject.size();
					}
					prevPt=subject.get(find_inter-1);
					if(taz_gn.getPolygonPath().contains(prevPt)){
						first_intersection=false;
					}else{
						first_intersection=true;
					}
				}
				
				//separate entrances and exits into separate lists
				for(int x=0;x<intersections.size();x++){
					if(first_intersection){
						entrances.add(intersections.get(x));
					}else{
						exits.add(intersections.get(x));
					}
					first_intersection=!first_intersection;
				}
		
				int added=0;
				for(Integer x:intersects_clip.keySet()){ //add points to clip
					ArrayList<Point2D.Double> toAdd = intersects_clip.get(x);
					for(int w=0;w<toAdd.size();w++){ //copy the points
						clip.add(x+added,toAdd.get(w));
						added++;
					}
				}
				
				
				//CLIP - Weiler-Atherton clipping algorithm
				clipped.clear();
				if(intersections.size()==0){//one polygon must be inside the other, otherwise would not be on adj list
					int check=0;
					for(int x=0;x<bg_gn.getPolygon().size();x++){ //bg in taz?
						if(taz_gn.getPolygonPath().contains(bg_gn.getPolygon().get(x))){
							check=check+1;
						}
					}
					for(int x=0;x<taz_gn.getPolygon().size();x++){ //taz in bg?
						if(bg_gn.getPolygonPath().contains(taz_gn.getPolygon().get(x))){
							check=check-1;
						}
					}
					if(check==bg_gn.getPolygon().size()){ //bg inside taz
						bg_gn.setTAZ(taz_gn.getTAZ());
						bg_gn.setNodeID(id_count);
						Outputmap.put(id_count, bg_gn);
						id_count++;
					}
					else if(check==-taz_gn.getPolygon().size()){ //taz inside bg
						taz_gn.setNodeID(id_count);
						taz_gn.setBG(bg_gn.getBG());
						taz_gn.setTract(bg_gn.getTract());
						Outputmap.put(id_count, taz_gn);
						id_count++;
					}
				}else if(intersections.size()==1){ //touches at a point, don't add anything
				}else{
					clipped=clip_polys(subject,clip,entrances,exits);
				}

				//add polygons
				for(int x=0;x<clipped.size();x++){
					Outputmap.put(id_count, new GraphNode(id_count,null,clipped.get(x),false,taz_gn.getNodeID(),bg_gn.getTract(),bg_gn.getBG()));
					
					//removal case: messed up polygon
					if(warning && Outputmap.get(id_count).getCentroid().distance(Outputmap.get(id_count).getPolygon().get(0))>2){
							Outputmap.remove(id_count);
							id_count--;
					}
					//removal case: too small
					else if(Outputmap.get(id_count).getArea()<smallestArea*areaFilter){
						Outputmap.remove(id_count);
						id_count--;
					}

					id_count++;
				}
				
				subject.clear();
				clip.clear();
				intersections.clear();
				entrances.clear();
				exits.clear();
				intersects_clip.clear();
			}	
		}
		System.out.println("Find intersections: "+(double)(System.currentTimeMillis()-start)/1000 +"secs");
	}
	
	public static ArrayList<ArrayList<Point2D.Double>> clip_polys(ArrayList<Point2D.Double> subj, ArrayList<Point2D.Double> cl, ArrayList<Point2D.Double> enter, ArrayList<Point2D.Double> exit){
		
		ArrayList<ArrayList<Point2D.Double>> results = new ArrayList<ArrayList<Point2D.Double>>();
		ArrayList<Point2D.Double> result;
		boolean[] visited = new boolean[enter.size()];
		for(int a=0;a<visited.length;a++){
			visited[a]=false;
		}
		
		int start;
		for(int z=0;z<enter.size();z++){ //for each entering intersection
			if(!visited[z]){ //if not visited yet, begin traversing from here
				result=new ArrayList<Point2D.Double>();
				//traverse subject until hit exiting intersection
				start=subj.indexOf(enter.get(z));
				visited[z]=true;
				while(true){
					while(!exit.contains(subj.get(start%subj.size()))){
						result.add(subj.get(start%subj.size()));
						start++;
					}
					//switch to clip, traverse from exiting intersection until reach entering intersection
					start=cl.indexOf(subj.get((start)%subj.size()));
					while(!enter.contains(cl.get(start%cl.size()))){
						result.add(cl.get(start%cl.size()));
						start++;
					}
					if(visited[enter.indexOf(cl.get(start%cl.size()))]){ //if entering intersection is visited, polygon finished
						break;
					}
					visited[enter.indexOf(cl.get(start%cl.size()))]=true;
					start=subj.indexOf(cl.get((start)%cl.size()));
				}
			result.add(cl.get(start%cl.size())); //close polygon
			results.add(result);
			}	
		}
		return results;
	}
	
	public static double determinant(double a, double b, double c, double d){
		return a*d-b*c;
	}
	public static boolean existParallelLines(int z, int y){
		boolean edit=false;
		GraphNode bg_gn,taz_gn;
		Line2D.Double bg_line=new Line2D.Double();
		Line2D.Double taz_line=new Line2D.Double();
		double x1, x2, x3, x4, y1, y2, y3, y4, slopebg, slopetaz, anglebg, angletaz;
		bg_gn=BGmap.get(z);
		taz_gn=TAZmap.get(bg_gn.getAdjTAZ().get(y));
		for(int x=0;x<bg_gn.getPolygon().size()-1;x++){ //check every BG line segment
			bg_line.setLine(bg_gn.getPolygon().get(x).getX(),bg_gn.getPolygon().get(x).getY(),bg_gn.getPolygon().get(x+1).getX(),bg_gn.getPolygon().get(x+1).getY());
			for(int w=0;w<taz_gn.getPolygon().size()-1;w++){ //against every TAZ line segment
				taz_line.setLine(taz_gn.getPolygon().get(w).getX(),taz_gn.getPolygon().get(w).getY(),taz_gn.getPolygon().get(w+1).getX(),taz_gn.getPolygon().get(w+1).getY());
				if(bg_line.intersectsLine(taz_line)){ //if they intersect, check for potential edge cases, adjust points until not near edge cases
					x1=bg_line.x1; x2=bg_line.x2; x3=taz_line.x1; x4=taz_line.x2;
					y1=bg_line.y1; y2=bg_line.y2; y3=taz_line.y1; y4=taz_line.y2;
					slopebg=(y2-y1)/(x2-x1); anglebg=(Math.atan(slopebg)+Math.PI/2)%Math.PI;
					slopetaz=(y4-y3)/(x4-x3); angletaz=(Math.atan(slopetaz)+Math.PI/2)%Math.PI;
					int count=0;

					while((Math.abs(x2-x1)<Math.pow(10,-5)) && (Math.abs(x3-x4)<Math.pow(10,-5))){ //vertical line
						bg_gn.getPolygon().get(x).setLocation(bg_gn.getPolygon().get(x).getX()+Math.pow(10,roundToPow)/5,bg_gn.getPolygon().get(x).getY());
						edit=true;
						count++;
						bg_line.setLine(bg_gn.getPolygon().get(x).getX(),bg_gn.getPolygon().get(x).getY(),bg_gn.getPolygon().get(x+1).getX(),bg_gn.getPolygon().get(x+1).getY());
						x1=bg_line.x1; x2=bg_line.x2; y1=bg_line.y1; y2=bg_line.y2;
						slopebg=(y2-y1)/(x2-x1); anglebg=(Math.atan(slopebg)+Math.PI/2)%Math.PI;
					}
					while(Math.abs(anglebg-angletaz)<Math.pow(10,-3)*5){ //collinear
						if(Math.abs(slopetaz)<1){
							bg_gn.getPolygon().get(x).setLocation(bg_gn.getPolygon().get(x).getX(),bg_gn.getPolygon().get(x).getY()-Math.pow(10,roundToPow)/2);
						}else{
							bg_gn.getPolygon().get(x).setLocation(bg_gn.getPolygon().get(x).getX()-Math.pow(10,roundToPow)/2,bg_gn.getPolygon().get(x).getY());
						}
						edit=true;
						count++;
						bg_line.setLine(bg_gn.getPolygon().get(x).getX(),bg_gn.getPolygon().get(x).getY(),bg_gn.getPolygon().get(x+1).getX(),bg_gn.getPolygon().get(x+1).getY());
						x1=bg_line.x1; x2=bg_line.x2; y1=bg_line.y1; y2=bg_line.y2;
						slopebg=(y2-y1)/(x2-x1); anglebg=(Math.atan(slopebg)+Math.PI/2)%Math.PI;
					}
					//chance for intersection to be close to an endpoint
					while(taz_line.ptLineDistSq(bg_gn.getPolygon().get(x))<Math.pow(10,-14) || taz_line.ptLineDistSq(bg_gn.getPolygon().get(x+1))<Math.pow(10,-14) || bg_line.ptLineDistSq(taz_gn.getPolygon().get(w))<Math.pow(10,-14) || bg_line.ptLineDistSq(taz_gn.getPolygon().get(w+1))<Math.pow(10,-14)){
						if(taz_line.ptLineDistSq(bg_gn.getPolygon().get(x))<Math.pow(10,-14)){
							bg_gn.getPolygon().get(x).setLocation(bg_gn.getPolygon().get(x).getX()+Math.pow(10, roundToPow),bg_gn.getPolygon().get(x).getY());
						}if(taz_line.ptLineDistSq(bg_gn.getPolygon().get(x+1))<Math.pow(10,-14)){
							bg_gn.getPolygon().get(x+1).setLocation(bg_gn.getPolygon().get(x+1).getX()+Math.pow(10, roundToPow),bg_gn.getPolygon().get(x+1).getY());
						}if(bg_line.ptLineDistSq(taz_gn.getPolygon().get(w))<Math.pow(10,-14)){
							taz_gn.getPolygon().get(w).setLocation(taz_gn.getPolygon().get(w).getX()-Math.pow(10, roundToPow),taz_gn.getPolygon().get(w).getY());
						}if(bg_line.ptLineDistSq(taz_gn.getPolygon().get(w+1))<Math.pow(10,-14)){
							taz_gn.getPolygon().get(w+1).setLocation(taz_gn.getPolygon().get(w+1).getX()-Math.pow(10, roundToPow),taz_gn.getPolygon().get(w+1).getY());
						}
						edit=true;
					}
				}
			}
		}
		return edit;
	}
	public static void adjustPoints(){ //check every polygon, get rid of duplicate points
		HashSet<Point2D.Double> pts = new HashSet<Point2D.Double>();
		for(Integer z:BGmap.keySet()){
			for(int y=1;y<BGmap.get(z).getPolygon().size();y++){
				if(pts.contains(BGmap.get(z).getPolygon().get(y))){ //duplicate exists
					if(BGmap.get(z).getPolygon().get(y).equals(BGmap.get(z).getPolygon().get(y-1))){ //duplicate of previous point
						BGmap.get(z).getPolygon().remove(y);
						y--;
					}else{ // possibly duplicate of point wrapping around
						boolean wraparound=true;
						for(int x=y+1;x<BGmap.get(z).getPolygon().size();x++){ //check if all points after current point until wraparound point are duplicate
							if(!pts.contains(BGmap.get(z).getPolygon().get(x))){
								wraparound=false;
							}
						}
						if(wraparound){ //wraps around
							BGmap.get(z).getPolygon().remove(y);
							y--;
						}else{ //maybe an extraneous loop
							Point2D.Double tmp = BGmap.get(z).getPolygon().get(y);
							if(BGmap.get(z).getPolygon().lastIndexOf(tmp)-BGmap.get(z).getPolygon().indexOf(tmp)<10){ //if the loop isn't too large, remove it
								for(int x=BGmap.get(z).getPolygon().lastIndexOf(tmp);x>BGmap.get(z).getPolygon().indexOf(tmp);x--){
									BGmap.get(z).getPolygon().remove(x);
								}
							}
						}
					}
				}else{
					pts.add(BGmap.get(z).getPolygon().get(y));
				}
			}
			pts.clear();
		}
		//identical, but for TAZs
		for(Integer z:TAZmap.keySet()){
			for(int y=1;y<TAZmap.get(z).getPolygon().size();y++){
				if(pts.contains(TAZmap.get(z).getPolygon().get(y))){
					if(TAZmap.get(z).getPolygon().get(y).equals(TAZmap.get(z).getPolygon().get(y-1))){
						TAZmap.get(z).getPolygon().remove(y);
						y--;
					}else{
						boolean wraparound=true;
						for(int x=y+1;x<TAZmap.get(z).getPolygon().size();x++){
							if(!pts.contains(TAZmap.get(z).getPolygon().get(x))){
								wraparound=false;
							}
						}
						if(wraparound){
							TAZmap.get(z).getPolygon().remove(y);
							y--;
						}else{
							Point2D.Double tmp = TAZmap.get(z).getPolygon().get(y);
							if(TAZmap.get(z).getPolygon().lastIndexOf(tmp)-TAZmap.get(z).getPolygon().indexOf(tmp)<10){
								for(int x=TAZmap.get(z).getPolygon().lastIndexOf(tmp);x>TAZmap.get(z).getPolygon().indexOf(tmp);x--){
									TAZmap.get(z).getPolygon().remove(x);
								}
							}
						}
					}
				}else{
					pts.add(TAZmap.get(z).getPolygon().get(y));
				}
			}
			pts.clear();
		}
	}
	public static void buildInterAdj(){
		long start = System.currentTimeMillis();
		
		HashMap<Point2D.Double,ArrayList<Integer>> BGptsMap = new HashMap<Point2D.Double,ArrayList<Integer>>();
		HashMap<Point2D.Double,ArrayList<Integer>> TAZptsMap = new HashMap<Point2D.Double,ArrayList<Integer>>();
		
		ArrayList<GraphNode> bg = new ArrayList<GraphNode>();
		ArrayList<GraphNode> taz = new ArrayList<GraphNode>();
		
		int processingBG = 0; //current node being processed
		int processingTAZ= 0;
		int addNeighborsOfBG=0; //the node that needs neighbors to be added
		int addNeighborsOfTAZ=0;
		
		bg.add(BGmap.get(BGmap.keySet().iterator().next())); //add first node to BG ArrayList
		taz.add(TAZmap.get(TAZmap.keySet().iterator().next()));
		
		Point2D.Double pt;
		GraphNode bg_gn; GraphNode taz_gn;
		int TAZ_prevPt=0;
		int BG_prevPt=0;
		
		int notinTAZ=0;
		int notinBG=0;
		int largestsearch=0;
		
		ArrayList<GraphNode> TAZsearch = new ArrayList<GraphNode>();
		ArrayList<GraphNode> BGsearch = new ArrayList<GraphNode>();
		ArrayList<Point2D.Double> pts;
		
		pt=bg.get(processingBG).getPolygon().get(0); //get first point
		for(Integer z:TAZmap.keySet()){ //find TAZ corresponding to it
			taz_gn=TAZmap.get(z);
			if(taz_gn.getPolygonPath().contains(pt)){
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				tmp.add(taz_gn.getNodeID());
				//update HashMaps
				TAZmap.get(taz_gn.getNodeID()).addAdjBG(bg.get(processingBG).getNodeID());
				BGmap.get(bg.get(processingBG).getNodeID()).addAdjTAZ(taz_gn.getNodeID());
				TAZ_prevPt=taz_gn.getNodeID();
				BGptsMap.put(pt, tmp);
			}
		}
		
		while(processingBG<BGmap.size() && addNeighborsOfBG<BGmap.size()){ //until all nodes are processed
			bg_gn = bg.get(processingBG);
			
			pts=bg.get(processingBG).getPolygon();
			for(int z=0;z<pts.size();z++){ //for each point
				pt=pts.get(z);
				
				if(!BGptsMap.containsKey(pt)){
					int neighborOfTAZ=0;
					if(TAZsearch.size()==0){ //if no TAZs to search, add one based on past
						TAZsearch.add(TAZmap.get(TAZ_prevPt));
					}
					for(int y=0;y<TAZsearch.size();y++){ //check against every TAZ in TAZsearch
						taz_gn=TAZsearch.get(y);
						ArrayList<Integer> tmp;
						if(taz_gn.getPolygonPath().contains(pt)){ //if the TAZ contains the point
							if(TAZsearch.size()>largestsearch)
								largestsearch=TAZsearch.size();
							if(!BGptsMap.containsKey(pt)){	
								tmp = new ArrayList<Integer>();
								tmp.add(taz_gn.getNodeID());
							}else{
								tmp = BGptsMap.get(pt);
								if(!tmp.contains(taz_gn.getNodeID()))
									tmp.add(taz_gn.getNodeID());
							}
							BGptsMap.put(pt, tmp);
							//update HashMaps
							TAZmap.get(taz_gn.getNodeID()).addAdjBG(bg_gn.getNodeID());
							BGmap.get(bg_gn.getNodeID()).addAdjTAZ(taz_gn.getNodeID());
							TAZ_prevPt=taz_gn.getNodeID();

							TAZsearch.clear();	
						}
						else if(y==TAZsearch.size()-1 && neighborOfTAZ<TAZmap.size()/5){	//if out of TAZs to look in, add neighbors
							while(y==TAZsearch.size()-1 && neighborOfTAZ<TAZsearch.size()){ //if searched through many TAZs, likely not in a TAZ
								TAZsearch=addNeighbors(TAZsearch,neighborOfTAZ,true);
								neighborOfTAZ++;
							}
						}
						if(y==TAZsearch.size()-1){ //if no neighbors were added, finish for point
							notinTAZ++;
							
							TAZsearch.clear();
							if(!BGptsMap.containsKey(pt)){
								tmp = new ArrayList<Integer>();
								tmp.add(-1);
								BGptsMap.put(pt, tmp);
							}
						}
					}
				}
				else{ //just add the adjacencies because already found the point
					ArrayList<Integer> tazs = BGptsMap.get(pt);
					for(int a=0;a<tazs.size();a++){
						if(tazs.get(a)!=-1){
							TAZmap.get(tazs.get(a)).addAdjBG(bg_gn.getNodeID());
							BGmap.get(bg_gn.getNodeID()).addAdjTAZ(tazs.get(a));
						}
					}
				}
			}
			//finished with all points for one BG
			//if no more BGs to process, add neighbors
			if(processingBG==bg.size()-1){
				while(processingBG==bg.size()-1 && addNeighborsOfBG<bg.size()){
					bg=addNeighbors(bg,addNeighborsOfBG,false);
					addNeighborsOfBG++;
				}
			}
			if(processingBG<bg.size()-1){ //move to next point
			
				processingBG++;
				if(!bg_gn.getAdjBG().contains(bg.get(processingBG).getNodeID())){ //if the new BG does not neighbor the previous BG, redo prev_TAZ
					double dist=-1;
					for(Integer i : bg.get(processingBG).getAdjBG()){
						if(BGmap.get(i).getAdjTAZ().size() > 0){
							for(Integer j : BGmap.get(i).getAdjTAZ()){
								if(bg.get(processingBG).getCentroid().distanceSq(TAZmap.get(j).getCentroid()) < dist || dist<0){
									TAZ_prevPt=TAZmap.get(j).getNodeID();
									dist=bg.get(processingBG).getCentroid().distanceSq(TAZmap.get(j).getCentroid());
								}
							}
						}
					}
				}
			}	
		}
		
		//Reverse: TAZ pts to BG
		largestsearch=0;		
		pt=taz.get(processingTAZ).getPolygon().get(0); //get first point
		for(Integer z:BGmap.keySet()){ //find TAZ corresponding to it
			bg_gn=BGmap.get(z);
			if(bg_gn.getPolygonPath().contains(pt)){
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				tmp.add(bg_gn.getNodeID());
				//update HashMaps
				BGmap.get(bg_gn.getNodeID()).addAdjTAZ(taz.get(processingTAZ).getNodeID());
				TAZmap.get(taz.get(processingTAZ).getNodeID()).addAdjBG(bg_gn.getNodeID());
				BG_prevPt=bg_gn.getNodeID();
				TAZptsMap.put(pt, tmp);
			}
		}
		
		while(processingTAZ<TAZmap.size() && addNeighborsOfTAZ<TAZmap.size()){ //until all nodes are processed
			taz_gn = taz.get(processingTAZ);
			pts=taz.get(processingTAZ).getPolygon();
			for(int z=0;z<pts.size();z++){ //for each point
				pt=pts.get(z);
				if(!TAZptsMap.containsKey(pt)){
					int neighborOfBG=0;
					if(BGsearch.size()==0){ //if no TAZs to search, add one based on past
						BGsearch.add(BGmap.get(BG_prevPt));
					}
					for(int y=0;y<BGsearch.size();y++){ //check against every TAZ in TAZsearch
						bg_gn=BGsearch.get(y);
						ArrayList<Integer> tmp;
						if(bg_gn.getPolygonPath().contains(pt)){ //if the TAZ contains the point
							if(BGsearch.size()>largestsearch)
								largestsearch=BGsearch.size();
							if(!TAZptsMap.containsKey(pt)){	
								tmp = new ArrayList<Integer>();
								tmp.add(bg_gn.getNodeID());
							}else{
								tmp = TAZptsMap.get(pt);
								if(!tmp.contains(bg_gn.getNodeID()))
									tmp.add(bg_gn.getNodeID());
							}
							TAZptsMap.put(pt, tmp);
							//update HashMaps
							BGmap.get(bg_gn.getNodeID()).addAdjTAZ(taz_gn.getNodeID());	
							TAZmap.get(taz_gn.getNodeID()).addAdjBG(bg_gn.getNodeID());
							BG_prevPt=bg_gn.getNodeID();
							BGsearch.clear();
						}
						else if(y==BGsearch.size()-1 && neighborOfBG<BGmap.size()/5){	//if out of TAZs to look in, add neighbors
							while(y==BGsearch.size()-1 && neighborOfBG<BGsearch.size()){ //if searched through many TAZs, likely not in a TAZ
								BGsearch=addNeighbors(BGsearch,neighborOfBG,false);
								neighborOfBG++;
							}
						}
						if(y==BGsearch.size()-1){ //if no neighbors were added, finish for point
							notinBG++;
							BGsearch.clear();
							if(!TAZptsMap.containsKey(pt)){
								tmp = new ArrayList<Integer>();
								TAZptsMap.put(pt, tmp);
							}
						}
					}
				}else{ //just add the adjacencies because already found the point
					ArrayList<Integer> bgs = TAZptsMap.get(pt);
					for(int a=0;a<bgs.size();a++){
						if(bgs.get(a)!=-1){
							BGmap.get(bgs.get(a)).addAdjTAZ(taz_gn.getNodeID());
							TAZmap.get(taz_gn.getNodeID()).addAdjBG(bgs.get(a));
						}
					}
				}	
			}
			//finished with all points for one BG
			//if no more BGs to process, add neighbors
			if(processingTAZ==taz.size()-1){
				while(processingTAZ==taz.size()-1 && addNeighborsOfTAZ<taz.size()){
					taz=addNeighbors(taz,addNeighborsOfTAZ,true);
					addNeighborsOfTAZ++;
				}
			}
			if(processingTAZ<taz.size()-1){ //move to next point
				processingTAZ++;
				if(!taz_gn.getAdjTAZ().contains(taz.get(processingTAZ).getNodeID())){ //if the new BG does not neighbor the previous BG, redo prev_TAZ
					double dist=-1;
					for(Integer i : taz.get(processingTAZ).getAdjTAZ()){
						if(TAZmap.get(i).getAdjBG().size() > 0){
							for(Integer j : TAZmap.get(i).getAdjBG()){
								if(taz.get(processingTAZ).getCentroid().distanceSq(BGmap.get(j).getCentroid()) < dist || dist<0){
									BG_prevPt=BGmap.get(j).getNodeID();
									dist=taz.get(processingTAZ).getCentroid().distanceSq(BGmap.get(j).getCentroid());
								}
							}
						}
					}
				}
			}	
		}
		System.out.println("Find inter-adjacencies: "+(double)(System.currentTimeMillis()-start)/1000 +"secs");

	}
	public static ArrayList<GraphNode> addNeighbors(ArrayList<GraphNode> nodes, int whichNode, boolean taz){
		GraphNode gn = nodes.get(whichNode);
		ArrayList<Integer> neighbors;
		if(taz){
			neighbors=gn.getAdjTAZ();
			for(int z=0;z<neighbors.size();z++){
				if(!nodes.contains(TAZmap.get(neighbors.get(z)))){
					nodes.add(TAZmap.get(neighbors.get(z)));
				}
			}
		}else{
			neighbors=gn.getAdjBG();
			for(int z=0;z<neighbors.size();z++){
				if(!nodes.contains(BGmap.get(neighbors.get(z)))){
					nodes.add(BGmap.get(neighbors.get(z)));
				}
			}
		}
		return nodes;
	}
	public static void buildAdj(HashMap<Integer,GraphNode> nodes, boolean taz){
		//add points into map, if the map already has it, then there is an adjacency
		HashMap<Point2D.Double,ArrayList<Integer>> adj=new HashMap<Point2D.Double,ArrayList<Integer>>(); //store the node index in nodes
		ArrayList<Point2D.Double> pts;
		Point2D.Double tmpPt;
		GraphNode tmpNode;
		ArrayList<Integer> adjlist;
		int numPts=0;
		for(Integer z:nodes.keySet()){
			tmpNode=nodes.get(z);
			pts=tmpNode.getPolygon();
			for(int y=0;y<pts.size()-1;y++){
				numPts++;
				tmpPt=pts.get(y);
				if(!adj.containsKey(tmpPt))
				{
					ArrayList<Integer> val = new ArrayList<Integer>();
					val.add(z);
					adj.put(tmpPt, val);
				}else{
					adjlist=adj.get(tmpPt);
					for(int x=0;x<adjlist.size();x++){
						if(taz){
							nodes.get(adjlist.get(x)).addAdjTAZ(tmpNode.getNodeID());
							nodes.get(z).addAdjTAZ(nodes.get(adjlist.get(x)).getNodeID());
						}else{
							nodes.get(adjlist.get(x)).addAdjBG(tmpNode.getNodeID());
							nodes.get(z).addAdjBG(nodes.get(adjlist.get(x)).getNodeID());
						}
					}
					if(!adjlist.contains(z))
						adjlist.add(z);
					adj.put(tmpPt, adjlist);
				}
			}
		}
	}
	public static void graphNodeBuilder(int nodeID, Point2D.Double centroid, int taz, String tract, int bg, Element polygon, boolean isTAZ){
		String[] yxz2, coordLst; String coords;
		Namespace ns = polygon.getNamespace();
		//parse polygon
		double round=Math.pow(10, -roundToPow);
		double offset = isTAZ ? Math.pow(10,roundToPow)/2 : 0;
		ArrayList<Point2D.Double> pts1 = new ArrayList<Point2D.Double>();
		coords = polygon.getChild("outerBoundaryIs",ns).getChild("LinearRing",ns).getChildText("coordinates",ns).trim();
		coordLst = coords.split(" ");
		for(int coun = 0; coun < coordLst.length; coun++){
			yxz2 = coordLst[coun].split(",");
			pts1.add(new Point2D.Double((Math.round(Double.parseDouble(yxz2[0])*round))/round+offset, (Math.round(Double.parseDouble(yxz2[1])*round))/round+offset));	
		}
		//special case: inner boundary
		if(polygon.getChild("innerBoundaryIs",ns)!=null){
			double minDistSq=1000;
			int BGindex1 = 0; int BGindex2=0;
			coords = polygon.getChild("innerBoundaryIs",ns).getChild("LinearRing",ns).getChildText("coordinates",ns).trim();
			coordLst=coords.split(" ");
			ArrayList<Point2D.Double> innerpts = new ArrayList<Point2D.Double>(); //get inner boundary points
			for(int coun = 0; coun < coordLst.length; coun++){
				yxz2 = coordLst[coun].split(",");
				innerpts.add(new Point2D.Double((Math.round(Double.parseDouble(yxz2[0])*round))/round+offset, (Math.round(Double.parseDouble(yxz2[1])*round))/round+offset));	
			}
			
			//find closest pair
			SortedMap<Double,Point> distances = new TreeMap<Double,Point>();
			for(int z=0;z<pts1.size();z++){ //outer index
				for(int y=0;y<innerpts.size();y++){ //inner index
					distances.put(pts1.get(z).distanceSq(innerpts.get(y)),new Point(z,y));
				}
			}
			int tmp;
			int out1=distances.get(distances.firstKey()).x; //pts1
			int in1=distances.get(distances.firstKey()).y; //pts2
			
			//find next closest pair not sharing a vertex
			distances.clear();
			for(int z=0;z<pts1.size();z++){ //outer index
				for(int y=0;y<innerpts.size();y++){ //inner index
					if(z!=out1&&y!=in1)
						distances.put(pts1.get(z).distanceSq(innerpts.get(y)),new Point(z,y));
				}
			}
			int out2=distances.get(distances.firstKey()).x; //pts1 
			int in2=distances.get(distances.firstKey()).y; //pts2
			
			//make two new polygons
			ArrayList<Point2D.Double> newpoly1 = new ArrayList<Point2D.Double>();
			ArrayList<Point2D.Double> newpoly2 = new ArrayList<Point2D.Double>();
			
			//polygon 1
			newpoly1.add(innerpts.get(in1));
			for(tmp=out1;tmp!=out2;tmp=(tmp+1)%pts1.size()){
				newpoly1.add(pts1.get(tmp));
			}
			newpoly1.add(pts1.get(out2));
			for(tmp=in2;tmp!=in1;tmp=(tmp+1)%innerpts.size()){
				newpoly1.add(innerpts.get(tmp));
			}
			newpoly1.add(innerpts.get(in1));
			
			//polygon 2
			newpoly2.add(pts1.get(out1));
			for(tmp=in1;tmp!=in2;tmp=(tmp+1)%innerpts.size()){
				newpoly2.add(innerpts.get(tmp));
			}
			newpoly2.add(innerpts.get(in2));
			for(tmp=out2;tmp!=out1;tmp=(tmp+1)%pts1.size()){
				newpoly2.add(pts1.get(tmp));
			}
			newpoly2.add(pts1.get(out1));
			
			if(isTAZ){
				TAZmap.put(nodeID,new GraphNode(nodeID, null,newpoly1,isTAZ,taz,"-1",-1));
				TAZmap.put((nodeID+2000),new GraphNode(nodeID+2000, null,newpoly2,isTAZ,taz,"-1",-1));
				
			}else{
				BGmap.put(nodeID,new GraphNode(nodeID, null,newpoly1,isTAZ,-1,tract,bg));
				BGmap.put((nodeID+2000),new GraphNode(nodeID+2000,null,newpoly2,isTAZ,-1,tract,bg));
			}
			
		}else{ //no inner boundary
			if(isTAZ){
				TAZmap.put(nodeID,new GraphNode(nodeID,centroid,pts1,isTAZ,taz,"-1",-1));
			}else{
				BGmap.put(nodeID,new GraphNode(nodeID,centroid,pts1,isTAZ,-1,tract,bg));
			}
		}
	}
	public static void readKML(String path,boolean isTAZ){
		
		SAXBuilder build = new SAXBuilder(); Document d = null;
		try {d = build.build(new File(path));}catch(Exception e){e.printStackTrace();}
		
		Element root = d.getRootElement();
		Namespace ns = root.getNamespace();
		List<Element> centroids = root.getChild("Document",ns).getChild("Folder",ns).getChildren("Placemark",ns);
		List<Element> outlines = root.getChild("Document",ns).getChildren("Placemark",ns);
		Iterator<Element> itercent = centroids.iterator(); Iterator<Element> iterout = outlines.iterator();
		Element tmp1, tmp2;
		int id1;
		String[] yxz1; String coordinate;
		String description; String tract="-1"; int bg=-1, taz=-1;
		while(itercent.hasNext() && iterout.hasNext()){
			tmp1=itercent.next(); tmp2=iterout.next();
			
			//parse nodeID
			id1 = Integer.parseInt(tmp1.getChildText("name",ns));
			
			//parse census tract, block group, or taz
			if(!isTAZ){
				String[] split1, split2;
				description=tmp2.getChildText("description",ns);
				split1=description.split("</td><td>");
				split2=split1[1].split("</td></tr>");
				tract=split2[0];
				split2=split1[2].split("</td></tr>");
				bg=Integer.parseInt(split2[0]);
			}else{
				taz=id1;
			}
			
			//parse centroid
			coordinate = tmp1.getChild("Point",ns).getChildText("coordinates",ns).trim();
			yxz1 = coordinate.split(",");
			Point2D.Double centroid = new Point2D.Double(Double.parseDouble(yxz1[0]),Double.parseDouble(yxz1[1]));
			
			//make a GraphNode for each <Polygon>
			if(tmp2.getChild("MultiGeometry",ns)!=null){
				int count = 0;
				Iterator<Element> it = tmp2.getChild("MultiGeometry",ns).getChildren("Polygon",ns).iterator();
				while(it.hasNext()){
					graphNodeBuilder(id1+2000*count, null, taz, tract, bg, it.next(), isTAZ);
					count++;
				}
			}else{
				graphNodeBuilder(id1, centroid, taz, tract, bg,tmp2.getChild("Polygon",ns), isTAZ);
			}
		}
	}

	public static void writeToKML(Map<Integer,GraphNode> nodes, String path, boolean taz){
		Element root = new Element("kml");
		root.setNamespace(Namespace.getNamespace(null,"http://www.opengis.net/kml/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("gx","http://www.google.com/kml/ext/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("kml","http://www.opengis.net/kml/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("atom","http://www.w3.org/2005/Atom"));
		
		Document doc = new Document(root);
		
		Element document = new Element("Document");
		root.addContent(document);
		
		Element style1 = new Element("Style");
		style1.setAttribute("id","shp_centerpoint");
		document.addContent(style1);
		
		Element iconstyle = new Element("IconStyle");
		style1.addContent(iconstyle);
		
		Element scale1 = new Element("scale");
		scale1.setText("0.8");
		iconstyle.addContent(scale1);
		
		Element icon = new Element("Icon");
		iconstyle.addContent(icon);
		
		Element href = new Element("href");
		href.setText("http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
		icon.addContent(href);
		
		Element labelstyle = new Element("LabelStyle");
		style1.addContent(labelstyle);
		
		Element scale2 = new Element("scale");
		scale2.setText("1");
		labelstyle.addContent(scale2);
		
		Element color1 = new Element("color");
		color1.setText("FFFFFFFF");
		labelstyle.addContent(color1);
		
		Element style2 = new Element("Style");
		style2.setAttribute("id","trb2");
		document.addContent(style2);
		
		Element linestyle = new Element("LineStyle");
		style2.addContent(linestyle);
		
		Element color2 = new Element("color");
		if(taz){
			color2.setText("80FD38A3");
		}else{
			color2.setText("80D1E6FE");
		}
		linestyle.addContent(color2);
		
		Element width = new Element("width");
		width.setText("1");
		linestyle.addContent(width);
		
		Element polystyle = new Element("PolyStyle");
		style2.addContent(polystyle);
		
		Element color3 = new Element("color");
		if(taz){
			color3.setText("40ff0000");
		}else{
			color3.setText("400000ff");
		}
		polystyle.addContent(color3);
		
		Element fill = new Element("fill");
		fill.setText("1");
		polystyle.addContent(fill);
		
		Element filename = new Element("name");
		String[] split = path.split("/");
		String tmp = split[split.length-1];
		filename.setText(tmp.substring(0,tmp.length()-4));
		document.addContent(filename);
		
		Element folder = new Element("Folder");
		document.addContent(folder);
		
		Element name1 = new Element("name");
		name1.setText("Labels");
		folder.addContent(name1);
		
		Element description1 = new Element("description");
		folder.addContent(description1);
		
		Element open1 = new Element("open");
		open1.setText("0");
		folder.addContent(open1);
		
		GraphNode gn;
		for(Integer z:nodes.keySet()){
			gn = nodes.get(z);
			Element placemark = new Element("Placemark");
			placemark.setAttribute("id",gn.getNodeID()+"lbl");
			folder.addContent(placemark);
			
			Element snippet = new Element("Snippet");
			snippet.setAttribute("maxLines","0");
			placemark.addContent(snippet);
			
			Element name = new Element("name");
			name.setText(gn.getNodeID()+"");
			placemark.addContent(name);
			
			Element description = new Element("description");
			description.setText(gn.getTAZ()+" "+gn.getTract()+" "+gn.getBG());
			placemark.addContent(description);
			
			Element visibility = new Element("visibility");
			visibility.setText("1");
			placemark.addContent(visibility);
			
			Element open = new Element("open");
			open.setText("0");
			placemark.addContent(open);
			
			Element point = new Element("Point");
			placemark.addContent(point);
			
			Element extrude = new Element("extrude");
			extrude.setText("0");
			point.addContent(extrude);
			
			Element tess = new Element("tessalate");
			tess.setText("1");
			point.addContent(tess);
			
			Element alt = new Element("altitudeMode");
			alt.setText("clampedToGround");
			point.addContent(alt);
			
			Element coord = new Element("coordinates");
			coord.setText(gn.getCentroid().getX()+","+gn.getCentroid().getY()+",0");
			point.addContent(coord);
			
			Element styleUrl = new Element("styleUrl");
			styleUrl.setText("#shp_centerpoint");
			placemark.addContent(styleUrl);
			
		}
		for(Integer z:nodes.keySet()){
			gn = nodes.get(z);
			Element placemark = new Element("Placemark");
			placemark.setAttribute("id",gn.getNodeID()+"");
			document.addContent(placemark);
			
			Element snippet = new Element("Snippet");
			snippet.setAttribute("maxLines","0");
			placemark.addContent(snippet);
			
			Element name = new Element("name");
			name.setText(gn.getNodeID()+"");
			placemark.addContent(name);
			
			Element description = new Element("description");
			description.setText(gn.getTAZ()+" "+gn.getTract()+" "+gn.getBG());
			placemark.addContent(description);
			
			Element visibility = new Element("visibility");
			visibility.setText("1");
			placemark.addContent(visibility);
			
			Element open = new Element("open");
			open.setText("0");
			placemark.addContent(open);
			
			Element polygon = new Element("Polygon");
			placemark.addContent(polygon);
			
			Element extrude = new Element("extrude");
			extrude.setText("1");
			polygon.addContent(extrude);
			
			Element tess = new Element("tessalate");
			tess.setText("1");
			polygon.addContent(tess);
			
			Element alt = new Element("altitudeMode");
			alt.setText("clampedToGround");
			polygon.addContent(alt);
			
			Element outerb = new Element("outerBoundaryIs");
			polygon.addContent(outerb);
			
			Element linearring = new Element("LinearRing");
			outerb.addContent(linearring);
			
			Element coord = new Element("coordinates");
			String s ="";
			ArrayList<Point2D.Double> pts=gn.getPolygon();
			for(int y=0;y<pts.size();y++){
				s=s+pts.get(y).getX()+","+pts.get(y).getY()+",0 ";
			}
			coord.setText(s.substring(0,s.length()-1));
			linearring.addContent(coord);
			
			Element styleUrl = new Element("styleUrl");
			styleUrl.setText("#trb2");
			placemark.addContent(styleUrl);
		}
		
		XMLOutputter outputter = new XMLOutputter();
	    try {
	      outputter.output(doc, new FileOutputStream(path));       
	    }
	    catch (IOException e) {
	      System.err.println(e);
	    }
	}
}
