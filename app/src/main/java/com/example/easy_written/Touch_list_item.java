package com.example.easy_written;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Touch_list_item extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_list);

        LinearLayout list_item=(LinearLayout)findViewById(R.id.Linear_list_item);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.list_item:
                        Intent intent=new Intent(Touch_list_item.this,KeywordTime.class);
                        startActivity(intent);
                }
            }

        };

    }
}
