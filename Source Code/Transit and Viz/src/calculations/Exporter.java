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

package calculations;


import graphics.ExportFrame;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class Exporter {
	
	ModelCalculator mod;
	
	ExportFrame f;
	
	
	public Exporter(ModelCalculator m){
		mod = m;
	}
	
	//Ask for configuration, which calls save
	public void showDialog(){
		f = new ExportFrame(this);
	}
	
	public void save(String path, int extrusion, int opacity){
		HashMap<String, Color> cols = mod.getModel();
		HashMap<String, Double> exts = mod.getExtrusions(extrusion);
		
		int nOpacity = Math.min((int)((double)opacity/100. * 256.), 255);
				
		int id = 0;
		
		//Namespaces used by KML
		Element root = new Element("kml",Namespace.getNamespace("http://www.opengis.net/kml/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("gx","http://www.google.com/kml/ext/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("kml","http://www.opengis.net/kml/2.2"));
		root.addNamespaceDeclaration(Namespace.getNamespace("atom","http://www.w3.org/2005/Atom"));
		
		Element doc = new Element("Document");
		
		Element name = new Element("name");
		name.setText(path.substring(path.lastIndexOf("\\") + 1));
		doc.addContent(name);
		
		//If the model doesn't contain any colors, just fill with blue
		if(cols.keySet().size() == 0){
			for(String s : exts.keySet())
				cols.put(s, Color.blue);
		}
		
		//For each shape, write KML with the specified color model
		for(String s : cols.keySet()){
			
			Element marker = new Element("Placemark");
			
			marker.setAttribute("id", id + "");
			id++;
			
					
			Element snip = new Element("Snippet");
			snip.setAttribute("maxLines", "0");
			
			Element taz = new Element("name");
			taz.setText(s);
			
			Element desc = new Element("description");
			
			Element vis = new Element("visibility");
			vis.setText("1");
			
			Element open = new Element("open");
			open.setText("0");
			
			
			
			marker.addContent(snip);
			marker.addContent(taz);
			marker.addContent(desc);
			marker.addContent(vis);
			marker.addContent(open);
			
			
			
			Element poly = new Element("Polygon");
			
			Element extrude = new Element("extrude");
			extrude.setText("1");
			
			Element tess = new Element("tesselate");
			tess.setText("1");
			
			Element alt = new Element("altitudeMode");
			if(extrusion == 0)
				alt.setText("clampedToGround");
			else
				alt.setText("relativeToGround");
			
			Element bound = new Element("outerBoundaryIs");
			
			Element linRing = new Element("LinearRing");
			
			
			//Take the path from the shape and add each coordinate to the KML shape
			Element coord = new Element("coordinates");
			String coords = "";
			Shape ss = mod.getZoneShape(s);
			PathIterator f = ss.getPathIterator(new AffineTransform());
			while(!f.isDone()){
				double[] cs = new double[6];
				f.currentSegment(cs);
				coords += cs[0] + "," + cs[1] + "," + exts.get(s) + " ";
				f.next();
			}
			coord.setText(coords);
			
			
			
			linRing.addContent(coord);
			bound.addContent(linRing);
			poly.addContent(extrude);
			poly.addContent(tess);
			poly.addContent(alt);
			poly.addContent(bound);
			marker.addContent(poly);
			
			
			Element style = new Element("Style");
			Color c = cols.get(s);
			Color wAlpha = new Color(c.getRed(), c.getGreen(), c.getBlue(), nOpacity);
			String col = colorToHex(wAlpha);
			
			
			Element linStyle = new Element("LineStyle");
			
			Element lineColor = new Element("color");
			
			//If the model is not extruded, outline with black
			if(extrusion == 0)
				lineColor.setText("FF000000");
			else
				lineColor.setText(col);
			
			Element linWidth = new Element("width");
			linWidth.setText("1");
			
			
			linStyle.addContent(lineColor);
			linStyle.addContent(linWidth);
			style.addContent(linStyle);
			
			Element polyStyle = new Element("PolyStyle");
			
			Element polyCol = new Element("color");
			polyCol.setText(col);
			
			Element polyFill = new Element("fill");
			polyFill.setText("1");
			
			polyStyle.addContent(polyCol);
			polyStyle.addContent(polyFill);
			style.addContent(polyStyle);
			
			marker.addContent(style);
			
			doc.addContent(marker);
		}
		
		root.addContent(doc);
		Document d = new Document(root);
		
		//Output the XML as KML to the specified directory
		XMLOutputter out = new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
        FileWriter writer;
        if(!path.endsWith(".kml"))
        	path += ".kml";
		try {
			writer = new FileWriter(path);
		
        out.output(d, writer);
        writer.flush();
        writer.close();
		}
        catch (IOException e) {
			e.printStackTrace();
		}
        
        //Once completed close the config window
        if(f != null)
        f.setVisible(false);

	}
	
	//KML and Java use different formats, need to convert
	public String colorToHex(Color c){
		String s = Integer.toHexString( c.getRGB() & 0xffffffff );
		char[] chars = s.toCharArray();
		StringBuilder build = new StringBuilder();
		
		
		//KML colors read as aabbggrr as opposed to aarrggbb
		build.append(chars[0]);
		build.append(chars[1]);
		build.append(chars[6]);
		build.append(chars[7]);
		build.append(chars[4]);
		build.append(chars[5]);
		build.append(chars[2]);
		build.append(chars[3]);
		return build.toString();
		
	}

}
