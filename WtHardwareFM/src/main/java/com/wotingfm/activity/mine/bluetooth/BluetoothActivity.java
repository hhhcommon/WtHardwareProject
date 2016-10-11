package com.wotingfm.activity.mine.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wotingfm.R;
import com.wotingfm.activity.common.baseactivity.AppBaseActivity;
import com.wotingfm.activity.mine.bluetooth.adapter.PairBluetoothAdapter;
import com.wotingfm.activity.mine.bluetooth.adapter.UserBluetoothAdapter;
import com.wotingfm.activity.mine.bluetooth.model.BluetoothInfo;
import com.wotingfm.activity.mine.main.MineActivity;
import com.wotingfm.common.constant.StringConstant;
import com.wotingfm.util.L;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * 蓝牙设置界面
 */
public class BluetoothActivity extends AppBaseActivity implements View.OnClickListener {
    private boolean hasRegister = false;// 标识 是否已注册蓝牙监听广播
    private SharedPreferences sharedPreferences;
    private boolean openDetectionState;// 标识 是否为开放检测
    private String newName;// 保存用户对设备设置的心名字
    private String time;
    private Dialog setBluetoothNameDialog;// 用户重命名蓝牙名字
    private CountDownTimer mCountDownTimer;

    private DeviceReceiver mDevice = new DeviceReceiver();// 蓝牙广播
    private ListView userBluetoothList;// 可用蓝牙设备列表
    private ListView pairBluetoothList;// 已配对蓝牙设备列表
    private List<BluetoothInfo> pairList;
    private List<BluetoothInfo> userList;
    private PairBluetoothAdapter pairAdapter;
    private UserBluetoothAdapter userAdapter;

    private LinearLayout linearBluetoothSearch;// 搜索蓝牙设备
    private RelativeLayout openDetection;// 开放检测设置  打开 OR 关闭
    private ImageView imageBluetoothSet;// 蓝牙设置开关
    private ImageView imageOpenDetection;// 蓝牙开放检测开关
    private TextView textOpenDetectionInfo;// 蓝牙开放检测提示信息
    private TextView textOpenDetectionTime;// 蓝牙开放检测倒计时时间
    private TextView textUserDevice;// 提示文字 可用设备
    private TextView textPairDevice;// 提示文字 已配对设备
    private TextView textBluetoothName;// 蓝牙名字
    private TextView textOpenDetection;// "开放检测"

