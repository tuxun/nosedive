package org.tflsh.nosedive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public class AsyncTaskManager extends Activity {
    static final String CLASSNAME = "AsyncTaskManager";
    private final LruCache<String, Bitmap> memoryCache;

    public static final String NO_JSON = "noJson";
    static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    static ExecutorService executor;// = Executors.newFixedThreadPool(1);

    /**
     * Called when the user taps the Send button
     */
    static Context mContext;
    final int screenWidth;
    final int screenHeight;
    private static File mCacheDir;
    private static int currentFile;
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int missingFilesNumber;

    //constructor: save the context for later uses
    public AsyncTaskManager(Context ctx, int width, int height,LruCache<String, Bitmap> memoryCacheArg,ExecutorService executorArg) {
        executor=executorArg;
        Log.d("asyncTaskManager", "starting helper with context");
        this.memoryCache=memoryCacheArg;
        mContext = ctx;
        mCacheDir = ctx.getCacheDir();
        //tuxun: try lru cache for large bitmap
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.

        screenWidth = width;
        screenHeight = height;
    }

    public static void sendMessage(String message) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }

    public static void sendMessageWithInt(String message, int params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());

        intent.putExtra(EXTRA_MESSAGE, params);
        mContext.sendBroadcast(intent);
    }

    public static void sendMessageWithString(String message, String params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());

        intent.putExtra(EXTRA_MESSAGE, params);
        mContext.sendBroadcast(intent);
    }
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////@ListImageTask////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ASYNCTASK////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    // https://developer.android.com/topic/performance/graphics/load-bitmap
    public Bitmap decodeSampledBitmapFromFilepath(String res,
        int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);
        options.inJustDecodeBounds = false;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, (int) (reqWidth / 1.5),
            (int) (reqHeight / 1.5));
        options.inSampleSize = calculateInSampleSize(options, (int) (reqWidth / 2),
            (int) (reqHeight / 2));
        options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(res, options);
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
        Log.d(CLASSNAME,"sample size from h: "+reqHeight+" w: "+reqWidth+ "==>"+inSampleSize);

        return inSampleSize;
    }

    //return the bitmap from cache or from file. idea from https://developer.android.com/topic/performance/graphics/cache-bitmap
    public Bitmap getOrAddBitmapToMemoryCache(String filePath) {
        synchronized (memoryCache) {
            Bitmap cachedResult = memoryCache.get(filePath);

            if (cachedResult == null) {
                cachedResult = decodeSampledBitmapFromFilepath(filePath, screenWidth, screenHeight);
if(memoryCache.evictionCount()==0)
{                memoryCache.put(filePath, cachedResult);

    Log.d("LRU:",
                    "[PUSH] "
                        + filePath
                        + cachedResult.getByteCount() / 1024 / 1024
                        + "Mo ["
                        + memoryCache.hitCount()
                        +
                        "/"
                        + memoryCache.missCount()
                        + "]"
                        + memoryCache.size() / 1024
                        + "Mo eviction= "
                        + memoryCache.evictionCount()
                );}
else{
Log.e(CLASSNAME,"cache is full, loading image from disk");}

            } else {
                Log.d("LRU:",
                    "[GET] "
                        + filePath
                        + cachedResult.getByteCount() / 1024 / 1024
                        + "Mo ["
                        + memoryCache.hitCount()
                        +
                        "/"
                        + memoryCache.missCount()
                        + "]"
                        + memoryCache.size() / 1024
                        + "Mo eviction= "
                        + memoryCache.evictionCount()
                );
            }

            return cachedResult;
        }
    }

    //asynchronous thread  which should:
    // -1 download file list on https://server/project/index.php
    // -2 save it under external_cache/filelist.json
    // -3 open it
    // -4 parse it,
    // -5 start the downloading of the missing images
    // -6 return a list of string for images found (second arg of constructor)
    // -7 and a list of the missing images names (third arg of constructor)

    /**
     * scope: package-private
     */

    @Override protected void onStop() {
        super.onStop();
        executor.shutdown();
        while (!executor.isTerminated()) {
            executor.shutdown();

            SystemClock.sleep(30);
            Log.d(CLASSNAME, "have a struggling task");
        }
    }

    public class ShowImageTask implements Runnable {
        static final String CLASSNAME = "showImageFileTask";
        final WeakReference<ImageView> bmImage;
        final int maxDelay;
        final String srcString;
        final long startTime;

        public ShowImageTask(ImageView bbmImage,
            int maxDelayParam, String urlSource) {
            this.bmImage = new WeakReference<>(bbmImage);
            this.maxDelay = maxDelayParam;
            this.srcString = urlSource;
            startTime = currentTimeMillis();
        }

        protected boolean doInBackground() {
            try {
                //

                final Bitmap result = getOrAddBitmapToMemoryCache(srcString);

                Runnable r = new Runnable() {
                    public void run() {
                        long timer = currentTimeMillis() - startTime;
                        long delay = maxDelay - timer;
                        if(delay>0)
                        {
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(bmImage!=null) {
                            bmImage.get().setAdjustViewBounds(true);
                            bmImage.get().setImageBitmap(result);

                            if (screenHeight > screenWidth) {
                                bmImage.get().setScaleType(ImageView.ScaleType.FIT_START);
                            } else {
                                bmImage.get().setScaleType(ImageView.ScaleType.FIT_CENTER);

                            }
                        }
                        Log.d(CLASSNAME, " took " + timer + "ms , waited " + delay + "ms");
                    }
                };
                runOnUiThread(r);
                return true;
            } catch (Exception e) {
                Log.e(CLASSNAME, "Exception in showImageFileTask.getOrAddBitmapToMemoryCache()");
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void run() {
            if (this.doInBackground()) {
                Log.d(CLASSNAME, "succeed");
            } else {
                Log.d(CLASSNAME, "running");
            }
        }
    }
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////