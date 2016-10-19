package com.wotingfm.activity.mine.update.activity;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivitya.AppBaseActivity;
import com.wotingfm.activity.person.modifypassword.activity.ModifyPasswordActivity;
import com.wotingfm.activity.person.modifyphonenumber.ModifyPhoneNumberActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;

/**
 * 修改个人信息(还未完成，后台接口暂时没有)
 * @author 辛龙
 *         2016年7月19日
 */
public class UpdatePersonActivity extends AppBaseActivity implements OnClickListener {
    private Dialog imageDialog;
    private TextView textGender;        // 性别
    private TextView textPhoneNumber;   // 手机号码

    @Override
    protected int setViewId() {
        return R.layout.activity_updateperson;
    }

    @Override
    protected void init() {
        setTitle("修改资料");
        callDialog();        // 初始化性别选择对话框
        setData();

        findViewById(R.id.linear_modify).setOnClickListener(this);              // 密码
        findViewById(R.id.lin_gender).setOnClickListener(this);                 // 性别
        findViewById(R.id.lin_age).setOnClickListener(this);                    // 年龄
        findViewById(R.id.lin_xingzuo).setOnClickListener(this);                // 星座
        findViewById(R.id.linear_modify_phone_number).setOnClickListener(this); // 修改手机号
    }

    // 设置性别对话框
    private void callDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        TextView textTitle = (TextView) dialog.findViewById(R.id.tv_title);
        textTitle.setText("请选择您的性别");

        TextView textCancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        textCancel.setText("男");
        textCancel.setOnClickListener(this);

        TextView textConfirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        textConfirm.setText("女");
        textConfirm.setOnClickListener(this);

        imageDialog = new Dialog(context, R.style.MyDialog);
        imageDialog.setContentView(dialog);
        imageDialog.setCanceledOnTouchOutside(true);
        imageDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 初始化数据
    private void setData() {
        textGender = (TextView) findViewById(R.id.tv_gender);           // 性别
        textPhoneNumber = (TextView) findViewById(R.id.tv_phone_number);// 手机号码

        String userName = BSApplication.SharedPreferences.getString(StringConstant.USERNAME, "");       // 用户名
        TextView textName = (TextView) findViewById(R.id.tv_name);
        textName.setText(userName);

        String userId = BSApplication.SharedPreferences.getString(StringConstant.USERID, "");           // 用户ID
        TextView textUserId = (TextView) findViewById(R.id.tv_zhanghu);
        textUserId.setText(userId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String phoneNumber = BSApplication.SharedPreferences.getString(StringConstant.PHONENUMBER, ""); // 用户手机号
        textPhoneNumber.setText(phoneNumber);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_gender:       // 性别
                imageDialog.show();
                break;
            case R.id.lin_age:          // 年龄

                break;
            case R.id.lin_xingzuo:      // 星座

                break;
            case R.id.linear_modify:    // 密码
                startActivity(new Intent(UpdatePersonActivity.this, ModifyPasswordActivity.class));
                break;
            case R.id.tv_cancle:
                textGender.setText("男");
                imageDialog.dismiss();
                break;
            case R.id.tv_confirm:
                textGender.setText("女");
                imageDialog.dismiss();
                break;
            case R.id.linear_modify_phone_number:
                startActivity(new Intent(context, ModifyPhoneNumberActivity.class));
                break;
        }
    }
}
