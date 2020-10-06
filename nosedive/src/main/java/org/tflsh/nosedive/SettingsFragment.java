package org.tflsh.nosedive;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.preference.PreferenceFragment;
import org.xmlpull.v1.XmlPullParser;

import static org.tflsh.nosedive.R.xml.root_preferences;

public class SettingsFragment extends Fragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//addPreferencesFromResource(root_preferences);

  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
   return inflater.inflate(R.layout.fragment_settings, container, false);
  }
}