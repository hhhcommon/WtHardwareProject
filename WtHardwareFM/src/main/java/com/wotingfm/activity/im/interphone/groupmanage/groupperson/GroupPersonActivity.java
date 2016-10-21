package com.wotingfm.activity.im.interphone.groupmanage.groupperson;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.alert.CallAlertActivity;
import com.wotingfm.activity.im.interphone.chat.fragment.ChatFragment;
import com.wotingfm.activity.im.interphone.chat.model.GroupTalkInside;
import com.wotingfm.activity.im.interphone.chat.model.TalkListGP;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.linkman.model.TalkPersonInside;
import com.wotingfm.activity.im.interphone.message.model.UserInviteMeInside;
import com.wotingfm.activity.mine.qrcode.EWMShowActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.CreatQRImageHelper;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 是我的好友的界面，通过群界面登入
 * 作者：xinlong on 2016/4/13
 * 邮箱：645700751@qq.com
 */
public class GroupPersonActivity extends BaseActivity {
    private String name;
    private String imageUrl;
    private String id;
    private LinearLayout head_left_btn;
    private ImageView image_add;
    private TextView tv_delete;
    private ImageView image_xiugai;
    private ImageView image_touxiang;
    private TextView tv_name;
    private TextView tv_id;
    private LinearLayout lin_person_xiugai;
    private GroupPersonActivity context;
    private Dialog confirmDialog;
    private Dialog dialogs;
    private EditText et_groupSignature;
    private EditText et_b_name;
    private boolean update;
    private String descn;
    private String num;
    private String b_name;
    private ImageView imageView_ewm;
    private LinearLayout lin_ewm;

