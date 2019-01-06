package com.example.guillemms.flatshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskListActivity extends AppCompatActivity {

    private static final int NEW_TASK = 0;
    private static final int EDIT_TASK = 1;

    private ArrayList<DisplayTask> tasks = new ArrayList<>();

    private RecyclerView taskListRecycler;
    private Adapter adapter = new Adapter();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    SimpleDateFormat df = new SimpleDateFormat("dd/MM", Locale.ITALIAN);
    Calendar cal = Calendar.getInstance(Locale.ITALIAN);

    String userId, flatId;

    Map userNames = new HashMap<>();

    private int numberOfWeeksAhead = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        final SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", ""); // Borrar EhtvSxlbPi2VHE2aFcOH
        flatId = prefs.getString("flatId", ""); // Borrar aspugPQibATokPjQNLTm

        Button btnShop = findViewById(R.id.shop_button);
        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShopButtonClick();
            }
        });

        taskListRecycler = findViewById(R.id.tasklist_recyclerView);
        taskListRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskListRecycler.setAdapter(adapter);

        // ! Es posible que rebi abans els items que els users i no mostri els noms
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = (String)document.getData().get("Name");
                                userNames.put(document.getId(), name);
                            }
                            getTasksFromThisFlat();
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case NEW_TASK:
                if(resultCode == RESULT_OK){
                    getTasksFromThisFlat();
                }
                break;
            case EDIT_TASK:
                if(resultCode == RESULT_OK){
                    getTasksFromThisFlat();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void clearTaskList() {
        int numTasks = tasks.size();
        tasks.clear();
        adapter.notifyItemRangeRemoved(0, numTasks);
    }

    private void getTasksFromThisFlat() {
        clearTaskList();
        db.collection("Flats")
                .document(flatId)
                .collection("Tasks")
                .orderBy("Initial date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int lastWeek = -1;
                            cal.setTime(new Date());
                            int thisWeek = cal.get(Calendar.WEEK_OF_YEAR);
                            for (int i = 0; i < numberOfWeeksAhead; i++) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Map flatTask = document.getData();

                                    String taskId = document.getId();
                                    Timestamp initialTimestamp = (Timestamp) flatTask.get("Initial date");
                                    Date initialDate = initialTimestamp.toDate();
                                    cal.setTime(initialDate);
                                    int startWeek = cal.get(Calendar.WEEK_OF_YEAR);

                                    ArrayList taskUserIds = (ArrayList) flatTask.get("User IDs");

                                    int periodicity = Math.round((long) flatTask.get("Periodicity"));

                                    int absWeek = thisWeek + i;

                                    Calendar weekCal = Calendar.getInstance();
                                    weekCal.add(Calendar.WEEK_OF_YEAR, i);
                                    String weekString = getWeekString(weekCal);

                                    if (periodicity == 0) {
                                        boolean hasTaskThisWeek = absWeek == startWeek;
                                        if (hasTaskThisWeek) {
                                            String taskUserId = (String) taskUserIds.get(0);
                                            String userName = (String) userNames.get(taskUserId);
                                            String taskName = (String) flatTask.get("Name");

                                            boolean isWeekDisplayed = absWeek != lastWeek;
                                            lastWeek = absWeek;

                                            tasks.add(new DisplayTask(taskName, userName, weekString, isWeekDisplayed, taskId));
                                        }
                                    } else {
                                        int relWeek = absWeek - startWeek;
                                        boolean hasTaskThisWeek = (relWeek % periodicity == 0) && relWeek >= 0;
                                        if (hasTaskThisWeek) {
                                            int idx = relWeek % taskUserIds.size();
                                            String taskUserId = (String) taskUserIds.get(idx);
                                            String userName = (String) userNames.get(taskUserId);
                                            String taskName = (String) flatTask.get("Name");

                                            boolean isWeekDisplayed = absWeek != lastWeek;
                                            lastWeek = absWeek;

                                            tasks.add(new DisplayTask(taskName, userName, weekString, isWeekDisplayed, taskId));
                                        }

                                    }
                                }
                            }
                            adapter.notifyItemRangeInserted(0, tasks.size());
                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private String getWeekString(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, 2-dayOfWeek);
        String monday = df.format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 7-dayOfWeek);
        String sunday = df.format(cal.getTime());
        return monday+" - "+sunday;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateView;
        private TextView taskNameView;
        private TextView userNameView;
        private View weekBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateView = itemView.findViewById(R.id.date_textView);
            taskNameView = itemView.findViewById(R.id.task_textView);
            userNameView = itemView.findViewById(R.id.user_textView);
            weekBackground = itemView.findViewById(R.id.week_bg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTaskClick(getAdapterPosition());
                }
            });
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
            DisplayTask displayTask = tasks.get(position);

            holder.dateView.setText(displayTask.getWeekStr());
            holder.taskNameView.setText(displayTask.getTaskName());
            holder.userNameView.setText(displayTask.getUserName());

            if(!displayTask.isWeekDisplayed()) {
                holder.dateView.setVisibility(View.GONE);
                holder.weekBackground.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }

    public void onTaskClick(int i){
        Intent intent = new Intent(this, TaskActivity.class);
        String taskId = tasks.get(i).getTaskId();
        intent.putExtra("taskId", taskId);
        startActivityForResult(intent, EDIT_TASK);
    }

    private void onShopButtonClick() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
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
                Intent newIntent = new Intent(this, TaskActivity.class);
                startActivityForResult(newIntent, NEW_TASK);
                break;
            case R.id.menu_newUser:
                Intent newUserIntent = new Intent(this, AddTenantActivity.class);
                newUserIntent.putExtra("lastActivity", 2);
                startActivity(newUserIntent);
                break;
        }
        return true;
    }
}
