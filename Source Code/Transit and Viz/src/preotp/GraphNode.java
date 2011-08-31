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

package preotp;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GraphNode {
	private int nodeID;
	private Point2D.Double centroid;
	private ArrayList<Point2D.Double> polygon;
	private boolean isTAZ;
	private ArrayList<Integer> adjTAZ;
	private ArrayList<Integer> adjBG;
	private double area;
	private Path2D.Double polygon_path;
	private int TAZ; //to identify which TAZ a split BG is part of
	private int BG;
	private String tract;
	
	public GraphNode(int nodeID, Point2D.Double centroid, ArrayList<Point2D.Double> polygon, boolean isTAZ, int TAZ, String tract, int BG){
		this.nodeID=nodeID;
		this.polygon=polygon;
		this.isTAZ=isTAZ;
		this.tract=tract;
		this.BG=BG;
		this.TAZ=TAZ;
		adjTAZ=new ArrayList<Integer>();
		adjBG=new ArrayList<Integer>();
		if(polygon !=null){
			area = calcArea(polygon);
			polygon_path=makePath(polygon);
		}
		if(centroid==null){
			this.centroid=findCentroid(polygon);
		}else{
			this.centroid=centroid;
		}
	}
	
	
	public void setTAZ(int TAZ){
		this.TAZ=TAZ;
	}
	public int getTAZ(){
		return TAZ;
	}
	public void setBG(int BG){
		this.BG=BG;
	}
	public int getBG(){
		return BG;
	}
	public void setTract(String tract){
		this.tract=tract;
	}
	public String getTract(){
		return tract;
	}
	public void setNodeID(int id){
		nodeID=id;
	}
	public int getNodeID(){
		return nodeID;
	}
	public Point2D.Double getCentroid(){
		return centroid;
	}
	public void setCentroid(Point2D.Double p){
		centroid=p;
	}
	public ArrayList<Point2D.Double> getPolygon(){
		return polygon;
	}
	public void setPolygon(ArrayList<Point2D.Double> p){
		polygon=p;
		polygon_path=makePath(p);
	}
	public boolean isTAZ(){
		return isTAZ;
	}
	public ArrayList<Integer> getAdjTAZ(){
		return adjTAZ;
	}
	public ArrayList<Integer> getAdjBG(){
		return adjBG;
	}
	public double getArea(){
		return area;
	}
	public Path2D.Double getPolygonPath(){
		return polygon_path;
	}
	public String toString(){
		return "";
	}
	public void addAdjTAZ(int i){
		if(!adjTAZ.contains(i))
			adjTAZ.add(i);
	}
	public void addAdjBG(int i){
		if(!adjBG.contains(i))
			adjBG.add(i);
	}
	public static double calcArea(ArrayList<Point2D.Double> pts){
		double sum=0;
		for(int z=0;z<pts.size()-1;z++){ //last pt in ArrayList same as first pt
			sum+=pts.get(z).getX() * pts.get((z+1)%pts.size()).getY() - pts.get(z).getY() * pts.get((z+1)%pts.size()).getX();
		}
		return Math.abs(sum)/2;
	}
	public static Path2D.Double makePath(ArrayList<Point2D.Double> pts){
		Path2D.Double p = new Path2D.Double();
		p.moveTo(pts.get(0).getX(),pts.get(0).getY());
		for(int y=1;y<pts.size();y++){
			p.lineTo(pts.get(y).getX(),pts.get(y).getY());
		}
		return p;
	}
	public Point2D.Double findCentroid(ArrayList<Point2D.Double> al){
		double x=0.0; double y=0.0;

		for(int z=0;z<al.size()-1;z++){ //last pt in ArrayList same as first pt
			
			x=x+(al.get(z).getX()+al.get((z+1)%al.size()).getX())*((al.get(z).getX()*al.get((z+1)%al.size()).getY())-(al.get((z+1)%al.size()).getX()*al.get(z).getY()));
			y=y+(al.get(z).getY()+al.get((z+1)%al.size()).getY())*((al.get(z).getX()*al.get((z+1)%al.size()).getY())-(al.get((z+1)%al.size()).getX()*al.get(z).getY()));
		}
		x/=(6*area);
		y/=(6*area);
		return new Point2D.Double(x,y);
	}
}
