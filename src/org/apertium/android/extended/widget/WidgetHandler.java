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
package org.apertium.android.extended.widget;


import android.content.Context;
import android.content.SharedPreferences.Editor;
import org.apertium.android.App;

public class WidgetHandler {


  public void setWidgetModes(String[] modes) {
    for (int i = 0; i < modes.length; i++) {
      Editor editor = App.prefs.edit();
      editor.putString("WidgetMode" + i, modes[i]);
      editor.commit();
    }
  }

  public String[] getWidgetModes() {
    udateWidget();

    String[] modes = new String[5];
    for (int i = 0; i < modes.length; i++) {
      String mode = App.prefs.getString("WidgetMode" + i, "+");
      modes[i] = mode;
    }
    return modes;
  }

  public void setWidgetMode(String mode, int id) {
    Editor editor = App.prefs.edit();
    editor.putString("WidgetMode" + id, mode);
    editor.commit();
  }

  public String getWidgetMode(int id) {
    String mode = App.prefs.getString("WidgetMode" + id, "+");
    return mode;
  }

  public void removeMode(String modeID) {
    for (int i = 0; i < 5; i++) {
      String mode = App.prefs.getString("WidgetMode" + i, "+");
      if (mode.equals(modeID)) {
        setWidgetMode("+", i);
      }
    }
  }

  private void udateWidget() {
    for (int i = 0; i < 5; i++) {
      String mode = App.prefs.getString("WidgetMode" + i, "+");
      setWidgetMode("+", i);
      App.longToast("TODO");
    }
  }
}
