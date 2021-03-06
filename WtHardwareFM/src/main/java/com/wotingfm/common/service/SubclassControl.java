package com.wotingfm.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.ui.interphone.alert.CallAlertFragment;
import com.wotingfm.ui.interphone.alert.ReceiveAlertFragment;
import com.wotingfm.ui.interphone.common.message.MessageUtils;
import com.wotingfm.ui.interphone.common.message.MsgNormal;
import com.wotingfm.ui.interphone.common.message.content.MapContent;
import com.wotingfm.ui.interphone.common.model.CallerInfo;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.Map;

/**
 * 单对单接听控制
 *
 * @author 辛龙
 *         2016年2月18日
 */
public class SubclassControl {
    private MessageReceiver Receiver;
    public static String callid;
    public static String callerId;
    public static boolean isallow = false;
    public static MediaPlayer musicPlayer;
    public static String callerinfo_username;
    public static String callerinfo_portrait;
    private Handler handler;

    private Context context;

    public SubclassControl(Context context) {
        this.context = context;
        if (Receiver == null) {
            Receiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.PUSH_SERVICE);
            context.registerReceiver(Receiver, filter);

            IntentFilter filterb3 = new IntentFilter();
            filterb3.addAction(BroadcastConstants.PUSH_BACK);
            filterb3.setPriority(1000);
            context.registerReceiver(Receiver, filterb3);
        }
        handler = new Handler();
    }

    /*
     * 接收socket的数据进行处理
     */
    class MessageReceiver extends BroadcastReceiver {
        private Runnable run;

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.PUSH_BACK)) {////////////////////////////////////////////////////////////////////////////////
                if (ReceiveAlertFragment.context == null) {
                } else {
                    abortBroadcast();
                    //					MsgNormal message = (MsgNormal) intent.getSerializableExtra("outmessage");
                    byte[] bt = intent.getByteArrayExtra("outmessage");
                    Log.e("push_back接收器中数据", Arrays.toString(bt) + "");
                    try {
                        MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                        if (message != null) {
                            int cmdType = message.getCmdType();
                            switch (cmdType) {
                                case 1:
                                    int command = message.getCommand();
                                    if (command == 0x30) {
                                        isallow = true;
                                        handler.removeCallbacks(run);
                                        if (musicPlayer != null) {
                                            musicPlayer.stop();
                                            musicPlayer = null;
                                        }
                                        if (ReceiveAlertFragment.context != null) {
                                            ReceiveAlertFragment.close();
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equals(BroadcastConstants.PUSH_SERVICE)) {///////////////////////////////////////////////////////////////////////
                //				MsgNormal message = (MsgNormal) intent.getSerializableExtra("outmessage");
                //				Log.i("弹出框服务push_service", "接收到的socket服务的信息"+message+"");
                byte[] bt = intent.getByteArrayExtra("outmessage");
                Log.e("push_service接收器中数据", Arrays.toString(bt) + "");
                try {
                    MsgNormal message = (MsgNormal) MessageUtils.buildMsgByBytes(bt);
                    if (message != null) {
                        int cmdType = message.getCmdType();
                        switch (cmdType) {
                            case 1:
                                int command = message.getCommand();
                                if (command == 0x10) {
                                    MapContent data = (MapContent) message.getMsgContent();
                                    String dialtype = data.get("DialType") + "";
                                    if (dialtype != null && !dialtype.equals("") && dialtype.equals("1")) {
                                        //应答消息：若Data.DialType=1必须要发送回执信息，否则不需要回执
                                        //										callid=data.getCallId();
                                        callid = data.get("CallId") + "";
                                        InterPhoneControlHelper.bdcallid = callid;
                                        //										callerId=data.getCallerId();
                                        callerId = data.get("CallerId") + "";

                                        try {
                                            Map<String, Object> map = data.getContentMap();
                                            String news = new Gson().toJson(map);

                                            JSONTokener jsonParser = new JSONTokener(news);
                                            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
                                            String callerinfos = arg1.getString("CallerInfo");

                                            CallerInfo callerinfo = new Gson().fromJson(callerinfos, new TypeToken<CallerInfo>() {
                                            }.getType());
                                            callerinfo_username = callerinfo.getNickName();
                                            callerinfo_portrait = callerinfo.getPortrait();
                                            isallow = false;//对应答消息是否处理
                                            if (run != null) {
                                                handler.removeCallbacks(run);
                                            }
                                            InterPhoneControlHelper.PersonTalkHJCDYD(context, callid, message.getMsgId().trim(), callerId);//呼叫传递应答
                                            if (CallAlertFragment.context != null) {
                                                CallAlertFragment.close();
                                            }
                                            if (ReceiveAlertFragment.context == null) {
                                                ReceiveAlertFragment fg = new ReceiveAlertFragment();
                                                DuiJiangActivity.open(fg);
                                            }
                                            run = new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (!isallow) {
                                                        //如果60s后没有没有对应答消息进行处理，则发送拒绝应答的消息已经弹出框消失
                                                        InterPhoneControlHelper.PersonTalkTimeOver(context, callid, callerId);//拒绝应答
                                                        if (musicPlayer != null) {
                                                            musicPlayer.stop();
                                                            musicPlayer = null;
                                                        }
                                                        if (ReceiveAlertFragment.context != null) {
                                                            ReceiveAlertFragment.close();
                                                        }
                                                        handler.removeCallbacks(run);
                                                    }
                                                }
                                            };
                                            handler.postDelayed(run, 60000);
                                            musicPlayer = MediaPlayer.create(context, R.raw.toy_mono);
                                            musicPlayer.start();
                                            //监听音频播放完的代码，实现音频的自动循环播放
                                            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer arg0) {
                                                    if (musicPlayer != null) {
                                                        musicPlayer.start();
                                                        musicPlayer.setLooping(true);
                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void unregister() {
        if (Receiver != null) {
            context.unregisterReceiver(Receiver);
            Receiver = null;
        }
    }

}
