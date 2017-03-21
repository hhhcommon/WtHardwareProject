//package com.test;
//
//import java.io.IOException;
//import java.security.InvalidParameterException;
//
//import com.mediatek.engineermode.io.EmGpio;
//
//import android.app.Activity;
//import android.app.ActionBar;
//import android.app.Fragment;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.os.Build;
//import android_serialport_api.ComBean;
//import android_serialport_api.SerialHelper;
//
//public class MainActivity extends Activity {
//
//	final int GPIO_DEVICE_OPEN = 12;
//	final int GPIO_AUDIO_EN	= 58;
//	final int GPIO_PTT	= 59;
////	final int DEVICE_OPEN = 1;
////	final int DEVICE_CLOSE = 0;
////	final int AUDIO_OPEN = 1;
////	final int AUDIO_CLOSE = 0;
////	final int PTT_OPEN = 1;
//	Button btnOpenDevice;
//	Button btnTalk;
//	final String STATE_OPEN = "开机";
//	final String STATE_CLOSE = "关机";
//	TextView textView;
//	SerialControl serialControl;
//	Handler handler;
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		ButtonListener b = new ButtonListener();
//		EmGpio.gpioInit();
//		EmGpio.setGpioOutput(GPIO_AUDIO_EN);
//		EmGpio.setGpioOutput(GPIO_DEVICE_OPEN);
//		EmGpio.setGpioOutput(GPIO_PTT);
//
//		serialControl = new SerialControl();
//        serialControl.setPort("/dev/ttyMT2");
//        serialControl.setBaudRate("9600");
//
//        OpenComPort(serialControl);
//
//		//EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);//关机
//		//EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
//		//EmGpio.setGpioDataHigh(GPIO_PTT);
//
//		//closeDevice();
//		textView = (TextView)findViewById(R.id.text);
//		btnOpenDevice = (Button)findViewById(R.id.btn_open);
//		btnTalk = (Button)findViewById(R.id.btn_talk);
//		btnTalk.setOnTouchListener(b);
//		btnOpenDevice.setText(STATE_OPEN);
//		if (btnOpenDevice.getText().toString().equals(STATE_OPEN)){
//			openDevice();
//		}else{
//			closeDevice();
//		}
//
//		//handler = new Handler();
//		//handler.postDelayed(runnable, 2000);
//
//	}
//
//	public void onGetState(View view){
//		if (btnOpenDevice.getText().toString().equals(STATE_OPEN)){
//			byte k[] = {0x41, 0x54, 0x2b, 0x44, 0x4d, 0x4f, 0x43, 0x4f, 0x4e, 0x4e, 0x45, 0x43, 0x54, 0x0d, 0x0a};
//			serialControl.send(k);
//		}
//
//		Log.i("7889", "-------------fsfsd");
//	}
//
//	Runnable runnable = new Runnable() {
//
//		@Override
//		public void run() {
//			Log.i("7889", "==========send======");
//
//			byte k[] = {0x41, 0x54, 0x2b, 0x44, 0x4d, 0x4f, 0x43, 0x4f, 0x4e, 0x4e, 0x45, 0x43, 0x54, 0x0d, 0x0a};
//			serialControl.send(k);
//			//serialControl.sendTxt("AT+DMOCONNECT\r\n");
//			//serialControl.sendTxt("AT+DMOCONNECT");
//			//serialControl.sendTxt("AT+DMOCONNECT");
//			handler.postDelayed(runnable, 2000);
//		}
//	};
//
//	class ButtonListener implements OnClickListener, OnTouchListener{
//
//        public void onClick(View v) {
////            if(v.getId() == R.id.button1){
////                Log.d("test", "cansal button ---> click");
////            }
//        }
//
//        public boolean onTouch(View v, MotionEvent event) {
//        	if (btnOpenDevice.getText().toString().equals(STATE_CLOSE)){
//    			//Toast.makeText(MainActivity.this, "请先开机", Toast.LENGTH_SHORT).show();
//    			return false;
//    		}
//
//            if(v.getId() == R.id.btn_talk){
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                	Log.i("7889", "==========recv");
//                	openDevice();
//                }
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                	Log.i("7889", "==========talk");
//                	talk();
//                }
//            }
//            return false;
//        }
//
//    }
//
//	public void onTalk(View view){
//		if (btnOpenDevice.getText().toString().equals(STATE_CLOSE)){
//			Toast.makeText(this, "请先开机", Toast.LENGTH_SHORT).show();
//			return;
//		}
//
//	}
//
//	public void onOpenDevice(View view){
//		if (btnOpenDevice.getText().toString().equals(STATE_CLOSE)){
//			btnOpenDevice.setText(STATE_OPEN);
//			OpenComPort(serialControl);
//			openDevice();
//		}else {
//			CloseComPort(serialControl);
//			btnOpenDevice.setText(STATE_CLOSE);
//			closeDevice();
//		}
//	}
//
//	public void talk(){
//		EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);
//		EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
//		EmGpio.setGpioDataLow(GPIO_PTT);
////		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
////				EmGpio.getCurrent(GPIO_AUDIO_EN),
////				EmGpio.getCurrent(GPIO_PTT)));
//	}
//
//	public void openDevice(){
//		EmGpio.setGpioDataHigh(GPIO_DEVICE_OPEN);//开机
//		EmGpio.setGpioDataHigh(GPIO_AUDIO_EN);
//		EmGpio.setGpioDataHigh(GPIO_PTT);
////		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
////				EmGpio.getCurrent(GPIO_AUDIO_EN),
////				EmGpio.getCurrent(GPIO_PTT)));
//	}
//
//	public void closeDevice(){
//		EmGpio.setGpioDataLow(GPIO_DEVICE_OPEN);//关机
//		EmGpio.setGpioDataLow(GPIO_AUDIO_EN);
//		EmGpio.setGpioDataHigh(GPIO_PTT);
////		textView.setText(String.format("开机:%d audio:%d ptt:%d", EmGpio.getCurrent(GPIO_DEVICE_OPEN),
////				EmGpio.getCurrent(GPIO_AUDIO_EN),
////				EmGpio.getCurrent(GPIO_PTT)));
//	}
//
//	private void CloseComPort(SerialHelper ComPort){
//        if (ComPort!=null){
//            ComPort.stopSend();
//            ComPort.close();
//        }
//    }
//    //----------------------------------------------------打开串口
//    private void OpenComPort(SerialHelper ComPort){
//        try
//        {
//            ComPort.open();
//        } catch (SecurityException e) {
//            //ShowMessage("打开串口失败:没有串口读/写权限!");
//            Log.i("7889", "打开串口失败:没有串口读/写权限!");
//        } catch (IOException e) {
//            //ShowMessage("打开串口失败:未知错误!");
//            Log.i("7889", "打开串口失败:未知错误!");
//        } catch (InvalidParameterException e) {
//            //ShowMessage("打开串口失败:参数错误!");
//            Log.i("7889", "打开串口失败:参数错误!");
//        }
//    }
//
//	private class SerialControl extends SerialHelper{
//
////      public SerialControl(String sPort, String sBaudRate){
////          super(sPort, sBaudRate);
////      }
//        public SerialControl(){
//        }
//
//        @Override
//        protected void onDataReceived(final ComBean ComRecData)
//        {
//
//        	//if (ComRecData.bRec)
//        	MainActivity.this.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					((TextView)findViewById(R.id.text_recv)).setText(new String(ComRecData.bRec));
//					Log.i("7889", "=======123============" + new String(ComRecData.bRec));
//				}
//			});
//        	//String string = new String(ComRecData.bRec);
//        	//Log.i("7889", "=======123============" + string);
//            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
//            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
//            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
//            //最终效果差不多-_-，线程定时刷新稍好一些。
//            //DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
//            //Log.i("7889", "uart recv" + ComRecData.bRec);
//            //ProUtil.PrintfBytes("uart recv:", ComRecData.bRec);
//
////            if (ComRecData.bRec.length >= 4){
//////                if (radarCallback != null){
//////                    radarCallback.result((ComRecData.bRec[1]<<8 & 0xff00) | (ComRecData.bRec[2] & 0xff));
//////                }
////            }
//            /*
//            runOnUiThread(new Runnable()//直接刷新显示
//            {
//                public void run()
//                {
//                    DispRecData(ComRecData);
//                }
//            });*/
//        }
//    }
//}
