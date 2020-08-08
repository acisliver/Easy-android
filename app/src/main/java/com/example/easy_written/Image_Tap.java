package com.example.easy_written;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class Image_Tap extends Fragment {
    private View view;
    private Animator currentAnimator;
    private int shortAnimationDuration;
    TextView text_view;
    private int k;
    private String ImageFileNamedata,ImageFileDatedata,ImagePathdata;
    private String imageTT;
    private ArrayList<String> ImagePtahArrayList;
    private ArrayList<ImageView> imageViewArrayList;
    private ScrollView scroll;
    private String imageTT2;

    public static Image_Tap newInstance(){
        Image_Tap image_tap=new Image_Tap();
        return image_tap;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activitiy_image_tap,container,false);
        scroll=(ScrollView)view.findViewById(R.id.Scroll);


        text_view=(TextView)view.findViewById(R.id.text_view);
        text_view.setMovementMethod(new ScrollingMovementMethod());

        Image_MainAdpater image_mainAdpater=new Image_MainAdpater();
        imageTT=image_mainAdpater.counter();
        Log.e("imageTT",imageTT);
        imageTT2=imageTT+"/"+"STTtext.txt";
        Log.e("imageTT2",imageTT2);

        //setText
        Highlighting highlighting=new Highlighting();
        text_view.setText(highlighting.highlight(ReadTextFile(imageTT2)));

        ImagePtahArrayList=new ArrayList<>();


        //경로 하위 폴터에서 이미지 파일 찾기
        findPNGFile(imageTT);

        imageViewArrayList=new ArrayList<>();

        //ImageTab 사진 넣기
        GridLayout gridLayout=view.findViewById(R.id.container);
        for(k=0;k<ImagePtahArrayList.size();k++){
            File imgFile = new  File(ImagePtahArrayList.get(k));
            if(imgFile.exists()){
                Log.e("ImagePtahArrayList.get(i)",ImagePtahArrayList.get(k));

                ImageView myImage = new ImageView(getActivity());
                myImage.setClickable(true);

                myImage.setImageBitmap(getImageFile(ImagePtahArrayList.get(k)));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                Log.e("ImagePtahArrayList.get(k)",ImagePtahArrayList.get(k));

                //이미지 크기 셋팅
                myImage.setLayoutParams(Imagesetting(params));

                imageViewArrayList.add(myImage);

                //해당 이미지를 gridlayout에 추가
                gridLayout.addView(myImage);

                //이미지 클릭시
                imageViewArrayList.get(k).setOnClickListener(getOnClickDoSomething(k));

            }else{
                Log.e("npop","nono");
            }
        }

        //데이터 받기
//        Bundle bundle = getArguments();
//        if(bundle!=null){
//            ImageFileNamedata = bundle.getString("imageFileNamedata");
//            ImageFileDatedata = bundle.getString("imageFileDatedata");
//            ImagePathdata = bundle.getString("imagePathdata"); }
//        else{
//        }


        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        return view;
    }

    private void zoomImageFromThumb(final View thumbView, String str) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) view.findViewById(
                R.id.expanded_image);
        expandedImageView.setImageURI(Uri.fromFile(new File(str)));

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
        view.findViewById(R.id.container)
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
                text_view.setText("");


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
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
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

    //해당 경로에 이미지 파일이 존재 하는지 확인
    private void findPNGFile(String strDirPath ) {
        File path = new File(strDirPath);
        File[] fList = path.listFiles();
        for (int i = 0; i < fList.length; i++) {
            if (fList[i].isFile() ) {
                if(fList[i].getPath().endsWith("png")) {
                    Log.e("fList[i].getPath()",fList[i].getPath());
                    ImagePtahArrayList.add(fList[i].getPath());
                }
            }
//            else if (fList[i].isDirectory()) {
//                //ListFile(fList[i].getPath()); // 재귀함수 호출 }
//            }
        }

    }

    //해당 경로의 이미지 파일 가져오기
    private Bitmap getImageFile(String path) {
        File file=new File(path);
        if(file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                return bitmap;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }else{
            Log.e("Do not exist file","!");
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

    //text파일 읽기 keyword에도 존재.
    private String ReadTextFile(String path){
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

    //image클릭시
    View.OnClickListener getOnClickDoSomething(int k)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                zoomImageFromThumb(imageViewArrayList.get(k),ImagePtahArrayList.get(k));
                scroll.smoothScrollTo(0,0);


            }
        };
    }
}
