package com.hhp227.application.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hhp227.application.user.User;

public class PreferenceManager {
    // LogCat tag
    private static String TAG = "세션메니져";

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "ApplicationLogin";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_STUDENT_NUMBER = "student_number";
    private static final String KEY_NAME = "name";
    private static final String KEY_KNU_ID = "knu_id";
    private static final String KEY_APIKEY = "api_key";
    private static final String KEY_PROFILE_IMAGE = "profile_img";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public PreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void storeUser(User user) {
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_STUDENT_NUMBER, user.getStudent_number());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_KNU_ID, user.getKnu_id());
        editor.putString(KEY_APIKEY, user.getApi_key());
        editor.putString(KEY_PROFILE_IMAGE, user.getProfile_img());
        editor.putString(KEY_CREATED_AT, user.getCreated_at());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.commit();

        Log.e(TAG, "사용자 Session 저장. " + user.getName() + ", " + user.getKnu_id());
    }

    public User getUser() {
        if(pref.getInt(KEY_USER_ID, 0) != 0) {
            int id;
            String student_number, name, knu_id, api_key, profile_img, created_at, password;
            id = pref.getInt(KEY_USER_ID, 0);
            student_number = pref.getString(KEY_STUDENT_NUMBER, null);
            name = pref.getString(KEY_NAME, null);
            knu_id = pref.getString(KEY_KNU_ID, null);
            api_key = pref.getString(KEY_APIKEY, null);
            profile_img = pref.getString(KEY_PROFILE_IMAGE, null);
            created_at = pref.getString(KEY_CREATED_AT, null);
            password = pref.getString(KEY_PASSWORD, null);

            User user = new User(id, student_number, name, knu_id, api_key, profile_img, created_at, password);
            return user;
        }
        return null;
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if(oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
