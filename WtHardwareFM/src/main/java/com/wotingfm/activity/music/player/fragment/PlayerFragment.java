package com.wotingfm.activity.music.player.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.squareup.picasso.Picasso;
import com.umeng.socialize.Config;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.wotingfm.R;
import com.wotingfm.activity.music.common.service.DownloadService;
import com.wotingfm.activity.music.download.activity.DownloadActivity;
import com.wotingfm.activity.music.download.dao.FileInfoDao;
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.activity.music.favorite.activity.FavoriteActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.adapter.ImageAdapter;
import com.wotingfm.activity.music.player.adapter.PlayerListAdapter;
import com.wotingfm.activity.music.player.adapter.gvMoreAdapter;
import com.wotingfm.activity.music.player.model.LanguageSearch;
import com.wotingfm.activity.music.player.model.LanguageSearchInside;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.player.model.sharemodel;
import com.wotingfm.activity.music.playhistory.activity.PlayHistoryActivity;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.album.model.ContentInfo;
import com.wotingfm.activity.music.program.schedule.activity.ScheduleActivity;
import com.wotingfm.activity.music.timeset.TimerPowerOffActivity;
import com.wotingfm.activity.music.video.TtsPlayer;
import com.wotingfm.activity.music.video.VlcPlayer;
import com.wotingfm.activity.music.video.WtAudioPlay;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.helper.CommonHelper;
import com.wotingfm.service.timeroffservice;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PlayermoreUtil;
import com.wotingfm.util.ShareUtils;
import com.wotingfm.util.TimeUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 播放主页
 * 2016年2月4日
 * @author 辛龙
 */
