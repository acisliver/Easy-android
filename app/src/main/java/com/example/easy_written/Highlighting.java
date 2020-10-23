package com.example.easy_written;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Highlighting {
    public SpannableString highlight(String text){
        String mContent = text;
        String mHighlightingWord="안드로이드";
        SpannableString mSpannableString = new SpannableString(mContent);
        int mStart=mContent.indexOf(mHighlightingWord);
        Log.i("start","start:"+mStart);
        while(mStart!=-1) {
            int mEnd = mStart + mHighlightingWord.length();
            mSpannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), mStart, mEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannableString.setSpan(new UnderlineSpan(), mStart, mEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mStart = mContent.indexOf(mHighlightingWord, mStart + 1);}
        return mSpannableString;
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
