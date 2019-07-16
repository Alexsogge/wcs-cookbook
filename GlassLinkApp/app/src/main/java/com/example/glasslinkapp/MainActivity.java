package com.example.glasslinkapp;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private BluetoothManager bluetoothManager;
    private WebAPIManager webAPIManager = null;

    private LinearLayout sessionList;
    private Button[] sessionButtons;

    private TextView devices;

    Button sendTestButton;


    interface SockResponseCallbackInterface {
        void sockResponseCallback(String text);
        void sessionCallback(JSONArray sessions);
        void loginCallback(boolean loggedIn);
    }



    interface BluetoothCallbackInterface {
        void msgRecived(String message);
        void connectedDevice(String device);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // the activity for this is pretty stripped, just a basic selection ui....
        setContentView(R.layout.activity_main);

        devices = (TextView) findViewById(R.id.connected_devices_values);

        bluetoothManager = new BluetoothManager(new BluetoothCallbackInterface() {
            @Override
            public void msgRecived(String message) {

            }

            @Override
            public void connectedDevice(String device) {
                devices.append(device + "\n");
            }
        });

        sendTestButton = (Button)findViewById(R.id.sendtest);
        sendTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothManager.setMsg("Hello World test");
            }
        });

        sessionList = (LinearLayout) findViewById(R.id.sessionlist);
        connectWebAPI();
    }


    public void connectWebAPI() {
        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);


        final Intent i = new Intent(this, Config.class);

        if (username == null || password == null) {
            startActivity(i);
        }
        else {
            Log.d("activity", "start webApiManager");

            webAPIManager = new WebAPIManager(username, password, new SockResponseCallbackInterface() {

                @Override
                public void sockResponseCallback(final String text) {
                    bluetoothManager.setMsg(text);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (webAPIManager == null) {
            connectWebAPI();
        }
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
}
