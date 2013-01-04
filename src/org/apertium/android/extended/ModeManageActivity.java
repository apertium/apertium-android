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
 ModeManageActivity.java Manage installed language package

 @author Arink Verma
 */
package org.apertium.android.extended;

import java.io.File;
import java.util.List;

import org.apertium.Translator;
import org.apertium.android.FileUtils;
import org.apertium.android.helper.Prefs;
import org.apertium.android.extended.languagepair.TranslationMode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apertium.android.R;
import org.apertium.android.App;

public class ModeManageActivity extends ListActivity {
  String TAG = "ModeManageActivity";
  private Activity thisActivity = null;

  /*Mode related variable*/
  private static String packagetoRemove = null;
  /* List of installed modes*/
  private List<TranslationMode> listTranslationMode = null;
  private String PrefToSet = null;

  /*Process Handler*/
  private static ProgressDialog progressDialog = null;

  /**
   Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Extended.init(this);
    thisActivity = this;
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      PrefToSet = extras.getString("PrefToSet");
    }

    listTranslationMode = Extended.databaseHandler.getAllModes();

    int len = listTranslationMode.size();
    final String[] ModeTitle = new String[len];
    final String[] ModeId = new String[len];

    for (int i = 0; i < len; i++) {
      TranslationMode m = listTranslationMode.get(i);
      ModeTitle[i] = m.title;
      ModeId[i] = m.id;
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, ModeTitle);

    this.setListAdapter(adapter);

    ListView lv = getListView();
    lv.setTextFilterEnabled(true);


    //Set current mode on click
    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView v = (TextView) view;
        Toast.makeText(getApplicationContext(), v.getText(), Toast.LENGTH_SHORT).show();
        UpdateMode(ModeId[position]);
        finish();
      }
    });

    //Actions on Mode, on LongPress
    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> av, View v, int pos, final long id) {
        final AlertDialog.Builder b = new AlertDialog.Builder(ModeManageActivity.this);
        b.setIcon(android.R.drawable.ic_dialog_alert);

        final TranslationMode tobeRemove = Extended.databaseHandler.getMode(ModeId[pos]);

        final String pack = tobeRemove.packageName;
        b.setTitle(getString(R.string.confirm_packageRemove));
        String message = "";
        List<TranslationMode> removeModes = Extended.databaseHandler.getModes(pack);
        for (int i = 0; i < removeModes.size(); i++) {
          message += ((TranslationMode) removeModes.get(i)).title + "\n";
        }
        b.setMessage(message);
        b.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            packagetoRemove = pack;

            FileRemoveRun();

            String currentPackage = Extended.rulesHandler.getCurrentPackage();

            Log.i(TAG, "PacketToRemove = " + packagetoRemove + ", CurrentPackage = " + currentPackage);
            if (currentPackage != null && packagetoRemove.equals(currentPackage)) {
              Extended.rulesHandler.clearCurrentMode();
            }

          }
        });
        b.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // yesOrNo = 0;
          }
        });

        b.show();

        return true;
      }
    });

  }


  /*Removing Package Entries*/
  private void FileRemoveRun() {
    progressDialog = ProgressDialog.show(this, getString(R.string.deleting) + "...", getString(R.string.removing_files), true, false);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Log.i(TAG, "removing file=" + Prefs.JAR_DIR + "/" + packagetoRemove);
          File file = new File(Prefs.JAR_DIR + "/" + packagetoRemove);
          FileUtils.remove(file);
        } catch (Exception e) {
          e.printStackTrace();
        }

        App.handler.post(new Runnable() {
          @Override
          public void run() {
            DB_EntriesRemoveRun();
          }
        });
      }
    };
    t.start();
  }

  /*Removing Package files*/
  private void DB_EntriesRemoveRun() {
    progressDialog.setMessage(getString(R.string.removing_db));
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Extended.databaseHandler.deletePackage(packagetoRemove);
        } catch (Exception e) {
          e.printStackTrace();
        }
        App.handler.post(new Runnable() {
          @Override
          public void run() {
            progressDialog.dismiss();
            Prefs.saveState();
          }
        });
      }
    };
    t.start();
  }

  private void UpdateMode(String MODE) {
    if (PrefToSet != null) {
      Intent intent = getIntent();
      intent.putExtra("Mode", MODE);
      setResult(RESULT_OK, intent);
      Log.i("CurrentMode", MODE);
    } else {
      try {
        String currentPackage = Extended.rulesHandler.getCurrentPackage();
        String PackageTOLoad = Extended.rulesHandler.findPackage(MODE);
        Log.i(TAG, "CurrentPackage =" + currentPackage + ", PackageToLoad=" + PackageTOLoad + ", ModeToset=" + MODE);

        Extended.rulesHandler.setCurrentMode(MODE);
        if (!PackageTOLoad.equals(currentPackage)) {
          Log.i(TAG, "BASE =" + Extended.rulesHandler.getDexClassLoader() + "path = " + Extended.rulesHandler.ExtractPathCurrentPackage());

          Translator.setBase(Extended.rulesHandler.ExtractPathCurrentPackage(), Extended.rulesHandler.getDexClassLoader());

          Translator.setDelayedNodeLoadingEnabled(true);
          Translator.setMemmappingEnabled(true);
        }


        Translator.setMode(MODE);
        Log.e("CurrentMode", Extended.rulesHandler.getCurrentMode());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onBackPressed() {
    Intent intent = getIntent();
    intent.putExtra("Mode", "+");
    setResult(RESULT_CANCELED, intent);
    finish();
  }
}
