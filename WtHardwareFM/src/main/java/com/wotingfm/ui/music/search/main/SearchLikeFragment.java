package com.wotingfm.ui.music.search.main;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseadapter.MyFragmentPagerAdapter;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.search.adapter.SearchHistoryAdapter;
import com.wotingfm.ui.music.search.adapter.SearchKeyAdapter;
import com.wotingfm.ui.music.search.adapter.SearchLikeAdapter;
import com.wotingfm.ui.music.search.dao.SearchHistoryDao;
import com.wotingfm.ui.music.search.fragment.RadioFragment;
import com.wotingfm.ui.music.search.fragment.SequFragment;
import com.wotingfm.ui.music.search.fragment.SoundFragment;
import com.wotingfm.ui.music.search.fragment.TTSFragment;
import com.wotingfm.ui.music.search.fragment.TotalFragment;
import com.wotingfm.ui.music.search.model.History;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 界面搜索界面
 * @author 辛龙
 * 2016年4月16日
 */
public class SearchLikeFragment extends Fragment implements
        View.OnClickListener, AdapterView.OnItemClickListener {
    private SearchHistoryDao shd;               // 搜索数据库
    private History history;                    // 数据库信息
    private SearchLikeAdapter adapter;
    private SearchKeyAdapter searchKeyAdapter;
    private List<History> historyDatabaseList;
    private List<String> topSearchList = new ArrayList<>();
    private List<String> hotSearchList = new ArrayList<>();
    private Dialog dialog;                      // 加载数据对话框
    private GridView flowTopSearch;             // 热门搜索内容
    private RelativeLayout linearHistory;       // 搜索历史
    private LinearLayout linearStatusFirst;     // 搜索初始化状态
    private LinearLayout linearStatusThird;     // 搜索结束展示搜索结果状态

    private static ViewPager mPager;
    private static TextView textTotal;                 // 全部
    private static TextView textSequ;                  // 专辑
    private static TextView textSound;                 // 声音
    private static TextView textRadio;                 // 电台
    private static TextView textTts;                   // TTS
    private TextView linearTopTitle;            // 展示 "热门搜索"

    private ListView mListView;                 // 搜索联想词列表 搜索中的状态
    private GridView gridHistory;               // 搜索历史内容
    private EditText mEtSearchContent;          // 输入  搜索内容
    private ImageView imageEditClear;           // 清除搜索框中输入的内容 搜索框有内容是显示
    private ImageView image;                    // 页面指示器 图片
    private TextView textSpeakStatus;           // 显示语音搜索的状态

    private int bmpW;
    private int offset;
    private String tag = "SEARCH_LIKE_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private static FragmentActivity context;
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_searchlike, container, false);
            rootView.setOnClickListener(this);
            context=getActivity();
            GlobalConfig.voiceRecognizer = BroadcastConstants.SEARCHVOICE;
            initImage();            // 初始化指示器图片
            initViews();            // 初始化视图
            initDao();              // 初始化数据库命令执行对象
            initTextWatcher();      // 输入框监听

            // 获取热门搜索对应接口 HotKey
            dialog = DialogUtils.Dialogph(context, "通讯中");
            send();

            // 广播注册
            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(BroadcastConstants.SEARCHVOICE);
            context.registerReceiver(mBroadcastReceiver, mFilter);
        }
        return rootView;
    }

    // 初始化控件
    private void initViews() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);                          // 返回
        rootView.findViewById(R.id.lin_head_right).setOnClickListener(this);                         // 搜索
        rootView.findViewById(R.id.img_clear).setOnClickListener(this);                              // 清理历史搜索数据库

        linearTopTitle = (TextView) rootView.findViewById(R.id.lin_top_title);                       // "热门搜索"
        linearStatusFirst = (LinearLayout) rootView.findViewById(R.id.lin_searchlike_status_first);  // 搜索初始化状态
        linearStatusThird = (LinearLayout) rootView.findViewById(R.id.lin_searchlike_status_third);  // 搜索结束状态
        linearHistory = (RelativeLayout) rootView.findViewById(R.id.lin_history);                    // 搜索历史

        mEtSearchContent = (EditText) rootView.findViewById(R.id.et_searchlike);                     // 搜索框输入的内容

        flowTopSearch = (GridView) rootView.findViewById(R.id.gv_topsearch);                         // 展示热门搜索词
        flowTopSearch.setSelector(new ColorDrawable(Color.TRANSPARENT));
        flowTopSearch.setOnItemClickListener(new MyGridViewListener());

        gridHistory = (GridView) rootView.findViewById(R.id.gv_history);                             // 展示搜索历史词
        gridHistory.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridHistory.setOnItemClickListener(this);

        mListView = (ListView) rootView.findViewById(R.id.lv_searchlike_status_second);              // 搜索时的联想词 搜索中状态
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));                        // 取消默认背景颜色
        setListItemListener();

        imageEditClear = (ImageView) rootView.findViewById(R.id.img_edit_clear);                     // 清理 editText 内容
        imageEditClear.setOnClickListener(this);

        textTotal = (TextView) rootView.findViewById(R.id.tv_total);                                 // 全部
        textTotal.setOnClickListener(new TextClickListener(0));

        textSequ = (TextView) rootView.findViewById(R.id.tv_sequ);                                   // 专辑
        textSequ.setOnClickListener(new TextClickListener(1));

        textSound = (TextView) rootView.findViewById(R.id.tv_sound);                                 // 声音
        textSound.setOnClickListener(new TextClickListener(2));

        textRadio = (TextView) rootView.findViewById(R.id.tv_radio);                                 // 电台
        textRadio.setOnClickListener(new TextClickListener(3));

        textTts = (TextView) rootView.findViewById(R.id.tv_tts);                                     // TTS
        textTts.setOnClickListener(new TextClickListener(4));

        mPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(5);
        initViewPager();
    }

    // 广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(BroadcastConstants.SEARCHVOICE)) {
                String searchString = intent.getStringExtra("VoiceContent");
                if (!searchString.trim().equals("")) {
                    mEtSearchContent.setText(searchString);
                    textSpeakStatus.setText("正在搜索:" + searchString);
                    checkEdit(searchString);
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 跳转到第三页的结果当中 并且默认打开第一页
        linearStatusFirst.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mEtSearchContent.setText(historyDatabaseList.get(position).getPlayName());
    }

    // GridView Item OnClick
    class MyGridViewListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            linearStatusFirst.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            mEtSearchContent.setText(hotSearchList.get(position));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:    // 返回
                MainActivity.changeOne();
                break;
            case R.id.lin_head_right:   // 搜索
                String searchString = mEtSearchContent.getText().toString().trim();
                if (!searchString.equals("")) {
                    checkEdit(searchString);
                }
                break;
            case R.id.img_clear:        // 清空搜索历史
                shd.deleteHistoryall(CommonUtils.getUserId(context));
                getHistoryListNow();
                break;
            case R.id.img_edit_clear:   // 清理
                mEtSearchContent.setText("");
                mListView.setVisibility(View.GONE);
                linearStatusFirst.setVisibility(View.VISIBLE);
                linearStatusThird.setVisibility(View.INVISIBLE);
                imageEditClear.setVisibility(View.GONE);
                getHistoryListNow();
                break;
        }
    }

    // 初始化界面
    private void initViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new TotalFragment());
        fragmentList.add(new SequFragment());
        fragmentList.add(new SoundFragment());
        fragmentList.add(new RadioFragment());
        fragmentList.add(new TTSFragment());
        mPager.setAdapter(new MyFragmentPagerAdapter(getChildFragmentManager(), fragmentList));
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());   // 页面变化时的监听器
        mPager.setCurrentItem(0);                                       // 设置当前显示标签页为第
    }

    // 初始化数据库
    private void initDao() {
        shd = new SearchHistoryDao(context);
        String userId = CommonUtils.getUserId(context);
        history = new History(userId, "");
        if (userId != null) {
            historyDatabaseList = shd.queryHistory(history);
        } else {
            historyDatabaseList = shd.queryHistory();
        }
        if (historyDatabaseList.size() != 0) {
            linearHistory.setVisibility(View.VISIBLE);
            gridHistory.setAdapter(new SearchHistoryAdapter(context, historyDatabaseList));
        } else {
            linearHistory.setVisibility(View.GONE);
        }
    }

    // 监控 editText 的当前输入状态 进行界面逻辑变更
    private void initTextWatcher() {
        mEtSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals("")) {
                    imageEditClear.setVisibility(View.VISIBLE);
                    linearStatusFirst.setVisibility(View.GONE);
                    sendKey(s.toString());// 发送搜索变更内容
                    mListView.setVisibility(View.VISIBLE);
                    linearStatusThird.setVisibility(View.GONE);
                } else {
                    imageEditClear.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    linearStatusFirst.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    protected void setListItemListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = topSearchList.get(position);
                if (s != null && !s.equals("")) {
                    checkEdit(topSearchList.get(position));
                }
            }
        });
    }

    // 获取搜索历史数据库中的数据
    private void getHistoryListNow() {
        History history1 = new History(CommonUtils.getUserId(context), "");
        String userId = CommonUtils.getUserId(context);
        if (userId != null) {
            historyDatabaseList = shd.queryHistory(history1);
        } else {
            historyDatabaseList = shd.queryHistory();
        }
        if (historyDatabaseList.size() == 0) {
            linearHistory.setVisibility(View.GONE);
        } else {
            linearHistory.setVisibility(View.VISIBLE);
            gridHistory.setAdapter(new SearchHistoryAdapter(context, historyDatabaseList));
        }
    }

    private void checkEdit(String str) {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            linearStatusFirst.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            linearStatusThird.setVisibility(View.VISIBLE);

            Intent mIntent = new Intent();
            mIntent.setAction(BroadcastConstants.SEARCH_VIEW_UPDATE);
            mIntent.putExtra("SearchStr", str);
            if (CommonUtils.getUserId(context) == null) {
                history = new History("wotingkeji", str);
            } else {
                history = new History(CommonUtils.getUserId(context), str);
            }
            shd.deleteHistory(str);
            shd.addHistory(history);
            getHistoryListNow();
            context.sendBroadcast(mIntent);
            mPager.setCurrentItem(0);
        } else {
            ToastUtils.show_always(context, "网络连接失败，请稍后重试");
        }
    }

    // 每个字检索
    protected void sendKey(String keyword) {
        // 以下操作需要网络支持
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (dialog != null) dialog.dismiss();
            ToastUtils.show_always(context, "网络连接失败，请检查网络设置!");
            return;
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("FunType", "1");
            jsonObject.put("WordSize", "10");
            jsonObject.put("ReturnType", "2");
            jsonObject.put("KeyWord", keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.searchHotKeysUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    try {
                        topSearchList.clear();
                        String s = result.getString("SysKeyList");
                        String[] s1 = s.split(",");
                        for (int i = 0; i < s1.length; i++) {
                            topSearchList.add(s1[i]);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if (topSearchList != null && topSearchList.size() > 0) {
                            if (searchKeyAdapter == null) {
                                mListView.setAdapter(searchKeyAdapter = new SearchKeyAdapter(context, topSearchList));
                            } else {
                                searchKeyAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 得到搜索热词，返回的是两个 list
    private void send() {
        // 以下操作需要网络支持
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (dialog != null) dialog.dismiss();
            ToastUtils.show_always(context, "网络连接失败，请检查网络设置!");
            return;
        }

        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("FunType", "1");
            jsonObject.put("WordSize", "12");
            jsonObject.put("ReturnType", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.getHotSearch, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    try {
                        hotSearchList.clear();
                        String s = result.getString("SysKeyList");
                        String[] s1 = s.split(",");
                        for (String str : s1) {
                            hotSearchList.add(str);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if (hotSearchList != null && hotSearchList.size() != 0) {
                            linearStatusFirst.setVisibility(View.VISIBLE);
                            adapter = new SearchLikeAdapter(context, hotSearchList);
                            flowTopSearch.setAdapter(adapter);
                        } else {
                            linearTopTitle.setVisibility(View.GONE);
                            flowTopSearch.setVisibility(View.GONE);
                        }
                    } else {
                        linearTopTitle.setVisibility(View.GONE);
                        flowTopSearch.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private static void viewChange(int index) {
        if (index == 0) {
            textTotal.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 1) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 2) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 3) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
            textTts.setTextColor(context.getResources().getColor(R.color.group_item_text2));
        } else if (index == 4) {
            textTotal.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSequ.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textSound.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textRadio.setTextColor(context.getResources().getColor(R.color.group_item_text2));
            textTts.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
        }
    }

    public static void updateViewPager(String mediaType) {
        int index = 0;
        if (mediaType != null && !mediaType.equals("")) {
            switch (mediaType) {
                case "SEQU":
                    index = 1;
                    break;
                case "AUDIO":
                    index = 2;
                    break;
                case "RADIO":
                    index = 3;
                    break;
                case "TTS":
                    index = 4;
                    break;
            }
            mPager.setCurrentItem(index);
            viewChange(index);
        }
    }

    // 动态设置 cursor 的宽
    public void initImage() {
        image = (ImageView) rootView.findViewById(R.id.cursor);
        LayoutParams lp = image.getLayoutParams();
        lp.width = (PhoneMessage.ScreenWidth / 5);
        image.setLayoutParams(lp);
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.left_personal_bg).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offset = (screenW / 5 - bmpW) / 2;
        // imageView 设置平移，使下划线平移到初始位置（平移一个 offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        image.setImageMatrix(matrix);
    }

    /**
     * 全部 专辑 声音 电台 TTS 的点击监听
     */
    class TextClickListener implements View.OnClickListener {
        private int index = 0;

        public TextClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
            viewChange(index);
        }
    }

    /**
     * 页面改变监听器
     */
    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int one = offset * 2 + bmpW;// 两个相邻页面的偏移量
        private int currIndex;

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(currIndex * one, arg0 * one, 0, 0);// 平移动画
            currIndex = arg0;
            animation.setFillAfter(true);   // 动画终止时停留在最后一帧，不然会回到没有执行前的状态
            animation.setDuration(200);     // 动画持续时间0.2秒
            image.startAnimation(animation);// 是用 ImageView 来显示动画的
            viewChange(currIndex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        mEtSearchContent = null;
        flowTopSearch = null;
        gridHistory = null;
        linearStatusFirst = null;
        linearHistory = null;
        mListView = null;
        imageEditClear = null;
        textSpeakStatus = null;
        linearStatusThird = null;
        historyDatabaseList = null;
        shd = null;
        searchKeyAdapter = null;
        context.unregisterReceiver(mBroadcastReceiver);
        context = null;
    }

}
