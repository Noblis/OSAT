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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.opentripplanner.api.ws.XMLFilter;

public class DataAppender2011 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//hard-coded file/directory paths
		String popdata = "C:/Summer2011/King_Data/CensusData/2000/dc_dec_2000_sf3_u_data1.txt";
		String empdata = "C:/Summer2011/King_Data/CensusData/wa_wac_S000_JT00_2009.csv";
		String xmldirPath = "C:/Summer2011/King_Data/OTPOutput/BG/";
		String xmloutputdir = "C:/Summer2011/King_Data/OTPOutput/BG_DataAdded/";
		
		
		HashMap<String,Integer> pops = new HashMap<String,Integer>();
		HashMap<String,Integer> emps = new HashMap<String,Integer>();
		HashSet<String> bgs = new HashSet<String>();
		HashMap<File, String> filemap = new HashMap<File, String>();
		
		try{
			//read xml files to know which block groups to pull data for
			File dir = new File(xmldirPath);
			System.out.println(xmldirPath);
			File[] files = dir.listFiles(new XMLFilter());
			System.out.println(files.length + " files");
			for(File f : files){
				String tmp = f.getName().substring(f.getName().indexOf("-")+1,f.getName().indexOf("."));
				bgs.add(tmp);
				filemap.put(f,tmp);
			}
			
			//find populations
			String s;
			String id;
			String[] split;
			FileInputStream fstream = new FileInputStream(popdata);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			s=br.readLine();
			split=s.split("\\|");
			int idIndex=0, popIndex=0;
			for(int z=0;z<split.length;z++){
				if(split[z].equals("GEO_ID2")){
					idIndex=z;
				}
				if(split[z].equals("P001001")){
					popIndex=z;
				}
			}
			br.readLine();
			while((s = br.readLine()) != null){
				split=s.split("\\|");
				id=split[idIndex].substring(5,split[idIndex].length());
				if(bgs.contains(id)){
					if(split.length <= popIndex){
						pops.put(id,0);
					}else{
						pops.put(id,Integer.parseInt(split[popIndex]));
					}
				}
			}
			System.out.println("Number of pop matches: "+pops.size());
			in.close();
			
			//find employment
			fstream = new FileInputStream(empdata);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			s=br.readLine();
			split=s.split(",");
			int empIndex=0;
			for(int z=0;z<split.length;z++){
				if(split[z].equals("w_geocode")){
					idIndex=z;
				}
				if(split[z].equals("C000")){
					empIndex=z;
				}
			}
			while((s = br.readLine()) != null){
				split=s.split(",");
				id=split[idIndex].substring(5,split[idIndex].length()-3);
				if(bgs.contains(id)){
					if(emps.containsKey(id)){
						emps.put(id,Integer.parseInt(split[empIndex])+emps.get(id));
					}else{
						emps.put(id,Integer.parseInt(split[empIndex]));
					}
				}
			}
			System.out.println("Number of emp matches: "+pops.size());
			in.close();
			
			//append data
			int count=0;
			for(File f : files){
				String key = filemap.get(f);
				SAXBuilder build = new SAXBuilder();
		        Document d = null;
		        d = build.build(f);
		        Element root = d.getRootElement();
		        Element population = root.getChild("population_data");
		        Element employment = root.getChild("employment_data");
		        
		        if(population.getChild("year_projection") == null){
		        	Element yrprojpop = new Element("year_projection");
		        	yrprojpop.setAttribute("year","2010");
		        	Element popval = new Element("population");
		        	popval.setAttribute("val",pops.get(key)+"");
		        	yrprojpop.addContent(popval);
		        	population.addContent(yrprojpop);
		        }else if(population.getChild("year_projection").getChild("population") == null){
		        	Element popval = new Element("population");
		        	popval.setAttribute("val",pops.get(key)+"");
		        	population.getChild("year_projection").addContent(popval);
		        }else{
		        	population.getChild("year_projection").getChild("population").setAttribute("val",pops.get(key)+"");
		        }
		        if(employment.getChild("year_projection") == null){		        	
		        	Element yrprojemp = new Element("year_projection");
		        	yrprojemp.setAttribute("year","2010");
		        	Element empval = new Element("employment");
		        	empval.setAttribute("val",emps.get(key)+"");
		        	yrprojemp.addContent(empval);
		        	employment.addContent(yrprojemp);
		        }else if(employment.getChild("year_projection").getChild("employment") == null){
		        	Element empval = new Element("employment");
		        	empval.setAttribute("val",emps.get(key)+"");
		        	employment.getChild("year_projection").addContent(empval);
		        }else{
		        	employment.getChild("year_projection").getChild("employment").setAttribute("val",emps.get(key)+"");
		        }
		        XMLOutputter out = new XMLOutputter();
				out.setFormat(Format.getPrettyFormat());
				out.output(d, new FileOutputStream(xmloutputdir+f.getName()));
				count++;
				if(count%50==0){
					System.out.println("Progress: "+count+" files complete");
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
