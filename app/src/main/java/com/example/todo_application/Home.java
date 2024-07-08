package com.example.todo_application;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.todo_application.Adapter.ToDoAdapter;
import com.example.todo_application.Model.ToDoModel;
import com.example.todo_application.Signup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements OnDialogCloseListner {

    private RecyclerView recyclerView;
    private com.google.android.material.floatingactionbutton.FloatingActionButton add_button;
    ImageView menu_button;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private ToDoAdapter toDoAdapter;
    private List<ToDoModel> list;
    private Query query;
    private String UID;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UID = user.getUid();
        } else {
            Toast.makeText(this, "You are Not Logged In ! Please Login ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Home.this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recycleview);
        add_button = findViewById(R.id.add_task_btn);
        menu_button = findViewById(R.id.btn_menu);

        // RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));

        // adapter and list
        list = new ArrayList<>();
        toDoAdapter = new ToDoAdapter(Home.this, list);

        // ItemTouchHelper for swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(toDoAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Attach adapter to RecyclerView
        recyclerView.setAdapter(toDoAdapter);

        // Load tasks from Firestore
        showData();


        // navigate to menu
        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, More_Info.class);
                startActivity(intent);
                finish();
            }
        });

        // Set up button listener to add new tasks
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask addNewTask = new AddNewTask();
                addNewTask.show(getSupportFragmentManager(), "AddNewTask");
            }
        });
    }

    private void showData() {
        query = firestore.collection("task")
                .orderBy("time", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            String id = documentChange.getDocument().getId();
                            ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);
                            list.add(toDoModel);
                            toDoAdapter.notifyDataSetChanged();
                        }
                    }
                    listenerRegistration.remove();
                }
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        list.clear();
        showData();
        toDoAdapter.notifyDataSetChanged();
    }
}
