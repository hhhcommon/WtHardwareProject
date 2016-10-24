package com.wotingfm.helper;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MyHttp {
    private static String TAG = "MyHttp";
    private static final String CHARSET = "UTF-8";
    public static HttpClient httpClient;
    public static String cookieStr;

    public MyHttp() {
    }

    public static HttpClient getHttp() {
        if(httpClient == null) {
            BasicHttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUseExpectContinue(params, true);
            HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
            ConnManagerParams.setTimeout(params, 3000L);
            HttpConnectionParams.setConnectionTimeout(params, 5000);
            HttpConnectionParams.setSoTimeout(params, 8000);
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ThreadSafeClientConnManager conMgr = new ThreadSafeClientConnManager(params, schReg);
            httpClient = new DefaultHttpClient(conMgr, params);
        }

        return httpClient;
    }

    public static String httpGet(String paramString) throws UnsupportedEncodingException, IllegalStateException, ClientProtocolException, IOException {
        HttpClient httpClient = getHttp();
        HttpGet httpUriRequest = new HttpGet(paramString);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpClient.execute(httpUriRequest).getEntity().getContent(), "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String str = bufferedReader.readLine();
        CookieStore mCookieStore = ((AbstractHttpClient)httpClient).getCookieStore();
        List cookies = mCookieStore.getCookies();

        for(int i = 0; i < cookies.size(); ++i) {
            cookieStr = ((Cookie)cookies.get(i)).getName() + "=" + ((Cookie)cookies.get(i)).getValue();
        }

        if(str != null && !str.equals("")) {
            return str;
        } else {
            stringBuilder.append(str);
            str = bufferedReader.readLine();
            return null;
        }
    }

    public static String httpPost(String paramString, List<NameValuePair> paramList) throws ClientProtocolException, IOException {
        HttpClient defaultHttpClient = getHttp();
        HttpPost httpPost = new HttpPost(paramString);
        if(paramList != null) {
            ((HttpEntityEnclosingRequestBase)httpPost).setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
        }

        HttpEntity httpEntity = defaultHttpClient.execute(httpPost).getEntity();
        if(httpEntity == null) {
            return null;
        } else {
            String resultStr = EntityUtils.toString(httpEntity);
            CookieStore mCookieStore = ((AbstractHttpClient)defaultHttpClient).getCookieStore();
            List cookies = mCookieStore.getCookies();

            for(int i = 0; i < cookies.size(); ++i) {
                cookieStr = ((Cookie)cookies.get(i)).getName() + "=" + ((Cookie)cookies.get(i)).getValue();
            }

            return resultStr;
        }
    }

    public static String postFile(File file, String url) throws ClientProtocolException, IOException {
        FileBody bin = null;
        HttpClient httpclient = getHttp();
        HttpPost httppost = new HttpPost(url);
        if(file != null) {
            bin = new FileBody(file);
        }

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("data", bin);
        httppost.setEntity(reqEntity);
        Log.i(TAG, "执行: " + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
        Log.i(TAG, "statusCode is " + response.getStatusLine().getStatusCode());
        HttpEntity resEntity = response.getEntity();
        Log.i(TAG, "" + response.getStatusLine());
        if(resEntity != null) {
            String resultStr = EntityUtils.toString(resEntity);
            resEntity.consumeContent();
            return resultStr;
        } else {
            return null;
        }
    }

    public static String getByUrl(String urlpath, String encoding) throws Exception {
        URL url = new URL(urlpath);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        if(conn.getResponseCode() == 200) {
            InputStream inStream = conn.getInputStream();
            byte[] data = readStream(inStream);
            Log.e("ndk", new String(data));
            return new String(data, encoding);
        } else {
            return null;
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        boolean len = true;

        int len1;
        while((len1 = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len1);
        }

        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
