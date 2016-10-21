package com.wotingfm.activity.im.interphone.groupmanage.grouppersonnews;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 组内联系人详情页(非好友)
 * 作者：xinlong on 2016/1/19
 * 邮箱：645700751@qq.com
 */
public class GroupPersonNewsActivity extends BaseActivity {
    private String name;
    private String imageUrl;
    private String id;
    private LinearLayout head_left_btn;
    private ImageView image_xiugai;
    private ImageView image_touxiang;
    private TextView tv_name;
    private TextView tv_id;
    private GroupPersonNewsActivity context;
    private Dialog dialogs;
    private EditText et_groupSignature;
    private EditText et_b_name;
    private boolean update;
    private String descn;
    private String num;
    private String b_name;
    private String groupId;
    private LinearLayout lin_delete;
    private EditText et_news;
    private Dialog dialog;
    private TextView tv_add;
    private SharedPreferences sharedPreferences = BSApplication.SharedPreferences;
    private String username;
    private String url12;
    private String tag = "GROUP_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_personnews);
        context = this;
        update = false;    // 此时修改的状态
        username = sharedPreferences.getString(StringConstant.USERNAME, "");            //当前登录账号的姓名
        setView();
        handleIntent();
        setData();
        setListener();
    }

    private void setView() {
        lin_delete = (LinearLayout) findViewById(R.id.lin_delete);      // 验证信息清空
        et_news = (EditText) findViewById(R.id.et_news);                // 验证信息输入框
        tv_add = (TextView) findViewById(R.id.tv_add);                  // 添加好友
        image_touxiang = (ImageView) findViewById(R.id.image_touxiang);
        tv_name = (TextView) findViewById(R.id.tv_name);
        et_b_name = (EditText) findViewById(R.id.et_b_name);
        et_groupSignature = (EditText) findViewById(R.id.et_groupSignature);
        tv_id = (TextView) findViewById(R.id.tv_id);
        head_left_btn = (LinearLayout) findViewById(R.id.head_left_btn);
        image_xiugai = (ImageView) findViewById(R.id.image_xiugai);
    }

    private void handleIntent() {
        String type = this.getIntent().getStringExtra("type");
        groupId = this.getIntent().getStringExtra("id");
        if (type == null || type.equals("")) {
        }
//		if (type.equals("talkoldlistfragment_p")) {
//			GroupTalkInside data = (GroupTalkInside) this.getIntent().getSerializableExtra("data");
//			name = data.getUserName();
//			imageurl = data.getPortraitBig();
//			id = data.getUserId();
//			descn = data.getDescn();
//			num = data.getUserNum();
//			b_name = data.getUserAliasName();
//		} else if (type.equals("TalkGroupNewsActivity_p")) {
//			 data = (GroupTalkInside) this.getIntent().getSerializableExtra("data");
//			name = data.getUserName();
//			imageurl = data.getPortraitBig();
//			id = data.getUserId();
//			descn = data.getDescn();
//			num = data.getUserNum();
//			b_name = data.getUserAliasName();
//		}
        else if (type.equals("GroupMemers")) {
            UserInfo data = (UserInfo) this.getIntent().getSerializableExtra("data");
            name = data.getUserName();
            imageUrl = data.getPortraitBig();
            id = data.getUserId();
            descn = data.getDescn();
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
            tv_id.setVisibility(View.GONE);
        } else {
            tv_id.setVisibility(View.VISIBLE);
            tv_id.setText(num);
        }
        if (descn == null || descn.equals("")) {
            et_groupSignature.setText("这家伙很懒，什么都没写");
        } else {
            et_groupSignature.setText(descn);
        }
        if (b_name == null || b_name.equals("")) {
            et_b_name.setText("暂无备注名");
        } else {
            et_b_name.setText(b_name);
        }
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null") || imageUrl.trim().equals("")) {
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
        if (username == null || username.equals("")) {
            et_news.setText("");
        } else {
            et_news.setText("我是 " + username);
        }
    }

    private void setListener() {
        image_xiugai.setOnClickListener(new OnClickListener() {
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

        head_left_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lin_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                et_news.setText("");
            }
        });

        tv_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String news = et_news.getText().toString().trim();
                if (news == null || news.equals("")) {
                    ToastUtils.show_always(context, "请输入验证信息");
                } else {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        //发送验证请求
                        dialog = DialogUtils.Dialogph(context, "申请中");
                        send();
                    } else {
                        ToastUtils.show_always(getApplicationContext(), "网络连接失败，请稍后重试");
                    }
                }
            }
        });
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("BeInvitedUserId", id);
            jsonObject.put("InviteMsg", et_news.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.sendInviteUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "验证发送成功，等待好友审核");
                    } else if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    } else if (ReturnType != null && ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    } else if (ReturnType != null && ReturnType.equals("200")) {
                        ToastUtils.show_always(context, "您未登录 ");
                    } else if (ReturnType != null && ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "添加好友不存在 ");
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "您已经是他好友了 ");
                    } else if (ReturnType != null && ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "对方已经邀请您为好友了，请查看 ");
                    } else if (ReturnType != null && ReturnType.equals("1006")) {
                        ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    } else if (ReturnType != null && ReturnType.equals("1007")) {
                        ToastUtils.show_always(context, "您已经添加过了 ");
                    } else {
                            ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    protected void update(final String b_name2, String groupSignature) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("GroupId", groupId);
            jsonObject.put("UpdateUserId", id);
            jsonObject.put("UserAliasName", b_name2);
            jsonObject.put("UserAliasDescn", groupSignature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updategroupFriendnewsUrl, groupSignature, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011") || ReturnType.equals("10012")) {
                            et_b_name.setText(b_name2);
                            // 保存通讯录是否刷新的属性
                            setResult(1);
                            ToastUtils.show_always(context, "修改成功");
                        } else if (ReturnType.equals("0000")) {
                            ToastUtils.show_always(context, "无法获取相关的参数");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "用户不存在");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_always(context, "用户组不存在");
                        } else if (ReturnType.equals("10021")) {
                            ToastUtils.show_always(context, "修改用户不在组");
                        } else if (ReturnType.equals("1004")) {
                            ToastUtils.show_always(context, "无法获得被修改用户Id");
                        } else if (ReturnType.equals("10041")) {
                            ToastUtils.show_always(context, "被修改用户不在组");
                        } else if (ReturnType.equals("1005")) {
                            ToastUtils.show_always(context, "无法获得修改所需的新信息");
                        } else if (ReturnType.equals("1006")) {
                            ToastUtils.show_always(context, "修改人和被修改人不能是同一个人");
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
                // 根据返回值来对程序进行解析
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        lin_delete = null;
        et_news = null;
        tv_add = null;
        image_touxiang = null;
        tv_name = null;
        et_b_name = null;
        et_groupSignature = null;
        tv_id = null;
        head_left_btn = null;
        image_xiugai = null;
        sharedPreferences = null;
        context = null;
        setContentView(R.layout.activity_null);
    }
}
