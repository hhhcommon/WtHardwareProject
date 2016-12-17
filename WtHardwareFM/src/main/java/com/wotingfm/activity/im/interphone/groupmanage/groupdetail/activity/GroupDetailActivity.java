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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.wotingfm.activity.im.interphone.creategroup.model.GroupRation;
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
import com.wotingfm.activity.im.interphone.groupmanage.transferauthority.TransferAuthorityActivity;
import com.wotingfm.activity.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.activity.im.interphone.message.model.GroupInfo;
import com.wotingfm.activity.mine.qrcode.EWMShowActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.pickview.LoopView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组详情
 * 以下界面可跳转到这界面
 * 1、搜索群组结果               - > FindNewsResultActivity
 * 2、通讯录                     - > LinkManFragment
 * 3、聊天界面                   - > ChatFragment
 * 4、创建群组成功后直接进入     - > CreateGroupItemActivity
 * 5、申请加入成功后直接进入     - > GroupAddActivity
 *
 * 作者：xinlong on 2016/4/13
 * 邮箱：645700751@qq.com
 */
public class GroupDetailActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private MessageReceivers receiver;
    private GroupTalkAdapter adapter;
    private List<UserInfo> list;
    private ArrayList<UserInfo> userList = new ArrayList<>();

    private Dialog confirmDialog;                       // 退出群确认对话框
    private Dialog dialog;                              // 加载数据对话框
    private EditText mGroupName;                        // 群名称
    private EditText mGroupSign;                        // 群签名
    private TextView mTextNumber;                       // 群成员人数
    private TextView textChannelOne;                    // 设备备用频道一
    private TextView textChannelTwo;                    // 设备备用频道二
    private GridView gridAllPerson;                     // 展示全部成全
    private ImageView mImageHead;                       // 头像
