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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * 
 * Not an essential class, used to read through a directory
 * load the associated XML data and see which have walking distances
 * over 1 mile long
 *
 */
public class FileChecker {

    static HashMap<String, TAZ> localTaz;


    public static void main(String [] args) throws FileNotFoundException{

        //Change this value in order to load a different directory
        String directory = "C:/output/";

        File dir = new File(directory);
        File[] files = dir.listFiles(new XMLFilter());

        localTaz = new HashMap<String, TAZ>();

        for(File f : files){
            String path = f.getPath();
            TAZ t = new TAZ(path);
            localTaz.put(t.getTAZ(), t);
        }
        
        //Change this value to change the output file
        PrintWriter results = new PrintWriter(new File("C:/longwalk.txt"));
        
        for(String i : localTaz.keySet()){
            TAZ t = localTaz.get(i);
            for(String j : localTaz.keySet())
                if(t.getWalkDistance(j) > 1609.344){
                    results.println(i + "-" + j + " : " + t.getWalkDistance(j));
                }
                    
                    
        }
        results.flush();
        results.close();
    }

}
