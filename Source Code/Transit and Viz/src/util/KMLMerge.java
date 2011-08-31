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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.*;
import org.jdom.output.XMLOutputter;

public class KMLMerge {

	/**
	 * merge KML files created by Shape2Earth
	 * args: paths to files to be merged
	 */
	
	public static void main(String[] args) {
		
		//read config file
		SAXBuilder build = new SAXBuilder(); Document d = null;
		try {d = build.build(new File(args[0]));}catch(Exception e){e.printStackTrace();}	
		Element root = d.getRootElement();
		ArrayList<String> files = new ArrayList<String>();
		List<Element> merge = root.getChildren("merge");
		Iterator<Element> it = merge.iterator();
		while(it.hasNext()){
			files.add((it.next().getText()));
		}
		String outputpath = root.getChild("output").getText();
		
		
		int numFiles = files.size();
		int a,b;
		SAXBuilder[] sbs = new SAXBuilder[numFiles];
		Document[] docs = new Document[numFiles];
		Element[] roots = new Element[numFiles];
		Element[] folders = new Element[numFiles-1];
		Element[] placemarks = new Element[numFiles-1];
		List<Element>[] centroids = new List[numFiles-1];
		List<Element>[] polygons = new List[numFiles-1];
		Namespace ns = null;
		
		//initialize
		for(a=0;a<numFiles;a++){
			sbs[a]=new SAXBuilder();
			docs[a]=null;
		}
		try {
			for(a=0;a<numFiles;a++){
				docs[a]=sbs[a].build(files.get(a));
				roots[a]=docs[a].getRootElement();
				ns=roots[a].getNamespace();
				if(a != 0){	//add data from subsequent kml files into first file, need to use cloneContent() 
					folders[a-1]=roots[a].getChild("Document",ns).getChild("Folder",ns);
					centroids[a-1]=folders[a-1].cloneContent();
					placemarks[a-1]=roots[a].getChild("Document",ns);
					polygons[a-1]=placemarks[a-1].cloneContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<Element> centroids0 = roots[0].getChild("Document",ns).getChild("Folder",ns).getChildren("Placemark",ns); //first kml file
		List<Element> polygons0 = roots[0].getChild("Document",ns).getChildren("Placemark",ns);
		
		Iterator<Element> iter;
		Element temp;
		int placemark_id;
		for(a=0;a<numFiles-1;a++){	//cloneContent() returns list of alternating Text and Element objects
			for(b=centroids[a].size();b>0;b-=2){
				centroids[a].remove(b-1);
			}
			for(b=polygons[a].size();b>0;b-=2){
				polygons[a].remove(b-1);
			}
			iter = centroids[a].iterator();
			while(iter.hasNext()){
				temp=iter.next();
				if(temp.getName().equals("Placemark")){
					placemark_id = Integer.parseInt(temp.getAttributeValue("id").split("lbl")[0]); //adjust placemark_id
					placemark_id += 500*(a+1);
					temp.setAttribute("id",placemark_id+"lbl");
					centroids0.add(temp);
				}
			}
			iter = polygons[a].iterator();
			while(iter.hasNext()){
				temp=iter.next();
				if(temp.getName().equals("Placemark")){
					placemark_id = Integer.parseInt(temp.getAttributeValue("id")); //adjust placemark_id
					placemark_id += 500*(a+1);
					temp.setAttribute("id",placemark_id+"");
					polygons0.add(temp);
				}
			}
		}
		
		XMLOutputter outputter = new XMLOutputter();
	    try {
	      outputter.output(docs[0], new FileOutputStream(outputpath));       
	    } catch (IOException e) {
	      System.err.println(e);
	    }
	}
}
