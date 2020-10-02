package org.tflsh.nosedive;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;

public class SlideshowActivity extends Activity {

  public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
  //temps unitaire (base de temps), sert a définir le delai entre deux images
  static final int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  static final int DELAY_GUESSING_SETTING = 5000;
  static final String M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "julia/";
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  static final int DELAY_CHOICE_WORDS_SETTING = 10000;
  private static final String TAG = "SlideshowActivity";
  private static final long UI_ANIMATION_DELAY = 300;
  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
  private final Handler mSlideshowHandler = new Handler();

  private final Runnable cleanButtonRunnable = new Runnable() {
    @Override
    public void run() {
      int clickedButtons = mCheckedToggleButtonsArrayList.size();
      Log.d(TAG, "cleanButtonRunnable:" + clickedButtons + " cleaned buttons");
      for (int i = clickedButtons - 1; i >= 0; i--) {
        mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
        mCheckedToggleButtonsArrayList.get(i).setClickable(true);
        mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
      }
    }
  };

  private final Handler mHideHandler = new Handler();
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
  AsyncTaskManager mAsyncTaskManager;
  int screenWidth;
  DisplayMetrics screenMetrics;
  private LinearLayout centralLinearLayout;
  private ProgressBar mDlProgressBar;
  private final Runnable mSetFullscreenOnRunnable = new Runnable() {
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      Log.e("mShowPart2Runnable", "ENTER FULLSCREEN");
      // Hide UI first
      android.app.ActionBar actionBar = getActionBar();
      if (actionBar != null) {
        actionBar.hide();
      }

      //findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      //exitfullscreen getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().getDecorView()
          .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

     // findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      // Enables regular immersive mode.
      // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
      // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
  };
  private int buttonTextSize;
  private int pressMeTextSize;
  private int pressTwoWordsTextSize;
  private ImageView mImageView;
  private int pwa = 0;
  private TextView pressMeTextView;

