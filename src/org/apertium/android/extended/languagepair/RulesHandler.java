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
package org.apertium.android.extended.languagepair;

import java.io.File;
import java.security.SecureClassLoader;

import org.apertium.android.extended.helper.Prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import dalvik.system.DexClassLoader;
import org.apertium.android.extended.Extended;

public class RulesHandler {
  private final static String TAG = "RulesHandler";
  private File _tmpDIR;
  private Context CTX;
  private final String _CurrentMode = "currentmode";
  private final SharedPreferences settings;
  private SharedPreferences.Editor editor;

  public RulesHandler(Context ctx) {
    this.CTX = ctx;
    this._tmpDIR = ctx.getDir("dex", 0);
    this.settings = CTX.getSharedPreferences(Prefs.PREFERENCE_NAME, 0);
    this.editor = settings.edit();
  }

  /**
   Gets the current mode. Returns null if no mode
   */
  public String getCurrentMode() {
    String mode = settings.getString(_CurrentMode, null);
    return mode;
  }

  public void setCurrentMode(String mode) {
    editor.putString(_CurrentMode, mode);
    editor.commit();
  }

  public void clearCurrentMode() {
    editor.putString(_CurrentMode, null);
    editor.commit();
  }

  public boolean isSetCurrentMode() {
    String mode = settings.getString(_CurrentMode, null);
    if (mode == null) {
      return false;
    }
    return true;
  }

  public String getCurrentPackage() {
    String mode = settings.getString(_CurrentMode, null);
    if (mode == null) {
      return null;
    }
    Log.i(TAG, "getting package of mode = " + mode);
    TranslationMode m = Extended.databaseHandler.getMode(mode);
    return (m == null) ? null : m.packageName;
  }

  public String findPackage(String M) {
    TranslationMode m = Extended.databaseHandler.getMode(M);
    return (m == null) ? null : m.packageName;
  }

  public String PathCurrentPackage() {
    return Prefs.JAR_DIR + "/" + getCurrentPackage();
  }

  public String ExtractPathCurrentPackage() {
    return PathCurrentPackage() + "/extract";
  }

  public DexClassLoader getDexClassLoader() {
    Log.d(TAG, "PathCurrentPackage =" + PathCurrentPackage() + "/" + getCurrentPackage() + ".jar, ODEX path=" + this._tmpDIR.getAbsolutePath());
    //return new DexClassLoader(PathCurrentPackage() + "/" + getCurrentPackage() + ".jar", this._tmpDIR.getAbsolutePath(), null, this.getClass().getDexClassLoader());
    return getDexClassLoader(PathCurrentPackage() + "/" + getCurrentPackage() + ".jar", this._tmpDIR.getAbsolutePath(), null, this.getClass().getClassLoader());
  }

  public static DexClassLoader getDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
    Log.d(TAG, "new DexClassLoader(" + dexPath + "," + optimizedDirectory + ", " + libraryPath + ", " + parent);
    return new DexClassLoader(dexPath, optimizedDirectory, libraryPath, parent);
  }
}
