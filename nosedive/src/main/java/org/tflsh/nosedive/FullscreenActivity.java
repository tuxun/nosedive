package org.tflsh.nosedive;

//import android.annotation.SuppressLint;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

//import java.io.InputStreamReader;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {


    final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
    //temps unitaire (base de temps), sert a définir le delai entre deux images
    final int delayinterframes = 750;
    //temps durant lequel onregarde une image proposé apres le menu (en multiple d'interframedelay)
    final int delayquestionnement = 5000;

    //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
    final int delaychoixmots = 10000;

    private final Handler mSlideshowHandler = new Handler();
    private final Runnable showpressmetextRunnable = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
        }
    };
    private final Runnable cleanbuttonRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mCheckedToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons - 1; i >= 0; i--) {

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
    private final Runnable makeButtonNotClickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mToggleButtonsArrayList.size();
            Log.d("makeButtonNotClickable", clickedbuttons + "makeButtonNotClickableRunnable ok");
            for (int i = 0; i < 16; i++) {

                mToggleButtonsArrayList.get(i).setClickable(false);
                mToggleButtonsArrayList.get(i).setPressed(true);

                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    private final Runnable makebuttonclickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = 0; i < 16; i++) {

                mToggleButtonsArrayList.get(i).setClickable(true);
                mToggleButtonsArrayList.get(i).setPressed(false);
                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    //runnnable s'appelant lui meme a la fin du diapo qu'il lance
    int screenHeight;
    ArrayList<String> missingFilesNames;
    IntentFilter filter;
    int missingfilesnumber = 0;
    public BroadcastReceiver intentreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        //    Log.d("FullscreenActivity", "org.tflsh.nosedive.FullscreenActivity.onReceive");
            switch (intent.getAction()) {
                //dlstarted?
                //dlreceived
                //dlcomplete

                case "dlstarted":
               //     Log.e("intentreceiver", "dlstarted action");
                    //  ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementSecondaryProgressBy(1);
                    // findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);
                    findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);


                    missingfilesnumber++;


                    break;
                case "dlreceived":
                    Log.e("intentreceiver", "dlreceived action");
                    ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).incrementProgressBy(1);
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.string_wait4dl);
                    //  findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);

                    break;
                case "dlcomplete":
                //    Log.e("intentreceiver", "dlcomplete action");
                    findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            mSlideshowHandler.removeCallbacks(showNextRunnable);
                            mSlideshowHandler.post(showMenuRunnable);


                        }
                    });
                    missingfilesnumber = 0;
                    findViewById(R.id.ui_dl_progressTextView).setVisibility(View.GONE);
                    findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.GONE);
                    mSlideshowHandler.post(startdiapoRunnable);
                    break;

                case "nojson":
                //    Log.e("intentreceiver", "nojson action" + intent.getIntExtra("EXTRA_MESSAGE", 0));
                    mHaveInternet = false;
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.activatewifiandrelauch);

                    break;

                case "filesfound":
                    //Log.e("intentreceiver", "filesfound action");
                    int max=intent.getIntExtra("EXTRA_MESSAGE", 0);
                    if(max>0) {
                        ((ProgressBar) findViewById(R.id.ui_dl_ProgressBar)).setMax(max);
                        Log.e("intentreceiver", "filesfound set pgbbarr" + max);
                        findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.VISIBLE);
                    }
                    else
                    {                        Log.e("intentreceiver", "filesfound set pgbbarr" + max);
                        Log.e("intentreceiver", "dlcomplete action");
                        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                mSlideshowHandler.removeCallbacks(showNextRunnable);
                                mSlideshowHandler.post(showMenuRunnable);


                            }
                        });
          mSlideshowHandler.post(startdiapoRunnable);

}
                    break;
                case "imgshown":
               //     Log.e("intentreceiver", "imageshownaction action");

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
            //mHideHandler.post(cleanbuttonRunnable);
            //    if (mDLisCompleted) {
            //reset progressBar

            //    mSlideshowHandler.post(makebuttonclickableRunnable);
            mSlideshowHandler.removeCallbacks(showNextRunnable);

            if (missingfilesnumber == 0) {
                if (mSlideshowIsRunning == true) {


                }
                mSlideshowIsRunning = true;
                Log.d("startdiapoRunnable", "start with diapo size=" + mSlideshowFilesNames.size());

                pwa = 0;
                int i, j;
                mHideHandler.postDelayed(hidemenuRunnable, UI_ANIMATION_DELAY);

                for (i = 0; i < mSlideshowFilesNames.size() + 1; i++) {
                    mSlideshowHandler.postDelayed(showNextRunnable, i * delayinterframes);
                }
            } else {
              /*  if (mHaveInternet == true) {
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.string_wait4dl);
                    Toast.makeText(getApplicationContext(), R.string.string_wait4dl, Toast.LENGTH_LONG).show();
                } else {
                    ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.activatewifiandrelauch);
                    Toast.makeText(getApplicationContext(), R.string.string_wait4dl, Toast.LENGTH_LONG).show();
                }*/


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
            mHideHandler.post(showpressmetextRunnable);
int nextimg=new Random().nextInt(mSlideshowFilesNames.size());
            if (mSlideshowIsRunning) {
                if (pwa < mSlideshowFilesNames.size()) {
                    if ((pwa % 2) == 1) {
                          asm.new showImageFileTask(
                                  (ImageView) findViewById(R.id.imageView)
                          ).execute(mSlideshowFilesNames.get(nextimg));
                          ((TextView) findViewById(R.id.ui_press_meTextView)).setTextColor(getResources().getColor(R.color.OurPink));

                    } else {
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
                    mSlideshowHandler.postDelayed(startdiapoRunnable, 0); //end handlepostdelay

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
            ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));

            mSlideshowHandler.post(cleanbuttonRunnable);
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            mSlideshowHandler.removeCallbacks(cleanbuttonRunnable);
            mSlideshowHandler.removeCallbacks(startdiapoRunnable);
            mSlideshowHandler.postDelayed(startdiapoRunnable, delaychoixmots);
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
                mSlideshowHandler.postDelayed(startdiapoRunnable, delaychoixmots);
            }

            //findViewById(R.id.pressme_text).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setText(getResources().getString(R.string.string_press_me));

            findViewById(R.id.leftMenuLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.rightMenuLinearLayout).setVisibility(View.GONE);
            //mHideHandler.post(cleanbuttonRunnable);

            // findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);
            //            findViewById(R.id.fullscreen_mumcontent_controls).setVisibility(View.GONE);
            findViewById(R.id.ui_centralLinearLayout).setVisibility(View.VISIBLE);

        }
    };
    int screenWidth;
    private ArrayList<String> mSums;


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
    private ProgressBar mDiapoProgressBar;//
    //    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private Button prevButton;
    private Button lastButton;
    private boolean mDLisCompleted = false;
    private ArrayList<String> mSlideshowFilesNames;
    private boolean mSlideshowIsRunning = false;
    //REGLAGE DE LAPPLI
    private int currentfile;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //https://www.journaldev.com/9357/android-actionbar-example-tutorial
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("MenuItem", "Selected");

        switch (item.getItemId()) {
            case R.id.add:
                //add the function to perform here
                return (true);
            case R.id.reset:
                //add the function to perform here
                File filetodelete = new File(getExternalCacheDir() + "/filelist.json");
                filetodelete.delete();
                this.onResume();
                return (true);
            case R.id.about:

                Toast.makeText(getApplicationContext(), "plop", Toast.LENGTH_LONG).show();
                return (true);
            case R.id.exit:
                finish();
                System.exit(0);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////ACTIVITY (MAIN)/////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("onConfigurationChanged", "onConfigurationChanged ?");
mSlideshowHandler.removeCallbacks(showNextRunnable);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_fullscreen_paysage);
            this.onResume();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_fullscreen);
            this.onResume();

        }
    }

    //vrac zone
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("onNewIntent", "onNewIntent ?");

        setIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSlideshowHandler.post(stopdiapoRunnable);
        Log.d("activity", "onStop");
      //  unregisterReceiver(intentreceiver);

    }

    private Runnable showimgandrestartdiapoafter2words = new Runnable() {
        @Override
        public void run() {
            Log.d("restartdiapoafter2words", "connexion limité");
            (findViewById(R.id.ui_press_meTextView)).setVisibility(View.GONE);
            mSlideshowHandler.postDelayed(cleanbuttonRunnable, UI_ANIMATION_DELAY);
            mSlideshowHandler.postDelayed(hidemenuRunnable, delayinterframes);

            ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));

            //  rm.setVisibility(8);
            //lm.setVisibility(8);
            asm.new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mSlideshowFilesNames.get(new Random().nextInt(mSlideshowFilesNames.size())));
            SystemClock.sleep(50);

            mSlideshowHandler.postDelayed(startdiapoRunnable, delayquestionnement);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter("dlreceived");
        filter.addAction("dlstarted");
        filter.addAction("dlcomplete");
        filter.addAction("filesfound");
        filter.addAction("filesmissing");
        filter.addAction("nojson");
        filter.addAction("imgshown");

        //  receiver = new MyIntentManager();

        registerReceiver(intentreceiver, filter);
        setScreenMetrics();

        asm = new asyncTaskManager(getApplicationContext(), screenWidth, screenHeight);





     /*   SeekBar seekBar = findViewById(R.id.seekBar);

       // int progress = seekBar.getProgress();
       // tvProgressLabel = findViewById(R.id.textView);
        //tvProgressLabel.setText("Progress: " + progress);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
             //   tvProgressLabel.setText("Progress: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
            }
        };
*/
//        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("activity", "onpause");
    }

