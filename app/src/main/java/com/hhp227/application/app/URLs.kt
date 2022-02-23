package com.hhp227.application.app

interface URLs {
    companion object {
        private const val BASEURL = "http://hong227.dothome.co.kr/hong227/v1"
        const val URL_REGISTER = "$BASEURL/register"
        const val URL_LOGIN = "$BASEURL/login"
        const val URL_PROFILE_EDIT = "$BASEURL/profile"

        // URL_POSTS는 php파일로 연결할때 주소도 같이 있음
        //const val URL_POSTS = "$BASEURL/php/posts.php?offset="
        const val URL_POST = "$BASEURL/post"
        const val URL_POSTS = "$BASEURL/posts?group_id={GROUP_ID}&offset={OFFSET}"
        const val URL_USER_POSTS = "$BASEURL/posts/?offset={OFFSET}"
        const val URL_POST_LIKE = "$BASEURL/like/{POST_ID}"
        const val URL_POST_IMAGE = "$BASEURL/image"
        const val URL_POST_IMAGE_DELETE = "$BASEURL/images"
        const val URL_POST_IMAGE_PATH = "$BASEURL/php/Images/"
        const val URL_REPLYS = "$BASEURL/replys/{POST_ID}"
        const val URL_REPLY = "$BASEURL/replys/post/{REPLY_ID}" // 댓글 url
        //const val URL_ALBUM = "$BASEURL/album"
        const val URL_ALBUM = "$BASEURL/posts_image?group_id={GROUP_ID}&offset={OFFSET}"
        const val URL_MEMBER = "$BASEURL/users"
        const val URL_CHAT_ROOMS = "$BASEURL/chat_rooms"
        const val URL_CHAT_SEND = "$BASEURL/chat_rooms/{CHATROOM_ID}/message"
        const val URL_CHAT_THREAD = "$BASEURL/chat_rooms/{CHATROOM_ID}?offset={OFFSET}"
        const val URL_USER_FCM = "$BASEURL/user/{USER_ID}"
        const val URL_USER_PROFILE_IMAGE = "$BASEURL/php/ProfileImages/"
        const val URL_USER_PROFILE_IMAGE_UPLOAD = "$BASEURL/profile_img"
        const val URL_GROUP = "$BASEURL/group"
        const val URL_GROUPS = "$BASEURL/groups?offset={OFFSET}"
        const val URL_USER_GROUP = "$BASEURL/user_groups?offset={OFFSET}"
        const val URL_GROUP_JOIN_REQUEST = "$BASEURL/group_join"
        const val URL_LEAVE_GROUP = "$BASEURL/leave_group"
        const val URL_GROUP_IMAGE = "$BASEURL/group_image"
        const val URL_GROUP_IMAGE_PATH = "$BASEURL/php/GroupImages/"
        //const val URL_ALBUM_UPLOAD = "$BASEURL/php/AlbumUpload.php"
    }
}