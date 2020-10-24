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

    private SignInButton mGoogleSignButton; //구글 로그인 버튼
    private FirebaseAuth mAuth;  //구글 파이어 베이스 인증 객체
    private GoogleApiClient mGoogleApiClient;  //구글 api 클라이언트 객체\
    private static final int mFireSignGoogle =100;  //구글 로그인 결과 코드
    private FirebaseAuth mFirebaseAuth;

    //kakao
    private Button mBtnCustomLogin;
    private SessionCallback mSessionCallback = new SessionCallback();
    private Session mSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                UserLogin(mEmail, mPassword);
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

        //google 회원 정보를 받아옴
        GoogleSignInOptions mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient =new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,mGoogleSignInOptions)
                .build();
        mAuth =FirebaseAuth.getInstance();//파이어 베이스 인증 객체 초기화

        //google login button
        mGoogleSignButton =findViewById(R.id.google_sign_button);
        mGoogleSignButton.setOnClickListener(new View.OnClickListener() {//구글 로그인 버튼 클릭
            @Override
            public void onClick(View v) {
                Intent mIntent=Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(mIntent, mFireSignGoogle);
            }
        });

        //kakao login button
        mSession = Session.getCurrentSession();
        mSession.addCallback(mSessionCallback);
        mBtnCustomLogin =findViewById(R.id.kakao_login);
        mBtnCustomLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSession.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
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
        //로그아웃 하지 않고 앱을 종료 했을경우, 자동 로그인 기능
        //google
        if(GoogleSignIn.getLastSignedInAccount(this)!=null){
            Intent mIntent=Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(mIntent, mFireSignGoogle);
        }
        //kakao
        if(Session.getCurrentSession().isOpened()){
            mSession.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
        }
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
        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(mSessionCallback);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //로그인 인증을 요청 했을 때 결과값을 돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);
        //google
        if(requestCode== mFireSignGoogle){
            GoogleSignInResult mResult=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(mResult.isSuccess()){//인증이 성공적이면 ==true
                GoogleSignInAccount mAccount=mResult.getSignInAccount();//account라는 데이터는 구글정보를 담고있다. 프로필, 이름, ...
                resultLogin(mAccount); //로그인 결과 값 출력 수행하라는 메소드
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
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){  //로그인 성공
                            Toast.makeText(MainActivity.this,"로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent mIntent=new Intent(getApplicationContext(), SelectMode.class);
                            mIntent.putExtra("name",account.getDisplayName());
                            mIntent.putExtra("photoUrl",String.valueOf(account.getPhotoUrl()));
                            startActivity(mIntent);
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

    //hash 킷값 얻기(kakao developer사이트에서 최초 앱 등록시 필요)
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

    //kakao로그인 클래스
    private class SessionCallback implements ISessionCallback {

        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            requestMe();
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void requestMe() {
            UserManagement.getInstance()
                    .me(new MeV2ResponseCallback() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                        }

                        @Override
                        public void onSuccess(MeV2Response result) {
                            Log.i("KAKAO_API", "사용자 아이디: " + result.getId());

                            UserAccount kakaoAccount = result.getKakaoAccount();
                            if (kakaoAccount != null) {

                                // 이메일
                                String email = kakaoAccount.getEmail();

                                if (email != null) {
                                    Log.i("KAKAO_API", "email: " + email);

                                } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // 동의 요청 후 이메일 획득 가능
                                    // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                                } else {
                                    // 이메일 획득 불가
                                    Log.i("KAKAO_API", "email: " + "null");
                                }

                                // 프로필
                                Profile mProfile = kakaoAccount.getProfile();

                                if (mProfile != null) {
                                    Log.d("KAKAO_API", "nickname: " + mProfile.getNickname());
                                    Log.d("KAKAO_API", "profile image: " + mProfile.getProfileImageUrl());
                                    Log.d("KAKAO_API", "thumbnail image: " + mProfile.getThumbnailImageUrl());

                                } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // 동의 요청 후 프로필 정보 획득 가능

                                } else {
                                    // 프로필 획득 불가
                                }
                            }

                            Intent mIntent=new Intent(getApplicationContext(),SelectMode.class);
                            Profile mProfile = kakaoAccount.getProfile();
                            mIntent.putExtra("name",mProfile.getNickname());
                            mIntent.putExtra("photoUrl",mProfile.getProfileImageUrl());
                            startActivity(mIntent);
                        }
                    });
        }
    }
}
