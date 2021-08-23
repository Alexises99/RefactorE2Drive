package com.example.refactore2drive.models;

public class SessionModel {
    String name;
    String tIni;
    String tFin;
    String comments;
    String username;
    long id;

    public SessionModel() {

    }

    public SessionModel(String name, String tIni, String tFin, String comments, String username) {
        this.name = name;
        this.tFin = tFin;
        this.tIni = tIni;
        this.comments = comments;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public String gettFin() {
        return tFin;
    }

    public String gettIni() {
        return tIni;
    }

    public String getUsername() {
        return username;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void settFin(String tFin) {
        this.tFin = tFin;
    }

    public void settIni(String tIni) {
        this.tIni = tIni;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "SessionModel{" +
                "name='" + name + '\'' +
                ", tIni='" + tIni + '\'' +
                ", tFin='" + tFin + '\'' +
                ", comments='" + comments + '\'' +
                ", username='" + username + '\'' +
                ", id=" + id +
                '}';
    }
}
