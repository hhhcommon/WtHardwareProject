package com.wotingfm.ui.mine.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.BitmapUtils;

/**
 * 已经配对过的设备信息
 * Created by Administrator on 2017/3/14.
 */
public class PairBluetoothInfoFragment extends Fragment implements View.OnClickListener {
    private FragmentActivity context;
    private InputMethodManager imm;

    private String name;

    private View viewNull;// 空白地方
    private View rootView;
    private EditText editRename;
    private ImageView imageConfirm;// 确认修改

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_left:// 返回
                MineActivity.close();
                break;
            case R.id.view_cancel:// 取消配对
                context.sendBroadcast(new Intent(BroadcastConstants.ACTION_BLUETOOTH_CANCEL_PAIR));
                MineActivity.close();
                break;
            case R.id.edit_rename:
                imageConfirm.setVisibility(View.VISIBLE);
                break;
            case R.id.image_confirm:// 确定修改
                // 隐藏键盘
                imm.hideSoftInputFromWindow(viewNull.getWindowToken(), 0);
                viewNull.requestFocus();
                viewNull.setFocusable(true);
                break;
            case R.id.view_null:
                // 隐藏键盘
                imm.hideSoftInputFromWindow(viewNull.getWindowToken(), 0);
                viewNull.requestFocus();
                viewNull.setFocusable(true);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        Bundle bundle = getArguments();
        name = bundle.getString("BLUETOOTH_NAME");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_pair_bluetooth_info, container, false);
            rootView.setOnClickListener(this);

            initView();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        rootView.findViewById(R.id.image_left).setOnClickListener(this);// 返回
        rootView.findViewById(R.id.view_cancel).setOnClickListener(this);// 取消配对

        editRename = (EditText) rootView.findViewById(R.id.edit_rename);
        if (name != null) editRename.setText(name);
        editRename.setOnClickListener(this);
        editRename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.length() > 2 && !string.equals(name)) {
                    imageConfirm.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_fm_check));
                    imageConfirm.setClickable(true);
                    imageConfirm.setEnabled(true);
                } else {
                    imageConfirm.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_bluetooth_confirm_gray));
                    imageConfirm.setClickable(false);
                    imageConfirm.setEnabled(false);
                }
            }
        });

        imageConfirm = (ImageView) rootView.findViewById(R.id.image_confirm);
        imageConfirm.setOnClickListener(this);

        viewNull = rootView.findViewById(R.id.view_null);
        viewNull.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }
}
