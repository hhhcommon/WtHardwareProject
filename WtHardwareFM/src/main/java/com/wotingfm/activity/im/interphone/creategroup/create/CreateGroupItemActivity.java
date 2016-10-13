package com.wotingfm.activity.im.interphone.creategroup.create;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shenstec.http.MyHttp;
import com.shenstec.utils.file.FileManager;
import com.wotingfm.R;
import com.wotingfm.activity.common.baseActivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.creategroup.model.GroupRation;
import com.wotingfm.activity.im.interphone.creategroup.model.UserPortaitInside;
import com.wotingfm.activity.im.interphone.creategroup.photocut.activity.PhotoCutActivity;
import com.wotingfm.activity.im.interphone.groupmanage.groupdetail.activity.GroupDetailAcitivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.L;
import com.wotingfm.util.PhoneMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 创建群组子页面  即有创建公开群、密码群、验证群
 */
public class CreateGroupItemActivity extends AppBaseActivity implements View.OnClickListener {
    private Uri outputFileUri;
    private Dialog headDialog;

    private int createGroupType = -1;           // 标识要创建的群类型
    private int imageNum;
    private int viewSuccess = -1;               //判断图片是否保存完成

    private EditText editGroupName;             // EditText 群组名称
    private EditText editGroupAutograph;        // EditText 群组签名
    private EditText editGroupPassWord;         // 设置群组密码
    private Button btnCommit;                   // 确定 提交数据
    private ImageView imageGroupHead;
    private Spinner spinnerChannel1;            // 设备备用频道1
    private Spinner spinnerChannel2;            // 设备备用频道2

