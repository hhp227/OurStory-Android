package com.hhp227.application.knu;

import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public class HTTPSession {
	public static String MainUrl = "http://smt.knu.ac.kr:8080/";
	private static HttpClient loginHttpClient;

	private static void createHttpClient() {
		if (loginHttpClient == null) {
			loginHttpClient = new DefaultHttpClient();
		}
	}

	public static String getPostUrl(String paramString)
		    throws Exception
		  {
		    createHttpClient();
			return paramString;
		  }
	
	public static String getPostUrl(String string, ArrayList localArrayList) {
		// TODO Auto-generated method stub
		return null;
	}
	
}