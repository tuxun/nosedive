package org.tflsh.multifacette;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import java.io.File;
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
  static final int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  static final int DELAY_GUESSING_SETTING = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  static final int DELAY_CHOICE_WORDS_SETTING = 10000;
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
  private static final String CLASSNAME = "SlideshowFragment";
//  static String SLIDESHOW_M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "rescatest/";
  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
  protected final Handler mSlideshowHandler = new Handler();
  private final Runnable mHidePart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
      /*int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
          */
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
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  /*private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        view.performClick();
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };
  */
  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed display of UI elements
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.show();
      }
      //!!!      mParentView.setVisibility(View.VISIBLE);
    }
  };
  BackgroundImageDecoder mBackgroundImageDecoder;
  Context mContext;
  File mCacheDirPath;
  int screenWidth;
  int screenHeight;
  Thread lastSlideLaunched;
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
        //?  mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
      }
      clickedButtons = mCheckedToggleButtonsArrayList.size();
      if (clickedButtons > 0) {
        for (int i = clickedButtons - 1; i >= 0; i--) {

          //mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
          mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
        }
      }
    }
  };
  private View mParentView;
  private int nextImageToShowIndex;
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
  private boolean mVisible;
  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };
  protected StartupFragment mStartupFragment;
  private ExecutorService executor;
  private boolean mSlideshowIsRunning = false;
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
        // mToggleButtonsArrayList.get(i).setEnabled(false);
        mToggleButtonsArrayList.get(i).setClickable(false);
        //?  mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
      }
      // mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      //mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      //mParentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);
    }
  };
  private final Runnable mHideMenuRunnable = new Runnable() {
    @Override
    public void run() {
      Log.e("mHideMenuRunnable", "visible ?");
      mSlideshowIsRunning = false;
      mParentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mSlideshowHandler.post(cleanButtonRunnable);
    }
  };
  private long mLastClickTime = 0;
  private ArrayList<String> mSlideshowFilesName;
  /**
   * @Runnable showMenuRunnable
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

      mParentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);

      //clean pending runnable
      mSlideshowHandler.removeCallbacks(showMenuRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);

      //restarting slideshow if no touch on button is detected for "delay" microseconds
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);

/* replaces it with placeholder?
      ((ImageView) mParentView.findViewById(R.id.imageView)).setImageDrawable(
          getResources().getDrawable(R.drawable.white_background, null));
*/

      //hide text, modify text, then show it again with menu
      mParentView.findViewById(R.id.ui_press_meTextView).setVisibility(View.GONE);
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextSize(
          TypedValue.COMPLEX_UNIT_DIP, pressTwoWordsTextSize);
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setText(
          R.string.string_choose2word);
      mParentView.findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);

      //show menu
      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);

      //trash/hack/glitch/poor fix: (works without)
      //? ((ImageView) mParentView.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.white_background,mContext.getTheme()));

    }
  };
  private long mLastMenuClickTime;

  public void cleanNext()
  {
    mSlideshowHandler.removeCallbacks(showNextRunnable);
  }
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {

      Log.d(CLASSNAME, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesName.size());
      //mSlideshowHandler.post(cleanButtonRunnable);

      makeImageClickable();

      if ((!mSlideshowFilesName.isEmpty())) {
        mSlideshowHandler.removeCallbacks(showNextRunnable);
        //      mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);


        if (!mSlideshowIsRunning) {
          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurWhite,null));

          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setText(
              getResources().getString(R.string.string_press_me));

          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextSize(
              TypedValue.COMPLEX_UNIT_DIP,
              pressMeTextSize);
          mSlideshowIsRunning = true;

          for (long i = 0; i < mSlideshowFilesName.size() + 1; i++) {
            mSlideshowHandler.postDelayed(showNextRunnable, i * DELAY_INTER_FRAME_SETTING);
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
      Objects.requireNonNull(getView())
          .findViewById(R.id.ui_centralLinearLayout)
          .setVisibility(View.VISIBLE);
      //      ((LinearLayout)mParentView.findViewById(R.id.slideshowScreenLinearSourceLayout)).setLayoutMode(
      //      LinearLayout.LayoutParams.MATCH_PARENT);
      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
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

          executor.execute(
              lastSlideLaunched = mBackgroundImageDecoder.new ShowImageTask(
                  (ImageView) mParentView.findViewById(R.id.imageView),
                  mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
                  DELAY_INTER_FRAME_SETTING)
          );
          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurWhite, null));
        } else {

          executor.execute(
              lastSlideLaunched = mBackgroundImageDecoder.new ShowImageTask(
                  ((ImageView) mParentView.findViewById(R.id.imageView)),
                  mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
                  DELAY_INTER_FRAME_SETTING)
          );
          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurPink, null));
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
  /**
   * @Runnable mShowImageAfterTwoWordsRunnable
   * replace the menu with a random image from the slideshow, wait for "delay", then restart
   * slideshow
   */
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {

    @Override
    public void run() {
      Log.d(CLASSNAME, "mShowImageAfterTwoWordsRunnable");
      // lastSlideLaunched.interrupt();
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.WHITE);

      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);

      mSlideshowHandler.post(
          lastSlideLaunched = mBackgroundImageDecoder.new ShowImageTask(
              ((ImageView) mParentView.findViewById(R.id.imageView)),
              mCacheDirPath + "/" + mSlideshowFilesName.get(
                  new Random().nextInt(mSlideshowFilesName.size()))
              , 0
          ));

      mSlideshowHandler.postDelayed(mHideMenuRunnable, UI_ANIMATION_DELAY * 2);
      //  mSlideshowHandler.post(mHideMenuRunnable);

      //  mSlideshowHandler.post(cleanButtonRunnable);

      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_GUESSING_SETTING);
    }
  };
  private boolean screenOrientationNormal;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mContext = getContext();
    mCacheDirPath = mContext.getCacheDir();



    return inflater.inflate(R.layout.fragment_slideshow, container, false);
  }


  //quand on crée le fragment, on commence par une image du slideshow
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(CLASSNAME, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");
    mParentView = getView();
/*
    mParentView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        Log.d(TAG, "SlideshowFragment toggle fullscreen");

        toggle();
        return false;
      }
    });
  */
    // The cache size will be measured in kilobytes rather than
    // number of items.
    LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };
    initScreenMetrics();
    if (savedInstanceState != null) {
          mSlideshowFilesName = savedInstanceState.getStringArrayList("SlideshowFilenames");
    }
    mVisible = true;
    executor = Executors.newFixedThreadPool(1);
    mBackgroundImageDecoder =
        new BackgroundImageDecoder(mContext, screenWidth, screenHeight, memoryCache,
            executor);
    //    mParentView = view.findViewById(R.id.fullscreen_content_controls);
    //mControlsView = view.findViewById(R.id.SlideshowFragment);

    // Set up the user interaction to manually show or hide the system UI.


