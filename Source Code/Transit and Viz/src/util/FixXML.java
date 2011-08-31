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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.opentripplanner.api.ws.XMLFilter;

public class FixXML {
	
	static HashMap<Integer,String> map = new HashMap<Integer,String>();
	public static void main(String[] args){
		try{
			/*//make map of id to 7-digit census tract/block group identifier
			SAXBuilder kml = new SAXBuilder();
			Document doc = null;
			doc = kml.build(new File("C:/Summer2011/King_Data/Clipping/king_bg.kml"));
			Element kmlroot = doc.getRootElement();
			List<Element> markers = kmlroot.getChild("Document").getChildren("Placemark");
			for(Element e : markers){
				String[] split = e.getChildText("description").split(" ");
				String tractbg = split[1]+split[2];
				Integer taz = Integer.parseInt(e.getChildText("name"));
				map.put(taz,tractbg);
			}*/
			/*//print out mapping of id to 7-digit census tract/block group identifier
			int count=0;
			for(Integer z:map.keySet()){
				System.out.print(z+"\t"+map.get(z));
				if(((count+1)%7)==0){
					System.out.print("\n");
				}else{
					System.out.print("\t\t");
				}
				count++;
			}
			*/

			/*//Fixes XML filename that were not named with the 7-digit census tract/block group identifier
			  //Fixes destination attributes to be 7-digit census tract/block group identifier
			File dir = new File("C:/Summer2011/King_Data/OTPOutput/OopsBG");
			File[] files = dir.listFiles(new XMLFilter());
			System.out.println(files.length);
			
			for(File f : files){
				SAXBuilder build = new SAXBuilder();
		        Document d = null;
		        d = build.build(f);
		        Element root = d.getRootElement();
		        String filename = root.getChild("zone").getAttributeValue("bg");
		        System.out.println("TAZ-"+filename+".xml");
		        List<Element> destinations = root.getChild("destinations").getChildren("destination");
		        for(Element e : destinations){
					e.setAttribute("taz",map.get(Integer.parseInt(e.getAttributeValue("taz"))));
				}
		        XMLOutputter out = new XMLOutputter();
				out.setFormat(Format.getPrettyFormat());
				out.output(d, new FileOutputStream("C:/Summer2011/King_Data/OTPOutput/OopsBG/TAZ-"+filename+".xml")); 
			}
			
			*/
			
			//fixes AKDT time zone to EDT
			File dir = new File("C:/Summer2011/King_Data/OTPOutput/BG");
			File[] files = dir.listFiles(new XMLFilter());
			System.out.println(files.length);
			int count=0;
			for(File f : files){
				boolean write=false;
				SAXBuilder build = new SAXBuilder();
		        Document d = null;
		        d = build.build(f);
		        Element root = d.getRootElement();
		        List<Element> destinations = root.getChild("destinations").getChildren("destination");
		        for(Element e : destinations){
		        	if(e.getChild("start_time").getAttribute("val").getValue().contains("AKDT")){
		        		e.getChild("start_time").setAttribute("val",e.getChild("start_time").getAttribute("val").getValue().replace("AKDT","EDT"));
		        		e.getChild("end_time").setAttribute("val",e.getChild("end_time").getAttribute("val").getValue().replace("AKDT","EDT"));
		        		write=true;
		        	}
		        	else{
		        		break;
		        	}
				}
		        if(write){
		        	XMLOutputter out = new XMLOutputter();
		        	out.setFormat(Format.getPrettyFormat());
		        	System.out.println("C:/Summer2011/King_Data/OTPOutput/BG/"+f.getName());
		        	out.output(d, new FileOutputStream("C:/Summer2011/King_Data/OTPOutput/BG/"+f.getName()));
		        	count++;
		        }
			}
			System.out.println("Files written: "+count);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
