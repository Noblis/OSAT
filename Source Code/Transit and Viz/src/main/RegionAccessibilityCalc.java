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

import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;


/**
 * 
 * Metric used to assess the regions ability to reach available
 * employment via automotive and public transit means
 * 
 * Not integrated into the main GUI, for local use only
 *
 */
public class RegionAccessibilityCalc {
	public static void main(String [] args){
		SAXBuilder build = new SAXBuilder();
		String directoryPath = "";
		HashMap<String, TAZ> localTaz;
		try {
			
			//Loads the XML files from the configuration file
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
			int totJobs = 0;
			int totPop = 0;
			
			//Tracks employment reachable within 45 minutes
			double autoPop = 0.0, transPop = 0.0;
			for(String from : localTaz.keySet()){
				TAZ f = localTaz.get(from);
				double transJob = 0;
				double autoJob = 0;
				for(String to: localTaz.keySet()){
					if(f.getTotalTime(to)/60 * .001 < 45)
						transJob += localTaz.get(to).getEmployment(2010);
					if(f.getDriveTime(to) < 45)
						autoJob += localTaz.get(to).getEmployment(2010);
				}
				totJobs += f.getEmployment(2010);
				totPop += f.getPopulation(2010);
				
				 autoPop += autoJob * f.getPopulation(2010);
				 transPop += transJob * f.getPopulation(2010);
			}
			
			JOptionPane.showMessageDialog(null, "Auto Access: " + (autoPop/totPop)/totJobs + "\nTransit Access: " + (transPop/totPop)/totJobs);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Faulty Config File");
			System.exit(0);
		} 



	}

}
