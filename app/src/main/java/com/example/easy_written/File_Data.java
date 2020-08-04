package com.example.easy_written;

public class File_Data {
    private String name;
    private String date;
    private int checked;

    public File_Data(String name,String date){
        this.name=name;
        this.date=date;
        this.checked=0;
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

    public int getChecked() { return checked; }

    public void setChecked(int checked) { this.checked = checked; }
}
