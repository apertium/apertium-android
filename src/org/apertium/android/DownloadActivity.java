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
 * DownloadActivity.java
 * Download package List from SVN as well as package
 *  @author Arink Verma
 */

package org.apertium.android;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apertium.Translator;
import org.apertium.android.Internet.InternetManifest;
import org.apertium.android.Internet.ManifestRow;
import org.apertium.android.filemanager.FileManager;
import org.apertium.android.helper.Prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadActivity extends Activity  implements OnClickListener{
	public static final String TAG = "DownloadActivity";

    ProgressDialog progressDialog;
    private DownloadActivity thisActivity;

    private final int HTML_PARSING_DONE = 2;

    private boolean isListLoaded = false;

    private ListView listView;
    private int FILE_SIZE = 0;
    private String ModifiedSince = null;
    private InternetManifest internetManifest = null;
    private ManifestRow toDownload = null;

    private Button _reloadButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.download_layout);
		FileManager.setDIR();

	    listView  = (ListView) findViewById(R.id.listView1);
	    thisActivity = this;

	    _reloadButton = (Button) findViewById(R.id.reloadButton);
	    _reloadButton.setOnClickListener(this);

	    progressDialog = new ProgressDialog(thisActivity);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.setTitle(getString(R.string.downloading)+"\n"+getString(R.string.package_list));

	    progressDialog.setCancelable(false);
	    progressDialog.show();

	    File svnCache = new File(Prefs.TEMP_DIR+"/"+Prefs.MANIFEST_FILE);
	    if(!svnCache.exists()){
	    	FileManager.DownloadRun(Prefs.SVN_MANIFEST_ADDRESS,Prefs.TEMP_DIR+"/"+Prefs.MANIFEST_FILE,thisActivity.handler);
	    }else{
	    	progressDialog.setMessage("Generating view");
        	ParseHtmlRun();
        	isListLoaded = true;
	    }
	}


	private void ParseHtmlRun(){
	    Thread t = new Thread() {
	        @Override
	        public void run() {
	        	try {
	        		internetManifest = new InternetManifest(Prefs.TEMP_DIR+"/"+Prefs.MANIFEST_FILE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	            Message msg = Message.obtain();
	            msg.what = HTML_PARSING_DONE;
	            handler.sendMessage(msg);
	        }
	    };
	    t.start();
	}


	/* Waiting for Acknowledgement from threads running */
	private final Handler handler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        switch(msg.what){
	        //HTTP connection started
		        case   FileManager.MESSAGE_CONNECTING_STARTED :
		        	Log.i(TAG,"Connecting Started");
		        	break;

		    //Download started
		        case   FileManager.MESSAGE_DOWNLOAD_STARTED :
		        	FILE_SIZE = (int)msg.arg1;
		        	ModifiedSince = (String) msg.obj;

		        	Log.d("Download","Lastmodified ="+ModifiedSince);
		        	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		        	Date resultdate = new Date(Long.parseLong(ModifiedSince));
		        	if(isListLoaded == false){
		        		progressDialog.setTitle(getString(R.string.downloading)+"\n"+getString(R.string.package_list));
		        		progressDialog.setMessage(getString(R.string.lastmodified)+" "+sdf.format(resultdate)+"\n"+getString(R.string.downloading)+" ["+ FILE_SIZE+"kb]");
		        	}else{

		        		progressDialog.setTitle(getString(R.string.downloading)+"\n"+Translator.getTitle(toDownload.getpackageMode())+" ("+FILE_SIZE+"KB)");
		        	}



		        	Log.i(TAG,"Download started "+ FILE_SIZE+"kb");
		        	break;

		    //Getting progress
		        case   FileManager.MESSAGE_UPDATE_PROGRESS_BAR :
		        	int currentProgress = msg.arg1;
		        	int percent = 0;
		        	if(FILE_SIZE>0){
		        		percent = (100*currentProgress)/FILE_SIZE;
		        		if(progressDialog.isShowing()){
		        			progressDialog.setProgress(percent);
		        		}else{
		        			FileManager.DownloadCancel();
		        		}
		        	}
	                Log.i(TAG,"Progessbar "+currentProgress+"kb ,"+percent+"%");
	                break;

            //Download cancel by user
		        case   FileManager.MESSAGE_DOWNLOAD_CANCELED :
		        	progressDialog.dismiss();
		        	Log.i(TAG,"Download cancel");
		        	break;

		    //Download cancel by Error
		        case   FileManager.MESSAGE_ENCOUNTERED_ERROR :
		        	progressDialog.dismiss();
		        	String error = (String)msg.obj;
		        	Log.e(TAG,error);

		            final AlertDialog.Builder ErrorDialog = new AlertDialog.Builder(thisActivity);
		            ErrorDialog.setIcon(android.R.drawable.ic_dialog_alert);
		            ErrorDialog.setTitle(getString(R.string.error));
		            ErrorDialog.setMessage(error);
		            ErrorDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	thisActivity.finish();
	                    }
		            });

		            ErrorDialog.show();

		            break;

		    //Download Completed  call install activity if package downloaded
		    //parse html if svn list download
	        case   FileManager.MESSAGE_DOWNLOAD_COMPLETE :
	        	if(isListLoaded == false){
		        	progressDialog.setMessage(getString(R.string.loading));
		        	ParseHtmlRun();
		        	Log.i(TAG,"Download complete");
		        	isListLoaded = true;
	        	}else{
		        	progressDialog.dismiss();
		        	Intent myIntent = new Intent(thisActivity, InstallActivity.class);
		        	myIntent.putExtra("filename",toDownload.getJarFileName());
			    	myIntent.putExtra("filepath",Prefs.TEMP_DIR+"/"+toDownload.getJarFileName());
			    	myIntent.putExtra("filedate",ModifiedSince);
			    	startActivity(myIntent);
	        	}
	        	break;


	       //Generate List View after parsing
	        case HTML_PARSING_DONE:
	        	progressDialog.dismiss();
	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisActivity,android.R.layout.simple_list_item_1, android.R.id.text1, internetManifest.PackageTitleList());
	        	listView.setAdapter(adapter);
	        	listView.setTextFilterEnabled(true);
	    	    //Set current mode on click
	        	listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	        @Override
	    	        public void onItemClick(AdapterView<?> av, View view, int position, final long id) {
	    				TextView v = (TextView) view;
	    				Toast.makeText(getApplicationContext(), v.getText(),   Toast.LENGTH_SHORT).show();
	    				toDownload = internetManifest.get(position);
						// start the download immediately
	    			    startDownload();

	    			    return;
	    			}
	    	    });
	     	    break;
	        }
	    }
	};


	private void startDownload() {
		progressDialog = new ProgressDialog(thisActivity);
		progressDialog.setTitle(getString(R.string.downloading)+"\n"+Translator.getTitle(toDownload.getpackageMode()));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.show();
		FileManager.DownloadRun(toDownload.getJarURL(),Prefs.TEMP_DIR+"/"+toDownload.getJarFileName(),thisActivity.handler);
	}


	@Override
	public void onClick(View v) {
		if (v.equals(_reloadButton)){
			isListLoaded = false;
			progressDialog = new ProgressDialog(thisActivity);
		    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle(getString(R.string.downloading)+"\n"+getString(R.string.package_list));
		    progressDialog.setCancelable(false);
		    progressDialog.show();
			FileManager.DownloadRun(Prefs.SVN_MANIFEST_ADDRESS,Prefs.TEMP_DIR+"/svn.html",thisActivity.handler);

		}

	}
}
