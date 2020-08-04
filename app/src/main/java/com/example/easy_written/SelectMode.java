package com.example.easy_written;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mode);

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

        Button mode2=findViewById(R.id.mode2);
        mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectMode.this,push_file.class);
                startActivity(intent);
            }
        });
    }
}
