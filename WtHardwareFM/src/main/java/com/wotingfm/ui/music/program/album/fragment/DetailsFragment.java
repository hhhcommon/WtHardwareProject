package com.wotingfm.ui.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.common.qrcode.EWMShowActivity;
import com.wotingfm.ui.music.comment.CommentActivity;
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.album.anchor.AnchorDetailsActivity;
import com.wotingfm.ui.music.program.album.model.ContentCatalogs;
import com.wotingfm.ui.music.program.album.model.ResultInfo;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.RoundImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 专辑详情页
 * @author woting11 2016/06/14
 */
public class DetailsFragment extends Fragment implements OnClickListener {
    private Context context;

    private Dialog dialog;// 加载数据对话框
    private View rootView;
    private TextView textConcern;
    private ImageView imageConcern;

    private ImageView imageAlbum;// 专辑封面图片
    private RoundImageView imageHead;// 主播头像
    private TextView textFavorite;// 喜欢
    private TextView textAnchor;// 主播
    private TextView textContent;// 内容介绍
    private TextView textLabel;// 标签

    private String personId;// 主播 ID
    private String contentPub;
    private String contentId;// ID
    private String contentFavorite;// 是否喜欢  == 1 喜欢  == 0 还没喜欢
    private String tag = "ALBUM_DETAILS_FRAGMENT_VOLLEY_REQUEST_CANCEL_TAG";

