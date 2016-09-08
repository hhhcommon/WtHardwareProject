package com.test;

import android.app.Activity;
import android.util.Log;
import java.util.Stack;

/**
 * type类型：im,music,mine
 * Activity管理器代码，主要是建立一个栈，把每个已打开的Activity压入栈中。退出的时候在依次取出来。
 * 介绍：在每一个activity中的onCreate方法里调用压入方法把当前activity压入管理栈中。
 * 比如在MainActivity中：MyActivityManager mam = MyActivityManager.getInstance();
 *  mam.pushOneActivity(MainActivity.this);就把当前activity压入了栈中。
 * 在退出所有Activity的地方调用退出所有Activity的方法即可退出所有activity
 * @author 辛龙
 *2016年3月7日
 */
public class MyActivityManager {
	private static MyActivityManager instance;
	private Stack<Activity> imActivityStack;//队将部分activity栈
	private Stack<Activity> musicActivityStack;//音乐部分activity栈
	private Stack<Activity> mineActivityStack;//剩余部分activity栈
	private MyActivityManager() {
	}

	/**
	 * 单例模式
	 */
	public static MyActivityManager getInstance() {
		if (instance == null) {
			instance = new MyActivityManager();
		}
		return instance;
	}

	/**
	 * 把一个activity压入栈中
	 * @param activity
	 * @param type
	 */
	public void pushOneActivity(Activity activity,String type) {

		if(type!=null&&!type.equals("")){
			if(type.equals("im")){
				if (imActivityStack == null) {
					imActivityStack = new Stack<>();
				}
				imActivityStack.add(activity);
				Log.e("MyActivityManager ", "imActivityStack::size = " + imActivityStack.size());
			}else if(type.equals("music")){
				if (musicActivityStack == null) {
					musicActivityStack = new Stack<>();
				}
				musicActivityStack.add(activity);
				Log.e("MyActivityManager ", "musicActivityStack::size = " + musicActivityStack.size());
			}else if(type.equals("mine")){
				if (mineActivityStack == null) {
					mineActivityStack = new Stack<>();
				}
				mineActivityStack.add(activity);
				Log.e("MyActivityManager ", "mineActivityStack::size = " + mineActivityStack.size());
			}
		}else{
			Log.e("MyActivityManager", "缺少type类型");
		}
	}

	/**
	 * 移除一个activity
	 * @param activity
	 * @param type
	 */
	public void popOneActivity(Activity activity,String type) {
		if(type!=null&&!type.equals("")){
			if(type.equals("im")){
				if (imActivityStack != null && imActivityStack.size() > 0) {
					if (activity != null) {
						activity.finish();
						imActivityStack.remove(activity);
						activity = null;
					}
				}
			}else if(type.equals("music")){
				if (musicActivityStack != null && musicActivityStack.size() > 0) {
					if (activity != null) {
						activity.finish();
						musicActivityStack.remove(activity);
						activity = null;
					}
				}
			}else if(type.equals("mine")){
				if (mineActivityStack != null && mineActivityStack.size() > 0) {
					if (activity != null) {
						activity.finish();
						mineActivityStack.remove(activity);
						activity = null;
					}
				}
			}
		}else{
			Log.e("MyActivityManager", "缺少type类型");
		}
	}

	/**
	 * 退出所有activity
	 */
	public void finishAllActivity() {
		if (imActivityStack != null) {
			while (imActivityStack.size() > 0) {
				Activity activity = getImLastActivity();
				if (activity == null) break;
				popOneActivity(activity,"im");
			}
		}
		if (musicActivityStack != null) {
			while (musicActivityStack.size() > 0) {
				Activity activity = getMusicLastActivity();
				if (activity == null) break;
				popOneActivity(activity,"music");
			}
		}
		if (mineActivityStack != null) {
			while (mineActivityStack.size() > 0) {
				Activity activity = getMineLastActivity();
				if (activity == null) break;
				popOneActivity(activity,"mine");
			}
		}
	}

	//获取栈顶的imActivity，先进后出原则
	public Activity getImLastActivity() {
		return imActivityStack.lastElement();
	}

	//获取栈顶的musicActivity，先进后出原则
	public Activity getMusicLastActivity() {
		return musicActivityStack.lastElement();
	}

	//获取栈顶的mineActivity，先进后出原则
	public Activity getMineLastActivity() {
		return mineActivityStack.lastElement();
	}
}
