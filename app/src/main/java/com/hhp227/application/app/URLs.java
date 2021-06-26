package com.hhp227.application.app;

public interface URLs {
    String BASEURL = "http://hong227.dothome.co.kr/hong227/v1";
    String URL_REGISTER = BASEURL + "/register";
    String URL_LOGIN = BASEURL + "/login";
    String URL_PROFILE_EDIT = BASEURL + "/profile";
    // URL_POSTS는 php파일로 연결할때 주소도 같이 있음
    //String URL_POSTS = BASEURL + "/php/posts.php?offset=";
    String URL_POST = BASEURL + "/post";
    String URL_POSTS = BASEURL + "/posts?offset={OFFSET}";
    String URL_POST_LIKE = BASEURL + "/like/{POST_ID}";
    String URL_POST_IMAGE = BASEURL + "/image";
    String URL_POST_IMAGE_DELETE = BASEURL + "/images";
    String URL_POST_IMAGE_PATH = BASEURL + "/php/Images/";
    String URL_REPLYS = BASEURL + "/replys/{POST_ID}";
    String URL_REPLY = BASEURL + "/replys/post/{REPLY_ID}"; // 댓글 url
    String URL_ALBUM = BASEURL + "/album";
    String URL_MEMBER = BASEURL + "/users";
    String URL_CHAT_SEND = BASEURL + "/chat_rooms/{CHATROOM_ID}/message";
    String URL_CHAT_THREAD = BASEURL + "/chat_rooms/{CHATROOM_ID}?offset=";
    String URL_USER_FCM = BASEURL + "/user/{USER_ID}";
    String URL_USER_PROFILE_IMAGE = BASEURL + "/php/ProfileImages/";
    String URL_GROUP = BASEURL + "/group";
    String URL_GROUPS = BASEURL + "/groups?offset={OFFSET}";
    String URL_USER_GROUP = BASEURL + "/user_groups";
    String URL_GROUP_JOIN_REQUEST = BASEURL + "/group_join";
    String URL_LEAVE_GROUP = BASEURL + "/leave_group";
    String URL_GROUP_IMAGE = BASEURL + "/group_image";
    String URL_GROUP_IMAGE_PATH = BASEURL + "/php/GroupImages/";
}
