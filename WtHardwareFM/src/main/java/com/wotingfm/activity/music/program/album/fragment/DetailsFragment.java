package com.wotingfm.activity.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
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

    private Dialog dialog;
    private View rootView;
//    private ImageView imageHead;
    private ImageView imageAlbum;
    private ImageView imgFavorite;
    private TextView textAnchor;        // 作者
    private TextView textContent;       // 内容
    private TextView textLabel;         // 标签
    private TextView textFavorite;      // 喜欢

    private List<ContentInfo> subList;  // 请求返回的网络数据值
    public static String contentFavorite;
    private String tag = "DETAILS_VOLLEY_REQUEST_CANCEL_TAG";
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
            findView(rootView);
        }
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取数据");
            send();
        } else {
            ToastUtils.show_short(context, "网络失败，请检查网络");
        }
        return rootView;
    }

    // 初始化控件
    private void findView(View view) {
        view.findViewById(R.id.lin_share).setOnClickListener(this);             // 分享
        view.findViewById(R.id.lin_favorite).setOnClickListener(this);          // 喜欢
        
//        imageHead = (ImageView) view.findViewById(R.id.round_image_head);       // 圆形头像
        textAnchor = (TextView) view.findViewById(R.id.text_anchor_name);       // 节目名
        textContent = (TextView) view.findViewById(R.id.text_content);          // 内容介绍
        textLabel = (TextView) view.findViewById(R.id.text_label);              // 标签
        textFavorite = (TextView) view.findViewById(R.id.tv_favorite);          // 喜欢
        imgFavorite = (ImageView) view.findViewById(R.id.img_favorite);         // 喜欢
        imageAlbum = (ImageView) view.findViewById(R.id.img_album);             // 专辑图片
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_favorite:     // 喜欢
                if (contentFavorite != null && !contentFavorite.equals("")) {
                    if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                        dialog = DialogUtils.Dialogph(context, "正在获取数据");
                        sendFavorite();
                    } else {
                        ToastUtils.show_always(context, "网络失败，请检查网络");
                    }
                } else {
                    ToastUtils.show_always(context, "专辑信息获取异常");
                }
                break;
            case R.id.lin_share:        // 分享
                AlbumActivity.shareDialog.show();
                break;
        }
    }

    // 发送网络请求  获取喜欢数据
    private void sendFavorite() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", AlbumActivity.id);
            jsonObject.put("PCDType", GlobalConfig.PCDType);
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
                    String ReturnType = result.getString("ReturnType");   // 根据返回值来对程序进行解析
                    L.v("ReturnType -- > > " + ReturnType);
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        if (contentFavorite.equals("0")) {
                            contentFavorite = "1";
                            textFavorite.setText("已喜欢");
                            imgFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_liked));
                        } else {
                            contentFavorite = "0";
                            textFavorite.setText("喜欢");
                            imgFavorite.setImageDrawable(getResources().getDrawable(R.mipmap.wt_img_like));
                        }
                    } else if (ReturnType != null && ReturnType.equals("1004")) {
                        ToastUtils.show_always(context, "所指定的节目不存在");
                    } else if (ReturnType != null && ReturnType.equals("1005")) {
                        ToastUtils.show_always(context, "已经喜欢了此内容");
                    } else if (ReturnType != null && ReturnType.equals("1006")) {
                        ToastUtils.show_always(context, "还未喜欢此内容");
                    } else {
                        try {
                            String Message = result.getString("Message");
                            ToastUtils.show_always(context, Message + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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


    // 向服务器发送请求
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

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    L.v("ReturnType -- > > " + ReturnType);

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultInfo")).nextValue();

                        // 此处后期需要用 typeToken 将字符串 StringSubList 转化成为一个 list 集合
                        Gson gson = new Gson();
                        subList = gson.fromJson(arg1.getString("SubList"), new TypeToken<List<ContentInfo>>(){}.getType());
                        ContentInfo contentInfo = gson.fromJson(result.getString("ResultInfo"), new TypeToken<ContentInfo>(){}.getType());
                        List<ContentCatalogs> contentCatalogsList = contentInfo.getContentCatalogs();

                        String contentDesc = null;
                        String contentImg = null;
                        String contentName = null;
                        try {
                            contentDesc = arg1.getString("ContentDesc");
                            contentImg = arg1.getString("ContentImg");
                            contentName = arg1.getString("ContentName");
//                        String contentShareURL = arg1.getString("ContentShareURL");
                            contentFavorite = arg1.getString("ContentFavorite");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (subList != null && subList.size() > 0) {
                            if (contentFavorite != null && !contentFavorite.equals("")) {
                                if (contentFavorite.equals("0")) {
                                    textFavorite.setText("喜欢");
                                    imgFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_like));
                                } else {
                                    textFavorite.setText("已喜欢");
                                    imgFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_liked));
                                }
                            }
                            if ( contentName != null && ! contentName.equals("")) {
                                textAnchor.setText(contentName);
                            }
                            if (contentImg != null && !contentImg.equals("")) {
                                String url;
                                if (contentImg.startsWith("http")) {
                                    url = contentImg;
                                } else {
                                    url = GlobalConfig.imageurl + contentImg;
                                }
                                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageAlbum);
                            }
                            if (contentDesc != null && !contentDesc.equals("") && !contentDesc.equals("null")) {
                                textContent.setText(contentDesc);
                            }
                            // 标签设置
                            if (contentCatalogsList != null && contentCatalogsList.size() > 0) {
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < contentCatalogsList.size(); i++) {
                                    String str = contentCatalogsList.get(i).getCataTitle();
                                    L.v(str);
                                    builder.append(str);
                                    if (i != contentCatalogsList.size() - 1) {
                                        builder.append("  ");
                                    }
                                }
                                textLabel.setText(builder.toString());
                            }
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
        textAnchor = null;
        textContent = null;
        textLabel = null;
        dialog = null;
        subList = null;
    }
}