    final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 1:
                    textOpenDetectionTime.setText("(倒计时" + time + ")");
                    break;
                case 0:
                    textOpenDetectionTime.setVisibility(View.GONE);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    break;
            }
        }
    };

    @Override
    protected int setViewId() {
        return R.layout.activity_bluetooth;
    }

    @Override
    protected void init() {
        setTitle("蓝牙");
        sharedPreferences = getSharedPreferences("wotingfm", Context.MODE_PRIVATE);

        Button btnSearchDevice = (Button) findViewById(R.id.bluetooth_search);// 搜索蓝牙设备
        btnSearchDevice.setOnClickListener(this);

        linearBluetoothSearch = (LinearLayout) findViewById(R.id.linear_bluetooth_search);
        userBluetoothList = (ListView) findViewById(R.id.list_user_bluetooth);
        userBluetoothList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        View headView = LayoutInflater.from(context).inflate(R.layout.head_view_bluetooth, null);
        userBluetoothList.addHeaderView(headView);
        pairBluetoothList = (ListView) headView.findViewById(R.id.list_pair_bluetooth);
        pairBluetoothList.setSelector(new ColorDrawable(Color.TRANSPARENT));

        openDetection = (RelativeLayout) headView.findViewById(R.id.open_detection);// 开放检测
        openDetection.setOnClickListener(this);
        imageOpenDetection = (ImageView) headView.findViewById(R.id.image_open_detection);// 开放检测开关
        textOpenDetection = (TextView) headView.findViewById(R.id.text_open_detection);
        textOpenDetectionInfo = (TextView) headView.findViewById(R.id.text_open_detection_info);
        textOpenDetectionTime = (TextView) headView.findViewById(R.id.text_open_detection_time);// 开放检测时间
        textUserDevice = (TextView) headView.findViewById(R.id.text_user_device);
        textPairDevice = (TextView) headView.findViewById(R.id.text_pair_device);

        textBluetoothName = (TextView) findViewById(R.id.text_device_name);
        textBluetoothName.setText(MineActivity.blueAdapter.getName());

        headView.findViewById(R.id.device_name_set).setOnClickListener(this);
        headView.findViewById(R.id.bluetooth_set).setOnClickListener(this);// 开启蓝牙
        imageBluetoothSet = (ImageView) findViewById(R.id.image_bluetooth_set);
        if (MineActivity.blueAdapter.isEnabled()) {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
            openDetection.setClickable(true);
            openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);// 开放蓝牙检测开关
            if(openDetectionState){
                imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                textOpenDetectionTime.setVisibility(View.VISIBLE);
//                timeCountDown(2 * 60 * 1000);
            } else {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                textOpenDetectionTime.setVisibility(View.GONE);
            }
            textOpenDetection.setTextColor(getResources().getColor(R.color.wt_login_third));
            openDetection.setBackgroundDrawable(getResources().getDrawable(R.drawable.person_color));
            textUserDevice.setVisibility(View.VISIBLE);
            textPairDevice.setVisibility(View.VISIBLE);
            pairBluetoothList.setVisibility(View.VISIBLE);
            findAvalibleDevice();
            linearBluetoothSearch.setVisibility(View.VISIBLE);
            userBluetoothList.setDividerHeight(1);
            userList = new ArrayList<>();
//            userList.add("No can be matched to use bluetooth");
            userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList));
            MineActivity.blueAdapter.startDiscovery();
        } else {
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
            imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            openDetection.setClickable(false);
            textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
            openDetection.setBackgroundDrawable(null);
            textUserDevice.setVisibility(View.GONE);
            textPairDevice.setVisibility(View.GONE);
            pairBluetoothList.setVisibility(View.GONE);
            linearBluetoothSearch.setVisibility(View.GONE);
            textOpenDetectionTime.setVisibility(View.GONE);
            textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
            userBluetoothList.setDividerHeight(0);
            userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList = new ArrayList<>()));
            pairBluetoothList.setAdapter(pairAdapter = new PairBluetoothAdapter(context, pairList = new ArrayList<>()));
        }

        setItemLis();
    }

    // 注册蓝牙接收广播
    @Override
    protected void onStart() {
        if(!hasRegister){
            hasRegister = true;
            IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            IntentFilter filterTime = new IntentFilter(StringConstant.UPDATE_BLUETO0TH_TIME);
            IntentFilter filterTimeOff = new IntentFilter(StringConstant.UPDATE_BLUETO0TH_TIME_OFF);
            registerReceiver(mDevice, filter);
            registerReceiver(mDevice, filterStart);
            registerReceiver(mDevice, filterEnd);
            registerReceiver(mDevice, filterTime);
            registerReceiver(mDevice, filterTimeOff);
        }
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bluetooth_search:// 搜索蓝牙设备
                MineActivity.blueAdapter.startDiscovery();
                userList.clear();
                userAdapter.notifyDataSetChanged();
                break;
            case R.id.bluetooth_set:// 蓝牙开关
                if (MineActivity.blueAdapter.isEnabled()) {
                    MineActivity.blueAdapter.cancelDiscovery();// 停止蓝牙搜索
                    MineActivity.blueAdapter.disable();// 关闭蓝牙
                    SharedPreferences.Editor et = sharedPreferences.edit();
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
                    et.putBoolean(StringConstant.BLUETOOTH_SET, false);
                    et.commit();
                    openDetection.setClickable(false);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    if(mCountDownTimer != null){
                        mCountDownTimer.cancel();
                        mCountDownTimer = null;
                    }
                    userList.clear();
//                    userList.add("");
                    if(userAdapter == null){
                        userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList));
                    } else {
                        userAdapter.notifyDataSetChanged();
                    }
                } else {
                    setBluetooth();// 打开蓝牙
                }
                break;
            case R.id.open_detection:// 开放检测
                SharedPreferences.Editor et = sharedPreferences.edit();
                if(openDetectionState){
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, false);
                    et.commit();
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    textOpenDetectionTime.setVisibility(View.GONE);
                    if(mCountDownTimer != null){
                        mCountDownTimer.cancel();
                        mCountDownTimer = null;
                    }
                } else {
                    Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                    intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                    startActivity(intentOpen);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                    et.putBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
                    et.commit();
                    textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                    textOpenDetectionTime.setVisibility(View.VISIBLE);
                    timeCountDown(2 * 60 * 1000);
                }
                openDetectionState = !openDetectionState;
                break;
            case R.id.device_name_set:
                setBluetoothNameDialog();
                setBluetoothNameDialog.show();
                break;
        }
    }

    // 重新命名本设备蓝牙名称
    private void setBluetoothNameDialog() {
        View dialog = LayoutInflater.from(this).inflate(R.layout.dialog_set_bluetooth_name, null);
        final EditText editNewName = (EditText) dialog.findViewById(R.id.edit_new_name);
        setBluetoothNameDialog = new Dialog(this, R.style.MyDialog);
        setBluetoothNameDialog.setContentView(dialog);
        setBluetoothNameDialog.setCanceledOnTouchOutside(false);
        setBluetoothNameDialog.getWindow().setBackgroundDrawableResource(R.color.dialog);
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = editNewName.getText().toString();
                if(!TextUtils.isEmpty(newName.trim())) {
                    MineActivity.blueAdapter.setName(newName);
                    textBluetoothName.setText(newName);
                    setBluetoothNameDialog.dismiss();
                } else {
                    Toast.makeText(BluetoothActivity.this, "设备名字不能为空，请重新输入!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBluetoothNameDialog.dismiss();
            }
        });
    }

    // 蓝牙设置
    private void setBluetooth(){
        if(MineActivity.blueAdapter != null){  // 设备支持蓝牙
            // 确认开启蓝牙
            MineActivity.blueAdapter.enable(); // 直接开启，不经过提示
//                Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 请求用户开启
//                startActivityForResult(intent, RESULT_FIRST_USER);
//            SharedPreferences.Editor et = sharedPreferences.edit();
            imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
//            et.putBoolean(StringConstant.BLUETOOTH_SET, true);
            openDetection.setClickable(true);
            openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);
            if(openDetectionState){
                imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                Intent intentOpen = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);// 使蓝牙设备可见，方便配对
                intentOpen.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);// 默认两分钟
                startActivity(intentOpen);
                timeCountDown(2 * 60 * 1000);
            } else {
                imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
            }
        } else{   // 设备不支持蓝牙
            Toast.makeText(BluetoothActivity.this, "设备不支持蓝牙!", Toast.LENGTH_SHORT).show();
        }
    }

    // 蓝牙搜索状态广播监听
    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                if(MineActivity.blueAdapter.getState() == BluetoothAdapter.STATE_OFF){
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_close);
                    imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                    openDetection.setClickable(false);
                    textOpenDetection.setTextColor(getResources().getColor(R.color.textshang2));
                    openDetection.setBackgroundDrawable(null);
                    textUserDevice.setVisibility(View.GONE);
                    textPairDevice.setVisibility(View.GONE);
                    pairBluetoothList.setVisibility(View.GONE);
                    linearBluetoothSearch.setVisibility(View.GONE);
                    textOpenDetectionTime.setVisibility(View.GONE);
                    textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                    userBluetoothList.setDividerHeight(0);
                    userList.clear();
