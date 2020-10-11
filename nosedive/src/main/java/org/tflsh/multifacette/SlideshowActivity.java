package org.tflsh.multifacette;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SlideshowActivity extends Activity {
  public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
  static final String M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "julia/";
  //temps unitaire (base de temps), sert a définir le delai entre deux images
  static final int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  static final int DELAY_GUESSING_SETTING = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  static final int DELAY_CHOICE_WORDS_SETTING = 10000;
  private static final String TAG = "SlideshowActivity";
  private static final long UI_ANIMATION_DELAY = 300;
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
  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////ATTRIBUTES////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  boolean mHaveInternet = false;
  ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
  //runnnable s'appelant lui meme a la fin du diapo qu'il lance
  int screenHeight;
  ArrayList<String> missingFilesNames;
  IntentFilter filter;
  int missingFilesNumber = 0;
  boolean fileschecked = false;
  private ProgressBar mDlProgressBar;
  private ArrayList<String> mSlideshowFilesName;
  private SlideshowFragment mSlideshowFragment;

  private int downloadedFilesNumber;
  private ArrayList mSlideshowDownloadedFilesName;
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
         /* (findViewById(R.id.button2)).setBackgroundColor(
              getResources().getColor(R.color.OurPink, null));*/
          if (!mSlideshowFilesName.isEmpty()) {
            fileschecked = true;
            findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);

            findViewById(R.id.repairFilesButton).setVisibility(View.GONE);
            startSlideshow("dlComplete");/*
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
          } else {
            findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
            findViewById(R.id.repairFilesButton).setEnabled(true);
            findViewById(R.id.repairFilesButton).setClickable(true);
            findViewById(R.id.repairFilesButton).setBackground(
                getResources().getDrawable(R.drawable.ic_button_on_off, null));
          }
          break;

        case "noJson":
          mHaveInternet = false;
          // findViewById(R.id.repairFilesButton).setEnabled(true);
          //findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
          //  findViewById(R.id.repairFilesButton).setBackground(
          //  getResources().getDrawable(R.drawable.ic_button_on_off, null));//

          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              R.string.pleaseRestartWithInternet);
          Log.d(TAG, "intentReceiver got action no json");

          break;
        case "StartupViewOk":
          Log.d(TAG, "intentReceiver got action StartupViewOk,startting startup fragment");

          //!!! ((SlideshowFragment)  getFragmentManager().findFragmentByTag("SlideshowFragment")).setBaseUrl(M_SERVER_DIRECTORY_URL);

          break;
        case "JSONparseok":
          Log.d(TAG, "intentReceiver got action JSONparseok");

          findViewById(R.id.checkFilesButton).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
              if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
                view.setEnabled(false);
                view.setClickable(false);
                ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setProgress(0);
                //!!! pour checkfiles=missinf findViewById(R.id.button2).setVisibility(View.VISIBLE);
                getFragmentManager().executePendingTransactions();

                new Thread(new Runnable() {
                  @Override public void run() {
                    try {
                      Thread.sleep(1000);
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                    missingFilesNames.clear();

                    mSlideshowFilesName.clear();
                    Log.d(TAG, "StartupFragment.exec()");
                    if (getFragmentManager().findFragmentByTag("SlideshowFragment") == null) {
                      Log.d(TAG, "SlideshowFragment.void()");
                    } else {

                    }
                    if (getFragmentManager().findFragmentByTag("StartupFragment") == null) {
                      Log.d(TAG, "StartupFragment.void()");
                    } else {

                    }

                    ((SlideshowFragment) getFragmentManager().findFragmentByTag(
                        "SlideshowFragment")).exec(
                        M_SERVER_DIRECTORY_URL);
                  }
                }).start();
              }
              return true;
            }
          });
          findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);

          (findViewById(R.id.repairFilesButton)).setEnabled(true);
          (findViewById(R.id.repairFilesButton)).setClickable(true);
          //!!! ((SlideshowFragment)  getFragmentManager().findFragmentByTag("SlideshowFragment")).setBaseUrl(M_SERVER_DIRECTORY_URL);

          break;

        case "JSONok":
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(R.string.updatedJsonOK);

          Log.d(TAG, "intentReceiver got JSONok");
          /*check files button*/
          break;
        case "JSONlocalonly":
          Log.d(TAG, "intentReceiver got JSONlocalonly");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "Liste des photos ok");

          break;

        //TODO:
        case "filesAllOk":
          Log.d(TAG, "intentReceiver got action all files ok");

          //(findViewById(R.id.checkFilesButton)).setBackgroundColor(
          // getResources().getColor(R.color.OurPink, null));
          if (!mSlideshowFilesName.isEmpty()) {
            fileschecked = true;

            findViewById(R.id.checkFilesButton).setVisibility(View.GONE);
            findViewById(R.id.ui_dl_ProgressBar).setBackground(
                getDrawable(R.drawable.ic_not_started_black));
            findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
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
            startSlideshow("filesAllOk");
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
          } else {
            //§!! findViewById(R.id.repairFilesButton).setEnabled(true);
          }

          break;

        case "filesFound":
          String max = intent.getStringExtra(EXTRA_MESSAGE);
          mSlideshowFilesName.add(max);
          Log.d(TAG,
              "intentReceiver got action files found " + mSlideshowFilesName.size() + " " + max);
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
          if (missingFilesNames.size() > 0) {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                (mSlideshowFilesName.size())
                    + " photos ok, "
                    + missingFilesNames.size()
                    + " manquantes");
          } else {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                (mSlideshowFilesName.size()) + " photos ok");
          }
          ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setProgress(
              mSlideshowFilesName.size());

          break;
        case "dlReceived":
          String max1 = intent.getStringExtra(EXTRA_MESSAGE);
          Log.d(TAG, "intentReceiver got action dl received" + max1);
          mSlideshowFilesName.add(max1);
          mSlideshowDownloadedFilesName.add(max1);
          downloadedFilesNumber++;
          if (((ProgressBar) findViewById(R.id.ui_missing_ProgressBar)) != null) {
            ((ProgressBar) findViewById(R.id.ui_missing_ProgressBar)).incrementProgressBy(-1);
          }
          if (((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)) != null) {
            ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementProgressBy(1);


          /* ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
           "il manque " + (missingFilesNames.size()-((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).getProgress()
            ) + " fichiers");*/
            ((Button) findViewById(R.id.repairFilesButton)).setText(
                new StringBuilder().append(getString(R.string.dl_progressText))
                    .append(" de ")
                    .append(missingFilesNames.size() - downloadedFilesNumber)
                    .append(" ")
                    .append(getString(R.string.files))
                    .toString());
            if (mSlideshowDownloadedFilesName.size() == missingFilesNames.size()) {
              Log.e(TAG, "intentReceiver got action dl received, starting slideshow"
                  + mSlideshowFilesName.size());
              startSlideshow("dlReceived");
            } else if (mSlideshowFilesName.size() > 0) {
              ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                  (mSlideshowFilesName.size()) + " photos ok, " + (missingFilesNames.size()
                      - downloadedFilesNumber) + " manquantes");
            } else {
              ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(("Aucune photo, "
                  + (missingFilesNames.size() - downloadedFilesNumber)
                  + " manquantes"));
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
              "Récupérer les " + (missingFilesNames.size())
                  + " photos manquantes");
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
          findViewById(R.id.repairFilesButton).setVisibility(View.VISIBLE);
          ((ProgressBar) findViewById(R.id.ui_missing_ProgressBar)).setProgress(
              missingFilesNames.size());

          if (mSlideshowFilesName.size() > 0) {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                (mSlideshowFilesName.size()) + " photos ok, " + (missingFilesNames.size()
                    - downloadedFilesNumber) + " manquantes");
          } else {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(("Aucune photo, "
                + (missingFilesNames.size() - downloadedFilesNumber)
                + " manquantes"));
          }

          ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setMax(
              missingFilesNames.size() + mSlideshowFilesName.size());
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

  private void startSlideshow(String fromintent) {
    Log.e(TAG, fromintent + "received, mSlideshowFilesName" + mSlideshowFilesName.size());
    Log.e(TAG, fromintent + "received, missingFilesNames" + missingFilesNames.size());
    Log.e(TAG, fromintent
        + "received, mSlideshowDownloadedFilesName"
        + mSlideshowDownloadedFilesName.size());

    findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);
    mSlideshowFragment.startSlideshow(mSlideshowFilesName);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //tools

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
  public void onSaveInstanceState(@NotNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean("allfileschecked", fileschecked);
  }

  @Override
  public void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    mSlideshowFragment =
        (SlideshowFragment) getFragmentManager().findFragmentByTag(
            "SlideshowFragment");//new org.tflsh.nosedive.SlideshowFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");
    if (savedInstanceState != null) {

      fileschecked = savedInstanceState.getBoolean("allfileschecked");
      Log.d(TAG, " fileschecked=" + fileschecked);
    }

    Log.e(TAG, " WARNING RESET COUNTER");

    this.mSlideshowDownloadedFilesName = new ArrayList<>();

    this.mSlideshowFilesName = new ArrayList<>();
    this.missingFilesNames = new ArrayList<>();
    this.missingFilesNumber = 0;
    /************

     View iframe=findViewById(R.id.motherLayout);
     ViewGroup parent = (ViewGroup) iframe.getParent();
     int index=parent.indexOfChild(iframe);
     parent.removeView(iframe);
     //inflate
     View dlLayout=getLayoutInflater().inflate(R.layout.activity_dlfullscreen,parent,false);
     parent.addView(dlLayout,index);
     ********/

    filter = new IntentFilter("dlReceived");
    filter.addAction("dlStarted");
    filter.addAction("dlComplete");
    filter.addAction("filesFound");
    filter.addAction("filesAllOk");
    filter.addAction("filesMissing");
    filter.addAction("StartupViewOk");

    filter.addAction("JSONparseok");
    filter.addAction("noJson");
    filter.addAction("JSONok");
    filter.addAction("ACTION_BOOT_COMPLETED");
    filter.addAction("JSONlocalonly");
    setContentView(R.layout.activity_fullscreen);

    //end try lru

    //baseurl,projectcode, todo

    SettingsFragment mSettingsFragment = new SettingsFragment();
    Log.d("activity", "onCreate" + getIntent());
  }

  @Override
  public void onConfigurationChanged(@NotNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.d(TAG, "onConfigurationChanged" + getIntent());
    onResume();
  }
