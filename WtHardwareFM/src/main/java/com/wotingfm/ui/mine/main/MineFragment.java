package com.wotingfm.ui.mine.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.manager.FileManager;
import com.wotingfm.common.manager.MyHttp;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.photocut.PhotoCutActivity;
import com.wotingfm.ui.common.qrcode.EWMShowFragment;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.ui.mine.FMTestActivity;
import com.wotingfm.ui.mine.bluetooth.BluetoothFragment;
import com.wotingfm.ui.mine.flowmanage.FlowManageFragment;
import com.wotingfm.ui.mine.fm.FMConnectFragment;
import com.wotingfm.ui.mine.model.UserPortaitInside;
import com.wotingfm.ui.mine.person.login.LoginActivity;
import com.wotingfm.ui.mine.person.updatepersonnews.UpdatePersonActivity;
import com.wotingfm.ui.mine.person.updatepersonnews.model.UpdatePerson;
import com.wotingfm.ui.mine.picture.ViewBigPictureActivity;
import com.wotingfm.ui.mine.set.SetFragment;
import com.wotingfm.ui.mine.wifi.WIFIFragment;
import com.wotingfm.util.AssembleImageUrlUtils;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 个人信息主页
 */
public class MineFragment extends Fragment implements OnClickListener {
    public static BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
    private DeviceReceiver mDevice = new DeviceReceiver();
    private WifiManager wifiManager;
    private UserPortaitInside UserPortait;
    private SharedPreferences sharedPreferences;

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
    private String tag = "MINE_UPDATE_PERSON_NEWS_VOLLEY_REQUEST_CANCEL_TAG";

    private final int UPDATE_USER = 3;// 标识 跳转到修改个人信息界面
    private final int TO_GALLERY = 1;// 打开图库
    private final int TO_CAMERA = 2;// 打开照相机
    private int imageNum;
    private boolean hasRegister;
    private boolean isCancelRequest;
    private boolean isFirst = true;// 第一次加载界面
    private boolean isUpdate;// 个人资料有改动
    private FragmentActivity context;
    private View rootView;
    private MineFragment ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_mine, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            ct = this;
            sharedPreferences = BSApplication.SharedPreferences;
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);// 获取 WiFi 服务
            initViews();
        }
        return rootView;
    }

    // 设置 view
    private void initViews() {
        imageDialog();// 更换头像对话框

        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.img_person_background);
        ImageView loginBackgroundImage = (ImageView) rootView.findViewById(R.id.lin_image);// 登录背景图片
        loginBackgroundImage.setImageBitmap(bmp);
        ImageView lin_image_0 = (ImageView) rootView.findViewById(R.id.lin_image_0);// 未登录背景图片
        lin_image_0.setImageBitmap(bmp);

        rootView.findViewById(R.id.lin_xiugai).setOnClickListener(this);// 修改个人资料
        rootView.findViewById(R.id.imageView_ewm).setOnClickListener(this);// 二维码
        rootView.findViewById(R.id.flow_set).setOnClickListener(this);// 流量管理
        rootView.findViewById(R.id.bluetooth_set).setOnClickListener(this);// 蓝牙设置
        rootView.findViewById(R.id.wifi_set).setOnClickListener(this);// WIFI设置
        rootView.findViewById(R.id.listener_set).setOnClickListener(this);// 频道设置
        rootView.findViewById(R.id.image_nodenglu).setOnClickListener(this);// 没有登录时的默认头像 点击跳转至登录界面
        rootView.findViewById(R.id.text_denglu).setOnClickListener(this);// "点击登录"
        rootView.findViewById(R.id.lin_set).setOnClickListener(this);// 设置

        relativeStatusUnLogin = rootView.findViewById(R.id.lin_status_nodenglu);// 未登录时的状态

        relativeStatusLogin = rootView.findViewById(R.id.lin_status_denglu);// 登录时的状态

        userHead = (ImageView) rootView.findViewById(R.id.image_touxiang);// 用户头像
        userHead.setOnClickListener(this);

        textUserName = (TextView) rootView.findViewById(R.id.text_user_name);// 用户名
        textUserArea = (TextView) rootView.findViewById(R.id.text_user_area);// 城市
        circleView = rootView.findViewById(R.id.circle_view);// 点
        textUserId = (TextView) rootView.findViewById(R.id.text_user_id);// 用户号
        textUserAutograph = (TextView) rootView.findViewById(R.id.text_user_autograph);// 用户签名

        textBluetoothState = (TextView) rootView.findViewById(R.id.text_bluetooth_state);// 蓝牙的状态 打开 OR 关闭
        textWifiName = (TextView) rootView.findViewById(R.id.text_wifi_name);// 连接的 WIFI 的名字
