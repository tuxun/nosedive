package org.tflsh.multifacette;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements
    SharedPreferences.OnSharedPreferenceChangeListener {
  private static final SettingsFragment instance=new SettingsFragment();
  //private String defaultProjectKey;

  public static SettingsFragment getInstance() {
    return instance;
  }

  private static final String CLASSNAME = "SettingsFragment";
  DataStore datastore;
  // --Commented out by Inspection (08/11/20 09:02):private FirebaseRemoteConfig mFirebaseRemoteConfig;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


  }

  public void makeEditTextPreference(Context contextArg, String keyArg) {
    EditTextPreference retPreference = new EditTextPreference(contextArg);
    String value =datastore.getValue(keyArg, "makeEditTextPreference no data with key "+keyArg);

    retPreference.setKey(keyArg);

    retPreference.setText(value);
    retPreference.setDefaultValue(value);

    retPreference.setTitle(keyArg + " original value: "+value);
  }

    public void makeSeekBar(Context contextArg, PreferenceScreen scr, String keyArg,int defValue, int maxValue,boolean showValueArg) {

    CustomSeekBarPreference retPreference = new CustomSeekBarPreference(contextArg);
//getPreferenceManager().getSharedPreferences().edit().putInt(keyArg,value);
    retPreference.setKey(keyArg);
    retPreference.setMax(maxValue);
      retPreference.setShowSeekBarValue(showValueArg);
      int value =datastore.getInt(keyArg, defValue,retPreference,scr);

    //retPreference.setValue(value);
    //retPreference.setDefaultValue(value);

    retPreference.setTitle(keyArg + " original value: "+value);
//return retPreference;
  }

  @RequiresApi(api = Build.VERSION_CODES.P) @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    datastore = new DataStore(getContext());

    PreferenceManager preferenceManager = getPreferenceManager();

    PreferenceScreen screen =    preferenceManager.createPreferenceScreen(getContext());
    preferenceManager.setPreferenceDataStore(datastore);

    if (datastore.isUserConnected()) {
      Log.d(CLASSNAME, "found connected user " + datastore.getUserName());
    }
      PreferenceCategory timerCategory = new PreferenceCategory(
          requireContext());
      timerCategory.setKey("timers");
      timerCategory.setTitle("Timers");
      screen.addPreference(timerCategory);


   makeSeekBar(getContext(),screen,"DELAY_INTER_FRAME_SETTING",666,10000,true);

      //screen.addPreference(delayFramePreference);

 makeSeekBar(getContext(),screen,"DELAY_GUESSING_SETTING",5000,30000,true);
    //  screen.addPreference(seekBarPreference);

   makeSeekBar(getContext(),screen,"DELAY_CHOICE_WORDS_SETTING",10000,30000,true);
      //screen.addPreference(delayGuessingPreference);
makeSeekBar(getContext(),screen,"UI_ANIMATION_DELAY",300,1000,true);
      //screen.addPreference(delayUIPreference);

  makeEditTextPreference(getContext(),"BASE_URL");
    //screen.addPreference(baseUrlPreference);

    String defaultProjectKey = "DEFAULT_PROJECT_KEY";
makeEditTextPreference(getContext(), defaultProjectKey);
//    screen.addPreference(baseProjectPreference);



      SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(requireContext());
      notificationPreference.setKey("logout");
      notificationPreference.setTitle("url" + datastore.getValue(defaultProjectKey, "null"));
      screen.addPreference(notificationPreference);

      PreferenceCategory helpCategory = new PreferenceCategory(
          Objects.requireNonNull(requireContext()));
      helpCategory.setKey("help");
      helpCategory.setTitle("Help");
      screen.addPreference(helpCategory);

      setPreferenceScreen(screen);


/*
    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(60)
        .build();

    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.root_preferences);

    mFirebaseRemoteConfig.fetchAndActivate()
        .addOnCompleteListener(Objects.requireNonNull(getActivity()).getMainExecutor(), new OnCompleteListener<Boolean>() {
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
                mFirebaseRemoteConfig.getString(defaultProjectKey));

          }
        });
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.root_preferences);
*/

  }

  @Override public void onPause() {
    super.onPause();
    Log.d(CLASSNAME, "onPause");

  }

  @Override public void onResume() {
    super.onResume();
    Log.d(CLASSNAME, "onResume");

  }

  /**
   * Called when a shared preference is changed, added, or removed. This
   * may be called even if a preference is set to its existing value.
   *
   * <p>This callback will be run on your main thread.
   *
   * <p><em>See also
   * {@link DataStore}.</em>
   *
   * @param sharedPreferences The {@link SharedPreferences} that received
   * the change.
   * @param key The key of the preference that was changed, added, or
   */
  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//method mandatory to extend from @FragmentPreferencesCompat, but we use firebase module instead
  }
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