package com.wotingfm.activity.mine.set.preference.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.mine.set.preference.adapter.PianHaoAdapter;
import com.wotingfm.activity.music.program.fenlei.model.FenLei;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 偏好设置界面
 * 作者：xinlong on 2016/9/5 17:36
 * 邮箱：645700751@qq.com
 */
public class PreferenceActivity extends BaseActivity implements View.OnClickListener {
    private static PianHaoAdapter adapter;
    private static List<FenLei> tempList;
    private List<String> preferenceList = new ArrayList<>();

    private Dialog dialog;
    private ListView listPrefer;

    private String tag = "PREFERENCE_SET_REQUEST_CANCEL_TAG"; // 取消网络请求标签
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        initView();
    }

    // 初始化视图
    private void initView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
        findViewById(R.id.tv_save).setOnClickListener(this);// 保存

        listPrefer = (ListView) findViewById(R.id.lv_prefer);

        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取信息");
            send();
        } else {
            ToastUtils.show_always(context, "网络连接失败，请检查网络!");
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.head_left_btn:// 返回
                finish();
                break;
            case R.id.tv_save:
                preferenceList.clear();
                try {
                    for (int i = 0; i < tempList.size(); i++) {
                        for (int j = 0; j < tempList.get(i).getChildren().size(); j++) {
                            if (tempList.get(i).getChildren().get(j).getchecked().equals("true")) {
                                String s = tempList.get(i).getChildren().get(j).getAttributes().getmId() + "::"
                                        + tempList.get(i).getChildren().get(j).getAttributes().getId();
                                preferenceList.add(s);
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                if(preferenceList.size() != 0) {
                    if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "通讯中...");
                        sendRequest();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "您还没有选择偏好");
                }
                break;
        }
    }

    private void sendRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            if(!TextUtils.isEmpty(CommonUtils.getUserIdNoImei(context))) {
                jsonObject.put("UserId", CommonUtils.getUserId(context));
            }
            String s = preferenceList.toString();
            jsonObject.put("PrefStr", s.substring(1, s.length() - 1));
            jsonObject.put("IsOnlyCata", 2);
            jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
            jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
            jsonObject.put("IMEI", PhoneMessage.imei);
            PhoneMessage.getGps(context);
            jsonObject.put("GPS-longitude", PhoneMessage.longitude);
            jsonObject.put("GPS-latitude ", PhoneMessage.latitude);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.setPreferenceUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if(dialog != null) dialog.dismiss();
                if(isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if(ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "偏好已经设置成功");
                    } else {
                        ToastUtils.show_always(context, "偏好设置失败，请稍后重试!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if(dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 发送网络请求
    private void send() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
            jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
            jsonObject.put("IMEI", PhoneMessage.imei);
            PhoneMessage.getGps(context);
            jsonObject.put("GPS-longitude", PhoneMessage.longitude);
            jsonObject.put("GPS-latitude ", PhoneMessage.latitude);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getPreferenceUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if(dialog != null) dialog.dismiss();
                if(isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if(ReturnType != null && ReturnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("PrefTree")).nextValue();
                        tempList = new Gson().fromJson(arg1.getString("children"), new TypeToken<List<FenLei>>() {}.getType());
                        if(tempList == null || tempList.size() == 0) return ;
                        if(!TextUtils.isEmpty(CommonUtils.getUserIdNoImei(context))) {
                            sendTwice();
                        } else {
                            // 对每个返回的分类做设置 默认为全部未选中状态 此时获取的为是所有的列表内容
                            if(adapter == null) {
                                listPrefer.setAdapter(adapter = new PianHaoAdapter(context, tempList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            setInterface();
                        }
                    } else {
                        ToastUtils.show_always(context, "获取列表失败，请稍后重试!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if(dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private void sendTwice() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("UserId", CommonUtils.getUserIdNoImei(context));
            jsonObject.put("MobileClass", PhoneMessage.model + "::" + PhoneMessage.productor);
            jsonObject.put("ScreenSize", PhoneMessage.ScreenWidth + "x" + PhoneMessage.ScreenHeight);
            jsonObject.put("IMEI", PhoneMessage.imei);
            PhoneMessage.getGps(context);
            jsonObject.put("GPS-longitude", PhoneMessage.longitude);
            jsonObject.put("GPS-latitude ", PhoneMessage.latitude);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getPreferenceUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if(dialog != null) dialog.dismiss();
                if(isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if(ReturnType != null && ReturnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("PrefTree")).nextValue();
                        List<FenLei> mList = new Gson().fromJson(arg1.getString("children"), new TypeToken<List<FenLei>>() {}.getType());
                        for(int i = 0; i < mList.size(); i++) {
                            for(int j = 0; j < tempList.size(); j++) {
                                for(int k = 0; k < tempList.get(j).getChildren().size(); k++) {
                                    if(mList.get(i).getId().equals(tempList.get(j).getChildren().get(k).getId())) {
                                        tempList.get(j).getChildren().get(k).setchecked("true");
                                    }
                                }
                            }
                        }
                        if(adapter == null) {
                            listPrefer.setAdapter(adapter = new PianHaoAdapter(context, tempList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setInterface();
                    } else if(ReturnType != null && ReturnType.equals("1011")) {
                        if(adapter == null) {
                            listPrefer.setAdapter(adapter = new PianHaoAdapter(context, tempList));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setInterface();
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private void setInterface() {
        adapter.setOnListener(new PianHaoAdapter.preferCheck() {
            @Override
            public void clickPosition(int position) {
                if(tempList.get(position).getChildren().get(0).getchecked().equals("false")) {
                    for(int i = 0; i < tempList.get(position).getChildren().size(); i++) {
                        tempList.get(position).getChildren().get(i).setchecked("true");
                    }
                    tempList.get(position).setTag(position);
                    tempList.get(position).setTagType(1);
                } else {
                    for(int i = 0; i < tempList.get(position).getChildren().size(); i++) {
                        tempList.get(position).getChildren().get(i).setchecked("false");
                    }
                    tempList.get(position).setTag(position);
                    tempList.get(position).setTagType(0);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static void RefreshView(List<FenLei> list) {
        if(adapter != null) {
            tempList = list;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
        et.putString(StringConstant.PREFERENCE, "1");// 保存偏好设置页查看状态
        if(!et.commit()) L.w("数据 commit 失败!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        adapter = null;
        tempList = null;
        setContentView(R.layout.activity_null);
    }
}
