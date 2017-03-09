package com.wotingfm.ui.music.program.radiolist.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.program.fenlei.model.FenLeiName;
import com.wotingfm.ui.music.program.radiolist.adapter.MyPagerAdapter;
import com.wotingfm.ui.music.program.radiolist.fragment.ClassifyFragment;
import com.wotingfm.ui.music.program.radiolist.fragment.RecommendFragment;
import com.wotingfm.ui.music.program.radiolist.model.CatalogData;
import com.wotingfm.ui.music.program.radiolist.model.SubCata;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.PagerSlidingTabStrip;
import com.wotingfm.widget.TipView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 某一分类数据
 * @author 辛龙
 * 2016年4月5日
 */
public class RadioListFragment extends Fragment implements OnClickListener, TipView.WhiteViewClick {
    private RecommendFragment recommend;
    private List<String> list = new ArrayList<>();
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private PagerSlidingTabStrip pageSlidingTab;

    private static Dialog dialog;
    private TextView mTextTitle;
    private ViewPager viewPager;

    private int count = 1;
    private static boolean isCancelRequest;

    public static String catalogName;
    public static String catalogType;
    public static String id;
    public static final String tag = "RADIO_LIST_VOLLEY_REQUEST_CANCEL_TAG";

    private TipView tipView;// 没有数据、没有网络提示
    private FragmentActivity context;
    private View rootView;

    @Override
    public void onWhiteViewClick() {
        if(GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
            return ;
        }
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    public static boolean isCancel() {
        return isCancelRequest;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_radiolist, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initViews();
        }
        return rootView;
    }

    // 初始化界面
    private void initViews() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        mTextTitle = (TextView) rootView.findViewById(R.id.head_name_tv);

        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        pageSlidingTab = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs_title);
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
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_NET);
            return ;
        }
        dialog = DialogUtils.Dialogph(context, "正在获取数据");
        sendRequest();
    }

    // 接收上一个页面传递过来的数据
    private void handleRequestType() {
        Bundle bundlea = getArguments();
        if (bundlea != null) {
            String type = bundlea.getString("type");
            if (type != null && type.trim().equals("fenLeiAdapter")) {
                try {
                    FenLeiName list = (FenLeiName) bundlea.getSerializable("Catalog");
                    catalogName = list.getName();
                    try {
                        catalogType = list.getAttributes().getmId();
                    } catch (Exception e) {
                        e.printStackTrace();
                        catalogType="-1";
                    }
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

                    viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager(), list, fragments));
                    pageSlidingTab.setViewPager(viewPager);
                    if (count == 1) pageSlidingTab.setVisibility(View.GONE);
                    tipView.setVisibility(View.GONE);
                } else {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.NO_DATA, "该分类暂没有节目推荐\n还有更多精彩节目赶紧去看看吧");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                closeDialog();
                ToastUtils.showVolleyError(context);
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
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
    public static void closeDialog() {
        if (dialog != null) dialog.dismiss();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                ProgramActivity.close();
                break;
        }
    }

    @Override
    public void onDestroy() {
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
    }
}
