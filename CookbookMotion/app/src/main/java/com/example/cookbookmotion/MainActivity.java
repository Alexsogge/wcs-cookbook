package com.example.cookbookmotion;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.activity.WearableActivity;
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

    private static final int SENSOR_COUNT = 3;
    private static final double RATE = 50.;


    private TextView mTextView;
    private MotionClassifier motionClassifier;

    private MotionRecorder motionRecorder;
    private WebAPIManager webAPIManager;

    private Button next;

    private LinearLayout sessionList;

    private Button[] sessionButtons;

    interface PeakCallbackInterface {
        boolean classifyPeak(ByteBuffer frame);
    }

    interface SockResponseCallbackInterface {
        void sockResponseCallback(String text);
        void sessionCallback(JSONArray sessions);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        // Enables Always-on
        setAmbientEnabled();

        sessionList = (LinearLayout) findViewById(R.id.sessionlist);


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
        motionRecorder.startRecording();
        webAPIManager = new WebAPIManager("user", "password", new SockResponseCallbackInterface(){

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
                }, 1000 );
            }

            @Override
            public void sessionCallback(JSONArray sessions) {
                buildSessionList(sessions);
            }
        }, this);
        next = (Button) findViewById(R.id.button3);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Test", "Pressed button");
                webAPIManager.nextStep();
            }
        });

    }


    private void buildSessionList(final JSONArray sessions){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sessionButtons = new Button[sessions.length()];

                for (int i = 0; i < sessions.length(); i++) {
                    try {
                        final JSONObject session = sessions.getJSONObject(i);
                        sessionButtons[i] = new Button(getApplicationContext());
                        sessionButtons[i].setText(session.getString("recipeName"));
                        sessionButtons[i].setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.FILL_PARENT,
                                ActionBar.LayoutParams.WRAP_CONTENT));
                        sessionButtons[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d("Test", "Pressed button");
                                try {
                                    webAPIManager.connectSession(session.getInt("id"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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

        if (gesture.equals("Left"))
            webAPIManager.prevStep();
        if (gesture.equals("Right"))
            webAPIManager.nextStep();

        if (gesture.equals("Noise"))
            return false;
        return true;
    }

    /*
    private void CheckGesture(){
        int currentStreamPos = sensorDataStream.position();

        if (currentStreamPos > SENSOR_COUNT * 101 * 4 ) {
            // Log.d("cut", "VAL: "+ sensorDataStream.getFloat(currentStreamPos - 50 * SENSOR_COUNT * 4 + 8) + " at " + (currentStreamPos - 50 * SENSOR_COUNT * 4 + 8));
            if (abs(sensorDataStream.getFloat(currentStreamPos - 50 * SENSOR_COUNT * 4 + 8)) > 0.53 && abs(sensorDataStream.getFloat(currentStreamPos - 100 * SENSOR_COUNT * 4 + 8)) < 0.30) {
                Log.d("cut", "Found Peak");
                sensorData.clear();
                String line = "0: [";
                int linenum = 1;
                int readed_sensors = 0;
                for (int i = currentStreamPos - 100 * SENSOR_COUNT * 4; i < currentStreamPos; i += 4){
                    sensorData.putFloat(sensorDataStream.getFloat(i));
                    line += sensorDataStream.getFloat(i) + ", ";
                    readed_sensors += 1;
                    if (readed_sensors == 3){
                        readed_sensors = 0;
                        Log.d("cut", line + "]");
                        line = linenum + ": [";
                        linenum += 1;
                    }

                }
                Log.d("cut", sensorData.toString());
                float[][] labelProbArray = motionClassifier.RunInference(sensorData);

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

                sensorDataStream.clear();

            }
        }
    }
    */
}
