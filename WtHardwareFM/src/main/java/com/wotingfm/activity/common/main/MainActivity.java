package com.wotingfm.activity.common.main;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.activity.common.favoritetype.FavoriteProgramTypeActivity;
import com.wotingfm.activity.im.interphone.main.DuiJiangActivity;
import com.wotingfm.activity.mine.main.MineActivity;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.program.citylist.dao.CityInfoDao;
import com.wotingfm.activity.music.program.fenlei.model.Catalog;
import com.wotingfm.activity.music.program.fenlei.model.CatalogName;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.devicecontrol.WtDeviceControl;
import com.wotingfm.manager.MyActivityManager;
import com.wotingfm.manager.UpdateManager;
import com.wotingfm.service.TimeOffService;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ScreenUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页
 * 作者：xinlong on 2016/8/23 22:59
 * 邮箱：645700751@qq.com
 */
public class MainActivity extends TabActivity {
    private MainActivity context;
    public static TabHost tabHost;
    private Dialog upDataDialog;     // 版本更新弹出框

    private String tag = "MAIN_VOLLEY_REQUEST_CANCEL_TAG";
    private String upDataNews;       // 版本更新内容
    private String mPageName = "MainActivity";
    private int upDataType = 1;      // 1,不需要强制升级2，需要强制升级
    private boolean isCancelRequest;
    private List<CatalogName> list;

    private CityInfoDao CID;         // 城市列表数据库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    //透明导航栏
        tabHost = extracted();
        context = this;
        WtDeviceControl mControl = new WtDeviceControl(context);
        GlobalConfig.device = mControl;

        if(!BSApplication.SharedPreferences.getBoolean(StringConstant.FAVORITE_PROGRAM_TYPE, false)) {
            startActivity(new Intent(context, FavoriteProgramTypeActivity.class));
        }

        MobclickAgent.openActivityDurationTrack(false);//友盟的数据统计
        update();           // 获取版本数据
        InitTextView();     // 设置界面
        InitDao();          // 加载数据库
        registerReceiver(); // 注册广播
        //mask();           // 蒙版
    }

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

    /**
     * 切换到音乐播放界面
     */
    public static void changeToMusic() {
        tabHost.setCurrentTabByTag("two");
    }

    //加载数据库
    private void InitDao() {
        CID = new CityInfoDao(context);
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            // 发送获取城市列表的网络请求
            sendRequest();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 初始化视图,主页跳转的3个界面
    private void InitTextView() {
        tabHost.addTab(tabHost.newTabSpec("one").setIndicator("one")
                .setContent(new Intent(this, HomeActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("two").setIndicator("two")
                .setContent(new Intent(this, DuiJiangActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("three").setIndicator("three")
                .setContent(new Intent(this, MineActivity.class)));
        tabHost.setCurrentTabByTag("one");
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
                                        list = CID.queryCityInfo();
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

    //注册广播  用于接收定时服务发送过来的广播
    private void registerReceiver() {
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(BroadcastConstants.TIMER_END);
        myFilter.addAction(BroadcastConstants.ACTIVITY_CHANGE);
        registerReceiver(endApplicationBroadcast, myFilter);
    }

    //接收定时服务发送过来的广播  用于结束应用
    private BroadcastReceiver endApplicationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.TIMER_END)) {
                ToastUtils.show_always(MainActivity.this, "定时关闭应用时间就要到了，应用即将退出");
                stopService(new Intent(MainActivity.this, TimeOffService.class));    // 停止服务
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            } else if (action.equals(BroadcastConstants.ACTIVITY_CHANGE)) {
                if (GlobalConfig.activitytype == 1) {
                    MyActivityManager mam = MyActivityManager.getInstance();
                    mam.finishAllActivity();
                    tabHost.setCurrentTabByTag("one");
                } else if (GlobalConfig.activitytype == 2) {
                    MyActivityManager mam = MyActivityManager.getInstance();
                    mam.finishAllActivity();
                    tabHost.setCurrentTabByTag("two");
                } else {
                    MyActivityManager mam = MyActivityManager.getInstance();
                    mam.finishAllActivity();
                    tabHost.setCurrentTabByTag("three");
                }
            }
        }
    };

    private TabHost extracted() {
        return getTabHost();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(context);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(context);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        unregisterReceiver(endApplicationBroadcast);    // 取消注册广播
//        BSApplication.onStop();
        Log.v("--- Main ---", "--- 杀死进程 ---");
        //		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //		manager.killBackgroundProcesses("com.woting");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
