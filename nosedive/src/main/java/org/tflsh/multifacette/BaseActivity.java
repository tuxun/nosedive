package org.tflsh.multifacette;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;
//deaaad!
public class BaseActivity extends AppCompatActivity {

  public ProgressDialog mProgressDialog;

  public void showProgressDialog() {
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setMessage(getString(R.string.app_name));
      mProgressDialog.setIndeterminate(true);
    }

    mProgressDialog.show();
  }

  public void hideProgressDialog() {
    if (mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
  }

  public void hideKeyboard(View view) {
    final InputMethodManager imm =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }
  @Override
  public boolean onKeyDown(int keycode, KeyEvent event) {
    Log.d("BaseActivity", "onKeyDown" + keycode);
/*  if(mSlideshowFragment!=null)
  {
    mSlideshowFragment.cleanNext();
  }*/

    if (keycode == KeyEvent.KEYCODE_BACK) {
      onResume();
      hideKeyboard(getWindow().getDecorView());
      return true;
    }
    return false;

  }

  @Override
  public void onStop() {
    super.onStop();
    hideProgressDialog();
  }
}
