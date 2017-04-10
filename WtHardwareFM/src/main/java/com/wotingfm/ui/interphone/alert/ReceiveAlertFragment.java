package com.wotingfm.ui.interphone.alert;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.IntegerConstant;
import com.wotingfm.common.helper.InterPhoneControlHelper;
import com.wotingfm.common.service.SubclassService;
import com.wotingfm.ui.interphone.chat.dao.SearchTalkHistoryDao;
import com.wotingfm.ui.interphone.chat.fragment.ChatFragment;
import com.wotingfm.ui.interphone.chat.model.DBTalkHistorary;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.ui.interphone.main.DuiJiangFragment;
import com.wotingfm.ui.main.MainActivity;
import com.wotingfm.util.AssembleImageUrlUtils;
import com.wotingfm.util.BitmapUtils;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.VibratorUtils;

/**
 * 来电话弹出框
 *
 * @author 辛龙
 *         2016年3月7日
 */
public class ReceiveAlertFragment extends Fragment implements OnClickListener {
    private SearchTalkHistoryDao dbDao;
    private String image;
    private String name;
    private String id;
    private long[] Vibrate = {400, 800, 400, 800};
    private FragmentActivity instance;
    private View rootView;
    public static ReceiveAlertFragment context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.dialog_receivecall, container, false);
            rootView.setOnClickListener(this);
            instance = getActivity();
            context = this;
            VibratorUtils.Vibrate(instance, Vibrate, true);
            if (DuiJiangFragment.ct == null) {
                //对讲主页界面更新
                MainActivity.changeTwo();
                DuiJiangFragment.update();
            }
            getSource();        // 获取展示数据
            setView();          // 设置界面，以及界面数据
            initDao();          // 初始化数据库
        }
        return rootView;
    }

    public static void close() {
        DuiJiangActivity.close();
    }

    private void setView() {
        ImageView imageview = (ImageView) rootView.findViewById(R.id.image);
        TextView tv_name = (TextView) rootView.findViewById(R.id.tv_name);

        LinearLayout lin_call = (LinearLayout) rootView.findViewById(R.id.lin_call);
        lin_call.setOnClickListener(this);

        LinearLayout lin_guaduan = (LinearLayout) rootView.findViewById(R.id.lin_guaduan);
        lin_guaduan.setOnClickListener(this);

        ImageView img_zhezhao = (ImageView) rootView.findViewById(R.id.img_zhezhao);
        Bitmap bmp_zhezhao = BitmapUtils.readBitMap(instance, R.mipmap.liubianxing_orange_big);
        img_zhezhao.setImageBitmap(bmp_zhezhao);

        //适配好友展示信息
        tv_name.setText(name);
        if (image == null || image.equals("") || image.equals("null") || image.trim().equals("")) {
            imageview.setImageResource(R.mipmap.wt_image_tx_hy);
        } else {
            String url = GlobalConfig.imageurl + image;
            String _url = AssembleImageUrlUtils.assembleImageUrl300(url);

            // 加载图片
            AssembleImageUrlUtils.loadImage(_url, url, imageview, IntegerConstant.TYPE_PERSON);
        }
    }

    private void getSource() {
        //查找当前好友的展示信息
        id = SubclassService.callerId;
        try {
            if (GlobalConfig.list_person != null && GlobalConfig.list_person.size() > 0) {
                for (int i = 0; i < GlobalConfig.list_person.size(); i++) {
                    if (id.equals(GlobalConfig.list_person.get(i).getUserId())) {
                        image = GlobalConfig.list_person.get(i).getPortraitBig();
                        name = GlobalConfig.list_person.get(i).getNickName();
                        break;
                    }
                }
            } else {
                image = null;
                name = "我听科技";
            }
        } catch (Exception e) {
            e.printStackTrace();
            image = null;
            name = "我听科技";
        }
    }

    /**
     * 初始化数据库
     */
    private void initDao() {
        dbDao = new SearchTalkHistoryDao(instance);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_call:
                SubclassService.isallow = true;
                InterPhoneControlHelper.PersonTalkAllow(instance, SubclassService.callid, SubclassService.callerId);//接收应答
                if (SubclassService.musicPlayer != null) {
                    SubclassService.musicPlayer.stop();
                    SubclassService.musicPlayer = null;
                }
                ChatFragment.isCalling = true;
                addUser();
                break;
            case R.id.lin_guaduan:
                SubclassService.isallow = true;
                InterPhoneControlHelper.PersonTalkOver(instance, SubclassService.callid, SubclassService.callerId);//拒绝应答
                if (SubclassService.musicPlayer != null) {
                    SubclassService.musicPlayer.stop();
                    SubclassService.musicPlayer = null;
                }
                DuiJiangActivity.close();
                break;
        }
    }

    public void addUser() {
        //获取最新激活状态的数据
        String addtime = Long.toString(System.currentTimeMillis());
        String bjuserid = CommonUtils.getUserId(instance);
        //如果该数据已经存在数据库则删除原有数据，然后添加最新数据
        dbDao.deleteHistory(id);
        DBTalkHistorary history = new DBTalkHistorary(bjuserid, "user", id, addtime);
        dbDao.addTalkHistory(history);
//        DBTalkHistorary talkdb = dbDao.queryHistory().get(0);//得到数据库里边数据
        //对讲主页界面更新
        MainActivity.changeTwo();
        DuiJiangFragment.update();
        ChatFragment.zhiDingPerson();
        DuiJiangActivity.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VibratorUtils.cancel(instance);
        Log.e("停止震动", "停止震动");
        instance = null;
        image = null;
        name = null;
        id = null;
        if (dbDao != null) {
            dbDao = null;
        }
    }
}
