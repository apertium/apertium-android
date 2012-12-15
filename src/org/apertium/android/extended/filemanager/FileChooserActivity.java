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
package org.apertium.android.extended.filemanager;

import org.apertium.android.extended.InstallActivity;
import org.apertium.android.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import org.apertium.android.extended.Extended;

public class FileChooserActivity extends ListActivity {
  private File currentDir;
  private FileArrayAdapter adapter;
  private Option o;

  /**
   Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Extended.init(this);
    currentDir = new File(Environment.getExternalStorageDirectory().getPath());
    fill(currentDir);
  }

  private void fill(File f) {
    File[] dirs = f.listFiles();
    this.setTitle("Current Dir: " + f.getName());
    List<Option> dir = new ArrayList<Option>();
    List<Option> fls = new ArrayList<Option>();
    try {
      for (File ff : dirs) {
        if (ff.isDirectory()) {
          dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
        } else {
          fls.add(new Option(ff.getName(), "File Size: " + ff.length(), ff.getAbsolutePath()));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Collections.sort(dir);
    Collections.sort(fls);
    dir.addAll(fls);
    if (!f.getName().equalsIgnoreCase("sdcard")) {
      dir.add(0, new Option("..", "Parent Directory", f.getParent()));
    }

    adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.file_view, dir);
    this.setListAdapter(adapter);


  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    o = adapter.getItem(position);
    if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
      currentDir = new File(o.getPath());
      fill(currentDir);
    } else {
      Intent myIntent = new Intent(FileChooserActivity.this, InstallActivity.class);
      myIntent.putExtra("filepath", o.getPath());
      myIntent.putExtra("filename", o.getName());
      startActivity(myIntent);
    }
  }
}
