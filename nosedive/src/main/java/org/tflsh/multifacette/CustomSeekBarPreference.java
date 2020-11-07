package org.tflsh.multifacette;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.preference.SeekBarPreference;

public class CustomSeekBarPreference extends SeekBarPreference implements OnSeekBarChangeListener {
  private int mProgress = 50;
  private static final String CLASS_NAME ="customSeekBarPreference";

  public CustomSeekBarPreference(Context context) {
    super(context);
  }


  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    Log.d(CLASS_NAME, "onProgressChanged" + mProgress + " " + getPersistedInt(mProgress));

    if (!fromUser) {
      return;
    }

    setValue(progress);
  }

  /**
   * https://stackoverflow.com/questions/30170384/custom-inline-seekbarpreference-how-to-set-seekbar-progress-on-the-1st-run
   * @param a see
   * @param index stack post
   * @return value
   */
  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInt(index, 0);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // not used
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // not used
  }
  /** @deprecated
   * important workaround to missing default values https://stackoverflow.com/questions/30170384/custom-inline-seekbarpreference-how-to-set-seekbar-progress-on-the-1st-run
   */
 @SuppressWarnings("deprecation") @Deprecated
  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    Log.d(CLASS_NAME,
        "onSetInitialValue" + mProgress + " " + restoreValue + " " + getPersistedInt(mProgress));
    super.
            setValue(mProgress);
    setDefaultValue(mProgress);

    notifyChanged();
  }

  @Override
  public void setValue(int value) {
    Log.d(CLASS_NAME, "setvalue" + mProgress + " " + getPersistedInt(mProgress)+" "+ value);
    if (shouldPersist()) {
    }
    persistInt(value);
    mProgress = value;
    setDefaultValue(mProgress);

    notifyChanged();
    if (value != mProgress) {

    }
  }
}

