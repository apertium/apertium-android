package org.apertium.android;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import java.io.File;
import java.io.IOException;
import org.apertium.utils.IOUtils;

public class App extends Application {
  public static boolean isSdk() {
    return Build.PRODUCT.contains("sdk");//.equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT);
  }
  public static App instance;
  public static Handler handler;
  public static SharedPreferences prefs;

  public static final String PREF_lastModeTitle = "lastModeTitle";
  public static final String PREF_cacheEnabled = "cacheEnabled";
  public static final String PREF_displayMark = "displayMark";
  public static final String PREF_clipBoardGet = "ClipGetPref";
  public static final String PREF_clipBoardPush = "ClipPushPref";
  public static final String PREF_showInitialText = "show initial text";

  public static void reportError(Exception ex) {
    ex.printStackTrace();
    longToast("Error: " + ex);
    longToast("The error will be reported to the developers. sorry for the inconvenience.");
    BugSenseHandler.sendException(ex);
  }
  public static ApertiumInstallation apertiumInstallation;

  @Override
  public void onCreate() {
    super.onCreate();
    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // If you want to use BugSense for your fork, register with
    // them and place your own API key in /assets/bugsense.txt
    if (!BuildConfig.DEBUG) try {
      byte[] buffer = new byte[16];
      int n = getAssets().open("bugsense.txt").read(buffer);
      String key = new String(buffer, 0, n).trim();

      Log.d("TAG", "Using bugsense key '" + key + "'");
      BugSenseHandler.initAndStartSession(this, key);
    } catch (IOException e) {
      Log.d("TAG", "No bugsense keyfile found");
    }

    instance = this;
    handler = new Handler();

		// The '2' below is for historic reasons, we keep these names as users have already installed pairs there
    File packagesDir = new File(getFilesDir(), "packages2"); // where packages' data are installed
    File bytecodeDir = new File(getFilesDir(), "bytecode2"); // where packages' bytecode are installed. Must be private
    File bytecodeCacheDir = new File(getCacheDir(), "bytecodecache2"); // where bytecode cache is kept. Must be private
    IOUtils.cacheDir = new File(getCacheDir(), "apertium-index-cache2"); // where cached transducerindexes are kept
    apertiumInstallation = new ApertiumInstallation(packagesDir, bytecodeDir, bytecodeCacheDir);
    apertiumInstallation.rescanForPackages();

    Log.i("TAG", "IOUtils.cacheDir set to " + IOUtils.cacheDir);

  }

  public static void longToast(final String txt) {
    Log.d("TAG", txt);
    handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(instance, txt, Toast.LENGTH_LONG).show();
      }
    });
  }

  /* Version fra http://developer.android.com/training/basics/network-ops/managing.html */
  public static boolean isOnline() {
    ConnectivityManager connMgr = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }
}
