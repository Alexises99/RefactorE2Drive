package com.example.refactore2drive.models;

import androidx.annotation.NonNull;

public class Device {
    String name;
    String address;
    String nickname;
    long id;

    public Device() {}

    public Device (String name, String address, String nickname) {
        this.name = name;
        this.address = address;
        this.nickname = nickname;
    }


    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "Device{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
