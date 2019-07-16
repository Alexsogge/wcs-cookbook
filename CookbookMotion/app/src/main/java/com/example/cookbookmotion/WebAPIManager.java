package com.example.cookbookmotion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class WebAPIManager {

    //private static final String WEB_HOST_NAME = "http://localhost:8000";
    //private static final String SOCK_HOST_NAME = "ws://localhost:8000";

    private String username;
    private String password;

    private OkHttpClient client;

    private WebConnection webConnection;
    private SocketConnection socketConnection;
    private WebSocket socket;
    private MainActivity.SockResponseCallbackInterface sockCallResp;

    private String token;

    private Context context;


    interface HTTPResponseCallbackInterface {
        void httpResponseCallback(String response);
    }


    public WebAPIManager(String username, String password, MainActivity.SockResponseCallbackInterface sockCallResp, Context context){
        this.username = username;
        this.password = password;
        this.sockCallResp = sockCallResp;
        this.context = context;

        client = new OkHttpClient();
        webConnection = (WebConnection) new WebConnection(username, password, new HTTPResponseCallbackInterface() {
            @Override
            public void httpResponseCallback(String response) {
                httpResponse(response);
                authendicated();
                if (token != null)
                    getSessions();
            }
        }, null).execute("");
    }

    public void connectSession(int sessionID){
        Request request = new Request.Builder().url(ConfigValues.SOCK_HOST_NAME + "/ws/api/" + sessionID + "/?Authorization="+webConnection.getToken()).build();
        socketConnection = new SocketConnection();
        WebSocket ws = client.newWebSocket(request, socketConnection);
        socket = ws;
        //this.debugMessage("New Wear connected");
        //client.dispatcher().executorService().shutdown();
    }

    private void authendicated(){
        token = webConnection.getToken();
        if (token == null){
            sockCallResp.loginCallback(false);
        }
    }

    private void httpResponse(String response){
        Log.d("HTTP", response);
    }

    public void nextStep(){
        Map<String, String> msg = new HashMap<String, String>();
        msg.put("message", "next_step");
        JSONObject msg_obj = new JSONObject(msg);
        socket.send(msg_obj.toString());
    }

    public void prevStep() {
        Map<String, String> msg = new HashMap<String, String>();
        msg.put("message", "previous_step");
        JSONObject msg_obj = new JSONObject(msg);
        socket.send(msg_obj.toString());
    }

    public void getSessions(){
        webConnection = (WebConnection)new WebConnection(username, password, new HTTPResponseCallbackInterface() {
            @Override
            public void httpResponseCallback(String response) {
                Log.d("HTTP", response);
                try {
                    JSONArray mainObject = new JSONArray(response);
                    ArrayList<String> listdata = new ArrayList<String>();
                    for (int i = 0; i < mainObject.length(); i++){
                        Log.d("HTTPJ", mainObject.get(i).toString());
                    }
                    sockCallResp.sessionCallback(mainObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, token).execute(ConfigValues.WEB_HOST_NAME + "/api/getsessions/");
    }

    private void output(final String txt) {
        Log.d("Test", txt);
        //sockCallResp.sockResponseCallback(txt);
    }


    public void close(){
        //socket.close(0, "End");
    }

    public void debugMessage(String message) {
        this.socketConnection.sendMessage("{\"message\": \"debug\", \"debug\": \"" + message + "\"}");
    }

    private final class SocketConnection extends WebSocketListener{
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        private WebSocket socket;


        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            //webSocket.send("");
            //webSocket.send("What's up ?");
            //webSocket.send(ByteString.decodeHex("deadbeef"));
            // webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
            output("OPen : " + response.toString());
            this.socket = webSocket;
        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Receiving : " + text);
            sockCallResp.sockResponseCallback(text);
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

        public void sendMessage(String message){
            this.socket.send(message);
        }
    }


    private class WebConnection extends AsyncTask<String, String, String> {

        private String username;
        private String password;

        private String token = null;

        private HTTPResponseCallbackInterface httpResponceInterface;

        private WebConnection(String username, String password, HTTPResponseCallbackInterface httpResponceInterface, String token){
            this.username = username;
            this.password = password;
            this.httpResponceInterface = httpResponceInterface;
            this.token = token;
            //this.execute("");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("HTTP", "Connect to " + params[0] + " with token " + token);

            if (token == null) authenticate();
            if (params[0].equals("")) return "";



            String urlString = params[0]; // URL to call
            // String data = params[1]; //data to post

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;

            String data = null;



            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setRequestProperty("Authorization","JWT " + token);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }
                httpResponceInterface.httpResponseCallback(total.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            try {
                data = URLEncoder.encode("Content-Type", "UTF-8")
                        + "=" + URLEncoder.encode("application/json", "UTF-8");
                data += "&" + URLEncoder.encode("Authorization", "UTF-8")
                        + "=" + URLEncoder.encode("JWT " + token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write( data );
                wr.flush();

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = null;
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                httpResponceInterface.httpResponseCallback(sb.toString());

            } catch (Exception e) {
                Log.v("Test", e.getMessage());
            }*/
            Log.v("Test", "Return Token: " + token);

            return null;
        }


        private void authenticate(){
            BufferedReader reader = null;

            String data = null;
            try {
                data = URLEncoder.encode("username", "UTF-8")
                        + "=" + URLEncoder.encode(username, "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }



            try {
                URL url = new URL(ConfigValues.WEB_HOST_NAME + "/api/api-token-auth/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write( data );
                wr.flush();

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                Log.v("Test", sb.toString());
                JSONObject mainObject = new JSONObject(sb.toString());
                token = mainObject.getString("token");

            } catch (Exception e) {
                Log.v("Test", e.getMessage());
            }
            Log.v("Test", "Return Token: " + token);
            httpResponceInterface.httpResponseCallback("Authendicated");
        }


        public String getToken() {
            return token;
        }
    }

}
