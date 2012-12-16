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
package org.apertium.android.extended.filemanager;

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

import org.apertium.android.extended.helper.Prefs;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FileManager {
  static String TAG = "FileManager";

  public static void setDIR() {
    File baseDir = new File(Prefs.BASE_DIR);
    File tempDir = new File(Prefs.TEMP_DIR);
    File jarDir = new File(Prefs.JAR_DIR);

    if (!baseDir.exists()) {
      baseDir.mkdirs();
    }
    if (!tempDir.exists()) {
      tempDir.mkdirs();
    }

    if (!jarDir.exists()) {
      jarDir.mkdirs();
    }
  }

  /* Download fucntion with handle communication */
  // Used to communicate state changes in the DownloaderThread
  public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
  public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
  public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
  public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
  public static final int MESSAGE_CONNECTING_STARTED = 1004;
  public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
  // constants
  public static final int DOWNLOAD_BUFFER_SIZE = 4096;
  private static boolean isDownloadRun = true;

  public static void DownloadRun(final String Source, final String Target, final Handler handler) {
    isDownloadRun = true;
    Thread downloadThread = new Thread() {
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
          ModifiedSince = conn.getLastModified() + "";

          // notify download start
          int fileSizeInKB = fileSize / 1024;
          msg = Message.obtain(handler, MESSAGE_DOWNLOAD_STARTED, fileSizeInKB, 0, ModifiedSince);
          handler.sendMessage(msg);

          // start download
          inStream = new BufferedInputStream(conn.getInputStream());
          outFile = new File(Target);
          fileStream = new FileOutputStream(outFile);
          outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
          byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
          int bytesRead = 0, totalRead = 0;
          while (isDownloadRun && !isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0) {
            outStream.write(data, 0, bytesRead);
            // update progress bar
            totalRead += bytesRead;
            int totalReadInKB = totalRead / 1024;
            msg = Message.obtain(handler, MESSAGE_UPDATE_PROGRESS_BAR, totalReadInKB, 0);
            handler.sendMessage(msg);

          }

          outStream.close();
          fileStream.close();
          inStream.close();

          if (isInterrupted() || !isDownloadRun) {
            // the download was canceled, so let's delete the partially downloaded file
            outFile.delete();
          } else {
            // notify completion
            msg = Message.obtain();
            msg.what = MESSAGE_DOWNLOAD_COMPLETE;
            handler.sendMessage(msg);
          }
        } catch (Exception e) {
          e.printStackTrace();
          msg = Message.obtain(handler, MESSAGE_ENCOUNTERED_ERROR, e.toString());
          handler.sendMessage(msg);
        }
      }
    };
    downloadThread.start();
  }

  public static void DownloadCancel() {
    isDownloadRun = false;
  }

  public static void FileInfoRun(final String Source, final Handler handler) {
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
          ModifiedSince = conn.getLastModified() + "";
          // get the filename
          lastSlash = url.toString().lastIndexOf('/');
          FileName = "file.txt";
          if (lastSlash >= 0) {
            FileName = url.toString().substring(lastSlash + 1);
          }
          if (FileName.equals("")) {
            FileName = "untitled";
          }
        } catch (MalformedURLException e) {
          Log.e(TAG, e + "");
        } catch (IOException e) {
          Log.e(TAG, e + "");
        }

        // notify download start
        int fileSizeInKB = FileSize / 1024;
        msg = Message.obtain(handler, MESSAGE_DOWNLOAD_STARTED, fileSizeInKB, 0, ModifiedSince);
        handler.sendMessage(msg);
      }
    };
    t.start();
  }
}