/*    View iframe = mParentView.findViewById(R.id.startupScreenLinearLayout);
    ViewGroup parent = (ViewGroup) iframe.getParent();
    parent.removeView(iframe);*/
    // Upon interacting with UI controls, delay any scheduled hide()
    // operations to prevent the jarring behavior of controls going away
    // while interacting with the UI.
    //!parent.findViewById(R.id.motherLayout).setOnTouchListener(mDelayHideTouchListener);
    makeButtons();
  }

  public void startSlideshow(ArrayList<String> arg) {
    mSlideshowFilesName = arg;
    Log.d(CLASSNAME, "startSlideshow()");

    mSlideshowHandler.post(mStartSlideshowRunnable);
  }


  private void makeImageNotClickable() {
    mParentView.findViewById(R.id.slideshowLayout).setClickable(false);
  }

  private void makeImageClickable() {
    Log.d(CLASSNAME, "makeImageClickable(): image is now clickable");

    mParentView.findViewById(R.id.slideshowLayout).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlideshowHandler.removeCallbacks(showMenuRunnable);

        makeImageNotClickable();
        ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.WHITE);

        //lastSlideLaunched.interrupt();
        showMenuRunnable.run();
      }
    });
    getActivity().getWindow()
        .findViewById(R.id.slideshowScreenLinearSourceLayout)
        .setClickable(true);
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

      Button tempButton = new androidx.appcompat.widget.AppCompatButton(mContext) {
        @Override public boolean performClick() {
          super.performClick();
          return true;
        }
      };
      tempButton.setBackground(
          ResourcesCompat.getDrawable(getResources(), R.drawable.ic_button_on_off, null));
      tempButton.setText(buttonName);
      tempButton.setStateListAnimator(null);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        tempButton.setTypeface(getResources().getFont(R.font.alef));
      }

      tempButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, buttonTextSize);
      tempButton.setAllCaps(true);
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      if (screenOrientationNormal) {
        Log.d("screenOrientationNormal", "portrait");

        layoutParams.setMargins(buttonHorizontalMargin, buttonVerticalMargin,
            buttonHorizontalMargin,
            buttonVerticalMargin);
        tempButton.setPadding(buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding,
            buttonVerticalPadding);
      } else {
        Log.e("screenOrientationNormal", "paysage");

        layoutParams.setMargins(buttonVerticalMargin, buttonHorizontalMargin
            ,
            buttonVerticalMargin,
            buttonHorizontalMargin);
        tempButton.setPadding(buttonVerticalPadding, buttonHorizontalPadding, buttonVerticalPadding,
            buttonHorizontalPadding);
      }

      tempButton.setLayoutParams(layoutParams);
