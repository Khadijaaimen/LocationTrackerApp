package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextView textView1, textView2;
    ImageView logoImage;
    Button mLogin;
    TextView mClickSignup, forgetPass;
    ProgressBar mProgressBar;
    FirebaseAuth fAuth;
    TextInputLayout emailLayout, passwordLayout;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLogin = findViewById(R.id.loginBtn);
        logoImage = findViewById(R.id.logoLogin);
        textView1 = findViewById(R.id.textLogoLogin);
        textView2 = findViewById(R.id.subtextLogoLogin);
        mClickSignup = findViewById(R.id.notCreatedAccount);
        mProgressBar = findViewById(R.id.progressBarLogin);
        forgetPass = findViewById(R.id.forgotPass);
        emailLayout = findViewById(R.id.editEmailLayout);
        passwordLayout = findViewById(R.id.editPasswordsLayout);

        fAuth = FirebaseAuth.getInstance();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateEmail();
                validatePassword();

                loginUser();
            }
        });

        mClickSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                Pair[] pairs = new Pair[7];
                pairs[0] = new Pair<View, String>(logoImage, "logo_image");
                pairs[1] = new Pair<View, String>(textView1, "logo_text");
                pairs[2] = new Pair<View, String>(textView2, "logo_desc");
                pairs[3] = new Pair<View, String>(emailLayout, "email_tran");
                pairs[4] = new Pair<View, String>(passwordLayout, "password_tran");
                pairs[5] = new Pair<View, String>(mLogin, "button_tran");
                pairs[6] = new Pair<View, String>(mClickSignup, "text_tran");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation( LoginActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetPassword = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter your email to receive link");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetPassword.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LoginActivity.this, "Reset Link sent to Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error: Reset Link not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                passwordResetDialog.create().show();
            }
        });
    }

    private Boolean validateEmail() {
        String userEnterEmail = emailLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(userEnterEmail)) {
            emailLayout.setError("Required Field!");
            return false;
        } else{
            emailLayout.setError(null);
            emailLayout.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String userEnterPassword = passwordLayout.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(userEnterPassword)) {
            passwordLayout.setError("Required Field!");
            return false;
        } else{
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);
            return true;
        }
    }

    public void loginUser(){
        if(!validateEmail() | !validatePassword()){
            return;
        } else{
            isUser();
        }
    }

    private void isUser() {

        if (!validateEmail() | !validatePassword()) {
            mProgressBar.setVisibility(View.GONE);
            return;
        } else {
            String userEnterEmail = emailLayout.getEditText().getText().toString().trim();
            String userEnterPassword = passwordLayout.getEditText().getText().toString().trim();
            mProgressBar.setVisibility(View.VISIBLE);
            fAuth.signInWithEmailAndPassword(userEnterEmail, userEnterPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(id);

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){

                                    UserModelClass user = new UserModelClass();
                                    emailLayout.setError(null);
                                    emailLayout.setErrorEnabled(false);
                                    String passwordFromDB = snapshot.child("password").getValue().toString();
                                    Log.d("TAg", passwordFromDB);

                                    if(passwordFromDB.equals(userEnterPassword)){

                                        emailLayout.setError(null);
                                        emailLayout.setErrorEnabled(false);

                                        String nameFromDB = snapshot.child("name").getValue().toString();
                                        String emailFromDB = snapshot.child("email").getValue().toString();
                                        String phoneNoFromDB = snapshot.child("phoneNo").getValue().toString();

                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                        intent.putExtra("name", nameFromDB);
                                        intent.putExtra("email", emailFromDB);
                                        intent.putExtra("phoneNo", phoneNoFromDB);
                                        intent.putExtra("password", passwordFromDB);

                                        startActivity(intent);
                                        Toast.makeText(LoginActivity.this, "User Logged in!", Toast.LENGTH_LONG).show();

                                        user.setEmail("");
                                        user.setPassword("");
                                    } else{
                                        passwordLayout.setError("Wrong Password");
                                        passwordLayout.requestFocus();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }else{
                                    emailLayout.setError("No such email exists");
                                    emailLayout.requestFocus();
                                    mProgressBar.setVisibility(View.GONE);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "Not Working", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        Toast.makeText(LoginActivity.this, "Error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

    }
}