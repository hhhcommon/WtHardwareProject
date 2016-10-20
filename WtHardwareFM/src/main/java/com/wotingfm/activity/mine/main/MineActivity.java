package com.wotingfm.activity.mine.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shenstec.http.MyHttp;
import com.shenstec.utils.file.FileManager;
import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.activity.common.preference.activity.PreferenceActivity;
import com.wotingfm.activity.im.interphone.creategroup.model.UserPortaitInside;
import com.wotingfm.activity.im.interphone.creategroup.photocut.activity.PhotoCutActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.mine.about.AboutActivity;
import com.wotingfm.activity.mine.bluetooth.BluetoothActivity;
import com.wotingfm.activity.mine.feedback.activity.FeedbackActivity;
import com.wotingfm.activity.mine.flowmanage.FlowManageActivity;
import com.wotingfm.activity.mine.fm.FMConnectActivity;
import com.wotingfm.activity.mine.help.HelpActivity;
import com.wotingfm.activity.mine.qrcode.EWMShowActivity;
import com.wotingfm.activity.mine.update.activity.UpdatePersonActivity;
import com.wotingfm.activity.mine.wifi.WIFIActivity;
import com.wotingfm.activity.person.login.activity.LoginActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.ImageLoader;
import com.wotingfm.manager.CacheManager;
import com.wotingfm.manager.UpdateManager;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.L;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;

/**
 * 个人信息主页
 */
public class MineActivity extends Activity implements OnClickListener {
    private MineActivity context;
    public DeviceReceiver mDevice = new DeviceReceiver();
    public static BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
    public static WifiManager wifiManager;
    private UserPortaitInside UserPortait;
    private ImageLoader imageLoader;

    private Dialog dialog;                          // 加载数据对话框
    protected Dialog imageDialog;                   // 修改头像对话框
    private Dialog updateDialog;                    // 更新对话框
    private Dialog clearCacheDialog;                // 清除缓存对话框
    private Dialog exitLoginDialog;                 // 退出登录对话框

    private RelativeLayout relativeStatusUnLogin;   // 未登录状态
    private RelativeLayout relativeStatusLogin;     // 登录状态
    private ImageView userHead;                     // 用户头像
    private TextView textWifiName;
    private TextView textBluetoothState;            // 蓝牙状态 打开 OR 关闭
    private TextView textCache;                     // 缓存统计
    private TextView textUserName;                  // 用户名
    private Button exitLogin;                       // 退出登录

    private String ReturnType;
    private String MiniUri;
    private String outputFilePath;
    private String filePath;
    private String tag = "MINE_REQUEST_CANCEL_TAG"; // 取消网络请求标签
    private String updateContent;                   // 更新内容
    private String cachePath;                       // 缓存路径
    private String cache;                           // 缓存
    private String url;
    private String userId;
    private String userName;
    private String PhotoCutAfterImagePath;
    private String isLogin;                         // 判断是否登录

