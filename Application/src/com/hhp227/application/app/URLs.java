package com.hhp227.application.app;

public interface URLs {
    String BASEURL = "http://knu.dothome.co.kr/knu/v1";
    String URL_REGISTER = BASEURL + "/register";
    String URL_LOGIN = BASEURL + "/login";
    String URL_PROFILE_EDIT = BASEURL + "/profile";
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

    // 학교URL
    String URL_KNU = "http://www.knu.ac.kr";
    String URL_SCHEDULE = URL_KNU + "/wbbs/wbbs/user/yearSchedule/xmlResponse.action?schedule.search_date={YEAR-MONTH}";
    String URL_SHUTTLE = URL_KNU + "/wbbs/wbbs/contents/index.action?menu_url=intro/map03_02&menu_idx=27";
    String URL_KNU_NOTICE = URL_KNU + "/wbbs/wbbs/bbs/btin/list.action?bbs_cde=1&btin.page={PAGE}&popupDeco=false&btin.search_type=&btin.search_text=&menu_idx=67";
    String URL_KNU_SC_DORM_MEAL = "http://dorm.knu.ac.kr/scdorm/_new_ver/";
    String URL_KNU_DORM_MEAL = "http://dorm.knu.ac.kr/xml/food.php?get_mode={ID}";
    String URL_KNULIBRARY_SEAT = "http://seat.knu.ac.kr/smufu-api/pc/{ID}/rooms-at-seat";
    // 외부 URL
    String URL_INTER_CITY_SHUTTLE = "http://www.gobus.co.kr/north/inquiry/inquiry_see.asp?code=300&explice=경북대상주";
}