    private int viewType = -1;//=1时代表来自groupmembers
    private String groupId;
    private String url12;
    private Bitmap bmp;
    private Bitmap bmpS;
    private String tag = "TALK_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private String url;
    private MessageReceivers Receiver;
    private UserInfo news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_person);
        context = this;
        update = false;    // 此时修改的状态
        setView();
        handleIntent();
        setData();
        setListener();
        dialogDelete();
        if (Receiver == null) {
            Receiver = new MessageReceivers();
            IntentFilter filters = new IntentFilter();
            filters.addAction("GROUP_DETAIL_CHANGE");
            context.registerReceiver(Receiver, filters);
        }
    }

    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("GROUP_DETAIL_CHANGE")) {
                send();
            }
        }
    }

    private void dialogDelete() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        TextView tv_confirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);
        tv_title.setText("确定要删除该好友？");
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        /*
         * LayoutParams pr2 = (LayoutParams)(confirmdialog.getLayoutParams());
		 * pr2.width = PhoneMessage.ScreenWidth - 120;
		 */
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id != null && !id.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        confirmDialog.dismiss();
                        dialogs = DialogUtils.Dialogph(context, "正在获取数据");
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "用户ID为空，无法删除该好友，请稍后重试");
                }
            }
        });
    }

    private void setView() {
        image_touxiang = (ImageView) findViewById(R.id.image_touxiang);
        tv_name = (TextView) findViewById(R.id.tv_name);
        et_b_name = (EditText) findViewById(R.id.et_b_name);
        et_groupSignature = (EditText) findViewById(R.id.et_groupSignature);
        tv_id = (TextView) findViewById(R.id.tv_id);
        lin_ewm = (LinearLayout) findViewById(R.id.lin_ewm);
        head_left_btn = (LinearLayout) findViewById(R.id.head_left_btn);
        imageView_ewm = (ImageView) findViewById(R.id.imageView_ewm);
        image_add = (ImageView) findViewById(R.id.image_add);
        image_xiugai = (ImageView) findViewById(R.id.image_xiugai);
        tv_delete = (TextView) findViewById(R.id.tv_delete);
        lin_person_xiugai = (LinearLayout) findViewById(R.id.lin_person_xiugai);
        et_b_name.setEnabled(false);
        et_groupSignature.setEnabled(false);
    }

    private void handleIntent() {
        String type = this.getIntent().getStringExtra("type");
        if (type == null || type.equals("")) {
        } else if (type.equals("talkoldlistfragment")) {
            TalkListGP data = (TalkListGP) this.getIntent().getSerializableExtra("data");
            name = data.getName();
            imageUrl = data.getPortrait();
            id = data.getId();
            descn = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("talkoldlistfragment_p")) {
            GroupTalkInside data = (GroupTalkInside) this.getIntent().getSerializableExtra("data");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descn = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();

        } else if (type.equals("findActivity")) {
            // 处理组邀请时进入
            UserInviteMeInside data = (UserInviteMeInside) this.getIntent().getSerializableExtra("data");
            name = data.getUserName();
            imageUrl = data.getPortrait();
            id = data.getUserId();
            descn = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
            tv_delete.setVisibility(View.GONE);
            lin_person_xiugai.setVisibility(View.INVISIBLE);
        } else if (type.equals("GroupMemers")) {
            TalkPersonInside data = (TalkPersonInside) this.getIntent().getSerializableExtra("data");
            groupId = this.getIntent().getStringExtra("id");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descn = data.getDescn();
            b_name = data.getUserAliasName();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
            viewType = 1;
        } else {
            UserInfo data = (UserInfo) this.getIntent().getSerializableExtra("data");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descn = data.getDescn();
            b_name = data.getUserAliasName();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        }
    }

    private void setData() {
        if (name == null || name.equals("")) {
            tv_name.setText("我听科技");
        } else {
            tv_name.setText(name);
        }
        if (num == null || num.equals("")) {
            num = "0000";
            tv_id.setVisibility(View.GONE);
        } else {
            tv_id.setVisibility(View.VISIBLE);
            tv_id.setText(num);
        }
        if (descn == null || descn.equals("")) {
            descn = "这家伙很懒，什么都没写";
            et_groupSignature.setText(descn);
        } else {
            et_groupSignature.setText(descn);
        }
        if (b_name == null || b_name.equals("")) {
            et_b_name.setText("暂无备注名");
        } else {
            et_b_name.setText(b_name);
        }
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null")
                || imageUrl.trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
            image_touxiang.setImageBitmap(bmp);
        } else {
            if (imageUrl.startsWith("http:")) {
                url12 = imageUrl;
            } else {
                url12 = GlobalConfig.imageurl + imageUrl;
            }
            Picasso.with(context).load(url12.replace("\\/", "/")).resize(100, 100).centerCrop().into(image_touxiang);
        }
        news = new UserInfo();
        news.setPortraitMini(imageUrl);
        news.setUserId(id);
        news.setUserName(name);
        bmp = CreatQRImageHelper.getInstance().createQRImage(1, news, 300, 300);
        if (bmp != null) {
            imageView_ewm.setImageBitmap(bmp);
        } else {
            bmpS = BitmapUtils.readBitMap(context, R.mipmap.ewm);
            imageView_ewm.setImageBitmap(bmpS);
        }
    }

    private void setListener() {
        lin_ewm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EWMShowActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "1");
                bundle.putString("id", num);
                bundle.putString("image", imageUrl);
                bundle.putString("news", descn);
                bundle.putString("name", name);
                bundle.putSerializable("person", news);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        image_xiugai.setOnClickListener(new View.OnClickListener() {
            private String biename;
            private String groupSignature;

            @Override
            public void onClick(View v) {
                if (update) {
                    // 此时是修改状态需要进行以下操作
                    if (id.equals(CommonUtils.getUserId(context))) {
                        if (et_b_name.getText().toString() == null
                                || et_b_name.getText().toString().trim().equals("")
                                || et_b_name.getText().toString().trim().equals("暂无备注名")) {
                            biename = " ";
                        } else {
                            biename = et_b_name.getText().toString();
                        }
                        if (et_groupSignature.getText().toString() == null
                                || et_groupSignature.getText().toString().trim().equals("")
                                || et_groupSignature.getText().toString().trim().equals("这家伙很懒，什么都没写")) {
                            groupSignature = " ";
                        } else {
                            groupSignature = et_groupSignature.getText().toString();
                        }
                    } else {
                        if (et_b_name.getText().toString() == null
                                || et_b_name.getText().toString().trim().equals("")
                                || et_b_name.getText().toString().trim().equals("暂无备注名")) {
                            biename = " ";
                        } else {
                            biename = et_b_name.getText().toString();
                        }
                        groupSignature = "";
                    }
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialogs = DialogUtils.Dialogph(context, "提交中");
                        update(biename, groupSignature);
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                    et_b_name.setEnabled(false);
                    et_groupSignature.setEnabled(false);
                    et_b_name.setBackgroundColor(context.getResources().getColor(R.color.dinglan_orange));
                    et_b_name.setTextColor(context.getResources().getColor(R.color.white));
                    et_groupSignature.setBackgroundColor(context.getResources().getColor(R.color.dinglan_orange));
                    et_groupSignature.setTextColor(context.getResources().getColor(R.color.white));
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.xiugai);
                    image_xiugai.setImageBitmap(bmp);
                    update = false;
                } else {
                    // 此时是未编辑状态
                    if (id.equals(CommonUtils.getUserId(context))) {
                        // 此时是我本人
                        et_b_name.setEnabled(true);
                        et_groupSignature.setEnabled(true);
                        et_b_name.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_b_name.setTextColor(context.getResources().getColor(R.color.gray));
                        et_groupSignature.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_groupSignature.setTextColor(context.getResources().getColor(R.color.gray));
                    } else {
                        // 此时我不是我本人
                        et_b_name.setEnabled(true);
                        et_b_name.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_b_name.setTextColor(context.getResources().getColor(R.color.gray));
                    }
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wancheng);
                    image_xiugai.setImageBitmap(bmp);
                    update = true;
                }
            }
        });

        head_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }

        });

        image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call(id);
            }
        });
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.show();
            }
        });
    }

    protected void update(final String b_name2, String groupSignature) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 公共请求属性
            if (viewType == -1) {
                jsonObject.put("FriendUserId", id);
                jsonObject.put("FriendAliasName", b_name2);
                jsonObject.put("FriendAliasDescn", groupSignature);
                url = GlobalConfig.updateFriendnewsUrl;
            } else {
                jsonObject.put("GroupId", groupId);
                jsonObject.put("UpdateUserId", id);
                jsonObject.put("UserAliasName", b_name2);
                jsonObject.put("UserAliasDescn", groupSignature);
                url = GlobalConfig.updategroupFriendnewsUrl;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(url, groupSignature, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    // 根据返回值来对程序进行解析
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                            et_b_name.setText(b_name2);
                            context.sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));
                            context.sendBroadcast(new Intent("GROUP_DETAIL_CHANGE"));
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
                if (dialogs != null) {
                    dialogs.dismiss();
                }
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
                    // 根据返回值来对程序进行解析
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001")) {
                            context.sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));
                            if (ChatFragment.context != null &&
                                    ChatFragment.interPhoneId != null && ChatFragment.interPhoneId.equals(id)) {
                                // 保存通讯录是否刷新的属性
                                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                                et.putString(StringConstant.PERSONREFRESHB, "true");
                                et.commit();
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
                if (dialogs != null) {
                    dialogs.dismiss();
                }
            }
        });
    }

    protected void call(String id) {
        Intent it = new Intent(GroupPersonActivity.this, CallAlertActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        it.putExtras(bundle);
        // it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(it);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        if (bmpS != null && !bmpS.isRecycled()) {
            bmpS.recycle();
            bmpS = null;
        }
        news = null;
        confirmDialog = null;
        context = null;
        name = null;
        imageUrl = null;
        id = null;
        head_left_btn = null;
        image_add = null;
        tv_delete = null;
        image_xiugai = null;
        image_touxiang = null;
        tv_name = null;
        tv_id = null;
        lin_person_xiugai = null;
        dialogs = null;
        et_groupSignature = null;
        et_b_name = null;
        descn = null;
        num = null;
        b_name = null;
        imageView_ewm = null;
        lin_ewm = null;
        groupId = null;
        url12 = null;
        tag = null;
        url = null;
        setContentView(R.layout.activity_null);
    }
}
