package com.example.easy_written;

import androidx.annotation.NonNull;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.auth.Session;

public class UserAccount extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private TextView user_id;
    private ImageView user_profile;
    private DrawerLayout mDrawerLayout;
    private String name,email,image;
    private FirebaseAuth accountfirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        user_id=findViewById(R.id.user_id);
        user_profile=findViewById(R.id.user_profile);

        Intent getintent=getIntent();
        name=getintent.getStringExtra("name");
        image=getintent.getStringExtra("photoUrl");

        //구글로 로그인 했을 시
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            name = acct.getDisplayName();
            email = acct.getEmail();
            Uri personPhoto = acct.getPhotoUrl();
            user_id.setText(name);
            Glide.with(this).load(personPhoto).into(user_profile);

        }


        //카카오로 로그인 했을 시
        else if(Session.getCurrentSession().isOpened()){
            user_id.setText(name);
            if(image!=null)
                Glide.with(this).load(image).into(user_profile);
            else
                Glide.with(this).load(R.drawable.easyicon2).into(user_profile);
        }

        //회원가입후 로그인 했을 시
        else{
            //이름 넣어 줘야 함
            Glide.with(this).load(R.drawable.easyicon2).into(user_profile);
        }


        //메뉴
        NavigationView navigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationViewing.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.navi_user_id);
        navUsername.setText(name);
        ImageView navi_user_image = headerView.findViewById(R.id.navi_user_image);
        if(image!=null)
            Glide.with(this).load(image).into(navi_user_image);
        else
            Glide.with(this).load(R.drawable.easyicon2).into(navi_user_image);

        //액션바
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 메뉴 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //메뉴 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                int id = menuItem.getItemId();
                //계정정보로 이동
                if(id == R.id.account){
                    Toast.makeText(getApplicationContext(),"이미 계정모드 입니다.", Toast.LENGTH_SHORT).show();
                }


                //로그아웃
                else if(id == R.id.logout){
                    //google 아이디로 로그인 했을 경우
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        MainActivity.googleApiClient.connect();
                        SelectMode selectMode=new SelectMode();
                        selectMode.google_logout();
                        finish();
                    }

                    //kakao 아이디로 로그인 했을 경우
                    if(Session.getCurrentSession().isOpened()){
                       SelectMode selectMode=new SelectMode();
                       selectMode.kakao_logout();
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