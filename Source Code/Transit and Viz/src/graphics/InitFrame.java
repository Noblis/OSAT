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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


/**
 * 
 * Frame which appears while the XML data is loaded into the program
 * When fired, the progress bar changes
 *
 */
public class InitFrame extends JFrame{
	
	JProgressBar progress;
	int count;
	
	
	public InitFrame(){
		
	}
	
	public void invoke(){
		setVisible(true);
		validate();
		repaint();
	}
	
	public void setVariables(int num, String logoPath){
		count = 0;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		InitPane pane = new InitPane(logoPath);
		progress= new JProgressBar(JProgressBar.HORIZONTAL, 0, num);
		progress.setStringPainted(true);
		progress.addPropertyChangeListener(new PropertyChangeListener(){			
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("Value"))
				progress.setValue((Integer)evt.getNewValue());	
			}
		});
		
		mainPanel.add(pane, BorderLayout.CENTER);
		mainPanel.add(progress, BorderLayout.SOUTH);
		
		add(mainPanel);
		setUndecorated(true);
		pack();
		setLocationRelativeTo(null);
	}
	
	public void changed(){
		progress.firePropertyChange("Value", count, count+1);
		count++;
	}

}
