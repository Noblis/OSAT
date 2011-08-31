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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;



public class ConfigFrame extends JFrame{
    
    JTextField centroid_field, output_dir_field, input_dir_field, tiger_line_field;
    
    public ConfigFrame(){
        
    	//Set the look and feel to Windows, not entirely necessary
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
        }
        
        
        /**************************************
         *            GUI Code
         **************************************/
        JPanel forCentroid = new JPanel();
        JPanel forOutPath = new JPanel();
        JPanel forInPath = new JPanel();
        JPanel forTigerLine = new JPanel();

        JPanel forButtons = new JPanel();
        
        forCentroid.setLayout(new FlowLayout());
        forOutPath.setLayout(new FlowLayout());
        forInPath.setLayout(new FlowLayout());
        forTigerLine.setLayout(new FlowLayout());

        
        centroid_field = new JTextField(10);
        output_dir_field = new JTextField(10);
        input_dir_field = new JTextField(10);
        tiger_line_field = new JTextField(10);


        /**
         * Define Text Fields to open up file/directory choosing dialogs
         * once they are clicked on and set the text field to selection.
         */
        
        centroid_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    int choice = choose.showOpenDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            centroid_field.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        output_dir_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int choice = choose.showOpenDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            output_dir_field.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        tiger_line_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int choice = choose.showOpenDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            tiger_line_field.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        input_dir_field.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                JFileChooser choose = new JFileChooser();
                choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int choice = choose.showOpenDialog(null);
                if(choice == JFileChooser.APPROVE_OPTION){
                        input_dir_field.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        JButton load_config_file_button = new JButton("Load Config.xml");
        JButton submit_fields_button = new JButton("Submit");
        
        load_config_file_button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	
            	/**
            	 * Load data from configuration file, should contain information for each
            	 * field, if not present, exception raised and program exits.
            	 */
                SAXBuilder build = new SAXBuilder();
                try{
                    Document d = build.build("config.xml");
                    Element root = d.getRootElement();
                    String kmlPath = root.getChildText("kml_read_in");
                    String tigerDir = root.getChildText("tiger_dir");
                    String inDir = root.getChildText("input_dir");
                    String outputDir = root.getChildText("output_dir");

                    Interface.calculateAutoTimes(inDir, outputDir, tigerDir, kmlPath);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(null, "Faulty Config File");
                    ex.printStackTrace();
                    System.exit(0);
                }
            }
        });
        
        submit_fields_button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                	
                	//Read fields from JFrame, if the fields fail to load
                	//the program does not raise an exception and allows for re-entry
                    String kmlPath = centroid_field.getText();
                    String tigerDir = tiger_line_field.getText();
                    String inDir = input_dir_field.getText();
                    String outputDir = output_dir_field.getText();
                    if(!outputDir.endsWith("\\"))
                    	outputDir+="\\";
                    if(!inDir.endsWith("\\"))
                    	inDir += "\\";
                    System.out.println(outputDir);
                    Interface.calculateAutoTimes(inDir, outputDir, tigerDir, kmlPath);
                }
                catch(Exception ex){}
            }
        });
        
        ////////////////////////More GUI Configuration////////////////////////////////
        forButtons.setLayout(new FlowLayout());
        
        forButtons.add(load_config_file_button);
        forButtons.add(new JPanel());
        forButtons.add(submit_fields_button);
        
        forCentroid.add(new JLabel("KML Centroid Path: "));
        forInPath.add(new JLabel("XML Input Directory: "));
        forOutPath.add(new JLabel("Output Directory: "));
        forTigerLine.add(new JLabel("TIGER Directory: "));
        
        forCentroid.add(centroid_field);
        forInPath.add(input_dir_field);
        forOutPath.add(output_dir_field);
        forTigerLine.add(tiger_line_field);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        mainPanel.add(forCentroid);
        mainPanel.add(forInPath);
        mainPanel.add(forOutPath);
        mainPanel.add(forTigerLine);
        
        mainPanel.add(forButtons);

        setTitle("Configuration");
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
    }

}
