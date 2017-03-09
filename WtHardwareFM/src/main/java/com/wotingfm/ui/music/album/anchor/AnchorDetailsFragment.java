package com.wotingfm.ui.music.album.anchor;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.helper.CommonHelper;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.music.main.PlayerActivity;
import com.wotingfm.ui.music.main.ProgramActivity;
import com.wotingfm.ui.music.album.anchor.adapter.AnchorMainAdapter;
import com.wotingfm.ui.music.album.anchor.adapter.AnchorSequAdapter;
import com.wotingfm.ui.music.album.anchor.main.AnchorListFragment;
import com.wotingfm.ui.music.album.anchor.model.PersonInfo;
import com.wotingfm.ui.music.album.main.AlbumFragment;
import com.wotingfm.ui.music.search.main.SearchLikeActivity;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.HeightListView;
import com.wotingfm.widget.RoundImageView;
import com.wotingfm.widget.TipView;
import com.wotingfm.widget.xlistview.XListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 主播详情界面
 */
public class AnchorDetailsFragment extends Fragment implements View.OnClickListener, TipView.WhiteViewClick {
    private List<PersonInfo> MediaInfoList;
    private List<PersonInfo> personInfoList;
    private AnchorSequAdapter adapterSequ;
    private AnchorMainAdapter adapterMain;

    private Dialog dialog;
    private XListView listAnchor;
    private RoundImageView img_head;
    private ListView lv_sequ;

    private TextView id_sequ;
    private TextView tv_descn;
    private TextView tv_visible_all;
    private TextView tv_more;
    private TextView textAnchorName;
    private TipView tipView;// 没有网络、没有数据、数据出错提示

    private int page = 1;
    private String ContentPub;
    private String PersonId;
    private String PersonName;
    private String PersonDescn;
    private String PersonImg;
    private String tag = "ANCHOR_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private FragmentActivity context;
    private View rootView;
    private String jump_type;

    @Override
    public void onWhiteViewClick() {
        if (!TextUtils.isEmpty(PersonId)) {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取数据");
                send();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_anchor_details, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            initView();
            handleIntent();
        }
        return rootView;
    }

