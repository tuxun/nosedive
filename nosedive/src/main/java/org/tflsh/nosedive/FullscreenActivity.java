package org.tflsh.nosedive;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private final String mServerDirectoryURL = "";
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


    private final Runnable cleanbuttonRunnable = new Runnable() {
        @Override
        public void run() {

            int clickedbuttons=mCheckedToggleButtonsArrayList.size();
            Log.d("cleanbuttonRunnable", clickedbuttons + "cleanboutons ok");
            for (int i = clickedbuttons-1; i >= 0; i--) {

                mCheckedToggleButtonsArrayList.get(i).setChecked(false);
                mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(i));
            }

        }
    };
    private final Runnable hidemenuRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("hidemenurunnable", "visible ?");
            findViewById(R.id.press2buttons_text).setVisibility(View.GONE);

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
    int delayinterframes = 750;
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
            findViewById(R.id.press2buttons_text).setVisibility(View.VISIBLE);

        }
    };
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

            ((LinearLayout) findViewById(R.id.windowLayout)).setFitsSystemWindows(false);


            // mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            Log.e("mShowPart2Runnable", "EXITFULLSCREEN");
            mFullscreen = false;

            ((LinearLayout) findViewById(R.id.fullscreen_content_controls)).setVisibility(View.VISIBLE);

        }
    };
    //    private static final int AUTO_HIDE_DELAY_MILLIS = 300;
    private ToggleButton prevButton;
    private ToggleButton lastButton;
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
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
            ((LinearLayout) findViewById(R.id.windowLayout)).setFitsSystemWindows(true);

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
                Log.e("startdiaporunnable", "mais missingimgs!");
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
        switch (item.getItemId()) {
            case R.id.add:
                //add the function to perform here
                return (true);
            case R.id.reset:
                //add the function to perform here
                return (true);
            case R.id.about:

                Toast.makeText(getApplicationContext(), "plop", Toast.LENGTH_LONG).show();
                return (true);
            case R.id.exit:

                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
      ArrayList<ToggleButton> mCheckedToggleButtonsArrayList = new ArrayList<ToggleButton>();

    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //debug    actionBar.hide();
        }
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_fullscreen);

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

        ArrayList<ToggleButton> mToggleButtonsArrayList = new ArrayList<ToggleButton>();
//how to get every button...
        //making ALL first

        int j = 0;
        for (j = 0; j < _buttonNames.length; j++) {

            ToggleButton temptg = new ToggleButton(this);
            ViewGroup.LayoutParams layoutParams=findViewById(R.id.pressme_text).getLayoutParams();
            temptg.setBackground(this.getResources().getDrawable(R.drawable.ic_bouttonoff));
            temptg.setTextOff(_buttonNames[j]);
            temptg.setTextOn(_buttonNames[j]);
            //
            temptg.setAllCaps(true);
            temptg.setLayoutParams(layoutParams);
            temptg.setPadding(10,5,10,5);

            temptg.setTag(_buttonprimalnumbers[j]);
              /*  android:layout_width="match_parent"
                    android:layout_height="wrap_content"*/

            ////////////////////////////////

            temptg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDiapoHandler.removeCallbacks(showNextRunnable);
                    mDiapoHandler.removeCallbacks(startdiapoRunnable);
                    mdiapo_isrunning = false;




                    mCheckedToggleButtonsArrayList.add(((ToggleButton) view));


                    //  ((ToggleButton)view.findViewWithTag("toggleButton")).setChecked(false);
                    if (mCheckedToggleButtonsArrayList.size() == 1) {
                        Log.d("toggleclick", "toggle 1 boutons ok" + view.getTag().toString());
                        prevButton = ((ToggleButton) view);

                        ((ImageView) findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.whitebackground));
                        mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);
                        mDiapoHandler.postDelayed(cleanbuttonRunnable, delayquestionnement);

return;

                    } else if (mCheckedToggleButtonsArrayList.size() == 2) {
                        //if its the second time we click on a button
//if it's the same button twice, uncheck it and return
                        lastButton = ((ToggleButton) view);
                        if (mCheckedToggleButtonsArrayList.get(0) == mCheckedToggleButtonsArrayList.get(1)) {
                            mHideHandler.post(cleanbuttonRunnable);

                            Log.d("toggleclick", "toggle twice SAME button");
                         //   mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(1));
                           // mCheckedToggleButtonsArrayList.remove(mCheckedToggleButtonsArrayList.get(0));

                            //prevButton.setChecked(false);
                            mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);
                            return;

                        } else {
                            (findViewById(R.id.pressme_text)).setVisibility(View.GONE);
                            mHideHandler.postDelayed(hidemenuRunnable, UI_ANIMATION_DELAY);

                            Log.d("toggleclick", "toggle 2 boutons ok");
                            mDiapoHandler.postDelayed(startdiapoRunnable, delayquestionnement);

                            new showImageFileTask((ImageView) findViewById(R.id.imageView)).execute(mDiapo.get(new Random().nextInt(mDiapo.size())));
                            return;
                        }
                    }


                    //a chaque button cliqué, si on est perdu, on decheck les bouttons
                    //should only  happen when 2 DIFFERENT buttons are pressed
                    //  ((ToggleButton) view).setChecked(true);
                    int clickedbuttons = mCheckedToggleButtonsArrayList.size();
                    mHideHandler.postDelayed(cleanbuttonRunnable, delaychoixmots);
                    mDiapoHandler.postDelayed(startdiapoRunnable, delaychoixmots);

                }
            });



          //avoid a glitch reloading button
            temptg.setChecked(false);

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


        this.mDiapo = new ArrayList<String>();
        this.mSums = new ArrayList<String>();
        this.missingfiles = new ArrayList<String>();
        //  this.missingfilessize = 0;

