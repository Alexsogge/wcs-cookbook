package com.example.cookbook;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class MainActivity extends AppCompatActivity {

    private Button start;
    private Button prev;
    private Button connect;
    private EditText ipInput;
    private Button next;
    private EditText sessionInput;
    private TextView output;
    private OkHttpClient client;
    private WebSocket socket;
    private String token;
    private CallAPI tokenapi;

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;


        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            //webSocket.send("");
            //webSocket.send("What's up ?");
            //webSocket.send(ByteString.decodeHex("deadbeef"));
            // webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
        }
    }

    public class CallAPI extends AsyncTask<String, String, String> {

        String token;


        public CallAPI(){
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            // String data = params[1]; //data to post
            OutputStream out = null;
            Log.v("Test", "Stasrt http");
            BufferedReader reader=null;

            String data = null;
            try {
                data = URLEncoder.encode("username", "UTF-8")
                        + "=" + URLEncoder.encode("alex", "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode("stein123", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }



            try {
                URL url = new URL(urlString);
                Log.v("Test", "1");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                Log.v("Test", "2");
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write( data );
                wr.flush();

                // Get the server response
                Log.v("Test", "3");
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                Log.v("Test", "4");
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                Log.v("Test", "5");
                //Log.v("Test", sb.toString());
                JSONObject mainObject = new JSONObject(sb.toString());
                token = mainObject.getString("token");
                Log.v("Test", token);

            } catch (Exception e) {
                Log.v("Test", e.getMessage());
            }
            Log.v("Test", "Return Token: " + token);
            return null;
        }

        public String getToken() {
            return token;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.start);
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        output = (TextView) findViewById(R.id.output);
        sessionInput = (EditText) findViewById(R.id.session);
        connect = (Button) findViewById(R.id.connect);
        ipInput = (EditText) findViewById(R.id.ipfield);

        client = new OkHttpClient();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevStep();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextStep();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tokenapi = (CallAPI) new CallAPI().execute("http://192.168.1.102:8000/api/api-token-auth/");
            }
        });
        Log.v("Test", "Stasrt app");

    }




    private void start() {
        Request request = new Request.Builder().url("ws://" + ipInput.getText() + ":8000/ws/api/" + sessionInput.getText() + "/?Authorization="+tokenapi.getToken()).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
        socket = ws;
        client.dispatcher().executorService().shutdown();
    }

    private void nextStep() {
        Map<String, String> msg = new HashMap<String, String>();
        msg.put("message", "next_step");
        JSONObject msg_obj = new JSONObject(msg);
        socket.send(msg_obj.toString());
    }

    private void prevStep() {
        Map<String, String> msg = new HashMap<String, String>();
        msg.put("message", "previous_step");
        JSONObject msg_obj = new JSONObject(msg);
        socket.send(msg_obj.toString());
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(output.getText().toString() + "\n\n" + txt);
            }
        });

    }

}
