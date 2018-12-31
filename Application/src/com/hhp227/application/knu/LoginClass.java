package com.hhp227.application.knu;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hhp227.application.LoginActivity;

public class LoginClass
{
  public static boolean isLogged = false;
  private Context context;
  
  public LoginClass(Context paramContext)
  {
    this.context = paramContext;
  }
  
  public boolean login()
  {
	    Object localObject = new PreferenceSetting(this.context, "Login_config");
    if (isLogged) {
      return login(((PreferenceSetting)localObject).getString("Login_ID", ""), ((PreferenceSetting)localObject).getString("Login_PW", ""), true);
    }
    localObject = new Intent(this.context, LoginActivity.class);
    ((Activity)this.context).startActivityForResult((Intent)localObject, 0);
    return false;
  }
  
  public boolean login(String paramString1, String paramString2, boolean paramBoolean)
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(new BasicNameValuePair("j_password", paramString2));
    localArrayList.add(new BasicNameValuePair("j_username", paramString1));
    Object localObject = "";
    try
    {
      String str1 = HTTPSession.getPostUrl(HTTPSession.MainUrl + "/user/subLogin?currentUri=", localArrayList);
      localObject = str1;
      String str2 = str1.split("name=\"j_username\" value=\"")[1].split("\"")[0];
      localObject = str1;
      localArrayList.clear();
      localObject = str1;
      localArrayList.add(new BasicNameValuePair("j_password", paramString2));
      localObject = str1;
      localArrayList.add(new BasicNameValuePair("j_username", str2));
      localObject = str1;
      str1 = HTTPSession.getPostUrl(HTTPSession.MainUrl + "login?currentUri=", localArrayList);
      localObject = str1;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        LoadingDialog.dismissLoading();
        localException.printStackTrace();
      }
    }
    if (((String)localObject).indexOf("location.replace('/main/')") != -1)
    {
      localObject = new PreferenceSetting(this.context, "Login_config");
      ((PreferenceSetting)localObject).putString("Login_ID", paramString1);
      ((PreferenceSetting)localObject).putString("Login_PW", paramString2);
      isLogged = true;
      return true;
    }
    return false;
  }
}
