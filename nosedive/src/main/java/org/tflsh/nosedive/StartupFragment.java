package org.tflsh.nosedive;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.tflsh.nosedive.BackgroundImageDecoder.executor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartupFragment extends Fragment {
    static final String M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "julia/";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "StartupFragment" ;

    // TODO: Rename and change types of parameters
//    private String M_SERVER_DIRECTORY_URL;
 //   private ArrayList<String> missingImagesNames;
    private File mCacheDirPath;
    private ViewGroup mParentViewGroup;
    private View mParentView;
    public void setBaseUrl(String url)
    {
        //M_SERVER_DIRECTORY_URL=new String(url);

        new Thread(new Runnable() {
            @Override public void run() {
                //                grabJson(M_SERVER_DIRECTORY_URL);
                Log.d(TAG, "M_SERVER_DIRECTORY_URL"+M_SERVER_DIRECTORY_URL);


                new Thread(new Runnable() {
                    @Override public void run() {
                        //                grabJson(M_SERVER_DIRECTORY_URL);
                        Log.d(TAG, "M_SERVER_DIRECTORY_URL"+M_SERVER_DIRECTORY_URL);

                        grabJson(M_SERVER_DIRECTORY_URL,false);
                    }
                }).start();            }
        }).start();
    }

    public StartupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartupFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*
    public static StartupFragment newInstance(String param1, ArrayList<String> param2) {
        StartupFragment fragment = new StartupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putStringArrayList(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
*/
     ArrayList<String> everyImagesNames ;
     ArrayList<String>missingImagesNames ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
         //   M_SERVER_DIRECTORY_URL = getArguments().getString(ARG_PARAM1);
           // missingImagesNames = getArguments().getStringArrayList(ARG_PARAM2);
        }

    }
    private static Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
       // mContext=container.getContext();

        mCacheDirPath=mContext.getCacheDir();
        executor = Executors.newFixedThreadPool(1);
        Log.d("startupfragment","onCreateView start grabjson with "+M_SERVER_DIRECTORY_URL);



//Log.d("startupfragment"," added to "+container.toString());
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_startup, null, false);
    }

    private static final String CLASSNAME = "startupfragment";
    // static List<String> name;
    //static String urlSource;
    //static List<String> missingImagesNames;

   // private static List<String> everyImagesNames;
    private static int currentFile;

    // private ListImageTask() {
    // Log.d(CLASSNAME, "ListImageTask constructor");
    //}

    {
        mGrabJsonRunnable = new Runnable() {
            @Override
            public void run() {

                try {
                    File localJsonFile = new File(mCacheDirPath.getAbsolutePath(), FILELIST_JSON);

                    if (!localJsonFile.exists()) {

                        Log.d(CLASSNAME, "unable to create json");
                        grabJson(M_SERVER_DIRECTORY_URL,true);
                        return;
                    }

                    Log.d(CLASSNAME, "opening"
                        + localJsonFile.getAbsolutePath()
                        + " of size "
                        + localJsonFile.length());

                    //if the images list file is not empty, we can parse its json content
                    List<String> result = parseJson(localJsonFile, M_SERVER_DIRECTORY_URL+FILELIST_JSON);
                    if (result.isEmpty()) {

                        Log.e(CLASSNAME, "EMPTY json file!!!");
                        sendMessage(NO_JSON);
                        Log.e(CLASSNAME,
                            "no results: unable to get json from internet or to create files");
                    } else {
                        Log.d(CLASSNAME, "found this total number of images :"
                            + result.size()
                           /* + " (missing:) "
                            + missingImagesNames.size()*/);
                        //sendMessageWithString("filesMissing", localJsonFile.getAbsolutePath());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use maximum available memory for this memory cache.
        Log.d(TAG, " onViewCreated()");

        view.findViewById(R.id.settingsImageButton).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();

                    loadSettingFragment();
                }
                return true;
            }
        });
        /*repair files button*/
        view.findViewById(R.id.repairFilesButton).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.findViewById(R.id.repairFilesButton).setBackgroundColor(
                        getResources().getColor(R.color.OurWhite, null));
                    view.performClick();
/*                    (view.findViewById(R.id.checkFilesButton)).setBackgroundColor(
                        getResources().getColor(R.color.OurWhite, null));
*/
                    new Thread(new Runnable() {
                        @Override public void run() {

                            repairfiles(
                                M_SERVER_DIRECTORY_URL, missingImagesNames);
                        }
                    }).start();
                }
                return true;
            }
        });



        everyImagesNames = new ArrayList<String>();
        missingImagesNames = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override public void run() {

                grabJson(  M_SERVER_DIRECTORY_URL,false );
                exec(M_SERVER_DIRECTORY_URL);
            }
        }).start();
      //  sendMessage("StartupViewOk");

    }

    private void loadSettingFragment() {

        Log.d(TAG, "loadSettingFragment()");

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();


        //transaction1.remove(this).commit();
SettingsFragment FS=new SettingsFragment();
        Bundle args = new Bundle();
args.putString("SlideshowFilenames", "plop");
        FS.setArguments(args);
        //mParentView.findViewById(R.id.motherLayout).setVisibility(View.GONE);
transaction.add(R.id.setupScreenLinearSourceLayout, FS , "MULTIFACETTE_Settings");
        transaction.addToBackStack(null);
        transaction.commit();

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
            Log.e("checkFile", "found file " + toTest.getAbsolutePath() + toTest.length());
            if (toTest.length() == 0) {
                toTest.delete();
                return false;
            }
            return true;
        }
        Log.e("checkFile", "unable to find file " + toTest.getAbsolutePath());

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

    private boolean isInternetOk() {
        //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
        ConnectivityManager cm =
            (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.e("checkFile", "SDK>R");

            Network activeNetwork = cm.getActiveNetwork();
            return Objects.requireNonNull(cm.getNetworkCapabilities(activeNetwork))
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            Log.e("checkFile", "SDK<R");

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                activeNetwork.isConnected();
        }
    }

    /**
     * A function for check is file exists or is empty
     *
     * @param path path where the file @name should be checked
     * @param name name of the file to check
     * @return return true if file is looking fine, else return false
     */
    public void repairfiles(String urlSource, ArrayList<String> missingsArg) {
        Log.e("repairfiles", "missing or broken " + missingsArg.size() + " files");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   exec( urlSource);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String name : missingsArg) {
            Log.e("repairfiles", "grab missing or broken " + name + " files");

            getFile(urlSource, mCacheDirPath.getAbsolutePath(), name);
        }

        sendMessage("dlComplete");
    }

    //parse the json file, start the dl of missing files or of corrupted files
    protected List<String> parseJson(File jsonFile, String downloadSrcUrl) {
        try (
            JsonReader reader = new JsonReader(
                new InputStreamReader(new FileInputStream(jsonFile.getAbsolutePath())))

        ) {

            reader.beginArray();

            while (reader.hasNext()) {
                reader.beginObject();
                reader.nextName();
                String newIn = reader.nextString();
                everyImagesNames.add(newIn);

                File file = new File(mCacheDirPath, newIn);
                if (file.exists()) {
                    reader.nextName();
                    reader.nextString();
                    reader.nextName();

                    String sum = reader.nextString();

                    if (!checkSum(mCacheDirPath + "/" + everyImagesNames.get(everyImagesNames.size() - 1),
                        sum)) {
                        Log.e(CLASSNAME, "on dl le file, il etait corrompu");

                        //todo in another fonction:
                        //
                            /*
                            getFile(downloadSrcUrl, mCacheDir.getAbsolutePath(),

                                everyImagesNames.get(everyImagesNames.size() - 1));    */
                        missingImagesNames.add(
                            everyImagesNames.get(everyImagesNames.size() - 1));
                        sendMessageWithString("filesMissing", newIn);
                    } else {
                        sendMessageWithString("filesFound", newIn);
                    }
                } else {
                    Log.e(CLASSNAME, "on dl le file");
                    missingImagesNames.add(everyImagesNames.get(everyImagesNames.size() - 1));
                        /*todo in another function
                           getFile(downloadSrcUrl, mCacheDir.getAbsolutePath(),
                            everyImagesNames.get(everyImagesNames.size() - 1));*/
                    sendMessageWithString("filesMissing", newIn);

                    reader.nextName();
                    reader.nextString();
                    reader.nextName();
                    reader.nextString();
                }
                reader.endObject();
            }
            Log.d(CLASSNAME, "ok synchronizing "
                    + currentFile
               /* + " of "
                + missingImagesNames.size()*/
            );
            if ((!everyImagesNames.isEmpty()) /*&& (missingImagesNames.isEmpty())*/) {
                Log.d(CLASSNAME, "last file, starting slideshow");
                sendMessage("filesAllOk");
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
            e.printStackTrace();

            // sendMessage(NO_JSON);
        } catch (Exception e) {
            Log.e(CLASSNAME, Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        } finally {

            Log.d(CLASSNAME,
                "Finished all threads (WARING: not really, we just removed the test)");
        }
        return Collections.emptyList();
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

               /*
                Log.d(CLASSNAME, "ok synchronizing "
                    + currentFile
                    + " of "
                    +  missingImagesNames.size()
                    + " "
                    + localFile.getAbsolutePath());*/
                sendMessageWithString("dlReceived", localFile.getName());
                currentFile++;
            } else {
                // sendMessage("JSONok");

            }
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
        } catch (ConnectException e) {
            Log.e(CLASSNAME,
                "Unable to download json file from internet (we think we have internet but we dont");
            e.printStackTrace();
            return null;
        } catch (
            IOException e) {
            Log.e(CLASSNAME, "Unable to download json file from internet");
            e.printStackTrace();

            //sendMessage(NO_JSON);
            return localFile;
        }
    }
    //should never occur

