package com.kawasdk.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.kawasdk.Fragment.fragmentFarmLocation;
import com.kawasdk.R;
import com.kawasdk.Utils.InterfaceKawaEvents;
import com.kawasdk.Utils.KawaMap;

import org.json.JSONObject;

import java.util.ArrayList;

public class kawaHomeActivity extends AppCompatActivity implements InterfaceKawaEvents {
    public final String TAG = "Kawa";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kawa_home);
        KawaMap kawaMap = new KawaMap(this);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.kawaMapView, fragmentFarmLocation.class,null).commit();
        kawaMap.startKawaSDK(kawaHomeActivity.this, getResources().getString(R.string.kawa_api_key));
        kawaMap.isMergeEnable = true;
        kawaMap.isEditEnable = true;
//        kawaMap.isFarmDetailsEnable = true;
//        // KawaMap.isOtherFarmDetailsEnable = false;
        kawaMap.isFormEnable = false;
        kawaMap.isFlyToLocationEnable = true;
    }


    @Override
    public void initKawaMap(boolean isValid) {
        Log.e("TAG", "initKawaMap: >>> " + isValid);
        if (isValid == true) {
//            kawaMap.setFooterBtnBgColorAndTextColor(this,R.color.blue, R.color.white); //set large button colors
//            kawaMap.setInnerBtnBgColorAndTextColor(this,R.color.orange, R.color.mapboxBlack); //set small button colors
//            kawaMap.setHeaderBgColorAndTextColor(this,R.color.blue, R.color.white); //set header messagebox  colors
//            KawaMap.isMergeEnable = true;
//            KawaMap.isEditEnable = true;
            //KawaMap.isFarmDetailsEnable = true;
            // KawaMap.isOtherFarmDetailsEnable = false;
//            KawaMap.isFormEnable = false;
//           // KawaMap.isBahasaEnable = false;
//            //KawaMap.isFlyToLocationEnable = false;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(kawaHomeActivity.this).create();
            alertDialog.setTitle(getResources().getString(R.string.app_name));
            alertDialog.setMessage("KAWA api key not found.");
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
            //Toast.makeText(this, "KAWA api key not found.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onkawaSubmit(String data) {
        Log.e(TAG, "SubmitJson>>>>: " + data);
//        KawaMap.exitKawaSDK(this, HomeActivity.class.getName());

    }

    @Override
    public void onkawaSelect(JsonObject data) {
       // finish();
        //Toast.makeText(this, String.valueOf(data), Toast.LENGTH_LONG).show();
        Log.e(TAG, "SelectJson: " + data);

    }

    @Override
    public void onkawaUpdate(JSONObject data) {
        // Log.e(TAG, String.valueOf(data));
    }

    @Override
    public void onkawaDestroy() {
         finish();
        // getSupportFragmentManager().findFragmentById(R.id.kawaMapView).
        // KawaMap.exitKawaSDKAndGoToActivity(this, HomeActivity.class.getName());
        //KawaMap.exitKawaSDKAndGoBack(this);
    }
}