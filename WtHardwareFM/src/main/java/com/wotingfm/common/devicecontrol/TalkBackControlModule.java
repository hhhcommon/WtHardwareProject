package com.wotingfm.common.devicecontrol;

import android.util.Log;

import com.mediatek.engineermode.io.EmGpio;

import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;

/**
 * author：辛龙 (xinLong)
 * 2017/2/13 16:33
 * 邮箱：645700751@qq.com
 */

public class TalkBackControlModule {

    final int GPIO_DEVICE_OPEN = 12;
    final int GPIO_AUDIO_EN = 58;
    final int GPIO_PTT = 59;

    SerialControl serialControl;

    private static class TalkBackControlModuleHolder {
        /**
         * 单例对象实例
         */
        static final TalkBackControlModule INSTANCE = new TalkBackControlModule();
    }

    public static TalkBackControlModule getInstance() {
        return TalkBackControlModuleHolder.INSTANCE;
    }

    /**
     * readResolve方法应对单例对象被序列化时候
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * private的构造函数用于避免外界直接使用new来实例化对象
     */
    private TalkBackControlModule() {

        //
        EmGpio.gpioInit();
        EmGpio.setGpioOutput(GPIO_AUDIO_EN);
        EmGpio.setGpioOutput(GPIO_DEVICE_OPEN);
        EmGpio.setGpioOutput(GPIO_PTT);

        // 串行？？？
        serialControl = new SerialControl();
        serialControl.setPort("/dev/ttyMT2");
        serialControl.setBaudRate("9600");
        OpenComPort(serialControl);
    }

    /**
     * 打开
     */
    public void open() {
        OpenComPort(serialControl);
        openDevice();
    }

    /**
     * 关闭
     */
    public void close() {
        CloseComPort(serialControl);
        closeDevice();
    }

    /**
     * 说话
     */
    public void talk() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataLow(GPIO_PTT);
    }

    /*
     * 开机
     */
    private void openDevice() {
        EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataHigh(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
    }

    /*
     * 关机
     */
    private void closeDevice() {
        EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);
        EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
        EmGpio.setGpioDataHigh(GPIO_PTT);
    }

    /*
     * 打开串口
     */
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            Log.e("7889", "打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            Log.e("7889", "打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            Log.e("7889", "打开串口失败:参数错误!");
        }
    }

    /*
     * 关闭串口
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
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
//            FMTestActivity.this.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    ((TextView) findViewById(R.id.text_recv)).setText(new String(ComRecData.bRec));
//                    Log.i("7889", "=======123============" + new String(ComRecData.bRec));
//                }
//            });
            //String string = new String(ComRecData.bRec);
            //Log.i("7889", "=======123============" + string);
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