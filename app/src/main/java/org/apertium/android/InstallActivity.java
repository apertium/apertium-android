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
package org.apertium.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 @author Mikel Artetxe, Jacob Nordfalk
 */
public class InstallActivity extends Activity implements OnItemClickListener, OnClickListener {
  static final String REPO_URL = "https://apertium.svn.sourceforge.net/svnroot/apertium/builds/language-pairs";
  private static String STR_INSTRUCTIONS = "Check the language pairs to install and uncheck the ones to uninstall.";
  private static String STR_INSTALLING = "Installing";
  private static String STR_UNINSTALLING = "Uninstalling";

  /**
   * Data regarding the activity.
   * Put in a seperate object so we don't have to reinitialize on screen change
   */
  private static class Data {
    InstallActivity activity;
    ArrayList<String> packages = new ArrayList<String>();
    HashSet<String> installedPackages = new HashSet<String>();
    HashSet<String> updatablePackages = new HashSet<String>();
    HashSet<String> updatedPackages = new HashSet<String>();
    HashSet<String> packagesToInstall = new HashSet<String>();
    HashSet<String> packagesToUninstall = new HashSet<String>();
    HashMap<String, String> packageToTitle = new HashMap<String, String>();
    HashMap<String, URL> packageToURL = new HashMap<String, URL>();
    File cachedRepoFile;
    RepoAsyncTask repoTask;
    InstallRemoveAsyncTask installTask;
    String progressText;
    private int progressMax;
    private int progress;
  }

  private Data d;
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

    /* Only for extended example - open ZIP file with Apertium language pair installer
    Intent i = getIntent();
    if (i!=null) {
      App.longToast(i.toString());
      App.longToast(""+i.getData());

    }*/

    d = (Data) getLastNonConfigurationInstance();
    if (d == null) {
      d = new Data();
      d.cachedRepoFile = new File(getCacheDir(), new File(REPO_URL).getName());
      d.progressText = "Refreshing package list, please wait...";
      d.repoTask = new RepoAsyncTask();
      d.repoTask.d = d;
      d.activity = this;
      d.repoTask.execute();
    }
    d.activity = this;
    updateUI();
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    return d;
  }
