package com.wotingfm.ui.music.player.more;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.qrcode.EWMShowFragment;
import com.wotingfm.ui.music.player.more.subscribe.SubscriberListFragment;
import com.wotingfm.ui.music.comment.main.CommentFragment;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.main.DownloadFragment;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.download.service.DownloadService;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.player.model.LanguageSearchInside;
import com.wotingfm.ui.music.player.more.album.main.AlbumFragment;
import com.wotingfm.ui.music.player.more.playhistory.activity.PlayHistoryFragment;
import com.wotingfm.ui.music.player.more.programme.ProgrammeFragment;
import com.wotingfm.ui.music.program.album.model.ContentInfo;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放界面  ->  更多操作
 */
public class PlayerMoreOperationFragment extends Fragment implements View.OnClickListener {
    private TextView textPlayName;// 當前正在播放的節目名

    private MessageReceiver mReceiver;// 廣播
    private FileInfoDao mFileDao;// 文件相关数据库

    private View viewLinearOne;
    private View viewLinearTwo;

    private Dialog dialog;
    private TextView textLike;// 喜歡
    private TextView textShape;// 分享
    private TextView textComment;// 評論
    private TextView textDetails;// 詳情
    private TextView textProgram;// 播單
    private TextView textDown;// 下載
    private TextView textSequ;// 專輯

    private String contentFavorite;// 是否喜欢  == "1" 喜欢  == null or "0" 还没喜欢
    private String SequId;
    private String SequDesc;
    private String SequImage;
    private String SequName;
    private boolean IsSequ;
    private View rootView;
    private FragmentActivity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_player_more_operation, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            mFileDao = new FileInfoDao(context);
            initView();
            initEvent();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        registeredBroad();// 注册广播
    }

    // 初始化视图
    private void initView() {
        viewLinearOne = rootView.findViewById(R.id.view_linear_1);
        viewLinearTwo = rootView.findViewById(R.id.view_linear_2);

        textPlayName = (TextView) rootView.findViewById(R.id.text_play_name);// 當前正在播放的節目名
        textLike = (TextView) rootView.findViewById(R.id.text_like);// 喜歡
        textShape = (TextView) rootView.findViewById(R.id.text_shape);// 分享
        textComment = (TextView) rootView.findViewById(R.id.text_comment);// 評論
        textDetails = (TextView) rootView.findViewById(R.id.text_details);// 詳情
        textProgram = (TextView) rootView.findViewById(R.id.text_program);// 播單
        textDown = (TextView) rootView.findViewById(R.id.text_down);// 下載
        textSequ = (TextView) rootView.findViewById(R.id.text_sequ);// 專輯

        resetDate();// 設置 View
    }

    // 初始化点击事件
    private void initEvent() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回

        rootView.findViewById(R.id.text_history).setOnClickListener(this);// 播放歷史
        rootView.findViewById(R.id.text_liked).setOnClickListener(this);// 我喜歡的
        rootView.findViewById(R.id.text_local).setOnClickListener(this);// 本地節目

        textLike.setOnClickListener(this);// 喜歡
        textShape.setOnClickListener(this);// 分享
        textComment.setOnClickListener(this);// 評論
        textDetails.setOnClickListener(this);// 詳情
        textProgram.setOnClickListener(this);// 播單
        textDown.setOnClickListener(this);// 下載
        textSequ.setOnClickListener(this);// 專輯
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                PlayerActivity.close();
                break;
            case R.id.text_history:// 播放歷史
                PlayerActivity.open(new PlayHistoryFragment());
                break;
            case R.id.text_liked:// 我喜歡的
                ToastUtils.show_always(context, "更换订阅图标");
