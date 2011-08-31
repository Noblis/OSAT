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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.opentripplanner.api.ws.TAZ;

import calculations.ModelCalculator;


public class MainFrame extends JFrame{
	
	private HashMap<String, TAZ> tazs;
	private DataPanel data_panel;
	private HashMap<String, Double> transportation_access, automotive_access;
	private ModelCalculator model_calculator;
	
	public MainFrame(String path, HashMap<String, TAZ> loc, HashMap<String, Double> transAccs, HashMap<String, Double> autoAccs){
		
		final MapPanel map_panel = new MapPanel(path);
		final JScrollPane scroll = new JScrollPane();
		
		//Add the map panel which displays the region to a scrollpane so that the user has the ability
		//to zoom into the map as well as pan around the map
		scroll.setViewportView(map_panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		transportation_access = transAccs;
		automotive_access = autoAccs;
		tazs = loc;
		
		data_panel = new DataPanel();
		data_panel.setData(tazs, transportation_access, automotive_access);
		model_calculator = new ModelCalculator(tazs, transAccs, autoAccs, map_panel.getShapes());
		
		
		map_panel.addMouseListener(new MouseListener() {

			//Init fields for capturing how long inbetween clicks
			private long elapsed = -1;
			private Point off = new Point(-1,-1);
			private boolean zooming = false;


			public void mouseClicked(MouseEvent event) {
				
				//If it's the first time the mouse is clicked, just take the point in for reference
				if(elapsed == -1){
					elapsed = System.currentTimeMillis();
					off = event.getPoint();
				}
				else{

					long curr = System.currentTimeMillis();
					long e = curr - elapsed;
					
					zooming = false;
					
					//If the elapsed time is less then half a second between clicks
					//then zoom in on that point by a factor of 1.2
					if(e < 500){
						if(Math.abs(off.distance(event.getPoint())) < 5){
							JViewport view = scroll.getViewport();
							view.setViewPosition(map_panel.zoom(view, 5./4., event.getPoint()));
							map_panel.validate();
							zooming = true;
						}
					}

					//Resets the time and point clicked
					elapsed = curr;
					off = event.getPoint();
					
					if (zooming == false){
						try {
							
							//Caputres the point clicked and transforms it to an actual lat/long coordinate
							Point2D click = event.getPoint();
							AffineTransform t = map_panel.screenToWorldTransform();
							click.setLocation(click.getX()-1, click.getY()-1);
							Point2D tLeft = t.transform(click, null);
							click.setLocation(click.getX()+2, click.getY()+2);
							Point2D bRight = t.transform(click, null);
							double width = bRight.getX() - tLeft.getX();
							double height = tLeft.getY() - bRight.getY();
							
							//Create a larger bounds by which to see if it the click intersects with portions of the screen
							Rectangle2D bounds = new Rectangle2D.Double(tLeft.getX(), tLeft.getY(), width, height);

							HashMap<String, Shape> shapes = map_panel.getShapes();
							map_panel.resetHighlight();
							
							//Iterate through TAZ's to see if any we're clicked on
							for(String i : shapes.keySet()){
								Shape s = shapes.get(i);
								if(s.contains(bounds)){
									
									//When a TAZ is right clicked, remove it from the display and calculations
									//Does not refocus on that TAZ and model is repainted
									if((event.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
										model_calculator.updateRemove(i);
										map_panel.setModelColor(model_calculator.getModel());
										break;
									}
									
									//If not right clicked, hilight TAZ and recolor map
									model_calculator.updateCenteredAround(i);
									map_panel.setModelColor(model_calculator.getModel());
									map_panel.highlight(i);
									
									//Updates the data panel to include new TAZ information
									data_panel.update(i);
									break;
								}
							}
							map_panel.repaint();
							map_panel.revalidate();
						} catch (NoninvertibleTransformException ex) {
							ex.printStackTrace();
						}
				}

				}
			}

			//Generic methods for mouse events
			public void mouseEntered(MouseEvent event) {}
			public void mouseExited(MouseEvent event) {}
			public void mousePressed(MouseEvent event) {
				//Capture the point used for scrolling and change the cursor
				map_panel.setPressedOn(event.getPoint());
				map_panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			public void mouseReleased(MouseEvent event) {
				//Change the cursor back to the default after done scrolling
				map_panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		map_panel.addMouseMotionListener(new MouseMotionListener(){
			public void mouseDragged(MouseEvent e) {

				//Gets the current viewport and position at the top left
				JViewport view = scroll.getViewport();
				Point curr = view.getViewPosition();
				int width = view.getWidth();
				int height = view.getHeight();

				//Finds where the mouse was pressed from and where it currently is
				Point fromPoint = map_panel.getPressedOn();
				Point toPoint = e.getPoint();

				//Calculates the change in position
				int dX = toPoint.x - fromPoint.x;
				int dY = toPoint.y - fromPoint.y;

				//Calculates the new position and adjusts accordingly if it goes off the map
				Point newPos = new Point(curr.x-dX, curr.y-dY);
				if(newPos.x < 0)
					newPos.setLocation(0, newPos.getY());
				else if(newPos.x > map_panel.getWidth() - width)
					newPos.setLocation(map_panel.getWidth() - width, newPos.getY());
				if(newPos.y < 0)
					newPos.setLocation(newPos.getX(), 0);
				else if(newPos.y > map_panel.getHeight() - height)
					newPos.setLocation(newPos.getX(), map_panel.getHeight() - height);  			
				view.setViewPosition(newPos);
			}

			public void mouseMoved(MouseEvent e) {}
		});
		
		
		//When the mouse wheel is used to scroll, zoom in and out accordingly by a factor of
		//either 1.2 or .8 to make for smooth even scrolling
		map_panel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation() > 0){
					JViewport view = scroll.getViewport();
					view.setViewPosition(map_panel.zoom(view, 4./5));
					map_panel.validate();
				}
				else{
					JViewport view = scroll.getViewport();
					view.setViewPosition(map_panel.zoom(view, 5./4));
					map_panel.validate();
				}
			} 	  
		});
		
		///////////       GUI     CODE    /////////
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel forControl = new JPanel();
		JPanel keyFlow = new JPanel();
		JPanel forKey = new JPanel();
		JPanel forLabels = new JPanel();
		JPanel forEast = new JPanel();
		KeyPanel keyPan = new KeyPanel();

		
		
		ControlPanel control = new ControlPanel(model_calculator, map_panel);
		control.addActionListeners();
		forControl.setLayout(new FlowLayout());
		forControl.add(control);
		forControl.setBorder(BorderFactory.createLoweredBevelBorder());
		
		forLabels.setLayout(new FlowLayout());
		
		JLabel keyLabel = new JLabel("Low Value               High Value");
		keyLabel.setFont(new Font("sansserif", Font.BOLD, 14));
		forLabels.add(keyLabel);
		
		keyFlow.setLayout(new FlowLayout());
		forKey.setLayout(new BoxLayout(forKey, BoxLayout.PAGE_AXIS));
		forKey.add(forLabels);
		forKey.add(keyPan);
		keyFlow.add(forKey);
		
		forEast.setLayout(new BoxLayout(forEast, BoxLayout.Y_AXIS));
		forEast.add(data_panel);
		forEast.add(new JPanel());
		forEast.add(forControl);
		forEast.add(keyFlow);
		forEast.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		//forEast.setBackground(new Color(224,238,224));
		forEast.setBackground(new Color(238,232,170));

		
		add(forEast, BorderLayout.EAST);
		add(scroll, BorderLayout.CENTER);
		setTitle("Accessibility Analysis Tool");
		pack();
		setLocationRelativeTo(null);
		
	}


}
