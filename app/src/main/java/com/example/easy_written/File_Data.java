package com.example.easy_written;

public class File_Data {
    private String name;
    private String date;
    private int checked_flag;

    public File_Data(String name,String date){
        this.name=name;
        this.date=date;
        this.checked_flag=0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getChecked_flag() { return checked_flag; }

    public void setChecked_flag(int checked_flag) { this.checked_flag = checked_flag; }
}
