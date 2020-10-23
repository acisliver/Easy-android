package com.example.easy_written;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

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
}
