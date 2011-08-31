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

import graphics.ControlPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import calculations.KMLDescriptor;
import calculations.KMLWriter;




/**
 * 
 * Tool developed to make it easier to output KML files
 * more quickly. Uses the control panel from the main GUI to 
 * help describe the models and also uses the Exporter used by the
 * main GUI.
 *
 */
public class KMLBuilder {

	static JTextField path, center;

	static HashMap<String, KMLDescriptor> descriptors;
	static JComboBox combo;
	static JSlider slide;
	static JButton add, remove;
	static JTable table;

	static ControlPanel control;

	static DefaultTableModel dataTab;

	static int count = 0;
	
	static KMLWriter write;

	public static void main(String [] args){
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){}
		
		write = new KMLWriter();

		JFrame mainFrame = new JFrame();

		String[] columnNames = {"#", "Path", "Description"};

		descriptors = new HashMap<String, KMLDescriptor>();

		String[] extrudeby = {"None", "Name", "Color", "Population", "Employment", "Households", "Transit Acc.", "Drive Acc.", "Modal Acc. Gap"};

		combo = new JComboBox(extrudeby);

		slide = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		slide.setPaintTicks(true);

		control = new ControlPanel(null, null);
		control.addEnableDisableListeners();

		path = new JTextField(20);
		center = new JTextField(20);

		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));

		JPanel forPath = new JPanel();
		JPanel forCenter = new JPanel();
		JPanel forExtrude = new JPanel();
		JPanel forOpac = new JPanel();
		JPanel flowControl = new JPanel();

		forCenter.setLayout(new FlowLayout());
		forPath.setLayout(new FlowLayout());
		forExtrude.setLayout(new FlowLayout());
		forOpac.setLayout(new FlowLayout());
		flowControl.setLayout(new FlowLayout());

		forPath.add(new JLabel("Path to Save: "));
		forPath.add(path);
		path.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				JFileChooser choose = new JFileChooser();
				//String savePath = null;
				int choice = choose.showSaveDialog(null);
				if(choice == JFileChooser.APPROVE_OPTION){
					path.setText(choose.getSelectedFile().getPath());
			}				
			}

			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			
		});


		forCenter.add(new JLabel("Centered Around: "));
		forCenter.add(center);

		forExtrude.add(new JLabel("Extrude By: "));
		forExtrude.add(combo);

		forOpac.add(new JLabel("Opacity"));
		forOpac.add(slide);

		flowControl.add(control);

		east.add(forPath);
		east.add(forCenter);
		east.add(forExtrude);
		east.add(forOpac);
		east.add(flowControl);


		JPanel south = new JPanel();
		south.setLayout(new FlowLayout());
		add = new JButton("Add");
		remove = new JButton("Remove");
		south.add(add);
		south.add(remove);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		dataTab = new DefaultTableModel(40,3);
		dataTab.setColumnIdentifiers(columnNames);


		table = new JTable(dataTab);
		JScrollPane scroll = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		add.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				HashMap<String, Boolean> vars = control.getState();
				
				//Builds an object to hold the current model which can then be used to output
				//the associated KML data
				try{
					KMLDescriptor descript = new KMLDescriptor(Integer.parseInt(center.getText()), 
							combo.getSelectedIndex(), 
							slide.getValue(),
							vars.get("iso"),
							vars.get("thir"),
							vars.get("four"),
							vars.get("fif"),
							vars.get("six"),
							vars.get("allIso"),
							vars.get("comThir"),
							vars.get("comFour"),
							vars.get("comSix"),
							vars.get("com"),
							vars.get("mag"),
							vars.get("access"),
							vars.get("trans"),
							vars.get("auto"),
							vars.get("heat"),
							vars.get("time"));

					descriptors.put(path.getText(), descript);
					dataTab.setValueAt(count+1, count, 0);
					dataTab.setValueAt(path.getText(),count, 1);
					dataTab.setValueAt(descript.getDescription(), count, 2);
					dataTab.fireTableDataChanged();
					count++;
					table.revalidate();
				}
				catch(Exception ex){
				}

			}
		});

		//Removes the selected row from the model as well as the data
		remove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int rows = table.getSelectedRow();
				String path = (String) dataTab.getValueAt(rows, 1);
				descriptors.remove(path);
				dataTab.removeRow(rows);
				String[] blank = {"", "", ""};
				dataTab.addRow(blank);
				count--;

				count = Math.max(0, count);
				dataTab.fireTableDataChanged();
			}
		});

		//Writes all of the models to the local directory in to_build.xml
		//Could be altered to directly be outputted as KML, though original
		//purpose was to allow for the user to make there own XML files to describe
		//the KML files they want
		control.addButtonListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				Element root = new Element("config");
				for(String path : descriptors.keySet()){
					root.addContent(descriptors.get(path).toElement(path));
				}

				Document d = new Document(root);

				XMLOutputter out = new XMLOutputter();
				FileWriter writer;
				try {
					writer = new FileWriter("to_build.xml");
					out.output(d, writer);
					writer.flush();
					writer.close();
					write.write();
					JOptionPane.showMessageDialog(null, "Finished Saving");
				}
				catch(Exception ex){}

			}
		});

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = table.getColumnModel().getColumn(0); 
		col.setPreferredWidth(40); 
		col = table.getColumnModel().getColumn(1); 
		col.setPreferredWidth(150);
		col = table.getColumnModel().getColumn(2); 
		col.setPreferredWidth(250);
		scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		east.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		mainPanel.add(scroll, BorderLayout.CENTER);
		mainPanel.add(east, BorderLayout.EAST);
		mainPanel.add(south, BorderLayout.SOUTH);

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(mainPanel);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);




	}



}
