package com.example.taskmanagerapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText taskNameEditText;
    private Button timePickerButton;
    private TextView selectedTimeTextView;
    private Button saveTaskButton;

    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskNameEditText = findViewById(R.id.taskNameEditText);
        timePickerButton = findViewById(R.id.timePickerButton);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        saveTaskButton = findViewById(R.id.saveTaskButton);

        timePickerButton.setOnClickListener(v -> showTimePickerDialog());

        saveTaskButton.setOnClickListener(v -> saveTask());
    }

    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    hour = hourOfDay;
                    minute = minuteOfHour;
                    selectedTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void saveTask() {
        String taskName = taskNameEditText.getText().toString();

        if (taskName.isEmpty()) {
            taskNameEditText.setError("Task name cannot be empty");
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("taskName", taskName);
        resultIntent.putExtra("hour", hour);
        resultIntent.putExtra("minute", minute);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
