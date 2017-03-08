package com.wotingfm.ui.interphone.chat.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.common.service.SimulationService;
import com.wotingfm.common.service.VoiceStreamRecordService;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.model.UserInfo;
import com.wotingfm.ui.interphone.alert.CallAlertFragment;
import com.wotingfm.ui.interphone.chat.adapter.ChatListAdapter;
import com.wotingfm.ui.interphone.chat.adapter.GroupPersonAdapter;
import com.wotingfm.ui.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.ui.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.ui.interphone.common.message.MessageUtils;
import com.wotingfm.ui.interphone.common.message.MsgNormal;
import com.wotingfm.ui.interphone.common.message.content.MapContent;
import com.wotingfm.ui.interphone.common.model.ListInfo;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.util.FrequencyUtil;
import com.wotingfm.ui.interphone.group.groupcontrol.grouppersonnews.GroupPersonNewsFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.personnews.TalkPersonNewsFragment;
import com.wotingfm.ui.interphone.linkman.model.LinkMan;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.main.DuiJiangFragment;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.mine.person.login.LoginActivity;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.JsonEncloseUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.util.VibratorUtils;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

;

/**
 * 对讲机-获取联系列表，包括群组跟个人
 *
 * @author 辛龙
 *         2016年1月18日
 */
public class ChatFragment extends Fragment implements OnClickListener, TipView.TipViewClick {

    public static FragmentActivity context;
    private static ChatListAdapter adapter;
    private static String groupFreq;                                    // 组对讲频率
    private static String groupName;
    private static String groupImage;
    private MessageReceiver Receiver;
    private static SearchTalkHistoryDao dbDao;
    private static Gson gson = new Gson();
    private SharedPreferences shared = BSApplication.SharedPreferences;

    private static ListView mListView;
    private static ImageView image_persontx;
    private static ImageView image_grouptx;
    public static   LinearLayout imageView_answer;
    private static TextView tv_groupname;
    private static TextView tv_num;
    private static TextView tv_grouptype;
    private static TextView tv_allnum;
    private static TextView tv_personname;
    private static TextView talkingName;
   // private TextView talking_news;
    private ImageView image_personvoice;
    private static ImageView image_group_persontx;
    private static ImageView image_voice;
    private View rootView;
    private static GridView gridView_person;
    private Dialog dialog;
    private static Dialog confirmDialog;

    private RelativeLayout Relative_listview;
    public static LinearLayout lin_foot;
    public static LinearLayout lin_head;
    public static LinearLayout lin_notalk;
    public static LinearLayout lin_personhead;
    public static TipView tipView;

    private static AnimationDrawable draw;
    private static AnimationDrawable draw_group;
    private static String UserName;
    private static String groupId;
    public static String interPhoneType;
    public static String interPhoneId;
    private static String phoneId;
    private String tag = "TALKOLDLIST_VOLLEY_REQUEST_CANCEL_TAG";
    private static long Vibrate = 100;
    private static int enterGroupType;
    private static int dialogType;
    public static boolean isCalling = false;//是否是在通话状态;
    private boolean isCancelRequest;
    private static boolean isTalking = false;
    private static List<UserInfo> groupPersonList = new ArrayList<>();              // 组成员
    private static ArrayList<UserInfo> groupPersonListS = new ArrayList<>();
    private static ArrayList<GroupInfo> allList = new ArrayList<>();                // 所有数据库数据
    private static List<DBTalkHistorary> historyDataBaseList;                       // list里边的数据
    private static List<ListInfo> listInfo;
    private RelativeLayout relative_view;
    public static boolean isVisible;
    private ChatFragment ct;
    private LinearLayout lin_switch_moni;
    private RelativeLayout relative_mo_ni;
    private LinearLayout lin_switch_im;
    private static TextView tv_group_moni_name;
    private static TextView tv_group_moni_number;
    private LinearLayout lin_cut_moni;
    private static ImageView image_moni;


    @Override
    public void onTipViewClick() {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        ct=this;
        initDao();      // 初始化数据库
        setReceiver();  // 注册广播接收socketService的数据
    }

