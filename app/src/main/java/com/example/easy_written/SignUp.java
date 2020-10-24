package com.example.easy_written;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText mSignUpEmail, mSignUpPassword, mSignUpPasswordChecked, mSignUpName;
    private Button mSignUpButton;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //파이어베이스 접근 설정
        mFirebaseAuth =  FirebaseAuth.getInstance();

        mSignUpEmail = findViewById(R.id.signUpEmail);
        mSignUpPassword = findViewById(R.id.signUpPassword);
        mSignUpPasswordChecked = findViewById(R.id.signUpPasswordChecked);
        mSignUpName = findViewById(R.id.signUpName);
        mSignUpButton = findViewById(R.id.signUpButton);

        //가입버튼 클릭리스너   -->  firebase에 데이터를 저장한다.
        mSignUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //가입 정보 가져오기
                final String mEmail = mSignUpEmail.getText().toString().trim();
                final String mPwd = mSignUpPassword.getText().toString().trim();
                final String mPwdcheck = mSignUpPasswordChecked.getText().toString().trim();


                if(mPwd.equals(mPwdcheck)) {
                    Log.d(TAG, "등록 버튼 " + mEmail + " , " + mPwd);
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("가입중입니다...");
                    mDialog.show();

                    //파이어베이스에 신규계정 등록하기
                    mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPwd)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //가입 성공시
                            if (task.isSuccessful()) {
                                mDialog.dismiss();

                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                String UserEmail = user.getEmail();
                                String UserId = user.getUid();
                                String UserName = mSignUpName.getText().toString().trim();

                                //해쉬맵 테이블을 파이어베이스 데이터베이스에 저장
                                HashMap<Object,String> hashMap = new HashMap<>();
                                hashMap.put("uid",UserId);
                                hashMap.put("email",UserEmail);
                                hashMap.put("name",UserName);
                                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference mReference = mDatabase.getReference("Users");
                                mReference.child(UserId).setValue(hashMap);

                                //로그인 완료시 화면전환
                                Intent mIntent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(mIntent);
                                finish();
                                Toast.makeText(SignUp.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();

                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                                return;  //해당 메소드 진행을 멈추고 빠져나감.
                            }
                        }
                    });

                    //비밀번호 오류시
                }else{
                    Toast.makeText(SignUp.this, "비밀번호가 틀렸습니다. 다시 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }
}