//TODO:quand le dl de chaques images est fini on doit lancé le diapo


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//tools

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
                    setContentView(R.layout.activity_fullscreen);

                } else {
                    setContentView(R.layout.activity_fullscreen_paysage);

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


        Log.d("activity", "onResume" + getIntent());
        android.app.ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        this.missingFilesNames = new ArrayList<>();
        this.missingfilesnumber = 0;
        this.mHaveInternet = testinternet();

        //   TextView destiv = findViewById(R.id.pressme_text);
        setScreenMetrics();

        this.mSlideshowFilesNames = new ArrayList<>();
        this.pwa = 0;
        ProgressBar mDlProgressBar = findViewById(R.id.ui_dl_ProgressBar);
        TextView mDLprogressText = findViewById(R.id.ui_dl_progressTextView);
//      mDiapoProgressBar.setProgress(0);
//WXCB        mDiapoProgressBar.setIndeterminate(false);
        mDlProgressBar.setIndeterminate(false);
        String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" +"rescatest/";//+ "julia/";

        View mContentView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content_controls);

        // LinearLayout des = findViewById(R.id.contentMUM);
        lm = findViewById(R.id.leftMenuLinearLayout);
        rm = findViewById(R.id.rightMenuLinearLayout);

        makeButtons();


        // Set up the user interaction to manually show or hide the system UI.

        //c  findViewById(R.id.dummy_button).setOnTouchListener(onclick);
        // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


        asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("ListimageResult", "missing file= " + missingFilesNames.size());


        ////////////////////////
        findViewById(R.id.imageView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("onLongClick", "LONGPRESS");

                togglefullscreen();
                return true;
            }
        });
        if ((missingFilesNames.size() == 0) && (mSlideshowFilesNames.size() > 0)) {
            //cache les mots, lance le diapo
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY - 10);
            mHideHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY + 10);


            findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mSlideshowIsRunning)
                        mSlideshowHandler.post(startdiapoRunnable);

                    else {
                        mSlideshowHandler.removeCallbacks(showNextRunnable);
                        mHideHandler.postDelayed(showMenuRunnable, UI_ANIMATION_DELAY);
                    }

                }
            });


        } else {
         /*   if (mHaveInternet == true) {
                ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.string_wait4dl);
            } else {
                ((TextView) (findViewById(R.id.ui_press_meTextView))).setText(R.string.activatewifiandrelauch);
            }*/
//cache les mots
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);
        }

    }

    //TOOOLS FUNCTIONS/.......
    private void togglefullscreen() {


        /////////
        if (!mFullscreen) {
            Log.d("togglefullscreen", "mHidePart2Runnable!");
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);


        } else {
            Log.d("togglefullscreen", "mShowPart2Runnable!");

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

        int[] _buttonprimalnumbers = {3, 5, 7, 11, 13, 17, 19, 23, 27, 29, 31, 37, 41, 43, 47, 53, 59};

        this.mToggleButtonsArrayList = new ArrayList<>();
//how to get every button...
        //making ALL first


        for (int j = 0; j < _buttonNames.length; j++) {

            Button temptg = new Button(this);
            ViewGroup.LayoutParams layoutParams = findViewById(R.id.fakeLinearLayout).getLayoutParams();
            temptg.setBackground(this.getResources().getDrawable(R.drawable.ic_bouttonoff));
            temptg.setText(_buttonNames[j]);
            //temptg.setTextOn(_buttonNames[j]);
            //
            temptg.setAllCaps(true);
            temptg.setLayoutParams(layoutParams);
            temptg.setPadding(10, 10, 10, 10);

            temptg.setTag(_buttonprimalnumbers[j]);
              /*  android:layout_width="match_parent"
                    android:layout_height="wrap_content"*/

            ////////////////////////////////////////////////////////////////////////////////////////////////
            temptg.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    mSlideshowHandler.removeCallbacks(showimgandrestartdiapoafter2words);
                    mSlideshowHandler.removeCallbacks(showNextRunnable);

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mCheckedToggleButtonsArrayList.add(((Button) view));


                        if (mCheckedToggleButtonsArrayList.size() > 2) {
                            Log.d("mCheckedToggle", "3 Button pressed");
                            view.setPressed(true);
                            // mCheckedToggleButtonsArrayList.clear();

                            mSlideshowHandler.post(showimgandrestartdiapoafter2words);


                        }
                        Log.d("Pressed", "Button pressed");
                        //  mHideHandler.post(makeButtonNotClickableRunnable);

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.d("Pressed", "Button released");
                        if (mCheckedToggleButtonsArrayList.size() == 1) {
                            Log.d("toggleclick", "toggle 1 boutons ok" + view.getTag().toString());

                            lastButton = (Button) view;
                            view.setPressed(true);
                            ((Button) view).setTextColor(Color.BLACK);
                            view.setEnabled(false);

                        } else if (mCheckedToggleButtonsArrayList.size() == 2) {
                            view.setPressed(true);
                            view.setEnabled(false);
                            mCheckedToggleButtonsArrayList.add(((Button) view));
                            ((Button) view).setTextColor(Color.BLACK);

//mCheckedToggleButtonsArrayList.get(0).setPressed(false);
                            Log.d("toggleclick", "toggle 2 boutons ok");

                            // mSlideshowHandler.post(makeButtonNotClickableRunnable);

                            mSlideshowHandler.post(showimgandrestartdiapoafter2words);


                        }


                    }

                    return false;
                }

            });


            //avoid a glitch reloading button
            temptg.setClickable(true);//                    setChecked(false);

            mToggleButtonsArrayList.add(temptg);

        }

        int j;
        //for the first 8 buttton, set in leftmenulayout
        for (j = 0; j < _buttonNames.length / 2; j++) {
            mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));

            lm.addView(mToggleButtonsArrayList.get(j));
        }


        //for the first 8 buttton, set in the right menu layout

        for (; j < _buttonNames.length; j++) {
            mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.alef));

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
            if (isMetered) {
                Log.d("NetworkInfo", "connexion limité");
                return false;
            } else {
                Log.d("NetworkInfo", "pas de limite de débit");
                return false;
            }

        } else {
            Log.d("NetworkInfo", "pas internet");
            return false;

        }
    }


}





