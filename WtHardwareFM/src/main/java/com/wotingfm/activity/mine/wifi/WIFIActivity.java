package com.wotingfm.activity.mine.wifi;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.mine.main.MineActivity;
import com.wotingfm.activity.mine.wifi.adapter.WiFiListAdapter;
import com.wotingfm.common.constant.BroadcastConstant;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * WIFI界面
 */
public class WiFiActivity extends AppBaseActivity implements View.OnClickListener {
    private ScanResult wiFiName;
    private ListView wifiListView;
    private ImageView imageWiFiSet;
    private TextView textUserWiFi;
    private View linearScan;

    private List<ScanResult> scanResultList;
    private WiFiListAdapter adapter;

    private List<WifiConfiguration> wifiConfigList;// 已经配置好的WiFi信息

    @Override
    protected int setViewId() {
        return R.layout.activity_wifi;
    }

    @Override
    protected void init() {
        setTitle("WiFi");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstant.UPDATE_WIFI_LIST);
        registerReceiver(mBroadcastReceiver, filter);

        wifiListView = findView(R.id.wifi_list_view);
        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_wifi, null);
        wifiListView.addHeaderView(headView);
        wifiListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        textUserWiFi = (TextView) headView.findViewById(R.id.user_wifi_list);// 提示文字  可用WiFi
        linearScan = findView(R.id.linear_scan);// 扫描

        findView(R.id.btn_scan_wifi).setOnClickListener(this);
        headView.findViewById(R.id.wifi_set).setOnClickListener(this);// WiFi设置
        imageWiFiSet = (ImageView) headView.findViewById(R.id.image_wifi_set);
        if(MineActivity.wifiManager.isWifiEnabled()) {
            textUserWiFi.setVisibility(View.VISIBLE);
            imageWiFiSet.setImageResource(R.mipmap.wt_person_on);
            linearScan.setVisibility(View.VISIBLE);
            getConfiguration();
        } else {
            textUserWiFi.setVisibility(View.GONE);
            imageWiFiSet.setImageResource(R.mipmap.wt_person_close);
            linearScan.setVisibility(View.GONE);
        }
        scanResultList = MineActivity.wifiManager.getScanResults();
        if(scanResultList != null) {
            wifiListView.setAdapter(adapter = new WiFiListAdapter(context, scanResultList));
        } else {
            wifiListView.setAdapter(new WiFiListAdapter(context, scanResultList = new ArrayList<>()));
        }
        setItemListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_set:// WiFi 开关
                if(MineActivity.wifiManager.isWifiEnabled()) {// 如果是打开状态则关闭WiFi
                    imageWiFiSet.setImageResource(R.mipmap.wt_person_close);
                    MineActivity.wifiManager.setWifiEnabled(false);
                    textUserWiFi.setVisibility(View.GONE);
                    scanResultList.clear();
                    adapter.notifyDataSetChanged();
                    isOpen = true;
                    linearScan.setVisibility(View.GONE);
                } else {// 否则打开WiFi
                    MineActivity.wifiManager.setWifiEnabled(true);
                    imageWiFiSet.setImageResource(R.mipmap.wt_person_on);
                    textUserWiFi.setVisibility(View.VISIBLE);
                    linearScan.setVisibility(View.VISIBLE);
                    openWiFiDialog = DialogUtils.Dialogph(context, "正在打开并扫面附近WiFi");
//                    MineActivity.wifiManager.startScan();
                    getNearWiFi();
                }
                break;
            case R.id.btn_scan_wifi:
                MineActivity.wifiManager.startScan();
                sendBroadcast(new Intent(BroadcastConstant.UPDATE_WIFI_LIST));
                break;
        }
    }

    private boolean isOpen = true;
    private Dialog openWiFiDialog;

    /**
     * 获取附近的WiFi
     */
    private void getNearWiFi(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isOpen) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(MineActivity.wifiManager.isWifiEnabled()) {
                        isOpen = false;
                        sendBroadcast(new Intent(BroadcastConstant.UPDATE_WIFI_LIST));
                    }
                }
            }
        }).start();
    }

    /*
     * ListView 子条目点击事件  连接WiFi
     */
    private void setItemListener(){
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position - 1 >= 0){
                    String ssid = MineActivity.wifiManager.getConnectionInfo().getSSID();
                    L.e("ssid -- > > " + ssid);
                    if(ssid.substring(1, ssid.length() - 1).equals(scanResultList.get(position - 1).SSID)){
                        Toast.makeText(context, "现在连接的WiFi", Toast.LENGTH_SHORT).show();
                        return ;
                    }
                    int wifiId = isConfiguration(scanResultList.get(position - 1).SSID);
                    L.e("wifiId -- > > " + wifiId);
                    if(wifiId != -1){
                        connectWifi(wifiId);
                    } else {
                        Intent intent = new Intent(context, ConfigWiFiActivity.class);
                        wiFiName = scanResultList.get(position - 1);
                        intent.putExtra("WIFINAME", wiFiName.SSID);
                        startActivityForResult(intent, 200);
                    }
                }
                L.v("点击的 position -- > > " + position);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 200){
            if(resultCode == RESULT_OK){
                String result = data.getExtras().getString("WIFIMIMA");//得到新Activity 关闭后返回的数据
                L.i("设置的WiFi密码 -- > " + result);
                int id = addWifiConfig(wiFiName, result);
                if(id != -1){
                    connectWifi(id);
                }
            }
        }
    }

    // 得到配置好的Wifi信息
    private void getConfiguration(){
        if(wifiConfigList != null){
            wifiConfigList.clear();
        }
        wifiConfigList = MineActivity.wifiManager.getConfiguredNetworks();
        for(int i =0;i<wifiConfigList.size();i++){
            L.i("getConfiguration",wifiConfigList.get(i).SSID);
            L.i("getConfiguration", String.valueOf(wifiConfigList.get(i).networkId));
        }
    }

    // 判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
    private int isConfiguration(String SSID){
        getConfiguration();
        if(wifiConfigList == null || wifiConfigList.size() == 0){
            return -1;
        }
        L.i("IsConfiguration",String.valueOf(wifiConfigList.size()));
        for(int i = 0; i < wifiConfigList.size(); i++){
            L.i(wifiConfigList.get(i).SSID,String.valueOf( wifiConfigList.get(i).networkId));
            L.e("ssid -- > > " + wifiConfigList.get(i).SSID);
            if(wifiConfigList.get(i).SSID.substring(1, wifiConfigList.get(i).SSID.length() - 1).equals(SSID)){// 地址相同
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }

    // 添加指定WIFI的配置信息,原列表不存在此SSID
    private int addWifiConfig(ScanResult wifi, String pwd){
        int wifiId = -1;
        WifiConfiguration wifiCong = new WifiConfiguration();
        wifiCong.SSID = "\""+wifi.SSID+"\"";//\"转义字符，代表"
        wifiCong.preSharedKey = "\""+pwd+"\"";//WPA-PSK密码
        wifiCong.hiddenSSID = false;
        wifiCong.status = WifiConfiguration.Status.ENABLED;

        // 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
        wifiId = MineActivity.wifiManager.addNetwork(wifiCong);
        if(wifiId != -1){
            return wifiId;
        }
        return wifiId;
    }

    // 连接指定Id的WIFI
    private boolean connectWifi(int wifiId){
        getConfiguration();
        for(int i = 0; i < wifiConfigList.size(); i++){
            // 断开当前连接的网络  连接新的网络
            if(MineActivity.wifiManager.getConnectionInfo().getSSID().equals(wifiConfigList.get(i).SSID.substring(1, wifiConfigList.get(i).SSID.length() - 1))) {
                MineActivity.wifiManager.disableNetwork(wifiConfigList.get(i).networkId);
                MineActivity.wifiManager.disconnect();
            }
            WifiConfiguration wifi = wifiConfigList.get(i);
            if(wifi.networkId == wifiId){
                if(MineActivity.wifiManager.enableNetwork(wifiId, true)){
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            adapter.notifyDataSetChanged();
//                        }
//                    }, 3000);
                    L.w("已成功连接网络");
                }
//                while(!(MineActivity.wifiManager.enableNetwork(wifiId, true))){// 激活该Id，建立连接
//                    // status:0--已经连接，1--不可连接，2--可以连接
//                    L.i("ConnectWifi",String.valueOf(wifiConfigList.get(wifiId).status));
//
//                    if(wifiConfigList.get(wifiId).status == 0){
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        }, 3000);
//                    } else if(wifiConfigList.get(wifiId).status == 1) {
//                        Toast.makeText(context, "网络不可连接", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(context, "正在连接网络", Toast.LENGTH_SHORT).show();
//                    }
//                }
                return true;
            }
        }
        return false;
    }

    /**
     * 广播接收器  用于更新WiFi列表
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BroadcastConstant.UPDATE_WIFI_LIST)) {
                L.i("扫描WiFi");
                scanResultList.clear();
                getConfiguration();
                scanResultList = MineActivity.wifiManager.getScanResults();
                if(scanResultList != null) {
                    wifiListView.setAdapter(adapter = new WiFiListAdapter(context, scanResultList));
                } else {
                    wifiListView.setAdapter(new WiFiListAdapter(context, scanResultList = new ArrayList<>()));
                }
                if(openWiFiDialog != null) {
                    openWiFiDialog.dismiss();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}