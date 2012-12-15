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
package org.apertium.android.extended.SMS;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apertium.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SmsArrayAdapter extends ArrayAdapter<SMSobject> {
  String TAG = "SmsArrayAdapter";
  private Context ctx;
  private int id;
  private List<SMSobject> items;

  public SmsArrayAdapter(Context context, int textViewResourceId, List<SMSobject> objects) {
    super(context, textViewResourceId, objects);
    ctx = context;
    id = textViewResourceId;
    items = objects;

  }

  public SMSobject getItem(int i) {
    return items.get(i);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
    }
    final SMSobject o = items.get(position);
    if (o != null) {
      TextView t1 = (TextView) v.findViewById(R.id.TextView_Sender);
      TextView t2 = (TextView) v.findViewById(R.id.TextView_Body);
      TextView t3 = (TextView) v.findViewById(R.id.TextView_Date);

      SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
      Date resultdate = new Date(o.getDate());

      if (t1 != null) {
        t1.setText(o.getSender());
      }
      if (t2 != null) {
        t2.setText(o.getBody());
      }
      if (t3 != null) {
        t3.setText(sdf.format(resultdate));
      }

    }
    return v;
  }
}