    private final int TO_GALLERY = 1;               // 打开图库
    private final int TO_CAMERA = 2;                // 打开照相机
    private int updateType = 1;                     // 版本更新类型
    private int imageNum;
    private boolean isCancelRequest;
    private boolean hasRegister = false;
    private boolean isFirst = true;                 // 第一次加载界面

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        context = this;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);             // 获取 WiFi 服务
        imageLoader = new ImageLoader(context);
        clearCacheDialog();     // 清除缓存对话框
        exitLoginDialog();      // 退出登录对话框
        setView();              // 设置界面
        imageDialog();          // 更换头像对话框
        getBluetoothState();    // 获取蓝牙的打开关闭状态
        initCache();            // 启动统计缓存的线程
    }

    // 设置 view
    private void setView() {
        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.img_person_background);
        ImageView loginBackgroundImage = (ImageView) findViewById(R.id.lin_image);      // 登录背景图片
        loginBackgroundImage.setImageBitmap(bmp);
        ImageView lin_image_0 = (ImageView) findViewById(R.id.lin_image_0);             // 未登录背景图片
        lin_image_0.setImageBitmap(bmp);

        textUserName = (TextView) findViewById(R.id.text_user_name);                    // 用户名
        userHead = (ImageView) findViewById(R.id.image_touxiang);                       // 用户头像
        userHead.setOnClickListener(this);

        ImageView imageViewEwm = (ImageView) findViewById(R.id.imageView_ewm);          // 二维码
        imageViewEwm.setOnClickListener(this);

        relativeStatusUnLogin = (RelativeLayout) findViewById(R.id.lin_status_nodenglu);// 未登录时的状态
        relativeStatusUnLogin.setOnClickListener(this);

        relativeStatusLogin = (RelativeLayout) findViewById(R.id.lin_status_denglu);    // 登录时的状态
        relativeStatusLogin.setOnClickListener(this);

        exitLogin = (Button) findViewById(R.id.exit_login);     // 退出
        exitLogin.setOnClickListener(this);

        View aboutWt = findViewById(R.id.about_set);            // 关于
        aboutWt.setOnClickListener(this);

        View feedbackView = findViewById(R.id.feedback_set);    // 反馈建议
        feedbackView.setOnClickListener(this);

        View userHelp = findViewById(R.id.help_set);            // 使用帮助
        userHelp.setOnClickListener(this);

        View checkUpdate = findViewById(R.id.update_set);       // 检查更新
        checkUpdate.setOnClickListener(this);
        TextView textVersionNumber = (TextView) findViewById(R.id.text_update_statistics);// 版本号
        textVersionNumber.setText(PhoneMessage.appVersonName);

        View clearCache = findViewById(R.id.cache_set);         // 清除缓存
        clearCache.setOnClickListener(this);
        textCache = (TextView) findViewById(R.id.text_cache_statistics);                // 缓存统计

        View flowManager = findViewById(R.id.flow_set);         // 流量管理
        flowManager.setOnClickListener(this);

        View likeSet = findViewById(R.id.like_set);             // 喜好设置
        likeSet.setOnClickListener(this);

        View bluetoothSet = findViewById(R.id.bluetooth_set);   // 蓝牙设置
        bluetoothSet.setOnClickListener(this);
        textBluetoothState = (TextView) findViewById(R.id.text_bluetooth_state);        // 蓝牙的状态 打开 OR 关闭

        View wifiSet = findViewById(R.id.wifi_set);             // WIFI设置
        wifiSet.setOnClickListener(this);
        textWifiName = (TextView) findViewById(R.id.text_wifi_name);                    // 连接的WIFI的名字

        View channelSet = findViewById(R.id.listener_set);      // 频道设置
        channelSet.setOnClickListener(this);
