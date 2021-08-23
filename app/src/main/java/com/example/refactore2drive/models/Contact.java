package com.example.refactore2drive.models;

public class Contact {
    String name;
    int number;
    String nickname;
    long id;

    public Contact() {

    }

    public Contact(String name, int number, String nickname) {
        this.name = name;
        this.number = number;
        this.nickname = nickname;
    }

    public Contact(String name, int number, String nickname, long id) {
        this.name = name;
        this.number = number;
        this.nickname = nickname;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public int getNumber() {
        return number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", number=" + number +
                ", nickname='" + nickname + '\'' +
                ", id=" + id +
                '}';
    }
}