public class PlayerFragment extends Fragment implements OnClickListener, XListView.IXListViewListener {
    public static FragmentActivity context;
    // 功能性
    private static SimpleDateFormat format;
    private static SearchPlayerHistoryDao dbDao;
    private FileInfoDao FID;
    private static SharedPreferences sp;
    private MessageReceiver Receiver;
    private static TextView tv_name;
    private View headView;
    private LinearLayout lin_right;
    private static ImageView img_play;
    private LinearLayout lin_left;
    private static ImageView img_news;
    private View rootView;
    private RelativeLayout lin_center;
    public static TextView time_start;
    public static TextView time_end;
    private static XListView mListView;
    private static LinearLayout lin_tuijian;
    private UMImage image;
    private LinearLayout lin_share;
    private static SeekBar seekBar;
    // 数据
    private static int num;// -2 播放器没有播放，-1播放器里边的数据不在list中，其它是在list中
    private static ArrayList<LanguageSearchInside> alllist = new ArrayList<LanguageSearchInside>();
    //播放url
    static String local;
    protected String SequId;
    protected String SequImage;
    protected String SequName;
    protected String SequDesc;
    private Boolean IsSequ=false;
    private int screen;
    // 其它
    private static PlayerListAdapter adapter;
    private static Dialog dialogs;
    private static Dialog wifiDialog;
    private Dialog shareDialog;
    private Dialog dialog1;
    private static Handler mHandler;
    private static int sendType;// 第一次获取数据是有分页加载的
    private int page = 1;
    private int RefreshType;// 是不是第一次请求数据
    private boolean first = true;// 第一次进入界面
    private int voice_type = 2;// 判断此时是否按下语音按钮，1，按下2，松手
    public static WtAudioPlay audioPlay;
    private LinearLayout lin_like;
    private static ImageView img_like;
    private GridView gv_more;
    private Dialog moreDialog;
    private LinearLayout lin_more;
    private static LinearLayout lin_sequ;
    private static TextView tv_like;
    private final static int TIME_UI = 10;
    private final static int VOICE_UI = 11;
    private final static int PLAY = 1;
    private final static int PAUSE = 2;
    private final static int STOP = 3;
    private final static int CONTINUE = 4;
    public static boolean isCurrentPlay;
    private static String playType;// 当前播放的媒体类型
    private static List<PlayerHistory> historyDatabaseList;
    private static LinearLayout lin_schedule;
    private static int moreType=1;//1为非电台类节目 0为电台节目 对应更多按钮弹出的不同布局而定
    private List<sharemodel> mList;
    public static int timerService; // 当前节目播放剩余时间长度


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        RefreshType = 0;
        format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        // 开启播放器服务
        context.startService(new Intent(context, TtsPlayer.class));
 //       android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        // 初始化语音配置对象
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=56275014");
        initDao();// 初始化数据库命令执行对象
        UMShareAPI.get(context);// 初始化友盟
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_play, container, false);
        mHandler = new Handler();
        setView(); // 设置界面
        setListener(); // 设置监听
        WifiDialog(); // wifi提示dialog
        shareDialog(); // 分享dialog
        return rootView;
    }

    private void setView() {
        mListView = (XListView) rootView.findViewById(R.id.listView);
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(this);
        headView = LayoutInflater.from(context).inflate(R.layout.headview_fragment_play, null);
        lin_center = (RelativeLayout) headView.findViewById(R.id.lin_center);
        lin_tuijian = (LinearLayout) headView.findViewById(R.id.lin_tuijian);
        lin_like = (LinearLayout) headView.findViewById(R.id.lin_like);
        img_like = (ImageView) headView.findViewById(R.id.img_like);
        tv_like = (TextView) headView.findViewById(R.id.tv_like);
        lin_right = (LinearLayout) headView.findViewById(R.id.lin_right);
        img_news = (ImageView) headView.findViewById(R.id.img_news);
        img_play = (ImageView) headView.findViewById(R.id.img_play);
        lin_more= (LinearLayout) headView.findViewById(R.id.lin_more);//更多
        lin_sequ= (LinearLayout) headView.findViewById(R.id.lin_sequ);//专辑
        lin_schedule=(LinearLayout) headView.findViewById(R.id.lin_schedule);//节目单
        lin_left = (LinearLayout) headView.findViewById(R.id.lin_left);
        tv_name = (TextView) headView.findViewById(R.id.tv_name);
        seekBar = (SeekBar) headView.findViewById(R.id.seekBar);
        lin_share = (LinearLayout) headView.findViewById(R.id.lin_share);
        seekBar.setEnabled(false);
        // 配合seekBar使用的标签
        time_start = (TextView) headView.findViewById(R.id.time_start);
        time_end = (TextView) headView.findViewById(R.id.time_end);
        mListView.addHeaderView(headView);
    }

    private void setListener() {
        lin_like.setOnClickListener(this);
        lin_left.setOnClickListener(this);
        lin_center.setOnClickListener(this);
        lin_right.setOnClickListener(this);
        lin_share.setOnClickListener(this);
        lin_more.setOnClickListener(this);
        lin_sequ.setOnClickListener(this);
        lin_schedule.setOnClickListener(this);


        // seekbar事件
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /**
                 * 定时服务开启当前节目播放完关闭时拖动进度条时更新定时时间
                 */
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (PlayerFragment.isCurrentPlay) {
                            Intent intent = new Intent(context, timeroffservice.class);
                            intent.setAction(BroadcastConstant.TIMER_START);
                            int time = PlayerFragment.timerService;
                            intent.putExtra("time", time);
                            context.startService(intent);
                        }
                    }
                }, 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (playType != null && playType != null && playType.equals("AUDIO")) {
                        audioPlay.setTime((long) progress);
                        mUIHandler.sendEmptyMessage(TIME_UI);
                    }
                }
            }
        });
    }

    private static LanguageSearchInside getdaolist(Context context) {
        if (dbDao == null) {
            dbDao = new SearchPlayerHistoryDao(context);
        }
        String userId = CommonUtils.getUserId(context);
        if(userId!=null){
            historyDatabaseList = dbDao.queryHistory();
        }else{
            historyDatabaseList = dbDao.queryHistoryNoUserId();
        }
        LanguageSearchInside historynews = null;
        if (historyDatabaseList != null && historyDatabaseList.size() > 0) {
            PlayerHistory historynew = historyDatabaseList.get(0);
            historynews = new LanguageSearchInside();
            historynews.setType("1");
            historynews.setContentURI(historynew.getPlayerUrI());
            historynews.setContentPersons("");
            historynews.setContentKeyWord("");
            historynews.setcTime("");
            historynews.setContentSubjectWord("");
            historynews.setContentTimes("");
            historynews.setContentName(historynew.getPlayerName());
            historynews.setContentPubTime("");
            historynews.setContentPub("");
            historynews.setContentPlay(historynew.getPlayerUrl());
            historynews.setMediaType(historynew.getPlayerMediaType());
            historynews.setContentId(historynew.getContentID());
            historynews.setContentDesc(historynew.getPlayerContentDesc());
            historynews.setContentImg(historynew.getPlayerImage());
            historynews.setPlayerAllTime(historynew.getPlayerAllTime());
            historynews.setPlayerInTime(historynew.getPlayerInTime());
            historynews.setContentShareURL(historynew.getPlayContentShareUrl());
            historynews.setContentFavorite(historynew.getContentFavorite());
            historynews.setLocalurl(historynew.getLocalurl());
            historynews.setSequId(historynew.getSequId());
            historynews.setSequName(historynew.getSequName());
            historynews.setSequDesc(historynew.getSequDesc());
            historynews.setSequImg(historynew.getSequImg());
        }
        return historynews;
    }

    private void initDao() {// 初始化数据库命令执行对象
        dbDao = new SearchPlayerHistoryDao(context);
        FID = new FileInfoDao(context);
    }

    /**
     * 把数据添加数据库
     *
     * @param languageSearchInside
     */
    private static String ContentFavorite;
    private static void adddb(LanguageSearchInside languageSearchInside) {
        String playerName = languageSearchInside.getContentName();
        String playerImage = languageSearchInside.getContentImg();
        String playerUrl = languageSearchInside.getContentPlay();
        String playerUrI = languageSearchInside.getContentURI();
        String playerMediaType = languageSearchInside.getMediaType();
        String playContentShareUrl = languageSearchInside.getContentShareURL();
        String playerAllTime = "";
        String playerInTime = "";
        String playerContentDesc = languageSearchInside.getContentDesc();
        String playerNum = "999";
        String playerZanType = "false";
        String playerFrom = "";
        String playerFromId = "";
        String playerFromUrl = "";
        String playerAddTime = Long.toString(System.currentTimeMillis());
        String bjUserId = CommonUtils.getUserId(context);
        if(languageSearchInside.getContentFavorite()!=null){
            String contentFavorite=languageSearchInside.getContentFavorite();
            if(contentFavorite!=null){
                if(contentFavorite.equals("0")||contentFavorite.equals("1")){
                    ContentFavorite=contentFavorite;
                }
            }

        }else {
            ContentFavorite = languageSearchInside.getContentFavorite();
        }
        String ContentID = languageSearchInside.getContentId();
        String localUrl = languageSearchInside.getLocalurl();
        String sequName = languageSearchInside.getSequName();
        String sequId = languageSearchInside.getSequId();
        String sequDesc = languageSearchInside.getSequDesc();
        String sequImg =languageSearchInside.getSequImg();

        PlayerHistory history = new PlayerHistory(playerName, playerImage,
                playerUrl, playerUrI, playerMediaType, playerAllTime,
                playerInTime, playerContentDesc, playerNum, playerZanType,
                playerFrom, playerFromId, playerFromUrl, playerAddTime,
                bjUserId, playContentShareUrl, ContentFavorite, ContentID, localUrl,sequName,sequId,sequDesc,sequImg);

        if (playerMediaType!= null && playerMediaType.trim().length() > 0&&playerMediaType.equals("TTS")) {
            dbDao.deleteHistoryById(ContentID);
        } else {
            dbDao.deleteHistory(playerUrl);
        }
        dbDao.addHistory(history);
    }

    private static void setItemListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                num = position - 2;
                getNetWork(num, context);
                stopCurrentTimer();
            }
        });
    }

    /**
     * 按照界面排序号进行播放
     *
     * @param number
     * 在play方法里初始化播放器对象 在musicplay方法里执行相关操作 要考虑entercenter方法
     *
     */
    protected static void play(int number) {
        if (alllist != null && alllist.get(number) != null&& alllist.get(number).getMediaType() != null) {
            playType = alllist.get(number).getMediaType();
            if (playType.equals("AUDIO") || playType.equals("RADIO")) {
                // 首先判断audioplay是否为空
                // 如果为空，新建
                // 如果不为空 判断instance是否为当前播放 如果不是stop他后面再新建当前播放器的对象
                // 以下为实现播放器的方法
                if (audioPlay == null) {
                    audioPlay = VlcPlayer.getInstance(context);
                } else {
                    // 不为空
                    if (audioPlay.mark().equals("TTS")) {
                        audioPlay.stop();
                    }
                    audioPlay = VlcPlayer.getInstance(context);
                }
                if (alllist.get(number)!= null&&alllist.get(number).getContentPlay() != null) {
                    img_play.setImageResource(R.mipmap.wt_play_play);
                    if (alllist.get(number).getContentName() != null) {
                        tv_name.setText(alllist.get(number).getContentName());
                    } else {
                        tv_name.setText("我听科技");
                    }
                    if (alllist.get(number).getContentImg() != null) {
                        String url;
                        if (alllist.get(number).getContentImg().startsWith("http")) {
                            url = alllist.get(number).getContentImg();
                        } else {
                            url = GlobalConfig.imageurl+ alllist.get(number).getContentImg();
                        }
                        Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
                    } else {
                        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                        img_news.setImageBitmap(bmp);
                    }
                    for (int i = 0; i < alllist.size(); i++) {
                        alllist.get(i).setType("1");
                    }
                    alllist.get(number).setType("2");
                    adapter.notifyDataSetChanged();
                    if (alllist.get(number).getLocalurl() != null) {
                        musicPlay("file:///"+ alllist.get(number).getLocalurl());
                        ToastUtils.show_always(context, "正在播放本地内容");
                    } else {
                        musicPlay(alllist.get(number).getContentPlay());
                    }
                    GlobalConfig.playerobject = alllist.get(number);
                    resetHeadView();
                    num = number;
                } else {
                    ToastUtils.show_short(context, "暂不支持播放");
                }
            } else if (playType.equals("TTS")) {
                if (alllist.get(number)!= null && alllist.get(number).getContentURI() != null &&alllist.get(number).getContentURI().trim().length() > 0) {
                    if (audioPlay == null) {
                        audioPlay = TtsPlayer.getInstance(context);
                    } else {
                        // 不为空
                        if (audioPlay.mark().equals("VLC")) {
                            audioPlay.stop();
                        }
                        audioPlay = TtsPlayer.getInstance(context);
                    }
                    img_play.setImageResource(R.mipmap.wt_play_play);
                    if(alllist.get(number)!=null){
                        if (alllist.get(number).getContentName() != null) {
                            tv_name.setText(alllist.get(number).getContentName());
                        } else {
                            tv_name.setText("我听科技");
                        }
                        if (alllist.get(number).getContentImg() != null) {
                            String url;
                            if (alllist.get(number).getContentImg().startsWith("http")) {
                                url = alllist.get(number).getContentImg();
                            } else {
                                url = GlobalConfig.imageurl + alllist.get(number).getContentImg();
                            }
                            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
                        } else {
                            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                            img_news.setImageBitmap(bmp);
                        }
                        for (int i = 0; i < alllist.size(); i++) {
                            alllist.get(i).setType("1");
                        }
                        alllist.get(number).setType("2");
                        adapter.notifyDataSetChanged();
                        adddb(alllist.get(number));
                        musicPlay(alllist.get(number).getContentURI());
                        GlobalConfig.playerobject = alllist.get(number);
                        resetHeadView();
                        num = number;
                    }else{
                        Log.e("","TTS播放数据异常");
                    }

                } else {
                    getContentNews(alllist.get(number).getContentId(), number);
                }
            }
        }
    }

    private static void getNetWork(int number, Context context) {
        String wifiset = sp.getString(StringConstant.WIFISET, "true"); // 是否开启网络流量提醒
        String wifishow = sp.getString(StringConstant.WIFISHOW, "true");// 是否网络弹出框提醒
        if (wifishow != null && !wifishow.trim().equals("") && wifishow.equals("true")) {
            if (wifiset != null && !wifiset.trim().equals("") && wifiset.equals("true")) {
                // 开启网络播放数据连接提醒
                CommonHelper.checkNetworkStatus(context);// 网络设置获取
                GlobalConfig.playerobject=alllist.get(number);
                adddb(alllist.get(number));
                resetHeadView();
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE == 1) {
                        play(number);
                        num = number;
                    } else {
                        num = number;
                        wifiDialog.show();
                    }
                } else { // 07/28 没有网络情况下还有可能播放本地文件
                    if (alllist.get(number).getLocalurl() != null) {
                        play(number);
                        num = number;
                    } else {
                        ToastUtils.show_always(context, "无网络连接");
                    }
                }
            } else {
                // 未开启网络播放数据连接提醒
                num = number;
                play(number);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_share:
                    shareDialog.show();
                break;
            case R.id.lin_like:
                if (GlobalConfig.playerobject.getContentFavorite() != null && !GlobalConfig.playerobject.getContentFavorite().equals("")) {
                    sendFavorite();
                } else {
                    ToastUtils.show_long(context, "本节目信息获取有误，暂时不支持喜欢");
                }
                break;
            case R.id.lin_left:
                playLast();
                break;
            case R.id.lin_center:
                enterCenter();
                stopCurrentTimer();
                break;
            case R.id.lin_schedule:
                Intent intent1 =new Intent(context,ScheduleActivity.class);
                if(GlobalConfig.playerobject.getContentName()!=null){
                    intent1.putExtra("ContentName",GlobalConfig.playerobject.getContentName());
                }
                if(GlobalConfig.playerobject.getContentId()!=null){
                    intent1.putExtra("ContentId",GlobalConfig.playerobject.getContentId());
                }
                startActivity(intent1);
                break;
            case R.id.lin_right:
                if (alllist != null && alllist.size() > 0) {
                    if (num + 1 < alllist.size()) {
                        num = num + 1;
                        getNetWork(num, context);
                    } else {
                        num = 0;
                        getNetWork(num, context);
                    }
                    stopCurrentTimer();
                }
                break;
            case R.id.lin_more:
                if(shareDialog.isShowing()){
                    shareDialog.dismiss();
                }
                moreDialog();
                moreDialog.show();
                break;
            case R.id.lin_sequ:
                if(GlobalConfig.playerobject!=null){
                  try {
                      if(GlobalConfig.playerobject.getSequId()!=null){
                          if(GlobalConfig.playerobject.getSequId()!=null){
                              SequId = GlobalConfig.playerobject.getSequId();
                              SequDesc = GlobalConfig.playerobject.getSequDesc();
                              SequImage = GlobalConfig.playerobject.getSequImg();
                              SequName = GlobalConfig.playerobject.getSequName();
                              IsSequ=true;
                          }else{
                              IsSequ=false;
                          }
                      } else{
                      if(GlobalConfig.playerobject.getSeqInfo()!=null){
                          if(GlobalConfig.playerobject.getSeqInfo().getContentId()!=null){
                          SequId = GlobalConfig.playerobject.getSeqInfo().getContentId();
                          SequDesc = GlobalConfig.playerobject.getSeqInfo().getContentDesc();
                          SequImage = GlobalConfig.playerobject.getSeqInfo().getContentImg();
                          SequName = GlobalConfig.playerobject.getSeqInfo().getContentName();
                          IsSequ=true;
                          }else{
                              IsSequ=false;
                          }
                      }else{
                          IsSequ=false;
                      }
                      }
                  }catch (Exception e){
                      e.printStackTrace();
                  }
                }
                if(IsSequ){
                    Intent intent = new Intent(context, AlbumActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "player");
                    bundle.putString("contentName",SequName);
                    bundle.putString("contentDesc",SequDesc);
                    bundle.putString("contentId",SequId);
                    bundle.putString("contentImg",SequImage);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    ToastUtils.show_always(context,"此节目目前没有所属专辑");
                }
                break;
        }
    }

    public  static void playLast() {
        if (num - 1 >= 0) {
            num = num - 1;
            getNetWork(num, context);//当播放下一首或者上一首时需要将此播放内容放到数据库当中
            stopCurrentTimer();
        } else {
            ToastUtils.show_always(context, "已经是第一条数据了");
        }

    }

    private void getLuKuangTTS() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);

        VolleyRequest.RequestPost(GlobalConfig.getLKTTS, jsonObject, new VolleyCallback() {
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
                try {
                    Message = result.getString("ContentURI");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (Message != null && Message.trim().length() > 0) {
                    img_news.setImageResource(R.mipmap.wt_image_lktts);
                    musicPlay(Message);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
            }
        });
    }

    /**
     * 开启定时服务中的当前播放完后关闭的关闭服务方法 点击暂停播放、下一首、上一首以及播放路况信息时都将自动关闭此服务
     */
    private static void stopCurrentTimer() {
        if (PlayerFragment.isCurrentPlay) {
            Intent intent = new Intent(context, timeroffservice.class);
            intent.setAction(BroadcastConstant.TIMER_STOP);
            context.startService(intent);
            PlayerFragment.isCurrentPlay = false;
        }
    }

    public static void enterCenter() {
        if (GlobalConfig.playerobject != null && GlobalConfig.playerobject.getMediaType() != null) {
            playType = GlobalConfig.playerobject.getMediaType();
            if (playType.equals("AUDIO") ||playType.equals("RADIO")) {
                // 首先判断audioplay是否为空
                // 如果为空，新建
                // 如果不为空 判断instance是否为当前播放 如果不是stop他后面再新建当前播放器的对象
                // 以下为实现播放器的方法
                if (audioPlay == null) {
                    audioPlay = VlcPlayer.getInstance(context);
                } else {
                    // 不为空
                    if (audioPlay.mark().equals("TTS")) {
                        audioPlay.stop();
                    }
                    audioPlay = VlcPlayer.getInstance(context);
                }
                if (GlobalConfig.playerobject.getContentPlay() != null) {
                    if (GlobalConfig.playerobject != null) {
                        tv_name.setText(GlobalConfig.playerobject.getContentName());
                    } else {
                        tv_name.setText("我听科技");
                    }
                    if (GlobalConfig.playerobject.getContentImg() != null) {
                        String url;
                        if (GlobalConfig.playerobject.getContentImg().startsWith("http")) {
                            url = GlobalConfig.playerobject.getContentImg();
                        } else {
                            url = GlobalConfig.imageurl+ GlobalConfig.playerobject.getContentImg();
                        }
                        Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
                    } else {
                        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                        img_news.setImageBitmap(bmp);
                    }
                    for (int i = 0; i < alllist.size(); i++) {
                        alllist.get(i).setType("1");
                    }
                    GlobalConfig.playerobject.setType("2");
                    adapter.notifyDataSetChanged();
                    if (GlobalConfig.playerobject.getLocalurl() != null) {
                        musicPlay("file:///"+ GlobalConfig.playerobject.getLocalurl());
                        ToastUtils.show_always(context, "正在播放本地内容");
                    } else {
                        musicPlay(GlobalConfig.playerobject.getContentPlay());
                    }
                    resetHeadView();
                } else {
                    ToastUtils.show_short(context, "暂不支持播放");
                }
            } else if (playType.equals("TTS")) {
                if (GlobalConfig.playerobject.getContentURI() != null
                        && GlobalConfig.playerobject.getContentURI().trim().length() > 0) {
                    if (audioPlay == null) {
                        audioPlay = TtsPlayer.getInstance(context);
                    } else {
                        // 不为空
                        if (audioPlay.mark().equals("VLC")) {
                            audioPlay.stop();
                        }
                        audioPlay = TtsPlayer.getInstance(context);
                    }

                    if (GlobalConfig.playerobject.getContentName() != null) {
                        tv_name.setText(GlobalConfig.playerobject.getContentName());
                    } else {
                        tv_name.setText("我听科技");
                    }
                    if (GlobalConfig.playerobject.getContentImg() != null) {
                        String url;
                        if (GlobalConfig.playerobject.getContentImg().startsWith("http")) {
                            url = GlobalConfig.playerobject.getContentImg();
                        } else {
                            url = GlobalConfig.imageurl+ GlobalConfig.playerobject.getContentImg();
                        }
                        Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
                    } else {
                        Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                        img_news.setImageBitmap(bmp);
                    }
                    for (int i = 0; i < alllist.size(); i++) {
                        alllist.get(i).setType("1");
                    }
                    GlobalConfig.playerobject.setType("2");
                    adapter.notifyDataSetChanged();
                    musicPlay(GlobalConfig.playerobject.getContentURI());
                    resetHeadView();
                }
            }
        } else {
            ToastUtils.show_always(context, "当前播放对象为空");
        }
    }

    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sendType == 1) {
                    mListView.setPullLoadEnable(false);
                    RefreshType = 1;
                    page = 1;
                    firstSend();
                }
            }
        }, 1000);
    }

    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sendType == 1) {
                    RefreshType = 2;
                    firstSend();
                }
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        sp = context.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
        if (first == true) {
            // 从播放历史界面跳转到该界面
            String enter = sp.getString(StringConstant.PLAYHISTORYENTER, "false");
            String news = sp.getString(StringConstant.PLAYHISTORYENTERNEWS, "");
            if (enter.equals("true")) {
                SendTextRequest(news, context);
                Editor et = sp.edit();
                et.putString(StringConstant.PLAYHISTORYENTER, "false");
                et.commit();
            } else {
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialogs = DialogUtils.Dialogph(context, "通讯中");
                    firstSend();// 搜索第一次数据
                } else {
                    ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                }
            }
            first = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Receiver != null) { // 注销广播
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
    }

    /**
     * 广播接收器
     */
    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
             if (action.equals(BroadcastConstant.PLAYERVOICE)) {
                String str = intent.getStringExtra("VoiceContent");
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    if(!str.trim().equals("")){
                        searchByVoice(str);
                        Handler handler =new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                            }
                        }, 2000);
                    }
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
            }

        }
    }

    /**
     * 下一首
     */
    public static void playNext() {
        int a=alllist.size();
        if (num + 1 < alllist.size()) {
            // 此时自动播放下一首
            num = num + 1;
            getNetWork(num, context);
            stopCurrentTimer();
        } else {
            // 全部播放完毕了
            num = 0;
            getNetWork(num, context);
            stopCurrentTimer();
        }
    }


    static Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_UI: // 更新进度及时间
                    if (GlobalConfig.playerobject != null&& GlobalConfig.playerobject.getMediaType() != null
                            && GlobalConfig.playerobject.getMediaType().trim().length() > 0
                            && GlobalConfig.playerobject.getMediaType().equals("AUDIO")) {
                        long currPosition = audioPlay.getTime();
                        long duration = audioPlay.getTotalTime();
                        updateTextViewWithTimeFormat(time_start,(int) (currPosition / 1000));
                        updateTextViewWithTimeFormat(time_end,(int) (duration / 1000));
                        seekBar.setMax((int) duration);
                        timerService = (int) (duration - currPosition);
                        if (audioPlay.isPlaying()) {
                            seekBar.setProgress((int) currPosition);
                        }
                    } else if (GlobalConfig.playerobject != null
                            && GlobalConfig.playerobject.getMediaType() != null
                            && GlobalConfig.playerobject.getMediaType().trim().length() > 0
                            && GlobalConfig.playerobject.getMediaType().equals("RADIO")) {
                        int _currPosition = TimeUtils.getTime(System.currentTimeMillis());
                        int _duration = 24 * 60 * 60;
                        updateTextViewWithTimeFormat(time_start, _currPosition);
                        updateTextViewWithTimeFormat(time_end, _duration);
                        seekBar.setMax(_duration);
                        seekBar.setProgress(_currPosition);

                    } else if (GlobalConfig.playerobject != null
                            && GlobalConfig.playerobject.getMediaType() != null
                            && GlobalConfig.playerobject.getMediaType().trim().length() > 0
                            && GlobalConfig.playerobject.getMediaType().equals("TTS")) {

                        int _currPosition = TimeUtils.getTime(System.currentTimeMillis());
                        int _duration = 24 * 60 * 60;
                        updateTextViewWithTimeFormat(time_start, _currPosition);
                        updateTextViewWithTimeFormat(time_end, _duration);
                        seekBar.setMax(_duration);
                        seekBar.setProgress(_currPosition);
                    }
                    mUIHandler.sendEmptyMessageDelayed(TIME_UI, 1000);
                    break;
                case PLAY:
                    audioPlay.play(local);
                    break;
                case PAUSE:
                    audioPlay.pause();
                    break;
                case CONTINUE:
                    audioPlay.continuePlay();
                    break;
                case STOP:
                    audioPlay.stop();
                    break;
            }
        }
    };

    private static void updateTextViewWithTimeFormat(TextView view, long second) {
        int hh = (int) (second / 3600);
        int mm = (int) (second % 3600 / 60);
        int ss = (int) (second % 60);
        String strTemp = null;
        strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        view.setText(strTemp);
    }

    /**
     * 播放函数
     */
    private static void musicPlay(String s) {
        if (local == null) {
            local = s;
            mUIHandler.sendEmptyMessage(PLAY);
            img_play.setImageResource(R.mipmap.wt_play_play);
            setPlayingType();
        } else {
            // 不等于空
            if (local.equals(s)) {
                // 里面可以根据播放类型判断继续播放或者停止
                if (audioPlay.isPlaying()) {
                    // 播放状态，对应暂停方法，播放图
                    audioPlay.pause();
                    if (playType.equals("AUDIO")) {
                        mUIHandler.removeMessages(TIME_UI);
                    }
                    img_play.setImageResource(R.mipmap.wt_play_stop);
                    setPauseType();
                } else {
                    // 暂停状态，对应播放方法，暂停图
                    audioPlay.continuePlay();
                    img_play.setImageResource(R.mipmap.wt_play_play);
                    setPlayingType();
                }
            } else {
                local = s;
                mUIHandler.sendEmptyMessage(PLAY);
                img_play.setImageResource(R.mipmap.wt_play_play);
                setPlayingType();
            }
        }
        if (playType != null && playType.trim().length() > 0 && playType.equals("AUDIO")) {
            seekBar.setEnabled(true);
            mUIHandler.sendEmptyMessage(TIME_UI);
        } else {
            seekBar.setEnabled(false);
            mUIHandler.sendEmptyMessage(TIME_UI);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(context).onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 语音搜索
     */
    private void searchByVoice(String str) {
        sendType = 2;
        // 发送数据
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("SearchStr", str);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
            jsonObject.put("PageType", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestTextVoicePost(GlobalConfig.searchvoiceUrl, jsonObject, new VolleyCallback() {
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
                    MainList = result.getString("ResultList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType.equals("1001")) {
                    try {
                        LanguageSearch lists = new Gson().fromJson(MainList, new TypeToken<LanguageSearch>() {}.getType());
                        List<LanguageSearchInside> list = lists.getList();
                        list.get(0).getContentDesc();
                        if (list != null && list.size() != 0) {
                            num = 0;
                            alllist.clear();
                            alllist.addAll(list);
                            GlobalConfig.playerobject=alllist.get(num);
                            lin_tuijian.setVisibility(View.VISIBLE);
                            adapter = new PlayerListAdapter(context, alllist);
                            mListView.setAdapter(adapter);
                            setItemListener();
                            getNetWork(0, context);
                            mListView.setPullRefreshEnable(false);
                            mListView.setPullLoadEnable(false);
                            mListView.stopRefresh();
                            mListView.stopLoadMore();
                            mListView.setRefreshTime(new Date().toLocaleString());
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        Log.e("语音搜索异常信息", e.toString());
                    }

                } else if (ReturnType.equals("1011")) {
                    ToastUtils.show_short(context, "没有查询内容");
                } else {
                    ToastUtils.show_short(context, "没有新的数据");
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (voice_type == 2) {
                            mUIHandler.sendEmptyMessage(VOICE_UI);
                        }
                    }
                }, 5000);
            }

            @Override
            protected void requestError(VolleyError error) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (voice_type == 2) {
                            mUIHandler.sendEmptyMessage(VOICE_UI);
                        }
                    }
                }, 5000);
            }
        });
    }

    /**
     * wifi对话框
     */
    private void WifiDialog() {
        final View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_set, null);
        TextView tv_over = (TextView) dialog1.findViewById(R.id.tv_cancle);
        TextView tv_first = (TextView) dialog1.findViewById(R.id.tv_first);
        TextView tv_all = (TextView) dialog1.findViewById(R.id.tv_all);
        wifiDialog = new Dialog(context, R.style.MyDialog);
        wifiDialog.setContentView(dialog1);
        wifiDialog.setCanceledOnTouchOutside(true);
        wifiDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        tv_over.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiDialog.dismiss();
            }
        });
        tv_first.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play(num);
                wifiDialog.dismiss();
            }
        });
        tv_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play(num);
                Editor et = sp.edit();
                et.putString(StringConstant.WIFISHOW, "false");
                et.commit();
                wifiDialog.dismiss();
            }
        });
    }

    /**
     * 第一次进入该界面时候的数据
     */
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
            private String ReturnType;
            private String MainList;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
                Log.e("第一次返回值",""+result.toString());
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType.equals("1001")) {
                    page++;
                    try {
                        String List = result.getString("ResultList");
                        JSONTokener jsonParser = new JSONTokener(List);
                        JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                        MainList = arg1.getString("List");
                        ArrayList<LanguageSearchInside> list = new Gson().fromJson(
                                MainList, new TypeToken<List<LanguageSearchInside>>() {}.getType());
//                        String s=list.get(0).getContentDesc();
                        if (RefreshType == 0) {
                            // 得到数据库里边的第一条数据
                            LanguageSearchInside fList = getdaolist(context);
                            // 第一次进入该界面获取数据
                            if (list != null && list.size() > 0) {
                                // 此时有返回数据
                                if (fList != null) {
                                    // 此时数据库里边的数据为空
                                    num = -1;
                                    setData(fList, list);
                                } else {
                                    // 此时数据库里边的数据为空
                                    if (list.get(0) != null) {
                                        num = 0;
                                        setDataForNoList(list);
                                    } else {
                                        num = -2;
                                    }
                                }
                            } else {
                                if (fList != null) {
                                    list.add(fList);
                                    num = -1;
                                    setData(fList, list);
                                } else {
                                    // 此时没有任何数据
                                    num = -2;
                                    mListView.setPullRefreshEnable(true);
                                    mListView.setPullLoadEnable(false);
                                    mListView.stopRefresh();
                                }
                            }
                        } else if (RefreshType == 1) {
                            // 下拉刷新--------暂未使用，注意：不要删除该段代码
                            alllist.clear();
                            alllist.addAll(list);
                            if (GlobalConfig.playerobject != null && alllist != null) {
                                for (int i = 0; i < alllist.size(); i++) {
                                    if (alllist.get(i).getContentPlay().equals(GlobalConfig.playerobject.getContentPlay())) {
                                        alllist.get(i).setType("0");
                                        num = i;
                                    }
                                }
                            }
                            lin_tuijian.setVisibility(View.VISIBLE);
                            adapter = new PlayerListAdapter(context, alllist);
                            mListView.setAdapter(adapter);
                            setItemListener();
                            mListView.setPullRefreshEnable(false);
                            mListView.setPullLoadEnable(true);
                            mListView.stopRefresh();
                        } else {
                            // 加载更多
                            mListView.stopLoadMore();
                            alllist.addAll(list);
                            adapter.notifyDataSetChanged();
                            setItemListener();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (dialogs != null) {
                            dialogs.dismiss();
                        }
                        alllist.clear();
                        lin_tuijian.setVisibility(View.GONE);
                        adapter = new PlayerListAdapter(context, alllist);
                        mListView.setAdapter(adapter);
                        mListView.setPullRefreshEnable(true);
                        mListView.setPullLoadEnable(false);
                        mListView.stopRefresh();
                        mListView.stopLoadMore();
                    }
                } else {
                    if (dialogs != null) {
                        dialogs.dismiss();
                    }
                    alllist.clear();
                    lin_tuijian.setVisibility(View.GONE);
                    adapter = new PlayerListAdapter(context, alllist);
                    mListView.setAdapter(adapter);
                    mListView.setPullRefreshEnable(true);
                    mListView.setPullLoadEnable(false);
                    mListView.stopRefresh();
                    mListView.stopLoadMore();
                }
                resetHeadView();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
                alllist.clear();
                lin_tuijian.setVisibility(View.VISIBLE);
                adapter = new PlayerListAdapter(context, alllist);
                mListView.setAdapter(adapter);
                mListView.setPullLoadEnable(false);
                mListView.stopRefresh();
                mListView.stopLoadMore();
                mListView.setRefreshTime(new Date().toLocaleString());
            }
        });
    }

    protected void setData(LanguageSearchInside flist, ArrayList<LanguageSearchInside> list) {
        // 如果数据库里边的数据不是空的，在头部设置该数据
        GlobalConfig.playerobject = flist;
        resetHeadView();
        if (flist.getContentName() != null) {
            tv_name.setText(flist.getContentName());
        } else {
            tv_name.setText("未知数据");
        }
        time_start.setText("00:00:00");
        time_end.setText("00:00:00");
        if (flist.getContentImg() != null) {
            String url;
            if (flist.getContentImg().startsWith("http")) {
                url = flist.getContentImg();
            } else {
                url = GlobalConfig.imageurl + flist.getContentImg();
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
        } else {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            img_news.setImageBitmap(bmp);
        }
        alllist.clear();
        alllist.addAll(list);

        if (GlobalConfig.playerobject != null && alllist != null) {
            for (int i = 0; i < alllist.size(); i++) {
                String s = alllist.get(i).getContentPlay();
                if (s != null) {
                    if (s.equals(GlobalConfig.playerobject.getContentPlay())) {
                        alllist.get(i).setType("0");
                        num = i;
                    }
                }
            }
        }
        lin_tuijian.setVisibility(View.VISIBLE);
        adapter = new PlayerListAdapter(context, alllist);
        mListView.setAdapter(adapter);
        setItemListener();
        mListView.setPullRefreshEnable(false);
        mListView.setPullLoadEnable(true);
        mListView.stopRefresh();
    }

    /**
     * 设置无数据时界面样式
     */
    protected void setDataForNoList(ArrayList<LanguageSearchInside> list) {
        GlobalConfig.playerobject = list.get(0);
        resetHeadView();
        if (list.get(0).getContentName() != null && list.get(0).getContentName().trim().length() > 0) {
            tv_name.setText(list.get(0).getContentName());
        } else {
            tv_name.setText("未知数据");
        }
        time_start.setText("00:00:00");
        time_end.setText("00:00:00");
        if (list.get(0).getContentImg() != null) {
            String url;
            if (list.get(0).getContentImg().startsWith("http")) {
                url = list.get(0).getContentImg();
            } else {
                url = GlobalConfig.imageurl + list.get(0).getContentImg();
            }
            Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);
        } else {
            Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
            img_news.setImageBitmap(bmp);
        }
        alllist.clear();
        alllist.addAll(list);
        alllist.get(0).setType("0");
        num = 0;
        lin_tuijian.setVisibility(View.VISIBLE);
        adapter = new PlayerListAdapter(context, alllist);
        mListView.setAdapter(adapter);
        setItemListener();
        mListView.setPullRefreshEnable(false);
        mListView.setPullLoadEnable(true);
        mListView.stopRefresh();
    }

    /**
     * 对headview头部进行控制的类
     */
    private static void resetHeadView() {
        if (GlobalConfig.playerobject != null) {
            if (GlobalConfig.playerobject.getMediaType().equals("RADIO")) {
                // 不支持分享
                if(moreType==1){
                moreType=0;
                }
                lin_sequ.setVisibility(View.GONE);
                lin_schedule.setVisibility(View.VISIBLE);
                return;
                // 设置灰色界面
            } else {
                // 支持分享
                // 设置回界面
                if(moreType==0){
                    moreType=1;
                }
                lin_schedule.setVisibility(View.GONE);
                lin_sequ.setVisibility(View.VISIBLE);
            }

            if (GlobalConfig.playerobject.getContentFavorite() != null && !GlobalConfig.playerobject.equals("")) {

                if(GlobalConfig.playerobject.getContentFavorite().equals("0")){
                    tv_like.setText("喜欢");
                    img_like.setImageResource(R.mipmap.wt_dianzan_nomal);
                }else{
                    tv_like.setText("已喜欢");
                    img_like.setImageResource(R.mipmap.wt_dianzan_select);
                }
            } else {
                tv_like.setText("喜欢");
                img_like.setImageResource(R.mipmap.wt_dianzan_nomal);

            }
        } else {

        }
    }

    /**
     * 文字请求
     */
    public static void SendTextRequest(String contentName, final Context mContext) {

        final LanguageSearchInside fList = getdaolist(mContext);// 得到数据库里边的第一条数据
        if (fList != null) {
            // 如果数据库里边的数据不是空的，在头部设置该数据
            if (alllist != null && alllist.size() > 0) {
                alllist.clear();
                alllist.add(fList);
            } else {
                alllist = new ArrayList<LanguageSearchInside>();
                alllist.add(fList);
            }
            num = 0;
            if (fList.getContentName() != null) {
                tv_name.setText(fList.getContentName());
            } else {
                tv_name.setText("我听科技");
            }
            adapter = new PlayerListAdapter(context, alllist);
            mListView.setAdapter(adapter);
            setItemListener();
            stopCurrentTimer();
            getNetWork(0, context);
        }
        // 发送数据
        sendType = 2;
        JSONObject jsonObject =VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("SearchStr", contentName);
            jsonObject.put("PCDType",GlobalConfig.PCDType);
            jsonObject.put("PageType", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestTextVoicePost(GlobalConfig.getSearchByText, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String MainList;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    MainList = result.getString("ResultList");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType.equals("1001")) {
                    try {
                        LanguageSearch lists = new Gson().fromJson(MainList, new TypeToken<LanguageSearch>() {}.getType());
                        List<LanguageSearchInside> list = lists.getList();
                        if (list != null && list.size() != 0) {
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getContentPlay() != null
                                        && !list.get(i).getContentPlay().equals("null")
                                        && !list.get(i).getContentPlay().equals("")
                                        && list.get(i).getContentPlay().equals(fList.getContentPlay())) {
                                    list.remove(i);
                                }
                            }
                            num = 0;
                            alllist.clear();
                            alllist.add(fList);
                            alllist.addAll(list);
                            GlobalConfig.playerobject=alllist.get(num);
                            lin_tuijian.setVisibility(View.VISIBLE);
                            adapter = new PlayerListAdapter(context, alllist);
                            mListView.setAdapter(adapter);
                            setItemListener();
                            mListView.setPullRefreshEnable(false);
                            mListView.setPullLoadEnable(false);
                            mListView.stopRefresh();
                            mListView.stopLoadMore();
                            mListView.setRefreshTime(new Date().toLocaleString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ReturnType.equals("1011")) {
                    ToastUtils.show_short(context, "没有查询内容");
                } else {
                    ToastUtils.show_short(context, "没有新的数据");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
            }
        });
    }

    /**
     * 请求TTS内容
     */
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
                if (ReturnType.equals("1001")) {
                    try {
                        LanguageSearchInside lists = new Gson().fromJson(MainList,
                                new TypeToken<LanguageSearchInside>() {}.getType());
                        String ContentURI = lists.getContentURI();
                        if (ContentURI != null && ContentURI.trim().length() > 0) {
                            img_play.setImageResource(R.mipmap.wt_play_play);
                            if (alllist.get(number).getContentName() != null) {
                                tv_name.setText(alllist.get(number).getContentName());
                            } else {
                                tv_name.setText("我听科技");
                            }
                            if (alllist.get(number).getContentImg() != null) {
                                String url;
                                if (alllist.get(number).getContentImg().startsWith("http")) {
                                    url = alllist.get(number).getContentImg();
                                } else {
                                    url = GlobalConfig.imageurl + alllist.get(number).getContentImg();
                                }
                                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_news);

                            } else {
                                Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                                img_news.setImageBitmap(bmp);
                            }
                            for (int i = 0; i < alllist.size(); i++) {
                                alllist.get(i).setType("1");
                            }
                            alllist.get(number).setType("2");
                            adapter.notifyDataSetChanged();
                            musicPlay(ContentURI);
                            GlobalConfig.playerobject = alllist.get(number);
                            resetHeadView();// 页面的对象改变，根据对象重新设置属性
                            num = number;
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void requestError(VolleyError error) {

            }
        });
    }

    /**
     * 点赞
     */
    private static void sendFavorite() {
        dialogs = DialogUtils.Dialogph(context, "通讯中");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", GlobalConfig.playerobject.getMediaType());
            jsonObject.put("ContentId", GlobalConfig.playerobject.getContentId());
            jsonObject.put("PCDType",GlobalConfig.PCDType);
            if (GlobalConfig.playerobject.getContentFavorite().equals("0")) {
                jsonObject.put("Flag", 1);
            } else {
                jsonObject.put("Flag", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.clickFavoriteUrl, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String Message;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Message = result.getString("Message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 根据返回值来对程序进行解析
                if (ReturnType != null) {
                    if (ReturnType.equals("1001")) {
                        if (GlobalConfig.playerobject.getContentFavorite().equals("0")) {
                            tv_like.setText("已喜欢");
                            img_like.setImageResource(R.mipmap.wt_dianzan_select);
                            GlobalConfig.playerobject.setContentFavorite("1");
                            for (int i = 0; i < alllist.size(); i++) {
                                if(alllist.get(i).getContentPlay()!=null&&GlobalConfig.playerobject.getContentPlay()!=null){
                                    if (alllist.get(i).getContentPlay()!=null&&alllist.get(i).getContentPlay().equals(GlobalConfig.playerobject.getContentPlay())) {
                                    GlobalConfig.playerobject.setContentFavorite("1");
                                    dbDao.updateFavorite(GlobalConfig.playerobject.getContentPlay(),"1");
                                }
                                }else{
                                    Log.e("点赞处理异常",""+alllist.get(i).getContentPlay()+"Global"+GlobalConfig.playerobject.getContentPlay());
                                }
                            }
                        } else {
                            tv_like.setText("喜欢");
                            img_like.setImageResource(R.mipmap.wt_dianzan_nomal);
                            GlobalConfig.playerobject.setContentFavorite("0");
                            for (int i = 0; i < alllist.size(); i++) {
                                if(alllist.get(i).getContentPlay()!=null&&GlobalConfig.playerobject.getContentPlay()!=null){
                                    if (alllist.get(i).getContentPlay()!=null&&alllist.get(i).getContentPlay().equals(GlobalConfig.playerobject.getContentPlay())) {
                                        GlobalConfig.playerobject.setContentFavorite("0");
                                        dbDao.updateFavorite(GlobalConfig.playerobject.getContentPlay(),"0");
                                    }
                                }else{
                                    Log.e("点赞处理异常",""+alllist.get(i).getContentPlay()+"Global"+GlobalConfig.playerobject.getContentPlay());
                                }
                            }
                        }
                    } else if (ReturnType.equals("0000")) {
                        ToastUtils.show_always(context, "无法获取相关的参数");
                    } else if (ReturnType.equals("1002")) {
                        ToastUtils.show_always(context, "无法获得内容类别");
                    } else if (ReturnType.equals("1003")) {
                        ToastUtils.show_always(context, "无法获得内容Id");
                    } else if (ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "所指定的节目不存在");
                    } else if (ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "已经喜欢了此内容");
                    } else if (ReturnType.equals("1006")) {
                        ToastUtils.show_always(context, "还未喜欢此内容");
                    } else if (ReturnType.equals("T")) {
                        ToastUtils.show_always(context, "获取列表异常");
                    } else {
                        ToastUtils.show_always(context, Message + "");
                    }
                } else {
                    ToastUtils.show_always(context, "ReturnType==null");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialogs != null) {
                    dialogs.dismiss();
                }
            }
        });
    }

    /**
     *更多
     */
    private void moreDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_player_more, null);
        gv_more=(GridView)dialog.findViewById(R.id.gv_more);
        TextView tv_cancle = (TextView) dialog.findViewById(R.id.tv_cancle);
        moreDialog = new Dialog(context, R.style.MyDialog);
        // 从底部上升到一个位置
        moreDialog.setContentView(dialog);
        Window window = moreDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screen = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = screen;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        moreDialog.setCanceledOnTouchOutside(true);
        moreDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        if(moreType==0){
            //电台
         mList = PlayermoreUtil.getPlayMoreList("RADIO");
        }else{
            //非电台类内容
            mList = PlayermoreUtil.getPlayMoreList("AUDIO");
        }
        gvMoreAdapter shareadapter = new gvMoreAdapter(context, mList);
        gv_more.setAdapter(shareadapter);
        gv_more.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv_more.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                callMore(position);//呼出更多
                moreDialog.dismiss();
            }
        });
        tv_cancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreDialog.isShowing()) {
                    moreDialog.dismiss();
                }
            }
        });
    }

    /**
     *更多回调
     */
    private void callMore(int position) {
        if(moreType==0){
            //电台调用
            switch (position){
                case 0://定时关闭
                    startActivity(new Intent(context,TimerPowerOffActivity.class));
                    break;
                case 1://播放历史
                    startActivity(new Intent(context,PlayHistoryActivity.class));
                    break;
                case 2://我喜欢的
                    startActivity(new Intent(context,FavoriteActivity.class));
                    break;
                case 3://预定节目单
                    Intent intent =new Intent(context,ScheduleActivity.class);
                    if(GlobalConfig.playerobject.getContentName()!=null){
                        intent.putExtra("ContentName",GlobalConfig.playerobject.getContentName());
                    }
                    if(GlobalConfig.playerobject.getContentId()!=null){
                        intent.putExtra("ContentId",GlobalConfig.playerobject.getContentId());
                    }
                    startActivity(intent);
                    break;
                case 4://实时路况
                    if (audioPlay == null) {
                        audioPlay = TtsPlayer.getInstance(context);
                    } else {
                        // 不为空
                        if (audioPlay.mark().equals("VLC")) {
                            audioPlay.pause();
                        }
                        audioPlay = TtsPlayer.getInstance(context);
                    }
                    ToastUtils.show_always(context, "点击了路况TTS按钮");
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialogs = DialogUtils.Dialogph(context, "通讯中");
                        getLuKuangTTS();// 获取路况数据播报
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                    break;
            }

        }else{
            //节目类调用
            switch(position){
                case 0://定时关闭
                    startActivity(new Intent(context,TimerPowerOffActivity.class));
                    break;
                case 1://下载
                    if (GlobalConfig.playerobject != null) {
                        if (GlobalConfig.playerobject.getMediaType().equals("AUDIO")) {
					/* ToastUtil.show_always(context, "已经将该节目添加到下载列表"); */
                            // 此处执行将当前播放任务加到数据库的操作
                            LanguageSearchInside datals = GlobalConfig.playerobject;
                            if (datals.getLocalurl() != null) {
                                ToastUtils.show_always(context, "此节目已经保存到本地，请到已下载界面查看");
                                return;
                            }
                            // 对数据进行转换
                            List<ContentInfo> dataList = new ArrayList<>();
                            ContentInfo mcontent = new ContentInfo();
                            mcontent.setAuthor(datals.getContentPersons());
                            mcontent.setContentPlay(datals.getContentPlay());
                            mcontent.setContentImg(datals.getContentImg());
                            mcontent.setContentName(datals.getContentName());
                            mcontent.setUserid(CommonUtils.getUserId(context));
                            mcontent.setDownloadtype("0");

                            if (datals.getSeqInfo() == null
                                    || datals.getSeqInfo().getContentName() == null
                                    || datals.getSeqInfo().getContentName().equals("")) {
                                mcontent.setSequname(datals.getContentName());
                            } else {
                                mcontent.setSequname(datals.getSeqInfo().getContentName());
                            }

                            if (datals.getSeqInfo() == null
                                    || datals.getSeqInfo().getContentId() == null
                                    || datals.getSeqInfo().getContentId().equals("")) {
                                mcontent.setSequid(datals.getContentId());
                            } else {
                                mcontent.setSequid(datals.getSeqInfo().getContentId());
                            }

                            if (datals.getSeqInfo() == null
                                    || datals.getSeqInfo().getContentImg() == null
                                    || datals.getSeqInfo().getContentImg().equals("")) {
                                mcontent.setSequimgurl(datals.getContentImg());
                            } else {
                                mcontent.setSequimgurl(datals.getSeqInfo().getContentImg());
                            }

                            if (datals.getSeqInfo() == null
                                    || datals.getSeqInfo().getContentDesc() == null
                                    || datals.getSeqInfo().getContentDesc().equals("")) {
                                mcontent.setSequdesc(datals.getContentDesc());
                            } else {
                                mcontent.setSequdesc(datals.getSeqInfo().getContentDesc());
                            }
                            dataList.add(mcontent);
                            // 检查是否重复,如果不重复插入数据库，并且开始下载，重复了提示
                            List<FileInfo> filedatalist = FID.queryFileinfoAll(CommonUtils.getUserId(context));
                            if (filedatalist.size() != 0) {
                                /**
                                 * 此时有下载数据
                                 */
                                boolean isdownload = false;
                                for (int j = 0; j < filedatalist.size(); j++) {
                                    if (filedatalist.get(j).getUrl().equals(mcontent.getContentPlay())) {
                                        isdownload = true;
                                        break;
                                    } else {
                                        isdownload = false;
                                    }
                                }
                                if (isdownload) {
                                    ToastUtils.show_always(context,mcontent.getContentName() + "已经存在于下载列表");
                                } else {
                                    FID.insertfileinfo(dataList);
                                    ToastUtils.show_always(context,mcontent.getContentName() + "已经插入了下载列表");
                                    // 未下载列表
                                    List<FileInfo> fileundownloadlist = FID.queryFileinfo("false",CommonUtils.getUserId(context));
                                    FileInfo file;
                                    for (int kk = 0; kk < fileundownloadlist.size(); kk++) {
                                        if (fileundownloadlist.get(kk).getDownloadtype() == 1) {
                                            DownloadService.workStop(fileundownloadlist.get(kk));
                                            FID.updatedownloadstatus(fileundownloadlist.get(kk).getUrl(), "2");
                                            //Log.e("测试下载问题",	" 暂停下载的单体"+ (fileundownloadlist.get(kk).getFileName()));
                                        }
                                    }

                                    for (int k = 0; k < fileundownloadlist.size(); k++) {
                                        if (fileundownloadlist.get(k).getUrl().equals(mcontent.getContentPlay())) {
                                            file = fileundownloadlist.get(k);
                                            FID.updatedownloadstatus(mcontent.getContentPlay(), "1");
                                            DownloadService.workStart(file);
                                            Intent p_intent = new Intent("push_down_uncompleted");
                                            context.sendBroadcast(p_intent);
                                           // Log.e("广播消息", "开始下载");
                                            break;
                                        }
                                    }
                                }
                            } else {
                                /**
                                 * 此时库里没数据
                                 */
                                FID.insertfileinfo(dataList);
                                ToastUtils.show_always(context,mcontent.getContentName() + "已经插入了下载列表");
                                // 未下载列表
                                List<FileInfo> fileundownloadlist = FID.queryFileinfo("false", CommonUtils.getUserId(context));
                                FileInfo file;
                                for (int k = 0; k < fileundownloadlist.size(); k++) {
                                    if (fileundownloadlist.get(k).getUrl().equals(mcontent.getContentPlay())) {
                                        file = fileundownloadlist.get(k);
                                        FID.updatedownloadstatus(mcontent.getContentPlay(), "1");
                                        DownloadService.workStart(file);
                                        Intent p_intent = new Intent("push_down_uncompleted");
                                        context.sendBroadcast(p_intent);
                                        break;
                                    }
                                }
                            }
                        } else {
                            ToastUtils.show_always(context, "您现在播放的是电台节目，不支持下载");
                        }
                    } else {
                        ToastUtils.show_always(context, "当前播放器播放对象为空");
                    }
                    break;
                case 2://播放历史
                    startActivity(new Intent(context,PlayHistoryActivity.class));
                    break;
                case 3://我喜欢的
                    startActivity(new Intent(context,FavoriteActivity.class));
                    break;
                case 4://本地音频
                    startActivity(new Intent(context,DownloadActivity.class));
                    break;
                case 5://预定节目单
                    break;
                case 6://实时路况
                    if (audioPlay == null) {
                        audioPlay = TtsPlayer.getInstance(context);
                    } else {
                        // 不为空
                        if (audioPlay.mark().equals("VLC")) {
                            audioPlay.pause();
                        }
                        audioPlay = TtsPlayer.getInstance(context);
                    }
                    ToastUtils.show_always(context, "点击了路况TTS按钮");
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialogs = DialogUtils.Dialogph(context, "通讯中");
                        getLuKuangTTS();// 获取路况数据播报
                    } else {
                        ToastUtils.show_always(context, "网络连接失败，请稍后重试");
                    }
                    break;

            }
        }
    }

    /**
     *分享
     */
    private void shareDialog() {
        final View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_sharedialog, null);
        GridView mGallery = (GridView) dialog.findViewById(R.id.share_gallery);
        TextView tv_cancel = (TextView) dialog.findViewById(R.id.tv_cancle);
        shareDialog = new Dialog(context, R.style.MyDialog);
        // 从底部上升到一个位置
        shareDialog.setContentView(dialog);
        Window window = shareDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screen = dm.widthPixels;
        ViewGroup.LayoutParams params = dialog.getLayoutParams();
        params.width = screen;
        dialog.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.sharestyle);
        shareDialog.setCanceledOnTouchOutside(true);
        shareDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog1 = DialogUtils.Dialogphnoshow(context, "通讯中");
        Config.dialog = dialog1;
        final List<sharemodel> myList = ShareUtils.getShareModelList();
        ImageAdapter shareAdapter = new ImageAdapter(context, myList);
        mGallery.setAdapter(shareAdapter);
        mGallery.setSelector(new ColorDrawable(Color.TRANSPARENT));// 取消默认selector
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SHARE_MEDIA Platform = myList.get(position).getSharePlatform();
                CallShare(Platform);
                shareDialog.dismiss();
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
            }
        });
    }

    /**
     * 分享回调
     */
    protected void CallShare(SHARE_MEDIA Platform) {
        String sharename;
        String shareDesc;
        String shareContentImg;
        String shareurl;
        if (GlobalConfig.playerobject != null) {
            if (GlobalConfig.playerobject.getContentName() != null
                    && !GlobalConfig.playerobject.getContentName().equals("")) {
                sharename = GlobalConfig.playerobject.getContentName();
            } else {
                sharename = "我听我享听";
            }
            if (GlobalConfig.playerobject.getContentDesc() != null
                    && !GlobalConfig.playerobject.getContentDesc().equals("")) {
                shareDesc = GlobalConfig.playerobject.getContentDesc();
            } else {
                shareDesc = "暂无本节目介绍";
            }
            if (GlobalConfig.playerobject.getContentImg() != null
                    && !GlobalConfig.playerobject.getContentImg().equals("")) {
                shareContentImg = GlobalConfig.playerobject.getContentImg();
                image = new UMImage(context, shareContentImg);
            } else {
                shareContentImg = "http://182.92.175.134/img/logo-web.png";
                image = new UMImage(context, shareContentImg);
            }
            if (GlobalConfig.playerobject.getContentShareURL() != null
                    && !GlobalConfig.playerobject.getContentShareURL().equals("")) {
                shareurl = GlobalConfig.playerobject.getContentShareURL();
            } else {
                shareurl = "http://www.wotingfm.com/";
            }
            new ShareAction(context).setPlatform(Platform).withMedia(image)
                    .withText(shareDesc).withTitle(sharename).withTargetUrl(shareurl).share();
        } else {
            ToastUtils.show_short(context, "没有数据");
        }
    }

    /**
     * 设置当前为播放状态
     */
    private static void setPlayingType(){
        if(PlayerFragment.audioPlay != null && num >= 0){
            alllist.get(num).setType("2");
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置当前为暂停状态
     */
    private static void setPauseType(){
        if(PlayerFragment.audioPlay != null && num >= 0){
            alllist.get(num).setType("0");
            adapter.notifyDataSetChanged();
        }
    }
}