    private void setOnResumeView() {
        //此处在splashActivity中refreshB设置成true
        UserName = shared.getString(StringConstant.USERNAME, "");
        String p = shared.getString(StringConstant.PERSONREFRESHB, "false");
        String l = shared.getString(StringConstant.ISLOGIN, "false");
        if (l.equals("true")) {
            if (p.equals("true")) {
                Relative_listview.setVisibility(View.VISIBLE);
                //显示此时没有人通话界面
                lin_notalk.setVisibility(View.VISIBLE);
                lin_personhead.setVisibility(View.GONE);
                lin_head.setVisibility(View.GONE);
                lin_foot.setVisibility(View.GONE);
                GlobalConfig.isActive = false;
                tipView.setVisibility(View.GONE);
                getTXL();
                Editor et = shared.edit();
                et.putString(StringConstant.PERSONREFRESHB, "false");
                et.commit();
            }
        } else {
            //显示未登录
            Relative_listview.setVisibility(View.GONE);
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_LOGIN);
        }
    }

    /*
     *注册广播接收socketservice的数据
     */
    private void setReceiver() {
        if (Receiver == null) {
            Receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PUSH);
            filter.addAction(BroadcastConstants.PUSH_NOTIFY);
            filter.addAction(BroadcastConstants.UP_DATA_GROUP);
            filter.addAction(BroadcastConstants.PUSH_ALLURL_CHANGE);
            filter.addAction(BroadcastConstants.PUSH_VOICE_IMAGE_REFRESH);
            context.registerReceiver(Receiver, filter);

            IntentFilter f = new IntentFilter();
            f.addAction(BroadcastConstants.PUSH_BACK);
            f.setPriority(1000);
            context.registerReceiver(Receiver, f);

            ToastUtils.show_short(context, "注册了广播接收器");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        rootView = inflater.inflate(R.layout.fragment_talkoldlist, container, false);
        rootView = inflater.inflate(R.layout.fragment_interphone, container, false);
        setView();//设置界面
        setOnResumeView();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listener();
        Dialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        setOnResumeView();
        isVisible=true;
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchTalkHistoryDao(context);
    }

    private void setView() {
        lin_notalk = (LinearLayout) rootView.findViewById(R.id.lin_notalk);                     // 没有对讲时候的界面
        lin_personhead = (LinearLayout) rootView.findViewById(R.id.lin_personhead);             // 有个人对讲时候的界面
        tv_personname = (TextView) rootView.findViewById(R.id.tv_personname);                   // 个人对讲时候的好友名字
        image_persontx = (ImageView) rootView.findViewById(R.id.image_persontx);                // 个人对讲时候的好友头像
        image_personvoice = (ImageView) rootView.findViewById(R.id.image_personvoice);          // 个人对讲声音波
        lin_head = (LinearLayout) rootView.findViewById(R.id.lin_head);                         // 有群组对讲时候的界面
        image_grouptx = (ImageView) rootView.findViewById(R.id.image_grouptx);                  // 群组对讲时候群组头像
        tv_groupname = (TextView) rootView.findViewById(R.id.tv_groupname);                     // 群组对讲时候的群名
        tv_grouptype = (TextView) rootView.findViewById(R.id.tv_grouptype);                     // 群组对讲时候的群类型名
        tv_num = (TextView) rootView.findViewById(R.id.tv_num);                                 // 群组对讲时候的群在线人数
        tv_allnum = (TextView) rootView.findViewById(R.id.tv_allnum);                           // 群组对讲时候的群所有成员人数
        talkingName = (TextView) rootView.findViewById(R.id.talkingname);                       // 群组对讲时候对讲人姓名
        image_group_persontx = (ImageView) rootView.findViewById(R.id.image_group_persontx);    // 群组对讲时候对讲人头像
        gridView_person = (GridView) rootView.findViewById(R.id.gridView_person);               // 群组对讲时候对讲成员展示
        gridView_person.setSelector(new ColorDrawable(Color.TRANSPARENT));                      // 取消GridView的默认背景色
        image_voice = (ImageView) rootView.findViewById(R.id.image_voice);                      // 群组对讲声音波

        rootView.findViewById(R.id.lin_tv_show).setOnClickListener(this);                       //
        rootView.findViewById(R.id.lin_tv_close).setOnClickListener(this);                      //
        relative_view = (RelativeLayout) rootView.findViewById(R.id.relative_view);             //

        lin_switch_moni =(LinearLayout)rootView.findViewById(R.id.lin_switch_moni);             // 切换到模拟按钮
        relative_mo_ni=(RelativeLayout)rootView.findViewById(R.id.relative_mo_ni);              // 模拟对讲布局

        lin_switch_im=(LinearLayout)rootView.findViewById(R.id.lin_switch_im);                  // 切换到网络对讲


        mListView = (ListView) rootView.findViewById(R.id.listView);                            //
        lin_foot = (LinearLayout) rootView.findViewById(R.id.lin_foot);                         // 对讲按钮
        imageView_answer = (LinearLayout) rootView.findViewById(R.id.imageView_answer);         //
        Relative_listview = (RelativeLayout) rootView.findViewById(R.id.Relative_listview);     //
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setTipClick(this);

        tv_group_moni_name=(TextView)rootView.findViewById(R.id.tv_group_moni_name);            // 模拟界面显示的群名
        tv_group_moni_number=(TextView)rootView.findViewById(R.id.tv_group_moni_number);        // 模拟界面显示的对讲频率
        lin_cut_moni=(LinearLayout)rootView.findViewById(R.id.lin_cut_moni);                    // 模拟对讲关闭

        image_moni=(ImageView)rootView.findViewById(R.id.image_moni);                           // 模拟页面头像


        image_personvoice.setBackgroundResource(R.drawable.talk_show);
        draw = (AnimationDrawable) image_personvoice.getBackground();

        image_personvoice.setVisibility(View.INVISIBLE);
        image_voice.setBackgroundResource(R.drawable.talk_show);
        draw_group = (AnimationDrawable) image_voice.getBackground();
        image_voice.setVisibility(View.INVISIBLE);

        talkingName.setVisibility(View.INVISIBLE);
    }

    private void listener() {
        image_grouptx.setOnClickListener(this);
        imageView_answer.setOnClickListener(this);
        lin_switch_moni.setOnClickListener(this);
        lin_switch_im.setOnClickListener(this);
        lin_cut_moni.setOnClickListener(this);
     /*   image_button.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        press();//按下状态
                        break;
                    case MotionEvent.ACTION_UP:
                        jack();//抬起手后的操作
                        break;
                }
                return false;
            }
        });*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_tv_show:
                relative_view.setVisibility(View.VISIBLE);
                break;
            case R.id.lin_tv_close:
                relative_view.setVisibility(View.GONE);
                break;
            case R.id.image_grouptx:
                //查看群成员
                checkGroup();
                break;
            case R.id.imageView_answer:
                //挂断
                hangUp();
                break;
            case R.id.lin_switch_moni:                               //  切换到模拟对讲
                Relative_listview.setVisibility(View.GONE);
                relative_mo_ni.setVisibility(View.VISIBLE);
                //此处应加提示框
                hangUp();
                break;
            case R.id.lin_switch_im:
                relative_mo_ni.setVisibility(View.GONE);             //  切换到网络对讲布局
                Relative_listview.setVisibility(View.VISIBLE);
                break;
            case R.id.lin_cut_moni:                                  //  关闭模拟对讲
                //对接关闭模拟对讲事件
                //  切换到网络对讲布局
                relative_mo_ni.setVisibility(View.GONE);
                Relative_listview.setVisibility(View.VISIBLE);
                break;
        }
    }

    public static void hangUp() {
        //挂断
        GlobalConfig.isIM=false;
        if (interPhoneType.equals("user")) {
            //挂断电话
            isCalling = false;

            InterPhoneControlHelper.PersonTalkHangUp(context, InterPhoneControlHelper.bdcallid);
            historyDataBaseList = dbDao.queryHistory();//得到数据库里边数据
            getList();
            if (allList.size() == 0) {
                if (adapter == null) {
                    adapter = new ChatListAdapter(context, allList, "0");
                    mListView.setAdapter(adapter);
                } else {
                    adapter.ChangeDate(allList, "0");
                }
            } else {
                if (adapter == null) {
                    adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                    mListView.setAdapter(adapter);
                } else {
                    adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
                }
            }
            setListener();
            setImageView(2, "", "");
            lin_notalk.setVisibility(View.VISIBLE);
            lin_personhead.setVisibility(View.GONE);
            lin_head.setVisibility(View.GONE);
            lin_foot.setVisibility(View.GONE);
            GlobalConfig.isActive = false;
            gridView_person.setVisibility(View.GONE);
        } else {
            InterPhoneControlHelper.Quit(context, interPhoneId);//退出小组
            historyDataBaseList = dbDao.queryHistory();//得到数据库里边数据
            getList();
            if (allList.size() == 0) {
                if (adapter == null) {
                    adapter = new ChatListAdapter(context, allList, "0");
                    mListView.setAdapter(adapter);
                } else {
                    adapter.ChangeDate(allList, "0");
                }
            } else {
                if (adapter == null) {
                    adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                    mListView.setAdapter(adapter);
                } else {
                    adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
                }
            }
            setListener();
            setImageView(2, "", "");
            lin_notalk.setVisibility(View.VISIBLE);
            lin_personhead.setVisibility(View.GONE);
            lin_head.setVisibility(View.GONE);
            lin_foot.setVisibility(View.GONE);
            GlobalConfig.isActive = false;
            gridView_person.setVisibility(View.GONE);
        }
    }

    private void checkGroup() {
        //查看群成员
        if (groupPersonList != null && groupPersonList.size() != 0) {
            groupPersonListS.clear();
            if (listInfo != null && listInfo.size() > 0) {
                for (int j = 0; j < listInfo.size(); j++) {
                    String id = listInfo.get(j).getUserId().trim();
                    if (id != null && !id.equals("")) {
                        for (int i = 0; i < groupPersonList.size(); i++) {
                            String ids = groupPersonList.get(i).getUserId();
                            if (id.equals(ids)) {
                                Log.e("ids", ids + "=======" + i);
                                groupPersonList.get(i).setOnLine(2);
                                groupPersonListS.add(groupPersonList.get(i));
                            }
                        }
                    }
                }
            } else {
                String id = CommonUtils.getUserId(context);
                if (id != null && !id.equals("")) {
                    for (int i = 0; i < groupPersonList.size(); i++) {
                        String ids = groupPersonList.get(i).getUserId();
                        if (id.equals(ids)) {
                            Log.e("ids", ids + "=======" + i);
                            groupPersonList.get(i).setOnLine(2);
                            groupPersonListS.add(groupPersonList.get(i));
                        }
                    }
                }
            }
            for (int h = 0; h < groupPersonList.size(); h++) {
                if (groupPersonList.get(h).getOnLine() != 2) {
                    groupPersonListS.add(groupPersonList.get(h));
                }
            }
            GroupPersonAdapter adapter = new GroupPersonAdapter(context, groupPersonListS);
            gridView_person.setAdapter(adapter);
            gridView_person.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean isFriend = false;
                    if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                        for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                            if (groupPersonListS.get(position).getUserId().equals(GlobalConfig.list_person.get(i).getUserId())) {
                                isFriend = true;
                                break;
                            }
                        }
                    } else {
                        //不是我的好友
                        isFriend = false;
                    }
                    if (isFriend) {
                        TalkPersonNewsFragment fg = new TalkPersonNewsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "talkoldlistfragment_p");
                        bundle.putSerializable("data", groupPersonListS.get(position));
                        fg.setArguments(bundle);
                        DuiJiangActivity.open(fg);
                    } else {

                        GroupPersonNewsFragment fg = new GroupPersonNewsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "talkoldlistfragment_p");
                        bundle.putString("id", interPhoneId);
                        bundle.putSerializable("data", groupPersonListS.get(position));
                        fg.setArguments(bundle);
                        fg.setTargetFragment(ct,1);
                        DuiJiangActivity.open(fg);

                    }
                }
            });

            if (gridView_person.getVisibility() == View.VISIBLE) {
                gridView_person.setVisibility(View.GONE);
            } else {
                gridView_person.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void setImageView(int i, String userName, String url) {
        //设置有人说话时候界面友好交互
        //发送消息线程
        if (i == 1) {
            Log.e("userName===============", userName + "");
            talkingName.setVisibility(View.VISIBLE);
            if (userName.equals(UserName)) {
                talkingName.setText("我");
            } else {
                talkingName.setText(userName);
            }
           // talking_news.setText("正在通话");
            image_voice.setVisibility(View.VISIBLE);
            if (url == null || url.equals("") || url.equals("null") || url.trim().equals("")) {
                image_group_persontx.setImageResource(R.mipmap.wt_image_tx_hy);
            } else {
                String urls;
                if (url.startsWith("http")) {
                    urls = url;
                } else {
                    urls = GlobalConfig.imageurl + url;
                }
                urls = AssembleImageUrlUtils.assembleImageUrl150(urls);
                Picasso.with(context).load(urls.replace("\\/", "/")).into(image_group_persontx);
            }
            if (draw_group.isRunning()) {
            } else {
                draw_group.start();
            }
        } else {
            talkingName.setVisibility(View.INVISIBLE);
            //talking_news.setText("无人通话");
            if (draw_group.isRunning()) {
                draw_group.stop();
            }
            image_group_persontx.setImageResource(R.mipmap.wt_image_tx_hy);
            image_voice.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置对讲组为激活状态
     */
    public static void zhiDingGroupSS(String groupIdS) {
        Intent intent = new Intent();
        intent.setAction(BroadcastConstants.UP_DATA_GROUP);
        context.sendBroadcast(intent);
        enterGroupType = 1;
        groupId = groupIdS;
        tv_num.setText("1");
        listInfo = null;
        InterPhoneControlHelper.Enter(context, groupId);//发送进入组的数据，socket
        getGridViewPerson(groupId);//获取群成员
    }

    /**
     * 设置对讲组为激活状态
     */
    public static void zhiDingGroup(GroupInfo talkGroupInside) {
        Intent intent = new Intent();
        intent.setAction(BroadcastConstants.UP_DATA_GROUP);
        context.sendBroadcast(intent);
        enterGroupType = 1;
        groupId = talkGroupInside.getGroupId();
        groupFreq=talkGroupInside.getGroupFreq();
        groupName=talkGroupInside.getGroupName();
        groupImage=talkGroupInside.getGroupImg();
        setMoniView(groupName,groupFreq,groupImage);
        tv_num.setText("1");
        listInfo = null;
        InterPhoneControlHelper.Enter(context, talkGroupInside.getGroupId());//发送进入组的数据，socket
        getGridViewPerson(talkGroupInside.getGroupId());//获取群成员
    }


    /**
     * 从通讯录来的数据处理
     */
    public static void zhiDingGroupString(String groupId,String groupName,String groupFreq,String groupImage) {
        Intent intent = new Intent();
        intent.setAction(BroadcastConstants.UP_DATA_GROUP);
        context.sendBroadcast(intent);
        enterGroupType = 1;
        setMoniView(groupName,groupFreq,groupImage);    //设置界面的方法
        tv_num.setText("1");
        listInfo = null;
        InterPhoneControlHelper.Enter(context, groupId);//发送进入组的数据，socket
        getGridViewPerson(groupId);//获取群成员
    }


    /**
     * 设置对讲组2为激活状态
     */
    public static void zhiDingGroupS(GroupInfo talkGroupInside) {
        Intent intent = new Intent();
        intent.setAction(BroadcastConstants.UP_DATA_GROUP);
        context.sendBroadcast(intent);
        enterGroupType = 2;
        groupId = talkGroupInside.getGroupId();
        groupFreq=talkGroupInside.getGroupFreq();
        groupName=talkGroupInside.getGroupName();
        groupImage=talkGroupInside.getGroupImg();
        setMoniView(groupName,groupFreq,groupImage);
        tv_num.setText("1");
        listInfo = null;
        InterPhoneControlHelper.Enter(context, talkGroupInside.getGroupId());//发送进入组的数据，socket
        getGridViewPerson(talkGroupInside.getGroupId());//获取群成员
    }

    /*
   *\  设置模拟对讲界面的方法
   */
    private static void setMoniView(String groupName, String groupFreq,String groupImage) {
        //tv_group_moni_name
        if(!TextUtils.isEmpty(groupName)){
            tv_group_moni_name.setText(groupName);
        }
        String s=groupFreq;
        if(!TextUtils.isEmpty(groupFreq)){
            List<String> tempList=FrequencyUtil.getFrequence(groupFreq);
            if(tempList!=null&&tempList.size()>0){
                tv_group_moni_number.setText(tempList.get(0));
            }else{
                tv_group_moni_number.setText(FrequencyUtil.DefaultFrequnce);
            }

        }else{
            tv_group_moni_number.setText(FrequencyUtil.DefaultFrequnce);
        }
        if (groupImage == null ||groupImage.equals("")) {
            image_moni.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            String url;
            if (groupImage.startsWith("http")) {
                url = groupImage;
            } else {
                url = GlobalConfig.imageurl + groupImage;
            }
            url = AssembleImageUrlUtils.assembleImageUrl150(url);
            Picasso.with(context).load(url.replace("\\/", "/")).into(image_moni);
        }
    }


    /**
     * 设置个人为激活状态/设置第一条为激活状态
     */
    public static void zhiDingPerson() {
        try {
            historyDataBaseList = dbDao.queryHistory();//得到数据库里边数据
            getList();
            setDatePerson();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show_always(context,"数据出错了，请您稍后再试");
        }
    }

    private static void setListener() {
        adapter.setOnListener(new ChatListAdapter.OnListener() {
            @Override
            public void zhiding(int position) {
                groupId = allList.get(position).getId();
                if (isCalling) {
                    //此时有对讲状态
                    if (interPhoneType.equals("user")) {
                        //对讲状态为个人时，弹出框展示
                        String t = allList.get(position).getTyPe();
                        if (t != null && !t.equals("") && t.equals("user")) {
                            dialogType = 1;
                            phoneId = allList.get(position).getId();
                        } else {
                            dialogType = 2;
                        }
                        confirmDialog.show();
                    } else {
                        InterPhoneControlHelper.Quit(context, interPhoneId);//退出小组
                        String t = allList.get(position).getTyPe();
                        if (t != null && !t.equals("") && t.equals("user")) {
                            call(allList.get(position).getId());
                        } else {
                            zhiDingGroupSS(groupId);
                        }
                    }
                } else {
                    String t = allList.get(position).getTyPe();
                    if (t != null && !t.equals("") && t.equals("user")) {
                        String id = allList.get(position).getId();
                        if (id != null && !id.equals("")) {
                            call(id);
                        }
                    } else {
                        zhiDingGroupSS(groupId);
                    }
                }
            }
        });

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String type = allList.get(position).getTyPe();
                if (type != null && type.equals("group")) {
                    //跳转到群组详情页面

                    GroupDetailFragment fg = new GroupDetailFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("type", "talkoldlistfragment");
                    bundle1.putString("activationid", interPhoneId);
                    bundle1.putSerializable("data", allList.get(position));
                    fg.setArguments(bundle1);
                    DuiJiangActivity.open(fg);
                } else {
                    // 跳转到详细信息界面

                    TalkPersonNewsFragment fg = new TalkPersonNewsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "talkoldlistfragment");
                    bundle.putSerializable("data", allList.get(position));
                    fg.setArguments(bundle);
                    DuiJiangActivity.open(fg);

                }
            }
        });
    }

    protected static void call(String id) {

        CallAlertFragment fg = new CallAlertFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        fg.setArguments(bundle);
        DuiJiangActivity.open(fg);

    }

    public void getTXL() {
        //第一次获取群成员跟组
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            JSONObject jsonObject = VolleyRequest.getJsonObject(context);
            VolleyRequest.RequestPost(GlobalConfig.gettalkpersonsurl, tag, jsonObject, new VolleyCallback() {

                @Override
                protected void requestSuccess(JSONObject result) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    if (isCancelRequest) {
                        return;
                    }
                    try {
                        LinkMan list;
                        list = new Gson().fromJson(result.toString(), new TypeToken<LinkMan>() {
                        }.getType());
                        try {
                            GlobalConfig.list_group = list.getGroupList().getGroups();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        try {
                            GlobalConfig.list_person = list.getFriendList().getFriends();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //获取到群成员后
                    update(context);//第一次进入该界面
                }

                @Override
                protected void requestError(VolleyError error) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    //获取到群成员后
                    update(context);//第一次进入该界面
                }
            });
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    /*
     * 第一次进入该界面
     */
    private void update(Context context) {
        //得到数据库里边数据
        historyDataBaseList = dbDao.queryHistory();
        //得到真实的数据
        getList();
        if (allList == null || allList.size() == 0) {
            //此时数据库里边没有数据，界面不变
            isCalling = false;
        } else {
            // 此处数据需要处理，第一条数据为激活状态组
            //第一条数据的状态
            //			String type = alllist.get(0).getTyPe();//对讲类型，个人跟群组
            //			String id = alllist.get(0).getId();//对讲组：groupid
            //			if(type!=null&&!type.equals("")&&type.equals("user")){
            //若上次退出前的通话状态是单对单通话则不处理
            isCalling = false;
            if (adapter == null) {
                adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                mListView.setAdapter(adapter);
                Log.e("适配========", "1");
            } else {
                adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
                Log.e("适配========", "2");
            }
            setListener();
        }
        if (MainActivity.groupInfo != null && MainActivity.groupInfo.getGroupId() != null
                && !MainActivity.groupInfo.getGroupId().equals("")) {
            String id = MainActivity.groupInfo.getGroupId();
            dbDao.deleteHistory(id);
            addGroup(id);//加入到数据库
            setDateGroup();
            getGridViewPerson(id);
            MainActivity.groupInfo = null;
        }

        if (MainActivity.talkdb != null) {
            zhiDingPerson();
            MainActivity.talkdb = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    getGridViewPerson(interPhoneId);//获取群成员
                }
                break;
        }
    }

    public void addGroup(String id) {
        //获取最新激活状态的数据
        String groupid = id;
        String type = "group";
        String addtime = Long.toString(System.currentTimeMillis());
        String bjuserid = CommonUtils.getUserId(context);
        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
        DBTalkHistorary history = new DBTalkHistorary(bjuserid, type, groupid, addtime);
        dbDao.addTalkHistory(history);
        historyDataBaseList = dbDao.queryHistory();//得到数据库里边数据
        getList();
    }

    public void setDateGroup() {
        //设置组为激活状态
        lin_notalk.setVisibility(View.GONE);
        lin_personhead.setVisibility(View.GONE);
        lin_head.setVisibility(View.VISIBLE);
        lin_foot.setVisibility(View.VISIBLE);
        //对讲控制
        if(GlobalConfig.isMONI==true){
            context.stopService(new Intent(context, SimulationService.class));
            GlobalConfig.isMONI=false;
        }
        GlobalConfig.isIM=true;
        GlobalConfig.isActive = true;
        tipView.setVisibility(View.GONE);
        GroupInfo firstdate = allList.remove(0);
        interPhoneType = firstdate.getTyPe();//对讲类型，个人跟群组
        interPhoneId = firstdate.getId();//对讲组：groupid
        groupId = firstdate.getGroupId();
        groupFreq=firstdate.getGroupFreq();
        groupName=firstdate.getGroupName();
        groupImage=firstdate.getGroupImg();
        setMoniView(groupName,groupFreq,groupImage);
        tv_groupname.setText(firstdate.getName());
        if (firstdate.getGroupType() == null || firstdate.getGroupType().equals("") || firstdate.getGroupType().equals("1")) {
            tv_grouptype.setText("公开群");
        } else if (firstdate.getGroupType().equals("0")) {
            tv_grouptype.setText("审核群");
        } else if (firstdate.getGroupType().equals("2")) {
            tv_grouptype.setText("密码群");
        }
        if (firstdate.getPortrait() == null || firstdate.getPortrait().equals("") || firstdate.getPortrait().trim().equals("")) {
            image_grouptx.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            String url;
            if (firstdate.getPortrait().startsWith("http")) {
                url = firstdate.getPortrait();
            } else {
                url = GlobalConfig.imageurl + firstdate.getPortrait();
            }
            url = AssembleImageUrlUtils.assembleImageUrl150(url);
            Picasso.with(context).load(url.replace("\\/", "/")).into(image_grouptx);
        }
        if (allList.size() == 0) {
            if (adapter == null) {
                adapter = new ChatListAdapter(context, allList, "0");
                mListView.setAdapter(adapter);
            } else {
                adapter.ChangeDate(allList, "0");
            }
        } else {
            if (adapter == null) {
                adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                mListView.setAdapter(adapter);
            } else {
                adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
            }
        }
        setListener();
    }

    public static void setDatePerson() {
        //设置个人为激活状态
        isCalling = true;
        GroupInfo firstdate = allList.remove(0);
        interPhoneType = firstdate.getTyPe();//
        interPhoneId = firstdate.getId();//
        Log.e("aaa=====callerid======", interPhoneId + "");
        lin_notalk.setVisibility(View.GONE);
        lin_personhead.setVisibility(View.VISIBLE);
        lin_head.setVisibility(View.GONE);
        lin_foot.setVisibility(View.VISIBLE);
        if(GlobalConfig.isMONI==true){
            context.stopService(new Intent(context, SimulationService.class));
            GlobalConfig.isMONI=false;
        }
        GlobalConfig.isIM=true;
        GlobalConfig.isActive = true;
        tipView.setVisibility(View.GONE);
        tv_personname.setText(firstdate.getName());
        groupId = firstdate.getGroupId();
        groupFreq=firstdate.getGroupFreq();
        groupName=firstdate.getGroupName();
        groupImage=firstdate.getGroupImg();
        setMoniView(groupName,groupFreq,groupImage);
        if (firstdate.getPortrait() == null || firstdate.getPortrait().equals("") || firstdate.getPortrait().trim().equals("")) {
            image_persontx.setImageResource(R.mipmap.wt_image_tx_qz);
        } else {
            String url;
            if (firstdate.getPortrait().startsWith("http")) {
                url = firstdate.getPortrait();
            } else {
                url = GlobalConfig.imageurl + firstdate.getPortrait();
            }
            url = AssembleImageUrlUtils.assembleImageUrl150(url);
            Picasso.with(context).load(url.replace("\\/", "/")).into(image_grouptx);
        }
        if (allList.size() == 0) {
            if (adapter == null) {
                adapter = new ChatListAdapter(context, allList, "0");
                mListView.setAdapter(adapter);
            } else {
                adapter.ChangeDate(allList, "0");
            }
        } else {
            if (adapter == null) {
                adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                mListView.setAdapter(adapter);
            } else {
                adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
            }
        }
        setListener();
    }

    private static void getList() {
        allList.clear();
        try {
            if (historyDataBaseList != null && historyDataBaseList.size() > 0) {
                for (int i = 0; i < historyDataBaseList.size(); i++) {
                    if (historyDataBaseList.get(i).getTyPe().equals("user")) {
                        if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() != 0) {
                            for (int j = 0; j < GlobalConfig.list_person.size(); j++) {
                                String id = historyDataBaseList.get(i).getID();
                                if (id != null && !id.equals("") && id.equals(GlobalConfig.list_person.get(j).getUserId())) {
                                    GroupInfo ListGP = new GroupInfo();
                                    ListGP.setTruename(GlobalConfig.list_person.get(j).getTruename());
                                    ListGP.setId(GlobalConfig.list_person.get(j).getUserId());
                                    ListGP.setName(GlobalConfig.list_person.get(j).getUserName());
                                    ListGP.setUserAliasName(GlobalConfig.list_person.get(j).getUserAliasName());
                                    ListGP.setPortrait(GlobalConfig.list_person.get(j).getPortraitBig());
                                    ListGP.setAddTime(historyDataBaseList.get(i).getAddTime());
                                    ListGP.setTyPe(historyDataBaseList.get(i).getTyPe());
                                    ListGP.setDescn(GlobalConfig.list_person.get(j).getDescn());
                                    ListGP.setUserNum(GlobalConfig.list_person.get(j).getUserNum());
                                    allList.add(ListGP);
                                    break;
                                }
                            }
                        }
                    } else {
                        if (GlobalConfig.list_group != null && GlobalConfig.list_group.size() != 0) {
                            for (int j = 0; j < GlobalConfig.list_group.size(); j++) {
                                String id = historyDataBaseList.get(i).getID();
                                if (id != null && !id.equals("") && id.equals(GlobalConfig.list_group.get(j).getGroupId())) {
                                    GroupInfo ListGP = new GroupInfo();
                                    ListGP.setCreateTime(GlobalConfig.list_group.get(j).getCreateTime());
                                    ListGP.setGroupCount(GlobalConfig.list_group.get(j).getGroupCount());
                                    ListGP.setGroupCreator(GlobalConfig.list_group.get(j).getGroupCreator());
                                    ListGP.setGroupDescn(GlobalConfig.list_group.get(j).getGroupDescn());
                                    ListGP.setId(GlobalConfig.list_group.get(j).getGroupId());
                                    ListGP.setPortrait(GlobalConfig.list_group.get(j).getGroupImg());
                                    ListGP.setGroupManager(GlobalConfig.list_group.get(j).getGroupManager());
                                    ListGP.setGroupMyAlias(GlobalConfig.list_group.get(j).getGroupMyAlias());
                                    ListGP.setName(GlobalConfig.list_group.get(j).getGroupName());
                                    ListGP.setGroupNum(GlobalConfig.list_group.get(j).getGroupNum());
                                    ListGP.setGroupSignature(GlobalConfig.list_group.get(j).getGroupSignature());
                                    ListGP.setGroupType(GlobalConfig.list_group.get(j).getGroupType());
                                    ListGP.setAddTime(historyDataBaseList.get(i).getAddTime());
                                    ListGP.setTyPe(historyDataBaseList.get(i).getTyPe());
                                    allList.add(ListGP);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("getlist异常", e.toString());
        }
    }

    private static void getGridViewPerson(String id) {
        Log.e("fasfasfa", "0");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.grouptalkUrl, jsonObject, new VolleyCallback() {

            @Override
            protected void requestSuccess(JSONObject result) {
                Log.e("fasfasfa", "1");
                String UserList = null;
                try {
                    UserList = result.getString("UserList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (groupPersonList != null) {
                    groupPersonList.clear();
                } else {
                    groupPersonList = new ArrayList<UserInfo>();
                }
                try {
                    groupPersonList = gson.fromJson(UserList, new TypeToken<List<UserInfo>>() {
                    }.getType());
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
                if (groupPersonList != null && groupPersonList.size() > 0) {
                    tv_allnum.setText("/" + groupPersonList.size());
                } else {
                    tv_allnum.setText("/1");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                Log.e("fasfasfa", "2");
            }
        });
    }

    private void Dialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_talk_person_del, null);
        TextView tv_cancel = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_confirm = (TextView) dialog1.findViewById(R.id.tv_confirm);
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InterPhoneControlHelper.PersonTalkHangUp(context, InterPhoneControlHelper.bdcallid);
                if (dialogType == 1) {
                    InterPhoneControlHelper.PersonTalkHangUp(context, InterPhoneControlHelper.bdcallid);
                    isCalling = false;
                    lin_notalk.setVisibility(View.VISIBLE);
                    lin_personhead.setVisibility(View.GONE);
                    lin_head.setVisibility(View.GONE);
                    lin_foot.setVisibility(View.GONE);
                    GlobalConfig.isActive = false;
                    call(phoneId);
                    confirmDialog.dismiss();
                } else {
                    InterPhoneControlHelper.PersonTalkHangUp(context, InterPhoneControlHelper.bdcallid);
                    isCalling = false;
                    lin_notalk.setVisibility(View.VISIBLE);
                    lin_personhead.setVisibility(View.GONE);
                    lin_head.setVisibility(View.GONE);
                    lin_foot.setVisibility(View.GONE);
                    GlobalConfig.isActive = false;
                    zhiDingGroupSS(groupId);
                    //对讲主页界面更新
                    DuiJiangFragment.update();
                    confirmDialog.dismiss();
                }
            }
        });
    }

    // 接收socket的数据进行处理
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.PUSH)) {
                //	MsgNormal message = (MsgNormal) intent.getSerializableExtra("outMessage");
                byte[] bt = intent.getByteArrayExtra("outMessage");
                //	Log.e("接收器中数据", Arrays.toString(bt)+"");
                try {
                    MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);

                    if (message != null) {
                        int biztype = message.getBizType();
                        if (biztype == 1) {
                            int cmdType = message.getCmdType();
                            if (cmdType == 2) {
                                int command = message.getCommand();
                                if (command == 9) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TTT
                                            //请求通话出异常了
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "请求通话—出异常了");
                                            break;
                                        case 0x00:
                                            //没有有效登录用户
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "没有有效登录用户");
                                            break;
                                        case 0x02:                                            //无法获取用户组
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "无法获取用户组");
                                            break;
                                        case 0x01:
                                            //用户可以通话了
                                            isTalking = true;
                                            ToastUtils.show_short(context, "可以说话");
                                            // headview中展示自己的头像
                                            String url = BSApplication.SharedPreferences.getString(StringConstant.IMAGEURL, "");
                                            setImageView(1, UserName, url);
                                            VoiceStreamRecordService.send();
                                            break;
                                        case 0x04:
                                            //用户不在所指定的组
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "用户不在所指定的组");
                                            break;
                                        case 0x05:
                                            //进入组的人员不足两人
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "进入组的人员不足两人");
                                            break;
                                        case 0x08:
                                            //有人在说话，无权通话
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "有人在说话");
                                            break;
                                        case 0x90:
                                            //用户在电话通话
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "用户在电话通话");
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (command == 0x0a) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TTT
                                            //结束对讲出异常
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_short(context, "结束对讲—出异常");
                                            break;
                                        case 0x00:
                                            //没有有效登录用户
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
                                            break;
                                        case 0x02:
                                            //无法获取用户组
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_always(context, "无法获取用户组");
                                            break;
                                        case 0x01:
                                            //成功结束对讲
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_short(context, "结束对讲—成功");
                                            break;
                                        case 0x04:
                                            //	用户不在组
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_short(context, "结束对讲");
                                            break;
                                        case 0x05:
                                            //	对讲人不是你，无需退出
                                            isTalking = false;
                                            setImageView(2, "", "");
                                            ToastUtils.show_short(context, "对讲人不是你，无需退出");
                                            break;

                                        default:
                                            break;
                                    }
                                } else if (command == 0x10) {
                                    //组内有人说话
                                    ToastUtils.show_short(context, "组内人有人说话，有人按下说话钮");
                                    MapContent data = (MapContent) message.getMsgContent();
                                    //说话人
                                    String talkUserId = data.get("SpeakerId") + "";
                                    Log.i("talkUserId", talkUserId + "");
                                    if (groupPersonList != null && groupPersonList.size() != 0) {
                                        for (int i = 0; i < groupPersonList.size(); i++) {
                                            if (groupPersonList.get(i).getUserId().equals(talkUserId)) {
                                                setImageView(1, groupPersonList.get(i).getUserName(), groupPersonList.get(i).getPortraitMini());
                                            }
                                        }
                                    }
                                } else if (command == 0x20) {
                                    //组内人说话完毕，有人松手
                                    setImageView(2, "", "");
                                    ToastUtils.show_short(context, "组内人说话完毕，有人松手");
                                }
                            } else if (cmdType == 1) {
                                int command = message.getCommand();
                                if (command == 9) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TT
                                            //进入组出异常
                                            isCalling = false;
                                            ToastUtils.show_short(context, "进入组—出异常");
                                            break;
                                        case 0x00:
                                            //没有有效登录用户
                                            isCalling = false;
                                            ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
                                            break;
                                        case 0x01:
                                            //进入组成功
                                            isCalling = true;
                                            ToastUtils.show_short(context, "进入组—成功");
                                            if (enterGroupType == 2) {
                                                InterPhoneControlHelper.Quit(context, interPhoneId);//退出小组
                                                String id = groupId;//对讲组：groupid
                                                dbDao.deleteHistory(id);
                                                addGroup(id);//加入到数据库
                                                setDateGroup();
                                            } else {
                                                String id = groupId;//对讲组：groupid
                                                dbDao.deleteHistory(id);
                                                addGroup(id);//加入到数据库
                                                setDateGroup();
                                            }
                                            break;
                                        case 0x02:
                                            //无法获取用户组
                                            isCalling = false;
                                            ToastUtils.show_short(context, "无法获取用户组");
                                            break;
                                        case 0x04:
                                            //用户不在该组
                                            isCalling = false;
                                            ToastUtils.show_short(context, "进入组—用户不在该组");
                                            break;
                                        case 0x08:
                                            //用户已在组
                                            isCalling = true;
                                            if (enterGroupType == 2) {
                                                InterPhoneControlHelper.Quit(context, interPhoneId);//退出小组
                                                String id = groupId;//对讲组：groupid
                                                dbDao.deleteHistory(id);
                                                addGroup(id);//加入到数据库
                                                setDateGroup();
                                            } else {
                                                String id = groupId;//对讲组：groupid
                                                dbDao.deleteHistory(id);
                                                addGroup(id);//加入到数据库
                                                setDateGroup();
                                            }
                                            ToastUtils.show_short(context, "进入组—用户已在组");
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (command == 0x0a) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TT
                                            //退出租出异常
                                            jack();
                                            ToastUtils.show_short(context, "退出租—出异常");
                                            isCalling = false;
                                            break;
                                        case 0x00:
                                            //没有有效登录用户
                                            jack();
                                            isCalling = false;
                                            ToastUtils.show_always(context, "数据出错，请注销后重新登录账户");
                                            break;
                                        case 0x01:
                                            //退出租成功
                                            jack();
                                            ToastUtils.show_short(context, "退出组—成功");
                                            isCalling = false;
                                            break;
                                        case 0x02:
                                            //退出租成功
                                            jack();
                                            isCalling = false;
                                            ToastUtils.show_short(context, "无法获取用户组");
                                            break;
                                        case 0x04:
                                            //用户不在该组
                                            jack();
                                            ToastUtils.show_short(context, "退出租—用户不在该组");
                                            isCalling = false;
                                            break;
                                        case 0x08:
                                            //用户已退出组
                                            jack();
                                            ToastUtils.show_short(context, "退出租—用户已退出组");
                                            isCalling = false;
                                            break;
                                        default:
                                            jack();
                                            ToastUtils.show_short(context, "退出租—用户已退出组");
                                            isCalling = false;
                                            break;
                                    }

                                } else if (command == 0x10) {
                                    //服务端发来的组内成员的变化
                                    ToastUtils.show_always(context, "服务端发来的组内成员的变化");
                                    try {
                                        MapContent data = (MapContent) message.getMsgContent();
                                        Map<String, Object> map = data.getContentMap();
                                        String news = new Gson().toJson(map);

                                        JSONTokener jsonParser = new JSONTokener(news);
                                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                        String ingroupusers = arg1.getString("InGroupUsers");

                                        listInfo = new Gson().fromJson(ingroupusers, new TypeToken<List<ListInfo>>() {
                                        }.getType());
                                        //组内所有在线成员
                                        //组内有人说话时，根据这个list数据，得到该成员信息啊：头像，昵称等
                                        Log.i("组内成员人数", listInfo.size() + "");
                                        tv_num.setText(listInfo.size() + "");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if (biztype == 2) {
                            int cmdType = message.getCmdType();
                            if (cmdType == 2) {
                                int command = message.getCommand();
                                if (command == 9) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TT
                                            //请求通话出异常了
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "请求通话—出异常了");
                                            break;
                                        case 0x02:
                                            //无权通话
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "当前有人在说话");
                                            break;
                                        case 0x01:
                                            //用户可以通话了
                                            isTalking = true;
                                            ToastUtils.show_short(context, "可以说话");
                                            image_personvoice.setVisibility(View.VISIBLE);
                                            if (draw.isRunning()) {
                                            } else {
                                                draw.start();
                                            }
                                            VoiceStreamRecordService.send();
                                            break;
                                        case 0x04:
                                            //用户无权通话
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "不能对讲，有人在说话");
                                            break;
                                        case 0x05:
                                            //无权通话
                                            VibratorUtils.Vibrate(ChatFragment.context, Vibrate);
                                            VoiceStreamRecordService.stop();
                                            ToastUtils.show_always(context, "不能对讲，状态错误");
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (command == 0x0a) {
                                    int returnType = message.getReturnType();
                                    switch (returnType) {
                                        case 0xff://TT
                                            //结束对讲出异常
                                            isTalking = false;
                                            if (draw.isRunning()) {
                                                draw.stop();
                                            }
                                            image_personvoice.setVisibility(View.INVISIBLE);
                                            ToastUtils.show_short(context, "结束对讲—出异常");
                                            break;
                                        case 0x02:
                                            //	无法获取用户
                                            isTalking = false;
                                            if (draw.isRunning()) {
                                                draw.stop();
                                            }
                                            image_personvoice.setVisibility(View.INVISIBLE);
                                            ToastUtils.show_short(context, "无法获取用户");
                                            break;
                                        case 0x01:
                                            //成功结束对讲
                                            isTalking = false;
                                            if (draw.isRunning()) {
                                                draw.stop();
                                            }
                                            image_personvoice.setVisibility(View.INVISIBLE);
                                            ToastUtils.show_short(context, "结束对讲—成功");
                                            break;
                                        case 0x04:
                                            //	清除者和当前通话者不同，无法处理
                                            isTalking = false;
                                            if (draw.isRunning()) {
                                                draw.stop();
                                            }
                                            image_personvoice.setVisibility(View.INVISIBLE);
                                            ToastUtils.show_short(context, "清除者和当前通话者不同，无法处理");
                                            break;
                                        case 0x05:
                                            //	状态错误
                                            isTalking = false;
                                            if (draw.isRunning()) {
                                                draw.stop();
                                            }
                                            image_personvoice.setVisibility(View.INVISIBLE);
                                            ToastUtils.show_short(context, "状态错误");
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else if (action.equals(BroadcastConstants.UP_DATA_GROUP)) {
                if (gridView_person != null) {
                    gridView_person.setVisibility(View.GONE);
                }
            } else if (action.equals(BroadcastConstants.PUSH_ALLURL_CHANGE)) {
                setOnResumeView();
            } else if (action.equals(BroadcastConstants.PUSH_BACK)) {
                //	MsgNormal message = (MsgNormal) intent.getSerializableExtra("outMessage");
                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("chatFragment的push_back", Arrays.toString(bt) + "");
                try {
                    MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);

                    if (message != null) {
                        int cmdType = message.getCmdType();
                        if (cmdType == 1) {
                            int command = message.getCommand();
                            if (command == 0x30) {
                                //挂断电话的数据处理
                                isCalling = false;
                                historyDataBaseList = dbDao.queryHistory();//得到数据库里边数据
                                getList();
                                if (allList.size() == 0) {
                                    if (adapter == null) {
                                        adapter = new ChatListAdapter(context, allList, "0");
                                        mListView.setAdapter(adapter);
                                    } else {
                                        adapter.ChangeDate(allList, "0");
                                    }
                                } else {
                                    if (adapter == null) {
                                        adapter = new ChatListAdapter(context, allList, allList.get(allList.size() - 1).getId());
                                        mListView.setAdapter(adapter);
                                    } else {
                                        adapter.ChangeDate(allList, allList.get(allList.size() - 1).getId());
                                    }
                                }
                                setListener();
                                if (draw.isRunning()) {
                                    draw.stop();
                                }
                                image_personvoice.setVisibility(View.INVISIBLE);
                                lin_notalk.setVisibility(View.VISIBLE);
                                lin_personhead.setVisibility(View.GONE);
                                lin_head.setVisibility(View.GONE);
                                lin_foot.setVisibility(View.GONE);
                                gridView_person.setVisibility(View.GONE);
                                GlobalConfig.isActive = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(BroadcastConstants.PUSH_VOICE_IMAGE_REFRESH)) {
                int seqNum = intent.getIntExtra("seqNum", -1);
                if (interPhoneType.equals("group")) {
                    //			image_voice.setVisibility(View.VISIBLE);
                } else {
                    //此处处理个人对讲的逻辑
                    if (seqNum < 0) {
                        if (image_personvoice.getVisibility() == View.VISIBLE) {
                            image_personvoice.setVisibility(View.INVISIBLE);
                            if (draw.isRunning()) {
                                draw.stop();
                            }
                        }
                    } else {
                        if (image_personvoice.getVisibility() == View.INVISIBLE) {
                            image_personvoice.setVisibility(View.VISIBLE);
                            if (draw.isRunning()) {
                            } else {
                                draw.start();
                            }
                        }
                    }
                }
            } else if (action.equals(BroadcastConstants.PUSH_NOTIFY)) {
                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("chat的PUSH_NOTIFY", Arrays.toString(bt) + "");
                try {
                    Log.e("chat的PUSH_NOTIFY", JsonEncloseUtils.btToString(bt) + "");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    if (message != null) {
                        int cmdType = message.getCmdType();
                        switch (cmdType) {
                            case 2:
                                int command2 = message.getCommand();
                                if (command2 == 4) {
                                    try {
                                        MapContent data = (MapContent) message.getMsgContent();
                                        Map<String, Object> map = data.getContentMap();
                                        String news = new Gson().toJson(map);

                                        JSONTokener jsonParser = new JSONTokener(news);
                                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                        String userinfos = arg1.getString("UserInfo");

                                        ListInfo userinfo = new Gson().fromJson(userinfos, new TypeToken<ListInfo>() {
                                        }.getType());
                                        String groupids = data.get("GroupId") + "";
                                        listInfo.add(userinfo);
                                        Log.i("组内成员人数", listInfo.size() + "");
                                        tv_num.setText(listInfo.size() + "");
                                        getGridViewPerson(groupids);
                                        //有人加入组
                                        ToastUtils.show_short(context, "有人加入组");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (command2 == 5) {
                                    //有人退出组
                                    try {
                                        MapContent data = (MapContent) message.getMsgContent();
                                        Map<String, Object> map = data.getContentMap();
                                        String news = new Gson().toJson(map);

                                        JSONTokener jsonParser = new JSONTokener(news);
                                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                        String userinfos = arg1.getString("UserInfo");

                                        ListInfo userinfo = new Gson().fromJson(userinfos, new TypeToken<ListInfo>() {
                                        }.getType());

                                        String userinfoid = userinfo.getUserId();
                                        String groupids = data.get("GroupId") + "";
                                        for (int i = 0; i < listInfo.size(); i++) {
                                            if (listInfo.get(i).getUserId().equals(userinfoid)) {
                                                listInfo.remove(i);
                                            }
                                        }
                                        Log.i("组内成员人数", listInfo.size() + "");
                                        tv_num.setText(listInfo.size() + "");
                                        getGridViewPerson(groupids);
                                        ToastUtils.show_short(context, "有人退出组");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            default:
                                break;
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void jack() {
        //抬手后的操作
        if (isTalking) {
            if (interPhoneType.equals("group")) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        VoiceStreamRecordService.stop();
                        InterPhoneControlHelper.Loosen(context, interPhoneId);//发送取消说话控制
                        if (draw_group.isRunning()) {
                            draw_group.stop();
                        }
                        Log.e("对讲页面====", "录音机停止+发送取消说话控制+延时0.30秒");
                    }
                }, 300);
            } else {//此处处理个人对讲的逻辑
                VoiceStreamRecordService.stop();
                InterPhoneControlHelper.PersonTalkPressStop(context);//发送取消说话控制
                if (draw.isRunning()) {
                    draw.stop();
                }
            }
        } else {
            VoiceStreamRecordService.stop();
        }
    }

    public static void press() {
        // 按下的动作
        if (interPhoneType.equals("group")) {
            InterPhoneControlHelper.Press(context, interPhoneId);
            VoiceStreamRecordService.stop();
            VoiceStreamRecordService.start(context, interPhoneId, "group");
        } else {
            //此处处理个人对讲的逻辑
            InterPhoneControlHelper.PersonTalkPressStart(context);
            VoiceStreamRecordService.stop();
            VoiceStreamRecordService.start(context, interPhoneId, "person");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
        isVisible=false;
    }
}
