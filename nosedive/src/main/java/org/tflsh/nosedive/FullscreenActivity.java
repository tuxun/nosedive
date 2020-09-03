package org.tflsh.nosedive;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class FullscreenActivity extends AppCompatActivity {

      //   ExecutorService executor;
    final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
    //temps unitaire (base de temps), sert a définir le delai entre deux images
    final int delayInterFrameSetting = 750;
    //temps durant lequel onregarde une image proposé apres le menu (en multiple d'interframedelay)
    final int delayQuestionnementSetting = 5000;
    final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/"+ "julia/"; //+"rescatest/";//

    //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
    final int delayChoixMotsSetting = 10000;
private ImageView imageView;
    private final Handler mSlideshowHandler = new Handler();
    private final Runnable showPressmeTextRunnable = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
        }
    };
    private final Runnable cleanButtonRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedButtons = mCheckedToggleButtonsArrayList.size();
            Log.d("cleanButtonRunnable", clickedButtons + "cleaned Buttons ok");
            for (int i = clickedButtons - 1; i >= 0; i--) {

                mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
                mCheckedToggleButtonsArrayList.get(i).setClickable(true);
                mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    public int pwa;
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
    public final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          //  Log.d("FullscreenActivity", "org.tflsh.nosedive.FullscreenActivity.onReceive");
            switch (Objects.requireNonNull(intent.getAction())) {
                //dlstarted?
                //dlreceived
                //dlcomplete

                case "dlStarted":
               //     Log.e("intentreceiver", "dlstarted action");
                    //  ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementSecondaryProgressBy(1);
                    // findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);
                    findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);


                    missingFilesNumber++;


                    break;
                case "dlReceived":
                    Log.e("intentreceiver", "dlreceived action");
                    ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementProgressBy(1);
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.string_wait4dl);
                    //  findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);

                    break;
                case "dlComplete":
                    Log.d("intentreceiver", "dlcomplete action");
                        makeImageClickable();

