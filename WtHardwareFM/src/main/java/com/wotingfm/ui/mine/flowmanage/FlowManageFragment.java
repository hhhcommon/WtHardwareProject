package com.wotingfm.ui.mine.flowmanage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.BitmapUtils;

import java.lang.reflect.Method;

/**
 * 流量管理
 */
public class FlowManageFragment extends Fragment implements View.OnClickListener {
    private FragmentActivity context;
    private View rootView;

    private ImageView imageFlowSwitch;// 移动数据开关
    private ImageView imageFlowRemindSwitch;// 流量提醒开关

    private boolean isFlowSwitch;// 移动数据是否打开
    private boolean isFlowRemindSwitch;// 流量提醒是否打开

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_flow_manage, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initView();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        isFlowSwitch = getMobileDataState(context, null);// 获取移动数据是否开启
        isFlowRemindSwitch = BSApplication.SharedPreferences.getBoolean(StringConstant.FLOW_REMIND_SWITCH, true);// 默认打开

        TextView textTitle = (TextView) rootView.findViewById(R.id.text_title);
        textTitle.setText("流量管理");// 设置标题

        rootView.findViewById(R.id.left_image).setOnClickListener(this);// 返回

        rootView.findViewById(R.id.text_view_flow_info).setOnClickListener(this);// 移动数据
        imageFlowSwitch = (ImageView) rootView.findViewById(R.id.image_flow_switch);// 移动数据
        imageFlowSwitch.setOnClickListener(this);
        if (isFlowSwitch) {
            imageFlowSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageFlowSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.text_flow_remind).setOnClickListener(this);// 流量提醒
        imageFlowRemindSwitch = (ImageView) rootView.findViewById(R.id.image_flow_remind_switch);// 流量提醒
        imageFlowRemindSwitch.setOnClickListener(this);
        if (isFlowRemindSwitch) {
            imageFlowRemindSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageFlowRemindSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_image:// 返回
                MineActivity.close();
                break;
            case R.id.image_flow_switch:// 移动数据
            case R.id.text_view_flow_info:
                if (!isFlowSwitch) {
                    imageFlowSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageFlowSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                isFlowSwitch = !isFlowSwitch;
                setMobileData(context, isFlowSwitch);
                break;
            case R.id.image_flow_remind_switch:// 流量提醒
            case R.id.text_flow_remind:
                if (!isFlowRemindSwitch) {
                    imageFlowRemindSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageFlowRemindSwitch.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                }
                isFlowRemindSwitch = !isFlowRemindSwitch;
                BSApplication.SharedPreferences.edit().putBoolean(StringConstant.FLOW_REMIND_SWITCH, isFlowRemindSwitch).commit();
                break;
        }
    }

    // 设置手机的移动数据
    private void setMobileData(Context pContext, boolean pBoolean) {
        ConnectivityManager connectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Method setMobileDataEnabl;
        try {
            /**
             * 1.在Android5.0及以上设备中，这个反射的方法不存在?
             * 2.setMobileDataEnabled 这个方法在内部实现私有化了？
             */
            setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
            setMobileDataEnabl.invoke(connectivityManager, pBoolean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回手机移动数据的状态
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean getMobileDataState(Context pContext, Object[] arg) {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class ownerClass = mConnectivityManager.getClass();
            Class[] argsClass = null;
            if (arg != null) {
                argsClass = new Class[1];
                argsClass[0] = arg.getClass();
            }
            Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);
            boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
            return isOpen;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
//        et.putBoolean(StringConstant.FLOW_NOTIFY, viewFlag);
//        if(!et.commit()) L.w("数据 commit 失败!");
    }
}
