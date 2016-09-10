package com.wotingfm.activity.im.interphone.find.add;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 添加群组详情界面
 *
 * @author 辛龙
 *         2016年1月20日
 */
public class GroupAddActivity extends AppBaseActivity implements OnClickListener {
    private TextView tv_add;
    private SharedPreferences sharedPreferences;
    private String username;
    private TextView tv_name;
    private String url;
    private ImageLoader imageLoader;
    private ImageView image_touxiang;
    private TextView tv_id;
    private String GroupType;        // 验证群0；公开群1[原来的号码群]；密码群2
    private LinearLayout lin_mm;
    private EditText et_news;
    private String news;
    private EditText et_password;
    private FindGroupNews contact;
    private String psd = null;        // 密码
    private GroupAddActivity context;
    private TextView tv_sign;
    private String tag = "GROUP_ADD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private View linearVerification;

    private ImageView clearImage;

    @Override
    protected int setViewId() {
        return R.layout.activity_groupadds;
    }

    @Override
    protected void init() {
        setTitle("详细资料");
        sharedPreferences = this.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        username = sharedPreferences.getString(StringConstant.USERNAME, "");            // 当前登录账号的姓名
        contact = (FindGroupNews) getIntent().getSerializableExtra("contact");
        GroupType = contact.getGroupType();    // 当前组的类型
        setView();                            // 设置界面
        setListener();                        // 设置监听
        if (contact != null && !contact.equals("")) {
            setvalue(contact);                // 适配数据
        }
    }

    private void setView() {
        image_touxiang = (ImageView) findViewById(R.id.image_touxiang);
        et_password = (EditText) findViewById(R.id.et_password);
        et_news = (EditText) findViewById(R.id.et_news);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_id = (TextView) findViewById(R.id.tv_id);
        tv_sign = (TextView) findViewById(R.id.tv_sign);
        tv_add = (TextView) findViewById(R.id.tv_add);
        lin_mm = (LinearLayout) findViewById(R.id.lin_mm);
        clearImage = (ImageView) findViewById(R.id.clear_image);
        linearVerification = findViewById(R.id.linear_item);
    }

    private void setvalue(FindGroupNews contact) {
        if (GroupType == null || GroupType.equals("")) {
            tv_add.setVisibility(View.INVISIBLE);
        } else {
            tv_add.setVisibility(View.VISIBLE);
            if (GroupType.equals("0")) {
                // 验证群，暂不做
                tv_add.setText("申请入群");
                lin_mm.setVisibility(View.GONE);
                linearVerification.setVisibility(View.VISIBLE);
            } else if (GroupType.equals("2")) {
                // 密码群
                tv_add.setText("申请入群");
                lin_mm.setVisibility(View.VISIBLE);
                linearVerification.setVisibility(View.GONE);
            } else {
                // 公开群
                lin_mm.setVisibility(View.GONE);
                linearVerification.setVisibility(View.GONE);
                tv_add.setText("加入群组");
            }
        }

        if (contact.getGroupName() == null || contact.getGroupName().equals("")) {
            tv_name.setText("未知");
        } else {
            tv_name.setText(contact.getGroupName());
        }
        if (contact.getGroupNum() == null || contact.getGroupNum().equals("")) {
            tv_id.setText("000000");
        } else {
            tv_id.setText("id:" + contact.getGroupNum());
        }
        if (contact.getGroupOriDesc() == null || contact.getGroupOriDesc().equals("")) {
            tv_sign.setText("这家伙很懒，什么都没写");
        } else {
            tv_sign.setText(contact.getGroupOriDesc());
        }
        if (contact.getGroupImg() == null || contact.getGroupImg().equals("")
                || contact.getGroupImg().equals("null") || contact.getGroupImg().trim().equals("")) {
            image_touxiang.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            if (contact.getGroupImg().startsWith("http:")) {
                url = contact.getGroupImg();
            } else {
                url = GlobalConfig.imageurl + contact.getGroupImg();
            }
            imageLoader.DisplayImage(url.replace("\\/", "/"), image_touxiang, false, false, null, null);
        }
        if (username == null || username.equals("")) {
            et_news.setText("");
        } else {
            et_news.setText("我是 " + username);
        }
    }