/*
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(R.string.installInternet);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.string.installInternet) {

    }
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://apertium.svn.sourceforge.net/svnroot/apertium/builds/")));
    return super.onOptionsItemSelected(item);
  }
*/
  private void updateUI() {
    setProgressBarIndeterminateVisibility(d.repoTask != null);
    progressTextView.setText(d.progressText);
    applyButton.setText(d.installTask==null?R.string.apply:R.string.cancel);
    progressBar.setVisibility(d.installTask != null ? View.VISIBLE : View.GONE);
    progressBar.setMax(d.progressMax);
    progressBar.setProgress(d.progress);
  }

  private static void initPackages(Data d, InputStream inputStream, boolean useNetwork) throws IOException {
    ArrayList<String> packages = new ArrayList<String>();
    // Get a copy of the list of installed packages, as we modify it below
    HashSet<String> installedPackages = new HashSet<String>(App.apertiumInstallation.modeToPackage.values());

    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] columns = line.split("\t");
      if (columns.length > 3) {
        // apertium-af-nl	https://apertium.svn.sourceforge.net/svnroot/apertium/builds/apertium-af-nl/apertium-af-nl.jar	file:apertium-af-nl-0.2.0.tar.gz	af-nl, nl-af
        // apertium-ca-it	https://apertium.svn.sourceforge.net/svnroot/apertium/builds/apertium-ca-it/apertium-ca-it.jar	file:apertium-ca-it-0.1.0.tar.gz	ca-it, it-ca
        String pkg = columns[0];
        packages.add(pkg);
        URL url = new URL(columns[1]);
        d.packageToURL.put(pkg, url);
        String modeTitle = LanguageTitles.getTitle(columns[3]);
        d.packageToTitle.put(pkg, modeTitle);
        if (installedPackages.contains(pkg)) {
          installedPackages.remove(pkg);
          d.installedPackages.add(pkg);
          if (useNetwork) {
            long localLastModified = new File(App.apertiumInstallation.getBasedirForPackage(pkg)).lastModified();
            long onlineLastModified = url.openConnection().getLastModified();
            if (onlineLastModified > localLastModified) {
              d.updatablePackages.add(pkg);
            } else {
              d.updatedPackages.add(pkg);
            }
          }
        }
      }
    }

    for (String pkg : installedPackages) {
      packages.add(pkg);
      d.installedPackages.add(pkg);
    }

    Collections.sort(packages);
    d.packages = packages;
  }



  private static class RepoAsyncTask extends AsyncTask {
    Data d;
    @Override
    protected Object doInBackground(Object... arg0) {
      try {
        // First load old version of the list to display
        if (d.cachedRepoFile.exists()) {
          initPackages(d, new FileInputStream(d.cachedRepoFile), false);
        } else {
          initPackages(d, d.activity.getResources().openRawResource(R.raw.language_pairs), false);
          //BugSenseHandler.sendException(new Exception("New installation"));
        }
        publishProgress();
        // Then make the check over the network
        FileUtils.copyStream(new URL(REPO_URL).openStream(), new FileOutputStream(d.cachedRepoFile));
        initPackages(d, new FileInputStream(d.cachedRepoFile), true);
        d.progressText = STR_INSTRUCTIONS;
      } catch (IOException ex) {
        ex.printStackTrace();
        d.progressText = d.activity.getString(R.string.network_error);
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
      if (d.activity == null) {
        return;
      }
      d.activity.adapter.notifyDataSetChanged();
      d.activity.updateUI();
    }

    @Override
    protected void onPostExecute(Object result) {
      d.repoTask = null;
      if (d.activity == null) {
        return;
      }
      d.activity.updateUI();
    }
  }

  private class LanguagePairAdapter extends BaseAdapter {
    public int getCount() {
      if (d == null) {
        return 0;
      }
      return d.packages.size();
    }

    public Object getItem(int n) {
      return n;
    }

    public long getItemId(int n) {
      return n;
    }

    private boolean isChecked(String pkg) {
      if (d.installedPackages.contains(pkg) && !d.packagesToUninstall.contains(pkg)) {
        return true;
      }
      if (d.packagesToInstall.contains(pkg)) {
        return true;
      }
      return false;
    }

    @Override
    public View getView(int row, View v, ViewGroup parent) {
      if (v == null) {
        v = getLayoutInflater().inflate(R.layout.simple_install_elem, null);
      }
      CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
      TextView name = (TextView) v.findViewById(R.id.name);
      TextView status = (TextView) v.findViewById(R.id.status);

      String pkg = d.packages.get(row);
      String pkgTitle = d.packageToTitle.get(pkg).replace(",","<br/>\n");
      boolean isChecked = isChecked(pkg);
      checkBox.setChecked(isChecked);

      if (d.packagesToInstall.contains(pkg)) {
        name.setText(Html.fromHtml("<html><b>" + pkgTitle + "</b></html>"));
        if (d.updatablePackages.contains(pkg)) {
          status.setText(Html.fromHtml("<html><b>Marked to update</b></html>"));
        } else {
          status.setText(Html.fromHtml("<html><b>Marked to install</b></html>"));
        }
      } else if (d.packagesToUninstall.contains(pkg)) {
        name.setText(Html.fromHtml("<html><b>" + pkgTitle + "</b></html>"));
        status.setText(Html.fromHtml("<html><b>Marked to uninstall</b></html>"));
      } else {
        name.setText(Html.fromHtml("<html>" + pkgTitle + "</html>"));
        String txt;
        if (d.updatedPackages.contains(pkg)) {
          txt = "<html><i>Installed from repository</i></html>";
        } else if (d.updatablePackages.contains(pkg)) {
          txt = "<html><i>Update available in repository</i></html>";
        } else if (d.installedPackages.contains(pkg)) {
          if (d.repoTask != null) {
            // During repo refresh packages are just listed in installedPackages, thus we end here during repo refresh
            txt = "<html><i>Installed</i></html>";
          } else {
            txt = "<html><i>Manually installed</i></html>";
          }
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

    if (!d.updatablePackages.contains(pkg)) {
      if (d.installedPackages.contains(pkg)) {
        if (d.packagesToUninstall.contains(pkg)) {
          d.packagesToUninstall.remove(pkg);
        } else {
          d.packagesToUninstall.add(pkg);
        }
      } else {
        if (d.packagesToInstall.contains(pkg)) {
          d.packagesToInstall.remove(pkg);
        } else {
          d.packagesToInstall.add(pkg);
        }
      }
    } else {
      // An updateable package - there are 3 states: untouched, update, uninstall
      if (d.packagesToInstall.contains(pkg)) {
        // update -> uninstall
        d.packagesToInstall.remove(pkg);
        d.packagesToUninstall.add(pkg);
      } else {
        if (d.packagesToUninstall.contains(pkg)) {
          // uninstall -> untouched
          d.packagesToUninstall.remove(pkg);
        } else {
          // untouched -> update
          d.packagesToInstall.add(pkg);
        }
      }
    }
    adapter.notifyDataSetChanged();
  }

  private static class InstallRemoveAsyncTask extends AsyncTask {
    Data d;

    @Override
    protected Object doInBackground(Object... arg0) {
      d.progressMax = d.packagesToInstall.size() * 100;

      int packageNo = 0;
      for (String pkg : new HashSet<String>(d.packagesToInstall)) { // Avoid ConcurrentModificationException
        if (isCancelled()) break;
        try {
          publishProgress(d.activity.getString(R.string.downloading) + " " + pkg + "...");
          URL url = d.packageToURL.get(pkg);
          URLConnection uc = url.openConnection();
          long lastModified = uc.getLastModified();
          int contentLength = uc.getContentLength();
          BufferedInputStream in = new BufferedInputStream(uc.getInputStream());
          File tmpjarfile = new File(d.activity.getCacheDir(), pkg + ".jar");
          FileOutputStream fos = new FileOutputStream(tmpjarfile);
          byte data[] = new byte[8192];
          int count;
          int total = 0;
          while ((count = in.read(data, 0, 1024)) != -1) {
            fos.write(data, 0, count);
            total += count;
            //Log.d("",""+100*packageNo + "+ 100* "+total+" / " +contentLength);
            publishProgress(100 * packageNo + 90 * total / contentLength);
          }
          fos.close();
          in.close();
          tmpjarfile.setLastModified(lastModified);
          publishProgress(d.activity.getString(R.string.installing) + " " + pkg + "...");
          App.apertiumInstallation.installJar(tmpjarfile, pkg);
          tmpjarfile.delete();
          packageNo++;
          publishProgress(98 * packageNo);
          d.installedPackages.add(pkg);
        } catch (Exception ex) {
          ex.printStackTrace();
          BugSenseHandler.sendException(ex);
          return ex;
        }
      }

      if (isCancelled()) return "";
      for (String pkg : d.packagesToUninstall) {
        publishProgress(d.activity.getString(R.string.deleting) + " " + pkg + "...");
        App.apertiumInstallation.uninstallPackage(pkg);
        d.installedPackages.remove(pkg);
      }
      d.packagesToInstall.clear();
      d.packagesToUninstall.clear();

      return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
      if (d.activity == null) {
        return;
      }
      Object v = values[0];
      //Log.d("", ""+v);
      if (v instanceof Integer) {
        d.progress = (Integer) v;
      } else {
        d.progressText = String.valueOf(v);
      }
      d.activity.updateUI();
    }

    @Override
    protected void onPostExecute(Object result) {
      d.installTask = null;
      App.apertiumInstallation.rescanForPackages();
      if (d.activity == null) {
        return;
      }
      if (result != null) {
        d.progressText = String.valueOf("" + result);
        d.activity.updateUI();
      } else {
        d.activity.finish();
      }
    }

    @Override
    protected void onCancelled() {
      d.installTask = null;
      d.progressText = "Cancelled";
      App.apertiumInstallation.rescanForPackages();
      if (d.activity == null) {
        return;
      }
      d.repoTask = new RepoAsyncTask();
      d.repoTask.d = d;
      d.repoTask.execute();
      d.activity.updateUI();
    }
  }

  public void onClick(View arg0) {
    if (d.installTask==null) {
      d.progressText = "Preparing...";
      d.installTask = new InstallRemoveAsyncTask();
      d.installTask.d = d;
      d.installTask.execute();
    } else {
      d.installTask.cancel(true);
    }
    updateUI();
  }


  @Override
  public void finish() {
    super.finish();
    if (d.repoTask != null) {
      d.repoTask.cancel(false);
    }
    if (d.installTask != null) {
      App.longToast("Continuing installation in background");
    }
  }
}
