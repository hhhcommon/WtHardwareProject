package com.wotingfm.ui.interphone.group.groupcontrol.personnews;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.alert.CallAlertFragment;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.personmore.PersonMoreFragment;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 个人详情页
 * 作者：xinlong on 2016/1/19 21:18
 * 邮箱：645700751@qq.com
 */
public class TalkPersonNewsFragment extends Fragment {
    private String name;
    private String imageUrl;
    private String id;
    private String descN;
    private String num;
    private String b_name;
    private String groupId;
    private String tag = "TALK_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";

    private TipView tipView;// 数据加载出错提示
    private RelativeLayout image_add;
    private ImageView image_xiugai;
    private ImageView image_touxiang;
    private TextView tv_name;
    private TextView tv_id;
    private Dialog dialogs;
    private EditText et_groupSignature;
    private EditText et_b_name;

    private Bitmap bmp;
    private Bitmap bmpS;

    private int viewType = -1;// == 1 时代表来自 groupMembers
    private boolean update;
    private boolean isCancelRequest;
    private UserInviteMeInside news;
    private TextView lin_head_right;
    private FragmentActivity context;
    private View rootView;
    private TalkPersonNewsFragment ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_talk_personnews, container, false);
            rootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            context = getActivity();
            ct = this;
            update = false;    // 此时修改的状态
            setView();
            handleIntent();
            setData();
            setListener();
        }
        return rootView;
    }

    private void setView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);

        image_touxiang = (ImageView) rootView.findViewById(R.id.image_portrait);
        tv_name = (TextView) rootView.findViewById(R.id.tv_name);
        et_b_name = (EditText) rootView.findViewById(R.id.et_b_name);
        et_groupSignature = (EditText) rootView.findViewById(R.id.et_groupSignature);
        tv_id = (TextView) rootView.findViewById(R.id.tv_id);

        image_add = (RelativeLayout) rootView.findViewById(R.id.imageView4);
        image_xiugai = (ImageView) rootView.findViewById(R.id.imageView3);
        et_b_name.setEnabled(false);
        et_groupSignature.setEnabled(false);
        lin_head_right = (TextView) rootView.findViewById(R.id.lin_head_right);

    }

    private void handleIntent() {
        String type = getArguments().getString("type");
        if (type == null || type.equals("")) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
        } else if (type.equals("talkoldlistfragment")) {
            GroupInfo data = (GroupInfo) getArguments().getSerializable("data");
            name = data.getName();
            imageUrl = data.getPortrait();
            id = data.getId();
            descN = data.getDescn();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("talkoldlistfragment_p")) {
            UserInfo data = (UserInfo) getArguments().getSerializable("data");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descN = data.getUserSign();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("TalkGroupNewsActivity_p")) {
            GroupInfo data = (GroupInfo) getArguments().getSerializable("data");
            groupId = getArguments().getString("id");
            name = data.getUserName();
            imageUrl = data.getPortraitBig();
            id = data.getUserId();
            descN = data.getGroupSignature();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
            viewType = 1;
        } else if (type.equals("findActivity")) {
            // 处理组邀请时进入
            UserInviteMeInside data = (UserInviteMeInside) getArguments().getSerializable("data");
            name = data.getUserName();
            imageUrl = data.getPortrait();
            id = data.getUserId();
            descN = data.getUserSign();
            num = data.getUserNum();
            b_name = data.getUserAliasName();
        } else if (type.equals("GroupMemers")) {
            UserInfo data = (UserInfo) getArguments().getSerializable("data");
            groupId = getArguments().getString("id");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descN = data.getUserSign();
            b_name = data.getUserAliasName();
            num = data.getUserNum();
            viewType = 1;
        } else {
            UserInfo data = (UserInfo) getArguments().getSerializable("data");
            name = data.getUserName();
            imageUrl = data.getPortraitMini();
            id = data.getUserId();
            descN = data.getUserSign();
            b_name = data.getUserAliasName();
            num = data.getUserNum();
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
        if (descN == null || descN.equals("")) {
            descN = "这家伙很懒，什么都没写";
            et_groupSignature.setText(descN);
        } else {
            et_groupSignature.setText(descN);
        }
        if (b_name == null || b_name.equals("")) {
            et_b_name.setText("暂无备注名");
            et_b_name.setVisibility(View.GONE);
        } else {
            et_b_name.setText(b_name);
            et_b_name.setVisibility(View.VISIBLE);
        }
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null")
                || imageUrl.trim().equals("")) {
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
        news = new UserInviteMeInside();
        news.setPortraitMini(imageUrl);
        news.setUserId(id);
        news.setUserName(name);
        news.setUserSign(descN);

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
                        if (et_groupSignature.getText().toString().trim().equals("")
                                || et_groupSignature.getText().toString().trim().equals("这家伙很懒，什么都没写")) {
                            groupSignature = " ";
                        } else {
                            groupSignature = et_groupSignature.getText().toString();
                        }
                    } else {
                        if (et_b_name.getText().toString().trim().equals("")
                                || et_b_name.getText().toString().trim().equals("暂无备注名")) {
                            biename = et_groupSignature.getText().toString().trim();
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
                    image_xiugai.setImageResource(R.mipmap.xiugai);
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
                        et_groupSignature.setEnabled(true);
                        et_groupSignature.setBackgroundColor(context.getResources().getColor(R.color.white));
                        et_groupSignature.setTextColor(context.getResources().getColor(R.color.gray));
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

        image_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                call(id);
            }
        });

        lin_head_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (news == null || news.getUserId() == null) {
                    ToastUtils.show_always(context, "个人信息有误");
                    return;
                }
                PersonMoreFragment fg = new PersonMoreFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("person", news);
                fg.setArguments(bundle);
                fg.setTargetFragment(ct, 0);
                DuiJiangActivity.open(fg);

            }
        });
    }

    public void setResult() {
        context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
        if (ChatFragment.context != null &&
                ChatFragment.interPhoneId != null && ChatFragment.interPhoneId.equals(id)) {
            // 保存通讯录是否刷新的属性
            SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
            et.putString(StringConstant.PERSONREFRESHB, "true");
            et.commit();
        }
        DuiJiangActivity.close();
    }

    protected void update(final String b_name2, String groupSignature) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        String url;
        try {
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
            VolleyRequest.RequestPost(url, groupSignature, jsonObject, new VolleyCallback() {
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
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                            et_b_name.setText(b_name2);
                            context.sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                            context.sendBroadcast(new Intent(BroadcastConstants.GROUP_DETAIL_CHANGE));
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
                            Log.v("TAG", "没有对好友信息进行修改");
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
                }

                @Override
                protected void requestError(VolleyError error) {
                    if (dialogs != null) dialogs.dismiss();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected void call(String id) {

        CallAlertFragment fg = new CallAlertFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        fg.setArguments(bundle);
        DuiJiangActivity.open(fg);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        context = null;
        name = null;
        imageUrl = null;
        id = null;
        image_add = null;
        image_xiugai = null;
        image_touxiang = null;
        tv_name = null;
        tv_id = null;
        dialogs = null;
        et_groupSignature = null;
        et_b_name = null;
        descN = null;
        num = null;
        b_name = null;
        groupId = null;
        tag = null;
    }
}
