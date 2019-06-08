package com.example.cookbookmotion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static java.lang.Math.abs;


public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final int SENSOR_COUNT = 3;
    private static final double RATE = 50.;


    private TextView mTextView;
    private MotionClassifier motionClassifier;

    private SensorManager sensorManager;
    private Sensor mSensor;

    private ByteBuffer sensorData;
    private ByteBuffer sensorDataStream;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        // Enables Always-on
        setAmbientEnabled();


        try {
            motionClassifier = new MotionClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sensorDataStream = ByteBuffer.allocateDirect(SENSOR_COUNT * 2000 * 4);
        sensorDataStream.order(ByteOrder.nativeOrder());

        sensorData = ByteBuffer.allocateDirect(SENSOR_COUNT * 100 * 4);
        sensorData.order(ByteOrder.nativeOrder());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("Sensor", event.sensor.getStringType());
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0] / 15;
        float y = values[1] / 15;
        float z = values[2] / 15;


        sensorDataStream.putFloat(x);
        sensorDataStream.putFloat(y);
        sensorDataStream.putFloat(z);

        if (sensorDataStream.remaining() == 0) {
            Log.d("Sensor", "Buffer full!!");
            // float[][] labelProbArray = motionClassifier.RunInference(sensorData);
            // Log.d("Pred", "Predicted: " + labelProbArray[0][0] + " " + labelProbArray[0][1] + " " + labelProbArray[0][2]);
            sensorDataStream.clear();
        }

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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                20000);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }





}
