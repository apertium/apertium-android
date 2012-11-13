package org.apertium.android;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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
import org.apertium.android.database.DatabaseHandler;
import org.apertium.android.filemanager.FileManager;
import org.apertium.android.helper.ClipboardHandler;
import org.apertium.android.helper.Prefs;
import org.apertium.android.languagepair.RulesHandler;
import org.apertium.utils.IOUtils;

public class App extends Application {
  public static boolean isSdk() {
    return Build.PRODUCT.contains("sdk");//.equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT);
  }
  public static App instance;

  private Handler forgrundstråd;
  public static DatabaseHandler databaseHandler;
  public static ClipboardHandler clipboardHandler;
  public static RulesHandler rulesHandler;
  public static Handler handler;


  @Override
  public void onCreate() {
    super.onCreate();

    // If you want to use BugSense for your fork, register with
    // them and place your API key in /assets/bugsense.txt
    try {
      //String key = getString(R.string.bugsensekey);
      byte[] buffer = new byte[16];
      int n = getAssets().open("bugsense.txt").read(buffer);
      String key = new String(buffer, 0, n).trim();

      Log.d("TAG", "Using bugsense key '"+key+"'");
      BugSenseHandler.initAndStartSession(this, key);
    } catch (IOException e) {
      Log.d("TAG", "No bugsense keyfile found");
    }

    IOUtils.cacheDir = new File(getCacheDir(),"apertium-cache/");
    Log.i("TAG", "IOUtils.cacheDir set to "+IOUtils.cacheDir);

    instance = this;
    forgrundstråd = new Handler();

    Prefs.init(this);


    FileManager.setDIR();

    databaseHandler     = new DatabaseHandler(this);
    rulesHandler        = new RulesHandler(this);
    clipboardHandler    = new ClipboardHandler(this);
    handler             = new Handler();


    try {
      Class.forName("android.os.AsyncTask"); // Fix for http://code.google.com/p/android/issues/detail?id=20915
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  public static void langToast(final String txt) {
    //new Throwable(txt).printStackTrace();
    instance.forgrundstråd.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(instance, txt, Toast.LENGTH_LONG).show();
      }
    });
  }

  public static void kortToast(final String txt) {
    //new Throwable(txt).printStackTrace();
    instance.forgrundstråd.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(instance, txt, Toast.LENGTH_SHORT).show();
      }
    });
  }


  /* Version fra http://developer.android.com/training/basics/network-ops/managing.html */
	public static boolean isOnline()
	{
    ConnectivityManager connMgr = (ConnectivityManager)
                instance.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }
}
