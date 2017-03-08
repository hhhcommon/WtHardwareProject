package com.wotingfm.ui.interphone.group.creategroup;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.manager.FileManager;
import com.wotingfm.common.manager.MyHttp;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.model.GroupInfo;
import com.wotingfm.ui.common.photocut.PhotoCutActivity;
import com.wotingfm.ui.interphone.group.creategroup.create.CreateGroupFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.main.GroupDetailFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.util.FrequencyUtil;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.mine.model.UserPortaitInside;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ImageUploadReturnUtil;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.pickview.LoopView;
import com.wotingfm.widget.pickview.OnItemSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * 创建组的实现界面 1：edittext已经做出限制，只可以设置英文和数字输入
 * 2：创建组接口对接完成，对返回失败的值做出了处理
 */
public class CreateGroupContentFragment extends Fragment implements OnClickListener {
    private Dialog dialog;
    private Dialog imageDialog;

    private LinearLayout lin_status_first;
    private LinearLayout lin_status_second;

    private TextView head_name_tv;
    private TextView tv_group_entry;
    private EditText et_group_nick;
    private EditText et_group_sign;
    private EditText et_group_password;
    private ImageView ImageUrl;

    private String password;
    private String GroupType;
    private String filePath;
    private String imagePath;
    private String MiniUri;
    private String NICK;
    private String SIGN;
    private String outputFilePath;
    private String PhotoCutAfterImagePath;
    private String tag = "CREATE_GROUP_CONTENT_VOLLEY_REQUEST_CANCEL_TAG";

