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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Map;

public class TaskListActivity extends AppCompatActivity {

    private static final int NEW_TASK = 0;
    private static final int EDIT_TASK = 1;
    private Button btnShop;

    private ArrayList<Map> tasks;
    private RecyclerView taskListRecycler;
    private Adapter adapter = new Adapter();
    private int lastDifferentWeekNum = -1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //DateFormat df = DateFormat.getDateInstance();
    Calendar cal = Calendar.getInstance();

    String userId, flatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        btnShop = findViewById(R.id.shop_button);

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShopButtonClick();
            }
        });

        tasks = new ArrayList<>();

        taskListRecycler = findViewById(R.id.tasklist_recyclerView);
        taskListRecycler.setLayoutManager(new LinearLayoutManager(this));

        taskListRecycler.setAdapter(adapter);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        final SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = "EhtvSxlbPi2VHE2aFcOH"; //prefs.getString("userId", null);
        flatId = prefs.getString("flatId", null);

        if (flatId == null) {
            db.collection("Users").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    //Log.d("user", "DocumentSnapshot data: " + document.getData());
                                    Map user = document.getData();
                                    String userFlatId = (String) user.get("ID Flat");
                                    prefs.edit().putString("flatId", userFlatId).commit();
                                    getTasksFromThisFlat();
                                } else {
                                    Log.d("user", "No such document");
                                }
                            } else {
                                Log.d("user", "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            getTasksFromThisFlat();
        }
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

    private void getTasksFromThisFlat() {
        tasks = new ArrayList<>();
        db.collection("Flats")
                .document(flatId)
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

    private void getTasksFromThisUser() {
        tasks = new ArrayList<>();
        db.collection("Flats")
                .document(flatId)
                .collection("Tasks")
                .whereArrayContains("User IDs", userId)
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
            case R.id.menu_filterTask:
                getTasksFromThisUser();
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
