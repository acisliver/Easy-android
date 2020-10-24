package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class SelectMode extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private DrawerLayout mDrawerLayout;
    private String mPhotoUrl, mName;
    private FirebaseAuth mGoogleAuth;  //파이어 베이스 인증 객체
    private FirebaseAuth mFireAuth;
    private GoogleApiClient mGoogleApiClient;  //구글 api 클라이언트 객체\

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        //main에서 넘어 온 값을 받는곳
        //main에서 로그인을 진행하기 때문에 값을 받음
        Intent mIntent=getIntent();
        mName =mIntent.getStringExtra("name");
        mPhotoUrl =mIntent.getStringExtra("photoUrl");

        //메뉴
        NavigationView mNavigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View mHeaderView = mNavigationViewing.getHeaderView(0);
        TextView mNavUsername = (TextView) mHeaderView.findViewById(R.id.navi_user_id);
        mNavUsername.setText(mName);
        ImageView mVaviUserImage = mHeaderView.findViewById(R.id.navi_user_image);
        if(mPhotoUrl !=null)
            Glide.with(this).load(mPhotoUrl).into(mVaviUserImage);
        else
            Glide.with(this).load(R.drawable.easyicon2).into(mVaviUserImage);

        //actionbar
        androidx.appcompat.widget.Toolbar mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        mActionBar.setDisplayHomeAsUpEnabled(true); // 메뉴 버튼 만들기
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //메뉴 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();

                //계정정보로 이동
                if(id == R.id.account){
                    Intent mAccountIntent=new Intent(getApplicationContext(),UserAccount.class);
                    mAccountIntent.putExtra("name", mName);
                    mAccountIntent.putExtra("photoUrl", mPhotoUrl);
                    startActivity(mAccountIntent);
                }

                //로그아웃
                else if(id == R.id.logout){
                    //회원 또는 google 아이디로 로그인 했을 경우
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        //구글 로그인 했을 경우
                        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected())
                            GoogleLogout();
                        //회원으로 로그인 했을 경우
                        else
                            FirebaseAuth.getInstance().signOut();

                        finish();
                    }

                    //kakao 아이디로 로그인 했을 경우
                    else if(Session.getCurrentSession().isOpened()){
                        KakaoLogout();
                        finish();
                    }
                    else{
                    }
                    Intent mIntent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(mIntent);
                }
                return true;
            }
        });

        //pc강의로 이동 버튼
        Button mMode1=findViewById(R.id.mode1);
        mMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(SelectMode.this,CV_record.class);
                startActivity(mIntent);
            }
        });

        Button mSelectFile=findViewById(R.id.SelectFile);
        mSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(SelectMode.this,FileView.class);
                startActivity(mIntent);
            }
        });

        GoogleSignInAccount mAcct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (mAcct != null) {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient =new GoogleApiClient.Builder(this)
                    .enableAutoManage(this,this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                    .build();
            mGoogleAuth =FirebaseAuth.getInstance();
        }
    }

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

    //google 로그아웃
   private void GoogleLogout() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mGoogleAuth.signOut();
                if(mGoogleApiClient.isConnected()){
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()){
                                setResult(1);
                            }else{
                                setResult(0);
                            }
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                setResult(-1);
                finish();

            }
        });

        //google

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();

        auth=FirebaseAuth.getInstance();//파이어 베이스 인증 객체 초기화

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //google 아이디로 로그인 했을 경우
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            google_logout();

        //kakao 아이디로 로그인 했을 경우
        if(Session.getCurrentSession().isOpened()==true)
            kakao_logout();

    }


    //google 로그아웃
    private void google_logout() {
        googleApiClient.connect();
        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                auth.signOut();
                if(googleApiClient.isConnected()){
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()){
                                Toast.makeText(SelectMode.this,"로그아웃 성공",Toast.LENGTH_SHORT).show();
                                setResult(1);
                            }else{
                                Toast.makeText(SelectMode.this,"로그아웃 실패",Toast.LENGTH_SHORT).show();
                                setResult(0);
                            }
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                setResult(-1);
                finish();

            }
        });
    }

    private void kakao_logout(){
        UserManagement.getInstance()
                .requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Toast.makeText(SelectMode.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        Log.d("kakao 로그아웃","ok");
                    }

                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void KakaoLogout(){
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
