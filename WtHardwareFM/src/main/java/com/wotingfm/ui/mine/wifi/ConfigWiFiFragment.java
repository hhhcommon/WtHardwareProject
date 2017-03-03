package com.wotingfm.ui.mine.wifi;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.MineActivity;

/**
 * 配置WiFi密码
 */
public class ConfigWiFiFragment extends Fragment implements View.OnClickListener {
    private Button btnConfirm;
    private EditText editPsw;

    private boolean isVisible;
    private FragmentActivity context;
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_config_wi_fi, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            init();
        }
        return rootView;
    }

    private void init() {
        String wiFiName = getArguments().getString(StringConstant.WIFI_NAME);

        TextView textTitle = (TextView) rootView.findViewById(R.id.text_title);
        if (wiFiName != null && !wiFiName.trim().equals("")) {
            textTitle.setText(wiFiName);// 设置标题
        } else {
            textTitle.setText("连接 WiFi");// 设置标题
        }

        ImageView leftImage = (ImageView) rootView.findViewById(R.id.left_image);
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MineActivity.close();
            }
        });
        editPsw = (EditText) rootView.findViewById(R.id.edit_psw);                          // 输入 密码
        editPsw.addTextChangedListener(new MyEditTextListener());

        rootView.findViewById(R.id.btn_cancel).setOnClickListener(this);         // 取消

        btnConfirm = (Button) rootView.findViewById(R.id.btn_confirm);                    // 连接
        btnConfirm.setOnClickListener(this);

        rootView.findViewById(R.id.image_visibility).setOnClickListener(this);   // 密码可见
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:                                   // 取消
                MineActivity.close();
                break;
            case R.id.btn_confirm:
                String res = editPsw.getText().toString().trim();

                Fragment targetFragment = getTargetFragment();
                ((WIFIFragment) targetFragment).setAddCardResult(res);

                MineActivity.close();
                break;
            case R.id.image_visibility:                             // 密码可见
                if (isVisible) {// 判断密码是否可见
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
            if (s.toString().length() < 8) {
                btnConfirm.setEnabled(false);
                btnConfirm.setTextColor(Color.parseColor("#BABAC1"));
            } else {
                btnConfirm.setEnabled(true);
                btnConfirm.setTextColor(Color.parseColor("#FFFFFFFF"));
            }
        }
    }
}
