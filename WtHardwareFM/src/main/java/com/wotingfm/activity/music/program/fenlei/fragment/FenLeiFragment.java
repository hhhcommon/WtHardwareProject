package com.wotingfm.activity.music.program.fenlei.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.music.program.fenlei.adapter.FLeiGridAdapter;
import com.wotingfm.activity.music.program.fenlei.model.fenLei;
import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;
import com.wotingfm.activity.music.program.radiolist.activity.RadioListActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 分类页面
 *
 * @author 辛龙
 *         2016年3月31日
 */
public class FenLeiFragment extends Fragment implements View.OnClickListener{
    private FragmentActivity context;
    private FLeiGridAdapter adapter;

    private Dialog dialog;
    private View rootView;
    private GridView gridFenLei;

    protected List<fenLeiName> subList;

    private String tag = "FENLEI_VOLLEY_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_fenlei_new, container, false);
            gridFenLei = (GridView) rootView.findViewById(R.id.gv_fenlei);
            LinearLayout lin_second = (LinearLayout) rootView.findViewById(R.id.lin_second);
            lin_second.setOnClickListener(this);
            gridFenLei.setSelector(new ColorDrawable(Color.TRANSPARENT));   // 取消默认背景选择器
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {            // 发送网络请求
                sendRequest();
            } else {
                ToastUtils.show_short(context, "网络失败，请检查网络");
            }
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
           case R.id.lin_second:
               if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {            // 发送网络请求
                   sendRequest();
               } else {
                   ToastUtils.show_short(context, "网络失败，请检查网络");
               }
               break;

        }
    }

    // GridView 点击事件 跳转到具体的分类中
    private void setItemListener() {
        gridFenLei.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, RadioListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Catalog", subList.get(position));
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    // 发送网络请求
    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isCancelRequest) {
                    return;
                }
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 根据返回值来对程序进行解析
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        fenLei subListAll = new Gson().fromJson(result.getString("CatalogData"), new TypeToken<fenLei>() {}.getType());
                        subList = subListAll.getSubCata();
                        if (subList != null && subList.size() != 0) {
                            if (adapter == null) {
                                gridFenLei.setAdapter(adapter = new FLeiGridAdapter(context, subList));
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            setItemListener();
                        } else {
                            ToastUtils.show_always(context, "获取分类列表为空");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (ReturnType != null && ReturnType.equals("1002")) {
                    ToastUtils.show_always(context, "无此分类信息");
                } else if (ReturnType != null && ReturnType.equals("1003")) {
                    ToastUtils.show_always(context, "分类不存在");
                } else if (ReturnType != null && ReturnType.equals("1011")) {
                    ToastUtils.show_always(context, "当前暂无分类");
                } else if (ReturnType != null && ReturnType.equals("T")) {
                    ToastUtils.show_always(context, "获取列表异常");
                }else {
                    ToastUtils.show_always(context, "数据获取异常，请稍候重试");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ToastUtils.showVolleyError(context);
            }
        });
    }

    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "3");
//			jsonObject.put("CatalogId", "1");
            jsonObject.put("ResultType", "1");
            // 以001类型的分类为基础，获得0001结点下的2级分类的树形分类数据（实际上是森林）
            jsonObject.put("RelLevel", "0");
            // 页数信息，第一页为1，若不设置，则返回第一页信息
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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
        gridFenLei = null;
        adapter = null;
        dialog = null;
        if(subList != null) {
            subList.clear();
            subList = null;
        }
        tag = null;
    }
}
