package com.hhp227.application.feed;

public class FeedItem {
    private int id, user_id, likeCount;
    private String name, status, image, feed, profilePic, timeStamp, replyCount;

    public FeedItem() {
    }

    public FeedItem(int id, int user_id, String name, String image, String feed, String status,
                    String profilePic, String timeStamp, String replyCount, int likeCount) {
        super();
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.image = image;
        this.feed = feed;
        this.status = status;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