//    private ImageView mImgEWM;

    private View relativeTransferAuthority;             // 移交管理员权限
    private View relativeModifyPassword;                // 修改密码
    private View relativeAddGroup;                      // 加群消息
    private View relativeVerifyGroup;                   // 审核消息

    
    private String groupId;                             // 群 ID
    private String imageUrl;                            // 群头像 URL
    private String creator;                             // 群组
    private String signature;                           // 群签名
    private String name;                                // 群名称
    private String groupType;                           // 群类型 0 -> 审核群  1 -> 公开群  2 -> 密码群
    private String channelOne = "CH01-409.7500";
    private String channelTwo = "CH02-409.7625";
    private String tag = "TALK_GROUP_NEWS_VOLLEY_REQUEST_CANCEL_TAG";

    private boolean isCancelRequest;
    private boolean isCreator;                          // 标识群组 true -> 是群主
    private boolean isUpdate;                           // 标识 是否修改群资料
    private String imagelocalUrl;
    private UserInfo userInfo;
    private TextView textPinLv;
    private LoopView pickCity;
    private Dialog frequencyDialog;
    private int screenWidth;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (userList.get(position).getType() == 1) {
            if (!userList.get(position).getUserId().equals(CommonUtils.getUserId(context))) {
                boolean isFriend = false;
                if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                    for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                        if (userList.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                            isFriend = true;
                            break;
                        }
                    }
                } else {
                    isFriend = false;// 不是我的好友
                }
                Intent intent;
                if (isFriend) {
                    intent = new Intent(context, TalkPersonNewsActivity.class);
                    ToastUtils.show_always(context, "是好友，跳转到好友页面");
                } else {
                    intent = new Intent(context, FriendAddActivity.class);
                    ToastUtils.show_always(context, "非好友跳转到群陌生人");
                }
                Bundle bundle = new Bundle();
                bundle.putString("type", "TalkGroupNewsActivity");
                bundle.putString("id", groupId);
                bundle.putSerializable("data", userList.get(position));
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
            }
        } else if (userList.get(position).getType() == 2) {
            Intent intent = new Intent(context, MemberAddActivity.class);
            intent.putExtra("GroupId", groupId);
            startActivityForResult(intent, 2);
        } else if (userList.get(position).getType() == 3) {
            Intent intent = new Intent(context, MemberDelActivity.class);
            intent.putExtra("GroupId", groupId);
            startActivityForResult(intent, 3);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupdetail);

        // 注册广播  用于获取成员列表
        if (receiver == null) {
            receiver = new MessageReceivers();
            IntentFilter filters = new IntentFilter();
            filters.addAction(BroadcastConstants.REFRESH_GROUP);
            context.registerReceiver(receiver, filters);
        }

        handleIntent();     // 处理其他页面传入的数据
        setView();          // 设置界面
        initConfirmDialog();// 初始化确认对话框
        initFrequencyDialog();

        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取群成员信息");
            send();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    /**
     *频率对话框
     */
    private void initFrequencyDialog() {
     /*   final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_frequency, null);
        LoopView pickProvince = (LoopView) dialog.findViewById(R.id.pick_province);
        pickCity = (LoopView) dialog.findViewById(R.id.pick_city);

      *//*  pickProvince.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                provinceIndex = index;
                List<String> tempList1 = positionMap.get(provinceList.get(index));
                pickCity.setItems(tempList1);
                pickCity.setInitPosition(0);
            }
        });
        pickCity.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                cityIndex = index;
            }
        });
        pickProvince.setItems(provinceList);
        List<String> tempList = positionMap.get(provinceList.get(4));*//*

        pickCity.setItems(tempList);

        pickProvince.setInitPosition(4);
        pickProvince.setTextSize(15);
        pickCity.setTextSize(15);

        frequencyDialog = new Dialog(context, R.style.MyDialog);
        frequencyDialog.setContentView(dialog);
        Window window = frequencyDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = screenWidth;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        frequencyDialog.setCanceledOnTouchOutside(true);
        frequencyDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

        dialog.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frequencyDialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frequencyDialog.isShowing()) {
                    frequencyDialog.dismiss();
                }
            }
        });

*/


    }

    // 处理从其他界面传入的值
    private void handleIntent() {
        String type = getIntent().getStringExtra("type");
        if (type == null || type.equals("")) {
            return;
        }
        switch (type) {
            case "talkoldlistfragment":         // 聊天界面传过来
                TalkListGP talkListGP = (TalkListGP) getIntent().getSerializableExtra("data");
                name = talkListGP.getName();
                imageUrl = talkListGP.getPortrait();
                groupId = talkListGP.getId();
                signature = talkListGP.getGroupSignature();
                groupType = talkListGP.getGroupType();
                if (talkListGP.getGroupManager() == null || talkListGP.getGroupManager().equals("")) {
                    creator = talkListGP.getGroupCreator();
                } else {
                    creator = talkListGP.getGroupManager();
                }
                break;
            case "talkpersonfragment":          // 通讯录界面传过来
                TalkGroupInside talkGroupInside = (TalkGroupInside) getIntent().getSerializableExtra("data");
                name = talkGroupInside.getGroupName();
                imageUrl = talkGroupInside.getGroupImg();
                groupId = talkGroupInside.getGroupId();
                signature = talkGroupInside.getGroupSignature();
                groupType = talkGroupInside.getGroupType();
                if (talkGroupInside.getGroupManager() == null || talkGroupInside.getGroupManager().equals("")) {
                    creator = talkGroupInside.getGroupCreator();
                } else {
                    creator = talkGroupInside.getGroupManager();
                }
                break;
            case "groupaddactivity":            // 申请加入组成功后进入
                FindGroupNews findGroupNews = (FindGroupNews) getIntent().getSerializableExtra("data");
                name = findGroupNews.getGroupName();
                imageUrl = findGroupNews.getGroupImg();
                groupId = findGroupNews.getGroupId();
                signature = findGroupNews.getGroupSignature();
                groupType = findGroupNews.getGroupType();
                if (findGroupNews.getGroupManager() == null || findGroupNews.getGroupManager().equals("")) {
                    creator = findGroupNews.getGroupCreator();
                } else {
                    creator = findGroupNews.getGroupManager();
                }
                break;
            case "findActivity":                // 处理组邀请时进入
                GroupInfo groupInfo = (GroupInfo) getIntent().getSerializableExtra("data");
                name = groupInfo.getGroupName();
                imageUrl = groupInfo.getGroupImg();
                groupId = groupInfo.getGroupId();
                signature = groupInfo.getGroupSignature();
                groupType = groupInfo.getGroupType();
                if (groupInfo.getGroupManager() == null || groupInfo.getGroupManager().equals("")) {
                    creator = groupInfo.getGroupCreator();
                } else {
                    creator = groupInfo.getGroupManager();
                }
                break;
            case "FindNewsResultActivity":      // 处理组邀请时进入
                FindGroupNews news = (FindGroupNews) getIntent().getSerializableExtra("data");
                name = news.getGroupName();
                imageUrl = news.getGroupImg();
                groupId = news.getGroupId();
                signature = news.getGroupSignature();
                groupType = news.getGroupType();
                creator = CommonUtils.getUserId(context);
                break;
            case "CreateGroup":                 // 创建群组成功进入
                GroupRation groupRation = (GroupRation) getIntent().getSerializableExtra("data");
                name = groupRation.getGroupName();
                imageUrl = groupRation.getGroupImg();
                groupId = groupRation.getGroupId();
                signature = groupRation.getGroupSignature();
                groupType = groupRation.getGroupType();
                creator = CommonUtils.getUserId(context);
                channelOne = groupRation.getAlternateChannel1();
                channelTwo = groupRation.getAlternateChannel2();
                if(imageUrl==null||imageUrl.equals("")){
                 imagelocalUrl=getIntent().getStringExtra("imageLocal");
                }
                break;
        }
        userInfo=new UserInfo();
        userInfo.setGroupName(name);
        userInfo.setGroupId(groupId);
        userInfo.setGroupCreator(creator);
        userInfo.setGroupType(groupType);
        userInfo.setGroupCreator(creator);
        userInfo.setPortraitMini(imageUrl);

        // 用于查找群内成员
        if (groupId == null || groupId.trim().equals("")) {
            groupId = "00";// 待定  此处为没有获取到 groupId
        }
        L.v("creator -- > > " + creator);
    }

    // 设置界面
    private void setView() {
        findViewById(R.id.rl_allperson).setOnClickListener(this);               // 全部成全
        findViewById(R.id.wt_back).setOnClickListener(this);                    // 返回
        findViewById(R.id.imageView4).setOnClickListener(this);                  // 对讲
        findViewById(R.id.text_exit).setOnClickListener(this);                  // 退出群
        findViewById(R.id.imageView3).setOnClickListener(this);                 // 修改群资料
        findViewById(R.id.lin_ewm).setOnClickListener(this);                    // 二维码
//        findViewById(R.id.auto_add).setOnClickListener(this);
//        findViewById(R.id.ewm_add).setOnClickListener(this);
//        mImgEWM = (ImageView) findViewById(R.id.img_ewm);

        mImageHead = (ImageView) findViewById(R.id.image_portrait);             // 群头像
        mGroupName = (EditText) findViewById(R.id.et_b_name);                   // 群名称
        mGroupSign = (EditText) findViewById(R.id.et_groupSignature);           // 群签名
        mTextNumber = (TextView) findViewById(R.id.tv_number);                  // 群成员数量

        textChannelOne = (TextView) findViewById(R.id.text_channel_one);        // 频道记录TextView1
        textChannelTwo = (TextView) findViewById(R.id.text_channel_two);        // 频道记录TextView2
        textPinLv=(TextView) findViewById(R.id.tv_pinlvxuanze);                 // 频率选择

        relativeTransferAuthority = findViewById(R.id.rl_transferauthority);    // 移交管理员权限
        relativeTransferAuthority.setOnClickListener(this);

        relativeModifyPassword = findViewById(R.id.rl_modifygpassword);         // 修改密码
        relativeModifyPassword.setOnClickListener(this);

        relativeAddGroup = findViewById(R.id.rl_addGroup);                      // 加群消息
        relativeAddGroup.setOnClickListener(this);

        relativeVerifyGroup = findViewById(R.id.rl_vertiygroup);                // 审核消息
        relativeVerifyGroup.setOnClickListener(this);

        gridAllPerson = (GridView) findViewById(R.id.gridView);                 // 展示全部成全
        gridAllPerson.setSelector(new ColorDrawable(Color.TRANSPARENT));        // 取消GridView中Item选中时默
        gridAllPerson.setOnItemClickListener(this);

        setData();
    }

    // 初始化数据
    private void setData() {
        if (name != null && !name.equals("")) {             // 设置群名称
            mGroupName.setText(name);
        }
        if (signature != null && !signature.equals("")) {   // 设置群签名
            mGroupSign.setText(signature);
        }

        // 设置群头像  群组没有设置群头像则使用系统默认的头像
        if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null") || imageUrl.trim().equals("")) {
            if(imagelocalUrl!=null&& !imagelocalUrl.equals("")){
                mImageHead.setImageURI(Uri.parse(imagelocalUrl));
            }else{
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
                mImageHead.setImageBitmap(bmp);
            }
        } else {
            String url;
            if (imageUrl.startsWith("http:")) {
                url = imageUrl;
            } else {
                url = GlobalConfig.imageurl + imageUrl;
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(mImageHead);
        }

        L.v("groupType -- > > " + groupType);

        // 根据群组和群类型初始化界面
        if (creator != null && !creator.equals("")) {
            if (creator.equals(CommonUtils.getUserId(context)) && groupType != null && !groupType.equals("")) {
                isCreator = true;
                switch (groupType) {
                    case "2":// 密码群
                        relativeModifyPassword.setVisibility(View.VISIBLE);
                        relativeTransferAuthority.setVisibility(View.VISIBLE);
                        break;
                    case "1":// 公开群
                        relativeTransferAuthority.setVisibility(View.VISIBLE);
                        break;
                    case "0":// 审核群 审核消息
                        relativeVerifyGroup.setVisibility(View.VISIBLE);
                        relativeTransferAuthority.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
            }
        }
    }

    // 初始化确认对话框
    private void initConfirmDialog() {
        final View dialog1 = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);  // 取消
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this); // 确定

        confirmDialog = new Dialog(this, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
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
                    String returnType = result.getString("ReturnType");
                    L.v("returnType -- > > " + returnType);

                    if(returnType != null && returnType.equals("1001")) {
                        list = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<UserInfo>>() {}.getType());
                        if (list == null || list.size() == 0) {
                            ToastUtils.show_always(context, "您当前没有数据");
                            sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        } else {
                            // 处理组装数据 判断 list 和 Create 大小进行组装
                            // 如果是管理员 判断 list 是否 > 3 大于 3 出现删除按钮 如果 list > 6 截取前六条添加增加删除按钮
                            int sum = list.size();
                            if (sum != -1) {
                                String sumString = "(" + sum + ")";
                                mTextNumber.setText(sumString);
                            }
                            UserInfo add = new UserInfo();
                            add.setType(2);
                            UserInfo del = new UserInfo();
                            del.setType(3);
                            userList.clear();
                            if (isCreator) {
                                if(list.size() == 1) {
                                    userList.addAll(list);
                                    userList.add(add);
                                } else if (list.size() > 1 && list.size() < 7) {
                                    userList.addAll(list);
                                    userList.add(add);
                                    userList.add(del);
                                } else if (list.size() > 6) {
                                    for (int i = 0; i < 6; i++) {
                                        userList.add(list.get(i));
                                    }
                                    userList.add(add);
                                    userList.add(del);
                                }
                            } else {// 如果不是管理员 判断 list 是否大于 8 大于 8 取前 7 条 添加添加按钮
                                if (list.size() > 7) {
                                    for (int i = 0; i < 7; i++) {
                                        userList.add(list.get(i));
                                    }
                                } else {
                                    userList.addAll(list);
                                }
                                userList.add(add);
                            }
                            if (adapter == null) {
                                gridAllPerson.setAdapter(adapter = new GroupTalkAdapter(context, userList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }else if(returnType != null && returnType.equals("1011")) {
                        ToastUtils.show_always(context, "群组无成员，群组已自动解散!");
                        sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                        et.putString(StringConstant.PERSONREFRESHB, "true");
                        if (!et.commit()) {
                            L.v("数据 commit 失败!");
                        }
                        finish();
                    } else {
                        ToastUtils.show_always(context, "获取群组成员失败，请重试!");
                    }
                } catch (JSONException e) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wt_back:              // 返回
                finish();
                break;
            case R.id.imageView4:            // 对讲
                Toast.makeText(context, "对讲", Toast.LENGTH_LONG).show();
                break;
            case R.id.rl_allperson:         // 全部成员
                Intent intent = new Intent(this, AllGroupMemberActivity.class);
                intent.putExtra("GroupId", groupId);
                startActivity(intent);
                break;
            case R.id.lin_ewm:              // 二维码
                Intent intent1 = new Intent(context, EWMShowActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type","2");
                if (imageUrl == null || imageUrl.equals("") || imageUrl.equals("null") || imageUrl.trim().equals("")) {
                    if(imagelocalUrl!=null&& !imagelocalUrl.equals("")){
                        bundle.putString("image", imagelocalUrl);
                    }else{
                      /*  bundle.putString("image", imagelocalUrl);*/
                    }
                }else{
                    bundle.putString("image", imageUrl);
                }
                bundle.putString("news",  signature);
                bundle.putString("name", name);
                if(userInfo!=null){
                    bundle.putSerializable("group", userInfo);
                    intent1.putExtras(bundle);
                    startActivity(intent1);
                }else{
                  ToastUtils.show_always(context,"用户资料获取异常，请返回上一界面重试");
                }
                break;
//            case R.id.auto_add:
//                Toast.makeText(context, "R.id.auto_add", Toast.LENGTH_LONG).show();
//                break;
//            case R.id.ewm_add:
//                Toast.makeText(context, "R.id.ewm_add", Toast.LENGTH_LONG).show();
//                break;
            case R.id.text_exit:            // 退出群
                confirmDialog.show();
                break;
            case R.id.rl_transferauthority: // 移交管理员权限
                Intent intentTransfer = new Intent(context, TransferAuthorityActivity.class);
                intentTransfer.putExtra("GroupId", groupId);
                startActivityForResult(intentTransfer, 100);
                break;
            case R.id.rl_modifygpassword:   // 修改密码
                startToActivity(ModifyGroupPasswordActivity.class);
                break;
            case R.id.rl_addGroup:          // 加群消息
                startToActivity(HandleGroupApplyActivity.class);
                break;
            case R.id.rl_vertiygroup:       // 审核消息
                startToActivity(JoinGroupListActivity.class);
                break;
            case R.id.imageView3:           // 修改群资料
                if (!isUpdate) {
                    mGroupName.setEnabled(true);
                    mGroupSign.setEnabled(true);
                    if(isCreator) {
                    //spiner
                    }
                } else {
                    String groupName = mGroupName.getText().toString().trim();
                    String groupSign = mGroupSign.getText().toString().trim();

                    // 如果群名称或群签名有改动则提交服务器修改 否则不需要进行提交
                    if (!groupName.equals(name) || !groupSign.equals(signature)) {
                        name = groupName;
                        signature = groupSign;
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            dialog = DialogUtils.Dialogph(context, "正在提交本次修改");
                            update(name, signature);
                        } else {
                            ToastUtils.show_always(context, "网络失败，请检查网络");
                        }
                    }
                    mGroupName.setEnabled(false);
                    mGroupSign.setEnabled(false);
                    if(isCreator) {

                    }
                }
                isUpdate = !isUpdate;
                break;
            case R.id.tv_cancle:            // 取消退出群组
                confirmDialog.dismiss();
                break;
            case R.id.tv_confirm:           // 确定退出群组
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    confirmDialog.dismiss();
                    sendExitRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
                break;
        }
    }

    // 更改群备注及信息
    private void update(String GroupName, String GroupSign) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
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
                    L.v("ReturnType -- > > " + ReturnType);

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                    } else {
                        ToastUtils.show_always(context, "修改群组资料失败，请稍后重试!");
                    }
                } catch (JSONException e) {
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

    // 退出群组
    private void sendExitRequest() {
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
                    L.v("ReturnType -- > > " + ReturnType);

                    if(ReturnType == null || ReturnType.equals("")) {
                        ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
                        return ;
                    }

                    if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                        ToastUtils.show_always(context, "已经成功退出该组");
                        sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
                        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                        et.putString(StringConstant.PERSONREFRESHB, "true");
                        if (!et.commit()) {
                            L.v("数据 commit 失败!");
                        }
                        finish();
                    } else {
                        ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
                    }
                } catch (JSONException e) {
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

    // 群管理 （修改密码、审核消息、加群消息）跳转
    private void startToActivity(Class toClass) {
        Intent intent = new Intent(context, toClass);
        intent.putExtra("GroupId", groupId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100 && resultCode == RESULT_OK) {
            isCreator = false;
            relativeTransferAuthority.setVisibility(View.GONE);
            sendBroadcast(new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN));
            SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
            et.putString(StringConstant.PERSONREFRESHB, "true");
            if (!et.commit()) {
                L.v("数据 commit 失败!");
            }
            send();
        }
    }

    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastConstants.REFRESH_GROUP)) {
                send();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mImageHead = null;
        mGroupName = null;
        mGroupSign = null;
        relativeTransferAuthority = null;
        relativeModifyPassword = null;
        relativeAddGroup = null;
        relativeVerifyGroup = null;
        mTextNumber = null;
        gridAllPerson = null;
        unregisterReceiver(receiver);
    }
}
