package com.example.easy_written;


import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> implements Filterable {

    private ArrayList<File_Data> mList;
    private int modify_flag=0; //하단바 표시여부에 따라 체크박스 표시 flag
    private ArrayList<File_Data>  mListAll;

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

    public CustomAdapter(){
        mList=new ArrayList<>();
        mListAll=new ArrayList<>();
    }

    public CustomAdapter(ArrayList<File_Data> list) {
        this.mList = list;
        this.mListAll=list;
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

        //항목 클릭 시 체크, 배경색 바꿈
        if (mList.get(position).getChecked()==1) {
            viewholder.checkBox.setChecked(true);
            setItemBackground(viewholder, "#959698");
        }
        else {
            viewholder.checkBox.setChecked(false);
            setItemBackground(viewholder, "#ffffff");
        }

        //하단바 표시 flag에 따라 checkBox 표시
        if (modify_flag==0) {
            viewholder.checkBox.setVisibility(View.GONE);
            setItemBackground(viewholder, "#ffffff");
        }
        else {
            viewholder.checkBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
    //modify_flag 외부 제어 함수
    public void checkBoxVisibility(int modify_flag){ this.modify_flag = modify_flag; }

    //클릭 & 해제 배경색 전환
    public void setItemBackground(@NonNull CustomViewHolder viewholder, String colorString){
        viewholder.checkBox.setBackgroundColor(Color.parseColor(colorString));
        viewholder.name.setBackgroundColor(Color.parseColor(colorString));
        viewholder.date.setBackgroundColor(Color.parseColor(colorString));
    }

    //파일 검색
    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter=new Filter() {
        //background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<File_Data> filteredList=new ArrayList<>();
            if(constraint.toString().isEmpty()||constraint.length()==0){
                Log.e("mListAll",mListAll.toString());
                filteredList.addAll(mListAll);
            }else{
                String filter_pattern=constraint.toString().toLowerCase().trim();
                for(File_Data item:mListAll){
                    if(item.getDate().toLowerCase().contains(filter_pattern)){  //현재 파일의 Name과 Date의 값이 서로 바뀌어져 있는 상태 이기 대문에 getDate로함. 나중에 수정
                        filteredList.add(item);
                    }
                }
            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=filteredList;
            return filterResults;
        }

        //ui thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
