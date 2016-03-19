/*
 * Copyright (C) 2012 Mikel Artetxe
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
package org.apertium.android;

import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import dalvik.system.DexClassLoader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apertium.Translator;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class ApertiumInstallation {
  /**
   * Convert a title of a mode, like 'Spanish -> Esperanto' to its mode name (es-eo).
   * This map is intentionally not encapsulated. Don't misuse, its only for reading
   */
  public final HashMap<String, String> titleToMode = new HashMap<String, String>();
  /**
   * Given a mode name, like 'es-eo', find its package name (eo-es).
   * This map is intentionally not encapsulated. Don't misuse, its only for reading
   */
  public final HashMap<String, String> modeToPackage = new HashMap<String, String>();

  /**
   * List of oberservers to notify after a rescan.
   * The run() method will be runned by the UI thread
   */
  public final ArrayList<Runnable> observers = new ArrayList<Runnable>();


  /**
   This is where the packages are installed
   */
  private File packagesDir;
  /**
   * This is where bytecode will be put.
   * NOTE: To avoid code injection attacks this should be placed in a private place, inaccesible for others
   */
  private File bytecodeDir;
  /**
   * This is where optimized bytecode will be put.
   * It will be regenerated if deleted, so it can be put in the cacheDir of the app.
   * NOTE: To avoid code injection attacks this should be placed in a private place, inaccesible for others
   */
  private File bytecodeCacheDir;

  ApertiumInstallation(File packagesDir, File bytecodeDir, File bytecodeCacheDir) {
    this.packagesDir = packagesDir;
    this.bytecodeDir = bytecodeDir;
    this.bytecodeCacheDir = bytecodeCacheDir;
    packagesDir.mkdirs();
    bytecodeDir.mkdirs();
    bytecodeCacheDir.mkdirs();
  }

  private final FilenameFilter apertiumPackageDirectoryFilter = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?");
    }
  };

  private void notifyObservers() {
    for (Runnable r : observers) App.handler.post(r);
  }

  public void rescanForPackages() {
    titleToMode.clear();
    modeToPackage.clear();
    //ClassLoader dummyClassLoader = getClass().getClassLoader(); // Not really used during scanning

    String[] installedPackages = packagesDir.list(apertiumPackageDirectoryFilter);
    Log.d("", "Scanning " + packagesDir + " gave " + Arrays.asList(installedPackages));
    for (String pkg : installedPackages) {

      String basedir = packagesDir + "/" + pkg;
      try {
        Translator.setBase(basedir, getClassLoaderForPackage(pkg)); // getClassLoaderForPackage(pkg)
        for (String mode : Translator.getAvailableModes()) {
          String title = LanguageTitles.getTitle(mode);
          Log.d("", mode + "  " + title + "  " + basedir);
          titleToMode.put(title, mode);
          modeToPackage.put(mode, pkg);
        }
      } catch (Throwable ex) {
        //Perhaps the directory contained a file that wasn't a valid package...
        ex.printStackTrace();
        Log.e("", basedir, ex);
      }
    }
    notifyObservers();
  }

  public void installJar(File tmpjarfile, String pkg) throws IOException {
    // TODO: Remove all unneeded stuff from jarfile // jarfile.delete();
    File dir = new File(packagesDir, pkg);
    FileUtils.unzip(tmpjarfile.getPath(), dir.getPath(), new FilenameFilter() {
      /**
       @param dir the directory in which the filename was found.
       @param filename the name of the file in dir to test.
       */
      public boolean accept(File dir, String filename) {
        if (filename.endsWith(".class")) {
          return false;
        }
        //if (filename.endsWith(".dex")) return false;
        return true;
      }
    });
    dir.setLastModified(tmpjarfile.lastModified());
    File classesDex = new File(dir, "classes.dex");
    File installedjarfile = new File(bytecodeDir, pkg + ".jar");
    if (!classesDex.exists()) {
      App.reportError(new IllegalStateException(classesDex + " missing for " + pkg + " " + tmpjarfile));
      tmpjarfile.renameTo(installedjarfile); // resolve to renaming and hope for the best!
    } else {
      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(installedjarfile)));
      try {
        ZipEntry entry = new ZipEntry(classesDex.getName());
        zos.putNextEntry(entry);
        //zos.write(bytes);
        //FileUtils.copyStream(new FileInputStream(classesDex) , zos);
        FileInputStream in = new FileInputStream(classesDex);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
          zos.write(buffer, 0, read);
        }
        in.close();

        classesDex.delete();
        zos.closeEntry();
      } finally {
        zos.close();
      }
      installedjarfile.setLastModified(tmpjarfile.lastModified());
    }
  }

  public void uninstallPackage(String pkg) {
    FileUtils.remove(new File(bytecodeDir, pkg + ".jar"));
    FileUtils.remove(new File(packagesDir, pkg));
    FileUtils.remove(new File(bytecodeCacheDir, pkg + ".dex"));
  }


  public DexClassLoader getClassLoaderForPackage(String pkg) {
//        Log.d(TAG, "new DexClassLoader(" + basedir + ".jar");
    //return new DexClassLoader(getBasedirForPackage(pkg)+ ".jar", bytecodeCacheDir.getAbsolutePath(), null, this.getClass().getClassLoader());
    if (!bytecodeCacheDir.exists()) { // Fix for https://mint.splunk.com/dashboard/project/185c8f8c/errors/987908380
      BugSenseHandler.sendException(new IOException("bytecodeCacheDir was removed: "+bytecodeCacheDir));
      bytecodeCacheDir.mkdirs();
    }
    return new DexClassLoader(bytecodeDir+ "/" + pkg + ".jar", bytecodeCacheDir.getAbsolutePath(), null, this.getClass().getClassLoader());
  }


  public String getBasedirForPackage(String pkg) {
    return packagesDir + "/" + pkg;
  }
}