//        TextView textChannel = (TextView) findViewById(R.id.text_listener_frequency);// 频率
        getBluetoothState();// 获取蓝牙的打开关闭状态
        rootView.findViewById(R.id.fm_set).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, FMTestActivity.class));
            }
        });
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
                UserInviteMeInside news = new UserInviteMeInside();
                news.setPortraitMini(url);
                news.setUserId(userId);
                news.setNickName(userName);

                EWMShowFragment fg_evm = new EWMShowFragment();
                Bundle bundle = new Bundle();
                bundle.putString(StringConstant.FROM_TYPE, StringConstant.TAG_MINE);
                bundle.putString("type", "3");
                bundle.putString("news", userSign);// 签名
                bundle.putSerializable("person", news);
                fg_evm.setArguments(bundle);

                MineActivity.open(fg_evm);
                break;
            case R.id.flow_set:// 流量管理
                MineActivity.open(new FlowManageFragment());
                break;
            case R.id.bluetooth_set:// 蓝牙
                MineActivity.open(new BluetoothFragment());
                break;
            case R.id.wifi_set:// WIFI连接设置
                MineActivity.open(new WIFIFragment());
                break;
            case R.id.image_touxiang:// 更换头像
                imageDialog.show();
                break;
            case R.id.listener_set:// 调频
                MineActivity.open(new FMConnectFragment());
                break;
            case R.id.lin_xiugai:// 修改个人资料
                UpdatePersonActivity fg = new UpdatePersonActivity();
                fg.setTargetFragment(ct, 0);
                MineActivity.open(fg);
                break;
            case R.id.image_nodenglu:// 登录
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.text_denglu:// 登录
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.lin_set:// 设置
                SetFragment fg1 = new SetFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("LOGIN_STATE", isLogin);
                fg1.setArguments(bundle1);
                fg1.setTargetFragment(ct, 0);
                MineActivity.open(fg1);
                break;
            case R.id.view_picture:// 查看大图
                String _url = sharedPreferences.getString(StringConstant.IMAGEURL, "");     // 用户头像
                ArrayList<String> listUrl = new ArrayList<>();
