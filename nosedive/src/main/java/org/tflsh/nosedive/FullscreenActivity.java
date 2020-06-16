package org.tflsh.nosedive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

//import java.io.InputStreamReader;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {


    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ATTRIBUTES////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Handler mDiapoHandler = new Handler();
    //REGLAGE DE LAPPLI
    //private final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/res/";
    private final String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/"+"julia/";
    private final boolean debuglayoutmode = false;
    private final Runnable showpressmetextRunnable = new Runnable() {
        @Override
        public void run() {
            ((TextView) findViewById(R.id.pressme_text)).setVisibility(View.VISIBLE);
        }
    };

    private final Runnable hidedebugmenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("hidedebugmenuRunnable", "visible ?");

            findViewById(R.id.DLprogress_text).setVisibility(View.GONE);
            findViewById(R.id.DLprogressbar).setVisibility(View.GONE);
            if (!debuglayoutmode) {
                findViewById(R.id.progressbar).setVisibility(View.GONE);
            }


        }
    };



    private final Runnable hidemenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("hidemenurunnable", "visible ?");
            int clickedbuttons = mCheckedToggleButtonsArrayList.size();
            if(clickedbuttons>0) {
                for (int i = clickedbuttons-1 ; i >=0; i--) {

                    mCheckedToggleButtonsArrayList.get(i).setEnabled(true);
                    mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
                }
                //a chaque button cliqué, si on est perdu, on decheck les bouttons
                //should only  happen when 2 DIFFERENT buttons are pressed
                //  ((ToggleButton) view).setChecked(true);
                mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);
            }

            //findViewById(R.id.pressme_text).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.pressme_text)).setText(getResources().getString(R.string.string_press_me));

            findViewById(R.id.leftmenu).setVisibility(View.GONE);
            findViewById(R.id.rightmenu).setVisibility(View.GONE);
            //mHideHandler.post(cleanbuttonRunnable);

            // findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);
            //            findViewById(R.id.fullscreen_mumcontent_controls).setVisibility(View.GONE);
            findViewById(R.id.fullscreen_mumcontent_controls).setVisibility(View.VISIBLE);

        }
    };
    public int pwa;
    int screenwidth;
    int screenheight;
    //temps unitaire (base de temps), sert a définir le delai entre deux images
    int delayinterframes = 1000;
    //temps durant lequel onregarde une image proposé apres le menu (en multiple d'interframedelay)
    int delayquestionnement = 5 * delayinterframes;
    //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
    int delaychoixmots = 2 * delayquestionnement;
    //used for show words?.
    private final Runnable showmenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("showmenuRunnable", "showmenuRunnable ?");

            mDiapoHandler.post(cleanbuttonRunnable);
            mDiapoHandler.removeCallbacks(showNextRunnable);
            mDiapoHandler.removeCallbacks(cleanbuttonRunnable);
            mDiapoHandler.removeCallbacks(startdiapoRunnable);
            mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);
            findViewById(R.id.fullscreen_mumcontent_controls).setVisibility(View.GONE);
            findViewById(R.id.leftmenu).setVisibility(View.VISIBLE);
            findViewById(R.id.rightmenu).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.pressme_text)).setText(R.string.string_choose2word);
        //findViewById(R.id.pressme_text).setVisibility(View.VISIBLE);


        }
    };


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("onConfigurationChanged", "onConfigurationChanged ?");

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_fullscreen_paysage);
this.onResume();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_fullscreen);
            this.onResume();

        }
    }




    ArrayList<String> missingfiles;
    TextView tvProgressLabel;
    private View mContentView;
    private ArrayList<String> mDiapo;
    private ArrayList<String> mSums;
    private ProgressBar mDLprogressBar;//
    private TextView mDLprogressText;//
    private ProgressBar mDiapoProgressBar;//
    private boolean mdiapo_isrunning = false;
    private final Runnable showNextRunnable = new Runnable() {
        @Override
        public void run() {
            //antibounce
            mDiapoHandler.removeCallbacks(startdiapoRunnable);
            // Log.d("showNextRunnable", "next!");
            mHideHandler.post(showpressmetextRunnable);

            if (mdiapo_isrunning) {
                if (pwa < mDiapo.size()) {

                    //dans l'ordre
                    new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(pwa));
                    //en pseudo random
                    //new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(new Random().nextInt(mDiapo.size())));

                    // Log.d("diapo", "showing " + mDiapo.get(pwa));
                    pwa++;
                } else {
                    Log.d("pwa", "no more pwa");
                    pwa = 0;
                    mDiapoHandler.postDelayed(startdiapoRunnable, delayinterframes); //end handlepostdelay

                    // pgb.setProgress(pwa);
                    //mdiapo_isrunning=false;
                    // Log.d("pwa","we added pwa for keep the diapo going... and going... and going...");
                }

            }
        }

        // Code here will run in UI thread


    };
    //important: everytime we stop diapo, we show menu
    private final Runnable stopdiapoRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("runnable", "stop!");
            mdiapo_isrunning = false;
            mDiapoHandler.removeCallbacks(showNextRunnable);
            mDiapoHandler.removeCallbacks(startdiapoRunnable);


        }
    };
    private boolean mTextBlink = false;
    private int currentfile;
    private LinearLayout lm;
    private LinearLayout rm;
    // private static final boolean AUTO_HIDE = true;
    private boolean mFullscreen;
    private final Runnable mfullscreenOffRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();


            }
            //  mContentView.getLayoutParams().notifyAll();

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            (findViewById(R.id.nosedive)).setFitsSystemWindows(true);


            // mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            Log.e("mShowPart2Runnable", "EXITFULLSCREEN");
            mFullscreen = false;

            ((LinearLayout) findViewById(R.id.fullscreen_content_controls)).setVisibility(View.VISIBLE);

        }
    };
    //    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private Button prevButton;
    private Button lastButton;
    private boolean mDLisCompleted = false;        //runnnable s'appelant lui meme a la fin du diapo qu'il lance
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////RUNNABLES////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    private final Runnable mfullscreenOnRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
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
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            lm.setVisibility(View.GONE);
            rm.setVisibility(View.GONE);
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            |View.SYSTEM_UI_FLAG_LOW_PROFILE
                    // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            ((LinearLayout) findViewById(R.id.windowLayout)).setFitsSystemWindows(false);

           /* findViewById(R.id.windowLayout).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    */

            if (mDLisCompleted) {
                //  findViewById(R.id.progressbar).setVisibility(View.GONE);

                //((TextView) findViewById(R.id.pressme_text)).setVisibility(View.GONE);
                ((ProgressBar) findViewById(R.id.progressbar)).setVisibility(View.GONE);
                // ((TextView) findViewById(R.id.fullshcreen_text)).setVisibility(View.GONE);
                //((TextView) findViewById(R.id.fullscreen_text)).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.DLprogress_text)).setVisibility(View.GONE);
                ((ProgressBar) findViewById(R.id.DLprogressbar)).setVisibility(View.GONE);
            }
            else {
                ((TextView) findViewById(R.id.DLprogress_text)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.DLprogress_text)).setVisibility(View.VISIBLE);
                findViewById(R.id.debug_content_controls).setVisibility(View.VISIBLE);
            }
        }
    };
    //end runnables list


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////ACTIVITY (MAIN)/////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final Runnable startdiapoRunnable = new Runnable() {

        @Override
        public void run() {
            //mHideHandler.post(cleanbuttonRunnable);
                    if (mDLisCompleted) {
                //reset progressBar
                // ((TextView) findViewById(R.id.fullscreen_text)).setText("Mots");
                mDiapoHandler.removeCallbacks(showNextRunnable);
                Log.d("startdiapoRunnable", "start!");

                mdiapo_isrunning = true;
                pwa = 0;
                mDiapoProgressBar.setProgress(0);
                //pgb.setBackgroundColor(0);
                mDiapoProgressBar.setMax(mDiapo.size());


                int i = 0;
                mHideHandler.postDelayed(hidemenuRunnable, UI_ANIMATION_DELAY);

                for (i = 0; i < mDiapo.size() + 1; i++) {
                    mDiapoHandler.postDelayed(showNextRunnable, i * delayinterframes);
                } //end for
                //Log.d("startdiapo", "stop!");

                //   mDiapoHandler.postDelayed(startdiapoRunnable, i * ut+ut); //end handlepostdelay


            } else {
                if(mNoInternet==true) {
                    Toast.makeText(getApplicationContext(), "impossible de récup " + mServerDirectoryURL + "index.php, veuillez  activer le WIFI et relancer l'appli", Toast.LENGTH_LONG).show();
                }
                else {

                Toast.makeText(getApplicationContext(), "Patientez pendant le téléchargement des images", Toast.LENGTH_LONG).show(); }
                Log.e("startdiaporunnable", "mais missingimgs! (showing toast)");
            }
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
            File filetodelete= new File(getExternalCacheDir() + "/filelist.json");
                filetodelete.delete();
                this.onResume();
            return (true);
            case R.id.about:

                Toast.makeText(getApplicationContext(), "plop", Toast.LENGTH_LONG).show();
                return (true);
            case R.id.exit:
                finish();System.exit(0);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
      ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<Button>();
    ArrayList<Button> mToggleButtonsArrayList = new ArrayList<Button>();

    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private final Runnable cleanbuttonRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons=mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons-1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(true);
                mToggleButtonsArrayList.get(i).setClickable(true);
                mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };

    private final Runnable makebuttonclickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons=mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons-1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(true);
                //mToggleButtonsArrayList.get(i).setClickable(true);
                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    private final Runnable makebuttonNOTclickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons=mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons-1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(false);
                //mToggleButtonsArrayList.get(i).setClickable(true);
                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };


    @Override protected   void onStop() {
        super.onStop();
        mDiapoHandler.post(stopdiapoRunnable);
        Log.d("activity", "onstop");


    }
    @Override protected   void onResume() {
        super.onResume();
        Log.d("activity", "onResume");
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);


        //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        boolean isMetered = cm.isActiveNetworkMetered();
        if(isConnected)
        {
            Log.d("NetworkInfo", "internet ok");
            if(isMetered)
            {
                Log.d("NetworkInfo", "mais connexion limité");

            }
            else
            {
                Log.d("NetworkInfo", "pas de limite de débit (youpi!)");

            }

        }else
        {
            Log.d("NetworkInfo", "pas internet");

        }



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

        screenwidth = metrics.widthPixels;
        screenheight = metrics.heightPixels;
  /*      if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
*/
        if(screenheight>screenwidth  ) {
            setContentView(R.layout.activity_fullscreen);

        }
        else
        {
            setContentView(R.layout.activity_fullscreen_paysage);

        }
        mContentView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content_controls);

        // LinearLayout des = findViewById(R.id.contentMUM);
        lm = findViewById(R.id.leftmenu);
        rm = findViewById(R.id.rightmenu);
        String[] _buttonNames = { getResources().getString(R.string.buttonlabel_smart)
                ,getResources().getString(R.string.buttonlabel_lovely)
                ,getResources().getString(R.string.buttonlabel_fun)
                ,getResources().getString(R.string.buttonlabel_sportive)
                ,getResources().getString(R.string.buttonlabel_natural)
                ,getResources().getString(R.string.buttonlabel_sexy)
                ,getResources().getString(R.string.buttonlabel_surprising)
                ,getResources().getString(R.string.buttonlabel_romantic)
                ,getResources().getString(R.string.buttonlabel_pretty)
                ,getResources().getString(R.string.buttonlabel_seductive)
                ,getResources().getString(R.string.buttonlabel_hungry)
                ,getResources().getString(R.string.buttonlabel_polite)
                ,getResources().getString(R.string.buttonlabel_girly)
                ,getResources().getString(R.string.buttonlabel_tenderly)
                ,getResources().getString(R.string.buttonlabel_kind)
                ,getResources().getString(R.string.buttonlabel_strong)
        };

        int[] _buttonprimalnumbers = {3, 5, 7, 11, 13, 17, 19, 23, 27, 29, 31, 37, 41, 43, 47, 53, 59};

        final ArrayList<Button> mToggleButtonsArrayList = new ArrayList<Button>();