  private int buttonVerticalPadding;
  private int buttonHorizontalPadding;
  private int buttonVerticalMargin;
  private int buttonHorizontalMargin;
  private ArrayList<String> mSlideshowFilesName;
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {
    @Override
    public void run() {
      Log.d(TAG, "mShowImageAfterTwoWordsRunnable");
      ((TextView)findViewById(R.id.ui_press_meTextView)).setTextColor(getColor(R.color.OurWhite));
      mSlideshowHandler.postDelayed(cleanButtonRunnable, UI_ANIMATION_DELAY);
      mSlideshowHandler.postDelayed(mHideMenuRunnable, DELAY_INTER_FRAME_SETTING);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
/*
      mImageView.setImageDrawable(
          ResourcesCompat.getDrawable(getResources(), R.drawable.whitebackground, null));
*/
      mSlideshowHandler.post(
          mAsyncTaskManager.new ShowImageTask(((ImageView)findViewById(R.id.imageView)), DELAY_INTER_FRAME_SETTING,
          getCacheDir() + "/" + mSlideshowFilesName.get(
              new Random().nextInt(mSlideshowFilesName.size()))));

      mHideHandler.post(cleanButtonRunnable);

      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_GUESSING_SETTING);
    }
  };
  private boolean mSlideshowIsRunning = false;
  private final Runnable mHideMenuRunnable = new Runnable() {
    @Override
    public void run() {
      Log.e("mHideMenuRunnable", "visible ?");
      int clickedButtons = mCheckedToggleButtonsArrayList.size();
      if (clickedButtons > 0) {
        for (int i = clickedButtons - 1; i >= 0; i--) {

          mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
          mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
        }
        //a chaque button cliqué, si on est perdu, on decheck les bouttons
        //should only  happen when 2 DIFFERENT buttons are pressed
        mSlideshowIsRunning = false;
      }

      findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);
    }
  };
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////ACTIVITY (MAIN)/////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {

      Log.d(TAG, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesName.size());
      findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

      findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);

      if ((missingFilesNames.isEmpty()) && (!mSlideshowFilesName.isEmpty())) {
        mSlideshowHandler.removeCallbacks(showNextRunnable);
        //      mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);

        makeImageClickable();
        //hummm
        mHideHandler.post(cleanButtonRunnable);

        ((TextView) findViewById(R.id.ui_press_meTextView)).setTypeface(
            ResourcesCompat.getFont(getApplicationContext(), R.font.alef));
        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);
        ((TextView) findViewById(R.id.ui_press_meTextView)).setText(
            getResources().getString(R.string.string_press_me));

        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_SP,
            pressMeTextSize);

        (findViewById(R.id.ui_press_meTextView)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(getColor(R.color.OurPink));
        //findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
        //findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);

        if (!mSlideshowIsRunning) {

          mSlideshowIsRunning = true;

          for (long i = 0; i < mSlideshowFilesName.size() + 1; i++) {
            mSlideshowHandler.postDelayed(showNextRunnable, i * DELAY_INTER_FRAME_SETTING);
          }
        } else {
          Log.e(TAG, "mStartSlideshowRunnable tried to start twice" + mSlideshowFilesName.size());
          mSlideshowIsRunning = false;
        }
      } else {
        Log.e(TAG, "isInternetOk" + missingFilesNames.size());

        //if (isInternetOk()) {
        //  pressMeTextView.setText(R.string.string_wait4dl);
        //} else {
        //
        //
        //
        //  ((TextView)findViewById(R.id.ui_press_meTextView)).setText(R.string.pleaseRestartWithInternet);
        //}
      }
    }
  };
  private LruCache<String, Bitmap> memoryCache;
  private Bundle bundle;
  private ExecutorService executor;
  private final Runnable showNextRunnable = new Runnable() {
    @Override
    public void run() {
      //antibounce

      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);

      int nextImageToShowIndex = new Random().nextInt(mSlideshowFilesName.size());
      if (pwa < mSlideshowFilesName.size()) {
        if ((pwa % 2) == 1) {

          executor.execute(
              mAsyncTaskManager.new ShowImageTask(
                  (ImageView) findViewById(R.id.imageView),
                  DELAY_INTER_FRAME_SETTING,
                  getCacheDir() + "/" + mSlideshowFilesName.get(nextImageToShowIndex))
          );
          ((TextView)findViewById(R.id.ui_press_meTextView)).setTextColor(getColor(R.color.OurWhite));
        } else {

          executor.execute(
              mAsyncTaskManager.new ShowImageTask(
                  ((ImageView)findViewById(R.id.imageView)),
                  DELAY_INTER_FRAME_SETTING,
                  getCacheDir() + "/" + mSlideshowFilesName.get(nextImageToShowIndex))
          );
          ((TextView)findViewById(R.id.ui_press_meTextView)).setTextColor(getColor(R.color.OurPink));
        }

        pwa++;
      } else {
        Log.d(TAG, "mShowNextRunnable: no more images, restarting slideshow");
        pwa = 0;
        mSlideshowIsRunning = false;
        mSlideshowHandler.post(mStartSlideshowRunnable); //end handlepostdelay
      }
    }

    // Code here will run in UI thread
  };
  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////RUNNABLES////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  //used for show words?.
  private final Runnable showMenuRunnable = new Runnable() {
    @Override
    public void run() {

      Log.d(TAG, "makeImageNotClickable(): image isshowMenuRunnable not clickable anymore");
      findViewById(R.id.imageView).setClickable(false);

      Log.d(TAG, "showMenuRunnable");
      ((TextView) findViewById(R.id.ui_press_meTextView)).setTypeface(
          ResourcesCompat.getFont(getApplicationContext(), R.font.alef));
      ((TextView) findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_SP,
          pressTwoWordsTextSize);
      ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);

      // mSlideshowHandler.post(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
      mSlideshowHandler.removeCallbacks(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowIsRunning = false;
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);
      findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);
      findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
      ((TextView) findViewById(R.id.ui_press_meTextView)).setText(R.string.string_choose2word);
    }
  };
  boolean fileschecked = false;
  private boolean screenOrientationNormal;
  private org.tflsh.nosedive.GrabFilesBackgroundTask mBackgroundFilesGrabber;
  private org.tflsh.nosedive.SlideshowFragment mSlideshowFragment;

  @Override
  protected void onStop() {
    super.onStop();
    mSlideshowIsRunning = false;
    mSlideshowHandler.removeCallbacks(showNextRunnable);
    mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
    Log.d(TAG, "Activity.onStop()");
    //done in pause    unregisterReceiver(intentReceiver);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //tools

  @Override
  protected void onPause() {
    super.onPause();
    mSlideshowHandler.removeCallbacks(showNextRunnable);
    mSlideshowIsRunning = false;
    Log.d(TAG, "Activity.onPause()");
    unregisterReceiver(intentReceiver);
  }

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
        case "dlReceived":
          Log.d(TAG, "intentReceiver got action dl received");
          //mDlProgressBar.setVisibility(View.VISIBLE);
          ((Button) findViewById(R.id.checkFilesButton)).setText("Téléchargement en cours de  "
              + ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).getProgress()
              + " fichiers");

          ((ProgressBar) findViewById(R.id.ui_missing_ProgressBar)).incrementProgressBy(-1);
          ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementProgressBy(1);
          //((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(R.string.string_wait4dl);
          break;
        case "dlComplete":
          Log.d(TAG, "intentReceiver got action dl complete");
          (findViewById(R.id.button2)).setBackgroundColor(
              getResources().getColor(R.color.OurPink, null));

          fileschecked = true;
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "Tous les fichiers sont OK!");
          findViewById(R.id.ui_dl_ProgressBar).setBackground(
              getDrawable(R.drawable.ic_not_started_black));
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
         /* View iframe=findViewById(R.id.slideshowLayout   );

          ViewGroup parent = (ViewGroup) iframe.getParent();
          int index=parent.indexOfChild(iframe);
          parent.removeView(iframe);
          //inflate
          View dlLayout=getLayoutInflater().inflate(R.layout.slideshow,parent,false);
          parent.addView(dlLayout,index);
//       !   setContentView(dlLayout);
//!! makeButtons();
  //    !    makeImageClickable();

          mSlideshowIsRunning = false;
          missingFilesNumber = 0;
missingFilesNames.clear();
     //!     mSlideshowHandler.post(mStartSlideshowRunnable);

          */
          break;

        case "noJson":
          mHaveInternet = false;
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              R.string.pleaseRestartWithInternet);

          break;
        case "JSONok":
          Log.d(TAG, "intentReceiver got JSONok");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText("Mise a jour OK");

          //       (findViewById(R.id.button5)).setBackgroundColor(getResources().getColor(R.color.OurPink,null));
          break;
        case "JSONlocalonly":
          Log.d(TAG, "intentReceiver got JSONlocalonly");
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "Pas internet mais liste fichiers ok");

          //   (findViewById(R.id.button5)).setBackgroundColor(getResources().getColor(R.color.OurPink,null));
          break;
        case "filesFound":
          String max = intent.getStringExtra(EXTRA_MESSAGE);
          mSlideshowFilesName.add(max);
          Log.d(TAG, "intentReceiver got action files found " + max);
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);

          //   ((ProgressBar)findViewById(R.id.ui_dl_ProgressBar)).setProgress(0);

          //((ProgressBar)findViewById(R.id.ui_dl_ProgressBar)).setMax(134);

              ((ProgressBar)findViewById(R.id.ui_dl_ProgressBar)).setProgress(mSlideshowFilesName.size());

