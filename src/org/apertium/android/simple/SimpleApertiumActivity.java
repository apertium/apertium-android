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
 SimpleApertiumActivity.java Main Launcher Activity of application

 @author Arink Verma, Jacob Nordfalk

 */
package org.apertium.android.simple;

import org.apertium.Translator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.bugsense.trace.BugSenseHandler;
import dalvik.system.DexClassLoader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apertium.android.R;
import org.apertium.android.extended.ExtendedApertiumActivity;
import org.apertium.android.extended.helper.Prefs;
import org.apertium.pipeline.Program;
import org.apertium.utils.IOUtils;
import org.apertium.utils.Timing;

public class SimpleApertiumActivity extends Activity implements OnClickListener {
  private static final String TAG = "ApertiumActiviy";

  /*Layout variable*/
  private EditText inputEditText;
  private TextView outputTextView;
  private Button fromButton;
  private Button submitButton;

  /*Mode related variable*/
  private String currentModeTitle = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(true);

    setContentView(R.layout.simple_layout);
    outputTextView = (TextView) findViewById(R.id.outputText);
    inputEditText = (EditText) findViewById(R.id.inputtext);

    submitButton = (Button) findViewById(R.id.translateButton);
    fromButton = (Button) findViewById(R.id.fromButton);

    submitButton.setOnClickListener(this);
    fromButton.setOnClickListener(this);

    // Connect asynctask to the correct activity
    if (translationTask!=null) translationTask.activity = this;
  }

  /* OnResume */
  @Override
  protected void onResume() {
    super.onResume();
    // Set from last selected mode if not set
    if (currentModeTitle==null) {
      currentModeTitle = App.prefs.getString(App.PREF_lastModeTitle, null);
    }
    // Reset if that mode isnt installed (anymore)
    if (!App.apertiumInstallation.titleToMode.containsKey(currentModeTitle)) {
      currentModeTitle = null;
    }
    // If there is no mode set at this stage then just pick any which is installed
    if (currentModeTitle==null && App.apertiumInstallation.titleToMode.size()>0) {
      currentModeTitle = App.apertiumInstallation.titleToMode.keySet().iterator().next();
    }
    // And, show on the button
    if (currentModeTitle!=null) {
      fromButton.setText(currentModeTitle);
    } else {
      fromButton.setText(R.string.choose_languages);
    }
  }


  @Override
  public void onClick(View v) {
    if (App.apertiumInstallation.titleToBasedir.isEmpty()) {
      startActivity(new Intent(this, InstallActivity.class));
      return;
    }

    if (v.equals(fromButton)) {
      ArrayList<String> modeTitle = new ArrayList<String>(App.apertiumInstallation.titleToBasedir.keySet());
      Collections.sort(modeTitle);
      modeTitle.add(getString(R.string.download_languages));

      final String[] modeTitlex = modeTitle.toArray(new String[modeTitle.size()]);
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.choose_languages));
      builder.setItems(modeTitlex, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int position) {
          if (position==modeTitlex.length-1) { // if user chose 'download_languages'
            startActivity(new Intent(App.instance, InstallActivity.class));
            return;
          }
          currentModeTitle = modeTitlex[position];
          fromButton.setText(currentModeTitle);
          App.prefs.edit().putString(App.PREF_lastModeTitle, currentModeTitle).commit();
          updateUi();
        }
      });
      AlertDialog alert = builder.create();
      alert.show();
    }
    else if (v.equals(submitButton)) {
      //Hiding soft keypad
      InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(inputEditText.getApplicationWindowToken(), 0);

      Translator.setCacheEnabled(Prefs.isCacheEnabled());
      Translator.setDisplayMarks(Prefs.isDisplayMarkEnabled());
      Translator.setDelayedNodeLoadingEnabled(true);
      Translator.setParallelProcessingEnabled(false);
      try {
        String mode = App.apertiumInstallation.titleToMode.get(currentModeTitle);
        // new DexClassLoader(/mnt/sdcard/apertium/jars/en-eo,eo-en/en-eo,eo-en.jar,/data/data/org.apertium.android/app_dex, null, dalvik.system.PathClassLoader[/data/app/org.apertium.android-2.apk]
        String basedir = App.apertiumInstallation.titleToBasedir.get(currentModeTitle);
        Log.d(TAG, "new DexClassLoader(" + basedir+".jar");
        DexClassLoader classloader = new DexClassLoader(basedir+".jar", App.apertiumInstallation.dexBytecodeCache.getAbsolutePath(), null, this.getClass().getClassLoader());
        Translator.setBase(basedir, classloader);
        Translator.setMode(mode);
        translationTask = new TranslationTask();
        translationTask.activity = this;
        translationTask.execute(inputEditText.getText().toString());
      } catch (Exception e) {
        e.printStackTrace();
        App.langToast(e.toString());
        BugSenseHandler.sendException(e);
      }
      updateUi();
    }
  }


  private void updateUi() {
    boolean ready = translationTask==null;
    submitButton.setEnabled(ready);
    submitButton.setText(ready?R.string.translate:R.string.translating);
    setProgressBarIndeterminateVisibility(!ready);
  }


  TranslationTask translationTask;

  /* Translation Thread,
   * Load translation rules and excute lttoolbox.jar */
  static class TranslationTask extends AsyncTask<String, Object, String> implements Translator.TranslationProgressListener {
    private SimpleApertiumActivity activity;

    @Override
    protected String doInBackground(String... inputText) {
      Runtime rt = Runtime.getRuntime();
      Log.d(TAG, "start mem f=" + rt.freeMemory() / 1000000 + "  t=" + rt.totalMemory() / 1000000 + " m=" + rt.maxMemory() / 1000000);
      IOUtils.timing = new org.apertium.utils.Timing("overall");
      try {
        String input = inputText[0];
        Log.i(TAG, "Translator Run input " +input);
        Timing timing = new Timing("Translator.translate()");
        //String output = Translator.translate(input);
        StringWriter output = new StringWriter();
        String format = "txt";
        Translator.translate(new StringReader(input), output, new Program("apertium-des" + format), new Program("apertium-re" + format), this);
        timing.report();
        Log.i(TAG, "Translator Run output " +output);
        return output.toString();
      } catch (Throwable e) {
        e.printStackTrace();
        Log.e(TAG, "ApertiumActivity.TranslationRun MODE =" + activity.currentModeTitle + ";InputText = " + activity.inputEditText.getText());
        return "error: "+e;
      } finally {
        IOUtils.timing.report();
        IOUtils.timing = null;
        Log.d(TAG, "start mem f=" + rt.freeMemory() / 1000000 + "  t=" + rt.totalMemory() / 1000000 + " m=" + rt.maxMemory() / 1000000);
      }
    }

    public void onTranslationProgress(String task, int progress, int progressMax) {
      publishProgress(task, progress, progressMax);
    }

    @Override
    protected void onProgressUpdate(Object... v) {
      activity.outputTextView.setText(v[0]+" "+v[1]+"/"+v[2]);
    }

    @Override
    protected void onPostExecute(String output) {
      activity.translationTask = null;
      activity.outputTextView.setText(output);
      activity.updateUi();
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
        intent = new Intent(this, SMSInboxActivity.class);
        startActivityForResult(intent, 0);
        return true;
        */
      case R.id.extended:
        intent = new Intent(this, ExtendedApertiumActivity.class);
        startActivity(intent);
        return true;
      case R.id.install:
        intent = new Intent(this, InstallActivity.class);
        startActivity(intent);
        return true;
      case R.id.manage:
        intent = new Intent(this, SettingsActivity.class);
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