package org.tflsh.nosedive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class asyncTaskManager extends AppCompatActivity {
    /**
     * Called when the user taps the Send button
     */
    final Context mContext;
    private int currentFile;
    static private int missingFilesNumber;

    final int screenWidth ;
    final int screenHeight;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //constructor: save the context for later uses
    public asyncTaskManager(Context ctx, int width, int height) {
        Log.d("asyncTaskManager", "starting helper with context");
        if (ctx == null) {
            Log.e("asyncTaskManager", "ctx null in constructor");
        }
        mContext = ctx;

        screenWidth = width;
        screenHeight = height;


    }

    public void sendMessage(String message) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        mContext.sendBroadcast(intent);

    }

    public void sendMessageWithInt(String message, int params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        intent.putExtra("EXTRA_MESSAGE", params);
        mContext.sendBroadcast(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }


    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////grabImageThread////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //grab a file from URL and save it on sdcard under the file name "@destName"
    private class grabImageThread implements Runnable {
        //declarations
        private static final String TAG = "grabImageTask";
        final String destFileImageName;
        final String urlToGrab;

        //TODO check the mess with the files

        public grabImageThread(String destName, String baseDir) {

            this.destFileImageName = destName;
            //String mServerDirectoryURL = baseDir;
            urlToGrab = baseDir + destName;
        }

        protected boolean doInBackground() {

            sendMessage("dlStarted");

            File file = new File(mContext.getCacheDir() + "/" + destFileImageName);
            try {
                if (urlToGrab.contains("json")) {
                    Log.e(TAG, "warn! we are trying to open the json file like an image :/");
                } else {
                    Log.e(TAG, "getting " + urlToGrab);

                }



                    int read;
 InputStream is =  new BufferedInputStream(new java.net.URL(urlToGrab).openStream());
                FileOutputStream fos = new FileOutputStream(file);

                SystemClock.sleep(500);
                byte[] bitmapBytesData = new byte[1024];
                        while ((read = is.read(bitmapBytesData)) != -1) {
                            fos.write(bitmapBytesData, 0, read);
                        }
                is.close();
                fos.flush();
                fos.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "unable  " + destFileImageName);
                //popup : pas internet
                //e.printStackTrace();
                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "unable to create json file or image" + e.getMessage());
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
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
                return true;
            }
        }

        @Override
        public void run() {
            if(this.doInBackground()) {
                Log.d(TAG, "succeed" );

            } else {
                Log.d(TAG, "running" );




            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////@ListImageTask////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    //asynchronous thread  which should:
// -1 download file list on https://server/project/index.php
// -2 save it under external_cache/filelist.json
// -3 open it
// -4 parse it,
// -5 start the downloading of the missing images
// -6 return a list of string for images found (second arg of constructor)
// -7 and a list of the missing images names (third arg of constructor)
    public class ListImageTask extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {
        private static final String TAG = "ListImageTask";
        final ArrayList<String> name;
        final ArrayList<String> missingImagesNames;
        final ExecutorService executor = Executors.newFixedThreadPool(1);

public ListImageTask(ArrayList<String> missingFileArg, ArrayList<String> img) {
            this.missingImagesNames = missingFileArg;
            this.name = img;
        }


        //return a array of string, naming the files downloaded, or found in the cache dir
        //@string url: base string to construct files url
        protected ArrayList<String> doInBackground(String... urls) {
            String urlSourceString = urls[0] + "index.php";
            try {
                //if the images list don't exists, download and save it
                File localJsonFile = new File(mContext.getCacheDir() + "/" + "filelist.json");


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
                        }

                        fos.flush();
                        fos.close();
                    }

                    if (!localJsonFile.exists()) {
                        throw new IOException();
                    }
                }
//TODO file can be empty if internet failed
                Log.d(TAG, "opening" + localJsonFile.getAbsolutePath() + " of size " + localJsonFile.length());

                InputStream i2s = new FileInputStream(localJsonFile.getAbsolutePath());

                JsonReader reader = new JsonReader(new InputStreamReader(i2s));
                boolean sumToCheck = false;

                //if the images list file is not empty, we can parse its json content
                if (localJsonFile.length() > 0) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();

                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            // Log.e("key:", key);
                            switch (key) {
                                case "name":
                                    String newIn = reader.nextString();

                                    File file = new File(mContext.getCacheDir() + "/" + newIn);
                                    if (!file.exists()) {
                                        //filename found in json file was not found in the cache directory
                                        Log.d("ListImageTask", "caching file " + newIn + " to " + mContext.getCacheDir());
                                        this.name.add(newIn);
                                        executor.execute(new grabImageThread(newIn,urls[0]));
                                        this.missingImagesNames.add(newIn);
                                        missingFilesNumber++;
                                    } else {
                                        sumToCheck = true;

                                        this.name.add(newIn);
                                    }
                                    break;
                                case "size":

                                    /*optional feature, TODO*/
                                    //size of file on the server, used to check file corruption
                                    reader.nextInt();

                                    break;

                                case "sum":
                                    if (sumToCheck) {
                                        int read;
                                        MessageDigest md = MessageDigest.getInstance("MD5");
                                        InputStream is = new FileInputStream(mContext.getCacheDir() + "/" + this.name.get(this.name.size() - 1));
                                        byte[] fb = new byte[8192];
                                        while ((read = is.read(fb)) != -1) {
                                            md.update(fb, 0, read);
                                        }
                                        byte[] sum = md.digest();
                                        is.close();
                                        BigInteger bi = new BigInteger(1, sum);

                                        String computedSum = String.format("%32s", bi.toString(16));
                                        computedSum = computedSum.replace(' ', '0');
                                        String originSum = reader.nextString();


                                        if ((originSum.equals(computedSum))) {
                                            Log.d("fs_sum", "found one file ok");
                                        } else {
                                            Log.d("fs_sum", "found one  broken file");

                                            executor.execute(new grabImageThread(this.name.get(this.name.size() - 1), urls[0]));
                                            this.missingImagesNames.add(this.name.get(this.name.size() - 1));
                                        }
                                        sumToCheck = false;
                                    } else {
                                        reader.nextString();

                                    }
                                    break;
                                default:
                                    reader.skipValue();


                                    break;
                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                    return this.name;

                } else {
                    if(localJsonFile.delete())
                    {
                        Log.d(TAG, "local json file deleted because it was empty");

                    }
                    else {
                        Log.e(TAG, "failed to delete empty local json");
                    }
                    return null;
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
return this.name;
        }


        protected void onPostExecute(ArrayList<String> result) {
            if(result!=null)
            {
                if (result.size() == 0) {

                    Log.e(TAG, "no results: unable to get json from internet or to create files");
                } else {
                    Log.d(TAG, "found this number of images :" + this.name.size() + " (missing:) " + missingImagesNames.size());
                    sendMessageWithInt("filesFound", missingImagesNames.size());

                }

            }
            else {
                Log.e(TAG, "EMPTY json file!!!");
                sendMessage("noJson");
            }
            }

        }



    public void sendMessageWithString(String message, String params) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());


        intent.putExtra("EXTRA_MESSAGE", params);
        mContext.sendBroadcast(intent);

    }


    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ASYNCTASK////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public class showImageFileTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;
        long startTime;
        public showImageFileTask(ImageView bbmImage) {
            this.bmImage = bbmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                //
                startTime = System.currentTimeMillis();

                return decodeSampledBitmapFromFilepath(mContext.getCacheDir() + "/" + urls[0], screenWidth, screenHeight);


            } catch (Exception e) {
                Log.e("showImageFileTask", "Exception in decodeSampledBitmapFromFilepath");
                return null;

            }
        }

        protected void onPostExecute(Bitmap result) {

            long timer=System.currentTimeMillis()- startTime;
            long delay=750-timer;

            if(delay>0) {

                SystemClock.sleep(delay);
            }
            bmImage.setImageBitmap(result);

            sendMessageWithString("imgShown", mContext.getCacheDir() + "/" + result);


        }
    }

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

    public Bitmap decodeSampledBitmapFromFilepath(String res,
                                                  int reqWidth,
                                                  int reqHeight) throws IOException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        FileInputStream fis = new FileInputStream(res);
        Bitmap result =BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);

        fis.close();
        return result;


    }


}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
