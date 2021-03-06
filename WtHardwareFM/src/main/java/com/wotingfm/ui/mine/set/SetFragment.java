package com.wotingfm.ui.mine.set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.manager.CacheManager;
import com.wotingfm.common.manager.UpdateManager;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.mine.main.MineActivity;
import com.wotingfm.ui.mine.main.MineFragment;
import com.wotingfm.ui.mine.person.modifypassword.ModifyPasswordFragment;
import com.wotingfm.ui.mine.person.phonecheck.PhoneCheckFragment;
import com.wotingfm.ui.mine.set.about.AboutFragment;
import com.wotingfm.ui.mine.set.downloadposition.DownloadPositionFragment;
import com.wotingfm.ui.mine.set.feedback.main.FeedbackFragment;
import com.wotingfm.ui.mine.set.help.HelpFragment;
import com.wotingfm.ui.mine.set.notifyset.NotifySetFragment;
import com.wotingfm.ui.mine.set.preference.main.PreferenceFragment;
import com.wotingfm.ui.mine.set.updateusernum.UpdateUserNumberFragment;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.PhoneMessage;
import com.wotingfm.util.SequenceUUID;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;

/**
 * 设置
 * @author 辛龙
 * 2016年2月26日
 */
public class SetFragment extends Fragment implements OnClickListener {
    private Dialog updateDialog;        // 版本更新对话框
    private Dialog dialog;              // 加载数据对话框
    private Dialog clearCacheDialog;    // 清除缓存对话框
    private Button logOut;              // 注销
    private TextView textCache;         // 缓存
    private View lin_IsLogin;
    private View linearIdName;

    private int updateType = 1;         // 版本更新类型
    private String updateNews;          // 版本更新内容
    private String cache;               // 缓存
    private String cachePath;           // 缓存路径
    private String tag = "SET_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;
    private SetFragment ct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_set, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            ct=this;
            cachePath = Environment.getExternalStorageDirectory() + "/WTFM/playCache";// 缓存路径
            initDialog();
            initViews();
        }
        return rootView;
    }

    // 初始化控件
    private void initViews() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);          // 返回
        rootView.findViewById(R.id.lin_bindPhone).setOnClickListener(this);          // 绑定手机号
        rootView.findViewById(R.id.lin_reset_password).setOnClickListener(this);     // 重置密码
        rootView.findViewById(R.id.lin_clear).setOnClickListener(this);              // 清除缓存
        rootView.findViewById(R.id.lin_update).setOnClickListener(this);             // 检查更新
        rootView.findViewById(R.id.lin_help).setOnClickListener(this);               // 我听帮助
        rootView.findViewById(R.id.lin_about).setOnClickListener(this);              // 关于
        rootView.findViewById(R.id.lin_feedback).setOnClickListener(this);           // 意见反馈
        rootView.findViewById(R.id.lin_downloadposition).setOnClickListener(this);   // 下载位置
        rootView.findViewById(R.id.lin_preference).setOnClickListener(this);         // 偏好设置
        rootView.findViewById(R.id.lin_id_name).setOnClickListener(this);            // ID 号
        rootView.findViewById(R.id.lin_notify).setOnClickListener(this);             // 通知设置

        lin_IsLogin= rootView.findViewById(R.id.lin_IsLogin);                        // 未登录时需要隐藏的绑定手机号和重置密码布局
        linearIdName = rootView.findViewById(R.id.lin_id_name);                      // 用户可以且仅可以设置一次的唯一标识 ID
        linearIdName.setOnClickListener(this);

        logOut = (Button) rootView.findViewById(R.id.lin_zhuxiao);                   // 注销
        logOut.setOnClickListener(this);
        if(getArguments() != null) {
            if (!getArguments().getString("LOGIN_STATE").equals("true")) {
                logOut.setVisibility(View.GONE);
                lin_IsLogin.setVisibility(View.GONE);
            }
        }

        textCache = (TextView) rootView.findViewById(R.id.text_cache);               // 缓存
        initCache();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!BSApplication.SharedPreferences.getString(StringConstant.USER_NUM, "").equals("")) {
            linearIdName.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:        // 返回
                MineActivity.close();
                break;
            case R.id.lin_zhuxiao:          // 注销登录
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "正在获取数据");
                    sendRequestLogout();    // 清空数据
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
                break;
            case R.id.lin_clear:            // 清空缓存
                clearCacheDialog.show();
                break;
            case R.id.lin_about:            // 关于
                MineActivity.open(new AboutFragment());
                break;
            case R.id.lin_preference:       // 偏好设置
                MineActivity.open(new PreferenceFragment());
                break;
            case R.id.lin_feedback:         // 意见反馈
                MineActivity.open(new FeedbackFragment());
                break;
            case R.id.lin_update:           // 检查更新
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    dialog = DialogUtils.Dialogph(context, "通讯中");
                    sendRequestUpdate();
                } else {
                    ToastUtils.show_short(context, "网络失败，请检查网络");
                }
                break;
            case R.id.lin_help:             // 使用帮助
                MineActivity.open(new HelpFragment());
                break;
            case R.id.lin_downloadposition: // 下载位置
                MineActivity.open(new DownloadPositionFragment());
                break;
            case R.id.tv_update:            // 更新
                okUpdate();
                updateDialog.dismiss();
                break;
            case R.id.tv_qx:                // 取消更新
                if (updateType == 1) {
                    updateDialog.dismiss();
                } else {
                    ToastUtils.show_always(context, "本次需要更新");
                }
                break;
            case R.id.tv_confirm:           // 确定清除
                new ClearCacheTask().execute();
                break;
            case R.id.tv_cancle:            // 取消清除
                clearCacheDialog.dismiss();
                break;
            case R.id.lin_bindPhone:        // 绑定手机号
                String phoneNumber = BSApplication.SharedPreferences.getString(StringConstant.USER_PHONE_NUMBER, ""); // 用户手机号

                PhoneCheckFragment fg = new PhoneCheckFragment();
                Bundle bundle = new Bundle();
                if(!phoneNumber.equals("") || !phoneNumber.equals("null")){// 已经有存在的手机号
                    bundle.putString("PhoneType","1");
                    bundle.putString("PhoneNumber",phoneNumber);
                }else{// 手机号为空
                    bundle.putString("PhoneType","2");
                }

                fg.setArguments(bundle);
                MineActivity.open(fg);
                break;
            case R.id.lin_reset_password:   // 重置密码
                MineActivity.open(new ModifyPasswordFragment());
                break;
            case R.id.lin_id_name:// ID
