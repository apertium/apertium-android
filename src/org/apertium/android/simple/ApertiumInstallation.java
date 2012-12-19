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
package org.apertium.android.simple;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apertium.Translator;
import org.apertium.utils.IOUtils;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class ApertiumInstallation {

  public static ApertiumInstallation instance;

  public HashMap<String, String> titleToBasedir = new HashMap<String, String>();
  public HashMap<String, String> titleToMode = new HashMap<String, String>();
  public HashMap<String, String> modeToPackage = new HashMap<String, String>();
  public HashMap<String, String> packageToBasedir = new HashMap<String, String>();

  /** This is where the packages are installed */
  public static File packagesDir;

  /** This is where optimized bytecode will be put. It will be regenerated if deleted,
   *  so it can be put in the cacheDir of the app
   *  NOTE: To avoid code injection attacks this should be placed in a private place, inaccesible for others */
  public static File dexBytecodeCache;

  public static void init(Context ctx) {
    if (instance!=null) return;
    instance = new ApertiumInstallation(App.instance);
  }

  public ApertiumInstallation(App app) {
    packagesDir = new File(app.getCacheDir(), "packages");
    packagesDir.mkdirs();
    dexBytecodeCache = new File(app.getCacheDir(), "dex-cache");
    dexBytecodeCache.mkdirs();
  }

  public static final FilenameFilter apertiumDirectoryFilter = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?");
    }
  };

  public HashSet<String> getInstalledPackages() {
    HashSet<String> installedPackages = new HashSet<String>(Arrays.asList(packagesDir.list(apertiumDirectoryFilter)));
    Log.d("", "Scanning "+packagesDir+" gave "+installedPackages);
    return installedPackages;
  }

  public void scanForPackages() {
    titleToBasedir.clear();
    titleToMode.clear();
    modeToPackage.clear();
    packageToBasedir.clear();

    for (String pkg : getInstalledPackages()) {
      String basedir = packagesDir + "/" + pkg;
      try {
        DexClassLoader cl = new DexClassLoader(basedir+".jar", dexBytecodeCache.getAbsolutePath(), null, this.getClass().getClassLoader());
        Translator.setBase(basedir, cl);
        for (String mode : Translator.getAvailableModes()) {
          String title = Translator.getTitle(mode);
          Log.d("", mode+"  "+title+"  "+basedir);
          titleToBasedir.put(title, basedir);
          titleToMode.put(title, mode);
          modeToPackage.put(mode, pkg);
        }
        packageToBasedir.put(pkg, basedir);
      } catch (Exception ex) {
        //Perhaps the directory contained a file that wasn't a valid package...
        ex.printStackTrace();
        Log.e("", basedir, ex);
      }
    }
  }

  void installJar(File tmpjarfile, String pkg) throws IOException {
    File dir = new File(packagesDir, pkg);
    FileUtils.unzip(tmpjarfile.getPath(), dir.getPath());
    File jarfile = new File(ApertiumInstallation.packagesDir, pkg+".jar");
    tmpjarfile.renameTo(jarfile);
    scanForPackages();
  }
}
