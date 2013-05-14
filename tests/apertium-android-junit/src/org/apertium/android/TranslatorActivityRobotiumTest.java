/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apertium.android;

import com.jayway.android.robotium.solo.Solo;
import static junit.framework.Assert.assertFalse;

public class TranslatorActivityRobotiumTest extends android.test.ActivityInstrumentationTestCase2<TranslatorActivity> {
  private Solo solo;

  public TranslatorActivityRobotiumTest() {
    super(TranslatorActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    solo = new Solo(getInstrumentation(), getActivity());
  }

  public void testSoloNoPackagesShouldStartInstallActivity() {

    TranslatorActivityTest.deleteAllLanguagePairs();

    solo.assertCurrentActivity(null, TranslatorActivity.class);
    solo.clickOnButton("Translate");
    solo.assertCurrentActivity(null, InstallActivity.class);
    solo.getCurrentActivity().finish();
  }

  public void testSoloInstallEsperantoEnglish() throws InterruptedException {

    TranslatorActivityTest.deleteAllLanguagePairs();

    solo.assertCurrentActivity(null, TranslatorActivity.class);
    solo.clickOnButton("Choose languages");
    solo.assertCurrentActivity(null, InstallActivity.class);
    assertFalse(solo.searchText("Marked to install"));
    solo.scrollListToTop(0);
    solo.clickOnText("Esperanto . English");
    assertTrue(solo.searchText("Marked to install"));
    solo.clickOnButton("Apply");
    solo.waitForActivity(TranslatorActivity.class);
    solo.clearEditText(0);
    solo.enterText(0, "Saluton mondo, mi testas la programon");
    solo.clickOnButton("Translate");
    assertEquals("Hello world, I test the program", solo.getEditText(1).getText().toString());
    Thread.sleep(500);
  }

  @Override
  protected void tearDown() throws Exception {
    //solo.finishOpenedActivities();
    super.tearDown();
  }



}
