package com.wotingfm.ui.mine.set.notifyset;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.mine.MineActivity;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.L;

/**
 * 通知设置
 */
public class NotifySetFragment extends Fragment implements View.OnClickListener {
    private SharedPreferences preferences;// 存储数据对象

    private ImageView imageNotifySound;// 通知声音开关
    private ImageView imageNotifyGroupChat;// 群聊通知开关
    private ImageView imageNotifyFriend;// 好友通知开关
    private ImageView imageNotifyProgram;// 节目推送通知开关
    private ImageView imageNotifySubscriber;// 订阅通知开关
    private ImageView imageNotifyConcern;// 关注通知开关

    private boolean soundState;// 声音开关
    private boolean groupChatState;// 群聊开关
    private boolean friendState;// 好友开关
    private boolean programState;// 节目推送开关
    private boolean subscriberState;// 订阅开关
    private boolean concernState;// 关注开关
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_notify_set, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            preferences = BSApplication.SharedPreferences;
            initViews();
        }
        return rootView;
    }


    // 初始化设置
    private void initViews() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);
        rootView.findViewById(R.id.view_notify_sound).setOnClickListener(this);// 通知声音
        imageNotifySound = (ImageView) rootView.findViewById(R.id.image_notify_sound);// 通知声音开关
        soundState = preferences.getBoolean(StringConstant.NOTIFY_SOUND_STATE, true);
        if(soundState) {
            imageNotifySound.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifySound.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.view_notify_group_chat).setOnClickListener(this);// 群聊通知
        imageNotifyGroupChat = (ImageView) rootView.findViewById(R.id.image_notify_group_chat);// 群聊通知开关
        groupChatState = preferences.getBoolean(StringConstant.NOTIFY_GROUP_CHAT_STATE, true);
        if(groupChatState) {
            imageNotifyGroupChat.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifyGroupChat.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.view_notify_friend).setOnClickListener(this);// 好友通知
        imageNotifyFriend = (ImageView) rootView.findViewById(R.id.image_notify_friend);// 好友通知开关
        friendState = preferences.getBoolean(StringConstant.NOTIFY_FRIEND_STATE, true);
        if(friendState) {
            imageNotifyFriend.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifyFriend.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.view_notify_program).setOnClickListener(this);// 节目推送通知
        imageNotifyProgram = (ImageView) rootView.findViewById(R.id.image_notify_program);// 节目推送通知开关
        programState = preferences.getBoolean(StringConstant.NOTIFY_PROGRAM_STATE, true);
        if(programState) {
            imageNotifyProgram.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifyProgram.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.view_notify_subscriber).setOnClickListener(this);// 订阅通知
        imageNotifySubscriber = (ImageView) rootView.findViewById(R.id.image_notify_subscriber);// 订阅通知开关
        subscriberState = preferences.getBoolean(StringConstant.NOTIFY_SUBSCRIBER_STATE, true);
        if(subscriberState) {
            imageNotifySubscriber.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifySubscriber.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }

        rootView.findViewById(R.id.view_notify_concern).setOnClickListener(this);// 关注通知
        imageNotifyConcern = (ImageView) rootView.findViewById(R.id.image_notify_concern);// 关注通知开关
        concernState = preferences.getBoolean(StringConstant.NOTIFY_CONCERN_STATE, true);
        if(concernState) {
            imageNotifyConcern.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
        } else {
            imageNotifyConcern.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.head_left_btn) MineActivity.close();// 返回

        SharedPreferences.Editor editor = preferences.edit();
        switch (v.getId()) {
            case R.id.view_notify_sound:// 通知声音
                if(!soundState) {
                    imageNotifySound.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifySound.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                soundState = !soundState;
                editor.putBoolean(StringConstant.NOTIFY_SOUND_STATE, soundState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
            case R.id.view_notify_group_chat:// 群聊通知
                if(!groupChatState) {
                    imageNotifyGroupChat.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifyGroupChat.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                groupChatState = !groupChatState;
                editor.putBoolean(StringConstant.NOTIFY_GROUP_CHAT_STATE, groupChatState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
            case R.id.view_notify_friend:// 好友通知
                if(!friendState) {
                    imageNotifyFriend.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifyFriend.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                friendState = !friendState;
                editor.putBoolean(StringConstant.NOTIFY_FRIEND_STATE, friendState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
            case R.id.view_notify_program:// 节目推送通知
                if(!programState) {
                    imageNotifyProgram.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifyProgram.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                programState = !programState;
                editor.putBoolean(StringConstant.NOTIFY_PROGRAM_STATE, programState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
            case R.id.view_notify_subscriber:// 订阅通知
                if(!subscriberState) {
                    imageNotifySubscriber.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifySubscriber.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                subscriberState = !subscriberState;
                editor.putBoolean(StringConstant.NOTIFY_SUBSCRIBER_STATE, subscriberState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
            case R.id.view_notify_concern:// 关注通知
                if(!concernState) {
                    imageNotifyConcern.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_on));
                } else {
                    imageNotifyConcern.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_person_close));
                }
                concernState = !concernState;
                editor.putBoolean(StringConstant.NOTIFY_CONCERN_STATE, concernState);
                if(!editor.commit()) L.w("数据 commit 失败!");
                break;
        }
    }
}
