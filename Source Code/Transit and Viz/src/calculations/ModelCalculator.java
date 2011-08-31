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

import java.awt.Color;
import java.awt.Shape;
import java.util.HashMap;
import org.opentripplanner.api.ws.TAZ;

/**
 * 
 * Calcualtes the colors of the heat map fo all of the metrics, if you want to add
 * or remove a metric, it would start here
 *
 */
public class ModelCalculator {
	HashMap<String, TAZ> tazs;
	HashMap<String, Double> transAcc, autoAcc;
	HashMap<String, Shape> shapes;
	HashMap<String, TAZ> invisible;
	HashMap<String, Double> tAccHolder, aAccHolder;
	
	String centeredAround = "1";

	boolean thir,four,fif,six,com,mag,access,time,trans,auto,heat,iso,comThir,comFour,comSix,allIso;

	//Initialize all of the data needed to calculate the necessary models
	public ModelCalculator(HashMap<String, TAZ> locTaz, HashMap<String, Double> locTrans, HashMap<String, Double> locAuto, HashMap<String, Shape> shape){
		tazs = locTaz;
		transAcc = locTrans;
		autoAcc = locAuto;
		shapes = shape;
		invisible = new HashMap<String, TAZ>();
		tAccHolder = new HashMap<String, Double>();
		aAccHolder = new HashMap<String, Double>();
		centeredAround = tazs.keySet().iterator().next();
	}

	//Updates the model based on which buttons were clicked
	public void updateClick(boolean locThir, boolean locFour, boolean locFif, boolean locSix, 
			boolean locCom, boolean locMag, boolean locAccess, boolean locTime, boolean locTrans,
			boolean locAuto, boolean locHeat, boolean locIso, boolean locComThir, boolean locComFour, boolean locComSix, boolean locAllIso){

		thir = locThir;
		four = locFour;
		fif = locFif;
		six = locSix;
		com = locCom;
		mag = locMag;
		access = locAccess;
		time = locTime;
		trans = locTrans;
		auto = locAuto;
		heat = locHeat;
		iso = locIso;
		comThir = locComThir;
		comFour = locComFour;
		comSix = locComSix;
		allIso = locAllIso;
	}

	//Updates which TAZ is clicked on the screen
	public void updateCenteredAround(String s){
		centeredAround = s;
	}
	
	//If a TAZ is right clicked, remove it from the calculations
	public void updateRemove(String s){
		if(invisible.containsKey(s)){
			tazs.put(s, invisible.remove(s));
			transAcc.put(s, tAccHolder.remove(s));
			//autoAcc.put(s, aAccHolder.remove(s));
		}
		else if(tazs.containsKey(s)){
			invisible.put(s, tazs.remove(s));
			tAccHolder.put(s, transAcc.remove(s));
			//aAccHolder.put(s, autoAcc.remove(s));
		}
		recalcAccess();
	}
	
