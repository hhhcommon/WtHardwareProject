package com.wotingfm.ui.mine;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.engineermode.io.EmGpio;
import com.wotingfm.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;

/**
 * FM 测试界面
 */
public class FMTestActivity extends Activity {
    final static int GPIO_DEVICE_OPEN = 12;
    final static int GPIO_AUDIO_EN = 58;
    final static int GPIO_PTT = 59;
    //	final int DEVICE_OPEN = 1;
//	final int DEVICE_CLOSE = 0;
//	final int AUDIO_OPEN = 1;
//	final int AUDIO_CLOSE = 0;
//	final int PTT_OPEN = 1;
    LinearLayout btnOpenDevice;
    Button btnTalk;
    final String STATE_OPEN = "开机";
    final String STATE_CLOSE = "关机";
    TextView textView, tv_kgtp;
    SerialControl serialControl;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fm_test);

        EmGpio.gpioInit();
        EmGpio.setGpioOutput(GPIO_AUDIO_EN);
        EmGpio.setGpioOutput(GPIO_DEVICE_OPEN);
        EmGpio.setGpioOutput(GPIO_PTT);

        serialControl = new SerialControl();
        serialControl.setPort("/dev/ttyMT2");
        serialControl.setBaudRate("9600");
        OpenComPort(serialControl);

        textView = (TextView) findViewById(R.id.text);

        tv_kgtp = (TextView) findViewById(R.id.tv_kgtp);
        btnOpenDevice = (LinearLayout) findViewById(R.id.btn_open);

        btnTalk = (Button) findViewById(R.id.btn_talk);
        btnTalk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (tv_kgtp.getText().toString().equals(STATE_CLOSE)) {
                    //Toast.makeText(MainActivity.this, "请先开机", Toast.LENGTH_SHORT).show();
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("按钮操作", "按下");
                        talk();//按下状态
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e("按钮操作", "松手");
                        openDevice();//抬起手后的操作
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.e("按钮操作", "取消");
                        openDevice();//抬起手后的操作
                        break;
                }
                return false;
            }
        });

        // 获取版本
        findViewById(R.id.btn_a).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialControl.sendTxt("AT+DMOVERQ\r\n");
            }
        });

        // 声控命令
        // 1-8：声控等级参数，等级越小越灵敏
        // 0：关闭声控功能（不为零则表示开启）
        findViewById(R.id.btn_b).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialControl.sendTxt("AT+DMOSETVOX=0\r\n");
            }
        });

        // 音量命令
        // 1-8：等级越高输出音量越大（最大320mv）
        findViewById(R.id.btn_c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialControl.sendTxt("AT+DMOSETVOLUME=5\r\n");
            }
        });

        // 设置频率12.5与25的整倍数（400.0000-470.0000）
        findViewById(R.id.btn_d).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialControl.sendTxt("AT+DMOSETGROUP=1,409.7500,409.7500,00,3,1\r\n");
            }
        });

        tv_kgtp.setText(STATE_OPEN);
        if (tv_kgtp.getText().toString().equals(STATE_OPEN)) {
            openDevice();
        } else {
            closeDevice();
        }

        //handler = new Handler();
        //handler.postDelayed(runnable, 2000);

    }

    public void onGetState(View view) {
        if (tv_kgtp.getText().toString().equals(STATE_OPEN)) {
            byte k[] = {0x41, 0x54, 0x2b, 0x44, 0x4d, 0x4f, 0x43, 0x4f, 0x4e, 0x4e, 0x45, 0x43, 0x54, 0x0d, 0x0a};
            serialControl.send(k);
        }

        Log.i("7889", "-------------fsfsd");
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            Log.i("7889", "==========send======");

            byte k[] = {0x41, 0x54, 0x2b, 0x44, 0x4d, 0x4f, 0x43, 0x4f, 0x4e, 0x4e, 0x45, 0x43, 0x54, 0x0d, 0x0a};
            serialControl.send(k);
            //serialControl.sendTxt("AT+DMOCONNECT\r\n");
            //serialControl.sendTxt("AT+DMOCONNECT");
            //serialControl.sendTxt("AT+DMOCONNECT");
            handler.postDelayed(runnable, 2000);
        }
    };

    class ButtonListener implements View.OnTouchListener {

        public boolean onTouch(View v, MotionEvent event) {

            if (v.getId() == R.id.btn_talk) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i("7889", "==========recv");

                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i("7889", "==========talk");

                }
            }
            return false;
        }

    }

    public void onTalk(View view) {
        if (tv_kgtp.getText().toString().equals(STATE_CLOSE)) {
            Toast.makeText(this, "请先开机", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    public void onOpenDevice(View view) {
        if (tv_kgtp.getText().toString().equals(STATE_CLOSE)) {
            tv_kgtp.setText(STATE_OPEN);
            OpenComPort(serialControl);
            openDevice();
        } else {
            CloseComPort(serialControl);
            tv_kgtp.setText(STATE_CLOSE);
            closeDevice();
        }
    }

    public void talk() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataLow(GPIO_PTT);
//		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
//				EmGpio.getCurrent(GPIO_AUDIO_EN),
//				EmGpio.getCurrent(GPIO_PTT)));
    }

    public static void openDevice() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);//开机
        EmGpio.setGpioDataHigh(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
//		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
//				EmGpio.getCurrent(GPIO_AUDIO_EN),
//				EmGpio.getCurrent(GPIO_PTT)));
    }

    public void closeDevice() {
        EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);//关机
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
//		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
//				EmGpio.getCurrent(GPIO_AUDIO_EN),
//				EmGpio.getCurrent(GPIO_PTT)));
    }

    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            //ShowMessage("打开串口失败:没有串口读/写权限!");
            Log.i("7889", "打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            //ShowMessage("打开串口失败:未知错误!");
            Log.i("7889", "打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            //ShowMessage("打开串口失败:参数错误!");
            Log.i("7889", "打开串口失败:参数错误!");
        }
    }

    private class SerialControl extends SerialHelper {

        //      public SerialControl(String sPort, String sBaudRate){
//          super(sPort, sBaudRate);
//      }
        public SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {

            //if (ComRecData.bRec)
            FMTestActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                /*    ((TextView) findViewById(R.id.text_recv)).setText(new String(ComRecData.bRec));*/
                    try {
                        Log.e("0000===7889", "=======123============" + new String(ComRecData.bRec).getBytes("utf-8"));
                        byte[] b = ComRecData.bRec;
                        for (int i = 0; i < b.length; i++) {
                            Log.e("硬件反馈消息===","["+i+"]==="+ b[i]);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
//            String string = new String(ComRecData.bRec);
//            Log.e("2222227889", "=======123============" + string);
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
            //DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
            //Log.i("7889", "uart recv" + ComRecData.bRec);
            //ProUtil.PrintfBytes("uart recv:", ComRecData.bRec);

//            if (ComRecData.bRec.length >= 4){
////                if (radarCallback != null){
////                    radarCallback.result((ComRecData.bRec[1]<<8 & 0xff00) | (ComRecData.bRec[2] & 0xff));
////                }
//            }
            /*
            runOnUiThread(new Runnable()//直接刷新显示
            {
                public void run()
                {
                    DispRecData(ComRecData);
                }
            });*/
        }
    }
}
