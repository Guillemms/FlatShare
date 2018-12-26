package com.example.guillemms.flatshare;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskListActivity extends AppCompatActivity {

    private ArrayList<Task> tasks;
    private RecyclerView taskListRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        tasks = new ArrayList<>();

        tasks.add(new Task("10/11 - 18-11", "Task A week 1"));
        tasks.add(new Task("10/11 - 18-11", "Task B week 1"));
        tasks.add(new Task("20/11 - 25-11", "Task A week 2"));
        tasks.add(new Task("20/11 - 25-11", "Task B week 2"));

        taskListRecycler = findViewById(R.id.tasklist_recyclerView);
        taskListRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskListRecycler.setAdapter(new Adapter());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateView;
        private TextView taskNameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateView = itemView.findViewById(R.id.date_textView);
            taskNameView = itemView.findViewById(R.id.task_textView);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.layout_task, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.dateView.setText(task.getDate());
            holder.taskNameView.setText(task.getTaskName());
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }
}
