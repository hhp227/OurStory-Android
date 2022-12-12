package com.hhp227.application.util

interface URLs {
    companion object {
        const val BASE_URL = "http://hong227.dothome.co.kr/hong227/v1"
        const val URL_REGISTER = "$BASE_URL/register"
        const val URL_LOGIN = "$BASE_URL/login"
        const val URL_PROFILE_EDIT = "$BASE_URL/profile"
        const val URL_POST = "$BASE_URL/post"

        // URL_POSTS는 php파일로 연결할때 주소도 같이 있음
        //const val URL_POSTS = "$BASEURL/php/posts.php?group_id={GROUP_ID}&offset={OFFSET}"
        const val URL_POSTS = "$BASE_URL/posts?group_id={GROUP_ID}&offset={OFFSET}"
        const val URL_USER_POSTS = "$BASE_URL/posts/?offset={OFFSET}"
        const val URL_POST_LIKE = "$BASE_URL/like/{POST_ID}"
        const val URL_POST_REPORT = "$BASE_URL/report/{POST_ID}"
        const val URL_POST_IMAGE = "$BASE_URL/image"
        const val URL_POST_IMAGE_DELETE = "$BASE_URL/images"
        const val URL_POST_IMAGE_PATH = "$BASE_URL/php/Images/"
        const val URL_REPLYS = "$BASE_URL/replys/{POST_ID}"
        const val URL_REPLY = "$BASE_URL/replys/post/{REPLY_ID}" // 댓글 url
        //const val URL_ALBUM = "$BASEURL/album"
        const val URL_ALBUM = "$BASE_URL/posts_image?group_id={GROUP_ID}&offset={OFFSET}"
        const val URL_MEMBER = "$BASE_URL/users"
        const val URL_CHAT_ROOMS = "$BASE_URL/chat_rooms"
        const val URL_CHAT_SEND = "$BASE_URL/chat_rooms/{CHATROOM_ID}/message"
        const val URL_CHAT_THREAD = "$BASE_URL/chat_rooms/{CHATROOM_ID}?offset={OFFSET}"
        const val URL_USER_FCM = "$BASE_URL/user/{USER_ID}"
        const val URL_USER_PROFILE_IMAGE = "$BASE_URL/php/ProfileImages/"
        const val URL_USER_PROFILE_IMAGE_UPLOAD = "$BASE_URL/profile_img"
        const val URL_GROUP = "$BASE_URL/group"
        const val URL_GROUPS = "$BASE_URL/groups?offset={OFFSET}"
        const val URL_USER_GROUP = "$BASE_URL/user_groups?offset={OFFSET}"
        const val URL_GROUP_JOIN_REQUEST = "$BASE_URL/group_join"
        const val URL_LEAVE_GROUP = "$BASE_URL/leave_group"
        const val URL_GROUP_IMAGE = "$BASE_URL/group_image"
        const val URL_GROUP_IMAGE_PATH = "$BASE_URL/php/GroupImages/"
        const val URL_USER_FRIEND = "$BASE_URL/friend/{USER_ID}"
        const val URL_USER_FRIENDS = "$BASE_URL/friends/?offset={OFFSET}"
        const val URL_TOGGLE_FRIEND = "$BASE_URL/toggle_friend/{USER_ID}"
        //const val URL_ALBUM_UPLOAD = "$BASEURL/php/AlbumUpload.php"
    }
}