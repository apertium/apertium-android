/*
 * Copyright (C) 2012 Arink Verma
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */


package org.apertium.android.Internet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apertium.Translator;
import org.apertium.android.helper.Prefs;

public class InternetManifest {

	private List<ManifestRow> manifestRowList = null;

	public InternetManifest(String ManifestFile) throws IOException{
		manifestRowList = new ArrayList<ManifestRow>();
		InputStream is = new FileInputStream(Prefs.TEMP_DIR+"/"+Prefs.MANIFEST_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String input="";
    	if (is!=null) {
    		while ((input = reader.readLine()) != null) {
    			/*PairName	JarURL FileName ModeID
    			 * apertium-af-nl	https://apertium.svn.sourceforge.net/svnroot/apertium/builds/apertium-af-nl/apertium-af-nl.jar	file:apertium-af-nl-0.2.0.tar.gz	af-nl, nl-af*/
    			String[] Row = input.split("\t");
    			if(Row.length>2){
    				manifestRowList.add(new ManifestRow(Row[0],Row[1],Row[2],Row[3]));
    			}
    		}
    	}
	}

	public int size(){
		return manifestRowList.size();
	}

	public ManifestRow get(int location){
		return manifestRowList.get(location);
	}

	public String[] PackageTitleList(){
		String[] list = new String[manifestRowList.size()];
		for(int i=0;i<manifestRowList.size();i++){
			list[i] = Translator.getTitle(manifestRowList.get(i).getpackageMode());
		}
		return list;
	}


}
