/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apertium.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

public class TranslatorActivityTest extends android.test.ActivityInstrumentationTestCase2<TranslatorActivity> {
	private TextView outputTextView;
	private EditText inputEditText;
	private Button translateButton;

  public TranslatorActivityTest() {
    super(TranslatorActivity.class);
  }

  private TranslatorActivity a;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    a = this.getActivity();
    outputTextView = (TextView) a.findViewById(R.id.outputText);
    inputEditText = (EditText) a.findViewById(R.id.inputtext);
    translateButton = (Button) a.findViewById(R.id.translateButton);
  }

  public void testPreconditions() {
    assertNotNull(outputTextView);
    assertNotNull(inputEditText);
    assertNotNull(translateButton);
  }

  public void testObserversOk() {
		assertTrue(App.apertiumInstallation.observers.contains(a.apertiumInstallationObserver));
  }

  public void testNoPackagesShouldStartInstallActivity() {
		deleteAllLanguagePairs();

		ActivityMonitor am = new Instrumentation.ActivityMonitor(InstallActivity.class.getName(), null, false);
		getInstrumentation().addMonitor(am);
		a.onClick(translateButton);
    getInstrumentation().waitForIdleSync();
    getInstrumentation().removeMonitor(am);
    Activity a2 = am.getLastActivity();
    a2.finish();

		assertEquals(InstallActivity.class, a2.getClass());
  }

  public static void deleteAllLanguagePairs() {
    App.apertiumInstallation.rescanForPackages();
    Set<String> set = new HashSet<String>(App.apertiumInstallation.modeToPackage.values());
    for (String s : set) App.apertiumInstallation.uninstallPackage(s);
    App.apertiumInstallation.rescanForPackages();
  }

}
