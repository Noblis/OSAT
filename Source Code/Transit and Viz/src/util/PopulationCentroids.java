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

package util;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import preotp.GraphNode;

public class PopulationCentroids {

	/**
	 * @param args
	 */
	static HashMap<Integer,GraphNode> popcentroids = new HashMap<Integer,GraphNode>(); 
	
	public static void main(String[] args) {
		try{
			int count = 0;
			//FileInputStream fstream = new FileInputStream("C:/Summer2011/DC_Data/CensusData/CenPop2010_Mean_BG11.txt");
			FileInputStream fstream = new FileInputStream("C:/Summer2011/King_Data/CensusData/CenPop2010_Mean_BG53.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s;
			String[] split;
			s=br.readLine();
			split=s.split(",");
			int latIndex=0, lonIndex=0, tractIndex=0,bgIndex=0,countyIndex=0;
			//parse first line
			for(int z=0;z<split.length;z++){
				if(split[z].equals("LATITUDE")){
					lonIndex=z;
				}
				if(split[z].equals("LONGITUDE")){
					latIndex=z;
				}
				if(split[z].equals("TRACTCE")){
					tractIndex=z;
				}
				if(split[z].equals("BLKGRPCE")){
					bgIndex=z;
				}
				if(split[z].equals("COUNTYFP")){
					countyIndex=z;
				}
			}
			//read data
			while((s = br.readLine()) != null){
				split = s.split(",");
				//if(split[countyIndex].equals("001")){
				if(split[countyIndex].equals("033")){
					popcentroids.put(count,new GraphNode(count, new Point2D.Double(Double.parseDouble(split[latIndex]),Double.parseDouble(split[lonIndex])), null, false, -1, split[tractIndex], Integer.parseInt(split[bgIndex])));
					count++;
				}
			}
		in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//writeKML("C:/Summer2011/DC_Data/CensusData/CenPop2010_Mean_BG11.kml");
		writeKML("C:/Summer2011/King_Data/CensusData/CenPop2010_Mean_BG53.kml");
	}
	public static void writeKML(String path){
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
		href.setText("http://maps.google.com/mapfiles/kml/paddle/red-circle.png");
		icon.addContent(href);
		
		Element labelstyle = new Element("LabelStyle");
		style1.addContent(labelstyle);
		
		Element scale2 = new Element("scale");
		scale2.setText("1");
		labelstyle.addContent(scale2);
		
		Element color1 = new Element("color");
		color1.setText("FFFFFFFF");
		labelstyle.addContent(color1);
		
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
		for(Integer z:popcentroids.keySet()){
			gn = popcentroids.get(z);
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
			
			XMLOutputter outputter = new XMLOutputter();
		    try {
		      outputter.output(doc, new FileOutputStream(path));       
		    }
		    catch (IOException e) {
		      System.err.println(e);
		    }
		}
	}
}
