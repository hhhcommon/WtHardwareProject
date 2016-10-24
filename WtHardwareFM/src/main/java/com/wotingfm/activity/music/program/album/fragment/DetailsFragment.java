package com.wotingfm.activity.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.album.model.ContentCatalogs;
import com.wotingfm.activity.music.program.album.model.ContentInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

/**
 * 专辑详情页
 * @author woting11 2016/06/14
 */
public class DetailsFragment extends Fragment implements OnClickListener {
    private Context context;
    private View rootView;
    private ImageView imageHead;
    private TextView textAnchor, textContent, textLabel;
    private ImageView imageConcern;
    private boolean isConcern;
    private Dialog dialog;
    private LinearLayout linearConcern;    // linear_concern
    private List<ContentInfo> SubList;    // 请求返回的网络数据值
    private String contentDesc;
    private TextView textConcern;        // text_concern
    private LinearLayout lin_share;
    private LinearLayout lin_favorite;
    public static String ContentFavorite;        // 从网络获取的当前值，如果为空，表示页面并未获取到此值
    private String tag = "DETAILS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private TextView tv_favorite;
    private ImageView imgFavorite;
    private String ContentShareURL;
    private String ContentImg;
    private String ContentName;
    private ImageView img_album;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_details, container, false);
            findView(rootView);
            setListener();
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            send();
        } else {
            ToastUtils.show_short(context, "网络失败，请检查网络");
        }
        return rootView;
    }

    private void setListener() {
        lin_share.setOnClickListener(this);
        lin_favorite.setOnClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void findView(View view) {
        lin_share = (LinearLayout) view.findViewById(R.id.lin_share);        // 分享按钮
        lin_favorite = (LinearLayout) view.findViewById(R.id.lin_favorite);    // 喜欢按钮
        imageHead = (ImageView) view.findViewById(R.id.round_image_head);    //圆形头像
        textAnchor = (TextView) view.findViewById(R.id.text_anchor_name);        //节目名
        textContent = (TextView) view.findViewById(R.id.text_content);            //内容介绍
        textLabel = (TextView) view.findViewById(R.id.text_label);                //标签
        imageConcern = (ImageView) view.findViewById(R.id.image_concern);        //关注
        textConcern = (TextView) view.findViewById(R.id.text_concern);
        tv_favorite = (TextView) view.findViewById(R.id.tv_favorite);
        imgFavorite = (ImageView) view.findViewById(R.id.img_favorite);
        img_album = (ImageView) view.findViewById(R.id.img_album);
        linearConcern = (LinearLayout) view.findViewById(R.id.linear_concern);
        linearConcern.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_concern://关注
                if (!isConcern) {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus_concern));
                    textConcern.setText("已关注");
                    ToastUtils.show_always(context, "关注成功");
                } else {
                    imageConcern.setImageDrawable(context.getResources().getDrawable(R.mipmap.focus));
                    textConcern.setText("关注");
                    ToastUtils.show_always(context, "取消关注");
                }
                break;
            case R.id.lin_favorite: // 喜欢
                if (ContentFavorite != null && !ContentFavorite.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在获取数据");
                        sendFavorite();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "专辑信息获取异常");
                }
                isConcern = !isConcern;
                break;
            case R.id.lin_share:
                AlbumActivity.shareDialog.show();
                break;
        }
    }

    /**
     * 发送网络请求  获取喜欢数据
     */
    private void sendFavorite() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", AlbumActivity.id);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
            if (ContentFavorite.equals("0")) {
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
                    String ReturnType = result.getString("ReturnType");   // 根据返回值来对程序进行解析
                    if (ReturnType != null) {
                        if (ReturnType.equals("1001")) {
                            if (ContentFavorite.equals("0")) {
                                ContentFavorite = "1";
                                tv_favorite.setText("已喜欢");
                                imgFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_liked));
                            } else {
                                ContentFavorite = "0";
                                tv_favorite.setText("喜欢");
                                imgFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_like));
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
                            try {
                                String Message = result.getString("Message");
                                ToastUtils.show_always(context, Message + "");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        ToastUtils.show_always(context, "Returntype==null");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }


    /**
     * 向服务器发送请求
     */
    public void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", AlbumActivity.id);
            jsonObject.put("Page", "1");
            jsonObject.put("PCDType", GlobalConfig.PCDType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getContentById, tag, jsonObject, new VolleyCallback() {
            private String ReturnType;
            private String ResultList;
            private String StringSubList;
            private JSONObject arg1;
            private List<ContentCatalogs> contentCatalogsList;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {   // 根据返回值来对程序进行解析
                        if (ReturnType.equals("1001")) {
                            try {
                                // 获取列表
                                ResultList = result.getString("ResultInfo");
                                JSONTokener jsonParser = new JSONTokener(ResultList);
                                arg1 = (JSONObject) jsonParser.nextValue();
                                // 此处后期需要用typetoken将字符串StringSubList 转化成为一个list集合
                                StringSubList = arg1.getString("SubList");
                                Gson gson = new Gson();
                                SubList = gson.fromJson(StringSubList, new TypeToken<List<ContentInfo>>(){}.getType());
                                ContentInfo contentInfo = gson.fromJson(ResultList, new TypeToken<ContentInfo>(){}.getType());
                                contentCatalogsList = contentInfo.getContentCatalogs();
                                contentDesc = arg1.getString("ContentDesc");
                                ContentImg = arg1.getString("ContentImg");
                                ContentName = arg1.getString("ContentName");
                                ContentShareURL = arg1.getString("ContentShareURL");
                                ContentFavorite = arg1.getString("ContentFavorite");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (SubList != null && SubList.size() > 0) {
                                if (ContentFavorite != null && !ContentFavorite.equals("")) {
                                    if (ContentFavorite.equals("0")) {
                                        tv_favorite.setText("喜欢");
                                        imgFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_like));
                                    } else {
                                        tv_favorite.setText("已喜欢");
                                        imgFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_liked));
                                    }
                                }
                                if ( ContentName != null && ! ContentName.equals("")) {
                                    textAnchor.setText(ContentName);
                                } else {
                                    textAnchor.setText("我听我享听");
                                }
                                if (ContentImg == null || ContentImg.equals("")) {
                                    Bitmap bmp = BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx);
                                    img_album.setImageBitmap(bmp);
                                } else {
                                    String url;
                                    if (ContentImg.startsWith("http")) {
                                        url = ContentImg;
                                    } else {
                                        url = GlobalConfig.imageurl + ContentImg;
                                    }
                                    Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_album);
                                }
                                if (contentDesc != null && !contentDesc.equals("") && !contentDesc.equals("null")) {
                                    textContent.setText(contentDesc);
                                } else {
                                    textContent.setText("暂无介绍内容");
                                }
                                // 标签设置
                                if (contentCatalogsList != null && contentCatalogsList.size() > 0) {
                                    StringBuilder builder = new StringBuilder();
                                    for (int i = 0; i < contentCatalogsList.size(); i++) {
                                        String str = contentCatalogsList.get(i).getCataTitle();
                                        builder.append(str);
                                        if (i != contentCatalogsList.size() - 1) {
                                            builder.append("  ");
                                        }
                                    }
                                    textLabel.setText(builder.toString());
                                }
                            }
                        }else if (ReturnType.equals("0000")) {
                            ToastUtils.show_always(context, "无法获取相关的参数");
                        } else if (ReturnType.equals("1002")) {
                            ToastUtils.show_always(context, "无此分类信息");
                        } else if (ReturnType.equals("1003")) {
                            ToastUtils.show_always(context, "无法获得列表");
                        } else if (ReturnType.equals("1011")) {
                            ToastUtils.show_always(context, "列表为空（列表为空[size==0]");
                        } else if (ReturnType.equals("T")) {
                            ToastUtils.show_always(context, "获取列表异常");
                        }
                    } else {
                        ToastUtils.show_always(context, "获取列表异常");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
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
        imageHead = null;
        textAnchor = null;
        textContent = null;
        textLabel = null;
        imageConcern = null;
        dialog = null;
        linearConcern = null;
        SubList = null;
        contentDesc = null;
        textConcern = null;
    }

}
