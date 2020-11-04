package org.tflsh.multifacette;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
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
  DataStore datastore;
  private FirebaseRemoteConfig mFirebaseRemoteConfig;

  Map<String, FirebaseRemoteConfigValue> getConfig() {
    return mFirebaseRemoteConfig.getAll();
  }

  public Map<String, ?> createSettings(Map<String, FirebaseRemoteConfigValue> SettingArg) {

  /* getActivity().getSharedPreferences("",Context.MODE_PRIVATE).edit().putInt("DELAY_INTER_FRAME_SETTING",
        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());*/

    return getActivity().getSharedPreferences("", Context.MODE_PRIVATE).getAll();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {

      getContext().getSharedPreferences("", Context.MODE_PRIVATE)
          .edit()
          .putString("DEFAUT_PROJECT_KEY",
              savedInstanceState.getString("DEFAUT_PROJECT_KEY"));
      Log.d("settingsFrgmntOnCreate",
          mFirebaseRemoteConfig.getString("DEFAUT_PROJECT_KEY"));

      FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    //setPreferencesFromResource(R.xml.root_preferences,null);

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
      Log.d("settingfragmennt", "found connected user " + datastore.getUserName());

      PreferenceCategory timerCategory = new PreferenceCategory(getContext());
      timerCategory.setKey("timers");
      timerCategory.setTitle("Timers");
      screen.addPreference(timerCategory);

      customSeekBarPreference delayFramePreference = new customSeekBarPreference(getContext());

      delayFramePreference.setKey("DELAY_INTER_FRAME_SETTING");
      delayFramePreference.setMax(10000);

      delayFramePreference.setValue(datastore.getSettings("", null).DELAY_INTER_FRAME_SETTING);
      delayFramePreference.setDefaultValue(
          datastore.getSettings("", null).DELAY_INTER_FRAME_SETTING);

      delayFramePreference.setTitle(
          "url" + datastore.getSettings("", null).DELAY_INTER_FRAME_SETTING);

      delayFramePreference.setShowSeekBarValue(true);

      //  delayFramePreference.update();
      screen.addPreference(delayFramePreference);

      SeekBarPreference delayGuessingPreference = new SeekBarPreference(getContext());
      delayGuessingPreference.setKey("DELAY_GUESSING_SETTING");
      delayGuessingPreference.setTitle(
          "url" + datastore.getSettings("", null).DELAY_GUESSING_SETTING);
      delayGuessingPreference.setMax(30000);
      delayGuessingPreference.setValue(datastore.getValue("DELAY_GUESSING_SETTING", 0));
      delayGuessingPreference.setShowSeekBarValue(true);

      screen.addPreference(delayGuessingPreference);

      SeekBarPreference delayChoicePreference = new SeekBarPreference(getContext());
      delayChoicePreference.setKey("DELAY_CHOICE_WORDS_SETTING");
      delayChoicePreference.setShowSeekBarValue(true);
      delayChoicePreference.setTitle(
          "url" + datastore.getSettings("", null).DELAY_CHOICE_WORDS_SETTING);
      delayChoicePreference.setValue(datastore.getValue("DELAY_CHOICE_WORDS_SETTING", 0));
      delayChoicePreference.setMax(30000);
      screen.addPreference(delayChoicePreference);

      SeekBarPreference delayUIPreference = new SeekBarPreference(getContext());
      delayUIPreference.setKey("UI_ANIMATION_DELAY");
      delayUIPreference.setShowSeekBarValue(true);
      delayUIPreference.setTitle("url" + datastore.getSettings("", null).UI_ANIMATION_DELAY);
      delayUIPreference.setValue(datastore.getValue("UI_ANIMATION_DELAY", 0));
      delayUIPreference.setMax(30000);

      screen.addPreference(delayUIPreference);

      SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(getContext());
      notificationPreference.setKey("logout");
      notificationPreference.setTitle("url" + datastore.getSettings("", null).DEFAUT_PROJECT_KEY);
      screen.addPreference(notificationPreference);

      PreferenceCategory helpCategory = new PreferenceCategory(getContext());
      helpCategory.setKey("help");
      helpCategory.setTitle("Help");
      screen.addPreference(helpCategory);

      setPreferenceScreen(screen);
    }
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

              Log.d("settingfragmennt", "Config params updated: " + updated);
              Toast.makeText(getActivity(), "Fetch and activate succeeded",
                  Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getActivity(), "Fetch failed",
                  Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getActivity(), "welcome!",
                Toast.LENGTH_SHORT).show();

            setPreferenceScreen(getPreferenceScreen());

            Log.d("settingsFrament",
                String.valueOf(mFirebaseRemoteConfig.getBoolean("resetSettingsOnNextStartup")));
            Log.d("settingsFrament",
                mFirebaseRemoteConfig.getString("DEFAUT_PROJECT_KEY"));
          /*  Log.d("settingsFrament",
                getPreferenceScreen().findPreference("DEFAUT_PROJECT_KEY").getSharedPreferences().getString("DEFAUT_PROJECT_KEY","proutnokey"));


            getPreferenceScreen().findPreference("DEFAUT_PROJECT_KEY").getSharedPreferences().edit().putString("DEFAUT_PROJECT_KEY",
                 mFirebaseRemoteConfig.getString("DEFAUT_PROJECT_KEY"));
            getPreferenceScreen().findPreference("DEFAUT_PROJECT_KEY").getSharedPreferences().edit().apply();
*/
          }
        });
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.root_preferences);

    createSettings(getConfig());

    //mFirebaseRemoteConfig.fetch(5);

    //mFirebaseRemoteConfig.getString("DEFAUT_PROJECT_KEY");

  }

  @Override public void onPause() {
    super.onPause();
    /*getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);*/
  }

  @Override public void onResume() {
    super.onResume();
/*    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  */
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
    switch (key) {
 /*getPreferenceManager().getSharedPreferences().edit().putInt("DELAY_GUESSING_SETTING",
        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());
    getPreferenceManager().getSharedPreferences().edit().putInt("DELAY_CHOICE_WORDS_SETTING",
        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());
    getPreferenceManager().getSharedPreferences().edit().putInt("UI_ANIMATION_DELAY",
        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());
    getPreferenceManager().getSharedPreferences().edit().putBoolean("resetSettingsOnNextStartup",SettingArg.get("resetSettingsOnNextStartup").asBoolean());
    getPreferenceManager().getSharedPreferences().edit().putString("DEFAUT_PROJECT_KEY",SettingArg.get("DEFAUT_PROJECT_KEY").asString());
    getPreferenceManager().getSharedPreferences().edit().putString("BASE_URL",SettingArg.get("BASE_URL").asString());
    getPreferenceManager().getSharedPreferences().edit().commit();*/
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
      case "DEFAUT_PROJECT_KEY": {
        getPreferenceScreen().getSharedPreferences()
            .edit()
            .putString(key, mFirebaseRemoteConfig.getString(key));
        getActivity().getSharedPreferences("", Context.MODE_PRIVATE).edit().apply();

        getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
            .edit()
            .putString(key, mFirebaseRemoteConfig.getString(key));

     /* getActivity().getSharedPreferences( "",Context.MODE_PRIVATE)
          .edit()
          .putString(key, sharedPreferences.getString(key, "keychanged"));
*/
        getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
            .edit().commit();
        //mFirebaseRemoteConfig.getKeysByPrefix("DELAY
      }
    }
    //        (int) SettingArg.get("DELAY_INTER_FRAME_SETTING").asDouble());
    Log.d("settingfragmennt", "Config params updated: " + key);
    getActivity().getSharedPreferences("", Context.MODE_PRIVATE)
        .edit().apply();
  }

  public SettingsFragment() {
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