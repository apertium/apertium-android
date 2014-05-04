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
package org.apertium.android;

import android.os.Environment;

public class Prefs {
  //Directories path
  public static final String BASE_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium";
  public static final String JAR_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium/jars";
  public static final String TEMP_DIR = Environment.getExternalStorageDirectory().toString() + "/apertium/temp";
  public static final String MANIFEST_FILE = "Manifest";
  public static final String SVN_MANIFEST_ADDRESS = "http://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";
  //Preferences name
  public static final String PREFERENCE_NAME = "ore.apertium.Pref";
}
