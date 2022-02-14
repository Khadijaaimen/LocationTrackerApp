package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText nName, nEmail, nPassword, nPhoneNo, nPassword2;
    Button nRegisterBtn;
    TextView nClickLogin;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    String userId;
    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        nName = findViewById(R.id.editNames);
        nEmail = findViewById(R.id.editEmails);
        nPassword = findViewById(R.id.editPasswords);
        nPhoneNo = findViewById(R.id.editPhoneNos);
        nPassword2 = findViewById(R.id.editRePasswords);
        nRegisterBtn = findViewById(R.id.signUpBtn);
        nClickLogin = findViewById(R.id.alreadyCreatedAccount);

        progressBar = findViewById(R.id.progressBarRegister);
        firebaseAuth = FirebaseAuth.getInstance();

        nRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                UserModelClass user = new UserModelClass();
                String name = nName.getText().toString().trim();
                String email = nEmail.getText().toString().trim();
                String password = nPassword.getText().toString().trim();
                String phoneNo = nPhoneNo.getText().toString().trim();
                String password2 = nPassword2.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    nName.setError("Required Field!");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    nEmail.setError("Required Field!");
                    return;
                }

                if (TextUtils.isEmpty(phoneNo)) {
                    nPhoneNo.setError("Required Field!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    nPassword.setError("Required Field!");
                    return;
                } else if (password.length() < 8) {
                    nPassword.setError("Password must have more than 8 characters!");
                    return;
                }

                if (TextUtils.isEmpty(password2)) {
                    nPassword2.setError("Required Field!");
                    return;
                } else if (!password2.equals(password)) {
                    nPassword2.setError("Both passwords should match");
                    return;
                }
                user.setName(name);
                user.setEmail(email);
                user.setPhoneNo(phoneNo);
                user.setPassword(password);
                user.setRe_password(password2);

                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

//                            UserModelClass userModelClass = new UserModelClass();
//                            userModelClass.setName(name);
//                            userModelClass.setEmail(email);
//                            userModelClass.setPassword(password);

                            rootNode = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");
                            reference = rootNode.getReference("users");
                            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    user.setName("");
                                    user.setEmail("");
                                    user.setPhoneNo("");
                                    user.setPassword("");
                                    user.setRe_password("");

                                    userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                                    startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));

                                    Toast.makeText(RegistrationActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
//                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//
//                            FirebaseUser fUser = firebaseAuth.getCurrentUser();
//
//                            Toast.makeText(RegistrationActivity.this, "User Added", Toast.LENGTH_SHORT).show();
//                            userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
//                            DocumentReference documentReference = firestore.collection("users").document(userId);
//                            Map<String, Object> user = new HashMap<>();
//                            user.put("name", name);
//                            user.put("email", email);
//                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    Log.d(TAG, "onSuccess: Profile is created for " + userId);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "onFailure: " + e.toString());
//                                }
//                            });
//                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                        } else {
//                            Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//                });
//            }
//        });
        nClickLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }
}