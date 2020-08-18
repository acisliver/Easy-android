package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

public class SelectMode extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private GoogleApiClient googleApiClient;  //구글 api 클라이언트 객체
    private FirebaseAuth auth;  //파이어 베이스 인증 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("모드 선택");
        actionBar.show();

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
}
