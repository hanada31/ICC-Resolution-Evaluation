package org.smssecure.smssecure.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.audio.AudioSlidePlayer;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.AttachmentDatabase;
import org.smssecure.smssecure.jobs.PartProgressEvent;
import org.smssecure.smssecure.mms.AudioSlide;
import org.smssecure.smssecure.mms.SlideClickListener;
import org.smssecure.smssecure.util.Util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioView extends FrameLayout implements AudioSlidePlayer.Listener {

  private static final String TAG = AudioView.class.getSimpleName();

  private final @NonNull AnimatingToggle controlToggle;
  private final @NonNull ImageView       playButton;
  private final @NonNull ImageView       pauseButton;
  private final @NonNull ImageView       downloadButton;
  private final @NonNull ProgressWheel   downloadProgress;
  private final @NonNull SeekBar         seekBar;
  private final @NonNull TextView        timestamp;

  private @Nullable SlideClickListener downloadListener;
  private @Nullable AudioSlidePlayer   audioSlidePlayer;
  private int backwardsCounter;

  public AudioView(Context context) {
    this(context, null);
  }

  public AudioView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, R.layout.audio_view, this);

    this.controlToggle    = (AnimatingToggle) findViewById(R.id.control_toggle);
    this.playButton       = (ImageView) findViewById(R.id.play);
    this.pauseButton      = (ImageView) findViewById(R.id.pause);
    this.downloadButton   = (ImageView) findViewById(R.id.download);
    this.downloadProgress = (ProgressWheel) findViewById(R.id.download_progress);
    this.seekBar          = (SeekBar) findViewById(R.id.seek);
    this.timestamp        = (TextView) findViewById(R.id.timestamp);

    this.playButton.setOnClickListener(new PlayClickedListener());
    this.pauseButton.setOnClickListener(new PauseClickedListener());
    this.seekBar.setOnSeekBarChangeListener(new SeekBarModifiedListener());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      this.playButton.setImageDrawable(context.getDrawable(R.drawable.play_icon));
      this.pauseButton.setImageDrawable(context.getDrawable(R.drawable.pause_icon));
      this.playButton.setBackground(context.getDrawable(R.drawable.ic_circle_fill_white_48dp));
      this.pauseButton.setBackground(context.getDrawable(R.drawable.ic_circle_fill_white_48dp));
    }

    if (attrs != null) {
      TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioView, 0, 0);
      setTint(typedArray.getColor(R.styleable.AudioView_foregroundTintColor, Color.WHITE),
              typedArray.getColor(R.styleable.AudioView_backgroundTintColor, Color.WHITE));
      typedArray.recycle();
    }
  }

  public void setAudio(final @NonNull MasterSecret masterSecret,
                       final @NonNull AudioSlide audio,
                       final boolean showControls)
  {

    if (showControls && audio.isPendingDownload()) {
      controlToggle.displayQuick(downloadButton);
      seekBar.setEnabled(false);
      downloadButton.setOnClickListener(new DownloadClickedListener(audio));
      if (downloadProgress.isSpinning()) downloadProgress.stopSpinning();
    } else if (showControls && audio.getTransferState() == AttachmentDatabase.TRANSFER_PROGRESS_STARTED) {
      controlToggle.displayQuick(downloadProgress);
      seekBar.setEnabled(false);
      downloadProgress.spin();
    } else {
      controlToggle.displayQuick(playButton);
      seekBar.setEnabled(true);
      if (downloadProgress.isSpinning()) downloadProgress.stopSpinning();
    }

    this.audioSlidePlayer = AudioSlidePlayer.createFor(getContext(), masterSecret, audio, this);
  }

  public void cleanup() {
    if (this.audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
      this.audioSlidePlayer.stop();
    }
  }

  public void setDownloadClickListener(@Nullable SlideClickListener listener) {
    this.downloadListener = listener;
  }

  @Override
  public void onStart() {
    if (this.pauseButton.getVisibility() != View.VISIBLE) {
      togglePlayToPause();
    }
  }

  @Override
  public void onStop() {
    if (this.playButton.getVisibility() != View.VISIBLE) {
      togglePauseToPlay();
    }

    if (seekBar.getProgress() + 5 >= seekBar.getMax()) {
      backwardsCounter = 4;
      onProgress(0.0, 0);
    }
  }

  @Override
  public void setFocusable(boolean focusable) {
    super.setFocusable(focusable);
    this.playButton.setFocusable(focusable);
    this.pauseButton.setFocusable(focusable);
    this.seekBar.setFocusable(focusable);
    this.seekBar.setFocusableInTouchMode(focusable);
    this.downloadButton.setFocusable(focusable);
  }

  @Override
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    this.playButton.setClickable(clickable);
    this.pauseButton.setClickable(clickable);
    this.seekBar.setClickable(clickable);
    this.seekBar.setOnTouchListener(clickable ? null : new TouchIgnoringListener());
    this.downloadButton.setClickable(clickable);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    this.playButton.setEnabled(enabled);
    this.pauseButton.setEnabled(enabled);
    this.seekBar.setEnabled(enabled);
    this.downloadButton.setEnabled(enabled);
  }

  @Override
  public void onProgress(double progress, long millis) {
    int seekProgress = (int)Math.floor(progress * this.seekBar.getMax());

    if (seekProgress > seekBar.getProgress() || backwardsCounter > 3) {
      backwardsCounter = 0;
      this.seekBar.setProgress(seekProgress);
      this.timestamp.setText(String.format("%02d:%02d",
                                           TimeUnit.MILLISECONDS.toMinutes(millis),
                                           TimeUnit.MILLISECONDS.toSeconds(millis)));
    } else {
      backwardsCounter++;
    }
  }

  public void setTint(int foregroundTint, int backgroundTint) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      this.playButton.setBackgroundTintList(ColorStateList.valueOf(foregroundTint));
      this.playButton.setImageTintList(ColorStateList.valueOf(backgroundTint));
      this.pauseButton.setBackgroundTintList(ColorStateList.valueOf(foregroundTint));
      this.pauseButton.setImageTintList(ColorStateList.valueOf(backgroundTint));
    } else {
      this.playButton.setColorFilter(foregroundTint, PorterDuff.Mode.SRC_IN);
      this.pauseButton.setColorFilter(foregroundTint, PorterDuff.Mode.SRC_IN);
    }

    this.downloadButton.setColorFilter(foregroundTint, PorterDuff.Mode.SRC_IN);
    this.downloadProgress.setBarColor(foregroundTint);

    this.timestamp.setTextColor(foregroundTint);
    this.seekBar.getProgressDrawable().setColorFilter(foregroundTint, PorterDuff.Mode.SRC_IN);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      this.seekBar.getThumb().setColorFilter(foregroundTint, PorterDuff.Mode.SRC_IN);
    }
  }

  private double getProgress() {
    if (this.seekBar.getProgress() <= 0 || this.seekBar.getMax() <= 0) {
      return 0;
    } else {
      return (double)this.seekBar.getProgress() / (double)this.seekBar.getMax();
    }
  }

  private void togglePlayToPause() {
    controlToggle.displayQuick(pauseButton);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      AnimatedVectorDrawable playToPauseDrawable = (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.play_to_pause_animation);
      pauseButton.setImageDrawable(playToPauseDrawable);
      playToPauseDrawable.start();
    }
  }

  private void togglePauseToPlay() {
    controlToggle.displayQuick(playButton);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      AnimatedVectorDrawable pauseToPlayDrawable = (AnimatedVectorDrawable)getContext().getDrawable(R.drawable.pause_to_play_animation);
      playButton.setImageDrawable(pauseToPlayDrawable);
      pauseToPlayDrawable.start();
    }
  }

  private class PlayClickedListener implements View.OnClickListener {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
      try {
        Log.w(TAG, "playbutton onClick");
        if (audioSlidePlayer != null) {
          togglePlayToPause();
          audioSlidePlayer.play(getProgress());
        }
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    }
  }

  private class PauseClickedListener implements View.OnClickListener {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
      Log.w(TAG, "pausebutton onClick");
      if (audioSlidePlayer != null) {
        togglePauseToPlay();
        audioSlidePlayer.stop();
      }
    }
  }

  private class DownloadClickedListener implements View.OnClickListener {
    private final @NonNull AudioSlide slide;

    private DownloadClickedListener(@NonNull AudioSlide slide) {
      this.slide = slide;
    }

    @Override
    public void onClick(View v) {
      if (downloadListener != null) downloadListener.onClick(v, slide);
    }
  }

  private class SeekBarModifiedListener implements SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    @Override
    public synchronized void onStartTrackingTouch(SeekBar seekBar) {
      if (audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
        audioSlidePlayer.stop();
      }
    }

    @Override
    public synchronized void onStopTrackingTouch(SeekBar seekBar) {
      try {
        if (audioSlidePlayer != null && pauseButton.getVisibility() == View.VISIBLE) {
          audioSlidePlayer.play(getProgress());
        }
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    }
  }

  private class TouchIgnoringListener implements OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      return true;
    }
  }

  @SuppressWarnings("unused")
  public void onEventAsync(final PartProgressEvent event) {
    if (audioSlidePlayer != null && event.attachment.equals(this.audioSlidePlayer.getAudioSlide().asAttachment())) {
      Util.runOnMain(new Runnable() {
        @Override
        public void run() {
          downloadProgress.setInstantProgress(((float) event.progress) / event.total);
        }
      });
    }
  }

}
