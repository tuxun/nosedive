package org.tflsh.multifacette;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(sdk = { Build.VERSION_CODES.O_MR1})
@RunWith(RobolectricTestRunner.class)
/*
 androidx.test.core.app.ActivityScenario
Simulates starting activity with the given class type and returns its reference.
Use androidx.test.core.app.ActivityScenario



 */
public class RobotTest {
  private final String TAG="RobotTest";
  SlideshowActivity activity;
  Intent expectedIntent;
  Intent dlFailedIntent;
  Intent dlOkIntent;
  @Before
  public void createTest()
  {
    //Context appContext =InstrumentationRegistry.getInstrumentation().getTargetContext();

    //mTarget=new SlideshowActivity();
    //noinspection deprecation
    activity = Robolectric.setupActivity(SlideshowActivity.class);



     expectedIntent = new Intent(activity, SlideshowActivity.class);

     dlFailedIntent = new Intent(activity, SlideshowActivity.class);
    dlFailedIntent.setAction("dlFailed");

     dlOkIntent = new Intent(activity, SlideshowActivity.class);
    dlOkIntent.setAction("dlCompleted");
  }


  @Test public void testGrabImage()
  {
    //activity.findViewById(R.id.login).performClick();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.d(TAG+"testFragment", "testing " + activity.toString());


    Assert.assertEquals(dlOkIntent, activity.getIntent());

  }



  @Test public void testGrabJson()
  {
    //activity.findViewById(R.id.login).performClick();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.d(TAG+"testFragment", "testing " + activity.toString());

    Assert.assertEquals(dlFailedIntent, activity.getIntent());

  }

  @Test public void testParseJson()
  {
    //activity.findViewById(R.id.login).performClick();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.d(TAG+"testFragment", "testing " + activity.toString());


    Assert.assertEquals(expectedIntent.getComponent(), activity.getIntent().getComponent());



  }
  @Test public void testDecodeImage()
  {
    //activity.findViewById(R.id.login).performClick();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.d(TAG+"testFragment", "testing " + activity.toString());

//tyodo create 4th intent
//    Assert.assertEquals(dlOkIntent, activity.getIntent());

  }

}