/*
 @Override
                public void onClick(View view) {
                    mDiapoHandler.removeCallbacks(showNextRunnable);
                    mDiapoHandler.removeCallbacks(startdiapoRunnable);
                    mdiapo_isrunning = false;
                   // SystemClock.sleep(100);

                        ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));


                    mCheckedToggleButtonsArrayList.add(((Button) view));
                    view.setClickable(false);

                    //  ((ToggleButton)view.findViewWithTag("toggleButton")).setChecked(false);
                    if (mCheckedToggleButtonsArrayList.size() == 1) {
                        Log.d("toggleclick", "toggle 1 boutons ok" + view.getTag().toString());
mHideHandler.removeCallbacks(cleanbuttonRunnable);
                        mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);
                        mHideHandler.postDelayed(cleanbuttonRunnable, delayquestionnement+UI_ANIMATION_DELAY);

return;

                    } else if (mCheckedToggleButtonsArrayList.size() == 2) {
                        //if its the second time we click on a button
//if it's the same button twice, uncheck it and return
//                        lastButton = ((ToggleButton) view);
                        if (mCheckedToggleButtonsArrayList.get(0) == mCheckedToggleButtonsArrayList.get(1)) {
                            //mHideHandler.post(cleanbuttonRunnable);

                            Log.d("toggleclick", "toggle twice SAME button");
                         //   mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(1));
                           // mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(0));

                            //prevButton.setChecked(false);
                            mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);

                        } else {
                            (findViewById(R.id.pressme_text)).setVisibility(View.GONE);
                            mHideHandler.post(hidemenuRunnable);

                            Log.d("toggleclick", "toggle 2 boutons ok");
                            mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);

                            new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(new Random().nextInt(mDiapo.size())));
                            ;
                        }
                    }

                    int clickedbuttons = mCheckedToggleButtonsArrayList.size();
if(clickedbuttons>0) {
    for (int i = clickedbuttons-1 ; i >=0; i--) {

        mCheckedToggleButtonsArrayList.get(i).setEnabled(false);
        mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
    }
    //a chaque button cliqué, si on est perdu, on decheck les bouttons
    //should only  happen when 2 DIFFERENT buttons are pressed
    //  ((ToggleButton) view).setChecked(true);
    mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);
}
                }
            });



          //avoid a glitch reloading button
            temptg.setClickable(true);//                    setChecked(false);

            mToggleButtonsArrayList.add(temptg);

        }


        //for the first 8 buttton, set in leftmenulayout
        for (j = 0; j < _buttonNames.length / 2; j++) {
            mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.alef));

            lm.addView(mToggleButtonsArrayList.get(j));
        }


        //for the first 8 buttton, set in the right menu layout

        for (; j < _buttonNames.length; j++) {
            mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(),R.font.alef));

            rm.addView(mToggleButtonsArrayList.get(j));
        }


        // Set up the user interaction to manually show or hide the system UI.

        //c  findViewById(R.id.dummy_button).setOnTouchListener(onclick);
        // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

 */
