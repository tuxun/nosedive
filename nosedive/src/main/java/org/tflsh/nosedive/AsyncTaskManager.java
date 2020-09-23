package org.tflsh.nosedive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.net.CookieStore;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static java.lang.System.*;

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
        options.inSampleSize = calculateInSampleSize(options, (int) (reqWidth/1.3),
            (int) (reqHeight/1.3));
options.inPreferQualityOverSpeed=true;
        //finde deformation de visage je regarde
//        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(res, options),reqWidth,options.outHeight/2,true);
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
    static class ListImageTask {
        private static final String CLASSNAME = "ListImageTask";
       // static List<String> name;
        static String urlSource;
        static List<String> missingImagesNames;



        private static List<String>  everyImagesNames;

        private ListImageTask() {
            Log.d(CLASSNAME, "ListImageTask constructor");
        }

        /**
         * A function for check is file exists or is empty
         *
         * @param path path where the file @name should be checked
         * @param name name of the file to check
         * @return return true if file is looking fine, else return false
         */
        protected static boolean checkFile(String path, String name) {
            File toTest = new File(path, name);
            if (toTest.exists()) {
                return toTest.length() != 0;
            }
            return false;
        }

        protected static boolean checkSum(String path, String originSum) {
            int read;

            try (InputStream is = new FileInputStream(path)

            ) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] fb = new byte[8192];
                while ((read = is.read(fb)) != -1) {
                    md.update(fb, 0, read);
                }
                byte[] sum = md.digest();
                BigInteger bi = new BigInteger(1, sum);

                String computedSum = String.format("%32s", bi.toString(16));
                computedSum = computedSum.replace(' ', '0');

                if ((originSum.equals(computedSum))) {
                    //Log.d("fs_sum", "found one file ok");
                    return true;
                } else {
                    Log.e("fs_sum", "found one  broken file " + path);
                    //super warn, inversed bool for test, was false at start
                    return false;
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        //parse the json file, start the dl of missing files or of corrupted files
        protected static List<String> parseJson(File jsonFile, String downloadSrcUrl) {
            try (
                JsonReader reader = new JsonReader(
                    new InputStreamReader(new FileInputStream(jsonFile.getAbsolutePath())))

            ) {
                everyImagesNames=new ArrayList<String>();
                missingImagesNames=new ArrayList<String>();
                reader.beginArray();

                while (reader.hasNext()) {
                    reader.beginObject();
                 reader.nextName();
                    String newIn = reader.nextString();
                    everyImagesNames.add(newIn);

                    File file = new File(mCacheDir, newIn);
                    if (file.exists()) {
                        reader.nextName();
                        reader.nextString();
                        reader.nextName();

                        String sum = reader.nextString();

                        if (!checkSum(mCacheDir + "/" + everyImagesNames.get(everyImagesNames.size() - 1), sum)) {
                            Log.e(CLASSNAME, "on dl le file, il etait corrompu");

                            getFile(downloadSrcUrl, mCacheDir.getAbsolutePath(),
                                everyImagesNames.get(everyImagesNames.size() - 1));
                            missingImagesNames.add(everyImagesNames.get(everyImagesNames.size() - 1));
                            sendMessageWithString("filesMissing", newIn);

                        }
                        else {
                            sendMessageWithString("filesFound", newIn);
                        }
                    } else {
                        Log.e(CLASSNAME, "on dl le file");
                        missingImagesNames.add(everyImagesNames.get(everyImagesNames.size() - 1));
                        getFile(downloadSrcUrl, mCacheDir.getAbsolutePath(),
                            everyImagesNames.get(everyImagesNames.size() - 1));
                        reader.nextName();
                        reader.nextString();
                        reader.nextName();
                        reader.nextString();

                    }
                    reader.endObject();
                }
                return everyImagesNames;
            } catch (FileNotFoundException e) {
                Log.e(CLASSNAME, "local json file not found");

                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(CLASSNAME, "bad url");

                e.printStackTrace();
            } catch (IOException e) {
                Log.e(CLASSNAME, "Unable to download json file from internet");
                sendMessage(NO_JSON);
            } catch (Exception e) {
                Log.e(CLASSNAME, Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            } finally {
                Log.d(CLASSNAME, "ok synchronizing "
                    + currentFile
                    + " of "
                    +  missingImagesNames.size()
                  );
                if (currentFile == missingImagesNames.size()         ) {
                    Log.d(CLASSNAME, "last file, starting slideshow");
                    sendMessage("dlComplete");
                }
                Log.d(CLASSNAME,
                    "Finished all threads (WARING: not really, we just removed the test)");
            }
            return everyImagesNames;
        }

        /*return a array of string, naming the files downloaded, or found in the cache dir
         * @param: String url: base string to construct files url
         */
        protected static File getFile(String urlSourceString, String pathDest, String nameDest) {
            File localFile = new File(pathDest, nameDest);


            Log.d(CLASSNAME, "creating ..."
                + localFile.getAbsolutePath()
                + " from "
                + urlSourceString
                + nameDest);

            try (
                OutputStream fos = new FileOutputStream(localFile);
                InputStream is = new BufferedInputStream(
                    new URL(urlSourceString + nameDest).openStream())) {

                byte[] bitmapBytesData = new byte[1024];
                int read;
                Log.d(CLASSNAME, "downloading " + localFile.getPath() + "  from " + localFile);
                SystemClock.sleep(2000);
                while ((read = is.read(bitmapBytesData)) != -1) {
                    fos.write(bitmapBytesData, 0, read);
                }

                if (!localFile.exists()) {
                    Log.d(CLASSNAME, "unable to create " + localFile.getAbsolutePath());

                    throw new IOException();
                }
                if (!localFile.getName().contains("json")) {
                    sendMessage("dlReceived");

               /*
                Log.d(CLASSNAME, "ok synchronizing "
                    + currentFile
                    + " of "
                    +  missingImagesNames.size()
                    + " "
                    + localFile.getAbsolutePath());*/
                sendMessageWithString("filesFound",localFile.getName());
            }
                currentFile++;
                return localFile;
            } catch (
                FileNotFoundException e) {
                Log.e(CLASSNAME, "local  file not found");

                e.printStackTrace();
                return localFile;
            } catch (
                MalformedURLException e) {
                Log.e(CLASSNAME, "bad url");

                e.printStackTrace();
                return localFile;
            } catch (
                IOException e) {
                Log.e(CLASSNAME, "Unable to download json file from internet");
                sendMessage(NO_JSON);
                return localFile;
            }
        }

        public static void exec(String urlSourceArg) {


            urlSource = urlSourceArg;

            FutureTask<String>
                futureTask1 = new FutureTask<>(mRunnable,
                "FutureTask1 is complete");

            executor.submit(futureTask1);
            try {

                if (!futureTask1.isDone()) {

                    // wait indefinitely for future
                    // task to complete
                    Log.d(CLASSNAME, "FutureTask1 output = "
                        + futureTask1.get());
                }

                // Wait if necessary for the computation to complete,
                // and then retrieves its result
                String s = futureTask1.get(250, TimeUnit.MILLISECONDS);

                if (s != null) {
                    Log.d(CLASSNAME, "FutureTask2 output=" + s);
                }
            } catch (Exception e) {
                Log.d(CLASSNAME, "Exception: " + e);
            }
        }




        /**
         * @param objects
         * oldeprecated
         */
        protected static final Runnable mRunnable;

        public static final String FILELIST_JSON = "filelist.json";

        static {
            mRunnable = new Runnable() {
                @Override
                public void run() {

                    try {
                        File localJsonFile;
                        //if the images list don't exists, download and save it
                        if (ListImageTask.checkFile(mCacheDir.getAbsolutePath(), FILELIST_JSON)) {
                            localJsonFile = new File(mCacheDir.getAbsolutePath(), FILELIST_JSON);
                        } else {
                            localJsonFile =
                                ListImageTask.getFile(urlSource, mCacheDir.getAbsolutePath(),
                                    FILELIST_JSON);
                        }

                        Log.d(CLASSNAME, "opening"
                            + localJsonFile.getAbsolutePath()
                            + " of size "
                            + localJsonFile.length());

                        //if the images list file is not empty, we can parse its json content
                        List<String> result = parseJson(localJsonFile, urlSource);
                        if (result.isEmpty()) {

                            Log.e(CLASSNAME, "EMPTY json file!!!");
                            sendMessage(NO_JSON);
                            Log.e(CLASSNAME,
                                "no results: unable to get json from internet or to create files");
                        } else {
                            Log.d(CLASSNAME, "found this number of images :"
                                + everyImagesNames.size()
                                + " (missing:) "
                                + missingImagesNames.size());
                            //sendMessageWithString("filesMissing", localJsonFile.getAbsolutePath());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
        }}

    @Override protected void onStop(){
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