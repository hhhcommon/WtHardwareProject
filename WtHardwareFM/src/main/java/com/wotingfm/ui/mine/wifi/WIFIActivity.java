package com.wotingfm.ui.mine.wifi;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wotingfm.R;
import com.wotingfm.ui.common.baseactivity.AppBaseActivity;
import com.wotingfm.ui.mine.wifi.adapter.WiFiListAdapter;
import com.wotingfm.common.constant.BroadcastConstants;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.DialogUtils;
import com.wotingfm.util.L;

import java.util.ArrayList;
import java.util.List;

/**
 * WIFI界面
 */
public class WIFIActivity extends AppBaseActivity implements View.OnClickListener {
    private WiFiListAdapter adapter;
    private ScanResult wiFiName;
    private WifiInfo mWifiInfo;

    private Dialog openWiFiDialog;
    private Dialog wiFiInfoDialog;
    private ListView wifiListView;
    private ImageView imageWiFiSet;
    private TextView textUserWiFi;
    private View linearScan;

    private List<WifiConfiguration> wifiConfigList;     // 已经配置好的 WiFi 信息
    private List<ScanResult> scanResultList;            // 扫描得到的附近的 WiFi 列表
    private WifiManager wifiManager;
    private int p;

    @Override
    protected int setViewId() {
        return R.layout.activity_wifi;
    }

