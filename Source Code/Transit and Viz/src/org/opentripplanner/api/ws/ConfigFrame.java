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

package org.opentripplanner.api.ws;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
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


/**
 * Similar to other config frames used in the other projects, just used to allow
 * for the user to specify which files to utilize in its operation
 *
 */
public class ConfigFrame extends JFrame{
    
    JTextField cent, out, graph, date, arrive, wDist;
    
    public ConfigFrame(){
        
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
        }
        
        JPanel forCentroid = new JPanel();
        JPanel forOutPath = new JPanel();
        JPanel forGraphObj = new JPanel();
        JPanel forTripVars = new JPanel();
        JPanel forDate = new JPanel();
        JPanel forArrival = new JPanel();
        JPanel forWalkDist = new JPanel();
        JPanel forButtons = new JPanel();
        
        forCentroid.setLayout(new FlowLayout());
        forOutPath.setLayout(new FlowLayout());
        forGraphObj.setLayout(new FlowLayout());
        forTripVars.setLayout(new FlowLayout());
        forDate.setLayout(new FlowLayout());
        forArrival.setLayout(new FlowLayout());
        forWalkDist.setLayout(new FlowLayout());
        
        cent = new JTextField(10);
        out = new JTextField(10);
        graph = new JTextField(10);
        date = new JTextField(10);
        arrive = new JTextField(10);
        wDist = new JTextField(10);
        
        forDate.add(new JLabel("Date (MM/DD/YY): "));
        forDate.add(date);
        forArrival.add(new JLabel("Arrive by (HH:MM): "));
        forArrival.add(arrive);
        forWalkDist.add(new JLabel("Max Walk Distance"));
        forWalkDist.add(wDist);
        
        forTripVars.setLayout(new BoxLayout(forTripVars, BoxLayout.Y_AXIS));
        forTripVars.add(forDate);
        forTripVars.add(forArrival);
        forTripVars.add(forWalkDist);
        
        
        
        //Allow for user input of the appropriate centroid file
        cent.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    //String savePath = null;
                    int choice = choose.showSaveDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            cent.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        //Allow for user input of appropriate output directory
        out.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    //String savePath = null;
                    int choice = choose.showSaveDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            out.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        //Allow for user input of Graph.obj
        graph.addMouseListener(new MouseListener(){
            public void mouseClicked(MouseEvent e) {
                    JFileChooser choose = new JFileChooser();
                    //String savePath = null;
                    int choice = choose.showSaveDialog(null);
                    if(choice == JFileChooser.APPROVE_OPTION){
                            graph.setText(choose.getSelectedFile().getPath());
            }                               
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
    });
        
        JButton loadCfg = new JButton("Load Config.xml");
        JButton load = new JButton("Submit");
        
        //Loads data from configuration file
        loadCfg.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                SAXBuilder build = new SAXBuilder();
                try{
                    Document d = build.build("config.xml");
                    Element root = d.getRootElement();
                    String graphPath = root.getChildText("graph_obj_path");
                    String centroidKml = root.getChildText("centroid_kml_path");
                    Element vars = root.getChild("trip_vars");
                    String DATE = vars.getChildText("date");
                    String ARRIVE_TIME = vars.getChildText("arrival");
                    String outDir = root.getChildText("output_dir");
                    double maxWalking = Double.parseDouble(vars.getChildText("max_walk"));
                    int skip = Integer.parseInt(vars.getChildText("skip"));
                    RouteFind.route(graphPath, centroidKml, DATE, ARRIVE_TIME, maxWalking, outDir, skip);
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(null, "Faulty Config File");
                    ex.printStackTrace();
                    System.exit(0);
                }
            }
        });
        
        //Loads data from fields in frame
        load.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try{
                String graphPath = graph.getText();
                String centroidKml = cent.getText();
                String outDir = out.getText();
                String DATE = date.getText();
                String ARRIVE_TIME = arrive.getText();
                double maxWalking = Double.parseDouble(wDist.getText());
                RouteFind.route(graphPath, centroidKml, DATE, ARRIVE_TIME, maxWalking, outDir, 0);
                }
                catch(Exception ex){}
            }
        });
        
        forButtons.setLayout(new FlowLayout());
        
        forButtons.add(loadCfg);
        forButtons.add(new JPanel());
        forButtons.add(load);
        
        forCentroid.add(new JLabel("KML Centroid Path: "));
        forGraphObj.add(new JLabel("Graph.obj Path: "));
        forOutPath.add(new JLabel("Output Directory: "));
        
        forCentroid.add(cent);
        forGraphObj.add(graph);
        forOutPath.add(out);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        mainPanel.add(forCentroid);
        mainPanel.add(forGraphObj);
        mainPanel.add(forOutPath);
        
        
        forTripVars.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Trip Variables"));
        mainPanel.add(forTripVars);
        mainPanel.add(forButtons);

        setTitle("Configuration");
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
    }

}
