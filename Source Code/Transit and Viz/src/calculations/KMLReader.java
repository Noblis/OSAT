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

package calculations;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 
 * Reads a KML file and extracts the coordinates of each shape and stores the information
 * 
 * ********The KML file must contain the name field so the program knows which TAZ the shape is for*******
 *
 */
public class KMLReader {
	
	String path;
	
	public KMLReader(String filename){
		path = filename;
	}
	
	public HashMap<String, Shape> getPolygons(){
		HashMap<String, Shape> shapes = new HashMap<String, Shape>();
		
		
		SAXBuilder build = new SAXBuilder();
		Document d = null;
		try {
			d = build.build(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Element root = d.getRootElement();
		List<Element> markers = root.getChild("Document").getChildren("Placemark");
		for(Element e : markers){
			String taz;
			String[] split = e.getChildText("description").split(" ");
			if(split[0].equals("-1") && !path.contains("dc_bg")){
				taz = split[1]+split[2];
			}else{
				taz = e.getChildText("name");
			}
			String coords = e.getChild("Polygon").getChild("outerBoundaryIs").getChild("LinearRing").getChildText("coordinates").trim();
			String[] coordLst = coords.split(" ");
			Path2D.Double path = new Path2D.Double();
			String[] yxz = coordLst[0].split(",");
			path.moveTo(Double.parseDouble(yxz[0]), Double.parseDouble(yxz[1]));
			for(int coun = 1; coun < coordLst.length; coun++){
				yxz = coordLst[coun].split(",");
				path.lineTo(Double.parseDouble(yxz[0]), Double.parseDouble(yxz[1]));
			}
			shapes.put(taz, path);
		}
		return shapes;
	}

}
