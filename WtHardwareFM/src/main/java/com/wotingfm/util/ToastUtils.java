package com.wotingfm.util;

import android.content.Context;
import android.widget.Toast;

import com.wotingfm.common.config.GlobalConfig;

/**toast提示工具类*/
public class ToastUtils {
	/**长时间提示*/
	public static void show_long(Context context, String content){
		if(GlobalConfig.istusi==true){
			Toast.makeText(context, content, Toast.LENGTH_LONG).show();
		}
	}
	/**短时间提示*/
	public static void show_short(Context context, String content){
		if(GlobalConfig.istusi==true){
			Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
		}
	}

	/**一直提示*/
	public static void show_always(Context context, String content){
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

    /**Volley 请求发生错误或服务器错误对用户的提示*/
    public static void showVolleyError(Context context){
		if(GlobalConfig.istusi==true){
			Toast.makeText(context, "无法连接到服务器!", Toast.LENGTH_LONG).show();
		}
    }
	/**没有网络时对用户的提示*/
	public static void showNoNetwork(Context context){
//        if(GlobalConfig.isToast) {
//            Toast.makeText(context, "连接服务器失败!", Toast.LENGTH_SHORT).show();
//        }
		Toast.makeText(context, "网路连接失败，请检查网络设置!", Toast.LENGTH_SHORT).show();
	}
}
