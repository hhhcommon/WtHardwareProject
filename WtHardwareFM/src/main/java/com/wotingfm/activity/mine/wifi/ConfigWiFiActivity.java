package com.wotingfm.activity.mine.wifi;

import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseActivity.AppBaseActivity;

/**
 * 配置WiFi密码
 */
public class ConfigWiFiActivity extends AppBaseActivity implements View.OnClickListener {
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
            String wiFiName = intent.getStringExtra("WIFINAME");
            setTitle(wiFiName);
        } else {
            setTitle("连接WiFi");
        }

        editPsw = findView(R.id.edit_psw);
        findView(R.id.btn_cancel).setOnClickListener(this);
        findView(R.id.btn_confirm).setOnClickListener(this);
        findView(R.id.image_visibility).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                String stringPsw = editPsw.getText().toString().trim();
                if(TextUtils.isEmpty(stringPsw)) {
                    Toast.makeText(context, "输入的密码为空，请重新输入!", Toast.LENGTH_SHORT).show();
                    return ;
                }
                Intent intent = new Intent();
                intent.putExtra("WIFIMIMA", stringPsw);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.image_visibility:
                if(isVisible){
                    editPsw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editPsw.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                editPsw.setSelection(editPsw.getText().toString().length());
                isVisible = !isVisible;
                break;
        }
    }
}
