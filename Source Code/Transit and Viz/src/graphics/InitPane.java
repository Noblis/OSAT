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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * Paints whatever logo or picture you want to see at startup
 * 
 *
 */
public class InitPane extends JPanel {
	
	Image logo;
	
	public InitPane(String path){
		
		try {
			logo = ImageIO.read(new File(path));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Failed");
			e.printStackTrace();
		}
		
		setPreferredSize(new Dimension(logo.getWidth(null), logo.getHeight(null)));
		
	}
	
	public void paint(Graphics g){
		g.drawImage(logo, 0,0, null);
	}

}
