package org.apertium.android.extended;

import android.content.Context;
import org.apertium.android.extended.database.DatabaseHandler;
import org.apertium.android.extended.filemanager.FileManager;
import org.apertium.android.extended.languagepair.RulesHandler;

public class Extended {
  public static Extended instance;
  public static DatabaseHandler databaseHandler;
  public static RulesHandler rulesHandler;

  public static void init(Context ctx) {
    if (databaseHandler != null) {
      return;
    }


    FileManager.setDIR();

    databaseHandler = new DatabaseHandler(ctx);
    rulesHandler = new RulesHandler(ctx);
  }
}
