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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import calculations.Exporter;




/**
 * 
 * Creates the frame which pops up when the user selects to export the model
 * to KML, allows for the option to alter opacity and extrude by certain models,
 * only need to alter this if you want to add features to export as, or if you want
 * to change how the frame looks.
 *
 */
public class ExportFrame extends JFrame{
	
	JRadioButton name, color, pop, emp, hous, tAcc, dAcc, mag,none;
	
	String path;
	
	int extrusion;
	
	Exporter exporter;
	
	JSlider slide;
	
	public ExportFrame(Exporter ex){
		
		path = null;
		extrusion = -1;		
		exporter = ex;
		
		slide = new JSlider(JSlider.HORIZONTAL,0, 100, 100);
		slide.setMajorTickSpacing(25);
		slide.setMinorTickSpacing(5);
		slide.setPaintTicks(true);
		slide.setPaintLabels(true);

		
		JPanel forSlider = new JPanel();
		forSlider.setLayout(new BorderLayout());
		forSlider.add(new JLabel("Opacity"), BorderLayout.NORTH);
		forSlider.add(slide);
		
		
		JPanel mainPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		none = new JRadioButton("None");
		name = new JRadioButton("Name");
		color = new JRadioButton("Color");
		pop = new JRadioButton("Population");
		emp = new JRadioButton("Employment");
		hous = new JRadioButton("Households");
		tAcc = new JRadioButton("Transit Acc.");
		dAcc = new JRadioButton("Drive Acc.");
		mag = new JRadioButton("Modal Acc. Gap");
		
		ButtonGroup extrudeGroup = new ButtonGroup();
		extrudeGroup.add(none);
		extrudeGroup.add(name);
		extrudeGroup.add(color);
		extrudeGroup.add(pop);
		extrudeGroup.add(emp);
		extrudeGroup.add(hous);
		extrudeGroup.add(tAcc);
		extrudeGroup.add(dAcc);
		extrudeGroup.add(mag);
		
		JPanel top = new JPanel();
		top.setLayout(new FlowLayout());
		JPanel mid = new JPanel();
		mid.setLayout(new FlowLayout());
		JPanel bot = new JPanel();
		bot.setLayout(new FlowLayout());
		none.setSelected(true);
		
		top.add(none);
		top.add(name);
		top.add(color);
		top.add(pop);
		mid.add(emp);
		mid.add(hous);
		mid.add(tAcc);
		bot.add(dAcc);
		bot.add(mag);
		
		buttonPanel.add(top);
		buttonPanel.add(mid);
		buttonPanel.add(bot);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Extrude By"));
		
		final JButton export = new JButton("Export to KML");
		
		export.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
				JFileChooser choose = new JFileChooser();
				String savePath = null;
				int choice = choose.showSaveDialog(null);
				if(choice == JFileChooser.APPROVE_OPTION){
					path = choose.getSelectedFile().getPath();
					System.out.println(path);
					if(none.isSelected())
						extrusion = 0;
					else if(name.isSelected())
						extrusion = 1;
					else if(color.isSelected())
						extrusion = 2;
					else if(pop.isSelected())
						extrusion = 3;
					else if(emp.isSelected())
						extrusion = 4;
					else if(hous.isSelected())
						extrusion = 5;
					else if(tAcc.isSelected())
						extrusion = 6;
					else if(dAcc.isSelected())
						extrusion = 7;
					else
						extrusion = 8;
					
					//Saves the file to the KML path
					exporter.save(path, extrusion, slide.getValue());
				}
				}
				
				
			
		});
		
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(forSlider);
		mainPanel.add(buttonPanel);
		mainPanel.add(export);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		export.setAlignmentX((float) 0.5);
		
		
		setTitle("Export to KML");
		add(mainPanel);
		pack();
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setVisible(true);
		
	}
	

}
