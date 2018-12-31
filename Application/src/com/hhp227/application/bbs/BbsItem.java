package com.hhp227.application.bbs;

public class BbsItem {
	public String mType, mTitle, mUrl, mWriter, mDate;
    
    public BbsItem(){
    }
    
    //데이터를 받는 클래스 메서드
    public BbsItem(String mType, String mTitle, String mUrl, String mWriter, String mDate)  {
        this.mType = mType;
        this.mTitle = mTitle;
        this.mUrl = mUrl;
        this.mWriter = mWriter;
        this.mDate = mDate;
    }
    
	public String getType() {
		return mType;
	}

	public void setType(String mType) {
		this.mType = mType;
	}
	
	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	
	public void setWriter(String mWriter) {
		this.mWriter = mWriter;
	}
	
	public String getWriter() {
		return mWriter;
	}
	
	public void setDate(String mDate) {
		this.mDate = mDate;
	}
	
	public String getDate() {
		return mDate;
	}
}