//        TextView textChannel = (TextView) findViewById(R.id.text_listener_frequency);   // 频率
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_gallery:               // 从手机相册选择
                doDialogClick(0);
                imageDialog.dismiss();
                break;
            case R.id.tv_camera:                // 拍照
                doDialogClick(1);
                imageDialog.dismiss();
                break;
            case R.id.lin_status_nodenglu:      // 登陆
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.lin_status_denglu:        // 修改个人资料
                startActivity(new Intent(context, UpdatePersonActivity.class));
                break;
            case R.id.imageView_ewm:            // 二维码
                UserInfo news = new UserInfo();
                news.setPortraitMini(url);
                news.setUserId(userId);
                news.setUserName(userName);
                Intent intentEwm = new Intent(context,EWMShowActivity.class);
                Bundle bundle =  new Bundle();
                bundle.putString("type", "1");
                bundle.putString("news","");
                bundle.putSerializable("person", news);
                intentEwm.putExtras(bundle);
                startActivity(intentEwm);
                break;
            case R.id.exit_login:               // 退出登录
                exitLoginDialog.show();
                break;
            case R.id.about_set:                // 关于
                startActivity(new Intent(context, AboutActivity.class));
                break;
            case R.id.feedback_set:             // 反馈意见
                startActivity(new Intent(context, FeedbackActivity.class));
                break;
            case R.id.help_set:                 // 使用帮助
                startActivity(new Intent(context, HelpActivity.class));
                break;
            case R.id.update_set:               // 检查更新
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "通讯中...");
                    sendRequestUpdate();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
                break;
            case R.id.cache_set:                // 清除缓存
                clearCacheDialog.show();
                break;
            case R.id.flow_set:                 // 流量管理
                startActivity(new Intent(context, FlowManageActivity.class));
                break;
            case R.id.like_set:                 // 偏好设置
                Intent intent = new Intent(context, PreferenceActivity.class);
                intent.putExtra("type", 2);
                startActivity(intent);
                break;
            case R.id.bluetooth_set:            // 蓝牙
                startActivity(new Intent(context, BluetoothActivity.class));
                break;
            case R.id.wifi_set:                 // WIFI连接设置
                startActivity(new Intent(context, WIFIActivity.class));
                break;
            case R.id.tv_update:                // 更新
                UpdateManager updateManager = new UpdateManager(context);
                updateManager.checkUpdateInfo1();
                updateDialog.dismiss();
                break;
            case R.id.tv_qx:                    // 取消更新
                if (updateType == 1) {
                    updateDialog.dismiss();
                } else {
                    ToastUtils.show_short(context, "本次需要更新");
                }
                break;
            case R.id.tv_confirm:               // 确认清除缓存
                new ClearCacheTask().execute();
                break;
            case R.id.tv_cancle:                // 取消清除缓存
                clearCacheDialog.dismiss();
                break;
            case R.id.image_touxiang:           // 更换头像
                imageDialog.show();
                break;
            case R.id.listener_set:
                startActivity(new Intent(context, FMConnectActivity.class));
                break;
        }
    }

    // 获取蓝牙状态
    private void getBluetoothState(){
        if(blueAdapter.isEnabled()){
            textBluetoothState.setText("打开");
        } else {
            textBluetoothState.setText("关闭");
        }
    }

    // 登陆状态下 用户设置头像对话框
    private void imageDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_imageupload, null);
        TextView textGallery = (TextView) dialog.findViewById(R.id.tv_gallery);
        textGallery.setOnClickListener(this);
        TextView textCamera = (TextView) dialog.findViewById(R.id.tv_camera);
        textCamera.setOnClickListener(this);
        imageDialog = new Dialog(context, R.style.MyDialog);
        imageDialog.setContentView(dialog);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 获取用户的登陆状态   登陆 OR 未登录
    private void getLoginStatus() {
        if(isFirst) {
            isFirst = false;
        } else if(isLogin.equals(BSApplication.SharedPreferences.getString(StringConstant.ISLOGIN, "false"))) {
            L.v("isLogin 登录状态没有发生变化 -- > > " + isLogin);
            return ;
        }
        isLogin = BSApplication.SharedPreferences.getString(StringConstant.ISLOGIN, "false");
        if (isLogin.equals("true")) {
            relativeStatusUnLogin.setVisibility(View.GONE);
            relativeStatusLogin.setVisibility(View.VISIBLE);
            exitLogin.setVisibility(View.VISIBLE);
            String imageUrl = BSApplication.SharedPreferences.getString(StringConstant.IMAGEURL, "");
            userName = BSApplication.SharedPreferences.getString(StringConstant.USERNAME, "");// 用户名，昵称
            userId = BSApplication.SharedPreferences.getString(StringConstant.USERID, "");
            textUserName.setText(userName);
            if(!imageUrl.equals("")) {
                if (imageUrl.startsWith("http:")) {
                    url = imageUrl;
                } else {
                    url = GlobalConfig.imageurl + imageUrl;
                }
                imageLoader.DisplayImage(url.replace("\\", "/"),userHead, false, false, null, null);
            } else {
                userHead.setImageResource(0);
            }
        } else {
            relativeStatusLogin.setVisibility(View.GONE);
            relativeStatusUnLogin.setVisibility(View.VISIBLE);
            exitLogin.setVisibility(View.GONE);
        }
    }

    // 注册广播 接收蓝牙、WiFi状态发生改变信息
    @Override
    protected void onStart() {
        if(!hasRegister){
            hasRegister = true;
            IntentFilter filterBluetooth = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mDevice, filterBluetooth);

            IntentFilter filterWiFi = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
            registerReceiver(mDevice, filterWiFi);
        }
        super.onStart();
    }

    // 设备状态广播监听
    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){    // 蓝牙状态发生改变
                if(blueAdapter.getState() == BluetoothAdapter.STATE_OFF){// 蓝牙关闭
                    textBluetoothState.setText("关闭");
                } else if(blueAdapter.getState() == BluetoothAdapter.STATE_ON){// 蓝牙打开
                    textBluetoothState.setText("打开");
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的打开与关闭，与wifi的连接无关
                if(wifiManager.isWifiEnabled()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String SSIDWiFi = wifiManager.getConnectionInfo().getSSID();
                            L.v("SSIDWiFi", SSIDWiFi);
                            if(SSIDWiFi.startsWith("\"")) {
                                SSIDWiFi = SSIDWiFi.substring(1, SSIDWiFi.length() - 1);
                            }
                            textWifiName.setText(SSIDWiFi);
                        }
                    }, 2000);
                } else {
                    textWifiName.setText("关闭");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoginStatus();
        if(wifiManager.isWifiEnabled()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String SSIDWiFi = wifiManager.getConnectionInfo().getSSID();
                    L.v("SSIDWiFi", SSIDWiFi);
                    if(SSIDWiFi.startsWith("\"")) {
                        SSIDWiFi = SSIDWiFi.substring(1, SSIDWiFi.length() - 1);
                    }
                    textWifiName.setText(SSIDWiFi);
                }
            }, 2000);
        } else {
            textWifiName.setText("关闭");
        }
    }

    // 注销数据交互
    private void sendRequestLogout(){
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        VolleyRequest.RequestPost(GlobalConfig.logoutUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                // 如果网络请求已经执行取消操作  就表示就算请求成功也不需要数据返回了  所以方法就此结束
                if(isCancelRequest){
                    return ;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(ReturnType != null && ReturnType.equals("1001")){        // 正常注销成功
                    Intent pushIntent = new Intent("push_down_completed");  // 发送广播 更新已下载和未下载界面
                    sendBroadcast(pushIntent);
                } else if (ReturnType != null && ReturnType.equals("200")) {// 还未登录，注销成功
                    L.w(ReturnType + "--->  还未登录，注销成功");
                } else if (ReturnType != null && ReturnType.equals("0000")) {// 无法获取相关的参数，注销成功
                    L.w(ReturnType + "--->  无法获取相关的参数，注销成功");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    L.w(ReturnType + "--->  异常");
                } else {
                    L.w(ReturnType + "--->  其它情况");
                }
                Editor et = BSApplication.SharedPreferences.edit();
                et.putString(StringConstant.ISLOGIN, "false");
                et.putString(StringConstant.USERID, "");
                et.putString(StringConstant.IMAGEURL, "");
                if(!et.commit()) {
                    L.w("数据 commit 失败!");
                }
                exitLogin.setVisibility(View.GONE);
                getLoginStatus();
                Toast.makeText(context, "注销成功!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 更新数据交互
    private void sendRequestUpdate(){
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Version", PhoneMessage.appVersonName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.VersionUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if(isCancelRequest){
                    return ;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(ReturnType != null && ReturnType.equals("1001")){
                    String MastUpdate;
                    String ResultList;
                    try {
                        GlobalConfig.apkUrl = result.getString("DownLoadUrl");
                        MastUpdate = result.getString("MastUpdate");
                        ResultList = result.getString("CurVersion");
                        if (ResultList != null && MastUpdate != null) {
                            dealVersion(ResultList, MastUpdate);
                        } else {
                            L.e("检查更新返回值", "返回值为1001，但是返回的数值有误");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtils.show_always(context, "当前已是最新版本");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 检查版本更新
    protected void dealVersion(String ResultList, String mastUpdate) {
        String Version = "0.1.0.X.0";
        String Descn = null;
        try {
            JSONTokener jsonParser = new JSONTokener(ResultList);
            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
            Version = arg1.getString("Version");
            Descn = arg1.getString("Descn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 版本更新比较
        String version = Version;
        String[] strArray;
        strArray = version.split("\\.");
        String versionBuild;
        try {
            versionBuild = strArray[4];
            int versionOld = PhoneMessage.versionCode;
            int versionNew = Integer.parseInt(versionBuild);
            if (versionNew > versionOld) {
                if (mastUpdate != null && mastUpdate.equals("1")) {		// 强制升级
                    if (Descn != null && !Descn.trim().equals("")) {
                        updateContent = Descn;
                    } else {
                        updateContent = "本次版本升级较大，需要更新";
                    }
                    updateType = 2;
                    updateDialog();
                    updateDialog.show();
                } else {			// 普通升级
                    if (Descn != null && !Descn.trim().equals("")) {
                        updateContent = Descn;
                    } else {
                        updateContent = "有新的版本需要升级喽";
                    }
                    updateType = 1;// 不需要强制升级
                    updateDialog();
                    updateDialog.show();
                }
            }else if(versionNew == versionOld){
                ToastUtils.show_always(context, "已经是最新版本");
            }
        } catch (Exception e) {
            e.printStackTrace();
            L.e("版本处理异常", e.toString() + "");
        }
    }

    // 更新弹出框
    private void updateDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);
        TextView textContent = (TextView) dialog.findViewById(R.id.text_contnt);
        textContent.setText(Html.fromHtml("<font size='26'>" + updateContent + "</font>"));
        TextView textUpdate = (TextView) dialog.findViewById(R.id.tv_update);
        textUpdate.setOnClickListener(this);
        TextView textCancel = (TextView) dialog.findViewById(R.id.tv_qx);
        textCancel.setOnClickListener(this);
        updateDialog = new Dialog(this, R.style.MyDialog);
        updateDialog.setContentView(dialog);
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 清除缓存对话框
    private void clearCacheDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialog.findViewById(R.id.tv_title);
        textTitle.setText("是否删除本地存储缓存？");
        TextView textConfirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        textConfirm.setOnClickListener(this);
        TextView textCancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        textCancel.setOnClickListener(this);
        clearCacheDialog = new Dialog(this, R.style.MyDialog);
        clearCacheDialog.setContentView(dialog);
        clearCacheDialog.setCanceledOnTouchOutside(true);
        clearCacheDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 退出登录对话框
    private void exitLoginDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.tv_title);
        textTitle.setText("是否退出登录？");
        dialogView.findViewById(R.id.tv_confirm).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    exitLoginDialog.dismiss();
                    dialog = DialogUtils.Dialogph(context, "正在注销...");
                    sendRequestLogout();
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
            }
        });
        dialogView.findViewById(R.id.tv_cancle).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                exitLoginDialog.dismiss();
            }
        });
        exitLoginDialog = new Dialog(this, R.style.MyDialog);
        exitLoginDialog.setContentView(dialogView);
        exitLoginDialog.setCanceledOnTouchOutside(false);
        exitLoginDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 拍照调用逻辑  从相册选择 which == 0   拍照 which == 1
    private void doDialogClick(int which) {
        switch (which) {
            case 0:    // 调用图库
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, TO_GALLERY);
                break;
            case 1:    // 调用相机
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
            default:
                ToastUtils.show_always(MineActivity.this, "发生未知异常");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TO_GALLERY:// 照片的原始资源地址
                if (resultCode == RESULT_OK) {
                    String path;
                    imageNum = 1;
                    Uri uri = data.getData();
                    int sdkVersion = Integer.valueOf(Build.VERSION.SDK);
                    if (sdkVersion >= 19) {  // 或者 android.os.Build.VERSION_CODES.KITKAT这个常量的值是19
//					path = uri.getPath();//5.0直接返回的是图片路径 Uri.getPath is ：  /document/image:46 ，5.0以下是一个和数据库有关的索引值
                        // path_above19:/storage/emulated/0/girl.jpg 这里才是获取的图片的真实路径
                        path = getPath_above19(context, uri);
                        startPhotoZoom(Uri.parse(path));
                    } else {
                        path = getFilePath_below19(uri);
                        startPhotoZoom(Uri.parse(path));
                    }
                }
                break;
            case TO_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    imageNum = 1;
                    startPhotoZoom(Uri.parse(outputFilePath));
                }
                break;
            case IntegerConstant.PHOTO_REQUEST_CUT:
                if (resultCode == 1) {
                    imageNum = 1;
                    PhotoCutAfterImagePath = data.getStringExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH);
                    dialog = DialogUtils.Dialogph(MineActivity.this, "头像上传中");
                    dealt();
                }
                break;
        }
    }

    // 图片裁剪
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
                    ToastUtils.show_always(MineActivity.this, "保存成功");
                    Editor et = BSApplication.SharedPreferences.edit();
                    String imageUrl;
                    if (MiniUri.startsWith("http:")) {
                        imageUrl = MiniUri;
                    } else {
                        imageUrl = GlobalConfig.imageurl + MiniUri;
                    }
                    et.putString(StringConstant.IMAGEURL, imageUrl);
                    // 正常切可用代码 已从服务器获得返回值，但是无法正常显示
                    imageLoader.DisplayImage(imageUrl.replace("\\", "/"), userHead, false, false, null, null);
                } else if (msg.what == 0) {
                    ToastUtils.show_always(context, "头像保存失败，请稍后再试");
                } else if (msg.what == -1) {
                    ToastUtils.show_always(context, "头像保存异常，图片未上传成功，请重新发布");
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (imageDialog != null) {
                    imageDialog.dismiss();
                }
            }
        };

        new Thread() {
            @Override
            public void run() {
                super.run();
                int m = 0;
                Message msg = new Message();
                try {
                    filePath = PhotoCutAfterImagePath;
                    String ExtName = filePath.substring(filePath.lastIndexOf("."));
                    String TestURI = GlobalConfig.baseUrl+"wt/common/upload4App.do?FType=UserP&ExtName=";
                    String Response = MyHttp.postFile(new File(filePath), TestURI
                            + ExtName
                            + "&PCDType="
                            + GlobalConfig.PCDType
                            + "&UserId="
                            + CommonUtils.getUserId(getApplicationContext())
                            + "&IMEI=" + PhoneMessage.imei);
                    L.e("图片上传数据", TestURI
                            + ExtName
                            + "&UserId="
                            + CommonUtils.getUserId(getApplicationContext())
                            + "&IMEI=" + PhoneMessage.imei);
                    L.e("图片上传结果", Response);
                    Gson gson = new Gson();
                    Response = ImageUploadReturnUtil.getResPonse(Response);
                    UserPortait = gson.fromJson(Response, new TypeToken<UserPortaitInside>() {}.getType());
                    try {
                        ReturnType = UserPortait.getReturnType();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    try {
                        MiniUri = UserPortait.getPortraitMini();
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
                    if(m == imageNum){
                        msg.what = 1;
                    }
                } catch (Exception e) {        // 异常处理
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        msg.obj = "异常" + e.getMessage().toString();
                        L.e("图片上传返回值异常", "" + e.getMessage());
                    } else {
                        L.e("图片上传返回值异常", "" + e);
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
    private String getFilePath_below19(Uri uri) {
        // 这里开始的第二部分，获取图片的路径：低版本的是没问题的，但是sdk>19会获取不到
        String[] proj = {MediaStore.Images.Media.DATA};

        // 好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);

        // 获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        System.out.println("***************" + column_index);

        // 将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();

        // 最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
        String path = cursor.getString(column_index);
        System.out.println("path:" + path);
        return path;
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
    public static String getPath_above19(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
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
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
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

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 与onbackpress同理 手机实体返回按键的处理
     */
    long waitTime = 2000;
    long touchTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - touchTime) >= waitTime) {
                    ToastUtils.show_always(MineActivity.this, "再按一次退出");
                    touchTime = currentTime;
                } else {
                    BSApplication.onStop();
                    MobclickAgent.onKillProcess(this);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 启动统计缓存的线程
    private void initCache() {
        new TotalCache().start();
    }

    // 统计缓存线程
    class TotalCache extends Thread implements Runnable {
        @Override
        public void run() {
            cachePath = Environment.getExternalStorageDirectory() + "/woting/image";
            File file = new File(cachePath);
            try {
                cache = CacheManager.getCacheSize(file);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textCache.setText(cache);
                    }
                });
            } catch (Exception e) {
                L.w("获取本地缓存文件大小", cache);
            }
        }
    }

    // 清除缓存异步任务
    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        private boolean clearResult;

        @Override
        protected void onPreExecute() {
            dialog = DialogUtils.Dialogph(context, "正在清除缓存");
        }

        @Override
        protected Void doInBackground(Void... params) {
            clearResult = CacheManager.delAllFile(cachePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            clearCacheDialog.dismiss();
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            if (clearResult) {
                ToastUtils.show_always(context, "缓存已清除");
                textCache.setText("0MB");
            } else {
                L.e("缓存异常", "缓存清理异常");
                initCache();
            }
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if(blueAdapter != null && blueAdapter.isDiscovering()){
            blueAdapter.cancelDiscovery();
        }
        if(hasRegister) {
            hasRegister = false;
            unregisterReceiver(mDevice);
        }
    }
}
