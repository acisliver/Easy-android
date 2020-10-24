package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private EditText mGetInputEmail;
    private EditText mGetInputPassword;
    private String mEmail, mPassword;
    private FirebaseAuth mAuth;  //구글 파이어 베이스 인증 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //파이어 베이스 인증 객체 초기화
        mAuth =FirebaseAuth.getInstance();

        //Edittext
        mGetInputEmail =findViewById(R.id.inputEmail);
        mGetInputPassword =findViewById(R.id.inputPassword);

        //로그인 버튼
        Button mLoginButton=findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmail = mGetInputEmail.getText().toString();
                mPassword = mGetInputPassword.getText().toString();
                //logging in the user
                if(!mEmail.equals("") && !mPassword.equals("")){
                    UserLogin(mEmail, mPassword);
                }else if(mEmail.equals(""))
                    Toast.makeText(getApplicationContext(),"아이디를 입력해 주세요",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해 주세요",Toast.LENGTH_SHORT).show();
            }
        });

        //회원가입 버튼
        Button signup_button=findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(MainActivity.this,SignUp.class);
                startActivity(mIntent);
            }
        });

        //방문자
        Button mVisitorAccount=findViewById(R.id.visitorAccount);
        mVisitorAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent mIntent=new Intent(MainActivity.this,SelectMode.class);
                startActivity(mIntent);
            }
        });
    }



    //firebase userLogin method
    private void UserLogin(String UserEmail, String UserPassword){
        mAuth = FirebaseAuth.getInstance();
        //logging in the user
        mAuth.signInWithEmailAndPassword(UserEmail, UserPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), SelectMode.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "로그인 실패!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //로그인 했을 시 뒤로가기 버튼으로 다시 로그인 화면이 뜨지 않도록 하기 위해 destroy부분에서 finish필요
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    //(kakao)앱 종료후 재 실행 때 중복적으로 로그인 되지 않도록 하기 위해 필요
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //로그인 인증을 요청 했을 때 결과값을 돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}
