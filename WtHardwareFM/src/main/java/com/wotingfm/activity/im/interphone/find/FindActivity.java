package com.wotingfm.activity.im.interphone.find;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.activity.im.interphone.find.result.FindNewsResultActivity;
import com.wotingfm.activity.im.interphone.scanning.activity.CaptureActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.ToastUtils;

/**
 * 查找好友或查找群组
 */
public class FindActivity extends Activity implements View.OnClickListener {
    private EditText editContent;           // 输入的内容
    private ImageView imageVoiceSearch;     // 语音搜索
    private ImageView imageDelete;          // 删除输入的内容
    private View relativeSao;               // 扫描
    private View linearContentSearch;       // 文字搜索
    private TextView textContent;           // 输入的要搜索的内容

    private Bitmap bmp;
    private Bitmap bmpPress;
    private Dialog yuYinDialog;             // 语音搜索对话框
    private LinearLayout linearVoice;
    private ImageView imageViewVoice;       // 语音搜索
    private TextView textCancel;            // 关闭
    private TextView textSpeakStatus;       // 语音搜索状态
    private TextView textSpeakContent;      // 语音搜索内容
    private int screen;
    //    private VoiceRecognizer mVoiceRecognizer;
    protected int curVolume;
    private AudioManager audioMgr;
    private int stepVolume;
    private String type;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(BroadcastConstants.FINDVOICE);
        registerReceiver(mBroadcastReceiver, myFilter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);    // 透明导航栏
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);          // 获取最大音乐音量
        stepVolume = maxVolume / 100;               // 每次调整的音量大概为最大音量的1/100
        initViews();
    }

    /**
     * 处理上一个界面传递过来的数据
     */
    private void handlerIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getStringExtra(StringConstant.FIND_TYPE);
            if (type != null && !type.equals("")) {
                if (type.equals(StringConstant.FIND_TYPE_GROUP)) {
                    editContent.setHint("群名称");
                }
            } else {
                ToastUtils.show_allways(FindActivity.this, "类型获取异常，请返回上一级界面重试");
            }
        }
    }

    /*
     * 初始化视图
     */
    private void initViews() {
        TextView leftBack = (TextView) findViewById(R.id.head_left_btn);
        leftBack.setOnClickListener(this);

        imageVoiceSearch = (ImageView) findViewById(R.id.img_voice_search);
        imageVoiceSearch.setOnClickListener(this);

        imageDelete = (ImageView) findViewById(R.id.img_delete);
        imageDelete.setOnClickListener(this);

        relativeSao = findViewById(R.id.relative_saoyisao);
        relativeSao.setOnClickListener(this);

        linearContentSearch = findViewById(R.id.lin_contact_search);
        linearContentSearch.setOnClickListener(this);

        editContent = (EditText) findViewById(R.id.et_news);
        textContent = (TextView) findViewById(R.id.text_search);

        initDialog();
        handlerIntent();
        setEditTextListener();
    }

    /**
     * 设置输入框监听事件
     */
    private void setEditTextListener() {
        editContent.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 有数据改变的时候界面的变化
                if (!s.toString().trim().equals("")) {
                    relativeSao.setVisibility(View.GONE);
                    imageVoiceSearch.setVisibility(View.GONE);
                    linearContentSearch.setVisibility(View.VISIBLE);
                    imageDelete.setVisibility(View.VISIBLE);
                    textContent.setText(s.toString());
                } else {
                    linearContentSearch.setVisibility(View.GONE);
                    imageDelete.setVisibility(View.GONE);
                    imageVoiceSearch.setVisibility(View.VISIBLE);
                    relativeSao.setVisibility(View.VISIBLE);
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

    // 语音搜索对话框
    private void initDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_yuyin_search, null);
        //定义dialog view
        bmp = BitmapUtils.readBitMap(FindActivity.this, R.mipmap.wt_image_talk_normal);
        bmpPress = BitmapUtils.readBitMap(FindActivity.this, R.mipmap.wt_duijiang_button_pressed);
        linearVoice = (LinearLayout) dialogView.findViewById(R.id.rl_voice);
        imageViewVoice = (ImageView) dialogView.findViewById(R.id.imageView_voice);
        imageViewVoice.setImageBitmap(bmp);
        textCancel = (TextView) dialogView.findViewById(R.id.tv_cancle);
        textSpeakStatus = (TextView) dialogView.findViewById(R.id.tv_speak_status);
        textSpeakStatus.setText("请按住讲话");
        textSpeakContent = (TextView) dialogView.findViewById(R.id.text_speak_content);
        // 初始化dialog出现配置
        yuYinDialog = new Dialog(FindActivity.this, R.style.MyDialog);
        yuYinDialog.setContentView(dialogView);
        Window window = yuYinDialog.getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screen = dm.widthPixels;
        ViewGroup.LayoutParams params = dialogView.getLayoutParams();
        params.width = screen;
        dialogView.setLayoutParams(params);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.ShareStyle);
        // 定义view的监听
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yuYinDialog.dismiss();
                textSpeakContent.setVisibility(View.GONE);
            }
        });
        imageViewVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                            curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                            audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, stepVolume, AudioManager.FLAG_PLAY_SOUND);
//                            mVoiceRecognizer.startListen();
                            textSpeakStatus.setText("开始语音转换");
                            imageViewVoice.setImageBitmap(bmpPress);
                            textSpeakContent.setVisibility(View.GONE);
                        } else {
                            ToastUtils.show_short(FindActivity.this, "网络失败，请检查网络");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
//                        mVoiceRecognizer.stopListen();
                        imageViewVoice.setImageBitmap(bmp);
                        textSpeakStatus.setText("请按住讲话");
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
            case R.id.img_voice_search:     // 语音搜索
                yuYinDialog.show();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(linearVoice.getWindowToken(), 0);
                break;
            case R.id.img_delete:           // 清空
                editContent.setText("");
                break;
            case R.id.relative_saoyisao:    // 扫描
                startActivity(new Intent(FindActivity.this, CaptureActivity.class));
                break;
            case R.id.lin_contact_search:   // 搜索
                String searchString = editContent.getText().toString().trim();
                if (searchString.equals("")) {
                    ToastUtils.show_allways(FindActivity.this, "您所输入的内容为空");
                    return;
                }
                // 跳转到搜索结果界面
                Intent intent = new Intent(FindActivity.this, FindNewsResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(StringConstant.FIND_CONTENT_TO_RESULT, searchString);
                bundle.putString(StringConstant.FIND_TYPE, type);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }

    //广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConstants.FINDVOICE)) {
                String str = intent.getStringExtra("VoiceContent");
                textSpeakStatus.setText("正在为您查找: " + str);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (yuYinDialog != null) {
                            yuYinDialog.dismiss();
                        }
                    }
                }, 2000);
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    if (!str.trim().equals("")) {
                        editContent.setText(str.trim());
                    }
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
