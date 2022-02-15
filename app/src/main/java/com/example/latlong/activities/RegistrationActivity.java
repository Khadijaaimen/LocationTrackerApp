package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    TextInputLayout nameLayout, emailLayout, passwordLayout, password2Layout, phoneNoLayout;
    EditText nEmail, mName, mPhoneNo;
    Button nRegisterBtn;
    ImageView logoImage;
    TextView nClickLogin, tv1, tv2;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    String userId;
    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        nRegisterBtn = findViewById(R.id.signUpBtn);
        nClickLogin = findViewById(R.id.alreadyCreatedAccount);
        nEmail = findViewById(R.id.editEmails);
        mName = findViewById(R.id.editNames);
        mPhoneNo = findViewById(R.id.editPhoneNos);

        logoImage = findViewById(R.id.imageView);
        tv1 = findViewById(R.id.textSignupLogin);
        tv2 = findViewById(R.id.subtextLogoSignup);
        nameLayout = findViewById(R.id.editName);
        emailLayout = findViewById(R.id.editEmailAddress);
        phoneNoLayout = findViewById(R.id.editPhoneNo);
        passwordLayout = findViewById(R.id.editPassword);
        password2Layout = findViewById(R.id.editRePassword);

        progressBar = findViewById(R.id.progressBarRegister);
        firebaseAuth = FirebaseAuth.getInstance();

        nRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateName();
                validateEmail();
                validatePhoneNo();
                validatePassword();
                validatePassword2();
                registerUser(v);
            }
        });
//
//                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//
//                        } else {
//                            Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//                });

        nClickLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(RegistrationActivity.this, LoginActivity.class);

                Pair[] pairs = new Pair[10];
                pairs[0] = new Pair<View, String>(logoImage, "logo_image");
                pairs[1] = new Pair<View, String>(tv1, "logo_text");
                pairs[2] = new Pair<View, String>(tv2, "logo_desc");
                pairs[3] = new Pair<View, String>(nameLayout, "email_tran");
                pairs[4] = new Pair<View, String>(emailLayout, "email_tran");
                pairs[5] = new Pair<View, String>(phoneNoLayout, "email_tran");
                pairs[6] = new Pair<View, String>(passwordLayout, "password_tran");
                pairs[7] = new Pair<View, String>(password2Layout, "password_tran");
                pairs[8] = new Pair<View, String>(nRegisterBtn, "button_tran");
                pairs[9] = new Pair<View, String>(nClickLogin, "text_tran");

                ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation( RegistrationActivity.this, pairs);
                startActivity(intent1, option.toBundle());
            }
        });
    }


    private Boolean validateName() {
        String name = nameLayout.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            nameLayout.setError("Required Field!");
            return false;
        } else {
            nameLayout.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String email = emailLayout.getEditText().getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.isEmpty()) {
            emailLayout.setError("Required Field!");
            return false;
        } else if (!email.matches(emailPattern)) {
            emailLayout.setError("Invalid Format!");
            return false;
        } else {
            emailLayout.setError(null);
            return true;
        }
    }

    private Boolean validatePhoneNo() {
        String phoneNo = phoneNoLayout.getEditText().getText().toString().trim();

        if (phoneNo.isEmpty()) {
            phoneNoLayout.setError("Required Field!");
            return false;
        } else {
            phoneNoLayout.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String password = passwordLayout.getEditText().getText().toString();

        if (password.isEmpty()) {
            passwordLayout.setError("Required Field!");
            return false;
        } else if (password.length() < 8) {
            passwordLayout.setError("Password must have more than 8 characters!");
            return false;
        } else {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword2() {
        String password = passwordLayout.getEditText().getText().toString();
        String password2 = password2Layout.getEditText().getText().toString();

        if (password2.isEmpty()) {
            password2Layout.setError("Required Field!");
            return false;
        } else if (!password2.equals(password)) {
            password2Layout.setError("Both passwords should match");
            return false;
        } else {
            password2Layout.setError(null);
            password2Layout.setErrorEnabled(false);
            return true;
        }
    }

    private void registerUser(View view) {

        if (!validateName() | !validateEmail() | !validatePassword() | !validatePhoneNo() | !validatePassword2()) {
            progressBar.setVisibility(View.GONE);
            return;
        } else {
            progressBar.setVisibility(View.VISIBLE);

            String name = nameLayout.getEditText().getText().toString();
            String email = emailLayout.getEditText().getText().toString();
            String password = passwordLayout.getEditText().getText().toString();
            String phoneNo = phoneNoLayout.getEditText().getText().toString();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        UserModelClass user = new UserModelClass(phoneNo, name, email, password);

                        rootNode = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");
                        reference = rootNode.getReference("users");
                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(RegistrationActivity.this, ProfileActivity.class);
                                user.setName("");
                                user.setEmail("");
                                user.setPhoneNo("");
                                user.setPassword("");
                                user.setRe_password("");

                                userId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                                startActivity(intent);

                                Toast.makeText(RegistrationActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                    }  else {
                        Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}