package com.wotingfm.activity.im.interphone.creategroup.frienddetails;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.alert.CallAlertActivity;
import com.wotingfm.activity.im.interphone.chat.fragment.ChatFragment;
import com.wotingfm.activity.im.interphone.creategroup.frienddetails.model.GroupTalkInside;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.linkman.model.TalkPersonInside;
import com.wotingfm.activity.im.interphone.message.model.UserInviteMeInside;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.CreatQRImageHelper;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 个人详情页
 * 以下界面可以跳转到这个界面
 * 1、群组详情         - >  GroupDetailActivity
 * 2、全部群组成员     - >  AllGroupMemberActivity
 * 3、通讯录好友列表   - >  LinkManFragment
 * 4、聊天界面         - >  ChatFragment
 * 5、搜索好友结果     - >  FindNewsResultActivity
 *
 * 作者：xinlong on 2016/1/19
 * 邮箱：645700751@qq.com
 */
public class TalkPersonNewsActivity extends BaseActivity implements  OnClickListener{
    private MessageReceivers receiver;      // 用于删除好友之后刷新界面的广播
    private Bitmap bmp;

    private Dialog confirmDialog;           // 确定删除好友对话框
    private Dialog dialogs;                 // 加载数据对话框
    private TextView editSignature;         // 用户签名
    private ImageView imageHead;            // 用户头像
    private ImageView imageEwm;             // 二维码
    private EditText editName;              // 用户名

