package com.example.easy_written;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class KeywordTime extends Fragment {
    private View view;
    private String KeywordTimeFileName,KeywordTimeFileDate,KeywordTimeFilePath, AudioPath;
    private Bundle KeywordTimeBundle;
    private int PlayAndCancelCheck=1;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private String[] SplitKeyName;

    //newInstance
    public static KeywordTime newInstance(){
        KeywordTime keywordtime=new KeywordTime();
        return keywordtime;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.activity_keyword_time,container,false);

        //데이터 전달받기
        KeywordTimeBundle = getArguments();
        if(KeywordTimeBundle!=null){
            KeywordTimeFileName = KeywordTimeBundle.getString("keyFileNamedata");
            KeywordTimeFileDate = KeywordTimeBundle.getString("keyFileDatedata");
            KeywordTimeFilePath = KeywordTimeBundle.getString("keyPathdata");
//            SplitKeyName=KeywordTimeFilePath.split("#");
            AudioPath=KeywordTimeFilePath+"/"+ "_audio_record"+".3gp";

            Log.e("KeywordTimeFileName!!~~ :",KeywordTimeFileName);
            Log.e("KeywordTimeFileDate!!~~ :",KeywordTimeFileDate);
            Log.e("KeywordTimeFilePath!!~~ :",KeywordTimeFilePath);
            Log.e("AudioPath->",AudioPath);
            Log.e("Environment",Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"EASYWRITTEN"+ "/"); }
        else{
        }

        SpannableString content1 = new SpannableString("안드로이드");
        content1.setSpan(new UnderlineSpan(), 0, content1.length(), 0);

        TextView Tandroid1=view.findViewById(R.id.Tandroid1);
        Tandroid1.setText(content1);
        TextView Tandroid2=view.findViewById(R.id.Tandroid2);
        Tandroid2.setText(content1);
        TextView Tandroid3=view.findViewById(R.id.Tandroid3);
        Tandroid3.setText(content1);

        Button AudioRunButton=view.findViewById(R.id.AudioRunButton);
        AudioRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAndCancel();
            }
        });

        int value=30;
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar) ;
        progressBar.setProgress(value) ;

        return view;
    }

    private void setupMediaRecorder() {
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioPath);
    }

    public void PlayAndCancel(){
        if(PlayAndCancelCheck==1) {
            mediaPlayer = new MediaPlayer();
            try {
                Log.i("AudioPathdata",AudioPath);
                mediaPlayer.setDataSource(AudioPath);
                //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }

            PlayAndCancelCheck=0;
        }
        else if(PlayAndCancelCheck==0){
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
                setupMediaRecorder();
            }
            PlayAndCancelCheck=1;
        }
    }



}
