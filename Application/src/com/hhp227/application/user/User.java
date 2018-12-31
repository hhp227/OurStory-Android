package com.hhp227.application.user;

import java.io.Serializable;

public class User implements Serializable {
    int id;
    String student_number, name, knu_id, api_key, profile_img, created_at, password;

    public User() {
    }

    public User(int id, String student_number, String name, String knu_id, String api_key, String profile_img, String created_at, String password) {
        this.id = id;
        this.student_number = student_number;
        this.name = name;
        this.knu_id = knu_id;
        this.api_key = api_key;
        this.profile_img = profile_img;
        this.created_at = created_at;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudent_number() {
        return student_number;
    }

    public void setStudent_number(String student_number) {
        this.student_number = student_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKnu_id() {
        return knu_id;
    }

    public void setKnu_id(String knu_id) {
        this.knu_id = knu_id;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
