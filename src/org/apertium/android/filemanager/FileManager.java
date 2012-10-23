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

package org.apertium.android.filemanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apertium.android.helper.Prefs;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FileManager {

	static String TAG = "FileManager";

/* unused
 	public static void  CopyAssets(String target,Context ctx) {
	    AssetManager assetManager = ctx.getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", e.getMessage());
          e.printStackTrace();
	    }
	    for(String filename : files) {
	        InputStream in = null;
	        OutputStream out = null;
	        try {
	          in = assetManager.open(filename);
	          out = new FileOutputStream(target);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(Exception e) {
	            Log.e("tag", e.getMessage());
              e.printStackTrace();
	        }
	    }
	}
*/

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

	public static void copyFile(String Src, String Target) throws IOException {

		InputStream in = new FileInputStream(Src);
		OutputStream out = new FileOutputStream(Target);
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}


	public static void downloadFile(String source, String target) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new URL(source).openStream());
		java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
		java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
		byte data[] = new byte[1024];
		while(in.read(data,0,1024)>=0)
		{
			bout.write(data);
		}
		bout.close();
		in.close();
	}


	public static void setDIR(){
	    File baseDir = new File(Prefs.BASE_DIR);
	    File tempDir = new File(Prefs.TEMP_DIR);
	    File jarDir = new File(Prefs.JAR_DIR);

	    if(!baseDir.exists()){
	    	baseDir.mkdirs();
	    }
	    if(!tempDir.exists()){
		    tempDir.mkdirs();
		}

	    if(!jarDir.exists()){
		    jarDir.mkdirs();
	    }
	}



	static public void unzip(String zipFile,String to) throws ZipException, IOException
	{
	    Log.i(TAG,zipFile);
	    int BUFFER = 2048;
	    File file = new File(zipFile);

	    ZipFile zip = new ZipFile(file);
	  //removing extention name
	    String newPath = to;

	    Log.i(TAG,"new path ="+newPath);
	    new File(newPath).mkdir();
	    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements())
	    {
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        //destFile = new File(newPath, destFile.getName());
	        File destinationParent = destFile.getParentFile();

	        // create the parent directory structure if needed
	        destinationParent.mkdirs();

	        if (!entry.isDirectory())
	        {
	            BufferedInputStream is = new BufferedInputStream(zip
	            .getInputStream(entry));
	            int currentByte;
	            // establish buffer for writing file
	            byte data[] = new byte[BUFFER];

	            // write the current file to disk
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	            BUFFER);

	            // read and write until last byte is encountered
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	        }

	    }
	}


	static public void unzip(String Source,String Target,String Filter) throws ZipException, IOException
	{
	    Log.i(TAG,Source);
	    int BUFFER = 2048;
	    File file = new File(Source);

	    ZipFile zip = new ZipFile(file);
	  //removing extention name
	    String newPath = Target;

	    Log.i(TAG,"new path ="+newPath);
	    new File(newPath).mkdir();
	    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements())
	    {
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        if(currentEntry.contains(Filter)){
		        File destFile = new File(newPath, currentEntry);
		        //destFile = new File(newPath, destFile.getName());
		        File destinationParent = destFile.getParentFile();

		        // create the parent directory structure if needed
		        destinationParent.mkdirs();

		        if (!entry.isDirectory())
		        {
		            BufferedInputStream is = new BufferedInputStream(zip
		            .getInputStream(entry));
		            int currentByte;
		            // establish buffer for writing file
		            byte data[] = new byte[BUFFER];

		            // write the current file to disk
		            FileOutputStream fos = new FileOutputStream(destFile);
		            BufferedOutputStream dest = new BufferedOutputStream(fos,BUFFER);

		            // read and write until last byte is encountered
		            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
		                dest.write(data, 0, currentByte);
		            }
		            dest.flush();
		            dest.close();
		            is.close();
		        }
	        }

	    }
	}





	public static Boolean move(String oldpath,String newpath){
		File dir = new File(oldpath);
		File file = new File(newpath);
		file.mkdirs();
		if(dir.renameTo(file)){
			return true;
		}
		return false;
	}


	public static void remove(File dir){
	    if (dir.isDirectory()){
	        for (File child : dir.listFiles())
	        	remove(child);
	    }
	    dir.delete();
	}



	/* Download fucntion with handle communication */

    // Used to communicate state changes in the DownloaderThread
    public static final int MESSAGE_DOWNLOAD_STARTED 		= 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE 	= 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR 	= 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED 	= 1003;
    public static final int MESSAGE_CONNECTING_STARTED 	= 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR 	= 1005;
    // constants
    public static final int DOWNLOAD_BUFFER_SIZE = 4096;

    private static boolean isDownloadRun = true;

	public static void DownloadRun(final String Source,final String Target,final Handler handler){
		isDownloadRun = true;
		Thread downloadThread  = new Thread() {
	        @Override
	        public void run() {
				URL url;
				URLConnection conn;
				int fileSize;
				String ModifiedSince = null;
				BufferedInputStream inStream;
				BufferedOutputStream outStream;
				File outFile;
				FileOutputStream fileStream;
				Message msg;


				msg = Message.obtain();
				msg.what = MESSAGE_CONNECTING_STARTED;
				handler.sendMessage(msg);
	                try {
	                        url = new URL(Source);
	                        conn = url.openConnection();
	                        conn.setUseCaches(false);
	                        fileSize = conn.getContentLength();
	                        ModifiedSince = conn.getLastModified()+"";

	                        // notify download start
	                        int fileSizeInKB = fileSize / 1024;
	                        msg = Message.obtain(handler, MESSAGE_DOWNLOAD_STARTED, fileSizeInKB , 0, ModifiedSince);
	        	            handler.sendMessage(msg);

	                        // start download
	                        inStream = new BufferedInputStream(conn.getInputStream());
	                        outFile = new File(Target);
	                        fileStream = new FileOutputStream(outFile);
	                        outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
	                        byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
	                        int bytesRead = 0, totalRead = 0;
	                        while(isDownloadRun && !isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0) {
	                                outStream.write(data, 0, bytesRead);
	                                // update progress bar
	                                totalRead += bytesRead;
	                                int totalReadInKB = totalRead / 1024;
	                                msg = Message.obtain(handler,MESSAGE_UPDATE_PROGRESS_BAR,totalReadInKB,0);
	    	        	            handler.sendMessage(msg);

	                        }

	                        outStream.close();
	                        fileStream.close();
	                        inStream.close();

	                        if(isInterrupted() || !isDownloadRun) {
	                                // the download was canceled, so let's delete the partially downloaded file
	                                outFile.delete();
	                        }
	                        else {
	                                // notify completion
	                               	msg = Message.obtain();
	                               	msg.what = MESSAGE_DOWNLOAD_COMPLETE;
	    	        	            handler.sendMessage(msg);
	                        }
	                } catch(Exception e) {
                    e.printStackTrace();
	                	msg = Message.obtain(handler,MESSAGE_ENCOUNTERED_ERROR,e.toString());
	        	        handler.sendMessage(msg);
	                }
	        }
	    };
	    downloadThread.start();
	}

	public static void DownloadCancel(){
		isDownloadRun = false;
	}

	public static void FileInfoRun(final String Source,final Handler handler){
	    Thread t = new Thread() {
	        @Override
	        public void run() {
				URL url;
				URLConnection conn;
				int FileSize = 0, lastSlash;
				String ModifiedSince = null;
				String FileName = null;

				Message msg;


				msg = Message.obtain();
				msg.what = MESSAGE_CONNECTING_STARTED;
				handler.sendMessage(msg);

                try {
					url = new URL(Source);
					conn = url.openConnection();
                    conn.setUseCaches(false);
                    FileSize = conn.getContentLength();
                    ModifiedSince = conn.getLastModified()+"";
                    // get the filename
                    lastSlash = url.toString().lastIndexOf('/');
                    FileName = "file.txt";
                    if(lastSlash >=0) {
                            FileName = url.toString().substring(lastSlash + 1);
                    }
                    if(FileName.equals("")) {
                            FileName = "untitled";
                    }
				} catch (MalformedURLException e) {
					Log.e(TAG,e+"");
				} catch (IOException e) {
					Log.e(TAG,e+"");
				}

                // notify download start
                int fileSizeInKB = FileSize / 1024;
                msg = Message.obtain(handler, MESSAGE_DOWNLOAD_STARTED, fileSizeInKB , 0, ModifiedSince);
	            handler.sendMessage(msg);
	        }
	    };
	    t.start();
	}
}
