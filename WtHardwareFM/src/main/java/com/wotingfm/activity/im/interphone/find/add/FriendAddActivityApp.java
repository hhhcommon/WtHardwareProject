package com.wotingfm.activity.im.interphone.find.add;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.find.result.model.UserInviteMeInside;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 添加好友详情界面
 */
public class FriendAddActivityApp extends AppBaseActivity implements OnClickListener {
    private TextView tv_add;
    private SharedPreferences sharedPreferences;
    private String username;
    private TextView tv_name;
    private String url;
    private ImageView image_touxiang;
    private TextView tv_id;
    private EditText et_news;
    private UserInviteMeInside contact;
    private TextView tv_sign;
    private ImageLoader imageLoader;
    private String tag = "FRIEND_ADD_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private ImageView clearImage;
    private String type;


    @Override
    protected int setViewId() {
        return R.layout.activity_friendadds;
    }

    @Override
    protected void init() {
        setTitle("详细资料");
        imageLoader = new ImageLoader(context);
        sharedPreferences = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        username = sharedPreferences.getString(StringConstant.USERNAME, "");            //当前登录账号的姓名

        setView();        //设置界面
        setListener();    //设置监听
        handleIntent();    //适配数据

    }

    private void setView() {
        et_news = (EditText) findViewById(R.id.et_news);//验证信息输入框
        image_touxiang = (ImageView) findViewById(R.id.image_touxiang);//头像
        tv_name = (TextView) findViewById(R.id.tv_name);//姓名
        tv_id = (TextView) findViewById(R.id.tv_id);//id号
        tv_sign = (TextView) findViewById(R.id.tv_sign);
        tv_add = (TextView) findViewById(R.id.tv_add);//添加好友
        clearImage = (ImageView) findViewById(R.id.clear_image);
    }

    private void handleIntent() {
        type = this.getIntent().getStringExtra("type");
        //数据适配
        if (type == null || type.equals("")) {
            contact = (UserInviteMeInside) getIntent().getSerializableExtra("contact");
        if (contact.getUserName() == null || contact.getUserName().equals("")) {
            tv_name.setText("未知");
        } else {
            tv_name.setText(contact.getUserName());
        }
        if (contact.getUserNum() == null || contact.getUserNum().equals("")) {
            tv_id.setVisibility(View.INVISIBLE);
        } else {
            tv_id.setVisibility(View.VISIBLE);
            tv_id.setText(contact.getUserNum());
        }
        if (contact.getDescn() == null || contact.getDescn().equals("")) {
            tv_sign.setVisibility(View.INVISIBLE);
        } else {
            tv_sign.setVisibility(View.VISIBLE);
            tv_sign.setText(contact.getDescn());
        }
        if (contact.getPortraitMini() == null || contact.getPortraitMini().equals("") || contact.getPortraitMini().equals("null") || contact.getPortraitMini().trim().equals("")) {
            image_touxiang.setImageResource(R.mipmap.wt_image_tx_hy);
        } else {
            if (contact.getPortraitMini().startsWith("http:")) {
                url = contact.getPortraitMini();
            } else {
                url = GlobalConfig.imageurl + contact.getPortraitMini();
            }
            imageLoader.DisplayImage(url.replace("\\/", "/"), image_touxiang, false, false, null, null);
        }
        if (username == null || username.equals("")) {
            et_news.setText("");
        } else {
            et_news.setText("我是 " + username);
        }
        } else if (type.equals("talkoldlistfragment")) {
            UserInfo  contact = (UserInfo) this.getIntent().getSerializableExtra("data");
            if (contact.getUserName() == null || contact.getUserName().equals("")) {
                tv_name.setText("未知");
            } else {
                tv_name.setText(contact.getUserName());
            }
            if (contact.getUserNum() == null || contact.getUserNum().equals("")) {
                tv_id.setVisibility(View.INVISIBLE);
            } else {
                tv_id.setVisibility(View.VISIBLE);
                tv_id.setText(contact.getUserNum());
            }
            if (contact.getDescn() == null || contact.getDescn().equals("")) {
                tv_sign.setVisibility(View.INVISIBLE);
            } else {
                tv_sign.setVisibility(View.VISIBLE);
                tv_sign.setText(contact.getDescn());
            }
            if (contact.getPortraitMini() == null || contact.getPortraitMini().equals("") || contact.getPortraitMini().equals("null") || contact.getPortraitMini().trim().equals("")) {
                image_touxiang.setImageResource(R.mipmap.wt_image_tx_hy);
            } else {
                if (contact.getPortraitMini().startsWith("http:")) {
                    url = contact.getPortraitMini();
                } else {
                    url = GlobalConfig.imageurl + contact.getPortraitMini();
                }
                imageLoader.DisplayImage(url.replace("\\/", "/"), image_touxiang, false, false, null, null);
            }
            if (username == null || username.equals("")) {
                et_news.setText("");
            } else {
                et_news.setText("我是 " + username);
            }


        }
    }

    private void setListener() {
        tv_add.setOnClickListener(this);
        clearImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
		case R.id.clear_image://验证信息清空
			et_news.setText("");
			break;
            case R.id.tv_add://点击申请添加按钮
                String news = et_news.getText().toString().trim();
                if (news.equals("")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "请输入验证信息");
                } else {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        //发送验证请求
                        DialogUtils.showDialog(context);
                        sendRequest();
                    } else {
                        ToastUtils.show_allways(getApplicationContext(), "网络连接失败，请稍后重试");
                    }
                }
                break;
        }
    }

    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.sendInviteUrl, tag, setParam(), new VolleyCallback() {
            //			private String SessionId;
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                DialogUtils.closeDialog();
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
//					SessionId = result.getString("SessionId");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "验证发送成功，等待好友审核");
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "添加失败, 请稍后再试 ");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "添加失败, 请稍后再试 ");
                } else if (ReturnType != null && ReturnType.equals("200")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "您未登录 ");
                } else if (ReturnType != null && ReturnType.equals("0000")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "添加失败, 请稍后再试 ");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "添加好友不存在 ");
                } else if (ReturnType != null && ReturnType.equals("1004")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "您已经是他好友了 ");
                } else if (ReturnType != null && ReturnType.equals("1005")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "对方已经邀请您为好友了，请查看 ");
                } else if (ReturnType != null && ReturnType.equals("1006")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "添加失败, 请稍后再试 ");
                } else if (ReturnType != null && ReturnType.equals("1007")) {
                    ToastUtils.show_allways(FriendAddActivityApp.this, "您已经添加过了 ");
                } else {
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_allways(FriendAddActivityApp.this, Message + "");
                    } else {
                        ToastUtils.show_allways(FriendAddActivityApp.this, "添加失败, 请稍后再试 ");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("BeInvitedUserId", contact.getUserId());
            jsonObject.put("UserId", CommonUtils.getUserId(this));
            jsonObject.put("InviteMsg", et_news.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        et_news = null;
        image_touxiang = null;
        tv_name = null;
        tv_id = null;
        tv_sign = null;
        tv_add = null;
        sharedPreferences = null;
        imageLoader = null;
        context = null;
        username = null;
        url = null;
        contact = null;
        tag = null;
    }
}
