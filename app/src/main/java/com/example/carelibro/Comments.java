package com.example.carelibro;

public class Comments {
    public String comment, date, fullName, time, uid;

    public Comments(){

    }

    public Comments(String comment, String date, String fullName, String time, String uid) {
        this.comment = comment;
        this.date = date;
        this.fullName = fullName;
        this.time = time;
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