    private boolean isConcern;// 是否关注
    private boolean isInitData;// 是否初始化数据
    private boolean isInitView;// 是否初始化界面
    private boolean isCancelRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_details, container, false);
            initView(rootView);
        }
        return rootView;
    }

    // 初始化控件
    private void initView(View view) {
        imageAlbum = (ImageView) view.findViewById(R.id.img_album);// 专辑封面图片
        imageHead = (RoundImageView) view.findViewById(R.id.round_image_head);// 主播头像

        textFavorite = (TextView) view.findViewById(R.id.tv_favorite);// 喜欢状态
        textFavorite.setOnClickListener(this);

        view.findViewById(R.id.text_shape).setOnClickListener(this);// 分享
        view.findViewById(R.id.lin_pinglun).setOnClickListener(this);// 评论

        textAnchor = (TextView) view.findViewById(R.id.text_anchor_name);// 主播名字 OR 节目名
        textAnchor.setOnClickListener(this);

        // 关注
        view.findViewById(R.id.linear_concern).setOnClickListener(this);
        imageConcern = (ImageView) view.findViewById(R.id.image_concern);
        textConcern = (TextView) view.findViewById(R.id.text_concern);

        textContent = (TextView) view.findViewById(R.id.text_content);// 内容介绍
        textLabel = (TextView) view.findViewById(R.id.text_label);// 标签

        isInitView = true;
        ResultInfo resultInfo = ((AlbumActivity) context).getResultInfo();// 获取主播信息
        if(resultInfo == null) return ;

        initData(resultInfo);
    }

    // 初始化数据
    public void initData(ResultInfo resultInfo) {
        if(isInitData || !isInitView || resultInfo == null) return ;
        isInitData = true;

        // 专辑封面图片
        String contentImage = resultInfo.getContentImg();
        if(contentImage == null || contentImage.trim().equals("")) {// 设置默认专辑图片
            imageAlbum.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
        } else {
            if(!contentImage.startsWith("http")) {
                contentImage = GlobalConfig.imageurl + contentImage;
            }
            contentImage = AssembleImageUrlUtils.assembleImageUrl150(contentImage);
            Picasso.with(context).load(contentImage.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageAlbum);
            Picasso.with(context).load(contentImage.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageHead);
        }

        // 喜欢状态
        contentFavorite = resultInfo.getContentFavorite();
        if(contentFavorite != null && contentFavorite.equals("1")) {// 喜欢
            textFavorite.setText("已喜欢");
            textFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, null, context.getResources().getDrawable(R.mipmap.wt_img_liked));
        }

        // 主播名字 OR 节目名
        String anchorName = resultInfo.getContentName();
        if(anchorName == null || anchorName.trim().equals("")) {
            anchorName = "未知";
        }
        textAnchor.setText(anchorName);

        // 内容介绍
        String content = resultInfo.getContentDescn();
        if(content == null || content.trim().equals("")) {
            content = "暂无介绍";
        }
        textContent.setText(content);

        // 标签
        List<ContentCatalogs> contentCatalogsList = resultInfo.getContentCatalogs();
        if (contentCatalogsList != null && contentCatalogsList.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < contentCatalogsList.size(); i++) {
                String str = contentCatalogsList.get(i).getCataTitle();
                builder.append(str);
                if (i != contentCatalogsList.size() - 1) builder.append("  ");
            }
            textLabel.setText(builder.toString());
        }

        contentId = resultInfo.getContentId();// ID
        contentPub = resultInfo.getContentPub();// 来源  到下一个界面需要的数据

        // 主播 ID  到下一个界面需要的数据
        try {
            personId = resultInfo.getSubList().get(0).getContentPersons().get(0).getPerId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_favorite:// 喜欢
                if(CommonHelper.checkNetwork(context)) {
                    dialog = DialogUtils.Dialogph(context, "加载中...");
                    sendFavorite();
                }
                break;
            case R.id.text_shape:// 分享
                Intent shapeIntent = new Intent(context, EWMShowActivity.class);
                shapeIntent.putExtra("type", 3);
                startActivity(shapeIntent);
                break;
            case R.id.lin_pinglun:// 评论
                if(!CommonHelper.checkNetwork(context)) return ;
                if (!TextUtils.isEmpty(contentId)) {
                    if (CommonUtils.getUserIdNoImei(context) != null && !CommonUtils.getUserIdNoImei(context).equals("")) {
                        Intent intent = new Intent(context, CommentActivity.class);
                        intent.putExtra("contentId", contentId);
                        intent.putExtra("MediaType", StringConstant.TYPE_SEQU);
                        startActivity(intent);
                    } else {
                        ToastUtils.show_always(context, "请先登录~~");
                    }
                } else {
                    ToastUtils.show_always(context, "当前播放的节目的信息有误，无法获取评论列表");
                }
                break;
            case R.id.linear_concern:// 关注
                if (!isConcern) {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus_concern));
                    textConcern.setText("已关注");
                    ToastUtils.show_always(context, "测试---关注成功");
                } else {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus));
                    textConcern.setText("关注");
                    ToastUtils.show_always(context, "测试---取消关注");
                }
                isConcern = !isConcern;
                break;
            case R.id.text_anchor_name:// 到主播详情界面
                if (!TextUtils.isEmpty(personId)) {
                    Intent intent = new Intent(context, AnchorDetailsActivity.class);
                    intent.putExtra("PersonId", personId);
                    intent.putExtra("ContentPub", contentPub);
                    startActivity(intent);
                } else {
                    ToastUtils.show_always(context, "此专辑还没有主播哦");
                }
                break;
        }
    }

    // 发送网络请求  获取喜欢数据
    private void sendFavorite() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", contentId);
            if (contentFavorite.equals("0")) {
                jsonObject.put("Flag", "1");
            } else {
                jsonObject.put("Flag", "0");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.clickFavoriteUrl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String returnType = result.getString("ReturnType");
                    L.i("TAG", "returnType -- > > " + returnType);

                    if (returnType != null && returnType.equals("1001")) {
                        if (contentFavorite.equals("0")) {
                            contentFavorite = "1";
                            textFavorite.setText("已喜欢");
                            textFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.mipmap.wt_img_liked));
                        } else {
                            contentFavorite = "0";
                            textFavorite.setText("喜欢");
                            textFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.mipmap.wt_img_like));
                        }
                    } else if(returnType != null && returnType.equals("1005")) {// 返回结果是已经喜欢了此内容
                        contentFavorite = "1";
                        textFavorite.setText("已喜欢");
                        textFavorite.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.mipmap.wt_img_liked));
                    } else {
                        ToastUtils.show_always(context, "获取数据失败，请稍后再试!");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != rootView) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        context = null;
        rootView = null;
    }
}
