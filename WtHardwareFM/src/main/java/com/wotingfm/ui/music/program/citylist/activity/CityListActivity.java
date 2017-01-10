package com.wotingfm.ui.music.program.citylist.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.ui.baseactivity.BaseActivity;
import com.wotingfm.ui.interphone.linkman.view.CharacterParser;
import com.wotingfm.ui.interphone.linkman.view.PinyinComparator_d;
import com.wotingfm.ui.interphone.linkman.view.SideBar;
import com.wotingfm.ui.music.program.citylist.adapter.CityListAdapter;
import com.wotingfm.ui.music.program.diantai.activity.CityRadioActivity;
import com.wotingfm.ui.music.program.fenlei.model.Catalog;
import com.wotingfm.ui.music.program.fenlei.model.CatalogName;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 城市列表
 * @author 辛龙
 * 2016年4月7日
 */
public class CityListActivity extends BaseActivity implements OnClickListener {
    private CharacterParser characterParser;
    private PinyinComparator_d pinyinComparator;
    private Dialog dialog;
    private TextView tvNoFriend;
    private SideBar sideBar;
    private TextView dialogs;
    private ListView listView;
    private EditText et_Search_content;
    private ImageView image_clear;
    private List<CatalogName> userList = new ArrayList<>();
    private CityListAdapter adapter;
    private List<CatalogName> srcList;
    private String tag = "CITY_LIST_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citylists);
        type = getIntent().getStringExtra("type");
        characterParser = CharacterParser.getInstance();// 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator_d();
        setView();
        setListener();
        if (GlobalConfig.CityCatalogList != null && GlobalConfig.CityCatalogList.size() > 0) {
            handleCityList(GlobalConfig.CityCatalogList);
        } else {
            if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
                dialog = DialogUtils.Dialogph(context, "正在获取信息");
                sendRequest();
            } else {
                ToastUtils.show_always(context, "网络失败，请检查网络");
            }
        }
    }

    private void setView() {
        findViewById(R.id.head_left_btn).setOnClickListener(this);

        tvNoFriend = (TextView) findViewById(R.id.title_layout_no_friends);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialogs = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialogs);
        listView = (ListView) findViewById(R.id.country_lvcountry);             // listview
        et_Search_content = (EditText) findViewById(R.id.et_search);         // 搜索控件
        image_clear = (ImageView) findViewById(R.id.image_clear);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.head_left_btn:
                finish();
                break;
        }
    }

    private void handleCityList(List<CatalogName> srcList) {
        if (srcList.size() == 0) {
            ToastUtils.show_always(context, "获取分类列表为空");
        } else {
            userList.clear();
            userList.addAll(srcList);
            filledData(userList);
            Collections.sort(userList, pinyinComparator);
            adapter = new CityListAdapter(context, userList);
            listView.setAdapter(adapter);
            setInterface();
        }
    }

    // 发送网络请求
    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
            private String ReturnType;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) dialog.dismiss();
                if (isCancelRequest) return;
                try {
                    ReturnType = result.getString("ReturnType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (ReturnType != null && ReturnType.equals("1001")) {
                    try {
                        Catalog SubList_all = new Gson().fromJson(result.getString("CatalogData"), new TypeToken<Catalog>() {}.getType());
                        srcList = SubList_all.getSubCata();
                        GlobalConfig.CityCatalogList = srcList;
                        handleCityList(srcList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtils.show_always(context, "获取列表失败，请返回重试!");
                }
            }

            @Override
            protected void requestError(VolleyError error) {
                if (dialog != null) dialog.dismiss();
                ToastUtils.showVolleyError(context);
            }
        });
    }

    // 设置请求参数
    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "2");
            jsonObject.put("ResultType", "1");
            jsonObject.put("RelLevel", "3");
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void filledData(List<CatalogName> person) {
        for (int i = 0; i < person.size(); i++) {
            person.get(i).setName(person.get(i).getCatalogName());
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(person.get(i).getCatalogName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                person.get(i).setSortLetters(sortString.toUpperCase());
            } else {
                person.get(i).setSortLetters("#");
            }
        }
    }

    private void setInterface() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type != null && !type.trim().equals("") && type.equals("address")) {
                    SharedPreferences sp = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);
                    Editor et = sp.edit();
                    et.putString(StringConstant.CITYTYPE, "true");
                    if (userList.get(position).getCatalogId() != null && !userList.get(position).getCatalogId().equals("")) {
                        et.putString(StringConstant.CITYID, userList.get(position).getCatalogId());
                        GlobalConfig.AdCode = userList.get(position).getCatalogId();
                    }
                    if (userList.get(position).getCatalogName() != null && !userList.get(position).getCatalogName().equals("")) {
                        et.putString(StringConstant.CITYNAME, userList.get(position).getCatalogName());
                        GlobalConfig.CityName = userList.get(position).getCatalogName();
                    }
                    et.commit();
                    finish();
                } else {
                    Intent intent = new Intent(context, CityRadioActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("fromtype", "city");
                    bundle.putString("name", userList.get(position).getCatalogName());
                    bundle.putString("type", "2");
                    bundle.putString("id", userList.get(position).getCatalogId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            }
        });

        /**
         * 设置右侧触摸监听
         */
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listView.setSelection(position);
                }
            }
        });
    }

    private void setListener() {
        image_clear.setOnClickListener(this);

        image_clear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                image_clear.setVisibility(View.INVISIBLE);
                et_Search_content.setText("");
            }
        });

        /**
         * 当输入框输入过汉字，且回复0后就要调用使用userlist1的原表数据
         */
        et_Search_content.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String search_name = s.toString();
                if (search_name.trim().equals("")) {
                    image_clear.setVisibility(View.INVISIBLE);
                    tvNoFriend.setVisibility(View.GONE);
                    // 关键词为空
                    if (srcList == null || srcList.size() == 0) {
                        listView.setVisibility(View.GONE);
                    } else {
                        listView.setVisibility(View.VISIBLE);
                        userList.clear();
                        userList.addAll(srcList);
                        filledData(userList);
                        Collections.sort(userList, pinyinComparator);
                        adapter = new CityListAdapter(context, userList);
                        listView.setAdapter(adapter);
                        setInterface();
                    }
                } else {
                    userList.clear();
                    userList.addAll(srcList);
                    image_clear.setVisibility(View.VISIBLE);
                    search(search_name);
                }
            }
        });
    }

    // 根据输入框中的值来过滤数据并更新 ListView
    private void search(String search_name) {
        List<CatalogName> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userList;
            tvNoFriend.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (CatalogName sortModel : userList) {
                String name = sortModel.getName();
                if (name.contains(search_name) || characterParser.getSelling(name).startsWith(search_name)) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.ChangeDate(filterDateList);
        userList.clear();
        userList.addAll(filterDateList);
        if (filterDateList.size() == 0) {
            tvNoFriend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        srcList = null;
        userList = null;
        adapter = null;
        tvNoFriend = null;
        sideBar = null;
        dialogs = null;
        listView = null;
        et_Search_content = null;
        listView = null;
        image_clear = null;
        pinyinComparator = null;
        characterParser = null;
        setContentView(R.layout.activity_null);
    }
}
