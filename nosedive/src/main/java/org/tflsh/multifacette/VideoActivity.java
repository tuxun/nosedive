package org.tflsh.multifacette;

import android.animation.TimeAnimator;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;

/**
 * This activity uses a {@link android.view.TextureView} to render the frames of a video decoded
 * using
 * {@link android.media.MediaCodec} API.
 */
public class VideoActivity extends Activity {

  private final TimeAnimator mTimeAnimator = new TimeAnimator();
  private final MediaExtractor mExtractor = new MediaExtractor();
  TextView mAttribView = null;
  String mName = null;
  private TextureView mPlaybackView;
  // A utility that wraps up the underlying input and output buffer processing operations
  // into an east to use API.
  private VideoDecoder mCodecWrapper;
  private Button mButton;
  private VideoActivity mThisActivity;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video);
    mPlaybackView = findViewById(R.id.PlaybackView);
    mAttribView = findViewById(R.id.AttribView);
    mThisActivity = this;

    //            mName=savedInstanceState.getString("filename");
    // Log.e("videoactivity", "starting video activity()"+videoUri.getPath());

  }

  /*
   * Called when the activity is first created.
   */
  @Override
  public void onResume() {
    super.onResume();
    mPlaybackView = findViewById(R.id.PlaybackView);
    mAttribView = findViewById(R.id.AttribView);
    Log.e("videoactivity", "onResume video activity()" + mName);
    //    startPlayback(mName);

    mButton = findViewById(R.id.button);
    mButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startPlayback();
        v.setVisibility(View.GONE);
      }
    });

    new Thread(new Runnable() {
      @Override public void run() {
        try {
          Thread.sleep(7500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        mThisActivity.finish();
        Log.e("videoactivity", "finish video activity()");
      }
    });
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.action_menu, menu);
      return true;
    }
  */
  @Override
  protected void onPause() {
    super.onPause();
    if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
      mTimeAnimator.end();
    }

    if (mCodecWrapper != null) {
      mCodecWrapper.stopAndRelease();
      mExtractor.release();
    }
  }
  /*
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_play) {
      mAttribView.setVisibility(View.VISIBLE);
      startPlayback();
      item.setEnabled(false);
    }
    return true;
  }
*/

  public void startPlayback() {

    // Construct a URI that points to the video resource that we want to play
  /*  Uri videoUri = Uri.parse("android.resource://"
        + getPackageName() + "/"
        + R.raw.sam);*/
    Uri videoUri = Uri.fromFile(new File(
        getIntent().getExtras().getString("filename")));

    try {
      // BEGIN_INCLUDE(initialize_extractor)
      mExtractor.setDataSource(this, videoUri, null);
      int nTracks = mExtractor.getTrackCount();

      // Begin by unselecting all of the tracks in the extractor, so we won't see
      // any tracks that we haven't explicitly selected.
      for (int i = 0; i < nTracks; ++i) {
        mExtractor.unselectTrack(i);
      }

      // Find the first video track in the stream. In a real-world application
      // it's possible that the stream would contain multiple tracks, but this
      // sample assumes that we just want to play the first one.
      for (int i = 0; i < nTracks; ++i) {
        // Try to create a video codec for this track. This call will return null if the
        // track is not a video track, or not a recognized video format. Once it returns
        // a valid VideoDecoder, we can break out of the loop.
        mCodecWrapper = VideoDecoder.fromVideoFormat(mExtractor.getTrackFormat(i),
            new Surface(mPlaybackView.getSurfaceTexture()));
        if (mCodecWrapper != null) {
          mExtractor.selectTrack(i);
          break;
        }
      }
      // END_INCLUDE(initialize_extractor)

      // By using a {@link TimeAnimator}, we can sync our media rendering commands with
      // the system display frame rendering. The animator ticks as the {@link Choreographer}
      // receives VSYNC events.
      mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
        @Override
        public void onTimeUpdate(final TimeAnimator animation,
            final long totalTime,
            final long deltaTime) {

          boolean isEos = ((mExtractor.getSampleFlags() & MediaCodec
              .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);

          // BEGIN_INCLUDE(write_sample)
          if (!isEos) {
            // Try to submit the sample to the codec and if successful advance the
            // extractor to the next available sample to read.
            boolean result = mCodecWrapper.writeSample(mExtractor, false,
                mExtractor.getSampleTime(), mExtractor.getSampleFlags());

            if (result) {
              // Advancing the extractor is a blocking operation and it MUST be
              // executed outside the main thread in real applications.
              mExtractor.advance();
            }
          }
          // END_INCLUDE(write_sample)

          // Examine the sample at the head of the queue to see if its ready to be
          // rendered and is not zero sized End-of-Stream record.
          MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
          mCodecWrapper.peekSample(out_bufferInfo);

          // BEGIN_INCLUDE(render_sample)
          if (out_bufferInfo.size <= 0 && isEos) {
            mTimeAnimator.end();
            mCodecWrapper.stopAndRelease();
            mExtractor.release();
          } else if (out_bufferInfo.presentationTimeUs / 1000 < totalTime) {
            // Pop the sample off the queue and send it to {@link Surface}
            mCodecWrapper.popSample(true);
          }
          // END_INCLUDE(render_sample)

        }
      });

      // We're all set. Kick off the animator to process buffers and render video frames as
      // they become available
      mTimeAnimator.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


