package com.example.refactore2drive.models;

import androidx.annotation.NonNull;

public class Disease {
    String name;
    String nickname;
    long id;

    public Disease() {

    }

    public Disease(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Injury{" +
                "name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", id=" + id +
                '}';
    }
}
