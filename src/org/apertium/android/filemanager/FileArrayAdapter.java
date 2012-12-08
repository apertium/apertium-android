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
package org.apertium.android.filemanager;

import org.apertium.android.R;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class FileArrayAdapter extends ArrayAdapter<Option> {
  private Context c;
  private int id;
  private List<Option> items;

  FileArrayAdapter(Context context, int textViewResourceId,
      List<Option> objects) {
    super(context, textViewResourceId, objects);
    c = context;
    id = textViewResourceId;
    items = objects;
  }

  public Option getItem(int i) {
    return items.get(i);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
    }
    final Option o = items.get(position);
    if (o != null) {
      TextView t1 = (TextView) v.findViewById(R.id.TextView01);
      TextView t2 = (TextView) v.findViewById(R.id.TextView02);

      if (t1 != null) {
        t1.setText(o.getName());
      }
      if (t2 != null) {
        t2.setText(o.getData());
      }

    }
    return v;
  }
}
