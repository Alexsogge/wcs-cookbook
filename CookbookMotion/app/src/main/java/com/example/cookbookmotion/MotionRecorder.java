package com.example.cookbookmotion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.util.CircularArray;
import android.util.Log;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import static android.content.Context.VIBRATOR_SERVICE;
import static java.lang.Math.abs;

public class MotionRecorder implements SensorEventListener {

    private static final double RATE = 50.;
    private static final int WINDOW_SIZE = 100;
    private static final float PEAK_THRESHOLD = 9;
    private static final float LOW_THRESHOLD = 4;
    private static final int BORDER_POINTS = 10;
    private static final int SIMPLE_FILTER = 0;
    private static final int BORDER_FILTER = 1;

    private static final int DEFAULT_FILTER_METHOD = SIMPLE_FILTER;

    private static final float NORMALIZE_ACCELEROMETER = 15;
    private static final float NORMALIZE_GYROSCOPE = 3;
    private static final float NORMALIZE_MAGNETIC_FIELD = 40;


    private Context mContext;
    private MainActivity.PeakCallbackInterface callbackInterface;

    /* for start synchronization */
    private Long mStartTimeNS = -1l;
    private CountDownLatch mSyncLatch = null;
    private PowerManager.WakeLock mwl = null;

    private SensorManager sensorManager;
    private Sensor mSensor;
    private LinkedList<Sensor> mSensors;

    private ByteBuffer sensorData;
    private ByteBuffer sensorDataStream;

    private Hashtable<Sensor, FloatBuffer> channelBuffers;
    private SensorFrame sensorFrame;
    private int mChanelCount;

    private Calendar calendar;
    private long lastGesture;

    private int filterMethod = DEFAULT_FILTER_METHOD;


    public MotionRecorder(Context context, MainActivity.PeakCallbackInterface callbackInterface){
        mContext = context;
        this.callbackInterface = callbackInterface;
        this.calendar = Calendar.getInstance();
        this.lastGesture = System.currentTimeMillis();
    }

    public void setFilterMethod(int newMethod) {
        this.filterMethod = newMethod;
    }

    public void startRecording() {
        final SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        int[] types = {
                //Sensor.TYPE_ROTATION_VECTOR,
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                //Sensor.TYPE_MAGNETIC_FIELD,
/*                Sensor.TYPE_RELATIVE_HUMIDITY,
                Sensor.TYPE_PRESSURE,
                Sensor.TYPE_LIGHT,
                Sensor.TYPE_AMBIENT_TEMPERATURE */
        };
        mSensors = new LinkedList<>();

        for (int type : types) {
            Sensor s = sm.getDefaultSensor(type, true);

            if (s == null)
                s = sm.getDefaultSensor(type);

            if (s != null)
                mSensors.add(s);
        }

        int us = (int) (1e6 / RATE);

        mStartTimeNS = -1L;
        mSyncLatch = new CountDownLatch(mSensors.size());

        channelBuffers = new Hashtable<>();

        mChanelCount = 0;
        for (int i = 0; i < mSensors.size(); i++) {
            sm.registerListener(this, mSensors.get(i), us);
            channelBuffers.put(mSensors.get(i), FloatBuffer.allocate(getNumChannels(mSensors.get(i))));
            channelBuffers.get(mSensors.get(i)).order();
            mChanelCount += getNumChannels(mSensors.get(i));
        }
        sensorFrame = new SensorFrame(WINDOW_SIZE, mChanelCount);
    }


    public void stopRecording() {
        SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (System.currentTimeMillis() < this.lastGesture + 2000) {
            return;
        }

        Sensor sensor = sensorEvent.sensor;

        channelBuffers.get(sensor).clear();

        for (float value: sensorEvent.values){
            channelBuffers.get(sensor).put(normalizeSensordata(value, sensor));
        }

        boolean fullBuffers = true;
        for (FloatBuffer buffer: channelBuffers.values()){
            if (buffer.remaining() != 0) {
                fullBuffers = false;
                break;
            }
        }
        if (fullBuffers){
            flushBuffers();
        }
    }

    private float normalizeSensordata(float value, Sensor sensor){
        switch (sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                return value / NORMALIZE_ACCELEROMETER;
            case Sensor.TYPE_GYROSCOPE:
                return value / NORMALIZE_GYROSCOPE;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return value / NORMALIZE_MAGNETIC_FIELD;

            default:
                return value;
        }
    }

    private void flushBuffers(){
        boolean oldFullState = sensorFrame.isFull;
        for (int i = 0; i < mSensors.size(); i++) {
            sensorFrame.pushBuffer(channelBuffers.get(mSensors.get(i)));
            channelBuffers.get(mSensors.get(i)).clear();
        }
        if (sensorFrame.isFull && !oldFullState)
            Log.d("peak", "Filled frame");
        if (sensorFrame.isFull)findPeak();

    }


    private void findPeak() {
        boolean found_peak = false;

        if (this.filterMethod == SIMPLE_FILTER)
            found_peak = simpleFilter();
        if (this.filterMethod == BORDER_FILTER)
            found_peak = borderFilter();


        if (found_peak) {
            vibrate(new long[]{0, 100});
            boolean isGesture = callbackInterface.classifyPeak(sensorFrame.getFrame());
            if (isGesture) {
                sensorFrame.clear();
                this.lastGesture = System.currentTimeMillis();
            }
        }

        // Log.d("look", (accelerometerOffset + 2) + " " + ((WINDOW_SIZE / 2) * mChanelCount + accelerometerOffset + 2) + " " + ((WINDOW_SIZE - 1) * mChanelCount + accelerometerOffset + 2));

        // Log.d("look", "Test: "+ z_first + " "+ z_mid + " "+ z_last);

    }


