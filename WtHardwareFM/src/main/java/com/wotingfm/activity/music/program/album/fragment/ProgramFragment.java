package com.wotingfm.activity.music.program.album.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.common.main.MainActivity;
import com.wotingfm.activity.music.common.service.DownloadService;
import com.wotingfm.activity.music.download.dao.FileInfoDao;
import com.wotingfm.activity.music.download.model.FileInfo;
import com.wotingfm.activity.music.main.HomeActivity;
import com.wotingfm.activity.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.activity.music.player.fragment.PlayerFragment;
import com.wotingfm.activity.music.player.model.PlayerHistory;
import com.wotingfm.activity.music.program.album.activity.AlbumActivity;
import com.wotingfm.activity.music.program.album.adapter.AlbumAdapter;
import com.wotingfm.activity.music.program.album.adapter.AlbumMainAdapter;
import com.wotingfm.activity.music.program.album.model.ContentInfo;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 专辑列表页
 * @author woting11
 */
public class ProgramFragment extends Fragment implements OnClickListener, OnItemClickListener {
    private Context context;
    private FileInfoDao fileInfoDao;
    private SearchPlayerHistoryDao dbDao;
    private AlbumMainAdapter mainAdapter;
    private AlbumAdapter adapter;

    private View rootView;
    private Dialog dialog;
    private ListView listAlbum;             // 节目列表
    private ListView listDownload;          // 下载列表
    private TextView textSum, textTotal;
    private LinearLayout linearStatus;
    private ImageView imageAllSelect;       // 下载 全选
    private ImageView imageSort;            // 排序
    private ImageView imageSortDown;

    private List<ContentInfo> subListAll = new ArrayList<>();
    private List<ContentInfo> urlList = new ArrayList<>();
    private List<ContentInfo> subList;      // 请求返回的网络数据值
    private List<FileInfo> fileInfoList;

    private int sum = 0;                    // 计数项
    private String userId;
    private String sequId;
    private String sequDesc;
    private String sequName;
    private String sequImg;
    private String tag = "PROGRAM_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private boolean flag = false;           // 标记全选的按钮

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
        fileInfoDao = new FileInfoDao(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        userId = CommonUtils.getUserId(context);
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_program, container, false);
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
        listAlbum = (ListView) view.findViewById(R.id.lv_album);            // 专辑显示界面
        listAlbum.setOnItemClickListener(this);

        listDownload = (ListView) view.findViewById(R.id.lv_download);      // 下载列表
        listDownload.setOnItemClickListener(new MyItemListener());

        imageSort = (ImageView) view.findViewById(R.id.img_sort);           // 排序
        imageSort.setOnClickListener(this);

        imageSortDown = (ImageView) view.findViewById(R.id.img_sort_down);
        imageSortDown.setOnClickListener(this);

        view.findViewById(R.id.img_download).setOnClickListener(this);
        view.findViewById(R.id.tv_quxiao).setOnClickListener(this);         // 取消动画
        view.findViewById(R.id.lin_quanxuan).setOnClickListener(this);      // 全部选择
        view.findViewById(R.id.tv_download).setOnClickListener(this);       // 开始下载

