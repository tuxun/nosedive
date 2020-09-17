package org.tflsh.nosedive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
  float screenHeight;
  ArrayList<String> missingFilesNames;
  IntentFilter filter;
  int missingFilesNumber = 0;
  AsyncTaskManager asm;
  float screenWidth;
  DisplayMetrics screenMetrics;
  private LinearLayout centralLinearLayout;
  private ProgressBar mDlProgressBar;
  private TextView mDLProgressText;
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
      findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mDlProgressBar.setVisibility(View.GONE);
      mDLProgressText.setVisibility(View.GONE);
      //exitfullscreen getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      getWindow().getDecorView()
          .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

      findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
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
  private final Runnable mShowPressMeTextViewRunnable = new Runnable() {
    @Override
    public void run() {
      pressMeTextView.setVisibility(View.VISIBLE);
    }
  };
  private int buttonVerticalPadding;
  private int buttonHorizontalPadding;
  private int buttonVerticalMargin;
  private int buttonHorizontalMargin;
  private ArrayList<String> mSlideshowFilesNames;
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {
    @Override
    public void run() {
      Log.d(TAG, "mShowImageAfterTwoWordsRunnable");
      pressMeTextView.setVisibility(View.GONE);
      mSlideshowHandler.postDelayed(cleanButtonRunnable, UI_ANIMATION_DELAY);
      mSlideshowHandler.postDelayed(mHideMenuRunnable, DELAY_INTER_FRAME_SETTING);

      mImageView.setImageDrawable(
          ResourcesCompat.getDrawable(getResources(), R.drawable.whitebackground, null));

      mSlideshowHandler.post(asm.new ShowImageTask(mImageView, DELAY_INTER_FRAME_SETTING,
          getCacheDir() + "/" + mSlideshowFilesNames.get(
              new Random().nextInt(mSlideshowFilesNames.size()))));
      SystemClock.sleep(50);

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
      centralLinearLayout.setVisibility(View.VISIBLE);
    }
  };
  ////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////RUNNABLES////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////
  //used for show words?.
  private final Runnable showMenuRunnable = new Runnable() {
    @Override
    public void run() {

      Log.d(TAG, "makeImageNotClickable(): image is not clickable anymore");
      mImageView.setClickable(false);

      Log.d(TAG, "showMenuRunnable");
      pressMeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, pressTwoWordsTextSize);
      pressMeTextView.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));

      // mSlideshowHandler.post(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
      mSlideshowHandler.removeCallbacks(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowIsRunning = false;
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);
      centralLinearLayout.setVisibility(View.GONE);
      findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
      pressMeTextView.setText(R.string.string_choose2word);
      pressMeTextView.setTextColor(Color.BLACK);
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
      mHideHandler.post(mShowPressMeTextViewRunnable);

      int nextImageToShowIndex = new Random().nextInt(mSlideshowFilesNames.size());
      if (mSlideshowIsRunning) {
        if (pwa < mSlideshowFilesNames.size()) {
          if ((pwa % 2) == 1) {

            executor.execute(
                asm.new ShowImageTask(
                    mImageView,
                    DELAY_INTER_FRAME_SETTING,
                    getCacheDir() + "/" + mSlideshowFilesNames.get(nextImageToShowIndex))
            );
            pressMeTextView.setTextColor(getColor(R.color.OurPink));
          } else {

            executor.execute(
                asm.new ShowImageTask(
                    mImageView,
                    DELAY_INTER_FRAME_SETTING,
                    getCacheDir() + "/" + mSlideshowFilesNames.get(nextImageToShowIndex))
            );
            pressMeTextView.setTextColor(getColor(R.color.OurWhite));
          }

          pwa++;
        } else {
          Log.d(TAG, "mShowNextRunnable: no more images, restarting slideshow");
          pwa = 0;
          mSlideshowIsRunning = false;
          mSlideshowHandler.post(mStartSlideshowRunnable); //end handlepostdelay
        }
      }
    }

    // Code here will run in UI thread
  };
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////ACTIVITY (MAIN)/////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {
      mSlideshowHandler.removeCallbacks(showNextRunnable);
      mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);
      makeImageClickable();
      mHideHandler.post(cleanButtonRunnable);
      pressMeTextView
          .setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));
      pressMeTextView.setTextSize(
          TypedValue.COMPLEX_UNIT_SP, pressMeTextSize);
      pressMeTextView.setTextColor(getColor(R.color.OurPink));
      //findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      //findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      pressMeTextView.setText(getResources().getString(R.string.string_press_me));
      centralLinearLayout.setVisibility(View.VISIBLE);

      if (!mSlideshowIsRunning) {

        mSlideshowIsRunning = true;
        Log.d(TAG, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesNames.size());

        pwa = 0;
        long i;
        for (i = 0; i < mSlideshowFilesNames.size() + 1; i++) {
          mSlideshowHandler.postDelayed(showNextRunnable, i * DELAY_INTER_FRAME_SETTING);
        }
      } else {
        Log.e(TAG, "mStartSlideshowRunnable tried to start twice" + mSlideshowFilesNames.size());
      }
    }
  };
  public final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (Objects.requireNonNull(intent.getAction())) {

        case "dlStarted":
          Log.d(TAG, "intentReceiver got action dl started");
          mDlProgressBar.incrementSecondaryProgressBy(1);
          mDLProgressText.setVisibility(View.VISIBLE);
          mDlProgressBar.setVisibility(View.VISIBLE);

          missingFilesNumber++;

          break;
        case "dlReceived":
          Log.d(TAG, "intentReceiver got action dl received");
          mDlProgressBar.incrementProgressBy(1);
          pressMeTextView.setText(R.string.string_wait4dl);
          break;
        case "dlComplete":
          Log.d(TAG, "intentReceiver got action dl complete");
          makeImageClickable();

          mSlideshowIsRunning = false;
          missingFilesNumber = 0;
          mDLProgressText.setVisibility(View.GONE);
          mDlProgressBar.setVisibility(View.GONE);

          mSlideshowHandler.post(mStartSlideshowRunnable);
          break;

        case "noJson":
          mHaveInternet = false;
          pressMeTextView.setText(R.string.pleaseRestartWithInternet);

          break;

        case "filesFound":
          Log.d(TAG, "intentReceiver got action files found");
          int max = intent.getIntExtra(EXTRA_MESSAGE, 0);
          if (max > 0) {
            mDlProgressBar.setMax(max);
            Log.d(TAG, "intentReceiver set progress bar " + max);
            mDlProgressBar.setVisibility(View.VISIBLE);
          } else {
            Log.d(TAG, "intentReceiver did not set progress bar to" + max);
            makeImageClickable();
            mSlideshowIsRunning = false;
            mSlideshowHandler.post(mStartSlideshowRunnable);
          }
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
  private boolean screenOrientationNormal;

  @Override
  protected void onStop() {
    super.onStop();
    mSlideshowIsRunning = false;
    mSlideshowHandler.removeCallbacks(showNextRunnable);
    mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
    Log.d(TAG, "Activity.onStop()");
    unregisterReceiver(intentReceiver);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //tools

  @Override
  protected void onPause() {
    super.onPause();

    Log.d(TAG, "Activity.onPause()");
  }

  @Override
  public void onSaveInstanceState(@NotNull Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  void setScreenMetrics() {
    screenMetrics = new DisplayMetrics();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Objects.requireNonNull(getDisplay()).getRealMetrics(screenMetrics);
    } else {
      getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);
    }

    boolean highDPI;
    float screenDPI = screenMetrics.densityDpi;
    float screenDensity = screenMetrics.scaledDensity;

    screenWidth = screenMetrics.widthPixels * screenDensity / 2;
    screenHeight = screenMetrics.heightPixels * screenDensity / 2;
    //en dp
    pressMeTextSize = 48;
    pressTwoWordsTextSize = 32;
    buttonTextSize = 20;

    buttonVerticalPadding = 3;
    buttonHorizontalPadding = 5;

    buttonVerticalMargin = 20;
    buttonHorizontalMargin = 30;

    //in pixel
    buttonVerticalPadding *= screenDensity / 160;
    buttonHorizontalPadding *= screenDensity / 160;

    buttonVerticalMargin *= screenDensity;
    buttonHorizontalMargin *= screenDensity;
    buttonTextSize *= screenDensity;

    pressMeTextSize *= screenDensity;

    pressTwoWordsTextSize *= screenDensity / 160;

    Log.d("dpi", "metrics returned DPI " + (int) (screenDPI / 160) + " density " + screenDensity);

    screenOrientationNormal = false;
    if (screenHeight > screenWidth) {

      Log.d("dpi", "we loaded activity fullscreen layout");
      setContentView(R.layout.activity_fullscreen);
      screenOrientationNormal = true;
    } else {
      Log.d("dpi", "we loaded activity fullscreen  landscape layout");
      setContentView(R.layout.activity_fullscreen_landscape);
    }

    makeButtons();

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

    if (savedInstanceState == null) {
      final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
      // Use 1/8th of the available memory for this memory cache.
      Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");

      memoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
          // The cache size will be measured in kilobytes rather than
          // number of items.
          return bitmap.getByteCount() / 1024;
        }
      };        //end try lru
    } else {
      Log.d(TAG, "we are RESTARTING");
    }

    filter = new IntentFilter("dlReceived");
    filter.addAction("dlStarted");
    filter.addAction("dlComplete");
    filter.addAction("filesFound");
    filter.addAction("filesMissing");
    filter.addAction("noJson");
    filter.addAction("imgShown");
    executor = Executors.newFixedThreadPool(1);

    registerReceiver(intentReceiver, filter);
    setScreenMetrics();

    asm = new AsyncTaskManager(getApplicationContext(), screenWidth, screenHeight, memoryCache,
        executor);

    Log.d("activity", "onCreate" + getIntent());
    android.app.ActionBar actionBar = getActionBar();

    if (actionBar != null) {
      actionBar.hide();
    }

    this.mSlideshowFilesNames = new ArrayList<>();
    pwa = 0;
    this.missingFilesNames = new ArrayList<>();
    this.missingFilesNumber = 0;
    this.mHaveInternet = isInternetOk();
    // startActivity(new Intent(this, AsyncTaskManager.ListImageTask.class).pu);
    org.tflsh.nosedive.AsyncTaskManager.ListImageTask.exec(missingFilesNames, mSlideshowFilesNames,
        M_SERVER_DIRECTORY_URL);
  }

  @Override
  public void onConfigurationChanged(@NotNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    this.onResume();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
/*
    if ((missingFilesNames.isEmpty()) && (!mSlideshowFilesNames.isEmpty())) {
      mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY - 10);
    }
*/
    Log.d(TAG, "onConfigurationChanged" + getIntent());
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume" + getIntent());

    setScreenMetrics();

    //init GUI parts
    this.pressMeTextView = findViewById(R.id.ui_press_meTextView);
    this.centralLinearLayout = findViewById(R.id.ui_centralLinearLayout);
    this.mImageView = findViewById(R.id.imageView);
    this.mImageView = findViewById(R.id.imageView);

    mDlProgressBar = findViewById(R.id.ui_dl_ProgressBar);
    mDLProgressText = findViewById(R.id.ui_dl_progressTextView);

    //REGLAGE DE LAPPLI
    //end runnables list
    LinearLayout lm = findViewById(R.id.leftMenuLinearLayout);
    LinearLayout rm = findViewById(R.id.rightMenuLinearLayout);

    mDlProgressBar.setIndeterminate(false);

    Log.d(TAG,
        "ListImageTask missing file after 5second and an intent? = " + missingFilesNames.size());

    mImageView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        Log.e("onLongClick", "LONG PRESS");

        mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY - 10);
        return true;
      }
    });

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if ((missingFilesNames.isEmpty()) && (!mSlideshowFilesNames.isEmpty())) {
      mHideHandler.postDelayed(mSetFullscreenOnRunnable, UI_ANIMATION_DELAY);
    } else {
      if (this.mHaveInternet) {
        pressMeTextView.setText(R.string.string_wait4dl);
      } else {
        pressMeTextView.setText(R.string.pleaseRestartWithInternet);
      }
    }
  }

  private void makeImageClickable() {
    Log.d(TAG, "makeImageClickable(): image is now clickable");
    mImageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        mSlideshowHandler.removeCallbacks(showNextRunnable);
        mHideHandler.postDelayed(showMenuRunnable, UI_ANIMATION_DELAY);
      }
    });
    mImageView.setClickable(true);
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
        tempButton.setPadding(buttonVerticalPadding, buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding);
      } else {
        layoutParams.setMargins(buttonHorizontalMargin, buttonVerticalMargin / 2,
            buttonHorizontalMargin,
            buttonVerticalMargin / 2);
        tempButton.setPadding(buttonHorizontalPadding, buttonVerticalPadding, buttonVerticalPadding,
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

              view.setPressed(true);
              view.setEnabled(false);
              mCheckedToggleButtonsArrayList.add(((Button) view));
              ((Button) view).setTextColor(Color.BLACK);
              mHideHandler.post(cleanButtonRunnable);

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

  private boolean isInternetOk() {
    //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Network activeNetwork = cm.getActiveNetwork();
      return Objects.requireNonNull(cm.getNetworkCapabilities(activeNetwork))
          .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    } else {
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
