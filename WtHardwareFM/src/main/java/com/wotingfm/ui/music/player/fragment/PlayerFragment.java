package com.wotingfm.ui.music.player.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.kingsoft.media.httpcache.OnCacheStatusListener;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.music.comment.CommentActivity;
import com.wotingfm.ui.music.common.service.DownloadService;
import com.wotingfm.ui.music.download.activity.DownloadActivity;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.favorite.activity.FavoriteActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.adapter.PlayerListAdapter;
import com.wotingfm.ui.music.player.adapter.gvMoreAdapter;
import com.wotingfm.ui.music.player.model.LanguageSearch;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.player.model.ShareModel;
import com.wotingfm.ui.music.player.programme.ProgrammeActivity;
import com.wotingfm.ui.music.playhistory.activity.PlayHistoryActivity;
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.album.model.ContentInfo;
import com.wotingfm.ui.music.video.IntegrationPlayer;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PlayermoreUtil;
import com.wotingfm.util.TimeUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 播放主页
 * 2016年2月4日
 * @author 辛龙
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, XListView.IXListViewListener, AdapterView.OnItemClickListener {
    private final static int PLAY = 1;// 播放
    private final static int PAUSE = 2;// 暂停
    private final static int CONTINUE = 4;// 继续播放
    private final static int TIME_UI = 10;// 更新时间

    private static SharedPreferences sp = BSApplication.SharedPreferences;// 数据存储
    public static FragmentActivity context;
    public static IntegrationPlayer mPlayer;// 播放器
    private static SearchPlayerHistoryDao mSearchHistoryDao;// 搜索历史数据库
    private static int moreType;
    private FileInfoDao mFileDao;// 文件相关数据库
    private MessageReceiver mReceiver;// 广播接收

    private static Handler mHandler;
    private static PlayerListAdapter adapter;

    private static Dialog dialog;// 加载数据对话框
    private static Dialog wifiDialog;// WIFI 提醒对话框
    private View rootView;

    public static TextView mPlayAudioTitleName;// 正在播放的节目的标题

    private static ImageView mPlayAudioImageCover;// 播放节目的封面
    private static ImageView mPlayImageStatus;// 播放状态图片  播放 OR 暂停

    private static SeekBar mSeekBar;// 播放进度
    public static TextView mSeekBarStartTime;// 进度的开始时间
    public static TextView mSeekBarEndTime;// 播放节目总长度

    public static TextView mPlayAudioTextLike;// 喜欢播放节目
    public static TextView mPlayAudioTextProgram;// 节目单
    public static TextView mPlayAudioTextDownLoad;// 下载

    public static TextView mPlayAudioTextComment;// 评论
    public static TextView mPlayAudioTextMore;// 更多

    private View mProgramDetailsView;// 节目详情
    private TextView mProgramVisible;// "隐藏" OR "显示"
    private static TextView mProgramTextAnchor;// 主播
    public static TextView mProgramTextSequ;// 专辑
    public static TextView mProgramSources;// 来源
    public static TextView mProgramTextDescn;// 节目介绍

    private static XListView mListView;// 播放列表

    public static int timerService;// 当前节目播放剩余时间长度
    public static int TextPage = 1;// 文本搜索 page
    private static int sendType;// 第一次获取数据是有分页加载的
    private static int page = 1;// mainPage
    private static int voicePage = 1;// 语音搜索 page
    private static int num;// == -2 播放器没有播放  == -1 播放器里边的数据不在 list 中  == 其它 是在 list 中

    private int refreshType = 0;// 是不是第一次请求数据

    private boolean detailsFlag = false;// 是否展示节目详情
    private boolean first = true;// 第一次进入界面

    private static String playType;// 当前播放的媒体类型
    private static String ContentFavorite;// 是否喜欢
    private String voiceStr;// 语音搜索内容

    private static ArrayList<LanguageSearchInside> allList = new ArrayList<>();
    private Dialog moreDialog;

    private List<ShareModel> mList;
    private String SequId;
    private String SequDesc;
    private String SequImage;
    private String SequName;
    private boolean IsSequ;

    /////////////////////////////////////////////////////////////
    // 以下是生命周期方法
    /////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setReceiver();// 注册广播接收器
        initData();// 初始化数据
        initDao();// 初始化数据库命令执行对象
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=56275014");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_play, container, false);
        View headView = LayoutInflater.from(context).inflate(R.layout.headview_fragment_play, null);
        initViews(headView);// 设置界面
        initEvent(headView);// 设置控件点击事件
        return rootView;
    }

    // 初始化视图
    private void initViews(View view) {
        // -----------------  HeadView 相关控件初始化 START  ----------------
        ImageView mPlayAudioImageCoverMask = (ImageView) view.findViewById(R.id.image_liu);// 封面图片的六边形遮罩
        mPlayAudioImageCoverMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_bd));

        mPlayAudioTitleName = (TextView) view.findViewById(R.id.tv_name);// 正在播放的节目的标题
        mPlayAudioImageCover = (ImageView) view.findViewById(R.id.img_news);// 播放节目的封面

        mPlayImageStatus = (ImageView) view.findViewById(R.id.img_play);// 播放状态图片  播放 OR 暂停

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);// 播放进度
        mSeekBarStartTime = (TextView) view.findViewById(R.id.time_start);// 进度的开始时间
        mSeekBarEndTime = (TextView) view.findViewById(R.id.time_end);// 播放节目总长度

        mPlayAudioTextLike = (TextView) view.findViewById(R.id.tv_like);// 喜欢播放节目
        mPlayAudioTextProgram = (TextView) view.findViewById(R.id.tv_programme);// 节目单
        mPlayAudioTextDownLoad = (TextView) view.findViewById(R.id.tv_download);// 下载
        mPlayAudioTextComment = (TextView) view.findViewById(R.id.tv_comment);// 评论
        mPlayAudioTextMore = (TextView) view.findViewById(R.id.tv_more);// 更多

        mProgramDetailsView = view.findViewById(R.id.rv_details);// 节目详情布局
        mProgramVisible = (TextView) view.findViewById(R.id.tv_details_flag);// "隐藏" OR "显示"
        mProgramTextAnchor = (TextView) view.findViewById(R.id.tv_zhu_bo);// 主播
        mProgramTextSequ = (TextView) view.findViewById(R.id.tv_sequ);// 专辑
        mProgramSources = (TextView) view.findViewById(R.id.tv_origin);// 来源
        mProgramTextDescn = (TextView) view.findViewById(R.id.tv_desc);// 节目介绍

        // ------- 暂无标签 ------
