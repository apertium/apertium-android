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
/**
 SMSInboxActivity.java Open SMS inbox list view

 @author Arink Verma

 */
package org.apertium.android.extended.SMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import org.apertium.android.R;
import org.apertium.android.TranslatorActivity;

public class SMSInboxActivity extends ListActivity {
  String TAG = "SMSInboxActivity";
  private SmsArrayAdapter adapter;
  private SMSobject smsObj;
  ContentResolver mContentResolver = null;

  /**
   Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContentResolver = getContentResolver();

    fill();
  }

  private void fill() {
    this.setTitle(getString(R.string.inbox));
    List<SMSobject> dir = getSms();

    Comparator<Object> comparator = Collections.reverseOrder();
    Collections.sort(dir, comparator);

    adapter = new SmsArrayAdapter(SMSInboxActivity.this, R.layout.sms_layout, dir);
    this.setListAdapter(adapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    smsObj = adapter.getItem(position);

    Intent intent = new Intent(this, TranslatorActivity.class);
    intent.putExtra(Intent.EXTRA_TEXT, smsObj.getBody());
    setResult(RESULT_OK, intent);
    finish();
  }

  public List<SMSobject> getSms() {
    Log.i(TAG, "getSMS");
    Uri mSmsQueryUri = Uri.parse("content://sms/inbox");
    List<SMSobject> messages = new ArrayList<SMSobject>();
    Cursor cursor = null;
    try {

      cursor = mContentResolver.query(mSmsQueryUri, null, null, null, null);
      if (cursor == null) {
        Log.i(TAG, "cursor is null. uri: " + mSmsQueryUri);
        return messages;
      }

      for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
        final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        final String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        final Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

        SMSobject s = new SMSobject(body, sender, date);
        messages.add(s);
      }
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      e.printStackTrace();
    } finally {
      cursor.close();
    }
    return messages;
  }
}
