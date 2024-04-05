package org.intelehealth.app.activities.callflow.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.callflow.models.CallFlowResponseData;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.klivekit.utils.DateTimeUtils;

import java.io.IOException;
import java.util.List;

public class CallRecordingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "RecordedCallsAdapter";
    private Context mContext;
    private List<CallFlowResponseData> mAudioList;
    private MediaPlayer mMediaPlayer;
    private boolean isPlaying = false;
    private int mCurrentlyPlayingPosition = -1;
    private AudioViewHolder mPlayingHolder; // Store the currently playing item
    private AudioViewHolder mPreviousPlayingHolder; // Store the previously playing item
    private int mPreviousPlayingPosition = -1;
    private int mPausedPosition = -1;
    private boolean isLaunchedFirstTime = true;
    private boolean isLoading = false;
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;

    public CallRecordingsAdapter(Context context, List<CallFlowResponseData> audioList) {
        this.mContext = context;
        this.mAudioList = audioList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_call_functionality_layout, parent, false);
            return new CallRecordingsAdapter.AudioViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_progressbar, parent, false);
            return new CallRecordingsAdapter.ProgressHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CallRecordingsAdapter.AudioViewHolder) {
            // ((CallRecordingsAdapter.AudioViewHolder) holder).onBind(mAudioList.get(position));
            bindData(holder, position);
        } else if (holder instanceof CallRecordingsAdapter.ProgressHolder) {
            // Handle progress bar view holder
            ProgressHolder progressHolder = (ProgressHolder) holder;
            final Handler handler = new Handler();

              Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isLoading) {
                                progressHolder.progressBarLoader.setVisibility(View.VISIBLE);
                                progressHolder.layoutProgress.setVisibility(View.VISIBLE);
                            } else {
                                progressHolder.progressBarLoader.setVisibility(View.GONE);
                                progressHolder.layoutProgress.setVisibility(View.GONE);
                            }                        } catch (IllegalStateException ed) {
                            ed.printStackTrace();
                        }
                    }
                };
                handler.postDelayed(runnable, 3000);

        }

    }

    private void bindData(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder viewHolder = ((CallRecordingsAdapter.AudioViewHolder) holder);
        CallFlowResponseData callFlowResponseData = mAudioList.get(position);
        viewHolder.tvName.setText(callFlowResponseData.getReceiver());
        viewHolder.tvDateTime.setText(DateTimeUtils.convertDateToDisplayFormatInCall(callFlowResponseData.getCallStartTime()));

        viewHolder.progressBarAudio.setVisibility(View.GONE);

        viewHolder.seekBar.setVisibility(View.GONE); // Initially hide SeekBar
        viewHolder.buttonPlay.setOnClickListener(v -> {
            if (NetworkConnection.isOnline(mContext)) {

                handleClickListener(position, viewHolder, callFlowResponseData);
                viewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && mMediaPlayer != null) {
                            mMediaPlayer.seekTo(progress);
                            mPausedPosition = progress; // Store the paused position
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // Not needed
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // Not needed
                    }
                });
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAudioList.size() + (isLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("TAG", "getItemViewType: ");
        return position == mAudioList.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    }
    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDateTime;
        SeekBar seekBar;
        ImageView buttonPlay;
        ProgressBar progressBarAudio;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_patient_name_call);
            tvDateTime = itemView.findViewById(R.id.tv_date_time_call);
            seekBar = itemView.findViewById(R.id.seekbar);
            buttonPlay = itemView.findViewById(R.id.iv_play_audio);
            progressBarAudio = itemView.findViewById(R.id.progressBar_recording);

        }
       /* public void onBind(MissedCallsResponseDataModel item) {
            textViewTitle.setText(item.getNoanswer());
            textViewDescription.setText(DateTimeUtils.convertDateToDisplayFormatInCall(item.getCallTime()));
        }*/
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (isPlaying && holder == mPlayingHolder) {
            AudioViewHolder viewHolder = ((CallRecordingsAdapter.AudioViewHolder) holder);
            stopAudio(viewHolder);
        }
    }

    public void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void changePlayPauseAudioButtons(boolean isPlay, AudioViewHolder holder) {
        if (isPlay) {
            holder.buttonPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ui2_ic_play_audio));

        } else {
            holder.buttonPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ui2_ic_pause_audio));

        }

    }

    private void handleClickListener(int position, AudioViewHolder holder, CallFlowResponseData callFlowResponseData) {
        if (position == mCurrentlyPlayingPosition) {
            Log.d(TAG, "handleClickListener: step 1");
            if (isPlaying) {
                Log.d(TAG, "handleClickListener: step 2");
                pauseAudio();
                changePlayPauseAudioButtons(true, holder);
            } else {
                Log.d(TAG, "handleClickListener: step 3");
                resumeAudio();
                changePlayPauseAudioButtons(false, holder);
            }
        } else {
            Log.d(TAG, "handleClickListener: step 4");
            if (isPlaying && mPlayingHolder != null && mPlayingHolder != holder) {
                stopAudio(mPlayingHolder); // Stop playback of the currently playing audio
                changePlayPauseAudioButtons(true, mPlayingHolder); // Update UI of the previous playing audio
            }
            holder.buttonPlay.setVisibility(View.GONE); // Hide play/pause button
            holder.progressBarAudio.setVisibility(View.VISIBLE);
            stopAudio(holder); // Stop any currently playing audio

            changePlayPauseAudioButtons(false, holder);

            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(callFlowResponseData.getRecordingURL());
                mMediaPlayer.prepare();
                mMediaPlayer.setOnPreparedListener(mp -> {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.progressBarAudio.setVisibility(View.GONE);
                            holder.buttonPlay.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                    // When MediaPlayer is prepared, hide progress bar and show play/pause button
                    mMediaPlayer.seekTo(0); // Ensure audio starts from the beginning
                    mMediaPlayer.setOnCompletionListener(mp1 -> {
                        holder.seekBar.setProgress(holder.seekBar.getMax());
                        stopAudio(holder);
                        //changePlayPauseAudioButtons(false, holder);
                    });
                    mMediaPlayer.setOnErrorListener((mp2, what, extra) -> {
                        stopAudio(holder);
                        return false;
                    });

                    mMediaPlayer.setOnSeekCompleteListener(mp3 -> {
                        updateSeekBar(holder); // Update seek bar after seeking
                    });

                    mMediaPlayer.start();
                    isPlaying = true;
                    mCurrentlyPlayingPosition = position;
                    mPlayingHolder = holder;
                    if (mPreviousPlayingHolder != null && mPreviousPlayingHolder != mPlayingHolder) {
                        mPreviousPlayingHolder.seekBar.setVisibility(View.GONE);
                        changePlayPauseAudioButtons(true, mPreviousPlayingHolder); // Update UI of the previous playing audio
                    }
                    holder.seekBar.setVisibility(View.VISIBLE);
                    updateSeekBar(holder);
                    mPreviousPlayingHolder = mPlayingHolder;

                    // Start playback...
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pauseAudio() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mPausedPosition = mMediaPlayer.getCurrentPosition(); // Store the paused position
            mMediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void resumeAudio() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(mPausedPosition); // Seek to the paused position
            mMediaPlayer.start();
            isPlaying = true;
        }
    }

    private void stopAudio(AudioViewHolder holder) {
        holder.buttonPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ui2_ic_play_audio));

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;
            mCurrentlyPlayingPosition = -1;
            mPausedPosition = 0;
        }
    }

   /* private void updateSeekBar(AudioViewHolder holder) {
        if (mMediaPlayer != null) {
            holder.seekBar.setMax(mMediaPlayer.getDuration());
            holder.seekBar.setProgress(mMediaPlayer.getCurrentPosition());

            // Update seek bar progress every 100 milliseconds
            new Handler().postDelayed(() -> updateSeekBar(holder), 100);
            //holder.seekBar.postDelayed(new SeekBarUpdater(holder), 100);

        }
    }*/
   private void updateSeekBar(AudioViewHolder holder) {
       if (mMediaPlayer != null) {
           holder.seekBar.setMax(mMediaPlayer.getDuration());
           holder.seekBar.setProgress(mMediaPlayer.getCurrentPosition());

           Runnable seekBarUpdater = new Runnable() {
               @Override
               public void run() {
                   if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                       holder.seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                       // Schedule the next update after a short delay
                       holder.seekBar.postDelayed(this, 100);
                   }
               }
           };

           // Remove any existing callbacks to avoid duplicates
           holder.seekBar.removeCallbacks(seekBarUpdater);
           // Schedule the first update
           holder.seekBar.postDelayed(seekBarUpdater, 100);
       }
   }

    private class SeekBarUpdater implements Runnable {
        private AudioViewHolder mHolder;

        public SeekBarUpdater(AudioViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mHolder.seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                mHolder.seekBar.postDelayed(this, 100); // Schedule the next update
            }
        }
    }

    public void addLoading() {
        isLoading = true;
        notifyDataSetChanged();
    }

    public void removeLoading() {
        isLoading = false;
        notifyDataSetChanged();
    }

    public void clear() {
        mAudioList.clear();
        notifyDataSetChanged();
    }


    public class ProgressHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBarLoader;
        ConstraintLayout layoutProgress;

        ProgressHolder(View itemView) {
            super(itemView);
            progressBarLoader = itemView.findViewById(R.id.progressBar_load_more);
            layoutProgress = itemView.findViewById(R.id.layout_load_more_progress);

        }
    }
}