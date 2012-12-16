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
package org.apertium.android.simple;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.util.Log;
import java.io.FilenameFilter;

public class FileUtils {
  static String TAG = "FileUtils";

  public static void copyFile(String Src, String Target) throws IOException {

    InputStream in = new FileInputStream(Src);
    OutputStream out = new FileOutputStream(Target);
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  public static void downloadFile(String source, String target) throws IOException {
    BufferedInputStream in = new BufferedInputStream(new URL(source).openStream());
    java.io.FileOutputStream fos = new java.io.FileOutputStream(target);
    java.io.BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
    byte data[] = new byte[1024];
    while (in.read(data, 0, 1024) >= 0) {
      bout.write(data);
    }
    bout.close();
    in.close();
  }

  static public void unzip(String zipFile, String to) throws ZipException, IOException {
    unzip(zipFile, to, new FilenameFilter() {
      public boolean accept(File arg0, String arg1) {
        return true;
      }
    });
  }

  static public void unzip(String zipFile, String to, FilenameFilter filter) throws ZipException, IOException {
    Log.i(TAG, zipFile);
    int BUFFER = 2048;
    File file = new File(zipFile);

    ZipFile zip = new ZipFile(file);
    //removing extention name
    String newPath = to;

    Log.i(TAG, "new path =" + newPath);
    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

    // Process each entry
    while (zipFileEntries.hasMoreElements()) {
      // grab a zip file entry
      ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
      String currentEntry = entry.getName();
      File destFile = new File(newPath, currentEntry);
      //destFile = new File(newPath, destFile.getName());
      File destinationParent = destFile.getParentFile();

      if (!filter.accept(destinationParent, destFile.getName())) continue;

      // create the parent directory structure if needed
      destinationParent.mkdirs();

      if (!entry.isDirectory()) {
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

  public static Boolean move(String oldpath, String newpath) {
    File dir = new File(oldpath);
    File file = new File(newpath);
    file.mkdirs();
    if (dir.renameTo(file)) {
      return true;
    }
    return false;
  }

  public static void remove(File dir) {
    if (dir.isDirectory()) {
      for (File child : dir.listFiles()) {
        remove(child);
      }
    }
    dir.delete();
  }
}
