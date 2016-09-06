package com.wotingfm.activity.mine.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shenstec.http.MyHttp;
import com.shenstec.utils.file.FileManager;
import com.umeng.analytics.MobclickAgent;
import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.creategroup.model.UserPortaitInside;
import com.wotingfm.activity.im.interphone.creategroup.photocut.activity.PhotoCutActivity;
import com.wotingfm.activity.mine.update.activity.UpdatePersonActivity;
import com.wotingfm.activity.person.login.activity.LoginActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import java.io.File;

/**
 * 个人信息主页
 */
public class MineActivity extends Activity implements OnClickListener {
    private MineActivity context;
    private SharedPreferences sharedPreferences;
    private UserPortaitInside UserPortait;
//    private ImageLoader imageLoader;
    private final int TO_GALLERY = 1;
    private final int TO_CAMARA = 2;
    private final int PHOTO_REQUEST_CUT = 7;
    private String ReturnType;
    private String MiniUri;
    private String isLogin;                // 是否登录
    private String outputFilePath;
    private String imagePath;
    private String filePath;
    private String url;
    private String imagurl;
    private Uri outputFileUri;
    private Dialog dialog;
    protected Dialog imageDialog;
    private RelativeLayout relativeStatusUnLogin;
    private RelativeLayout relativeStatusLogin;
    private ImageView imageView_ewm;
    private ImageView lin_image_0;
    private String PhotoCutAfterImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        context = this;
        setView();           // 设置界面
        getLoginStatus();    // 获取是否登录的状态
        setListener();        // 设置监听
        imageDialog();
//        imageLoader = new ImageLoader(context);
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

    // 设置view
    private void setView() {
        imageView_ewm = (ImageView) findViewById(R.id.imageView_ewm);

        relativeStatusUnLogin = (RelativeLayout) findViewById(R.id.lin_status_nodenglu);// 未登录时的状态
        relativeStatusUnLogin.setOnClickListener(this);

        relativeStatusLogin = (RelativeLayout) findViewById(R.id.lin_status_denglu);    // 登录时的状态
        relativeStatusLogin.setOnClickListener(this);

        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.img_person_background);
        lin_image_0 = (ImageView) findViewById(R.id.lin_image_0);
        lin_image_0.setImageBitmap(bmp);
    }

    //初始化状态  登陆 OR 未登录
    private void judegListener() {
        if (isLogin.equals("true")) {
            relativeStatusUnLogin.setVisibility(View.GONE);
            relativeStatusLogin.setVisibility(View.VISIBLE);
        } else {
            relativeStatusLogin.setVisibility(View.GONE);
            relativeStatusUnLogin.setVisibility(View.VISIBLE);
        }
    }

    //获取用户的登陆状态
    private void getLoginStatus() {
        sharedPreferences = this.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        isLogin = sharedPreferences.getString(StringConstant.ISLOGIN, "false");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_gallery: //从手机相册选择
                doDialogClick(0);
                imageDialog.dismiss();
                break;
            case R.id.tv_camera://拍照
                doDialogClick(1);
                imageDialog.dismiss();
                break;
            case R.id.lin_status_nodenglu:// 登陆
                startActivity(new Intent(context, LoginActivity.class));
                break;
            case R.id.lin_status_denglu:            // 修改个人资料
                startActivity(new Intent(context, UpdatePersonActivity.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoginStatus();
        judegListener();
        if (isLogin.equals("true")) {
            imagurl = sharedPreferences.getString(StringConstant.IMAGEURL, "");
            if (imagurl.startsWith("http:")) {
                url = imagurl;
            } else {
                url = GlobalConfig.imageurl + imagurl;
            }
        }
    }

    private void setListener() {
        imageView_ewm.setOnClickListener(this);
    }

    // 拍照调用逻辑  从相册选择which==0   拍照which==1
    private void doDialogClick(int which) {
        switch (which) {
            case 0:    // 调用图库
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, TO_GALLERY);
                break;
            case 1:    // 调用相机
                String savepath = FileManager.getImageSaveFilePath(context);
                FileManager.createDirectory(savepath);
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(savepath, fileName);
                outputFileUri = Uri.fromFile(file);
                outputFilePath = file.getAbsolutePath();
                Intent intentss = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentss.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(intentss, TO_CAMARA);
                break;
            default:
                ToastUtils.show_allways(MineActivity.this, "发生未知异常");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TO_GALLERY:// 照片的原始资源地址
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    int sdkVersion = Integer.valueOf(Build.VERSION.SDK);
                    String path;
                    if (sdkVersion >= 19) {  // 或者 android.os.Build.VERSION_CODES.KITKAT这个常量的值是19
//					path = uri.getPath();//5.0直接返回的是图片路径 Uri.getPath is ：  /document/image:46 ，5.0以下是一个和数据库有关的索引值
                        // path_above19:/storage/emulated/0/girl.jpg 这里才是获取的图片的真实路径
                        path = getPath_above19(context, uri);
                        imagePath = path;
                        startPhotoZoom(Uri.parse(imagePath));
                    } else {
                        path = getFilePath_below19(uri);
                        imagePath = path;
                        startPhotoZoom(Uri.parse(imagePath));
                    }
                }
                break;
            case TO_CAMARA:
                if (resultCode == Activity.RESULT_OK) {
                    imagePath = outputFilePath;
                    startPhotoZoom(Uri.parse(imagePath));
                }
                break;
            case PHOTO_REQUEST_CUT:
                if (resultCode == 1) {
                    PhotoCutAfterImagePath = data.getStringExtra("return");
                    dialog = DialogUtils.Dialogph(MineActivity.this, "头像上传中");
                    dealt();
                }
                break;
        }
    }

    // 图片裁剪
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent(context, PhotoCutActivity.class);
        intent.putExtra("URI", uri.toString());
        intent.putExtra("type", 1);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    //图片处理
    private void dealt() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    ToastUtils.show_allways(MineActivity.this, "保存成功");
                    Editor et = sharedPreferences.edit();
                    String imageurl;
                    if (MiniUri.startsWith("http:")) {
                        imageurl = MiniUri;
                    } else {
                        imageurl = GlobalConfig.imageurl + MiniUri;
                    }
                    et.putString(StringConstant.IMAGEURL, imageurl);
                    // 正常切可用代码 已从服务器获得返回值，但是无法正常显示
