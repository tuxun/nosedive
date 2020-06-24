package org.tflsh.nosedive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MyIntentManager extends BroadcastReceiver {

    public MyIntentManager(Context context, ProgressBar pgb, ImageView buffer, ImageView result) {


        Log.d("MyBroadcastReceiver", "MyBroadcastReceiver");
    }

    private static final String TAG = "MyIntentManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();

        Log.d("MyIntentManager", "org.tflsh.nosedive.MyIntentManager.onReceive");
        Log.e("intentereceiverparent:", super.getClass().getSuperclass().toString());

        Task asyncTask = new Task(pendingResult, intent);
        asyncTask.execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private Task(PendingResult pendingResult, Intent intent) {
            this.pendingResult = pendingResult;
            this.intent = intent;
        }

        @Override
        protected String doInBackground(String... strings) {
            String log = "";
            log += ("Action: " + intent.getAction() + "\n");
            log += ("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME));
            Log.d(TAG, log);

            switch (intent.getAction()) {
                case "msg":
                    // Log.e("intentereceiverparent:",super.getClass().toString());
                    break;
                default:
                    Log.e("intentereceiver", "unknow action");

                    break;
            }
            return log;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }

}
