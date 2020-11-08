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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

  // static final String M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "rescatest/";
  //temps unitaire (base de temps), sert a définir le delai entre deux images
  /*

  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();


  private final Handler mHideHandler = new Handler();
  private final Runnable mSetFullscreenOnRunnable = new Runnable() {
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      Log.e("SlideshowActivity", "mShowPart2Runnable ENTER FULLSCREEN");
      // Hide UI first
      android.app.ActionBar actionBar = getActionBar();
      if (actionBar != null) {
        actionBar.hide();
      }

      getWindow().getDecorView()
          .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
  };
   */
  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////ATTRIBUTES////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  //ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
  //runnnable s'appelant lui meme a la fin du diapo qu'il lance
  //int screenHeight;
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
        //todo: we now also receive filename as string

        case "dlComplete":
          Log.d(TAG, "intentReceiver got action dl complete");
//          mHaveInternet=true;
          filesChecked = true;

  /* (findViewById(R.id.button2)).setBackgroundColor(
              getResources().getColor(R.color.OurPink, null));*/
         //   findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);

           // findViewById(R.id.repairFilesButton).setVisibility(View.GONE);
            //startSlideshow("dlComplete");
          /*
          findViewById(R.id.ui_dl_ProgressBar).setBackground(
              getDrawable(R.drawable.ic_not_started_black));
          (findViewById(R.id.ui_dl_ProgressBar)).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
              if (event.getAction() == MotionEvent.ACTION_UP) {
                fileschecked = true;
                view.performClick();
                ((View) findViewById(R.id.startupScreenLinearSourceLayout)).setVisibility(
                    View.GONE);
                ((View) findViewById(R.id.ui_press_meTextView)).setVisibility(View.VISIBLE);
                //getFragmentManager().findFragmentByTag("StartupFragment").onDestroy();

                mSlideshowFragment.toggle();
                mSlideshowFragment.startSlideshow(mSlideshowFilesName);

        }
              return true;
            }
          });
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
   */
        /*  } else {
            findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
            findViewById(R.id.repairFilesButton).setEnabled(true);
            findViewById(R.id.repairFilesButton).setClickable(true);
            findViewById(R.id.repairFilesButton).setBackground(
                getResources().getDrawable(R.drawable.ic_button_on_off, null));
          }*/
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

                 /* !!! mSlideshowFragment.mStartupFragment.grabJson(
                      StartupFragment.M_SERVER_DIRECTORY_URL,
                      true);*/
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

          //!!! ((SlideshowFragment)  getFragmentManager().findFragmentByTag("SlideshowFragment")).setBaseUrl(M_SERVER_DIRECTORY_URL);

          break;
        case "checkStarted":
          Log.d(TAG, "intentReceiver got action StartupViewOk,starting startup fragment");

          //!!! ((SlideshowFragment)  getFragmentManager().findFragmentByTag("SlideshowFragment")).setBaseUrl(M_SERVER_DIRECTORY_URL);
          findViewById(R.id.repairFilesButton).setEnabled(false);
          findViewById(R.id.repairFilesButton).setClickable(false);
         /* findViewById(R.id.repairFilesButton).setBackground(
              ResourcesCompat.getDrawable(getResources(), R.drawable.white_background, null));
          */
          break;
        //TODO: fix!
        case "JSON_ParseOk":
          Log.d(TAG, "intentReceiver got action JSON_ParseOk");
