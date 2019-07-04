package com.example.glasslinktest;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;

    private View mView;


    private WebAPIManager webAPIManager;

    interface SockResponseCallbackInterface {
        void sockResponseCallback(String text);
        void sessionCallback(JSONArray sessions);
        void loginCallback(boolean loggedIn);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mView = buildView("Start cookbook");

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });


        webAPIManager = new WebAPIManager("alex", "stein123", new SockResponseCallbackInterface() {

            @Override
            public void sockResponseCallback(final String text) {
                Log.d("test", text);
                    /* Handler handler = new Handler(Looper.getMainLooper());

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int duration = Toast.LENGTH_SHORT;
                            CharSequence chText = text;

                            Toast toast = Toast.makeText(getApplicationContext(), chText, duration);
                            toast.show();
                        }
                    }, 1000);*/

                try {
                    JSONObject jsono = new JSONObject(text).getJSONObject("message");
                    final String step = jsono.getString("step_desc");
                    Log.d("session", step);
                    Log.d("view", mView.getClass().getName());
                    RelativeLayout relViev = (RelativeLayout)mView;

                    for(int i = 0; i < relViev.getChildCount(); i++) {
                        View child = relViev.getChildAt(i);
                        Log.d("view", i + ": " + child.getClass().getName());
                    }
                    Log.d("view", "in layout");
                    FrameLayout linearLayout = (FrameLayout) relViev.getChildAt(1);
                    for(int i = 0; i < linearLayout.getChildCount(); i++) {
                        View child = linearLayout.getChildAt(i);
                        Log.d("view", child.getClass().getName());
                    }
                    final TextView textv = (TextView)relViev.getChildAt(2);
                    Log.d("view", "Text:");
                    Log.d("view", textv.getText().toString());
                    Log.d("view", textv.toString());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            textv.setText(step);
                            // Stuff that updates the UI

                        }
                    });
                    //textv.setText(step);
                    //setContentView(buildView(step));
                    Log.d("view", "set text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void sessionCallback(JSONArray sessions) {
                webAPIManager.connectSession(2);
            }

            @Override
            public void loginCallback(boolean loggedIn) {

            }


        }, this);



        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);
            }
        });
        setContentView(mCardScroller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    /**
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
    private View buildView(String displayText) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        card.setText(displayText);
        return card.getView();
    }

}
