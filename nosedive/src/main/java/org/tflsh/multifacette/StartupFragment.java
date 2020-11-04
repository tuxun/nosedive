package org.tflsh.multifacette;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.SystemClock;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;

//import com.google.firebase.quickstart.auth.java.EmailPasswordActivity;
//import com.google.firebase.quickstart.auth.java.EmailPasswordActivity;
//import com.google.firebase.auth.EmailAuthCredential;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartupFragment extends Fragment {
  public static final String FILE_LIST_JSON = "filelist.json";
  public static final String NO_JSON = "noJson";
  static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String TAG = "StartupFragment";
  private static final String CLASSNAME = "StartupFragment";
  // private static List<String> everyImagesNames;
  private static int currentFile;
  private static String M_SERVER_DIRECTORY_URL;
  protected final Runnable mGrabJsonRunnable;
  ArrayList<String> everyImagesNames;
  ArrayList<String> missingImagesNames;
  List<String> result;
  /*/**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment StartupFragment.
   */
  // TODO: Rename and change types and number of parameters
  private Context mContext;
  private boolean active;
  // TODO: Rename and change types of parameters
  //    private String M_SERVER_DIRECTORY_URL;
  //   private ArrayList<String> missingImagesNames;
  private File mCacheDirPath;
  private ViewGroup mParentViewGroup;
  private View mParentView;
  private ExecutorService executor;
  // static List<String> name;
  //static String urlSource;
  //static List<String> missingImagesNames;
  private Thread globalCheckThread = new Thread(new Runnable() {
    @Override public void run() {

      grabJson(M_SERVER_DIRECTORY_URL, false);
      checkFiles(M_SERVER_DIRECTORY_URL);
      repairMissingFiles(M_SERVER_DIRECTORY_URL, missingImagesNames);
    }
  });
  //waring ce runnable n'enoive plus d'intent, on tente de favoriser celes émises dans grabJson

  /**
   * @mGrabJsonRunnable runnable qui dl le json
   * intent good:NO_JSON,
   * intent bad: JSONok
   * intent strange: JSON_ParseOk, JSON_LocalOnly
   */ {
    mGrabJsonRunnable = new Runnable() {
      @Override
      public void run() {

        try {
          File localJsonFile = checkFile(grabJson(M_SERVER_DIRECTORY_URL,true));

          //TODO: grabjson still return an empty file somewhere.
          if (localJsonFile==null) {

            Log.d(CLASSNAME, "mGrabJsonRunnable unable to create json, we gave up");
            sendMessage("dlFailed");
            return;
          }
          Log.d(CLASSNAME, "mGrabJsonRunnable opening"
              + localJsonFile.getAbsolutePath()
              + " of size "
              + localJsonFile.length());

          //if the images list file is not empty, we can parse its json content
          result = parseJson(localJsonFile);
          if (result.isEmpty()) {

            Log.e(CLASSNAME, "EMPTY json file!!!");
            //sendMessage(NO_JSON);
            Log.e(CLASSNAME,
                "no results: unable to get json from internet or to create files");
          } else {
            Log.d(CLASSNAME, "found this total number of images :"
                    + result.size()
                           /* + " (missing:) "
                            + missingImagesNames.size()*/);
            //            getView().setVisibility(View.VISIBLE);
            //sendMessageWithString("filesMissing", localJsonFile.getAbsolutePath());
            //sendMessage("JSON_ParseOk");
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
  }

  public StartupFragment() {

  }

  public static StartupFragment newInstance(String param1, ArrayList<String> param2) {
    StartupFragment fragment = new StartupFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putStringArrayList(ARG_PARAM2, param2);
    fragment.setArguments(args);
    //M_SERVER_DIRECTORY_URL=getActivity().getSharedPreferences("root",Context.MODE_PRIVATE).getString("DEFAUT_PROJECT_KEY","nokey");

    return fragment;
  }

  /**
   * @param toTest path where the file @name should be checked
   * @return return true if file is looking fine, else return false
   *
   * NO INTENT!
   * checkFile A function for check is file exists or delete it if it is empty
   */
  @Nullable
  protected static File checkFile(File toTest) {
    if (toTest.exists()) {
      if (toTest.length() == 0) {
        Log.e("checkFile", "deleted empty file " + toTest.getAbsolutePath());

        //noinspection ResultOfMethodCallIgnored
        toTest.delete();
        return null;
      }
      Log.e("checkFile", "found file " + toTest.getAbsolutePath() + toTest.length());

      return toTest;
    } else {
      Log.e("checkFile", " empty file, return null" + toTest.getAbsolutePath());
    }

    return null;
  }

  /**
   * @param path path where the file @name should be checked
   * @param originSum sum to check file against
   * @return return true if file is looking fine, else return false
   *
   * NO INTENT!
   * @checkSum A function for check if a file is corrupted
   */
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
        //super warn, reversed bool for test, was false at start
        return false;
      }
    } catch (NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void onPause() {
    active = false;
    globalCheckThread.interrupt();
    super.onPause();
    Log.d(TAG, "Fragment.onPause()");
    // Clear the systemUiVisibility flag

  }

  public void sendMessage(String message) {
    if (active) {
      Intent intent = new Intent(message);    //action: "msg"
      intent.setPackage(mContext.getPackageName());
      mContext.sendBroadcast(intent);
    } else {
      Log.e(TAG, "sendMessage with string canceled due to no view");
    }
  }

  public void sendMessageWithString(String message, String params) {
    if (active) {
      Intent intent = new Intent(message);    //action: "msg"
      intent.setPackage(mContext.getPackageName());

      intent.putExtra(EXTRA_MESSAGE, params);
      mContext.sendBroadcast(intent);
    } else {
      Log.e(TAG, "sendMessageWithString with string canceled due to no view");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      Log.d(TAG, "onCreate had arguments at start");
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
  }
  //should never occur

/*
            //if the images list don't exists, download and save it
            if (ListImageTask.checkFile(mCacheDir.getAbsolutePath(), FILE_LIST_JSON)) {
                Log.d(CLASSNAME, "grabJson got local file,update skipped");
                sendMessage("JSON_LocalOnly");

                return new File(mCacheDir.getAbsolutePath(), FILELIST_JSON);

            } else {

                if (!isInternetOk())
                {
                    Log.d(CLASSNAME, "grabJson did not find local file+no internet");


                sendMessage(NO_JSON);

                return null;
            }
                else {
                return
                    ListImageTask.getFile(urlSource, mCacheDir.getAbsolutePath(),
                        FILELIST_JSON);
            }}

        }
*/

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // mContext=container.getContext();
    active = true;
    mCacheDirPath = mContext.getCacheDir();
    executor = Executors.newFixedThreadPool(1);
    Log.d(CLASSNAME, "onCreateView start grabJson with " + M_SERVER_DIRECTORY_URL);
    Log.e("lastchance", getActivity().getSharedPreferences("", Context.MODE_MULTI_PROCESS)
        .getString("DEFAUT_PROJECT_KEY", "nokeyy"));

    //Log.d(CLASSNAME," added to "+container.toString());
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_startup, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.

    Log.d(TAG, "onCreate had arguments at start " + M_SERVER_DIRECTORY_URL);
    //Log.e("lastchance",getArguments().getString("DEFAUT_PROJECT_KEY"));

    Log.d(TAG, " onViewCreated()");
    setupButtons(view);
    //  view.findViewById(R.id.repairFilesButton).setEnabled(false);
    // view.findViewById(R.id.repairFilesButton).setClickable(false);
    everyImagesNames = new ArrayList<>();
    missingImagesNames = new ArrayList<>();

    //avant on lancait le thread de check d'ici.
    //  sendMessage("StartupViewOk");

  }

  private void setupButtons(View view) {

    view.findViewById(R.id.settingsImageButton).setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          //  view.performClick();

          loadSettingFragment();
        }

        return true;
      }
    });

    view.findViewById(R.id.aboutImageButton);
    /*view.setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          view.performClick();
          Intent intent = null;
          intent = new Intent(getActivity(), EmailPasswordActivity.class);

          String message = "editText.getText().toString()";
          //    intent.putExtra(EXTRA_MESSAGE, message);

          startActivity(intent);
        }
        return true;
      }
    });





     */

    /*login show activity button*/

    view.findViewById(R.id.loginImageButton).setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          view.performClick();
          Intent intent = null;
          intent = new Intent(getActivity(), EmailPasswordActivity.class);

          Log.d(TAG, "EmailPasswordActivity()");

          // String message = "editText.getText().toString()";
          //    intent.putExtra(EXTRA_MESSAGE, message);

          startActivity(intent);
        }
        return true;
      }
    });
    /*repair files button*/
    view.findViewById(R.id.repairFilesButton).setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          Log.d(TAG, "repairFilesButton()");

        /*  view.findViewById(R.id.repairFilesButton).setBackground(
              ResourcesCompat.getDrawable(getResources(), R.color.OurWhite, null));*/
          view.setClickable(false);
          view.setEnabled(false);

       /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.findViewById(R.id.repairFilesButton)
                .setOutlineAmbientShadowColor(R.color.OurWhite);
          }*/
          view.performClick();