/* save!!!
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
              mSlideshowHandler.post(cleanButtonRunnable);

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
 */
      ////////////////////////////////CRITICAL//////////////////////////////////////
      /*urgent
       */
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

            ((Button) view).setTextColor(Color.BLACK);
            view.performClick();
            view.setPressed(true);
            view.setEnabled(false);
            Log.d("toggleClick", "toggle 1 buttons ok");
            ((Button) view).setTextColor(Color.BLACK);
          } else if (mCheckedToggleButtonsArrayList.size() == 2) {
            mSlideshowHandler.post(blockMenuRunnable);

            //should only  happen when 2 DIFFERENT buttons are pressed
            //   ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
            // getResources().getColor(R.color.OurWhite));

            ((Button) view).setTextColor(Color.BLACK);
            view.performClick();
            //deux boutons sont préssés
            view.setPressed(true);
            view.setEnabled(false);
            Log.d("toggleClick", "toggle 2 buttons ok");
            mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
            /*try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }*/

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

      ((LinearLayout) mParentView.findViewById(R.id.leftMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }

    //for the first 8 buttons, set in the right menu layout

    for (; j < buttonNames.length; j++) {

      ((LinearLayout) mParentView.findViewById(R.id.rightMenuLinearLayout)).addView(
          mToggleButtonsArrayList.get(j));
    }
  }

  @Override
  public void onAttach(Context context) {
    Log.d(CLASSNAME, " onAttach()");

    super.onAttach(context);
    mContext = context;
  }

  @Override
  public void onStop() {
    super.onStop();
    mSlideshowIsRunning = false;
    mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
    Log.d(CLASSNAME, "Fragment.onStop()");
    //done in pause    unregisterReceiver(intentReceiver);
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

    getActivity().getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);

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

    //in pixel

    //    buttonVerticalPadding *= screenDensity;
    //  buttonHorizontalPadding *= screenDensity;

    if (screenDPI < 340) {

      buttonVerticalMargin *= screenDensity;
      buttonVerticalMargin = Math.round(buttonVerticalMargin);
      buttonHorizontalMargin *= screenDensity;
      buttonHorizontalMargin = Math.round(buttonHorizontalMargin);

      buttonVerticalPadding *= screenDensity;
      buttonVerticalPadding = Math.round(buttonVerticalMargin);
      buttonHorizontalPadding *= screenDensity;
      buttonHorizontalPadding = Math.round(buttonHorizontalMargin);

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

      Log.e("screenOrientationNormal", "paysage");
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
    mSlideshowFilesName = new ArrayList<>();
    loadStartupFragment();
   // mStartupFragment = (StartupFragment) getFragmentManager().findFragmentByTag("StartupFragment");

    Log.d(CLASSNAME, " onResume()");

    if ((getFragmentManager().findFragmentByTag("StartupFragment")) != null) {
      Log.e(CLASSNAME, "we found our StartupFragment!!!!");
    } else {
      Log.d(CLASSNAME, "SlideshowFragment onCreate() mStartupFragment=void");
    }
    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    mStartupFragment.startGlobalCheckThread();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mParentView = null;
  }

  void toggle() {
    if (mVisible) {
      hide();
    } else {
      show();
    }
  }

  protected void hide() {
    // Hide UI first
    Log.d(CLASSNAME, "Fragment.hide()");

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    //mControlsView.setVisibility(View.GONE);
    mVisible = false;

    // Schedule a runnable to remove the status and navigation bar after a delay
    mSlideshowHandler.removeCallbacks(mShowPart2Runnable);
    mSlideshowHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  private void show() {
    // Show the system bar
    Objects.requireNonNull(getView()).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

    mVisible = true;
    Log.d(CLASSNAME, "Fragment.show()");
    // Schedule a runnable to display UI elements after a delay
    mSlideshowHandler.removeCallbacks(mHidePart2Runnable);
    mSlideshowHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.show();
    }
  }

  /**
   * Schedules a call to hide() in delay milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide() {
    mSlideshowHandler.removeCallbacks(mHideRunnable);
    mSlideshowHandler.postDelayed(mHideRunnable, 100);
  }

  @Nullable
  private ActionBar getSupportActionBar() {
    ActionBar actionBar = null;
    if (getActivity() != null) {
      Activity activity = getActivity();
      actionBar = activity.getActionBar();
    }
    return actionBar;
  }

  private void loadStartupFragment() {

    Log.d(CLASSNAME, "loadStartupFragment()");
    //Bundle args = new Bundle();
    //args.putString("M_SERVER_DIRECTORY_URL", M_SERVER_DIRECTORY_URL);
    //args.putStringArrayList("SlideshowFilenames", mSlideshowFilesName);
    //mStartupFragment.setArguments(args);
    FragmentManager manager = getFragmentManager();
    mStartupFragment = new StartupFragment();

    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(R.id.startupScreenLinearSourceLayout, mStartupFragment, "MULTIFACETTE_LOADING");
    transaction.addToBackStack(null);
    transaction.commit();
  }
}