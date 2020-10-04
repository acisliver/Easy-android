package com.example.easy_written;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


//참고 사이트 : https://webnautes.tistory.com/1300

public class FileView extends AppCompatActivity  {
    private ArrayList<File_Data> mArrayList;
    private CustomAdapter mAdapter;
    ArrayList<String> filesNameList = new ArrayList<>();
    ArrayList<String> filesDateList = new ArrayList<>();
    ArrayList<File_Data> Variable = new ArrayList<>();
    File[] files;
    //고대은
    private int modify_flag;
    private int checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        //고대은
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        modify_flag = 0;

        final CheckBox check_all = findViewById(R.id.check_all);

        mArrayList = new ArrayList<>();

        mAdapter = new CustomAdapter(mArrayList);
        mRecyclerView.setAdapter(mAdapter);


        //actionbar
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 찾기 버튼 만들기

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLinearLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            //파일 클릭 시
            @Override
            public void onClick(View view, int position) {//recycler list 하나씩 위치에 따라 다른 화면 띄우기, 지금은 파일 하나만 했음
                if (modify_flag == 0) {
                    Intent intent = new Intent(getBaseContext(), Image_MainAdpater.class);

                    //파일값 넘기기
                    intent.putExtra("filesNameList", filesNameList.get(position));
                    intent.putExtra("filesDateList", filesDateList.get(position));
                    intent.putExtra("paths", files[position].getPath());

                    startActivity(intent);
                }
                //수정 모드(하단 바 있을 시)
                else {
                    checked = Variable.get(position).getChecked();
                    if (checked == 0)
                        Variable.get(position).setChecked(1);
                    else
                        Variable.get(position).setChecked(0);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                modify_flag = 1;
                handleVisible(modify_flag);
                mAdapter.notifyDataSetChanged();
            }
        }));


        //고대은
        //bottomnavigationview의 아이콘을 선택 했을때 기능 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    //삭제
                    case R.id.delete_tab: {
                        Toast.makeText(getApplicationContext(), "삭제", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    //공유
                    case R.id.share_tab: {
                        Toast.makeText(getApplicationContext(), "공유", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    //새폴더
                    case R.id.folder_tab: {
                        Toast.makeText(getApplicationContext(), "새폴더", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    //하단 바 내리기
                    case R.id.close_tab: {
                        modify_flag = 0;
                        handleVisible(modify_flag);
                        mAdapter.notifyDataSetChanged();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        //전체선택
        check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checked;
                int count = mAdapter.getItemCount();
                if (check_all.isChecked()==true) checked=1;
                else checked=0;
                for (int pos=0;pos<count;pos++){
                    Variable.get(pos).setChecked(checked);
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "EASYWRITTEN" + "/";
        File directory = new File(path);
        files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            String[] result = name.split("#");
            filesNameList.add(result[0]);
            filesDateList.add(result[1]);
            Variable.add(new File_Data("날짜:" + filesDateList.get(i), "파일이름 : " + filesNameList.get(i)));
            mArrayList.add(Variable.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

    //파일 검색 icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.find_file_menu,menu);
        MenuItem item=menu.findItem(R.id.search_file_icon);
        SearchView searchView= (SearchView) item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        return super.onCreateOptionsMenu(menu);
    }



    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private FileView.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final FileView.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    //고대은
    //하단 바 표시 여부 modify_flag=0 Gone, modify_flag=1 Visible
    public void handleBottomNavVisible(int modify_flag) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (modify_flag == 1) bottomNavigationView.setVisibility(View.VISIBLE);
        else bottomNavigationView.setVisibility(View.GONE);
    }

    //하단 바 표시 여부 어댑터에 전달
    public void handleCheckBoxVisible(int n) {
        mAdapter.checkBoxVisibility(n);
    }

    //전체선택 표시 여부
    public void handleCheckedAllVisible(int modify_flag) {
        CheckBox check_all = findViewById(R.id.check_all);
        if(modify_flag==0) check_all.setVisibility(View.GONE);
        else check_all.setVisibility(View.VISIBLE);
    }

    //표시 여부 함수 모음
    public void handleVisible(int modify_flag) {
        handleCheckBoxVisible(modify_flag);
        handleCheckedAllVisible(modify_flag);
        handleBottomNavVisible(modify_flag);
    }
}
