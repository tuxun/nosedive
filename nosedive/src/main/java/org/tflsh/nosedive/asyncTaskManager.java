package org.tflsh.nosedive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

public class asyncTaskManager extends AppCompatActivity {
    /**
     * wrapper around the async classes we need in FullscreenActivity
     */
    public static final String EXTRA_MESSAGE = "org.tflsh.nosedive.SEND";
    Context mContext;
    ArrayList<String> missingFilesNames = new ArrayList<>();
    int screenWidth = 800;
    int screenHeight = 600;
    private int currentFile;
    private int missingFilesNumber;
    private boolean mTextBlink = false;
    //constructor: save the context for alter uses
    public asyncTaskManager(Context ctx, int width, int height) {
        if (ctx == null) {
            Log.e("asyncTaskManager", "ctx null in constructor");
        } else {
            this.mContext = ctx;

            Log.d("asyncTaskManager", "starting helper with context");

        }

        screenWidth = width;
        screenHeight = height;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.e("asyncTaskManager", "onCreate");

    }

    ////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////tools functions////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    public /*static*/ int calculateInSampleSize(
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



    public Bitmap lastdecodeSampledBitmapFromFilepath(String res,
                                                  int reqWidth,
                                                  int reqHeight) throws IOException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        FileInputStream fis = new FileInputStream(res);
        //BufferedInputStream bis = new BufferedInputStream(fis);
        //
        //Bitmap result = BitmapFactory.decodeStream(bis, null, options);
        Bitmap result = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);

        fis.close();
        return result;


    }


    public /*static*/ Bitmap oldDecodeSampledBitmapFromFilepath(String res,
                                                                int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

    public void sendMessage(String message) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        // intent.putExtra("EXTRA_MESSAGE", message);
        mContext.sendBroadcast(intent);
         Log.e("sendMessage", "ok" + intent);

    }

    public void sendMessageWithInt(String message, int params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        intent.putExtra("EXTRA_MESSAGE", params);
        mContext.sendBroadcast(intent);
         Log.e("sendMessage", "ok" + intent);

    }


    public void sendMessageWithString(String message, String params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        intent.putExtra("EXTRA_MESSAGE", params);
        mContext.sendBroadcast(intent);

    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////grabImageThread////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //grab a file from URL and save it on sdcard under the file name "@destName"
    protected class grabImageThread implements Runnable {
        //declarations
        private static final String TAG = "grabImageThread";
        final String destFileImageName;
        final String destPath;
        final String urlToGrab;


        //TODO check the mess with the files

        public grabImageThread(String sourceUrl, String destPathArg, String destName) {
            missingFilesNames = new ArrayList<>();
            this.destPath = destPathArg;

            this.destFileImageName = destName;
            urlToGrab = sourceUrl + destName;
        }

        protected boolean doInBackground() {

            sendMessage("dlStarted");

            File file = new File(mContext.getExternalCacheDir() + "/" + destFileImageName);
            Bitmap mIcon11;
            try {
                if (urlToGrab.contains("json")) {
                    Log.e(TAG, "warn! we are trying to open the json file like an image :/");
                } else {
                    Log.e(TAG, "getting " + urlToGrab);

                }
                BufferedInputStream bis = new BufferedInputStream(new java.net.URL(urlToGrab).openStream());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);
                options.outWidth = screenWidth;
                options.outHeight = screenHeight;
                mIcon11 = BitmapFactory.decodeStream(bis, null, options);
                bis.close();


                if (mIcon11 != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    //result.copyPixelsToBuffer((Buffer)buf);//result=//get //compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                    mIcon11.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
                    byte[] bitmapByteData = bos.toByteArray();
                    bos.close();

                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(bitmapByteData);
                    fos.close();
                    mIcon11.recycle();

//result.recycle();


                } else {
                    Log.e(TAG, "skipping one empty file:" + destFileImageName);
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "unable  " + destFileImageName);
            } catch (IOException e) {
                Log.e(TAG, "unable to create json file or image" + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            currentFile++;
            if (!file.exists()) {
                Log.e("error", "FAILED ! written file " + file.getAbsolutePath());
                return false;
            } else {
                sendMessage("dlReceived");

                Log.d(TAG, "ok synchronizing " + currentFile + " of " + missingFilesNumber + " " + file.getAbsolutePath());
                if (currentFile == missingFilesNumber) {

                    Log.d(TAG, "last file, starting slideshow");

                    sendMessage("dlComplete");
                }
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
                return true;


            }


        }

        @Override
        public void run() {
            Log.d(TAG, "run");
            if (this.doInBackground()) {

            } else {


            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ListImageTask////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    //asynchronous thread  which should:
// -1 download file list on https://server/project/index.php
// -2 save it under external_cache/filelist.json
// -3 open it
// -4 parse it,
// -5 start the downloading of the missing images
// -6 return a list of string for images found (second arg of constructor)
// -7 and a list of the missing images names (third arg of constructor)
    public class listImageTask implements Runnable {
        private static final String TAG = "ListImageTask";
        final ArrayList<String> name;
        final ArrayList<String> missingImagesNames;
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        ArrayList<String> sums;
        String mServerDirectoryURL;
        String destPath;

        public listImageTask(String destPathArg,String sourceUrl, ArrayList<String> missingFileArg, ArrayList<String> img) {
            this.missingImagesNames = missingFileArg;
            this.name = img;
            this.mServerDirectoryURL = sourceUrl;
            this.destPath=destPathArg;

        }

        //return a array of string, naming the files downloaded, or found in the cache dir
        //@string url: base string to construct files url
        protected boolean doInBackground() {
            String urlSourceString = mServerDirectoryURL + "index.php";
            try {


                //if the images list don't exists, download and save it
                File localJsonFile = new File(destPath + "/" + "filelist.json");
                if (!localJsonFile.exists()) {
                    if (localJsonFile.createNewFile()) {
                        byte[] bitmapBytesData = new byte[1024];
                        int read;
                        Log.d(TAG, "downloading " + localJsonFile.getPath() + "  from " + urlSourceString);
                        OutputStream fos = new FileOutputStream(localJsonFile);
                        InputStream is = new java.net.URL(urlSourceString).openStream();

                        SystemClock.sleep(2000);

                        while ((read = is.read(bitmapBytesData)) != -1) {
                            fos.write(bitmapBytesData, 0, read);
                            Log.d(TAG, "downloading filelist.json from loop");
                        }
                        Log.d(TAG, "downloading filelist.json ok ");

                        fos.flush();
                        fos.close();
                    }           //  is.reset();
                    if (!localJsonFile.exists()) {
                        throw new IOException();
                    }
                }
                Log.d(TAG, "opening" + localJsonFile.getAbsolutePath() + " of size " + localJsonFile.length());

                //if the images list file is not empty, we can parse its json content
                if (localJsonFile.length() > 0) {
                    InputStream i2s = new FileInputStream(localJsonFile.getAbsolutePath());
                    JsonReader reader = new JsonReader(new InputStreamReader(i2s));

                    reader.beginArray();

                    String description = "";
                    while (reader.hasNext()) {
                        reader.beginObject();

                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            // Log.e("key:", key);
                            switch (key) {
                                case "name":
                                    String newIn = reader.nextString();
                                    //TODO: should check if we already downloaded the files
                                    //need context, really?

                                    //File file;// = new File(getExternalFilesDir(null), newIn);
                                    File file = new File(destPath + "/" + newIn);
                                    if (!file.exists()) {
                                        //filename found in json file was not found in the cache directory
                                        Log.d("ListImageTask", "caching file " + newIn + " to " + destPath);
                                        this.name.add(newIn);
                                        executor.execute(new grabImageThread(mServerDirectoryURL,destPath, newIn));
                                        this.missingImagesNames.add(newIn);
                                        missingFilesNumber++;

                                    } else {
                                        //!log bcp
                                        // Log.d("ListImageTask", "file " + newIn + " already found to " + getExternalCacheDir());
                                        this.name.add(newIn);
                                        //  mDLisCompleted = true;
                                        //Log.e("found_file:", newIn);
                                    }
                                    break;
                                case "size":

                                    /*optional feature, TODO*/
                                    //size of file on the server, used to check file corruption
                                    reader.nextInt();

                                    break;
                                case "sum":
                                    /*optional feature, TODO*/
                                    //sum of file on the server, used to check file corruption
                                    reader.nextString();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();

                } else {
                    localJsonFile.delete();
this.name.clear();
                }


            } catch (FileNotFoundException e) {
                Log.e(TAG, "local json file not found");

                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(TAG, "bad url");

                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Unable to download json file from internet");
                sendMessage("noJson");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Finished all threads");
            }


            if (this.name != null) {
                if (this.name.size() == 0) {
                    Log.e(TAG, "no results: unable to get json from internet or to create files");
                } else {
                    Log.d(TAG, "found this number of images :" + this.name.size() + " (missing:) " + missingImagesNames.size());
                    sendMessageWithInt("filesFound", missingImagesNames.size());
                                    return true;

                }

            } else {
                Log.e(TAG, "EMPTY json file!!!");
                sendMessage("noJson");
            }

            return false;
        }

           @Override
        public void run() {
            Log.d(TAG, "run");
            if (this.doInBackground()) {

            } else {


            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////showImageFileTask////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public class showImageFileTask implements Runnable {
        private static final String TAG = "showImageFileTask";
        final ImageView bmImage;
        String url;
        long startThreadTime;

        public showImageFileTask(ImageView bbmImage,String urlArg) {
            this.bmImage = bbmImage;
            this.url=urlArg;
        }

        protected boolean doInBackground() {
            try {
                //
                startThreadTime = System.currentTimeMillis();

                // return BitmapFactory.decodeFile(mContext.getExternalCacheDir() + "/" + urls[0]);
                // decodeSampledBitmapFromFilepath
                //return decodeSampledBitmapFromFilepath(mContext.getExternalCacheDir() + "/" + urls[0],screenWidth,screenHeight);
                //byte[] imgBits = decodeSampledBitmapFromFilepath(mContext.getExternalCacheDir() + "/" + url, screenWidth, screenHeight);

                long timer = System.currentTimeMillis() - startThreadTime;
                long delay = 750 - timer;

                if (delay > 0) {
                    long time1 = delay + timer;
                 //   SystemClock.sleep(delay);

                    sendMessageWithString("imgShown",mContext.getExternalCacheDir() + "/" + url);

                } else {
                    sendMessageWithString("imgShown",mContext.getExternalCacheDir() + "/" + url);
                }
                Log.d(TAG, "Slideshow succeed in  " + timer + "ms");
                mTextBlink = !mTextBlink;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
       @Override
        public void run() {
            Log.d(TAG, "run");
            if (this.doInBackground()) {

            } else {


            }
        }
    }

}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