//how to get every button...
        //making ALL first

        int j = 0;
        for (j = 0; j < _buttonNames.length; j++) {

            Button temptg = new Button(this);
            ViewGroup.LayoutParams layoutParams=findViewById(R.id.fakelayout).getLayoutParams();
            temptg.setBackground(this.getResources().getDrawable(R.drawable.ic_bouttonoff));
            temptg.setText(_buttonNames[j]);
            //temptg.setTextOn(_buttonNames[j]);
            //
            temptg.setAllCaps(true);
            temptg.setLayoutParams(layoutParams);
            temptg.setPadding(10,10,10,10);

            temptg.setTag(_buttonprimalnumbers[j]);
              /*  android:layout_width="match_parent"
                    android:layout_height="wrap_content"*/

            ////////////////////////////////////////////////////////////////////////////////////////////////
            temptg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDiapoHandler.removeCallbacks(showNextRunnable);
                    mDiapoHandler.removeCallbacks(startdiapoRunnable);

                    ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));


                    mdiapo_isrunning = false;
                    // SystemClock.sleep(100);

                    //  ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));




                    //  ((ToggleButton)view.findViewWithTag("toggleButton")).setChecked(false);
                    if (mCheckedToggleButtonsArrayList.size() == 0) {
                        Log.d("toggleclick", "toggle 1 boutons ok" + view.getTag().toString());
                        mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);
                        // mHideHandler.postDelayed(cleanbuttonRunnable, delayquestionnement+UI_ANIMATION_DELAY);
                        view.setPressed(true);
                        view.setEnabled(false);
                        lastButton = (Button) view;

                        mCheckedToggleButtonsArrayList.add(((Button) view));
                        return;

                    } else if (mCheckedToggleButtonsArrayList.size() == 1) {
                        //if its the second time we click on a button
//if it's the same button twice, uncheck it and return
//                        lastButton = ((ToggleButton) view);
                        lastButton.setEnabled(true);
                        mCheckedToggleButtonsArrayList.add(((Button) view));

                        if(mCheckedToggleButtonsArrayList.get(0)==mCheckedToggleButtonsArrayList.get(1))
                        {
                            Log.d("mCheckedToggleButtons", "same BUTTON AND FUCK");
                        }
                        (findViewById(R.id.pressme_text)).setVisibility(View.GONE);
                        mHideHandler.post(hidemenuRunnable);
//mCheckedToggleButtonsArrayList.get(0).setPressed(false);

                        Log.d("toggleclick", "toggle 2 boutons ok");
                        mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);

                        //mHideHandler.post(cleanbuttonRunnable);
                        // mHideHandler.post(makebuttonclickableRunnable);
                        mHideHandler.post(makebuttonNOTclickableRunnable);
                        new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(new Random().nextInt(mDiapo.size())));
                        ;
                        return;
                    }

