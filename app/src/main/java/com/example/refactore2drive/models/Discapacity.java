package com.example.refactore2drive.models;

public class Discapacity {
    String type;
    String nickname;
    int degree;
    long id;

    public Discapacity() {

    }

    public Discapacity(String type, String nickname, int degree) {
        this.type = type;
        this.nickname = nickname;
        this.degree = degree;
    }

    public Discapacity( String type, String nickname, int degree, long id) {
        this.type = type;
        this.nickname = nickname;
        this.degree = degree;
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }


    public String getType() {
        return type;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Discapacity{" +
                "type='" + type + '\'' +
                ", nickname='" + nickname + '\'' +
                ", degree=" + degree +
                ", id=" + id +
                '}';
    }
}