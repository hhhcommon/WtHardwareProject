package com.wotingfm.ui.music.program.radiolist.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.AppBaseFragmentActivity;
import com.wotingfm.ui.music.program.fenlei.model.FenLeiName;
import com.wotingfm.ui.music.program.radiolist.adapter.MyPagerAdapter;
import com.wotingfm.ui.music.program.radiolist.fragment.ClassifyFragment;
import com.wotingfm.ui.music.program.radiolist.fragment.RecommendFragment;
import com.wotingfm.ui.music.program.radiolist.model.CatalogData;
import com.wotingfm.ui.music.program.radiolist.model.SubCata;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.PagerSlidingTabStrip;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 某一分类数据
 * @author 辛龙
 * 2016年4月5日
 */
public class RadioListActivity extends AppBaseFragmentActivity implements OnClickListener {
    private RecommendFragment recommend;
    private List<String> list = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private PagerSlidingTabStrip pageSlidingTab;

    private Dialog dialog;
    private TextView mTextTitle;
    private ViewPager viewPager;

    private int count = 1;
    private boolean isCancelRequest;

    public static String catalogName;
    public static String catalogType;
    public static String id;
    public static final String tag = "RADIO_LIST_VOLLEY_REQUEST_CANCEL_TAG";

    public boolean isCancel() {
        return isCancelRequest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radiolist);

        initViews();
    }

    // 初始化界面
    private void initViews() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);
        mTextTitle = (TextView) findViewById(R.id.head_name_tv);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pageSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.tabs_title);
        pageSlidingTab.setIndicatorHeight(4);                                 // 滑动指示器的高度
        pageSlidingTab.setIndicatorColorResource(R.color.dinglan_orange);     // 滑动指示器的颜色
        pageSlidingTab.setDividerColorResource(R.color.WHITE);                // 菜单之间的分割线颜色
        pageSlidingTab.setSelectedTextColorResource(R.color.dinglan_orange);  // 选中的字体颜色
        pageSlidingTab.setTextColorResource(R.color.wt_login_third);          // 默认字体颜色

        handleRequestType();

        list.add("推荐");
        recommend = new RecommendFragment();
        fragments.add(recommend);

        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            ToastUtils.show_always(context, "网络连接失败，请检查网络设置!");
            return ;
        }
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    // 接收上一个页面传递过来的数据
    private void handleRequestType() {
        Intent listIntent = getIntent();
        if (listIntent != null) {
            String type = listIntent.getStringExtra("type");
            if (type != null && type.trim().equals("fenLeiAdapter")) {
                try {
                    FenLeiName list = (FenLeiName) listIntent.getSerializableExtra("Catalog");
                    catalogName = list.getName();
                    catalogType = list.getAttributes().getmId();
                    id = list.getAttributes().getId();
                    mTextTitle.setText(catalogName);
                } catch (Exception e) {
                    e.printStackTrace();
                    mTextTitle.setText("分类");
                }
            }

        }
    }

    // 请求网络获取分类信息
    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
            private String ReturnType;
            private List<SubCata> subDataList;
            private String CatalogData;

            @Override
            protected void requestSuccess(JSONObject result) {
                try {
                    ReturnType = result.getString("ReturnType");
                    CatalogData = result.getString("CatalogData");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    CatalogData catalogData = new Gson().fromJson(CatalogData, new TypeToken<CatalogData>() {}.getType());
                    subDataList = catalogData.getSubCata();
                    if (subDataList != null && subDataList.size() > 0) {
                        for (int i = 0; i < subDataList.size(); i++) {
                            list.add(subDataList.get(i).getCatalogName());
                            fragments.add(ClassifyFragment.instance(subDataList.get(i).getCatalogId(), subDataList.get(i).getCatalogType()));
                            count++;
                        }
                    }
                    viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), list, fragments));
                    pageSlidingTab.setViewPager(viewPager);
                    if (count == 1) pageSlidingTab.setVisibility(View.GONE);
                } else {
                    ToastUtils.show_always(context, "暂没有该分类数据");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                closeDialog();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", catalogType);
            jsonObject.put("CatalogId", id);
            jsonObject.put("Page", "1");
            jsonObject.put("ResultType", "1");
            jsonObject.put("RelLevel", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // 关闭加载对话框
    public void closeDialog() {
        if (dialog != null) dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1) {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        pageSlidingTab = null;
        viewPager = null;
        mTextTitle = null;
        dialog = null;
        if (list != null) {
            list.clear();
            list = null;
        }
        recommend = null;
        if (fragments != null) {
            fragments.clear();
            fragments = null;
        }
        setContentView(R.layout.activity_null);
    }
}
