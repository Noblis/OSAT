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

package graphics;

import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opentripplanner.api.ws.TAZ;


/**
 * 
 * Top right hand side of the GUI which updates the values of the fields
 * based on which TAZ was selected, only need to alter  if you want it to include
 * other information or take information out.
 * 
 */
public class DataPanel extends JPanel {
	
	HashMap<String, TAZ> tazs;
	HashMap<String,Double> tAccess, aAccess;
	static JTextField name, area, acres, house, emp, pop, trans, auto, mag;
	
	
	public void setData(HashMap<String, TAZ> loc, HashMap<String, Double> trans, HashMap<String, Double> auto){
		tazs = loc;
		tAccess = trans;
		aAccess = auto;
	}
	
	public DataPanel(){
		
		JPanel forName = new JPanel();
		JPanel forArea = new JPanel();
		JPanel forAcres = new JPanel();
		JPanel forHouse = new JPanel();
		JPanel forEmp = new JPanel();
		JPanel forPop = new JPanel();
		JPanel forTransAcc = new JPanel();
		JPanel forAutoAcc = new JPanel();
		JPanel forMag = new JPanel();
		
		forName.setLayout(new FlowLayout());
		forArea.setLayout(new FlowLayout());
		forAcres.setLayout(new FlowLayout());
		forHouse.setLayout(new FlowLayout());
		forEmp.setLayout(new FlowLayout());
		forPop.setLayout(new FlowLayout());
		forTransAcc.setLayout(new FlowLayout());
		forAutoAcc.setLayout(new FlowLayout());
		forMag.setLayout(new FlowLayout());
		
		name = new JTextField(10);
		area = new JTextField(10);
		acres = new JTextField(10);
		house = new JTextField(10);
		emp = new JTextField(10);
		pop = new JTextField(10);
		trans = new JTextField(10);
		auto = new JTextField(10);
		mag = new JTextField(10);
		
		
		forName.add(new JLabel("TAZ: "));
		forName.add(name);
		forArea.add(new JLabel("Area: "));
		forArea.add(area);
		forAcres.add(new JLabel("Acres: "));
		forAcres.add(acres);
		forHouse.add(new JLabel("Households (2010): "));
		forHouse.add(house);
		forEmp.add(new JLabel("Employment (2010): "));
		forEmp.add(emp);
		forPop.add(new JLabel("Population (2010): "));
		forPop.add(pop);
		forTransAcc.add(new JLabel("Transit Acces.: "));
		forTransAcc.add(trans);
		forAutoAcc.add(new JLabel("Auto Acces.: "));
		forAutoAcc.add(auto);
		forMag.add(new JLabel("Modal Acces. Gap: "));
		forMag.add(mag);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data"));
		
		add(forName);
		add(forArea);
		add(forAcres);
		add(forHouse);
		add(forEmp);
		add(forPop);
		add(forTransAcc);
		add(forAutoAcc);
		add(forMag);
		
		disableFields();
		
	}
	
	public void update(String taz){
		
		enableFields();
		
		DecimalFormat d = new DecimalFormat("0.00");
		name.setText(taz+"");
		if(tazs.get(taz)!=null){
			//area.setText(d.format(tazs.get(taz).getArea()));
			//acres.setText(d.format(tazs.get(taz).getAcres()));
			house.setText(tazs.get(taz).getHouseholds(2010)+ "");
			emp.setText(tazs.get(taz).getEmployment(2010)+ "");
			pop.setText(tazs.get(taz).getPopulation(2010)+ "");
		
			double tran = tAccess.get(taz);
			//double aut = aAccess.get(taz);
			trans.setText(d.format(tran));
			//auto.setText(d.format(aut));
			//mag.setText(d.format((tran-aut)/(tran+aut)));
		}
		
		disableFields();
	}
	
	private void enableFields(){
		name.setEditable(true);
		area.setEditable(true);
		acres.setEditable(true);
		house.setEditable(true);
		emp.setEditable(true);
		pop.setEditable(true);
		trans.setEditable(true);
		auto.setEditable(true);
		mag.setEditable(true);
	}
	
	private void disableFields(){
		name.setEditable(false);
		area.setEditable(false);
		acres.setEditable(false);
		house.setEditable(false);
		emp.setEditable(false);
		pop.setEditable(false);
		trans.setEditable(false);
		auto.setEditable(false);
		mag.setEditable(false);
	}

}