//                    imgloader.DisplayImage(imageurl.replace("\\", "/"), imgview_touxiang, false, false, null, null);
                } else if (msg.what == 0) {
                    ToastUtils.show_allways(context, "头像保存失败，请稍后再试");
                } else if (msg.what == -1) {
                    ToastUtils.show_allways(context, "头像保存异常，图片未上传成功，请重新发布");
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
//                int m = 0;
                Message msg = new Message();
                try {
                    filePath = PhotoCutAfterImagePath;
                    String ExtName = filePath.substring(filePath.lastIndexOf("."));
                    String TestURI = "http://182.92.175.134:808/wt/common/upload4App.do?FType=UserP&ExtName=";
                    String Response = MyHttp.postFile(new File(filePath), TestURI
                            + ExtName
                            + "&SessionId="
                            + CommonUtils.getSessionId(getApplicationContext())
                            + "&PCDType="
                            + GlobalConfig.PCDType
                            + "&UserId="
                            + CommonUtils.getUserId(getApplicationContext())
                            + "&IMEI=" + PhoneMessage.imei);
                    Log.e("图片上传数据", TestURI
                            + ExtName
                            + "&SessionId="
                            + CommonUtils.getSessionId(getApplicationContext())
                            + "&UserId="
                            + CommonUtils.getUserId(getApplicationContext())
                            + "&IMEI=" + PhoneMessage.imei);
                    Log.e("图片上传结果", Response);
                    Gson gson = new Gson();
                    Response = ImageUploadReturnUtil.getResPonse(Response);
                    UserPortait = gson.fromJson(Response, new TypeToken<UserPortaitInside>() {}.getType());
                    try {
                        ReturnType = UserPortait.getReturnType();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    try {
                        String SessionId = UserPortait.getSessionId();
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
                } catch (Exception e) {        // 异常处理
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        msg.obj = "异常" + e.getMessage().toString();
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
                ToastUtils.show_allways(MineActivity.this, "再按一次退出");
                touchTime = currentTime;
            } else {
                MobclickAgent.onKillProcess(this);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
