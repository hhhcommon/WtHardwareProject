package com.wotingfm.common.volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.PhoneMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Volley 网络请求类
 *
 * @author woting11
 */
public class VolleyRequest {
    private static final String TAG = "VOLLEY_CANCEL_REQUEST_DEFAULT_TAG";

    /**
     * get网络请求 带 默认标签   用于取消网络请求
     *
     * @param url        网络请求地址
     * @param jsonObject 请求参数
     * @param callback   返回值
     */
    public static void RequestGet(String url, JSONObject jsonObject, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener());
        jsonObjectRequest.setTag(TAG);// 设置标签
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        //		BSApplication.getHttpQueues().start();// 启动

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
    }

    /**
     * get网络请求  自定义标签  用于取消网络请求
     *
     * @param tag        标签
     * @param url        网络请求地址
     * @param jsonObject 请求参数
     * @param callback   返回值
     */
    public static void RequestGet(String url, JSONObject jsonObject, String tag, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener());
        jsonObjectRequest.setTag(tag);// 设置标签
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        //		BSApplication.getHttpQueues().start();// 启动

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
    }

    /**
     * post网络请求  带 默认标签  用于取消网络请求
     */
    public static void RequestPost(String url, JSONObject jsonObject, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Method.POST, GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener());

        jsonObjectRequest.setTag(TAG);// 设置标签
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(GlobalConfig.HTTP_CONNECTION_TIMEOUT, 1, 1.0f));
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        //		BSApplication.getHttpQueues().start();// 启动

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
        Log.v("请求服务器提交的参数", "--- > > >  " + jsonObject.toString());
    }

    /**
     * post网络请求  自定义标签  用于取消网络请求
     *
     * @param tag        标签
     * @param url        网络请求地址
     * @param jsonObject 请求参数
     * @param callback   返回值
     */
    public static void RequestPost(String url, String tag, JSONObject jsonObject, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Method.POST, GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener());

        jsonObjectRequest.setTag(tag);// 设置标签
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(GlobalConfig.HTTP_CONNECTION_TIMEOUT, 1, 1.0f));
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        //		BSApplication.getHttpQueues().start();// 启动

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
        Log.v("请求服务器提交的参数", "--- > > >  " + jsonObject.toString());
    }

    /**
     * 发送语音搜索
     *
     * @param tag        标签
     * @param url        网络请求地址
     * @param jsonObject 请求参数
     * @param callback   返回值
     */
    public static void RequestTextVoicePost(String url, String tag, JSONObject jsonObject, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Method.POST, GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener()) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, "UTF-8");
                    if (jsonString.startsWith("\ufeff")) {
                        jsonString = jsonString.substring(1);
                    }
                    JSONObject jsonObject = new JSONObject(jsonString);
                    return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        jsonObjectRequest.setTag(tag);// 设置标签
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(GlobalConfig.HTTP_CONNECTION_TIMEOUT, 1, 1.0f));
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        //		BSApplication.getHttpQueues().start();// 启动

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
        Log.v("请求服务器提交的参数", "--- > > >  " + jsonObject.toString());
    }

    /**
     * 发送语音搜索
     */
    public static void RequestTextVoicePost(String url, JSONObject jsonObject, VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Method.POST, GlobalConfig.baseUrl+url, jsonObject, callback.loadingListener(), callback.errorListener()) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, "UTF-8");
                    if (jsonString.startsWith("\ufeff")) {
                        jsonString = jsonString.substring(1);
                    }
                    JSONObject jsonObject = new JSONObject(jsonString);
                    return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception je) {
                    return Response.error(new ParseError(je));
                }
            }
        };
        jsonObjectRequest.setTag(TAG);// 设置标签
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(GlobalConfig.HTTP_CONNECTION_TIMEOUT, 1, 1.0f));
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列
        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.baseUrl+url);
        Log.v("请求服务器提交的参数", "--- > > >  " + jsonObject.toString());
    }

    /**
     * 取消默认标签网络请求
     */
    public static boolean cancelRequest() {
        BSApplication.getHttpQueues().cancelAll(TAG);
        Log.w("取消网络请求", "--- > > >" + "\t" + TAG);
        return true;
    }

    /**
     * 取消自定义标签的网络请求
     */
    public static boolean cancelRequest(String tag) {
        BSApplication.getHttpQueues().cancelAll(tag);
        Log.w("取消网络请求", "--- > > >" + "\t" + tag);
        return true;
    }

    /**
     * 获取网络请求公共请求属性
     */
    public static JSONObject getJsonObject(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            String userId = CommonUtils.getSocketUserId(context);
            if (userId != null && !userId.trim().equals("")) {
                jsonObject.put("UserId", userId);
            }
            jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
            jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
            jsonObject.put("IMEI", PhoneMessage.imei);
            PhoneMessage.getGps(context);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
            jsonObject.put("GPS-longitude", PhoneMessage.longitude);
            jsonObject.put("GPS-latitude", PhoneMessage.latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 上传数据
     */
    public static void updateData(String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Method.POST, GlobalConfig.gatherData, jsonObject, null, null);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(GlobalConfig.HTTP_CONNECTION_TIMEOUT, 1, 1.0f));
        BSApplication.getHttpQueues().add(jsonObjectRequest);// 加入队列

        Log.i("请求服务器地址", "--- > > >  " + GlobalConfig.gatherData);
        Log.v("请求服务器提交的参数", "--- > > >  " + jsonObject.toString());
    }
}