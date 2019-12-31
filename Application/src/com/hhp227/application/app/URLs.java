package com.hhp227.application.app;

public interface URLs {
    String BASEURL = "http://hong227.dothome.co.kr/hong227/v1";
    String URL_REGISTER = BASEURL + "/register";
    String URL_LOGIN = BASEURL + "/login";
    String URL_PROFILE_EDIT = BASEURL + "/profile";
    // URL_FEEDS는 php파일로 연결할때 주소도 같이 있음
    //String URL_FEEDS = BASEURL + "/php/feeds.php?offset=";
    String URL_FEEDS = BASEURL + "/feeds";
    String URL_FEED = BASEURL + "/feeds/{FEED_ID}"; // 피드 url
    String URL_FEED_LIKE = BASEURL + "/like/{FEED_ID}";
    String URL_FEED_IMAGE_UPLOAD = BASEURL + "/feed_image";
    String URL_FEED_IMAGE = BASEURL + "/php/Feed_Images/";
    String URL_REPLYS = BASEURL + "/replys/{FEED_ID}";
    String URL_REPLY = BASEURL + "/replys/feed/{REPLY_ID}"; // 댓글 url
    String URL_ALBUM = BASEURL + "/album";
    String URL_MEMBER = BASEURL + "/users"; // 회원 url
    String URL_CHAT_SEND = BASEURL + "/chat_rooms/{CHATROOM_ID}/message";
    String URL_CHAT_THREAD = BASEURL + "/chat_rooms/{CHATROOM_ID}?offset=";
    String URL_USER_FCM = BASEURL + "/user/{USER_ID}";
    String URL_USER_PROFILE_IMAGE = BASEURL + "/php/ProfileImages/";
}