//                listUrl.add(_url);
                listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1490690384432&di=7d4dddbf5ec3a415a2abfda9b0c771e3&imgtype=0&src=http%3A%2F%2Fd.hiphotos.baidu.com%2Fzhidao%2Fwh%253D600%252C800%2Fsign%3Df8ab0485a964034f0f98ca009ff35509%2Fa71ea8d3fd1f4134245acf26271f95cad1c85e7d.jpg");
                Intent intentPicture = new Intent(context, ViewBigPictureActivity.class);
                intentPicture.putExtra(StringConstant.PICTURE_INDEX, 0);
                intentPicture.putStringArrayListExtra(StringConstant.PICTURE_URL, listUrl);
                context.startActivity(intentPicture);
                imageDialog.dismiss();
                break;
        }
    }

    // 获取蓝牙状态
    private void getBluetoothState() {
        if (blueAdapter.isEnabled()) {
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
        View viewPicture = dialog.findViewById(R.id.view_picture);          // 查看大图
        viewPicture.setVisibility(View.VISIBLE);
        viewPicture.setOnClickListener(this);

        imageDialog = new Dialog(context, R.style.MyDialog);
        imageDialog.setContentView(dialog);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 获取用户的登陆状态   登陆 OR 未登录
    public void getLoginStatus() {
        if (isFirst) {
            isFirst = false;
        } else if (isLogin.equals(sharedPreferences.getString(StringConstant.ISLOGIN, "false"))) {
            L.v("isLogin 登录状态没有发生变化 -- > > " + isLogin);
            return;
        }
        isLogin = sharedPreferences.getString(StringConstant.ISLOGIN, "false");
        if (isLogin.equals("true")) {
            relativeStatusUnLogin.setVisibility(View.GONE);
            relativeStatusLogin.setVisibility(View.VISIBLE);

            String imageUrl = sharedPreferences.getString(StringConstant.IMAGEURL, "");             // 头像
            userName = sharedPreferences.getString(StringConstant.NICK_NAME, "");                   // 昵称
            userId = sharedPreferences.getString(StringConstant.USERID, "");                        // 用户 ID
            userNum = sharedPreferences.getString(StringConstant.USER_NUM, "");                     // 用户号
            userSign = sharedPreferences.getString(StringConstant.USER_SIGN, "");                   // 签名
            region = sharedPreferences.getString(StringConstant.REGION, "");                        // 区域

            if (region.equals("")) {
                region = "您还没有填写地址";
            }
            textUserArea.setText(region);
            textUserName.setText(userName);
            textUserAutograph.setText(userSign);

            if (userNum.equals("")) {
                circleView.setVisibility(View.GONE);
                textUserId.setVisibility(View.GONE);
            } else {
                circleView.setVisibility(View.VISIBLE);
                textUserId.setVisibility(View.VISIBLE);
                textUserId.setText(userNum);
            }

            if (!imageUrl.equals("")) {
                if (imageUrl.startsWith("http:")) {
                    url = imageUrl;
                } else {
                    url = GlobalConfig.imageurl + imageUrl;
                }
                String _url = AssembleImageUrlUtils.assembleImageUrl150(url);

                // 加载图片
                AssembleImageUrlUtils.loadImage(_url, url, userHead, IntegerConstant.TYPE_MINE);
            } else {
                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_tx_hy);
                userHead.setImageBitmap(bmp);
            }
        } else {
            relativeStatusLogin.setVisibility(View.GONE);
            relativeStatusUnLogin.setVisibility(View.VISIBLE);
        }
    }

    // 注册广播 接收蓝牙、WiFi 状态发生改变信息
    @Override
    public void onStart() {
        if (!hasRegister) {
            hasRegister = true;
            IntentFilter filterBluetooth = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mDevice, filterBluetooth);

            IntentFilter filterWiFi = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
            context.registerReceiver(mDevice, filterWiFi);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoginStatus();
        if (wifiManager.isWifiEnabled()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String SSIDWiFi = wifiManager.getConnectionInfo().getSSID();
                    L.v("SSIDWiFi", SSIDWiFi);
                    if (SSIDWiFi.startsWith("\"")) {
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
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {// 蓝牙状态发生改变
                if (blueAdapter.getState() == BluetoothAdapter.STATE_OFF) {// 蓝牙关闭
                    textBluetoothState.setText("关闭");
                } else if (blueAdapter.getState() == BluetoothAdapter.STATE_ON) {// 蓝牙打开
                    textBluetoothState.setText("打开");
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// wifi的打开与关闭，与wifi的连接无关
                if (wifiManager.isWifiEnabled()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String SSIDWiFi = wifiManager.getConnectionInfo().getSSID();
                            L.v("SSIDWiFi", SSIDWiFi);
                            if (SSIDWiFi.startsWith("\"")) {
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
                ToastUtils.show_always(context, "发生未知异常");
                break;
        }
    }

    public void setResult() {
        getLoginStatus();
    }

    public void setAddCardResult(int i, UpdatePerson pM, String regionIds) {
        if (i == 1) {// 修改个人资料界面返回
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) return;
            sendUpdate(pM,regionIds);
        } else if (i == 2) {
            // 处理接收结果的逻辑
            userNum = sharedPreferences.getString(StringConstant.USER_NUM, "");// 用户号
            if (!userNum.equals("")) {
                circleView.setVisibility(View.VISIBLE);
                textUserId.setVisibility(View.VISIBLE);
                textUserId.setText(userNum);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TO_GALLERY:// 照片的原始资源地址
                if (resultCode == Activity.RESULT_OK) {
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
//                    imageNum = 1;
//                    photoCutAfterImagePath = data.getStringExtra("return");
//                    dialog = DialogUtils.Dialogph(context, "头像上传中");
//                    dealt();
                }
                break;
        }
    }

    // 图片裁剪
    private void startPhotoZoom(Uri uri) {
        PhotoCutActivity fg = new PhotoCutActivity();
        Bundle bundle = new Bundle();
        bundle.putString(StringConstant.START_PHOTO_ZOOM_URI, uri.toString());
        bundle.putString(StringConstant.FROM_TYPE, "mine");
        bundle.putString(StringConstant.FRAGMENT_TYPE, "MineFragment");
        bundle.putInt(StringConstant.START_PHOTO_ZOOM_TYPE, 1);
        fg.setArguments(bundle);
        fg.setTargetFragment(ct, IntegerConstant.PHOTO_REQUEST_CUT);
        MineActivity.open(fg);
    }

    public void setResultForPhotoZoom(int resultCode, Intent data) {
        if (resultCode == 1 && data != null) {
            imageNum = 1;
            PhotoCutAfterImagePath = data.getStringExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH);
            dialog = DialogUtils.Dialogph(context, "头像上传中");
            dealt();
        } else {
            ToastUtils.show_always(context, "用户退出上传图片");
        }
    }

    // 图片处理
    private void dealt() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    ToastUtils.show_always(context, "保存成功");
                    Editor et = sharedPreferences.edit();
                    String imageUrl;
                    if (MiniUri.startsWith("http:")) {
                        imageUrl = MiniUri;
                    } else {
                        imageUrl = GlobalConfig.imageurl + MiniUri;
                    }
                    et.putString(StringConstant.IMAGEURL, imageUrl);
                    if (et.commit()) L.v("数据 commit 失败!");

                    String _url = AssembleImageUrlUtils.assembleImageUrl150(imageUrl);

                    // 加载图片
                    AssembleImageUrlUtils.loadImage(_url, imageUrl, userHead, IntegerConstant.TYPE_MINE);
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
                    String TestURI = GlobalConfig.baseUrl + "wt/common/upload4App.do?FType=UserP&ExtName=";
                    String Response = MyHttp.postFile(new File(filePath), TestURI
                            + ExtName
                            + "&PCDType="
                            + GlobalConfig.PCDType
                            + "&UserId="
                            + CommonUtils.getUserId(context)
                            + "&IMEI=" + PhoneMessage.imei);
                    L.e("图片上传数据", TestURI
                            + ExtName
                            + "&UserId="
                            + CommonUtils.getUserId(context)
                            + "&IMEI=" + PhoneMessage.imei);
                    L.e("图片上传结果", Response);
                    Gson gson = new Gson();
                    Response = ImageUploadReturnUtil.getResPonse(Response);
                    UserPortait = gson.fromJson(Response, new TypeToken<UserPortaitInside>() {
                    }.getType());
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
                    if (m == imageNum) msg.what = 1;
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
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        System.out.println("***************" + column_index);
        cursor.moveToFirst();

        // 最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
        String path = cursor.getString(column_index);
        System.out.println("path:" + path);
        return path;
    }

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

    // 判断个人资料是否有修改过  有则将数据提交服务器
    private void sendUpdate(UpdatePerson pM,String id) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            String regionId = id;

            try {
                String nickName = pM.getNickName();
                if (nickName != null && !nickName.trim().equals("")) {
                    // 昵称不能为空，所以为空的时候不提交修改
                    if (!nickName.equals(sharedPreferences.getString(StringConstant.NICK_NAME, ""))) {
                        jsonObject.put("NickName", nickName);
                        isUpdate = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String sign = pM.getUserSign();
                if (sign != null && !sign.trim().equals("")) {
                    if (!sign.equals(sharedPreferences.getString(StringConstant.USER_SIGN, ""))) {
                        jsonObject.put("UserSign", sign);
                        isUpdate = true;
                    }
                } else {
                    jsonObject.put("UserSign", " ");
                    isUpdate = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String gender = pM.getGender();
                if (gender != null && !gender.trim().equals("")) {
                    Log.v("gender", "gender -- > > " + gender);
                    if (!gender.equals(sharedPreferences.getString(StringConstant.GENDERUSR, "xb001"))) {
                        jsonObject.put("SexDictId", gender);
                        isUpdate = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String birthday = pM.getBirthday();
                if (birthday != null && !birthday.trim().equals("")) {
                    if (!birthday.equals(sharedPreferences.getString(StringConstant.BIRTHDAY, " "))) {
                        jsonObject.put("Birthday", Long.valueOf(birthday));
                        isUpdate = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String starSign = pM.getStarSign();
                if (starSign != null && !starSign.trim().equals("")) {
                    if (!starSign.equals(sharedPreferences.getString(StringConstant.STAR_SIGN, " "))) {
                        jsonObject.put("StarSign", starSign);
                        isUpdate = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                String email = pM.getEmail();
                if (email != null && !email.trim().equals("")) {
                    if (!email.equals(sharedPreferences.getString(StringConstant.EMAIL, " "))) {
                        if (isEmail(email)) {
                            // 邮箱格式正确之后再提交，格式不正确不修改
                            jsonObject.put("MailAddr", email);
                            isUpdate = true;
                        }
                    }
                } else {
                    jsonObject.put("MailAddr", " ");
                    isUpdate = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                String area = pM.getRegion();
                if (area != null && !area.trim().equals("")) {
                    if (!area.equals(sharedPreferences.getString(StringConstant.REGION, " "))) {
                        jsonObject.put("RegionDictId", regionId);
                        isUpdate = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            isUpdate = false;
        }

        // 个人资料没有修改过则不需要将数据提交服务器
        if (!isUpdate) {
            return;
        }
        isUpdate = false;
        Log.v("数据改动", "数据有改动，将数据提交到服务器!");
        VolleyRequest.RequestPost(GlobalConfig.updateUserUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String returnType = result.getString("ReturnType");
                    Log.v("returnType", "returnType -- > > " + returnType);

                    if (returnType != null && returnType.equals("1001")) {

                        try {
                            JSONObject ui = (JSONObject) new JSONTokener(result.getString("UserInfo")).nextValue();
                            SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                            try {
                                String imageUrl = ui.getString("PortraitMini");
                                et.putString(StringConstant.IMAGEURL, imageUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.IMAGEURL, "");
                            }
//                            try {
//                                String returnUserName = ui.getString("UserName");
//                                et.putString(StringConstant.USERNAME, returnUserName);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                et.putString(StringConstant.USERNAME, "");
//                            }
                            try {
                                String UserNum = ui.getString("UserNum");
                                et.putString(StringConstant.USER_NUM, UserNum);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USER_NUM, "");
                            }
                            try {
                                String imageUrlBig = ui.getString("PortraitBig");
                                et.putString(StringConstant.IMAGEURBIG, imageUrlBig);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.IMAGEURBIG, "");
                            }
                            try {
                                String userId = ui.getString("UserId");
                                et.putString(StringConstant.USERID, userId);
                                textUserId.setText(userNum);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USERID, "");
                            }
                            try {
                                String phoneNumber = ui.getString("PhoneNum");
                                et.putString(StringConstant.USER_PHONE_NUMBER, phoneNumber);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.USER_PHONE_NUMBER, "");
                            }
                            try {
                                String gender = ui.getString("Sex");
                                if (gender.equals("男")) {
                                    et.putString(StringConstant.GENDERUSR, "xb001");
                                } else if (gender.equals("女")) {
                                    et.putString(StringConstant.GENDERUSR, "xb002");
                                } else {
                                    et.putString(StringConstant.GENDERUSR, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.GENDERUSR, "");
                            }
                            try {
                                String region = ui.getString("Region");
                                /**
                                 * 地区的三种格式
                                 * 1、行政区划\/**市\/市辖区\/**区
                                 * 2、行政区划\/**特别行政区  港澳台三地区
                                 * 3、行政区划\/**自治区\/通辽市  自治区地区
                                 */
                                if (region != null && !region.equals("")) {
                                    String[] subRegion = region.split("/");
                                    if (subRegion.length > 3) {
                                        region = subRegion[1] + " " + subRegion[3];
                                    } else if (subRegion.length == 3) {
                                        region = subRegion[1] + " " + subRegion[2];
                                    } else {
                                        region = subRegion[1].substring(0, 2);
                                    }
                                    et.putString(StringConstant.REGION, region);
                                    textUserArea.setText(region);
                                } else {
                                    et.putString(StringConstant.REGION, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.REGION, "");
                            }
                            try {
                                String birthday = ui.getString("Birthday");
                                et.putString(StringConstant.BIRTHDAY, birthday);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.BIRTHDAY, "");
                            }
                            try {
                                String age = ui.getString("Age");
                                et.putString(StringConstant.AGE, age);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.AGE, "");
                            }
                            try {
                                String starSign = ui.getString("StarSign");
                                et.putString(StringConstant.STAR_SIGN, starSign);
                            } catch (Exception e) {
                                e.printStackTrace();
                                et.putString(StringConstant.STAR_SIGN, "");
                            }
                            try {
                                String email = ui.getString("Email");
                                if (email != null && !email.equals("")) {
                                    if (email.equals("&null")) {
                                        et.putString(StringConstant.EMAIL, "");
                                    } else {
                                        et.putString(StringConstant.EMAIL, email);
                                    }
                                } else {
                                    et.putString(StringConstant.EMAIL, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                String userSign = ui.getString("UserSign");
                                if (userSign != null && !userSign.equals("")) {
                                    if (userSign.equals("&null")) {
                                        et.putString(StringConstant.USER_SIGN, "");
                                    } else {
                                        et.putString(StringConstant.USER_SIGN, userSign);
                                        textUserAutograph.setText(userSign);
                                    }
                                } else {
                                    et.putString(StringConstant.USER_SIGN, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                String nickName = ui.getString("NickName");
                                if (nickName != null && !nickName.equals("")) {
                                    if (nickName.equals("&null")) {
                                        et.putString(StringConstant.NICK_NAME, "");
                                    } else {
                                        et.putString(StringConstant.NICK_NAME, nickName);
                                    }
                                } else {
                                    et.putString(StringConstant.NICK_NAME, "");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!et.commit()) {
                                Log.v("commit", "数据 commit 失败!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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

    // 验证邮箱的方法
    private boolean isEmail(String str) {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$"); // 验证邮箱格式
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        if (blueAdapter != null && blueAdapter.isDiscovering()) {
            blueAdapter.cancelDiscovery();
        }
        if (hasRegister) {
            hasRegister = false;
            context.unregisterReceiver(mDevice);
        }
    }
}
