package com.wotingfm.activity.mine.wifi;

import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.constant.StringConstant;

/**
 * 配置WiFi密码
 */
public class ConfigWiFiActivity extends AppBaseActivity implements View.OnClickListener {
    private Button btnConfirm;
    private EditText editPsw;

    private boolean isVisible;

    @Override
    protected int setViewId() {
        return R.layout.activity_config_wi_fi;
    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        if(intent != null){
            String wiFiName = intent.getStringExtra(StringConstant.WIFI_NAME);
            setTitle(wiFiName);
        } else {
            setTitle("连接 WiFi");
        }

        editPsw = findView(R.id.edit_psw);                          // 输入 密码
        editPsw.addTextChangedListener(new MyEditTextListener());

        findView(R.id.btn_cancel).setOnClickListener(this);         // 取消

        btnConfirm = findView(R.id.btn_confirm);                    // 连接
        btnConfirm.setOnClickListener(this);

        findView(R.id.image_visibility).setOnClickListener(this);   // 密码可见
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:                                   // 取消
                finish();
                break;
            case R.id.btn_confirm:                                  // 连接
                Intent intent = new Intent();
                intent.putExtra(StringConstant.WIFI_NAME, editPsw.getText().toString().trim());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.image_visibility:                             // 密码可见
                if(isVisible){// 判断密码是否可见
                    editPsw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editPsw.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                editPsw.setSelection(editPsw.getText().toString().length());
                isVisible = !isVisible;
                break;
        }
    }

    // 输入框对输入内容的长度监听
    class MyEditTextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.toString().length() < 8) {
                btnConfirm.setEnabled(false);
                btnConfirm.setTextColor(Color.parseColor("#BABAC1"));
            } else {
                btnConfirm.setEnabled(true);
                btnConfirm.setTextColor(Color.parseColor("#FFFFFFFF"));
            }
        }
    }
}