/*                    (view.findViewById(R.id.checkFilesButton)).setBackgroundColor(
                        getResources().getColor(R.color.OurWhite, null));
*/
          globalCheckThread = new Thread(new Runnable() {
            @Override public void run() {

              //  repairMissingFiles(
              //      M_SERVER_DIRECTORY_URL, missingImagesNames);
              grabJson(M_SERVER_DIRECTORY_URL, true);
              checkFiles(M_SERVER_DIRECTORY_URL);
              repairMissingFiles(M_SERVER_DIRECTORY_URL, missingImagesNames);
            }
          });
          globalCheckThread.start();
        }
        return true;
      }
    });
    view.findViewById(R.id.repairFilesButton).setClickable(true);
    view.findViewById(R.id.repairFilesButton).setEnabled(true);
  }

  protected void startGlobalCheckThread() {
    globalCheckThread.start();
  }

  private void loadSettingFragment() {

    Log.d(TAG, "loadSettingFragment()");

    FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();

    //transaction1.remove(this).commit();
    SettingsFragment FS = new SettingsFragment();
    Bundle args = new Bundle();
    args.putString("SlideshowFilenames", "plop");
    args.putString("DEFAUT_PROJECT_KEY", getActivity().getApplicationContext()
        .getSharedPreferences(null, Context.MODE_MULTI_PROCESS)
        .getString("DEFAUT_PROJECT_KEY", "alsoNOKEY"));
    FS.setArguments(args);
    //mParentView.findViewById(R.id.motherLayout).setVisibility(View.GONE);
    transaction.replace(R.id.startupScreenLinearLayout, FS, "MULTIFACETTE_Settings");
    // was working transaction.add(R.id.setupScreenLinearSourceLayout, FS, "MULTIFACETTE_Settings");
    //transaction.addToBackStack(null);
    transaction.commit();
  }
