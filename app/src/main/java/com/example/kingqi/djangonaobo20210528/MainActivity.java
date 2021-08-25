package com.example.kingqi.djangonaobo20210528;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.asus.robotframework.API.Utility;
import com.example.robotactivitylibrsry.RobotActivity;
import com.asus.robotframework.API.DialogSystem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sql.StatementEvent;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.asus.robotframework.API.Utility.PlayAction.Dance_2_loop;
import static com.asus.robotframework.API.Utility.PlayAction.Dance_3_loop;
import static com.asus.robotframework.API.Utility.PlayAction.Head_up_7;
import com.asus.robotframework.API.RobotCommand;

public class MainActivity extends RobotActivity{

    static final String SERVER_IP = "140.134.26.196";
    private Button fab;
    private Button studentbtn1, studentbtn2, studentbtn3, studentbtn4, studentbtn5, studentbtn6, readybtn, readingexplanation;
    private String content = null, issue = null, sampleanswer = null;
    public String[] bundlescanresult = new String[]{"student 1", "student 2", "student 3", "student 4", "student 5", "student 6"};
    private Handler handler = null;
    public SpeakConfig config = new SpeakConfig();
    private boolean stop = false, checkreadingpartornot=true, jumptoscan=true;
    private int[] sectionrobottalk = new int[]{0,0,0,0,0,0,0};
    public JSONArray classidinfo, classgroupinfo, classstudentinfo;
    public String nowclassid, nowgroup;

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //設定顯示介面
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getclassinfo("getclassid");
        config.languageId(2);
        config.readMode(SpeakConfig.READ_MODE_SENTENCE);
        config.pitch(80);
        config.volume(50);
        init();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
        handler = new Handler();
        //相機權限android 6.0版本要詢問
        getCameraPermission();
    }

    public void getclassinfo(final String access){
        if(access.equals("getclassid")){
            final Runnable UIRunnable = new  Runnable(){
                @Override
                public void run() {
                    try {
                        List<String> classidinfotostring = new ArrayList<String>();
                        for(int i=0; i<classidinfo.length(); i++){
                            classidinfotostring.add(classidinfo.getString(i));
                        }
                        AlertDialog.Builder classidtochoose = new AlertDialog.Builder(MainActivity.this);
                        classidtochoose.setTitle("choose the class id");
                        classidtochoose.setIcon(R.mipmap.ic_launcher);
                        classidtochoose.setSingleChoiceItems(classidinfotostring.toArray(new String[classidinfotostring.size()]), -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    nowclassid = classidinfo.getString(i);
                                    getclassinfo("getgroups");
                                    dialogInterface.dismiss();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        classidtochoose.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        AlertDialog classidtochoosedialog = classidtochoose.create();
                        classidtochoosedialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://"+SERVER_IP+"/getclassinfo/?access="+access)
                            .get()
                            .build();
                    Response response = null;
                    String resStr = null;
                    try {
                        response = client.newCall(request).execute();
                        resStr = response.body().string();
                        JSONArray json = new JSONArray(resStr);
                        System.out.println(json.toString());
                        classidinfo = json;
                        handler.post(UIRunnable);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else if(access.equals("getgroups")){
            final Runnable UIRunnable = new  Runnable(){
                @Override
                public void run() {
                    try {
                        List<String> classgroupinfotostring = new ArrayList<String>();
                        for(int i=0; i<classgroupinfo.length(); i++){
                            classgroupinfotostring.add(classgroupinfo.getString(i));
                        }
                        AlertDialog.Builder classgrouptochoose = new AlertDialog.Builder(MainActivity.this);
                        classgrouptochoose.setTitle("choose the group number");
                        classgrouptochoose.setIcon(R.mipmap.ic_launcher);
                        classgrouptochoose.setSingleChoiceItems(classgroupinfotostring.toArray(new String[classgroupinfotostring.size()]), -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    nowgroup = classgroupinfo.getString(i).substring(5);
                                    getclassinfo("getstudentlist");
                                    dialogInterface.dismiss();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        classgrouptochoose.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        AlertDialog classgrouptochoosedialog = classgrouptochoose.create();
                        classgrouptochoosedialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://"+SERVER_IP+"/getclassinfo/?access="+access+"&classid="+nowclassid)
                            .get()
                            .build();
                    Response response = null;
                    String resStr = null;
                    try {
                        response = client.newCall(request).execute();
                        resStr = response.body().string();
                        JSONArray json = new JSONArray(resStr);
                        System.out.println(json.toString());
                        classgroupinfo = json;
                        handler.post(UIRunnable);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else if(access.equals("getstudentlist")){
            final Runnable UIRunnable = new  Runnable(){
                @Override
                public void run() {
                    setstudentsid(classstudentinfo);
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://"+SERVER_IP+"/getclassinfo/?access="+access+"&classid="+nowclassid+"&group="+nowgroup)
                            .get()
                            .build();
                    Response response = null;
                    String resStr = null;
                    try {
                        response = client.newCall(request).execute();
                        resStr = response.body().string();
                        JSONArray json = new JSONArray(resStr);
                        System.out.println(json.toString());
                        classstudentinfo = json;
                        handler.post(UIRunnable);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
    }

    public void setstudentsid(JSONArray studentids){
        try {
            for(int i=0; i<studentids.length(); i++){
                switch (i){
                    case 0:
                        studentbtn1.setText(studentids.getString(0));
                        break;
                    case 1:
                        studentbtn2.setText(studentids.getString(1));
                        break;
                    case 2:
                        studentbtn3.setText(studentids.getString(2));
                        break;
                    case 3:
                        studentbtn4.setText(studentids.getString(3));
                        break;
                    case 4:
                        studentbtn5.setText(studentids.getString(4));
                        break;
                    case 5:
                        studentbtn6.setText(studentids.getString(5));
                        break;
                    default:
                        break;
                }
            }
            TextView Grouptext = (TextView) findViewById(R.id.groupnum);
            String showgroupinzenbo = "Group      " + nowgroup;
            Grouptext.setText(showgroupinzenbo);
            checksection();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init(){

        fab = (Button) findViewById(R.id.SC);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(MainActivity.this, qrcode_scanner.class);
                intent.putExtra("group",nowgroup);
                startActivityForResult(intent,1);
            }
        });

        studentbtn1 = (Button) findViewById(R.id.student1);
        studentbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        studentbtn2 = (Button) findViewById(R.id.student2);
        studentbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        studentbtn3 = (Button) findViewById(R.id.student3);
        studentbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        studentbtn4 = (Button) findViewById(R.id.student4);
        studentbtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        studentbtn5 = (Button) findViewById(R.id.student5);
        studentbtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        studentbtn6 = (Button) findViewById(R.id.student6);
        studentbtn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

            }
        });

        readybtn = (Button) findViewById(R.id.readybtn);
        readybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Runnable UIRunnable = new  Runnable(){
                    @Override
                    public void run() {
                    }
                };

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://"+SERVER_IP+"/zenbogetready/?group="+nowgroup)
                                .get()
                                .build();
                        Response response = null;
                        String resStr = null;
                        try {
                            response = client.newCall(request).execute();
                            resStr = response.body().string();
                            JSONObject json = new JSONObject(resStr);
                            System.out.println(json.getString("result"));
                            content = json.getString("section");
                            handler.post(UIRunnable);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        readingexplanation = (Button) findViewById(R.id.readingexplanation);
        readingexplanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });

    }

    public void zenbogetdiscussion(){
        final String[] settextstring = {""};
        final TextView discussion = (TextView) findViewById(R.id.discussion);
        final Runnable UIRunnable = new  Runnable(){
            @Override
            public void run() {
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                settextstring[0] = "Question：" + issue + "\n" + "Sample Answer：" + sampleanswer;
                discussion.setText(settextstring[0]);
                discussion.setTextSize(30);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://"+SERVER_IP+"/zenbogetdiscussion/?group="+nowgroup)
                        .get()
                        .build();
                Response response = null;
                String resStr = null;
                try {
                    response = client.newCall(request).execute();
                    resStr = response.body().string();
                    JSONObject json = new JSONObject(resStr);
                    System.out.println(json.getString("issue"));
                    issue = json.getString("issue");
                    sampleanswer = json.getString("sampleanswer");
                    handler.post(UIRunnable);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void checksection(){

        final TextView section = (TextView) findViewById(R.id.textView);
        final Runnable UIRunnable = new  Runnable(){
            @Override
            public void run() {
                robotAPI.cancelCommand(RobotCommand.MOTION_PLAY_ACTION.getValue());
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                if(content.equals("section")){
                    section.setText("Connected !");
                    stop = false;
                }
                else if(content.equals("studentcheck")){
                    section.setText("Press Scan ID button to check !");
                }else if(content.equals("exercise")){
                    robotAPI.utility.playAction(Dance_3_loop);
                    robotAPI.robot.setExpression(RobotFace.HAPPY);
                }else if(content.equals("reading")){
                    readingexplanation.setVisibility(View.VISIBLE);
                }else if(content.equals("discussion")){
                    zenbogetdiscussion();
                }else{
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stop){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://"+SERVER_IP+"/zenbochecksection/?group="+nowgroup)
                            .get()
                            .build();
                    Response response = null;
                    String resStr = null;
                    try {
                        response = client.newCall(request).execute();
                        resStr = response.body().string();
                        JSONObject json = new JSONObject(resStr);
                        System.out.println(json.getString("section"));
                        content = json.getString("section");
                        handler.post(UIRunnable);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resume Here");
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                Bundle bundleresult = data.getExtras();
                bundlescanresult[0] = bundleresult.getString("resultcId1");
                bundlescanresult[1] = bundleresult.getString("resultcId2");
                bundlescanresult[2] = bundleresult.getString("resultcId3");
                bundlescanresult[3] = bundleresult.getString("resultcId4");
                bundlescanresult[4] = bundleresult.getString("resultcId5");
                bundlescanresult[5] = bundleresult.getString("resultcId6");
                for(int i=0; i<6; i++){
                    if (studentbtn1.getText().toString().equals(bundlescanresult[i])) studentbtn1.setBackgroundResource(R.drawable.button_shape3);
                    else if (studentbtn2.getText().toString().equals(bundlescanresult[i])) studentbtn2.setBackgroundResource(R.drawable.button_shape3);
                    else if (studentbtn3.getText().toString().equals(bundlescanresult[i])) studentbtn3.setBackgroundResource(R.drawable.button_shape3);
                    else if (studentbtn4.getText().toString().equals(bundlescanresult[i])) studentbtn4.setBackgroundResource(R.drawable.button_shape3);
                    else if (studentbtn5.getText().toString().equals(bundlescanresult[i])) studentbtn5.setBackgroundResource(R.drawable.button_shape3);
                    else if (studentbtn6.getText().toString().equals(bundlescanresult[i])) studentbtn6.setBackgroundResource(R.drawable.button_shape3);
                }
                jumptoscan = true;
            }
        }else if(requestCode == 2){
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("I'm pause");
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    //相機權限詢問
    public void getCameraPermission(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
    }

}