/*
          findViewById(R.id.repairFilesButton).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
              if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
                view.setEnabled(false);
                view.setClickable(false);
                mTotalFilesProgressBar.setProgress(0);
                //!!! pour checkfiles=missinf findViewById(R.id.button2).setVisibility(View.VISIBLE);
                //getFragmentManager().executePendingTransactions();

                new Thread(new Runnable() {
                  @Override public void run() {
                    try {
                      Thread.sleep(1000);
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                    //     missingFilesNames.clear();

                    //   mSlideshowFilesName.clear();

                    Log.d(TAG, "StartupFragment.checkFiles()");
                    if (getFragmentManager().findFragmentByTag("SlideshowFragment") == null) {
                      Log.d(TAG, "SlideshowFragment.void()");
                    } else {
                      Log.d(TAG, "SlideshowFragment.found in SlideshowActivity()");
                    }
                    if (getFragmentManager().findFragmentByTag("StartupFragment") == null) {
                      Log.d(TAG, "StartupFragment.void()");
                    } else {
                      Log.d(TAG, "StartupFragment.found in SlideshowActivity()");
                    }

                     */
                    //F! do nothing cause it not the fragment Startup!
                    /*((SlideshowFragment) getFragmentManager().findFragmentByTag(
                        "SlideshowFragment")).exec(
                        M_SERVER_DIRECTORY_URL);
                  }
                }).start();
              }
              return true;
            }
          });*/
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(R.string.updatedJsonOK);
         // findViewById(R.id.repairFilesButton).setEnabled(true);
          //findViewById(R.id.repairFilesButton).setClickable(true);
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

        //TODO:
        case "filesAllOk":
          Log.d(TAG, "intentReceiver got action all files ok");

          //(findViewById(R.id.checkFilesButton)).setBackgroundColor(
          // getResources().getColor(R.color.OurPink, null));
          if (!mSlideshowFilesName.isEmpty()) {
            filesChecked = true;

            findViewById(R.id.checkFilesButton).setVisibility(View.GONE);
            mTotalFilesProgressBar.setBackground(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_not_started_black, null));
            mTotalFilesProgressBar.setVisibility(View.VISIBLE);
            /*start button
            (findViewById(R.id.ui_dl_ProgressBar)).setOnTouchListener(new OnTouchListener() {

              @Override
              public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                  fileschecked = true;
                  view.performClick();
                  findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);
                  findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
                  //getFragmentManager().findFragmentByTag("StartupFragment").onDestroy();

                  mSlideshowFragment.toggle();
                  mSlideshowFragment.startSlideshow(mSlideshowFilesName);
                }
                return true;
              }
            });*/
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            loadSlideshowFragment("filesAllOk");
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
          } else {
            Log.d(TAG, "intentReceiver got action all files ok but slideshow file names was empty");

            //§!! findViewById(R.id.repairFilesButton).setEnabled(true);
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
                String.format(Locale.FRANCE,"%d photos ok, %d manquantes", mSlideshowFilesName.size(),
                    missingFilesNames.size()));
          }
          mTotalFilesProgressBar.setProgress(
              mSlideshowFilesName.size());

          break;
        case "dlReceived":
          //mHaveInternet=true;
          String max1 = intent.getStringExtra(EXTRA_MESSAGE);
          Log.d(TAG, "intentReceiver got action dl received" + max1);
          mSlideshowFilesName.add(max1);
          mSlideshowDownloadedFilesName.add(max1);
          //!!! ((SlideshowFragment)  getFragmentManager().findFragmentByTag("SlideshowFragment")).setBaseUrl(M_SERVER_DIRECTORY_URL);
          findViewById(R.id.repairFilesButton).setEnabled(false);
          findViewById(R.id.repairFilesButton).setClickable(false);
         /* findViewById(R.id.repairFilesButton).setBackground(
              ResourcesCompat.getDrawable(getResources(), R.drawable.white_background, null));*/
          downloadedFilesNumber++;
          if (mDlProgressBar != null) {
            mDlProgressBar.incrementProgressBy(
                -1);

            mTotalFilesProgressBar.incrementProgressBy(1);


          /* ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
           "il manque " + (missingFilesNames.size()-((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).getProgress()
            ) + " fichiers");*/
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
              //boolean filesWereAlreadyFound = true;
              ((Button) findViewById(R.id.repairFilesButton)).setText(
                  getText(R.string.download_files_ended));
              findViewById(R.id.repairFilesButton).setOnClickListener(
                  new View.OnClickListener() {
                    @Override public void onClick(View view) {
                      loadSlideshowFragment("dlReceived");

                      findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);
                      //mSlideshowFragment.startSlideshow(mSlideshowFilesName);
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
                  String.format(Locale.FRANCE,"%d photos ok, %d manquantes", mSlideshowFilesName.size(),
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
                String.format(Locale.FRANCE,"%d photos ok, %d manquantes", mSlideshowFilesName.size(),
                    missingFilesNames.size()
                        - downloadedFilesNumber));
          }

          mTotalFilesProgressBar.setMax(
              missingFilesNames.size());
          //    Log.d(TAG, "intentReceiver set progress bar " + missingFilesNames.size() + max2);
         /* ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "il manque " + missingFilesNames.size() + " fichiers");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "il manque " + missingFilesNames.size() + " fichiers");
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);*/

          break;

        default:
          Log.e(TAG, "intentReceiver got unknown action");

          break;
      }
    }
  };
