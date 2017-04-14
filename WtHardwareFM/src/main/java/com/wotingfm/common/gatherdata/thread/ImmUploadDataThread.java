package com.wotingfm.common.gatherdata.thread;

import android.util.Log;

import com.wotingfm.common.gatherdata.GatherData;
import com.wotingfm.common.gatherdata.model.DataModel;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.JsonEncloseUtils;

/**
 * 立即上传数据线程
 * Created by Administrator on 2017/4/11.
 */
public class ImmUploadDataThread extends Thread {

    @Override
    public void run() {
        while (GatherData.isRun) {
            try {
                DataModel data = GatherData.immQueue.take();
                if (data != null) {
                    String jsonStr = JsonEncloseUtils.btToString(data);
                    Log.v("TAG", "IMM jsonStr -- > > " + jsonStr);

                    // 上传数据
                    VolleyRequest.updateData(jsonStr);
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
