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
package org.apertium.android.extended.helper;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs {
  private static final String TAG = "AppPreference";
  //Directories path
  public static final String BASE_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium";
  public static final String JAR_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium/jars";
  public static final String TEMP_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium/temp";
  public static final String MANIFEST_FILE = "Manifest";
  public static final String SVN_MANIFEST_ADDRESS = "http://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";
  public static final String SUPPORT_MAIL = "arinkverma@gmail.com";
  //Preferences name
  public static final String PREFERENCE_NAME = "ore.apertium.Pref";
  private static SharedPreferences prefs = null;

  public static void init(Context ctx) {
    prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
  }

  /*Cache Preference*/
  public static final String cacheEnabled = "CachePref";

  public static boolean isCacheEnabled() {
    return prefs.getBoolean(cacheEnabled, false);
  }
  /*DisplayMark Preference*/
  public static final String displayMark = "MarkPref";

  public static void setDisplayMark(boolean y) {
    Editor editor = prefs.edit();
    editor.putBoolean(displayMark, y);
    editor.commit();
  }

  public static boolean isDisplayMarkEnabled() {
    return prefs.getBoolean(displayMark, false);
  }
  /*ClipBoardPush Preference*/
  public static final String clipBoardGet = "ClipGetPref";
  public static final String clipBoardPush = "ClipPushPref";

  public static void setClipBoardPush(boolean y) {
    Editor editor = prefs.edit();
    editor.putBoolean(clipBoardPush, y);
    editor.commit();
  }

  public static boolean isClipBoardPushEnabled() {
    return prefs.getBoolean(clipBoardPush, false);
  }

  public static void setClipBoardGet(boolean y) {
    Editor editor = prefs.edit();
    editor.putBoolean(clipBoardGet, y);
    editor.commit();
  }

  public static boolean isClipBoardGetEnabled() {
    return prefs.getBoolean(clipBoardGet, false);
  }
  /*Crash Preference*/
  public static final String CrashPref = "CrashPref";

  public static void reportCrash(String y) {
    Editor editor = prefs.edit();
    editor.putString(CrashPref, y);
    editor.commit();
  }

  public static String getCrashReport() {
    return prefs.getString(CrashPref, null);
  }

  public static void clearCrashReport() {
    Editor editor = prefs.edit();
    editor.putString(CrashPref, null);
    editor.commit();
  }
  //Last state
  private static final String LocalePref = "LocalePref";
  private static final String LastJARDirChangedPref = "LastJARDirChangedPref";

  public static boolean isStateChanged() {
    String lastLocale = prefs.getString(LocalePref, "");
    String currentLocale = Locale.getDefault().getDisplayLanguage();

    Log.i(TAG, "lastLocale = " + lastLocale + ", currentLocale = " + currentLocale);
    if (!lastLocale.equals(currentLocale)) {
      return true;
    }

    File f = new File(JAR_DIR);

    String LastModified = f.lastModified() + "";
    String SavedLastModified = prefs.getString(LastJARDirChangedPref, "");

    Log.i(TAG, "LastModified = " + LastModified + ", SavedLastModified = " + SavedLastModified);
    if (!LastModified.equals(SavedLastModified)) {
      return true;
    }

    return false;

  }

  public static void saveState() {
    Editor editor = prefs.edit();
    editor.putString(LocalePref, Locale.getDefault().getDisplayLanguage());

    File f = new File(JAR_DIR);
    editor.putString(LastJARDirChangedPref, f.lastModified() + "");

    Log.i(TAG, "lastLocale = " + Locale.getDefault().getDisplayLanguage() + ", LastJARDirChanged = " + f.lastModified());
    editor.commit();

  }
}
