package com.example.kingqi.djangonaobo20210528;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.example.robotactivitylibrsry.RobotActivity;

import android.media.AudioManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.view.KeyEvent.*;


public class qrcode_scanner extends RobotActivity {

    static final String SERVER_IP = "140.134.26.196";
    static final int PORT = 1234;
    public int scantime = 0;
    public boolean checkscanornot = false;
    SpeakConfig config = new SpeakConfig();
    private Handler handler = null;
    private String week = "2";
    private String group = "";
    private String[] resultcId = {"","","","","",""};
    private MediaPlayer mediaPlayer;

    public qrcode_scanner() {
        super(robotCallback, robotListenCallback);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //設定顯示介面
        handler = new Handler();
        Intent intent = getIntent();
        group = intent.getStringExtra("group");
        System.out.println("jjjjjjjjjjjjjjjjj"+group);
        openqrcodescanner();
        config.languageId(2);
        config.readMode(SpeakConfig.READ_MODE_SENTENCE);
        config.pitch(100);
        config.volume(50);
        config.speed(95);
        robotAPI.robot.speak("Please show me your I D", config);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("scantime : "+ scantime);
        if (checkscanornot)qrcodecontinueornot();
    }

    public void qrcodecontinueornot(){
        AlertDialog.Builder checkbuilder = new AlertDialog.Builder(qrcode_scanner.this);
        checkbuilder.setTitle("簽到確認");
        checkbuilder.setMessage("是否繼續簽到 ? ");

        checkbuilder.setPositiveButton("繼續", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkscanornot = false;
                openqrcodescanner();
            }
        });
        checkbuilder.setNegativeButton("都已簽到完成", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://"+SERVER_IP+"/groupcheckok/?group="+group+"&week="+week)
                                .get()
                                .build();
                        try {
                            client.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                final Intent resultIntent = new Intent();
                Bundle bundleback = new Bundle();
                bundleback.putString("resultcId1", resultcId[0]);
                bundleback.putString("resultcId2", resultcId[1]);
                bundleback.putString("resultcId3", resultcId[2]);
                bundleback.putString("resultcId4", resultcId[3]);
                bundleback.putString("resultcId5", resultcId[4]);
                bundleback.putString("resultcId6", resultcId[5]);
                resultIntent.putExtras(bundleback);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        AlertDialog checkdialog = checkbuilder.create();
        checkdialog.show();
    }
    public void openqrcodescanner(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0); // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.addExtra("SCAN_WIDTH", 480);
        integrator.addExtra("SCAN_HEIGHT", 480);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        scantime++;
        checkscanornot = true;
        final Runnable UIRunnable = new  Runnable(){
            @Override
            public void run() {
            }
        };

        if(result != null) {
            if(result.getContents() != null)  {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                new Thread(new Runnable() {
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://"+SERVER_IP+"/customizestudentcheck/?zenbo="+group+"&cId="+result.getContents())
                                .get()
                                .build();
                        Response response = null;
                        String resStr = null;
                        try {
                            response = client.newCall(request).execute();
                            resStr = response.body().string();
                            JSONObject json = new JSONObject(resStr);
                            if(!json.getString("cGroup").equals(group)){
                                robotAPI.robot.speak("You are in group", config);
                                robotAPI.robot.speak(json.getString("cGroup"), config);
                                robotAPI.robot.speak("Please go back to your group", config);
                                scantime--;
                            }else{
                                switch (scantime){
                                    case 1:
                                        resultcId[0] = result.getContents();
                                        break;
                                    case 2:
                                        resultcId[1] = result.getContents();
                                        break;
                                    case 3:
                                        resultcId[2] = result.getContents();
                                        break;
                                    case 4:
                                        resultcId[3] = result.getContents();
                                        break;
                                    case 5:
                                        resultcId[4] = result.getContents();
                                        break;
                                    case 6:
                                        resultcId[5] = result.getContents();
                                        break;
                                    default:
                                        break;
                                }
                                robotAPI.robot.speak("I D" + result.getContents() + "checked", config);
                                handler.post(UIRunnable);
                            }
//                            System.out.println(json.getString("cId"));
//                            System.out.println("scantime" + scantime);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            /**
                             * Called when the request could not be executed due to cancellation, a connectivity problem or
                             * timeout. Because networks can fail during an exchange, it is possible that the remote server
                             * accepted the request before the failure.
                             *
                             * @param call
                             * @param e
                             */
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            /**
                             * Called when the HTTP response was successfully returned by the remote server. The callback may
                             * proceed to read the response body with . The response is still live until
                             * its response body is {@linkplain ResponseBody closed}. The recipient of the callback may
                             * consume the response body on another thread.
                             *
                             * <p>Note that transport-layer success (receiving a HTTP response code, headers and body) does
                             * not necessarily indicate application-layer success: {@code response} may still indicate an
                             * unhappy HTTP response code like 404 or 500.
                             *
                             * @param call
                             * @param response
                             */
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                            }

                        });
                    }
                }).start();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
}
