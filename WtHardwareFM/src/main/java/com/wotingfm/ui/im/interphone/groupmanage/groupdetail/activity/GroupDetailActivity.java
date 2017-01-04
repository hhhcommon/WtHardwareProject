package com.wotingfm.ui.im.interphone.groupmanage.groupdetail.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.BaseActivity;
import com.wotingfm.ui.im.interphone.alert.CallAlertActivity;
import com.wotingfm.ui.im.interphone.chat.model.TalkListGP;
import com.wotingfm.ui.im.interphone.creategroup.frienddetails.TalkPersonNewsActivity;
import com.wotingfm.ui.im.interphone.creategroup.model.GroupRation;
import com.wotingfm.ui.im.interphone.creategroup.model.UserPortaitInside;
import com.wotingfm.ui.im.interphone.creategroup.photocut.PhotoCutActivity;
import com.wotingfm.ui.im.interphone.find.add.FriendAddActivity;
import com.wotingfm.ui.im.interphone.find.result.model.FindGroupNews;
import com.wotingfm.ui.im.interphone.groupmanage.allgroupmember.activity.AllGroupMemberActivity;
import com.wotingfm.ui.im.interphone.groupmanage.groupdetail.adapter.GroupTalkAdapter;
import com.wotingfm.ui.im.interphone.groupmanage.groupdetail.util.FrequencyUtil;
import com.wotingfm.ui.im.interphone.groupmanage.handlegroupapply.HandleGroupApplyActivity;
import com.wotingfm.ui.im.interphone.groupmanage.joingrouplist.activity.JoinGroupListActivity;
import com.wotingfm.ui.im.interphone.groupmanage.memberadd.activity.MemberAddActivity;
import com.wotingfm.ui.im.interphone.groupmanage.memberdel.MemberDelActivity;
import com.wotingfm.ui.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.ui.im.interphone.groupmanage.modifygrouppassword.ModifyGroupPasswordActivity;
import com.wotingfm.ui.im.interphone.groupmanage.transferauthority.TransferAuthorityActivity;
import com.wotingfm.ui.im.interphone.linkman.model.TalkGroupInside;
import com.wotingfm.ui.im.interphone.message.model.GroupInfo;
import com.wotingfm.ui.mine.qrcode.EWMShowActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.common.manager.FileManager;
import com.wotingfm.common.manager.MyHttp;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.L;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.MyGridView;
import com.wotingfm.widget.pickview.LoopView;
import com.wotingfm.widget.pickview.OnItemSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
public class GroupDetailActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
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
    private MyGridView gridAllPerson;                     // 展示全部成全
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
    private boolean update;                           // 标识 是否修改群资料
    private String imagelocalUrl;
    private UserInfo userInfo;
    private TextView textPinLv;
    private LoopView pickCity;
    private Dialog frequencyDialog;
    private int screenWidth;
    private int pRate=-1;
    private int pFrequency;
    private ImageView imageModify;
    private String groupNum;
    private String mAlias;
    private TextView mGroupNum;
    private Dialog headDialog;
    private Uri outputFileUri;
    private String outputFilePath;
    private String photoCutAfterImagePath;
    private int imageNum;
    private String filePath;
    private int viewSuccess;
    private String miniUri;
    private Intent pushIntent = new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN);


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
        setHeadDialog();

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
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_frequency, null);
        LoopView pickProvince = (LoopView) dialog.findViewById(R.id.pick_province);
        LoopView pickCity = (LoopView) dialog.findViewById(R.id.pick_city);


        pickProvince.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                pRate=index;

            }
        });
        pickCity.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                pFrequency=index;
            }
        });
        final List<String> rateList = FrequencyUtil.getFrequency();
        final List<String> frequencyList=FrequencyUtil.getFrequencyList();

        pickProvince.setItems(rateList);

        pickCity.setItems(frequencyList);

        pickProvince.setInitPosition(3);
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
                int a=pRate;
                if(pRate==-1){
                    textChannelOne.setText(frequencyList.get(pFrequency).trim());
                }else{
                String rate=rateList.get(pRate);
                if(!TextUtils.isEmpty(rate.trim())){
                    if(rate.equals("频道一")){
                        textChannelOne.setText(frequencyList.get(pFrequency).trim());
                    }else if(rate.equals("频道二")){
                        textChannelTwo.setText(frequencyList.get(pFrequency).trim());
                    }
                }
                }
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
                groupNum=talkListGP.getGroupNum();
                mAlias=talkListGP.getGroupMyAlias();
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
                groupNum=talkGroupInside.getGroupNum();
                mAlias=talkGroupInside.getGroupMyAlias();
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
                groupNum=findGroupNews.getGroupNum();
                mAlias=findGroupNews.getGroupMyAlias();
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
                groupNum=groupInfo.getGroupNum();
                mAlias=groupInfo.getGroupMyAlias();
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
                groupNum=news.getGroupNum();
                mAlias=news.getGroupMyAlias();
                break;
            case "CreateGroup":                 // 创建群组成功进入
                GroupRation groupRation = (GroupRation) getIntent().getSerializableExtra("data");
                name = groupRation.getGroupName();
                imageUrl = groupRation.getGroupImg();
                groupId = groupRation.getGroupId();
                signature = groupRation.getGroupSignature();
                groupType = groupRation.getGroupType();
                creator = CommonUtils.getUserId(context);
                groupNum=groupRation.getGroupNum();
                mAlias=groupRation.getGroupMyAlias();
                channelOne = groupRation.getAlternateChannel1();
                channelTwo = groupRation.getAlternateChannel2();
                if(TextUtils.isEmpty(channelOne)){
                    textChannelOne.setText(channelOne);
                }
                if(TextUtils.isEmpty(channelTwo)){
                    textChannelTwo.setText(channelTwo);
                }
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
        findViewById(R.id.imageView4).setOnClickListener(this);                 // 对讲
        findViewById(R.id.text_exit).setOnClickListener(this);                  // 退出群
        findViewById(R.id.lin_ewm).setOnClickListener(this);                    // 二维码
        findViewById(R.id.linear_channel).setOnClickListener(this);             // 频率选择
