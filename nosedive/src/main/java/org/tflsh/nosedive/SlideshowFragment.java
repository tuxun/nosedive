package org.tflsh.nosedive;

import android.annotation.SuppressLint;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SlideshowFragment extends Fragment {

  public SlideshowFragment ()
  {
    mSlideshowFilesName=new ArrayList<String>();
  }
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

      Activity activity = getActivity();
      if (activity != null
          && activity.getWindow() != null) {
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
      }
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.hide();
      }
    }
  };
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };
  private View mContentView;
  private View mControlsView;
  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed display of UI elements
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.show();
      }
      mControlsView.setVisibility(View.VISIBLE);
    }
  };

  private int buttonTextSize;
  private int pressMeTextSize;
  private int pressTwoWordsTextSize;
  private ImageView mImageView;
  private int pwa = 0;
  private TextView pressMeTextView;

  //ne devrait plus exister?/devenir un showimagetask seulement
  AsyncTaskManager mAsyncTaskManager;

  private int buttonVerticalPadding;
  private int buttonHorizontalPadding;
  private int buttonVerticalMargin;
  private int buttonHorizontalMargin;
  private boolean mVisible;
  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };
  private LruCache<String, Bitmap> memoryCache;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_slideshow, container, false);
  }
//quand on crée le fragment, on commence forcemment par une image du slideshow
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");

    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };

    mSlideshowFilesName=getArguments().getStringArrayList("SlideshowFilenames");
    mVisible = true;
    initScreenMetrics();
    executor = Executors.newFixedThreadPool(1);
    mAsyncTaskManager =
        new AsyncTaskManager(getContext(), screenWidth, screenHeight, memoryCache,
            executor);
    mControlsView = view.findViewById(R.id.fullscreen_content_controls);
    mContentView = view.findViewById(R.id.fullscreen_content);

    // Set up the user interaction to manually show or hide the system UI.
    mContentView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggle();
      }
    });

    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    view.findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    makeButtons();
    makeImageClickable();

    mSlideshowHandler.post(mStartSlideshowRunnable);


  }

  int screenWidth;
  int screenHeight;
  DisplayMetrics screenMetrics;
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

      Button tempButton = new AppCompatButton(getContext()) {
        @Override public boolean performClick() {
          super.performClick();
          return true;
        }
      };
      tempButton.setBackground(
          ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bouttonoff, null));
      tempButton.setText(buttonName);
      tempButton.setStateListAnimator(null);
      tempButton.setTypeface(ResourcesCompat.getFont(getContext(), R.font.alef));

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
      tempButton.setOnTouchListener(new View.OnTouchListener() {

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

      ((LinearLayout) mContentView.findViewById(R.id.leftMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }

    //for the first 8 buttons, set in the right menu layout

    for (; j < buttonNames.length; j++) {

      ((LinearLayout) mContentView.findViewById(R.id.rightMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }
  }
  ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
  private final Handler mSlideshowHandler = new Handler();

  private void makeImageClickable() {
    Log.d(TAG, "makeImageClickable(): image is now clickable");
    mContentView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        mSlideshowHandler.removeCallbacks(showNextRunnable);
        mHideHandler.postDelayed(showMenuRunnable, UI_ANIMATION_DELAY);
      }
    });
    mContentView.findViewById(R.id.imageView).setClickable(true);



  }


  static final int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  static final int DELAY_GUESSING_SETTING = 5000;
  static final String M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "julia/";
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  static final int DELAY_CHOICE_WORDS_SETTING = 10000;
  private static final String TAG = "SlideshowActivity";
  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();


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
                  (ImageView) mContentView.findViewById(R.id.imageView),
                  DELAY_INTER_FRAME_SETTING,
                  getContext().getCacheDir() + "/" + mSlideshowFilesName.get(nextImageToShowIndex))
          );
          ((TextView)mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurWhite));
        } else {

          executor.execute(
              mAsyncTaskManager.new ShowImageTask(
                  ((ImageView)mContentView.findViewById(R.id.imageView)),
                  DELAY_INTER_FRAME_SETTING,
                  getContext().getCacheDir() + "/" + mSlideshowFilesName.get(nextImageToShowIndex))
          );
          ((TextView)mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurPink));
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

      mContentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mContentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mContentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);
    }
  };
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {
    @Override
    public void run() {
      Log.d(TAG, "mShowImageAfterTwoWordsRunnable");
      ((TextView)mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurWhite));
      mSlideshowHandler.postDelayed(cleanButtonRunnable, UI_ANIMATION_DELAY);
      mSlideshowHandler.postDelayed(mHideMenuRunnable, DELAY_INTER_FRAME_SETTING);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
/*
      mImageView.setImageDrawable(
          ResourcesCompat.getDrawable(getResources(), R.drawable.whitebackground, null));
*/
      mSlideshowHandler.post(
          mAsyncTaskManager.new ShowImageTask(((ImageView)mContentView.findViewById(R.id.imageView)), DELAY_INTER_FRAME_SETTING,
              getContext().getCacheDir() + "/" + mSlideshowFilesName.get(
                  new Random().nextInt(mSlideshowFilesName.size()))));

      mHideHandler.post(cleanButtonRunnable);

      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_GUESSING_SETTING);
    }
  };


  private ArrayList<String> mSlideshowFilesName;
  private ArrayList<String> missingFilesNames;

  private boolean screenOrientationNormal;
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {

      Log.d(TAG, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesName.size());
      mContentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

      mContentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mContentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);

      if ((!mSlideshowFilesName.isEmpty())) {
        mSlideshowHandler.removeCallbacks(showNextRunnable);
        //      mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);

        makeImageClickable();
        //hummm
        mHideHandler.post(cleanButtonRunnable);

        ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTypeface(
            ResourcesCompat.getFont(getContext(), R.font.alef));
        ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);
        ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setText(
            getResources().getString(R.string.string_press_me));

        ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_SP,
            pressMeTextSize);

        (mContentView.findViewById(R.id.ui_press_meTextView)).setVisibility(View.VISIBLE);
        ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurPink));
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
  void initScreenMetrics() {
    screenMetrics = new DisplayMetrics();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Objects.requireNonNull(getView().getDisplay()).getRealMetrics(screenMetrics);
    } else {
      getActivity().getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);
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

  private final Runnable showMenuRunnable = new Runnable() {
    @Override
    public void run() {

      Log.d(TAG, "makeImageNotClickable(): image isshowMenuRunnable not clickable anymore");
      mContentView.findViewById(R.id.imageView).setClickable(false);

      Log.d(TAG, "showMenuRunnable");
      ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTypeface(
          ResourcesCompat.getFont(getContext(), R.font.alef));
      ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_SP,
          pressTwoWordsTextSize);
      ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);

      // mSlideshowHandler.post(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
      mSlideshowHandler.removeCallbacks(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowIsRunning = false;
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);
      mContentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);
      mContentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      mContentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
      ((TextView) mContentView.findViewById(R.id.ui_press_meTextView)).setText(R.string.string_choose2word);
    }
  };
  @Override
  public void onResume() {
    super.onResume();
    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }

  @Override
  public void onPause() {
    super.onPause();
    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      // Clear the systemUiVisibility flag
      getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
    }
    show();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mContentView = null;
    mControlsView = null;
  }

  private void toggle() {
    if (mVisible) {
      hide();
    } else {
      show();
    }
  }

  private void hide() {
    // Hide UI first
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    mControlsView.setVisibility(View.GONE);
    mVisible = false;

    // Schedule a runnable to remove the status and navigation bar after a delay
    mHideHandler.removeCallbacks(mShowPart2Runnable);
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  @SuppressLint("InlinedApi")
  private void show() {
    // Show the system bar
    mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    mVisible = true;

    // Schedule a runnable to display UI elements after a delay
    mHideHandler.removeCallbacks(mHidePart2Runnable);
    mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  }

  /**
   * Schedules a call to hide() in delay milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  @Nullable
  private ActionBar getSupportActionBar() {
    ActionBar actionBar = null;
    if (getActivity() instanceof AppCompatActivity) {
      AppCompatActivity activity = (AppCompatActivity) getActivity();
      actionBar = activity.getSupportActionBar();
    }
    return actionBar;
  }
}