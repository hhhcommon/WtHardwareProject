package com.wotingfm.ui.mine.flowmanage.main;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.L;

/**
 * 流量管理
 */
public class FlowManageActivity extends AppBaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private TextView textShowChoice;// 显示设置的更新时间
    private ImageView imageNotify;// 流量提醒开关

    private final String[] arr = new String[]{"每天", "每三天", "每周"};// 使用数组作为数据源
    private boolean viewFlag;

    @Override
    protected int setViewId() {
        return R.layout.activity_flow_manage;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_flow_set:
                if (viewFlag) {
                    imageNotify.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotify.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                viewFlag = !viewFlag;
                break;
        }
    }

    @Override
    protected void init() {
        setTitle("流量管理");// 设置标题

        Spinner spinner = (Spinner) findViewById(R.id.sp_liuliang);//设置下拉框
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, arr);// adapter 对象
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉菜单显示的内容风格
        spinner.setAdapter(arrayAdapter);// 设置显示的数据
        spinner.setOnItemSelectedListener(this);

        textShowChoice = (TextView) findViewById(R.id.tv_showchoice);
        textShowChoice.setText(arr[0]);

        imageNotify = (ImageView) findViewById(R.id.image_flow_set);
        imageNotify.setOnClickListener(this);

        viewFlag = BSApplication.SharedPreferences.getBoolean(StringConstant.FLOW_NOTIFY, true);
        if (viewFlag) {
            imageNotify.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        } else {
            imageNotify.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        textShowChoice.setText(arr[position]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
        et.putBoolean(StringConstant.FLOW_NOTIFY, viewFlag);
        if(!et.commit()) L.w("数据 commit 失败!");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