    private void setListener() {
        imageLoader = new ImageLoader(this);
        tv_add.setOnClickListener(this);
        clearImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                et_news.setText("");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.tv_add:
            /*
			 * 此处需要分类型添加 1.直接添加，点击申请入组按钮后发送数据，然后获取返回值成功与否进行操作 2.发送验证消息的添加
			 * 3.输入密码的添加
			 */
                if (GroupType == null || GroupType.equals("")) {
                    ToastUtils.show_allways(getApplicationContext(), "数据异常，请稍后重试");
                } else {
                    if (GroupType.equals("0")) {
                        // 验证群
                        news = et_news.getText().toString().trim();
                        if (news == null || news.equals("")) {
                            ToastUtils.show_allways(getApplicationContext(), "请输入验证信息");
                        } else {
                            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                                // 进入验证群走单独接口
                                DialogUtils.showDialog(context);
                                sendrequest();
                            } else {
                                ToastUtils.show_allways(getApplicationContext(), "网络连接失败，请稍后重试");
                            }
                        }
                    } else if (GroupType.equals("2")) {
                        // 密码群
                        psd = et_password.getText().toString().trim();
                        if (psd.equals("")) {
                            ToastUtils.show_allways(getApplicationContext(), "请输入验证密码");
                        } else {
                            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                                DialogUtils.showDialog(context);
                                send();
                            } else {
                                ToastUtils.show_allways(getApplicationContext(), "网络连接失败，请稍后重试");
                            }
                        }
                    } else {
                        // 公开群
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            DialogUtils.showDialog(context);
                            send();
                        } else {
                            ToastUtils.show_allways(getApplicationContext(), "网络连接失败，请稍后重试");
                        }
                    }
                }
                break;
        }
    }

    private void sendrequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(context));
            jsonObject.put("GroupId", contact.getGroupId());
            if (news != null && !news.equals("")) {
                jsonObject.put("ApplyMsg", news);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.JoinGroupVertifyUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            //			private String Message;
            //			private String SessionId;

            @Override
            protected void requestSuccess(JSONObject result) {
                DialogUtils.closeDialog();
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                    //					SessionId = result.getString("SessionId");
                    //					Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null) {        // 根据返回值来对程序进行解析
                    if (ReturnType.equals("1001")) {
                        ToastUtils.show_allways(context, "验证请求已经发送，请等待管理员审核");
                    } else if (ReturnType.equals("0000")) {
                        ToastUtils.show_allways(context, "无法获取相关的参数");
                    } else if (ReturnType.equals("1002")) {
                        ToastUtils.show_allways(context, "无此分类信息");
                    } else if (ReturnType.equals("1003")) {
                        ToastUtils.show_allways(context, "无法获得列表");
                    } else if (ReturnType.equals("1011")) {
                        ToastUtils.show_allways(context, "列表为空（列表为空[size==0]");
                    } else if (ReturnType.equals("1006")) {
                        ToastUtils.show_allways(context, "您已经邀请过，请等待");
                    } else if (ReturnType.equals("T")) {
                        ToastUtils.show_allways(context, "获取列表异常");
                    }
                } else {
                    ToastUtils.show_allways(context, "获取数据失败");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    // 加入公开群和密码群的网络通信方法
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("UserId", CommonUtils.getUserId(GroupAddActivity.this));
            jsonObject.put("GroupNum", contact.getGroupNum());
            if (GroupType.equals("2")) {
                if (psd != null && !psd.equals("")) {
                    jsonObject.put("GroupPwd", psd);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.JoinGroupUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                DialogUtils.closeDialog();
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null) {
                    if (ReturnType.equals("T")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "异常返回值");
                    } else if (ReturnType.equals("1000")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "无法获取用户组ID");
                    } else if (ReturnType.equals("1001")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "成功返回，用户已经成功加入了这个群组");
                        Intent pushintent = new Intent("push_refreshlinkman");
                        context.sendBroadcast(pushintent);
                        // TalkGroupNewsActivity.class
                        Intent intent = new Intent(GroupAddActivity.this, GroupAddActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "groupaddactivity");
                        bundle.putSerializable("data", contact);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else if (ReturnType.equals("1101")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "成功返回，已经在用户组");
                        // TalkGroupNewsActivity.class
                        Intent intent = new Intent(GroupAddActivity.this, GroupAddActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "groupaddactivity");
                        bundle.putSerializable("data", contact);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    } else if (ReturnType.equals("1002")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "用户不存在");
                    } else if (ReturnType.equals("1003")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "用户组不存在");
                    } else if (ReturnType.equals("1004")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "用户组已经超过五十人，不允许再加入了");
                    } else if (ReturnType.equals("1006")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "加入密码群 ,需要提供密码");
                    } else if (ReturnType.equals("1007")) {
                        ToastUtils.show_allways(GroupAddActivity.this, "加入密码群 , 密码不正确");
                    }
                } else {
                    ToastUtils.show_allways(GroupAddActivity.this, "返回值异常，请稍后重试");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.popOneActivity(context);
        imageLoader = null;
        image_touxiang = null;
        et_password = null;
        et_news = null;
        tv_name = null;
        tv_id = null;
        tv_sign = null;
        tv_add = null;
        lin_mm = null;
        linearVerification = null;
        clearImage = null;
        sharedPreferences = null;
        context = null;
        username = null;
        url = null;
        GroupType = null;
        news = null;
        contact = null;
        psd = null;
        tag = null;
    }
}