mSlideshowIsRunning=false;
                    missingFilesNumber = 0;
                    findViewById(R.id.ui_dl_progressTextView).setVisibility(View.GONE);
                    findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.GONE);

                    mSlideshowHandler.post(startdiapoRunnable);
                    break;

                case "noJson":
                //    Log.e("intentreceiver", "nojson action" + intent.getIntExtra("EXTRA_MESSAGE", 0));
                    mHaveInternet = false;
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.activatewifiandrelauch);

                    break;

                case "filesFound":
                    //Log.e("intentreceiver", "filesfound action");
                    int max=intent.getIntExtra("EXTRA_MESSAGE", 0);
                    if(max>0) {
                        ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setMax(max);
                        Log.e("intent Receiver", "filesfound set pgbbarr" + max);
                        findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
                    }
                    else
                    {                        Log.e("intentreceiver", "filesfound set pgbbarr" + max);
                        Log.e("intentreceiver", "dlcomplete action");
           makeImageClickable();
           mSlideshowIsRunning=false;
          mSlideshowHandler.post(startdiapoRunnable);

}
                    break;
                case "imgShown":
                    String b =intent.getStringExtra("EXTRA_MESSAGE");
                  //  Log.e("intentreceiver", "imageshownaction got "+b);
                   //fss Log.d("intentreceiver", "imageshownaction");

                        //imageView.setImageBitmap(decodeSampledBitmapFromFilepath(b,screenWidth,screenHeight));

                    /*catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    break;

                default:
                    Log.e("intentreceiver", "unknow action");

                    break;
            }
        }
    };
    private final Runnable startdiapoRunnable = new Runnable() {

        @Override
        public void run() {
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            mSlideshowHandler.removeCallbacks(showimgandrestartdiapoafter2words);
            findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);
            mHideHandler.post(cleanButtonRunnable);
            ((TextView) findViewById(R.id.ui_press_meTextView))
                    .setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));
            ((TextView) findViewById(R.id.ui_press_meTextView)).setTextSize(
                    TypedValue.COMPLEX_UNIT_SP, 64);
            //findViewById(R.id.pressme_text).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setText(getResources().getString(R.string.string_press_me));
            if (!mSlideshowIsRunning) {

//!? makeimageclickable();
                mSlideshowIsRunning = true;
                Log.d("startdiapoRunnable", "start with diapo size=" + mSlideshowFilesNames.size());

                pwa = 0;
                int i, j;

                for (i = 0; i < mSlideshowFilesNames.size() + 1; i++) {
                    mSlideshowHandler.postDelayed(showNextRunnable, i * delayInterFrameSetting);
                }
            } else {

                Log.d("startdiapoRunnable", "started twice=" + mSlideshowFilesNames.size());


            }

        }


    };
    TextView tvProgressLabel;

    private static final int UI_ANIMATION_DELAY = 300;
    asyncTaskManager asm;
    private final Runnable showNextRunnable = new Runnable() {
        @Override
        public void run() {
            //antibounce

            mSlideshowHandler.removeCallbacks(startdiapoRunnable);
            //Log.d("showNextRunnable", "next!");
            mHideHandler.post(showPressmeTextRunnable);

int nextimg=new Random().nextInt(mSlideshowFilesNames.size());
            if (mSlideshowIsRunning) {
                if (pwa < mSlideshowFilesNames.size()) {
                    if ((pwa % 2) == 1) {
                         //runnables:
                        /* executor.execute(asm.new showImageFileTask(
                        imageView
                        ,mSlideshowFilesNames.get(nextimg)));
                        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurWhite));
                        */
                        asm.new showImageFileTask(
                                  (ImageView) findViewById(R.id.imageView)
                          ).execute(mSlideshowFilesNames.get(nextimg));
                        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurPink));

                    } else {
                        //runnables:
                        /* executor.execute(asm.new showImageFileTask(
                        imageView
                        ,mSlideshowFilesNames.get(nextimg)));
                        */

                        asm.new showImageFileTask(
                                  (ImageView) findViewById(R.id.imageView)
                          ).execute(mSlideshowFilesNames.get(nextimg));
                        ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurWhite));
                    }

                    //dans l'ordre
                    //!!!!!!!!!!!!!!new asyncTaskManager.showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(pwa));
                    //en pseudo random

                    // Log.d("diapo", "showing " + mDiapo.get(pwa));
                    pwa++;
                } else {
                    Log.d("pwa", "no more pwa");
                    pwa = 0;
                    mSlideshowIsRunning=false;
                    mSlideshowHandler.post(startdiapoRunnable); //end handlepostdelay

                    // pgb.setProgress(pwa);
                    //mdiapo_isrunning=false;
                    // Log.d("pwa","we added pwa for keep the diapo going... and going... and going...");
                }

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
            Log.e("showMenuRunnable", "showmenuRunnable ?");
            imageView.setDrawingCacheBackgroundColor(Color.WHITE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 38);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));

            mSlideshowHandler.post(cleanButtonRunnable);
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            mSlideshowHandler.removeCallbacks(cleanButtonRunnable);
            mSlideshowHandler.removeCallbacks(startdiapoRunnable);
            mSlideshowIsRunning = false;
            mSlideshowHandler.postDelayed(startdiapoRunnable, delayChoixMotsSetting);
            findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setText(R.string.string_choose2word);
            findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(Color.BLACK);


        }
    };


    private final Handler mHideHandler = new Handler();

    private final boolean debuglayoutmode = false;
    private final Runnable hidemenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("hidemenurunnable", "visible ?");
            int clickedbuttons = mCheckedToggleButtonsArrayList.size();
            if (clickedbuttons > 0) {
                for (int i = clickedbuttons - 1; i >= 0; i--) {

                    mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
                    mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
                }
                //a chaque button cliqué, si on est perdu, on decheck les bouttons
                //should only  happen when 2 DIFFERENT buttons are pressed
                //  ((ToggleButton) view).setChecked(true);
                           mSlideshowIsRunning=false;

                // mSlideshowHandler.postDelayed(startdiapoRunnable, delayChoixMotsSetting);
            }


            findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
            //mHideHandler.post(cleanbuttonRunnable);

            // findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);
            //            findViewById(R.id.fullscreen_mumcontent_controls).setVisibility(View.GONE);
            findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

        }
    };
    int screenWidth;


    private final Runnable mfullscreenOffRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements

            //  mContentView.getLayoutParams().notifyAll();

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //decorView.setFitsSystemWindows(false);


            // mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            Log.e("mShowPart2Runnable", "EXITFULLSCREEN");
            mFullscreen = false;

            findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);

        }
    };

    private ArrayList<String> mSlideshowFilesNames;
    private boolean mSlideshowIsRunning = false;
    //REGLAGE DE LAPPLI
    //end runnables list
    private LinearLayout lm;
    private LinearLayout rm;
    // private static final boolean AUTO_HIDE = true;
    private boolean mFullscreen;
    private final Runnable mfullscreenOnRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
                /*     mContentView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                               | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
            Log.e("mShowPart2Runnable", "ENTERFULLSCREEN");
            mFullscreen = true;
            // Hide UI first
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            lm.setVisibility(View.GONE);
            findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.GONE);
            findViewById(R.id.ui_dl_progressTextView).setVisibility(View.GONE);

            rm.setVisibility(View.GONE);
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LOW_PROFILE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            // ((LinearLayout) findViewById(R.id.windowLayout)).setFitsSystemWindows(false);

           /* findViewById(R.id.windowLayout).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    */


        }
    };
    //important: everytime we stop diapo, we show menu
    private final Runnable stopdiapoRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("runnable", "stop!");
            mSlideshowIsRunning = false;
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            mSlideshowHandler.removeCallbacks(startdiapoRunnable);


        }
    };



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////ACTIVITY (MAIN)/////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("onConfigurationChanged", "onConfigurationChanged ?");
mSlideshowHandler.removeCallbacks(showNextRunnable);
       setScreenMetrics();
            this.onResume();

        }




    @Override
    protected void onStop() {
        super.onStop();
        mSlideshowHandler.post(stopdiapoRunnable);
        Log.d("activity", "onStop");
        unregisterReceiver(intentReceiver);

    }

    private final Runnable showimgandrestartdiapoafter2words = new Runnable() {
        @Override
        public void run() {
            Log.d("restartdiapoafter2words", "connexion limité");
            (findViewById(R.id.ui_press_meTextView)).setVisibility(View.GONE);
            mSlideshowHandler.postDelayed(cleanButtonRunnable, UI_ANIMATION_DELAY);
            mSlideshowHandler.postDelayed(hidemenuRunnable, delayInterFrameSetting);

            imageView.setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));

            //  rm.setVisibility(8);
            //lm.setVisibility(8);
            //runnable version:
            // executor.execute(asm.new showImageFileTask((ImageView) findViewById(R.id.imageView),mSlideshowFilesNames.get(new Random().nextInt(mSlideshowFilesNames.size()))));
            //async version
            asm.new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mSlideshowFilesNames.get(new Random().nextInt(mSlideshowFilesNames.size())));            SystemClock.sleep(50);

            mSlideshowHandler.postDelayed(startdiapoRunnable, delayQuestionnementSetting);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("activity", "onpause");
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//tools

    void setScreenMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;


        switch (metrics.densityDpi) {

            case DisplayMetrics.DENSITY_LOW:
                Log.d("dpi", "metrics returned DPI LOW");


            case DisplayMetrics.DENSITY_TV:
                Log.d("dpi", "metrics returned DPI TV");


            case DisplayMetrics.DENSITY_MEDIUM:
                Log.d("dpi", "metrics returned DPI MEDIUM");
                if (screenHeight > screenWidth) {
                                        Log.d("dpi", "we loaded activity_fullscreen");

                    setContentView(R.layout.activity_fullscreen);

                } else {
                    setContentView(R.layout.activity_fullscreen_paysage);
                    Log.d("dpi", "we loaded activity_fullscreen_paysage");

                }
                break;

            case DisplayMetrics.DENSITY_HIGH:
                Log.d("dpi", "metrics returned DPI HIGH");


            case DisplayMetrics.DENSITY_XHIGH:
                Log.d("dpi", "metrics returned DPI XHIGH");
            case DisplayMetrics.DENSITY_XXHIGH:
                Log.d("dpi", "metrics returned DPI XXHIGH");
            case DisplayMetrics.DENSITY_XXXHIGH:
                Log.d("dpi", "metrics returned DPI XXXHIGH");
                if (screenHeight > screenWidth) {
                    Log.d("dpi", "we loaded activity_fullscreenphone");

                    setContentView(R.layout.activity_fullscreenphone);

                } else {
                    Log.d("dpi", "we loaded activity_fullscreen_phonepaysage");

                    setContentView(R.layout.activity_fullscreen_phonepaysage);

                }
            default:
                Log.e("dpi", "metrics returned DPI UNKNONW" + metrics.density);
                Log.e("dpi", "metrics returned DPI UNKNONW" + metrics.densityDpi);
                setContentView(R.layout.activity_fullscreenphone);

                break;

        }


        Log.d("defaultvalue", "screenwidth= " + screenWidth);
        Log.d("defaultvalue", "screenheight= " + screenHeight);

    }

    @Override
    protected void onResume() {
        super.onResume();
        filter = new IntentFilter("dlReceived");
        filter.addAction("dlStarted");
        filter.addAction("dlComplete");
        filter.addAction("filesFound");
        filter.addAction("filesMissing");
        filter.addAction("noJson");
        filter.addAction("imgShown");

        registerReceiver(intentReceiver, filter);
        setScreenMetrics();

        asm = new asyncTaskManager(getApplicationContext(), screenWidth, screenHeight);

       // this.executor = Executors.newFixedThreadPool(1);
        Log.d("activity", "onResume" + getIntent());
        android.app.ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        this.missingFilesNames = new ArrayList<>();
        this.missingFilesNumber = 0;
        this.mHaveInternet = testinternet();

        setScreenMetrics();
        this.imageView= findViewById(R.id.imageView);

        this.mSlideshowFilesNames = new ArrayList<>();
        this.pwa = 0;
        ProgressBar mDlProgressBar = findViewById(R.id.ui_dl_ProgressBar);
        TextView mDLprogressText = findViewById(R.id.ui_dl_progressTextView);
        mDlProgressBar.setIndeterminate(false);

        lm = findViewById(R.id.leftMenuLinearLayout);
        rm = findViewById(R.id.rightMenuLinearLayout);

        makeButtons();
//Runnable version:
// executor.execute(asm.new listImageTask(getApplicationContext().getExternalCacheDir().getAbsolutePath(),mServerDirectoryURL,missingFilesNames, mSlideshowFilesNames));
       //async version:
        asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(this.mServerDirectoryURL);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("ListimageResult", "missing file= " + missingFilesNames.size());


        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("onLongClick", "LONGPRESS");

                toggleFullscreen();
                return true;
            }
        });
        if ((missingFilesNames.size() == 0) && (mSlideshowFilesNames.size() > 0)) {
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY - 10);

            //mHideHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY + 100);





        } else {
            if (this.mHaveInternet) {
                ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.string_wait4dl);
                //Toast.makeText(getApplicationContext(), R.string.string_wait4dl, Toast.LENGTH_LONG).show();
            } else {
                ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.activatewifiandrelauch);
                // Toast.makeText(getApplicationContext(), R.string.string_wait4dl, Toast.LENGTH_LONG).show();
            }
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);
        }

    }

    private void makeImageClickable()
    {
        Log.d("makeimageclickable","image is now clickable");
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        mSlideshowHandler.removeCallbacks(showNextRunnable);
                        mHideHandler.postDelayed(showMenuRunnable, UI_ANIMATION_DELAY);


                }
            });
    }
    //TOOLS FUNCTIONS/.......
    private void toggleFullscreen() {


        /////////
        if (!mFullscreen) {
            Log.d("toggleFullscreen", "mHidePart2Runnable!");
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);


        } else {
            Log.d("toggleFullscreen", "mShowPart2Runnable!");

            mHideHandler.postDelayed(mfullscreenOffRunnable, UI_ANIMATION_DELAY);


        }
    }

    void makeButtons() {
        String[] _buttonNames = {getResources().getString(R.string.buttonLabel_smart)
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
                , getResources().getString(R.string.buttonLabel_girly)
                , getResources().getString(R.string.buttonLabel_tenderly)
                , getResources().getString(R.string.buttonLabel_kind)
                , getResources().getString(R.string.buttonLabel_strong)
        };


        this.mToggleButtonsArrayList = new ArrayList<>();
        //making ALL first

        for (String buttonName : _buttonNames) {

            Button temptg = new Button(this);
            ViewGroup.LayoutParams layoutParams =
                    findViewById(R.id.fakeLinearLayout).getLayoutParams();
            temptg.setBackground(this.getResources().getDrawable(R.drawable.ic_bouttonoff));
            temptg.setText(buttonName);
            temptg.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));


            temptg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
            temptg.setAllCaps(true);
            temptg.setLayoutParams(layoutParams);
            temptg.setPadding(5, 3, 5, 3);

            ////////////////////////////////CRITICAL//////////////////////////////////////
            temptg.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    mSlideshowHandler.removeCallbacks(showimgandrestartdiapoafter2words);
                    mSlideshowHandler.removeCallbacks(startdiapoRunnable);
                    mSlideshowHandler.removeCallbacks(showNextRunnable);
                    mSlideshowHandler.postDelayed(startdiapoRunnable, delayChoixMotsSetting);
                    mSlideshowIsRunning = false;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mCheckedToggleButtonsArrayList.add(((Button) view));

                        if (mCheckedToggleButtonsArrayList.size() > 2) {
                            Log.d("mCheckedToggle", "3 Button pressed");
                            view.setPressed(true);

                            mSlideshowHandler.post(startdiapoRunnable);
                        }
                        Log.d("Pressed", "Button pressed");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d("Pressed", "Button released");
                        if (mCheckedToggleButtonsArrayList.size() == 1) {
                            view.setPressed(true);
                            ((Button) view).setTextColor(Color.BLACK);
                            view.setEnabled(false);
                            Log.d("toggleClick", "toggle 1 buttons ok");


                        } else if (mCheckedToggleButtonsArrayList.size() == 2) {

                            view.setPressed(true);
                            view.setEnabled(false);
                            mCheckedToggleButtonsArrayList.add(((Button) view));
                            ((Button) view).setTextColor(Color.BLACK);

                            Log.d("toggleClick", "toggle 2 buttons ok");
                            mSlideshowHandler.removeCallbacks(startdiapoRunnable);

                            mSlideshowHandler.post(showimgandrestartdiapoafter2words);
                        }
                        view.performClick();
                    }

                    return false;
                }
            });

            //avoid a glitch reloading button
            temptg.setClickable(true);

            mToggleButtonsArrayList.add(temptg);
        }

        int j;
        //for the first 8 button, set in the left menu layout
        for (j = 0; j < _buttonNames.length / 2; j++) {

            lm.addView(mToggleButtonsArrayList.get(j));
        }


        //for the first 8 buttons, set in the right menu layout

        for (; j < _buttonNames.length; j++) {

            rm.addView(mToggleButtonsArrayList.get(j));
        }
    }

    private boolean testinternet() {
        //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        boolean isMetered = cm.isActiveNetworkMetered();
        if (isConnected) {

            return true;

        } else {
            Log.d("NetworkInfo", "pas internet");
            return false;

        }
    }


}