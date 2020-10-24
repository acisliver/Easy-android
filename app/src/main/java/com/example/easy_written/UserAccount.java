package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;


public class UserAccount extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private TextView mUserId;
    private ImageView mUserProfile;
    private DrawerLayout mDrawerLayout;
    private String mName, mEmail, mImage;
    private FirebaseAuth mAuth;  //파이어 베이스 인증 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        mUserId =findViewById(R.id.user_id);
        mUserProfile =findViewById(R.id.user_profile);
        Intent mGetintent=getIntent();
        mName =mGetintent.getStringExtra("name");
        mImage =mGetintent.getStringExtra("photoUrl");

        mUserId.setText("Easy-쉽게 기록");
        Glide.with(this).load(R.drawable.easyicon2).into(mUserProfile);


        //메뉴
        NavigationView mNavigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View headerView = mNavigationViewing.getHeaderView(0);
        TextView mNavUsername = (TextView) headerView.findViewById(R.id.navi_user_id);
        mNavUsername.setText(mName);
        ImageView mNaviUserImage = headerView.findViewById(R.id.navi_user_image);
        if(mImage !=null)
            Glide.with(this).load(mImage).into(mNaviUserImage);
        else
            Glide.with(this).load(R.drawable.easyicon2).into(mNaviUserImage);

        //액션바
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 메뉴 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //메뉴 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int mId = menuItem.getItemId();
                //계정정보로 이동
                if(mId == R.id.account){
                    Toast.makeText(getApplicationContext(),"이미 계정모드 입니다.", Toast.LENGTH_SHORT).show();
                }

                //로그아웃
                else if(mId == R.id.logout){
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        mAuth.getInstance().signOut();
                        finish();
                    }
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }


    //메뉴 탭에서 항목 선택시 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}