//                PlayerActivity.open(new FavoriteFragment());
                PlayerActivity.open(new SubscriberListFragment());
                break;
            case R.id.text_local:// 本地節目
                PlayerActivity.open(new DownloadFragment());
                break;
            case R.id.text_like:// 喜歡
                sendFavorite();
                break;
            case R.id.text_shape:// 分享
                if (GlobalConfig.playerObject == null) return;
                EWMShowFragment fg_evm = new EWMShowFragment();
                Bundle bundle_evm = new Bundle();
                bundle_evm.putInt("type", 0);
                fg_evm.setArguments(bundle_evm);
                PlayerActivity.open(fg_evm);
                break;
            case R.id.text_comment:// 評論
                if (!CommonHelper.checkNetwork(context)) return;
                if (!TextUtils.isEmpty(GlobalConfig.playerObject.getContentId()) && !TextUtils.isEmpty(GlobalConfig.playerObject.getMediaType())) {
                    if (CommonUtils.getUserIdNoImei(context) != null && !CommonUtils.getUserIdNoImei(context).equals("")) {
                        CommentFragment fg = new CommentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("contentId", GlobalConfig.playerObject.getContentId());
                        bundle.putString("MediaType", GlobalConfig.playerObject.getMediaType());
                        fg.setArguments(bundle);
                        PlayerActivity.open(fg);
                    } else {
                        ToastUtils.show_always(context, "请先登录~~");
                    }
                } else {
                    ToastUtils.show_always(context, "当前播放的节目的信息有误，无法获取评论列表");
                }
                break;
            case R.id.text_details:// 詳情
                PlayerActivity.open(new PlayDetailsFragment());
                break;
            case R.id.text_program:// 播單
                if (!CommonHelper.checkNetwork(context)) return;

                ProgrammeFragment fg_programme = new ProgrammeFragment();
                Bundle b = new Bundle();
                b.putString("BcId", GlobalConfig.playerObject.getContentId());
                fg_programme.setArguments(b);
                PlayerActivity.open(fg_programme);
                break;
            case R.id.text_down:// 下載
                download();
                break;
            case R.id.text_sequ:// 專輯
                if (GlobalConfig.playerObject != null) {
                    try {
                        if (GlobalConfig.playerObject.getSequId() != null) {
                            SequId = GlobalConfig.playerObject.getSequId();
                            SequDesc = GlobalConfig.playerObject.getSequDesc();
                            SequImage = GlobalConfig.playerObject.getSequImg();
                            SequName = GlobalConfig.playerObject.getSequName();
                            IsSequ = true;
                        } else if (GlobalConfig.playerObject.getSeqInfo() != null && GlobalConfig.playerObject.getSeqInfo().getContentId() != null) {
                            SequId = GlobalConfig.playerObject.getSeqInfo().getContentId();
                            SequDesc = GlobalConfig.playerObject.getSeqInfo().getContentDesc();
                            SequImage = GlobalConfig.playerObject.getSeqInfo().getContentImg();
                            SequName = GlobalConfig.playerObject.getSeqInfo().getContentName();
                            IsSequ = true;
                        } else {
                            IsSequ = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        IsSequ = false;
                    }
                }
                if (IsSequ) {
                    AlbumFragment fg_album = new AlbumFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "player");
                    bundle.putString("contentName", SequName);
                    bundle.putString("contentDesc", SequDesc);
                    bundle.putString("contentId", SequId);
                    bundle.putString("contentImg", SequImage);
                    fg_album.setArguments(bundle);
                    PlayerActivity.open(fg_album);
                } else {
                    ToastUtils.show_always(context, "此节目目前没有所属专辑");
                }
                break;
        }
    }

    // 重置數據
    private void resetDate() {
        if (GlobalConfig.playerObject == null || GlobalConfig.playerObject.getMediaType() == null) {
            textPlayName.setVisibility(View.GONE);
            viewLinearOne.setVisibility(View.GONE);
            viewLinearTwo.setVisibility(View.GONE);
            return;
        }

        textPlayName.setVisibility(View.VISIBLE);
        viewLinearOne.setVisibility(View.VISIBLE);
        viewLinearTwo.setVisibility(View.VISIBLE);

        // 標題
        String contentName;
        if (GlobalConfig.playerObject.getContentName() != null) {
            contentName = GlobalConfig.playerObject.getContentName();
        } else {
            contentName = "未知";
        }
        textPlayName.setText(contentName);

        // 播放類型
        String mediaType = GlobalConfig.playerObject.getMediaType();
        if (mediaType.equals(StringConstant.TYPE_RADIO)) {
            textDetails.setVisibility(View.GONE);// 詳情
            textProgram.setVisibility(View.VISIBLE);// 播单
            textSequ.setVisibility(View.INVISIBLE);// 專輯
            textDown.setVisibility(View.INVISIBLE);// 下载
        } else {
            textProgram.setVisibility(View.GONE);// 播单
            textDetails.setVisibility(View.VISIBLE);// 詳情
            textSequ.setVisibility(View.VISIBLE);// 專輯
            textDown.setVisibility(View.VISIBLE);// 下载

            if (mediaType.equals(StringConstant.TYPE_TTS)) {
                textComment.setClickable(false);
                textComment.setEnabled(false);
                textComment.setTextColor(context.getResources().getColor(R.color.gray));
                textComment.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(R.mipmap.wt_image_play_more_comment_gray), null, null);
            } else {
                textComment.setClickable(true);
                textComment.setEnabled(true);
                textComment.setTextColor(context.getResources().getColor(R.color.wt_login_third));
                textComment.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(R.mipmap.wt_image_play_more_comment), null, null);
            }
        }

        // 喜欢状态
        contentFavorite = GlobalConfig.playerObject.getContentFavorite();
        if (mediaType.equals(StringConstant.TYPE_TTS)) {// TTS 不支持喜欢
            textLike.setClickable(false);
            textLike.setEnabled(false);
            textLike.setText("喜欢");
            textLike.setTextColor(getResources().getColor(R.color.gray));
            textLike.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_like_gray), null, null);
        } else {
            textLike.setClickable(true);
            textLike.setEnabled(true);
            textLike.setTextColor(getResources().getColor(R.color.wt_login_third));
            if (contentFavorite == null || contentFavorite.equals("0")) {
                contentFavorite = "0";
                textLike.setText("喜欢");
                textLike.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_like), null, null);
            } else {
                contentFavorite = "1";
                textLike.setText("已喜欢");
                textLike.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_liked), null, null);
            }
        }

        // 下載狀態
        if (mediaType.equals(StringConstant.TYPE_AUDIO)) {// 可以下载
            if (!TextUtils.isEmpty(GlobalConfig.playerObject.getLocalurl())) {// 已下载
                textDown.setClickable(false);
                textDown.setEnabled(false);
                textDown.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_down_gray), null, null);
                textDown.setTextColor(getResources().getColor(R.color.gray));
                textDown.setText("已下载");
            } else {// 没有下载
                textDown.setClickable(true);
                textDown.setEnabled(true);
                textDown.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_down), null, null);
                textDown.setTextColor(getResources().getColor(R.color.wt_login_third));
                textDown.setText("下载");
            }
        } else if (mediaType.equals(StringConstant.TYPE_TTS)) {// 不可以下载
            textDown.setClickable(false);
            textDown.setEnabled(false);
            textDown.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.wt_image_play_more_down_gray), null, null);
            textDown.setTextColor(getResources().getColor(R.color.gray));
            textDown.setText("下载");
        }
    }

    // 喜欢---不喜欢操作
    private void sendFavorite() {
        dialog = DialogUtils.Dialogph(context, "通讯中");
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", GlobalConfig.playerObject.getMediaType());
            jsonObject.put("ContentId", GlobalConfig.playerObject.getContentId());
            if (contentFavorite.equals("0")) jsonObject.put("Flag", 1);
            else jsonObject.put("Flag", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.clickFavoriteUrl, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                try {
                    String returnType = result.getString("ReturnType");
                    if (returnType != null && (returnType.equals("1001") || returnType.equals("1005"))) {
                        if (contentFavorite.equals("0")) {
                            textLike.setText("已喜欢");
                            textLike.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(R.mipmap.wt_image_play_more_liked), null, null);
                            GlobalConfig.playerObject.setContentFavorite("1");
                            contentFavorite = "1";
                        } else {
                            textLike.setText("喜欢");
                            textLike.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(R.mipmap.wt_image_play_more_like), null, null);
                            GlobalConfig.playerObject.setContentFavorite("0");
                            contentFavorite = "0";
                        }
                    } else {
                        ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "数据出错了，请您稍后再试!");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 内容的下载
    private void download() {
        LanguageSearchInside data = GlobalConfig.playerObject;
        if (data.getLocalurl() != null) {
            ToastUtils.show_always(context, "此节目已经保存到本地，请到已下载界面查看");
            return;
        }
        // 对数据进行转换
        List<ContentInfo> dataList = new ArrayList<>();
        ContentInfo m = new ContentInfo();
//        m.setAuthor(data.getContentPersons());
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
                        context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED));
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
                    context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED));
                    break;
                }
            }
        }
    }

    // 注册广播
    private void registeredBroad() {
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BroadcastConstants.UPDATE_PLAY_VIEW);
            filter.addAction(BroadcastConstants.UPDATE_MORE_OPERATION_VIEW);// 更新界面
            context.registerReceiver(mReceiver, filter);
        }
    }

    // 广播接收器
    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastConstants.UPDATE_PLAY_VIEW:
                    resetDate();// 設置 View
                    break;
                case BroadcastConstants.UPDATE_MORE_OPERATION_VIEW:// 更新界面
                    L.w("TAG", "updateLocalList -- > " + GlobalConfig.playerObject.getLocalurl());
                    resetDate();// 設置 View
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