//        findViewById(R.id.auto_add).setOnClickListener(this);
//        findViewById(R.id.ewm_add).setOnClickListener(this);
//        mImgEWM = (ImageView) findViewById(R.id.img_ewm);

        mImageHead = (ImageView) findViewById(R.id.image_portrait);             // 群头像
        mImageHead.setOnClickListener(this);
        mGroupName = (EditText) findViewById(R.id.et_b_name);                   // 群名称
        mGroupSign = (EditText) findViewById(R.id.et_groupSignature);           // 群签名
        mTextNumber = (TextView) findViewById(R.id.tv_number);                  // 群成员数量
        mGroupNum = (TextView) findViewById(R.id.tv_id);                        // 群号
        imageModify=(ImageView)findViewById(R.id.imageView3);                   // 修改群资料
        imageModify.setOnClickListener(this);
        textChannelOne = (TextView) findViewById(R.id.text_channel_one);        // 频道记录TextView1
        textChannelTwo = (TextView) findViewById(R.id.text_channel_two);        // 频道记录TextView2

        relativeTransferAuthority = findViewById(R.id.rl_transferauthority);    // 移交管理员权限
        relativeTransferAuthority.setOnClickListener(this);

        relativeModifyPassword = findViewById(R.id.rl_modifygpassword);         // 修改密码
        relativeModifyPassword.setOnClickListener(this);

        relativeAddGroup = findViewById(R.id.rl_addGroup);                      // 加群消息
        relativeAddGroup.setOnClickListener(this);

        relativeVerifyGroup = findViewById(R.id.rl_vertiygroup);                // 审核消息
        relativeVerifyGroup.setOnClickListener(this);

        gridAllPerson = (MyGridView)findViewById(R.id.gridView);                 // 展示全部成全
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

        if(!TextUtils.isEmpty(groupNum)){
            mGroupNum.setText("群号:"+groupNum);
        }else{
            mGroupNum.setText("群号暂无");
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
                        relativeAddGroup.setVisibility(View.VISIBLE);
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
                /*Toast.makeText(context, "对讲", Toast.LENGTH_LONG).show();*/
                if(!TextUtils.isEmpty(groupId)){
                call(groupId);
                }
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
                Intent intent2 = new Intent(context, JoinGroupListActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("GroupId", groupId);
                bundle2.putSerializable("userlist", userList);
                intent2.putExtras(bundle2);
                startActivity(intent2);
                break;
            case R.id.imageView3:           // 修改群资料
          /*      mGroupName.setEnabled(true);
                mGroupSign.setEnabled(true);*/
                if (update) {// 此时是修改状态需要进行以下操作
                    mGroupName.setEnabled(false);
                    mGroupSign.setEnabled(false);
                    mGroupName.setBackgroundColor(getResources().getColor(R.color.dinglan_orange));
                    mGroupName.setTextColor(getResources().getColor(R.color.white));
                    mGroupSign.setBackgroundColor(getResources().getColor(R.color.dinglan_orange));
                    mGroupSign.setTextColor(getResources().getColor(R.color.white));

                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.xiugai);
                    imageModify.setImageBitmap(bmp);

                    update = false;

                    String groupName = mGroupName.getText().toString().trim();
                    String groupsignature = mGroupSign.getText().toString().trim();
                    if ( groupName.equals(name) && groupsignature.equals(signature)) {
                        return;
                    }
                    name= groupName;
                    signature= groupsignature;
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在提交本次修改");
                        update(groupName,groupsignature);
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {// 此时是未编辑状态
                    if (creator.equals(CommonUtils.getUserId(context))) {// 此时我是群主
                        mGroupName.setEnabled(true);
                        mGroupSign.setEnabled(true);
                        mGroupSign.setBackgroundColor(getResources().getColor(R.color.white));
                        mGroupSign.setTextColor(getResources().getColor(R.color.gray));
                        mGroupName.setBackgroundColor(getResources().getColor(R.color.white));
                        mGroupName.setTextColor(getResources().getColor(R.color.gray));
                    }else{
                        mGroupSign.setEnabled(true);
                        mGroupSign.setBackgroundColor(getResources().getColor(R.color.white));
                        mGroupSign.setTextColor(getResources().getColor(R.color.gray));
                    }
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wancheng);
                    imageModify.setImageBitmap(bmp);
                    update = true;
                }
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
            case R.id.linear_channel:
               if(creator.equals(CommonUtils.getUserId(context))) {
                  frequencyDialog.show();
                }else{
                    ToastUtils.show_always(context,"您不是本群的管理员，无法修改对讲频率");
                }
                break;
            case R.id.image_portrait:
                //上传群头像
                if (creator.equals(CommonUtils.getUserId(context))) {
                    headDialog.show();
                }else{
                    ToastUtils.show_always(context,"只有群管理可以修改本群头像");
                }
                break;
        }
    }

    // 对讲
    protected void call(String id) {
        Intent it = new Intent(context, CallAlertActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        it.putExtras(bundle);
        startActivity(it);
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
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath;
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    setResult(1);
                }
                finish();
                break;
            case IntegerConstant.TO_GALLERY:
                if (resultCode == RESULT_OK) {       // 照片的原始资源地址
                    Uri uri = data.getData();
                    int sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);

                    L.e("URI:", uri.toString());
                    L.d("sdkVersion:", String.valueOf(sdkVersion));
                    L.d("KITKAT:", String.valueOf(Build.VERSION_CODES.KITKAT));

                    if (sdkVersion >= 19) {  // 或者 android.os.Build.VERSION_CODES.KITKAT这个常量的值是19
                        imagePath = uri.getPath();//5.0返回图片路径 Uri.getPath is:/document/image:46，5.0以下是一个和数据库有关的索引值

                        L.e("path:", imagePath);

                        // path_above19:/storage/emulated/0/girl.jpg 这里才是获取的图片的真实路径
                        imagePath = getPathAbove19(context, uri);

                        L.v("path_above19:", imagePath);

                        imageNum = 1;
                        startPhotoZoom(Uri.parse(imagePath));
                    } else {
                        imagePath = getFilePathBelow19(uri);
                        imageNum = 1;
                        startPhotoZoom(Uri.parse(imagePath));

                        L.i("path_below19:", imagePath);
                    }
                }
                break;
            case IntegerConstant.TO_CAMARA:
                if (resultCode == Activity.RESULT_OK) {
                    imagePath = outputFilePath;
                    imageNum = 1;
                    startPhotoZoom(Uri.parse(imagePath));
                }
                break;
            case IntegerConstant.PHOTO_REQUEST_CUT:
                if (resultCode == 1) {
                    imageNum = 1;
                    photoCutAfterImagePath = data.getStringExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH);
                    mImageHead.setImageURI(Uri.parse(photoCutAfterImagePath));
                    viewSuccess = 1;
                    dialog = DialogUtils.Dialogph(context, "头像上传中");
                    dealt();
                } else {
                    Toast.makeText(context, "用户退出上传图片", Toast.LENGTH_SHORT).show();
                }
                break;
            case 100:
                if(resultCode == RESULT_OK)   {
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
                break;
        }
    }

    /*
    * 图片裁剪
    */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent(context, PhotoCutActivity.class);
        intent.putExtra(StringConstant.START_PHOTO_ZOOM_URI, uri.toString());
        intent.putExtra(StringConstant.START_PHOTO_ZOOM_TYPE, 1);
        startActivityForResult(intent, IntegerConstant.PHOTO_REQUEST_CUT);
    }


    // 图片处理
    private void dealt() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    ToastUtils.show_always(context, "群头像保存成功");
                    if (!miniUri.startsWith("http:")) {
                        miniUri = GlobalConfig.imageurl + miniUri;
                    }
                    miniUri = AssembleImageUrlUtils.assembleImageUrl150(miniUri);
                    // 正常切可用代码 已从服务器获得返回值，但是无法正常显示
                    Picasso.with(context).load(miniUri.replace("\\/", "/")).into(mImageHead);
                    sendBroadcast(pushIntent);
                } else if (msg.what == 0) {
                    ToastUtils.show_short(context, "头像保存失败，请稍后再试");
                } else if (msg.what == -1) {
                    ToastUtils.show_always(context, "头像保存异常，图片未上传成功，请重新发布");
                }
                if (dialog != null) dialog.dismiss();
            }
        };
        new Thread() {
            private UserPortaitInside UserPortait;
            private String ReturnType;

            @Override
            public void run() {
                super.run();
                Message msg = new Message();
                try {
                    filePath = photoCutAfterImagePath;
                    String ExtName = filePath.substring(filePath.lastIndexOf("."));
                    Log.i("图片", "地址" + filePath);
                    // http 协议 上传头像  FType 的值分为两种 一种为 UserP 一种为 GroupP
                    String TestURI = GlobalConfig.baseUrl + "/wt/common/upload4App.do?FType=GroupP&ExtName=";// 测试用 URI
                    String Response = MyHttp.postFile(
                            new File(filePath),
                            TestURI
                                    + ExtName
                                    + "&PCDType=" + "1" + "&GroupId="
                                    + groupId + "&IMEI="
                                    + PhoneMessage.imei);
                    Log.e("图片上传数据",
                            TestURI
                                    + ExtName
                                    + "&UserId="
                                    + CommonUtils.getUserId(getApplicationContext())
                                    + "&IMEI=" + PhoneMessage.imei);
                    Log.e("图片上传结果", Response);
                    Gson gson = new Gson();
                    Response = ImageUploadReturnUtil.getResPonse(Response);
                    UserPortait = gson.fromJson(Response, new TypeToken<UserPortaitInside>() {
                    }.getType());
                    try {
                        ReturnType = UserPortait.getReturnType();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    try {
                        miniUri = UserPortait.getGroupImg();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (ReturnType == null || ReturnType.equals("")) {
                        msg.what = 0;
                    } else {
                        if (ReturnType.equals("1001")) {
                            msg.what = 1;
                        } else {
                            msg.what = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        msg.obj = "异常" + e.getMessage();
                        Log.e("图片上传返回值异常", "" + e.getMessage());
                    } else {
                        Log.e("图片上传返回值异常", "" + e);
                        msg.obj = "异常";
                    }
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * API19以下获取图片路径的方法
     */
    private String getFilePathBelow19(Uri uri) {
        //这里开始的第二部分，获取图片的路径：低版本的是没问题的，但是sdk>19会获取不到
        String[] proj = {MediaStore.Images.Media.DATA};
        //好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        //获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        L.d("***************" + column_index);
        //将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        //最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
        String path = cursor.getString(column_index);
        L.i("path:" + path);
        return path;
    }

    // 设置群组头像
    private void setHeadDialog() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_imageupload, null);
        dialogView.findViewById(R.id.tv_gallery).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IntegerConstant.TO_GALLERY);
                headDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.tv_camera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String savePath = FileManager.getImageSaveFilePath(context);
                FileManager.createDirectory(savePath);
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(savePath, fileName);
                outputFileUri = Uri.fromFile(file);
                outputFilePath = file.getAbsolutePath();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intent, IntegerConstant.TO_CAMARA);
                headDialog.dismiss();
            }
        });

        headDialog = new Dialog(context, R.style.MyDialog);
        headDialog.setContentView(dialogView);
        headDialog.setCanceledOnTouchOutside(true);
        headDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }



    /**
     * APIlevel 19以上才有
     * 创建项目时，我们设置了最低版本API Level，比如我的是10，
     * 因此，AS检查我调用的API后，发现版本号不能向低版本兼容，
     * 比如我用的“DocumentsContract.isDocumentUri(context, uri)”是Level 19 以上才有的，
     * 自然超过了10，所以提示错误。
     * 添加    @TargetApi(Build.VERSION_CODES.KITKAT)即可。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPathAbove19(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     */
    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
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
