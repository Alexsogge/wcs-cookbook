package com.example.glasslinktest;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {

    private BluetoothManager bluetoothManager;

    private TextView nbrTextView;
    private TextView descTextView;

    interface BluetoothCallbackInterface {
        void msgRecived(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //      publishCards(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_activity_layout);

        bluetoothManager = new BluetoothManager(this, new BluetoothCallbackInterface() {
            @Override
            public void msgRecived(String message) {
                try {
                    JSONObject jobj = new JSONObject(message).getJSONObject("message");
                    nbrTextView.setText("Step: " + jobj.getInt("new_step"));
                    descTextView.setText(jobj.getString("step_desc"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        nbrTextView = (TextView)findViewById(R.id.stepnbr);
        descTextView = (TextView)findViewById(R.id.descTextView);

    }

    @Override
    public void onDestroy() {
        bluetoothManager.close();
        super.onDestroy();

    }
}
