package com.wotingfm.ui.main;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.devicecontrol.WtDeviceControl;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.common.manager.UpdateManager;
import com.wotingfm.common.receiver.NetWorkChangeReceiver;
import com.wotingfm.common.service.FloatingWindowService;
import com.wotingfm.common.service.LocationService;
import com.wotingfm.common.service.NotificationService;
import com.wotingfm.common.service.SocketService;
import com.wotingfm.common.service.SubclassService;
import com.wotingfm.common.service.TestWindowService;
import com.wotingfm.common.service.VoiceStreamPlayerService;
import com.wotingfm.common.service.VoiceStreamRecordService;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.favoritetype.FavoriteProgramTypeActivity;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.ui.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.ui.interphone.common.message.MessageUtils;
import com.wotingfm.ui.interphone.common.message.MsgNormal;
import com.wotingfm.ui.interphone.common.message.content.MapContent;
import com.wotingfm.ui.interphone.common.model.CallerInfo;
import com.wotingfm.ui.interphone.common.model.Data;
import com.wotingfm.ui.interphone.common.model.MessageForMainGroup;
import com.wotingfm.ui.interphone.group.creategroup.model.Freq;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.main.DuiJiangFragment;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.mine.person.login.LoginActivity;
import com.wotingfm.ui.music.download.service.DownloadService;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.program.citylist.dao.CityInfoDao;
import com.wotingfm.ui.music.program.fenlei.model.Catalog;
import com.wotingfm.ui.music.program.fenlei.model.CatalogName;
import com.wotingfm.ui.music.search.main.SearchLikeActivity;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.JsonEncloseUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ScreenUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 主页
 * 作者：xinlong on 2016/8/23 22:59
 * 邮箱：645700751@qq.com
 */
public class MainActivity extends TabActivity {
    private static MainActivity context;
    public static TabHost tabHost;
    private Dialog upDataDialog;     // 版本更新弹出框

    private String tag = "MAIN_VOLLEY_REQUEST_CANCEL_TAG";
    private String upDataNews;       // 版本更新内容
    private String mPageName = "MainActivity";
    private int upDataType = 1;      // 1,不需要强制升级2，需要强制升级
    private boolean isCancelRequest;

    private static Intent Socket, VoiceStreamRecord, VoiceStreamPlayer, Location, Subclass, Download, Notification, TestFloatingWindow, FloatingWindow;

    private NetWorkChangeReceiver netWorkChangeReceiver = null;

