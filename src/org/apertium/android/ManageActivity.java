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

/**
 * ManageActivity.java
 * Manage setting of application
 * @author Arink Verma
 */
package org.apertium.android;

import org.apertium.android.database.DatabaseHandler;
import org.apertium.android.filemanager.FileChooserActivity;
import org.apertium.android.helper.AppPreference;
import org.apertium.android.widget.WidgetConfigActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
 
public class ManageActivity extends PreferenceActivity {
	
	ProgressDialog progressDialog = null;
	private static Handler handler = null;
	AppPreference appPreference = null;
	Activity thisActivity = null;
        @SuppressWarnings("deprecation")
		@Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        	thisActivity = this;
            addPreferencesFromResource(R.xml.setting);
            this.setTheme(R.style.PreferenceTheme); 
            
            handler = new Handler();
            appPreference = new AppPreference(this);

   
            /*List Package*/
			Preference listPref = (Preference) findPreference("listPref");
			listPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
        			Intent myIntent1 = new Intent(ManageActivity.this, ModeManageActivity.class);
        			ManageActivity.this.startActivity(myIntent1);
                    return true;
                }
			});
			
			/*Install Package*/
            Preference installLocalPref = (Preference) findPreference("installLocalPref");
            installLocalPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
        			Intent myIntent1 = new Intent(ManageActivity.this, FileChooserActivity.class);
        			ManageActivity.this.startActivity(myIntent1);
                    return true;
                }
            });
            
            Preference installSVNPref = (Preference) findPreference("installSVNPref");
            installSVNPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
        			Intent myIntent1 = new Intent(ManageActivity.this, DownloadActivity.class);
        			ManageActivity.this.startActivity(myIntent1);
        			return true;
                }
            });
				
      
            /*Cache Enable */
            Preference CachePref = (Preference) findPreference(AppPreference.CachePref);
            CachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    boolean CachePreference = prefs.getBoolean(AppPreference.CachePref, appPreference.isCacheEnabled());            
                    Log.i(AppPreference.CachePref,CachePreference+"");
        			return true;   
                }
            });
            
            
            /*Push Clip Enable */
            Preference ClipPushPref = (Preference) findPreference(AppPreference.ClipBoardPushPref);
            ClipPushPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    boolean ClipPushPreference = prefs.getBoolean(AppPreference.ClipBoardPushPref, appPreference.isClipBoardPushEnabled());            
                    Log.i(AppPreference.ClipBoardPushPref,ClipPushPreference+"");
        			return true;   
                }
            });
            
            /*Get Clip Enable */
            Preference ClipGetPref = (Preference) findPreference(AppPreference.ClipBoardGetPref);
            ClipGetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    boolean ClipGetPreference = prefs.getBoolean(AppPreference.ClipBoardGetPref, appPreference.isClipBoardGetEnabled());            
                    Log.i(AppPreference.ClipBoardGetPref,ClipGetPreference+"");
        			return true;   
                }
            });
            
            /*DisplayMark Enable */
            Preference MarkPref = (Preference) findPreference(AppPreference.MarkPref);
            MarkPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    boolean MarkPreference = prefs.getBoolean(AppPreference.MarkPref, appPreference.isDisplayMarkEnabled());            
                    Log.i(AppPreference.MarkPref,MarkPreference+"");
        			return true;   
                }
            });
            
            
  
            /*Widget */
            Preference WidgetPref = (Preference) findPreference("WidgetPref");
            WidgetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
        			Intent myIntent1 = new Intent(ManageActivity.this, WidgetConfigActivity.class);
        			ManageActivity.this.startActivity(myIntent1);
                    return true;
                }
            });
            
            /*Update DB */
            Preference UpdateDBPref = (Preference) findPreference("UpdateDBPref");
            UpdateDBPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        		public boolean onPreferenceClick(Preference preference) {
        			  progressDialog = new ProgressDialog(thisActivity);
        			    progressDialog.setTitle(getString(R.string.updating_db));
        			    progressDialog.setMessage(getString(R.string.working));
        			    progressDialog.setCancelable(false);
        			    progressDialog.show();
        			 Thread t = new Thread() {
        				 @Override
        			     public void run() {
        					DatabaseHandler DB = new DatabaseHandler(thisActivity);
			        		DB.updateDB();
			        		
			        		  handler.post(new Runnable() {
			                      @Override
			                      public void run() {
			                          progressDialog.dismiss();
			                      }
			                  });
			                
	       			        }
        			        };
        			t.start();
        			
                    return true;
                }
            });
            
        }    	        
}