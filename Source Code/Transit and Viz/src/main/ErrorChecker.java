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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import org.opentripplanner.api.ws.TAZ;
import org.opentripplanner.api.ws.XMLFilter;

//import com.sun.awt.AWTUtilities;

/**
 * 
 * Originally used for checking the time calculated by WMATA tripplanner against
 * the value that was calculated by OTP, no purpose outside of that
 *
 */
public class ErrorChecker {
	public static void main(String [] args) throws FileNotFoundException{
		
		File dir = new File("C:/DCData/");
		File[] files = dir.listFiles(new XMLFilter());

		HashMap<Integer, TAZ>localTaz = new HashMap<Integer, TAZ>();

		for(File f : files){
			String path = f.getPath();
			TAZ t = new TAZ(path);
			localTaz.put(Integer.parseInt(t.getTAZ()), t);

		}
		
		int count = 0;
		int num = 0;
		Scanner reader = new Scanner(new File("C:/datapoints.txt"));
		while(reader.hasNext()){
			num ++;
			String rLine = reader.nextLine();
			int ind = rLine.indexOf("-");
			int ind2 = rLine.indexOf(" ");
			int from = Integer.parseInt(rLine.substring(0, ind));
			String to = rLine.substring(ind+1, ind2);
			int time = Integer.parseInt(rLine.substring(ind2).trim());
			int calcTime = (int) (localTaz.get(from).getTotalTime(to) / 1000. / 60.);
			//System.out.println(time + "   " + gotThere + "    " + diff + "    " + act + "   vs    " + calcTime);
			int diff = calcTime - time;
			count += diff;
			if(diff < 14 && diff > -14)
			System.out.println(count/(double)num + "\t" + num + "\t" + calcTime + "\t" + time + "\t" + diff);
		}
		
		
		
	}

}
