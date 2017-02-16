package com.wotingfm.common.helper;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.android.volley.VolleyError;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author：辛龙 (xinLong)
 * 2017/1/13 11:17
 * 邮箱：645700751@qq.com
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例
    private Context ctx;

    private CrashHandler(){}

    public synchronized static CrashHandler getInstance(){  //同步方法，以免单例多线程环境下出现异常
        if (instance == null){
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context ctx){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        this.ctx=ctx;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {  //当有未处理的异常发生时，就会来到这里。。
        String s="uncaughtException, thread: " + thread
                + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
                + ex+"时间："+System.currentTimeMillis();
        send(s);
        Log.d("woTing",s );
        Looper.loop();
    }

    private void send(String sEditContent) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(ctx);
        try {
            jsonObject.put("PCDType", GlobalConfig.PCDType);
            jsonObject.put("Opinion", sEditContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.FeedBackUrl, "", jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
            }

            @Override
            protected void requestError(VolleyError error) {
            }
        });
    }
}