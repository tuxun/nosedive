package org.tflsh.multifacette;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat implements
    SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String CLASSNAME = "SettingsFragment";
  DataStore datastore;
  private FirebaseRemoteConfig mFirebaseRemoteConfig;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
//get uid?
      FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
  }

  public EditTextPreference makeEditTextPreference(Context contextArg, String keyArg) {
    EditTextPreference retPreference = new EditTextPreference(contextArg);
    String value =datastore.getValue(keyArg, "makeEditTextPreference no data with key "+keyArg);

    retPreference.setKey(keyArg);

    retPreference.setText(value);
    retPreference.setDefaultValue(value);

    retPreference.setTitle(keyArg + " original value: "+value);
    return retPreference;

  }

    public customSeekBarPreference makeSeekBar(Context contextArg, String keyArg,int defValue, int maxValue,boolean showValueArg) {

    customSeekBarPreference retPreference = new customSeekBarPreference(contextArg);
    int value =datastore.getValue(keyArg, defValue);

    retPreference.setKey(keyArg);
    retPreference.setMax(maxValue);

    retPreference.setValue(value);
    retPreference.setDefaultValue(value);
    retPreference.setShowSeekBarValue(showValueArg);

    retPreference.setTitle(keyArg + " original value: "+value);
return retPreference;
    //  delayFramePreference.update();
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    //?PreferenceManager.getDefaultSharedPreferences(getContext());
    // addPreferencesFromResource(R.xml.root_preferences);
    //setPreferencesFromResource(R.xml.root_preferences, rootKey);
   /*
    Context context = getPreferenceManager().getContext();
    //screen.getSharedPreferences();



    PreferenceCategory notificationCategory = new PreferenceCategory(context);
    notificationCategory.setKey("notifications_category");
    notificationCategory.setTitle("Notifications");
    screen.addPreference(notificationCategory);
    notificationCategory.addPreference(notificationPreference);

    Preference feedbackPreference = new Preference(context);
    feedbackPreference.setKey("feedback");
    feedbackPreference.setTitle("Send feedback");
    feedbackPreference.setSummary("Report technical issues or suggest new features");
    getPreferenceManager().getSharedPreferences().edit().putString("feedback","Send feedback");


    helpCategory.addPreference(feedbackPreference);

    SharedPreferences prefs =  getPreferenceManager().getSharedPreferences();
 */
    datastore = new DataStore();

    //!!!!addPreferencesFromResource(R.xml.root_preferences);

    PreferenceScreen screen = /*getPreferenceScreen();*/
        getPreferenceManager().createPreferenceScreen(getContext());

    if (datastore.isUserConnected()) {
      Log.d(CLASSNAME, "found connected user " + datastore.getUserName());
    }
      PreferenceCategory timerCategory = new PreferenceCategory(getContext());
      timerCategory.setKey("timers");
      timerCategory.setTitle("Timers");
      screen.addPreference(timerCategory);




      customSeekBarPreference delayFramePreference = makeSeekBar(getContext(),"DELAY_INTER_FRAME_SETTING",750,10000,true);
      screen.addPreference(delayFramePreference);

      customSeekBarPreference SeekBarPreference = makeSeekBar(getContext(),"DELAY_GUESSING_SETTING",5000,30000,true);
      screen.addPreference(SeekBarPreference);

      customSeekBarPreference delayGuessingPreference = makeSeekBar(getContext(),"DELAY_CHOICE_WORDS_SETTING",10000,30000,true);
      screen.addPreference(delayGuessingPreference);

      customSeekBarPreference delayUIPreference = makeSeekBar(getContext(),"UI_ANIMATION_DELAY",300,1000,true);
      screen.addPreference(delayUIPreference);

    EditTextPreference
        baseUrlPreference = makeEditTextPreference(getContext(),"BASE_URL");
    screen.addPreference(baseUrlPreference);

    EditTextPreference
        baseProjectPreference = makeEditTextPreference(getContext(),"DEFAULT_PROJECT_KEY");
    screen.addPreference(baseProjectPreference);



      SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(getContext());
      notificationPreference.setKey("logout");
      notificationPreference.setTitle("url" + datastore.getValue("DEFAULT_PROJECT_KEY", "null"));
      screen.addPreference(notificationPreference);

      PreferenceCategory helpCategory = new PreferenceCategory(getContext());
      helpCategory.setKey("help");
      helpCategory.setTitle("Help");
      screen.addPreference(helpCategory);

      setPreferenceScreen(screen);

    PreferenceManager preferenceManager = getPreferenceManager();

    preferenceManager.setPreferenceDataStore(datastore);

    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(60)/*3600*/
        .build();

    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.root_preferences);
    //TODO: this can occurs even after fragment left the screen

    mFirebaseRemoteConfig.fetchAndActivate()
        .addOnCompleteListener(getActivity().getMainExecutor(), new OnCompleteListener<Boolean>() {
          @Override
          public void onComplete(@NonNull Task<Boolean> task) {
            if (task.isSuccessful()) {
              boolean updated = task.getResult();

              Log.d(CLASSNAME, "Config params updated: " + updated);
              Toast.makeText(getActivity(), "Fetch and activate succeeded",
                  Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getActivity(), "Fetch failed",
                  Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getActivity(), "welcome!",
                Toast.LENGTH_SHORT).show();

            setPreferenceScreen(getPreferenceScreen());

            Log.d(CLASSNAME,
                String.valueOf(mFirebaseRemoteConfig.getBoolean("resetSettingsOnNextStartup")));
            Log.d(CLASSNAME,
                mFirebaseRemoteConfig.getString("DEFAULT_PROJECT_KEY"));
          /*  Log.d("settingsFrament",
                getPreferenceScreen().findPreference("DEFAULT_PROJECT_KEY").getSharedPreferences().getString("DEFAULT_PROJECT_KEY","proutnokey"));


            getPreferenceScreen().findPreference("DEFAULT_PROJECT_KEY").getSharedPreferences().edit().putString("DEFAULT_PROJECT_KEY",
                 mFirebaseRemoteConfig.getString("DEFAULT_PROJECT_KEY"));
            getPreferenceScreen().findPreference("DEFAULT_PROJECT_KEY").getSharedPreferences().edit().apply();
*/
          }
        });
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.root_preferences);


    //mFirebaseRemoteConfig.fetch(5);

    //mFirebaseRemoteConfig.getString("DEFAULT_PROJECT_KEY");

  }

  @Override public void onPause() {
    super.onPause();
    Log.d(CLASSNAME, "onPause");

    /*getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);*/
  }

  @Override public void onResume() {
    super.onResume();
    Log.d(CLASSNAME, "onResume");

/*    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  */
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
  /*  switch (key) {

      case "DELAY_CHOICE_WORDS_SETTING":
      case "DELAY_GUESSING_SETTING":
      case "DELAY_INTER_FRAME_SETTING": {
        PreferenceManager pfm = getPreferenceManager();
        pfm.getSharedPreferences()
            .edit()
            .putInt(key, sharedPreferences.getInt(key, 999));
        //mFirebaseRemoteConfig.getKeysByPrefix("DELAY
        getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
            .edit().commit();
      }

      case "BASE_URL":
      case "DEFAULT_PROJECT_KEY": {
        getPreferenceScreen().getSharedPreferences()
            .edit()
            .putString(key, mFirebaseRemoteConfig.getString(key));
        getActivity().getSharedPreferences("", Context.MODE_PRIVATE).edit().apply();

        getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
            .edit()
            .putString(key, mFirebaseRemoteConfig.getString(key));


        getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
            .edit().commit();
        //mFirebaseRemoteConfig.getKeysByPrefix("DELAY
      }
    }
    //        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());
    Log.d("settingfragmennt", "Config params updated: " + key);
    getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
        .edit().apply();
   */
  }



/*
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    //return getListView();
    return inflater.inflate(R.layout.fragment_settings, container, false);

  }
*/
}

/*A PreferenceFragmentCompat is the entry point to using the Preference library.
 This Fragment displays a hierarchy of Preference objects to the user.
 It also handles persisting values to the device.

  To retrieve an instance of android.content.SharedPreferences that the preference hierarchy in this fragment will use by default, call PreferenceManager.getDefaultSharedPreferences(Context) with a context in the same package as this fragment.


You can define a preference hierarchy as an XML resource, or you can build a hierarchy in code. In both cases you need to use a PreferenceScreen as the root component in your hierarchy.
To inflate from XML, use the setPreferencesFromResource(int, String). An example example XML resource is shown further down.
To build a hierarchy from code, use PreferenceManager.createPreferenceScreen(Context) to create the root PreferenceScreen. Once you have added other Preferences to this root scree with PreferenceScreen.addPreference(Preference), you then need to set the screen as the root screen in your hierarchy with setPreferenceScreen(PreferenceScreen).
As a convenience, this fragment implements a click listener for any preference in the current hierarchy, see onPreferenceTreeClick(Preference).
*/