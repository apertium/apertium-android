/*
 * Copyright (C) 2012 Arink Verma, Jacob Nordfalk
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
 ApertiumActivity.java Main Launcher Activity of application

 @author Arink Verma, Jacob Nordfalk

 */
package org.apertium.android.simple;

import org.apertium.Translator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import org.apertium.android.R;

public class ApertiumActivity extends Activity implements OnClickListener {
  private final String TAG = "ApertiumActiviy";

  /*Layout variable*/
  //Text Fields
  private EditText inputEditText;
  private TextView outputTextView;
  //Button
  private Button submitButton;
  private Button fromButton;

  /*Mode related variable*/
  private String currentMode = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    setProgressBarIndeterminate(true);

    Log.i(TAG, "ApertiumActivityInitView Started");
    setContentView(R.layout.simple_layout);
    outputTextView = (TextView) findViewById(R.id.outputText);
    inputEditText = (EditText) findViewById(R.id.inputtext);

    ApertiumCaffeine.init(this);
    ApertiumCaffeine.instance.initModes(ApertiumCaffeine.packagesDir);

    submitButton = (Button) findViewById(R.id.translateButton);
    fromButton = (Button) findViewById(R.id.fromButton);

    submitButton.setOnClickListener(this);
    fromButton.setOnClickListener(this);
  }

  /* OnResume */
  @Override
  protected void onResume() {
    super.onResume();
  }


  @Override
  public void onClick(View v) {

    if (v.equals(submitButton)) {
      //Hiding soft keypad
      InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(inputEditText.getApplicationWindowToken(), 0);
      Translator.setCacheEnabled(true);
      Translator.setDelayedNodeLoadingEnabled(true);
      Translator.setCacheEnabled(true);
      translationTask = new TranslationTask();
      translationTask.execute(inputEditText.getText().toString());
      updateGui();

    } else if (v.equals(fromButton)) {
      final String[] modeTitle = ApertiumCaffeine.instance.titleToBase.keySet().toArray(new String[0]); //{"eo", "sv", "da" };
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.translate_from));
      builder.setItems(modeTitle, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int position) {
          currentMode = modeTitle[position];
          fromButton.setText(currentMode);
          try {
            Translator.setBase(ApertiumCaffeine.instance.titleToBase.get(currentMode));
            Translator.setMode(ApertiumCaffeine.instance.titleToMode.get(currentMode));
          } catch (Exception e) {
            e.printStackTrace();
            App.langToast(e.toString());
            BugSenseHandler.sendException(e);
          }
          updateGui();
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
    }
  }


  private void updateGui() {
    boolean ready = translationTask==null;
    submitButton.setEnabled(ready);
    setProgressBarIndeterminateVisibility(!ready);
  }


  TranslationTask translationTask;

  /* Translation Thread,
   * Load translation rules and excute lttoolbox.jar */
  class TranslationTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... inputText) {
      Runtime rt = Runtime.getRuntime();
      Log.d(TAG, "start mem f=" + rt.freeMemory() / 1000000 + "  t=" + rt.totalMemory() / 1000000 + " m=" + rt.maxMemory() / 1000000);
      App.timing = new org.apertium.utils.Timing("overall");
      try {
        //Translator.setCacheEnabled(Prefs.isCacheEnabled());
        //Log.i(TAG, "Translator Run Cache =" + Prefs.isCacheEnabled() + ", Mark =" + Prefs.isDisplayMarkEnabled() + ", MODE = " + currentMode);
        //Translator.setDisplayMarks(Prefs.isDisplayMarkEnabled());
        String input = inputText[0];
        Log.i(TAG, "Translator Run input " +input);
        String output = Translator.translate(input);
        Log.i(TAG, "Translator Run output " +output);
        return output;
      } catch (Throwable e) {
        e.printStackTrace();
        Log.e(TAG, "ApertiumActivity.TranslationRun MODE =" + currentMode + ";InputText = " + inputEditText.getText());
        return "error: "+e;
      } finally {
        App.timing.report();
        App.timing = null;
        Log.d(TAG, "start mem f=" + rt.freeMemory() / 1000000 + "  t=" + rt.totalMemory() / 1000000 + " m=" + rt.maxMemory() / 1000000);
      }
    }

    @Override
    protected void onPostExecute(String output) {
      translationTask = null;
      outputTextView.setText(output);
      updateGui();
    }
  }


  /**
   **
   Option menu 1. share 2. inbox 3. manage 4. setting
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.simple_option_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent = null;
    switch (item.getItemId()) {
      case R.id.share:
        share_text();
        return true;
        /*
      case R.id.inbox:
        intent = new Intent(ApertiumActivity.this, SMSInboxActivity.class);
        startActivityForResult(intent, 0);
        return true;
        */
      case R.id.install:
        intent = new Intent(ApertiumActivity.this, InstallDialog.class);
        startActivity(intent);
        return true;
      case R.id.manage:
        intent = new Intent(ApertiumActivity.this, SettingsDialog.class);
        startActivity(intent);
        return true;
      case R.id.clear:
        inputEditText.setText("");
        outputTextView.setText("");
        return true;
      case R.id.about:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about));
        WebView wv = new WebView(this);
        Log.d(TAG, getString(R.string.aboutText));
        wv.loadData(getString(R.string.aboutText), "text/html", "utf-8");
        builder.setView(wv);
        AlertDialog alert = builder.create();
        alert.show();
        /*
         try {
         ArrayList al = new ArrayList();
         Runtime rt = Runtime.getRuntime();
         while (true) {
         al.add(new byte[1000000]);
         Log.d(TAG, "al "+al.size()+"M f="+rt.freeMemory()/1000000+"  t="+rt.totalMemory()/1000000+" m="+rt.maxMemory()/1000000);
         }
         } catch (Throwable t) {
         t.printStackTrace();
         }
         */
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /* Share text
   * Intent to share translated text over other installed application services */
  private void share_text() {
    Log.i(TAG, "ApertiumActivity.share_text Started");
    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Apertium Translate");
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, outputTextView.getText().toString());
    startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) return;
    inputEditText.setText(data.getStringExtra("input"));
  }
}