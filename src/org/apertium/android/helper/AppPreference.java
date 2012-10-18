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

package org.apertium.android.helper;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


public class AppPreference   {
	private static final String TAG = "AppPreference";
	
	//Directories path
	public static final String BASE_DIR = Environment.getExternalStorageDirectory().toString() +"/apertium";
	public static final String JAR_DIR = Environment.getExternalStorageDirectory().toString() +"/apertium/jars";
	public static final String TEMP_DIR  = Environment.getExternalStorageDirectory().toString() +"/apertium/temp";
	public static final String MANIFEST_FILE  = "Manifest";
	public static final String SVN_MANIFEST_ADDRESS = "http://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";

	public static final String SUPPORT_MAIL = "arinkverma@gmail.com";

	//Preferences name
	public static final String PREFERENCE_NAME = "ore.apertium.Pref";
	
	private Context context = null;
	private SharedPreferences prefs = null;
	private SharedPreferences.Editor editor;
	
	public AppPreference(Context ctx){
		this.context = ctx;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		this.editor = prefs.edit();
	}
	
/*Cache Preference*/
	public static final String CachePref = "CachePref";
	
	public void setCacheEnabled(boolean y){
		 editor.putBoolean(CachePref, y);
		 editor.commit();
	}
	
	public boolean isCacheEnabled(){
        return prefs.getBoolean(CachePref, false);
	}
	

/*DisplayMark Preference*/	
	public static final String MarkPref = "MarkPref";
	
	public void setDisplayMark(boolean y){
		 editor.putBoolean(MarkPref, y);
		 editor.commit();
	}
	
	public boolean isDisplayMarkEnabled(){
		return prefs.getBoolean(MarkPref, false);
	}
	
	
/*ClipBoardPush Preference*/	
	public static final String ClipBoardGetPref = "ClipGetPref";
	public static final String ClipBoardPushPref = "ClipPushPref";
	
	public void setClipBoardPush(boolean y){
		 editor.putBoolean(ClipBoardPushPref, y);
		 editor.commit();
	}
	
	public boolean isClipBoardPushEnabled(){
		return prefs.getBoolean(ClipBoardPushPref, false);
	}
	
	public void setClipBoardGet(boolean y){
		 editor.putBoolean(ClipBoardGetPref, y);
		 editor.commit();
	}
	
	public boolean isClipBoardGetEnabled(){
		return prefs.getBoolean(ClipBoardGetPref, false);
	}
	

/*Crash Preference*/	
	public static final String CrashPref = "CrashPref";
	
	public void ReportCrash(String y){
		 editor.putString(CrashPref, y);
		 editor.commit();
	}
	
	public String GetCrashReport(){
		return prefs.getString(CrashPref, null);
	}
	
	public void ClearCrashReport(){
		editor.putString(CrashPref, null);
		editor.commit();
	}
	
	
	
	//Last state
	private static final String LocalePref = "LocalePref";
	private static final String LastJARDirChangedPref = "LastJARDirChangedPref";
	
	
	public boolean isStateChanged(){
		String lastLocale = prefs.getString(LocalePref, "");
		String currentLocale = Locale.getDefault().getDisplayLanguage();
		
		Log.i(TAG,"lastLocale = "+lastLocale+", currentLocale = "+currentLocale);
		if(!lastLocale.equals(currentLocale)){
			return true;
		}
		
		File f = new File(JAR_DIR);
		
		String LastModified = f.lastModified()+"";
		String SavedLastModified = prefs.getString(LastJARDirChangedPref,"");
		
		Log.i(TAG,"LastModified = "+LastModified+", SavedLastModified = "+SavedLastModified);
		if(!LastModified.equals(SavedLastModified)){
			return true;
		}
		
		return false;
		
	}
	
	public void SaveState(){
		editor.putString(LocalePref, Locale.getDefault().getDisplayLanguage());
		
		File f = new File(JAR_DIR);
		editor.putString(LastJARDirChangedPref,f.lastModified()+"");
		
		Log.i(TAG,"lastLocale = "+Locale.getDefault().getDisplayLanguage()+", LastJARDirChanged = "+f.lastModified());
		editor.commit();
	
	}
}
