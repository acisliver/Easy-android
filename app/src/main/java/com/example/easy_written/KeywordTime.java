package com.example.easy_written;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KeywordTime extends Fragment {
    private View view;
    private String KeywordTimeFileName,KeywordTimeFileDate,KeywordTimeFilePath, iwantplayaudio;
    private Bundle KeywordTimeBundle;
    public boolean PlayAndCancelCheck=true, isPause=false;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private String[] SplitKeyName;
    private static TextView setSTTString;
    private String STT;
    private int AudioDuration;
    private SeekBar AudioSeekBar;

    //newInstance
    public static KeywordTime newInstance(){
        KeywordTime keywordtime=new KeywordTime();
        return keywordtime;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("create","create");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.activity_keyword_time,container,false);

        Image_MainAdpater image_mainAdpater=new Image_MainAdpater();
        STT=image_mainAdpater.counter();
        String STT2=STT+"/"+"STTtext.txt";
        iwantplayaudio=STT+"/"+ "_audio_record"+".3gp";
        Log.i("iwantplayaudio",iwantplayaudio);

        //데이터 전달받기
        KeywordTimeBundle = getArguments();
        if(KeywordTimeBundle!=null){
            KeywordTimeFileName = KeywordTimeBundle.getString("keyFileNamedata");
            KeywordTimeFileDate = KeywordTimeBundle.getString("keyFileDatedata");
            KeywordTimeFilePath = KeywordTimeBundle.getString("keyPathdata");
        }
        else{
        }

        SpannableString content1 = new SpannableString("안드로이드");
        content1.setSpan(new UnderlineSpan(), 0, content1.length(), 0);

        TextView textView=view.findViewById(R.id.setSTTTExtView);
        Highlighting highlighting=new Highlighting();
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(highlighting.highlight(ReadTextFile(STT2)));

        ImageView AudioRunButton=view.findViewById(R.id.AudioRunButton);
        AudioRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAndCancel(iwantplayaudio);
                if(PlayAndCancelCheck==true) {
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_play_circle_filled_24, getActivity().getTheme()));
                }
                else{
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_pause_circle_filled_24, getActivity().getTheme()));
                }
            }
        });


        AudioSeekBar = (SeekBar) view.findViewById(R.id.AudioSeekBar) ;

        ViewPageAdapter viewPageAdapter=new ViewPageAdapter(getFragmentManager());
        viewPageAdapter.notifyDataSetChanged();

        return view;
    }

    private void setupMediaRecorder() {
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void PlayAndCancel(String path){
        if(PlayAndCancelCheck==true) {
            mediaPlayer = new MediaPlayer();

            try {
                Log.e("녹음파일 재생","재생");
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
            AudioDuration=mediaPlayer.getDuration();
            AudioSeekBar.setMax(AudioDuration);
            AudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser)
                        mediaPlayer.seekTo(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            mediaPlayer.start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(mediaPlayer.isPlaying()){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AudioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            }).start();
            PlayAndCancelCheck=false;
        }
        else if(PlayAndCancelCheck==false){
            Log.e("녹음파일 재생 중지","중지");
            if (mediaPlayer != null) {
                isPause=true;
                mediaPlayer.pause();
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                setupMediaRecorder();
            }
            PlayAndCancelCheck=true;
        }
    }

    //경로의 텍스트 파일읽기
    public String ReadTextFile(String path){
        StringBuffer strBuffer = new StringBuffer();
        try{
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line="";
            while((line=reader.readLine())!=null){
                strBuffer.append(line+"\n");
            }
            reader.close();
            is.close();
        }catch (IOException e){
            e.printStackTrace();
            return "";
        }
        return strBuffer.toString();
    }
}
