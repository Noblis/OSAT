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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Simply paints a JPanel the spectrum of colors used in the heatmap
 * so that the user has a concept of what is a high and low value
 *
 */
public class KeyPanel extends JPanel{
	
	public KeyPanel(){
		setPreferredSize(new Dimension(254, 20));
		setBorder(BorderFactory.createEtchedBorder(Color.black, Color.white));
	}
	
	public void paintComponent(Graphics g){
		for(int x = 0; x < 254; x = x + 2){
			g.setColor(new Color(x, 255, 0));
			g.drawLine(x/2, 0, x/2, 20);
		}
		for(int x = 254; x >= 0; x = x - 2){
			g.setColor(new Color(255, x, 0));
			g.drawLine(127 + (254-x)/2, 0, 127 + (254-x)/2, 20);
		}
	}
	

}
