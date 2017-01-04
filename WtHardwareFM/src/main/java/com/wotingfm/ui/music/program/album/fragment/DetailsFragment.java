package com.wotingfm.ui.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
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
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.album.model.ContentCatalogs;
import com.wotingfm.ui.music.program.album.model.ContentInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.RoundImageView;

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
    private RoundImageView imageHead;
    private TextView textAnchor, textContent, textLabel,textConcern;
    private ImageView imageConcern;
    private Dialog dialog;
    private String contentDesc;
    private String tag = "DETAILS_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private boolean isConcern;


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
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取数据");
                send();
            } else {
                ToastUtils.show_short(context, "网络失败，请检查网络");
            }
        }
        return rootView;
    }

    /**
     * 初始化控件
     */
    private void findView(View view) {
        imageHead = (RoundImageView) view.findViewById(R.id.round_image_head);    //圆形头像
        textAnchor = (TextView) view.findViewById(R.id.text_anchor_name);        //节目名
        textContent = (TextView) view.findViewById(R.id.text_content);            //内容介绍
        textLabel = (TextView) view.findViewById(R.id.text_label);                //标签
        imageConcern = (ImageView) view.findViewById(R.id.image_concern);        //关注
        textConcern = (TextView) view.findViewById(R.id.text_concern);
        LinearLayout linearConcern = (LinearLayout) view.findViewById(R.id.linear_concern);
        linearConcern.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_concern://关注
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
        }
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getContentById, tag, jsonObject, new VolleyCallback() {
            private List<ContentCatalogs> contentCatalogsList;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null) {// 根据返回值来对程序进行解析
                        if (ReturnType.equals("1001")) {
                            try {
                                // 获取列表
                                String ResultList = result.getString("ResultInfo");
                                JSONObject arg1 = (JSONObject) new JSONTokener(ResultList).nextValue();
                                Gson gson = new Gson();
                                ContentInfo contentInfo = gson.fromJson(ResultList, new TypeToken<ContentInfo>() {
                                }.getType());
                                contentCatalogsList = contentInfo.getContentCatalogs();
                                try {
                                    contentDesc = arg1.getString("ContentDescn");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    AlbumActivity.ContentImg = arg1.getString("ContentImg");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    AlbumActivity.ContentName = arg1.getString("ContentName");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    AlbumActivity.ContentShareURL = arg1.getString("ContentShareURL");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    AlbumActivity.ContentFavorite = arg1.getString("ContentFavorite");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            AlbumActivity.returnResult = 1;
                            if (AlbumActivity.ContentFavorite != null && !AlbumActivity.ContentFavorite.equals("")) {
                                if (AlbumActivity.ContentFavorite.equals("0")) {
                                    AlbumActivity.tv_favorite.setText("喜欢");
                                    AlbumActivity.imageFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_like));
                                } else {
                                    AlbumActivity.tv_favorite.setText("已喜欢");
                                    AlbumActivity.imageFavorite.setImageDrawable(context.getResources().getDrawable(R.mipmap.wt_img_liked));
                                }
                            }

                            if (AlbumActivity.ContentName != null && !AlbumActivity.ContentName.equals("")) {
                                AlbumActivity.tv_album_name.setText(AlbumActivity.ContentName);
                                textAnchor.setText(AlbumActivity.ContentName);
                            } else {
                                textAnchor.setText("我听我享听");
                            }

                            if (AlbumActivity.ContentImg == null || AlbumActivity.ContentImg.equals("")) {
                                AlbumActivity.img_album.setImageResource(R.mipmap.wt_image_playertx);
                            } else {
                                String url;
                                if (AlbumActivity.ContentImg.startsWith("http")) {
                                    url = AlbumActivity.ContentImg;
                                } else {
                                    url = GlobalConfig.imageurl + AlbumActivity.ContentImg;
                                }
                                url= AssembleImageUrlUtils.assembleImageUrl150(url);
                                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(AlbumActivity.img_album);
                                Picasso.with(context).load(url.replace("\\/", "/")).resize(100, 100).centerCrop().into(imageHead);
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
                        } else {
                            if (ReturnType.equals("0000")) {
//                                ToastUtils.show_always(context, "无法获取相关的参数");
                                ToastUtils.show_always(context, "出错了，请您稍后再试");
                            } else if (ReturnType.equals("1002")) {
//                                ToastUtils.show_always(context, "无此分类信息");
                                ToastUtils.show_always(context, "出错了，请您稍后再试");
                            } else if (ReturnType.equals("1003")) {
//                                ToastUtils.show_always(context, "无法获得列表");
                                ToastUtils.show_always(context, "出错了，请您稍后再试");
                            } else if (ReturnType.equals("1011")) {
//                                ToastUtils.show_always(context, "列表为空（列表为空[size==0]");
                                ToastUtils.show_always(context, "出错了，请您稍后再试");
                            } else if (ReturnType.equals("T")) {
//                                ToastUtils.show_always(context, "获取列表异常");
                                ToastUtils.show_always(context, "出错了，请您稍后再试");
                            }
                        }
                    } else {
                        ToastUtils.show_always(context, "出错了，请您稍后再试");
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
        contentDesc = null;
        textConcern = null;
    }
}
