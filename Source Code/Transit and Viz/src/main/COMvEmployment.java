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

package main;

import java.io.File;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;


/**
 * 
 * This class is used to generate an output that compares cumlative opportunity to 
 * accessibility. The output is tabbed so it can be copy and pasted into Excel.
 *
 */
public class COMvEmployment {
	public static void main(String [] args){
		SAXBuilder build = new SAXBuilder();
		String directoryPath = "";
		HashMap<String, TAZ> localTaz;
		
		//Reads in all XML data from the configuration file
		try {
			Document d = build.build(new File("config.xml"));
			Element root = d.getRootElement();
			directoryPath = root.getChildText("taz_dir");


			File dir = new File(directoryPath);
			File[] files = dir.listFiles(new XMLFilter());


			localTaz = new HashMap<String, TAZ>();

			for(File f : files){
				String path = f.getPath();
				TAZ t = new TAZ(path);
				localTaz.put(t.getTAZ(), t);
			}
			//Distance or rather duration set for cumulative opportunity in minutes
			int distance  = 30;
			for(String fr : localTaz.keySet()){
				TAZ from = localTaz.get(fr);
				double sumJob = 0.;
				double sumAuto = 0.;
				int tOpp = 0;
				int aOpp = 0;
				
				//Using gravity model, calculate accessibility and store the information
				for(String j : localTaz.keySet()){
					TAZ to = localTaz.get(j);
					sumJob += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getTotalTime(j)*.001/60.));
					sumAuto += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getDriveTime(j)));
					if(from.getTotalTime(j)/1000/60 < distance)
						tOpp += to.getEmployment(2010);
					if(from.getDriveTime(j) < distance)
						aOpp += to.getEmployment(2010);
				}
				System.out.println(fr + "\t" + sumJob + "\t" + tOpp + "\t" + sumAuto + "\t" + aOpp);
			}
	}
		catch(Exception e){}
	}

}