@Override
  public boolean onKeyDown(int keycode, KeyEvent event)
  {
    if(keycode==KeyEvent.KEYCODE_BACK)
    {
      return true;
    }

    return super.onKeyDown(keycode,event);
  }
  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Activity.resume()");
    mSlideshowFilesName.clear();
    registerReceiver(intentReceiver, filter);




  //!!! mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY);

    // mSlideshowFragment.setBaseUrl(M_SERVER_DIRECTORY_URL);

    //!!!StartupFragment.newInstance(M_SERVER_DIRECTORY_URL,missingFilesNames);

    if (!fileschecked) {
      Log.d(TAG, "onResume: no bundle" + getIntent());


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
      fileschecked = true;
    } else {
      Log.e("onResume", "files were already ALLOK");

      if (findViewById(R.id.imageView) == null) {
        Log.e("onResume", "loading fragment");
        //!!!  loadSlideshowFragment();
      } else {
        Log.e("onResume", "loading startup screen");
        View iframe = findViewById(R.id.slideshowLayout);

/*        ViewGroup parent = (ViewGroup) iframe.getParent();
        parent.removeView(iframe);*/
        //grgr.setContentView(R.layout.activity_fullscreen);
        ((ImageView) findViewById(R.id.imageView)).setImageDrawable(
            getResources().getDrawable(R.drawable.default_background, null));
      }
    }
  }

  /**
   * create the slideshow fragment with slideshow filenames as argument
   * TODO: it uses a setArguments()
   *
   */
  private void loadSlideshowFragment() {

    Log.d(TAG, "loadSlideshowFragment()");
    Bundle args = new Bundle();
    args.putStringArrayList("SlideshowFilenames", mSlideshowFilesName);
    mSlideshowFragment.setArguments(args);
    FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(R.id.motherLayout, mSlideshowFragment, "MULTIFACETTE_SLIDESHOW");
    transaction.addToBackStack(null);
    transaction.commit();

    findViewById(R.id.startupScreenLinearSourceLayout).setVisibility(View.GONE);
  }
}