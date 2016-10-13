package com.wotingfm.activity.mine.fm;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.mine.fm.adapter.FMListAdapter;
import com.wotingfm.activity.mine.fm.model.FMInfo;
import com.wotingfm.common.constant.StringConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * FM 连接界面
 */
public class FMConnectActivity extends AppBaseActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private ListView fmListView;// 频率列表
    private LinearLayout linearScan;// 底部扫描
    private List<FMInfo> list = new ArrayList<>();
    private FMListAdapter adapter;

    private ImageView imageFmSet;// FM开关
    private TextView userFmList;// 提示文字  可用FM列表
    private Button btnScan;

    private boolean isOpenFm;// 是否打开FM

    @Override
    protected int setViewId() {
        return R.layout.activity_fmconnect;
    }

    @Override
    protected void init() {
        setTitle("我听调频设置");
        sharedPreferences = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);

        fmListView = findView(R.id.fm_list_view);
        linearScan = findView(R.id.linear_scan);
        btnScan = findView(R.id.btn_scan_fm);// 扫描
        btnScan.setOnClickListener(this);

        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_fm, null);
        fmListView.addHeaderView(headView);
//        fmListView.set

        headView.findViewById(R.id.fm_set).setOnClickListener(this);// FM开关
        imageFmSet = (ImageView) headView.findViewById(R.id.image_fm_set);
        userFmList = (TextView) headView.findViewById(R.id.user_fm_list);

        isOpenFm = sharedPreferences.getBoolean(StringConstant.FM_IS_OPEN, true);// 开放蓝牙检测开关
        if(isOpenFm){
            imageFmSet.setImageResource(R.mipmap.wt_person_on);
            getData();
            userFmList.setVisibility(View.VISIBLE);
            btnScan.setVisibility(View.VISIBLE);
            fmListView.setDividerHeight(1);
        } else {
            imageFmSet.setImageResource(R.mipmap.wt_person_close);
            FMInfo fmInfo = new FMInfo();
            fmInfo.setFmName("");
            fmInfo.setFmIntroduce("");
            list.add(fmInfo);
            fmListView.setAdapter(adapter = new FMListAdapter(context, list));
            fmListView.setDividerHeight(0);
            userFmList.setVisibility(View.GONE);
            btnScan.setVisibility(View.GONE);
        }
        setListItemLis();
    }

    /**
     * ListView 点击事件监听
     */
    private void setListItemLis(){
        fmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.size() == 1) {
                    return ;
                }
                if(position - 1 >= 0) {
                    for(int i=0; i<list.size(); i++){
                        list.get(i).setType(0);
                    }
                    list.get(position - 1).setType(1);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan_fm:// 扫描

                break;
            case R.id.fm_set:
                if(isOpenFm){
                    list.clear();
                    imageFmSet.setImageResource(R.mipmap.wt_person_close);
                    FMInfo fmInfo = new FMInfo();
                    fmInfo.setFmName("");
                    fmInfo.setFmIntroduce("");
                    list.add(fmInfo);
                    adapter.notifyDataSetChanged();
                    userFmList.setVisibility(View.GONE);
                    btnScan.setVisibility(View.GONE);
                    fmListView.setDividerHeight(0);
                } else {
                    imageFmSet.setImageResource(R.mipmap.wt_person_on);
                    getData();
                    userFmList.setVisibility(View.VISIBLE);
                    btnScan.setVisibility(View.VISIBLE);
                    fmListView.setDividerHeight(1);
                }
                SharedPreferences.Editor et = sharedPreferences.edit();
                isOpenFm = !isOpenFm;
                et.putBoolean(StringConstant.FM_IS_OPEN, isOpenFm);
                et.commit();
                break;
        }
    }

    /**
     * 获取数据
     */
    private void getData(){
        if(list != null) {
            list.clear();
        }
        FMInfo fmInfo = new FMInfo();
        fmInfo.setFmName("87.5MHz");
        fmInfo.setFmIntroduce("将车载调频调到87.5MHz");
        fmInfo.setType(1);
        list.add(fmInfo);

        fmInfo = new FMInfo();
        fmInfo.setFmName("97.3MHz");
        fmInfo.setFmIntroduce("将车载调频调到97.3MHz");
        fmInfo.setType(0);
        list.add(fmInfo);

        fmInfo = new FMInfo();
        fmInfo.setFmName("106.4MHz");
        fmInfo.setFmIntroduce("将车载调频调到106.4MHz");
        fmInfo.setType(0);
        list.add(fmInfo);
        fmListView.setAdapter(adapter = new FMListAdapter(context, list));
    }
}
