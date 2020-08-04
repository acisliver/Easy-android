package com.example.easy_written;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class Image_MainAdpater extends AppCompatActivity {

    private FragmentStatePagerAdapter fragmentPagerAdapter;
    private String getfilesNameList,getPaths,getfilesDateList;
    private Bundle imageMainBundle,keyWordTimeBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adepter);

        ViewPager viewPager=findViewById(R.id.viewpager);
        fragmentPagerAdapter=new ViewPageAdapter(getSupportFragmentManager());

        TabLayout tableLayout=(TabLayout) findViewById(R.id.tab_layout);
        viewPager.setAdapter(fragmentPagerAdapter);
        tableLayout.setupWithViewPager(viewPager);

        Intent getIntent=getIntent();
        getfilesNameList=getIntent.getStringExtra("filesNameList");
        getPaths=getIntent.getStringExtra("paths");
        getfilesDateList=getIntent.getStringExtra("filesDateList");

        Image_Tap imageTapFragment = new Image_Tap();
        imageMainBundle = new Bundle();
        imageMainBundle.putString("imageFileNamedata",getfilesNameList);
        imageMainBundle.putString("imageFileDatedata",getfilesDateList);
        imageMainBundle.putString("imagePathdata",getPaths);
        imageTapFragment.setArguments(imageMainBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.viewpager,imageTapFragment).commitNow();

        KeywordTime keywordTimeFragment=new KeywordTime();
        keyWordTimeBundle=new Bundle();
        keyWordTimeBundle.putString("keyFileNamedata",getfilesNameList);
        keyWordTimeBundle.putString("keyFileDatedata",getfilesDateList);
        keyWordTimeBundle.putString("keyPathdata",getPaths);
        keywordTimeFragment.setArguments(keyWordTimeBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.viewpager,keywordTimeFragment).commitNow();
    }


}