//  protected boolean mHaveInternet = false;
  //private static final SettingsFragment mSettingsFragment=new SettingsFragment().getInstance();
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


    filter = new IntentFilter("dlReceived");
    filter.addAction("dlStarted");
    filter.addAction("dlComplete");
    filter.addAction("filesFound");
    filter.addAction("filesAllOk");
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

    //end try lru

    //baseurl,projectcode, todo make static every fragment.get_instance()?
    //mSettingsFragment = new SettingsFragment();
    //mSlideshowFragment = new SlideshowFragment();

    Log.d("activity", "onCreate" + getIntent());
   // View initView = getWindow().getDecorView();
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
/*  if(mSlideshowFragment!=null)
  {
    mSlideshowFragment.cleanNext();
  }*/

  if (keycode == KeyEvent.KEYCODE_BACK) {
    this.onRestart();
    //loadStartupFragment();
   // onResume();
    return true;
  }

  return super.onKeyDown(keycode, event);
}
  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Activity.resume()");
    //mSlideshowFilesName.clear();
    registerReceiver(intentReceiver, filter);

    //!!! mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY);

    // mSlideshowFragment.setBaseUrl(M_SERVER_DIRECTORY_URL);

    //!!!StartupFragment.newInstance(M_SERVER_DIRECTORY_URL,missingFilesNames);

    if (!filesChecked) {
      Log.d(TAG, "onResume: no bundle" + getIntent());
      //loadStartupFragment();
      mDlProgressBar= findViewById(R.id.uiVerticalMissingProgressBar);
      mTotalFilesProgressBar= findViewById(R.id.uiTotalFilesProgressBar);


     /* FragmentManager manager = getFragmentManager();

      FragmentTransaction transaction = manager.beginTransaction();
      transaction.attach(new StartupFragment());
      transaction.addToBackStack(null);
      transaction.commit();
      mAsyncTaskManager =
          new BackgroundImageDecoder(getApplicationContext(), screenWidth, screenHeight, memoryCache,
              executor);
      */

      ActionBar actionBar = getActionBar();

      if (actionBar != null) {
        actionBar.hide();
      }

      Log.d(TAG,
          "ListImageTask missing file after 5second and an intent? = " + missingFilesNames.size());
      //!!!  loadStartupFragment();
     // fileschecked = true;
    } else {
      Log.e("onResume", "files were already ALL OK");

      loadSlideshowFragment("ActivityResumeOk");
/*
      if (findViewById(R.id.imageView) == null) {
        Log.e("onResume", "loading fragment");
        //!!!  loadSlideshowFragment();
      } else {
        Log.e("onResume", "loading startup screen");
        View iframe = findViewById(R.id.slideshowLayout);

     ViewGroup parent = (ViewGroup) iframe.getParent();
        parent.removeView(iframe);
        //grgr.setContentView(R.layout.activity_fullscreen);
        ((ImageView) findViewById(R.id.imageView)).setImageDrawable(
            getResources().getDrawable(R.drawable.default_background, null));
        */
      //  startSlideshow("onresume");
    }
  }

  /**
   * create the slideshow fragment with slideshow filenames as argument
   * TODO: it uses a setArguments()
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
      //
      // stop  we prob dont have internet onResume();
    }
  }


}