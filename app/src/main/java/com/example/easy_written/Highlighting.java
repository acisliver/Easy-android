package com.example.easy_written;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;


public class Highlighting {
    public SpannableString highlight(String text){
        String content = text;
        String HighlightingWord="안드로이드";
        SpannableString spannableString = new SpannableString(content);
        int start=content.indexOf(HighlightingWord);
        Log.i("start","start:"+start);
        while(start!=-1) {
            int end = start + HighlightingWord.length();
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = content.indexOf(HighlightingWord, start + 1);}
        return spannableString;
    }
}
