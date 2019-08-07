package com.ghl.wuhan.tqpicturetest;

import android.graphics.Bitmap;

public class ListData {
	public static final int SEND = 1;//发消息
	public static final int RECEIVE = 2;//收消息
	//接收数据类型
	public static final int USER_WORD = 0;//消息类型（文字）
	public static final int USER_IMG = 1;//消息类型（图片）
	public static final int USER_VOICE = 2;//消息类型（声音）

	private String fromWho;
	private String toUser;
	private String content;
	private int flag;//左右佈局
	private String time;
	private int state;//消息類型
	private Bitmap bitmap;
	private String pitureUrl;//图片地址


	public ListData(String fromWho, String toUser, String content, int flag, String time,int state) {
		this.fromWho = fromWho;
		this.toUser = toUser;
		this.content = content;
		this.flag = flag;
		this.time = time;
        this.state = state;
	}

	//添加了一個判斷消息類型標志
	public ListData(String fromWho, String toUser, int flag, String time, int state, Bitmap bitmap) {
		this.fromWho = fromWho;
		this.toUser = toUser;
		this.flag = flag;
		this.time = time;
		this.state = state;
		this.bitmap = bitmap;
	}

	//添加了判断消息类型标志和图片地址
	public ListData(String fromWho, String toUser, int flag, String time, int state, Bitmap bitmap, String pitureUrl) {
		this.fromWho = fromWho;
		this.toUser = toUser;
		this.flag = flag;
		this.time = time;
		this.state = state;
		this.bitmap = bitmap;
		this.pitureUrl = pitureUrl;
	}

	public ListData(String content, int flag, String time) {
		setContent(content);
		setFlag(flag);
		setTime(time);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFromWho() {
		return fromWho;
	}

	public void setFromWho(String fromWho) {
		this.fromWho = fromWho;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getPitureUrl() {
		return pitureUrl;
	}

	public void setPitureUrl(String pitureUrl) {
		this.pitureUrl = pitureUrl;
	}
}
