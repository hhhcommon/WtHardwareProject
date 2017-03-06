package com.wotingfm.ui.mine.fm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.mine.fm.adapter.FMListAdapter;
import com.wotingfm.ui.mine.fm.model.FMInfo;
import com.wotingfm.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * FM 连接界面
 */
public class FMConnectFragment extends Fragment implements View.OnClickListener {
    private ListView fmListView;// 频率列表
//    private LinearLayout linearScan;// 底部扫描
    private List<FMInfo> list = new ArrayList<>();
    private FMListAdapter adapter;

    private ImageView imageFmSet;// FM 开关
    private TextView userFmList;// 提示文字  可用FM列表
    private Button btnScan;

    private boolean isOpenFm;// 是否打开 FM
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_fmconnect, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            init();
        }
        return rootView;
    }

    private void init() {
        TextView textTitle = (TextView) rootView.findViewById(R.id.text_title);
        textTitle.setText("我听调频设置");// 设置标题

        ImageView leftImage = (ImageView)rootView.findViewById(R.id.left_image);
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MineActivity.close();
            }
        });

        fmListView = (ListView) rootView.findViewById(R.id.fm_list_view);
//        linearScan = findView(R.id.linear_scan);
        btnScan = (Button) rootView.findViewById(R.id.btn_scan_fm);// 扫描
        btnScan.setOnClickListener(this);

        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_fm, null);
        fmListView.addHeaderView(headView);
//        fmListView.set

        headView.findViewById(R.id.fm_set).setOnClickListener(this);// FM开关
        imageFmSet = (ImageView) headView.findViewById(R.id.image_fm_set);
        userFmList = (TextView) headView.findViewById(R.id.user_fm_list);

        isOpenFm = BSApplication.SharedPreferences.getBoolean(StringConstant.FM_IS_OPEN, true);// 开放蓝牙检测开关
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

    // ListView 点击事件监听
    private void setListItemLis(){
        fmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(list.size() == 1) return ;
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
                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                isOpenFm = !isOpenFm;
                et.putBoolean(StringConstant.FM_IS_OPEN, isOpenFm);
                if(et.commit()) L.w("数据 commit 失败!");
                break;
        }
    }

    // 获取数据
    private void getData(){
        if(list != null) list.clear();
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