//                    userList.add("");
                    if(userAdapter == null){
                        userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList));
                    } else {
                        userAdapter.notifyDataSetChanged();
                    }
                    MineActivity.blueAdapter.startDiscovery();
                } else if(MineActivity.blueAdapter.getState() == BluetoothAdapter.STATE_ON){
                    imageBluetoothSet.setImageResource(R.mipmap.wt_person_on);
                    openDetection.setClickable(true);
                    openDetectionState = sharedPreferences.getBoolean(StringConstant.BLUETOOTH_OPEN_DETECTION_SET, true);// 开放蓝牙检测开关
                    if(openDetectionState){
                        imageOpenDetection.setImageResource(R.mipmap.wt_person_on);
                        textOpenDetectionInfo.setText("对附近所有的蓝牙设备可见");
                        textOpenDetectionTime.setVisibility(View.VISIBLE);
                    } else {
                        imageOpenDetection.setImageResource(R.mipmap.wt_person_close);
                        textOpenDetectionInfo.setText("仅让已配对的蓝牙设备可见");
                        textOpenDetectionTime.setVisibility(View.GONE);
                    }
                    textOpenDetection.setTextColor(getResources().getColor(R.color.wt_login_third));
                    openDetection.setBackgroundDrawable(getResources().getDrawable(R.drawable.person_color));
                    textUserDevice.setVisibility(View.VISIBLE);
                    textPairDevice.setVisibility(View.VISIBLE);
                    pairBluetoothList.setVisibility(View.VISIBLE);
                    linearBluetoothSearch.setVisibility(View.VISIBLE);
                    userBluetoothList.setDividerHeight(1);
                    findAvalibleDevice();
                }
            }

            if(action.equals(StringConstant.UPDATE_BLUETO0TH_TIME)){
                timeCountDown(2 * 60 * 1000);
            } else if(action.equals(StringConstant.UPDATE_BLUETO0TH_TIME_OFF)){
                if(mCountDownTimer != null){
                    mCountDownTimer.cancel();
                }
            }
            if(BluetoothDevice.ACTION_FOUND.equals(action)){    // 搜索到新设备
                if(userList == null){
                    userList = new ArrayList<>();
                }
                BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {// 搜索没有配过对的蓝牙设备
                    BluetoothInfo bName = new BluetoothInfo();
                    bName.setBluetoothName(btd.getName());
                    bName.setBluetoothAddress(btd.getAddress());
                    userList.add(bName);
                    if(userAdapter == null) {
                        userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList));
                    } else {
                        userAdapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){// 搜索结束
                if (userBluetoothList.getCount() == 0) {
//                    userList.add("No can be matched to use bluetooth");
                    BluetoothInfo bName = new BluetoothInfo();
                    bName.setBluetoothName("No can be matched to use bluetooth");
                    bName.setBluetoothAddress("No can be matched to use bluetooth");
                    userList.add(bName);
                    if(userAdapter == null) {
                        userBluetoothList.setAdapter(userAdapter = new UserBluetoothAdapter(context, userList));
                    } else {
                        userAdapter.notifyDataSetChanged();
                    }
                    L.w("搜索结束");
                }
            }

            if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)){
                //自动确认配对
                BluetoothDevice  device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(("00:17:6F:68:B0:91").equals(device.getAddress())){
                    L.i("PairingConfirmation\n");
                    try{
                        Method setPairingConfirmationMethod = BluetoothDevice.class.getMethod("setPairingConfirmation",new Class[]{boolean.class});
                        setPairingConfirmationMethod.invoke(device,true);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 获取已经配对的蓝牙设备
     */
    private void findAvalibleDevice(){
        if(pairList == null){
            pairList = new ArrayList<>();
        } else {
            pairList.clear();
        }
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device = MineActivity.blueAdapter.getBondedDevices();
        if(device.size() > 0){ //存在已经配对过的蓝牙设备
            L.w("有配对过的蓝牙设备 ==== > > > out" + device.size());
            Iterator<BluetoothDevice> it = device.iterator();
            while (it.hasNext()){
                BluetoothDevice btd = it.next();
                BluetoothInfo bName = new BluetoothInfo();
                bName.setBluetoothName(btd.getName());
                bName.setBluetoothAddress(btd.getAddress());
                pairList.add(bName);
                L.w("有配对过的蓝牙设备 ==== > > > in" + btd.getAddress());
                L.w("有配对过的蓝牙设备 ==== > > > in" + btd.getName());
                if(pairAdapter == null){
                    pairBluetoothList.setAdapter(pairAdapter = new PairBluetoothAdapter(context, pairList));
                } else {
                    pairAdapter.notifyDataSetChanged();
                }
            }
            L.w("--- > > " + pairList.size());
            for(int i=0; i<pairList.size(); i++){
                L.i(pairList.get(i).getBluetoothName());
            }
        } else {
            BluetoothInfo bName = new BluetoothInfo();
            bName.setBluetoothName("No can be matched to use bluetooth");
            bName.setBluetoothAddress("No can be matched to use bluetooth");
            pairList.add(bName);
            pairBluetoothList.setAdapter(pairAdapter = new PairBluetoothAdapter(context, pairList));
        }
    }

    /**
     * 子条目点击事件  发送配对请求
     */
    private void setItemLis(){
        userBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!MineActivity.blueAdapter.isEnabled()) {
                    return ;
                }
                if(position - 1 >= 0) {
                    String address = userList.get(position - 1).getBluetoothAddress();
                    if(address != null && !address.equals("No can be matched to use bluetooth")) {
                        MineActivity.blueAdapter.cancelDiscovery();
                        BluetoothDevice mBluetoothDevice = MineActivity.blueAdapter.getRemoteDevice(address);
                        try {
                            // 连接建立之前的先配对
                            Method createBond = BluetoothDevice.class.getMethod("createBond");
                            L.e("TAG", "开始配对");
                            createBond.invoke(mBluetoothDevice);
                        } catch (Exception e) {
                            //DisplayMessage("无法配对！");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * 点击取消已配对的蓝牙设备
         */
        pairAdapter.setListener(new PairBluetoothAdapter.CancelListener() {
            @Override
            public void cancelPair(int p) {
                if(!MineActivity.blueAdapter.isEnabled()){
                    return ;
                }
                try {
                    BluetoothDevice btDevice = MineActivity.blueAdapter.getRemoteDevice(pairList.get(p).getBluetoothAddress());
                    Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
                    Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
                    if(returnValue){
                        Toast.makeText(BluetoothActivity.this, p + "--- > 取消配对", Toast.LENGTH_SHORT).show();
                        findAvalibleDevice();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 倒计时
     */
    private void timeCountDown(long EndTime){
        mCountDownTimer = new CountDownTimer(EndTime, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                time = format.format(millisUntilFinished);
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
            }
            @Override
            public void onFinish() {
                Message message = Message.obtain();
                message.what = 0;
                handler.sendMessage(message);
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        MineActivity.blueAdapter.cancelDiscovery();// 停止蓝牙搜索
        if(hasRegister){
            hasRegister = false;
            unregisterReceiver(mDevice);
        }
        super.onDestroy();
    }
}
