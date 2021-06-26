package com.hhp227.application.dto;

public class ReplyItem {
    private int id, userId;
    private String name, profileImage, timeStamp, reply;

    public ReplyItem() {
    }

    public ReplyItem(int id, int userId, String name, String profileImage, String timeStamp, String reply) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
        this.timeStamp = timeStamp;
        this.reply = reply;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