/*
                    int clickedbuttons = mToggleButtonsArrayList.size();
                    if (clickedbuttons > 0) {
                        for (int i = clickedbuttons - 1; i >= 0; i--) {

                            mToggleButtonsArrayList.get(i).setEnabled(true);
                            mToggleButtonsArrayList.get(i).setClickable(true);
                            mToggleButtonsArrayList.remove(mToggleButtonsArrayList.get(i));
                        }
                        //a chaque button cliqué, si on est perdu, on decheck les bouttons
                        //should only  happen when 2 DIFFERENT buttons are pressed
                        //  ((ToggleButton) view).setChecked(true);
                        mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);
                    */
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


        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mdiapo_isrunning)
                    mDiapoHandler.post(startdiapoRunnable);

                else {
                    mDiapoHandler.removeCallbacks(showNextRunnable);
                    mHideHandler.postDelayed(showmenuRunnable, UI_ANIMATION_DELAY);
                }

            }
        });


        ////////////////////////
        findViewById(R.id.imageView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("onLongClick", "LONGPRESS");

                togglefullscreen();
                return true;
            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.


        //   TextView destiv = findViewById(R.id.pressme_text);

        //todo
        //refactor for use constant
        //  String jsonfilename;



        //  this.missingfilessize = 0;

// since SDK_INT = 1;

        Log.d("defaultvalue", "screenwidth= " + screenwidth);
        Log.d("defaultvalue", "screenheight= " + screenheight);


        //Bitmap mIcon11;
        mDiapoProgressBar = findViewById(R.id.progressbar);
        mDLprogressBar = findViewById(R.id.DLprogressbar);
        mDLprogressText = findViewById(R.id.DLprogress_text);







        this.mDiapo = new ArrayList<String>();
        this.mSums = new ArrayList<String>();
        this.missingfiles = new ArrayList<String>();

        new ListImageTask(mSums, mDiapo).execute(mServerDirectoryURL);

        this.pwa = 0;

        Log.d("ListimageResult", "missing file= " + missingfiles.size());
        mDiapoProgressBar.setProgress(0);
        mDiapoProgressBar.setIndeterminate(false);
        mDLprogressBar.setIndeterminate(false);

        if (mDLisCompleted)
        {
            //cache les mots, lance le diapo
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);
            mHideHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY);
        }
        else
        {
            //on a des dl a faire
            //cache "pressme"
            (findViewById(R.id.pressme_text)).setVisibility(View.GONE);
//cache les mots
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);
        }