    // 初始化视图
    private void initView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);

        View headView = LayoutInflater.from(context).inflate(R.layout.headview_activity_anchor_details, null);
        img_head = (RoundImageView) headView.findViewById(R.id.round_image_head);   //  头像
        id_sequ = (TextView) headView.findViewById(R.id.id_sequ);                   //  专辑数
        tv_descn = (TextView) headView.findViewById(R.id.text_introduce);           //  介绍
        lv_sequ = (ListView) headView.findViewById(R.id.list_sequ);                 //  专辑列表
        tv_visible_all = (TextView) headView.findViewById(R.id.text_visible_all);
        tv_more = (TextView) headView.findViewById(R.id.tv_more);                   //  更多

        listAnchor = (XListView) rootView.findViewById(R.id.list_anchor);                    // 主播的节目列表
        listAnchor.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listAnchor.setHeaderDividersEnabled(false);
        listAnchor.addHeaderView(headView);

        textAnchorName = (TextView) rootView.findViewById(R.id.text_anchor_name);// 标题  即主播 Name
  /*      textAnchorName.setOnClickListener(this);*/
        initEvent();
    }

    // 初始化点击事件
    private void initEvent() {
        tipView.setWhiteClick(this);
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);// 返回
        tv_more.setOnClickListener(this);

        listAnchor.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                if (CommonHelper.checkNetwork(context)) {
                    dialog = DialogUtils.Dialogph(context, "正在获取数据");
                    listAnchor.stopRefresh();
                    page = 1;
                    send();
                } else {
                    listAnchor.stopRefresh();
                }
            }

            @Override
            public void onLoadMore() {
                if (CommonHelper.checkNetwork(context)) {
                    dialog = DialogUtils.Dialogph(context, "正在获取数据");
                    listAnchor.stopLoadMore();
                    page++;
                    getMediaContents();
                } else {
                    listAnchor.stopLoadMore();
                }
            }
        });
    }

    private void handleIntent() {
        jump_type=getArguments().getString(StringConstant.JUMP_TYPE);
        PersonId = getArguments().getString("PersonId");
        ContentPub = getArguments().getString("ContentPub");
        if (!TextUtils.isEmpty(PersonId)) {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取数据");
                send();
            } else {
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.NO_NET);
            }
        } else {
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.IS_ERROR);
        }
    }

    private void send() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PersonId", PersonId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getPersonInfo, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        try {
                            PersonName = result.getString("PersonName");
                            if (!TextUtils.isEmpty(PersonName) && !PersonName.equals("null")) {
                                textAnchorName.setText(PersonName);
                            } else {
                                textAnchorName.setText("未知");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            PersonDescn = result.getString("PersonDescn");
                            if (!TextUtils.isEmpty(PersonDescn) && !PersonDescn.equals("null")) {
                                tv_descn.setText(PersonDescn);
                            } else {
                                tv_descn.setText("暂无简介");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            PersonImg = result.getString("PersonImg");
                            if (TextUtils.isEmpty(PersonImg)) {
                                img_head.setImageBitmap(BitmapUtils.readBitMap(context, R.mipmap.wt_image_playertx));
                            } else if (!PersonImg.startsWith("http")) {
                                PersonImg = GlobalConfig.imageurl + PersonImg;
                                PersonImg = AssembleImageUrlUtils.assembleImageUrl180(PersonImg);
                                Picasso.with(context).load(PersonImg.replace("\\/", "/")).resize(100, 100).centerCrop().into(img_head);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Gson gson = new Gson();
                        try {
                            String SeqList = result.getString("SeqMediaList");
                            personInfoList = gson.fromJson(SeqList, new TypeToken<List<PersonInfo>>() {}.getType());
                            if (personInfoList != null && personInfoList.size() > 0) {
                                // 此处要对 lv_sequ 的高度进行适配
                                adapterSequ = new AnchorSequAdapter(context, personInfoList);
                                lv_sequ.setAdapter(adapterSequ);
                                id_sequ.setText("专辑(" + personInfoList.size() + ")");
                                new HeightListView(context).setListViewHeightBasedOnChildren(lv_sequ);
                            } else {
                                lv_sequ.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            String MediaList = result.getString("MediaAssetList");
                            MediaInfoList = gson.fromJson(MediaList, new TypeToken<List<PersonInfo>>() {}.getType());
                            if (MediaInfoList != null && MediaInfoList.size() > 0) {
                                // listAnchor
                                adapterMain = new AnchorMainAdapter(context, MediaInfoList);
                                listAnchor.setAdapter(adapterMain);
                                if (MediaInfoList.size() < 10) {
                                    listAnchor.setPullLoadEnable(false);
                                } else {
                                    listAnchor.setPullLoadEnable(true);
                                }
                            } else {
                                listAnchor.setPullLoadEnable(false);
                            }
                            listAnchor.setPullRefreshEnable(true);
                            setItemListener();
                            tipView.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        listAnchor.stopRefresh();
                        listAnchor.setPullLoadEnable(false);
                        listAnchor.setPullRefreshEnable(true);
                        tipView.setVisibility(View.VISIBLE);
                        tipView.setTipView(TipView.TipStatus.IS_ERROR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    listAnchor.stopRefresh();
                    listAnchor.setPullLoadEnable(false);
                    listAnchor.setPullRefreshEnable(true);
                    tipView.setVisibility(View.VISIBLE);
                    tipView.setTipView(TipView.TipStatus.IS_ERROR);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                tipView.setVisibility(View.VISIBLE);
                tipView.setTipView(TipView.TipStatus.IS_ERROR);
            }
        });
    }

    private void setItemListener() {
        // 跳到专辑
        lv_sequ.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlbumFragment fg = new AlbumFragment();
                Bundle bundle=new Bundle();
                bundle.putString("type", "main");
                bundle.putString(StringConstant.JUMP_TYPE,jump_type);
                bundle.putString("id", personInfoList.get(position).getContentId());
                fg.setArguments(bundle);
                if(jump_type!=null){
                    if( jump_type.equals("search")){
                        SearchLikeActivity.open(fg);
                    }else if( jump_type.equals("program")){
                        ProgramActivity.open(fg);
                    }else if( jump_type.equals("play")){
                        PlayerActivity.open(fg);
                    }
                }
            }
        });
        // 跳到单体
        listAnchor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToastUtils.show_always(context, MediaInfoList.get(position - 2).getContentName());
             /*   String playername = MediaInfoList.get(position - 2).getContentName();
                String playerimage = MediaInfoList.get(position - 2).getContentImg();
                String playerurl = MediaInfoList.get(position - 2).getContentPlay();
                String playerurI = MediaInfoList.get(position - 2).getContentURI();
                String playermediatype = MediaInfoList.get(position - 2).getMediaType();
                String playerContentShareUrl = MediaInfoList.get(position - 2).getContentShareURL();
                String plaplayeralltime =MediaInfoList.get(position - 2).getContentTimes();
                String playerintime = "0";
                String playercontentdesc = newList.get(position - 2).getContentDescn();
                String playernum = newList.get(position - 2).getPlayCount();
                String playerzantype = "0";
                String playerfrom = newList.get(position - 2).getContentPub();
                String playerfromid = "";
                String playerfromurl = "";
                String playeraddtime = Long.toString(System.currentTimeMillis());
                String bjuserid = CommonUtils.getUserId(context);
                String ContentFavorite = newList.get(position - 2).getContentFavorite();
                String ContentId = newList.get(position - 2).getContentId();
                String localurl = newList.get(position - 2).getLocalurl();

                String sequName = newList.get(position - 2).getSequName();
                String sequId = newList.get(position - 2).getSequId();
                String sequDesc = newList.get(position - 2).getSequDesc();
                String sequImg = newList.get(position - 2).getSequImg();
                String ContentPlayType= newList.get(position-2).getContentPlayType();

                //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                PlayerHistory history = new PlayerHistory(
                        playername, playerimage, playerurl, playerurI, playermediatype,
                        plaplayeralltime, playerintime, playercontentdesc, playernum,
                        playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid, playerContentShareUrl,
                        ContentFavorite, ContentId, localurl, sequName, sequId, sequDesc, sequImg,ContentPlayType);
                dbDao.deleteHistory(playerurl);
                dbDao.addHistory(history);
                HomeActivity.UpdateViewPager();
                PlayerActivity.TextPage=1;
                Intent push=new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                Bundle bundle1=new Bundle();
                bundle1.putString("text",newList.get(position - 2).getContentName());
                push.putExtras(bundle1);
                context.sendBroadcast(push);*/
            }
        });
    }

    private void getMediaContents() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("PersonId", PersonId);
            jsonObject.put("Page", String.valueOf(page));
            jsonObject.put("MediaType", "AUDIO");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyRequest.RequestPost(GlobalConfig.getPersonContents, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    if (ReturnType != null && ReturnType.equals("1001")) {
                        String MediaList = result.getString("ResultList");
                        List<PersonInfo> ResultList = new Gson().fromJson(MediaList, new TypeToken<List<PersonInfo>>() {}.getType());
                        if (ResultList != null && ResultList.size() > 0) {
                            MediaInfoList.addAll(ResultList);
                            if (ResultList.size() < 10) {
                                listAnchor.stopLoadMore();
                                listAnchor.setPullLoadEnable(false);
                                listAnchor.setPullRefreshEnable(true);
                            }
                            if (adapterMain == null) {
                                adapterMain = new AnchorMainAdapter(context, MediaInfoList);
                            } else {
                                adapterMain.notifyDataSetChanged();
                            }
                        } else {
                            listAnchor.stopLoadMore();
                            listAnchor.setPullLoadEnable(false);
                            listAnchor.setPullRefreshEnable(true);
                            ToastUtils.show_always(context, "已经没有更多数据了");
                        }
                    } else {
                        ToastUtils.show_always(context, "出错了，请您稍后再试");
                        listAnchor.stopLoadMore();
                        listAnchor.setPullLoadEnable(false);
                        listAnchor.setPullRefreshEnable(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show_always(context, "出错了，请您稍后再试");
                    listAnchor.stopLoadMore();
                    listAnchor.setPullLoadEnable(false);
                    listAnchor.setPullRefreshEnable(true);
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                listAnchor.stopLoadMore();
                listAnchor.setPullLoadEnable(false);
                listAnchor.setPullRefreshEnable(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:// 返回
                if(jump_type!=null){
                    if(jump_type.equals("search")){
                        SearchLikeActivity.close();
                    }else if(jump_type.equals("program")){
                        ProgramActivity.close();
                    }else if(jump_type.equals("play")){
                        PlayerActivity.close();
                    }
                }
                break;
            case R.id.tv_more:
                if (!TextUtils.isEmpty(PersonId)) {
                    AnchorListFragment fg = new AnchorListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("PersonId", PersonId);
                    bundle.putString(StringConstant.JUMP_TYPE, AlbumFragment.jump_type);
                    if (!TextUtils.isEmpty(PersonName)) {
                        bundle.putString("PersonName", PersonName);
                    }
                    fg.setArguments(bundle);
                    ProgramActivity.open(fg);
                } else {
                    ToastUtils.show_always(context, "该主播还没有详细的个人信息~");
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }
}
