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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ApiErrorCode;
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
    private EditText get_input_email;
    private EditText get_input_password;
    private String Temail,Tpassword;

    private SignInButton google_sign_button; //구글 로그인 버튼
    public static FirebaseAuth auth;  //파이어 베이스 인증 객체
    public static GoogleApiClient googleApiClient;  //구글 api 클라이언트 객체\
    private static final int fire_sign_google=100;  //구글 로그인 결과 코드

    //kakao
    private Button btn_custom_login;
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

                //logging in the user
                userLogin(Temail,Tpassword);
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

        //google 회원 정보를 받아옴
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        auth=FirebaseAuth.getInstance();//파이어 베이스 인증 객체 초기화

        //google login button
        google_sign_button=findViewById(R.id.google_sign_button);
        google_sign_button.setOnClickListener(new View.OnClickListener() {//구글 로그인 버튼 클릭
            @Override
            public void onClick(View v) {
                Intent intent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,fire_sign_google);
            }
        });

        //kakao login button
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);
        btn_custom_login=findViewById(R.id.kakao_login);
        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
            }
        });

        //로그아웃 하지 않고 앱을 종료 했을경우, 자동 로그인 기능
        //google
       if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(intent,fire_sign_google);
        }
       //kakao
        if(Session.getCurrentSession().isOpened()==true){
            session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
        }
    }

    //firebase userLogin method
    private void userLogin(String UserEmail, String UserPassword){
        firebaseAuth = FirebaseAuth.getInstance();
        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(UserEmail, UserPassword)
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
                            intent.putExtra("name",account.getDisplayName());
                            intent.putExtra("photoUrl",String.valueOf(account.getPhotoUrl()));
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
                                Profile profile = kakaoAccount.getProfile();

                                if (profile != null) {
                                    Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                    Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                    Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());

                                } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // 동의 요청 후 프로필 정보 획득 가능

                                } else {
                                    // 프로필 획득 불가
                                }
                            }

                            Intent intent=new Intent(getApplicationContext(),SelectMode.class);
                            Profile profile = kakaoAccount.getProfile();
                            intent.putExtra("name",profile.getNickname());
                            intent.putExtra("photoUrl",profile.getProfileImageUrl());
                            startActivity(intent);
                        }
                    });
        }
    }
}
