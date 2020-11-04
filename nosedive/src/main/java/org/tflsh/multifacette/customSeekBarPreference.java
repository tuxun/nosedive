package org.tflsh.multifacette;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.preference.SeekBarPreference;

public class customSeekBarPreference extends SeekBarPreference implements OnSeekBarChangeListener {
  private SeekBar mSeekBar;
  private int mProgress = 50;

  public customSeekBarPreference(Context context) {
    super(context);
  }

  public customSeekBarPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public customSeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /*
    @Override
    protected View onCreateView(ViewGroup parent) {
      super.onCreateView(parent);
      LayoutInflater inflater =
          (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflater.inflate(R.layout.customseekbar, parent, false);
      mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
      mSeekBar.setProgress(mProgress);
      mSeekBar.setOnSeekBarChangeListener(this);
      return view;
    }
  */
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    Log.d("seekpref", "onProgressChanged" + mProgress + " " + getPersistedInt(mProgress));

    if (!fromUser) {
      return;
    }

    setValue(progress);
  }

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

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    Log.d("seekpref",
        "onSetInitialValue" + mProgress + " " + restoreValue + " " + getPersistedInt(mProgress));
    super.
        //  setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
            setValue(mProgress);
    setDefaultValue(mProgress);

    notifyChanged();
  }

  public void setValue(int value) {
    Log.d("seekpref", "onProgressChanged" + mProgress + " " + getPersistedInt(mProgress));

    if (shouldPersist()) {
    }
    persistInt(value);

    if (value != mProgress) {
      mProgress = value;
      notifyChanged();
    }
  }
}

