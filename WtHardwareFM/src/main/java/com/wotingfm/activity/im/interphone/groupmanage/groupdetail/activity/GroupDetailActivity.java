package com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.BaseActivity;
import com.wotingfm.activity.im.interphone.chat.model.TalkListGP;
import com.wotingfm.activity.im.interphone.creategroup.frienddetails.TalkPersonNewsActivity;
import com.wotingfm.activity.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.activity.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.activity.im.interphone.groupmanage.allgroupmember.activity.AllGroupMemberActivity;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.adapter.GroupTalkAdapter;
import com.wotingfm.activity.im.interphone.groupmanage.handlegroupapply.HandleGroupApplyActivity;
import com.wotingfm.activity.im.interphone.groupmanage.joingrouplist.activity.JoinGroupListActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberadd.activity.MemberAddActivity;
import com.wotingfm.activity.im.interphone.groupmanage.memberdel.MemberDelActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.im.interphone.groupmanage.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.activity.im.interphone.groupmanage.transferauthority.TransferAuthority;
import com.wotingfm.activity.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.activity.im.interphone.message.model.GroupInfo;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组详情
 * 作者：xinlong on 2016/4/13
 * 邮箱：645700751@qq.com
 */
public class GroupDetailActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private TextView mBack;
    private EditText mGroupName;
    private EditText mGroupSign;
    private TextView mtv_talk;
    private LinearLayout lin_ewm;
    private ImageView mImgTouXiang;
    private ImageView mImgEWM;
    private TextView mtv_number;
    private GridView gv_allperson;
    private TextView mtv_autoadd;
    private TextView mtv_ewmadd;
    private TextView mtv_exit;
    private RelativeLayout rl_allperson;
    private RelativeLayout rl_transferauthority;
    private RelativeLayout rl_modifygpassword;
    private RelativeLayout rl_addGroup;
    private RelativeLayout rl_vertiygroup;
    private String groupId;
    private String imageUrl;
    private Dialog dialog;
    private String tag = "TALKGROUPNEWS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private List<UserInfo> list;
    private ArrayList<UserInfo> userlist = new ArrayList<UserInfo>();

    private Boolean IsCreator = false;
    private Dialog confirmDialog;
    private ImageView mimg_update;
    private boolean IsUpaData = false;
    private MessageReceivers Receiver;
    private GroupTalkAdapter adapter;
    private String type;
    private String number;
    private String creator;
    private String signature;
    private String myAlias;
    private String name;
    private String groupDesc;
    private String groupType;
    private FindGroupNews news;
    private String url12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_groupdetail);
        handleIntent();// 处理其他页面传入的数据
        setView();// 设置界面
        setData();//设置data View
        setListener();// 设置监听
        InitConfirmDialog();//初始化确认对话框

        if (Receiver == null) {
            Receiver = new MessageReceivers();
            IntentFilter filters = new IntentFilter();
            filters.addAction(BroadcastConstant.REFRESH_GROUP);
            context.registerReceiver(Receiver, filters);
        }

        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            if (groupId != null) {
                dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
                send();
            } else {
                ToastUtils.show_always(context, "获取组ID失败");
            }
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }

    }

    private void setData() {
        if (name == null || name.equals("")) {
            name = "我听科技";
            mGroupName.setText(name);
        } else {
            mGroupName.setText(name);
        }

        if (signature == null || signature.equals("")) {
            mGroupSign.setText(name);
        } else {
            mGroupSign.setText(signature);
        }

        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
            mImgTouXiang.setImageBitmap(bmp);
        } else {
            if (imageUrl.startsWith("http:")) {
                url12 = imageUrl;
            } else {
                url12 = GlobalConfig.imageurl + imageUrl;
            }
            Picasso.with(context).load(url12.replace("\\/", "/")).resize(100, 100).centerCrop().into(mImgTouXiang);
        }
        news = new FindGroupNews();
        news.setGroupName(name);
        news.setGroupType(groupType);
        news.setGroupCreator(creator);
        news.setGroupImg(imageUrl);
        news.setGroupId(groupId);
        news.setGroupNum(number);

        if (creator != null && !creator.equals("")) {
            if (creator.equals(CommonUtils.getUserId(context))) {
                //自己是群主
                IsCreator = true;
                if (groupType != null && !groupType.equals("")) {
                    if (groupType.equals("0")) {
                        //审核群
                        // 审核消息
                        rl_modifygpassword.setVisibility(View.VISIBLE);
                        rl_addGroup.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);
                    } else if (groupType.equals("2")) {
                        //密码群
                        // 加群消息 lin_jiaqun
                        rl_addGroup.setVisibility(View.GONE);
                        rl_modifygpassword.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);
                    } else {
                        //公开群
                        rl_addGroup.setVisibility(View.GONE);
                        rl_modifygpassword.setVisibility(View.GONE);
                        rl_transferauthority.setVisibility(View.VISIBLE);
                        rl_vertiygroup.setVisibility(View.GONE);
                    }
                } else {
                    IsCreator = false;
                    rl_addGroup.setVisibility(View.GONE);
                    rl_modifygpassword.setVisibility(View.GONE);
                    rl_transferauthority.setVisibility(View.GONE);
                    rl_vertiygroup.setVisibility(View.GONE);
                    mtv_exit.setVisibility(View.GONE);
                }
            } else {
                IsCreator = false;
                rl_addGroup.setVisibility(View.GONE);
                rl_modifygpassword.setVisibility(View.GONE);
                rl_transferauthority.setVisibility(View.GONE);
                rl_vertiygroup.setVisibility(View.GONE);

            }
        }
    }

    private void InitConfirmDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        confirmDialog = new Dialog(this, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    confirmDialog.dismiss();
                    SendExitRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
            }
        });
    }

    // 获取群成员列表
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String srcList = result.getString("UserList");
                    list = new Gson().fromJson(srcList, new TypeToken<List<UserInfo>>() {
                    }.getType());
                    if (list == null || list.size() == 0) {
                        ToastUtils.show_always(context, "您当前没有数据");
                        context.sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));
                    } else {
                        //处理组装数据 判断list和Creater大小进行组装
                        //如果是管理员 判断list是否>3 大于3出现删除按钮 如果list>6截取前六条添加增加删除按钮
                        int sum = -1;
                        sum = list.size();
                        if (sum != -1) {
                            mtv_number.setText("(" + sum + ")");
                        }
                        UserInfo add = new UserInfo();
                        add.setType(2);
                        UserInfo del = new UserInfo();
                        del.setType(3);
                        userlist.clear();
                        if (IsCreator) {
                            if (list.size() > 0 && list.size() < 3) {
                                list.add(add);
                                userlist.addAll(list);
                            } else if (list.size() > 3 && list.size() < 7) {
                                list.add(add);
                                list.add(del);
                                userlist.addAll(list);
                            } else if (list.size() >= 7) {
                                for (int i = 0; i < 6; i++) {
                                    userlist.add(list.get(i));
                                }
                                userlist.add(add);
                                userlist.add(del);
                            }
                        } else {
                            //如果不是管理员 判断list是否大于8 大于8取前7条 添加添加按钮
                            if (list.size() > 7) {
                                for (int i = 0; i < 7; i++) {
                                    userlist.add(list.get(i));
                                }
                                userlist.add(add);
                            } else {
                                list.add(add);
                                userlist.addAll(list);
                            }
                        }
                        if (adapter == null) {
                            adapter = new GroupTalkAdapter(context, userlist);
                            gv_allperson.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        setItemListener();
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

    private void setItemListener() {
        gv_allperson.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (userlist.get(position).getType() == 1) {
                    if (userlist.get(position).getUserId().equals(CommonUtils.getUserId(context))) {
                        /*	ToastUtil.show_allways(context, "点击的是本人");*/
                    } else {
                        boolean isFriend = false;
                        if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                            for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                                if (userlist.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                                    isFriend = true;
                                    break;
                                }
                            }
                        } else {
                            // 不是我的好友
                            isFriend = false;
                        }
                        if (isFriend) {
                            Intent intent = new Intent(context, TalkPersonNewsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "TalkGroupNewsActivity_p");
                            bundle.putString("id", groupId);
                            bundle.putSerializable("data", userlist.get(position));
                            intent.putExtras(bundle);
                            startActivityForResult(intent, 2);
                            ToastUtils.show_always(context, "是好友，跳转到好友页面");
                        } else {
                            Intent intent = new Intent(context, FriendAddActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "TalkGroupNewsActivity_p");
                            bundle.putSerializable("data", userlist.get(position));
                            bundle.putString("id", groupId);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, 2);
                            ToastUtils.show_always(context, "非好友跳转到群陌生人");
                        }
                    }
                } else if (userlist.get(position).getType() == 2) {
                    Intent intent = new Intent(context, MemberAddActivity.class);
                    intent.putExtra("GroupId", groupId);
                    startActivityForResult(intent, 2);
            /*        ToastUtils.show_allways(context,"加");*/
                } else if (userlist.get(position).getType() == 3) {
                    Intent intent = new Intent(context, MemberDelActivity.class);
                    intent.putExtra("GroupId", groupId);
                    startActivityForResult(intent, 3);
                  /*  ToastUtils.show_allways(context,"减");*/
                }
            }
        });
    }

    //处理从通讯录传入的值
    private void handleIntent() {
        type = this.getIntent().getStringExtra("type");
        if (type == null || type.equals("")) {
        } else if (type.equals("talkoldlistfragment")) {
            // 聊天界面传过来
            TalkListGP data = (TalkListGP) this.getIntent().getSerializableExtra("data");
            number = data.getGroupNum();
            name = data.getName();
            imageUrl = data.getPortrait();
            groupId = data.getId();
            if (data.getGroupManager() == null || data.getGroupManager().equals("")) {
                creator = data.getGroupCreator();
            } else {
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupDesc = data.getGroupDesc();
            myAlias = data.getGroupMyAlias();
            groupType = data.getGroupType();
        } else if (type.equals("talkpersonfragment")) {
            // 通讯录界面传过来
            TalkGroupInside data = (TalkGroupInside) this.getIntent().getSerializableExtra("data");
            name = data.getGroupName();
            imageUrl = data.getGroupImg();
            groupId = data.getGroupId();
            if (data.getGroupManager() == null || data.getGroupManager().equals("")) {
                creator = data.getGroupCreator();
            } else {
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupDesc = data.getGroupMyDesc();
            myAlias = data.getGroupMyAlias();
            number = data.getGroupNum();
            groupType = data.getGroupType();
        } else if (type.equals("groupaddactivity")) {
            // 申请加入组成功后进入
            FindGroupNews data = (FindGroupNews) this.getIntent().getSerializableExtra("data");
            name = data.getGroupName();
            imageUrl = data.getGroupImg();
            groupId = data.getGroupId();
            number = data.getGroupNum();
            if (data.getGroupManager() == null || data.getGroupManager().equals("")) {
                creator = data.getGroupCreator();
            } else {
                creator = data.getGroupManager();
            }
            signature = data.getGroupSignature();
            groupDesc = data.getGroupOriDesc();
            myAlias = data.getGroupMyAlias();
            groupType = data.getGroupType();
        } else if (type.equals("findActivity")) {
            // 处理组邀请时进入
            GroupInfo f = (GroupInfo) this.getIntent().getSerializableExtra("data");
            name = f.getGroupName();
            imageUrl = f.getGroupImg();
            groupId = f.getGroupId();
            number = f.getGroupNum();
            if (f.getGroupManager() == null || f.getGroupManager().equals("")) {
                creator = f.getGroupCreator();
            } else {
                creator = f.getGroupManager();
            }
            signature = f.getGroupSignature();
            groupType = f.getGroupType();
            /* myAlias=f.get; */
        } else if (type.equals("FindNewsResultActivity")) {
            // 处理组邀请时进入
            FindGroupNews news = (FindGroupNews) this.getIntent().getSerializableExtra("contact");
            imageUrl = news.getGroupImg();
            name = news.getGroupName();
            groupId = news.getGroupId();
            number = news.getGroupNum();
            groupType = news.getGroupType();
            creator = CommonUtils.getUserId(context);
            signature = news.getGroupSignature();
        }
        // 用于查找群内成员
        if (groupId == null || groupId.trim().equals("")) {
            groupId = "00";// 待定，此处为没有获取到groupid
        }
    }

    //设置监听
    private void setListener() {
        mBack.setOnClickListener(this);
        mtv_talk.setOnClickListener(this);
        lin_ewm.setOnClickListener(this);
        rl_allperson.setOnClickListener(this);
        mtv_autoadd.setOnClickListener(this);
        mtv_ewmadd.setOnClickListener(this);
        mtv_exit.setOnClickListener(this);
        rl_transferauthority.setOnClickListener(this);
        rl_modifygpassword.setOnClickListener(this);
        rl_addGroup.setOnClickListener(this);
        rl_vertiygroup.setOnClickListener(this);
        mimg_update.setOnClickListener(this);
    }

    //设置界面
    private void setView() {
        mBack = (TextView) findViewById(R.id.wt_back);
        mImgTouXiang = (ImageView) findViewById(R.id.image_portrait);
        mGroupName = (EditText) findViewById(R.id.et_group_name);
        mGroupSign = (EditText) findViewById(R.id.et_group_sign);
        mtv_talk = (TextView) findViewById(R.id.starttalk);
        lin_ewm = (LinearLayout) findViewById(R.id.lin_ewm);
        mImgEWM = (ImageView) findViewById(R.id.img_ewm);
        rl_allperson = (RelativeLayout) findViewById(R.id.rl_allperson);
        rl_transferauthority = (RelativeLayout) findViewById(R.id.rl_transferauthority);
        rl_modifygpassword = (RelativeLayout) findViewById(R.id.rl_modifygpassword);
        rl_addGroup = (RelativeLayout) findViewById(R.id.rl_addGroup);
        rl_vertiygroup = (RelativeLayout) findViewById(R.id.rl_vertiygroup);
        mtv_number = (TextView) findViewById(R.id.tv_number);
        gv_allperson = (GridView) findViewById(R.id.gridView);
        mtv_autoadd = (TextView) findViewById(R.id.auto_add);
        mtv_ewmadd = (TextView) findViewById(R.id.ewm_add);
        mtv_exit = (TextView) findViewById(R.id.tv_exit);
        mimg_update = (ImageView) findViewById(R.id.img_update);//修改群资料
        //初始化界面布局
        mGroupName.setEnabled(false);
        mGroupSign.setEnabled(false);
        //取消selector
        gv_allperson.setSelector(new ColorDrawable(Color.TRANSPARENT));            // 取消GridView中Item选中时默
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:
                finish();
                break;
            case R.id.starttalk:
                Toast.makeText(context, "对讲", Toast.LENGTH_LONG).show();
                break;
            case R.id.rl_allperson:
                Intent intent = new Intent(this, AllGroupMemberActivity.class);
                //此处测试
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.lin_ewm:
                Toast.makeText(context, "R.id.lin_ewm", Toast.LENGTH_LONG).show();
                break;
            case R.id.auto_add:
                Toast.makeText(context, "R.id.auto_add", Toast.LENGTH_LONG).show();
                break;
            case R.id.ewm_add:
                Toast.makeText(context, "R.id.ewm_add", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_exit:
                confirmDialog.show();
                break;
            case R.id.rl_transferauthority:
                Intent intent1 = new Intent(this, TransferAuthority.class);
                intent1.putExtra("GroupId", groupId);
                startActivity(intent1);
                break;
            case R.id.rl_modifygpassword:
                Intent intent2 = new Intent(this, ModifyGroupPasswordActivity.class);
                intent2.putExtra("GroupId", groupId);
                startActivity(intent2);
                break;
            case R.id.rl_addGroup:
                Intent intent3 = new Intent(this, HandleGroupApplyActivity.class);
                intent3.putExtra("GroupId", groupId);
                startActivity(intent3);
                break;
            case R.id.rl_vertiygroup:
                Intent intent4 = new Intent(this, JoinGroupListActivity.class);
                intent4.putExtra("GroupId", groupId);
                startActivity(intent4);
                break;
            case R.id.img_update:
                if (!IsUpaData) {
                    mGroupName.setEnabled(true);
                    mGroupSign.setEnabled(true);
                    IsUpaData = true;
                } else {
                    String GroupName = mGroupName.getText().toString().trim();
                    String GroupSign = mGroupSign.getText().toString().trim();
                    if (GroupName == null && GroupName.equals("")) {
                        GroupName = "";
                    }
                    if (GroupSign == null && GroupName.equals("")) {
                        GroupSign = "";
                    }
                  /*  if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在提交本次修改");*/
                    update(GroupName, GroupSign);

                    mGroupName.setEnabled(false);/* } else {
                        ToastUtils.show_allways(context, "网络失败，请检查网络");
                    }*/
                    mGroupSign.setEnabled(false);
                    IsUpaData = false;
                }
                break;
        }
    }

    // 更改群备注及信息
    private void update(String GroupName, String GroupSign) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 公共请求属性
            jsonObject.put("GroupId", groupId);
            jsonObject.put("GroupName", GroupName);
            jsonObject.put("GroupSignature", GroupSign);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.UpdateGroupInfoUrl, tag, jsonObject, new VolleyCallback() {

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && !ReturnType.equals("")) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                            sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));
                        } else {
                            if (ReturnType.equals("0000")) {
                                ToastUtils.show_always(context, "无法获取相关的参数");
                            } else if (ReturnType.equals("1000")) {
                                ToastUtils.show_always(context, "无法获取用户组id");
                            } else if (ReturnType.equals("1101")) {
                                ToastUtils.show_always(context, "成功返回已经在用户组");
                            } else if (ReturnType.equals("1002")) {
                                ToastUtils.show_always(context, "用户不存在");
                            } else if (ReturnType.equals("1003")) {
                                ToastUtils.show_always(context, "用户组不存在");
                            } else if (ReturnType.equals("1011")) {
                                ToastUtils.show_always(context, "用户不在改组，无法删除");
                            } else if (ReturnType.equals("T")) {
                                ToastUtils.show_always(context, "异常返回值");
                            } else {
                                ToastUtils.show_always(context, "消息异常");
                            }
                        }
                    } else {
                        ToastUtils.show_always(context, "ReturnType不能为空");
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

    // 退出群组
    private void SendExitRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.ExitGroupurl, tag, jsonObject, new VolleyCallback() {

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && !ReturnType.equals("")) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                            ToastUtils.show_always(context, "已经成功退出该组");
                            sendBroadcast(new Intent(BroadcastConstant.PUSH_REFRESH_LINKMAN));
                            SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                            et.putString(StringConstant.PERSONREFRESHB, "true");
                            et.commit();
                            finish();
                        } else {
                            if (ReturnType.equals("0000")) {
                                ToastUtils.show_always(context, "无法获取相关的参数");
                            } else if (ReturnType.equals("1000")) {
                                ToastUtils.show_always(context, "无法获取用户组id");
                            } else if (ReturnType.equals("1101")) {
                                ToastUtils.show_always(context, "成功返回已经在用户组");
                            } else if (ReturnType.equals("1002")) {
                                ToastUtils.show_always(context, "用户不存在");
                            } else if (ReturnType.equals("1003")) {
                                ToastUtils.show_always(context, "用户组不存在");
                            } else if (ReturnType.equals("1011")) {
                                ToastUtils.show_always(context, "用户不在改组，无法删除");
                            } else if (ReturnType.equals("T")) {
                                ToastUtils.show_always(context, "异常返回值");
                            }
                        }
                    } else {
                        ToastUtils.show_always(context, "ReturnType不能为空");
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

    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstant.REFRESH_GROUP)) {
                send();
            /*    ToastUtils.show_allways(context,"收到了群员信息变化的广播");*/
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mBack = null;
        mImgTouXiang = null;
        mGroupName = null;
        mGroupSign = null;
        mtv_talk = null;
        lin_ewm = null;
        mImgEWM = null;
        rl_allperson = null;
        rl_transferauthority = null;
        rl_modifygpassword = null;
        rl_addGroup = null;
        rl_vertiygroup = null;
        mtv_number = null;
        gv_allperson = null;
        mtv_autoadd = null;
        mtv_ewmadd = null;
        mtv_exit = null;
        unregisterReceiver(Receiver);
    }
}
