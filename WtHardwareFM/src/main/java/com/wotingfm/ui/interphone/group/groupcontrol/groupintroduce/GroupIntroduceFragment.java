package com.wotingfm.ui.interphone.group.groupcontrol.groupintroduce;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.interphone.main.DuiJiangActivity;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 群组详情页面
 * 辛龙 2016年1月21日
 */
public class GroupIntroduceFragment extends Fragment implements View.OnClickListener{

    private String groupId;
    private String tag = "TALK_GROUP_INTRODUCE_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private Bitmap bmp;
    private ImageView img_EWM;
    private FragmentActivity context;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_group_introduce, container, false);
            rootView.setOnClickListener(this);
            context = getActivity();
            setView();
            handleIntent();
        }
        return rootView;
    }


    // 初始化视图
    private void setView() {
        rootView.findViewById(R.id.head_left_btn).setOnClickListener(this);          // 返回
    }


    // 处理请求
    private void handleIntent() {
    /*    GroupInfo groupNews = (GroupInfo) getIntent().getSerializableExtra("group");
        if(!TextUtils.isEmpty(groupNews.getGroupId())){
            groupId=groupNews.getGroupId();
        }
        try {

            bmp = CreateQRImageHelper.getInstance().createQRImage(2, groupNews, null, 300, 300);

        }catch (Exception e){

        }
        if (bmp == null) {

            bmp = BitmapUtils.readBitMap(context, R.mipmap.ewm);
        }

        img_EWM.setImageBitmap(bmp);*/
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                DuiJiangActivity.close();
                break;
         /*   case R.id.tv_exit:
                 if(confirmDialog!=null&&!confirmDialog.isShowing()&&!TextUtils.isEmpty(groupId)){
                     confirmDialog.show();
                 }else{
                     ToastUtils.show_always(context,"网络异常，请稍后重试");
                 }
                break;
            case R.id.tv_cancle:
                if(confirmDialog!=null&&confirmDialog.isShowing()){
                confirmDialog.dismiss();
                }
                break;
            case R.id.tv_confirm:
                if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                    if(confirmDialog!=null&&confirmDialog.isShowing()){
                        confirmDialog.dismiss();
                    }
                    SendExitRequest();
                } else {
                    ToastUtils.show_always(context, "网络失败，请检查网络");
                }
                break;*/
        }
    }

    // 退出群组
    private void SendExitRequest() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("GroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyRequest.RequestPost(GlobalConfig.ExitGroupurl, tag, jsonObject, new VolleyCallback() {
            @Override
            protected void requestSuccess(JSONObject result) {
                if (isCancelRequest) return;
                try {
                    String ReturnType = result.getString("ReturnType");
                    //Log.v("ReturnType", "ReturnType -- > > " + ReturnType);
                   if(!TextUtils.isEmpty(ReturnType)){
                    if (ReturnType.equals("1001") || ReturnType.equals("10011")) {
                        ToastUtils.show_always(context, "已经成功退出该组");
                        context.setResult(1);
                        DuiJiangActivity.close();
                    } else {
                        ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
                    }
                   }else{
                       ToastUtils.show_always(context, "退出群组失败，请稍后重试!");
                   }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                ToastUtils.showVolleyError(context);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
    }
}
