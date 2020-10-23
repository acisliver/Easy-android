package com.example.easy_written;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.IOException;

public class KeywordTime extends Fragment {
    private View mView;
    private TextView mTextView;
    private String mKeywordTimeFileName, mKeywordTimeFileDate, mKeywordTimeFilePath, mIWantPlayAudio;
    private String mSTT,mSTT2;
    private Bundle mKeywordTimeBundle;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private int mAudioDuration;
    private SeekBar mAudioSeekBar;
    public boolean mPlayAndCancelCheck =true, mIsPause=false;

    //newInstance
    public static KeywordTime newInstance(){
        KeywordTime mkeywordtime=new KeywordTime();
        return mkeywordtime;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.activity_keyword_time,container,false);

        Image_MainAdpater mImageMainAdpater=new Image_MainAdpater();
        mSTT =mImageMainAdpater.counter();
        mSTT2 = mSTT +"/"+"STTtext.txt";
        mIWantPlayAudio = mSTT +"/"+ "_audio_record"+".3gp";

        //데이터 전달받기
        mKeywordTimeBundle = getArguments();
        if(mKeywordTimeBundle !=null){
            mKeywordTimeFileName = mKeywordTimeBundle.getString("keyFileNamedata");
            mKeywordTimeFileDate = mKeywordTimeBundle.getString("keyFileDatedata");
            mKeywordTimeFilePath = mKeywordTimeBundle.getString("keyPathdata");
        }
        else{
        }

        Highlighting mHighlighting=new Highlighting();
        mTextView =mView.findViewById(R.id.setSTTTExtView);
        mTextView.setText(mHighlighting.ReadTextFile(mSTT2));

        ImageView AudioRunButton=mView.findViewById(R.id.AudioRunButton);
        AudioRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAndCancel(mIWantPlayAudio);
                if(mPlayAndCancelCheck ==true) {
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_pause_circle_filled_24, getActivity().getTheme()));
                }
                else{
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_play_circle_filled_24, getActivity().getTheme()));
                }
            }
        });

        mAudioSeekBar = (SeekBar) mView.findViewById(R.id.AudioSeekBar) ;
        ViewPageAdapter viewPageAdapter=new ViewPageAdapter(getFragmentManager());
        viewPageAdapter.notifyDataSetChanged();
        return mView;
    }

    private void setupMediaRecorder() {
        mMediaRecorder =new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void PlayAndCancel(String path){
        if(mPlayAndCancelCheck ==true) {
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
            mAudioDuration = mMediaPlayer.getDuration();
            mAudioSeekBar.setMax(mAudioDuration);
            mAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser)
                        mMediaPlayer.seekTo(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            mMediaPlayer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(mMediaPlayer.isPlaying()){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mAudioSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    }
                }
            }).start();
            mPlayAndCancelCheck =false;
        }
        else if(mPlayAndCancelCheck ==false){
            Log.e("녹음파일 재생 중지","중지");
            if (mMediaPlayer != null) {
                mIsPause =true;
                mMediaPlayer.pause();
            }
            mPlayAndCancelCheck =true;
        }
    }

}