	//Recalculate accessibility after a TAZ is added or removed
	private void recalcAccess(){
		for(String s : tazs.keySet()){
			double sumJob = 0.;
			//double sumAuto = 0.;
			TAZ from = tazs.get(s);
			for(String t : tazs.keySet()){
				TAZ to = tazs.get(t);
				if(!Double.isNaN(to.getEmployment(2010)) && from.getTotalTime(t)!=-1){
					sumJob += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getTotalTime(t)*.001/60.));
					//sumAuto += to.getEmployment(2010) * Math.pow(Math.E, -.1 * (from.getDriveTime(t)));
				}
			}
			transAcc.put(s, sumJob);
			//autoAcc.put(s, sumAuto);
		}
	}

	//Based on which buttons are selected, calculate the necessary model
	public HashMap<String, Color> getModel(){
		
		if(tazs.get(centeredAround)==null){
			return showAvailable();
		}
		
		// Iso-chronic Displays
		if(iso){
			
			//Public Transportation
			if(trans){
				
				//Times frames to calculate for
				if(thir)
					return transIso(30);
				else if(four)
					return transIso(45);
				else if(fif)
					return transIso(60);
				else if(six)
					return transIso(65);
				else if(allIso)
					return transIso(0);
				else
					return defaultCentered();
			}
			
			//Automotive means of transportation
			/*else if(auto){
				
				//Times frames to calculate for
				if(thir)
					return autoIso(30);
				else if(four)
					return autoIso(45);
				else if(fif)
					return autoIso(60);
				else if(six)
					return autoIso(65);
				else if(allIso)
					return autoIso(0);
				else
					return defaultCentered();
			}*/
		}
		
		//Generate a heat map
		else if(heat){
			
			//Modal Accessibility model
			/*if(mag)
				return calcMag();
			*/
			
			//Cumulative Opportunity Model
			/*else*/ if(com){
				
				//Public transportation
				if(trans){
					if(comThir)
						return transCom(30);
					else if(comFour)
						return transCom(45);
					else if(comSix)
						return transCom(60);
					else
						return defaultCentered();
				}
				
				//Automotive transprotation
				/*else if(auto){
					if(comThir)
						return autoCom(30);
					else if(comFour)
						return autoCom(45);
					else if(comSix)
						return autoCom(60);
					else
						return defaultCentered();
				}*/
				
				//Return a blank mapping
				else
					return defaultCentered();
			}
			
			//Accessibility Model
			else if(access){
				
				//Public Transportation
				if(trans)
					return transAccessHeat();
				
				/*
				//Automotive Transportation
				else if(auto)
					return autoAccessHeat();
				else
					return defaultCentered();
				*/
			}
			
			//Time away heat map
			else if(time){
				
				//Public Transportation
				if(trans)
					return transTimeHeat();
				
				//Automotive Transportation
				/*else if(auto)
					return autoTimeHeat();*/
				else
					return defaultCentered();
			}
			else
				return defaultCentered();
		}
		else
			return defaultCentered();
		return defaultCentered();
	}

	//Iso-chronic display, depending on the time away, set the color appropriately
	private HashMap<String, Color> transIso(int min) {
		HashMap<String, Color> cols = new HashMap<String, Color>();
		
		//Depending on which taz the map is centered around, look at each of it's trip durations
		TAZ centered = tazs.get(centeredAround);
		for(String s : tazs.keySet()){
			
			double tim = centered.getTotalTime(s)*.001/60.;
			switch(min) {
			case 0:
				if(tim <= 0)
					cols.put(s, Color.BLACK);
				else if(tim <= 30)
					cols.put(s, new Color(0,255,0));
				else if(tim < 45)
					cols.put(s, new Color(170,255, 0));
				else if(tim < 60)
					cols.put(s, new Color(255, 170, 0));
				else
					cols.put(s, new Color(255, 0, 0));
				break;
			case 30:
				if(tim <= 0)
					cols.put(s, Color.BLACK);
				else if(tim <= 30)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 45:
				if(tim <= 0)
					cols.put(s, Color.BLACK);
				else if(tim > 30 && tim <= 45)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 60:
				if(tim <= 0)
					cols.put(s, Color.BLACK);
				else if(tim > 45 && tim <= 60)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 65:
				if(tim <= 0)
					cols.put(s, Color.BLACK);
				else if(tim > 60)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			default:
				cols.put(s, Color.gray);
			}
		}

		return cols;
	}

	//Automotive isochronic display, looks at TAZ which the map is
	//centered around and sets the color appropriately to time away
	/*private HashMap<String, Color> autoIso( int min) {
		HashMap<String, Color> cols = new HashMap<String, Color>();
		TAZ centered = tazs.get(centeredAround);
		for(String s : tazs.keySet()){
			double tim = centered.getDriveTime(s);
			switch(min) {
			case 0:
				if(tim <= 30)
					cols.put(s, new Color(0,255,0));
				else if(tim < 45)
					cols.put(s, new Color(170,255, 0));
				else if(tim < 60)
					cols.put(s, new Color(255, 170, 0));
				else
					cols.put(s, new Color(255, 0, 0));
				break;
			case 30:
				if(tim <= 30)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 45:
				if(tim > 30 && tim <= 45)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 60:
				if(tim > 45 && tim <= 60)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			case 65:
				if(tim > 60)
					cols.put(s, Color.yellow);
				else
					cols.put(s, Color.gray);
				break;
			default:
				cols.put(s, Color.gray);
			}
		}

		return cols;
	}*/

	//Return a map with no color mappings which will be interpreted as green
	private HashMap<String, Color> defaultCentered() {
		return new HashMap<String, Color>();
	}
	private HashMap<String,Color> showAvailable() {
		HashMap<String, Color> col = new HashMap<String, Color>();
		for(String s : tazs.keySet()){
			col.put(s, Color.BLUE);
		}
		return col;
	}
	//Calcualtes the normalized color of modal accessibility gap
	/*private HashMap<String, Color> calcMag() {
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		
		//Calculates MAG and actively looks for max and min values
		for(String s : transAcc.keySet()){
			double mag = (transAcc.get(s)-autoAcc.get(s))/(transAcc.get(s) + autoAcc.get(s));
			if(mag < min)
				min = mag;
			if(mag > max)
				max = mag;
			calc.put(s, mag);
		}

		
		//Based on max and min, normalizes value and retrieves color
		double newMax = max - min;

		for(String s : calc.keySet()){
			double mag = calc.get(s);
			double newMag = mag - min;
			double normalized = newMag/newMax;
			col.put(s, percToColor(normalized));
		}

		return col;
	}*/

	//Calculates cumulative opportunity model, and generates associate colors for heatmap
	private HashMap<String, Color> transCom(int minT) {
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(String from : tazs.keySet()){
			TAZ a = tazs.get(from);
			double sum = 0.;
			for(String to : tazs.keySet()){
				if(a.getTotalTime(to)*.001/60 < minT && a.getTotalTime(to)>0){
					sum += tazs.get(to).getEmployment(2010);
				}
			}
			if(sum < min)
				min = sum;
			if (sum > max)
				max = sum;
			calc.put(from, sum);
		}
		
		//Normalizes each value based on maximum and minimum COM, and assigns appropriate color
		double newMax = max - min;

		for(String s : calc.keySet()){
			double com = calc.get(s);
			double newCom = com - min;
			double normalized = newCom/newMax;
			col.put(s, percToColor(normalized));
		}


		return col;

	}
	
	//Calculates cumulative opportunity model, and generates associate colors for heatmap
	/*private HashMap<String, Color> autoCom(int minT) {
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(String from : tazs.keySet()){
			TAZ a = tazs.get(from);
			double sum = 0.;
			for(String to : tazs.keySet()){
				if(a.getDriveTime(to)< minT){
					sum += tazs.get(to).getEmployment(2010);
				}
			}
			if(sum < min)
				min = sum;
			if (sum > max){
				max = sum;
			}
			calc.put(from, sum);
		}

		//Normalizes each value based on maximum and minimum COM, and assigns appropriate color
		double newMax = max - min;

		for(String s : calc.keySet()){
			double com = calc.get(s);
			double newCom = com - min;
			double normalized = newCom/newMax;
			col.put(s, percToColor(normalized));
		}


		return col;

	}*/

	//Generate accessibility heat map for public transit
	private HashMap<String, Color> transAccessHeat() {
		return getAccess(transAcc);
	}

	//Calcualte Acessibility Heatmap
	private HashMap<String, Color> getAccess(HashMap<String, Double> access){
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(String i : access.keySet()){
			double acc = access.get(i);
			if(acc < min)
				min = acc;
			if(acc > max)
				max = acc;
			calc.put(i, acc);
		}

		double newMax = max - min;

		for(String s : calc.keySet()){
			double acc = calc.get(s);
			double newAcc = acc + (-1 * min);
			double normalized = newAcc/newMax;
			col.put(s, percToColor(normalized));
		}
		return col;

	}

	//Generate accessibility heat map for automotive transit
	/*private HashMap<String, Color> autoAccessHeat() {
		return getAccess(autoAcc);
	}*/

	//Calculate Public Transit time away heatmap
	private HashMap<String, Color> transTimeHeat() {
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		TAZ t = tazs.get(centeredAround);
		for(String s : t.getMappings().keySet()){
			double acc = t.getTotalTime(s);
			if(acc < min && acc>0)
				min = acc;
			if(acc > max)
				max = acc;
			calc.put(s, acc);
		}

		double newMax = max - min;

		for(String s : calc.keySet()){
			double acc = calc.get(s);
			double newAcc = acc + (-1 * min);
			double normalized = newAcc/newMax;
			if(acc == 0){
				col.put(s, Color.BLACK);
			}else{
				col.put(s, percToColor(normalized));
			}
		}
		return col;
	}
	/*
	//Calculate Automotive time away heatmap
	private HashMap<String, Color> autoTimeHeat() {
		HashMap<String, Color> col = new HashMap<String, Color>();
		HashMap<String, Double> calc = new HashMap<String, Double>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		TAZ t = tazs.get(centeredAround);
		for(String s : tazs.keySet()){
			double acc = t.getDriveTime(s);
			if(acc < min)
				min = acc;
			if(acc > max)
				max = acc;
			calc.put(s, acc);
		}

		double newMax = max - min;

		for(String s : calc.keySet()){
			double acc = calc.get(s);
			double newAcc = acc + (-1 * min);
			double normalized = newAcc/newMax;
			col.put(s, percToColor(normalized));
		}
		return col;
	}*/
	
	//Convert a normalized percentage to a color
	private Color percToColor(double d){
		if(d < .5){
			double locPerc = d / .5;
			return new Color((int)(locPerc*255), 255, 0);
		}
		else{
			double locPerc = (d-.5)/.5;
			return new Color(255, (int) ((1-locPerc)*255), 0);
		}
	}
	
	
	/* Possible addition of purple to spectrum
	private Color percToColor(double d){
		if(d < .3333){
			double locPerc = d / .3333;
			return new Color((int)(locPerc*255), 255, 0);
		}
		else if(d < .6666){
			double locPerc = (d-.3333)/.3333;
			return new Color(255, (int) ((1-locPerc)*255), 0);
		}
		else{
			double locPerc = (d-.6666)/.3333;
			return new Color((int)(255 - (locPerc * 255*.5)), 0, (int)(locPerc*255*.5));
		}
	}
	*/

	//Takes a color and converts it to a normalized percentage
	private double colorToPerc(Color c){
		int red = c.getRed();
		int green = c.getGreen();
		double perc;
		if(red == 255){
			perc = (double)green/255. * .5;
		}
		else{
			perc = (double)red/255. * .5 + .5;
		}
		return perc;

	}
	
	
	/* Possible addition of Purple
	private double colorToPerc(Color c){
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		double perc;
		if(red == 255){
			perc = (double)green/255. * .3333;
		}
		else if(blue == 0){
			perc = (double)red/255. * .3333 + .3333;
		}
		else{
			perc = (double)blue*2/255 * .3333 + .6666;
		}
		return perc;

	}
	*/

	//Based on the type of extrusion specified, returns extrusions necessary for each TAZ
	public HashMap<String, Double> getExtrusions(int extrusion){
		HashMap<String, Double> exts = new HashMap<String, Double>();
		HashMap<String, Color> col = getModel();
		HashMap<String, Double> mags = new HashMap<String, Double>();

		double min = Double.MAX_VALUE, max = 0.;

		if(extrusion == 3){
			for(TAZ t : tazs.values()){
				if(t.getPopulation(2010) > max)
					max = t.getPopulation(2010);
				else if(t.getPopulation(2010) < min)
					min = t.getPopulation(2010);
			}
		}

		else if(extrusion == 4){
			for(TAZ t : tazs.values()){
				if(t.getHouseholds(2010) > max)
					max = t.getHouseholds(2010);
				else if(t.getHouseholds(2010) < min)
					min = t.getHouseholds(2010);
			}
		}

		else if(extrusion == 5){
			for(TAZ t : tazs.values()){
				if(t.getEmployment(2010) > max)
					max = t.getEmployment(2010);
				else if(t.getEmployment(2010) < min)
					min = t.getEmployment(2010);
			}
		}

		else if(extrusion == 6){
			for(Double d : transAcc.values()){
				if(d > max){
					max = d;
				}
				else if(d < min){
					min = d;
				}
			}
		}

		else if(extrusion == 7){
			for(Double d : autoAcc.values()){
				if(d > max){
					max = d;
				}
				else if(d < min){
					min = d;
				}
			}
		}

		else if(extrusion == 8){
			for(String s : transAcc.keySet()){
				double mag = (transAcc.get(s)-autoAcc.get(s))/(transAcc.get(s) + autoAcc.get(s));
				if(mag < min)
					min = mag;
				if(mag > max)
					max = mag;
				mags.put(s, mag);
			}
		}

		//Scaling factor for how high the highest point will be, may want to alter based on how you want to visualize it
		double scale = 5000.;
		
		for(String s : tazs.keySet()){
			TAZ t = tazs.get(s);
			switch(extrusion){
			
			//Not Extruded
			case 0:
				exts.put(s, 0.);
				break;
				
			//Extrusion Placeholder
			case 1:
				exts.put(s, 0.);
				break;
				
			//Extruded by Color of model
			case 2:
				exts.put(s, colorToPerc(col.get(s)) * scale + 5);
				break;
				
			//Extruded by Population
			case 3:
				exts.put(s, (t.getPopulation(2010)-min)/(max-min) * scale + 5);
				break;
				
			//Extruded by Housholds in TAZ
			case 4: //don't use
				exts.put(s, (t.getHouseholds(2010) - min) /(max - min) * scale + 5);
				break;
				
			//Extruded by Employment available in TAZ
			case 5:
				exts.put(s, (t.getEmployment(2010) - min) /(max - min) * scale + 5);
				break;
				
			//Extruded by Public Transit Accessibility
			case 6:
				exts.put(s, (transAcc.get(s) - min) /(max - min) * scale + 5);
				break;
				
			//Extruded by Automotive Accessibility
			case 7: //don't use
				exts.put(s, (autoAcc.get(s) - min) / (max - min) * scale + 5);
				break;
				
		    //Extruded by Modal Accessibility Gap
			case 8: //don't use
				exts.put(s, (mags.get(s) - min) / (max - min) * scale + 5);
				break;
			}
		}
		return exts;
	}

	//Returns the shape of a particular zone
	public Shape getZoneShape(String s){
		return shapes.get(s);
	}




}
