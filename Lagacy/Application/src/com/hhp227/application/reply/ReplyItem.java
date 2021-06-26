package com.hhp227.application.reply;

public class ReplyItem {
    private int id, user_id;
    private String name, profile_img, timeStamp, reply;

    public ReplyItem() {
    }

    public ReplyItem(int id, int user_id, String name, String profile_img, String timeStamp, String reply) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.profile_img = profile_img;
        this.timeStamp = timeStamp;
        this.reply = reply;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImg() {
        return profile_img;
    }

    public void setProfileImg(String profile_img) {
        this.profile_img = profile_img;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
