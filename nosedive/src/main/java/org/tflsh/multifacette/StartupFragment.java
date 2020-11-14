package org.tflsh.multifacette;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import java.nio.file.Files;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {link StartupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartupFragment extends Fragment {
  public static final String FILE_LIST_JSON = "files_list.json";
  public static final String NO_JSON = "noJson";
  static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

  private static final String TAG = "StartupFragment";
  private static final String CLASSNAME = "StartupFragment";
  private  String mServerBaseProjectKey = "rescatest";
  private  String mServerDirectoryUrl;
  protected final Runnable mGrabJsonRunnable = new Runnable() {
      @Override
      public void run() {

        try {
          File localJsonFile = checkFile(grabJson(mServerDirectoryUrl + mServerBaseProjectKey,true));

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
            Log.e(CLASSNAME,
                "no results: unable to get json from internet or to create files");
          } else {
            Log.d(CLASSNAME, "found this total number of images :"
                + result.size());

          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };

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
  private Context mContext;
  private boolean active;

  private File mCacheDirPath;
  private ExecutorService executor;
  private Thread globalCheckThread = new Thread(new Runnable() {
    @Override public void run() {

      grabJson(mServerDirectoryUrl + mServerBaseProjectKey, false);
      checkFiles();
      repairMissingFiles(mServerDirectoryUrl + mServerBaseProjectKey, missingImagesNames);
    }
  });
  //waring ce runnable n'enoive plus d'intent, on tente de favoriser celes émises dans grabJson

  /*
   * @mGrabJsonRunnable runnable qui dl le json
   * intent good:NO_JSON,
   * intent bad: JSONok
   * intent strange: JSON_ParseOk, JSON_LocalOnly
   */


  /**
   * @param toTest path where the file @name should be checked
   * @return return true if file is looking fine, else return false
   *
   * NO INTENT!
   * checkFile A function for check is file exists or delete it if it is empty
   */
  @Nullable
  protected static File checkFile(@Nullable File toTest) {
    if(null==toTest)
    {
          return null;

    }
    if (toTest.exists()) {
      if (toTest.length() == 0) {
        Log.e(CLASSNAME, "deleted empty file " + toTest.getAbsolutePath());

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          try {
            Files.delete(toTest.toPath());
            return null;
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      }
      Log.e(CLASSNAME, "found file " + toTest.getAbsolutePath() + toTest.length());

      return toTest;
    }

    return null;
  }

  /**
   * @param path path where the file @name should be checked
   * @param originSum sum to check file against
   * @return return true if file is looking fine, else return false
   *
   * NO INTENT!
   ** checkSum: A function for check if a file is corrupted
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
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    mContext = context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    active = true;
    mCacheDirPath = mContext.getCacheDir();
    executor = Executors.newFixedThreadPool(1);
    Log.d(CLASSNAME, "onCreateView start grabJson with " + mServerDirectoryUrl);



    return inflater.inflate(R.layout.fragment_startup, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Use maximum available memory for this memory cache.

    Log.d(TAG, "onCreate had arguments at start " + mServerDirectoryUrl);

    Log.d(TAG, " onViewCreated()");
    setupButtons(view);
    everyImagesNames = new ArrayList<>();
    missingImagesNames = new ArrayList<>();


  }

  private void setupButtons(View view) {

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

    view.findViewById(R.id.aboutImageButton);


    /*login show activity button*/

    view.findViewById(R.id.loginImageButton).setOnTouchListener(new View.OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          view.performClick();
          Intent intent = new Intent(getActivity(), EmailPasswordActivity.class);

          Log.d(TAG, "EmailPasswordActivity()");

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
          mServerDirectoryUrl =new DataStore(getContext()).getValue("BASE_URL","startup fragment button but no url");
          mServerBaseProjectKey =new DataStore(getContext()).getValue("DEFAULT_PROJECT_KEY","startup fragment button but no project key")+"/";

          view.setClickable(false);
          view.setEnabled(false);


          view.performClick();

          globalCheckThread = new Thread(new Runnable() {
            @Override public void run() {

              grabJson(mServerDirectoryUrl + mServerBaseProjectKey, true);
              checkFiles();
              repairMissingFiles(mServerDirectoryUrl + mServerBaseProjectKey, missingImagesNames);
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

  private void loadSettingFragment() {

    Log.d(TAG, "loadSettingFragment()");

    @SuppressWarnings("deprecation") FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction = Objects.requireNonNull(manager).beginTransaction();

    SettingsFragment settingsFragment = SettingsFragment.getInstance();

    transaction.replace(R.id.startupScreenLinearLayout, settingsFragment, "MULTIFACETTE_Settings");
    transaction.commit();
  }

  /**
   * A function for check is file exists or is empty
   *  @param urlSource path where the file @name should be checked
   * @param missingFileNamesArg name of the file to check
   */
  public void repairMissingFiles(final String urlSource, List<String> missingFileNamesArg) {

    if(missingFileNamesArg==null)
    {Log.e(CLASSNAME,
        "repairMissingFiles()  canceled cause no files to repair");
      sendMessage("dlFailed");

      return;
    }Log.e(CLASSNAME,
        "repairMissingFiles()  missing or broken " + missingFileNamesArg.size() + " files");
    boolean thereIsMissingFiles = true;
    for (final String name : missingFileNamesArg) {
      Log.e(CLASSNAME, "repairMissingFiles() grab missing or broken " + name + " files");
      if (getFile(urlSource, mCacheDirPath.getAbsolutePath(), name)==null) {
        thereIsMissingFiles = false;
        Log.e(CLASSNAME, "unable to dl a file in repairMissingFiles()");

      }

    }
    if (!thereIsMissingFiles) {
      sendMessage("dlComplete");
    }
  }
@Override
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
   * parseJson read existing Json file
   */
  //parse the json file, start the dl of missing files or of corrupted files
  protected List<String> parseJson(@Nullable File jsonFile) {
    if (jsonFile == null) throw new AssertionError();

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
            Log.e(CLASSNAME, "grabbing file, it was corrupted");

            missingImagesNames.add(
                everyImagesNames.get(everyImagesNames.size() - 1));
            sendMessageWithString("filesMissing", newIn);
          } else {
            sendMessageWithString("filesFound", newIn);
          }
        } else {
          Log.e(CLASSNAME, "on dl le file");
          missingImagesNames.add(everyImagesNames.get(everyImagesNames.size() - 1));

          sendMessageWithString("filesMissing", newIn);

          reader.nextName();
          reader.nextString();
          reader.nextName();
          reader.nextString();
        }
        reader.endObject();
      }
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
   * Made to be used with, {@link StartupFragment#repairMissingFiles(String, List)} and {@link
   * StartupFragment#grabJson(String, boolean)}
   *
   * Intent("dlReceived", filename);
   * getFile download a file from the project (Warning: NO CHECK: IT OVERWRITES FILES!)
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
      if(localFile.createNewFile())
      {
                Log.d(CLASSNAME, "created " + localFile.getAbsolutePath());

      }
      else {
        Log.d(CLASSNAME, "file was already existing: replacing it. " + localFile.getAbsolutePath());

      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
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
      Thread.sleep(500);

      byte[] bitmapBytesData = new byte[1024];
      int read;
      Log.d(CLASSNAME,
          "downloading " + localFile.getPath() + "  from " + urlSourceString + nameDest);
      while ((read = is.read(bitmapBytesData)) != -1) {
        fos.write(bitmapBytesData, 0, read);
      }
      fos.flush();
      if (!localFile.exists()) {
        Log.d(CLASSNAME, "unable to create " + localFile.getAbsolutePath());

        throw new IOException();
      }
      if (!localFile.getName().contains("json")) {


        sendMessageWithString("dlReceived", localFile.getName());
      }
    } catch (
        FileNotFoundException e) {
      Log.e(CLASSNAME, "local  file not found");

      e.printStackTrace();
      return localFile;
    }
    catch (
  NetworkOnMainThreadException e) {
    Log.e(CLASSNAME, "UNABLE TO REACH INTERNET!");

    e.printStackTrace();
    return null;
  } catch (
        SSLException e) {
      Log.e(CLASSNAME, "SSL exception " + getString(R.string.downloadError));
      ((TextView) requireView().findViewById(R.id.ui_dl_progressTextView)).setText(
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

      return localFile;
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
    return localFile;
  }

  /**
   * return return void, it send intents instead
   *
   * Made to be used with, {@link StartupFragment#repairMissingFiles(String, List)} and {@link
   * StartupFragment#grabJson(String, boolean)}
   *
   * Intent("dlReceived", filename);
   * checkFiles download a file from the project (Warning: NO CHECK: IT OVERWRITES FILES!)
   */
  public void checkFiles() {
    sendMessage("checkStarted");

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

    if (!forced) {
      File file = new File(requireActivity().getCacheDir().getAbsolutePath(),
          FILE_LIST_JSON);
      if (checkFile(file) == null) {
        Log.d(CLASSNAME,
            "grabJson update forced was canceled and we had no local json");
        Log.d(CLASSNAME, "grabJson update NOT forced, url source=" + urlSource);
      } else {
        sendMessage("JSON_LocalOnly");
        return file;
      }
    }
    //if internet update not forced:
    else {
      File file = getFile(urlSource, mCacheDirPath.getAbsolutePath(), FILE_LIST_JSON);

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

}