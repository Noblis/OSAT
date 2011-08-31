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

import org.jdom.Element;


/**
 * 
 * Used with the KML Builder in order to handle the creation and destructions
 * of models to print, not used in the main GUI.
 *
 */
public class KMLDescriptor {

	int around;
	int extrude;
	int opacity;

	boolean iso, thir, four, fif, six, allIso, comThir, comFour, comSix, com, mag, access, trans, auto, heat, time;

	//Object to contain data detailing what model/metric to use and formulate
	public KMLDescriptor(int centeredAround, int extrusion, int opacity, boolean iso, boolean thir,
			boolean four, boolean fif, boolean six, boolean allIso, boolean comThir, boolean comFour, 
			boolean comSix, boolean com, boolean mag, boolean access, boolean trans, boolean auto, boolean heat, boolean time){
		around = centeredAround;
		extrude = extrusion;
		this.opacity = opacity;
		this.iso = iso;
		this.thir = thir;
		this.four = four;
		this.fif = fif;
		this.six = six;
		this.allIso = allIso;
		this.comThir = comThir;
		this.comFour = comFour;
		this.comSix = comSix;
		this.com = com;
		this.mag = mag;
		this.access = access;
		this.trans = trans;
		this.auto = auto;
		this.heat = heat;
		this.time = time;
	}

	//Takes a path as well as the data detailing the model and converts it to XML
	public Element toElement(String loc){
		Element ret = new Element("kml");
		Element path = new Element("path");
		path.setText(loc);
		Element extrude_by = new Element("extrude_by");
		extrude_by.setText(extrude+"");
		Element op = new Element("opacity");
		op.setText(opacity + "");
		Element cent = new Element("centered_around");
		cent.setText(around + "");
		Element map = new Element("map_type");

		Element vIso = new Element("iso");
		Element vThir = new Element("thir");
		Element vFour = new Element("four");
		Element vFif = new Element("fif");
		Element vSix = new Element("six");
		Element vAllIso = new Element("allIso");
		Element vComThir = new Element("comThir");
		Element vComFour = new Element("comFour");
		Element vComSix = new Element("comSix");
		Element vCom = new Element("com");
		Element vMag = new Element("mag");
		Element vAccess = new Element("access");
		Element vTrans = new Element("trans");
		Element vAuto = new Element("auto");
		Element vHeat = new Element("heat");
		Element vTime = new Element("time");

		vIso.setText(toString(iso));
		vThir.setText(toString(thir));
		vFour.setText(toString(four));
		vFif.setText(toString(fif));
		vSix.setText(toString(six));
		vAllIso.setText(toString(allIso));
		vComThir.setText(toString(comThir));
		vComFour.setText(toString(comFour));
		vComSix.setText(toString(comSix));
		vCom.setText(toString(com));
		vMag.setText(toString(mag));
		vAccess.setText(toString(access));
		vTrans.setText(toString(trans));
		vAuto.setText(toString(auto));
		vHeat.setText(toString(heat));
		vTime.setText(toString(time));

		map.addContent(vIso);
		map.addContent(vThir);
		map.addContent(vFour);
		map.addContent(vFif);
		map.addContent(vSix);
		map.addContent(vAllIso);
		map.addContent(vComThir);
		map.addContent(vComFour);
		map.addContent(vComSix);
		map.addContent(vCom);
		map.addContent(vMag);
		map.addContent(vTrans);
		map.addContent(vAccess);
		map.addContent(vAuto);
		map.addContent(vHeat);
		map.addContent(vTime);

		ret.addContent(path);
		ret.addContent(extrude_by);
		ret.addContent(op);
		ret.addContent(cent);
		ret.addContent(map);

		return ret;
	}

	//Creates a string describing the type of model that was created
	public String getDescription(){
		String desc = "";

		desc += "Centered around " + around + ",";
		desc += "Opacity " + opacity + ",";
		if(auto)
			desc += "Automotive,";
		if(trans)
			desc += "Transit,";
		if(thir)
			desc += "<30 Min,";
		if(four)
			desc += "<45 Min,";
		if(fif)
			desc += "<60 Min,";
		if(six)
			desc += ">60 Min,";
		if(comThir)
			desc += "<30 Min,";
		if(comFour)
			desc += "<45 Min,";
		if(comSix)
			desc += "<60 Min,";
		if(mag)
			desc += "MAG,";
		if(access)
			desc += "Accessability,";
		if(heat)
			desc += "HeatMap,";
		if(iso)
			desc += "Isochron,";
		if(allIso)
			desc += "All-Isos,";
		if(com)
			desc += "COM,";

		if(desc.length() > 1)
			return desc.substring(0, desc.length()-1);
		return desc;
	}

	private String toString(boolean b){
		if(b == true)
			return "1";
		return "0";
	}


}
