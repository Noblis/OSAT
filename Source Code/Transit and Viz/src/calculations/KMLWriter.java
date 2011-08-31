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

import graphics.InitFrame;

import java.awt.Shape;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;


public class KMLWriter {

	HashMap<String, TAZ> localTaz;
	HashMap<String, Double> autoAcc;
	HashMap<String, Double> transAcc;

	String kmlPath, directoryPath, logoPath;

	ModelCalculator mod;

	/**
	 * Writes the models detailed in to_build.xml to there specified paths
	 * Used for KMLBuilder, and not the main GUI
	 */
	
	public KMLWriter(){
		

		loadConfig();

		loadData();
	}
	
	public void write()
	{

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());


		} catch (Exception e) {
			e.printStackTrace();
		}


		KMLReader read = new KMLReader(kmlPath);
		HashMap<String, Shape> shapes = read.getPolygons();

		mod = new ModelCalculator(localTaz, transAcc, autoAcc, shapes);

		Exporter ex = new Exporter(mod);

		SAXBuilder builder = new SAXBuilder();
		Document d = null;

		try {
			d = builder.build(new File("to_build.xml"));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed");
		} 

		Element root = d.getRootElement();

		//Extract elements which represent a kml file to be written
		List<Element> kmlDocs = root.getChildren("kml");

		//For each file to be written, update the ModelCalculator and save
		for(Element e : kmlDocs){

			String path = e.getChildText("path");
			if(!path.endsWith(".kml"));
				path += ".kml";
			int extrusion = Integer.parseInt(e.getChildText("extrude_by"));
			int opacity = Integer.parseInt(e.getChildText("opacity"));
			updateModelCalc(e);
			ex.save(path, extrusion, opacity);
			System.out.println("Saved : " + path);
		}

	}

	//Converts the variables to booleans in order to update the ModelCalculator
	private void updateModelCalc(Element e){
		String centeredAround = e.getChildText("centered_around");
		Element vars = e.getChild("map_type");
		boolean locThir = Integer.parseInt(vars.getChildText("thir")) == 1 ;
		boolean locFour=  Integer.parseInt(vars.getChildText("four")) == 1;
		boolean locFif = Integer.parseInt(vars.getChildText("fif")) == 1 ;
		boolean locSix=  Integer.parseInt(vars.getChildText("six")) == 1;

		boolean locCom= Integer.parseInt(vars.getChildText("com")) == 1;
		boolean locMag= Integer.parseInt(vars.getChildText("mag")) == 1;
		boolean locAccess= Integer.parseInt(vars.getChildText("access")) == 1;
		boolean locTime= Integer.parseInt(vars.getChildText("time")) == 1;
		boolean locTrans= Integer.parseInt(vars.getChildText("trans")) == 1;

		boolean locAuto= Integer.parseInt(vars.getChildText("auto")) == 1;
		boolean locHeat= Integer.parseInt(vars.getChildText("heat")) == 1;
		boolean locIso= Integer.parseInt(vars.getChildText("iso")) == 1;
		boolean locComThir= Integer.parseInt(vars.getChildText("comThir")) ==1;
		boolean locComFour= Integer.parseInt(vars.getChildText("comFour")) ==1;
		boolean locComSix = Integer.parseInt(vars.getChildText("comSix")) ==1;
		
		boolean allIso = Integer.parseInt(vars.getChildText("allIso")) == 1;

		mod.updateClick(locThir, locFour, locFif, locSix, locCom, locMag, locAccess, locTime, locTrans, locAuto, locHeat, locIso, locComThir, locComFour, locComSix, allIso);
		mod.updateCenteredAround(centeredAround);
	}

	//Loads the configuration file variables
	private void loadConfig(){
		SAXBuilder build = new SAXBuilder();
		try {
			Document d = build.build(new File("config.xml"));
			Element root = d.getRootElement();
			kmlPath = root.getChildText("kml_path");
			directoryPath = root.getChildText("taz_dir");
			logoPath = root.getChildText("logo_path");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Faulty Config File");
			System.exit(0);
		} 
	}

	//Loads the directory containing XML files for each TAZ
	private void loadData(){

		File dir = new File(directoryPath);
		File[] files = dir.listFiles(new XMLFilter());
		
		InitFrame frame = new InitFrame();
		frame.setVariables(files.length, logoPath);
		frame.invoke();
		
		localTaz = new HashMap<String, TAZ>();

		for(File f : files){
			String path = f.getPath();
			TAZ t = new TAZ(path);
			localTaz.put(t.getTAZ(), t);
			frame.changed();
		}
		calcAccess();
		frame.setVisible(false);
	}

	//Precalculates Accessibility measures
	private void calcAccess(){
		autoAcc = new HashMap<String, Double>();
		transAcc = new HashMap<String, Double>();
		for(String i : localTaz.keySet()){
			double sumJob = 0.;
			double sumAuto = 0.;
			TAZ from = localTaz.get(i);
			for(String j : localTaz.keySet()){
				TAZ to = localTaz.get(j);
				sumJob += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getTotalTime(j)*.001/60.));
				sumAuto += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getDriveTime(j)));
			}
			autoAcc.put(i, sumJob);
			transAcc.put(i, sumAuto);
		}


	}
}
