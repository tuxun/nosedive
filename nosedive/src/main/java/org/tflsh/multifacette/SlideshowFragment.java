package org.tflsh.multifacette;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SlideshowFragment extends Fragment {
  private static final SlideshowFragment instance;

  static {
    instance = new SlideshowFragment();
  }


  public static SlideshowFragment getInstance() {
    return instance;
  }


  /*
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  /*
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final String CLASSNAME = "SlideshowFragment";
  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
  protected final Handler mSlideshowHandler = new Handler();
  private final Runnable mHidePart2Runnable = new Runnable() {
    @Override
    public void run() {

      int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

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

  private final Runnable blockMenuRunnable = new Runnable() {
    @Override
    public void run() {
      Log.e("blockMenuRunnable", "visible ?");

      //a chaque button cliqué, si on est perdu, on decheck les bouttons
      //should only  happen when 2 DIFFERENT buttons are pressed
      mSlideshowIsRunning = false;
      int clickedButtons = mToggleButtonsArrayList.size() - 1;
      Log.d(CLASSNAME, "blockMenuRunnable:" + clickedButtons + " blockMenuRunnable buttons");
      for (int i = clickedButtons; i >= 0; i--) {
        mToggleButtonsArrayList.get(i).setClickable(false);
      }
       }
  };

  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed display of UI elements
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.show();
      }
    }
  };
  File mCacheDirPath;
  int screenWidth;
  int screenHeight;
  DisplayMetrics screenMetrics;
  ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
  private final Runnable cleanButtonRunnable = new Runnable() {
    @Override
    public void run() {
      int clickedButtons = mToggleButtonsArrayList.size() - 1;
      Log.d(CLASSNAME, "cleanButtonRunnable:" + clickedButtons + " cleaned buttons");
      for (int i = clickedButtons; i >= 0; i--) {
        mToggleButtonsArrayList.get(i).setEnabled(true);
        mToggleButtonsArrayList.get(i).setClickable(true);
      }
      clickedButtons = mCheckedToggleButtonsArrayList.size();
      if (clickedButtons > 0) {
        for (int i = clickedButtons - 1; i >= 0; i--) {

          mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
        }
      }
    }
  };
  private int nextImageToShowIndex;
  private int buttonTextSize;
  private int pressMeTextSize;
  private int pressTwoWordsTextSize;
  private int pwa = 0;
  private int buttonVerticalPadding;
  private int buttonHorizontalPadding;
  private int buttonVerticalMargin;
  private int buttonHorizontalMargin;

  private boolean mSlideshowIsRunning = false;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
   int guessingDelay;
  private final Runnable mHideMenuRunnable = new Runnable() {
    @Override
    public void run() {
      Log.e("mHideMenuRunnable", "visible ?");
      mSlideshowIsRunning = false;
      requireView().findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

      requireView().findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      requireView().findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mSlideshowHandler.post(cleanButtonRunnable);
    }
  };
  private long mLastClickTime = 0;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
   int choiceDelay;
   int interFrameDelay;
  private long mLastMenuClickTime;
  boolean screenOrientationNormal=false;
  private List<String> mSlideshowFilesName;
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {

      Log.d(CLASSNAME, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesName.size());
      mSlideshowHandler.post(cleanButtonRunnable);

Log.d(CLASSNAME, "makeImageClickable(): image is now clickable");

    requireView().findViewById(R.id.slideshowLayout).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlideshowHandler.removeCallbacks(showMenuRunnable);


          requireView().findViewById(R.id.slideshowLayout).setClickable(false);

        showMenuRunnable.run();
      }
    });

    requireActivity().getWindow()
        .findViewById(R.id.slideshowScreenLinearSourceLayout)
        .setOnTouchListener(mDelayHideTouchListener);
    requireActivity().getWindow()
        .findViewById(R.id.slideshowScreenLinearSourceLayout)
        .setClickable(true);

      if ((!mSlideshowFilesName.isEmpty())) {
        mSlideshowHandler.removeCallbacks(showNextRunnable);

        if (!mSlideshowIsRunning) {
          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurWhite, requireActivity().getTheme()));

          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setText(
              getResources().getString(R.string.string_press_me));

          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextSize(
              TypedValue.COMPLEX_UNIT_DIP,
              pressMeTextSize);
          mSlideshowIsRunning = true;
          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setVisibility(View.VISIBLE);

          for (long i = 0; i < mSlideshowFilesName.size() + 1; i++) {
            mSlideshowHandler.postDelayed(showNextRunnable, i * interFrameDelay);
          }
        } else {
          Log.e(CLASSNAME,
              "mStartSlideshowRunnable tried to start twice" + mSlideshowFilesName.size());
          mSlideshowIsRunning = false;
        }
      } else {
        Log.e(CLASSNAME, "mSlideshowFilesName is empty" + mSlideshowFilesName.size());
      }

      mSlideshowHandler.post(mHideMenuRunnable);
      requireView().findViewById(R.id.ui_centralLinearLayout)
          .setVisibility(View.VISIBLE);
      requireView().findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      requireView().findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      requireView().findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
    }
  };
  /*
   * Runnable showMenuRunnable
   * replace the image from the slideshow, with menu
   */
  private final Runnable showMenuRunnable = new Runnable() {
    @Override
    public void run() {

      if (SystemClock.elapsedRealtime() - mLastMenuClickTime < 100) {
        return;
      }
      mLastMenuClickTime = SystemClock.elapsedRealtime();
      Log.d(CLASSNAME, "showMenuRunnable");
      mSlideshowIsRunning = false;
      //hide image

      requireView().findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);

      //clean pending runnable
      mSlideshowHandler.removeCallbacks(showMenuRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);

      //restarting slideshow if no touch on button is detected for "delay" microseconds
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, choiceDelay);

      //hide text, modify text, then show it again with menu
      requireView().findViewById(R.id.ui_press_meTextView).setVisibility(View.GONE);
      ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextSize(
          TypedValue.COMPLEX_UNIT_DIP, pressTwoWordsTextSize);
      ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurBlack,requireActivity().getTheme()));
      ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setText(
          R.string.string_choose2word);
      requireView().findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);

      //show menu
      requireView().findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      requireView().findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);

      //trash/hack/glitch/poor fix: (works without)

    }
  };
  public final Runnable showNextRunnable = new Runnable() {
    @Override
    public void run() {
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);

      int lastIndex = nextImageToShowIndex;
      nextImageToShowIndex = new Random().nextInt(mSlideshowFilesName.size());
      if (lastIndex == nextImageToShowIndex) {
        nextImageToShowIndex = new Random().nextInt(mSlideshowFilesName.size());
        Log.e(CLASSNAME, "mShowNextRunnable: avoiding to show same image twice");
      }
      if (pwa < mSlideshowFilesName.size()) {
        if ((pwa % 2) == 0) {

        new BackgroundImageDecoder.ShowImageTask(ContextCompat.getMainExecutor(requireActivity()),
              (ImageView) requireView().findViewById(R.id.imageView),
              mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
              interFrameDelay,screenHeight,screenWidth).start();

          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurWhite, requireActivity().getTheme()));
        } else {

          new BackgroundImageDecoder.ShowImageTask(ContextCompat.getMainExecutor(requireActivity()),
              (ImageView) requireView().findViewById(R.id.imageView),
              mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
              interFrameDelay,screenHeight,screenWidth).start();

          ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurPink, requireActivity().getTheme()));
        }

        pwa++;
      } else {
        Log.d(CLASSNAME, "mShowNextRunnable: no more images, restarting slideshow");
        pwa = 0;
        mSlideshowIsRunning = false;
        mSlideshowHandler.post(mStartSlideshowRunnable);
      }
    }

    // Code here will run in UI thread
  };
  private int uiDelay;
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.performClick();
        hide();
      return false;
    }

  };

  //quand on crée le fragment, on commence par une image du slideshow
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(CLASSNAME, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");

    initScreenMetrics();
    if (savedInstanceState != null) {
          mSlideshowFilesName = savedInstanceState.getStringArrayList("SlideshowFilenames");
    }

    makeButtons();
  }
  /*
   * Runnable mShowImageAfterTwoWordsRunnable
   * replace the menu with a random image from the slideshow, wait for "delay", then restart
   * slideshow
   */
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {

    @Override
    public void run() {
      Log.d(CLASSNAME, "mShowImageAfterTwoWordsRunnable");
      ((TextView) requireView().findViewById(R.id.ui_press_meTextView)).setVisibility(View.GONE);

      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
      new BackgroundImageDecoder.ShowImageTask(ContextCompat.getMainExecutor(requireActivity()),
          (ImageView) requireView().findViewById(R.id.imageView),
          mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
          0,screenHeight,screenWidth).start();

      mSlideshowHandler.postDelayed(mHideMenuRunnable, uiDelay);

      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, guessingDelay);
    }
  };

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mCacheDirPath = requireContext().getCacheDir();

    DataStore dataStore=new DataStore(getContext());
    choiceDelay =dataStore.getValue("DELAY_CHOICE_WORDS_SETTING",10000);
    guessingDelay =dataStore.getValue("DELAY_GUESSING_SETTING",5000);
    interFrameDelay =dataStore.getValue("DELAY_INTER_FRAME_SETTING",750);
    uiDelay =dataStore.getValue("UI_ANIMATION_DELAY",300);
    Log.d(CLASSNAME, "DELAY_INTER_FRAME_SETTING" + interFrameDelay);

    if (savedInstanceState != null) {
      mSlideshowFilesName = savedInstanceState.getStringArrayList("SlideshowFilenames");
    }

    return inflater.inflate(R.layout.fragment_slideshow, container, false);
  }

  public void startSlideshow(List<String> arg) {
    mSlideshowFilesName = arg;
    Log.d(CLASSNAME, "startSlideshow()");

    mSlideshowHandler.post(mStartSlideshowRunnable);
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

      Button tempButton = new androidx.appcompat.widget.AppCompatButton(requireActivity()) {
        @Override public boolean performClick() {
          super.performClick();
          return true;
        }
      };
      tempButton.setBackground(
          ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_on_off, null));
      tempButton.setText(buttonName);
      tempButton.setStateListAnimator(null);

        tempButton.setTypeface(ResourcesCompat.getFont(requireContext(),R.font.alef));


      tempButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
      tempButton.setAllCaps(true);
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      if (screenOrientationNormal) {
        Log.d(CLASSNAME, "screenOrientationNormal portrait");

        layoutParams.setMargins(buttonHorizontalMargin, buttonVerticalMargin,
            buttonHorizontalMargin,
            buttonVerticalMargin);
        tempButton.setPadding(buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding,
            buttonVerticalPadding);
      } else {
        Log.e(CLASSNAME, "screenOrientationNormal paysage");

        layoutParams.setMargins(buttonVerticalMargin, buttonHorizontalMargin
            ,
            buttonVerticalMargin,
            buttonHorizontalMargin);
        tempButton.setPadding(buttonVerticalPadding, buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding);
      }

      tempButton.setLayoutParams(layoutParams);

      tempButton.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {
          mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);
          mSlideshowHandler.removeCallbacks(showNextRunnable);
          mSlideshowIsRunning = false;
          if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
            return;
          }
          mLastClickTime = SystemClock.elapsedRealtime();

          mCheckedToggleButtonsArrayList.add(((Button) view));
          //can happens if you have big fingers:
          // deselect every checked button if more than 3 are pressed
          if (mCheckedToggleButtonsArrayList.size() > 2) {
            Log.d("mCheckedToggle", "3 Button pressed");
            mSlideshowHandler.post(blockMenuRunnable);
            mSlideshowHandler.post(cleanButtonRunnable);
            mSlideshowHandler.post(mStartSlideshowRunnable);
          } else if (mCheckedToggleButtonsArrayList.size() == 1) {

            ((Button) view).setTextColor(getResources().getColor(R.color.OurBlack,requireActivity().getTheme()));
            view.performClick();
            view.setPressed(true);
            view.setEnabled(false);
            Log.d("toggleClick", "toggle 1 buttons ok");
          } else if (mCheckedToggleButtonsArrayList.size() == 2) {
            mSlideshowHandler.post(blockMenuRunnable);

            ((Button) view).setTextColor(getResources().getColor(R.color.OurBlack,requireActivity().getTheme()));
            view.performClick();
            //deux boutons sont préssés
            view.setPressed(true);
            view.setEnabled(false);
            Log.d("toggleClick", "toggle 2 buttons ok");
            mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);


            mSlideshowHandler.post(mShowImageAfterTwoWordsRunnable);
          }

        }
      });

      //avoid a glitch reloading button7

      tempButton.setClickable(true);

      mToggleButtonsArrayList.add(tempButton);
    }

    int j;
    //for the first 8 button, set in the left menu layout
    for (j = 0; j < buttonNames.length / 2; j++) {

      ((LinearLayout) requireView().findViewById(R.id.leftMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }

    //for the first 8 buttons, set in the right menu layout

    for (; j < buttonNames.length; j++) {

      ((LinearLayout) requireView().findViewById(R.id.rightMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }
  }

  @Override
  public void onAttach(@NonNull Context context) {
    Log.d(CLASSNAME, " onAttach()");

    super.onAttach(context);
  }

  @Override
  public void onStop() {
    super.onStop();
    mSlideshowIsRunning = false;
    mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
    mSlideshowHandler.removeCallbacks(showNextRunnable);
    Log.d(CLASSNAME, "Fragment.onStop()");
  }

  @Override
  public void onPause() {
    super.onPause();
    mSlideshowHandler.removeCallbacks(mShowPart2Runnable);

    mSlideshowHandler.removeCallbacks(showNextRunnable);
    mSlideshowIsRunning = false;
    Log.d(CLASSNAME, "Fragment.onPause()");

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      // Clear the systemUiVisibility flag
      getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
    }
    show();
  }

  void initScreenMetrics() {
    screenMetrics = new DisplayMetrics();

    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);

    // DPI 240.0 density 1.5
    //en dp
    pressMeTextSize = 55;
    pressTwoWordsTextSize = 24;
    buttonTextSize = 22;
    //marge intérieure: entre le texte et la bordure du cadre (inversé si tablette en paysage)
    buttonVerticalPadding = 10;
    buttonHorizontalPadding = 8;

    buttonVerticalMargin = 20;
    buttonHorizontalMargin = 14;

    int screenDPI = screenMetrics.densityDpi;
    float screenDensity = screenMetrics.density;

    if (screenDPI < 340) {

      buttonVerticalMargin *= screenDensity;
      buttonHorizontalMargin *= screenDensity;

      buttonVerticalPadding *= screenDensity;
      buttonHorizontalPadding *= screenDensity;

      buttonTextSize *= screenDensity;

      pressMeTextSize *= screenDensity;

      pressTwoWordsTextSize *= screenDensity;
      Log.d("low DPI !!!", String.valueOf(screenDensity));
    } else {
      Log.e("high dpi !!!", String.valueOf(screenDensity));

      buttonVerticalPadding -= 10;
      buttonVerticalMargin -= 10;
      buttonHorizontalPadding -= 10;
      buttonHorizontalMargin -= 10;
    }
    Log.d("dpi", "metrics returned DPI "
        + screenDPI
        + " density "
        + screenMetrics.density
        + " text size "
        + pressMeTextSize);

    screenOrientationNormal = false;

    screenWidth = screenMetrics.widthPixels;
    screenHeight = screenMetrics.heightPixels;
    //en dp
    if (screenHeight > screenWidth) {
      screenOrientationNormal = true;
      Log.d("screenOrientationNormal", "portrait");
    } else {
      buttonVerticalPadding -= 10;
      buttonVerticalMargin -= 10;
      buttonHorizontalPadding -= 10;
      buttonHorizontalMargin -= 10;

      Log.e(CLASSNAME, "screenOrientationNormal paysage");
    }


    Log.d(CLASSNAME, "default screen width= " + screenWidth);
    Log.d(CLASSNAME, "default screen height= " + screenHeight);
  }

  @Override
  public void onResume() {
    super.onResume();

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    Log.d(CLASSNAME,
        " onResume() DEFAULT_PROJECT_KEY" + requireArguments().getString("DEFAULT_PROJECT_KEY",
            "DEFAULT_PROJECT_KEY_NOT_FOUND"));

  }


  protected void hide() {
    // Hide UI first
    Log.d(CLASSNAME, "Fragment.hide()");

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }

    // Schedule a runnable to remove the status and navigation bar after a delay
    mSlideshowHandler.removeCallbacks(mShowPart2Runnable);
    mSlideshowHandler.postDelayed(mHidePart2Runnable, uiDelay);
  }

  private void show() {
    // Show the system bar
    requireView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

    Log.d(CLASSNAME, "Fragment.show()");
    // Schedule a runnable to display UI elements after a delay
    mSlideshowHandler.removeCallbacks(mHidePart2Runnable);
    mSlideshowHandler.postDelayed(mShowPart2Runnable, uiDelay);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  }

  /**
   * Schedules a call to hide() in delay milliseconds, canceling any
   * previously scheduled calls.

  private void delayedHide() {
    mSlideshowHandler.removeCallbacks(mHideRunnable);
    mSlideshowHandler.postDelayed(mHideRunnable, 100);
  }
   */
  @Nullable
  private ActionBar getSupportActionBar() {
    ActionBar actionBar = null;
    if (getActivity() != null) {
      Activity activity = getActivity();
      actionBar = activity.getActionBar();
    }
    return actionBar;
  }

}