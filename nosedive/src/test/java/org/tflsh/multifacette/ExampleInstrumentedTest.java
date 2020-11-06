
package org.tflsh.multifacette;

import android.app.Instrumentation;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
/*state of the app:
   TOUPDATE=APPVIERGE, NOINTERNET for grab it
   TOCONNECT=on a un json mais il manque des images et internet pour les recup
   TOCHECK=le json a pu etre lu et il ne manque pas d'images, mais on n'avait pas internet au depart
   TORUN= le json vient d'etre recup du net et il ne manque pas d'images (l'app est a jour)

 */

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(JUnit4.class)
public class ExampleInstrumentedTest extends Instrumentation {

  final String CLASSNAME = "ExampleInstrumentedTest";
  //final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" + "julia/";
  ArrayList<String> missingFilesNames;
  ArrayList<String> mSlideshowFilesNames;
  //Context appContext;

  @Before
  public void initVARS() {
    Log.d(CLASSNAME, "BEFORE");

    missingFilesNames = new ArrayList<>();
    mSlideshowFilesNames = new ArrayList<>();
   // appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    Looper.prepare();
  }
/*
  //ce test doit passser si on a internet, et echouer si on l'a
  @Test
  public void testListImageTaskWithInternetWhileGRABBING() {

    AsyncTaskManager asm = new AsyncTaskManager(appContext);
    asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);
    try {
      Thread.sleep(5 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //pendant un listing fructeux, on a des  missingimg
    assertNotEquals(missingFilesNames.size(), 0);
    //par contre un a un nombre d'image different de zero
    assertNotEquals(mSlideshowFilesNames.size(), 0);
  }

  @Test
  public void testListImageTaskWithInternet() {

    AsyncTaskManager asm = new AsyncTaskManager(appContext);
    asm.new listImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);
    try {
      Thread.sleep(120 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //apres un listimg d'image fructueux, on a des  missingimg
    assertNotEquals(missingFilesNames.size(), 0);
    //par contre un a un nombre d'image different de zero
    assertNotEquals(mSlideshowFilesNames.size(), 0);
  }

  //ce test doit passser si on n'a pas internet, et echouer si on l'a
  @Test
  public void testListImageTaskWithoutInternetNorJSON() {

    StartupFragment asm = new StartupFragment();
    //we should delete JSON
    File json = new File(appContext.getExternalCacheDir() + "/filelist.json");
    Log.d(TAG, "NO JSON AND NO NET");
    if (json.delete()) {
      Log.d(TAG, "deleting " + json.getPath());
    } else {
      Log.e(TAG, "unable to delete " + json.getPath());
    }
    asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //apres un listimg d'image qui n'a pas internet et pas dejson fructueux:
    //  on n'a plus de missingimg
    assertEquals(missingFilesNames.size(), 0);
    //ET on un nombre d'image à zero
    assertEquals(mSlideshowFilesNames.size(), 0);
    //le json ne devra pas exister
    assertFalse(json.exists());
    //le json ou devrait etre vide
    assertEquals(json.length(), 0);
  }


  */

}