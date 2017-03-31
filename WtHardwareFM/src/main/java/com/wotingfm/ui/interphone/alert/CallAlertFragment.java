package com.wotingfm.ui.interphone.alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.ui.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.ui.interphone.common.message.MessageUtils;
import com.wotingfm.ui.interphone.common.message.MsgNormal;
import com.wotingfm.ui.interphone.common.message.content.MapContent;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.main.DuiJiangFragment;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;

import java.util.Arrays;

/**
 * 呼叫弹出框
 * author：辛龙 (xinLong)
 * 2016/12/21 18:10
 * 邮箱：645700751@qq.com
 */
public class CallAlertFragment extends Fragment implements OnClickListener {
    private TextView tv_news;
    private TextView tv_name;
    private LinearLayout lin_call;
    private LinearLayout lin_guaduan;
    private MediaPlayer musicPlayer;
    private SearchTalkHistoryDao dbdao;
    private String id;
    private MessageReceiver Receiver;
    private String image;
    private String name;
    private ImageView imageview;
    private boolean isCall = true;
    private FragmentActivity instance;
    private View rootView;
    public static CallAlertFragment context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.dialog_calling, container, false);
            rootView.setOnClickListener(this);

//        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMicrophoneMute(false);
//        audioManager.setSpeakerphoneOn(true);//使用扬声器外放，即使已经插入耳机
//        setVolumeControlStream(AudioManager.STREAM_MUSIC);//控制声音的大小
//        audioManager.setMode(AudioManager.STREAM_MUSIC);

            instance = getActivity();
            context = this;
            getSource();        // 获取展示数据
            setReceiver();      // 设置广播接收器
            setView();          // 设置界面，以及界面数据
            setDate();          // 业务数据处理
            initDao();          // 初始化数据库
        }
        return rootView;
    }

    /*
     *业务数据处理
     */
    private void setDate() {
        InterPhoneControlHelper.PersonTalkPress(instance, id);//拨号
        musicPlayer = MediaPlayer.create(instance, R.raw.ringback);
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(instance, R.raw.talkno);
        }
//        musicPlayer = MediaPlayer.create(instance, getSystemDefultRingtoneUri());

        if (musicPlayer != null) {
            musicPlayer.start();
            // 监听音频播放完的代码，实现音频的自动循环播放
            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    if (musicPlayer != null) {
                        musicPlayer.start();
                        musicPlayer.setLooping(true);
                    }
                }
            });
        } else {
            // 播放器初始化失败
        }
    }

