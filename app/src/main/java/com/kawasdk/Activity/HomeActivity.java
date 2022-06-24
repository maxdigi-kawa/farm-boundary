package com.kawasdk.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.R;
import com.kawasdk.Utils.Common;
import com.kawasdk.Utils.KawaMap;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.smartlook.sdk.smartlook.Smartlook;

import timber.log.Timber;

public class HomeActivity extends AppCompatActivity {
    Button startKawaBtn;
    EditText nameTxt, addressTxt, companyTxt;
    boolean isCompanyName = false; // true only for client app 6

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        startKawaBtn = findViewById(R.id.startKawaBtn);
        nameTxt = findViewById(R.id.nameTxt);
        addressTxt = findViewById(R.id.addressTxt);
        companyTxt = findViewById(R.id.companyTxt);
        if (isCompanyName) {
            companyTxt.setVisibility(View.VISIBLE); // for client app 6
        }
        startKawaBtn.setOnClickListener(viewV -> gotoKawaSDK());
    }

    private void gotoKawaSDK() {
        String nameStr = nameTxt.getText().toString().trim();
        String addressStr = addressTxt.getText().toString().trim();
        String companyStr = companyTxt.getText().toString().trim();
//        if (nameStr.isEmpty()) {
//            Toast.makeText(HomeActivity.this, "Please enter name", Toast.LENGTH_LONG).show();
//            return;
//        } else if (addressStr.isEmpty()) {
//            Toast.makeText(HomeActivity.this, "Please enter address", Toast.LENGTH_LONG).show();
//            return;
//        } else {
            Common.USER_NAME = nameTxt.getText().toString();
            Common.USER_ADDRESS = addressTxt.getText().toString();
            Common.USER_COMPANY = companyTxt.getText().toString();
            Intent intent = new Intent(HomeActivity.this, kawaHomeActivity.class);
            startActivity(intent);
       // }


    }

}