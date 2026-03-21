package com.example.taskmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_TASK_REQUEST = 1;

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);

        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, ADD_TASK_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK && data != null) {
            String taskName = data.getStringExtra("taskName");
            int hour = data.getIntExtra("hour", -1);
            int minute = data.getIntExtra("minute", -1);

            if (taskName != null && hour != -1 && minute != -1) {
                taskList.add(new Task(taskName, false, hour, minute));
                taskAdapter.notifyDataSetChanged();
            }
        }
    }

    // Task data model
    class Task {
        String name;
        boolean isCompleted;
        int hour, minute;

        Task(String name, boolean isCompleted, int hour, int minute) {
            this.name = name;
            this.isCompleted = isCompleted;
            this.hour = hour;
            this.minute = minute;
        }
    }

    // RecyclerView Adapter for Tasks
    class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<Task> tasks;

        TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.taskNameTextView.setText(task.name);
            holder.taskCheckBox.setChecked(task.isCompleted);
            holder.taskTimeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", task.hour, task.minute));
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            CheckBox taskCheckBox;
            TextView taskNameTextView;
            TextView taskTimeTextView;

            TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
                taskNameTextView = itemView.findViewById(R.id.taskNameTextView);
                taskTimeTextView = itemView.findViewById(R.id.taskTimeTextView);
            }
        }
    }
}