    private CityInfoDao CID;         // 城市列表数据库
    // 消息通知
    public static GroupInfo groupInfo;
    private String callId, callerId;
    public static DBTalkHistorary talkdb;
    private SearchTalkHistoryDao talkDao;
    private Intent Simulation;
    public static int SearchLikeActivityJumpType=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabHost = extracted();
        context = this;
        SpeechUtility.createUtility(context, "appid=58116950");
        MobclickAgent.openActivityDurationTrack(false);//友盟的数据统计

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createService();    // 启动服务
                registerReceiver(); // 注册广播
            }
        }, 0);

        update();           // 获取版本数据
        InitTextView();     // 设置界面
        InitData();         // 加载数据
        setType();
        // mask();          // 蒙版

        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        // Setting.setShowLog(false);

        WtDeviceControl mControl = new WtDeviceControl(context);
        GlobalConfig.device = mControl;

        // 是否是第一次打开该应用----打开偏好设置
        if (!BSApplication.SharedPreferences.getBoolean(StringConstant.FAVORITE_PROGRAM_TYPE, false)) {
            startActivity(new Intent(context, FavoriteProgramTypeActivity.class));
        }
    }

    private void createService() {
        Socket = new Intent(this, SocketService.class);  //socket服务
        startService(Socket);
        VoiceStreamRecord = new Intent(this, VoiceStreamRecordService.class);  //录音服务
        startService(VoiceStreamRecord);
        VoiceStreamPlayer = new Intent(this, VoiceStreamPlayerService.class);//播放服务
        startService(VoiceStreamPlayer);
        Location = new Intent(this, LocationService.class);//定位服务
        startService(Location);
        Subclass = new Intent(this, SubclassService.class);
        startService(Subclass);
        Download = new Intent(this, DownloadService.class);
        startService(Download);
        Notification = new Intent(this, NotificationService.class);
        startService(Notification);
        FloatingWindow = new Intent(this, FloatingWindowService.class);//启动全局弹出框服务
        startService(FloatingWindow);
//        Simulation=new Intent(this,SimulationService.class);
//        startService(Simulation);
        TestFloatingWindow = new Intent(this, TestWindowService.class);//启动全局弹出框服务
        startService(TestFloatingWindow);
    }

    //注册广播  用于接收定时服务发送过来的广播
    private void registerReceiver() {

        IntentFilter m = new IntentFilter();
        m.addAction(BroadcastConstants.PUSH_REGISTER);
        m.addAction(BroadcastConstants.PUSH_NOTIFY);
        m.addAction(BroadcastConstants.ACTIVITY_CHANGE);
        m.addAction(BroadcastConstants.PUSH);
        registerReceiver(endApplicationBroadcast, m);

        IntentFilter n = new IntentFilter();
        n.addAction(NetWorkChangeReceiver.intentFilter);
        netWorkChangeReceiver = new NetWorkChangeReceiver(this);
        registerReceiver(netWorkChangeReceiver, n);

    }


    private void setType() {
        try {
            String a = android.os.Build.VERSION.RELEASE;
            Log.e("系统版本号", a + "");
            Log.e("系统版本号截取", a.substring(0, a.indexOf(".")) + "");
            boolean v = false;
            if (Integer.parseInt(a.substring(0, a.indexOf("."))) >= 5) {
                v = true;
            }
            if (v) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        //透明状态栏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    //透明导航栏
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //接收定时服务发送过来的广播  用于结束应用
    private BroadcastReceiver endApplicationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.ACTIVITY_CHANGE)) {
                // 按钮切换-----档位切换广播
                if (GlobalConfig.activityType == 1) {
                    if (ProgramActivity.context != null) {
                        tabHost.setCurrentTabByTag("four");
                    } else {
                        tabHost.setCurrentTabByTag("one");
                    }
                } else if (GlobalConfig.activityType == 2) {
                    tabHost.setCurrentTabByTag("two");
                } else {
                    tabHost.setCurrentTabByTag("three");
                }
            } else if (intent.getAction().equals(BroadcastConstants.PUSH)) {

                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("mainActivity接收器中数据=原始数据", Arrays.toString(bt) + "");
                try {
                    MsgNormal outMessage = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    if (outMessage != null) {
                        int biztype = outMessage.getBizType();
                        if (biztype == 1) {
                            // 上次存在的组对讲消息
                            int cmdType = outMessage.getCmdType();
                            if (cmdType == 3) {
                                int command = outMessage.getCommand();
                                if (command == 0) {
                                    int rtType = outMessage.getReturnType();
                                    if (rtType != 0) {
                                        Log.e("mainActivity接收器中数据=组", JsonEncloseUtils.btToString(bt) + "");
                                        try {
                                            MapContent data = (MapContent) outMessage.getMsgContent();
                                            Map<String, Object> map = data.getContentMap();
                                            String news = new Gson().toJson(map);

                                            JSONTokener jsonParser = new JSONTokener(news);
                                            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                            String groupList = arg1.getString("GroupList");

                                            try {
                                                List<MessageForMainGroup> gList = new Gson().fromJson(groupList, new TypeToken<List<MessageForMainGroup>>() {
                                                }.getType());
                                                if (gList != null && gList.size() > 0) {
                                                    try {
                                                        MessageForMainGroup gInfo = gList.get(0);   // 本版本（2017元旦前）暂时只获取第一条数据，后续需要修改
                                                        groupInfo = gInfo.getGroupInfo();
//                                                    userIds = gInfo.getGroupEntryUserIds();
                                                        String groupName = groupInfo.getGroupName();
                                                        showGroup(groupName); // 展示上次存在的对讲组
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } else if (biztype == 2) {
                            // 上次存在的单对单消息
                            int cmdType = outMessage.getCmdType();
                            if (cmdType == 3) {
                                int command = outMessage.getCommand();
                                if (command == 0) {
                                    int rtType = outMessage.getReturnType();
                                    if (rtType != 0) {
                                        Log.e("mainActivity接收器中数据=单", JsonEncloseUtils.btToString(bt) + "");

                                        try {
                                            MapContent data = (MapContent) outMessage.getMsgContent();
                                            Map<String, Object> map = data.getContentMap();
                                            String news = new Gson().toJson(map);

                                            JSONTokener jsonParser = new JSONTokener(news);
                                            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                            List<Data> userList = null;
                                            try {
                                                String callingList = arg1.getString("CallingList");

                                                userList = new Gson().fromJson(callingList, new TypeToken<List<Data>>() {
                                                }.getType());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (userList != null && userList.size() > 0) {
                                                Data userInfo = userList.get(0);   // 本版本（2017元旦前）暂时只获取第一条数据，后续需要修改
                                                try {
                                                    callId = userInfo.getCallId(); // 本次通信的id

                                                    if (CommonUtils.getUserIdNoImei(context) != null && !CommonUtils.getUserIdNoImei(context).equals("")) {
                                                        if (CommonUtils.getUserIdNoImei(context).equals(userInfo.getCallerId())) {
                                                            // 此次呼叫是"我"主动呼叫别人，所以callerId就是自己==主叫方
                                                            callerId = userInfo.getCallerId();
                                                            try {
//                                                                String callerInfo = arg1.getString("CallerInfo");
//                                                                CallerInfo caller = new Gson().fromJson(callerInfo, new TypeToken<CallerInfo>() {
//                                                                 }.getType());

                                                                CallerInfo caller = userInfo.getCallerInfo();
                                                                String name = caller.getUserName();
                                                                showPerson(name); // 展示上次存在的单对单对讲

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        } else if (CommonUtils.getUserIdNoImei(context).equals(userInfo.getCallederId())) {
                                                            // 此次呼叫是"我"被别人呼叫，所以callerId就是对方==被叫方
                                                            callerId = userInfo.getCallederId();
                                                            try {
//                                                                String callederInfo = arg1.getString("CallederInfo");
//                                                                CallerInfo calleder = new Gson().fromJson(callederInfo, new TypeToken<CallerInfo>() {
//                                                                }.getType());
                                                                CallerInfo calleder = userInfo.getCallederInfo();
                                                                String name = calleder.getUserName();
                                                                showPerson(name); // 展示上次存在的单对单对讲
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equals(BroadcastConstants.PUSH_NOTIFY)) {
                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("mainActivity接收器中数据=原始数据", Arrays.toString(bt) + "");
                try {
                    MsgNormal outMessage = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    if (outMessage != null) {
                        int biztype = outMessage.getBizType();
                        if (biztype == 4) {
                            // 上次存在的组对讲消息
                            int cmdType = outMessage.getCmdType();
                            if (cmdType == 3) {
                                int command = outMessage.getCommand();
                                if (command == 1) {
                                    Log.e("mainActivity接收器中数据=踢人", JsonEncloseUtils.btToString(bt) + "");
                                    unRegisterLogin();
                                    showQuitPerson();// 展示账号被顶替的弹出框
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equals(BroadcastConstants.PUSH_REGISTER)) {
                // 注册消息，biztype=15，returnType为0是没有登录，为1是登录状态
                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("mainActivity接收器中数据=原始数据", Arrays.toString(bt) + "");
                try {
                    MsgNormal outMessage = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    if (outMessage != null) {
                        int biztype = outMessage.getBizType();
                        if (biztype == 15) {
                            int rtType = outMessage.getReturnType();
                            if (rtType != 0) {
                                // 此时是登录状态,不需要处理
                            } else {
                                // 此时是未登录状态,更改一下登录状态
                                unRegisterLogin();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void mask() {
        final WindowManager windowManager = getWindowManager();
        final ImageView img = new ImageView(this);          // 动态初始化图层
        img.setLayoutParams(new WindowManager.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        Bitmap bmp = BitmapUtils.readBitMap(this, R.mipmap.ee);
        img.setImageBitmap(bmp);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(); // 设置LayoutParams参数
        // 设置显示的类型，TYPE_PHONE指的是来电话的时候会被覆盖，其他时候会在最前端，显示位置在stateBar下面，其他更多的值请查阅文档
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;               // 设置显示格式
        params.gravity = Gravity.LEFT | Gravity.TOP;         // 设置对齐方式
        params.width = ScreenUtils.getScreenWidth(this);     // 设置宽高
        params.height = ScreenUtils.getScreenHeight(this);
        windowManager.addView(img, params);                  // 添加到当前的窗口上
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                windowManager.removeView(img);
            }
        });                                                  // 点击图层之后，将图层移除
    }

    //加载数据库以及请求数据
    private void InitData() {
        CID = new CityInfoDao(context);
        talkDao = new SearchTalkHistoryDao(context);
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            // 发送获取城市列表的网络请求
            sendRequest();
            sendFreq();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    public static void changeOne() {
        tabHost.setCurrentTabByTag("one");
    }

    public static void changeTwo() {
        tabHost.setCurrentTabByTag("two");
    }

    public static void changeThree() {
        tabHost.setCurrentTabByTag("three");
    }

    public static void changeFour() {
        tabHost.setCurrentTabByTag("four");
    }

    public static void changeFive() {
        tabHost.setCurrentTabByTag("five");
    }

    // 初始化视图,主页跳转的3个界面
    private void InitTextView() {
        tabHost.addTab(tabHost.newTabSpec("one").setIndicator("one")
                .setContent(new Intent(this, PlayerActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("two").setIndicator("two")
                .setContent(new Intent(this, DuiJiangActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("three").setIndicator("three")
                .setContent(new Intent(this, MineActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("four").setIndicator("four")
                .setContent(new Intent(this, ProgramActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("five").setIndicator("five")
                .setContent(new Intent(this, SearchLikeActivity.class)));
    }

    //获取版本数据---检查是否需要更新
    private void update() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Version", PhoneMessage.appVersonName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.VersionUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) {
                    return;
                }
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001")) {
                            try {
                                GlobalConfig.apkUrl = result.getString("DownLoadUrl");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String MastUpdate = null;
                            try {
                                MastUpdate = result.getString("MastUpdate");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String ResultList = null;
                            try {
                                ResultList = result.getString("CurVersion");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            if (ResultList != null && MastUpdate != null) {
                                dealVersion(ResultList, MastUpdate);
                            } else {
                                Log.e("检查更新返回值", "返回值为1001，但是返回的数值有误");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(tag + "检查更新异常", e.toString());
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                Log.e(tag + "检查更新网络异常", error.toString());
            }
        });
    }

    //发送获取城市列表的网络请求
    private void sendRequest() {
        //设置获取城市列表的请求参数
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "2");
            jsonObject.put("ResultType", "1");
            jsonObject.put("RelLevel", "0");
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                // 如果网络请求已经执行取消操作  就表示就算请求成功也不需要数据返回了  所以方法就此结束
                if (isCancelRequest) {
                    return;
                }
                try {
                    String ReturnType = result.getString("ReturnType");
                    // 根据返回值来对程序进行解析
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001")) {
                            try {
                                // 获取列表
                                String ResultList = result.getString("CatalogData");
                                Catalog SubList_all = new Gson().fromJson(ResultList, new TypeToken<Catalog>() {
                                }.getType());
                                List<CatalogName> srcList = SubList_all.getSubCata();
                                if (srcList != null) {
                                    if (srcList.size() == 0) {
                                        ToastUtils.show_short(context, "获取城市列表为空");
                                    } else {
                                        //组装从后台获取到的数据
                                        List<CatalogName> mList = new ArrayList<>();
                                        for (int i = 0; i < srcList.size(); i++) {
                                            CatalogName mFenLeiName = new CatalogName();
                                            mFenLeiName.setCatalogId(srcList.get(i).getCatalogId());
                                            mFenLeiName.setCatalogName(srcList.get(i).getCatalogName());
                                            mList.add(mFenLeiName);
                                        }
                                        //获取数据库中的地理位置数据
                                        List<CatalogName> list = CID.queryCityInfo();
                                        if (list.size() == 0) {
                                            if (mList.size() != 0)
                                                CID.InsertCityInfo(mList);  //将数据写入数据库
                                        } else {
                                            //此处要对数据库查询出的list和获取的mList进行去重
                                            CID.DelCityInfo();
                                            if (mList.size() != 0) CID.InsertCityInfo(mList);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("MainActivity获取城市列表异常", e.toString() + "");
                            }
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_short(context, "无此分类信息");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_short(context, "分类不存在");
                        } else if (ReturnType.equals("1011")) {
                            ToastUtils.show_short(context, "当前暂无分类");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_short(context, "获取列表异常");
                        }
                    } else {
                        ToastUtils.show_short(context, "数据获取异常，请稍候重试");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MainActivity获取城市列表异常", e.toString() + "");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                Log.e("MainActivity获取城市列表异常", error.toString() + "");
            }
        });
    }

    // 获取对讲的频率，存储在Manifest当中
    private void sendFreq() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "11");
            jsonObject.put("ResultType", "2");
            jsonObject.put("RelLevel", "0");
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (!TextUtils.isEmpty(ReturnType)) {
                        if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                            try {
                                String ResultList = result.getString("CatalogData");
                                List<Freq> freqList = new Gson().fromJson(ResultList, new TypeToken<List<Freq>>() {
                                }.getType());
                                GlobalConfig.FreqList = freqList;
                                GlobalConfig.getFreq = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
            }
        });

    }

    //检查版本更新
    private void dealVersion(String ResultList, String mastUpdate) {
        String Version = "0.1.0.X.0";
        String Desc = null;
        try {
            JSONTokener jsonParser = new JSONTokener(ResultList);
            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
            Version = arg1.getString("Version");
            //String AppName = arg1.getString("AppName");
            Desc = arg1.getString("Descn");
            //String BugPatch = arg1.getString("BugPatch");
            //String ApkSize = arg1.getString("ApkSize");
            //String PubTime = arg1.getString("Version");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 版本更新比较
        String version = Version;
        String[] strArray;
        strArray = version.split("\\.");
        //String verson_big = strArray[0].toString();//大版本
        //String verson_medium = strArray[1].toString();//中版本
        //String verson_small = strArray[2].toString();//小版本
        //String verson_x = strArray[3];//X
        String version_build;
        try {
            version_build = strArray[4];
            int version_old = PhoneMessage.versionCode;
            int version_new = Integer.parseInt(version_build);
            if (version_new > version_old) {
                if (mastUpdate != null && mastUpdate.equals("1")) {
                    //强制升级
                    if (Desc != null && !Desc.trim().equals("")) {
                        upDataNews = Desc;
                    } else {
                        upDataNews = "本次版本升级较大，需要更新";
                    }
                    upDataType = 2;
                    UpdateDialog();
                    upDataDialog.show();
                } else {
                    //普通升级
                    if (Desc != null && !Desc.trim().equals("")) {
                        upDataNews = Desc;
                    } else {
                        upDataNews = "有新的版本需要升级喽";
                    }
                    upDataType = 1;//不需要强制升级
                    UpdateDialog();
                    upDataDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("版本处理异常", e.toString() + "");
        }
    }

    //版本更新对话框
    private void UpdateDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);
        TextView text_content = (TextView) dialog.findViewById(R.id.text_content);
        text_content.setText(Html.fromHtml("<font size='26'>" + upDataNews + "</font>"));
        TextView tv_update = (TextView) dialog.findViewById(R.id.tv_update);
        TextView tv_qx = (TextView) dialog.findViewById(R.id.tv_qx);
        upDataDialog = new Dialog(this, R.style.MyDialog);
        upDataDialog.setContentView(dialog);
        upDataDialog.setCanceledOnTouchOutside(false);
        upDataDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);

        //开始更新
        tv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okUpData();
                upDataDialog.dismiss();
            }
        });

        //取消更新
        tv_qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (upDataType == 1) {
                    upDataDialog.dismiss();
                } else {
                    ToastUtils.show_always(MainActivity.this, "本次需要更新");
                }
            }
        });
    }

    // 调用更新功能
    private void okUpData() {
        UpdateManager updateManager = new UpdateManager(this);
        updateManager.checkUpdateInfo1();
    }

    private TabHost extracted() {
        return getTabHost();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(context);
    }


    /**
     * app退出时执行该操作
     */
    public void stop() {
        context.stopService(Socket);
        context.stopService(VoiceStreamRecord);
        context.stopService(VoiceStreamPlayer);
        context.stopService(Location);
        context.stopService(Subclass);
        context.stopService(Download);
        context.stopService(Notification);
        context.stopService(Simulation);
        context.stopService(FloatingWindow);
        context.stopService(TestFloatingWindow);
        Log.e("app退出", "app退出");
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(context);
//        wm.removeView(tv);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        wm.addView(tv,lp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        unregisterReceiver(netWorkChangeReceiver);
        unregisterReceiver(endApplicationBroadcast);    // 取消注册广播
        stop();
        Log.v("--- Main ---", "--- 杀死进程 ---");
        //		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //		manager.killBackgroundProcesses("com.woting");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // 展示账号被顶替的弹出框
    private void showQuitPerson() {
        pushDialog("下线通知", "您的账号在其他设备上登录，是否重新登录？", 1);
        ToastUtils.show_short(MainActivity.this, "展示账号被顶替的 消息");
    }

    // 展示上次存在的单对单消息
    private void showPerson(String personName) {
        pushDialog("对讲通知", "您刚刚在跟" + personName + "通话，是否继续？", 2);
        ToastUtils.show_short(MainActivity.this, "展示上次存在的单对单消息");
    }

    // 展示上次存在的组对讲消息
    private void showGroup(String groupName) {
        pushDialog("对讲通知", "您刚刚在" + groupName + "对讲组中聊天，是否继续？", 3);
        ToastUtils.show_short(MainActivity.this, "展示上次存在的组对讲消息");
    }

    // 更改一下登录状态
    private void unRegisterLogin() {
        // 发送全局广播，更改所有界面为未登录状态
        context.sendBroadcast(new Intent(BroadcastConstants.PUSH_ALLURL_CHANGE));
        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
        et.putString(StringConstant.ISLOGIN, "false");
        et.putString(StringConstant.USERID, "");
        et.putString(StringConstant.USER_NUM, "");
        et.putString(StringConstant.IMAGEURL, "");
        et.putString(StringConstant.PHONENUMBER, "");
        et.putString(StringConstant.USER_NUM, "");
        et.putString(StringConstant.GENDERUSR, "");
        et.putString(StringConstant.EMAIL, "");
        et.putString(StringConstant.REGION, "");
        et.putString(StringConstant.BIRTHDAY, "");
        et.putString(StringConstant.USER_SIGN, "");
        et.putString(StringConstant.STAR_SIGN, "");
        et.putString(StringConstant.AGE, "");
        et.putString(StringConstant.NICK_NAME, "");
        if (!et.commit()) {
            Log.v("commit", "数据 commit 失败!");
        }
    }

    //消息通知对话框
    private void pushDialog(String title, String message, final int type) {
        //type 0=默认值,1=被顶替,2=展示个人,3=展示群组

        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_push_message, null);
        TextView push_dialog_text_context = (TextView) dialog.findViewById(R.id.text_context);// 展示内容
        TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);// 展示标题
        TextView tv_update = (TextView) dialog.findViewById(R.id.tv_ok);
        TextView tv_qx = (TextView) dialog.findViewById(R.id.tv_qx);

        push_dialog_text_context.setText("" + message);
        tv_title.setText("" + title);

        final Dialog pushDialog = new Dialog(this, R.style.MyDialog);
        pushDialog.setContentView(dialog);
        pushDialog.setOnKeyListener(keyListener);
        pushDialog.setCanceledOnTouchOutside(false);
        pushDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        pushDialog.show();
//        if (type == 1) {
//            dialogShowTypeQuitPerson = 1;
//        } else if (type == 2) {
//            dialogShowTypePerson = 1;
//        } else if (type == 3) {
//            dialogShowTypeGroup = 1;
//        }
        // 取消
        tv_qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
                    // 不需要处理
//                    dialogShowTypeQuitPerson = 0;
                } else if (type == 2) {
                    // 挂断电话
//                    dialogShowTypePerson = 0;
                    InterPhoneControlHelper.PersonTalkHangUp(context, callId);
                } else if (type == 3) {
                    // 退出组
//                    dialogShowTypeGroup = 0;
                    InterPhoneControlHelper.Quit(context, groupInfo.getGroupId());//退出小组
                }
                pushDialog.dismiss();
            }
        });

        // 继续
        tv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
//                    dialogShowTypeQuitPerson = 0;
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else if (type == 2) {
//                    dialogShowTypePerson = 0;
                    addUser();
                } else if (type == 3) {
//                    dialogShowTypeGroup = 0;
                    addGroup();
                }
                pushDialog.dismiss();
            }
        });

    }

    DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                return true;
            } else {
                return false;
            }
        }
    };

    /*
     * 把此时还在对讲状态的组对讲数据设置活跃状态
     */
    public void addGroup() {
        //对讲主页界面更新
        changeTwo();
        DuiJiangFragment.update();
    }

    /*
     * 把此时还在对讲状态的单对单数据设置活跃状态
     */
    private void addUser() {
        InterPhoneControlHelper.bdcallid = callId;
        //获取最新激活状态的数据
        String addTime = Long.toString(System.currentTimeMillis());
        String bjuserid = CommonUtils.getUserId(context);
        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
        talkDao.deleteHistory(callerId);
        Log.e("=====callerid======", callerId + "");
        DBTalkHistorary history = new DBTalkHistorary(bjuserid, "user", callerId, addTime);
        talkDao.addTalkHistory(history);
        talkdb = talkDao.queryHistory().get(0);//得到数据库里边数据
        //对讲主页界面更新
        MainActivity.changeTwo();
        DuiJiangFragment.update();
    }

    // 设置android app 的字体大小不受系统字体大小改变的影响
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
