package com.wotingfm.common.gatherdata.thread;

import android.util.Log;

import com.wotingfm.common.gatherdata.GatherData;
import com.wotingfm.common.gatherdata.model.DataModel;
import com.wotingfm.common.volley.VolleyRequest;
import com.wotingfm.util.JsonEncloseUtils;

import java.util.ArrayList;

/**
 * 定时、定量上传数据线程
 * Created by Administrator on 2017/4/11.
 */
public class GivenUploadDataThread extends Thread {

    @Override
    public void run() {
        while (GatherData.isRun) {
            try {
                if (GatherData.givenList.size() >= GatherData.uploadCount) {
                    ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < GatherData.givenList.size(); i++) {
                        DataModel n = GatherData.givenList.get(i);
                        String jsonStr = JsonEncloseUtils.btToString(n);
                        list.add(jsonStr);
                    }

                    String jsonStr = JsonEncloseUtils.jsonEnclose(list).toString();
                    if (jsonStr != null) {
                        Log.v("TAG", "GIVEN jsonStr -- > > " + jsonStr);

                        // 上传数据
                        VolleyRequest.updateData(jsonStr);
                        GatherData.givenList.clear();
                        list.clear();
                    }
                }

                Thread.sleep(5 * 1000 * 60);// 五分钟检查一次  如果有数据则上传
            } catch (Exception e) {
                e.printStackTrace();
                GatherData.givenList.clear();
            }
        }
    }
}