    @Override
    protected void init() {
        setTitle("WiFi 设置");
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 注册广播 监听 WiFi 的状态
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.UPDATE_WIFI_LIST);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, filter);

        wifiListView = findView(R.id.wifi_list_view);
        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_wifi, null);
        wifiListView.addHeaderView(headView);
        wifiListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        textUserWiFi = (TextView) headView.findViewById(R.id.user_wifi_list);   // 提示文字  可用 WiFi
        linearScan = findView(R.id.linear_scan);                                // 扫描

        findView(R.id.btn_scan_wifi).setOnClickListener(this);                  // 扫描 WiFi
        headView.findViewById(R.id.wifi_set).setOnClickListener(this);          // WiFi设置

        imageWiFiSet = (ImageView) headView.findViewById(R.id.image_wifi_set);
        if (wifiManager.isWifiEnabled()) {// WiFi 打开
            textUserWiFi.setVisibility(View.VISIBLE);
            imageWiFiSet.setImageResource(R.mipmap.wt_person_on);
            linearScan.setVisibility(View.VISIBLE);
            getConfiguration();
            mWifiInfo = wifiManager.getConnectionInfo();
            L.v(mWifiInfo.toString());
        } else {    // WiFi 关闭
            textUserWiFi.setVisibility(View.GONE);
            imageWiFiSet.setImageResource(R.mipmap.wt_person_close);
            linearScan.setVisibility(View.GONE);
        }
        scanResultList = wifiManager.getScanResults();
        if (scanResultList != null && scanResultList.size() > 0) {// 判断附近是否有可用 WiFi
            wifiListView.setAdapter(adapter = new WiFiListAdapter(context, scanResultList));
        } else {
            wifiListView.setAdapter(adapter = new WiFiListAdapter(context, scanResultList = new ArrayList<>()));
        }
        setItemListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_set:         // WiFi 开关
                if (wifiManager.isWifiEnabled()) {// 如果是打开状态则关闭 WiFi
                    wifiManager.setWifiEnabled(false);
                } else {                // 否则打开 WiFi
                    wifiManager.setWifiEnabled(true);
                    openWiFiDialog = DialogUtils.Dialogph(context, "正在打开并扫面附近WiFi");
                    scanResultList = wifiManager.getScanResults();
                }
                break;
            case R.id.btn_scan_wifi:    // 扫描附近 WiFi
                wifiManager.startScan();
                sendBroadcast(new Intent(BroadcastConstants.UPDATE_WIFI_LIST));
                break;
            case R.id.btn_cancel:       // 取消
                wiFiInfoDialog.dismiss();
                break;
            case R.id.btn_not_save:     // 不保存 删除已经配置好的网络 下次连接时需要输入密码连接
                wiFiInfoDialog.dismiss();
                wifiManager.removeNetwork(isConfiguration(scanResultList.get(p).SSID));
                break;
            case R.id.text_disconnect:  // 断开网络连接
                wiFiInfoDialog.dismiss();
                for (int i = 0; i < wifiConfigList.size(); i++) {
                    L.v("wiFiInfoDialog", "SSID -- > " + mWifiInfo.getSSID());
                    L.v("wiFiInfoDialog", "SSID -- > " + wifiConfigList.get(i).SSID);

                    if (mWifiInfo.getSSID().equals(wifiConfigList.get(i).SSID)) {// 断开当前连接的网络
                        wifiManager.disableNetwork(wifiConfigList.get(i).networkId);
                        wifiManager.disconnect();
                    }
                }
                break;
        }
    }

    // ListView 子条目点击事件  连接 WiFi
    private void setItemListener() {
        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position - 1 >= 0) {
                    String ssid = mWifiInfo.getSSID();
                    L.e("ssid -- > > " + ssid);
                    if (ssid.startsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                    if (ssid.equals(scanResultList.get(position - 1).SSID)) {
                        p = position - 1;
                        wiFiInfoDialog();
                        return;
                    }
                    final int wifiId = isConfiguration(scanResultList.get(position - 1).SSID);
                    L.e("wifiId -- > > " + wifiId);
                    if (wifiId != -1) {
                        openWiFiDialog = DialogUtils.Dialogph(context, "正在连接网络...");
                        connectWifi(wifiId);
                    } else {
                        Intent intent = new Intent(context, ConfigWiFiActivity.class);
                        wiFiName = scanResultList.get(position - 1);
                        intent.putExtra(StringConstant.WIFI_NAME, wiFiName.SSID);
                        startActivityForResult(intent, 200);
                    }
                }
            }
        });
    }

    // 连接的 WiFi 信息
    private void wiFiInfoDialog() {
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_connect_wifi_info, null);
        wiFiInfoDialog = new Dialog(context, R.style.MyDialog);
        TextView textTitle = (TextView) dialog.findViewById(R.id.text_title);       // 网络名称
        textTitle.setText(scanResultList.get(p).SSID);

        TextView textSignal = (TextView) dialog.findViewById(R.id.text_signal);     // 信号强度
        int signal = mWifiInfo.getRssi();
        if (signal < 0 && signal >= -50) {
            textSignal.setText("信号强度：强");
        } else if (signal < -50 && signal >= -70) {
            textSignal.setText("信号强度：较强");
        } else if (signal < -70 && signal >= -80) {
            textSignal.setText("信号强度：一般");
        } else {
            textSignal.setText("信号强度：差");
        }

        TextView textSpeed = (TextView) dialog.findViewById(R.id.text_speed);       // 网络速率
        textSpeed.setText("连接速度：" + mWifiInfo.getLinkSpeed() + "Mbps");

        TextView textSecurity = (TextView) dialog.findViewById(R.id.text_security); // 网络安全性
        String capabilities = scanResultList.get(p).capabilities;
        capabilities = capabilities.replaceAll("\\[", "");
        capabilities = capabilities.replaceAll("\\]", "/");
        capabilities = capabilities.substring(0, capabilities.length() - 2);
        textSecurity.setText("安全性：" + capabilities);

        TextView textIp = (TextView) dialog.findViewById(R.id.text_ip);             // IP地址
        textIp.setText("IP地址：" + mWifiInfo.getIpAddress());

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(this);              // 取消
        dialog.findViewById(R.id.btn_not_save).setOnClickListener(this);            // 不保存
        dialog.findViewById(R.id.text_disconnect).setOnClickListener(this);         // 断开连接

        wiFiInfoDialog.setContentView(dialog);
        wiFiInfoDialog.setCanceledOnTouchOutside(true);
        wiFiInfoDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        wiFiInfoDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                String result = data.getExtras().getString(StringConstant.WIFI_NAME);// 得到新 Activity 关闭后返回的数据
                L.i("设置的WiFi密码 -- > " + result);
                int id = addWifiConfig(wiFiName, result);
                if (id != -1) connectWifi(id);
            }
        }
    }

    // 得到配置好的Wifi信息
    private void getConfiguration() {
        if (wifiConfigList != null) wifiConfigList.clear();
        wifiConfigList = wifiManager.getConfiguredNetworks();
    }

    // 判定指定 WIFI 是否已经配置好,依据 WIFI 的地址 BSSID, 返回 NetId
    private int isConfiguration(String SSID) {
        getConfiguration();
        if (wifiConfigList == null || wifiConfigList.size() == 0) return -1;
        L.i("IsConfiguration", String.valueOf(wifiConfigList.size()));
        for (int i = 0; i < wifiConfigList.size(); i++) {
            String ssid = wifiConfigList.get(i).SSID;
            L.i(ssid, String.valueOf(wifiConfigList.get(i).networkId));
            L.e("ssid -- > > " + wifiConfigList.get(i).SSID);
            if (ssid.startsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            if (ssid.equals(SSID)) {// 地址相同
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }

    // 添加指定 WIFI 的配置信息,原列表不存在此 SSID
    private int addWifiConfig(ScanResult wifi, String pwd) {
        WifiConfiguration wifiCong = new WifiConfiguration();
        wifiCong.SSID = "\"" + wifi.SSID + "\"";
        wifiCong.preSharedKey = "\"" + pwd + "\"";// WPA-PSK 密码
        wifiCong.hiddenSSID = false;
        wifiCong.status = WifiConfiguration.Status.ENABLED;

        // 将配置好的特定 WIFI 密码信息添加,添加完成后默认是不激活状态,成功返回 ID,否则为 -1
        return wifiManager.addNetwork(wifiCong);
    }

    // 连接指定 id 的 WIFI
    private boolean connectWifi(int wifiId) {
        getConfiguration();
        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration wifi = wifiConfigList.get(i);
            if (wifi.networkId == wifiId) {// status: == 0 已经连接，== 1 不可连接，== 2 可以连接
                if (wifiManager.enableNetwork(wifiId, true)) {// 激活 Id，建立连接
                    L.w("已成功连接网络");
                    Intent pushWifi = new Intent(BroadcastConstants.UPDATE_WIFI_LIST);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("wifiName", wifiManager.getConnectionInfo().getSSID());
                    pushWifi.putExtras(bundle1);
                    sendBroadcast(pushWifi);
                }
                return true;
            }
        }
        return false;
    }

    // 广播接收器  用于更新 WiFi 列表
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastConstants.UPDATE_WIFI_LIST)) {// 更新 WiFi 列表
                L.i("扫描WiFi");
                getConfiguration();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWifiInfo = wifiManager.getConnectionInfo();// 已连接 WiFi 信息
                        adapter.setList(scanResultList = wifiManager.getScanResults());
                        if (openWiFiDialog != null) openWiFiDialog.dismiss();
                        L.i("scanResultList.size() --- > > " + scanResultList.size());
                        L.v(mWifiInfo.toString());
                    }
                }, 1200L);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听 wifi 的打开与关闭，与连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                L.e("H3c", "wifiState : " + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:// WiFi 关闭
                        imageWiFiSet.setImageResource(R.mipmap.wt_person_close);
                        textUserWiFi.setVisibility(View.GONE);
                        linearScan.setVisibility(View.GONE);
                        scanResultList.clear();
                        adapter.notifyDataSetChanged();
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:// WiFi 打开
                        imageWiFiSet.setImageResource(R.mipmap.wt_person_on);
                        textUserWiFi.setVisibility(View.VISIBLE);
                        linearScan.setVisibility(View.VISIBLE);
                        sendBroadcast(new Intent(BroadcastConstants.UPDATE_WIFI_LIST));
                        break;
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