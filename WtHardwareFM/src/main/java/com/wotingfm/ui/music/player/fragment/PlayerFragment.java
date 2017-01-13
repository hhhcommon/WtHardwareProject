package com.wotingfm.ui.music.player.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.adapter.PlayerListAdapter;
import com.wotingfm.ui.music.player.fragment.more.PlayerMoreOperationActivity;
import com.wotingfm.ui.music.player.model.LanguageSearch;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.video.IntegrationPlayer;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.TimeUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.AutoScrollTextView;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.ijkvideo.IjkVideoView;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放主页
 * 2016年2月4日
 *
 * @author 辛龙
 */
public class PlayerFragment extends Fragment implements View.OnClickListener,
        XListView.IXListViewListener, TipView.WhiteViewClick, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {

    public static FragmentActivity context;
    private MessageReceiver mReceiver;// 广播
    private IntegrationPlayer mPlayer;// 播放器
    private SearchPlayerHistoryDao mSearchHistoryDao;// 搜索历史数据库
    private PlayerListAdapter adapter;
    private WindowManager windowManager;

    private List<LanguageSearchInside> playList = new ArrayList<>();// 播放列表
    private List<LanguageSearchInside> subList = new ArrayList<>();// 保存临时数据

    private View rootView;
    private SeekBar mSeekBar;// 播放进度

    private ImageView imagePlay;// 播放 OR 暂停

    private TextView mPlayCurrentTime;// 当前播放时间
    private AutoScrollTextView mPlayAudioTitle;// 当前播放节目的标题
    private ImageView imagePlayCover;// 节目封面图片

    private View recommendView;// 相关推荐部分
    private XListView mListView;// 数据列表
    private TipView tipView;// 没有数据、没有网络提示

    /**
     * 1.== "MAIN_PAGE"  ->  mainPageRequest();
     * 2.== "SEARCH_TEXT"  ->  searchByTextRequest();
     * 3.== "SEARCH_VOICE"  ->  searchByVoiceRequest();
     * Default  == "MAIN_PAGE";
     */
    private String requestType = StringConstant.PLAY_REQUEST_TYPE_MAIN_PAGE;
    private String sendTextContent;// 文字搜索内容
    private String sendVoiceContent;// 语音搜索内容
    private String mediaType;// 当前播放节目类型

    private long totalTime;// 播放总长度
    private int index = -1;// 当前播放在列表中的位置
    private int mainPage = 1;// mainPage
    private int refreshType = 0;// == -1 刷新  == 1 加载更多  == 0 第一次加载
    private boolean isPlaying;// 是否正在播放
    private boolean isInitData;// 第一次进入应用加载数据

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_recommend_visible:// 展示相关推荐列表
                recommendView.setVisibility(View.VISIBLE);
                break;
            case R.id.text_recommend_gone:// 隐藏相关推荐列表
                recommendView.setVisibility(View.GONE);
                break;
            case R.id.play_more:// 更多
                startActivity(new Intent(context, PlayerMoreOperationActivity.class));
                break;
            case R.id.image_play:// 播放 OR 暂停
                play();
                break;
            case R.id.image_right:// 下一首
                next();
                break;
            case R.id.image_left:// 上一首
                last();
                break;
        }
    }

    // 初始化数据
    private void initData() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        registeredBroad();

        mSearchHistoryDao = new SearchPlayerHistoryDao(context);// 数据库对象

        mPlayer = IntegrationPlayer.getInstance();// 播放器对象
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_play, container, false);
            initView();
            initEvent();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        // 百度云播放器
        IjkVideoView BDAudio = (IjkVideoView) rootView.findViewById(R.id.video_view);
        mPlayer.bindService(context, BDAudio);// 绑定服务

        ImageView mPlayAudioImageCoverMask = (ImageView) rootView.findViewById(R.id.play_cover_mask);// 封面图片的六边形遮罩
        mPlayAudioImageCoverMask.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_6_b_y_bd));

        mPlayCurrentTime = (TextView) rootView.findViewById(R.id.play_current_time);// 当前播放时间
        mPlayAudioTitle = (AutoScrollTextView) rootView.findViewById(R.id.play_audio_title);// 当前播放节目的标题
        mPlayAudioTitle.init(windowManager);
        mPlayAudioTitle.startScroll();
        imagePlayCover = (ImageView) rootView.findViewById(R.id.play_cover);// 节目封面图片

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);// 播放进度

        imagePlay = (ImageView) rootView.findViewById(R.id.image_play);// 播放 OR 暂停
        imagePlay.setOnClickListener(this);

        recommendView = rootView.findViewById(R.id.recommend_view);// 相关推荐部分
        mListView = (XListView) rootView.findViewById(R.id.list_view);
        mListView.setXListViewListener(this);

        tipView = (TipView) rootView.findViewById(R.id.tip_view);
        tipView.setWhiteClick(this);

        queryData();
    }

    // 初始化点击事件
    private void initEvent() {
        rootView.findViewById(R.id.text_recommend_visible).setOnClickListener(this);// 展示相关推荐列表
        rootView.findViewById(R.id.text_recommend_gone).setOnClickListener(this);// 隐藏相关推荐列表
        rootView.findViewById(R.id.play_more).setOnClickListener(this);// 更多
        rootView.findViewById(R.id.image_right).setOnClickListener(this);// 下一首
        rootView.findViewById(R.id.image_left).setOnClickListener(this);// 上一首

        mSeekBar.setOnSeekBarChangeListener(this);
        mListView.setOnItemClickListener(this);
    }

    // 获取数据库第一条数据并加入播放列表
    private void queryData() {
        playList.clear();
        LanguageSearchInside languageSearchInside = getDaoList(context);
        if (languageSearchInside != null) {
            playList.add(languageSearchInside);// 将查询得到的第一条数据加入播放列表中
            if (requestType.equals(StringConstant.PLAY_REQUEST_TYPE_SEARCH_TEXT)) {
                ArrayList<LanguageSearchInside> playerList = new ArrayList<>();
                playerList.add(languageSearchInside);
                mPlayer.updatePlayList(playerList);
                index = 0;
                mPlayer.startPlay(index);
            }
        }
        mainPageRequest();
    }

    // 内容主页网络请求
    private void mainPageRequest() {
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == -1) {
            if (refreshType == 0 && playList.size() <= 0) {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
            setPullAndLoad(false, false);
            return;
        }
        String requestUrl;
        switch (requestType) {
            case StringConstant.PLAY_REQUEST_TYPE_SEARCH_TEXT:
                requestUrl = GlobalConfig.getSearchByText;// 文字搜索
                break;
            case StringConstant.PLAY_REQUEST_TYPE_SEARCH_VOICE:
                requestUrl = GlobalConfig.searchvoiceUrl;// 语音搜索
                break;
            default:
                requestUrl = GlobalConfig.mainPageUrl;// 主网络请求
                break;
        }
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            if (requestType != null && requestType.equals(StringConstant.PLAY_REQUEST_TYPE_SEARCH_TEXT)) {
                jsonObject.put("SearchStr", sendTextContent);
            } else if (requestType != null && requestType.equals(StringConstant.PLAY_REQUEST_TYPE_SEARCH_VOICE)) {
                jsonObject.put("SearchStr", sendVoiceContent);
            }
            jsonObject.put("PageType", "0");
            jsonObject.put("Page", String.valueOf(mainPage));
            jsonObject.put("PageSize", "10");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(requestUrl, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType.equals("1001")) {
                        List<LanguageSearchInside> list;
                        if (requestType.equals(StringConstant.PLAY_REQUEST_TYPE_MAIN_PAGE)) {
                            JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultList")).nextValue();
                            String listString = arg1.getString("List");
                            list = new Gson().fromJson(listString, new TypeToken<List<LanguageSearchInside>>() {}.getType());
                        } else {// "SEARCH_TEXT" OR "SEARCH_VOICE"
                            LanguageSearch lists = new Gson().fromJson(result.getString("ResultList"), new TypeToken<LanguageSearch>() {}.getType());
                            list = lists.getList();
                        }
                        subList = clearContentPlayNull(list);// 去空
                        if (subList != null && subList.size() > 0) {
                            mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST, 1000);
                        }
                        setPullAndLoad(true, true);
                        mainPage++;
                        if (tipView.getVisibility() == View.VISIBLE)
                            tipView.setVisibility(View.GONE);
                    } else {
                        setPullAndLoad(true, false);
                        if (refreshType == 0 && playList.size() <= 0) {
                            tipView.setVisibility(View.VISIBLE);
                            tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");
                        } else {
                            mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST, 1000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setPullAndLoad(true, false);
                    if (refreshType == 0 && playList.size() <= 0) {
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    } else {
                        mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST, 1000);
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
                setPullAndLoad(false, false);
                if (refreshType == 0 && playList.size() <= 0) {
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                } else {
                    mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST, 1000);
                }
            }
        });
    }

    // 注册广播
    private void registeredBroad() {
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PLAYERVOICE);// searchByVoice
            filter.addAction(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);// searchByText

            filter.addAction(BroadcastConstants.UPDATE_PLAY_CURRENT_TIME);// 更新当前播放时间
            filter.addAction(BroadcastConstants.UPDATE_PLAY_TOTAL_TIME);// 更新当前播放总时间
            filter.addAction(BroadcastConstants.UPDATE_PLAY_LIST);// 更新播放列表
            filter.addAction(BroadcastConstants.UPDATE_PLAY_VIEW);// 更新播放界面

            // 下载完成更新 LocalUrl
            filter.addAction(BroadcastConstants.PUSH_DOWN_COMPLETED);
            filter.addAction(BroadcastConstants.PUSH_ALLURL_CHANGE);

            context.registerReceiver(mReceiver, filter);
        }
    }

    // 广播接收器
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastConstants.PLAY_TEXT_VOICE_SEARCH:// 文字搜索
                    mainPage = 1;
                    refreshType = 0;
                    requestType = StringConstant.PLAY_REQUEST_TYPE_SEARCH_TEXT;
                    sendTextContent = intent.getStringExtra(StringConstant.TEXT_CONTENT);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            queryData();
                        }
                    }, 500);
                    break;
                case BroadcastConstants.PLAYERVOICE:// 语音搜索
                    sendVoiceContent = intent.getStringExtra(StringConstant.VOICE_CONTENT);
                    if (sendVoiceContent == null || sendVoiceContent.trim().equals("")) return;
                    if (CommonHelper.checkNetwork(context)) {
                        mainPage = 1;
                        refreshType = 0;
                        requestType = StringConstant.PLAY_REQUEST_TYPE_SEARCH_VOICE;
                        mainPageRequest();
                    }
                    break;
                case BroadcastConstants.UPDATE_PLAY_TOTAL_TIME:// 更新时间总长度
                    mediaType = intent.getStringExtra(StringConstant.PLAY_MEDIA_TYPE);
                    totalTime = intent.getLongExtra(StringConstant.PLAY_TOTAL_TIME, -1);
                    if (totalTime == -1) {
                        mSeekBar.setEnabled(false);
                        mSeekBar.setClickable(false);
                        mSeekBar.setMax(24 * 60 * 60);
                    } else {
                        mSeekBar.setEnabled(true);
                        mSeekBar.setClickable(true);
                        mSeekBar.setMax((int) totalTime);
                    }
                    addDb(GlobalConfig.playerObject);// 将播放对象加入数据库
                    break;
                case BroadcastConstants.UPDATE_PLAY_CURRENT_TIME:// 更新当前播放时间
                    if (mediaType != null && mediaType.equals(StringConstant.TYPE_AUDIO)) {
                        long secondProgress = intent.getLongExtra(StringConstant.PLAY_SECOND_PROGRESS, 0);
                        if (secondProgress == -1) {
                            mSeekBar.setSecondaryProgress((int) totalTime);
                        } else if (secondProgress == -100) {
                            mSeekBar.setSecondaryProgress(mSeekBar.getMax());
                        } else {
                            mSeekBar.setSecondaryProgress((int) secondProgress);
                        }
                    }

                    long currentTime = intent.getLongExtra(StringConstant.PLAY_CURRENT_TIME, -1);
                    if (mediaType != null && mediaType.equals(StringConstant.TYPE_AUDIO)) {
                        mSeekBar.setProgress((int) currentTime);
                        updateTextViewWithTimeFormat(mPlayCurrentTime, (int) (currentTime / 1000));
                    } else {
                        int progress = TimeUtils.getTime(currentTime);
                        mSeekBar.setProgress(progress);
                        updateTextViewWithTimeFormat(mPlayCurrentTime, progress);
                    }

                    // playInTime
                    mSearchHistoryDao.updatePlayerInTime(GlobalConfig.playerObject.getContentPlay(), currentTime, totalTime);
                    break;
                case BroadcastConstants.UPDATE_PLAY_VIEW:// 更新界面
                    index = intent.getIntExtra(StringConstant.PLAY_POSITION, 0);// 列表中的位置

                    // 标题
                    String title = GlobalConfig.playerObject.getContentName();
                    if (title == null || title.trim().equals("")) {
                        title = "未知";
                    }

                    mPlayAudioTitle.setText(title);
                    mPlayAudioTitle.init(windowManager);
                    mPlayAudioTitle.startScroll();

                    // 封面图片
                    String coverUrl = GlobalConfig.playerObject.getContentImg();// imagePlayCover
                    if (coverUrl != null) {// 有封面图片
                        if (!coverUrl.startsWith("http")) {
                            coverUrl = GlobalConfig.imageurl + coverUrl;
                        }
                        coverUrl = AssembleImageUrlUtils.assembleImageUrl180(coverUrl);
                        Picasso.with(context).load(coverUrl.replace("\\/", "/")).into(imagePlayCover);
                    } else {// 没有封面图片设置默认图片
                        imagePlayCover.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
                    }

                    // 更新列表视图
                    mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST_VIEW, 0);
                    if (isInitData) {
                        imagePlay.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_play_play));
                        isPlaying = true;
                    }
                    isInitData = true;
                    break;
                case BroadcastConstants.PUSH_DOWN_COMPLETED:// 更新下载列表
                case BroadcastConstants.PUSH_ALLURL_CHANGE:
                    if (mPlayer != null) mPlayer.updateLocalList();
                    break;
            }
        }
    }

    // 处理消息
    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IntegerConstant.PLAY_UPDATE_LIST:// 更新列表
                    if (subList != null && subList.size() > 0) {
                        if (playList.size() > 0) {
                            List<String> contentIdList = new ArrayList<>();// 保存用于区别是否重复的内容
                            String contentId;// 用于区别是否重复
                            for (int i = 0, size = playList.size(); i < size; i++) {
                                contentId = playList.get(i).getContentId();
                                if (contentId != null && !contentId.trim().equals("")) {
                                    contentIdList.add(contentId);
                                }
                            }
                            for (int i = 0, size = subList.size(); i < size; i++) {
                                if (!contentIdList.contains(subList.get(i).getContentId())) {
                                    if (refreshType == -1) {
                                        playList.add(0, subList.get(i));
                                        index++;
                                    } else {
                                        playList.add(subList.get(i));
                                    }
                                }
                            }
                            contentIdList.clear();
                        } else {
                            playList.addAll(subList);
                        }
                    }
                    if (adapter == null) {
                        mListView.setAdapter(adapter = new PlayerListAdapter(context, playList));
                    } else {
//                        adapter.setList(playList);
                        adapter.notifyDataSetChanged();
                        L.v("TAG", "adapter update view");
                    }
                    ArrayList<LanguageSearchInside> playerList = new ArrayList<>();
                    playerList.addAll(playList);
                    mPlayer.updatePlayList(playerList);

                    subList.clear();
                    break;
                case IntegerConstant.PLAY_UPDATE_LIST_VIEW:// 更新列表界面
                    for (int i = 0, size = playList.size(); i < size; i++) {
                        if (i == index) {
                            if (isPlaying) {
                                playList.get(i).setType("2");
                            } else {
                                playList.get(i).setType("0");
                            }
                        } else {
                            playList.get(i).setType("1");
                        }
                    }
                    adapter.setList(playList);
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        refreshType = -1;
        mainPageRequest();
    }

    @Override
    public void onLoadMore() {
        refreshType = 1;
        mainPageRequest();
    }

    @Override
    public void onWhiteViewClick() {
        refreshType = 0;
        mainPageRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) ((ViewGroup) rootView.getParent()).removeView(rootView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {// 解绑服务
            mPlayer.unbindService(context);
            mPlayer = null;
        }
        if (mReceiver != null) {// 注销广播
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (windowManager != null) {
            windowManager = null;
        }
        if (playList != null) {
            playList.clear();
            playList = null;
        }
    }

    // 播放
    private void play() {
        if(GlobalConfig.playerObject == null) return ;
        if (mPlayer.playStatus()) {// 正在播放
            mPlayer.pausePlay();
            imagePlay.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_play_stop));
            isPlaying = false;
        } else {// 暂停状态
            mPlayer.continuePlay();
            imagePlay.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_play_play));
            isPlaying = true;
        }
        mUIHandler.sendEmptyMessageDelayed(IntegerConstant.PLAY_UPDATE_LIST_VIEW, 0);
    }

    // 下一首
    private void next() {
        index = index + 1;
        if (index >= playList.size()) {
            index = 0;
        }
        mPlayer.startPlay(index);
    }

    // 上一首
    private void last() {
        index = index - 1;
        if (index < 0) {
            index = playList.size() - 1;
        }
        mPlayer.startPlay(index);
    }

    // Item OnClick
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position - 1 >= 0) {
            position = position - 1;
            if (index == position) {// 判断和当前播放节目是否相同
                play();
            } else {// 和当前播放节目不相同则直接开始播放
                index = position;
                mPlayer.startPlay(index);
                imagePlay.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_play_play));
            }
        }
    }

    // SeekBar 的更改操作
    private void progressChange(int progress, boolean fromUser) {
        if (fromUser && mediaType != null && mediaType.equals(StringConstant.TYPE_AUDIO)) {
            mPlayer.setPlayCurrentTime((long) progress);
        }
    }

    // 获取数据库数据
    private LanguageSearchInside getDaoList(Context context) {
        if (mSearchHistoryDao == null) mSearchHistoryDao = new SearchPlayerHistoryDao(context);
        List<PlayerHistory> historyDatabaseList = mSearchHistoryDao.queryHistory();
        if (historyDatabaseList != null && historyDatabaseList.size() > 0) {
            PlayerHistory historyNew = historyDatabaseList.get(0);
            LanguageSearchInside historyNews = new LanguageSearchInside();
            historyNews.setType("1");
            historyNews.setContentURI(historyNew.getPlayerUrI());
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
                historyNews.setContentPersons(historyNew.getContentPersons());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    private void addDb(LanguageSearchInside languageSearchInside) {
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
        String ContentFavorite = "";
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

        PlayerHistory history = new PlayerHistory(playerName, playerImage,
                playerUrl, playerUrI, playerMediaType, playerAllTime,
                playerInTime, playerContentDesc, playerNum, playerZanType,
                playerFrom, playerFromId, playerFromUrl, playerAddTime,
                bjUserId, playContentShareUrl, ContentFavorite, ContentID, localUrl, sequName, sequId, sequDesc, sequImg);

        if (mSearchHistoryDao == null)
            mSearchHistoryDao = new SearchPlayerHistoryDao(context);// 如果数据库没有初始化，则初始化 db
        if (playerMediaType != null && playerMediaType.trim().length() > 0 && playerMediaType.equals(StringConstant.TYPE_TTS)) {
            mSearchHistoryDao.deleteHistoryById(ContentID);
        } else {
            mSearchHistoryDao.deleteHistory(playerUrl);
        }
        mSearchHistoryDao.addHistory(history);
    }

    // 更新时间展示数据
    private void updateTextViewWithTimeFormat(TextView view, long second) {
        int hh = (int) (second / 3600);
        int mm = (int) (second % 3600 / 60);
        int ss = (int) (second % 60);
        String strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        view.setText(strTemp);
    }

    // 去除 ContentPlay == null 的数据
    private List<LanguageSearchInside> clearContentPlayNull(List<LanguageSearchInside> list) {
        int index = 0;
        while (index < list.size()) {
            if (list.get(index).getMediaType().equals(StringConstant.TYPE_TTS)) {
                index++;
            } else {
                if (list.get(index).getContentPlay() == null || list.get(index).getContentPlay().trim().equals("")
                        || list.get(index).getContentPlay().trim().toUpperCase().equals("NULL")) {
                    list.remove(index);
                } else {
                    index++;
                }
            }
        }
        return list;
    }

    // 设置刷新和加载
    private void setPullAndLoad(boolean isPull, boolean isLoad) {
        mListView.setPullRefreshEnable(isPull);
        mListView.setPullLoadEnable(isLoad);
        if (refreshType == -1) {
            mListView.stopRefresh();
        } else {
            mListView.stopLoadMore();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressChange(progress, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
