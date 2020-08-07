package com.example.easy_written;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText get_input_email;
    private EditText get_input_password;
    private String Temail,Tpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Edittext
        get_input_email=findViewById(R.id.inputEmail);
        get_input_password=findViewById(R.id.inputPassword);


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

        Button signup_button=findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);
            }
        });


    }
}