    private String name;                    // 用户名
    private String imageUrl;                // 用户头像
    private String id;                      // 用户 ID
    private String descN;                   // 用户签名
    private String aliasName;               // 用户备注名
    private String url12;                   // 用户头像
    private String tag = "TALK_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private boolean update;                 // 标记是否是修改状态
    private EditText et_beizhu;
    private ImageView imageModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_personnews);

        // 注册广播
        receiver = new MessageReceivers();
        IntentFilter filters = new IntentFilter();
        filters.addAction(BroadcastConstants.GROUP_DETAIL_CHANGE);
        registerReceiver(receiver, filters);

        dialogDelete();
        handleIntent();

        initView();
    }

    // 处理上一个界面传递过来的数据
    private void handleIntent() {
        String type = getIntent().getStringExtra("type");
        if (type == null || type.equals("")) {
            ToastUtils.show_always(context, "数据传递失败，请返回重试!");
            return ;
        }

        switch (type) {
            case "talkoldlistfragment_p":   // 由聊天界面跳转过来
                GroupTalkInside groupTalkInside = (GroupTalkInside) getIntent().getSerializableExtra("data");
                name = groupTalkInside.getUserName();
                imageUrl = groupTalkInside.getPortraitMini();
                id = groupTalkInside.getUserId();
                descN = groupTalkInside.getDescn();
                aliasName = groupTalkInside.getUserAliasName();
                break;
            case "TalkGroupNewsActivity": // 由群组详情、全部群组成员列表跳转过来
                UserInfo userInfo = (UserInfo) getIntent().getSerializableExtra("data");
                name = userInfo.getUserName();
                imageUrl = userInfo.getPortraitBig();
                id = userInfo.getUserId();
                descN = userInfo.getDescn();
                aliasName = userInfo.getUserAliasName();
                break;
            case "findActivity":            // 由搜索好友结果界面跳转过来
                UserInviteMeInside userInviteMeInside = (UserInviteMeInside) getIntent().getSerializableExtra("data");
                name = userInviteMeInside.getUserName();
                imageUrl = userInviteMeInside.getPortrait();
                id = userInviteMeInside.getUserId();
                descN = userInviteMeInside.getDescn();
                aliasName = userInviteMeInside.getUserAliasName();
                break;
            case "talkpersonfragment":      // 由通讯录好友跳转过来
                TalkPersonInside talkPersonInside = (TalkPersonInside) getIntent().getSerializableExtra("data");
                name = talkPersonInside.getUserName();
                imageUrl = talkPersonInside.getPortraitMini();
                id = talkPersonInside.getUserId();
                descN = talkPersonInside.getDescn();
                aliasName = talkPersonInside.getUserAliasName();
                break;
        }
    }

    // 初始化界面
    private void initView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);          // 返回
        findViewById(R.id.imageView4).setOnClickListener(this);             // 对讲
        findViewById(R.id.tv_delete).setOnClickListener(this);              // 删除好友

        imageModify=(ImageView)findViewById(R.id.imageView3);
        imageModify.setOnClickListener(this);
        imageHead = (ImageView) findViewById(R.id.image_portrait);          // 头像
        editName = (EditText) findViewById(R.id.et_b_name);                 // 用户名 备注名
        et_beizhu=(EditText) findViewById(R.id.et_beizhu);
        editSignature = (TextView) findViewById(R.id.et_groupSignature);    // 用户签名
        imageEwm = (ImageView) findViewById(R.id.imageView_ewm);            // 二维码图片

        setData();
    }

    // 初始化数据
    private void setData() {
        // 设置好友头像显示
        if (imageUrl == null || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
            imageHead.setImageBitmap(bmp);
        } else {
            if (imageUrl.startsWith("http:")) {
                url12 = imageUrl;
            } else {
                url12 = GlobalConfig.imageurl + imageUrl;
            }
            Picasso.with(context).load(url12.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageHead);
        }

        // 好友签名 不可更改好友签名 只能由其本人修改
        if (descN == null || descN.equals("")) {
            descN = "这家伙很懒，什么都没写";
        }
        editSignature.setText(descN);

        if(!TextUtils.isEmpty(name)){
            editName.setText(name);
        }else{
            editName.setText("未知");
        }

        // 好友名称  给好友的备注名  可随时修改 但修改的是给好友的备注好友的名称没有改变
        if (aliasName == null || aliasName.equals("")) {
            et_beizhu.setText(name);
        }else{
            et_beizhu.setText(aliasName);
        }
        et_beizhu.setVisibility(View.VISIBLE);
        et_beizhu.setEnabled(false);

        // 二维码以及二维码中包含的好友信息
        UserInfo userInfo = new UserInfo();
        userInfo.setPortraitMini(imageUrl);
        userInfo.setUserId(id);
        userInfo.setUserName(name);
        bmp = CreatQRImageHelper.getInstance().createQRImage(1, userInfo, 300, 300);
        if (bmp == null) {
            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }
        imageEwm.setImageBitmap(bmp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:    // 返回
                finish();
                break;
            case R.id.imageView4:        // 对讲
                call(id);
                break;
            case R.id.tv_delete:        // 删除好友
                confirmDialog.show();
                break;
            case R.id.imageView3:     // 修改好友备注
                et_beizhu.setEnabled(false);
                String beiName;
                if (update) {           // 此时是修改状态需要进行以下操作
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wancheng);
                    imageModify.setImageBitmap(bmp);
                    beiName = et_beizhu.getText().toString().trim();
                    if(beiName.equals(aliasName)) {
                        return ;
                    }
                    if (beiName.equals("")) {
                        beiName = name;
                    }

                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialogs = DialogUtils.Dialogph(context, "提交中");
                        update(beiName);
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {                // 此时是未编辑状态
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wancheng);
                    imageModify.setImageBitmap(bmp);
                    et_beizhu.setEnabled(true);
                    et_beizhu.setBackgroundColor(getResources().getColor(R.color.dinglan_orange));
                    et_beizhu.setTextColor(getResources().getColor(R.color.white));
                }
                update = !update;
                break;
            case R.id.tv_cancle:        // 取消删除好友
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:       // 确定删除好友
                confirmDialog.dismiss();
                if (id != null && !id.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialogs = DialogUtils.Dialogph(context, "正在获取数据");
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请检查网络!");
                    }
                } else {
                    ToastUtils.show_always(context, "删除好友失败，请稍后重试!");
                }
                break;
        }
    }

    // 修改好友资料
    protected void update(final String friendAliasName) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("FriendUserId", id);
            jsonObject.put("FriendAliasName", friendAliasName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updateFriendnewsUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                        /*    editName.setText(friendAliasName);*/
                            sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                            sendBroadcast(new Intent(BroadcastConstants.REFRESH_GROUP));
                            ToastUtils.show_always(context, "修改成功");
                        } else if (ReturnType.equals("0000")) {
                            ToastUtils.show_always(context, "无法获取相关的参数");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "无法获取用ID");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_always(context, "好友Id无法获取");
                        } else if (ReturnType.equals("1004")) {
                            ToastUtils.show_always(context, "好友不存在");
                        } else if (ReturnType.equals("1005")) {
                            ToastUtils.show_always(context, "好友为自己无法修改");
                        } else if (ReturnType.equals("1006")) {
                            ToastUtils.show_always(context, "没有可修改信息");
                        } else if (ReturnType.equals("1007")) {
                            ToastUtils.show_always(context, "不是好友，无法修改");
                        } else if (ReturnType.equals("1008")) {
                            ToastUtils.show_always(context, "修改失败");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_always(context, "获取列表异常");
                        } else if (ReturnType.equals("200")) {
                            ToastUtils.show_always(context, "您没有登录");
                        }
                    } else {
                        ToastUtils.show_always(context, "列表处理异常");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) dialogs.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("FriendUserId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.delFriendUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001")) {
                            sendBroadcast( new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                            if (ChatFragment.context != null &&
                                    ChatFragment.interPhoneId != null && ChatFragment.interPhoneId.equals(id)) {
                                // 保存通讯录是否刷新的属性
                                Editor et = BSApplication.SharedPreferences.edit();
                                et.putString(StringConstant.PERSONREFRESHB, "true");
                                if(et.commit()) {
                                    L.v("数据 commit 失败!");
                                }
                            }
                            ToastUtils.show_always(context, "已经删除成功");
                            finish();
                        } else if (ReturnType.equals("0000")) {
                            ToastUtils.show_always(context, "无法获取相关的参数");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "无法获取用ID");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_always(context, "好友Id无法获取");
                        } else if (ReturnType.equals("1004")) {
                            ToastUtils.show_always(context, "好友不存在");
                        } else if (ReturnType.equals("1005")) {
                            ToastUtils.show_always(context, "不是好友，不必删除");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_always(context, "获取列表异常");
                        }
                    } else {
                        ToastUtils.show_always(context, "列表处理异常");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) dialogs.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 对讲
    protected void call(String id) {
        Intent it = new Intent(TalkPersonNewsActivity.this, CallAlertActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        it.putExtras(bundle);
        startActivity(it);
    }

    // 确定删除好友对话框
    private void dialogDelete() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        dialog.findViewById(R.id.tv_cancle).setOnClickListener(this);// 取消
        dialog.findViewById(R.id.tv_confirm).setOnClickListener(this);// 确定
        TextView textTitle = (TextView) dialog.findViewById(R.id.tv_title);
        textTitle.setText("确定要删除该好友？");
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 广播  用于删除好友之后的刷新界面
    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.GROUP_DETAIL_CHANGE)) {
                send();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        confirmDialog = null;
        name = null;
        imageUrl = null;
        id = null;
        imageHead = null;
        dialogs = null;
        editSignature = null;
        editName = null;
        descN = null;
        aliasName = null;
        imageEwm = null;
        url12 = null;
        tag = null;
        setContentView(R.layout.activity_null);
    }
}
