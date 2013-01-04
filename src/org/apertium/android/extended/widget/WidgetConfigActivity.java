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

import org.apertium.android.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import org.apertium.android.App;

public class WidgetConfigActivity extends Activity implements OnClickListener {
  private final String TAG = "WidgetConfigActivity";
  private int AppWidgetId = -1;
  //Button
  private Button[] _modeButton;
  private WidgetHandler widgetHandler;
  private String[] Modes = null;
  private int CurrentButton = -1;

  /**
   Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.widget_config);
    setResult(RESULT_CANCELED);

    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      AppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    Log.i(TAG, "Widget ID=" + AppWidgetId);

    if (AppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      this.finish();
      return;
    } else {
      widgetHandler = new WidgetHandler();
    }

    initView();
    fillView();

  }

  @Override
  public void onClick(View v) {
    for (int i = 0; i < 5; i++) {
      if (v.equals(_modeButton[i])) {
        CurrentButton = i;
        App.longToast("TODO: Choose language");
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    String m = "";
    for (int i = 0; i < Modes.length; i++) {
      m += Modes[i] + ",";
    }
    Log.i(TAG, "Modes " + m);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    String Mode = data.getStringExtra("Mode");
    if (CurrentButton > -1 && Mode != null) {
      widgetHandler.setWidgetMode(Mode, CurrentButton);
    }

    fillView();
  }

  //Initialize View variable
  void initView() {
    _modeButton = new Button[5];
    _modeButton[0] = (Button) findViewById(R.id.ModeButton1);
    _modeButton[1] = (Button) findViewById(R.id.ModeButton2);
    _modeButton[2] = (Button) findViewById(R.id.ModeButton3);
    _modeButton[3] = (Button) findViewById(R.id.ModeButton4);
    _modeButton[4] = (Button) findViewById(R.id.ModeButton5);
  }

  //Setting up View variable
  void fillView() {
    Modes = widgetHandler.getWidgetModes();
    int[] ModeButtonCode = {R.id.widgetmode1, R.id.widgetmode2, R.id.widgetmode3, R.id.widgetmode4, R.id.widgetmode5};

    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
    ComponentName thisWidget = new ComponentName(this.getApplicationContext(), WidgetProvider.class);


    //Add click listener and hiding unused buttons
    for (int i = 0; i < 5; i++) {
      Log.i("Mode", Modes[i]);


      //Set visibility of buttons
      if (Modes[i].equals("+")) {
        _modeButton[i].setText("+");
        remoteViews.setViewVisibility(ModeButtonCode[i], View.GONE);
      } else {
        String modeTitle = App.prefs.getString("widget.mode"+i, "???");
        _modeButton[i].setText(modeTitle);
        remoteViews.setViewVisibility(ModeButtonCode[i], View.VISIBLE);
      }


      _modeButton[i].setOnClickListener(this);

      //Set assgined mode
      remoteViews.setTextViewText(ModeButtonCode[i], Modes[i]);
    }

    AppWidgetManager.getInstance(this.getApplicationContext()).updateAppWidget(thisWidget, remoteViews);
  }
}
