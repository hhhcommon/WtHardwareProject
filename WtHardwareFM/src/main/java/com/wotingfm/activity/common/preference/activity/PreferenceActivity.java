package com.wotingfm.activity.common.preference.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.preference.adapter.PianHaoAdapter;
import com.wotingfm.activity.common.preference.model.pianhao;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;

import java.util.ArrayList;

/**
 * 偏好设置界面
 * 作者：xinlong on 2016/9/5 17:36
 * 邮箱：645700751@qq.com
 */
public class PreferenceActivity extends Activity implements View.OnClickListener {
    private TextView tv_over;
    private TextView tv_tiaoguo;
    private LinearLayout head_left_btn;
    private GridView gv_pianhao;
    private int type = 1;
    private ArrayList<pianhao> list;
    private PianHaoAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        receiveData();
        initView();
        setListener();
        setData();
    }

    private void receiveData() {
        //1：第一次进入  其它：其它界面进入
        type = this.getIntent().getIntExtra("type", 1);
    }

    private void initView() {
        head_left_btn = (LinearLayout) findViewById(R.id.head_left_btn);
        tv_tiaoguo = (TextView) findViewById(R.id.tv_tiaoguo);
        gv_pianhao = (GridView) findViewById(R.id.gv_pianhao);
        gv_pianhao.setSelector(new ColorDrawable(Color.TRANSPARENT));
        tv_over = (TextView) findViewById(R.id.tv_over);
        if (type == 1) {
            head_left_btn.setVisibility(View.INVISIBLE);
        } else {
            tv_tiaoguo.setVisibility(View.INVISIBLE);
        }

    }

    private void setListener() {
        head_left_btn.setOnClickListener(this);
        tv_tiaoguo.setOnClickListener(this);
        tv_over.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.tv_tiaoguo:
                finish();
                break;
            case R.id.tv_over:
                finish();
                break;
        }
    }

    private void setData() {
        list = new ArrayList<pianhao>();
        for (int i = 0; i < 20; i++) {
            pianhao listT = new pianhao();
            listT.setId("wt" + 1);
            listT.setName("我听：" + i);
            listT.setType(1);
            list.add(listT);
        }

        adapter = new PianHaoAdapter(this, list);
        gv_pianhao.setAdapter(adapter);
        setListViewListener();
    }

    private void setListViewListener() {
        gv_pianhao.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.get(position).getType() == 1) {
                    list.get(position).setType(2);
                } else {
                    list.get(position).setType(1);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存偏好设置页查看状态
        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
        et.putString(StringConstant.PREFERENCE, "1");
        et.commit();
        setContentView(R.layout.activity_null);
    }
}
