package org.tflsh.multifacette;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //?PreferenceManager.getDefaultSharedPreferences(getContext());
    // addPreferencesFromResource(R.xml.root_preferences);
  }

  @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    //  setPreferencesFromResource(R.xml.root_preferences, rootKey);
    Context context = getPreferenceManager().getContext();
    //    Preferences prefs = (Preferences) getPreferenceManager().getSharedPreferences();
    PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

    SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(context);
    notificationPreference.setKey("notifications");
    notificationPreference.setTitle("Enable message notifications");

    PreferenceCategory notificationCategory = new PreferenceCategory(context);
    notificationCategory.setKey("notifications_category");
    notificationCategory.setTitle("Notifications");
    screen.addPreference(notificationCategory);
    notificationCategory.addPreference(notificationPreference);

    Preference feedbackPreference = new Preference(context);
    feedbackPreference.setKey("feedback");
    feedbackPreference.setTitle("Send feedback");
    feedbackPreference.setSummary("Report technical issues or suggest new features");

    PreferenceCategory helpCategory = new PreferenceCategory(context);
    helpCategory.setKey("help");
    helpCategory.setTitle("Help");
    screen.addPreference(helpCategory);
    helpCategory.addPreference(feedbackPreference);

    setPreferenceScreen(screen);
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