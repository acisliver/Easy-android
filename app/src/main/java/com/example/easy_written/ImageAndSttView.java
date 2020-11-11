package com.example.easy_written;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageAndSttView extends AppCompatActivity {
    private TextView mTextView;
    private Animator mCurrentAnimator;
    private ScrollView mScroll;
    private int mShortAnimationDuration;
    private ArrayList<String> mImagePtahArrayList;
    private ArrayList<ImageView> mImageViewArrayList;
    private MediaPlayer mMediaPlayer;
    private int mAudioDuration;
    private SeekBar mAudioSeekBar;
    public boolean mPlayAndCancelCheck =true, mIsPause=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_and_stt_view);


        mScroll=(ScrollView)findViewById(R.id.Scroll);
        mTextView=(TextView)findViewById(R.id.text_view);

        Intent mintent=getIntent();
        String paths=mintent.getStringExtra("paths");


        //setText
        Highlighting mHighlighting=new Highlighting();
        mTextView.setText(mHighlighting.ReadTextFile(paths+"/"+"STTtext.txt"));

        //imagelist
        mImagePtahArrayList=new ArrayList<>();

        //경로 하위 폴터에서 이미지 파일 찾기
        findPNGFile(paths);
        mImageViewArrayList=new ArrayList<>();

        //ImageTab 사진 넣기
        GridLayout gridLayout=findViewById(R.id.container);
        for(int k=0;k<mImagePtahArrayList.size();k++){
            File mImgFile = new  File(mImagePtahArrayList.get(k));
            if(mImgFile.exists()){
                ImageView mMyImage = new ImageView(getApplicationContext());
                mMyImage.setClickable(true);

                mMyImage.setImageBitmap(getImageFile(mImagePtahArrayList.get(k)));
                GridLayout.LayoutParams mParams = new GridLayout.LayoutParams();

                //이미지 크기 셋팅
                mMyImage.setLayoutParams(Imagesetting(mParams));
                mImageViewArrayList.add(mMyImage);

                //해당 이미지를 gridlayout에 추가
                gridLayout.addView(mMyImage);

                //이미지 클릭시
                mImageViewArrayList.get(k).setOnClickListener(getOnClickDoSomething(k));
            }else{
            }
        }

        // Retrieve and cache the system's default "short" animation time.
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

    private void zoomImageFromThumb(final View thumbView, String str) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        // Load the high-resolution "zoomed-in" image.
        final ImageView mExpandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        mExpandedImageView.setImageURI(Uri.fromFile(new File(str)));

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect mStartBounds = new Rect();
        final Rect mFinalBounds = new Rect();
        final Point mGlobalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // mView. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(mStartBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(mFinalBounds, mGlobalOffset);
        mStartBounds.offset(-mGlobalOffset.x, -mGlobalOffset.y);
        mFinalBounds.offset(-mGlobalOffset.x, -mGlobalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float mStartScale;
        if ((float) mFinalBounds.width() / mFinalBounds.height()
                > (float) mStartBounds.width() / mStartBounds.height()) {
            // Extend start bounds horizontally
            mStartScale = (float) mStartBounds.height() / mFinalBounds.height();
            float mStartWidth = mStartScale * mFinalBounds.width();
            float mDeltaWidth = (mStartWidth - mStartBounds.width()) / 2;
            mStartBounds.left -= mDeltaWidth;
            mStartBounds.right += mDeltaWidth;
        } else {
            // Extend start bounds vertically
            mStartScale = (float) mStartBounds.width() / mFinalBounds.width();
            float mStartHeight = mStartScale * mFinalBounds.height();
            float mDeltaHeight = (mStartHeight - mStartBounds.height()) / 2;
            mStartBounds.top -= mDeltaHeight;
            mStartBounds.bottom += mDeltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        mExpandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mExpandedImageView.setPivotX(0f);
        mExpandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet mSet = new AnimatorSet();
        mSet
                .play(ObjectAnimator.ofFloat(mExpandedImageView, View.X,
                        mStartBounds.left, mFinalBounds.left))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.Y,
                        mStartBounds.top, mFinalBounds.top))
                .with(ObjectAnimator.ofFloat(mExpandedImageView, View.SCALE_X,
                        mStartScale, 1f))
                .with(ObjectAnimator.ofFloat(mExpandedImageView,
                        View.SCALE_Y, mStartScale, 1f));
        mSet.setDuration(mShortAnimationDuration);
        mSet.setInterpolator(new DecelerateInterpolator());
        mSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        mSet.start();
        mCurrentAnimator = mSet;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = mStartScale;
        mExpandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }
                mTextView.setText("");


                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet mSet = new AnimatorSet();
                mSet.play(ObjectAnimator
                        .ofFloat(mExpandedImageView, View.X, mStartBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.Y,mStartBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mExpandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                mSet.setDuration(mShortAnimationDuration);
                mSet.setInterpolator(new DecelerateInterpolator());
                mSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        mExpandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        mExpandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                mSet.start();
                mCurrentAnimator = mSet;
            }
        });
    }

    //해당 경로에 이미지 파일이 존재 하는지 확인
    private void findPNGFile(String strDirPath) {
        File mPath = new File(strDirPath);
        File[] mFList = mPath.listFiles();
        for (int i = 0; i < mFList.length; i++) {
            if (mFList[i].isFile() ) {
                if(mFList[i].getPath().endsWith("png")) {
                    mImagePtahArrayList.add(mFList[i].getPath());
                }
            }
        }
    }

    //해당 경로의 이미지 파일 가져오기
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

    //각각의 이미지 크기 조절
    private GridLayout.LayoutParams Imagesetting(GridLayout.LayoutParams params){
        params.width = 243;
        params.height = 243;
        params.topMargin=10;
        params.bottomMargin=10;
        params.leftMargin=10;
        params.rightMargin=10;
        return params;
    }

    //image클릭시
    View.OnClickListener getOnClickDoSomething(int k)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                zoomImageFromThumb(mImageViewArrayList.get(k),mImagePtahArrayList.get(k));
                mScroll.smoothScrollTo(0,0);
            }
        };
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