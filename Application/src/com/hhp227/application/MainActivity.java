package com.hhp227.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hhp227.application.app.AppController;
import com.hhp227.application.app.Config;
import com.hhp227.application.app.URLs;
import com.hhp227.application.chat.Message;
import com.hhp227.application.fcm.NotificationUtils;
import com.hhp227.application.feed.CircularNetworkImageView;
import com.hhp227.application.fragments.BusFragment;
import com.hhp227.application.fragments.MealFragment;
import com.hhp227.application.fragments.SeatFragment;
import com.hhp227.application.fragments.TabHostLayoutFragment;
import com.hhp227.application.fragments.TimetableFragment;
import com.hhp227.application.fragments.UnivNoticeFragment;
import com.hhp227.application.helper.PreferenceManager;
import com.hhp227.application.ui.navigationdrawer.ActionBarDrawerToggle;
import com.hhp227.application.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.application.user.User;


public class MainActivity extends FragmentActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private PreferenceManager preferenceManager;
    private DrawerLayout drawerLayout;
    private RelativeLayout drawerrelativeLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private CharSequence TitleSection;
    private String name, knu_id, profile_img;
    private TextView txtKnu_id;
    private CircularNetworkImageView imageView;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    String[] menu = {"메인화면", "본관게시판", "시간표", "통학버스시간표", "식단보기", "도서관좌석", "로그아웃"};

    TabHostLayoutFragment fragMain;
    UnivNoticeFragment fragUnivnotice;
    TimetableFragment fragTimetable;
    BusFragment fragBus;
    MealFragment fragMeal;
    SeatFragment fragSeat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayShowHomeEnabled(false); // 제목앞에 아이콘 안보이기
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		txtKnu_id = (TextView) findViewById(R.id.knu_id);
        // 프로필사진 정의,아직 속성없음
        imageView = (CircularNetworkImageView) findViewById(R.id.profileimage);
		
		imageView.setOnClickListener(new View.OnClickListener() {
			 
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(MainActivity.this, MyinfoActivity.class);
                startActivity(intent);
            }
        });
		
        // 세션 메니져
        preferenceManager = new PreferenceManager(getApplicationContext());
        
        User user = AppController.getInstance().getPrefManager().getUser();
        
        if(user == null)
            logoutUser();

        knu_id = user.getKnu_id();
        profile_img = user.getProfile_img().equals(null) ? null : user.getProfile_img();

        txtKnu_id.setText(knu_id);
        imageView.setImageUrl(URLs.URL_USER_PROFILE_IMAGE + profile_img, imageLoader);
        imageView.setDefaultImageResId(R.drawable.profile_img_circle);
        imageView.setErrorImageResId(R.drawable.profile_img_circle);

        fragMain = TabHostLayoutFragment.newInstance();
        fragUnivnotice = UnivNoticeFragment.newInstance();
        fragTimetable = TimetableFragment.newInstance();
        fragBus = BusFragment.newInstance();
        fragSeat = SeatFragment.newInstance();
        fragMeal = new MealFragment().newInstance();

        /**
         * 네비게이션 드로어부분
         */
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerrelativeLayout = (RelativeLayout) findViewById(R.id.left_drawer);
        drawerList = (ListView) findViewById(R.id.list_view_drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menu));

        // 첫화면 액션바에 "메인화면" 문구가 들어감
        TitleSection = "메인화면";
        getActionBar().setTitle(TitleSection);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMain).commit();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch(position) {
                    case 0 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMain).commit();
                        break;
                    case 1 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragUnivnotice).commit();
                        break;
                    case 2 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragTimetable).commit();
                        break;
                    case 3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragBus).commit();
                        break;
                    case 4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMeal).commit();
                        break;
                    case 5:
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragSeat).commit();
                        break;
                    case 6 :
                        logoutUser();
                        break;
                }
                drawerList.setItemChecked(position, true);
                TitleSection = menu[position];
                drawerLayout.closeDrawer(drawerrelativeLayout);
            }
        });

        drawerList.setItemChecked(0, true);

        getTitle();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                drawerArrow, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(TitleSection);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("메뉴목록");
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        /**
         * Broadcast receiver calls in two scenarios
         * 1. fcm registration is completed
         * 2. when new push notification is received
         * */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // fcm registration id is stored in our server's MySQL
                    Log.e(TAG, "FCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Message message = (Message) intent.getSerializableExtra("message");
                    // new push notification is received
                    switch(intent.getIntExtra("type", -1)) {
                        case Config.PUSH_TYPE_CHATROOM :
                            String chatRoomId = intent.getStringExtra("chat_room_id");

                            if(message != null && chatRoomId != null) {
                                Toast.makeText(getApplicationContext(), message.getMessage() + chatRoomId, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case Config.PUSH_TYPE_USER :
                            Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        };
        FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1"); // 1번방의 메시지를 받아옴
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));

        // clearing the notification tray
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

	@Override
    protected void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    // 액션바 메뉴 연결
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item))
            return true;
        // 액션바 버튼의 아이템
        switch(item.getItemId()) {
            case R.id.action_chat :
                // 채팅버튼
                OpenChat();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OpenChat() {
        startActivity(new Intent(this, ChatActivity.class));
    }
    
  	// 유저 로그아웃
  	private void logoutUser() {
        preferenceManager.clear();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
	}
}