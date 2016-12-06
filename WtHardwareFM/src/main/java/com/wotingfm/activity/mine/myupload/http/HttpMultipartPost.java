package com.wotingfm.activity.mine.myupload.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wotingfm.R;
import com.wotingfm.activity.mine.myupload.model.FileContentInfo;
import com.wotingfm.activity.mine.myupload.upload.UploadActivity;
import com.wotingfm.common.config.GlobalConfig;
import com.wotingfm.util.CommonUtils;
import com.wotingfm.util.PhoneMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Multipart 上传
 */
public class HttpMultipartPost extends AsyncTask<String, Integer, String> {
    private Context context;
    private List<String> filePathList;

    private ProgressDialog pd;
    private TextView textProgress;
    private ProgressBar progressBar;

    private long totalSize;
    private int srcType;// == 1 图片  == 2 音频

    public HttpMultipartPost(Context context, List<String> filePathList, int srcType) {
        this.context = context;
        this.filePathList = filePathList;
        this.srcType = srcType;
    }

    @Override
    protected void onPreExecute() {
        View progressView = LayoutInflater.from(context).inflate(R.layout.progress_dialog_view, null);
        textProgress = (TextView) progressView.findViewById(R.id.text_progress);
        progressBar = (ProgressBar) progressView.findViewById(R.id.pb_progressbar);
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.show();
        pd.setContentView(progressView);
    }

    @Override
    protected String doInBackground(String... params) {
        String serverResponse = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(GlobalConfig.uploadFileUrl);

        try {
            CustomMultipartEntity multipartContent = new CustomMultipartEntity(new CustomMultipartEntity.ProgressListener() {
                @Override
                public void transferred(long num) {
                    publishProgress((int) ((num / (float) totalSize) * 100));
                }
            });

            // 把上传内容添加到MultipartEntity
            for (int i = 0; i < filePathList.size(); i++) {
                multipartContent.addPart("DeviceId", new StringBody(PhoneMessage.imei));
                multipartContent.addPart("PCDType", new StringBody("1"));
                multipartContent.addPart("MobileClass", new StringBody(PhoneMessage.model + "::" + PhoneMessage.productor));
                multipartContent.addPart("UserId", new StringBody(CommonUtils.getUserId(context)));
                multipartContent.addPart("ContentFile", new FileBody(new File(filePathList.get(i))));
                multipartContent.addPart("SrcType", new StringBody(String.valueOf(srcType)));
                if(srcType == 1) {
                    multipartContent.addPart("Purpose", new StringBody("2"));
                } else {
                    multipartContent.addPart("Purpose", new StringBody("1"));
                }
                multipartContent.addPart("data", new StringBody(filePathList.get(i), Charset.forName(org.apache.http.protocol.HTTP.UTF_8)));
            }
            totalSize = multipartContent.getContentLength();
            System.out.println("totalSize:=========" + totalSize);
            httpPost.setEntity(multipartContent);
            HttpResponse response = httpClient.execute(httpPost, httpContext);
            serverResponse = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponse;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressBar.setProgress(progress[0]);
        if(progress[0] < 100) {
            textProgress.setText("文件上传中... " + progress[0] + "%");
        } else {
            textProgress.setText("系统处理中请稍等...");
        }
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("result: " + result);
        pd.dismiss();
        if(result == null || result.equals("null") || result.equals("")) {
            uploadFail();
            return ;
        }
        try {
            JSONTokener jsonParser = new JSONTokener(result);
            JSONObject arg1 = (JSONObject) jsonParser.nextValue();
            String ful = arg1.getString("ful");
            List<FileContentInfo> fileContentInfo = new Gson().fromJson(ful, new TypeToken<List<FileContentInfo>>() {}.getType());
            if(fileContentInfo.get(0).getSuccess().equals("TRUE")) {
                String filePath = fileContentInfo.get(0).getFilePath();
                ((UploadActivity)context).addFileContent(filePath);// 上传完成回到原界面
            } else {
                uploadFail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            uploadFail();
        }
    }

    private void uploadFail() {
        ProgressDialog uploadFailDialog = new ProgressDialog(context);
        uploadFailDialog.setCancelable(true);
        uploadFailDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadFailDialog.show();
        uploadFailDialog.setContentView(R.layout.dialog_upload_fail);
    }

    @Override
    protected void onCancelled() {
        System.out.println("cancel");
    }
}
