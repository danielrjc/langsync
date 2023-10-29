package com.example.langsync;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FormActivity extends AppCompatActivity {

    private TextView desiredLanguages, proficientLanguages, learningPreferences, interests, age;
    boolean desiredLanguagesSelected[] = new boolean[DESIRED_LANGUAGES.length];
    boolean proficientLanguagesSelected[] = new boolean[PROFICIENT_LANGUAGES.length];
    boolean interestsSelected[] = new boolean[INTERESTS.length];
    private static final String TAG = "FormActivity";
    private static final String[] PROFICIENT_LANGUAGES = new String[]{
            "English",
            "Spanish",
            "French",
            "Arabic",
            "Hindi",
            "Portuguese",
            "Punjabi"
    };
    private static final String[] DESIRED_LANGUAGES = new String[]{
            "English",
            "Spanish",
            "French",
            "Arabic",
            "Hindi",
            "Portuguese",
            "Punjabi"
    };
    private static final String[] LEARNING_PREFERENCES = new String[]{
            "Taught by Expert",
            "Peer Learning",
            "Both"
    };

    private static final String[] INTERESTS = new String[]{
            "Business",
            "Sports",
            "Cooking",
            "Travel",
            "Movies",
            "Art",
            "Music",
            "Reading",
            "Gaming",
    };

    private List<String> selectedDesires = new ArrayList<>();
    private List<String> selectedProficiencies = new ArrayList<>();
    private List<String> selectedInterests = new ArrayList<>();
    private String selectedPreference = "";
    private String selectedAge = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("loggedUserId", null); // null is the default value if the key is not found
        Log.d(TAG, "Shared Pref User Id: " + userId);

        desiredLanguages = findViewById(R.id.desired_languages);
        proficientLanguages = findViewById(R.id.proficient_languages);
        learningPreferences = findViewById(R.id.learning_preferences);
        interests = findViewById(R.id.interests);
        age = findViewById(R.id.age);

        multiSelectOnClick(desiredLanguages, DESIRED_LANGUAGES, desiredLanguagesSelected, selectedDesires);
        multiSelectOnClick(proficientLanguages, PROFICIENT_LANGUAGES, proficientLanguagesSelected, selectedProficiencies);
        multiSelectOnClick(interests, INTERESTS, interestsSelected, selectedInterests);
        learningPreferences.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
            builder.setTitle("Select Learning Preferences");
            builder.setCancelable(false);

            builder.setSingleChoiceItems(LEARNING_PREFERENCES, -1, (dialog, i) -> {
                selectedPreference = LEARNING_PREFERENCES[i];
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                learningPreferences.setText(selectedPreference);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        age.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
            builder.setTitle("Select Learning Preferences");
            builder.setCancelable(false);

            final EditText input = new EditText(this);
            input.setLayoutParams(new FrameLayout.LayoutParams(40, FrameLayout.LayoutParams.MATCH_PARENT));
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                age.setText(input.getText().toString());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void multiSelectOnClick(TextView textView, String[] options, boolean[] selected, List<String> selectedOptions){
        textView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FormActivity.this);
            builder.setTitle("Select Languages");
            builder.setCancelable(false);
            builder.setMultiChoiceItems(options, selected, (dialog, i, isChecked) -> {
                if(isChecked){
                    selectedOptions.add(options[i]);
                }else{
                    selectedOptions.remove(options[i]);
                }
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                StringBuilder selectedOptionsString = new StringBuilder();
                for(String s : selectedOptions){
                    selectedOptionsString.append(s).append(", ");
                }
                textView.setText(selectedOptionsString.toString());
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
}