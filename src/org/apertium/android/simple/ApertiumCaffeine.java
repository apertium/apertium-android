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
import java.util.HashMap;
import org.apertium.Translator;

/**
 @author Mikel Artetxe, Jacob NordfalkPref
 */
public class ApertiumCaffeine {

  protected static final FilenameFilter filter = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      /*
      return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?")
          || name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?.jar");
      */
      //return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?.jar");
      return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?");
    }
  };
  public static ApertiumCaffeine instance;
  public HashMap<String, String> titleToBase;
  public HashMap<String, String> titleToMode;
  public static File packagesDir;
  public static File dexOutputDir;

  public static void init(Context ctx) {
    if (instance!=null) return;
    instance = new ApertiumCaffeine(App.instance);
  }

  public ApertiumCaffeine(App app) {
    packagesDir = new File(app.getCacheDir(), "packages");
    packagesDir.mkdirs();
    dexOutputDir = new File(app.getCacheDir(), "dex");
    dexOutputDir.mkdirs();
    Translator.setParallelProcessingEnabled(false);
  }

  public void initModes(File packagesDir) {
    titleToBase = new HashMap<String, String>();
    titleToMode = new HashMap<String, String>();
    File packages[] = packagesDir.listFiles(filter);
    for (File p : packages) {
      String base = p.getPath();
      try {
        DexClassLoader cl = new DexClassLoader(base+".jar", ApertiumCaffeine.dexOutputDir.getAbsolutePath(), null, this.getClass().getClassLoader());
        Translator.setBase(base, cl);
        for (String mode : Translator.getAvailableModes()) {
          String title = Translator.getTitle(mode);
          Log.d("", mode+"  "+title+"  "+base);
          titleToBase.put(title, base);
          titleToMode.put(title, mode);
        }
      } catch (Exception ex) {
        //Perhaps the directory contained a file that wasn't a valid package...
        ex.printStackTrace();
        Log.e("", base, ex);
      }
    }
  }

  public static String stripJar(String fn_jar) {
    return fn_jar.substring(0, fn_jar.length()-4); // strip '.jar'
  }
}
