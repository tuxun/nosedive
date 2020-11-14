package org.tflsh.multifacette;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import static java.util.Objects.requireNonNull;

// [END post_class]

@IgnoreExtraProperties class NosediveSettings {
  private static final NosediveSettings instance;

  static {
    instance = new NosediveSettings();
  }

  protected int interFrameDelay = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  protected int guessingDelay = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  protected int choiceDelay = 10000;
  protected long uiDelay = 300;
  protected String projectKey = "rescatest";
  protected String baseUrl = "https://dev.tuxun.fr/nosedive/";

  protected boolean syncProjectOnNextStartup = false;

  protected boolean syncCloud = false;
  protected boolean sync = false;

  //nevr call this, use getinstance
  public NosediveSettings() {
    // Default constructor required for calls to DataSnapshot.getValue(User.class)
  }

  public static NosediveSettings getInstance() {
    return instance;
  }
}

public class DataStore extends PreferenceDataStore {
  private static final String TAG = "DataStore";
  private final FirebaseAuth mAuth;
  final NosediveSettings settings;
  final FirebaseUser user;

  final FirebaseDatabase database;

  final DatabaseReference usersPath;
  final DatabaseReference thisUserPath;
final Context mContext;
  private String setValue;

  public DataStore(Context context) {
    super();
    mContext=context;
    //get auth for id in db
    settings = NosediveSettings.getInstance();

    Log.d(TAG, "DataStore creating new settings");

    mAuth = FirebaseAuth.getInstance();

    user = mAuth.getCurrentUser();
    // Write a message to the database
    database = FirebaseDatabase.getInstance();

    usersPath = database.getReference("configs");

    if (isUserConnected()) {
      thisUserPath = usersPath.child(requireNonNull(user).getUid());
    } else {
      //could be null to avoid write mess in db
      thisUserPath = usersPath.child("user_unset");
    }


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

  public boolean isUserConnected() {
    return mAuth.getCurrentUser() != null;
  }

  protected void setValue(String key, boolean value,Context context) {
    setValue = "setValue";
    Log.d(TAG, thisUserPath + " " + setValue + " " + value);
    if(isUserConnected())
    {    thisUserPath.child(key).setValue(value);}
    else {
      Toast.makeText(context, mContext.getString(R.string.loginTosaveConfigAcrossDevices),
          Toast.LENGTH_SHORT).show();
    }
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
        default:

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

      default: return def;
    }
  }

  protected void setValue(String key, int value,Context context) {
    Log.d(TAG, thisUserPath + " " + setValue + " " + value);
if(isUserConnected())
{    thisUserPath.child(key).setValue(value);}
else {
  Toast.makeText(context, mContext.getString(R.string.loginTosaveConfigAcrossDevices),
      Toast.LENGTH_SHORT).show();
}

    switch (key) {
      case "DELAY_INTER_FRAME_SETTING":
        settings.interFrameDelay = value;
        break;
      case "DELAY_GUESSING_SETTING":
        settings.guessingDelay = value;
        break;

      case "DELAY_CHOICE_WORDS_SETTING":
        settings.choiceDelay = value;
        break;
      case "UI_ANIMATION_DELAY":
        settings.uiDelay = value;
        break;
      default:
    }
  }

  protected int getValue(String key,int def) {
    switch (key) {
      case "DELAY_INTER_FRAME_SETTING":
        return settings.interFrameDelay;
      case "DELAY_GUESSING_SETTING":
        return settings.guessingDelay;

      case "DELAY_CHOICE_WORDS_SETTING":
        return
            settings.choiceDelay;

      case "UI_ANIMATION_DELAY":
        return (int)
            settings.uiDelay;
      default:
        return def;
    }
  }

  protected void setValue(String key, String value,Context context) {

    Log.d(TAG, thisUserPath + " " + setValue + " " + value);
    if(isUserConnected())
    {    thisUserPath.child(key).setValue(value);}
    else {
      Toast.makeText(context, mContext.getString(R.string.loginTosaveConfigAcrossDevices),
          Toast.LENGTH_SHORT).show();
    }
    switch (key) {
      case "BASE_URL":
        settings.baseUrl = value;

        break;
      case "DEFAULT_PROJECT_KEY":
        settings.projectKey = value;
        break;
      default:
    }
  }
@Nullable public String getValue(String key, String def) {
    switch (key) {
      case "BASE_URL":
        return settings.baseUrl;
      case "DEFAULT_PROJECT_KEY":
        return settings.projectKey;

      default:
        return def;
    }
  }

  public String getUserName() {
    return requireNonNull(mAuth.getCurrentUser()).getEmail();
  }

  //should set settings in db and file
  @Override
  public void putString(String key,  String value) {
     //we should set config locally then push to db
    setValue(key, value,mContext);

  }

  @Override
  public void putBoolean(String key,  boolean value) {
    // Save the value somewhere
    setValue(key, value,mContext);
  }

  @Override
  public void putInt(String key,  int value) {
    // Save the value somewhere
    setValue(key, value,mContext);

  }

  //should get settings from db or file
  @Override
  @Nullable
  public String getString(final String key,  String defValue) {


    return getValue(key, defValue);
  }
  @Override
  public boolean getBoolean(final String key,  final boolean defValue) {

    return getValue(key, defValue);
  }



  public int getInt(final String key,  int defValue,  final  CustomSeekBarPreference seekBarPreference,
      final PreferenceScreen screen)
  {
ValueEventListener postListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // Get Post object and use the values to update the UI
         int value=requireNonNull(dataSnapshot.getValue(int.class));
        Log.d(TAG,
            "getBoolean grabbing " + key + " default value= " + value);


seekBarPreference.setValue(value);
screen.addPreference(seekBarPreference);

      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

      }
    };
    thisUserPath.child(key).addListenerForSingleValueEvent(postListener);
    return getValue(key, defValue);
  }
}
