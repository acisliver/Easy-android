package com.example.easy_written;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

public class Image_MainAdpater extends AppCompatActivity {

    private FragmentStatePagerAdapter mFragmentPagerAdapter;
    private String mGetfilesNameList,mGetPaths,mGetfilesDateList;
    private Bundle mImageMainBundle,mKeyWordTimeBundle;
    public static String mS;
    public static String mD;

    public String counter(){
        return mS;
    }
    public String Imagecounter(){ return mD; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adepter);

        ViewPager viewPager=findViewById(R.id.viewpager);
        mFragmentPagerAdapter=new ViewPageAdapter(getSupportFragmentManager());

        TabLayout tableLayout=(TabLayout) findViewById(R.id.tab_layout);
        viewPager.setAdapter(mFragmentPagerAdapter);
        tableLayout.setupWithViewPager(viewPager);

        //Fileview에서 값 받음
        Intent getIntent=getIntent();
        mGetfilesNameList=getIntent.getStringExtra("filesNameList");
        mGetPaths=getIntent.getStringExtra("paths");
        mGetfilesDateList=getIntent.getStringExtra("filesDateList");

        //Image_Tap에 값 전달
        Image_Tap imageTapFragment = new Image_Tap();
        mImageMainBundle = new Bundle();
        mImageMainBundle.putString("imageFileNamedata",mGetfilesNameList);
        mImageMainBundle.putString("imageFileDatedata",mGetfilesDateList);
        mImageMainBundle.putString("imagePathdata",mGetPaths);
        mD=mImageMainBundle.getString("imagePathdata");
        imageTapFragment.setArguments(mImageMainBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.viewpager,imageTapFragment).commitNow();

        //KeywordTime에 값 전달
        KeywordTime keywordTimeFragment=new KeywordTime();
        mKeyWordTimeBundle=new Bundle();
        mKeyWordTimeBundle.putString("keyFileNamedata",mGetfilesNameList);
        mKeyWordTimeBundle.putString("keyFileDatedata",mGetfilesDateList);
        mKeyWordTimeBundle.putString("keyPathdata",mGetPaths);
        mS=mKeyWordTimeBundle.getString("keyPathdata");
        keywordTimeFragment.setArguments(mKeyWordTimeBundle);
        //getSupportFragmentManager().beginTransaction().replace(R.id.viewpager,keywordTimeFragment).commitNow();


    }


}
