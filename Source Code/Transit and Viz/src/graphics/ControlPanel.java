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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import calculations.Exporter;
import calculations.ModelCalculator;




/**
 * 
 * Virtually All GUI work to develop the Panel on the right hand side of the GUI
 * which then updates the ModelCalculator which then effects the MapPanel, only need
 * to modify if you want to add more metrics
 *
 */
public class ControlPanel extends JPanel{
	
	JRadioButton thir,four,fif,six,heat,iso,mag,access,com,time, trans, auto, comThir, comFour, comSix, allIso;
	ModelCalculator mod;
	MapPanel map;
	JButton button;
	
	public ControlPanel(ModelCalculator m, MapPanel pane){
		mod = m;
		map = pane;
		
		JPanel forMode = new JPanel();
		JPanel isoHeat = new JPanel();
		JPanel forIso = new JPanel();
		JPanel forPick = new JPanel();
		JPanel forHeat = new JPanel();
		JPanel forComMin = new JPanel();
		JPanel heatFlow = new JPanel();
		
		heatFlow.setLayout(new FlowLayout());
		forMode.setLayout(new FlowLayout());
		isoHeat.setLayout(new BoxLayout(isoHeat, BoxLayout.PAGE_AXIS));
		forPick.setLayout(new FlowLayout());
		forIso.setLayout(new FlowLayout());
		forHeat.setLayout(new GridLayout(4,1));
		forComMin.setLayout(new FlowLayout());
		
		thir = new JRadioButton("<=30");
		four = new JRadioButton("30-45");
		fif = new JRadioButton("45-60");
		six = new JRadioButton(">=60");
		allIso = new JRadioButton("All");
		
		heat = new JRadioButton("Heat Map");
		iso = new JRadioButton("Isochron");
		
		trans = new JRadioButton("Transit");
		auto = new JRadioButton("Driving");
		
		
		mag = new JRadioButton("Modal Accessibility Gap");
		com = new JRadioButton("Cumulative Opportunity");
		access = new JRadioButton("Accessibility");
		time = new JRadioButton("Time Away");
		
		comThir = new JRadioButton("<30");
		comFour = new JRadioButton("<45");
		comSix = new JRadioButton("<60");

		
		forComMin.add(comThir);
		forComMin.add(comFour);
		forComMin.add(comSix);
		forComMin.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"COM Trip Length (min)"));;
		
		forHeat.add(mag);
		forHeat.add(com);
		forHeat.add(access);
		forHeat.add(time);
		
		heatFlow.add(forHeat);
		heatFlow.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Heat Map Display"));
		
		
		forIso.add(thir);
		forIso.add(four);
		forIso.add(fif);
		forIso.add(six);
		forIso.add(allIso);
		forIso.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Time (minutes)"));
		
		
		heat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableIso();
				enableHeat();
				disableCom();
			}
		});
		
		iso.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				enableIso();
				disableHeat();
				disableCom();
			}
		});

		
		forPick.add(heat);
		forPick.add(iso);
		forPick.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Display Type"));

		
		isoHeat.add(forPick);
		isoHeat.add(forIso);
		isoHeat.add(heatFlow);
		isoHeat.add(forComMin);
		
		forMode.add(trans);
		forMode.add(auto);
		forMode.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Transportation Mode"));
		
		createButtonGroups();
		
		JPanel forButton = new JPanel();
		forButton.setLayout(new FlowLayout());
		
		button = new JButton("Export to KML");
		button.setAlignmentX((float) 0.5);
		
		
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		initSelection();
		initDisable();
		
		add(forMode);
		add(isoHeat);
		add(forIso);
		add(button);

		
	}
	
	private void initSelection(){
		thir.setSelected(true);
		heat.setSelected(true);
		trans.setSelected(true);
		comThir.setSelected(true);
	}
	
	private void initDisable(){
		disableCom();
		disableIso();
	}
	
	private void disableIso(){
		thir.setEnabled(false);
		four.setEnabled(false);
		fif.setEnabled(false);
		six.setEnabled(false);
		allIso.setEnabled(false);
	}
	
	private void enableIso(){
		thir.setEnabled(true);
		four.setEnabled(true);
		fif.setEnabled(true);
		six.setEnabled(true);
		allIso.setEnabled(true);
	}
	
	private void disableCom(){
		comThir.setEnabled(false);
		comFour.setEnabled(false);
		comSix.setEnabled(false);
	}
	
	private void enableCom(){
		comThir.setEnabled(true);
		comFour.setEnabled(true);
		comSix.setEnabled(true);
	}
	
	private void enableHeat(){
		mag.setEnabled(true);
		com.setEnabled(true);
		access.setEnabled(true);
		time.setEnabled(true);
	}
	
	private void disableHeat(){
		mag.setEnabled(false);
		com.setEnabled(false);
		access.setEnabled(false);
		time.setEnabled(false);
	}
	
	private void createButtonGroups(){
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(trans);
		modeGroup.add(auto);
		
		ButtonGroup heatGroup = new ButtonGroup();
		heatGroup.add(mag);
		heatGroup.add(com);
		heatGroup.add(access);
		heatGroup.add(time);
		
		ButtonGroup isoGroup = new ButtonGroup();
		isoGroup.add(thir);
		isoGroup.add(four);
		isoGroup.add(fif);
		isoGroup.add(six);
		isoGroup.add(allIso);
		
		ButtonGroup choiceGroup = new ButtonGroup();
		choiceGroup.add(heat);
		choiceGroup.add(iso);
		
		ButtonGroup comMinGroup = new ButtonGroup();
		comMinGroup.add(comThir);
		comMinGroup.add(comFour);
		comMinGroup.add(comSix);
		
	}
	
	public HashMap<String, Boolean> getState(){
		HashMap<String, Boolean> selected = new HashMap<String, Boolean>();
		selected.put("thir", thir.isSelected());
		selected.put("four", four.isSelected());
		selected.put("fif", fif.isSelected());
		selected.put("six", six.isSelected());
		selected.put("comThir", comThir.isSelected());
		selected.put("comFour", comFour.isSelected());
		selected.put("comSix", comSix.isSelected());
		selected.put("trans", trans.isSelected());
		selected.put("iso", iso.isSelected());
		selected.put("heat", heat.isSelected());
		selected.put("auto", auto.isSelected());
		selected.put("time", time.isSelected());
		selected.put("com", com.isSelected());
		selected.put("mag", mag.isSelected());
		selected.put("allIso", allIso.isSelected());
		selected.put("access", access.isSelected());

		return selected;
	}
	
	public void addButtonListener(ActionListener a){
		button.addActionListener(a);
	}
	
	public void addEnableDisableListeners(){
		mag.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableCom();
			}
		});
		
		com.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				enableCom();
			}
		});
		
		time.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableCom();
			}
		});
		
		access.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableCom();
			}
		});
		
		
		
	}
	
	public void addActionListeners(){
		
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Exporter exp = new Exporter(mod);
				exp.showDialog();
			}
		});
		
		
		thir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(), allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		four.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
					mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
					heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(), allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		fif.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(), allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		six.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected()
						,allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		allIso.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(), allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		heat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		iso.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		trans.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		auto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		mag.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				disableCom();
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		com.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				enableCom();
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();

			}
		});
		
		access.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				disableCom();
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
		time.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				disableCom();
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		comThir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		comFour.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		comSix.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mod.updateClick(thir.isSelected(), four.isSelected(), fif.isSelected(), six.isSelected(), com.isSelected(),
						mag.isSelected(), access.isSelected(), time.isSelected(), trans.isSelected(), auto.isSelected(),
						heat.isSelected(), iso.isSelected(), comThir.isSelected(), comFour.isSelected(), comSix.isSelected(),allIso.isSelected());
				map.setModelColor(mod.getModel());
				map.repaint();
				map.validate();
			}
		});
		
	}

}
