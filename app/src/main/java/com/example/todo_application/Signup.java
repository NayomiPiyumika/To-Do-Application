package com.example.todo_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    public static final String TAG = "TAG";
    private EditText username,password,usern;
     private Button register;
    boolean passwordVisible;
    private TextView signintxt;
     ProgressBar progressBar;
     FirebaseAuth auth;
     FirebaseFirestore firestore;
     public String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        username = findViewById(R.id.create_username);
        password = findViewById(R.id.create_password);
        signintxt = findViewById(R.id.signintxt);
        register = findViewById(R.id.register_btn);
        usern = findViewById(R.id.username);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //already logged in checking
        if (auth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(),Home.class));
            finish();
        }

        signintxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupintent=new Intent(Signup.this,SignIn.class);
                startActivity(signupintent);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String create_username = username.getText().toString().trim();
                String create_password = password.getText().toString().trim();

                String userE =  username.getText().toString();
                String phone = usern.getText().toString();

                // Credential Validation
                if(TextUtils.isEmpty(create_username)){
                    username.setError("Username is Required !");
                    return;
                }
                if(TextUtils.isEmpty(create_password)){
                    password.setError("Password is Required!");
                    return;
                }
                if(password.length()<6){
                    password.setError("Password must be at least 6 Characters");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    usern.setError("Username is Required!");
                    return;
                }



                // Register the user in firebase
                auth.createUserWithEmailAndPassword(create_username,create_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Signup.this, "User Registration Successfully", Toast.LENGTH_SHORT).show();

                            UID=auth.getCurrentUser().getUid();
                            DocumentReference documentReference = firestore.collection("users").document(UID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("User_Name",userE);
                            user.put("Mobile_No",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG,"onSuccess: User Profile is Created for " + UID);
                                }
                            });

                            startActivity(new Intent(getApplicationContext(),Home.class));
                        }
                        else{
                            Toast.makeText(Signup.this, "Registration Error"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });



        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Right=2;
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(event.getRawX()>=password.getRight()-password.getCompoundDrawables()[Right].getBounds().width()){
                        int selection=password.getSelectionEnd();
                        if(passwordVisible){

                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24,  0);

                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible=false;
                        }else{

                            password.setCompoundDrawablesRelativeWithIntrinsicBounds( 0, 0, R.drawable.baseline_visibility_24, 0);

                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible=true;
                        }
                        password.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

    }
}