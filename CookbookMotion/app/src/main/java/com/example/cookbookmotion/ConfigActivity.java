package com.example.cookbookmotion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigActivity extends WearableActivity {
    private static double FACTOR = 0.2; // c = a * sqrt(2)

    private Button login;
    private EditText userTextView;
    private EditText pwTextView;

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
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        int filter = sp.getInt("filtermethod", 0);
        spinner.setSelection(filter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("menue", "Pos: " + position);
                SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
                sp.edit().putInt("filtermethod", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
