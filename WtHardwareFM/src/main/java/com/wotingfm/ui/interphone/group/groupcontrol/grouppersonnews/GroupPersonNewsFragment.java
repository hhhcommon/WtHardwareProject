package com.wotingfm.ui.interphone.group.groupcontrol.grouppersonnews;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 组内联系人详情页(非好友)
 * 辛龙
 * 2016年1月19日
 */
public class GroupPersonNewsFragment extends Fragment {
    private String name;
    private String imageUrl;
    private String id;
    private String descN;
    private String num;
    private String b_name;
    private String groupId;
    private String username;
    private String tag = "GROUP_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private LinearLayout lin_delete;
    private ImageView image_xiugai;
    private ImageView image_touxiang;
    private TextView et_groupSignature;
    private EditText et_b_name;
    private EditText et_news;
    private Dialog dialogs;
    private Dialog dialog;
    private TextView tv_add;
    private TextView tv_name;
    private TextView tv_id;
    private SharedPreferences sharedPreferences = BSApplication.SharedPreferences;
    private boolean update;
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_group_personnews, container, false);
            rootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            context = getActivity();
            update = false;    // 此时修改的状态
            username = sharedPreferences.getString(StringConstant.NICK_NAME, "");            //当前登录账号的姓名
            setView();
            handleIntent();
            setData();
            setListener();
            createEWM();
        }
        return rootView;
    }

    private void createEWM() {
        UserInviteMeInside meInside = new UserInviteMeInside();
        meInside.setUserId(num);
        meInside.setNickName(name);
        meInside.setDescn(descN);
        meInside.setPortraitMini(imageUrl);
     /*   bmp = CreateQRImageHelper.getInstance().createQRImage(1, null, meInside, 220, 220);
        if (bmp == null) {
            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }
        img_EWM.setImageBitmap(bmp);*/
    }

    private void setView() {
        lin_delete = (LinearLayout) rootView.findViewById(R.id.lin_delete);    // 验证信息清空
        et_news = (EditText) rootView.findViewById(R.id.et_news);              // 验证信息输入框
        tv_add = (TextView) rootView.findViewById(R.id.tv_add);                // 添加好友
        image_touxiang = (ImageView) rootView.findViewById(R.id.image_portrait);
        tv_name = (TextView) rootView.findViewById(R.id.tv_name);
        et_b_name = (EditText) rootView.findViewById(R.id.et_b_name);
        et_groupSignature = (TextView) rootView.findViewById(R.id.et_groupSignature);
        tv_id = (TextView) rootView.findViewById(R.id.tv_id);
        image_xiugai = (ImageView) rootView.findViewById(R.id.imageView3);
    }

    private void handleIntent() {
        String type = getArguments().getString("type");
        groupId = getArguments().getString("id");
        if (type == null) {
            return;
        }
        if (type.equals("talkoldlistfragment_p")) {
            UserInfo data = (UserInfo) getArguments().getSerializable("data");
            name = data.getNickName();
            imageUrl = data.getPortraitBig();
            id = data.getUserId();
            descN = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("TalkGroupNewsActivity_p")) {
            GroupInfo data = (GroupInfo) getArguments().getSerializable("data");
            name = data.getNickName();
            imageUrl = data.getPortraitBig();
            id = data.getUserId();
            descN = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("GroupMemers")) {
            UserInfo data = (UserInfo) getArguments().getSerializable("data");
            name = data.getNickName();
            imageUrl = data.getPortraitBig();
            id = data.getUserId();
            descN = data.getDescn();
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
        if (descN == null || descN.equals("")) {
            et_groupSignature.setText("这家伙很懒，什么都没写");
        } else {
            et_groupSignature.setText(descN);
        }
        if (b_name == null || b_name.equals("")) {
            et_b_name.setText("暂无备注名");
        } else {
            et_b_name.setText(b_name);
        }
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            image_touxiang.setImageResource(R.mipmap.wt_image_tx_hy);
        } else {
            String url;
            if (imageUrl.startsWith("http:")) {
                url = imageUrl;
            } else {
                url = GlobalConfig.imageurl + imageUrl;
            }
            String _url = AssembleImageUrlUtils.assembleImageUrl150(url);

            // 加载图片
            AssembleImageUrlUtils.loadImage(_url, url, image_touxiang, IntegerConstant.TYPE_GROUP);
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
                        if (et_b_name.getText().toString().trim().equals("")
                                || et_b_name.getText().toString().trim().equals("暂无备注名")) {
                            biename = " ";
                        } else {
                            biename = et_b_name.getText().toString();
                        }
                    } else {
                        if (et_b_name.getText().toString().trim().equals("")
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
                    et_b_name.setBackgroundColor(context.getResources().getColor(R.color.dinglan_orange));
                    et_b_name.setTextColor(context.getResources().getColor(R.color.white));
                    image_xiugai.setImageResource(R.mipmap.xiugai);
                    update = false;
                } else {
                    // 此时是未编辑状态
                    if (id.equals(CommonUtils.getUserId(context))) {
                        // 此时是我本人
                        et_b_name.setEnabled(true);
                        et_b_name.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_b_name.setTextColor(context.getResources().getColor(R.color.gray));

                    } else {
                        // 此时我不是我本人
                        et_b_name.setEnabled(true);
                        et_b_name.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_b_name.setTextColor(context.getResources().getColor(R.color.gray));
                    }
                    image_xiugai.setImageResource(R.mipmap.wancheng);
                    update = true;
                }
            }
        });

        rootView.findViewById(R.id.head_left_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DuiJiangActivity.close();
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
                if (news.equals("")) {
                    ToastUtils.show_always(context, "请输入验证信息");
                } else {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "申请中");
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                }
            }
        });
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("BeInvitedUserId", id);
            jsonObject.put("InviteMsg", et_news.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.sendInviteUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    if (Message != null && !Message.trim().equals("")) {
                        ToastUtils.show_always(context, Message + "");
                    } else {
                        ToastUtils.show_always(context, "添加失败, 请稍后再试 ");
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    protected void update(final String b_name2, String groupSignature) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
            jsonObject.put("UpdateUserId", id);
            jsonObject.put("UserAliasName", b_name2);
            jsonObject.put("UserAliasDescn", groupSignature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.updategroupFriendnewsUrl, groupSignature, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null) {
                    if (ReturnType.equals("1001") || ReturnType.equals("10011") || ReturnType.equals("10012")) {
                        et_b_name.setText(b_name2);
                        // 保存通讯录是否刷新的属性
                        context.setResult(1);
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
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) dialogs.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    @Override
    public void onDestroy() {
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
        image_xiugai = null;
        sharedPreferences = null;
    }
}
