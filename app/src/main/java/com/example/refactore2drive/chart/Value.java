package com.example.refactore2drive.chart;


public class Value {
    private long x, y;
    private String nickname, date;
    private long id;

    public Value() {

    }
    public Value (long x, long y, String nickname, String date) {
        this.x = x;
        this.y = y;
        this.nickname = nickname;
        this.date = date;
    }

    public Value (long x, long y, String nickname,String date, long id) {
        this.x = x;
        this.y = y;
        this.nickname = nickname;
        this.id = id;
        this.date = date;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getNickname() {
        return nickname;
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(long y) {
        this.y = y;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Value{" +
                "x=" + x +
                ", y=" + y +
                ", nickname='" + nickname + '\'' +
                ", date='" + date + '\'' +
                ", id=" + id +
                '}';
    }
}
