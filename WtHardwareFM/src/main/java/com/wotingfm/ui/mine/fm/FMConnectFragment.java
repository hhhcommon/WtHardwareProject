package com.wotingfm.ui.mine.fm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.fm.adapter.FMListAdapter;
import com.wotingfm.ui.mine.fm.model.FMInfo;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.LineEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * FM 连接界面
 */
public class FMConnectFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {
    private ListView fmListView;// 频率列表
    private List<FMInfo> list = new ArrayList<>();
    private FMListAdapter adapter;
    private InputMethodManager imm;

    private LineEditText editFm;
    private ImageView imageFmSet;// FM 开关
    private View viewFmList;// 可用FM列表

    private boolean isOpenFm;// 是否打开 FM
    private FragmentActivity context;
    private View rootView;

    private String fmFrequency;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_fmconnect, container, false);
            rootView.setOnClickListener(this);
            rootView.findViewById(R.id.view_root).setOnClickListener(new ViewRequestFocusLis());
            initView();
        }
        return rootView;
    }

    private void initView() {
        TextView textTitle = (TextView) rootView.findViewById(R.id.text_title);
        textTitle.setText("FM调频");// 设置标题

        ImageView leftImage = (ImageView) rootView.findViewById(R.id.left_image);
        leftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MineActivity.close();
            }
        });

        fmListView = (ListView) rootView.findViewById(R.id.fm_list_view);

        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_fm, null);
        fmListView.addHeaderView(headView);

        viewFmList = headView.findViewById(R.id.view_fm_list);
        viewFmList.setOnClickListener(new ViewRequestFocusLis());

        headView.findViewById(R.id.fm_set).setOnClickListener(this);// FM开关
        imageFmSet = (ImageView) headView.findViewById(R.id.image_fm_set);

        editFm = (LineEditText) headView.findViewById(R.id.edit_fm);
        editFm.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editFm.setOnEditorActionListener(this);

        isOpenFm = BSApplication.SharedPreferences.getBoolean(StringConstant.FM_IS_OPEN, true);
        if (isOpenFm) {
            imageFmSet.setImageResource(R.mipmap.wt_person_on);
            getData();
            viewFmList.setVisibility(View.VISIBLE);
            fmListView.setDividerHeight(1);
        } else {
            imageFmSet.setImageResource(R.mipmap.wt_person_close);
            FMInfo fmInfo = new FMInfo();
            fmInfo.setFmName("");
            fmInfo.setFmIntroduce("");
            list.add(fmInfo);
            fmListView.setAdapter(adapter = new FMListAdapter(context, list));
            fmListView.setDividerHeight(0);
            viewFmList.setVisibility(View.GONE);
        }
        setListItemLis();
    }

    // ListView 点击事件监听
    private void setListItemLis() {
        fmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.size() == 1) return;
                if (position - 1 >= 0) {
                    for (int i = 0; i < list.size(); i++) {
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
            case R.id.fm_set:
                if (isOpenFm) {
                    list.clear();
                    imageFmSet.setImageResource(R.mipmap.wt_person_close);
                    FMInfo fmInfo = new FMInfo();
                    fmInfo.setFmName("");
                    fmInfo.setFmIntroduce("");
                    list.add(fmInfo);
                    adapter.notifyDataSetChanged();
                    viewFmList.setVisibility(View.GONE);
                    fmListView.setDividerHeight(0);

                    // 隐藏键盘
                    imm.hideSoftInputFromWindow(viewFmList.getWindowToken(), 0);
                } else {
                    imageFmSet.setImageResource(R.mipmap.wt_person_on);
                    getData();
                    viewFmList.setVisibility(View.VISIBLE);
                    fmListView.setDividerHeight(1);
                }
                SharedPreferences.Editor et = BSApplication.SharedPreferences.edit();
                isOpenFm = !isOpenFm;
                et.putBoolean(StringConstant.FM_IS_OPEN, isOpenFm);
                if (et.commit()) L.w("数据 commit 失败!");
                break;
        }
    }

    // 获取数据 -- > 测试数据
    private void getData() {
        if (list != null) list.clear();
        FMInfo fmInfo = new FMInfo();
        fmInfo.setFmName("FM 87.5MHz");
        fmInfo.setType(1);
        list.add(fmInfo);

        fmInfo = new FMInfo();
        fmInfo.setFmName("FM 97.3MHz");
        fmInfo.setType(0);
        list.add(fmInfo);

        fmInfo = new FMInfo();
        fmInfo.setFmName("FM 106.4MHz");
        fmInfo.setType(0);
        list.add(fmInfo);
        fmListView.setAdapter(adapter = new FMListAdapter(context, list));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            String temp = editFm.getText().toString();

            viewFmList.setFocusable(true);
            viewFmList.setFocusableInTouchMode(true);
            viewFmList.requestFocus();

            // 隐藏键盘
            imm.hideSoftInputFromWindow(viewFmList.getWindowToken(), 0);

            if (fmFrequency == null || fmFrequency.equals(temp)) {
                return true;
            }

            // 然后再执行保存操作
            ToastUtils.show_always(context, "保存成功");
            fmFrequency = temp;
            return true;
        }
        return false;
    }

    // 点击空白处 EditText 失去焦点  然后执行保存操作
    private class ViewRequestFocusLis implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String temp = editFm.getText().toString();

            viewFmList.setFocusable(true);
            viewFmList.setFocusableInTouchMode(true);
            viewFmList.requestFocus();

            // 隐藏键盘
            imm.hideSoftInputFromWindow(viewFmList.getWindowToken(), 0);

            if (fmFrequency == null || fmFrequency.equals(temp)) {
                return ;
            }

            // 然后再执行保存操作
            ToastUtils.show_always(context, "保存成功");
            fmFrequency = temp;
        }
    }
}
