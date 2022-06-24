package com.kawasdk.Utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kawasdk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FormBuilder extends AppCompatActivity {

    public static EditText editFiled(Context context, JSONObject jsonObject, LinearLayout.LayoutParams editTextLayoutParams) {
        EditText rowEditView = new EditText(context);
        try {
            String filed_placeholder = jsonObject.getString("field_placeholder");
            int filed_id = Integer.parseInt(jsonObject.getString("field_id"));
            String filed_tags = jsonObject.getString("field_tag");
           // String keyboard_button = jsonObject.getString("keyboard_button");
            rowEditView.setHint(filed_placeholder);
            rowEditView.setId(filed_id);
            rowEditView.setBackgroundColor(Color.WHITE);
            rowEditView.setHintTextColor(Color.LTGRAY);
            rowEditView.setTextColor(Color.BLACK);
            rowEditView.setTextSize(16f);
//            if (keyboard_button.equals("next"))
//                rowEditView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//            else
//                rowEditView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            rowEditView.setSingleLine(true);
            rowEditView.setLayoutParams(editTextLayoutParams);
            rowEditView.setPadding(20, 15, 10, 15);
            rowEditView.setTag(filed_tags);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowEditView;
    }

    public static TextView textFiled(Context context, String textstr, Float textsize, int colorCode, String fontStyle) {
        TextView rowTextView = new TextView(context);
        rowTextView.setText(textstr);
        rowTextView.setTextSize(textsize);
        if (fontStyle.equalsIgnoreCase("bold"))
            rowTextView.setTypeface(rowTextView.getTypeface(), Typeface.BOLD);

        rowTextView.setTextColor(colorCode);
        rowTextView.setPadding(20, 0, 10, 25);
        return rowTextView;
    }


    public static Spinner spinnerFiled(Context context, JSONObject jsonObject, LinearLayout.LayoutParams editTextLayoutParams) {
        Spinner spinnerView = new Spinner(context);

        try {
            String filed_tags = jsonObject.getString("field_tag");
            String field_placeholder = jsonObject.getString("field_placeholder");
            JSONArray jsonArray = jsonObject.getJSONArray("options");
            ArrayList<String> spinnerArray = new ArrayList<String>();
            spinnerArray.add("Select " + field_placeholder);
            for (int i = 0; i < jsonArray.length(); i++) {
                spinnerArray.add(jsonArray.getString(i));
            }
            spinnerView.setBackgroundColor(Color.WHITE);
            spinnerView.setPadding(5, 0, 0, 0);
            LinearLayout.LayoutParams spinnerLayoutParams = new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, 105);
            spinnerLayoutParams.setMargins(20, 10, 20, 10);
            spinnerView.setLayoutParams(spinnerLayoutParams);
            spinnerView.setTag(filed_tags);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, R.layout.spinnner_unselected, spinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerView.setAdapter(spinnerArrayAdapter);
        } catch (Exception e) {
            Log.e("TAG", "Exception: " + e);
        }
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if (position != 0)
                    ((TextView) parentView.getSelectedView()).setTextColor(context.getResources().getColor(R.color.mapboxBlack));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        return spinnerView;
    }

    public static EditText dateFiled(Context context, JSONObject jsonObject, LinearLayout.LayoutParams editTextLayoutParams) {
        EditText rowDateView = new EditText(context);
        final Calendar date_calender = Calendar.getInstance();
        try {
            String filed_placeholder = jsonObject.getString("field_placeholder");
            int filed_id = Integer.parseInt(jsonObject.getString("field_id"));
            String filed_tags = jsonObject.getString("field_tag");

            rowDateView.setHint(filed_placeholder);
            rowDateView.setId(filed_id);
            rowDateView.setBackgroundColor(Color.WHITE);
            rowDateView.setHintTextColor(Color.LTGRAY);
            rowDateView.setTextColor(Color.BLACK);
            rowDateView.setTextSize(16f);
            rowDateView.setSingleLine(true);
            rowDateView.setLayoutParams(editTextLayoutParams);
            rowDateView.setPadding(20, 15, 10, 15);
            rowDateView.setTag(filed_tags);
            rowDateView.setFocusable(false);
            rowDateView.setClickable(true);

            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    date_calender.set(Calendar.YEAR, year);
                    date_calender.set(Calendar.MONTH, month);
                    date_calender.set(Calendar.DAY_OF_MONTH, day);
                    String myFormat = "dd-MM-yyyy";
                    SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
                    rowDateView.setText(dateFormat.format(date_calender.getTime()));
                }
            };

            rowDateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final long oneDay = 24 * 60 * 60 * 1000L;
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context, date, date_calender.get(Calendar.YEAR), date_calender.get(Calendar.MONTH), date_calender.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - oneDay);
                    datePickerDialog.show();
//                                    new DatePickerDialog(getActivity(), date, sowing_date_calender.get(Calendar.YEAR), sowing_date_calender.get(Calendar.MONTH), sowing_date_calender.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowDateView;
    }

    public static EditText textArea(Context context, JSONObject jsonObject, LinearLayout.LayoutParams textLayoutParams) {
        EditText rowTextArea = new EditText(context);
        try {
            String filed_placeholder = jsonObject.getString("field_placeholder");
            int filed_id = Integer.parseInt(jsonObject.getString("field_id"));
            String filed_tags = jsonObject.getString("field_tag");

            rowTextArea.setHint(filed_placeholder);
            rowTextArea.setId(filed_id);
            rowTextArea.setBackgroundColor(Color.WHITE);
            rowTextArea.setHintTextColor(Color.LTGRAY);
            rowTextArea.setTextColor(Color.BLACK);
            rowTextArea.setLines(5);
            rowTextArea.setMinLines(3);
            rowTextArea.setMaxLines(6);
            rowTextArea.setGravity(0);
            rowTextArea.setTextSize(16f);
            rowTextArea.setSingleLine(false);
            rowTextArea.setLayoutParams(textLayoutParams);
            rowTextArea.setPadding(20, 15, 10, 15);
            rowTextArea.setTag(filed_tags);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowTextArea;
    }
}
