package com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CreateQRImageHelper;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.common.manager.FileManager;
import com.wotingfm.common.manager.MyHttp;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.photocut.PhotoCutActivity;
import com.wotingfm.ui.common.qrcode.EWMShowFragment;
import com.wotingfm.ui.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.adapter.GroupTalkAdapter;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.util.FrequencyUtil;
import com.wotingfm.ui.interphone.group.groupcontrol.groupmanage.GroupManagerFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupmore.GroupMoreFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupnumdel.GroupMemberDelFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.grouppersonnews.GroupPersonNewsFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.memberadd.GroupMemberAddFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.membershow.GroupMembersFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.personnews.TalkPersonNewsFragment;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.main.DuiJiangFragment;
import com.wotingfm.ui.mine.model.UserPortaitInside;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.MyGridView;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.pickview.LoopView;
import com.wotingfm.widget.pickview.OnItemSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 群组详情页面
 * 辛龙 2016年1月21日
 */
public class GroupDetailFragment extends Fragment implements OnClickListener, OnItemClickListener, TipView.WhiteViewClick {
    private Bitmap bmp;
    private GroupInfo news;
    private GroupTalkAdapter adapter;
    private SearchTalkHistoryDao dbDao;
    private List<GroupInfo> list;
    private ArrayList<GroupInfo> lists = new ArrayList<>();
    private MessageReceivers receiver = new MessageReceivers();
    private Intent pushIntent = new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN);

    private Dialog imageDialog;// 修改群组头像对话框
    private Dialog dialog;// 加载数据对话框
    private MyGridView gridView;// 展示群组成员
    private EditText editAliasName;// 群别名
    private EditText editSignature;// 群描述
    //private TextView textIntroduce;// 群介绍
    private ImageView imageHead;// 群头像
    private ImageView imageModify;// 修改
    private ImageView imageEwm;// 二维码
    private TextView textGroupName;// 群名称
    private TextView textGroupId;// 群 ID
    private TextView textGroupNumber;// 群成员人数
    private TipView tipView;// 数据加载出错提示

    private String groupId;// ID
    private String groupName;// NAME
    private String headUrl;// HEAD
    private String groupNumber;// NUMBER
    private String groupCreator;// 群组管理员
    private String groupSignature;// 群描述
    private String groupAlias;// 别名
    private String groupType;// 群组类型
    private String groupIntroduce;// 群介绍

    private String filePath;
    private String outputFilePath;
    private String miniUri;
    private String photoCutAfterImagePath;
    private String tag = "TALK_GROUP_NEWS_VOLLEY_REQUEST_CANCEL_TAG";

    private boolean isCancelRequest;
    private boolean update;
    private final int TO_GALLERY = 5;                // 打开图库
    private final int TO_CAMERA = 6;                 // 打开系统相机
    private final int PHOTO_REQUEST_CUT = 7;         // 图片裁剪
    private final int GROUP_MORE = 20;
    private TextView textChannelOne;
    private TextView textChannelTwo;
    private int pRate = -1;
    private int pFrequency = -1;
    private Dialog frequencyDialog;
    private int screenWidth;
    private TextView tv_pinlvxuanze;
    private RelativeLayout rl_group_manager;
    private FragmentActivity context;
    private View rootView;
    private GroupDetailFragment ct;

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchTalkHistoryDao(context);
    }

    @Override
    public void onWhiteViewClick() {
        send();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_groupdetail, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            ct=this;
            // 注册广播
            IntentFilter filters = new IntentFilter();
            filters.addAction(BroadcastConstants.GROUP_DETAIL_CHANGE);
            context.registerReceiver(receiver, filters);

            initDao();
            initDialog();
            initFrequencyDialog();
            setView();
            getData();
        }
        return rootView;
    }

    // 初始化对话框
    private void initDialog() {
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_imageupload, null);
        dialog.findViewById(R.id.tv_gallery).setOnClickListener(this);
        dialog.findViewById(R.id.tv_camera).setOnClickListener(this);

        imageDialog = new Dialog(context, R.style.MyDialog);
        imageDialog.setContentView(dialog);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

    }

    // 获取上一个界面传递过来的数据
    private void getData() {
        if (getArguments() == null) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
            return;
        }
        String type = getArguments().getString("type");
        if (type == null || type.equals("")) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
            return;
        }
        switch (type) {
            case "talkoldlistfragment":// 聊天界面传过来
                GroupInfo talkListGP = (GroupInfo) getArguments().getSerializable("data");
                groupNumber = talkListGP.getGroupNum();
                groupName = talkListGP.getName();
                headUrl = talkListGP.getPortrait();
                groupId = talkListGP.getId();
                if (talkListGP.getGroupManager() == null || talkListGP.getGroupManager().equals("")) {
                    groupCreator = talkListGP.getGroupCreator();
                } else {
                    groupCreator = talkListGP.getGroupManager();
                }
                groupSignature = talkListGP.getGroupSignature();
                groupIntroduce = talkListGP.getGroupDescn();
                groupAlias = talkListGP.getGroupMyAlias();
                groupType = talkListGP.getGroupType();
                break;
            case "talkpersonfragment":// 通讯录界面传过来
                GroupInfo talkGroupInside = (GroupInfo) getArguments().getSerializable("data");
                groupName = talkGroupInside.getGroupName();
                headUrl = talkGroupInside.getGroupImg();
                groupId = talkGroupInside.getGroupId();
                if (talkGroupInside.getGroupManager() == null || talkGroupInside.getGroupManager().equals("")) {
                    groupCreator = talkGroupInside.getGroupCreator();
                } else {
                    groupCreator = talkGroupInside.getGroupManager();
                }
                groupSignature = talkGroupInside.getGroupSignature();
                groupIntroduce = talkGroupInside.getGroupMyDescn();
                groupAlias = talkGroupInside.getGroupMyAlias();
                groupNumber = talkGroupInside.getGroupNum();
                groupType = talkGroupInside.getGroupType();
                break;
            case "groupaddactivity":// 添加群组搜索结果或申请加入组成功后进入
                GroupInfo findGroupNews = (GroupInfo) getArguments().getSerializable("data");
                groupName = findGroupNews.getGroupName();
                headUrl = findGroupNews.getGroupImg();
                groupId = findGroupNews.getGroupId();
                groupNumber = findGroupNews.getGroupNum();
                if (findGroupNews.getGroupManager() == null || findGroupNews.getGroupManager().equals("")) {
                    groupCreator = findGroupNews.getGroupCreator();
                } else {
                    groupCreator = findGroupNews.getGroupManager();
                }
                groupSignature = findGroupNews.getGroupSignature();
                groupIntroduce = findGroupNews.getGroupOriDescn();
                groupAlias = findGroupNews.getGroupMyAlias();
                groupType = findGroupNews.getGroupType();
                break;
            case "findActivity":// 处理组邀请时进入
                GroupInfo groupInfo = (GroupInfo) getArguments().getSerializable("data");
                groupName = groupInfo.getGroupName();
                headUrl = groupInfo.getGroupImg();
                groupId = groupInfo.getGroupId();
                groupNumber = groupInfo.getGroupNum();
                if (groupInfo.getGroupManager() == null || groupInfo.getGroupManager().equals("")) {
                    groupCreator = groupInfo.getGroupCreator();
                } else {
                    groupCreator = groupInfo.getGroupManager();
                }
                groupSignature = groupInfo.getGroupSignature();
                groupType = groupInfo.getGroupType();
                break;
            case "CreateGroupContentActivity":// 创建群组成功时进入
                GroupInfo groupInformation = (GroupInfo) getArguments().getSerializable("news");
                headUrl = getArguments().getString("imageurl");
                groupName = groupInformation.getGroupName();
                groupId = groupInformation.getGroupId();
                groupNumber = groupInformation.getGroupNum();
                groupType = groupInformation.getGroupType();
                groupCreator = CommonUtils.getUserId(context);
                groupSignature = groupInformation.getGroupSignature();
                break;
        }
        if (groupId == null || groupId.trim().equals("")) {
            groupId = "00";// 待定 此处为没有获取到 groupId
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
        }

        setData();
    }

    // 初始化视图
    private void setView() {
        rootView.findViewById(R.id.wt_back).setOnClickListener(this);                    // 返回
        rootView.findViewById(R.id.lin_ewm).setOnClickListener(this);                    // 二维码
        rootView.findViewById(R.id.imageView4).setOnClickListener(this);                 // 群聊天
        rootView.findViewById(R.id.rl_allperson).setOnClickListener(this);               // 查看所有群成员
        rootView.findViewById(R.id.linear_channel).setOnClickListener(this);             // 频率选择
        rootView.findViewById(R.id.lin_head_right).setOnClickListener(this);             // 顶栏更多


        rl_group_manager = (RelativeLayout) rootView.findViewById(R.id.rl_group_manager);   // 群管理
        rl_group_manager.setOnClickListener(this);

        textChannelOne = (TextView) rootView.findViewById(R.id.text_channel_one);        // 频道记录TextView1
        textChannelTwo = (TextView) rootView.findViewById(R.id.text_channel_two);        // 频道记录TextView2

        tv_pinlvxuanze = (TextView) rootView.findViewById(R.id.tv_pinlvxuanze);           // 频率选择TextView

        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        imageHead = (ImageView) rootView.findViewById(R.id.image_portrait); // 群头像
        imageHead.setOnClickListener(this);

        imageModify = (ImageView) rootView.findViewById(R.id.imageView3); // 修改群组资料
        imageModify.setOnClickListener(this);


        imageEwm = (ImageView) rootView.findViewById(R.id.img_ewm);   // 二维码
        textGroupNumber = (TextView) rootView.findViewById(R.id.tv_number); // 群成员数量
        editAliasName = (EditText) rootView.findViewById(R.id.et_b_name);   // 别名
        editSignature = (EditText) rootView.findViewById(R.id.et_groupSignature);// 描述
        textGroupId = (TextView) rootView.findViewById(R.id.tv_id);         // 群号


        gridView = (MyGridView) rootView.findViewById(R.id.gridView);      // 展示群成员
        gridView.setOnItemClickListener(this);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        textGroupName = (TextView) rootView.findViewById(R.id.tv_name);// 群名
        //textIntroduce = (TextView) findViewById(R.id.et_jieshao);// 群介绍
    }

    // 数据初始化
    private void setData() {
     /*   if (groupIntroduce != null && !groupIntroduce.equals("")) {// 群介绍
            textIntroduce.setText(groupIntroduce);
        }*/
        if (groupName == null || groupName.equals("")) {// 群名称
            groupName = "我听科技";
        }
        textGroupName.setText(groupName);

        if (groupNumber != null && !groupNumber.equals("")) {// 群 ID
            String idString = "ID:" + groupNumber;
            textGroupId.setText(idString);
        }

        if (groupAlias == null || groupAlias.equals("")) {// 群别名
            groupAlias = groupName;
        }
        editAliasName.setText(groupAlias);
        if (groupSignature != null && !groupSignature.equals("")) {// 群描述
            editSignature.setText(groupSignature);
        } else {
            editSignature.setText("这家伙很懒，什么也没写");
        }
        if (headUrl == null || headUrl.equals("null") || headUrl.trim().equals("")) {// 群头像
            Bitmap bitmap = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_qz);
            imageHead.setImageBitmap(bitmap);
        } else {
            if (!headUrl.startsWith("http:")) {
                headUrl = GlobalConfig.imageurl + headUrl;
            }
            headUrl = AssembleImageUrlUtils.assembleImageUrl150(headUrl);
            Picasso.with(context).load(headUrl.replace("\\/", "/")).into(imageHead);
        }

        news = new GroupInfo();
        news.setGroupName(groupName);
        news.setGroupType(groupType);
        news.setGroupCreator(groupCreator);
        news.setGroupImg(headUrl);
        news.setGroupId(groupId);
        news.setGroupNum(groupNumber);
        news.setGroupSignature(groupSignature);
        bmp = CreateQRImageHelper.getInstance().createQRImage(2, news, null, 300, 300);// 群二维码
        if (bmp == null) {
            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }
        imageEwm.setImageBitmap(bmp);
        if (groupCreator != null && groupCreator.equals(CommonUtils.getUserId(context))) {
            tv_pinlvxuanze.setVisibility(View.VISIBLE);
            rl_group_manager.setVisibility(View.VISIBLE);
        } else {
            tv_pinlvxuanze.setVisibility(View.GONE);
            rl_group_manager.setVisibility(View.GONE);
        }
        send();
    }

    // 获取网络数据
    public void send() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "通讯中");
            sendNet();
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
        }
    }

    /**
     * 频率对话框
     */
    private void initFrequencyDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_frequency, null);
        LoopView pickProvince = (LoopView) dialog.findViewById(R.id.pick_province);
        LoopView pickCity = (LoopView) dialog.findViewById(R.id.pick_city);


        pickProvince.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                pRate = index;

            }
        });
        pickCity.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                pFrequency = index;
            }
        });
        final List<String> rateList = FrequencyUtil.getFrequency();
        final List<String> frequencyList = FrequencyUtil.getFrequencyList();

        pickProvince.setItems(rateList);

        pickCity.setItems(frequencyList);

        pickProvince.setInitPosition(3);
        pickProvince.setTextSize(15);
        pickCity.setTextSize(15);

        frequencyDialog = new Dialog(context, R.style.MyDialog);
        frequencyDialog.setContentView(dialog);
        Window window = frequencyDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
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
                int a = pRate;
                if (pFrequency == -1) {
                    textChannelOne.setText(frequencyList.get(1).trim());
                } else {
                    String rate = rateList.get(pRate);
                    if (!TextUtils.isEmpty(rate.trim())) {
                        if (rate.equals("频道一")) {
                            textChannelOne.setText(frequencyList.get(pFrequency).trim());
                        } else if (rate.equals("频道二")) {
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

    // 获取群组成员
    private void sendNet() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, tag, jsonObject, new VolleyCallback() {
            private String returnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    returnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1011")) {
                    context.sendBroadcast(pushIntent);
                    DuiJiangActivity.close();
                    ToastUtils.show_always(context, "对讲组内没有成员自动解散!");
                } else {
                    try {
                        list = new Gson().fromJson(result.getString("UserList"), new TypeToken<List<GroupInfo>>() {
                        }.getType());
                        lists.clear();
                        String numString = "(" + list.size() + ")";
                        textGroupNumber.setText(numString);
                        if (groupCreator.equals(CommonUtils.getUserId(context)) && list.size() > 6) {// 群主
                            for (int i = 0; i < 6; i++) {
                                lists.add(list.get(i));
                            }
                        } else if (!groupCreator.equals(CommonUtils.getUserId(context)) && list.size() > 7) {// 非群主
                            for (int i = 0; i < 7; i++) {
                                lists.add(list.get(i));
                            }
                        } else {
                            lists.addAll(list);
                        }
                        GroupInfo groupTalkInsideType2 = new GroupInfo();// 添加
                        groupTalkInsideType2.setType(2);
                        lists.add(groupTalkInsideType2);

                        if (groupCreator.equals(CommonUtils.getUserId(context)) && list.size() >= 2) {
                            GroupInfo groupTalkInsideType3 = new GroupInfo();// 删除
                            groupTalkInsideType3.setType(3);
                            lists.add(groupTalkInsideType3);
                        }
                        if (adapter == null) {
                            gridView.setAdapter(adapter = new GroupTalkAdapter(context, lists));
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        if (list.size() <= 0) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.IS_ERROR);
                        } else {
                            tipView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_ewm:// 二维码
                EWMShowFragment fg = new EWMShowFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 2);
                bundle.putString("image", headUrl);
                bundle.putString("news", groupIntroduce);
                bundle.putString("name", groupName);
                bundle.putSerializable("group", news);
                fg.setArguments(bundle);
                DuiJiangActivity.open(fg);
                break;
            case R.id.wt_back:// 返回
                DuiJiangActivity.close();
                break;
            case R.id.rl_allperson:// 查看所有成员
                GroupMembersFragment fg1 = new GroupMembersFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("GroupId", groupId);
                fg1.setArguments(bundle1);
                DuiJiangActivity.open(fg1);
                break;
            case R.id.imageView4:// 加入激活状态
                addGroup();
                break;
            case R.id.imageView3:// 修改
                if (update) {// 此时是修改状态需要进行以下操作
                    editAliasName.setEnabled(false);
                    editSignature.setEnabled(false);
                    editAliasName.setBackgroundColor(getResources().getColor(R.color.dinglan_orange));
                    editAliasName.setTextColor(getResources().getColor(R.color.white));
                    editSignature.setBackgroundColor(getResources().getColor(R.color.dinglan_orange));
                    editSignature.setTextColor(getResources().getColor(R.color.white));

                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.xiugai);
                    imageModify.setImageBitmap(bmp);

                    update = false;

                    String name = editAliasName.getText().toString().trim();
                    String signature = editSignature.getText().toString().trim();
                    if (name.equals(groupName) && signature.equals(groupSignature)) {
                        return;
                    }
                    groupName = name;
                    groupSignature = signature;
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在提交本次修改");
                        update(groupName, groupSignature);
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {// 此时是未编辑状态
                    if (groupCreator.equals(CommonUtils.getUserId(context))) {// 此时我是群主
                        editSignature.setEnabled(true);
                        editSignature.setBackgroundColor(getResources().getColor(R.color.white));
                        editSignature.setTextColor(getResources().getColor(R.color.gray));
                    }
                    editAliasName.setEnabled(true);
                    editAliasName.setBackgroundColor(getResources().getColor(R.color.white));
                    editAliasName.setTextColor(getResources().getColor(R.color.gray));
                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wancheng);
                    imageModify.setImageBitmap(bmp);
                    update = true;
                }
                break;
            case R.id.image_portrait:// 修改群头像
                if (groupCreator.equals(CommonUtils.getUserId(context))) {
                    imageDialog.show();
                } else {
                    ToastUtils.show_always(context, "您不是本群的管理员无法修改本群头像");
                }

                break;
            case R.id.tv_gallery:// 打开图库
                doDialogClick(0);
                imageDialog.dismiss();
                break;
            case R.id.tv_camera:// 打开系统相机
                doDialogClick(1);
                imageDialog.dismiss();
                break;
            case R.id.linear_channel:// 点击channel
                if (groupCreator.equals(CommonUtils.getUserId(context))) {
                    frequencyDialog.show();
                } else {
                    ToastUtils.show_always(context, "您不是本群的管理员，无法修改对讲频率");
                }
                break;
            case R.id.lin_head_right:
                if (!TextUtils.isEmpty(groupId)) {
                    GroupMoreFragment fg2 = new GroupMoreFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("group", news);
                    fg2.setArguments(bundle2);
                    fg2.setTargetFragment(ct, GROUP_MORE);
                    DuiJiangActivity.open(fg2);
                } else {
                    ToastUtils.show_always(context, "群信息获取失败，请检查网络，并返回上一级重试");
                }
                break;
            case R.id.rl_group_manager:
                if (!TextUtils.isEmpty(groupId)) {
                    GroupManagerFragment fg3 = new GroupManagerFragment();
                    Bundle bundle3 = new Bundle();
                    bundle3.putSerializable("group", news);
                    bundle3.putSerializable("userlist", lists);
                    fg3.setArguments(bundle3);
                    DuiJiangActivity.open(fg3);
                } else {
                    ToastUtils.show_always(context, "群信息获取失败，请检查网络，并返回上一级重试");
                }
                break;
        }
    }

    // 更改群备注及信息
    private void update(String name, String signature) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
            jsonObject.put("GroupName", name);
            jsonObject.put("GroupSignature", signature);
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
                    Log.v("ReturnType", "ReturnType -- > > " + ReturnType);

                    if (ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "已经成功修改该组信息");
                        context.sendBroadcast(pushIntent);
                    } else {
                        ToastUtils.show_always(context, "修改群组信息失败，请稍后重试!");
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

    public void addGroup() {
        if (ChatFragment.isCalling && ChatFragment.interPhoneType.equals("user")) {// 此时有对讲状态 对讲状态为个人时弹出框展示
            InterPhoneControlHelper.PersonTalkHangUp(context, InterPhoneControlHelper.bdcallid);
        }
        ChatFragment.zhiDingGroupSS(groupId);
        DuiJiangFragment.update();
    }


    protected void delete() {
        dbDao.deleteHistory(groupId);
        DuiJiangActivity.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (lists.get(position).getType() == 1) {
            if (lists.get(position).getUserId().equals(CommonUtils.getUserId(context))) {
                return;
            }
            boolean isFriend = false;
            if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                    if (lists.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                        isFriend = true;
                        break;
                    }
                }
            } else {// 不是我的好友
                isFriend = false;
            }
            if (isFriend) {
                TalkPersonNewsFragment fg = new TalkPersonNewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "TalkGroupNewsActivity_p");
                bundle.putSerializable("data", lists.get(position));
                bundle.putString("id", groupId);
                fg.setArguments(bundle);
                fg.setTargetFragment(ct, 2);
                DuiJiangActivity.open(fg);
            } else {
                GroupPersonNewsFragment fg = new GroupPersonNewsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "TalkGroupNewsActivity_p");
                bundle.putString("id", groupId);
                bundle.putSerializable("data", lists.get(position));
                fg.setArguments(bundle);
                fg.setTargetFragment(ct, 2);
                DuiJiangActivity.open(fg);
            }
        } else {
            if (lists.get(position).getType() == 2) {
                GroupMemberAddFragment fg = new GroupMemberAddFragment();
                Bundle bundle = new Bundle();
                bundle.putString("GroupId", groupId);
                fg.setArguments(bundle);
                DuiJiangActivity.open(fg);
            } else if (lists.get(position).getType() == 3) {
                GroupMemberDelFragment fg = new GroupMemberDelFragment();
                Bundle bundle = new Bundle();
                bundle.putString("GroupId", groupId);
                fg.setArguments(bundle);
                fg.setTargetFragment(ct, 2);
                DuiJiangActivity.open(fg);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {

                    lists.remove(lists.size() - 1);
                    GroupTalkAdapter adapter = new GroupTalkAdapter(context, lists);
                    gridView.setAdapter(adapter);
                    context.sendBroadcast(pushIntent);
                }
                break;
            case 2:
                if (resultCode == 1) {
                    send();
                }
                break;
            case 3:
                if (resultCode == 1) {
                    send();
                }
                break;
            case 4:
                if (resultCode == 1) {
                    send();
                }
                break;
            case TO_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.e("URI:", uri.toString());
                    int sdkVersion = Integer.valueOf(Build.VERSION.SDK);
//                    Log.d("sdkVersion:", String.valueOf(sdkVersion));
//                    Log.d("KITKAT:", String.valueOf(Build.VERSION_CODES.KITKAT));
                    String path;
                    if (sdkVersion >= 19) {
                        path = getPath_above19(context, uri);
                    } else {
                        path = getFilePath_below19(uri);
                    }
                    Log.e("path:", path);
                    startPhotoZoom(Uri.parse(path));
                }
                break;
            case TO_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    startPhotoZoom(Uri.parse(outputFilePath));
                }
                break;
            case PHOTO_REQUEST_CUT:
                if (resultCode == 1) {
                    photoCutAfterImagePath = data.getStringExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH);
                    dialog = DialogUtils.Dialogph(context, "提交中");
                    dealt();
                }
                break;
            case GROUP_MORE:
                if (resultCode == 1) {
                    context.sendBroadcast(pushIntent);
                    if (ChatFragment.context != null && ChatFragment.interPhoneId != null &&
                            ChatFragment.interPhoneId.equals(groupId)) {
                        // 保存通讯录是否刷新的属性
                        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                        et.putString(StringConstant.PERSONREFRESHB, "true");
                        if (!et.commit()) {
                            Log.w("commit", "数据 commit 失败!");
                        }
                    }
                    delete();
                }
                break;
        }
    }

    // 图片裁剪
    private void startPhotoZoom(Uri uri) {
        PhotoCutActivity fg = new PhotoCutActivity();
        Bundle bundle = new Bundle();
        bundle.putString(StringConstant.START_PHOTO_ZOOM_URI, uri.toString());
        bundle.putInt(StringConstant.START_PHOTO_ZOOM_TYPE, 1);
        fg.setArguments(bundle);
        fg.setTargetFragment(ct, PHOTO_REQUEST_CUT);
        DuiJiangActivity.open(fg);

    }

    // 拍照调用逻辑  从相册选择 which == 0  拍照 which == 1
    private void doDialogClick(int which) {
        switch (which) {
            case 0:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, TO_GALLERY);
                break;
            case 1:// 调用相机
                String savePath = FileManager.getImageSaveFilePath(context);
                FileManager.createDirectory(savePath);
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(savePath, fileName);
                Uri outputFileUri = Uri.fromFile(file);
                outputFilePath = file.getAbsolutePath();
                Intent intents = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intents.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intents, TO_CAMERA);
                break;
        }
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
                    Picasso.with(context).load(miniUri.replace("\\/", "/")).into(imageHead);
                    context.sendBroadcast(pushIntent);
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
                                    + "&PCDType=" + GlobalConfig.PCDType + "&GroupId="
                                    + groupId + "&IMEI="
                                    + PhoneMessage.imei);
                    Log.e("图片上传数据",
                            TestURI
                                    + ExtName
                                    + "&UserId=" + "&PCDType=" + GlobalConfig.PCDType + "&GroupId="
                                    + groupId);
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

    // API19 以下获取图片路径的方法
    private String getFilePath_below19(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        System.out.println("***************" + column_index);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        System.out.println("path:" + path);
        return path;
    }

    /**
     * API level 19以上才有
     * 创建项目时，我们设置了最低版本 API Level，比如我的是 10，
     * 因此，AS 检查我调用的 API 后，发现版本号不能向低版本兼容，
     * 比如我用的“DocumentsContract.isDocumentUri(context, uri)”是 Level 19 以上才有的，
     * 自然超过了10，所以提示错误。
     * 添加    @TargetApi(Build.VERSION_CODES.KITKAT)即可。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath_above19(final Context context, final Uri uri) {
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
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
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
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
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
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    // 跳转到新的 Activity
    private void startToActivity(Class toClass) {
        Intent intent = new Intent(context, toClass);
        Bundle bundle = new Bundle();
        bundle.putString("GroupId", groupId);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // 跳转到新的 Activity  带返回值
    private void startToActivity(Class toClass, int requestCode) {
        Intent intent = new Intent(context, toClass);
        Bundle bundle = new Bundle();
        bundle.putString("GroupId", groupId);
        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    class MessageReceivers extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastConstants.GROUP_DETAIL_CHANGE)) {
                send();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(receiver);
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        if (list != null) {
            list.clear();
            list = null;
        }
        lists.clear();
        lists = null;

        dialog = null;
        news = null;
        adapter = null;
        dbDao = null;
        imageDialog = null;
        imageHead = null;
        textGroupNumber = null;
        editAliasName = null;
        editSignature = null;
        textGroupId = null;
        imageModify = null;
        gridView = null;
        textGroupName = null;
        //textIntroduce = null;
    }
}