//TODO:quand le dl de chaques images est fini on doit lancé le diapo
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//tools

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public /*static*/ int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromFilepath(String res,
                                                  int reqWidth,
                                                  int reqHeight) throws IOException {
               final BitmapFactory.Options options = new BitmapFactory.Options();
                      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        FileInputStream fis =new FileInputStream(res);
        BufferedInputStream bis = new BufferedInputStream(fis);
        Bitmap result= BitmapFactory.decodeStream(bis, null, options);
        fis.close();
        return      result;


    }

    public /*static*/ Bitmap olddecodeSampledBitmapFromFilepath(String res,
                                                                int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
//    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //              options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ASYNCTASK////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    private class showImageFileTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;
        String name;

        public showImageFileTask(ImageView bbmImage) {
            this.bmImage = bbmImage;
            //   this.name=namee;
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                return BitmapFactory.decodeFile(getExternalCacheDir() + "/" + urls[0]);
            }
            catch (Exception e)
            {
                Log.e("fileeroor","inshowimgtask");
                return null;

            }
        }

        protected void onPostExecute(Bitmap result) {

            //((TextView) findViewById(R.id.pressme_text)).setText("Diapo " + (pwa + 1) + "/" + mDiapo.size() + "réussi");
            //!log bcp  Log.d("showimageTask","Diapo " + (pwa ) + "/" + mDiapo.size() + "réussi");

            //((TextView)findViewById(R.id.pressme_text)).setText(getResources().getString(R.string.string_press_me)
            mDiapoProgressBar.incrementProgressBy(1);
            bmImage.setImageBitmap(result);
            if(mTextBlink)
            {mTextBlink=false;
                ((TextView) findViewById(R.id.pressme_text)).setTextColor(getResources().getColor(R.color.OurPink));}
            else{mTextBlink=true;
                ((TextView) findViewById(R.id.pressme_text)).setTextColor(getResources().getColor(R.color.colorAccent));}


            //SystemClock.sleep(1000);

        }
    }

    //grab from URL and save on sdcard the file DESTNAME
    private class grabImageTask extends AsyncTask<String, Void, File> {
        private static final String TAG = "grabImageTask";

        final String destifileImage;
            String urldisplay;

        //TODO check the mess with the files
        public grabImageTask(String destname) {

            this.destifileImage = destname;
            urldisplay = mServerDirectoryURL+destname;
    }

    protected File doInBackground(String... urls) {
            //  mDLprogressBar.incrementSecondaryProgressBy(1);

//            mDLprogressBar.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
            // SystemClock.sleep(5);

            File file = new File(getExternalCacheDir() + "/" + destifileImage);
            Bitmap mIcon11 = null;
            try {
                // this.destifileImage.createNewFile();
                if (urldisplay.contains("json")) {
                    Log.e("MEGAWWARN", "on decompresse le json comme une image :/");
                }
                //InputStream is = new java.net.URL(urldisplay).openStream();
                BufferedInputStream bis = new BufferedInputStream(new java.net.URL(urldisplay).openStream());
                //

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
                options.outWidth=screenwidth;
                options.outHeight=screenheight;//screenheight;
                mIcon11 = BitmapFactory.decodeStream(bis,null,options);

                bis.close();


                if (mIcon11 != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    //result.copyPixelsToBuffer((Buffer)buf);//result=//get //compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                    mIcon11.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bos.close();

                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(bitmapdata);
                    fos.close();
                    mIcon11.recycle();

//result.recycle();


                } else {
                    Log.e("error", "skiping one empty file:" + destifileImage);
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "pas internet pour recup le fichier " + destifileImage);
                //popup : pas internet
                //e.printStackTrace();

                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "pb pour ouvrir le fichier json ou sauver limage" + e.getMessage());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return file;

        }

        protected void onPostExecute(File result) {

            if (!result.exists()) {
                Log.e("error", "FAILED ! writen file " + result.getAbsolutePath());
            } else {
                Log.d("GRABtaskPOST", "ok synchonizing " + currentfile + " of " + missingfiles.size()+" "+result.getAbsolutePath());
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
                mDLprogressText.setText("Téléchargement " + currentfile + "/" + missingfiles.size() + "réussi");



                //  mDLprogressBar.incrementSecondaryProgressBy(1);

                mDLprogressBar.setProgress(currentfile);
                mDLprogressBar.setSecondaryProgress(currentfile+1);


            }
            currentfile++;
            if (currentfile == missingfiles.size()) {
                mDLisCompleted = true;
                mDiapoHandler.post(startdiapoRunnable);
                Log.d("GRABtaskPOST", "lastfile, on lance le diapo");
                mDLprogressText.setText("Téléchargement complet");
                mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

            }

            // result.recycle();
            // this.destifileImage.write(result);
        }



    }

    boolean mNoInternet=false;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //grab from URL and save on sdcard the file DESTNAME



    private class grabImageThread implements Runnable {
        private static final String TAG = "grabImageTask";
    private String command;
        final String destifileImage;
        String urldisplay;

        //TODO check the mess with the files
        public grabImageThread(String destname) {

            this.destifileImage = destname;
            urldisplay = mServerDirectoryURL+destname;
        }

        protected boolean doInBackground() {
            //  mDLprogressBar.incrementSecondaryProgressBy(1);

//            mDLprogressBar.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
            // SystemClock.sleep(5);

            File file = new File(getExternalCacheDir() + "/" + destifileImage);
            Bitmap mIcon11 = null;
            try {
                // this.destifileImage.createNewFile();
                if (urldisplay.contains("json")) {
                    Log.e("MEGAWWARN", "on decompresse le json comme une image :/");
                }
                else
                {
                    Log.e("grabImageThread", "on decompresse "+urldisplay);

                }
                //InputStream is = new java.net.URL(urldisplay).openStream();
                BufferedInputStream bis = new BufferedInputStream(new java.net.URL(urldisplay).openStream());
                //

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(options, screenwidth, screenheight);
                options.outWidth=screenwidth;
               options.outHeight=screenheight;//screenheight;

                mIcon11 = BitmapFactory.decodeStream(bis,null,options);
                bis.close();


                if (mIcon11 != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    //result.copyPixelsToBuffer((Buffer)buf);//result=//get //compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
                    mIcon11.compress(Bitmap.CompressFormat.JPEG, 80 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bos.close();

                    FileOutputStream fos = new FileOutputStream(file);

                    fos.write(bitmapdata);
                    fos.close();
                    mIcon11.recycle();

//result.recycle();


                } else {
                    Log.e("error", "skiping one empty file:" + destifileImage);
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "pas internet pour recup le fichier " + destifileImage);
                //popup : pas internet
                //e.printStackTrace();

                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "pb pour ouvrir le fichier json ou sauver limage" + e.getMessage());
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            currentfile++;
            if (currentfile == missingfiles.size()) {
                mDLisCompleted = true;
                mDiapoHandler.post(startdiapoRunnable);
                Log.d("GRABtaskPOST", "lastfile, on lance le diapo");
//!                mDLprogressText.setText("Téléchargement complet");
                mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

            }
            if (!file.exists()) {
                Log.e("error", "FAILED ! writen file " + file.getAbsolutePath());
                return false;
            } else {
                Log.d("GRABtaskPOST", "ok synchonizing " + currentfile + " of " + missingfiles.size()+" "+file.getAbsolutePath());
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
              return true;


            }


            // result.recycle();
            // this.destifileImage.write(result);
        }


        @Override
        public void run() {
            Log.d(TAG, "run" );
            if(this.doInBackground()) {

runOnUiThread(new Runnable() {
    @Override
    public void run() {
          mDLprogressBar.incrementSecondaryProgressBy(1);
                mDLprogressText.setText("Téléchargement " + currentfile + "/" + missingfiles.size() + "réussi");
        Toast.makeText(getApplicationContext(), "récup "+currentfile+", ok", Toast.LENGTH_SHORT).show();

        mDLprogressBar.setProgress(currentfile);
         mDLprogressBar.setSecondaryProgress(currentfile + 1);
    }
});

            }
            else {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });




            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private  class  ListImageTask extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {
        private static final String TAG = "ListImageTask";
        ArrayList<String> name;
        ArrayList<String> sums;
        ExecutorService executor = Executors.newFixedThreadPool(1);

        public ListImageTask(ArrayList<String> sumss, ArrayList<String> img) {
            this.sums = sumss;
            this.name = img;
        }

        //return a array of string, naming the files downloaded, or found in the cache dir
        //@url: basestring to construct files url
        protected ArrayList<String> doInBackground(String... urls) {
            String urldisplay = urls[0] + "index.php";
            try {
                //si la list des fichiers n'existe pas, on la recupere et on l'enregistre
                File localjsonfile = new File(getExternalCacheDir() + "/" + "filelist.json");


                if (!localjsonfile.exists()) {
                    if (localjsonfile.createNewFile()) {
//ByteArrayOutputStream bos=new ByteArrayOutputStream();
                        byte[] bitmapdata = new byte[1024];
                        //  is.read(bitmapdata);

                        //  byte[] jsondata = new byte[0];
                        int read = 0;

                        Log.d(TAG, "downloading filelist.jsonn from " + urldisplay);


                        OutputStream fos = new FileOutputStream(localjsonfile);

                        InputStream is = new java.net.URL(urldisplay).openStream();

                        while ((read = is.read(bitmapdata)) != -1) {
                            fos.write(bitmapdata, 0, read);
                            Log.d(TAG, "downloading filelist.jsonn from loop");
                        }
                        Log.d(TAG, "downloading filelist.jsonn ok ");
                       SystemClock.sleep(1000);

                        fos.flush();
                        fos.close();
                    }           //  is.reset();
                    if (!localjsonfile.exists()) {
                        throw new IOException();
                    }
                }
//TODO file can be empty if internet failed
                Log.d(TAG, "opening" + localjsonfile.getAbsolutePath()+" of size "+localjsonfile.length());

                    InputStream i2s = new FileInputStream(localjsonfile.getAbsolutePath());


                    //une fois le fichier recupéré, on peut l'ouvrir

                    JsonReader reader = new JsonReader(new InputStreamReader(i2s));

                    //fis.close();
if(localjsonfile.length()>0)
{
                    reader.beginArray();

                    String description = "";
                    while (reader.hasNext()) {
                        boolean mssgfile = false;
                        reader.beginObject();

                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            // Log.e("key:", key);
                            if (key.equals("name")) {
                                String newIn = reader.nextString();
                                //TODO: should check if we already downloaded the files
                                //need context, really?

                                //File file;// = new File(getExternalFilesDir(null), newIn);
                                File file = new File(getExternalCacheDir() + "/" + newIn);
                                if (!file.exists()) {
                                    Log.d("ListImageTask", "caching file " + newIn + " to " + getExternalCacheDir());
                                    this.name.add(newIn);
                                    mDLisCompleted = false;


                                    executor.execute(new grabImageThread(newIn));

                                    //new grabImageTask(newIn).execute(mServerDirectoryURL + newIn).executeOnExecutor(THREAD_POOL_EXECUTOR,singlr);



                                    missingfiles.add(newIn);


                                    // Log.e("error", "FAILED ! writen file " +testfile.getAbsolutePath() );
                                } else {
                                    //!log bcp Log.d("ListImageTask", "file " + newIn + " already found to " + getExternalCacheDir());
                                    this.name.add(newIn);
                                    //  mDLisCompleted = true;
                                    //Log.e("found_file:", newIn);
                                }
                            } else if (key.equals("size")) {

                                int filesize = reader.nextInt();
                                //    missingfilessize += filesize;
                                //  Log.d("ListImageTask", "added " + filesize + " Bytes to download for " + this.name.get(this.name.size() - 1));
                                // reader.skipValue();

                            } else if (key.equals("sum")) {
                                this.sums.add(reader.nextString());
                            } else {
                                reader.skipValue();


                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                    return this.name;

} else {
        return null;
}




            } catch (FileNotFoundException e) {
                Log.e(TAG, "fichier json local non trouvé");

                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(TAG, "mauvaise url");

                e.printStackTrace();
            }  catch (IOException e) {
                Log.e(TAG, "fichier json internet non trouvé ou malformé");
                //popup : pas internet
                e.printStackTrace();

                // e.printStackTrace();
            } catch (Exception e) {
                Log.e("UnexpectedError", e.getMessage());
                e.printStackTrace();
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Finished all threads");
            }
            return this.name;
        }
        protected void onPostExecute(ArrayList<String> result) {
if(result!=null)
{
            if (result.size() == 0) {
                Toast.makeText(getApplicationContext(), "impossible de récup "+mServerDirectoryURL+"index.php, veuillez  activer le WIFI et relancer l'appli", Toast.LENGTH_LONG).show();
mNoInternet=true;

                Log.e(TAG, "aucun resultat (impossible de creer le fichier json ou d'aller sur internet)");
            } else {
                Log.d(TAG, "on a trouvé ce nombre d'image a afficher:" + result.size() + " " + this.sums.size());
                if (currentfile == missingfiles.size()) {
                    mDLisCompleted = true;

                    mDiapoHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY);


                    mHideHandler.postDelayed(hidedebugmenuRunnable, UI_ANIMATION_DELAY);

                }
                else{mDLisCompleted=false;}
                mDLprogressBar.setMax(missingfiles.size());

                //    totalfiles = result.size();
            }

        }else { Log.e(TAG, "jsonfile VIDE!!!");
    }

    }}


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