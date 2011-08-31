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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;

/**
 * 
 * Not part of the main GUI, though is a tool which takes in an 
 * Excel file and quickly appends data to outputed XML files. Useful when population or 
 * even acreage data isn't readily available when outputted and needs to be added on later
 * 
 * File needs to be in the format
 * 
 *   | TAZ1 | VALUE1 |    
 *   | TAZ2 | VALUE2 |
 *   
 *   where the first two columns are utilized, and the first column is designated for the
 *   TAZ and the second column is for the value to be associated with the TAZ
 *   
 *   Don't need to append all files, only the ones you want.
 * 
 */
public class DataAppender {

	static JComboBox append_combo, taz_combo;
	static JTextField excel_directory;
	static HashMap<String, TAZ> localTaz;
	static ArrayList<String> tazForCombo ;


	public static void main(String [] args){
		String[] toAppend = {"Acres", "Area", "Population", "Employment", "Housing", "Drive Time", "Transit Duration", "Walk Time", "Walk Distance"};

		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		append_combo = new JComboBox(toAppend);

		//Have the user select the directory where the original TAZ xml files are stored
		JFileChooser choose = new JFileChooser();
		choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int choice = choose.showOpenDialog(null);
		String path;
		tazForCombo = new ArrayList<String>();
		if(choice == JFileChooser.APPROVE_OPTION){
			path = choose.getSelectedFile().getPath();

			File dir = new File(path);
			File[] files = dir.listFiles(new XMLFilter());


			localTaz = new HashMap<String, TAZ>();

			for(File f : files){
				String xmlPath = f.getPath();
				TAZ t = new TAZ(xmlPath);
				localTaz.put(t.getTAZ(), t);
				tazForCombo.add(t.getTAZ() + "");
			}
		}

		
		////////////   GUI WORK   /////////////////
		taz_combo = new JComboBox(tazForCombo.toArray());

		excel_directory = new JTextField(10);
		excel_directory.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				JFileChooser choose = new JFileChooser();

				int choice = choose.showOpenDialog(null);
				if(choice == JFileChooser.APPROVE_OPTION){
					excel_directory.setText(choose.getSelectedFile().getPath());
				}                               
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

		System.out.println("Files loaded");

		JFrame mainFrame = new JFrame();
		JPanel mainPanel = new JPanel();

		JPanel forCombo = new JPanel();
		final JPanel forTAZ = new JPanel();
		JPanel forDirectory = new JPanel();

		JButton submit = new JButton("Submit");


		forCombo.setLayout(new FlowLayout());
		forTAZ.setLayout(new FlowLayout());
		forDirectory.setLayout(new FlowLayout());

		forCombo.add(new JLabel("Data to Alter: "));
		forTAZ.add(new JLabel("TAZ to Alter: "));
		forDirectory.add(new JLabel("Excel File: "));

		append_combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(append_combo.getSelectedIndex() < 5)
					taz_combo.setEnabled(false);
				else
					taz_combo.setEnabled(true);

			}
		});


		forCombo.add(append_combo);
		forTAZ.add(taz_combo);
		forDirectory.add(excel_directory);

		forTAZ.setEnabled(false);

		submit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				HashMap<String, Double> vals = new HashMap<String, Double>();

				try{
					
					//Opens excel workbook and extracts information from first two columns
					InputStream inp = new FileInputStream(excel_directory.getText());
					HSSFWorkbook wb = (HSSFWorkbook) WorkbookFactory.create(inp);
					int row = 0;
					Sheet sheet = wb.getSheetAt(0);
					while(sheet.getRow(row) != null){
						int taz = (int) sheet.getRow(row).getCell(0).getNumericCellValue();
						double val = sheet.getRow(row).getCell(1).getNumericCellValue();
						vals.put(taz+"", val);
						row++;
					}

				}
				catch(Exception ex){
					ex.printStackTrace();
					System.out.println("Excel Failed");
					System.exit(0);
				}



				//Appends the data based on the combo box selection
				switch(append_combo.getSelectedIndex()){
				case 0:
					for(String tz : vals.keySet()){
						TAZ loc = localTaz.get(tz);
						loc.setAcres(vals.get(tz));
						try {
							loc.toXML("C:/testingAmmendment/");
						} catch (IOException e1) {
							System.out.println("Failed To Output");
						}
					}
					break;
				case 1:

					for(String tz : vals.keySet()){
						TAZ loc = localTaz.get(tz);
						loc.setArea(vals.get(tz));
						try {
							loc.toXML("C:/testingAmmendment/");
						} catch (IOException e1) {
							System.out.println("Failed To Output");
						}
					}
					break;
					
					
					/////////////////////////BY DEFAULT THE YEAR 2010 IS USED FOR POPULATION, HOUSING, and EMPLOYMENT////////////////////////////////
				case 2:
					System.out.println("Population");
					for(String tz : vals.keySet()){
						TAZ loc = localTaz.get(tz);
						HashMap<Integer, Double> population = new HashMap<Integer, Double>();
						population.put(2010, vals.get(tz));
						loc.addPopulationData(population);
						try {
							loc.toXML("C:/testingAmmendment/");
						} catch (IOException e1) {
							System.out.println("Failed To Output");
						}
					}
					break;
				case 3:
					System.out.println("Employment");
					for(String tz : vals.keySet()){
						TAZ loc = localTaz.get(tz);
						HashMap<Integer, Double> employment = new HashMap<Integer, Double>();
						employment.put(2010, vals.get(tz));
						loc.addEmploymentData(employment);
						try {
							loc.toXML("C:/testingAmmendment/");
						} catch (IOException e1) {
							System.out.println("Failed To Output");
						}
					}
					break;
				case 4:
					System.out.println("Housing");

					for(String tz : vals.keySet()){
						TAZ loc = localTaz.get(tz);
						HashMap<Integer, Double> housing = new HashMap<Integer, Double>();
						housing.put(2010, vals.get(tz));
						loc.addHousingData(housing);
						try {
							loc.toXML("C:/testingAmmendment/");
						} catch (IOException e1) {
							System.out.println("Failed To Output");
						}
					}
					break;
				case 5:
					TAZ loc = localTaz.get(Integer.parseInt(tazForCombo.get(taz_combo.getSelectedIndex())));
					for(String tz : vals.keySet()){
						loc.setDriveTime(tz, vals.get(tz));
					}
					try {
						loc.toXML("C:/testingAmmendment/");
					} catch (IOException e1) {
						System.out.println("Failed To Output");
					}

					break;
				case 6:
					System.out.println(tazForCombo.get(taz_combo.getSelectedIndex()));
					TAZ curr = localTaz.get(Integer.parseInt(tazForCombo.get(taz_combo.getSelectedIndex())));
					System.out.println("CURRENT: " + curr.getTAZ());
					for(String tz : vals.keySet()){
						System.out.println("ITERATING");
						curr.setTransitDuration(tz,  vals.get(tz).longValue());
					}
					try {
						curr.toXML("C:/testingAmmendment/");
					} catch (IOException e1) {
						System.out.println("Failed To Output");
					}

					break;
				case 7:
					TAZ fTime = localTaz.get(Integer.parseInt(tazForCombo.get(taz_combo.getSelectedIndex())));
					for(String tz : vals.keySet())
						fTime.setWalkTime(tz, vals.get(tz).longValue());
					try {
						fTime.toXML("C:/testingAmmendment/");
					} catch (IOException e1) {
						System.out.println("Failed To Output");
					}

					break;
				case 8:
					TAZ fDist = localTaz.get(Integer.parseInt(tazForCombo.get(taz_combo.getSelectedIndex())));
					for(String tz : vals.keySet())
						fDist.setWalkDistance(tz, vals.get(tz));
					try {
						fDist.toXML("C:/testingAmmendment/");
					} catch (IOException e1) {
						System.out.println("Failed To Output");
					}

					break;
				default: break;

				}
				JOptionPane.showMessageDialog(null, "Alterations Completed");
			}
		});

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(forCombo);
		mainPanel.add(forTAZ);
		mainPanel.add(forDirectory);
		mainPanel.add(submit);

		mainFrame.setTitle("Configuration");
		mainFrame.add(mainPanel);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


	}

}
