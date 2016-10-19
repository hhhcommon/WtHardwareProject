package com.wotingfm.activity.music.program.citylist.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.im.interphone.linkman.view.CharacterParser;
import com.wotingfm.activity.im.interphone.linkman.view.PinyinComparator_d;
import com.wotingfm.activity.im.interphone.linkman.view.SideBar;
import com.wotingfm.activity.music.program.citylist.adapter.CityListAdapter;
import com.wotingfm.activity.music.program.fenlei.model.fenLei;
import com.wotingfm.activity.music.program.fenlei.model.fenLeiName;
import com.wotingfm.common.application.BSApplication;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.common.volley.VolleyCallback;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;
import com.wotingfm.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 城市列表
 *
 * @author 辛龙
 *         2016年4月7日
 */
public class CityListActivity extends AppBaseActivity {
    private CityListAdapter adapter;
    private CharacterParser characterParser;
    private PinyinComparator_d pinyinComparator;

    private Dialog dialog;
    private SideBar sideBar;
    private ListView listView;
    private TextView textNoFriend;
    private EditText editSearchContent;
    private ImageView imageClear;

    private List<fenLeiName> userList = new ArrayList<>();
    private List<fenLeiName> srcList;

    private String tag = "CITY_LIST_REQUEST_CANCEL_TAG";
    private boolean isCancelRequest;

    @Override
    protected int setViewId() {
        return R.layout.activity_citylists;
    }

    @Override
    protected void init() {
        setTitle("省市台");

        characterParser = CharacterParser.getInstance();                                // 实例化汉字转拼音类
        pinyinComparator = new PinyinComparator_d();
        setView();
        setListener();
        if (GlobalConfig.CURRENT_NETWORK_STATE_TYPE != -1) {
            dialog = DialogUtils.Dialogph(context, "正在获取信息");
            sendRequest();
        } else {
            ToastUtils.show_always(context, "网络失败，请检查网络");
        }
    }

    // 初始化控件
    private void setView() {
        textNoFriend = (TextView) findViewById(R.id.title_layout_no_friends);

        TextView dialogs = (TextView) findViewById(R.id.dialog);
        sideBar = (SideBar) findViewById(R.id.sidrbar);
        sideBar.setTextView(dialogs);

        listView = (ListView) findViewById(R.id.country_lvcountry);        // listView
        editSearchContent = (EditText) findViewById(R.id.et_search);        // 搜索控件
        imageClear = (ImageView) findViewById(R.id.image_clear);
    }

    // 发送网络请求获取数据列表
    private void sendRequest() {
        VolleyRequest.RequestPost(GlobalConfig.getCatalogUrl, tag, setParam(), new VolleyCallback() {
            private String ReturnType;
//            private fenLeiName mFenleiname;

            @Override
            protected void requestSuccess(JSONObject result) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                // 如果网络请求已经执行取消操作  就表示就算请求成功也不需要数据返回了  所以方法就此结束
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
                        srcList = subListAll.getSubCata();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (srcList.size() == 0) {
                        ToastUtils.show_always(context, "获取分类列表为空");
                    } else {
                        userList.clear();
                        userList.addAll(srcList);
                        filledData(userList);
                        Collections.sort(userList, pinyinComparator);
                        listView.setAdapter(adapter = new CityListAdapter(context, userList));
                        setInterface();
						  /*  //将数据写入数据库
						    List<fenLeiName> mlist=new ArrayList<fenLeiName>();
						    for(int i=0;i<srclist.size();i++){
						    	 mFenleiname=new fenLeiName();
						    	 mFenleiname.setCatalogId(srclist.get(i).getCatalogId());
						    	 mFenleiname.setCatalogName(srclist.get(i).getCatalogName());
						    	 mlist.add(mFenleiname);
						    	 // 暂时只解析一层 不向下解析了
						    	 if(srclist.get(i).getSubCata()!=null&&srclist.get(i).getSubCata().size()>0){
						    		 for(int j=0;j<srclist.get(i).getSubCata().size();j++){
						    			 mFenleiname=new fenLeiName();
								    	 mFenleiname.setCatalogId(srclist.get(i).getSubCata().get(j).getCatalogId());
								    	 mFenleiname.setCatalogName(srclist.get(i).getSubCata().get(j).getCatalogName());
								    	 mlist.add(mFenleiname);
						    		 }
						    	 }
						    }
						    if(mlist.size()!=0){
						    	CID.InsertCityInfo(mlist);
						    } */
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

    // 请求网络需要提交的参数
    private JSONObject setParam() {
        JSONObject jsonObject = VolleyRequest.getJsonObject(context);
        try {
            jsonObject.put("CatalogType", "2");
            jsonObject.put("ResultType", "1");
            jsonObject.put("RelLevel", "0");
            jsonObject.put("Page", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void filledData(List<fenLeiName> person) {
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
                Editor et = BSApplication.SharedPreferences.edit();
                et.putString(StringConstant.CITYTYPE, "true");
                if (userList.get(position).getCatalogId() != null && !userList.get(position).getCatalogId().equals("")) {
                    et.putString(StringConstant.CITYID, userList.get(position).getCatalogId());
                    GlobalConfig.AdCode = userList.get(position).getCatalogId();
                }
                if (userList.get(position).getCatalogName() != null && !userList.get(position).getCatalogName().equals("")) {
                    et.putString(StringConstant.CITYNAME, userList.get(position).getCatalogName());
                    GlobalConfig.CityName = userList.get(position).getCatalogName();
                }
                if(!et.commit()) {
                    L.w("数据 commit 失败!");
                }
                sendBroadcast(new Intent(BroadcastConstant.CITY_CHANGE));// 发送广播更新城市信息
                finish();
            }
        });

        // 设置右侧触摸监听
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
        imageClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClear.setVisibility(View.INVISIBLE);
                editSearchContent.setText("");
            }
        });

        // 当输入框输入过汉字，且回复0后就要调用使用userList1的原表数据
        editSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String search_name = s.toString();
                if (search_name.equals("") || search_name.trim().equals("")) {
                    imageClear.setVisibility(View.INVISIBLE);
                    textNoFriend.setVisibility(View.GONE);
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
                    imageClear.setVisibility(View.VISIBLE);
                    search(search_name);
                }
            }
        });
    }

    // 根据输入框中的值来过滤数据并更新ListView
    private void search(String search_name) {
        List<fenLeiName> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(search_name)) {
            filterDateList = userList;
            textNoFriend.setVisibility(View.GONE);
        } else {
            filterDateList.clear();
            for (fenLeiName sortModel : userList) {
                String name = sortModel.getName();
                if (name.indexOf(search_name) != -1
                        || characterParser.getSelling(name).startsWith(search_name)) {
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
            textNoFriend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelRequest = VolleyRequest.cancelRequest(tag);
        srcList = null;
        userList = null;
        adapter = null;
        textNoFriend = null;
        sideBar = null;
        listView = null;
        editSearchContent = null;
        listView = null;
        imageClear = null;
        pinyinComparator = null;
        characterParser = null;
    }
}
