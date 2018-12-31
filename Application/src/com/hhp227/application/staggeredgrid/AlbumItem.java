package com.hhp227.application.staggeredgrid;

public class AlbumItem {
	private int id;
	private String name, image, timeStamp;

	public AlbumItem() {
	}

	public AlbumItem(int id, String name, String image, String timeStamp) {
		super();
		this.id = id;
		this.name = name;
		this.image = image;
		this.timeStamp = timeStamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImge() {
		return image;
	}

	public void setImge(String image) {
		this.image = image;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
}
