package org.tflsh.nosedive;

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
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SlideshowFragment extends Fragment {
  static String SLIDESHOW_M_SERVER_DIRECTORY_URL = "https://dev.tuxun.fr/nosedive/" + "julia/";

  private View mParentView;
  private int nextImageToShowIndex;

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
      int flags =View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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

  private int buttonTextSize;
  private int pressMeTextSize;
  private int pressTwoWordsTextSize;
  private ImageView mImageView;
  private int pwa = 0;
  private TextView pressMeTextView;

  BackgroundImageDecoder mBackgroundImageDecoder;

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
 Context mContext;
 File mCacheDirPath;
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    mContext=getContext();
    mCacheDirPath=mContext.getCacheDir();

    return inflater.inflate(R.layout.fragment_slideshow, container, false);
  }
  private StartupFragment mStartupFragment;


  void exec(final String arg){
    new Thread(new Runnable() {
      @Override public void run() {
        if (mStartupFragment!= null) {
          //strtpfrgmnt.exec(arg);
          Log.d("sldshowexec", " exec() mStartupFragment OOOK"+arg);

        } else {
          Log.d("sldshowexec", " exec() mStartupFragment=void");
        }
      }
    }).start();
  }

  private final Runnable cleanButtonRunnable = new Runnable() {
    @Override
    public void run() {
      int clickedButtons = mToggleButtonsArrayList.size() - 1;
      Log.d(TAG, "cleanButtonRunnable:" + clickedButtons + " cleaned buttons");
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







  //quand on crée le fragment, on commence forcemment par une image du slideshow
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use maximum available memory for this memory cache.
    Log.d(TAG, " onCreate() creating a " + cacheSize / 1024 + "Mo LRU cache");
    mParentView = getView();


    mParentView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        Log.d(TAG, "slidefragment toggle fscreen");

        toggle();
        return false;
      }
    });
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
if (savedInstanceState!=null) {
  mSlideshowFilesName = savedInstanceState.getStringArrayList("SlideshowFilenames");
}
else {mSlideshowFilesName=new ArrayList<>();}
    mVisible = true;
    initScreenMetrics();
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
  public void startSlideshow(ArrayList<String> arg){
    mSlideshowFilesName=arg;
    Log.d(TAG, "startSlideshow()");

     mSlideshowHandler.post(mStartSlideshowRunnable);

  }

  public void setBaseUrl(String arg) {

    SLIDESHOW_M_SERVER_DIRECTORY_URL = arg;
  }

  int screenWidth;
  int screenHeight;
  Thread lastSlideLaunched;
  DisplayMetrics screenMetrics;
  private final Runnable mShowImageAfterTwoWordsRunnable = new Runnable() {

    @Override
    public void run() {
      Log.d(TAG, "mShowImageAfterTwoWordsRunnable");
      lastSlideLaunched.interrupt();
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.WHITE);

//      ((ImageView)mParentView.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));
 /*  mSlideshowHandler.post(mBackgroundImageDecoder.new ShowImageTask(
          (ImageView) mParentView.findViewById(R.id.imageView),
          "",0));

      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
          getResources().getColor(R.color.OurWhite));

*/
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);
/*
      ((ImageView) mParentView.findViewById(R.id.imageView)).setImageDrawable(
          getResources().getDrawable(R.drawable.whitebackground, null));
*/
      mSlideshowHandler.post(
          lastSlideLaunched = mBackgroundImageDecoder.new ShowImageTask(
              ((ImageView) mParentView.findViewById(R.id.imageView)),
              mCacheDirPath + "/" + mSlideshowFilesName.get(
                  new Random().nextInt(mSlideshowFilesName.size()))
              , 0
          ));

      mSlideshowHandler.postDelayed(mHideMenuRunnable, UI_ANIMATION_DELAY * 2);
      //  mSlideshowHandler.post(mHideMenuRunnable);

      //  mHideHandler.post(cleanButtonRunnable);

      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_GUESSING_SETTING);
    }
  };
  ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
  private final Handler mSlideshowHandler = new Handler();

  private void makeImageNotClickable() {
    mParentView.findViewById(R.id.SlideshowLayout).setClickable(false);
  }

  private void makeImageClickable() {
    Log.d(TAG, "makeImageClickable(): image is now clickable");

    mParentView.findViewById(R.id.SlideshowLayout).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSlideshowHandler.removeCallbacks(showMenuRunnable);

        makeImageNotClickable();
((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.WHITE);

        lastSlideLaunched.interrupt();
        showMenuRunnable.run();
      }
    });
    getActivity().getWindow()
        .findViewById(R.id.slideshowScreenLinearSourceLayout)
        .setClickable(true);
  }

  static final int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  static final int DELAY_GUESSING_SETTING = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  static final int DELAY_CHOICE_WORDS_SETTING = 10000;
  private static final String TAG = "SlideshowActivity";
  final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
  private ExecutorService executor;
  private boolean mSlideshowIsRunning = false;
  private final Runnable blockmenu = new Runnable() {
    @Override
    public void run() {
      Log.e("blockmenu", "visible ?");

      //a chaque button cliqué, si on est perdu, on decheck les bouttons
      //should only  happen when 2 DIFFERENT buttons are pressed
      mSlideshowIsRunning = false;
      int clickedButtons = mToggleButtonsArrayList.size() - 1;
      Log.d(TAG, "blockmenu:" + clickedButtons + " blockmenu buttons");
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
  private final Runnable showNextRunnable = new Runnable() {
    @Override
    public void run() {
      //antibounce

      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);

      int lastIndex=nextImageToShowIndex;
       nextImageToShowIndex = new Random().nextInt(mSlideshowFilesName.size());
       if(lastIndex==nextImageToShowIndex)
{         nextImageToShowIndex = new Random().nextInt(mSlideshowFilesName.size());
  Log.e(TAG, "mShowNextRunnable: avoiding to show same image twice");

}
      if (pwa < mSlideshowFilesName.size()) {
        if ((pwa % 2) == 0) {

          executor.execute(
              lastSlideLaunched= mBackgroundImageDecoder.new ShowImageTask(
                  (ImageView) mParentView.findViewById(R.id.imageView),
                  mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
                  DELAY_INTER_FRAME_SETTING)
          );
          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurWhite,null));
        } else {

          executor.execute(
              lastSlideLaunched=mBackgroundImageDecoder.new ShowImageTask(
                  ((ImageView) mParentView.findViewById(R.id.imageView)),
                  mCacheDirPath + "/" + mSlideshowFilesName.get(nextImageToShowIndex),
                  DELAY_INTER_FRAME_SETTING)
          );
          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
              getResources().getColor(R.color.OurPink,null));
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
  private final Runnable mHideMenuRunnable = new Runnable() {
    @Override
    public void run() {
      Log.e("mHideMenuRunnable", "visible ?");
      //a chaque button cliqué, si on est perdu, on decheck les bouttons
      //should only  happen when 2 DIFFERENT buttons are pressed
      mSlideshowIsRunning = false;
      mParentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
      mSlideshowHandler.post(cleanButtonRunnable);
    }
  };
  private long mLastClickTime = 0;
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

      Button tempButton = new Button(mContext) {
        @Override public boolean performClick() {
          super.performClick();
          return true;
        }
      };
      tempButton.setBackground(
          getResources().getDrawable( R.drawable.ic_bouttonoff, null));
      tempButton.setText(buttonName);
      tempButton.setStateListAnimator(null);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        tempButton.setTypeface(getResources().getFont( R.font.alef));
      }

      tempButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);
      tempButton.setAllCaps(true);
      LinearLayout.LayoutParams layoutParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      if (screenOrientationNormal) {

        layoutParams.setMargins(buttonHorizontalMargin, buttonVerticalMargin, buttonHorizontalMargin,
            buttonVerticalMargin);
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
          if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
          }
          mLastClickTime = SystemClock.elapsedRealtime();

          mCheckedToggleButtonsArrayList.add(((Button) view));
          //ne doit arriver que si vous avez des gros doigts ;)
          if (mCheckedToggleButtonsArrayList.size() > 2) {
            Log.d("mCheckedToggle", "3 Button pressed");
            mSlideshowHandler.post(blockmenu);
            mHideHandler.post(cleanButtonRunnable);
            mSlideshowHandler.post(mStartSlideshowRunnable);
          } else if (mCheckedToggleButtonsArrayList.size() == 1) {

            ((Button) view).setTextColor(Color.BLACK);
            view.performClick();
            view.setPressed(true);
            view.setEnabled(false);
            Log.d("toggleClick", "toggle 1 buttons ok");
            ((Button) view).setTextColor(Color.BLACK);
          } else if (mCheckedToggleButtonsArrayList.size() == 2) {


            mSlideshowHandler.post(blockmenu);
            //deux boutons sont préssés
          /*  ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(
                getResources().getColor(R.color.OurWhite));
           */
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

          return;
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
    Log.d("sldshow", " onAttach()");

    super.onAttach(context);
    mContext = context;
  }

  @Override
  public void onStop() {
    super.onStop();
    mSlideshowIsRunning = false;
    mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
    Log.d(TAG, "Fragment.onStop()");
    //done in pause    unregisterReceiver(intentReceiver);
  }

  @Override
  public void onPause() {
    super.onPause();
    mHideHandler.removeCallbacks(mShowPart2Runnable);

    mSlideshowHandler.removeCallbacks(showNextRunnable);
    mSlideshowIsRunning = false;

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      Log.d(TAG, "Fragment.onPause()");
      // Clear the systemUiVisibility flag
      getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
    }
    show();
  }


  private ArrayList<String> mSlideshowFilesName;
  private ArrayList<String> missingFilesNames;

  private boolean screenOrientationNormal;
  private final Runnable mStartSlideshowRunnable = new Runnable() {

    @Override
    public void run() {

      Log.d(TAG, "mStartSlideshowRunnable with slideshow size=" + mSlideshowFilesName.size());
      //mHideHandler.post(cleanButtonRunnable);

      makeImageClickable();


      if ((!mSlideshowFilesName.isEmpty())) {
        mSlideshowHandler.removeCallbacks(showNextRunnable);
        //      mSlideshowHandler.removeCallbacks(mShowImageAfterTwoWordsRunnable);

        //hummm



        if (!mSlideshowIsRunning) {
          ((TextView)mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurWhite));

          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setText(
              getResources().getString(R.string.string_press_me));

          ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_PX,
              pressMeTextSize);
          mSlideshowIsRunning = true;

          for (long i = 0; i < mSlideshowFilesName.size() + 1; i++) {
            mSlideshowHandler.postDelayed(showNextRunnable, i * DELAY_INTER_FRAME_SETTING);
          }
        } else {
          Log.e(TAG, "mStartSlideshowRunnable tried to start twice" + mSlideshowFilesName.size());
          mSlideshowIsRunning = false;
        }
      } else {
        Log.e(TAG, "mSlideshowFilesName is empty" + mSlideshowFilesName.size());

      }

      mSlideshowHandler.post(mHideMenuRunnable);
      getView().findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);
      //      ((LinearLayout)mParentView.findViewById(R.id.slideshowScreenLinearSourceLayout)).setLayoutMode(
      //      LinearLayout.LayoutParams.MATCH_PARENT);
      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
    }
  };
  void initScreenMetrics() {
    screenMetrics = new DisplayMetrics();

      getActivity().getWindowManager().getDefaultDisplay().getMetrics(screenMetrics);


    //en dp
    pressMeTextSize = 75;
    pressTwoWordsTextSize = 38;
    buttonTextSize = 32;
    //marge intérieure: entre le texte et la bordure du cadre (inversé si tablette en paysage)
    buttonVerticalPadding = 12;
    buttonHorizontalPadding = 20;

    buttonVerticalMargin = 30;
    buttonHorizontalMargin = 20;

    float screenDPI = screenMetrics.densityDpi;
    float screenDensity = screenMetrics.scaledDensity;

    //in pixel

    buttonVerticalPadding *= screenDPI/160;
    buttonHorizontalPadding *= screenDPI/160;

    buttonVerticalMargin *= screenDPI/160;
    buttonHorizontalMargin *= screenDPI/160;
    buttonTextSize *= screenDPI/160;

    pressMeTextSize *= screenDPI/160;

    pressTwoWordsTextSize *=screenDPI/160;
    Log.d("dpi", "metrics returned DPI " + screenDPI  + " density " + screenDensity+ " textsize " +pressMeTextSize);

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
      mSlideshowHandler.removeCallbacks(showMenuRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowHandler.removeCallbacks(showNextRunnable);

      lastSlideLaunched.interrupt();
      Log.d(TAG, "showMenuRunnable");
      mSlideshowHandler.removeCallbacks(showNextRunnable);
new Thread(new Runnable() {
  @Override public void run() {

  }
}).start();



((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_PX,
          pressTwoWordsTextSize);

      // mSlideshowHandler.post(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(cleanButtonRunnable);
      mSlideshowHandler.removeCallbacks(mStartSlideshowRunnable);
      mSlideshowIsRunning = false;
      mSlideshowHandler.postDelayed(mStartSlideshowRunnable, DELAY_CHOICE_WORDS_SETTING);
      mParentView.findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);
      mParentView.findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
      mParentView.findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setText(R.string.string_choose2word);
      ((TextView) mParentView.findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);
     //!!!? ((ImageView) mParentView.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground,mContext.getTheme()));



    }
  };
  @Override
  public void onResume() {
    super.onResume();
    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    loadStartupFragment();
    mStartupFragment =(StartupFragment)          getFragmentManager().findFragmentByTag("StartupFragment");

    Log.d("sldshow", " onResume()");

    if ((getFragmentManager().findFragmentByTag("StartupFragment")) != null) {
      //! strtpfrgmnt.exec(arg);
    } else {
      Log.d("sldshowexec", " onCreate() mStartupFragment=void");
    }
    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
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
    Log.d(TAG,"Fragment.hide()");

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    //mControlsView.setVisibility(View.GONE);
    mVisible = false;

    // Schedule a runnable to remove the status and navigation bar after a delay
    mHideHandler.removeCallbacks(mShowPart2Runnable);
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  private void show() {
    // Show the system bar
    getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);


    mVisible = true;
Log.d(TAG,"Fragment.show()");
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
    if (getActivity() instanceof Activity) {
      Activity activity = getActivity();
      actionBar = activity.getActionBar();
    }
    return actionBar;
  }

  private void loadStartupFragment() {

    Log.d(TAG, "loadStartupFragment()");
    //Bundle args = new Bundle();
    //args.putString("M_SERVER_DIRECTORY_URL", M_SERVER_DIRECTORY_URL);
    //args.putStringArrayList("SlideshowFilenames", mSlideshowFilesName);
    //mStartupFragment.setArguments(args);
    FragmentManager manager = getFragmentManager();
    mStartupFragment=new StartupFragment();

    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(R.id.startupScreenLinearSourceLayout, mStartupFragment, "MULTIFACETTE_LOADING");
    transaction.addToBackStack(null);
    transaction.commit();

  }
}