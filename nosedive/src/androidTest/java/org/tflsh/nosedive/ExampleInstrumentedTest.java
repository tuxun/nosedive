
package org.tflsh.nosedive;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest extends Instrumentation {


    @Test
    public void hmmm() {

        String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" + "julia/";
        ArrayList<String> missingFilesNames = new ArrayList<>();
        ArrayList<String> mSlideshowFilesNames = new ArrayList<>();
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        asyncTaskManager asm = new asyncTaskManager(this.getContext());


        asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);
        assertEquals(4, 2 + 2);

    }

}