    private String spinnerString1;
    private String spinnerString2;
    private String outputFilePath;
    //    private String imagePath;
    private String filePath;
    private String photoCutAfterImagePath;
    private String nick;                        // String 群组名称
    private String sign;                        // String 群组签名
    private String groupPassWord;
    private String miniUri;
    private String tag = "CREATE_GROUP_ITEM_ACTIVITY_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_create_group_item;
    }

    @Override
    protected void init() {
        setHeadDialog();
        handleIntent();

        imageGroupHead = (ImageView) findViewById(R.id.image_head);
        imageGroupHead.setOnClickListener(this);

        editGroupName = (EditText) findViewById(R.id.edit_group_name);
        editGroupAutograph = (EditText) findViewById(R.id.edit_group_autograph);
        editGroupPassWord = (EditText) findViewById(R.id.edit_group_password);
        TextView textGroupPassWord = (TextView) findViewById(R.id.text_group_password);
        TextView groupVerification = (TextView) findViewById(R.id.edit_group_verification);
        TextView textGroupVerification = (TextView) findViewById(R.id.text_group_verification);
        btnCommit = (Button) findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(this);
        spinnerChannel1 = (Spinner) findViewById(R.id.spinner_channel1);
        spinnerChannel1.setSelection(0);
        spinnerChannel2 = (Spinner) findViewById(R.id.spinner_channel2);
        spinnerChannel2.setSelection(1);
        setSpinnerItemListener();

        switch (createGroupType) {
            case IntegerConstant.CREATE_GROUP_PUBLIC:
                setTitle("创建公开群");
                editGroupPassWord.setVisibility(View.GONE);
                textGroupPassWord.setVisibility(View.GONE);
                groupVerification.setVisibility(View.GONE);
                textGroupVerification.setVisibility(View.GONE);
                break;
            case IntegerConstant.CREATE_GROUP_PRIVATE:
                setTitle("创建密码群");
                groupVerification.setVisibility(View.GONE);
                textGroupVerification.setVisibility(View.GONE);
                break;
            case IntegerConstant.CREATE_GROUP_VERIFICATION:
                setTitle("创建验证群");
                editGroupPassWord.setVisibility(View.GONE);
                textGroupPassWord.setVisibility(View.GONE);
                break;
            default:
                setTitle("创建群组");
                break;
        }
    }

    /*
     * 处理上个界面传递过来的数据  接收的数据用于判断用户想创建的群组类型
     */
    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            createGroupType = intent.getIntExtra(StringConstant.CREATE_GROUP_TYPE, -1);
        }
    }

    // 密码群时的editText输入框验证方法
    private boolean checkEdit() {
        groupPassWord = editGroupPassWord.getText().toString().trim();
        if (groupPassWord.trim().equals("") || groupPassWord.length() < 6) {
            Toast.makeText(context, "密码为空或输入的密码不足六位，请重新输入!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
     * 设置群组头像
     */
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

    // 判断网络类型 主网络请求模块
    private void sendRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupType", createGroupType);
            jsonObject.put("GroupSignature", sign);
            jsonObject.put("UserId", CommonUtils.getUserId(context));
            jsonObject.put("GroupName", nick);
            if (createGroupType == IntegerConstant.CREATE_GROUP_PRIVATE) {
                jsonObject.put("GroupPwd", groupPassWord);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.talkgroupcreatUrl, tag, jsonObject, new VolleyCallback() {
            private String returnType;
            private String message;
            private String groupInfo;
            private GroupRation groupRation;

            @Override
            protected void requestSuccess(JSONObject result) {
                DialogUtils.closeDialog();
                if (isCancelRequest) {
                    return;
                }
                try {
                    returnType = result.getString("ReturnType");
                    message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (returnType != null && returnType.equals("1001")) {
                    try {
                        groupInfo = result.getString("GroupInfo");
                        groupRation = new Gson().fromJson(groupInfo, new TypeToken<GroupRation>() {}.getType());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (viewSuccess == 1) {
                        dealt(groupRation);
                    } else {      // 跳转到群组详情界面
                        Intent pushIntent = new Intent("push_refreshlinkman");
                        sendBroadcast(pushIntent);
                        setResult(1);
                        Intent intent = new Intent(context, GroupDetailAcitivity.class);
                        intent.putExtra("GroupId", groupRation.getGroupId());
                        intent.putExtra("ImageUrl", miniUri);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    if (returnType != null && returnType.equals("1002")) {
                        Toast.makeText(context, "未登陆无法创建群组", Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1003")) {
                        Toast.makeText(context, "无法得到用户分类" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1004")) {
                        Toast.makeText(context, "无法得到组密码" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1005")) {
                        Toast.makeText(context, "无法得到组员信息" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1006")) {
                        Toast.makeText(context, "给定的组员信息不存在" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1007")) {
                        Toast.makeText(context, "只有一个有效成员，无法构建用户组" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1008")) {
                        Toast.makeText(context, "您所创建的组已达50个，不能再创建了" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else if (returnType != null && returnType.equals("1009")) {
                        Toast.makeText(context, "20分钟内创建组不能超过5个" + message, Toast.LENGTH_SHORT).show();
                        setTitle("创建失败");
                        btnCommit.setVisibility(View.INVISIBLE);
                    } else {
                        if (message != null && !message.trim().equals("")) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                DialogUtils.closeDialog();
            }
        });
    }

    /**
     * 获取用户选择的设备备用频道
     */
    private void setSpinnerItemListener() {
        spinnerChannel1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerString1 = CreateGroupItemActivity.this.getResources().getStringArray(R.array.spingar_channel)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerChannel2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerString2 = CreateGroupItemActivity.this.getResources().getStringArray(R.array.spingar_channel)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_head:       // 设置群头像
                headDialog.show();
                break;
            case R.id.btn_commit:       // 确定
                Toast.makeText(context, "备用频道1: " + spinnerString1 + ", 备用频道2: " + spinnerString2, Toast.LENGTH_LONG).show();
                nick = editGroupName.getText().toString().trim();
                sign = editGroupAutograph.getText().toString().trim();
                if (nick == null || nick.equals("")) {
                    Toast.makeText(context, "请输入群名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sign == null || sign.equals("")) {
                    Toast.makeText(context, "请输入群签名", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (createGroupType == IntegerConstant.CREATE_GROUP_PRIVATE) {
                    if (!checkEdit()) {
                        return;
                    }
                }
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    DialogUtils.showDialog(context);
                    sendRequest();
                } else {
                    Toast.makeText(context, "网络失败，请检查网络", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
                    imageGroupHead.setImageURI(Uri.parse(photoCutAfterImagePath));
                    viewSuccess = 1;
                } else {
                    Toast.makeText(context, "用户退出上传图片", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /*
     *  图片处理
     */
    private void dealt(final GroupRation groupRation) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Intent pushIntent = new Intent("push_refreshlinkman");
                    sendBroadcast(pushIntent);
                    setResult(1);
                    Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
                    if (groupRation != null && !groupRation.equals("")) {
                        // 跳转到群组详情页面
                        Intent intent = new Intent(context, GroupDetailAcitivity.class);
                        intent.putExtra("GroupId", groupRation.getGroupId());
                        intent.putExtra("ImageUrl", miniUri);
                        startActivity(intent);
                    }
                    finish();
                } else if (msg.what == 0) {
                    Toast.makeText(context, "头像保存失败，请稍后再试", Toast.LENGTH_SHORT).show();
                } else if (msg.what == -1) {
                    Toast.makeText(context, "头像保存异常，图片未上传成功，请重新发布", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new Thread() {
            private UserPortaitInside userPortait;
            private String returnType;

            @Override
            public void run() {
                super.run();
                int m = 0;
                Message msg = new Message();
                try {
                    for (int i = 0; i < imageNum; i++) {
                        filePath = photoCutAfterImagePath;
                        String ExtName = filePath.substring(filePath.lastIndexOf("."));
                        String TestURI = "http://182.92.175.134:808/wt/common/upload4App.do?FType=GroupP&ExtName=";
                        String Response = MyHttp.postFile(new File(filePath), TestURI + ExtName + "&PCDType=" + GlobalConfig.PCDType + "&GroupId=" + groupRation.GroupId
                                + "&IMEI=" + PhoneMessage.imei);
                        L.e("图片上传数据", TestURI + ExtName
                                + "&UserId=" + CommonUtils.getUserId(getApplicationContext()) + "&IMEI=" + PhoneMessage.imei);
                        Response = ImageUploadReturnUtil.getResPonse(Response);
                        userPortait = new Gson().fromJson(Response, new TypeToken<UserPortaitInside>() {
                        }.getType());
                        try {
                            returnType = userPortait.getReturnType();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        try {
                            miniUri = userPortait.getGroupImg();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        if (returnType == null || returnType.equals("")) {
                            msg.what = 0;
                        } else {
                            if (returnType.equals("1001")) {
                                msg.what = 1;
                            } else {
                                msg.what = 0;
                            }
                        }
                    }
                    if (m == imageNum) {
                        msg.what = 1;
                    }
                } catch (Exception e) {
                    // 异常处理
                    e.printStackTrace();
                    if (e.getMessage() != null) {
                        msg.obj = "异常" + e.getMessage();
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

    /*
     * 图片裁剪
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent(context, PhotoCutActivity.class);
        intent.putExtra(StringConstant.START_PHOTO_ZOOM_URI, uri.toString());
        intent.putExtra(StringConstant.START_PHOTO_ZOOM_TYPE, 1);
        startActivityForResult(intent, IntegerConstant.PHOTO_REQUEST_CUT);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }
}