//    //获取系统默认铃声的Uri
//    private Uri getSystemDefultRingtoneUri() {
//        return RingtoneManager.getActualDefaultRingtoneUri(this,
//                RingtoneManager.TYPE_RINGTONE);
//    }

    public static void close() {
        DuiJiangActivity.close();
    }

    /*
     *设置界面，以及界面数据
     */
    private void setView() {


        rootView.findViewById(R.id.im_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InterPhoneControlHelper.PersonTalkHangUp(instance, InterPhoneControlHelper.bdcallid);
                DuiJiangActivity.close();
            }
        });
        tv_news = (TextView) rootView.findViewById(R.id.tv_news);
        imageview = (ImageView) rootView.findViewById(R.id.image);
        tv_name = (TextView) rootView.findViewById(R.id.tv_name);

        lin_call = (LinearLayout) rootView.findViewById(R.id.lin_call);
        lin_call.setOnClickListener(this);

        lin_guaduan = (LinearLayout) rootView.findViewById(R.id.lin_guaduan);
        lin_guaduan.setOnClickListener(this);

        ImageView img_zhezhao = (ImageView) rootView.findViewById(R.id.img_zhezhao);
        Bitmap bmp_zhezhao = BitmapUtils.readBitMap(instance, R.mipmap.liubianxing_orange_big);
        img_zhezhao.setImageBitmap(bmp_zhezhao);

        tv_name.setText(name);
        if (image == null || image.equals("") || image.equals("null") || image.trim().equals("")) {
            imageview.setImageResource(R.mipmap.wt_image_tx_hy);
        } else {
            if (!image.startsWith("http:")) {
                image = GlobalConfig.imageurl + image;
            }
            String _url = AssembleImageUrlUtils.assembleImageUrl300(image);

            // 加载图片
            AssembleImageUrlUtils.loadImage(_url, image, imageview, IntegerConstant.TYPE_PERSON);
        }
    }

    /*
     *设置广播接收器
     */
    private void setReceiver() {
        if (Receiver == null) {
            Receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PUSH_CALL);
            instance.registerReceiver(Receiver, filter);
        }
    }

    /*
     *获取展示数据
     */
    private void getSource() {
        if (getArguments() != null) {
            id = getArguments().getString("id");
        }
        try {
            if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() > 0) {
                for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                    if (id.equals(GlobalConfig.list_person.get(i).getUserId())) {
                        image = GlobalConfig.list_person.get(i).getPortraitBig();
                        name = GlobalConfig.list_person.get(i).getUserName();
                        break;
                    }
                }
            } else {
                image = null;
                name = "未知";
            }
        } catch (Exception e) {
            e.printStackTrace();
            image = null;
            name = "未知";
        }
    }

    /*
     * 初始化数据库
     */
    private void initDao() {
        dbdao = new SearchTalkHistoryDao(instance);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_call:
                tv_news.setText("呼叫中..");
                lin_call.setVisibility(View.GONE);
                lin_guaduan.setVisibility(View.VISIBLE);
                isCall = true;
                InterPhoneControlHelper.PersonTalkPress(instance, id);        //拨号
                musicPlayer = MediaPlayer.create(instance, R.raw.ringback);
                if (musicPlayer == null) {
                    musicPlayer = MediaPlayer.create(instance, R.raw.talkno);
                }
//                musicPlayer = MediaPlayer.create(instance, getSystemDefultRingtoneUri());
                if (musicPlayer != null) {
                    musicPlayer.start();
                    // 监听音频播放完的代码，实现音频的自动循环播放
                    musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer arg0) {
                            if (musicPlayer != null) {
                                musicPlayer.start();
                                musicPlayer.setLooping(true);
                            }
                        }
                    });
                } else {
                    // 播放器初始化失败
                }
                break;
            case R.id.lin_guaduan:
                tv_news.setText("重新呼叫");
                lin_call.setVisibility(View.VISIBLE);
                lin_guaduan.setVisibility(View.GONE);
                isCall = false;
                if (musicPlayer != null) {
                    musicPlayer.stop();
                    musicPlayer = null;
                }
                InterPhoneControlHelper.PersonTalkHangUp(instance, InterPhoneControlHelper.bdcallid);
                break;
        }
    }

    public void addUser() {
        String addTime = Long.toString(System.currentTimeMillis());    // 获取最新激活状态的数据
        String BJUserId = CommonUtils.getUserId(instance);
        dbdao.deleteHistory(id);                                       // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
        DBTalkHistorary history = new DBTalkHistorary(BJUserId, "user", id, addTime);
        dbdao.addTalkHistory(history);
//        DBTalkHistorary talkdb = dbdao.queryHistory().get(0);          // 得到数据库里边数据
        ChatFragment.zhiDingPerson();
        DuiJiangFragment.update();
        DuiJiangActivity.close();
    }

    /*
     * 接收socket的数据进行处理
     */
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.PUSH_CALL)) {
                byte[] bt = intent.getByteArrayExtra("outMessage");
                Log.e("push_call接收器中数据", Arrays.toString(bt) + "");
                try {
                    MsgNormal outMessage = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    //				MsgNormal outMessage = (MsgNormal) intent.getSerializableExtra("outMessage");
                    //				Log.i("对讲页面====", "接收到的socket服务的信息" + outMessage+"");
                    if (outMessage != null) {
                        int cmdType = outMessage.getCmdType();
                        if (cmdType == 1) {
                            int command = outMessage.getCommand();
                            switch (command) {
                                case 9:
                                    int returnType = outMessage.getReturnType();
                                    switch (returnType) {
                                        case 0x01:
                                            Log.e("服务端拨号状态", "成功返回，对方可通话");
                                            break;
                                        case 2:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "呼叫用户不在线");
                                            break;
                                        case 3:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败，用户不在线");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "被叫用户不在线");
                                            break;
                                        case 4:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "呼叫用户占线（在通电话）");
                                            break;
                                        case 5:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "呼叫用户占线（在对讲）");
                                            break;
                                        case 6:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "呼叫用户占线（自己呼叫自己）");
                                            break;
                                        case 0x81:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "此通话已被占用");
                                            break;
                                        case 0x82:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            //此通话对象状态错误（status应该为0，这个消息若没有特殊情况，是永远不会返回的）
                                            Log.e("服务端拨号状态", "此通话对象状态错误");
                                            break;
                                        case 0xff:
                                            if (musicPlayer != null) {
                                                musicPlayer.stop();
                                                musicPlayer = null;
                                            }
                                            tv_news.setText("呼叫失败");
                                            lin_guaduan.setVisibility(View.GONE);
                                            lin_call.setVisibility(View.VISIBLE);
                                            isCall = false;
                                            Log.e("服务端拨号状态", "异常返回值");
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                case 0x40:
                                    MapContent data = (MapContent) outMessage.getMsgContent();
                                    String onlinetype = data.get("OnLineType") + "";
                                    if (onlinetype != null && !onlinetype.equals("") && onlinetype.equals("1")) {
                                        //被叫着在线，不用处理
                                    } else {
                                        //被叫着不在线，挂断电话
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        tv_news.setText("对方不在线");
                                        lin_guaduan.setVisibility(View.GONE);
                                        lin_call.setVisibility(View.VISIBLE);
                                        isCall = false;
                                    }
                                    break;
                                case 0x20:
                                    MapContent datas = (MapContent) outMessage.getMsgContent();
                                    String ACKType = datas.get("ACKType") + "";
                                    if (ACKType != null && !ACKType.equals("") && ACKType.equals("1")) {
                                        //此时对讲连接建立可以通话
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        if (isCall) addUser();
                                    } else if (ACKType != null && !ACKType.equals("") && ACKType.equals("2")) {
                                        //拒绝通话，挂断电话
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        tv_news.setText("呼叫失败");
                                        lin_guaduan.setVisibility(View.GONE);
                                        lin_call.setVisibility(View.VISIBLE);
                                        isCall = false;
                                    } else if (ACKType != null && !ACKType.equals("") && ACKType.equals("31")) {
                                        //被叫客户端超时应答，挂断电话
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        tv_news.setText("呼叫失败");
                                        lin_guaduan.setVisibility(View.GONE);
                                        lin_call.setVisibility(View.VISIBLE);
                                        isCall = false;
                                    } else if (ACKType != null && !ACKType.equals("") && ACKType.equals("32")) {
                                        //长时间不接听，服务器超时，挂断电话
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        tv_news.setText("呼叫失败");
                                        lin_guaduan.setVisibility(View.GONE);
                                        lin_call.setVisibility(View.VISIBLE);
                                        isCall = false;
                                    } else {
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        tv_news.setText("呼叫失败");
                                        lin_guaduan.setVisibility(View.GONE);
                                        lin_call.setVisibility(View.VISIBLE);
                                        isCall = false;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer = null;
        }
        if (Receiver != null) {
            instance.unregisterReceiver(Receiver);
            Receiver = null;
        }
        instance = null;
        tv_news = null;
        tv_name = null;
        lin_call = null;
        lin_guaduan = null;
        id = null;
        image = null;
        name = null;
        imageview = null;
        if (dbdao != null) {
            dbdao = null;
        }
    }
}
