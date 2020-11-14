
package org.tflsh.multifacette;

import android.app.Instrumentation;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertNotEquals;
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
//    Log.d(CLASSNAME, "BEFORE");

    missingFilesNames = new ArrayList<>();
    mSlideshowFilesNames = new ArrayList<>();
   // appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//    Looper.prepare();
  }

  //ce test doit passser si on a internet, et echouer si on l'a
  @Test
  public void testListImageTaskWithInternetWhileGRABBING() {
/*
    public ShowImageTask(Executor executorArg, ImageView bbmImage, @Nullable String urlSource,
        int maxDelayParam,int screenHeightArg, int screenWidthArg)*/
//Uri path = Uri.parse("android.resource://org.tflsh.multifacette/@drawable/default_background" + R.drawable.default_background);
    ImageView img=new ImageView(null);
//    assertEquals(img.getWidth(), 0);

    BackgroundImageDecoder.ShowImageTask asm = new BackgroundImageDecoder.ShowImageTask(
        Executors.newFixedThreadPool(2)
, img, "android.resource://org.tflsh.multifacette/@drawable/default_background",
    750,600,800);

    //pendant un listing fructeux, on a des  missingimg
    assertNotEquals(asm, null);
    //par contre un a un nombre d'image different de zero
  //  assertNotEquals(img.getWidth(), 0);
  }
/*
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