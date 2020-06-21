package org.tflsh.nosedive;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class asyncTaskManager  extends AppCompatActivity {
    Context mContext;
    private int currentfile;
    private int missingfilessize;

    public asyncTaskManager(Context ctx)
    {
        Log.d("asynctaskMNG","starting helper with context");
        mContext=ctx;
    int missingfilessize=0;

    int currentfile=0;
    }
    ArrayList<String> missingfiles = new ArrayList<String>();


///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //constructor: save the context for alter uses
        /** Called when the user taps the Send button */
        public static final String EXTRA_MESSAGE = "org.tflsh.nosedive.MESSAGE";
    public void sendMessage(String message) {
        Intent intent = new Intent(mContext, FullscreenActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        mContext.sendBroadcast(intent);
        Log.e("sendintent", "ok");

    }



    //grab from URL and save on sdcard the file DESTNAME
//est devenu un runnable
    private class grabImageThread implements Runnable {
        //declarations
        private static final String TAG = "grabImageTask";
        private String command;
        final String destifileImage;
        String urldisplay;


        //private final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/res/";
        private final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" + "julia/";


        //TODO check the mess with the files

        public grabImageThread(String destname) {
            missingfiles = new ArrayList<String>();

            this.destifileImage = destname;
            urldisplay = mServerDirectoryURL+destname;
        }

        protected boolean doInBackground() {
            //  mDLprogressBar.incrementSecondaryProgressBy(1);

//            mDLprogressBar.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
            // SystemClock.sleep(5);

            File file = new File(mContext.getExternalCacheDir() + "/" + destifileImage);
            Bitmap mIcon11 = null;
            try {
                // this.destifileImage.createNewFile();
                if (urldisplay.contains("json")) {
                    Log.e("MEGAWWARN", "on decompresse le json comme une image :/");
                }
                else
                {
                    Log.e("grabImageThread", "on decompresse "+urldisplay);

                }
                //InputStream is = new java.net.URL(urldisplay).openStream();
                BufferedInputStream bis = new BufferedInputStream(new java.net.URL(urldisplay).openStream());
                //

                BitmapFactory.Options options = new BitmapFactory.Options();
                /*options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
                options.outWidth=screenwidth;
                options.outHeight=screenheight;//screenheight;
*/
                mIcon11 = BitmapFactory.decodeStream(bis,null,options);
                bis.close();


                if (mIcon11 != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    //result.copyPixelsToBuffer((Buffer)buf);//result=//get //compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                    mIcon11.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bos.close();

                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(bitmapdata);
                    fos.close();
                    mIcon11.recycle();

//result.recycle();


                } else {
                    Log.e("error", "skiping one empty file:" + destifileImage);
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "pas internet pour recup le fichier " + destifileImage);
                //popup : pas internet
                //e.printStackTrace();

                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "pb pour ouvrir le fichier json ou sauver limage" + e.getMessage());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }



            currentfile++;
            /* !!!!if (currentfile == missingfiles.size()) {
                mDLisCompleted = true;
                mDiapoHandler.post(startdiapoRunnable);
                Log.d("GRABtaskPOST", "lastfile, on lance le diapo");
//!                mDLprogressText.setText("Téléchargement complet");
                mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

            }*/
            if (!file.exists()) {
                Log.e("error", "FAILED ! writen file " + file.getAbsolutePath());
                return false;
            } else {
                Log.d("GRABtaskPOST", "ok synchonizing " + currentfile + " of " + missingfilessize+" "+file.getAbsolutePath());
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
                return true;


            }


            // result.recycle();
            // this.destifileImage.write(result);
        }

        @Override
        public void run() {
            Log.d(TAG, "run" );
            if(this.doInBackground()) {
                sendMessage("completed");
/*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDLprogressBar.incrementSecondaryProgressBy(1);
                        mDLprogressText.setText("Téléchargement " + currentfile + "/" + missingfiles.size() + "réussi");
                        Toast.makeText(getApplicationContext(), "récup "+currentfile+", ok", Toast.LENGTH_SHORT).show();

                        mDLprogressBar.setProgress(currentfile);
                        mDLprogressBar.setSecondaryProgress(currentfile + 1);
                    }
                });
*/
            }
            else {
/*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
*/



            }
        }
    }
//thread asynchrone chargé de:
//récup la liste des fichiers sur https://server/projets/index.php
//l'enregistrer sur external_cache/filelist.json

    //l'ouvrir, le parser, returnez une liste de string corrrspondant aux noms des images présentes, une liste des images manquantes
//et lancer le téléchargement des images manquantes.
    public  class  ListImageTask extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {
        private static final String TAG = "ListImageTask";
        ArrayList<String> name;
        ArrayList<String> sums;
        ArrayList<String> missings;
        ExecutorService executor = Executors.newFixedThreadPool(1);

        public ListImageTask(ArrayList<String> missinfil, ArrayList<String> img) {
            this.missings = missinfil;
            this.name = img;
        }

        //return a array of string, naming the files downloaded, or found in the cache dir
        //@url: basestring to construct files url
        protected ArrayList<String> doInBackground(String... urls) {
            String urldisplay = urls[0] + "index.php";
            try {
                //si la list des fichiers n'existe pas, on la recupere et on l'enregistre
                File localjsonfile = new File(mContext.getExternalCacheDir() + "/" + "filelist.json");


                if (!localjsonfile.exists()) {
                    if (localjsonfile.createNewFile()) {
//ByteArrayOutputStream bos=new ByteArrayOutputStream();
                        byte[] bitmapdata = new byte[1024];
                        //  is.read(bitmapdata);

                        //  byte[] jsondata = new byte[0];
                        int read = 0;

                        Log.d(TAG, "downloading filelist.jsonn from " + urldisplay);


                        OutputStream fos = new FileOutputStream(localjsonfile);

                        InputStream is = new java.net.URL(urldisplay).openStream();

                        while ((read = is.read(bitmapdata)) != -1) {
                            fos.write(bitmapdata, 0, read);
                            Log.d(TAG, "downloading filelist.jsonn from loop");
                        }
                        Log.d(TAG, "downloading filelist.jsonn ok ");
                        SystemClock.sleep(1000);

                        fos.flush();
                        fos.close();
                    }           //  is.reset();
                    if (!localjsonfile.exists()) {
                        throw new IOException();
                    }
                }
//TODO file can be empty if internet failed
                Log.d(TAG, "opening" + localjsonfile.getAbsolutePath()+" of size "+localjsonfile.length());

                InputStream i2s = new FileInputStream(localjsonfile.getAbsolutePath());


                //une fois le fichier recupéré, on peut l'ouvrir

                JsonReader reader = new JsonReader(new InputStreamReader(i2s));

                //fis.close();
                if(localjsonfile.length()>0)
                {
                    //si le json fais plus de 0 octets, on peut le parser
                    reader.beginArray();

                    String description = "";
                    while (reader.hasNext()) {
                        boolean mssgfile = false;
                        reader.beginObject();

                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            // Log.e("key:", key);
                            if (key.equals("name")) {
                                String newIn = reader.nextString();
                                //TODO: should check if we already downloaded the files
                                //need context, really?

                                //File file;// = new File(getExternalFilesDir(null), newIn);
                                File file = new File(mContext.getExternalCacheDir() + "/" + newIn);
                                if (!file.exists()) {
                                    //le fichier listé dans le json n'est pas trouvé, on ajoute son nom dans "missing[]"
                                    Log.d("ListImageTask", "caching file " + newIn + " to " + mContext.getExternalCacheDir());
                                    this.name.add(newIn);
                                    //mDLisCompleted = false;


                                    executor.execute(new asyncTaskManager.grabImageThread(newIn));

                                    this.missings.add(newIn);
                                    missingfilessize++;

                                    // Log.e("error", "FAILED ! writen file " +testfile.getAbsolutePath() );
                                } else {
                                    //!log bcp Log.d("ListImageTask", "file " + newIn + " already found to " + getExternalCacheDir());
                                    this.name.add(newIn);
                                    //  mDLisCompleted = true;
                                    //Log.e("found_file:", newIn);
                                }
                            } else if (key.equals("size")) {

                                int filesize = reader.nextInt();
                                //    missingfilessize += filesize;
                                //  Log.d("ListImageTask", "added " + filesize + " Bytes to download for " + this.name.get(this.name.size() - 1));
                                // reader.skipValue();

                            } else if (key.equals("sum")) {
                              reader.nextString();
                            } else {
                                reader.skipValue();


                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                    return this.name;

                } else {
                    return null;
                }




            } catch (FileNotFoundException e) {
                Log.e(TAG, "fichier json local non trouvé");

                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(TAG, "mauvaise url");

                e.printStackTrace();
            }  catch (IOException e) {
                Log.e(TAG, "fichier json internet non trouvé ou malformé");
                //popup : pas internet
                e.printStackTrace();

                // e.printStackTrace();
            } catch (Exception e) {
                Log.e("UnexpectedError", e.getMessage());
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
        //backgroundtask end:
        //if result==null=>jsonfile is empty( no internet)

        protected void onPostExecute(ArrayList<String> result) {
            if(result!=null)
            {
                if (result.size() == 0) {
                    Toast.makeText(mContext, "impossible de récup index.php, veuillez  activer le WIFI et relancer l'appli", Toast.LENGTH_LONG).show();
                    //mNoInternet=true;

                    Log.e(TAG, "aucun resultat (impossible de creer le fichier json ou d'aller sur internet)");
                } else {
                    Log.d(TAG, "on a trouvé ce nombre d'image a afficher:" + result.size() + " " + missings.size());
                    if (true /*currentfile == missingfiles.size()*/) {
                        //   mDLisCompleted = true;

//!!                    mDiapoHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY);


                        //!!!                mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

                    }
                    else{//mDLisCompleted=false;
                    }
                    //       mDLprogressBar.setMax(missingfiles.size());
                    //     mNoInternet=true;

                    //    totalfiles = result.size();
                }

            }else { Log.e(TAG, "jsonfile VIDE!!!");
            }

        }}
    private boolean mTextBlink = false;

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ASYNCTASK////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    public class showImageFileTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;
        String name;

        public showImageFileTask(ImageView bbmImage) {
            this.bmImage = bbmImage;
            //   this.name=namee;
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                return BitmapFactory.decodeFile(mContext.getExternalCacheDir() + "/" + urls[0]);
            } catch (Exception e) {
                Log.e("fileeroor", "inshowimgtask");
                return null;

            }
        }

        protected void onPostExecute(Bitmap result) {

            //((TextView) findViewById(R.id.pressme_text)).setText("Diapo " + (pwa + 1) + "/" + mDiapo.size() + "réussi");
            //!log bcp  Log.d("showimageTask","Diapo " + (pwa ) + "/" + mDiapo.size() + "réussi");

            //((TextView)findViewById(R.id.pressme_text)).setText(getResources().getString(R.string.string_press_me)
//            mDiapoProgressBar.incrementProgressBy(1);
            bmImage.setImageBitmap(result);
            if (mTextBlink) {
                mTextBlink = false;
//                ((TextView) findViewById(R.id.pressme_text)).setTextColor(getResources().getColor(R.color.OurPink));
            } else {
                mTextBlink = true;
                //     ((TextView) findViewById(R.id.pressme_text)).setTextColor(getResources().getColor(R.color.colorAccent));
            }


            //SystemClock.sleep(1000);

        }
    }

    //grab from URL and save on sdcard the file DESTNAME
    private class grabImageTask extends AsyncTask<String, Void, File> {
        private static final String TAG = "grabImageTask";
        private final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" + "julia/";

        final String destifileImage;
        String urldisplay;

        //TODO check the mess with the files
        public grabImageTask(String destname) {

            this.destifileImage = destname;
            urldisplay = mServerDirectoryURL + destname;
        }

        protected File doInBackground(String... urls) {
            //mDLprogressBar.incrementSecondaryProgressBy(1);

            // mDLprogressBar.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
            // SystemClock.sleep(5);

            File file = new File(mContext.getExternalCacheDir() + "/" + destifileImage);
            Bitmap mIcon11 = null;
            try {
                // this.destifileImage.createNewFile();
                if (urldisplay.contains("json")) {
                    Log.e("MEGAWWARN", "on decompresse le json comme une image :/");
                }
                //InputStream is = new java.net.URL(urldisplay).openStream();
                BufferedInputStream bis = new BufferedInputStream(new java.net.URL(urldisplay).openStream());
                //

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
                //options.outWidth = screenwidth;
                //options.outHeight = screenheight;//screenheight;
                mIcon11 = BitmapFactory.decodeStream(bis, null, options);

                bis.close();


                if (mIcon11 != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    //result.copyPixelsToBuffer((Buffer)buf);//result=//get //compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                    mIcon11.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bos.close();

                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(bitmapdata);
                    fos.close();
                    mIcon11.recycle();

//result.recycle();


                } else {
                    Log.e("error", "skiping one empty file:" + destifileImage);
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "pas internet pour recup le fichier " + destifileImage);
                //popup : pas internet
                //e.printStackTrace();

                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "pb pour ouvrir le fichier json ou sauver limage" + e.getMessage());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return file;

        }

        protected void onPostExecute(File result) {

            if (!result.exists()) {
                Log.e("error", "FAILED ! writen file " + result.getAbsolutePath());
            } else {
                Log.d("GRABtaskPOST", "ok synchonizing " + missingfiles.size() + " " + result.getAbsolutePath());
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
              /*  mDLprogressText.setText("Téléchargement " + currentfile + "/" + missingfiles.size() + "réussi");


                mDLprogressBar.setMax(missingfiles.size());
                mDLprogressBar.incrementSecondaryProgressBy(1);

                mDLprogressBar.setProgress(currentfile);
                mDLprogressBar.setSecondaryProgress(currentfile + 1);*/


            }
        /*    currentfile++;
            if (currentfile == missingfiles.size()) {
                mDLisCompleted = true;
                mDiapoHandler.post(startdiapoRunnable);
                Log.d("GRABtaskPOST", "lastfile, on lance le diapo");
                mDLprogressText.setText("Téléchargement complet");
                mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

            }*/

            // result.recycle();
            // this.destifileImage.write(result);
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
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        FileInputStream fis = new FileInputStream(res);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Bitmap result = BitmapFactory.decodeStream(bis, null, options);
        fis.close();
        return result;


    }

    int screenwidth;
    int screenheight;
    public /*static*/ Bitmap olddecodeSampledBitmapFromFilepath(String res,
                                                                int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
//    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //              options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
