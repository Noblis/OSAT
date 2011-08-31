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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import calculations.KMLReader;



/**
 * 
 * Defines a JPanel to paint the relevant shapes and colors to the screen
 *
 */
public class MapPanel extends JPanel implements Scrollable{
	
	private Rectangle2D viewport;
	private double aspectRatio;
	private int prefHeight;
	private int prefWidth;
	private Point pressed;
	private HashMap<String, Shape> shapes;
	private ArrayList<String> toHighlight = new ArrayList<String>();
	private HashMap<String, Color> toColor = new HashMap<String, Color>();
	
	public MapPanel(String path){
		
		//Reads kml file containing shape data, and extracts polygons as Shapes
		KMLReader read = new KMLReader(path);
		shapes = read.getPolygons();
		
		setBackground(new Color(47,47,79));
		
		setViewport(Viewport.boundingBox(shapes));
		
	}
	
	public void setViewport(Rectangle2D viewport) {
		this.viewport = viewport;
		double h = viewport.getHeight();
		double w = viewport.getWidth();
		
		//Sets aspect ratio to an appropriate value and adjusts for wider regions
		aspectRatio = w/h;
		if(w > 2 * h)
			prefHeight = 500;
		else
			prefHeight = 800;
		prefWidth = (int) (prefHeight * aspectRatio);
		this.setPreferredSize(new Dimension(prefWidth, prefHeight));

	}
	
	//Using screen size, correctly transforms and lat and long coordinate
	//into the associated screen coordinate
	public AffineTransform worldToScreenTransform() {
		AffineTransform t = new AffineTransform();

		double w = viewport.getWidth();
		double h = viewport.getHeight();

		t.translate(0, getHeight());
		t.scale(getWidth()/w, -getHeight() / h);
		t.translate(-viewport.getX(), -viewport.getY());

		return t;
	}
	
	//Accepts the new model to color the screen as
	public void setModelColor(HashMap<String, Color> locColor){
		toColor = locColor;
	}

	public AffineTransform screenToWorldTransform() throws NoninvertibleTransformException {
		return worldToScreenTransform().createInverse();
	}
	
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visible, int orientation, int dir) {
		if(orientation == SwingConstants.HORIZONTAL)
			return (int)visible.getWidth();
		else
			return (int)visible.getHeight();
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visible, int orientation, int dir) {
		return 10;
	}

	
	public void setPressedOn(Point p){
		pressed = p;	
	}

	public Point getPressedOn() {
		return pressed;
	}


	public Point zoom(JViewport view, double z){
		Dimension d = this.getSize();
		Point p = view.getViewPosition();
		
		//Find the middle of the screen
		int midW = (int) (view.getWidth() / 2.);
		int midH = (int) (view.getHeight() / 2.);
		
		//Translate and zoom accordingly
		p.translate(midW, midH);
		int w = (int) (d.getWidth() * z);
		int h = (int) (d.getHeight() * z);
		if(w >= view.getWidth()){
		int x1 = (int)(p.getX()*z);
		int x2 = (int) (p.getY()*z);
		p = new Point(x1, x2);
		
		//Translate back to the top left corner
		p.translate(-midW, -midH);
		if(z > 1)
			w++;
		this.setPreferredSize(new Dimension(w, h));
		this.setSize(w, h);
		return p;
		}
		else
			return view.getViewPosition();

	}
	
	public Point zoom(JViewport view, double z, Point mid){
		Dimension d = this.getSize();
		Point p = mid;
		int midW = (int) (view.getWidth() / 2.);
		int midH = (int) (view.getHeight() / 2.);
		int w = (int) (d.getWidth() * z);
		int h = (int) (d.getHeight() * z);
		int x1 = (int)(p.getX()*z);
		int x2 = (int) (p.getY()*z);
		p = new Point(x1, x2);
		p.translate(-midW, -midH);
		if(z > 1)
			w++;
		this.setPreferredSize(new Dimension(w, h));
		this.setSize(w, h);
		return p;
	}
	
	public void paintComponent(Graphics gContext) {
		super.paintComponent(gContext);
		Graphics2D g2 = (Graphics2D)gContext;
		
		//For each shape to be displayed, transform it to the screen and color it
		//as per what is stored in the color model
		for(String i : shapes.keySet()){
			Shape s = shapes.get(i);
			Shape altered = worldToScreenTransform().createTransformedShape(s);
			
			if(toColor.size() > 0){
				g2.setColor(toColor.get(i));
			}
			
			//If no colors are returned, set the color to green
			else
				g2.setColor(new Color(0,205,0));
			g2.fill(altered);
			
			//Outline each TAZ in black
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(1));
			g2.draw(altered);
			

		}
		
		//Highlights the selected TAZ in white
		for(String i : toHighlight){
			g2.setStroke(new BasicStroke((float) 1.5));
			g2.setColor(Color.white);
			g2.draw(worldToScreenTransform().createTransformedShape(shapes.get(i)));
		}

	}
	
	public HashMap<String, Shape> getShapes(){
		return shapes;
	}
	
	public void highlight(String i){
		toHighlight.add(i);
	}
	
	public void resetHighlight(){
		toHighlight = new ArrayList<String>();
	}

}
