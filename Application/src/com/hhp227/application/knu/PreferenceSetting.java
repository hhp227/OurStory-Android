package com.hhp227.application.knu;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceSetting {
	private SharedPreferences m_Setting;

	public PreferenceSetting(Context paramContext, String paramString) {
		this.m_Setting = paramContext.getSharedPreferences(paramString, 0);
	}

	public boolean getBoolean(String paramString, boolean paramBoolean) {
		return this.m_Setting.getBoolean(paramString, paramBoolean);
	}

	public int getInt(String paramString, int paramInt) {
		return this.m_Setting.getInt(paramString, paramInt);
	}

	public String getString(String paramString1, String paramString2) {
		return this.m_Setting.getString(paramString1, paramString2);
	}

	public void putBoolean(String paramString, boolean paramBoolean) {
		SharedPreferences.Editor localEditor = this.m_Setting.edit();
		localEditor.putBoolean(paramString, paramBoolean);
		localEditor.commit();
	}

	public void putInt(String paramString, int paramInt) {
		SharedPreferences.Editor localEditor = this.m_Setting.edit();
		localEditor.putInt(paramString, paramInt);
		localEditor.commit();
	}

	public void putString(String paramString1, String paramString2) {
		SharedPreferences.Editor localEditor = this.m_Setting.edit();
		localEditor.putString(paramString1, paramString2);
		localEditor.commit();
	}
}
