package com.example.langsync;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.langsync.util.AuthenticationUtilities;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

// Timepicker logic adapted from https://www.youtube.com/watch?v=c6c1giRekB4&ab_channel=CodeWithCal
public class CalendarActivity extends AppCompatActivity {
    private Spinner durationSpinner;
    private Button createEventButton;
    private Button dateButton;
    private Button timeButton;
    private Integer minute, hour, day, month, year;
    private final String TAG = "CalendarActivity";
    private final AuthenticationUtilities utilities = new AuthenticationUtilities(CalendarActivity.this);
    private String invitedUserId;

    // ChatGPT usage: No
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            invitedUserId = extras.getString("otherUserId");
        }
        timeButton = findViewById(R.id.time_button);
        dateButton = findViewById(R.id.date_button);
        durationSpinner = findViewById(R.id.duration_spinner);
        createEventButton = findViewById(R.id.create_event_button);

        // Set up a listener for the create event button
        createEventButton.setOnClickListener(v -> {
            if(year == null || month == null || day == null || minute == null || hour == null) {
                utilities.showToast(getString(R.string.select_all_meeting_details));
                return;
            }

            OkHttpClient client = new OkHttpClient();

            JSONObject bodyObject;
            try {
                bodyObject = generateCreateEventRequestBody();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(bodyObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(getString(R.string.base_url) + "events/")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    utilities.showToast(getString(R.string.meeting_error));
                }
                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        utilities.showToast(getString(R.string.meeting_successful));
                        finish();
                    } else {
                        Log.d(TAG, "onResponse: " + response.body().string());
                        utilities.showToast(getString(R.string.meeting_error));
                    }
                }
            });
        });

        // Populate the duration spinner with options
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this,
                R.array.duration_options, android.R.layout.simple_spinner_item);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);
    }

    // ChatGPT usage: No
    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            hour = selectedHour;
            minute = selectedMinute;
            timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                onTimeSetListener,
                hour == null ? 0 : hour,
                minute == null ? 0 : minute,
                true);

        timePickerDialog.setTitle(getString(R.string.select_time) +" (" +  TimeZone.getDefault().getID() + ")");
        timePickerDialog.show();
    }

    // ChatGPT usage: No
    public void popDatePicker(View view) {
        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy");
            String formattedDate = formatter.format(calendar.getTime());
            dateButton.setText(formattedDate);
        };
        Calendar calendar = Calendar.getInstance();
        int defaultDay = calendar.get(Calendar.DAY_OF_MONTH);
        int defaultMonth = calendar.get(Calendar.MONTH);
        int defaultYear = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, defaultYear, defaultMonth, defaultDay);
        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    // ChatGPT usage: Partial
    private JSONObject generateCreateEventRequestBody() throws JSONException {
        String selectedDuration = durationSpinner.getSelectedItem().toString().replace(" minutes", "");
        String timeZone = TimeZone.getDefault().getID();

        ZoneId zoneId = ZoneId.of(timeZone);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month + 1, day, hour, minute, 0, 0, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        String datetime = zonedDateTime.format(formatter);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String hostUserId = sharedPreferences.getString("loggedUserId", null);

        Log.d(TAG, "datetime: " + datetime);
        Log.d(TAG, "Timezone: " + timeZone);

        JSONObject object = new JSONObject();

        JSONObject event = new JSONObject();
        try {
            event.put("startTime", datetime);
            event.put("hostUserId", hostUserId);
            event.put("invitedUserId", invitedUserId);
            event.put("durationMinutes", selectedDuration);
            event.put("timeZone", timeZone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(CalendarActivity.this);
        String authCode = acct.getServerAuthCode();

        try {
            object.put("authCode", authCode);
            object.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }
}
