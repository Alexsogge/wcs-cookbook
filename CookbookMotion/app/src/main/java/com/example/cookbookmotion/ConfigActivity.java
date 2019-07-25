package com.example.cookbookmotion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigActivity extends WearableActivity {
    private static double FACTOR = 0.2; // c = a * sqrt(2)

    private Button login;
    private EditText userTextView;
    private EditText pwTextView;


    private SeekBar tsHighSlider;
    private SeekBar tsLowSlider;

    private TextView tsHighVal;
    private TextView tsLowVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Enables Always-on
        setAmbientEnabled();
        adjustInset();

        login = (Button) findViewById(R.id.loginBtn);
        userTextView = (EditText) findViewById(R.id.nameInput);
        pwTextView = (EditText) findViewById(R.id.pwInput);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                sp.edit().putString("username", userTextView.getText().toString()).apply();
                sp.edit().putString("password", pwTextView.getText().toString()).apply();

                goToMainActivity();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        SharedPreferences spconf = getSharedPreferences("config", MODE_PRIVATE);
        int filter = spconf.getInt("filtermethod", 0);
        spinner.setSelection(filter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("menue", "Pos: " + position);
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                sp.edit().putInt("filtermethod", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        tsHighSlider = (SeekBar)findViewById(R.id.tsHighSlider);
        tsHighVal = (TextView)findViewById(R.id.tsHighVal);
        tsHighVal.setText(Float.toString(spconf.getFloat("tsHigh", MotionRecorder.PEAK_THRESHOLD)));
        tsHighSlider.setProgress((int)(spconf.getFloat("tsHigh", MotionRecorder.PEAK_THRESHOLD) * (100 / 15.0)));
        tsHighSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newval = Math.round(progress * 1.5f) / 10.0f;
                Log.d("menu", "New threshold: " + newval);
                tsHighVal.setText(Float.toString(newval));
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                sp.edit().putFloat("tsHigh", newval).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tsLowSlider = (SeekBar)findViewById(R.id.tsLowSlider);
        tsLowVal = (TextView)findViewById(R.id.tsLowVal);
        tsLowVal.setText(Float.toString(spconf.getFloat("tsLow", MotionRecorder.LOW_THRESHOLD)));
        //Log.d("menu", "Set slider to " + spconf.getFloat("tsLow", MotionRecorder.tsLow) + " -> "+ (int)(spconf.getFloat("tsLow", MotionRecorder.tsLow) * (100 / 15.0)));
        tsLowSlider.setProgress((int)(spconf.getFloat("tsLow", MotionRecorder.LOW_THRESHOLD) * (100 / 15.0)));
        tsLowSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newval = Math.round(progress * 1.5f) / 10.0f;
                Log.d("menu", "New threshold: " + newval);
                tsLowVal.setText(Float.toString(newval));
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                sp.edit().putFloat("tsLow", newval).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    private void adjustInset() {
        if (getResources().getConfiguration().isScreenRound()) {
            int inset = (int)(FACTOR * getResources().getConfiguration().screenWidthDp);
            View layout = (View) findViewById(R.id.mainview);
            layout.setPadding(inset, inset, inset, inset);
        }
    }


    private void goToMainActivity(){
        finish();
    }
}
