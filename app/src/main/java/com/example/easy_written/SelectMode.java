package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

public class SelectMode extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private String photoUrl,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        //main에서 넘어 온 값을 받는곳
        //main에서 로그인을 진행하기 때문에 값을 받음
        Intent intent=getIntent();
        name=intent.getStringExtra("name");
        photoUrl=intent.getStringExtra("photoUrl");

        //메뉴
        NavigationView navigationViewing = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationViewing.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.navi_user_id);
        navUsername.setText(name);
        ImageView navi_user_image = headerView.findViewById(R.id.navi_user_image);
        if(photoUrl!=null)
            Glide.with(this).load(photoUrl).into(navi_user_image);
        else
            Glide.with(this).load(R.drawable.easyicon2).into(navi_user_image);

        //actionbar
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
                String title = menuItem.getTitle().toString();

                //계정정보로 이동
                if(id == R.id.account){
                    Intent accountIntent=new Intent(getApplicationContext(),UserAccount.class);
                    accountIntent.putExtra("name",name);
                    accountIntent.putExtra("photoUrl",photoUrl);
                    startActivity(accountIntent);
                }

                //설정정보로 이동
                else if(id == R.id.setting){
                    Toast.makeText(context, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                }

                //로그아웃
                else if(id == R.id.logout){
                    //google 아이디로 로그인 했을 경우
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        google_logout();
                        finish();
                    }

                    //kakao 아이디로 로그인 했을 경우
                    if(Session.getCurrentSession().isOpened()){
                        kakao_logout();
                        finish();
                    }
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);

                }

                return true;
            }
        });

        //pc강의로 이동 버튼
        Button mode1=findViewById(R.id.mode1);
        mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectMode.this,CV_record.class);
                startActivity(intent);
            }
        });

        Button SelectFile=findViewById(R.id.SelectFile);
        SelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectMode.this,FileView.class);
                startActivity(intent);
            }
        });

        //google
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        MainActivity.googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        MainActivity.auth=FirebaseAuth.getInstance();//파이어 베이스 인증 객체 초기화

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
    void google_logout() {
        MainActivity.googleApiClient.connect();
        MainActivity.googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                MainActivity.auth.signOut();
                if(MainActivity.googleApiClient.isConnected()){
                    Auth.GoogleSignInApi.signOut(MainActivity.googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()){
                                Log.e("google로그아웃","성공");
                                setResult(1);
                            }else{
                                Log.e("google로그아웃","실패");
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

    void kakao_logout(){
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Log.e("kakao로그아웃","성공");
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
