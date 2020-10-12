package org.tflsh.multifacette;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.currentTimeMillis;

public class BackgroundImageDecoder extends Activity {
  static final String CLASSNAME = "BackgroundImageDecoder";
  static final ExecutorService executor = Executors.newFixedThreadPool(1);
  /**
   * Called when the user taps the Send button
   */
  private final Context mContext;
  private static int currentFile;
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private static int missingFilesNumber;
  final int screenWidth;
  final int screenHeight;

  //constructor: save the context for later uses
  public BackgroundImageDecoder(Context ctx, int width, int height,
      LruCache<String, Bitmap> memoryCacheArg, ExecutorService executorArg) {
    //    executor=executorArg;
    Log.d("asyncTaskManager", "starting helper with context");
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    mContext = ctx;
    File mCacheDir = ctx.getCacheDir();
    //tuxun: try lru cache for large bitmap
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.

    screenWidth = width;
    screenHeight = height;
  }

  public static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight
          && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////@ListImageTask////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////ASYNCTASK////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * scope: package-private
   */
  public static Bitmap decodeSampledBitmapFromFilepath(String res) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(res, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, options.outWidth, options.outHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(res, options), options.outWidth,
        options.outHeight, true);
    //! return BitmapFactory.decodeFile(res,   options);

    //! return BitmapFactory.decodeFile(res,   options);
  }


  //asynchronous thread  which should:
  // -1 download file list on https://server/project/index.php
  // -2 save it under external_cache/filelist.json
  // -3 open it
  // -4 parse it,
  // -5 start the downloading of the missing images
  // -6 return a list of string for images found (second arg of constructor)
  // -7 and a list of the missing images names (third arg of constructor)

  //return the bitmap from cache or from file. idea from https://developer.android.com/topic/performance/graphics/cache-bitmap

  @Override protected void onStop() {
    super.onStop();
    Log.d(CLASSNAME, "onStop()");

    executor.shutdown();
    while (!executor.isTerminated()) {
      executor.shutdown();

      SystemClock.sleep(30);
      Log.d(CLASSNAME, "have a struggling task");
    }
  }

  public class ShowImageTask extends Thread {
    static final String CLASSNAME = "showImageFileTask";
    final WeakReference<ImageView> bmImage;
    final int maxDelay;
    final String srcString;
    final long startTime;

    public ShowImageTask(ImageView bbmImage, @Nullable String urlSource,
        int maxDelayParam) {
      this.bmImage = new WeakReference<>(bbmImage);
      this.maxDelay = maxDelayParam;
      this.srcString = urlSource;
      startTime = currentTimeMillis();
    }

    protected void doInBackground() {
      try {
        //

        if (srcString.isEmpty()) {
          bmImage.get()
              .setImageDrawable(mContext.getResources()
                  .getDrawable(R.drawable.default_background, mContext.getTheme()));
          Log.d(CLASSNAME, "default image took " + (currentTimeMillis() - startTime) + "ms");

          return;
        }
        final Bitmap result = decodeSampledBitmapFromFilepath(srcString);

        Runnable r = new Runnable() {
          public void run() {
            long timer = currentTimeMillis() - startTime;
            long delay = maxDelay - timer;
            if (maxDelay != 0) {
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            }
            if (bmImage != null) {
              if (bmImage.get().isShown()) {
                bmImage.get().setImageBitmap(result);
                bmImage.get().setAdjustViewBounds(true);

                if (screenHeight > screenWidth) {
                  bmImage.get().setScaleType(ImageView.ScaleType.FIT_START);
                } else {
                  bmImage.get().setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
              } else {
                Log.e(CLASSNAME, "tentative d'affichage d'une image sur un composant caché");
                //  bmImage.get().setImageDrawable(mContext.getResources().getDrawable(R.drawable.whitebackground,mContext.getTheme()));

              }
            }
           /* Log.d(CLASSNAME,
                " took " + timer + "ms for decode " + srcString + " , waited " + delay + "ms");*/
          }
        };
        //executor.execute(r);
        runOnUiThread(r);
      } catch (Exception e) {
        Log.e(CLASSNAME, "Exception in showImageFileTask.doInBackground()");
        e.printStackTrace();
      }
    }

    @Override
    public void run() {
      this.doInBackground();
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////