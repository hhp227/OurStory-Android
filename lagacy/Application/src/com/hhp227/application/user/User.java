package com.hhp227.application.user;

import java.io.Serializable;

public class User implements Serializable {
    int id;
    String name, email, api_key, profile_img, created_at;

    public User() {
    }

    public User(int id, String name, String email, String api_key, String profile_img, String created_at) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.api_key = api_key;
        this.profile_img = profile_img;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
