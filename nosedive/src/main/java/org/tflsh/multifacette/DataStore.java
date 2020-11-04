package org.tflsh.multifacette;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

// [END post_class]

@IgnoreExtraProperties class nosediveSettings {
  private static final nosediveSettings instance;

  static {
    instance = new nosediveSettings();
  }

  public String username = "defaultUserName";
  public String email = "defaultUserMailAddress";
  public int DELAY_INTER_FRAME_SETTING = 750;
  //temps durant lequel on regarde une image proposé apres le menu (en multiple d'interframedelay)
  public int DELAY_GUESSING_SETTING = 5000;
  //temps durant lequel on peut choisir deux mots (en multiple d'interframedelay)
  public int DELAY_CHOICE_WORDS_SETTING = 10000;
  public long UI_ANIMATION_DELAY = 300;
  public String DEFAUT_PROJECT_KEY = "rescatest";
  public String BASE_URL = "https://dev.tuxun.fr/nosedive/";

  public boolean syncProjectOnNextStartup = false;

  public boolean syncCloud = false;
  public boolean sync = false;

  //nevr call this, use getinstance
  public nosediveSettings() {
    // Default constructor required for calls to DataSnapshot.getValue(User.class)
  }

  public nosediveSettings(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public static nosediveSettings getInstance() {
    return instance;
  }
}

public class DataStore extends PreferenceDataStore {
  private static final String TAG = "DataStore";
  private final FirebaseAuth mAuth;
  nosediveSettings settings;
  FirebaseUser user;
  FirebaseDatabase database;

  DatabaseReference usersPath;
  DatabaseReference thisUserPath;
  DatabaseReference dbusersPath;

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
      thisUserPath = usersPath.child(user.getUid());
    } else {
      thisUserPath = usersPath.child("nouser");
    }

    //  thisUserPath.keepSynced(true);

    //Firebase Database paths must not contain '.', '#', '$', '[', or ']'
    DatabaseReference myRef = database.getReference("test");
    myRef.setValue("test1");
    myRef.setValue("test2");
    myRef = database.getReference("test_testjkjnk");
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

  protected boolean getValue(String key, boolean value) {
    switch (key) {
      case "syncProjectOnNextStartup":
        return settings.syncProjectOnNextStartup;
      case "syncCloud":
        return settings.syncCloud;

      case "sync":
        return
            settings.sync;

      default:
        return false;
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

  protected int getValue(String key, int value) {
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
        return 0;
    }
  }

  protected void setValue(String key, String value) {
    Log.d(TAG, "setValue " + value);

    thisUserPath.child(key).setValue(value);

    switch (key) {
      case "BASE_URL":
        settings.BASE_URL = value;

        break;
      case "DEFAUT_PROJECT_KEY":
        settings.DEFAUT_PROJECT_KEY = value;
        break;
    }
  }

  protected String getValue(String key, String value) {
    switch (key) {
      case "BASE_URL":
        return settings.BASE_URL;
      case "DEFAUT_PROJECT_KEY":
        return settings.DEFAUT_PROJECT_KEY;

      default:
        return "voidstring";
    }
  }

  protected String getUserName() {
    return mAuth.getCurrentUser().getEmail();
  }

  //should set settings in db and file
  @Override
  public void putString(String key, @Nullable String value) {
    // Save the value somewhere

    setValue(key, value);
    //thisUserPath.setValue(key,value);
    //we should set config localy then push to db
    Log.d(TAG, "wrote value to db " + user.getUid() + " " + key + " " + value);
    Log.d(TAG, "szttings said:  " + getValue(key, "void"));
  }

  @Override
  public void putBoolean(String key, @Nullable boolean value) {
    // Save the value somewhere
    setValue(key, value);
    Log.d(TAG, "wrote value to db " + user.getUid() + " " + key + " " + value);
    Log.d(TAG, "szttings said:  " + getValue(key, false));
  }

  @Override
  public void putInt(String key, @Nullable int value) {
    // Save the value somewhere
    setValue(key, value);

    Log.d(TAG, "szttings said:  " + getValue(key, 0));

    Log.d(TAG, "wrote value to db " + user.getUid() + " " + key + " " + value);
  }

  //should get settings from db or file
  @Override
  @Nullable
  public String getString(final String key, @Nullable String defValue) {
    // Retrieve the value
    final String[] post = new String[0];
    post[0] = defValue;

    DatabaseReference myRef = database.getReference(user.getUid());
    ValueEventListener postListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        // Get Post object and use the values to update the UI
        post[0] = dataSnapshot.getValue(String.class);
        setValue(key, dataSnapshot.getValue(boolean.class));

        // ...
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        // ...
      }
    };
    myRef.addListenerForSingleValueEvent(postListener);

    return post[0];
  }

  @Nullable
  public boolean getBoolean(final String key, @Nullable final boolean defValue) {
    final boolean[] post = new boolean[1];
    post[0] = defValue;

    DatabaseReference myRef = database.getReference(user.getUid());
    ValueEventListener postListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        // Get Post object and use the values to update the UI
        Log.d(TAG,
            "getBoolean grabbing " + key + " defvalue= " + dataSnapshot.getValue(boolean.class));
        //!!! type change cause mess
        ///thisUserPath.child(key).setValue(dataSnapshot.getValue(boolean.class));
        // ...
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        // ...
      }
    };
    myRef.addListenerForSingleValueEvent(postListener);

    return post[0];
  }

  @Nullable
  public int getInt(final String key, @Nullable int defValue) {

    final int[] ret = { 0 };
    DatabaseReference myRef = database.getReference(user.getUid());
    ValueEventListener postListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d(TAG, "datachanged " + key);

        // Get Post object and use the values to update the UI
        //             thisUserPath.setValue(key,  dataSnapshot.getValue(nosediveSettings.class));
        //!!! stop it overwrites db !!! thisUserPath.child(key).setValue(dataSnapshot.getValue(int.class));

        //ret[0] =dataSnapshot.getValue(int.class);
        // ...
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        // ...
      }
    };
    myRef.addListenerForSingleValueEvent(postListener);

    return getValue(key, defValue);
  }

  @Nullable
  public nosediveSettings getSettings(final String key, @Nullable nosediveSettings defValue) {
    Log.d(TAG, "getSettings " + key + " " + defValue);

    DatabaseReference myRef = database.getReference(user.getUid());
    ValueEventListener postListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        // Get Post object and use the values to update the UI
        //        thisUserPath.setValue(key,  dataSnapshot.getValue(nosediveSettings.class));
        Log.d(TAG, "datachanged " + dataSnapshot.getValue(nosediveSettings.class));
        // ...
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        // Getting Post failed, log a message
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
        // ...
      }
    };
    thisUserPath.addListenerForSingleValueEvent(postListener);

    return settings;
  }
}
