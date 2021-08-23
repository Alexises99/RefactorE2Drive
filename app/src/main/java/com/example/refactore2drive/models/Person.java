package com.example.refactore2drive.models;

import androidx.annotation.NonNull;

public class Person {
    String name;
    String nickname;
    int age;
    String genre;
    float height;
    float weight; //NULL
    long id;

    public Person() {

    }

    public Person(@NonNull String name, String nickname, int age, String genre, float height) {
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.genre = genre;
        this.height = height;
    }

    public Person(String name, String nickname,int age, String genre, float height, long id) {
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.genre = genre;
        this.height = height;
        this.id = id;
    }

    public Person(String name, String nickname,int age, String genre, float height, float weight) {
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.genre = genre;
        this.height = height;
        this.weight = weight;
    }

    public Person(String name, String nickname,int age, String genre, float height, float weight, long id) {
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.genre = genre;
        this.height = height;
        this.weight = weight;
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public int getAge() {
        return age;
    }

    public String getGenre() {
        return genre;
    }

    public String getNickname() {
        return nickname;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", age=" + age +
                ", genre='" + genre + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", id=" + id +
                '}';
    }
}
