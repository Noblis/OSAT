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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * 
 * This file is not used in the main GUI, simply an example of
 * how block data and statistics was aggregated in order to add
 * the data to the TAZ XML files
 *
 */
public class PopulationHousing {
	public static void main(String [] args) throws IOException{
		
		String path = "C:/taz-block.txt";
		Scanner reader = new Scanner(new File(path));
		HashMap<Long, Integer> id2taz = new HashMap<Long, Integer>();
		HashMap<Integer, Integer> counter = new HashMap<Integer, Integer>();
		while(reader.hasNext()){
			String line = reader.nextLine();
			String[] split = line.split("\t");
			int taz = Integer.parseInt(split[0]);
			if(counter.containsKey(taz))
				counter.put(taz, counter.get(taz) + 1);
			else
				counter.put(taz, 1);
			long id = 0;
			id = Long.parseLong(split[1]);
			id2taz.put(id, taz);
		}
		
		String secPath = "C:/blockdata.txt";
		reader = new Scanner(new File(secPath));
		
		
		//Block 4008, Block Group 4, Census Tract 2, King County, Washington|44|18
		String bl = "Block";
		String blgrp = "Block Group";
		String cens = "Census Tract";
		String wash = "Washington";
		
		HashMap<Long, Integer> blockPop = new HashMap<Long, Integer>();
		HashMap<Long, Integer> blockHous = new HashMap<Long, Integer>();
		
		reader.nextLine();
		reader.nextLine();
		while(reader.hasNext()){
			String line = reader.nextLine();
			int blInd = line.indexOf(bl);
			int grpInd = line.indexOf(blgrp);
			int censInd = line.indexOf(cens);
			int washInd = line.indexOf(wash);
			int block = Integer.parseInt(line.substring(blInd + bl.length() + 1, grpInd-2));
			int blockGroup = Integer.parseInt(line.substring(grpInd + blgrp.length() + 1, censInd-2));
			String preCen = line.substring(censInd + cens.length() + 1, washInd-15);
			int comInd = preCen.indexOf(".");
			int census = 0;
			if(comInd != -1){
				census = Integer.parseInt(preCen.substring(0,comInd) + preCen.substring(comInd+1, preCen.length()));
			}
			else
				census = Integer.parseInt(preCen) * 100;
			String test = line.substring(washInd + wash.length() + 1);
			int ind = test.indexOf("|");
			
			int houses = 0, pop = 0;
			try{
			pop = Integer.parseInt(test.substring(0,ind));
			}
			catch(Exception e){}
			try{
			houses = Integer.parseInt(test.substring(ind+1));
			}
			catch(Exception e){}
			long id = Long.parseLong(census +""+blockGroup+""+block);
			blockPop.put(id, pop);
			blockHous.put(id, houses);

		}
		
		HashMap <Integer, Integer> tazHous = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> tazPop = new HashMap<Integer, Integer>();
		
		//int count = 0;
		for(Long id : blockPop.keySet()){
			int t = id2taz.get(id);
			if(tazPop.containsKey(t)){
				int locPop = tazPop.get(t);
				if(blockPop.get(id) > 600)
					System.out.println(id);
				tazPop.put(t, locPop + blockPop.get(id));
				int locHous = tazHous.get(t);
				tazHous.put(t, locHous + blockHous.get(id));
			}
			else{
				tazPop.put(t, blockPop.get(id));
				tazHous.put(t, blockHous.get(id));
			}	
			
		}
		
		//Alter this value for where to read in jobs by block
		reader = new Scanner(new File("C:/blockjobs.txt"));
		HashMap<Integer, Integer> tazEmp = new HashMap<Integer, Integer>();
		HashSet<Integer> bigEmp = new HashSet<Integer>();
		while(reader.hasNext()){
			String line = reader.nextLine();
			String idstr = line.substring(5,11) + line.charAt(11) + line.substring(11,15);
			long id = Long.parseLong(idstr);
			int jobs = Integer.parseInt(line.substring(16));
			int taz = id2taz.get(id);
			if(jobs > 400)
				bigEmp.add(taz);
			
			if(!tazEmp.containsKey(taz))
				tazEmp.put(taz, jobs);
			else
				tazEmp.put(taz, tazEmp.get(taz) + jobs);
		}
		
		for(Integer i : counter.keySet())
			System.out.println(i + " " + counter.get(i));

		
	}
	

}