/*
            //if the images list don't exists, download and save it
            if (ListImageTask.checkFile(mCacheDir.getAbsolutePath(), FILELIST_JSON)) {
                Log.d(CLASSNAME, "grabJson got local file,update skipped");
                sendMessage("JSONlocalonly");

                return new File(mCacheDir.getAbsolutePath(), FILELIST_JSON);

            } else {

                if (!isInternetOk())
                {
                    Log.d(CLASSNAME, "grabJson did not find local file+no internet");


                sendMessage("noJson");

                return null;
            }
                else {
                return
                    ListImageTask.getFile(urlSource, mCacheDir.getAbsolutePath(),
                        FILELIST_JSON);
            }}

        }
*/

    public  void exec(String urlSourceArg) {

      //  urlSource = urlSourceArg;

        FutureTask<String>
            futureTask1 = new FutureTask<>(mGrabJsonRunnable,
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
    protected  final Runnable mGrabJsonRunnable;

    public static final String FILELIST_JSON = "filelist.json";

    public  File grabJson(String urlSource,boolean forced) {
        Log.d("startupfragment","start grabjson with "+M_SERVER_DIRECTORY_URL+urlSource);

        Log.d(CLASSNAME, "grabJson update forced");
        if (!forced) {
            if (!checkFile(mCacheDirPath.getAbsolutePath(), FILELIST_JSON)) {
                Log.d(CLASSNAME,
                    "grabJson update forced was canceled and wed had no local json");
                sendMessage("noJson");
                return null;
            } else {
                sendMessage("JSONlocalonly");
                return new File(mCacheDirPath.getAbsolutePath(), FILELIST_JSON);
            }
        } else {
            Log.d(CLASSNAME, "grabJson update forced think we have internet,urlsource="+urlSource);

            File file = getFile(urlSource, mCacheDirPath.getAbsolutePath(),
                FILELIST_JSON);

            if (checkFile(mContext.getCacheDir().getAbsolutePath(), FILELIST_JSON)) {
                Log.d(CLASSNAME,
                    "grabJson got local file after update");
                sendMessage("JSONok");
                return new File(mCacheDirPath.getAbsolutePath(), FILELIST_JSON);
            } else {
                Log.d(CLASSNAME,
                    "grabJson got no local file and was unable to dl the update");
                sendMessage("noJson");
                return null;
            }
        }
    }
    public static void sendMessage(String message) {

        Intent intent = new Intent(message);    //action: "msg"
        intent.setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }

    public static final String NO_JSON = "noJson";
    static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public  void sendMessageWithInt(String message, int params) {

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
}