    private final int TO_GALLERY = 5;
    private final int TO_CAMERA = 6;
    private final int PHOTO_REQUEST_CUT = 7;
    private int ViewSuccess = -1;//判断图片是否保存完成
    private int RequestStatus = -1;// 标志当前页面的处理状态根据HandleIntent设定对应值 =1公开群 =2密码群 =3验证群
    private int groupType = -1;// 服务器端需求的grouptype参数 验证群为0 公开群为1 密码群为2
    private int imageNum;
    private boolean isCancelRequest;
    private Uri outputFileUri;
    private LinearLayout lin_channel1;
    private TextView tv_channel1;
    private LinearLayout lin_channel2;
    private TextView tv_channel2;
    private int pRate = -1;
    private int pFrequency = -1;
    private Dialog frequencyDialog;
    private int screenWidth;
    private String Frequence;
    private FragmentActivity context;
    private View rootView;
    private CreateGroupContentFragment ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_create_group_item, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            ct=this;
            imageNum = 0;
            setView();
            handleIntent();
            setListener();
            Dialog();
            initFrequencyDialog();
        }
        return rootView;
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
                try {
                    int a = pRate;
                    if (pFrequency == -1) {
                        tv_channel1.setText(frequencyList.get(1).trim());
                    } else {
                        String rate = rateList.get(pRate);
                        if (!TextUtils.isEmpty(rate.trim())) {
                            if (rate.equals("频道一")) {
                                tv_channel1.setText(frequencyList.get(pFrequency).trim());
                            } else if (rate.equals("频道二")) {
                                tv_channel2.setText(frequencyList.get(pFrequency).trim());
                            }
                        }
                    }
                    frequencyDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    frequencyDialog.dismiss();
                }
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

    private void Dialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_imageupload, null);
        TextView tv_gallery = (TextView) dialog.findViewById(R.id.tv_gallery);
        TextView tv_camera = (TextView) dialog.findViewById(R.id.tv_camera);
        tv_gallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, TO_GALLERY);
                imageDialog.dismiss();
            }
        });
        tv_camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String savePath = FileManager.getImageSaveFilePath(context);
                    FileManager.createDirectory(savePath);
                    String fileName = System.currentTimeMillis() + ".jpg";
                    File file = new File(savePath, fileName);
                    outputFileUri = Uri.fromFile(file);
                    outputFilePath = file.getAbsolutePath();
                    Intent s = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    s.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(s, TO_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageDialog.dismiss();
            }
        });
        imageDialog = new Dialog(context, R.style.MyDialog);
        imageDialog.setContentView(dialog);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 判断网络类型 主网络请求模块
    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            // 模块属性
            jsonObject.put("GroupType", groupType);
            jsonObject.put("GroupSignature", SIGN);
            jsonObject.put("GroupName", NICK);
            if (!TextUtils.isEmpty(tv_channel1.getText().toString().trim().substring(0, tv_channel1.getText().toString().trim().length() - 3))) {
                Frequence = tv_channel1.getText().toString().trim();
            }
            if (!TextUtils.isEmpty(tv_channel2.getText().toString().trim().substring(0, tv_channel2.getText().toString().trim().length() - 3))) {
                if (!TextUtils.isEmpty(Frequence)) {
                    Frequence = Frequence + "," + tv_channel2.getText().toString().trim();
                } else {
                    Frequence = tv_channel2.getText().toString().trim();
                }
            }

            jsonObject.put("GroupFreq", Frequence);

			/*
             * //NeedMember参数 0为不需要 1为需要 jsonObject.put("NeedMember", 0);
			 */
            // 测试数据
			/* jsonObject.put("NeedMember", 1); */
            // 当NeedMember=1时 也就是需要传送一个members的list时需处理
			/* jsonObject.put("Members", "a5d27255a5dd,956439fe9cbc"); */
            if (groupType == 2) {
                jsonObject.put("GroupPwd", password);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.talkgroupcreatUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;
            private GroupInfo groupinfo;

            @Override
            protected void requestSuccess(JSONObject result) {

                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        String GroupInfo = result.getString("GroupInfo");
                        groupinfo = new Gson().fromJson(GroupInfo, new TypeToken<GroupInfo>() {
                        }.getType());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (ViewSuccess == 1) {
                        chuLi(groupinfo);
                    } else {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Intent p = new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN);
                        context.sendBroadcast(p);

                        GroupDetailFragment fg = new GroupDetailFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "CreateGroupContentActivity");
                        bundle.putSerializable("news", groupinfo);
                        bundle.putString("imageurl", MiniUri);
                        fg.setArguments(bundle);
                        DuiJiangActivity.open(fg);

                        DuiJiangActivity.close();
                    }
                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    if (ReturnType != null && ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "未登陆无法创建群组");
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "无法得到用户分类" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "无法得到组密码" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "无法得到组员信息" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1006")) {
                        ToastUtils.show_always(context, "给定的组员信息不存在" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1007")) {
                        ToastUtils.show_always(context, "只有一个有效成员，无法构建用户组" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1008")) {
                        ToastUtils.show_always(context, "您所创建的组已达50个，不能再创建了" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else if (ReturnType != null && ReturnType.equals("1009")) {
                        ToastUtils.show_always(context, "20分钟内创建组不能超过5个" + Message);
                        head_name_tv.setText("创建失败");
                        tv_group_entry.setVisibility(View.INVISIBLE);
                    } else {
                        if (Message != null && !Message.trim().equals("")) {
                            ToastUtils.show_always(context, Message + "");
                        }
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    // 负责处理从上一个页面的来的事件 并处理对应的布局文件
    private void handleIntent() {
        GroupType = getArguments().getString("Type");
        if (GroupType == null || GroupType.equals("")) {
            ToastUtils.show_always(context, "获取组类型异常，请返回上一界面重新选择");
        } else if (GroupType.equals("Open")) {
            RequestStatus = 1;
            groupType = 1;
        } else if (GroupType.equals("PassWord")) {
            lin_status_first.setVisibility(View.VISIBLE);
            lin_status_second.setVisibility(View.GONE);
            RequestStatus = 2;
            groupType = 2;
        } else if (GroupType.equals("Validate")) {
            lin_status_first.setVisibility(View.GONE);
            lin_status_second.setVisibility(View.VISIBLE);
            RequestStatus = 3;
            groupType = 0;
        }
    }

    private void setListener() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        ImageUrl.setOnClickListener(this);
        tv_group_entry.setOnClickListener(this);
    }

    private void setView() {
        lin_status_first = (LinearLayout) rootView.findViewById(R.id.lin_groupcreate_status_first);
        lin_status_second = (LinearLayout) rootView.findViewById(R.id.lin_groupcreate_status_second);
        head_name_tv = (TextView) rootView.findViewById(R.id.head_name_tv);
        tv_group_entry = (TextView) rootView.findViewById(R.id.tv_group_entrygroup);
        et_group_nick = (EditText) rootView.findViewById(R.id.et_group_nick);
        et_group_sign = (EditText) rootView.findViewById(R.id.et_group_sign);
        ImageUrl = (ImageView) rootView.findViewById(R.id.ImageUrl);
        et_group_password = (EditText) rootView.findViewById(R.id.edittext_password);


        lin_channel1 = (LinearLayout) rootView.findViewById(R.id.lin_channel1);
        tv_channel1 = (TextView) rootView.findViewById(R.id.tv_channel1);
        lin_channel2 = (LinearLayout) rootView.findViewById(R.id.lin_channel2);
        tv_channel2 = (TextView) rootView.findViewById(R.id.tv_channel2);
        lin_channel1.setOnClickListener(this);
        lin_channel2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageUrl:
                imageDialog.show();
                break;
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
            case R.id.tv_group_entrygroup:
                NICK = et_group_nick.getText().toString().trim();
                SIGN = et_group_sign.getText().toString().trim();
                if (NICK == null || NICK.equals("")) {
                    ToastUtils.show_always(context, "请输入群名");
                    return;
                } else if (SIGN == null || SIGN.equals("")) {
                    ToastUtils.show_always(context, "请输入群签名");
                    return;
                } else {
                    if (RequestStatus == 2) {
                        checkEdit();
                    } else if (RequestStatus == 1 || RequestStatus == 3) {
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            dialog = DialogUtils.Dialogph(context, "正在为您创建群组");
                            send();
                        } else {
                            ToastUtils.show_always(context, "网络失败，请检查网络");
                        }
                    }
                }
                break;
            case R.id.lin_channel1:
                frequencyDialog.show();
                break;
            case R.id.lin_channel2:
                frequencyDialog.show();
                break;
        }
    }

    // 密码群时的edittext输入框验证方法
    private void checkEdit() {
        password = et_group_password.getText().toString().trim();

        if (password == null || password.trim().equals("")) {
            Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(context, "请输入六位以上密码", Toast.LENGTH_SHORT).show();
            // mEditTextPassWord.setError(Html.fromHtml("<font color=#ff0000>密码请输入六位以上</font>"));
            return;
        }
        // 提交数据
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在为您创建群组");
            send();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TO_GALLERY:
                // 照片的原始资源地址
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.e("URI:", uri.toString());
                    int sdkVersion = Integer.valueOf(Build.VERSION.SDK);
                    Log.d("sdkVersion:", String.valueOf(sdkVersion));
                    Log.d("KITKAT:", String.valueOf(Build.VERSION_CODES.KITKAT));
                    String path;
                    if (sdkVersion >= 19) {  // 或者 android.os.Build.VERSION_CODES.KITKAT这个常量的值是19
                        path = uri.getPath();//5.0直接返回的是图片路径 Uri.getPath is ：  /document/image:46 ，5.0以下是一个和数据库有关的索引值
                        Log.e("path:", path);
                        // path_above19:/storage/emulated/0/girl.jpg 这里才是获取的图片的真实路径
                        path = getPath_above19(context, uri);
                        Log.e("path_above19:", path);
                        imagePath = path;
                        imageNum = 1;
                        startPhotoZoom(Uri.parse(imagePath));
                    } else {
                        path = getFilePath_below19(uri);
                        Log.e("path_below19:", path);
                        imagePath = path;
                        imageNum = 1;
                        startPhotoZoom(Uri.parse(imagePath));
                    }
                }
                break;
            case TO_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    imagePath = outputFilePath;
                    Log.e("imagePath======", imagePath + "");
                    imageNum = 1;
                    if (imagePath != null && !imagePath.trim().equals("")) {
                        startPhotoZoom(Uri.parse(imagePath));
                    } else {
                        ToastUtils.show_always(context, "暂不支持拍照上传");
                    }
                }
                break;
        }
    }

    public void setResult(int resultCode, Intent data) {
        if (resultCode == 1&&data!=null) {
            imageNum = 1;
            PhotoCutAfterImagePath = data.getStringExtra(StringConstant.PHOTO_CUT_RETURN_IMAGE_PATH);
            ImageUrl.setImageURI(Uri.parse(PhotoCutAfterImagePath));
            ViewSuccess = 1;
        } else {
            ToastUtils.show_always(context, "用户退出上传图片");
        }
        }
    
    /**
     * 图片裁剪
     */
    private void startPhotoZoom(Uri uri) {
        PhotoCutActivity fg = new PhotoCutActivity();
        Bundle bundle = new Bundle();
        bundle.putString(StringConstant.START_PHOTO_ZOOM_URI, uri.toString());
        bundle.putInt(StringConstant.START_PHOTO_ZOOM_TYPE, 1);
        fg.setArguments(bundle);
        fg.setTargetFragment(ct, PHOTO_REQUEST_CUT);
        DuiJiangActivity.open(fg);
    }

    /* * 图片处理 */
    private void chuLi(final GroupInfo groupinfo) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Intent p = new Intent(BroadcastConstants.PUSH_REFRESH_LINKMAN);
                    context.sendBroadcast(p);
                    Fragment targetFragment = getTargetFragment();
                    ((CreateGroupFragment) targetFragment).setResult(1);
                    if (groupinfo == null || groupinfo.equals("")) {
                        ToastUtils.show_always(context, "创建成功");
                    } else {
                        ToastUtils.show_always(context, "创建成功");
                        
                        GroupDetailFragment fg = new GroupDetailFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "CreateGroupContentActivity");
                        bundle.putSerializable("news", groupinfo);
                        bundle.putString("imageurl", MiniUri);
                        fg.setArguments(bundle);
                        DuiJiangActivity.open(fg);
                    }
                    DuiJiangActivity.close();
                } else if (msg.what == 0) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    ToastUtils.show_always(context, "头像保存失败，请稍后再试");
                } else if (msg.what == -1) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    ToastUtils.show_always(context, "头像保存异常，图片未上传成功，请重新发布");
                }
            }
        };

        new Thread() {
            private UserPortaitInside UserPortait;
            private String ReturnType;

            @Override
            public void run() {
                super.run();
                int m = 0;
                Message msg = new Message();
                try {
                    for (int i = 0; i < imageNum; i++) {
                        filePath = PhotoCutAfterImagePath;
                        String ExtName = filePath.substring(filePath.lastIndexOf("."));
                        String TestURI = GlobalConfig.baseUrl + "/wt/common/upload4App.do?FType=GroupP&ExtName=";// 测试用 URI
                        String Response = MyHttp.postFile(new File(filePath), TestURI + ExtName + "&PCDType=" + GlobalConfig.PCDType + "&GroupId=" + groupinfo.GroupId
                                + "&IMEI=" + PhoneMessage.imei);
                        Log.e("图片上传数据", TestURI + ExtName
                                + "&UserId=" + CommonUtils.getUserId(context) + "&IMEI=" + PhoneMessage.imei);
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
                            MiniUri = UserPortait.getGroupImg();
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
                    }
                    if (m == imageNum) {
                        msg.what = 1;
                    }
                } catch (Exception e) {
                    // 异常处理
                    e.printStackTrace();
                    if (e != null && e.getMessage() != null) {
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
     *
     * @param uri
     */
    private String getFilePath_below19(Uri uri) {
        //这里开始的第二部分，获取图片的路径：低版本的是没问题的，但是sdk>19会获取不到
        String[] proj = {MediaStore.Images.Media.DATA};
        //好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        //获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        System.out.println("***************" + column_index);
        //将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        //最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
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
     *
     * @param context
     * @param uri
     * @return
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
                // TODO handle non-primary volumes
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
                final String[] selectionArgs = new String[]{
                        split[1]
                };
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        GroupType = null;
        lin_status_first = null;
        lin_status_second = null;
        dialog = null;
        head_name_tv = null;
        tv_group_entry = null;
        et_group_nick = null;
        et_group_password = null;
        password = null;
        et_group_sign = null;
        NICK = null;
        SIGN = null;
        imageDialog = null;
        ImageUrl = null;
        outputFileUri = null;
        outputFilePath = null;
        filePath = null;
        imagePath = null;
        MiniUri = null;
        PhotoCutAfterImagePath = null;
        tag = null;
    }
}