//                startActivityForResult(new Intent(context, UpdateUserNumberActivity.class), 0x111);
                UpdateUserNumberFragment fg1 = new UpdateUserNumberFragment();
                fg1.setTargetFragment(ct, 0);
                context.getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_content, fg1)
                        .addToBackStack(SequenceUUID.getUUID())
                        .commit();
                break;
            case R.id.lin_notify:// 通知设置
                MineActivity.open(new NotifySetFragment());
                break;
        }
    }

    // 清除缓存对话框
    private void initDialog() {
        View dialog1 = LayoutInflater.from(context).inflate(R.layout.dialog_exit_confirm, null);
        dialog1.findViewById(R.id.tv_confirm).setOnClickListener(this); // 清空
        dialog1.findViewById(R.id.tv_cancle).setOnClickListener(this);  // 取消
        TextView textTitle = (TextView) dialog1.findViewById(R.id.tv_title);
        textTitle.setText("是否删除本地存储缓存?");

        clearCacheDialog = new Dialog(context, R.style.MyDialog);
        clearCacheDialog.setContentView(dialog1);
        clearCacheDialog.setCanceledOnTouchOutside(false);
        clearCacheDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
    }

    // 更新弹出框
    private void initUpdateDialog() {
        View dialog2 = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        dialog2.findViewById(R.id.tv_update).setOnClickListener(this);  // 开始更新
        dialog2.findViewById(R.id.tv_qx).setOnClickListener(this);      // 取消
        TextView textContent = (TextView) dialog2.findViewById(R.id.text_content);
        textContent.setText(Html.fromHtml("<font size='26'>" + updateNews + "</font>"));

        updateDialog = new Dialog(context, R.style.MyDialog);
        updateDialog.setContentView(dialog2);
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        updateDialog.show();
    }

    // 注销数据交互
    private void sendRequestLogout() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        VolleyRequest.RequestPost(GlobalConfig.logoutUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String returnType = result.getString("ReturnType");
                    String message = result.getString("Message");
                    Log.v("returnType", "returnType -- > " + returnType + ", message -- > " + message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Editor et = BSApplication.SharedPreferences.edit();
                et.putString(StringConstant.ISLOGIN, "false");
                et.putString(StringConstant.USERID, "");
                et.putString(StringConstant.USER_NUM, "");
                et.putString(StringConstant.IMAGEURL, "");
                et.putString(StringConstant.USER_PHONE_NUMBER, "");
                et.putString(StringConstant.USER_NUM, "");
                et.putString(StringConstant.GENDERUSR, "");
                et.putString(StringConstant.EMAIL, "");
                et.putString(StringConstant.REGION, "");
                et.putString(StringConstant.BIRTHDAY, "");
                et.putString(StringConstant.USER_SIGN, "");
                et.putString(StringConstant.STAR_SIGN, "");
                et.putString(StringConstant.AGE, "");
                et.putString(StringConstant.NICK_NAME, "");
                if (!et.commit()) Log.v("commit", "数据 commit 失败!");
                logOut.setVisibility(View.GONE);
                lin_IsLogin.setVisibility(View.GONE);
                context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_COMPLETED));// 发送广播 更新已下载和未下载界面
                Toast.makeText(context, "注销成功", Toast.LENGTH_SHORT).show();
                Fragment targetFragment = getTargetFragment();
                ((MineFragment) targetFragment).setResult();
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 调用更新功能
    protected void okUpdate() {
        UpdateManager updateManager = new UpdateManager(context);
        updateManager.checkUpdateInfo1();
    }

    // 更新数据交互
    private void sendRequestUpdate() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("Version", PhoneMessage.appVersonName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.VersionUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String returnType = result.getString("ReturnType");
                    if (returnType != null && returnType.equals("1001")) {
                        GlobalConfig.apkUrl = result.getString("DownLoadUrl");
                        String MastUpdate = result.getString("MastUpdate");
                        String ResultList = result.getString("CurVersion");
                        if (ResultList != null && MastUpdate != null) {
                            dealVersion(ResultList, MastUpdate);
                        } else {
                            Log.e("检查更新返回值", "返回值为1001，但是返回的数值有误");
                        }
                    } else {
                        ToastUtils.show_always(context, "当前已是最新版本");
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

    // 检查版本更新
    protected void dealVersion(String ResultList, String mastUpdate) {
        String version = "0.1.0.X.0";
        String Descn = null;
        try {
            JSONObject arg1 = (JSONObject) new JSONTokener(ResultList).nextValue();
            version = arg1.getString("Version");
            Descn = arg1.getString("Descn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 版本更新比较
        String[] strArray = version.split("\\.");
        try {
            int versionOld = PhoneMessage.versionCode;
            int versionNew = Integer.parseInt(strArray[4]);
            if (versionNew > versionOld) {
                if (mastUpdate != null && mastUpdate.equals("1")) {// 强制升级
                    if (Descn != null && !Descn.trim().equals("")) {
                        updateNews = Descn;
                    } else {
                        updateNews = "本次版本升级较大，需要更新";
                    }
                    updateType = 2;
                    initUpdateDialog();
                } else {            // 普通升级
                    if (Descn != null && !Descn.trim().equals("")) {
                        updateNews = Descn;
                    } else {
                        updateNews = "有新的版本需要升级喽";
                    }
                    updateType = 1;// 不需要强制升级
                    initUpdateDialog();
                }
            } else if(versionNew == versionOld) {
                ToastUtils.show_always(context, "已经是最新版本");
            } else {
                ToastUtils.show_always(context, "已经是最新版本");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("版本处理异常", e.toString() + "");
        }
    }

    // 启动统计缓存的线程
    private void initCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(cachePath);
                try {
                    cache = CacheManager.getCacheSize(file);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textCache.setText(cache);
                        }
                    });
                } catch (Exception e) {
                    Log.e("获取本地缓存文件大小", cache);
                }
            }
        }).start();
    }

    // 清除缓存异步任务
    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        private boolean clearResult;

        @Override
        protected void onPreExecute() {
            clearCacheDialog.dismiss();
            dialog = DialogUtils.Dialogph(context, "正在清除缓存");
        }

        @Override
        protected Void doInBackground(Void... params) {
            clearResult = CacheManager.delAllFile(cachePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog != null) dialog.dismiss();
            if (clearResult) {
                ToastUtils.show_always(context, "缓存已清除");
                textCache.setText("0MB");
            } else {
                Log.e("缓存异常", "缓存清理异常");
                initCache();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0x111) {
            if(resultCode == 1) {
                Intent intent = new Intent();
                intent.putExtra("SET_USER_NUM_SUCCESS", true);
                context.setResult(Activity.RESULT_OK);
            }
        }
    }

    public void setAddCardResult() {
        Fragment targetFragment = getTargetFragment();
        ((MineFragment) targetFragment).setAddCardResult(2,null,null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        updateDialog = null;
        dialog = null;
        clearCacheDialog = null;
        updateNews = null;
        cache = null;
        cachePath = null;
        textCache = null;
    }
}
