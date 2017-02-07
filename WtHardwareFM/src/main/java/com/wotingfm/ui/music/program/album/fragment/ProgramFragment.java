package com.wotingfm.ui.music.program.album.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.ui.music.common.service.DownloadService;
import com.wotingfm.ui.music.download.activity.DownloadActivity;
import com.wotingfm.ui.music.download.dao.FileInfoDao;
import com.wotingfm.ui.music.download.fragment.DownLoadUnCompleted;
import com.wotingfm.ui.music.download.model.FileInfo;
import com.wotingfm.ui.music.main.HomeActivity;
import com.wotingfm.ui.music.main.dao.SearchPlayerHistoryDao;
import com.wotingfm.ui.music.player.model.PlayerHistory;
import com.wotingfm.ui.music.program.album.activity.AlbumActivity;
import com.wotingfm.ui.music.program.album.adapter.AlbumAdapter;
import com.wotingfm.ui.music.program.album.adapter.AlbumMainAdapter;
import com.wotingfm.ui.music.program.album.model.ContentInfo;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;
import com.wotingfm.widget.TipView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 专辑列表页
 * @author woting11
 */
public class ProgramFragment extends Fragment implements OnClickListener {
    private Context context;
    private List<ContentInfo> contentList;// 列表
    private List<ContentInfo> downLoadList = new ArrayList<>();// 下载列表
    private List<ContentInfo> subList = new ArrayList<>();
    private AlbumMainAdapter albumMainAdapter;
    private AlbumAdapter albumAdapter;

    private View rootView;
    private TextView textTotal;// 总共集数
    private ImageView imageSort;// 排序
    private ImageView imageSortDown;// 倒序

    private ListView listAlbum;// 专辑列表

    private TipView tipView;// 没有数据提示
    private View viewStatus;// 下载状态
    private TextView textSum;// 选择下载的数量
    private ListView listDownload;// 下载列表
    private ImageView imageAllCheck;// 全选图标

    private boolean isInitData;// 是否初始化数据
    private boolean isInitView;// 是否初始化界面
    private boolean flag;// 是否全选
    private int sum;// 选择的数量

    private SearchPlayerHistoryDao dbDao;
    private FileInfoDao FID;