        imageAllSelect = (ImageView) view.findViewById(R.id.img_quanxuan);
        textSum = (TextView) view.findViewById(R.id.tv_sum);                // 计数项
        linearStatus = (LinearLayout) view.findViewById(R.id.lin_status2);  // 第二种状态
        textTotal = (TextView) view.findViewById(R.id.text_total);          // 下载列表的总计
    }

    // ListView 的 Item 的监听事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (subList != null && subList.get(position) != null && subList.get(position).getMediaType() != null) {
            String MediaType = subList.get(position).getMediaType();
            if (MediaType.equals("RADIO") || MediaType.equals("AUDIO")) {
                String playername = subList.get(position).getContentName();
                String playerimage = subList.get(position).getContentImg();
                String playerurl = subList.get(position).getContentPlay();
                String playerurI = subList.get(position).getContentURI();
                String playermediatype = subList.get(position).getMediaType();
                String playcontentshareurl = subList.get(position).getContentShareURL();
                String contentid = subList.get(position).getContentId();
                String plaplayeralltime = "0";
                String playerintime = "0";
                String playercontentdesc = subList.get(position).getContentDesc();
                String playernum = subList.get(position).getPlayCount();
                String playerzantype = "0";
                String playerfrom = "";
                String playerfromid = "";
                String playerfromurl = "";
                String playeraddtime = Long.toString(System.currentTimeMillis());
                String bjuserid = CommonUtils.getUserId(context);
                String ContentFavorite = subList.get(position).getContentFavorite();
                String localurl = subList.get(position).getLocalurl();

                // 如果该数据已经存在数据库则删除原有数据，然后添加最新数据
                PlayerHistory history = new PlayerHistory(playername, playerimage, playerurl, playerurI,
                        playermediatype, plaplayeralltime, playerintime, playercontentdesc, playernum,
                        playerzantype, playerfrom, playerfromid, playerfromurl, playeraddtime, bjuserid,
                        playcontentshareurl, ContentFavorite, contentid, localurl, sequName, sequId, sequDesc, sequImg);

                dbDao.deleteHistory(playerurl);
                dbDao.addHistory(history);
                if (PlayerFragment.context != null) {
                    MainActivity.changeToMusic();
                    HomeActivity.UpdateViewPager();
                    PlayerFragment.SendTextRequest(subList.get(position).getContentName(), context);
                } else {
                    SharedPreferences sp = context.getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
                    SharedPreferences.Editor et = sp.edit();
                    et.putString(StringConstant.PLAYHISTORYENTER, "true");
                    et.putString(StringConstant.PLAYHISTORYENTERNEWS, subList.get(position).getContentName());
                    if (!et.commit()) {
                        L.w("数据 commit 失败!");
                    }
                    MainActivity.changeToMusic();
                    HomeActivity.UpdateViewPager();
                }
                getActivity().setResult(1);
                getActivity().finish();
            } else {
                ToastUtils.show_short(context, "暂不支持的Type类型");
            }
        }
    }


    // 获取专辑列表
    public void send() {
        VolleyRequest.RequestPost(GlobalConfig.getContentById, tag, setParam(), new VolleyCallback() {

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
                    L.v("ReturnType -- > > " + ReturnType);

                    if (ReturnType != null && ReturnType.equals("1001")) {
                        JSONObject arg1 = (JSONObject) new JSONTokener(result.getString("ResultInfo")).nextValue();

                        try {
                            sequDesc = arg1.getString("ContentDesc");
                            sequId = arg1.getString("ContentId");
                            sequImg = arg1.getString("ContentImg");
                            sequName = arg1.getString("ContentName");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        subList = new Gson().fromJson(arg1.getString("SubList"), new TypeToken<List<ContentInfo>>() {}.getType());
                        if (subList != null && subList.size() > 0) {
                            subListAll.clear();
                            subListAll.addAll(subList);
                            listAlbum.setAdapter(mainAdapter = new AlbumMainAdapter(context, subList));
                            getDate();
                            listDownload.setAdapter(adapter = new AlbumAdapter(context, subListAll));
                            textTotal.setText("共" + subListAll.size() + "集");
                        }
                    } else {
                        ToastUtils.show_always(context, "获取列表失败，请稍后重试!");
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

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
//            jsonObject.put("UserId", CommonUtils.getUserId(context));
            jsonObject.put("MediaType", "SEQU");
            jsonObject.put("ContentId", AlbumActivity.id);
            jsonObject.put("Page", "1");
            jsonObject.put("PCDType", GlobalConfig.PCDType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // 获取数据
    protected void getDate() {
        fileInfoList = fileInfoDao.queryFileinfoAll(userId);
        Log.e("fileList", fileInfoList.size() + "");
        ArrayList<FileInfo> seqList = new ArrayList<>();
        if (fileInfoList != null && fileInfoList.size() > 0) {
            for (int i = 0; i < fileInfoList.size(); i++) {
                if (fileInfoList.get(i).getSequimgurl() != null && fileInfoList.get(i).getSequimgurl().equals(AlbumActivity.contentImg)) {
                    seqList.add(fileInfoList.get(i));
                }
            }
        }
        L.e("seqList", seqList.size() + "");
        if (seqList.size() > 0) {
            for (int i = 0; i < seqList.size(); i++) {
                String linShi = seqList.get(i).getUrl();
                if (linShi != null && !linShi.trim().equals("")) {
                    for (int j = 0; j < subListAll.size(); j++) {
                        if (subListAll.get(j).getContentPlay() != null && subListAll.get(j).getContentPlay().equals(linShi)) {
                            subListAll.get(j).setCheckType(3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_download:    // 显示下载列表
                if (subList.size() == 0) {
                    return;
                }
                subListAll.clear();
                subListAll.addAll(subList);
                getDate();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter = new AlbumAdapter(context, subListAll);
                    listDownload.setAdapter(adapter);
                }
                listDownload.setSelection(0);
                linearStatus.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_quxiao:        // 取消
                linearStatus.setVisibility(View.GONE);
                for (int i = 0; i < subListAll.size(); i++) {
                    if (subListAll.get(i).getCheckType() != 3) {
                        imageAllSelect.setImageResource(R.mipmap.image_not_all_check);
                        subListAll.get(i).setCheckType(1);
                    }
                }
                sum = 0;
                setSum();
                flag = false;
                break;
            case R.id.lin_quanxuan:     // 全选
                if (!flag) {            // 默认为未选中状态
                    sum = 0;
                    for (int i = 0; i < subListAll.size(); i++) {
                        if (subListAll.get(i).getCheckType() != 3) {
                            subListAll.get(i).setCheckType(2);
                            sum++;
                        }
                    }
                    flag = true;
                    imageAllSelect.setImageResource(R.mipmap.image_all_check);
                    setSum();
                } else {
                    for (int i = 0; i < subListAll.size(); i++) {
                        if (subListAll.get(i).getCheckType() != 3) {
                            subListAll.get(i).setCheckType(1);
                        }
                    }
                    flag = false;
                    imageAllSelect.setImageResource(R.mipmap.image_not_all_check);
                    sum = 0;
                    setSum();
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.tv_download:        // 下载
                urlList.clear();
                for (int i = 0; i < subListAll.size(); i++) {
                    if (subListAll.get(i).getCheckType() == 2) {
                        ContentInfo mContent = subListAll.get(i);
                        mContent.setSequdesc(AlbumActivity.contentDesc);
                        mContent.setSequname(AlbumActivity.contentName);
                        mContent.setSequimgurl(AlbumActivity.contentImg);
                        mContent.setSequid(AlbumActivity.id);
                        mContent.setUserid(userId);
                        mContent.setDownloadtype("0");
                        fileInfoDao.updatedownloadstatus(mContent.getContentPlay(), "0");// 将所有数据设置
                        urlList.add(mContent);
                    }
                }
                if (urlList.size() > 0) {
                    fileInfoDao.insertfileinfo(urlList);
                    List<FileInfo> linShiList = fileInfoDao.queryFileinfo("false", userId);// 查询表中未完成的任务
                    // 未下载列表
                    for (int kk = 0; kk < linShiList.size(); kk++) {
                        if (linShiList.get(kk).getDownloadtype() == 1) {
                            DownloadService.workStop(linShiList.get(kk));
                            fileInfoDao.updatedownloadstatus(linShiList.get(kk).getUrl(), "2");
                            L.e("测试下载问题", " 暂停下载的单体" + (linShiList.get(kk).getFileName()));
                        }
                    }
                    linShiList.get(0).setDownloadtype(1);
                    fileInfoDao.updatedownloadstatus(linShiList.get(0).getUrl(), "1");
                    L.v("数据库内数据", linShiList.toString());
                    DownloadService.workStart(linShiList.get(0));
                    // 发送更新界面数据广播
                    context.sendBroadcast(new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED));
                    linearStatus.setVisibility(View.GONE);
                } else {
                    ToastUtils.show_always(context, "请重新选择数据");
                    return;
                }
                break;
            case R.id.img_sort:
                if (subList.size() != 0 && mainAdapter != null) {
                    Collections.reverse(subList);            // 倒序
                    mainAdapter.notifyDataSetChanged();
                    imageSortDown.setVisibility(View.VISIBLE);
                    imageSort.setVisibility(View.GONE);
                }
                break;
            case R.id.img_sort_down:
                if (subList.size() != 0 && mainAdapter != null) {
                    Collections.reverse(subList);            // 倒序
                    mainAdapter.notifyDataSetChanged();
                    imageSortDown.setVisibility(View.GONE);
                    imageSort.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    protected void setSum() {
        textSum.setText(sum + "");
    }

    class MyItemListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (subListAll != null && subListAll.get(position) != null) {
                if (subListAll.get(position).getCheckType() == 3) {
                    ToastUtils.show_always(context, "已经下载过");
                } else {
                    if (subListAll.get(position).getCheckType() == 1) {
                        subListAll.get(position).setCheckType(2);
                    } else {
                        subListAll.get(position).setCheckType(1);
                    }
                    int downLoadSum = 0;
                    sum = 0;
                    for (int i = 0; i < subListAll.size(); i++) {
                        if (subListAll.get(i).getCheckType() == 2) {
                            sum++;
                        }
                        if (subListAll.get(i).getCheckType() == 3) {
                            downLoadSum++;
                        }
                        setSum();
                        adapter.notifyDataSetChanged();
                    }

                    // 更新全选图标
                    if (sum == (subListAll.size() - downLoadSum)) {
                        flag = true;
                        imageAllSelect.setImageResource(R.mipmap.image_all_check);
                    } else {
                        flag = false;
                        imageAllSelect.setImageResource(R.mipmap.image_not_all_check);
                    }
                }
            }
        }
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
        rootView = null;
        context = null;
        fileInfoDao = null;
        dbDao = null;
        dialog = null;
        listAlbum = null;
        listDownload = null;
        imageAllSelect = null;
        textSum = null;
        textTotal = null;
        linearStatus = null;
        imageSort = null;
        imageSortDown = null;
        mainAdapter = null;
        adapter = null;
        subListAll = null;
        urlList = null;
        subList = null;
        fileInfoList = null;
        userId = null;
    }
}
