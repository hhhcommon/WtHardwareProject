package com.wotingfm.ui.interphone.simulation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.FrequenceConstant;
import com.wotingfm.common.service.SimulationService;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.group.groupcontrol.groupdetail.util.FrequencyUtil;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.pickview.LoopView;
import com.wotingfm.widget.pickview.OnItemSelectedListener;

import java.util.List;

/**
 * author：辛龙 (xinLong)
 * 2017/2/15 17:58
 * 邮箱：645700751@qq.com
 * 模拟对讲
 */
public class SimulationInterphoneFragment extends Fragment implements View.OnClickListener {

    private LoopView pickfrequency;
    private LinearLayout lin_frequency_no, lin_frequency;
    private TextView tv_text, tv_number, tv_set;
    private List<String> list;
    private int list_number;
    private String frequence;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_simulation, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            list = FrequencyUtil.getFrequencyListNoView();
            frequence = BSApplication.SharedPreferences.getString(FrequenceConstant.FREQUENCE, "");
            setView();                                     // 设置界面
            setData();                                     // 设置数据
            initEmp();                                     // 初始化模拟对讲
            GlobalConfig.isMONI = true;
        }
        return rootView;
    }

    // 初始化模拟对讲
    private void initEmp() {
        if (GlobalConfig.isIM == true) {
            GlobalConfig.isIM = false;
            if (ChatFragment.isVisible) {
                ChatFragment.hangUp();
            }
        }
        context.startService(new Intent(context, SimulationService.class));
    }

    // 初始化视图
    private void setView() {
        rootView.findViewById(R.id.tv_save).setOnClickListener(this);                                        // 退出
        tv_set = (TextView) rootView.findViewById(R.id.tv_set);
        tv_set.setOnClickListener(this);                                                            // 频率设置

        tv_text = (TextView) rootView.findViewById(R.id.tv_text);                                            // 文字说明
        tv_number = (TextView) rootView.findViewById(R.id.tv_number);                                        // 频率号

        lin_frequency_no = (LinearLayout) rootView.findViewById(R.id.lin_frequency_no);                      // 默认控件
        lin_frequency = (LinearLayout) rootView.findViewById(R.id.lin_frequency);                            // 频率现在控件

        pickfrequency = (LoopView) rootView.findViewById(R.id.pick_frequency);                               // 频率选择器

        pickfrequency.setInitPosition(0);
        pickfrequency.setTextSize(25, 40);

        pickfrequency.setItems(list);

        pickfrequency.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                list_number = index;
            }
        });
    }


    // 数据适配
    private void setData() {

        tv_set.setText("频率设置");

        if (frequence != null && !frequence.trim().equals("")) {
            // 已经使用过模拟对讲
            tv_text.setText("当前频道");
            tv_number.setText(frequence + "");
        } else {
            // 从来没有使用过模拟对讲
            tv_text.setText("当前频道为空，请设置频率");
            tv_number.setText("CH00-000.0000");
            // 截取代码 s.substring(s.indexOf("-")+1,s.length())
        }
        lin_frequency.setVisibility(View.GONE);
        lin_frequency_no.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save:                                       // 退出
                DuiJiangActivity.close();
                break;
            case R.id.tv_set:                                        // 频率设置
                String _text_set = tv_set.getText().toString().trim();
                if (_text_set != null && !_text_set.equals("")) {
                    if (_text_set.equals("频率设置")) {
                        tv_set.setText("确定");
                        lin_frequency_no.setVisibility(View.GONE);
                        lin_frequency.setVisibility(View.VISIBLE);
                    }
                    if (_text_set.equals("确定")) {
                        tv_set.setText("频率设置");
                        if (list != null && list.size() > 0) {
                            String _frequence = list.get(list_number);
                            if (_frequence != null && !_frequence.equals("")) {
                                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                                et.putString(FrequenceConstant.FREQUENCE, _frequence);// 保存选中的数据
                                if (!et.commit()) L.e("数据 commit 失败!");
                                tv_text.setText("当前频道");
                                tv_number.setText(_frequence + "");
                                //此处要要设置重新设置的频率
                                SimulationService.setFrequence(_frequence);
                            } else {
                                ToastUtils.show_always(context, "数据出错了，请您稍后再试");
                            }
                        } else {
                            ToastUtils.show_always(context, "数据出错了，请您稍后再试");
                        }
                        lin_frequency.setVisibility(View.GONE);
                        lin_frequency_no.setVisibility(View.VISIBLE);

                    }
                }
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SimulationService.closeDevice();
    }
}
