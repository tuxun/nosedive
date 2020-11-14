package org.tflsh.multifacette;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SlideshowActivity extends AppCompatActivity {
  public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
  private static final String TAG = "SlideshowActivity";
  public static final String FILES_ALL_OK = "filesAllOk";
  public static final String DL_RECEIVED = "dlReceived";

  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////ATTRIBUTES////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////

  ArrayList<String> missingFilesNames;
  IntentFilter filter;
  int missingFilesNumber = 0;
  boolean filesChecked = false;
  private ProgressBar mDlProgressBar;
  private ProgressBar mTotalFilesProgressBar;

  private ArrayList<String> mSlideshowFilesName;

  private int downloadedFilesNumber;
  private ArrayList<String> mSlideshowDownloadedFilesName;
  public final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (Objects.requireNonNull(intent.getAction())) {

        case "dlStarted":
          Log.d(TAG, "intentReceiver got action dl started");
          mDlProgressBar.incrementSecondaryProgressBy(1);
          mDlProgressBar.setVisibility(View.VISIBLE);

          missingFilesNumber++;

          break;

        case "dlComplete":
          Log.d(TAG, "intentReceiver got action dl complete");
          filesChecked = true;
          break;

        case "noJson":
        case "dlFailed":
          findViewById(R.id.repairFilesButton).setEnabled(true);
          findViewById(R.id.repairFilesButton).setClickable(true);

          findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
          ((Button)findViewById(R.id.repairFilesButton)).setText(getText(R.string.there_is_no_files));

          findViewById(R.id.repairFilesButton).setBackground(
              ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_on_off, null));
          findViewById(R.id.repairFilesButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
              new Thread(new Runnable() {
                @Override public void run() {

onResume();
                }
              });
            }
          });


          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              R.string.pleaseRestartWithInternet);

          Log.d(TAG, "intentReceiver got action no json");

          break;

        case "StartupViewOk":
          Log.d(TAG, "intentReceiver got action StartupViewOk, starting startup fragment");


          break;
        case "checkStarted":
          Log.d(TAG, "intentReceiver got action StartupViewOk,starting startup fragment");

          findViewById(R.id.repairFilesButton).setEnabled(false);
          findViewById(R.id.repairFilesButton).setClickable(false);

          break;
        case "JSON_ParseOk":
          Log.d(TAG, "intentReceiver got action JSON_ParseOk");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(R.string.updatedJsonOK);

          loadSlideshowFragment("jsonOk");

          break;

        case "JSONok":

          Log.d(TAG, "intentReceiver got JSONok");
          /*check files button*/
          break;
        case "JSON_LocalOnly":
          Log.d(TAG, "intentReceiver got JSON_LocalOnly");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              R.string.files_list_ok);

          break;

        case FILES_ALL_OK:
          Log.d(TAG, "intentReceiver got action all files ok");

          if (!mSlideshowFilesName.isEmpty()) {
            filesChecked = true;

            findViewById(R.id.checkFilesButton).setVisibility(View.GONE);
            mTotalFilesProgressBar.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_not_started_black, null));
            mTotalFilesProgressBar.setVisibility(View.VISIBLE);

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            loadSlideshowFragment(FILES_ALL_OK);
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
          } else {
            Log.d(TAG, "intentReceiver got action all files ok but slideshow file names was empty");

          }

          break;

        case "filesFound":
          String max = intent.getStringExtra(EXTRA_MESSAGE);
          mSlideshowFilesName.add(max);
          Log.d(TAG,
              "intentReceiver got action files found " + mSlideshowFilesName.size() + " " + max);
          mTotalFilesProgressBar.setVisibility(View.VISIBLE);
          if (missingFilesNames.isEmpty()) {

            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                String.format(Locale.FRANCE,"%d photos ok", mSlideshowFilesName.size()));    } else {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                String.format(Locale.FRANCE,
                    getResources().getQuantityString(R.plurals.missing_files_update_string,mSlideshowFilesName.size()), mSlideshowFilesName.size(),
                    missingFilesNames.size()));
          }
          mTotalFilesProgressBar.setProgress(
              mSlideshowFilesName.size());

          break;
        case DL_RECEIVED:
          String max1 = intent.getStringExtra(EXTRA_MESSAGE);
          Log.d(TAG, "intentReceiver got action dl received" + max1);
          mSlideshowFilesName.add(max1);
          mSlideshowDownloadedFilesName.add(max1);
          findViewById(R.id.repairFilesButton).setEnabled(false);
          findViewById(R.id.repairFilesButton).setClickable(false);
          downloadedFilesNumber++;
          if (mDlProgressBar != null) {
            mDlProgressBar.incrementProgressBy(
                -1);

            mTotalFilesProgressBar.incrementProgressBy(1);


            ((Button) findViewById(R.id.repairFilesButton)).setText(
                String.format(Locale.FRANCE,"%s %d %s", getString(R.string.dl_progressText),
                    missingFilesNames.size() - downloadedFilesNumber, getString(R.string.files)));
            if (mSlideshowDownloadedFilesName.size() == missingFilesNames.size()) {
              Log.e(TAG, "intentReceiver got action dl received, starting slideshow"
                  + mSlideshowFilesName.size());
              ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                  R.string.download_files_ended);
              findViewById(R.id.repairFilesButton).setEnabled(true);
              findViewById(R.id.repairFilesButton).setClickable(true);
              findViewById(R.id.repairFilesButton).setBackground(
                  ResourcesCompat.getDrawable(getResources(),R.drawable.ic_button_on_off, null));
              ((Button) findViewById(R.id.repairFilesButton)).setText(
                  getText(R.string.download_files_ended));
              findViewById(R.id.repairFilesButton).setOnClickListener(
                  new View.OnClickListener() {
                    @Override public void onClick(View view) {
                      loadSlideshowFragment(DL_RECEIVED);

                      findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);
                    }
                  });
            } else if (mSlideshowFilesName.isEmpty()) {

              ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(("Aucune photo, "
                  + (missingFilesNames.size() - downloadedFilesNumber)
                  + " manquantes"));     } else {
              Log.e(TAG,
                  "(missingFilesNames.size()-downloadedFilesNumber" + (missingFilesNames.size()
                      - downloadedFilesNumber));

              ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                  String.format(Locale.FRANCE,getResources().getQuantityString(R.plurals.missing_files_update_string,mSlideshowFilesName.size()), mSlideshowFilesName.size(),
                      missingFilesNames.size()
                          - downloadedFilesNumber));
            }
          } else {
            Log.e(TAG, "intentReceiver got action dl received but fragment was hidden");
          }

          break;
        case "filesMissing":

          String max2 = intent.getStringExtra(EXTRA_MESSAGE);
          missingFilesNames.add(max2);
          Log.d(TAG, "intentReceiver got action files missing " + max2);

          ((Button) findViewById(R.id.repairFilesButton)).setText(
              String.format(Locale.FRANCE,"Récupérer les %d photos manquantes", missingFilesNames.size()));
          mTotalFilesProgressBar.setVisibility(View.VISIBLE);
          findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
          mDlProgressBar.setMax(
              missingFilesNames.size());
          mTotalFilesProgressBar.setProgress(
              missingFilesNames.size());

          if (mSlideshowFilesName.isEmpty()) {

            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(("Aucune photo, "
                + (missingFilesNames.size() - downloadedFilesNumber)
                + " manquantes"));    } else {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                String.format(Locale.FRANCE,
                    getResources().getQuantityString(R.plurals.missing_files_update_string,mSlideshowFilesName.size()), mSlideshowFilesName.size(),
                    missingFilesNames.size()
                        - downloadedFilesNumber));
          }

          mTotalFilesProgressBar.setMax(
              missingFilesNames.size());


          break;

        default:
          Log.e(TAG, "intentReceiver got unknown action");

          break;
      }
    }
  };
  private final SlideshowFragment mSlideshowFragment=SlideshowFragment.getInstance();

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "Activity.onStop()");
  }

  @Override
  protected void onPause() {
    Log.d(TAG, "Activity.onPause()");
    unregisterReceiver(intentReceiver);
    super.onPause();

  }

  @Override
  public void onSaveInstanceState( @NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean("allFilesChecked", filesChecked);
  }

  @Override
  public void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");
    if (savedInstanceState != null) {

      filesChecked = savedInstanceState.getBoolean("allFilesChecked");
      Log.d(TAG, " files checked=" + filesChecked);
    }

    Log.e(TAG, " WARNING RESET COUNTER");

    this.mSlideshowDownloadedFilesName = new ArrayList<>();

    this.mSlideshowFilesName = new ArrayList<>();
    this.missingFilesNames = new ArrayList<>();
    this.missingFilesNumber = 0;


    filter = new IntentFilter(DL_RECEIVED);
    filter.addAction("dlStarted");
    filter.addAction("dlComplete");
    filter.addAction("filesFound");
    filter.addAction(FILES_ALL_OK);
    filter.addAction("filesMissing");
    filter.addAction("StartupViewOk");

    filter.addAction("JSON_ParseOk");
    filter.addAction("checkStarted");

    filter.addAction("noJson");
    filter.addAction("JSONok");
    filter.addAction("ACTION_BOOT_COMPLETED");
    filter.addAction("JSON_LocalOnly");
    AppCompatDelegate.setDefaultNightMode(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); //For night mode theme

    setContentView(R.layout.activity_fullscreen);
    Log.d("activity", "onCreate" + getIntent());
  }

  @Override
  public void onConfigurationChanged( @NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.d(TAG, "onConfigurationChanged" + getIntent());
    onResume();
  }
  public void hideKeyboard(View view) {
    final InputMethodManager imm =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  @Override
  public boolean onKeyDown(int keycode, KeyEvent event) {
  Log.d(TAG, "onKeyDown" + keycode);
    hideKeyboard(getWindow().getDecorView());

  if (keycode == KeyEvent.KEYCODE_BACK) {
    mSlideshowFragment.onStop();


    return true;
  }

  return super.onKeyDown(keycode, event);
}
  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Activity.resume()");
    registerReceiver(intentReceiver, filter);
    if (!filesChecked) {
      Log.d(TAG, "onResume: no bundle" + getIntent());
      mDlProgressBar= findViewById(R.id.uiVerticalMissingProgressBar);
      mTotalFilesProgressBar= findViewById(R.id.uiTotalFilesProgressBar);


      ActionBar actionBar = getActionBar();

      if (actionBar != null) {
        actionBar.hide();
      }

      Log.d(TAG,
          "ListImageTask missing file after 5second and an intent? = " + missingFilesNames.size());

    } else {
      Log.e("onResume", "files were already ALL OK");

      loadSlideshowFragment("ActivityResumeOk");

    }
  }

  /**
   * create the slideshow fragment with slideshow filenames as argument
   */

  private void loadSlideshowFragment(String fromIntent) {
    if (mSlideshowFilesName.isEmpty()) {


      Log.e(TAG, "startSlideshow aborted cause array was empty, halt (no net?)"
          + mSlideshowFilesName.size());
      Log.e(TAG, fromIntent + "received, mSlideshowFilesName" + mSlideshowFilesName.size());
      Log.e(TAG, fromIntent + "received, missingFilesNames" + missingFilesNames.size());
      Log.e(TAG, fromIntent
          + "received, mSlideshowDownloadedFilesName"
          + mSlideshowDownloadedFilesName.size());
    } else {
      Log.e(TAG, fromIntent + "received, mSlideshowFilesName" + mSlideshowFilesName.size());
      Log.e(TAG, fromIntent + "received, missingFilesNames" + missingFilesNames.size());
      Log.e(TAG, fromIntent
          + "received, mSlideshowDownloadedFilesName"
          + mSlideshowDownloadedFilesName.size());

      findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);

      Log.d(TAG, "loadSlideshowFragment()");
      Bundle args = new Bundle();
      args.putStringArrayList("SlideshowFilenames", mSlideshowFilesName);
      mSlideshowFragment.setArguments(args);

      FragmentManager manager = getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();
      transaction.add(R.id.slideshowScreenLinearSourceLayout, mSlideshowFragment,
          "MULTIFACETTE_SLIDESHOW");
      transaction.addToBackStack(null);
      transaction.commit();

      mSlideshowFragment.startSlideshow(mSlideshowFilesName);

    }
  }


}