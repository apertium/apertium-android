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

//import android.annotation.TargetApi;
import android.content.Context;

//@TargetApi(11)
public class ClipboardHandler {
  protected Context activity;

  public ClipboardHandler(Context thisActivity) {
    activity = thisActivity;

  }

  @SuppressWarnings("deprecation")
  public void putText(String text) {
    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    clipboard.setText(text);
  }

  @SuppressWarnings("deprecation")
  public String getText() {
    String text = null;
    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    text = clipboard.getText().toString();
    return text;
  }
}
