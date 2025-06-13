package com.pvc.circleup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";

    EditText mFullName, mEmail, mPassword, mConfirmPass;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mConfirmPass = findViewById(R.id.confirmPass);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String fullName = mFullName.getText().toString().trim();
                String confirmPass = mConfirmPass.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)){
                    mFullName.setError("Full Name is Required");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is Required");
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmail.setError("Invalid Email Format");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is Required");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must be at least 6 characters");
                    return;
                }

                if (confirmPass.isEmpty() || !password.equals(confirmPass)) {
                    mConfirmPass.setError("Passwords do not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    String uid = user.getUid();

                                    // Store user info in Firebase Realtime Database
                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("uid", uid);
                                    userMap.put("name", fullName);
                                    userMap.put("email", email);
                                    userMap.put("search", fullName.toLowerCase());
                                    userMap.put("phone", "");
                                    userMap.put("image", "default");
                                    userMap.put("cover", "default");
                                    userMap.put("onlineStatus", "online");
                                    userMap.put("typingTo", "noOne");
                                    userMap.put("isBlocked", false);

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    ref.child(uid).setValue(userMap);

                                    Toast.makeText(RegisterActivity.this, "User Registered", Toast.LENGTH_SHORT).show();

                                    // Send verification email
                                    user.sendEmailVerification()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(RegisterActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "Email not sent: " + e.getMessage());
                                                }
                                            });

                                    // Go to Login screen
                                    startActivitySecond();

                                } else {
                                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void startActivitySecond(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
