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
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


/**
 * Not an essential class, used to read in a directory and
 *  see what sort of data has been collected
 *
 *
 */
public class DataManager {
    
    static JTextField wTime, wDist, eTime, sTime, tTime, dTime, wtTime, acre, area, pop, emp, hous, drTime;
    static JComboBox start, end, year;
    static String[] startZone, endZone, years;
    static HashMap<Integer, TAZ> localTaz;
    
    
    public static void main(String [] args){
        
        JFrame mainFrame = new JFrame("Manager");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        //CHANGE THIS DIRECTORY, if you want to see a different set of XML files loaded
        String directory = "C:/output2/";
        File dir = new File(directory);
        File[] files = dir.listFiles(new XMLFilter());
        
        localTaz = new HashMap<Integer, TAZ>();
        
        for(File f : files){
            String path = f.getPath();
            TAZ t = new TAZ(path);
            localTaz.put(Integer.parseInt(t.getTAZ()), t);
        }
        
        JPanel top = new JPanel();
        JPanel mid = new JPanel();
        JPanel bot = new JPanel();
        
        TitledBorder opt = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options");
         opt.setTitlePosition(TitledBorder.ABOVE_TOP);
         
         TitledBorder tazData = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Start Taz Data");
         opt.setTitlePosition(TitledBorder.ABOVE_TOP);
         
         TitledBorder tripData = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Trip Data");
         opt.setTitlePosition(TitledBorder.ABOVE_TOP);

         top.setBorder(opt);
         top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
         mid.setBorder(tazData);
         mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

         bot.setBorder(tripData);
         bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));

        
        JPanel forYear = new JPanel();
        JPanel forAcres = new JPanel();
        JPanel forArea = new JPanel();
        JPanel forPop = new JPanel();
        JPanel forEmp = new JPanel();
        JPanel forHous = new JPanel();
        
        JPanel forStart = new JPanel();
        JPanel forEnd = new JPanel();
        JPanel forWalkTime = new JPanel();
        JPanel forWaitTime = new JPanel();
        JPanel forTotalTime = new JPanel();
        JPanel forTransitTime = new JPanel();
        JPanel forWalkDistance = new JPanel();
        JPanel forStartTime = new JPanel();
        JPanel forEndTime = new JPanel();
        JPanel forDriveTime = new JPanel();

        JPanel forButton = new JPanel();
        
        forYear.setLayout(new FlowLayout());
        forAcres.setLayout(new FlowLayout());
        forArea.setLayout(new FlowLayout());
        forPop.setLayout(new FlowLayout());
        forEmp.setLayout(new FlowLayout());
        forHous.setLayout(new FlowLayout());
        
        forStart.setLayout(new FlowLayout());
        forEnd.setLayout(new FlowLayout());
        forWalkTime.setLayout(new FlowLayout());
        forWaitTime.setLayout(new FlowLayout());
        forTotalTime.setLayout(new FlowLayout());
        forTransitTime.setLayout(new FlowLayout());
        forWalkDistance.setLayout(new FlowLayout());
        forStartTime.setLayout(new FlowLayout());
        forEndTime.setLayout(new FlowLayout());
        forDriveTime.setLayout(new FlowLayout());
        forButton.setLayout(new FlowLayout());
        
        
        acre = new JTextField(20);
        area = new JTextField(20);
        pop = new JTextField(20);
        emp = new JTextField(20);
        hous = new JTextField(20);
        
        wTime = new JTextField(20);
        wDist = new JTextField(20);
        eTime = new JTextField(20);
        sTime = new JTextField(20);
        tTime = new JTextField(20);
        dTime = new JTextField(20);
        drTime = new JTextField(20);
        wtTime = new JTextField(20);
        
        Set<Integer> keys = localTaz.keySet();
        startZone = new String[keys.size()];
        int counter = 0;
        for(Integer i : keys){
            startZone[counter] = i+"";
            counter++; 
        }
        endZone = new String[localTaz.get(1).getNumDestinations()];
        for(int a = 1; a <= endZone.length ; a++)
            endZone[a-1] = a + "";
        
        years = new String[localTaz.get(1).getNumYearData()];
        for(int a = 0; a < years.length; a++){
            years[a] = (2005 + a * 5) + "";
        }
        
        start = new JComboBox(startZone);
        end = new JComboBox(endZone);
        year = new JComboBox(years);
        
        
        
        forStart.add(new JLabel("Start (TAZ): "));
        forStart.add(start);
        forEnd.add(new JLabel("End (TAZ): "));
        forEnd.add(end);
        forYear.add(new JLabel("Year: "));
        forYear.add(year);
        
        forAcres.add(new JLabel("Acres: "));
        forAcres.add(acre);
        forArea.add(new JLabel("Area: "));
        forArea.add(area);
        forEmp.add(new JLabel("Employment: "));
        forEmp.add(emp);
        forPop.add(new JLabel("Population: "));
        forPop.add(pop);
        forHous.add(new JLabel("Households: "));
        forHous.add(hous);
        forWalkTime.add(new JLabel("Walk Time: "));
        forWalkTime.add(wTime);
        forWaitTime.add(new JLabel("Wait Time: "));
        forWaitTime.add(wtTime);
        forTotalTime.add(new JLabel("Total Time: "));
        forTotalTime.add(dTime);
        forTransitTime.add(new JLabel("Transit Time: "));
        forTransitTime.add(tTime);
        forWalkDistance.add(new JLabel("Walk Distance: "));
        forWalkDistance.add(wDist);
        forStartTime.add(new JLabel("Start Time: "));
        forStartTime.add(sTime);
        forEndTime.add(new JLabel("End Time: "));
        forEndTime.add(eTime);
        forDriveTime.add(new JLabel("Drive Time: "));
        forDriveTime.add(drTime);
        
        JButton submit = new JButton("Submit");
        submit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                int s = start.getSelectedIndex();
                int f = end.getSelectedIndex();
                int y = year.getSelectedIndex();
                int actYear = 2005 + 5*y;
                TAZ taz = localTaz.get(Integer.parseInt(startZone[s]));
                String fin = endZone[f];
                acre.setText(taz.getAcres() + "");
                area.setText(taz.getArea() + "");
                emp.setText(taz.getEmployment(actYear) + "");
                hous.setText(taz.getHouseholds(actYear) + "");
                pop.setText(taz.getPopulation(actYear) + "");
                wTime.setText(taz.getWalkTime(fin) + "");
                wtTime.setText(taz.getWaitTime(fin) + "");
                dTime.setText(taz.getTotalTime(fin) + "");
                tTime.setText(taz.getTransitTime(fin) + "");
                wDist.setText(taz.getWalkDistance(fin) + "");
                sTime.setText(taz.getStart(fin) + "");
                eTime.setText(taz.getEnd(fin) + "");
                drTime.setText(taz.getDriveTime(fin) + "");
            }
        });
        
        
        top.add(forStart);
        top.add(forEnd);
        top.add(forYear);
        mainPanel.add(top);
        mainPanel.add(new JPanel());
        mainPanel.add(new JPanel());
        mid.add(forAcres);
        mid.add(forArea);
        mid.add(forPop);
        mid.add(forHous);
        mid.add(forEmp);
        mainPanel.add(mid);
        bot.add(forStartTime);
        bot.add(forEndTime);
        bot.add(forTotalTime);
        bot.add(forTransitTime);
        bot.add(forWalkTime);
        bot.add(forWalkDistance);
        bot.add(forWaitTime);
        bot.add(forDriveTime);
        mainPanel.add(bot);
        mainPanel.add(submit);
        mainFrame.add(mainPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        
    }

}
