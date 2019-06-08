package com.example.cookbookmotion;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class MotionClassifier {

    private Interpreter tfInterpreter;
    private List<String> labelList;


    /** Options for configuring the Interpreter. */
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    /** The loaded TensorFlow Lite model. */
    private MappedByteBuffer tfliteModel;


    public void Run() {

    }


    protected MotionClassifier(Activity activity) throws IOException{
        // tfInterpreter = new Interpreter(loadModelFile(activity));
        tfliteModel = loadModelFile(activity);
        tfInterpreter = new Interpreter(tfliteModel, tfliteOptions);
        labelList = new ArrayList<String>();
        labelList.add("noise");
        labelList.add("left");
        labelList.add("right");

        Log.d("Tensorflow", "Created a Tensorflow Lite Gesture Classifier.");
    }


    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("mobilenet_v1_1.0_224.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


}
