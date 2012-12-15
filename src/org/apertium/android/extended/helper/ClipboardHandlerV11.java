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
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

//@TargetApi(11)
public class ClipboardHandlerV11 extends ClipboardHandler {

  public ClipboardHandlerV11(Context thisActivity) {
    super(thisActivity);
  }

  @Override
  public void putText(String text) {
    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    android.content.ClipData clip = ClipData.newPlainText("simple text", text);
    clipboard.setPrimaryClip(clip);
  }

  @Override
  public String getText() {
    String text = null;
    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard.getText() != null) {
      text = clipboard.getText().toString();
    }
    return text;
  }
}
