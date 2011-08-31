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

package main;

import graphics.ConfigFrame;
import graphics.InitFrame;
import graphics.MainFrame;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;
//import org.pushingpixels.substance.api.SubstanceLookAndFeel;
//import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;
//import org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel;
//import org.pushingpixels.substance.api.skin.TwilightSkin;


/**
 * 
 * Main file which initiates the operation of the GUI, primary function is
 * to load XML data and pre-calculate accessibility data
 */
public class GUIMain {

	static HashMap<String, TAZ> localTaz;
	static HashMap<String, Double> autoAcc;
	static HashMap<String, Double> transAcc;

	static String kmlPath, directoryPath, logoPath;

	static Method mSetWindowOpacity;
	
	static ConfigFrame cfg;
	static InitFrame frame;

	public static void main(String [] args){

		try {

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		frame = new InitFrame();
		
		//Allow the user to specify the proper directories and files to operate
		cfg = new ConfigFrame();
		cfg.configure();

		cfg.setVisible(false);
		cfg.dispose();
		kmlPath = cfg.getKMLPath();
		directoryPath = cfg.getDirectoryPath();
		
		//If the user specifies the directory and not the config file, changes are it does not end in a / or \
		if(!directoryPath.endsWith("\\") && !directoryPath.endsWith("/"))
			directoryPath += "\\";
		
		//Loads the logo
		loadConfig();

		//Loads the XML data and precalculates accessibility measures
		loadData();

		
		//Open the GUI with a fading transparent effect
		final MainFrame frame = new MainFrame(kmlPath, localTaz, transAcc, autoAcc);
		frame.setAlwaysOnTop(true);
		
		for(float f = 0; f < 1; f = (float) (f + .08)){
			try{
				mSetWindowOpacity.invoke(null, frame, Float.valueOf(f));
				if(f == 0)
					frame.setVisible(true);
				Thread.sleep(2);
			}
			catch(Exception e){
				System.out.println("Error");
			}
		}

		try{
			mSetWindowOpacity.invoke(null, frame, (float) 1);
		}
		catch(Exception e){}

	}


	static void loadConfig(){
		SAXBuilder build = new SAXBuilder();
		try {
			Document d = build.build(new File("configviz.xml"));
			Element root = d.getRootElement();
			logoPath = root.getChildText("logo_path");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Faulty Config File");
			System.exit(0);
		} 
	}

	static void loadData(){

		File dir = new File(directoryPath);
		System.out.println(directoryPath);
		File[] files = dir.listFiles(new XMLFilter());
		System.out.println(files.length);
		frame.setVariables(files.length, logoPath);
		frame.invoke();
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);

		//Make the window translucent
		//AWTUtilities.setWindowShape(frame, new RoundRectangle2D.Float(0, 0, frame.getWidth(), frame.getHeight(), 10, 10));

		try {
			Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
			mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
			mSetWindowOpacity.invoke(null, frame, Float.valueOf(0.75f));
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}

		frame.repaint();
		frame.validate();

		localTaz = new HashMap<String, TAZ>();

		for(File f : files){
			String path = f.getPath();
			TAZ t = new TAZ(path);
			localTaz.put(t.getTAZ(), t);

			frame.changed();
		}
		calcAccess();
		frame.setVisible(false);
		/*try {
			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed");
			e.printStackTrace();
		}*/
	}

	static void calcAccess(){
		//autoAcc = new HashMap<Integer, Double>();
		transAcc = new HashMap<String, Double>();
		for(String i : localTaz.keySet()){
			double sumJob = 0.;
			//double sumAuto = 0.;
			TAZ from = localTaz.get(i);
			for(String j : localTaz.keySet()){
				TAZ to = localTaz.get(j);
				
				//Gravity model used to calcualte accessibility
				if(!Double.isNaN(to.getEmployment(2010)) && from.getTotalTime(j)!=-1){
					sumJob += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getTotalTime(j)*.001/60.));
					//sumAuto += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getDriveTime(j)));
				}
			}
			transAcc.put(i, sumJob);
			//autoAcc.put(i, sumAuto);
		}

	}


}
