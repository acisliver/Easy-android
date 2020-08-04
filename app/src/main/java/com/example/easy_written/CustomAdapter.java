package com.example.easy_written;


import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<File_Data> mList;
    private int bar_flag=0; //하단바 표시여부에 따라 체크박스 표시 flag

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView date;
        protected CheckBox checkBox;


        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.R_date);
            this.date = (TextView) view.findViewById(R.id.R_name);
            this.checkBox=(CheckBox) view.findViewById(R.id.file_checkbox);
        }
    }


    public CustomAdapter(ArrayList<File_Data> list) {
        this.mList = list;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_list, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        viewholder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        viewholder.date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

        viewholder.name.setGravity(Gravity.CENTER);
        viewholder.date.setGravity(Gravity.CENTER);

        viewholder.name.setText(mList.get(position).getName());
        viewholder.date.setText(mList.get(position).getDate());

        //flag에 따라 checkBox 표시
        if (bar_flag==0)
            viewholder.checkBox.setVisibility(View.GONE);
        else
            viewholder.checkBox.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
    //bar_flag 외부 제어 함수
    public void checkBoxVisibility(int n){ bar_flag = n; }
}