/*
          try {
            Thread.sleep(200);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
*/
          break;
        //TODO:
        case "filesAllOk":
          (findViewById(R.id.checkFilesButton)).setBackgroundColor(
              getResources().getColor(R.color.OurPink, null));
          if (missingFilesNames.isEmpty()) {
            fileschecked = true;
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                "Tous les fichiers sont OK!");
            findViewById(R.id.ui_dl_ProgressBar).setBackground(
                getDrawable(R.drawable.ic_not_started_black));
            findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
            /*start button*/
            (findViewById(R.id.ui_dl_ProgressBar)).setOnTouchListener(new OnTouchListener() {

              @Override
              public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                  fileschecked = true;
                  //!!!loadSlideshowLayout();
                  loadSlideshowFragment();
                  //*/

                }
                return true;
              }
            });
            //////////
          } else {
            ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
                "il manque " + missingFilesNames.size() + " fichiers");
            findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
          }

          break;
        case "filesMissing":
          Log.d(TAG, "intentReceiver got action files missing");
          String max5 = intent.getStringExtra(EXTRA_MESSAGE);
          missingFilesNames.add(max5);
          findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
          findViewById(R.id.button2).setVisibility(View.VISIBLE);
          ((ProgressBar) findViewById(R.id.ui_missing_ProgressBar)).setProgress(
              missingFilesNames.size());
          ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setSecondaryProgress(
              missingFilesNames.size());
          Log.d(TAG, "intentReceiver set progress bar " + missingFilesNames.size() + max5);
          ((TextView) findViewById(R.id.ui_dl_progressTextView)).setText(
              "il manque " + missingFilesNames.size() + " fichiers");

          break;
        case "imgShown":
          //should be never called (cause it bug, and a bitmap is too heavy for intent)
          String b = intent.getStringExtra(EXTRA_MESSAGE);
          Log.d(TAG, Objects.requireNonNull(b));
          byte[] btArray = intent.getByteArrayExtra(EXTRA_MESSAGE);
          mImageView.setImageBitmap(
              BitmapFactory.decodeByteArray(btArray, 0, Objects.requireNonNull(btArray).length));
          break;

        default:
          Log.e(TAG, "intentReceiver got unknown action");

          break;
      }
    }
  };

  @Override
  public void onSaveInstanceState(@NotNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean("allfileschecked", fileschecked);
  }

  void initScreenMetrics() {
    screenMetrics = new DisplayMetrics();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Objects.requireNonNull(getDisplay()).getRealMetrics(screenMetrics);
    } else {
      getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);
    }

    //en dp
    pressMeTextSize = 48;
    pressTwoWordsTextSize = 32;
    buttonTextSize = 24;
    //marge intérieure: entre le texte et la bordure du cadre (inversé si tablette en paysage)
    buttonVerticalPadding = 15;
    buttonHorizontalPadding = 20;

    buttonVerticalMargin = 18;
    buttonHorizontalMargin = 20;

    float screenDPI = screenMetrics.densityDpi;
    float screenDensity = screenMetrics.scaledDensity;

    //in pixel

    buttonVerticalPadding *= screenDensity;
    buttonHorizontalPadding *= screenDensity;

    buttonVerticalMargin *= screenDensity;
    buttonHorizontalMargin *= screenDensity;
    buttonTextSize *= screenDensity;

    pressMeTextSize *= screenDensity;

    pressTwoWordsTextSize *= screenDensity;
    Log.d("dpi", "metrics returned DPI " + (int) (screenDPI / 160) + " density " + screenDensity);

    screenOrientationNormal = false;

    screenWidth = screenMetrics.widthPixels;
    screenHeight = screenMetrics.heightPixels;
    //en dp
    if (screenHeight > screenWidth) {
      screenOrientationNormal = true;
    }
    Log.d(TAG, "default screen width= " + screenWidth);
    Log.d(TAG, "default screen height= " + screenHeight);
  }

  public void logScreenMetrics() {
    switch (screenMetrics.densityDpi) {

      case DisplayMetrics.DENSITY_LOW:
        Log.d("dpi", "metrics returned DPI LOW");
        break;
      case DisplayMetrics.DENSITY_TV:
        Log.d("dpi", "metrics returned DPI TV");
        break;

      case DisplayMetrics.DENSITY_MEDIUM:
        Log.d("dpi", "metrics returned DPI MEDIUM");
        break;

      case DisplayMetrics.DENSITY_HIGH:
        Log.d("dpi", "metrics returned DPI HIGH");
        break;

      case DisplayMetrics.DENSITY_XHIGH:
        Log.d("dpi", "metrics returned DPI XHIGH");
        break;
      case DisplayMetrics.DENSITY_XXHIGH:
        Log.d("dpi", "metrics returned DPI XXHIGH");
        break;
      case DisplayMetrics.DENSITY_XXXHIGH:
        Log.d("dpi", "metrics returned DPI XXXHIGH");
        break;
      default:
        Log.e("dpi", "metrics returned DPI UNKNOWN" + screenMetrics.density);
        Log.e("dpi", "metrics returned DPI UNKNOWN" + screenMetrics.densityDpi);
        break;
    }
  }


  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bundle = savedInstanceState;
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");
      setContentView(R.layout.activity_fullscreen);
    if (savedInstanceState != null) {

      fileschecked = savedInstanceState.getBoolean("allfileschecked");
      Log.d(TAG, " fileschecked=" + fileschecked);
    }
    /************

    View iframe=findViewById(R.id.motherLayout);
    ViewGroup parent = (ViewGroup) iframe.getParent();
    int index=parent.indexOfChild(iframe);
    parent.removeView(iframe);
    //inflate
    View dlLayout=getLayoutInflater().inflate(R.layout.activity_dlfullscreen,parent,false);
    parent.addView(dlLayout,index);
     ********/

    initScreenMetrics();
    filter = new IntentFilter("dlReceived");
    filter.addAction("dlStarted");
    filter.addAction("dlComplete");
    filter.addAction("filesFound");
    filter.addAction("filesAllOk");
    filter.addAction("filesMissing");
    filter.addAction("noJson");
    filter.addAction("JSONok");
    filter.addAction("JSONlocalonly");

    filter.addAction("imgShown");
    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };        //end try lru

    executor = Executors.newFixedThreadPool(1);


    Log.d("activity", "onCreate" + getIntent());
  }

  @Override
  public void onConfigurationChanged(@NotNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.d(TAG, "onConfigurationChanged" + getIntent());
   onResume();
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver(intentReceiver, filter);
    mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY);

    if (!fileschecked) {
      Log.d(TAG, "onResume: no bundle" + getIntent());
      //setScreenMetrics();
      mAsyncTaskManager =
          new AsyncTaskManager(getApplicationContext(), screenWidth, screenHeight, memoryCache,
              executor);
      mBackgroundFilesGrabber =          new org.tflsh.nosedive.GrabFilesBackgroundTask(getApplicationContext());
      mSlideshowFragment =          new org.tflsh.nosedive.SlideshowFragment();

  /*  View iframe=findViewById(R.id.motherLayout);

    ViewGroup parent = (ViewGroup) iframe.getParent();
    int index=parent.indexOfChild(iframe);
    parent.removeView(iframe);
    //inflate
    View dlLayout=getLayoutInflater().inflate(R.layout.slideshow,parent,false);
    parent.addView(dlLayout,index);
//setContentView(dlLayout);
    makeButtons();
*/
      android.app.ActionBar actionBar = getActionBar();

      if (actionBar != null) {
        actionBar.hide();
      }

      this.mSlideshowFilesName = new ArrayList<>();
      pwa = 0;
      this.missingFilesNames = new ArrayList<>();
      this.missingFilesNumber = 0;
      // startActivity(new Intent(this, AsyncTaskManager.ListImageTask.class).pu);

      mSlideshowIsRunning = false;

      //REGLAGE DE LAPPLI
      //end runnables list
      LinearLayout lm = findViewById(R.id.leftMenuLinearLayout);
      LinearLayout rm = findViewById(R.id.rightMenuLinearLayout);

      Log.d(TAG,
          "ListImageTask missing file after 5second and an intent? = " + missingFilesNames.size());
/*
    ((ImageView)findViewById(R.id.imageView))/.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        Log.e("onLongClick", "LONG PRESS");

        mHideHandler.post(mSetFullscreenOnRunnable);
        return true;
      }
    });
*/


    /*update json (verifier l'appli )button
    ((Button)findViewById(R.id.button5)).setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

          (findViewById(R.id.button5)).setBackgroundColor(getResources().getColor(R.color.OurWhite,null));
          (findViewById(R.id.button2)).setBackgroundColor(getResources().getColor(R.color.OurWhite,null));
          (findViewById(R.id.button)).setBackgroundColor(getResources().getColor(R.color.OurWhite,null));
          ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setProgress(0);

          new Thread(new Runnable() {
            @Override public void run() {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              org.tflsh.nosedive.AsyncTaskManager.ListImageTask.grabJson(M_SERVER_DIRECTORY_URL);

            }
          }).start();
        }
        return true;
      }
    });
  */  //////////

      /*check files button*/
      findViewById(R.id.checkFilesButton).setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            ((Button)view).setClickable(false);
            ((Button)view).setEnabled(false);
      //      (findViewById(R.id.button)).setBackgroundColor(
         //       getResources().getColor(R.color.OurWhite, null));
            //          (findViewById(R.id.button5)).setBackgroundColor(getResources().getColor(R.color.OurWhite,null));
           // (findViewById(R.id.button2)).setBackgroundColor(
             //   getResources().getColor(R.color.OurWhite, null));
            ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setProgress(0);
            findViewById(R.id.button2).setVisibility(View.GONE);

            new Thread(new Runnable() {
              @Override public void run() {
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                missingFilesNames.clear();

                mSlideshowFilesName.clear();
               mBackgroundFilesGrabber.exec(M_SERVER_DIRECTORY_URL);
              }
            }).start();
          }
          return true;
        }
      });



      /*repair files button*/
      findViewById(R.id.button2).setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            (findViewById(R.id.button2)).setBackgroundColor(
                getResources().getColor(R.color.OurWhite, null));

            (findViewById(R.id.checkFilesButton)).setBackgroundColor(
                getResources().getColor(R.color.OurWhite, null));
            //   (findViewById(R.id.button5)).setBackgroundColor(getResources().getColor(R.color.OurWhite,null));

            new Thread(new Runnable() {
              @Override public void run() {

               mBackgroundFilesGrabber.repairfiles(
                    M_SERVER_DIRECTORY_URL, missingFilesNames);
              }
            }).start();
          }
          return true;
        }
      });
      //////////

      new Thread(new Runnable() {
        @Override public void run() {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          mBackgroundFilesGrabber.grabJson(M_SERVER_DIRECTORY_URL);
        }
      }).start();
    } else {
      Log.e("onResume", "files were already ALLOK");
      initScreenMetrics();

      if (findViewById(R.id.imageView) == null) {
        Log.e("onResume", "loading fragment");
//!!!loadSlideshowLayout();
  loadSlideshowFragment();
      }
    }

  }

  private void loadSlideshowLayout() {
    View iframe = findViewById(R.id.motherLayout);

    ViewGroup parent = (ViewGroup) iframe.getParent();
    int index = parent.indexOfChild(iframe);
    parent.removeView(iframe);
    //inflate
    View dlLayout = getLayoutInflater().inflate(R.layout.slideshow, parent, false);
    parent.addView(dlLayout, index);
    //  setContentView(dlLayout);
    makeButtons();
    makeImageClickable();
    mSlideshowHandler.post(mStartSlideshowRunnable);

  }

  private void loadSlideshowFragment() {


    Log.d(TAG, "loadSlideshowFragment() removing startup screen layout");


    View iframe = findViewById(R.id.buttons);

    ViewGroup parent = (ViewGroup) iframe.getParent();
    int index = parent.indexOfChild(iframe);
    parent.removeView(iframe);

    Log.d(TAG, "loadSlideshowFragment()");


    Bundle args = new Bundle();
    args.putStringArrayList("SlideshowFilenames", mSlideshowFilesName);
    mSlideshowFragment.setArguments(args);













    FragmentManager manager = getFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(R.id.motherLayout,mSlideshowFragment,"MULTIFACETTE_SLIDESHOW");
    transaction.addToBackStack(null);
    transaction.commit();
    //mSlideshowHandler.post(mStartSlideshowRunnable);

  }


  private void makeImageClickable() {
    Log.d(TAG, "makeImageClickable(): image is now clickable");
    findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        mSlideshowHandler.removeCallbacks(showNextRunnable);
        mHideHandler.postDelayed(showMenuRunnable, UI_ANIMATION_DELAY);
      }
    });
    findViewById(R.id.imageView).setClickable(true);



  }

  void makeButtons() {
    String[] buttonNames = {
        getResources().getString(R.string.buttonLabel_smart)
        , getResources().getString(R.string.buttonLabel_lovely)
        , getResources().getString(R.string.buttonLabel_fun)
        , getResources().getString(R.string.buttonLabel_sportive)
        , getResources().getString(R.string.buttonLabel_natural)
        , getResources().getString(R.string.buttonLabel_sexy)
        , getResources().getString(R.string.buttonLabel_surprising)
        , getResources().getString(R.string.buttonLabel_romantic)
        , getResources().getString(R.string.buttonLabel_pretty)
        , getResources().getString(R.string.buttonLabel_seductive)
        , getResources().getString(R.string.buttonLabel_hungry)
        , getResources().getString(R.string.buttonLabel_polite)
        , getResources().getString(R.string.buttonLabel_feminine)
        , getResources().getString(R.string.buttonLabel_tenderly)
        , getResources().getString(R.string.buttonLabel_kind)
        , getResources().getString(R.string.buttonLabel_strong)
    };

    this.mToggleButtonsArrayList = new ArrayList<>();
    //making ALL first

    for (String buttonName : buttonNames) {

      Button tempButton = new AppCompatButton(this) {
        @Override public boolean performClick() {
          super.performClick();
          return true;
        }
      };
      tempButton.setBackground(
          ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bouttonoff, null));
      tempButton.setText(buttonName);
      tempButton.setStateListAnimator(null);
      tempButton.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));

      tempButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonTextSize);
      tempButton.setAllCaps(true);
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      if (screenOrientationNormal) {
        layoutParams.setMargins(buttonVerticalMargin, buttonHorizontalMargin, buttonVerticalMargin,
            buttonHorizontalMargin);
        tempButton.setPadding(buttonHorizontalPadding, buttonVerticalPadding, buttonHorizontalPadding,
            buttonVerticalPadding);
      } else {
        layoutParams.setMargins(buttonHorizontalMargin, buttonVerticalMargin
            ,
            buttonHorizontalMargin,
            buttonVerticalMargin );
        tempButton.setPadding(buttonVerticalPadding, buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding);
      }

      tempButton.setLayoutParams(layoutParams);

      ////////////////////////////////CRITICAL//////////////////////////////////////
      tempButton.setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
          mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);
          mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
          mSlideshowHandler.removeCallbacks(showNextRunnable);
          mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);
          mSlideshowIsRunning = false;

          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCheckedToggleButtonsArrayList.add(((Button) view));
            //ne doit arriver que si vous avez des gros doigts ;)
            if (mCheckedToggleButtonsArrayList.size() > 2) {
              Log.d("mCheckedToggle", "3 Button pressed");
              mHideHandler.post(cleanButtonRunnable);

              mSlideshowHandler.post(mStartSlideshowRunnable);
            }
            Log.d("Pressed", "Button pressed");
          } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("Pressed", "Button released");
            if (mCheckedToggleButtonsArrayList.size() == 1) {
              view.setPressed(true);
              ((Button) view).setTextColor(Color.BLACK);
              view.setEnabled(false);
              Log.d("toggleClick", "toggle 1 buttons ok");
              view.performClick();
            } else if (mCheckedToggleButtonsArrayList.size() == 2) {
              view.performClick();
              //deux boutons sont préssés
              view.setPressed(true);
              view.setEnabled(false);
              mCheckedToggleButtonsArrayList.add(((Button) view));
              ((Button) view).setTextColor(Color.BLACK);

              Log.d("toggleClick", "toggle 2 buttons ok");
              mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);

              mSlideshowHandler.post(mShowImageAfterTwoWordsRunnable);
            }
            view.performClick();
          }

          return false;
        }
      });

      //avoid a glitch reloading button
      tempButton.setClickable(true);

      mToggleButtonsArrayList.add(tempButton);
    }

    int j;
    //for the first 8 button, set in the left menu layout
    for (j = 0; j < buttonNames.length / 2; j++) {

      ((LinearLayout) findViewById(R.id.leftMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }

    //for the first 8 buttons, set in the right menu layout

    for (; j < buttonNames.length; j++) {

      ((LinearLayout) findViewById(R.id.rightMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }
  }

/*
  enum appStatus {
    SUMMING, DL_IN_PROGRESS, PLAYING, MENU, GUESSING,
  }

  enum appFlag {
    GOT_ONE_SUM, CHECKSUM_COMPLETE, GOT_ONE_DL, DL_COMPLETE,
  }
*/
}