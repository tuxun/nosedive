package org.tflsh.nosedive;

//import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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


    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////ATTRIBUTES////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    boolean mNoInternet = false;

    private static final int UI_ANIMATION_DELAY = 300;
    //temps unitaire (base de temps), sert a définir le delai entre deux images
    final int delayinterframes = 1000;
    //temps durant lequel onregarde une image proposé apres le menu (en multiple d'interframedelay)
    final int delayquestionnement = 5 * delayinterframes;
    //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
    final int delaychoixmots = 2 * delayquestionnement;
    final ArrayList<Button> mCheckedToggleButtonsArrayList = new ArrayList<>();
    final ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
    private final Handler mSlideshowHandler = new Handler();
    private final Runnable showressmetextRunnable = new Runnable() {
        @Override
        public void run() {
            findViewById(R.id.ui_press_meTextView).setVisibility(View.VISIBLE);
        }
    };
    private final Handler mHideHandler = new Handler();
    private final Runnable makeButtonNotClickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mToggleButtonsArrayList.size();
            Log.d("makeButtonNotClickable", clickedbuttons + "makeButtonNotClickableRunnable ok");
            for (int i = clickedbuttons - 1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(false);
                //mToggleButtonsArrayList.get(i).setClickable(true);
                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
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
    //used for show words?.
    private final Runnable showMenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("showMenuRunnable", "showmenuRunnable ?");

            mSlideshowHandler.post(cleanbuttonRunnable);
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            mSlideshowHandler.removeCallbacks(cleanbuttonRunnable);
            mSlideshowHandler.removeCallbacks(startdiapoRunnable);
            mSlideshowHandler.postDelayed(startdiapoRunnable, delaychoixmots);
            findViewById(R.id.ui_centralLinearLayout).setVisibility(View.GONE);
            findViewById(R.id.leftMenuLinearLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.rightMenuLinearLayout).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.ui_press_meTextView)).setText(R.string.string_choose2word);
            //findViewById(R.id.pressme_text).setVisibility(View.VISIBLE);


        }
    };
    public int pwa;
    //runnnable s'appelant lui meme a la fin du diapo qu'il lance
    public MyIntentManager receiver;
    TextView tvProgressLabel;
    asyncTaskManager asm;
    int screenWidth;
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////RUNNABLES////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    int screenHeight;
    ArrayList<String> missingFilesNames;
    IntentFilter filter;
    private ArrayList<String> mSums;
    private ProgressBar mDiapoProgressBar;//
    //REGLAGE DE LAPPLI
    private int currentfile;
    private LinearLayout lm;
    private LinearLayout rm;
    // private static final boolean AUTO_HIDE = true;
    private boolean mFullscreen;
    private final Runnable mfullscreenOffRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();


            }
            //  mContentView.getLayoutParams().notifyAll();

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //!!!   (findViewById(R.id.nosedive)).setFitsSystemWindows(true);


            // mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            Log.e("mShowPart2Runnable", "EXITFULLSCREEN");
            mFullscreen = false;

            findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);

        }
    };
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

            if (missingFilesNames.size() == 0) {
                //  findViewById(R.id.progressbar).setVisibility(View.GONE);

                //((TextView) findViewById(R.id.pressme_text)).setVisibility(View.GONE);
//                ((ProgressBar) findViewById(R.id.progressbar)).setVisibility(View.GONE);
                // ((TextView) findViewById(R.id.fullshcreen_text)).setVisibility(View.GONE);
                //((TextView) findViewById(R.id.fullscreen_text)).setVisibility(View.GONE);
                findViewById(R.id.ui_dl_progressTextView).setVisibility(View.GONE);
                findViewById(R.id.ui_dl_ProgressBar).setVisibility(View.GONE);
            } else {
                findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);
                findViewById(R.id.ui_dl_progressTextView).setVisibility(View.VISIBLE);
                findViewById(R.id.debug_content_controls).setVisibility(View.VISIBLE);
            }
        }
    };
    //    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private Button prevButton;
    private Button lastButton;
    private boolean mDLisCompleted = false;
    private ArrayList<String> mSlideshowFilesNames;
    private boolean mSlideshowIsRunning = false;
    private final Runnable showNextRunnable = new Runnable() {
        @Override
        public void run() {
            //antibounce
            mSlideshowHandler.removeCallbacks(startdiapoRunnable);
            Log.d("showNextRunnable", "next!");
            mHideHandler.post(showressmetextRunnable);

            if (mSlideshowIsRunning) {
                if (pwa < mSlideshowFilesNames.size()) {

                    //dans l'ordre
                    //!!!!!!!!!!!!!!new asyncTaskManager.showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(pwa));
                    //en pseudo random
                    asm.new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mSlideshowFilesNames.get(new Random().nextInt(mSlideshowFilesNames.size())));

                    // Log.d("diapo", "showing " + mDiapo.get(pwa));
                    pwa++;
                } else {
                    Log.d("pwa", "no more pwa");
                    pwa = 0;
                    mSlideshowHandler.postDelayed(startdiapoRunnable, delayinterframes); //end handlepostdelay

                    // pgb.setProgress(pwa);
                    //mdiapo_isrunning=false;
                    // Log.d("pwa","we added pwa for keep the diapo going... and going... and going...");
                }

            }
        }

        // Code here will run in UI thread


    };
    //end runnables list


    private final Runnable cleanbuttonRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons - 1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(true);
                mToggleButtonsArrayList.get(i).setClickable(true);
                mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };

    private final Runnable makebuttonclickableRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons = mToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons - 1; i >= 0; i--) {

                mToggleButtonsArrayList.get(i).setEnabled(true);
                //mToggleButtonsArrayList.get(i).setClickable(true);
                //mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    private final Runnable startdiapoRunnable = new Runnable() {

        @Override
        public void run() {
            //mHideHandler.post(cleanbuttonRunnable);
            //    if (mDLisCompleted) {
            //reset progressBar
            // ((TextView) findViewById(R.id.fullscreen_text)).setText("Mots");
            mSlideshowHandler.removeCallbacks(showNextRunnable);
            Log.d("startdiapoRunnable", "start with diapo size=" + mSlideshowFilesNames.size());

            mSlideshowIsRunning = true;
            pwa = 0;
            // mDiapoProgressBar.setProgress(0);
            //pgb.setBackgroundColor(0);
            //mDiapoProgressBar.setMax(mDiapo.size());


            int i, j;
            mHideHandler.postDelayed(hidemenuRunnable, UI_ANIMATION_DELAY);

            for (i = 0; i < mSlideshowFilesNames.size() + 1; i++) {
                mSlideshowHandler.postDelayed(showNextRunnable, i * delayinterframes);
            } //end for
            //Log.d("startdiapo", "stop!");

            //   mDiapoHandler.postDelayed(startdiapoRunnable, i * ut+ut); //end handlepostdelay


            //   } else {
             /*   if (mNoInternet == true) {
                    Toast.makeText(getApplicationContext(), "impossible de récup index.php, veuillez  activer le WIFI et relancer l'appli", Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), "Patientez pendant le téléchargement des images", Toast.LENGTH_LONG).show();
                }
                Log.e("startdiaporunnable", "mais missingimgs! (showing toast)");*/
        }
        //  }
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
        unregisterReceiver(receiver);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter("msg");


        receiver = new MyIntentManager();

        registerReceiver(receiver, filter);

        asm = new asyncTaskManager(getApplicationContext());





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
    protected void onResume() {
        super.onResume();


        Log.d("activity", "onResume" + getIntent());
        android.app.ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        this.missingFilesNames = new ArrayList<>();

        //https://developer.android.com/training/monitoring-device-state/connectivity-status-type
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        boolean isMetered = cm.isActiveNetworkMetered();
        if (isConnected) {
            Log.d("NetworkInfo", "internet ok");
            if (isMetered) {
                Log.d("NetworkInfo", "mais connexion limité");
                mNoInternet = false;

            } else {
                Log.d("NetworkInfo", "pas de limite de débit (youpi!)");
                mNoInternet = true;

            }

        } else {
            Log.d("NetworkInfo", "pas internet");
            mNoInternet = false;

        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.


        //   TextView destiv = findViewById(R.id.pressme_text);

        //todo
        //refactor for use constant
        //  String jsonfilename;


        //  this.missingfilessize = 0;

// since SDK_INT = 1;
//
        setScreenMetrics();

  /*      if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
*/

        //Bitmap mIcon11;
        // mDiapoProgressBar = findViewById(R.id.progressbar);
        //
        ProgressBar mDlProgressBar = findViewById(R.id.ui_dl_ProgressBar);
        //
        TextView mDLprogressText = findViewById(R.id.ui_dl_progressTextView);


        this.mSlideshowFilesNames = new ArrayList<>();
        this.missingFilesNames = new ArrayList<>();

        //should be an intent?

        this.pwa = 0;

//        mDiapoProgressBar.setProgress(0);
//WXCB        mDiapoProgressBar.setIndeterminate(false);
        mDlProgressBar.setIndeterminate(false);
        String mServerDirectoryURL = "https://dev.tuxun.fr/nosedive/" + "julia/";
        asm.new ListImageTask(missingFilesNames, mSlideshowFilesNames).execute(mServerDirectoryURL);


        Log.d("ListimageResult", "missing file= " + missingFilesNames.size());


        View mContentView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content_controls);

        // LinearLayout des = findViewById(R.id.contentMUM);
        lm = findViewById(R.id.leftMenuLinearLayout);
        rm = findViewById(R.id.rightMenuLinearLayout);

        makeButtons();


        // Set up the user interaction to manually show or hide the system UI.

        //c  findViewById(R.id.dummy_button).setOnTouchListener(onclick);
        // findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


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


        ////////////////////////
        findViewById(R.id.imageView).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.e("onLongClick", "LONGPRESS");

                togglefullscreen();
                return true;
            }
        });
        if (missingFilesNames.size() == 0) {
            //cache les mots, lance le diapo
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY - 10);
            mHideHandler.postDelayed(startdiapoRunnable, UI_ANIMATION_DELAY + 10);
        } else {
            //on a des dl a faire
            //cache "pressme"
            (findViewById(R.id.ui_press_meTextView)).setVisibility(View.GONE);
//cache les mots
            mHideHandler.postDelayed(mfullscreenOnRunnable, UI_ANIMATION_DELAY);
        }

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

        final ArrayList<Button> mToggleButtonsArrayList = new ArrayList<>();
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
            temptg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSlideshowHandler.removeCallbacks(showNextRunnable);
                    mSlideshowHandler.removeCallbacks(startdiapoRunnable);

                    ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));


                    mSlideshowIsRunning = false;
                    // SystemClock.sleep(100);

                    //  ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));


                //  ((ToggleButton)view.findViewWithTag("toggleButton")).setChecked(false);
                if (mCheckedToggleButtonsArrayList.size() == 0) {
                    Log.d("toggleclick", "toggle 1 boutons ok" + view.getTag().toString());
                    mSlideshowHandler.postDelayed(startdiapoRunnable, delayquestionnement);
                    // mHideHandler.postDelayed(cleanbuttonRunnable, delayquestionnement+UI_ANIMATION_DELAY);
                    view.setPressed(true);
                    view.setEnabled(false);
                    lastButton = (Button) view;

                    mCheckedToggleButtonsArrayList.add(((Button) view));

                } else if (mCheckedToggleButtonsArrayList.size() == 1) {
                    //if its the second time we click on a button
//if it's the same button twice, uncheck it and return
//                        lastButton = ((ToggleButton) view);
                    lastButton.setEnabled(true);
                    mCheckedToggleButtonsArrayList.add(((Button) view));

                    if (mCheckedToggleButtonsArrayList.get(0) == mCheckedToggleButtonsArrayList.get(1)) {
                        Log.d("mCheckedToggleButtons", "same BUTTON AND FUCK");
                    }
                    (findViewById(R.id.ui_press_meTextView)).setVisibility(View.GONE);
                    mHideHandler.post(hidemenuRunnable);
//mCheckedToggleButtonsArrayList.get(0).setPressed(false);

                    Log.d("toggleclick", "toggle 2 boutons ok");
                    mSlideshowHandler.postDelayed(startdiapoRunnable, delayquestionnement);

                    //mHideHandler.post(cleanbuttonRunnable);
                    // mHideHandler.post(makebuttonclickableRunnable);
                    mHideHandler.post(makeButtonNotClickableRunnable);
                    asm.new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mSlideshowFilesNames.get(new Random().nextInt(mSlideshowFilesNames.size())));

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

        int j;
    //for the first 8 buttton, set in leftmenulayout
        for (j = 0; j < _buttonNames.length / 2; j++) {
        mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.andbasr));

        lm.addView(mToggleButtonsArrayList.get(j));
    }


    //for the first 8 buttton, set in the right menu layout

        for (; j < _buttonNames.length; j++) {
        mToggleButtonsArrayList.get(j).setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.andbasr));

        rm.addView(mToggleButtonsArrayList.get(j));
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