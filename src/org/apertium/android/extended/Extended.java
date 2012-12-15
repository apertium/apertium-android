package org.apertium.android.extended;

import android.content.Context;
import android.util.Log;
import java.io.File;
import org.apertium.android.extended.database.DatabaseHandler;
import org.apertium.android.extended.filemanager.FileManager;
import org.apertium.android.extended.helper.ClipboardHandler;
import org.apertium.android.extended.helper.ClipboardHandlerV11;
import org.apertium.android.extended.helper.Prefs;
import org.apertium.android.extended.languagepair.RulesHandler;
import org.apertium.utils.IOUtils;

public class Extended {
  public static Extended instance;
  public static DatabaseHandler databaseHandler;
  public static ClipboardHandler clipboardHandler;
  public static RulesHandler rulesHandler;

  public static void init(Context ctx) {
    if (databaseHandler!=null) return;

    IOUtils.cacheDir = new File(ctx.getCacheDir(), "apertium-cache/");
    Log.i("TAG", "IOUtils.cacheDir set to " + IOUtils.cacheDir);

    Prefs.init(ctx);


    FileManager.setDIR();

    databaseHandler = new DatabaseHandler(ctx);
    rulesHandler = new RulesHandler(ctx);
    int sdk = android.os.Build.VERSION.SDK_INT;
    if (sdk < 11) {
      clipboardHandler = new ClipboardHandler(ctx);
    } else {
      clipboardHandler = new ClipboardHandlerV11(ctx);
    }
  }
}
