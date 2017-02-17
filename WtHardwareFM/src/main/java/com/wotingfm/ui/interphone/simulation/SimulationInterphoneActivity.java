package com.wotingfm.ui.interphone.simulation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.FrequenceConstant;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.pickview.LoopView;
import com.wotingfm.widget.pickview.OnItemSelectedListener;

import java.util.Arrays;
import java.util.List;

/**
 * author：辛龙 (xinLong)
 * 2017/2/15 17:58
 * 邮箱：645700751@qq.com
 * 模拟对讲
 */
public class SimulationInterphoneActivity extends BaseActivity implements View.OnClickListener {

    private LoopView pickfrequency;
    private LinearLayout lin_frequency_no, lin_frequency;
    private TextView tv_text, tv_number, tv_set;
    private List<String> list;
    private int list_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        String[] arr = new String[]{"CH01-460.7000", "CH02-460.7100", "CH03-460.7200", "CH04-460.7300", "CH05-460.7400"
                , "CH06-460.7500", "CH07-460.7600", "CH08-460.7700", "CH09-460.7800", "CH10-460.7900"
                , "CH11-460.8000", "CH12-460.8100", "CH13-460.8200", "CH14-460.8300", "CH15-460.8400"
                , "CH16-460.8500", "CH17-460.8600", "CH18-460.8700", "CH19-460.8800", "CH20-460.8900"};
        list = Arrays.asList(arr);
        setView();  // 设置界面
        setData();  // 设置数据
    }

    // 初始化视图
    private void setView() {

        findViewById(R.id.head_left_btn).setOnClickListener(this);                                  // 返回
        findViewById(R.id.tv_save).setOnClickListener(this);                                        // 退出

        tv_set = (TextView) findViewById(R.id.tv_set);
        tv_set.setOnClickListener(this);                                                            // 频率设置

        tv_text = (TextView) findViewById(R.id.tv_text);                                            // 文字说明
        tv_number = (TextView) findViewById(R.id.tv_number);                                        // 频率号

        lin_frequency_no = (LinearLayout) findViewById(R.id.lin_frequency_no);                      // 默认控件
        lin_frequency = (LinearLayout) findViewById(R.id.lin_frequency);                            // 频率现在控件

        pickfrequency = (LoopView) findViewById(R.id.pick_frequency);                               // 频率选择器

        pickfrequency.setInitPosition(0);
        pickfrequency.setTextSize(15, 20);
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

        String _frequence = BSApplication.SharedPreferences.getString(FrequenceConstant.FREQUENCE, "");
        if (_frequence != null && !_frequence.trim().equals("")) {
            // 已经使用过模拟对讲
            tv_text.setText("当前频道");
            tv_number.setText(_frequence + "");
        } else {
            // 从来没有使用过模拟对讲
            tv_text.setText("当前频道为空，请设置频率");
            tv_number.setText("CH00-000.0000");
        }
        lin_frequency.setVisibility(View.GONE);
        lin_frequency_no.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:                                 // 返回
                finish();
                break;
            case R.id.tv_save:                                       // 退出
                finish();
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
                                if(!et.commit()) L.e("数据 commit 失败!");
                                tv_text.setText("当前频道");
                                tv_number.setText(_frequence + "");
                            }else {
                                ToastUtils.show_always(this, "数据出错了，请您稍后再试");
                            }
                        } else {
                            ToastUtils.show_always(this, "数据出错了，请您稍后再试");
                        }
                        lin_frequency.setVisibility(View.GONE);
                        lin_frequency_no.setVisibility(View.VISIBLE);

                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setContentView(R.layout.activity_null);
    }
}
