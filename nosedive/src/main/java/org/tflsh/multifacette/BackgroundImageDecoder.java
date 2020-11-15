package org.tflsh.multifacette;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import static java.lang.System.currentTimeMillis;

public class BackgroundImageDecoder extends Activity {
  static final String CLASSNAME = "BackgroundImageDecoder";
  /**
   * Called when the user taps the Send button
   */
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //constructor: save the context for later uses
  public BackgroundImageDecoder() {
    Log.d("asyncTaskManager", "starting helper with context");
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
  public static Bitmap decodeSampledBitmapFromFilepath(String res, int width,int height) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(res, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, width, height);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(res, options), width,
        height, true);
  }

  public static Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    int width = drawable.getIntrinsicWidth();
    width = width > 0 ? width : 1;
    int height = drawable.getIntrinsicHeight();
    height = height > 0 ? height : 1;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }
  //asynchronous thread  which should:
  // -1 download file list on https://server/project/index.php
  // -2 save it under external_cache/files_list.json
  // -3 open it
  // -4 parse it,
  // -5 start the downloading of the missing images
  // -6 return a list of string for images found (second arg of constructor)
  // -7 and a list of the missing images names (third arg of constructor)

  //return the bitmap from cache or from file. idea from https://developer.android.com/topic/performance/graphics/cache-bitmap

  @Override protected void onStop() {
    super.onStop();
    Log.d(CLASSNAME, "onStop()");

  }

  public static class ShowImageTask extends Thread {
    static final String CLASSNAME = "showImageFileTask";
    final WeakReference<ImageView> bmImage;
    private final Resources mResources;
    final String srcString;
    final long startTime;
    private final Executor executor;
    final int screenWidth;
    final int screenHeight;
    private final Context mContext;
    int maxDelay;

    public ShowImageTask(Executor executorArg, Context ctxArg, ImageView bbmImage,
        @Nullable String urlSource,
        int maxDelayParam, int screenHeightArg, int screenWidthArg) {
      this.bmImage = new WeakReference<ImageView>(bbmImage);
      this.maxDelay = maxDelayParam;
      this.mResources = ctxArg.getResources();
      this.mContext = ctxArg;
      this.srcString = urlSource;
      startTime = currentTimeMillis();
      executor = executorArg;
      screenWidth = screenWidthArg;
      screenHeight = screenHeightArg;
    }

    protected void doInBackground() {
      try {
        Log.e(CLASSNAME, "trying to decode " + srcString);
        final Bitmap result;

        if (srcString.contains("MP4")) {
          result = drawableToBitmap(mResources.getDrawable(R.drawable.ic_not_started_black, null));
          Uri myUri = Uri.fromFile(new File(srcString));
          MediaPlayer mediaPlayer = new MediaPlayer();
  /*mediaPlayer.setAudioAttributes(
      AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build()
  );*/
          mediaPlayer.setDataSource(mContext, myUri);
          mediaPlayer.prepare();
          mediaPlayer.start();

          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
          }
        } else {
          result = decodeSampledBitmapFromFilepath(srcString, screenWidth, screenHeight);
        }

        Runnable r = new Runnable() {
          public void run() {
            long timer = currentTimeMillis() - startTime;
            long delay = maxDelay - timer;
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                  Thread.currentThread().interrupt();

                }
              }
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

              }

            Log.d(CLASSNAME,
                " took " + timer + "ms for decode " + srcString + " , waited " + delay + "ms");
          }
        };
        executor.execute(r);

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