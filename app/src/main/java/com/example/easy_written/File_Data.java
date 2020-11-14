package com.example.easy_written;

public class File_Data {
    private String mName;
    private String mDate;
    private String mCategory;
    private int mChecked;


    public File_Data(String category,String name,String date){
        this.mCategory=category;
        this.mName=name;
        this.mDate=date;
        this.mChecked = 0;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public int getChecked() { return mChecked; }

    public void setChecked(int checked) { this.mChecked = checked; }

    public String getmCategory() {
        return mCategory;
    }

    public void setmCategory(String mCategory) {
        this.mCategory = mCategory;
    }
}
