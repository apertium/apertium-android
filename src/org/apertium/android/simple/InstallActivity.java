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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apertium.Translator;
import org.apertium.android.R;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class InstallActivity extends Activity implements OnItemClickListener, OnClickListener {
  static final String REPO_URL = "https://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";

  private static String STR_INSTRUCTIONS = "Check the language pairs to install and uncheck the ones to uninstall.";
  private static String STR_INSTALLING = "Installing";
  private static String STR_UNINSTALLING = "Uninstalling";


  /** Data regarding the activity.
      Put in a seperate object so we don't have to reinitialize on screen change */
  private static class Data {
    ArrayList<String> packages = new ArrayList<String>();
    HashSet<String> installedPackages = new HashSet<String>();
    HashSet<String> updatablePackages = new HashSet<String>();
    HashSet<String> updatedPackages = new HashSet<String>();
    HashMap<String, String> packageToJarfile = new HashMap<String, String>();
    HashMap<String, URL> packageToURL = new HashMap<String, URL>();
    HashSet<String> packagesToInstall = new HashSet<String>();
    HashSet<String> packagesToUninstall = new HashSet<String>();
    File cachedRepoFile;
    RepoAsyncTask repoTask;
    InstallAsyncTask installTask;
    String progressText;
    private int progressMax;
    private int progress;
  }

  private static Data d;

  private ListView listView;
  private ProgressBar progressBar;
  private TextView progressTextView;
  private Button applyButton;
  private LanguagePairAdapter adapter = new LanguagePairAdapter();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.simple_install);
    applyButton = (Button) findViewById(R.id.applyButton);
    applyButton.setOnClickListener(this);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);
    progressTextView = (TextView) findViewById(R.id.progressTextView);
    listView = (ListView) findViewById(R.id.listView1);
    listView.setOnItemClickListener(this);
    listView.setAdapter(adapter);

    if (d == null) {
      d = new Data();
      d.cachedRepoFile = new File(getCacheDir(), new File(REPO_URL).getName());
      d.progressText = "Downloading package list, please wait...";
      d.repoTask = new RepoAsyncTask();
      d.repoTask.activity = this;
      d.repoTask.execute();
    }
    if (d.repoTask!=null) d.repoTask.activity = this;

    updateUI();
  }


  private void updateUI() {
    setProgressBarIndeterminateVisibility(d.repoTask!=null);
    progressTextView.setText(d.progressText);
    progressBar.setVisibility(d.installTask!=null?View.VISIBLE:View.GONE);
    progressBar.setMax(d.progressMax);
    progressBar.setProgress(d.progress);
  }



  private static void initPackages(InputStream inputStream, boolean useNetwork) throws IOException {
    ArrayList<String> installedPackagesFilenames = new ArrayList<String>(Arrays.asList(ApertiumInstallation.packagesDir.list(ApertiumInstallation.apertiumDirectoryFilter)));
    ArrayList<String> packages = new ArrayList<String>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] columns = line.split("\t");
      if (columns.length > 3) {
        String p = Translator.getTitle(columns[3]);
        packages.add(p);
        URL url = new URL(columns[1]);
        d.packageToURL.put(p, url);
        d.packageToJarfile.put(p, columns[0] + ".jar");
        if (installedPackagesFilenames.contains(columns[0])) {
          installedPackagesFilenames.remove(columns[0]);
          d.installedPackages.add(p);
          if (useNetwork) {
            long localLastModified = new File(ApertiumInstallation.packagesDir, columns[0]).lastModified();
            long onlineLastModified = url.openConnection().getLastModified();
            if (onlineLastModified > localLastModified) {
              d.updatablePackages.add(p);
            } else {
              d.updatedPackages.add(p);
            }
          }
        }
      }
    }

    for (String code : installedPackagesFilenames) {
      packages.add(code);
      d.installedPackages.add(code);
      d.packageToJarfile.put(code, code);
    }

    Collections.sort(packages);
    d.packages = packages;
  }

  private static class RepoAsyncTask extends AsyncTask {
    private InstallActivity activity;

    @Override
    protected Object doInBackground(Object... arg0) {
      try {
        // First load old version of the list to display
        if (d.cachedRepoFile.exists()) {
          initPackages(new FileInputStream(d.cachedRepoFile), false);
        } else {
          initPackages(activity.getResources().openRawResource(R.raw.language_pairs), false);
        }
        publishProgress();
        // Then make the check over the network
        FileUtils.copyStream(new URL(REPO_URL).openStream(), new FileOutputStream(d.cachedRepoFile));
        initPackages(new FileInputStream(d.cachedRepoFile), true);
        d.progressText = STR_INSTRUCTIONS;
      } catch (IOException ex) {
        ex.printStackTrace();
        d.progressText = activity.getString(R.string.network_error);
      } catch (Exception ex) {
        ex.printStackTrace();
        d.progressText = ex.toString();
        App.reportError(ex);
      }
      publishProgress();
      return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      if (activity==null) return;
      activity.adapter.notifyDataSetChanged();
      activity.updateUI();
    }

    @Override
    protected void onPostExecute(Object result) {
      d.repoTask = null;
      if (activity==null) return;
      activity.updateUI();
      activity = null;
    }
  }

  private class LanguagePairAdapter extends BaseAdapter
  {
    public int getCount() {
      if (d==null) return 0;
      return d.packages.size();
    }

    public Object getItem(int n) {
      return n;
    }

    public long getItemId(int n) {
      return n;
    }

    private boolean isChecked(String pkg) {
      if (d.installedPackages.contains(pkg) && !d.packagesToUninstall.contains(pkg)) return true;
      if (d.packagesToInstall.contains(pkg)) return true;
      return false;
    }

    @Override
    public View getView(int row, View v, ViewGroup parent) {
      if (v==null) v=getLayoutInflater().inflate(R.layout.simple_install_elem, null);
      CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
      TextView name = (TextView) v.findViewById(R.id.name);
      TextView status = (TextView) v.findViewById(R.id.status);

      String pkg = d.packages.get(row);
      boolean isChecked = isChecked(pkg);
      checkBox.setChecked(isChecked);

      if (d.packagesToInstall.contains(pkg)) {
        name.setText(Html.fromHtml("<html><b>" + pkg + "</b></html>"));
        status.setText(Html.fromHtml("<html><b>Marked to install</b></html>"));
      } else if (d.packagesToUninstall.contains(pkg)) {
        name.setText(Html.fromHtml("<html><b>" + pkg + "</b></html>"));
        status.setText(Html.fromHtml("<html><b>Marked to uninstall</b></html>"));
      } else  {
        name.setText(pkg);
        String txt;
        if (d.updatedPackages.contains(pkg)) {
          txt = "<html><i>Installed from repository</i></html>";
        } else if (d.updatablePackages.contains(pkg)) {
          txt = "<html><i>Installed from repository</i></html>";
        } else if (d.installedPackages.contains(pkg)) {
          txt = "<html><i>Manually installed</i></html>";
        } else {
          txt = "<html><i>Not installed</i></html>";
        }
        status.setText(Html.fromHtml(txt));
      }
      return v;
    }
  };


  public void onItemClick(AdapterView<?> arg0, View arg1, int row, long arg3) {
    String pkg = d.packages.get(row);

    if (d.installedPackages.contains(pkg)) {
      if (d.packagesToUninstall.contains(pkg)) d.packagesToUninstall.remove(pkg);
      else d.packagesToUninstall.add(pkg);
    } else {
      if (d.packagesToInstall.contains(pkg)) d.packagesToInstall.remove(pkg);
      else d.packagesToInstall.add(pkg);
    }
    adapter.notifyDataSetChanged();
  }


  private static class InstallAsyncTask extends AsyncTask {
    private InstallActivity activity;
    @Override
    protected Object doInBackground(Object... arg0) {
      d.progressMax = d.packagesToInstall.size()*100;

      int packageNo = 0;
      for (String pkg : d.packagesToInstall) {
        if (isCancelled()) return null;
        try {
          publishProgress(activity.STR_INSTALLING + " " + pkg + "...");
          URL url = d.packageToURL.get(pkg);
          URLConnection uc = url.openConnection();
          long lastModified = uc.getLastModified();
          int contentLength = uc.getContentLength();
          BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
          File tmpjarfile = new File(activity.getCacheDir(), d.packageToJarfile.get(pkg));
          FileOutputStream fos = new FileOutputStream(tmpjarfile);
          byte data[] = new byte[8192];
          int count;
          int total = 0;
          while ((count = in.read(data, 0, 1024)) != -1) {
            fos.write(data, 0, count);
            total += count;
            Log.d("",""+100*packageNo + "+ 100* "+total+" / " +contentLength);
            publishProgress(100*packageNo + 100*total/contentLength);
          }
          fos.close();
          in.close();
          File dir = new File(ApertiumInstallation.packagesDir, ApertiumInstallation.stripJar(d.packageToJarfile.get(pkg)));
          FileUtils.unzip(tmpjarfile.getPath(), dir.getPath());
          File jarfile = new File(ApertiumInstallation.packagesDir, d.packageToJarfile.get(pkg));
          tmpjarfile.renameTo(jarfile);
          dir.setLastModified(lastModified);
          // TODO: Remove all unneeded stuff from jarfile // jarfile.delete();
          d.installedPackages.add(pkg);
        } catch (IOException ex) {
          ex.printStackTrace();
          return ex;
        }
        packageNo++;
      }

      for (String pkg : d.packagesToUninstall) {
        publishProgress(activity.STR_UNINSTALLING + " " + pkg + "...");
        String fn = d.packageToJarfile.get(pkg);
        FileUtils.remove(new File(ApertiumInstallation.packagesDir, fn));
        FileUtils.remove(new File(ApertiumInstallation.packagesDir, ApertiumInstallation.stripJar(fn)));
        FileUtils.remove(new File(ApertiumInstallation.dexBytecodeCache, fn));
        d.installedPackages.remove(pkg);
      }
      d.packagesToInstall.clear();
      d.packagesToUninstall.clear();

      ApertiumInstallation.instance.scanForPackages();
      return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      if (activity==null) return;
      Object v = values[0];
      //Log.d("", ""+v);
      if (v instanceof Integer) {
        d.progress = (Integer) v;
      } else {
        d.progressText = String.valueOf(v);
      }
      activity.updateUI();
    }

    @Override
    protected void onPostExecute(Object result) {
      if (activity==null) return;
      if (result!=null) {
        d.progressText = String.valueOf(""+result);
        activity.updateUI();
      } else {
        activity.finish();
      }
    }
  }

  public void onClick(View arg0) {
    d.progressText = "Preparing...";
    updateUI();
    //progressTextView.setText(STR_INSTALLING + "...");
    d.installTask = new InstallAsyncTask();
    d.installTask.activity = this;
    d.installTask.execute();
    updateUI();
  }

  @Override
  public void finish() {
    super.finish();
    if (d.repoTask!=null) d.repoTask.cancel(false);
    d = null;
  }
}