    public void vibrate(long[] time) {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = time;
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }

    private boolean simpleFilter(){
        // get the pos where acc_sensor starts

        int accelerometerOffset = 0;

        for (int i = 0; i < mSensors.size(); i ++){
            if (mSensors.get(i).getType() != Sensor.TYPE_ACCELEROMETER)
                accelerometerOffset += getNumChannels(mSensors.get(i));
            else
                break;
        }


        // z value is on third pos ([2]) in acc values

        // first z value
        float z_first = sensorFrame.get(accelerometerOffset + 2);

        // mid z value
        float z_mid = sensorFrame.get((WINDOW_SIZE / 2) * mChanelCount + accelerometerOffset + 2);

        // last z value
        float z_last = sensorFrame.get((WINDOW_SIZE - 1) * mChanelCount + accelerometerOffset + 2);

        // Log.d("look", (accelerometerOffset + 2) + " " + ((WINDOW_SIZE / 2) * mChanelCount + accelerometerOffset + 2) + " " + ((WINDOW_SIZE - 1) * mChanelCount + accelerometerOffset + 2));

        // Log.d("look", "Test: "+ z_first + " "+ z_mid + " "+ z_last);


        if (abs(z_first) < LOW_THRESHOLD / NORMALIZE_ACCELEROMETER &&
                abs(z_mid) > PEAK_THRESHOLD / NORMALIZE_ACCELEROMETER &&
                abs(z_last) < LOW_THRESHOLD / NORMALIZE_ACCELEROMETER) {
            // Log.d("peak", "Found peak");
            // Log.d("peak", sensorFrame.toString());
            return true;
        }
        return false;
    }

    private boolean borderFilter(){
        // get the pos where acc_sensor starts

        int accelerometerOffset = 0;

        for (int i = 0; i < mSensors.size(); i ++){
            if (mSensors.get(i).getType() != Sensor.TYPE_ACCELEROMETER)
                accelerometerOffset += getNumChannels(mSensors.get(i));
            else
                break;
        }


        // z value is on third pos ([2]) in acc values

        int z_pos = accelerometerOffset + 2;

        // we search for a peak  in z axis where the median of the first and last 10 points is under a certain threshold


        // check for peak
        //Log.d("look", "peak? " + sensorFrame.getFrameSlize(WINDOW_SIZE / 2)[z_pos]);

        if (abs(sensorFrame.getFrameSlize(WINDOW_SIZE / 2)[z_pos]) < PEAK_THRESHOLD / NORMALIZE_ACCELEROMETER) return false;

        Log.d("look", "Found peak");

        // check for first points
        float z_first = 0;
        for (int i = 0; i < BORDER_POINTS; i++) {
            z_first += abs(sensorFrame.getFrameSlize(i)[z_pos]);
        }
        z_first /= 10;
        //Log.d("look", "z first: " + z_first);
        if (z_first > LOW_THRESHOLD / NORMALIZE_ACCELEROMETER) return false;
        Log.d("look", "z first: " + z_first);
        // check for last points
        float z_last = 0;
        for (int i = 0; i < BORDER_POINTS; i++) {
            z_last += abs(sensorFrame.getFrameSlize(WINDOW_SIZE - i)[z_pos]);
        }
        z_last /= 10;
        Log.d("look", "z last: " + z_last);
        if (z_last > LOW_THRESHOLD / NORMALIZE_ACCELEROMETER) return false;
        return true;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private int getNumChannels(Sensor s) {
        /*
         * https://developer.android.com/reference/android/hardware/SensorEvent#sensor
         */
        switch (s.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_MAGNETIC_FIELD:
                return 3;

            case Sensor.TYPE_ROTATION_VECTOR:
                return 5;


            default:
                return 0;
        }
    }


    private class SensorFrame {
        public int size;
        public int valuesPerFrame;
        private int maxSize;

        public boolean isFull = false;

        public float[] mData;

        private int pointer = 0;

        public SensorFrame(int size, int valuesPerFrame) {
            this.size = size;
            this.valuesPerFrame = valuesPerFrame;
            maxSize = size * valuesPerFrame;
            mData = new float[maxSize];
        }

        public void pushBuffer(FloatBuffer buffer) {
            for (int i = 0; i < buffer.capacity(); i++){
                mData[pointer] = buffer.get(i);
                pointer ++;
            }
            if (pointer == maxSize) {
                pointer = 0;
                isFull = true;
            }
        }

        public float get(int at) {
            return mData[(at + pointer) % maxSize];
        }

        public FloatBuffer getFrame(int at) {
            FloatBuffer frame = FloatBuffer.allocate(valuesPerFrame);
            for (int i = 0; i < valuesPerFrame; i++){
                frame.put(get(at + i));
            }
            return frame;
        }

        public ByteBuffer getFrame() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(maxSize*4);
            buffer.order(ByteOrder.nativeOrder());
            for (int i = 0; i < maxSize; i++){
                buffer.putFloat(get(i));
            }
            return buffer;
        }

        public float[] getFrameSlize(int at) {
            float[] frame = new float[this.valuesPerFrame];
            for (int i = 0; i < this.valuesPerFrame; i++) {
                frame[i] = this.get(at * this.valuesPerFrame + i);
            }

            return frame;
        }

        public void clear() {
            pointer = 0;
            isFull = false;
            mData = new float[maxSize];
        }

        public String toString() {
            String out = "";
            for (int i = 0; i < maxSize; i++) {
                if (i % valuesPerFrame == 0)
                    out += "[";
                out += get(i);

                if ((i+1) % valuesPerFrame == 0)
                    out += "]\n";
                else
                    out += ", ";
            }
            return out;
        }
    }


}