    // 初始化数据库命令执行对象
    private void initDao() {
        dbDao = new SearchPlayerHistoryDao(context);
        FID = new FileInfoDao(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_album_program, container, false);
            initView();
        }
        return rootView;
    }

    // 初始化控件
    private void initView() {
        tipView = (TipView) rootView.findViewById(R.id.tip_view);// 没有数据提示

        textTotal = (TextView) rootView.findViewById(R.id.text_total);// 总共集数

        imageSort = (ImageView) rootView.findViewById(R.id.img_sort);// 排序
        imageSort.setOnClickListener(this);

        imageSortDown = (ImageView) rootView.findViewById(R.id.img_sort_down);// 倒序
        imageSortDown.setOnClickListener(this);

        rootView.findViewById(R.id.img_download).setOnClickListener(this);// 下载

        listAlbum = (ListView) rootView.findViewById(R.id.lv_album);// 展示专辑列表

        viewStatus = rootView.findViewById(R.id.lin_status2);// 下载状态
        textSum = (TextView) rootView.findViewById(R.id.tv_sum);// 选择下载的数量
        rootView.findViewById(R.id.tv_quxiao).setOnClickListener(this);// 取消下载
        listDownload = (ListView) rootView.findViewById(R.id.lv_download);// 下载列表
        imageAllCheck = (ImageView) rootView.findViewById(R.id.img_quanxuan);// 全选图标
        rootView.findViewById(R.id.lin_quanxuan).setOnClickListener(this);// 全选
        rootView.findViewById(R.id.tv_download).setOnClickListener(this);// 开始下载

        setListener();

        isInitView = true;

        contentList = ((AlbumActivity) context).getContentList();// 获取列表
        initData(contentList);
    }

    // 初始化数据
    public void initData(List<ContentInfo> contentList) {
        if (isInitData || !isInitView || contentList == null || contentList.size() <= 0) return;
        isInitData = true;

        // 总共集数
        int size = contentList.size();
        textTotal.setText("列表 (共" + size + "集)");

        // 列表
        this.contentList = contentList;
        if(size <= 0) {// 没有数据
            tipView.setVisibility(View.VISIBLE);
            tipView.setTipView(TipView.TipStatus.NO_DATA, "数据君不翼而飞了\n点击界面会重新获取数据哟");
        } else {
            tipView.setVisibility(View.GONE);
            listAlbum.setAdapter(albumMainAdapter = new AlbumMainAdapter(context, this.contentList));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_download:// 显示下载列表
                if (contentList.size() == 0) return;
                downLoadList.clear();
                downLoadList.addAll(contentList);
                getData();
                if (albumAdapter != null) albumAdapter.notifyDataSetChanged();
                else listDownload.setAdapter(albumAdapter = new AlbumAdapter(context, downLoadList));
                setInterface();
                listDownload.setSelection(0);
                viewStatus.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_quxiao:// 取消
                viewStatus.setVisibility(View.GONE);
                for (int i = 0; i < downLoadList.size(); i++) {
                    if (downLoadList.get(i).getCheckType() != 3) {
                        imageAllCheck.setImageResource(R.mipmap.image_not_all_check);
                        downLoadList.get(i).setCheckType(1);
                    }
                }
                sum = 0;
                setSum();
                flag = false;
                break;
            case R.id.lin_quanxuan:// 全选
                if (!flag) {// 默认为未选中状态
                    sum = 0;
                    for (int i = 0; i < downLoadList.size(); i++) {
                        if (downLoadList.get(i).getCheckType() != 3) {
                            downLoadList.get(i).setCheckType(2);
                            sum++;
                        }
                    }
                    flag = true;
                    imageAllCheck.setImageResource(R.mipmap.wt_group_checked);
                    setSum();
                } else {
                    for (int i = 0; i < downLoadList.size(); i++) {
                        if (downLoadList.get(i).getCheckType() != 3)
                            downLoadList.get(i).setCheckType(1);
                    }
                    flag = false;
                    imageAllCheck.setImageResource(R.mipmap.wt_group_nochecked);
                    sum = 0;
                    setSum();
                }
                albumAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_download:// 下载
                download();
                break;
            case R.id.img_sort:// 排序
                if (contentList.size() != 0 && albumMainAdapter != null) {
                    Collections.reverse(contentList);
                    albumMainAdapter.notifyDataSetChanged();
                    imageSortDown.setVisibility(View.VISIBLE);
                    imageSort.setVisibility(View.GONE);
                }
                break;
            case R.id.img_sort_down:// 倒序
                if (contentList.size() != 0 && albumMainAdapter != null) {
                    Collections.reverse(contentList);
                    albumMainAdapter.notifyDataSetChanged();
                    imageSortDown.setVisibility(View.GONE);
                    imageSort.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    // 获取数据
    private void getData() {
        List<FileInfo> fileList = FID.queryFileInfoAll(CommonUtils.getUserId(context));
        ArrayList<FileInfo> seqList = new ArrayList<>();
        ArrayList<String> stringList = new ArrayList<>();

        // 判断列表中已经下载过的数据
        if (fileList != null && fileList.size() > 0) {
            for (int i = 0; i < contentList.size(); i++) {
                stringList.add(contentList.get(i).getContentImg());
            }
            String cId;
            for (int i = 0; i < fileList.size(); i++) {
                cId = fileList.get(i).getSequimgurl();
                if (cId != null && !stringList.contains(cId)) {
                    seqList.add(fileList.get(i));
                }
            }
            stringList.clear();
        }
        if (seqList.size() <= 0) return;
        for (int i = 0; i < seqList.size(); i++) {
            String temp = seqList.get(i).getUrl();
            if (temp != null && !temp.trim().equals("")) {
                for (int j = 0; j < downLoadList.size(); j++) {
                    if (downLoadList.get(j).getContentPlay() != null && downLoadList.get(j).getContentPlay().equals(temp)) {
                        downLoadList.get(j).setCheckType(3);
                    }
                }
            }
        }
    }

    private String contentDesc;
    private String contentName;
    private String contentImg;
    private String contentId;

    // 获取专辑下载需要的信息
    public void setInfo(String contentDesc, String contentName, String contentImg, String contentId) {
        this.contentDesc = contentDesc;
        this.contentName = contentName;
        this.contentImg = contentImg;
        this.contentId = contentId;
    }

    // 下载
    private void download() {
        subList.clear();
        for (int i = 0; i < downLoadList.size(); i++) {
            if (downLoadList.get(i).getCheckType() == 2) {
                ContentInfo mContent = downLoadList.get(i);
                mContent.setSequdesc(contentDesc);
                mContent.setSequname(contentName);
                mContent.setSequimgurl(contentImg);
                mContent.setSequid(contentId);
                // 判断 userId 是否为空
                mContent.setUserid(CommonUtils.getUserId(context));
                mContent.setDownloadtype("0");
                FID.updataDownloadStatus(mContent.getContentPlay(), "0");// 将所有数据设置
                subList.add(mContent);
            }
        }
        if (subList.size() > 0) {
            FID.insertFileInfo(subList);
            List<FileInfo> tempList = FID.queryFileInfo("false", CommonUtils.getUserId(context));// 查询表中未完成的任务
            // 未下载列表
            for (int kk = 0; kk < tempList.size(); kk++) {
                if (tempList.get(kk).getDownloadtype() == 1) {
                    DownloadService.workStop(tempList.get(kk));
                    FID.updataDownloadStatus(tempList.get(kk).getUrl(), "2");
                }
            }
            ToastUtils.show_always(context, "已将选中条目加载下载列表");
            tempList.get(0).setDownloadtype(1);
            FID.updataDownloadStatus(tempList.get(0).getUrl(), "1");
            DownloadService.workStart(tempList.get(0));
            if(DownloadActivity.isVisible==true){
                      /*  if(DownLoadUnCompleted.dwType!){

                        }*/
                DownLoadUnCompleted.dwType=true;
            }

            // 发送更新界面数据广播
            Intent pushIntent = new Intent(BroadcastConstants.PUSH_DOWN_UNCOMPLETED);
            context.sendBroadcast(pushIntent);
            viewStatus.setVisibility(View.GONE);
        } else {
            ToastUtils.show_always(context, "请重新选择数据");
        }
    }

    // ListView 的 Item 的监听事件
    private void setListener() {
        listAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (contentList != null && contentList.get(position) != null && contentList.get(position).getMediaType() != null) {
                    String MediaType = contentList.get(position).getMediaType();
                    if (MediaType.equals(StringConstant.TYPE_RADIO) || MediaType.equals(StringConstant.TYPE_AUDIO)) {
                        String playerName = contentList.get(position).getContentName();
                        String playerImage = contentList.get(position).getContentImg();
                        String playUrl = contentList.get(position).getContentPlay();
                        String playUrI = contentList.get(position).getContentURI();
                        String playMediaType = contentList.get(position).getMediaType();
                        String playContentShareUrl = contentList.get(position).getContentShareURL();
                        String ContentId = contentList.get(position).getContentId();
                        String playAllTime = contentList.get(position).getContentTimes();
                        String playInTime = "0";
                        String playContentDesc = contentList.get(position).getContentDescn();
                        String playNum = contentList.get(position).getPlayCount();
                        String playZanType = "0";
                        String playFrom = contentList.get(position).getContentPub();
                        String playFromId = "";
                        String playFromUrl = "";
                        String playAddTime = Long.toString(System.currentTimeMillis());
                        String bjUserId = CommonUtils.getUserId(context);
                        String ContentFavorite = contentList.get(position).getContentFavorite();
                        String localUrl = contentList.get(position).getLocalurl();
                        // name id desc img
                        String sequName1 = contentName;
                        String sequId1 = contentId;
                        String sequDesc1 = contentDesc;
                        String sequImg1 = contentImg;

                        PlayerHistory history = new PlayerHistory(
                                playerName,  playerImage, playUrl, playUrI,playMediaType,
                                playAllTime, playInTime, playContentDesc, playNum,
                                playZanType, playFrom , playFromId,playFromUrl,playAddTime,bjUserId,playContentShareUrl,
                                ContentFavorite,ContentId,localUrl,sequName1,sequId1,sequDesc1,sequImg1);
                        dbDao.deleteHistory(playUrl);
                        dbDao.addHistory(history);
                        MainActivity.changeToMusic();
                        HomeActivity.UpdateViewPager();
                        Intent push = new Intent(BroadcastConstants.PLAY_TEXT_VOICE_SEARCH);
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(StringConstant.TEXT_CONTENT,contentList.get(position).getContentName());
                        push.putExtras(bundle1);
                        context.sendBroadcast(push);
                        getActivity().setResult(1);
                        getActivity().finish();
                    } else {
                        ToastUtils.show_always(context, "暂不支持播放");
                    }
                }
            }
        });
    }

    // 实现接口的方法
    private void setInterface() {
        listDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (downLoadList != null && downLoadList.get(position) != null) {
                    if (downLoadList.get(position).getCheckType() == 3) {
                        L.w("TAG", "已经下载过");
                    } else {
                        if (downLoadList.get(position).getCheckType() == 1) {
                            downLoadList.get(position).setCheckType(2);
                        } else {
                            downLoadList.get(position).setCheckType(1);
                        }
                        int downLoadSum = 0;
                        sum = 0;
                        for (int i = 0; i < downLoadList.size(); i++) {
                            if (downLoadList.get(i).getCheckType() == 2) {
                                sum++;
                            } else if(downLoadList.get(i).getCheckType() == 3) {
                                downLoadSum++;
                            }
                            setSum();
                            albumAdapter.notifyDataSetChanged();
                        }

                        // 更新全选图标
                        if(sum == (downLoadList.size() - downLoadSum)){
                            flag = true;
                            imageAllCheck.setImageResource(R.mipmap.wt_group_checked);
                        }else{
                            flag = false;
                            imageAllCheck.setImageResource(R.mipmap.wt_group_nochecked);
                        }
                    }
                }
            }
        });
    }

    // 设置选中的数量显示在界面
    private void setSum() {
        textSum.setText(String.valueOf(sum));
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
        rootView = null;
        context = null;
        if(dbDao != null) {
            dbDao.closedb();
            dbDao = null;
        }
        if(FID != null) {
            FID.closeDB();
            FID = null;
        }
        if(contentList != null) {
            contentList.clear();
            contentList = null;
        }
        if(downLoadList != null) {
            downLoadList.clear();
            downLoadList = null;
        }
        if(subList != null) {
            subList.clear();
            subList = null;
        }
        albumMainAdapter = null;
        albumAdapter = null;
        textTotal = null;
        imageSort = null;
        imageSortDown = null;
        listAlbum = null;
        tipView = null;
        viewStatus = null;
        textSum = null;
        listDownload = null;
        imageAllCheck = null;
    }
}
