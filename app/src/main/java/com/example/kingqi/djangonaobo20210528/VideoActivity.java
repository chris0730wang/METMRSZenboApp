package com.example.kingqi.djangonaobo20210528;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoActivity extends AppCompatActivity {
    Button clk;
    VideoView videoView;
    static final String SERVER_IP = "140.134.26.196";
    private String content;
    private Handler handler = null;
    private String videopath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        checkreadingpart();
        clk = (Button) findViewById(R.id.videoplaybtn);
        videoView = (VideoView) findViewById(R.id.videoView) ;
        handler = new Handler();
    }

    public void videoplay(View v){
        System.out.println("hahaha" + videopath);
        Uri uri = Uri.parse(videopath);
        videoView.setVideoURI(uri);
        videoView.start();
    }

    public void checkreadingpart(){

        final Runnable UIRunnable = new  Runnable(){
            @Override
            public void run() {
                if(content.equals("test.mp4")){
                    videopath = "android.resource://com.example.kingqi.djangonaobo20210528/" + R.raw.test;
                }else if(content.equals("showqrcode.mp3")){
                    videopath = "android.resource://com.example.kingqi.djangonaobo20210528/" + R.raw.showqrcode;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://"+SERVER_IP+"/readingpartsettingandgetting/?access=getting")
                        .get()
                        .build();
                Response response = null;
                String resStr = null;
                try {
                    response = client.newCall(request).execute();
                    resStr = response.body().string();
                    JSONObject json = new JSONObject(resStr);
                    System.out.println(json.getString("result"));
                    content = json.getString("result");
                    handler.post(UIRunnable);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}