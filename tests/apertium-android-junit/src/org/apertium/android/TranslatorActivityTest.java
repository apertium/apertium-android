/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apertium.android;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

public class TranslatorActivityTest extends android.test.ActivityInstrumentationTestCase2<TranslatorActivity> {
	private TextView outputTextView;
	private EditText inputEditText;
	private Button submitButton;

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
    submitButton = (Button) a.findViewById(R.id.translateButton);
  }

  public void testPreconditions() {
    assertNotNull(outputTextView);
    assertNotNull(inputEditText);
    assertNotNull(submitButton);
  }

  public void testObserversOk() {
		assertTrue(App.apertiumInstallation.observers.contains(a.apertiumInstallationObserver));
  }


  @UiThreadTest
  public void testNoPackagesShouldStartInstallActivity() {

		App.apertiumInstallation.rescanForPackages();
		Set<String> set = new HashSet<String>(App.apertiumInstallation.modeToPackage.values());
		for (String s : set) App.apertiumInstallation.uninstallPackage(s);
		App.apertiumInstallation.rescanForPackages();

		ActivityMonitor am = new Instrumentation.ActivityMonitor(InstallActivity.class.getName(), null, true);
		getInstrumentation().addMonitor(am);

		a.onClick(submitButton);
		assertEquals("Expected InstallActivity to be invoked", 1, am.getHits());
  }

}
