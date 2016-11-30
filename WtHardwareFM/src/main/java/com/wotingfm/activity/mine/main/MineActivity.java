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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.creategroup.model.UserPortaitInside;
import com.wotingfm.activity.im.interphone.creategroup.photocut.PhotoCutActivity;
import com.wotingfm.activity.im.interphone.groupmanage.model.UserInfo;
import com.wotingfm.activity.mine.bluetooth.BluetoothActivity;
import com.wotingfm.activity.mine.flowmanage.main.FlowManageActivity;
import com.wotingfm.activity.mine.fm.FMConnectActivity;
import com.wotingfm.activity.mine.myupload.MyUploadActivity;
import com.wotingfm.activity.mine.qrcode.EWMShowActivity;
import com.wotingfm.activity.mine.update.UpdatePersonActivity;
import com.wotingfm.activity.mine.wifi.WIFIActivity;
import com.wotingfm.activity.person.login.LoginActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.manager.FileManager;
import com.wotingfm.manager.MyHttp;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.L;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import java.io.File;

/**
 * 个人信息主页
 */
public class MineActivity extends Activity implements OnClickListener {
    private MineActivity context;
    public DeviceReceiver mDevice = new DeviceReceiver();
    public static BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
    private WifiManager wifiManager;
    private UserPortaitInside UserPortait;

    private Dialog dialog;// 加载数据对话框
    private Dialog imageDialog;// 修改头像对话框

    private View relativeStatusUnLogin;// 未登录状态
    private View relativeStatusLogin;// 登录状态
    private View circleView;// 点
    private ImageView userHead;// 用户头像
    private TextView textWifiName;
    private TextView textBluetoothState;// 蓝牙状态 打开 OR 关闭
    private TextView textUserName;// 用户名
    private TextView textUserArea;// 用户所在城市
    private TextView textUserId;// 用户号
    private TextView textUserAutograph;// 用户签名

    private String MiniUri;
    private String outputFilePath;
    private String url;
    private String userId;
    private String userName;
    private String PhotoCutAfterImagePath;
    private String isLogin;// 判断是否登录
    private String userNum;// 用户号
    private String userSign;// 用户签名
    private String region;// 城市

    private final int UPDATE_USER = 3;// 标识 跳转到修改个人信息界面
    private final int TO_GALLERY = 1;// 打开图库
    private final int TO_CAMERA = 2;// 打开照相机
    private int imageNum;
    private boolean hasRegister;
    private boolean isFirst = true;// 第一次加载界面

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        context = this;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);// 获取 WiFi 服务
        initViews();
    }

    // 设置 view
    private void initViews() {
        imageDialog();// 更换头像对话框

        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.img_person_background);
        ImageView loginBackgroundImage = (ImageView) findViewById(R.id.lin_image);// 登录背景图片
        loginBackgroundImage.setImageBitmap(bmp);
        ImageView lin_image_0 = (ImageView) findViewById(R.id.lin_image_0);// 未登录背景图片
        lin_image_0.setImageBitmap(bmp);

        findViewById(R.id.lin_xiugai).setOnClickListener(this);// 修改个人资料
        findViewById(R.id.imageView_ewm).setOnClickListener(this);// 二维码
        findViewById(R.id.lin_album).setOnClickListener(this);// 我的上传
        findViewById(R.id.flow_set).setOnClickListener(this);// 流量管理
        findViewById(R.id.bluetooth_set).setOnClickListener(this);// 蓝牙设置
        findViewById(R.id.wifi_set).setOnClickListener(this);// WIFI设置
        findViewById(R.id.listener_set).setOnClickListener(this);// 频道设置
        findViewById(R.id.image_nodenglu).setOnClickListener(this);// 没有登录时的默认头像 点击跳转至登录界面
        findViewById(R.id.text_denglu).setOnClickListener(this);// "点击登录"

        relativeStatusUnLogin = findViewById(R.id.lin_status_nodenglu);// 未登录时的状态

        relativeStatusLogin = findViewById(R.id.lin_status_denglu);    // 登录时的状态

        userHead = (ImageView) findViewById(R.id.image_touxiang);// 用户头像
        userHead.setOnClickListener(this);

        textUserName = (TextView) findViewById(R.id.text_user_name);// 用户名
        textUserArea = (TextView) findViewById(R.id.text_user_area);// 城市
        circleView = findViewById(R.id.circle_view);// 点
        textUserId = (TextView) findViewById(R.id.text_user_id);// 用户号
        textUserAutograph = (TextView) findViewById(R.id.text_user_autograph);// 用户签名

        textBluetoothState = (TextView) findViewById(R.id.text_bluetooth_state);// 蓝牙的状态 打开 OR 关闭
        textWifiName = (TextView) findViewById(R.id.text_wifi_name);// 连接的 WIFI 的名字