/*
  private boolean isInternetOk() {
    //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
    ConnectivityManager cm =
        (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
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
*/
  /**
   * A function for check is file exists or is empty
   *
   * @param urlSource path where the file @name should be checked
   * @param missingFileNamesArg name of the file to check
   */
  public int repairMissingFiles(final String urlSource, ArrayList<String> missingFileNamesArg) {
    int downloadedFilesNumber = 0;

    if(missingFileNamesArg==null)
    {Log.e(CLASSNAME,
        "repairMissingFiles()  canceled cause no files to repair");
      sendMessage("dlFailed");

      return -1;
    }Log.e(CLASSNAME,
        "repairMissingFiles()  missing or broken " + missingFileNamesArg.size() + " files");
    //   checkFiles( urlSource);
    boolean thereIsMissingFiles = true;
    for (final String name : missingFileNamesArg) {
      Log.e(CLASSNAME, "repairMissingFiles() grab missing or broken " + name + " files");
      if (getFile(urlSource, mCacheDirPath.getAbsolutePath(), name)==null) {
        thereIsMissingFiles = true;
        Log.e(CLASSNAME, "unable to dl a file in repairMissingFiles()");

      }
      else {
        downloadedFilesNumber++;
      }
    }
    if (thereIsMissingFiles == false) {
      sendMessage("dlComplete");
    }
    return downloadedFilesNumber;
  }

  public void onResume() {
    Log.e(CLASSNAME, "onResume");
    super.onResume();
    missingImagesNames.clear();

    active = true;
  }

  /**
   * @param jsonFile existing file checked with {@link StartupFragment#checkFile}
   * @return return true if file is looking fine, else return false
   *
   * Intent("filesMissing", filename);
   * Intent("JSON_ParseOk", filename);
   * Intent("filesFound", filename);
   * @parseJson read existing Json file
   */
  //parse the json file, start the dl of missing files or of corrupted files
  protected List<String> parseJson(@Nullable File jsonFile) {
    try (
        JsonReader reader = new JsonReader(
            new InputStreamReader(new FileInputStream(jsonFile.getAbsolutePath())))

    ) {
if(jsonFile==null)
{
  return null;
}
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
            Log.e(CLASSNAME, "grabbing file, it was corrupted");

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
          //  Thread.sleep(50);
        }
        reader.endObject();
      }
      Log.d(CLASSNAME, "ok synchronizing "
              + currentFile //+ "enabling button"
               /* + " of "
                + missingImagesNames.size()*/
      );
      sendMessage("JSON_ParseOk");
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
      return Collections.emptyList();

      // sendMessage(NO_JSON);
    } catch (Exception e) {
      Log.e(CLASSNAME, "unknown exception" + Objects.requireNonNull(e.getMessage()));
      e.printStackTrace();
      return Collections.emptyList();
    } finally {

      Log.d(CLASSNAME,
          "Finished all threads (WARING: not really, we just removed the test)");
    }
    return Collections.emptyList();
  }

  /**
   * @param urlSourceString root url of the project: "https://server.tld/basedir/"
   * @param pathDest where to save downloaded file
   * @param nameDest name of the file to get (string is to urlSourceString and pathDest to obtain
   * complete path
   * @return return null if we are unable to grab the file, or the file directly.
   * Made to be used with, {@link StartupFragment#repairMissingFiles(String, ArrayList)} and {@link
   * StartupFragment#grabJson(String, boolean)}
   *
   * Intent("dlReceived", filename);
   * @getFile download a file from the project (Warning: NO CHECK: IT OVERWRITES FILES!)
   * return null if no internet
   */
  /*return a array of string, naming the files downloaded, or found in the cache dir
   * @param: String url: base string to construct files url
   dlReceived
   * */
  @Nullable
    protected File getFile(String urlSourceString, String pathDest, String nameDest) {
    File localFile = new File(pathDest, nameDest);
    try {
      localFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Log.d(CLASSNAME, "creating ..."
        + localFile.getAbsolutePath()
        + " from "
        + urlSourceString
        + nameDest
        + "  "
        + pathDest);

    try (

        OutputStream fos = new FileOutputStream(localFile);
        InputStream is = new BufferedInputStream(
            new URL(urlSourceString + nameDest).openStream())

    ) {
      Thread.sleep(2000);

      byte[] bitmapBytesData = new byte[1024];
      int read;
      Log.d(CLASSNAME,
          "downloading " + localFile.getPath() + "  from " + urlSourceString + nameDest);
      while ((read = is.read(bitmapBytesData)) != -1) {
        fos.write(bitmapBytesData, 0, read);
      }
      // SystemClock.sleep(1000);
      fos.flush();
      fos.close();
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
      }
    } catch (
        FileNotFoundException e) {
      Log.e(CLASSNAME, "local  file not found");

      e.printStackTrace();
      return localFile;
    }
    catch (
  NetworkOnMainThreadException e) {
    Log.e(CLASSNAME, "NO NET HAHAHAHAHA");

    e.printStackTrace();
    return null;
  } catch (
        SSLException e) {
      Log.e(CLASSNAME, "SSL exception " + getString(R.string.downloadError));
      ((TextView) getView().findViewById(R.id.ui_dl_progressTextView)).setText(
          R.string.downloadError);
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

      // sendMessage(NO_JSON);
      return localFile;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return localFile;
  }

  /**
   * @param urlSourceArg root url of the project: "https://server.tld/basedir/"
   * @return return void, it send intents instead
   *
   * Made to be used with, {@link StartupFragment#repairMissingFiles(String, ArrayList)} and {@link
   * StartupFragment#grabJson(String, boolean)}
   *
   * Intent("dlReceived", filename);
   * @checkFiles download a file from the project (Warning: NO CHECK: IT OVERWRITES FILES!)
   */
  public void checkFiles(String urlSourceArg) {
    sendMessage("checkStarted");
    //  urlSource = urlSourceArg;

    FutureTask<String>
        futureTask1 = new FutureTask<>(mGrabJsonRunnable,
        "mGrabJsonRunnable is complete");

    executor.submit(futureTask1);
    try {

      if (!futureTask1.isDone()) {

        // wait indefinitely for future
        // task to complete
        Log.d(CLASSNAME, "mGrabJsonRunnable output = "
            + futureTask1.get());
      }

      // Wait if necessary for the computation to complete,
      // and then retrieves its result
      String s = futureTask1.get(250, TimeUnit.MILLISECONDS);

      if (s != null) {
        Log.d(CLASSNAME, "mGrabJsonRunnable output=" + s);
      }
    } catch (Exception e) {
      Log.d(CLASSNAME, "Exception in checkFiles(): " + e);
      futureTask1.cancel(true);
    }
  }

  //wtf, on a un deja un runnable grabjson
  public File grabJson(String urlSource, boolean forced) {
    Log.d(CLASSNAME, "start grabJson FUNCTION with " + urlSource);
    Log.d(CLASSNAME, "start grabJson FUNCTION with settings" + getContext().getSharedPreferences("",
        Context.MODE_PRIVATE).getString("DEFAUT_PROJECT_KEY", "noheckingkey"));

    if (!forced) {
      File file = new File(getActivity().getCacheDir().getAbsolutePath(),
          FILE_LIST_JSON);
      if (checkFile(file) == null) {
        Log.d(CLASSNAME,
            "grabJson update forced was canceled and we had no local json");
        // sendMessage(NO_JSON);
        Log.d(CLASSNAME, "grabJson update NOT forced, url source=" + urlSource);
      } else {
        sendMessage("JSON_LocalOnly");
        return file;
      }
    }
    //if internet update not forced:
    else {
      File file = getFile(urlSource, getActivity().getCacheDir().getAbsolutePath(),
          FILE_LIST_JSON);

      Log.d(CLASSNAME, "grabJson update forced, url source=" + urlSource);

      SystemClock.sleep(1000);

      if (null != checkFile(file)) {
        Log.e(CLASSNAME,
            "grabJson got local file after update");
        sendMessage("JSONok");
        return file;
      } else {
        Log.e(CLASSNAME,
            "grabJson got no local file and was unable to dl the update" + mCacheDirPath);
        sendMessage(NO_JSON);

      }
    }
    return null;
  }

  public void sendMessageWithInt(String message, int params) {
    if (this.active) {
      Intent intent = new Intent(message);    //action: "msg"
      intent.setPackage(mContext.getPackageName());

      intent.putExtra(EXTRA_MESSAGE, params);
      mContext.sendBroadcast(intent);
    }
  }
}