package com.example.easy_written;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAndSttView extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private TextView textView;
    private Animator currentAnimator;
    private int shortAnimationDuration;
    private ScrollView scrollview;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private MediaPlayer mMediaPlayer;
    private int mAudioDuration;
    private List<SlideItem> slideItemList;
    private SeekBar mAudioSeekBar;
    public boolean mPlayAndCancelCheck =true, mIsPause=false;
    private TextView mTextView;
    private int starting=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //build vierson plugin 4.0.0
        //grable vierson 6.1.1
        //gradle/wrapper distributionUrl=https\://services.gradle.org/distributions/gradle-6.1.1-all.zip

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_and_stt_view);
        findViewById(R.id.imageView).setVisibility(View.GONE);
        scrollview=findViewById(R.id.scrollView);
        viewPager2=findViewById(R.id.viepager2);
        textView=findViewById(R.id.textView3);
        textView.setMovementMethod(new ScrollingMovementMethod());

        scrollview.setOnTouchListener( new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        scrollview.setVisibility(View.GONE);


        Intent mintent=getIntent();
        String paths=mintent.getStringExtra("paths");

        Highlighting mHighlighting=new Highlighting();
        String content = mHighlighting.ReadTextFile(paths+"/"+"STTtext.txt");
        textView.setText(content);

        if (! Python.isStarted())
            Python.start(new AndroidPlatform(this));
        Python py=Python.getInstance();
        PyObject pyf=py.getModule("myscript");
        PyObject obj=pyf.callAttr("test",textView.getText().toString());
        if(!obj.toString().equals("null")){
            SpannableString spannableString = new SpannableString(content);
            for(int i=0;i<obj.asList().size();i++){
                int start=-1;
                while(true){
                    if(content.indexOf(obj.asList().get(i).toString(),start+1)==-1){
                        break;
                    }
                    start= content.indexOf(obj.asList().get(i).toString(),start+1);
                    int end = start + obj.asList().get(i).toString().length();
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6702")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(spannableString);
                }
            }
        }

        slideItemList=new ArrayList<>();
        SliderAdaper sliderAdaper=new SliderAdaper(slideItemList,viewPager2);

        //경로 하위 폴터에서 이미지 파일 찾기
        findPNGFile(paths);
        if(!slideItemList.isEmpty()){
            System.out.println(slideItemList);
            scrollview.setVisibility(View.VISIBLE);
        }
        viewPager2.setAdapter(sliderAdaper);

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);


        CompositePageTransformer compositePageTransformer=new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r=1-Math.abs(position);
                page.setScaleY(0.85f+r*0.15f);
            }
        });
        sliderAdaper.setOnItemClickListener(new SliderAdaper.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                zoomImageFromThumb(viewPager2,slideItemList.get(pos).getImage());
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        ImageView AudioRunButton=findViewById(R.id.AudioRunButton);
        AudioRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mIWantPlayAudio = paths +"/"+ "_audio_record"+".3gp";
                PlayAndCancel(mIWantPlayAudio);
                if(mPlayAndCancelCheck ==true) {
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_play_circle_filled_24, getApplicationContext().getTheme()));
                }
                else{
                    AudioRunButton.setImageDrawable(getResources().
                            getDrawable(R.drawable.ic_baseline_pause_circle_filled_24, getApplicationContext().getTheme()));
                }
            }
        });

        mAudioSeekBar = (SeekBar) findViewById(R.id.AudioSeekBar) ;
    }


    private void zoomImageFromThumb(final View thumbView, String imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.imageView);
        expandedImageView.setImageURI(Uri.fromFile(new File(imageResId)));

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });

        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);

                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        scrollview.smoothScrollTo(0,0);
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        scrollview.smoothScrollTo(0,0);
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private void findPNGFile(String strDirPath) {
        File mPath = new File(strDirPath);
        File[] mFList = mPath.listFiles();
        for (int i = 0; i < mFList.length; i++) {
            if (mFList[i].isFile() ) {
                if(mFList[i].getPath().endsWith("png")) {
                    slideItemList.add(new SlideItem(mFList[i].getPath()));
                }
            }
        }
    }
    private Bitmap getImageFile(String path) {
        File mFile=new File(path);
        if(mFile.exists()) {
            try {
                Bitmap mBitmap = BitmapFactory.decodeFile(path);
                return mBitmap;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }else{
        }
        return null;
    }


    //image클릭시
    private void PlayAndCancel(String path){
        if(starting==0){
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
            starting=1;
        }
        if(mPlayAndCancelCheck ==true && starting==1) {

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
        else if(mPlayAndCancelCheck ==false && starting==1){
            Log.e("녹음파일 재생 중지","중지");
            if (mMediaPlayer != null) {
                mIsPause =true;
                mMediaPlayer.pause();
            }
            mPlayAndCancelCheck =true;
        }
    }
}