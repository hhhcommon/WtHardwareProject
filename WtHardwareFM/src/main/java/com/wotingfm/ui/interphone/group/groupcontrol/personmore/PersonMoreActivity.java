package com.wotingfm.ui.interphone.group.groupcontrol.personmore;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.helper.CreateQRImageHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.interphone.model.UserInviteMeInside;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 群组详情页面
 * 辛龙 2016年1月21日
 */
public class PersonMoreActivity extends BaseActivity implements View.OnClickListener{

    private Dialog confirmDialog;// 退出群组确认对话框
    private String groupId;
    private String tag = "TALK_GROUP_MORE_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private Bitmap bmp;
    private ImageView img_EWM;
    private TextView tv_sign;
    private Dialog dialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_more);
        setView();
        handleIntent();
        initDialog();
        dialogDelete();
    }


    // 初始化视图
    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);          // 返回
        findViewById(R.id.tv_exit).setOnClickListener(this);                // 退出

        img_EWM=(ImageView)findViewById(R.id.img_EWM);                      // 二维码图片
        tv_sign=(TextView)findViewById(R.id.tv_sign);

    }


    // 处理请求
    private void handleIntent() {
        UserInviteMeInside groupNews = (UserInviteMeInside) getIntent().getSerializableExtra("person");
        if(groupNews==null){
            return;
        }

        if(!TextUtils.isEmpty(groupNews.getUserId())){
            groupId=groupNews.getUserId();
        }else{
            ToastUtils.show_always(context,"组ID获取异常");
            return;
        }

        if(!TextUtils.isEmpty(groupNews.getUserSign())){
            tv_sign.setText(groupNews.getUserSign());
        }else{
            if(!TextUtils.isEmpty(groupNews.getUserAliasName())){
                tv_sign.setText(groupNews.getUserAliasName());
            }
        }

        try {

            bmp = CreateQRImageHelper.getInstance().createQRImage(1, null,groupNews, 300, 300);

        }catch (Exception e){

        }
        if (bmp == null) {

            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }

        img_EWM.setImageBitmap(bmp);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.tv_exit:
                 if(confirmDialog!=null&&!confirmDialog.isShowing()&&!TextUtils.isEmpty(groupId)){
                     confirmDialog.show();
                 }else{
                     ToastUtils.show_always(context,"网络异常，请稍后重试");
                 }
                break;
            case R.id.tv_cancle:
                if(confirmDialog!=null&&confirmDialog.isShowing()){
                confirmDialog.dismiss();
                }
                break;
            case R.id.tv_confirm:
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    if(confirmDialog!=null&&confirmDialog.isShowing()){
                        confirmDialog.dismiss();
                    }
                    SendExitRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
                break;
        }
    }

    // 退出群组
    private void SendExitRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.ExitGroupurl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    //Log.v("ReturnType", "ReturnType -- > > " + ReturnType);
                   if(!TextUtils.isEmpty(ReturnType)){
                    if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                        ToastUtils.show_always(context, "已经成功退出该组");
                        setResult(1);
                        finish();
                    } else {
                        ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
                    }
                   }else{
                       ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
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

    // 初始化对话框
    private void initDialog() {
        View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this);
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog1);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    private void dialogDelete() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        TextView tv_confirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);
        tv_title.setText("确定要删除该好友？");
        confirmDialog = new Dialog(context, R.style.MyDialog);
        confirmDialog.setContentView(dialog);
        confirmDialog.setCanceledOnTouchOutside(true);
        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupId != null && !groupId.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        confirmDialog.dismiss();
                        dialogs = DialogUtils.Dialogph(context, "正在获取数据");
                        send();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "用户ID为空，无法删除该好友，请稍后重试");
                }
            }
        });
    }


    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("FriendUserId",groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.delFriendUrl, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) dialogs.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null) {
                    if (ReturnType.equals("1001")) {
                        ToastUtils.show_always(context, "已经删除成功该好友");
                        setResult(1);
                        finish();
                    } else if (ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取相关的参数");
                    } else if (ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获取用ID");
                    } else if (ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "好友Id无法获取");
                    } else if (ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "好友不存在");
                    } else if (ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "不是好友，不必删除");
                    } else if (ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "获取列表异常");
                    }
                } else {
                    ToastUtils.show_always(context, "列表处理异常");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) dialogs.dismiss();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        setContentView(R.layout.activity_null);
    }
}
