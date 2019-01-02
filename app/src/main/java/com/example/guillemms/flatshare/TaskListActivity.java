package com.example.guillemms.flatshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TaskListActivity extends AppCompatActivity {

    private ArrayList<Map> tasks;
    private RecyclerView taskListRecycler;
    private int lastDifferentWeekNum = -1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    DateFormat df = DateFormat.getDateInstance();
    Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        tasks = new ArrayList<>();

        taskListRecycler = findViewById(R.id.tasklist_recyclerView);
        taskListRecycler.setLayoutManager(new LinearLayoutManager(this));

        final Adapter adapter = new Adapter();

        taskListRecycler.setAdapter(adapter);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        db.collection("Flats")
                .document("2UHhup7o3ukp672EwXIo")
                .collection("Tasks")
                .orderBy("Initial date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map flatTask = document.getData();
                                tasks.add(flatTask);
                                adapter.notifyItemInserted(tasks.size()-1);
                            }
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });
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
            Map task = tasks.get(position);
            Timestamp initialTimestamp = (Timestamp) task.get("Initial date");
            Date initialDate = initialTimestamp.toDate();

            cal.setTime(initialDate);
            int week = cal.get(Calendar.WEEK_OF_YEAR);

            //String initialDateString = df.format(initialDate);

            String taskInfo = task.get("Name").toString();

            holder.dateView.setText("Week: " + week);
            holder.taskNameView.setText(taskInfo);

            boolean isDateDisplayed = week == lastDifferentWeekNum;

            if(isDateDisplayed) {
                holder.dateView.setVisibility(View.GONE);
            } else {
                lastDifferentWeekNum = week;
            }
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newTask:
                //Afaguir nova tasca
                break;
            case R.id.menu_filterTask:
                //Filtrar les teves tasques
                break;
            case R.id.menu_newUser:
                Intent intent = new Intent(this, AddTenantActivity.class);
                intent.putExtra("lastActivity", 2);
                startActivity(intent);
                break;
        }
        return true;
    }
}
