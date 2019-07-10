package com.example.glasslinkapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Config extends AppCompatActivity {

    Button login;
    TextView userTextView;
    TextView pwTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


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
    }

    private void goToMainActivity(){
        finish();
    }
}
