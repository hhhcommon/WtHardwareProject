package com.wotingfm.activity.mine.flowmanage.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.common.constant.StringConstant;

/**
 * 流量管理
 */
public class FlowManageActivity extends AppBaseActivity  {

    //使用数组作为数据源
    final String arr[] = new String[] { "每天", "每三天", "每周" };
    private Spinner sp;
    private TextView tv_ShowChoice;
    private ImageView img_notify;
    private Boolean viewFlag;
    private SharedPreferences sharedPreferences;

    @Override
    protected int setViewId() {
        return R.layout.activity_flow_manage;
    }

    @Override
    protected void init() {
        setTitle("流量管理");// 设置标题
        sharedPreferences = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);

        sp=(Spinner)findViewById(R.id.sp_liuliang);//设置下拉框
        setSpinner();

        tv_ShowChoice=(TextView)findViewById(R.id.tv_showchoice);
        tv_ShowChoice.setText(arr[0]);

        img_notify= (ImageView)findViewById(R.id.image_bluetooth_set);


        viewFlag= sharedPreferences.getBoolean(StringConstant.FLOW_NOTIFY, true);


        if(viewFlag==true){
            img_notify.setImageResource(R.mipmap.wt_person_close);
        }else{
            img_notify.setImageResource(R.mipmap.wt_person_on);
        }
        img_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewFlag==true){
                    img_notify.setImageResource(R.mipmap.wt_person_on);
                    viewFlag=false;
                }else{
                    img_notify.setImageResource(R.mipmap.wt_person_close);
                    viewFlag=true;
                }

            }
        });


    }

    private void setSpinner() {
        // adapter对象
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arr);
        // 设置下拉菜单显示的内容风格
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 设置显示的数据
        sp.setAdapter(arrayAdapter);
        setItemListener();
    }

    private void setItemListener() {

      sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                Spinner spinner = (Spinner) parent;

                tv_ShowChoice.setText(""+spinner.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor et = sharedPreferences.edit();
        et.putBoolean(StringConstant.FLOW_NOTIFY,viewFlag);
        et.commit();
    }
}
