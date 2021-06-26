package com.hhp227.application.dto;

public class MemberItem {///////////////////////
    private String id, name, email, profile_img, timeStamp;

    public MemberItem() {
    }

    public MemberItem(String id, String name, String email, String profile_img, String created_at) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profile_img = profile_img;
        this.timeStamp = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getProfile_img() {
        return profile_img;
    }

    public void setProfile_img(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
