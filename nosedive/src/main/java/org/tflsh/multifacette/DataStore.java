package org.tflsh.multifacette;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Objects;

// [END post_class]

@IgnoreExtraProperties class nosediveSettings {
  private static final nosediveSettings instance;

  static {
    instance = new nosediveSettings();
  }

//  public String username = "defaultUserName";
 // public String email = "defaultUserMailAddress";
  public int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  public int DELAY_GUESSING_SETTING = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  public int DELAY_CHOICE_WORDS_SETTING = 10000;
  public long UI_ANIMATION_DELAY = 300;
  public String DEFAULT_PROJECT_KEY = "rescatest";
  public String BASE_URL = "https://dev.tuxun.fr/nosedive/";

  public boolean syncProjectOnNextStartup = false;

  public boolean syncCloud = false;
  public boolean sync = false;

  //nevr call this, use getinstance
  public nosediveSettings() {
    // Default constructor required for calls to DataSnapshot.getValue(User.class)
  }

  public static nosediveSettings getInstance() {
    return instance;
  }
}

public class DataStore extends PreferenceDataStore {
  private static final String TAG = "DataStore";
  private final FirebaseAuth mAuth;
  final nosediveSettings settings;
  final FirebaseUser user;
  final FirebaseDatabase database;

  final DatabaseReference usersPath;
  final DatabaseReference thisUserPath;

  public DataStore() {
    super();
    //get auth for id in db
    settings = nosediveSettings.getInstance();

    Log.d(TAG, "DataStore creating new settings");

    mAuth = FirebaseAuth.getInstance();

    user = mAuth.getCurrentUser();
    // Write a message to the database
    database = FirebaseDatabase.getInstance();

    //dbusersPath =
    usersPath = database.getReference("configs");

    if (isUserConnected()) {
      thisUserPath = usersPath.child(Objects.requireNonNull(user).getUid());
    } else {
      //could be null to avoid write mess in db
      thisUserPath = usersPath.child("user_unset");
    }

    //  thisUserPath.keepSynced(true);

    //Firebase Database paths must not contain '.', '#', '$', '[', or ']'
    DatabaseReference myRef = database.getReference("logs");
    myRef.setValue("test1");
    myRef.setValue("test2");
    myRef = database.getReference("test_second");
    myRef.setValue("test3");
    myRef.setValue("test4");
    myRef = database.getReference("test_test");
    myRef.setValue("test5");
    myRef.setValue("test6");
  }

  protected boolean isUserConnected() {
    return mAuth.getCurrentUser() != null;
  }

  protected void setValue(String key, boolean value) {
    thisUserPath.child(key).setValue(value);

    switch (key) {
      case "syncProjectOnNextStartup":
        settings.syncProjectOnNextStartup = value;
        break;
      case "syncCloud":
        settings.syncCloud = value;
        break;

      case "sync":
        settings.sync = value;
        break;
    }
  }

  protected boolean getValue(String key, boolean def) {
    switch (key) {
      case "syncProjectOnNextStartup":
        return settings.syncProjectOnNextStartup;
      case "syncCloud":
        return settings.syncCloud;

      case "sync":
        return
            settings.sync;

      default:
        return def;
    }
  }

  protected void setValue(String key, int value) {
    Log.d(TAG, thisUserPath + "setValue " + value);

    thisUserPath.child(key).setValue(value);

    switch (key) {
      case "DELAY_INTER_FRAME_SETTING":
        settings.DELAY_INTER_FRAME_SETTING = value;
        break;
      case "DELAY_GUESSING_SETTING":
        settings.DELAY_GUESSING_SETTING = value;
        break;

      case "DELAY_CHOICE_WORDS_SETTING":
        settings.DELAY_CHOICE_WORDS_SETTING = value;
        break;
      case "UI_ANIMATION_DELAY":
        settings.UI_ANIMATION_DELAY = value;
        break;
    }
  }

  protected int getValue(String key,int def) {
    switch (key) {
      case "DELAY_INTER_FRAME_SETTING":
        return settings.DELAY_INTER_FRAME_SETTING;
      case "DELAY_GUESSING_SETTING":
        return settings.DELAY_GUESSING_SETTING;

      case "DELAY_CHOICE_WORDS_SETTING":
        return
            settings.DELAY_CHOICE_WORDS_SETTING;

      case "UI_ANIMATION_DELAY":
        return (int)
            settings.UI_ANIMATION_DELAY;
      default:
        return def;
    }
  }

  protected void setValue(String key, String value) {
    Log.d(TAG, "setValue " + value);

    thisUserPath.child(key).setValue(value);

    switch (key) {
      case "BASE_URL":
        settings.BASE_URL = value;

        break;
      case "DEFAULT_PROJECT_KEY":
        settings.DEFAULT_PROJECT_KEY = value;
        break;
    }
  }
@Nullable
  protected String getValue(String key, String def) {
    switch (key) {
      case "BASE_URL":
        return settings.BASE_URL;
      case "DEFAULT_PROJECT_KEY":
        return settings.DEFAULT_PROJECT_KEY;

      default:
        return def;
    }
  }

  protected String getUserName() {
    return Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
  }

  //should set settings in db and file
  @Override
  public void putString(String key,  String value) {
     //we should set config locally then push to db
    setValue(key, value);
  }

  @Override
  public void putBoolean(String key,  boolean value) {
    // Save the value somewhere
    setValue(key, value);
  }

  @Override
  public void putInt(String key,  int value) {
    // Save the value somewhere
    setValue(key, value);

  }

  //should get settings from db or file
  @Override
  @Nullable
  public String getString(final String key,  String defValue) {


    return getValue(key, defValue);
  }

  public boolean getBoolean(final String key,  final boolean defValue) {

    return getValue(key, defValue);
  }

  public int getInt(final String key,  int defValue) {

    return getValue(key, defValue);
  }
}
