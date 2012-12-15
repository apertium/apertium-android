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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apertium.Translator;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class ApertiumCaffeine {

  public static SharedPreferences prefs;
  protected static final FilenameFilter filter = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?.jar");
    }
  };
  private HashMap<String, String> titleToBase;
  private HashMap<String, String> titleToMode;
  private File packagesDir;

  ApertiumCaffeine(App app) {
    prefs = PreferenceManager.getDefaultSharedPreferences(app);
    packagesDir = new File(app.getCacheDir(), "packages");
    packagesDir.mkdirs();
  }

  public void init() {
    Translator.setParallelProcessingEnabled(false);
    initModes(packagesDir);
    /*
     if (modesComboBox.getItemCount() == 0 &&
     JOptionPane.showConfirmDialog(null,
     "You don't have any language pair installed yet.\n"
     + "Would you like to install some now?",
     "We need language pairs!", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
     try {
     new InstallDialog((Frame)null, true) {
     @Override
     protected void initStrings() {
     STR_TITLE = "Install language pairs";
     STR_INSTRUCTIONS = "Check the language pairs to install.";
     }
     }.setVisible(true);
     initModes(packagesDir);
     } catch (IOException ex) {
     Logger.getLogger(ApertiumCaffeine.class.getName()).log(Level.SEVERE, null, ex);
     JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
     }
     int idx = prefs.getInt("modesComboBox", 0);
     if (idx < 0) idx = 0;
     if (idx < modesComboBox.getItemCount())
     modesComboBox.setSelectedIndex(idx);
     */

    boolean displayMarks = prefs.getBoolean("displayMarks", true);
  }

  private void initModes(File packagesDir) {
    titleToBase = new HashMap<String, String>();
    titleToMode = new HashMap<String, String>();
    File packages[] = packagesDir.listFiles(filter);
    for (File p : packages) {
      try {
        String base = p.getPath();
        Translator.setBase(base);
        for (String mode : Translator.getAvailableModes()) {
          String title = Translator.getTitle(mode);
          titleToBase.put(title, base);
          titleToMode.put(title, mode);
        }
      } catch (Exception ex) {
        //Perhaps the directory contained a file that wasn't a valid package...
        Logger.getLogger(ApertiumCaffeine.class.getName()).log(Level.WARNING, null, ex);
      }
    }
    Object titles[] = titleToBase.keySet().toArray();
    Arrays.sort(titles);
//    modesComboBox.setModel(new DefaultComboBoxModel(titles));
  }
/*
  private void modesComboBoxActionPerformed() {
    try {
      Translator.setBase(titleToBase.get(modesComboBox.getSelectedItem()));
      Translator.setMode(titleToMode.get(modesComboBox.getSelectedItem()));
      update();
    } catch (Exception ex) {
      Logger.getLogger(ApertiumCaffeine.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  */
}