//        TextView textChannel = (TextView) findViewById(R.id.text_listener_frequency);// 频率

        getBluetoothState();// 获取蓝牙的打开关闭状态
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_gallery:// 从手机相册选择
                doDialogClick(0);
                imageDialog.dismiss();
                break;
            case R.id.tv_camera:// 拍照
                doDialogClick(1);
                imageDialog.dismiss();
                break;
            case R.id.imageView_ewm:// 二维码
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
            case R.id.lin_album:// 我的上传
                startActivity(new Intent(context, MyUploadActivity.class));
                break;
            case R.id.flow_set:// 流量管理
                startActivity(new Intent(context, FlowManageActivity.class));
                break;
            case R.id.bluetooth_set:// 蓝牙
                startActivity(new Intent(context, BluetoothActivity.class));
                break;
            case R.id.wifi_set:// WIFI连接设置
                startActivity(new Intent(context, WIFIActivity.class));
                break;
            case R.id.image_touxiang:// 更换头像
                imageDialog.show();
                break;
            case R.id.listener_set:
                startActivity(new Intent(context, FMConnectActivity.class));
                break;
            case R.id.lin_xiugai:// 修改个人资料
                startActivityForResult(new Intent(context, UpdatePersonActivity.class), UPDATE_USER);
                break;
            case R.id.image_nodenglu:// 登录
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.text_denglu:// 登录
                startActivity(new Intent(context, LoginActivity.class));
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
        dialog.findViewById(R.id.tv_gallery).setOnClickListener(this);
        dialog.findViewById(R.id.tv_camera).setOnClickListener(this);
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
            String imageUrl = BSApplication.SharedPreferences.getString(StringConstant.IMAGEURL, "");// 头像
            userName = BSApplication.SharedPreferences.getString(StringConstant.USERNAME, "");// 用户名
            userId = BSApplication.SharedPreferences.getString(StringConstant.USERID, "");// 用户 ID
            userNum = BSApplication.SharedPreferences.getString(StringConstant.USER_NUM, "");// 用户号
            userSign = BSApplication.SharedPreferences.getString(StringConstant.USER_SIGN, "");// 签名
            region = BSApplication.SharedPreferences.getString(StringConstant.REGION, "");// 区域

            if(region.equals("")) {
                region = "您还没有填写地址";
            }
            textUserArea.setText(region);
            textUserName.setText(userName);
            textUserAutograph.setText(userSign);

            if(userNum.equals("")) {
                circleView.setVisibility(View.GONE);
                textUserId.setVisibility(View.GONE);
            } else {
                circleView.setVisibility(View.VISIBLE);
                textUserId.setVisibility(View.VISIBLE);
                textUserId.setText(userNum);
            }

            if(!imageUrl.equals("")) {
                if (imageUrl.startsWith("http:")) {
                    url = imageUrl;
                } else {
                    url = GlobalConfig.imageurl + imageUrl;
                }
                url = AssembleImageUrlUtils.assembleImageUrl150(url);
                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(userHead);
            } else {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_default_head);
                userHead.setImageBitmap(bmp);
            }
        } else {
            relativeStatusLogin.setVisibility(View.GONE);
            relativeStatusUnLogin.setVisibility(View.VISIBLE);
        }
    }

    // 注册广播 接收蓝牙、WiFi 状态发生改变信息
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
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// wifi的打开与关闭，与wifi的连接无关
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
                    if (sdkVersion >= 19) {
                        path = getPath_above19(context, uri);
                    } else {
                        path = getFilePath_below19(uri);
                    }
                    startPhotoZoom(Uri.parse(path));
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
                    ToastUtils.show_always(context, "保存成功");
                    Editor et = BSApplication.SharedPreferences.edit();
                    String imageUrl;
                    if (MiniUri.startsWith("http:")) {
                        imageUrl = MiniUri;
                    } else {
                        imageUrl = GlobalConfig.imageurl + MiniUri;
                    }
                    et.putString(StringConstant.IMAGEURL, imageUrl);
                    if(et.commit()) L.v("数据 commit 失败!");
                    Picasso.with(context).load(imageUrl.replace("\\/", "/")).resize(100, 100).centerCrop().into(userHead);
                } else {
                    ToastUtils.show_always(context, "头像保存失败，请稍后再试");
                }
                if (dialog != null) dialog.dismiss();
                if (imageDialog != null) imageDialog.dismiss();
            }
        };

        new Thread() {
            @Override
            public void run() {
                super.run();
                int m = 0;
                Message msg = new Message();
                try {
                    String filePath = PhotoCutAfterImagePath;
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
                    String ReturnType = null;
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
                    if(m == imageNum) msg.what = 1;
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

    // API 19 以下获取图片路径的方法
    private String getFilePath_below19(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        System.out.println("***************" + column_index);
        cursor.moveToFirst();

        // 最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
        String path = cursor.getString(column_index);
        System.out.println("path:" + path);
        return path;
    }


    /**
     * API level 19以上才有
     * 创建项目时，我们设置了最低版本API Level，比如我的是10，
     * 因此，AS检查我调用的API后，发现版本号不能向低版本兼容，
     * 比如我用的“DocumentsContract.isDocumentUri(context, uri)”是Level 19 以上才有的，
     * 自然超过了10，所以提示错误。
     * 添加    @TargetApi(Build.VERSION_CODES.KITKAT)即可。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPath_above19(final Context context, final Uri uri) {
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
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

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

    // 设置android app 的字体大小不受系统字体大小改变的影响
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

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
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(blueAdapter != null && blueAdapter.isDiscovering()){
            blueAdapter.cancelDiscovery();
        }
        if(hasRegister) {
            hasRegister = false;
            unregisterReceiver(mDevice);
        }
    }
}
