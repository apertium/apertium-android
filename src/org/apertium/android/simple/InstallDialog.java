/*
 * Copyright (C) 2012 Mikel Artetxe, Jacob Nordfalk
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
package org.apertium.android.simple;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apertium.Translator;
import org.apertium.android.R;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class InstallDialog extends Activity implements OnClickListener {
  static final String REPO_URL = "https://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";
  ArrayList<String> packages;
  ArrayList<String> installedPackages;
  ArrayList<String> updatablePackages;
  ArrayList<String> updatedPackages;
  HashMap<String, String> packageToFilename;
  HashMap<String, URL> packageToURL;
  Object tableContent[][];
  Object[][] originalTableContent;
  boolean packagesToInstall[], packagesToUninstall[];
  String STR_TITLE = "Install / Uninstall language pairs";
  String STR_INSTRUCTIONS = "Check the language pairs to install and uncheck the ones to uninstall.";
  String STR_INSTALL = "install";
  String STR_INSTALLING = "Installing";
  String STR_UNINSTALL = "uninstall";
  String STR_UNINSTALLING = "Uninstalling";
  private ArrayAdapter adapter;
  private ListView listView;
  private ProgressBar progressBar;
  private TextView progressTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simple_install);
    findViewById(R.id.applyButton).setOnClickListener(this);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    progressTextView = (TextView) findViewById(R.id.progressTextView);
    progressTextView.setText("Downloading package list, please wait...");
    listView = (ListView) findViewById(R.id.listView1);
    listView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0, View arg1, int row, long arg3) {
        tableContent[row][0] = !((Boolean) tableContent[row][0]);
        if (tableContent[row][0].equals(originalTableContent[row][0])) {
          tableContent[row][1] = originalTableContent[row][1];
          tableContent[row][2] = originalTableContent[row][2];
          packagesToInstall[row] = packagesToUninstall[row] = false;
        } else {
          boolean install = (Boolean) tableContent[row][0];
          tableContent[row][1] = "<html><b>" + originalTableContent[row][1] + "</b></html>";
          tableContent[row][2] = "<html><b>Marked to " + (install ? STR_INSTALL : STR_UNINSTALL) + "</b></html>";
          if (install) {
            packagesToInstall[row] = true;
          } else {
            packagesToUninstall[row] = true;
          }
        }
        adapter.notifyDataSetChanged();
      }
    });
    new DownloadTask().execute();
  }

  class DownloadTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object... arg0) {
      try {
        initPackages();
      } catch (IOException ex) {
        Logger.getLogger(InstallDialog.class.getName()).log(Level.SEVERE, null, ex);
      }
      packagesToInstall = new boolean[packages.size()];
      packagesToUninstall = new boolean[packages.size()];
      initTableContent();
      return null;
    }

    @Override
    protected void onPostExecute(Object result) {
      adapter = new ArrayAdapter(InstallDialog.this, R.layout.simple_install_elem, R.id.name, tableContent)
      {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          View v = super.getView(position, convertView, parent);
          CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
          TextView name = (TextView) v.findViewById(R.id.name);
          TextView status = (TextView) v.findViewById(R.id.status);
          checkBox.setChecked((Boolean)tableContent[position][0]);
          name.setText(Html.fromHtml((String) tableContent[position][1]));
          status.setText(Html.fromHtml((String) tableContent[position][2]));
          return v;
        }
      };
      listView.setAdapter(adapter);
    }
  }


  private void initPackages() throws IOException {
    packages = new ArrayList<String>();
    installedPackages = new ArrayList<String>();
    updatablePackages = new ArrayList<String>();
    updatedPackages = new ArrayList<String>();
    packageToFilename = new HashMap<String, String>();
    packageToURL = new HashMap<String, URL>();
    ArrayList<String> installedPackagesFilenames = new ArrayList<String>(Arrays.asList(ApertiumCaffeine.packagesDir.list(ApertiumCaffeine.filter)));

    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(REPO_URL).openStream()));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] columns = line.split("\t");
      if (columns.length > 3) {
        String p = Translator.getTitle(columns[3]);
        packages.add(p);
        URL url = new URL(columns[1]);
        packageToURL.put(p, url);
        packageToFilename.put(p, columns[0] + ".jar");
        if (installedPackagesFilenames.contains(columns[0] + ".jar")) {
          installedPackagesFilenames.remove(columns[0] + ".jar");
          installedPackages.add(p);
          long localLastModified = ApertiumCaffeine.prefs.getLong("last_modified_" + columns[0] + ".jar", -1);
          long onlineLastModified = url.openConnection().getLastModified();
          if (onlineLastModified > localLastModified) {
            updatablePackages.add(p);
          } else {
            updatedPackages.add(p);
          }
        }
      }
    }

    for (String code : installedPackagesFilenames) {
      packages.add(code);
      installedPackages.add(code);
      packageToFilename.put(code, code);
    }

    Collections.sort(packages);
    Collections.sort(updatedPackages);
    Collections.sort(updatablePackages);
  }

  void initTableContent() {
    tableContent = new Object[packages.size()][3];
    originalTableContent = new Object[packages.size()][3];
    for (int i = 0; i < packages.size(); i++) {
      tableContent[i][0] = installedPackages.contains(packages.get(i));
      tableContent[i][1] = packages.get(i);
      if (updatedPackages.contains(packages.get(i))) {
        tableContent[i][2] = "<html><i>Installed from repository</i></html>";
      } else if (updatablePackages.contains(packages.get(i))) {
        tableContent[i][2] = "<html><i>Installed from repository</i></html>";
      } else if (installedPackages.contains(packages.get(i))) {
        tableContent[i][2] = "<html><i>Manually installed</i></html>";
      } else {
        tableContent[i][2] = "<html><i>Not installed</i></html>";
      }
      for (int j=0; j<3; j++) originalTableContent[i][j] = tableContent[i][j];
    }
  }

  class InstallTask extends AsyncTask {
    int length = 1;
    @Override
    protected Object doInBackground(Object... arg0) {
      int value = 0;
      for (int i = 0; i < packagesToInstall.length; i++) {
        if (packagesToInstall[i]) {
          try {
            String pkg = (String) originalTableContent[i][1];
            URL url = packageToURL.get(pkg);
            Log.d("", pkg +" " + url);
            length += url.openConnection().getContentLength();
          } catch (Exception ex) {
            ex.printStackTrace();
            return ex;
          }
        }
      }
      App.handler.post(new Runnable() {
        public void run() {
          progressBar.setMax(length);
        }
      });

      for (int i = 0; i < packagesToInstall.length; i++) {
        if (packagesToInstall[i]) {
          try {
            publishProgress(STR_INSTALLING + " " + tableContent[i][1] + "...");
            String fn = packageToFilename.get(originalTableContent[i][1]);
            URL url = packageToURL.get(originalTableContent[i][1]);
            URLConnection uc = url.openConnection();
            long lastModified = uc.getLastModified();
            BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
            FileOutputStream fos = new FileOutputStream(new File(ApertiumCaffeine.packagesDir, fn));
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
              fos.write(data, 0, count);
              value += count;
              publishProgress(value);
            }
            fos.close();
            in.close();
            ApertiumCaffeine.prefs.edit().putLong("last_modified_" + fn, lastModified).commit();
          } catch (IOException ex) {
            ex.printStackTrace();
            return ex;
          }
        }
      }

      for (int i = 0; i < packagesToInstall.length; i++) {
        if (packagesToUninstall[i]) {
          publishProgress(STR_UNINSTALLING + " " + tableContent[i][1] + "...");
          String fn = packageToFilename.get(originalTableContent[i][1]);
          if (!new File(new File(ApertiumCaffeine.prefs.getString("packagesPath", "")), fn).delete()) {
            Log.w("Error", "Unable to uninstall " + tableContent[i][1]);
          }
          ApertiumCaffeine.prefs.edit().remove("last_modified_" + fn).commit();
        }
      }
      return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      Object v = values[0];
      if (v instanceof Integer) {
        progressBar.setProgress((Integer) v);
        //progress.setString(value * 100 / length + "%");
      } else {
        progressTextView.setText(String.valueOf(v));
      }
    }

    @Override
    protected void onPostExecute(Object result) {
      if (result!=null) {
        App.kortToast(""+result);
      } else {
        finish();
      }
    }
  }

  public void onClick(View arg0) {
    //progressTextView.setText(STR_INSTALLING + "...");
    progressTextView.setText("Preparing...");
    new InstallTask().execute();
  }
}