// since SDK_INT = 1;

        Log.d("defaultvalue", "screenwidth= " + screenwidth);
        Log.d("defaultvalue", "screenheight= " + screenheight);


        //Bitmap mIcon11;
        mDiapoProgressBar = findViewById(R.id.progressbar);
        mDLprogressBar = findViewById(R.id.DLprogressbar);
        mDLprogressText = findViewById(R.id.DLprogress_text);
        new ListImageTask(mSums, mDiapo).execute(mServerDirectoryURL);




    }

    @Override protected   void onStop() {
        super.onStop();
        mDiapoHandler.post(stopdiapoRunnable);
        Log.d("activity", "onstop");


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        this.pwa = 0;

        Log.d("ListimageResult", "missing file= " + missingfiles.size());
        mDiapoProgressBar.setProgress(0);

        mDiapoProgressBar.setIndeterminate(false);

        mDLprogressBar.setIndeterminate(false);

        if (mDLisCompleted) {
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
        FileInputStream fis=new FileInputStream(res);

        // First decode with inJustDecodeBounds=true to check dimensions

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(fis, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.outWidth = reqWidth;
        options.outHeight = reqHeight;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(fis, null, options);

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
            //   this.name=namee;§
        }

        protected Bitmap doInBackground(String... urls) {
            try {
                return olddecodeSampledBitmapFromFilepath(getExternalCacheDir() + "/" + urls[0], screenwidth, screenheight);
            }
            catch (Exception e)
            {
                Log.e("fileeroor","inshowimgtask");               return null;

            }
        }

        protected void onPostExecute(Bitmap result) {

            //((TextView) findViewById(R.id.pressme_text)).setText("Diapo " + (pwa + 1) + "/" + mDiapo.size() + "réussi");
            Log.d("showimageTask","Diapo " + (pwa ) + "/" + mDiapo.size() + "réussi");

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

        //TODO check the mess with the files
        public grabImageTask(String destname) {

            this.destifileImage = destname;
        }

        protected File doInBackground(String... urls) {
            //  mDLprogressBar.incrementSecondaryProgressBy(1);

//            mDLprogressBar.setSecondaryProgressTintMode(PorterDuff.Mode.DARKEN);
            // SystemClock.sleep(5);

            File file = new File(getExternalCacheDir() + "/" + destifileImage);
            String urldisplay = urls[0];
            //  String destistring;
            currentfile++;
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
                Log.d("GRABtaskPOST", "ok synchonizing " + currentfile + " of " + missingfiles.size());
                // pgb.incrementProgressBy((int)(result.getAbsoluteFile().length()/1024));}
                mDLprogressText.setText("Téléchargement " + currentfile + "/" + missingfiles.size() + "réussi");



                //  mDLprogressBar.incrementSecondaryProgressBy(1);

                mDLprogressBar.setProgress(currentfile);
                mDLprogressBar.setSecondaryProgress(currentfile+1);


            }
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


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private  class  ListImageTask extends AsyncTask<String, ArrayList<String>, ArrayList<String>> {
        private static final String TAG = "ListImageTask";
        ArrayList<String> name;
        ArrayList<String> sums;

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

                InputStream is = null;
                if (!localjsonfile.exists()) {
                    is = new java.net.URL(urldisplay).openStream();
                    //ByteArrayOutputStream bos=new ByteArrayOutputStream();
                    byte[] bitmapdata = new byte[1024];
                    //  is.read(bitmapdata);
                    //  SystemClock.sleep(100);

                    //  byte[] jsondata = new byte[0];
                    int read;

                    Log.d(TAG, "downloading filelist.jsonn from " + urldisplay);


                    OutputStream fos = new FileOutputStream(localjsonfile);

                    do {
                        read = is.read(bitmapdata);
                        if(read>0 && !localjsonfile.exists()) {
                            localjsonfile.createNewFile();
                        }
                        fos.write(bitmapdata, 0, read);
                    }
                    while ((read!= -1));


                    //   SystemClock.sleep(1000);
                    fos.close();

                    //  is.reset();
                }
//TODO file can be empty if internet failed
                Log.d(TAG, "opening" + localjsonfile.getAbsolutePath());
                if (localjsonfile.exists()) {
                    FileInputStream fis = new FileInputStream(localjsonfile.getAbsoluteFile());
                    is = new BufferedInputStream(fis);


                    //une fois le fichier recupéré, on peut l'ouvrir

                    JsonReader reader = new JsonReader(new InputStreamReader(is));

                    //fis.close();

                    String description = "";
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        boolean mssgfile = false;

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

                                    new grabImageTask(newIn).execute(mServerDirectoryURL + newIn);
                                    missingfiles.add(newIn);
                                    Thread.sleep(50);


                                    // Log.e("error", "FAILED ! writen file " +testfile.getAbsolutePath() );
                                } else {
                                    Log.d("ListImageTask", "file " + newIn + " already found to " + getExternalCacheDir());
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
                    throw new NetworkErrorException();

                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "fichier json local non trouvé");

                e.printStackTrace();
            } catch (MalformedURLException e) {
                Log.e(TAG, "mauvaise url");

                e.printStackTrace();
            } catch (NetworkErrorException e) {
                Log.e(TAG, "pas internet pour recup le fichier json");
                //popup : pas internet
                //e.printStackTrace();

                // e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "fichier json internet non trouvé");
                //popup : pas internet
                e.printStackTrace();

                // e.printStackTrace();
            } catch (Exception e) {
                Log.e("UnexpectedError", e.getMessage());
                e.printStackTrace();
            }
            return this.name;
        }

        protected void onPostExecute(ArrayList<String> result) {
            if (result.size() == 0) {
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

        }

    }

}