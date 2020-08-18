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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
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
import com.kakao.auth.Session;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private EditText get_input_email;
    private EditText get_input_password;
    private String Temail,Tpassword;

    private SignInButton google_sign_button; //구글 로그인 버튼
    private FirebaseAuth auth;  //파이어 베이스 인증 객체
    private GoogleApiClient googleApiClient;  //구글 api 클라이언트 객체\
    private static final int fire_sign_google=100;  //구글 로그인 결과 코드

    //kakao
    private Button btn_custom_login;
    private KakaoSessionCallback sessionCallback = new KakaoSessionCallback();
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Edittext
        get_input_email=findViewById(R.id.inputEmail);
        get_input_password=findViewById(R.id.inputPassword);

        //로그인 버튼
        Button login_button=findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SelectMode.class);

                Temail=get_input_email.getText().toString();
                Tpassword=get_input_password.getText().toString();

                Log.d("get_input_email",Temail);
                Log.d("get_input_password",Tpassword);
                if(Temail.equals("kyanggogo") && Tpassword.equals("kyanggogo")){
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"다시 입력해 주세요!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //회원가입 버튼
        Button signup_button=findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);
            }
        });

        //google login
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();

        auth=FirebaseAuth.getInstance();//파이어 베이스 인증 객체 초기화

        google_sign_button=findViewById(R.id.google_sign_button);
        google_sign_button.setOnClickListener(new View.OnClickListener() {//구글 로그인 버튼 클릭
            @Override
            public void onClick(View v) {
                Intent intent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,fire_sign_google);
            }
        });
        //auth=FirebaseAuth.getInstance();

        //kakao login
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);

        btn_custom_login=findViewById(R.id.kakao_login);
        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
                Intent intent=new Intent(MainActivity.this,SelectMode.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //로그인 인증을 요청 했을 때 결과값을 돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);
        //google
        if(requestCode==fire_sign_google){
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){//인증이 성공적이면 ==true
                GoogleSignInAccount account=result.getSignInAccount();//account라는 데이터는 구글정보를 담고있다. 프로필, 이름, ...
                resultLogin(account); //로그인 결과 값 출력 수행하라는 메소드
            }
        }

        //kakao
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    // (google)onActivityResult에서 인증이 끝났으면 최종적 로그인 수행하는 메소드
    private void resultLogin(final GoogleSignInAccount account) {
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){  //로그인 성공
                            Toast.makeText(MainActivity.this,"로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(), SelectMode.class);
//                            intent.putExtra("nickname",account.getDisplayName());
//                            intent.putExtra("photoUrl",String.valueOf(account.getPhotoUrl()));
                            startActivity(intent);
                        }
                        else{  //로그인 실패
                            Toast.makeText(MainActivity.this,"로그인 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }

}
