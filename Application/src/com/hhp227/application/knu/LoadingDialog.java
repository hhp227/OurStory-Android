package com.hhp227.application.knu;

import android.app.ProgressDialog;
import android.content.Context;

public class LoadingDialog
{
  private static ProgressDialog loadingDialog;
  
  public static void dismissLoading()
  {
    if (loadingDialog == null) {
      return;
    }
    loadingDialog.dismiss();
    loadingDialog = null;
  }
  
  public static void showLoading(Context paramContext)
  {
    if (loadingDialog != null) {
      dismissLoading();
    }
    loadingDialog = ProgressDialog.show(paramContext, "로딩중", "데이터 로딩중입니다..", true, false);
    loadingDialog.setCanceledOnTouchOutside(false);
  }
  
  public static void showLoading(Context paramContext, String paramString1, String paramString2)
  {
    if (loadingDialog != null) {
      dismissLoading();
    }
    loadingDialog = ProgressDialog.show(paramContext, paramString1, paramString2, true, true);
    loadingDialog.setCanceledOnTouchOutside(false);
  }
  
  public static void showLoading(Context paramContext, String paramString1, String paramString2, boolean paramBoolean)
  {
    if (loadingDialog != null) {
      dismissLoading();
    }
    loadingDialog = ProgressDialog.show(paramContext, paramString1, paramString2, true, paramBoolean);
    loadingDialog.setCanceledOnTouchOutside(false);
  }
}
