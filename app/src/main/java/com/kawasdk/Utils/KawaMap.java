package com.kawasdk.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.Fragment.fragmentFarmLocation;
import com.kawasdk.R;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.smartlook.sdk.smartlook.Smartlook;

public class KawaMap extends AppCompatActivity {

//    public static Context context;

    public static InterfaceKawaEvents interfaceKawaEvents;
    public static Integer footerBtnBgColor = Color.BLUE;
    public static Integer footerBtnTextColor  = Color.WHITE;

    public static Integer innerBtnBgColor = R.color.orange;
    public static Integer innerBtnTextColor = Color.BLACK;

    public static Integer headerBgColor = Color.WHITE;
    public static Integer headerTextColor = Color.BLACK;

    public static boolean isValidKawaAPiKey;
    public static String KAWA_API_KEY = "";
    public static String SEGMENT_API_KEY = "";
    public static String SMARTLOOK_API_KEY = "";
    public static boolean isMergeEnable = false;
    public static boolean isDrawEnable = false;
    public static boolean isEditEnable = false;
    public static boolean isFormEnable = false;
    public static boolean isFarmDetailsEnable = false;
    public static boolean isOtherFarmDetailsEnable = false;
    public static boolean isSaveResultEnable = false;
    public static boolean isBahasaEnable = false;
    public static boolean isDevelop = false;
    public static boolean isFlyToLocationEnable = false;

    public KawaMap(Context context) {
//        this.context = context;

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        interfaceKawaEvents = (InterfaceKawaEvents) this;

    }

    private static void initSmartlook(String smartlook_api_key) {
        if (smartlook_api_key != null || smartlook_api_key != "") {
            SMARTLOOK_API_KEY = smartlook_api_key;
            smartlookIntegration();
            //  smartlookIntegration_from_SDK();
        }
    }

    private static void initSegment(Context context, String segment_api_key) {

        if (segment_api_key != null || segment_api_key != "") {
            SEGMENT_API_KEY = segment_api_key;
            segmentIntegration(context);
//            segmentIntegration_from_SDK(context);
        }
    }

    public static void setFooterBtnBgColorAndTextColor(Context context ,int bgColor, int textColor) {
        footerBtnBgColor = context.getResources().getColor(bgColor);
        footerBtnTextColor = context.getResources().getColor(textColor);
    }

    public static void setInnerBtnBgColorAndTextColor(Context context ,int bgColor, int textColor) {
        innerBtnBgColor = bgColor;
        innerBtnTextColor =  context.getResources().getColor(textColor);
    }

    public static void setHeaderBgColorAndTextColor(Context context ,int bgColor, int textColor) {
        headerBgColor = context.getResources().getColor(bgColor);
        headerTextColor = context.getResources().getColor(textColor);
    }

    public static void setFooterButtonColor(Button[] footerButtons) {
        for (int i = 0; i < footerButtons.length; i++) {
            footerButtons[i].setTextColor(footerBtnTextColor);
            footerButtons[i].setBackgroundColor(footerBtnBgColor);

        }
    }

    public static void setInnerButtonColor(Context context, Button[] innerButtons) {
        for (int i = 0; i < innerButtons.length; i++) {
            innerButtons[i].setTextColor(innerBtnTextColor);

          //innerButtons[i].setBackgroundColor(context.getResources().getColor(innerBtnBgColor));
          innerButtons[i].getBackground().setColorFilter(ContextCompat.getColor(context,innerBtnBgColor), PorterDuff.Mode.MULTIPLY);

        }
    }

    public static void smartlookIntegration() {
        Smartlook.setUserIdentifier("KAWA");
        Smartlook.SetupOptionsBuilder builder = new Smartlook.SetupOptionsBuilder(SMARTLOOK_API_KEY)
                .setFps(2)
                .setExperimental(true)
                .setActivity(null);
        Smartlook.setupAndStartRecording(builder.build());

        Log.e("TAG", "SMARTLOOK API >> : " + SMARTLOOK_API_KEY);
    }


    public static void segmentIntegration(Context context) {
        // Create an analytics client with the given context and Segment write key.
        String fildsInformation = Common.getFiledsDetails();
        try {
            Analytics analytics = new Analytics.Builder(context, SEGMENT_API_KEY)
                    // Enable this to record certain application events automatically!
                    .trackApplicationLifecycleEvents()
                    // Enable this to record screen views automatically!
                    .recordScreenViews()
                    .build();
            Analytics.setSingletonInstance(analytics);

            Properties properties = new Properties();
            String jString = fildsInformation + ",\"metadata\":{\"message\":\"User Details Saved\"}}";
            JsonObject jsonObject = JsonParser.parseString(jString).getAsJsonObject();
            properties.putValue("data", jsonObject);
            analytics.with(context).track("Map Initialisation", properties);

             Log.e("segment-properties", String.valueOf(properties));
        } catch (Exception e) {
            // Log.e("catch-error", String.valueOf(e.getMessage()));
            //Toast.makeText(context, String.valueOf(e.getMessage()), Toast.LENGTH_LONG).show();
        }

    }
    public static boolean initKawaMap(Context context, String kawa_api_key) {
        isValidKawaAPiKey = false;
        if (kawa_api_key != null && kawa_api_key != "") {
            KAWA_API_KEY = kawa_api_key;
            isValidKawaAPiKey = true;
        }
        if (SMARTLOOK_API_KEY == null || SMARTLOOK_API_KEY == "") {
            initSmartlook("627da9d9178be1e4d7e3b5e54404d34f3bbf1877");

        }
        if (SEGMENT_API_KEY == null || SEGMENT_API_KEY == "") {
            initSegment(context,"8GDsJGGITP2nXZHTV4mFpA5yBNLsalUS");
        }
        return isValidKawaAPiKey;
        //interfaceKawaEvents.initKawaMap(isValid);
    }

    public static void startKawaSDK(FragmentActivity context,String kawa_api_key) {
        KawaMap.initKawaMap(context,kawa_api_key);
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.kawaMapView, new fragmentFarmLocation()).commit();

//        footerBtnBgColor = context.getResources().getColor(R.color.orange);
//        footerBtnTextColor = context.getResources().getColor(R.color.mapboxGray);
//        headerBgColor = context.getResources().getColor(R.color.orange);
//        headerTextColor = context.getResources().getColor(R.color.white);
    }

    public static void exitKawaSDKAndGoToActivity(Context context,String classname) {
        try {
            Class<?> c = Class.forName(classname);
            Intent intent = new Intent(context, c);
            context.startActivity(intent);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public static void exitKawaSDKAndGoBack(Activity activity) {
        activity.finish();

    }
}
