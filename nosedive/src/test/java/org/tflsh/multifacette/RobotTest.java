package org.tflsh.multifacette;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

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
    activity.findViewById(R.id.repairFilesButton).performClick();

    Log.d(TAG+"testFragment", "testing " + activity.toString());
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(dlOkIntent, activity.getIntent());

  }
  @Test public void testLaunchLoginActivity()
  {
    activity.findViewById(R.id.loginImageButton).performClick();

    Log.d(TAG+"testFragment", "testing " + activity.toString());
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
        Intent expectedIntent = new Intent(activity, EmailPasswordActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    Log.d(TAG+"testFragment", "testing " + dlOkIntent.getComponent());
    Log.d(TAG+"testFragment", "testing " + activity.getIntent().getComponent());

  //  Assert.assertEquals(dlOkIntent.getComponent(), activity.getIntent().getComponent());
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }



  @Test public void testGrabJson()
  {
    activity.findViewById(R.id.repairFilesButton).performClick();

    Log.d(TAG+"testFragment", "testing " + activity.toString());

    assertEquals(dlFailedIntent, activity.getIntent());

  }

  @Test public void testParseJson()
  {
    activity.findViewById(R.id.repairFilesButton).performClick();

    Log.d(TAG+"testFragment", "testing " + activity.toString());


    assertEquals(expectedIntent.getComponent(), activity.getIntent().getComponent());



  }
  @Test public void testDecodeImage()
  {
    activity.findViewById(R.id.repairFilesButton).performClick();

    Log.d(TAG+"testFragment", "testing " + activity.toString());

//todo create 4th intent
    assertEquals(dlOkIntent, activity.getIntent());

  }

}
