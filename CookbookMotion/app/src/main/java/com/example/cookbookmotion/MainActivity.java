package com.example.cookbookmotion;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.activity.WearableActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static java.lang.Math.abs;


public class MainActivity extends WearableActivity{
    private static double FACTOR = 0.146467; // c = a * sqrt(2)

    private TextView mTextView;
    private MotionClassifier motionClassifier;

    private MotionRecorder motionRecorder;
    private WebAPIManager webAPIManager;

    private Button next;

    private Button confBtn;

    private LinearLayout sessionList;

    private Button[] sessionButtons;

    interface PeakCallbackInterface {
        boolean classifyPeak(ByteBuffer frame);
    }

    interface SockResponseCallbackInterface {
        void sockResponseCallback(String text);
        void sessionCallback(JSONArray sessions);
        void loginCallback(boolean loggedIn);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        // Enables Always-on
        setAmbientEnabled();

        sessionList = (LinearLayout) findViewById(R.id.sessionlist);
        /*SharedPreferences mySPrefs = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = mySPrefs.edit();
        editor.remove("username");
        editor.apply();*/
        adjustInset();
    }


    @Override
    protected void onStart() {
        super.onStart();

        try {
            motionClassifier = new MotionClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        motionRecorder = new MotionRecorder(this, new PeakCallbackInterface() {
            @Override
            public boolean classifyPeak(ByteBuffer frame) {
                return ClassifyPeak(frame);
            }
        });


        next = (Button) findViewById(R.id.button3);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "Pressed button");
                webAPIManager.nextStep();
            }
        });

        confBtn = (Button) findViewById(R.id.confButton);
        confBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);

        SharedPreferences sp_conf = getSharedPreferences("config", MODE_PRIVATE);
        int filterMethod = sp_conf.getInt("filtermethod", 0);
        this.motionRecorder.setFilterMethod(filterMethod);

        final Intent i = new Intent(this, ConfigActivity.class);

        if (username == null || password == null) {
            startActivity(i);
        }
        else {
            Log.d("activity", "start webApiManager");

            webAPIManager = new WebAPIManager(username, password, new SockResponseCallbackInterface() {

                @Override
                public void sockResponseCallback(final String text) {
                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int duration = Toast.LENGTH_SHORT;
                            CharSequence chText = text;

                            Toast toast = Toast.makeText(getApplicationContext(), chText, duration);
                            toast.show();
                        }
                    }, 1000);
                }

                @Override
                public void sessionCallback(JSONArray sessions) {
                    buildSessionList(sessions);
                }

                @Override
                public void loginCallback(boolean loggedIn) {
                    if (loggedIn == false) {
                        startActivity(i);
                    }
                }


            }, this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("activity", "Finish Mainactivity");
        //finishAndRemoveTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionRecorder.stopRecording();
        if (webAPIManager != null)
            webAPIManager.close();
    }

    private void buildSessionList(final JSONArray sessions){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sessionButtons != null) {
                    for (int i = 0; i < sessionButtons.length; i++) {
                        sessionList.removeView(sessionButtons[i]);
                    }
                }

                sessionButtons = new Button[sessions.length()];

                for (int i = 0; i < sessions.length(); i++) {
                    try {
                        final JSONObject session = sessions.getJSONObject(i);
                        sessionButtons[i] = new Button(getApplicationContext());
                        sessionButtons[i].setText(session.getString("recipeName"));
                        sessionButtons[i].setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT,
                                ActionBar.LayoutParams.WRAP_CONTENT));
                        final int finalI = i;
                        sessionButtons[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d("Test", "Pressed button");
                                    try {
                                        webAPIManager.connectSession(session.getInt("id"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    motionRecorder.startRecording();
                                    sessionButtons[finalI].setBackgroundColor(Color.BLUE);
                                }
                            });
                        sessionList.addView(sessionButtons[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private boolean ClassifyPeak(ByteBuffer frame){
        Log.d("callback", "Classify Peak");
        float[][] labelProbArray = motionClassifier.RunInference(frame);

        Log.d("Pred", "Predicted: " + labelProbArray[0][0] + " " + labelProbArray[0][1] + " " + labelProbArray[0][2]);

        float max_pred = labelProbArray[0][0];
        String gesture = "Noise";
        if (labelProbArray[0][1] > max_pred){
            gesture = "Left";
            max_pred = labelProbArray[0][1];
        }
        if (labelProbArray[0][2] > max_pred){
            gesture = "Right";
        }
        Log.d("Pred", "=>: " + gesture);
        TextView gestureList = (TextView)findViewById(R.id.textView2);
        gestureList.append(gesture + "\n");

        if (gesture.equals("Left")){
            webAPIManager.prevStep();
            webAPIManager.debugMessage("Classified left");
            motionRecorder.vibrate(new long[]{0, 200, 50, 200});
        }
        if (gesture.equals("Right")) {
            webAPIManager.nextStep();
            webAPIManager.debugMessage("Classified right");
            motionRecorder.vibrate(new long[]{0, 200, 50, 400});
        }

        if (gesture.equals("Noise")){
            webAPIManager.debugMessage("Classified Noise");
            return false;
        }

        return true;
    }


    private void adjustInset() {
        if (getResources().getConfiguration().isScreenRound()) {
            int inset = (int)(FACTOR * getResources().getConfiguration().screenWidthDp);
            View layout = (View) findViewById(R.id.mainview);
            layout.setPadding(inset, inset, inset, inset);
        }
    }
}