//        GridView flowTag = (GridView) view.findViewById(R.id.gv_tag);
//        List<String> testList = new ArrayList<>();
//        testList.add("逻辑思维");
//        testList.add("不是我不明白");
//        testList.add("今天你吃饭了吗");
//        testList.add("看世界");
//        testList.add("影视资讯");
//        flowTag.setAdapter(new SearchHotAdapter(context, testList));// 展示热门搜索词
        // ------- 暂无标签 ------
        // -----------------  HeadView 相关控件初始化 END  ----------------

        // -----------------  RootView 相关控件初始化 START  ----------------
        mListView = (XListView) rootView.findViewById(R.id.listView);
        // -----------------  RootView 相关控件初始化 END  ----------------

        mListView.addHeaderView(view);
        wifiDialog();// wifi 提示 dialog
    }

    // 初始化点击事件
    private void initEvent(View view) {
        // -----------------  HeadView 相关控件设置监听 START  ----------------
        view.findViewById(R.id.lin_left).setOnClickListener(this);// 播放上一首
        view.findViewById(R.id.lin_center).setOnClickListener(this);// 播放
        view.findViewById(R.id.lin_right).setOnClickListener(this);// 播放下一首

        mPlayAudioTextLike.setOnClickListener(this);// 喜欢播放节目
        mPlayAudioTextProgram.setOnClickListener(this);// 节目单
        mPlayAudioTextDownLoad.setOnClickListener(this);// 下载
        mPlayAudioTextComment.setOnClickListener(this);// 评论
        mPlayAudioTextMore.setOnClickListener(this);// 更多
        mProgramVisible.setOnClickListener(this);// 点击显示节目详情
        setListener();
        // -----------------  HeadView 相关控件设置监听 END  ----------------

        // -----------------  RootView 相关控件设置监听 START  ----------------
        mListView.setXListViewListener(this);// 设置下拉刷新和加载更多监听
        mListView.setOnItemClickListener(this);
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(true);
        // -----------------  RootView 相关控件设置监听 END  ----------------
    }

    // 初始化数据
    private void initData() {
        mHandler = new Handler();
        mPlayer = IntegrationPlayer.getInstance(context);
    }

    // 注册广播接收器
    private void setReceiver() {
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PLAYERVOICE);
            filter.addAction(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
            context.registerReceiver(mReceiver, filter);
        }
    }

    // 初始化数据库命令执行对象
    private void initDao() {
        mSearchHistoryDao = new SearchPlayerHistoryDao(context);
        mFileDao = new FileInfoDao(context);
    }

    private void setListener() {
        mSeekBar.setEnabled(false);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                stopSeekBarTouch();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChange(progress, fromUser);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (first) {
            // 从播放历史界面或者我喜欢的界面跳转到该界面
            String enter = sp.getString(StringConstant.PLAYHISTORYENTER, "false");
            String news = sp.getString(StringConstant.PLAYHISTORYENTERNEWS, "");
            if (enter.equals("true")) {
                TextPage = 1;
                sendTextRequest(news);
                SharedPreferences.Editor et = sp.edit();
                et.putString(StringConstant.PLAYHISTORYENTER, "false");
                if (et.commit()) Log.v("TAG", "数据 commit 失败!");
            } else {
                if (CommonHelper.checkNetwork(context)) {
                    dialog = DialogUtils.Dialogph(context, "通讯中");
                    firstSend();
                } else {
                    mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                    setPullAndLoad(true, false);
                }
            }
            first = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) { // 注销广播
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (mPlayer != null) {
            mPlayer.destroyPlayer();
            mPlayer = null;
        }
    }

    /////////////////////////////////////////////////////////////
    // 以下是播放方法
    /////////////////////////////////////////////////////////////
    // 点击 item 的播放事件
    private static void itemPlay(int position) {
        if (CommonHelper.checkNetwork(context)) {
            boolean isN = getWifiSet(); // 是否开启非 wifi 网络流量提醒
            if (isN && getWifiShow(context) && GlobalConfig.CURRENT_NETWORK_STATE_TYPE != 1) {
                wifiDialog.show();
            } else {
                GlobalConfig.playerObject = allList.get(position);
                addDb(allList.get(position));
                play(position);
            }
        } else {
            localPlay(position);// 播放本地文件
        }
    }

    // 播放本地文件
    private static boolean localPlay(int number) {
        if (allList.get(number).getLocalurl() != null) {
            GlobalConfig.playerObject = allList.get(number);
            playType = GlobalConfig.playerObject.getMediaType();
            addDb(allList.get(number));
            musicPlay("file:///" + allList.get(number).getLocalurl());
            return true;
        } else {
            return false;
        }
    }

    // 在 play 方法里初始化播放器对象 在 musicPlay 方法里执行相关操作 要考虑 enterCenter 方法
    protected static void play(int number) {
        if (allList != null && allList.get(number) != null && allList.get(number).getMediaType() != null) {
            playType = allList.get(number).getMediaType();
            if (playType.equals("AUDIO") || playType.equals("RADIO")) {
                if (allList.get(number).getContentPlay() != null) {
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
                    resetView();
                    if (allList.get(number).getLocalurl() != null) {
                        musicPlay("file:///" + allList.get(number).getLocalurl());
                    } else {
                        musicPlay(allList.get(number).getContentPlay());
                    }
                    GlobalConfig.playerObject = allList.get(number);
                    resetHeadView();
                    num = number;
                }
            } else if (playType.equals("TTS")) {
                if (allList.get(number).getContentURI() != null && allList.get(number).getContentURI().trim().length() > 0) {
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
                    resetView();
                    musicPlay(allList.get(number).getContentURI());
                    GlobalConfig.playerObject = allList.get(number);
                    resetHeadView();
                    num = number;
                } else {
                    getContentNews(allList.get(number).getContentId(), number);// 当 contentUri 为空时 获取内容
                }
            }
        }
    }

    static String local;

    private static void musicPlay(String s) {
        if (local == null) {
            local = s;
            mUIHandler.sendEmptyMessage(PLAY);
            mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
            setPlayingType();
        } else if (local.equals(s)) {
            if (playType.equals("TTS")) {
                if (mPlayer.isPlaying()) {
                    mPlayer.stopPlay();
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_stop);
                    setPauseType();
                } else {
                    local = s;
                    mUIHandler.sendEmptyMessage(PLAY);
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
                    setPlayingType();
                }
            } else {
                if (mPlayer.isPlaying()) {
                    mPlayer.pausePlay();
                    if (playType.equals("AUDIO")) {
                        mUIHandler.removeMessages(TIME_UI);
                    }
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_stop);
                    setPauseType();
                } else {
                    mPlayer.continuePlay();
                    mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
                    setPlayingType();
                }
            }
        } else {
            local = s;
            mUIHandler.sendEmptyMessage(PLAY);
            mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
            setPlayingType();
        }

        if (playType != null && playType.trim().length() > 0 && playType.equals("AUDIO")) {
            mSeekBar.setEnabled(true);
        } else {
            mSeekBar.setEnabled(false);
        }
        mUIHandler.sendEmptyMessage(TIME_UI);
    }

    public static void playNoNet() {
        LanguageSearchInside mContent = getDaoList(context);
        if (mContent == null) return;
        GlobalConfig.playerObject = mContent;
        playType = mContent.getMediaType();
        if (allList.size() > 0) allList.clear();
        allList.add(mContent);
        allList.get(0).setType("2");
        if (adapter == null) {
            mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
        } else {
            adapter.notifyDataSetChanged();
        }
        mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
        if (!TextUtils.isEmpty(GlobalConfig.playerObject.getLocalurl())) {
            setPullAndLoad(false, false);
            resetHeadView();
            musicPlay("file:///" + GlobalConfig.playerObject.getLocalurl());
        }
    }

    /////////////////////////////////////////////////////////////
    // 以下是播放控制方法
    /////////////////////////////////////////////////////////////
    // 播放上一首节目
    public static void playLast() {
        if (num - 1 >= 0) {
            num = num - 1;
            itemPlay(num);
        } else {
            ToastUtils.show_always(context, "已经是第一条数据了");
        }
    }

    // 播放下一首
    public static void playNext() {
        if (allList != null && allList.size() > 0) {
            if (num + 1 < allList.size()) {
                num = num + 1;
            } else {
                num = 0;
            }
            itemPlay(num);
        }
    }

    // 按中间按钮的操作方法
    public static void enterCenter() {
        if (GlobalConfig.playerObject != null && GlobalConfig.playerObject.getMediaType() != null) {
            playType = GlobalConfig.playerObject.getMediaType();
            if (playType.equals("AUDIO") || playType.equals("RADIO")) {
                if (GlobalConfig.playerObject.getContentPlay() != null) {
                    resetView();
                    if (GlobalConfig.playerObject.getLocalurl() != null) {
                        musicPlay("file:///" + GlobalConfig.playerObject.getLocalurl());
                    } else {
                        musicPlay(GlobalConfig.playerObject.getContentPlay());
                    }
                    resetHeadView();
                } else {
                    ToastUtils.show_short(context, "暂不支持播放");
                }
            } else if (playType.equals("TTS")) {
                if (GlobalConfig.playerObject.getContentURI() != null && GlobalConfig.playerObject.getContentURI().trim().length() > 0) {
                    resetView();
                    musicPlay(GlobalConfig.playerObject.getContentURI());
                    resetHeadView();
                } else {
                    getContentNews(GlobalConfig.playerObject.getContentId(), 0);// 当 contentUri 为空时 获取内容
                }
            }
        } else {
            ToastUtils.show_always(context, "当前播放对象为空");
        }
    }

    // 停止拖动进度条
    private void stopSeekBarTouch() {
        Log.e("停止拖动进度条", "停止拖动进度条");
    }

    // SeekBar 的更改操作
    private void progressChange(int progress, boolean fromUser) {
        if (fromUser && playType != null && playType.equals("AUDIO")) {
            mPlayer.setCurrentTime((long) progress);
            mUIHandler.sendEmptyMessage(TIME_UI);
        }
    }

    /////////////////////////////////////////////////////////////
    // 以下是界面设置方法
    /////////////////////////////////////////////////////////////
    // 设置当前为播放状态
    private static void setPlayingType() {
        if (num >= 0) {
            allList.get(num).setType("2");
            adapter.notifyDataSetChanged();
        }
    }

    // 设置当前为暂停状态
    private static void setPauseType() {
        if (num >= 0) {
            allList.get(num).setType("0");
            adapter.notifyDataSetChanged();
        }
    }

    // 更新时间展示数据
    private static void updateTextViewWithTimeFormat(TextView view, long second) {
        int hh = (int) (second / 3600);
        int mm = (int) (second % 3600 / 60);
        int ss = (int) (second % 60);
        String strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        view.setText(strTemp);
    }

    // 重置 View
    private static void resetView() {
        for (int i = 0; i < allList.size(); i++) {
            allList.get(i).setType("1");
        }
        GlobalConfig.playerObject.setType("2");
        adapter.notifyDataSetChanged();
    }

    // 设置 headView 的界面
    protected static void resetHeadView() {
        if (GlobalConfig.playerObject != null) {
            String type = GlobalConfig.playerObject.getMediaType();

            // 播放的节目标题
            String contentTitle = GlobalConfig.playerObject.getContentName();
            if (contentTitle != null) {
                mPlayAudioTitleName.setText(contentTitle);
            } else {
                mPlayAudioTitleName.setText("未知");
            }

            // 播放的节目封面图片
            String url = GlobalConfig.playerObject.getContentImg();
            if (url != null) {// 有封面图片
                if (!url.startsWith("http")) {
                    url = GlobalConfig.imageurl + url;
                }
                url = AssembleImageUrlUtils.assembleImageUrl180(url);
                Picasso.with(context).load(url.replace("\\/", "/")).into(mPlayAudioImageCover);
            } else {// 没有封面图片设置默认图片
                mPlayAudioImageCover.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
            }

            if (GlobalConfig.playerObject.getMediaType() != null) {
                if (GlobalConfig.playerObject.getMediaType().equals("AUDIO")) {
                    moreType = 1;
                } else {
                    moreType = 0;
                }
            }
            // 喜欢状态
            String contentFavorite = GlobalConfig.playerObject.getContentFavorite();
            if (type != null && type.equals("TTS")) {// TTS 不支持喜欢
                mPlayAudioTextLike.setClickable(false);
                mPlayAudioTextLike.setText("喜欢");
                mPlayAudioTextLike.setTextColor(context.getResources().getColor(R.color.gray));
                mPlayAudioTextLike.setCompoundDrawablesWithIntrinsicBounds(
                        null, context.getResources().getDrawable(R.mipmap.wt_dianzan_nomal_gray), null, null);
            } else {
                mPlayAudioTextLike.setClickable(true);
                mPlayAudioTextLike.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                if (contentFavorite == null || contentFavorite.equals("0")) {
                    mPlayAudioTextLike.setText("喜欢");
                    mPlayAudioTextLike.setCompoundDrawablesWithIntrinsicBounds(
                            null, context.getResources().getDrawable(R.mipmap.wt_dianzan_nomal), null, null);
                } else {
                    mPlayAudioTextLike.setText("已喜欢");
                    mPlayAudioTextLike.setCompoundDrawablesWithIntrinsicBounds(
                            null, context.getResources().getDrawable(R.mipmap.wt_dianzan_select), null, null);
                }
            }

            // 节目单 RADIO
            if (type != null && type.equals("RADIO")) {
                mPlayAudioTextProgram.setVisibility(View.VISIBLE);
                mPlayAudioTextProgram.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                mPlayAudioTextProgram.setCompoundDrawablesWithIntrinsicBounds(
                        null, context.getResources().getDrawable(R.mipmap.img_play_more_jiemudan), null, null);
            } else {// 电台 有节目单
                mPlayAudioTextProgram.setVisibility(View.GONE);
            }

            // 下载状态
            if (type != null && type.equals("AUDIO")) {// 可以下载
                mPlayAudioTextDownLoad.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(GlobalConfig.playerObject.getLocalurl())) {// 已下载
                    mPlayAudioTextDownLoad.setClickable(false);
                    mPlayAudioTextDownLoad.setCompoundDrawablesWithIntrinsicBounds(
                            null, context.getResources().getDrawable(R.mipmap.wt_play_xiazai_no), null, null);
                    mPlayAudioTextDownLoad.setTextColor(context.getResources().getColor(R.color.gray));
                    mPlayAudioTextDownLoad.setText("已下载");
                } else {// 没有下载
                    mPlayAudioTextDownLoad.setClickable(true);
                    mPlayAudioTextDownLoad.setCompoundDrawablesWithIntrinsicBounds(
                            null, context.getResources().getDrawable(R.mipmap.wt_play_xiazai), null, null);
                    mPlayAudioTextDownLoad.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                    mPlayAudioTextDownLoad.setText("下载");
                }
            } else {// 不可以下载
                if (type != null && type.equals("TTS")) {
                    mPlayAudioTextDownLoad.setVisibility(View.VISIBLE);
                    mPlayAudioTextDownLoad.setClickable(false);
                    mPlayAudioTextDownLoad.setCompoundDrawablesWithIntrinsicBounds(
                            null, context.getResources().getDrawable(R.mipmap.wt_play_xiazai_no), null, null);
                    mPlayAudioTextDownLoad.setTextColor(context.getResources().getColor(R.color.gray));
                    mPlayAudioTextDownLoad.setText("下载");
                } else {
                    mPlayAudioTextDownLoad.setVisibility(View.GONE);
                }
            }

            // 评论  TTS 不支持评论
            if (type != null && type.equals("TTS")) {
                mPlayAudioTextComment.setClickable(false);
                mPlayAudioTextComment.setTextColor(context.getResources().getColor(R.color.gray));
                mPlayAudioTextComment.setCompoundDrawablesWithIntrinsicBounds(
                        null, context.getResources().getDrawable(R.mipmap.wt_comment_image_gray), null, null);
            } else if (type != null && !type.equals("TTS")) {
                mPlayAudioTextComment.setClickable(true);
                mPlayAudioTextComment.setTextColor(context.getResources().getColor(R.color.dinglan_orange));
                mPlayAudioTextComment.setCompoundDrawablesWithIntrinsicBounds(
                        null, context.getResources().getDrawable(R.mipmap.wt_comment_image), null, null);
            }

            // 节目详情 主播  暂没有主播
            mProgramTextAnchor.setText("未知");

            // 节目详情 专辑
            String sequName = GlobalConfig.playerObject.getSequName();
            if (sequName != null && !sequName.trim().equals("") && !sequName.equals("null")) {
                mProgramTextSequ.setText(sequName);
            } else {
                mProgramTextSequ.setText("暂无专辑");
            }

            // 节目详情 来源
            String contentPub = GlobalConfig.playerObject.getContentPub();
            if (contentPub != null && !contentPub.trim().equals("") && !contentPub.equals("null")) {
                mProgramSources.setText(contentPub);
            } else {
                mProgramSources.setText("暂无来源");
            }

            // 节目详情 介绍
            String contentDescn = GlobalConfig.playerObject.getContentDescn();
            if (contentDescn != null && !contentDescn.trim().equals("") && !contentDescn.equals("null")) {
                mProgramTextDescn.setText(contentDescn);
            } else {
                mProgramTextDescn.setText("暂无介绍");
            }
        } else {
            ToastUtils.show_always(context, "播放器数据获取异常，请退出程序后尝试");
        }
    }

    protected void setData(LanguageSearchInside fList, ArrayList<LanguageSearchInside> list) {
        // 如果数据库里边的数据不是空的，在 headView 设置该数据
        GlobalConfig.playerObject = fList;
        resetHeadView();
        // 如果进来就要看到 在这里设置界面
        if (!TextUtils.isEmpty(GlobalConfig.playerObject.getPlayerInTime()) &&
                !TextUtils.isEmpty(GlobalConfig.playerObject.getPlayerAllTime())
                && !GlobalConfig.playerObject.getPlayerInTime().equals("null") && !GlobalConfig.playerObject.getPlayerAllTime().equals("null")) {
            long current = Long.valueOf(GlobalConfig.playerObject.getPlayerInTime());
            long duration = Long.valueOf(GlobalConfig.playerObject.getPlayerAllTime());
            updateTextViewWithTimeFormat(mSeekBarStartTime, (int) (current / 1000));
            updateTextViewWithTimeFormat(mSeekBarEndTime, (int) (duration / 1000));
            mSeekBar.setMax((int) duration);
            mSeekBar.setProgress((int) current);
        } else {
            mSeekBarStartTime.setText("00:00:00");
            mSeekBarEndTime.setText("00:00:00");
        }
        allList.clear();
        allList.add(fList);
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i).getContentPlay() != null && !list.get(i).getContentPlay().equals(fList.getContentPlay())) {
                allList.add(list.get(i));
            }
        }
        for (int i = 0; i < allList.size(); i++) {
            String s = allList.get(i).getContentPlay();
            if (s != null && s.equals(GlobalConfig.playerObject.getContentPlay())) {
                allList.get(i).setType("0");
                num = i;
            }
        }
        mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
    }

    protected void setDataForNoList(ArrayList<LanguageSearchInside> list) {
        GlobalConfig.playerObject = list.get(0);
        resetHeadView();
        mSeekBarStartTime.setText("00:00:00");
        mSeekBarEndTime.setText("00:00:00");
        allList.clear();
        allList.addAll(list);
        allList.get(0).setType("0");
        num = 0;
        mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
    }

    // wifi 弹出框
    private void wifiDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_set, null);
        wifiDialog = new Dialog(context, R.style.MyDialog);
        wifiDialog.setContentView(dialog1);
        wifiDialog.setCanceledOnTouchOutside(true);
        wifiDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        // 取消播放
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiDialog.dismiss();
            }
        });
        // 允许本次播放
        dialog1.findViewById(R.id.tv_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(num);
                wifiDialog.dismiss();
            }
        });
        // 不再提醒
        dialog1.findViewById(R.id.tv_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(num);
                SharedPreferences.Editor et = sp.edit();
                et.putString(StringConstant.WIFISHOW, "false");
                if (et.commit()) Log.i("TAG", "commit Fail");
                wifiDialog.dismiss();
            }
        });
    }

    /////////////////////////////////////////////////////////////
    // 以下是业务方法
    /////////////////////////////////////////////////////////////
    // 下拉刷新
    public void onRefresh() {
        if (!CommonHelper.checkNetwork(context)) {
            if (dialog != null) dialog.dismiss();
            setPullAndLoad(true, false);
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshType = 1;
                if (sendType == 1) {
                    firstSend();
                } else if (sendType == 2) {
                    sendTextRequest(sendTextContent);
                } else if (sendType == 3) {
                    searchByVoice(voiceStr);
                }
            }
        }, 1000);
    }

    // 加载更多
    public void onLoadMore() {
        if (!CommonHelper.checkNetwork(context)) {
            if (dialog != null) dialog.dismiss();
            setPullAndLoad(true, false);
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshType = 2;
                if (sendType == 1) {
                    firstSend();
                } else if (sendType == 2) {
                    sendTextRequest(sendTextContent);
                } else if (sendType == 3) {
                    searchByVoice(voiceStr);
                }
            }
        }, 1000);
    }

    // 是否开启非 wifi 网络流量提醒
    private static boolean getWifiSet() {
        String wifiSet = sp.getString(StringConstant.WIFISET, "true");
        return !wifiSet.trim().equals("") && wifiSet.equals("true");
    }

    // 是否网络弹出框提醒
    private static boolean getWifiShow(Context context) {
        String wifiShow = sp.getString(StringConstant.WIFISHOW, "true");
        if (wifiShow.equals("true")) {
            CommonHelper.checkNetworkStatus(context);// 网络设置获取
            return true;
        } else {
            return false;
        }
    }

    // 获取数据库数据
    private static LanguageSearchInside getDaoList(Context context) {
        if (mSearchHistoryDao == null) mSearchHistoryDao = new SearchPlayerHistoryDao(context);
        List<PlayerHistory> historyDatabaseList = mSearchHistoryDao.queryHistory();
        if (historyDatabaseList != null && historyDatabaseList.size() > 0) {
            PlayerHistory historyNew = historyDatabaseList.get(0);
            LanguageSearchInside historyNews = new LanguageSearchInside();
            historyNews.setType("1");
            historyNews.setContentURI(historyNew.getPlayerUrI());
            historyNews.setContentPersons(historyNew.getPlayCount());
            historyNews.setContentKeyWord("");
            historyNews.setcTime(historyNew.getPlayerInTime());
            historyNews.setContentSubjectWord("");
            historyNews.setContentTimes(historyNew.getPlayerAllTime());
            historyNews.setContentName(historyNew.getPlayerName());
            historyNews.setContentPubTime("");
            historyNews.setContentPub(historyNew.getPlayerFrom());
            historyNews.setContentPlay(historyNew.getPlayerUrl());
            historyNews.setMediaType(historyNew.getPlayerMediaType());
            historyNews.setContentId(historyNew.getContentID());
            historyNews.setContentDescn(historyNew.getPlayerContentDescn());
            historyNews.setPlayCount(historyNew.getPlayCount());
            historyNews.setContentImg(historyNew.getPlayerImage());
            try {
                if (historyNew.getPlayerAllTime() != null && historyNew.getPlayerAllTime().equals("")) {
                    historyNews.setPlayerAllTime("0");
                } else {
                    historyNews.setPlayerAllTime(historyNew.getPlayerAllTime());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (historyNew.getPlayerInTime() != null && historyNew.getPlayerInTime().equals("")) {
                    historyNews.setPlayerInTime("0");
                } else {
                    historyNews.setPlayerInTime(historyNew.getPlayerInTime());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            historyNews.setContentShareURL(historyNew.getPlayContentShareUrl());
            historyNews.setContentFavorite(historyNew.getContentFavorite());
            historyNews.setLocalurl(historyNew.getLocalurl());
            historyNews.setSequId(historyNew.getSequId());
            historyNews.setSequName(historyNew.getSequName());
            historyNews.setSequDesc(historyNew.getSequDesc());
            historyNews.setSequImg(historyNew.getSequImg());
            historyNews.setContentPlayType(historyNew.getContentPlayType());
            return historyNews;
        } else {
            return null;
        }
    }

    // 把数据添加数据库----播放历史数据库
    private static void addDb(LanguageSearchInside languageSearchInside) {
        String playerName = languageSearchInside.getContentName();
        String playerImage = languageSearchInside.getContentImg();
        String playerUrl = languageSearchInside.getContentPlay();
        String playerUrI = languageSearchInside.getContentURI();
        String playerMediaType = languageSearchInside.getMediaType();
        String playContentShareUrl = languageSearchInside.getContentShareURL();
        String playerAllTime = languageSearchInside.getPlayerAllTime();
        String playerInTime = languageSearchInside.getPlayerInTime();
        String playerContentDesc = languageSearchInside.getContentDescn();
        String playerNum = languageSearchInside.getPlayCount();
        String playerZanType = "false";
        String playerFrom = languageSearchInside.getContentPub();
        String playerFromId = "";
        String playerFromUrl = "";
        String playerAddTime = Long.toString(System.currentTimeMillis());
        String bjUserId = CommonUtils.getUserId(context);
        if (languageSearchInside.getContentFavorite() != null) {
            String contentFavorite = languageSearchInside.getContentFavorite();
            if (contentFavorite != null) {
                if (contentFavorite.equals("0") || contentFavorite.equals("1")) {
                    ContentFavorite = contentFavorite;
                }
            }
        } else {
            ContentFavorite = languageSearchInside.getContentFavorite();
        }
        String ContentID = languageSearchInside.getContentId();
        String localUrl = languageSearchInside.getLocalurl();
        String sequName = languageSearchInside.getSequName();
        String sequId = languageSearchInside.getSequId();
        String sequDesc = languageSearchInside.getSequDesc();
        String sequImg = languageSearchInside.getSequImg();
        String ContentPlayType = languageSearchInside.getContentPlayType();

        PlayerHistory history = new PlayerHistory(playerName, playerImage,
                playerUrl, playerUrI, playerMediaType, playerAllTime,
                playerInTime, playerContentDesc, playerNum, playerZanType,
                playerFrom, playerFromId, playerFromUrl, playerAddTime,
                bjUserId, playContentShareUrl, ContentFavorite, ContentID, localUrl, sequName, sequId, sequDesc, sequImg);

        if (mSearchHistoryDao == null)
            mSearchHistoryDao = new SearchPlayerHistoryDao(context);// 如果数据库没有初始化，则初始化 db
        if (playerMediaType != null && playerMediaType.trim().length() > 0 && playerMediaType.equals("TTS")) {
            mSearchHistoryDao.deleteHistoryById(ContentID);
        } else {
            mSearchHistoryDao.deleteHistory(playerUrl);
        }
        mSearchHistoryDao.addHistory(history);
    }

    // 内容的下载
    private void download() {
        LanguageSearchInside data = GlobalConfig.playerObject;
        if (data == null || !data.getMediaType().equals("AUDIO")) {
            ToastUtils.show_always(context, "此节目无法下载");
            return;
        }

        if (data.getLocalurl() != null) {
            ToastUtils.show_always(context, "此节目已经保存到本地，请到已下载界面查看");
            return;
        }
        // 对数据进行转换
        List<ContentInfo> dataList = new ArrayList<>();
        ContentInfo m = new ContentInfo();
        m.setAuthor(data.getContentPersons());
        m.setContentPlay(data.getContentPlay());
        m.setContentImg(data.getContentImg());
        m.setContentName(data.getContentName());
        m.setContentPub(data.getContentPub());
        m.setContentTimes(data.getContentTimes());
        m.setUserid(CommonUtils.getUserId(context));
        m.setDownloadtype("0");
        if (data.getSeqInfo() == null || data.getSeqInfo().getContentName() == null || data.getSeqInfo().getContentName().equals("")) {
            m.setSequname(data.getContentName());
        } else {
            m.setSequname(data.getSeqInfo().getContentName());
        }
        if (data.getSeqInfo() == null || data.getSeqInfo().getContentId() == null || data.getSeqInfo().getContentId().equals("")) {
            m.setSequid(data.getContentId());
        } else {
            m.setSequid(data.getSeqInfo().getContentId());
        }
        if (data.getSeqInfo() == null || data.getSeqInfo().getContentImg() == null || data.getSeqInfo().getContentImg().equals("")) {
            m.setSequimgurl(data.getContentImg());
        } else {
            m.setSequimgurl(data.getSeqInfo().getContentImg());
        }
        if (data.getSeqInfo() == null || data.getSeqInfo().getContentDesc() == null || data.getSeqInfo().getContentDesc().equals("")) {
            m.setSequdesc(data.getContentDescn());
        } else {
            m.setSequdesc(data.getSeqInfo().getContentDesc());
        }
        dataList.add(m);
        // 检查是否重复,如果不重复插入数据库，并且开始下载，重复了提示
        List<FileInfo> fileDataList = mFileDao.queryFileInfoAll(CommonUtils.getUserId(context));
        if (fileDataList.size() != 0) {// 此时有下载数据
            boolean isDownload = false;
            for (int j = 0; j < fileDataList.size(); j++) {
                if (fileDataList.get(j).getUrl().equals(m.getContentPlay())) {
                    if (fileDataList.get(j).getLocalurl() != null) {
                        isDownload = true;
                        break;
                    }
                }
            }
            if (isDownload) {
                ToastUtils.show_always(context, m.getContentName() + "已经存在于下载列表");
            } else {
                mFileDao.insertFileInfo(dataList);
                ToastUtils.show_always(context, m.getContentName() + "已经开始下载");
                List<FileInfo> fileUnDownLoadList = mFileDao.queryFileInfo("false", CommonUtils.getUserId(context));// 未下载列表
                for (int kk = 0; kk < fileUnDownLoadList.size(); kk++) {
                    if (fileUnDownLoadList.get(kk).getDownloadtype() == 1) {
                        DownloadService.workStop(fileUnDownLoadList.get(kk));
                        mFileDao.updataDownloadStatus(fileUnDownLoadList.get(kk).getUrl(), "2");
                    }
                }
                for (int k = 0; k < fileUnDownLoadList.size(); k++) {
                    if (fileUnDownLoadList.get(k).getUrl().equals(m.getContentPlay())) {
                        FileInfo file = fileUnDownLoadList.get(k);
                        mFileDao.updataDownloadStatus(m.getContentPlay(), "1");
                        DownloadService.workStart(file);
                        Intent p_intent = new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED);
                        context.sendBroadcast(p_intent);
                        break;
                    }
                }
            }
        } else {// 此时库里没数据
            mFileDao.insertFileInfo(dataList);
            ToastUtils.show_always(context, m.getContentName() + "已经插入了下载列表");
            List<FileInfo> fileUnDownloadList = mFileDao.queryFileInfo("false", CommonUtils.getUserId(context));// 未下载列表
            for (int k = 0; k < fileUnDownloadList.size(); k++) {
                if (fileUnDownloadList.get(k).getUrl().equals(m.getContentPlay())) {
                    FileInfo file = fileUnDownloadList.get(k);
                    mFileDao.updataDownloadStatus(m.getContentPlay(), "1");
                    DownloadService.workStart(file);
                    Intent p_intent = new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED);
                    context.sendBroadcast(p_intent);
                    break;
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////
    // 以下是系统方法
    /////////////////////////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_like:// 喜欢
                if (!CommonHelper.checkNetwork(context)) return;
                if (GlobalConfig.playerObject == null) return;
                if (GlobalConfig.playerObject.getContentFavorite() != null && !GlobalConfig.playerObject.getContentFavorite().equals("")) {
                    sendFavorite();
                } else {
                    ToastUtils.show_always(context, "本节目暂时不支持喜欢!");
                }
                break;
            case R.id.tv_details_flag:// 节目详情
                if (!detailsFlag) {
                    mProgramVisible.setText("  隐藏  ");
                    mProgramDetailsView.setVisibility(View.VISIBLE);
                } else {
                    mProgramVisible.setText("  显示  ");
                    mProgramDetailsView.setVisibility(View.GONE);
                }
                detailsFlag = !detailsFlag;
                break;
            case R.id.lin_left:// 上一首
                playLast();
                break;
            case R.id.lin_center:// 播放
                enterCenter();
                break;
            case R.id.lin_right:// 下一首
                playNext();
                break;
            case R.id.tv_more:// 更多
                moreDialog();
                moreDialog.show();
                break;
            case R.id.tv_programme:// 节目单
                if (!CommonHelper.checkNetwork(context)) return;
                Intent p = new Intent(context, ProgrammeActivity.class);
                Bundle b = new Bundle();
                b.putString("BcId", GlobalConfig.playerObject.getContentId());
                p.putExtras(b);
                startActivity(p);
                break;
            case R.id.tv_comment:// 专辑
                if (GlobalConfig.playerObject != null) {
                    try {
                        if (GlobalConfig.playerObject.getSequId() != null) {
                            if (GlobalConfig.playerObject.getSequId() != null) {
                                SequId = GlobalConfig.playerObject.getSequId();
                                SequDesc = GlobalConfig.playerObject.getSequDesc();
                                SequImage = GlobalConfig.playerObject.getSequImg();
                                SequName = GlobalConfig.playerObject.getSequName();
                                IsSequ = true;
                            } else {
                                IsSequ = false;
                            }
                        } else {
                            if (GlobalConfig.playerObject.getSeqInfo() != null) {
                                if (GlobalConfig.playerObject.getSeqInfo().getContentId() != null) {
                                    SequId = GlobalConfig.playerObject.getSeqInfo().getContentId();
                                    SequDesc = GlobalConfig.playerObject.getSeqInfo().getContentDesc();
                                    SequImage = GlobalConfig.playerObject.getSeqInfo().getContentImg();
                                    SequName = GlobalConfig.playerObject.getSeqInfo().getContentName();
                                    IsSequ = true;
                                } else {
                                    IsSequ = false;
                                }
                            } else {
                                IsSequ = false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (IsSequ) {
                    Intent intent = new Intent(context, AlbumActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "player");
                    bundle.putString("contentName", SequName);
                    bundle.putString("contentDesc", SequDesc);
                    bundle.putString("contentId", SequId);
                    bundle.putString("contentImg", SequImage);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    ToastUtils.show_always(context, "此节目目前没有所属专辑");
                }
                break;
            case R.id.tv_download:// 下载
                download();
                break;
        }
    }

    static Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_UI: // 更新进度及时间
                    if (GlobalConfig.playerObject == null || GlobalConfig.playerObject.getMediaType() == null)
                        return;
                    if (GlobalConfig.playerObject.getMediaType().equals("AUDIO")) {
                        long currPosition = mPlayer.getCurrentTime();
                        long duration = mPlayer.getTotalTime();
                        updateTextViewWithTimeFormat(mSeekBarStartTime, (int) (currPosition / 1000));
                        updateTextViewWithTimeFormat(mSeekBarEndTime, (int) (duration / 1000));
                        mSeekBar.setMax((int) duration);

                        if (isCacheFinish(local))
                            mSeekBar.setSecondaryProgress((int) mPlayer.getTotalTime());
                        timerService = (int) (duration - currPosition);
                        if (mPlayer.isPlaying()) mSeekBar.setProgress((int) currPosition);

                        mSearchHistoryDao.updatePlayerInTime(GlobalConfig.playerObject.getContentPlay(), currPosition, duration);
                    } else {
                        int _currPosition = TimeUtils.getTime(System.currentTimeMillis());
                        int _duration = 24 * 60 * 60;
                        updateTextViewWithTimeFormat(mSeekBarStartTime, _currPosition);
                        updateTextViewWithTimeFormat(mSeekBarEndTime, _duration);
                        mSeekBar.setMax(_duration);
                        mSeekBar.setProgress(_currPosition);
                    }
                    mUIHandler.sendEmptyMessageDelayed(TIME_UI, 1000);
                    break;
                case PLAY:// 播放
                    if (playType.equals("AUDIO")) {
                        if (GlobalConfig.playerObject.getLocalurl() != null) {
                            mPlayer.startPlay("AUDIO", null, local);
                            mSeekBar.setSecondaryProgress((int) mPlayer.getTotalTime());
                        } else {
                            mPlayer.startPlay("AUDIO", local, null);
                            if (!isCacheFinish(local)) {// 判断是否已经缓存过  没有则开始缓存
                                BSApplication.getKSYProxy().registerCacheStatusListener(new OnCacheStatusListener() {
                                    @Override
                                    public void OnCacheStatus(String url, long sourceLength, int percentsAvailable) {
                                        secondProgress = mPlayer.getTotalTime() * percentsAvailable / 100;
                                        mSeekBar.setSecondaryProgress((int) secondProgress);
                                    }
                                }, local);
                            }
                        }
                    } else {
                        mPlayer.startPlay(playType, local, null);
                    }
                    break;
                case PAUSE:
                    mPlayer.pausePlay();
                    break;
                case CONTINUE:
                    mPlayer.continuePlay();
                    break;
            }
        }
    };

    static long secondProgress;

    // listView 的 item 点击事件监听
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        num = position - 2;
        itemPlay(num);// item 的播放
    }

    // 广播接收器
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastConstants.PLAY_TEXT_VOICE_SEARCH:
                    sendTextContent = intent.getStringExtra("text");
                    sendTextRequest(sendTextContent);
                    break;
                case BroadcastConstants.PLAYERVOICE:
                    voiceStr = intent.getStringExtra("VoiceContent");
                    if (CommonHelper.checkNetwork(context)) {
                        if (!voiceStr.trim().equals("")) {
                            voicePage = 1;
                            searchByVoice(voiceStr);
                        }
                    }
                    break;
            }
        }
    }

    private static List<String> contentUrlList = new ArrayList<>();// 保存 ContentURI 用于去重  用完即 clear

    /////////////////////////////////////////////////////////////
    // 以下是网络请求操作
    /////////////////////////////////////////////////////////////
    // 第一次进入该界面时候的数据
    private void firstSend() {
        sendType = 1;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PageType", "0");
            jsonObject.put("Page", String.valueOf(page));
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.mainPageUrl, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType.equals("1001")) {
                        page++;
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                        ArrayList<LanguageSearchInside> list = new Gson().fromJson(arg1.getString("List"), new TypeToken<List<LanguageSearchInside>>() {}.getType());
                        if (refreshType == 0) {
                            LanguageSearchInside fList = getDaoList(context);// 得到数据库里边的第一条数据
                            if (list != null && list.size() > 0 && fList != null) {// 有返回数据并且数据库中有数据
                                num = -1;
                                setData(fList, list);
                            } else if (list != null && list.size() > 0 && fList == null) {// 有返回数据但数据库中没有数据
                                if (list.get(0) != null) {
                                    num = 0;
                                    setDataForNoList(list);
                                } else {
                                    num = -2;
                                }
                            } else if (list != null && list.size() == 0 && fList != null) {// 没有返回数据但数据库中有数据
                                list.add(fList);
                                num = -1;
                                setData(fList, list);
                            } else {// 没有任何数据
                                num = -2;
                                setPullAndLoad(true, false);
                                mListView.setAdapter(new PlayerListAdapter(context, allList));
                            }
                        } else if (refreshType == 1) {// 下拉刷新
                            if (allList.size() > 0) {
                                for (int i = 0, size = allList.size(); i < size; i++) {
                                    contentUrlList.add(allList.get(i).getContentURI());
                                }
                            }
                            if (list.size() > 0) {
                                for (int i = 0, size = list.size(); i < size; i++) {
                                    if (!contentUrlList.contains(list.get(i).getContentURI())) {
                                        allList.add(0, list.get(i));
                                    }
                                }
                            }
                            contentUrlList.clear();
                            if (GlobalConfig.playerObject != null && allList != null) {
                                for (int i = 0; i < allList.size(); i++) {
                                    if (allList.get(i).getContentPlay().equals(GlobalConfig.playerObject.getContentPlay())) {
                                        allList.get(i).setType("0");
                                        num = i;
                                    }
                                }
                            }
                            if (adapter == null) {
                                mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            setPullAndLoad(true, true);
                        } else {// 加载更多
                            mListView.stopLoadMore();
                            allList.addAll(list);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        if (dialog != null) dialog.dismiss();
                        if (allList.size() <= 0 || adapter == null) {
                            mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                        }
                        setPullAndLoad(true, false);
                        ToastUtils.show_always(context, "暂时没有更多的推荐了!");
                    }
                    resetHeadView();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
                if (allList.size() <= 0 || adapter == null) {
                    mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                }
                setPullAndLoad(true, false);
            }
        });
    }

    // 喜欢---不喜欢操作
    private static void sendFavorite() {
        dialog = DialogUtils.Dialogph(context, "通讯中");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", GlobalConfig.playerObject.getMediaType());
            jsonObject.put("ContentId", GlobalConfig.playerObject.getContentId());
            if (GlobalConfig.playerObject.getContentFavorite().equals("0")) {
                jsonObject.put("Flag", 1);
            } else {
                jsonObject.put("Flag", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.clickFavoriteUrl, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                try {
                    ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && (ReturnType.equals("1001") || ReturnType.equals("1005"))) {
                        if (GlobalConfig.playerObject.getContentFavorite().equals("0")) {
                            mPlayAudioTextLike.setText("已喜欢");
                            mPlayAudioTextLike.setCompoundDrawablesWithIntrinsicBounds(
                                    null, context.getResources().getDrawable(R.mipmap.wt_dianzan_select), null, null);
                            GlobalConfig.playerObject.setContentFavorite("1");
                            if (num > 0) allList.get(num).setContentFavorite("1");
                        } else {
                            mPlayAudioTextLike.setText("喜欢");
                            mPlayAudioTextLike.setCompoundDrawablesWithIntrinsicBounds(
                                    null, context.getResources().getDrawable(R.mipmap.wt_dianzan_nomal), null, null);
                            GlobalConfig.playerObject.setContentFavorite("0");
                            if (num > 0) allList.get(num).setContentFavorite("0");
                        }
                    } else {
                        ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                    }
                } catch (Exception e) {
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

    // 获取 TTS 的播放内容
    private static void getContentNews(String id, final int number) {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "TTS");
            jsonObject.put("ContentId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestTextVoicePost(GlobalConfig.getContentById, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String MainList;

            @Override
            protected void requestSuccess(JSONObject result) {
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    MainList = result.getString("ResultInfo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        LanguageSearchInside lists = new Gson().fromJson(MainList, new TypeToken<LanguageSearchInside>() {
                        }.getType());
                        String ContentURI = lists.getContentURI();
                        Log.e("ContentURI", ContentURI + "");
                        if (ContentURI != null && ContentURI.trim().length() > 0) {
                            mPlayImageStatus.setImageResource(R.mipmap.wt_play_play);
                            if (allList.get(number).getContentName() != null) {
                                mPlayAudioTitleName.setText(allList.get(number).getContentName());
                            } else {
                                mPlayAudioTitleName.setText("未知");
                            }
                            String url = allList.get(number).getContentImg();
                            if (url != null) {
                                if (!url.startsWith("http")) url = GlobalConfig.imageurl + url;
                                url = AssembleImageUrlUtils.assembleImageUrl180(url);
                                Picasso.with(context).load(url.replace("\\/", "/")).into(mPlayAudioImageCover);
                            } else {
                                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                                mPlayAudioImageCover.setImageBitmap(bmp);
                            }
                            for (int i = 0; i < allList.size(); i++) {
                                allList.get(i).setType("1");
                            }
                            allList.get(number).setType("2");
                            adapter.notifyDataSetChanged();
                            GlobalConfig.playerObject = allList.get(number);
                            musicPlay(ContentURI);
                            resetHeadView();// 页面的对象改变，根据对象重新设置属性
                            num = number;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                    }
                } else {
                    ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private String sendTextContent = "";// 关键字

    // 获取与文字相关的内容数据
    private void sendTextRequest(String contentName) {
        final LanguageSearchInside fList = getDaoList(context);// 得到数据库里边的第一条数据
        if (TextPage == 1) {
            num = 0;
            allList.clear();
            if (fList != null) allList.add(fList);
            GlobalConfig.playerObject = allList.get(num);
            if (adapter == null) {
                mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
            } else {
                adapter.notifyDataSetChanged();
            }
            itemPlay(0);
        }
        sendType = 2;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("SearchStr", contentName);
            jsonObject.put("PageType", "0");
            jsonObject.put("Page", TextPage);
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestTextVoicePost(GlobalConfig.getSearchByText, jsonObject, new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        LanguageSearch lists = new Gson().fromJson(result.getString("ResultList"), new TypeToken<LanguageSearch>() {}.getType());
                        List<LanguageSearchInside> list = lists.getList();
                        if (list != null && list.size() != 0 && fList != null) {
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getContentPlay() != null && list.get(i).getContentPlay().equals(fList.getContentPlay())) {
                                    list.remove(i);
                                }
                            }
                            if (TextPage == 1 || refreshType == 2) {
                                allList.addAll(list);
                            } else if (refreshType == 1) {// 刷新
                                for (int i = 0, size = allList.size(); i < size; i++) {
                                    contentUrlList.add(allList.get(i).getContentURI());
                                }
                                for (int i = 0, size = list.size(); i < size; i++) {
                                    if (!contentUrlList.contains(list.get(i).getContentURI())) {
                                        allList.add(0, list.get(i));
                                    }
                                }
                                contentUrlList.clear();
                            }
                            if (adapter == null) {
                                mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            TextPage++;
                            setPullAndLoad(true, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setPullAndLoad(true, false);
                    }
                } else {
                    ToastUtils.show_always(context, "已经没有相关数据啦");
                    setPullAndLoad(true, false);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                setPullAndLoad(true, false);
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 语音搜索请求
    private void searchByVoice(String str) {
        sendType = 3;
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("SearchStr", str);
            jsonObject.put("PageType", "0");
            jsonObject.put("Page", voicePage);
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestTextVoicePost(GlobalConfig.searchvoiceUrl, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType.equals("1001")) {
                        LanguageSearch lists = new Gson().fromJson(result.getString("ResultList"), new TypeToken<LanguageSearch>() {}.getType());
                        List<LanguageSearchInside> list = lists.getList();
                        if (list.size() != 0) {
                            if (voicePage == 1) {
                                num = 0;
                                allList.clear();
                                allList.addAll(list);
                                GlobalConfig.playerObject = allList.get(0);
                                itemPlay(0);
                            } else if (refreshType == 1) {
                                for (int i = 0, size = allList.size(); i < size; i++) {
                                    contentUrlList.add(allList.get(i).getContentURI());
                                }
                                for (int i = 0, size = list.size(); i < size; i++) {
                                    if (!contentUrlList.contains(list.get(i).getContentURI())) {
                                        allList.add(0, list.get(i));
                                    }
                                }
                                contentUrlList.clear();
                            } else if (refreshType == 2) {
                                allList.addAll(list);
                            }
                            if (adapter == null) {
                                mListView.setAdapter(adapter = new PlayerListAdapter(context, allList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            voicePage++;
                            setPullAndLoad(true, true);
                        }
                    } else {
                        ToastUtils.show_always(context, "已经没有相关数据啦");
                        setPullAndLoad(true, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "已经没有相关数据啦");
                    setPullAndLoad(true, false);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                setPullAndLoad(true, false);
            }
        });
    }

    // 更多
    private void moreDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_player_more, null);
        GridView gv_more = (GridView) dialog.findViewById(R.id.gv_more);
        TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        moreDialog = new Dialog(context, R.style.MyDialog);
        moreDialog.setContentView(dialog);
        Window window = moreDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = screen;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        moreDialog.setCanceledOnTouchOutside(true);
        moreDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        if (moreType == 0) {// 电台
            mList = PlayermoreUtil.getPlayMoreList("RADIO");
        } else {// 非电台类内容
            mList = PlayermoreUtil.getPlayMoreList("AUDIO");
        }
        gvMoreAdapter shareadapter = new gvMoreAdapter(context, mList);
        gv_more.setAdapter(shareadapter);
        gv_more.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv_more.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callMore(position);// 呼出更多
                moreDialog.dismiss();
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreDialog.isShowing()) {
                    moreDialog.dismiss();
                }
            }
        });
    }

    // 更多回调
    private void callMore(int position) {
        if (moreType == 0) {// 电台调用
            switch (position) {
                case 0:// 播放历史
                    startActivity(new Intent(context, PlayHistoryActivity.class));
                    break;
                case 1:// 我喜欢的
                    startActivity(new Intent(context, FavoriteActivity.class));
                    break;
                case 2:// 分享
                    ToastUtils.show_always(context, "点击了分享按钮");
                    break;
                case 3:// 评论
                    if (!CommonHelper.checkNetwork(context)) return;
                    if (!TextUtils.isEmpty(GlobalConfig.playerObject.getContentId()) && !TextUtils.isEmpty(GlobalConfig.playerObject.getMediaType())) {
                        if (CommonUtils.getUserIdNoImei(context) != null && !CommonUtils.getUserIdNoImei(context).equals("")) {
                            Intent intent = new Intent(context, CommentActivity.class);
                            intent.putExtra("contentId", GlobalConfig.playerObject.getContentId());
                            intent.putExtra("MediaType", GlobalConfig.playerObject.getMediaType());
                            startActivity(intent);
                        } else {
                            ToastUtils.show_always(context, "请先登录~~");
                        }
                    } else {
                        ToastUtils.show_always(context, "当前播放的节目的信息有误，无法获取评论列表");
                    }
                    break;
            }
        } else {// 节目类调用
            switch (position) {
                case 0:// 播放历史
                    startActivity(new Intent(context, PlayHistoryActivity.class));
                    break;
                case 1:// 我喜欢的
                    startActivity(new Intent(context, FavoriteActivity.class));
                    break;
                case 2:// 本地音频
                    startActivity(new Intent(context, DownloadActivity.class));
                    break;
                case 3:// 分享
                    ToastUtils.show_always(context, "分享");
                    break;
                case 4:// 评论
                    if (!CommonHelper.checkNetwork(context)) return;
                    if (!TextUtils.isEmpty(GlobalConfig.playerObject.getContentId()) && !TextUtils.isEmpty(GlobalConfig.playerObject.getMediaType())) {
                        if (CommonUtils.getUserIdNoImei(context) != null && !CommonUtils.getUserIdNoImei(context).equals("")) {
                            Intent intent = new Intent(context, CommentActivity.class);
                            intent.putExtra("contentId", GlobalConfig.playerObject.getContentId());
                            intent.putExtra("MediaType", GlobalConfig.playerObject.getMediaType());
                            startActivity(intent);
                        } else {
                            ToastUtils.show_always(context, "请先登录~~");
                        }
                    } else {
                        ToastUtils.show_always(context, "当前播放的节目的信息有误，无法获取评论列表");
                    }
                    break;
            }
        }
    }

    // 设置刷新和加载
    private static void setPullAndLoad(boolean isPull, boolean isLoad) {
        mListView.setPullRefreshEnable(isPull);
        mListView.setPullLoadEnable(isLoad);
        mListView.stopRefresh();
        mListView.stopLoadMore();
    }

    // 判断是否已经缓存完成
    private static boolean isCacheFinish(String url) {
        HashMap<String, File> cacheMap = BSApplication.getKSYProxy().getCachedFileList();
        File cacheFile = cacheMap.get(url);
        return cacheFile != null && cacheFile.length() > 0;
    }
}
