package com.example.glasslinkapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothManager {

    public static String msgToSend="";
    public static final int STATE_CONNECTION_STARTED = 0;
    public static final int STATE_CONNECTION_LOST = 1;
    public static final int READY_TO_CONN = 2;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    MainActivity.BluetoothCallbackInterface bluetoothCallbackInterface;

    // our last connection
    ConnectedThread mConnectedThread;// = new ConnectedThread(socket);
    // track our connections
    ArrayList<ConnectedThread> mConnThreads;
    // bt adapter for all your bt needs (where we get all our bluetooth powers)
    BluetoothAdapter myBt;
    // list of sockets we have running (for multiple connections)
    ArrayList<BluetoothSocket> mSockets = new ArrayList<BluetoothSocket>();
    // list of addresses for devices we've connected to
    ArrayList<String> mDeviceAddresses = new ArrayList<String>();
    // just a name, nothing more...
    String NAME="DEVNAME";
    // We can handle up to 7 connections... or something...
    UUID[] uuids = new UUID[2];
    // some uuid's we like to use..
    String uuid1 = "05f2934c-1e81-4554-bb08-44aa761afbfb";
    String uuid2 = "c2911cd0-5c3c-11e3-949a-0800200c9a66";
    // just a tag..
    String TAG = "G6 Bluetooth Host Activity";
    // constant we define and pass to startActForResult (must be >0), that the system passes back to you in your onActivityResult()
    // implementation as the requestCode parameter.
    int REQUEST_ENABLE_BT = 1;
    AcceptThread accThread;
    Handler handle;
    BroadcastReceiver receiver;



    public BluetoothManager(MainActivity.BluetoothCallbackInterface callbackInterface) {
        uuids[0] = UUID.fromString(uuid1);
        uuids[1] = UUID.fromString(uuid2);
        this.bluetoothCallbackInterface = callbackInterface;
        handle = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case STATE_CONNECTION_STARTED:
                        Log.d(TAG, "Connection started with" + msg.getData().getString("NAMES"));
                        break;
                    case STATE_CONNECTION_LOST:
                        Log.d("DebugConnect", "STATE_CONNECTION_LOST");
                        bluetoothCallbackInterface.connectionLostDevice("");
                        startListening();
                        break;
                    case READY_TO_CONN:
                        startListening();
                    default:
                        break;
                }
            }
        };

        // ....
        myBt = BluetoothAdapter.getDefaultAdapter();
        // run the "go get em" thread..
        accThread = new AcceptThread();
        accThread.start();
    }


    public void startListening() {
        if(accThread!=null) {
            accThread.cancel();
            accThread = null;
        }else if (mConnectedThread!= null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        } else {
            accThread = new AcceptThread();
            accThread.start();
        }
    }



    private void manageConnectedSocket(BluetoothSocket socket) {
        // start our connection thread
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        // so the HH can show you it's working and stuff...
        String devs="";
        for(BluetoothSocket sock: mSockets) {
            devs+=sock.getRemoteDevice().getName()+"\n";
        }
        // pass it to the UI....
        Message msg = handle.obtainMessage(STATE_CONNECTION_STARTED);
        Bundle bundle = new Bundle();
        bundle.putString("NAMES", devs);
        msg.setData(bundle);

        handle.sendMessage(msg);
    }


    public static synchronized void setMsg(String newMsg) {
        msgToSend = newMsg;
    }

    public static class HostBroadRec extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b= intent.getExtras();
            String vals ="";
            for(String key: b.keySet()) {
                vals+=key+"&"+b.getString(key)+"Z";
            }
            setMsg(vals);
        }
    }



    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        BluetoothServerSocket tmp;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = myBt.listenUsingInsecureRfcommWithServiceRecord(NAME, uuids[0]);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.e(TAG,"Run AcceptThread");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.d(TAG, "Can't accept connection");
                    e.printStackTrace();
                    break;
                }
                // If a connection was accepted

                if (socket != null) {
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
                Message msg = handle.obtainMessage(READY_TO_CONN);
                handle.sendMessage(msg);
            } catch (IOException e) { }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private long lastPing;
        volatile boolean running = true;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            bluetoothCallbackInterface.connectedDevice(socket.getRemoteDevice().getName());

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (running) {
                if(! mmSocket.isConnected()){
                    Log.d("DebugConnect", "Connection Lost!");
                    connectionLost();

                }
                try {
                    if(!msgToSend.equals("")) {
                        Log.e(TAG,"writing!");
                        write(msgToSend.getBytes());
                        setMsg("");
                        lastPing = Calendar.getInstance().getTimeInMillis();
                    } else if (lastPing + 10000 < Calendar.getInstance().getTimeInMillis()){
                        // Log.d("DebugConnect", "Ping");
                        write("ping".getBytes());
                        lastPing = Calendar.getInstance().getTimeInMillis();
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                }
            }
        }
        public void connectionLost() {
            Message msg = handle.obtainMessage(STATE_CONNECTION_LOST);
            handle.sendMessage(msg);
        }
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                connectionLost();
                running = false;
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                Message msg = handle.obtainMessage(READY_TO_CONN);
                handle.sendMessage(msg);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
