package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.text.DecimalFormat;

/**
 * MainActivity for Length Converter Application
 * This app converts between five length units: Feet, Inches, Centimeters, Meters, and Yards
 */
public class MainActivity extends AppCompatActivity {

    // UI Component declarations
    private EditText inputValue;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private Button buttonConvert;
    private TextView textResult;
    private ImageButton buttonSettings;

    // Shared Preferences constants
    private static final String PREFERENCES_NAME = "unit_converter_prefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    // Conversion constants - all relative to meters as the base unit
    private static final double FEET_TO_METERS = 0.3048;    // 1 foot = 0.3048 meters
    private static final double INCHES_TO_METERS = 0.0254;  // 1 inch = 0.0254 meters
    private static final double CM_TO_METERS = 0.01;        // 1 cm = 0.01 meters
    private static final double YARDS_TO_METERS = 0.9144;   // 1 yard = 0.9144 meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before setting content view
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize and connect UI components to their layout counterparts
        inputValue = findViewById(R.id.input_value);
        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        buttonConvert = findViewById(R.id.button_convert);
        textResult = findViewById(R.id.text_result);
        buttonSettings = findViewById(R.id.button_settings);

        // Create and set adapter for the spinners using the string array resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.length_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to both spinners
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Set up click listener for the convert button
        buttonConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertUnits();
            }
        });

        // Set up click listener for the settings button
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open settings activity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Apply the theme based on saved preferences
     */
    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        boolean isDarkModeOn = sharedPreferences.getBoolean(KEY_DARK_MODE, false);

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Main conversion method that handles user input validation and conversion process
     */
    private void convertUnits() {
        // Get and validate the user input
        String valueStr = inputValue.getText().toString();
        if (valueStr.isEmpty()) {
            Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse input value and get selected unit positions
            double value = Double.parseDouble(valueStr);
            int fromUnitPosition = spinnerFrom.getSelectedItemPosition();
            int toUnitPosition = spinnerTo.getSelectedItemPosition();

            // Two-step conversion process:
            // 1. Convert from source unit to meters (base unit)
            double valueInMeters = convertToMeters(value, fromUnitPosition);

            // 2. Convert from meters to target unit
            double result = convertFromMeters(valueInMeters, toUnitPosition);

            // Format and display the result
            DecimalFormat df = new DecimalFormat("#.#####");
            String unitFrom = getResources().getStringArray(R.array.length_units)[fromUnitPosition];
            String unitTo = getResources().getStringArray(R.array.length_units)[toUnitPosition];

            textResult.setText(df.format(value) + " " + unitFrom + " = " +
                    df.format(result) + " " + unitTo);

        } catch (NumberFormatException e) {
            // Handle invalid number format
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Converts a value from any supported unit to meters (base unit)
     *
     * @param value The numerical value to convert
     * @param fromUnit The index of the source unit (0=Feet, 1=Inches, 2=Centimeters, 3=Meters, 4=Yards)
     * @return The equivalent value in meters
     */
    private double convertToMeters(double value, int fromUnit) {
        switch (fromUnit) {
            case 0: // Feet
                return value * FEET_TO_METERS;
            case 1: // Inches
                return value * INCHES_TO_METERS;
            case 2: // Centimeters
                return value * CM_TO_METERS;
            case 3: // Meters
                return value; // Already in meters
            case 4: // Yards
                return value * YARDS_TO_METERS;
            default:
                return value;
        }
    }

    /**
     * Converts a value from meters to any supported unit
     *
     * @param valueInMeters The value in meters to convert
     * @param toUnit The index of the target unit (0=Feet, 1=Inches, 2=Centimeters, 3=Meters, 4=Yards)
     * @return The equivalent value in the target unit
     */
    private double convertFromMeters(double valueInMeters, int toUnit) {
        switch (toUnit) {
            case 0: // Feet
                return valueInMeters / FEET_TO_METERS;
            case 1: // Inches
                return valueInMeters / INCHES_TO_METERS;
            case 2: // Centimeters
                return valueInMeters / CM_TO_METERS;
            case 3: // Meters
                return valueInMeters; // Already in meters
            case 4: // Yards
                return valueInMeters / YARDS_TO_METERS;
            default:
                return valueInMeters;
        }
    }
}