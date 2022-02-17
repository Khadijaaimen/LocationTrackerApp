package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    TextView latitudes, longitudes, boldName;
    TextInputLayout mName, mEmail, mPhone, mPassword, updateNameInfo, updatePasswordInfo, oldPasswordInfo;
    String name, email, phoneNo, password, lat, longi, oldPassword;
    ImageView addImage;
    FirebaseAuth firebaseAuth;
    Button logoutBtn, updateButton, refreshButton;
    GpsTracker gpsTracker;
    double latitude, longitude;
    private AlertDialog updateInfo;

    String personName, personEmail;
    UserModelClass userModelClass;
    GoogleSignInAccount acct;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        boldName = findViewById(R.id.nameBoldProfile);
        logoutBtn = findViewById(R.id.logoutButton);
        updateButton = findViewById(R.id.updateBtn);
        latitudes = findViewById(R.id.latProfile);
        longitudes = findViewById(R.id.longProfile);
        addImage = findViewById(R.id.imageAddImage);
        refreshButton = findViewById(R.id.refreshLocation);

        mName = findViewById(R.id.layout1);
        mEmail = findViewById(R.id.layout2);
        mPhone = findViewById(R.id.layout3);
        mPassword = findViewById(R.id.layout4);

        firebaseAuth = FirebaseAuth.getInstance();
        userModelClass = new UserModelClass();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        phoneNo = intent.getStringExtra("phoneNo");
        password = intent.getStringExtra("password");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        database = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");
        reference = database.getReference("users");

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(ProfileActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitudeFromNetwork();
            longitude = gpsTracker.getLongitudeFromNetwork();
            lat = String.valueOf(latitude);
            longi = String.valueOf(longitude);

        } else {
            gpsTracker.showSettingsAlert();
        }

        showAllUserData();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateInfoDialog();
                showAllUserData();
            }
        });


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(ProfileActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitude = gpsTracker.getLatitudeFromNetwork();
                    longitude = gpsTracker.getLongitudeFromNetwork();
                    lat = String.valueOf(latitude);
                    longi = String.valueOf(longitude);

                } else {
                    gpsTracker.showSettingsAlert();
                }
                showAllUserData();
            }
        });
    }

    private void showUpdateInfoDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.update_profile, findViewById(R.id.updateProfileContainer));
        alert.setView(view);

        updateNameInfo = view.findViewById(R.id.layout5);
        updateNameInfo.getEditText().setText(name);
        updateNameInfo.requestFocus();

        oldPasswordInfo = view.findViewById(R.id.layout6);

        updatePasswordInfo = view.findViewById(R.id.layout7);
//        updatePasswordInfo.getEditText().setText(password);

        updateInfo = alert.create();
        view.findViewById(R.id.doneBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateName() || validatePassword()) {
                    Toast.makeText(ProfileActivity.this, "Information updated, you need to login again to authenticate.", Toast.LENGTH_LONG).show();
                } else {
                    return;
                }
            }
        });

        view.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInfo.dismiss();
            }
        });
        updateInfo.show();
    }

    private boolean validatePassword() {
        if (password.equals(oldPasswordInfo.getEditText().getText().toString())) {
            if (!password.equals(updatePasswordInfo.getEditText().getText().toString())) {
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("password").setValue(updatePasswordInfo.getEditText().getText().toString());
                password = updatePasswordInfo.getEditText().getText().toString();
                mPassword.getEditText().setText(password);
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.putExtra("oldPasswordEnteredByUser", oldPasswordInfo.getEditText().getText().toString());
                intent.putExtra("updatedPassword", password);
                startActivity(intent);
                return true;
            } else {
                updatePasswordInfo.getEditText().setText(password);
                Toast.makeText(ProfileActivity.this, "Password is same as before", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else{
            Toast.makeText(ProfileActivity.this, "Password entered doesn't match with current password", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private boolean validateName() {
        if (!name.equals(updateNameInfo.getEditText().getText().toString())) {
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(updateNameInfo.getEditText().getText().toString());
            name = updateNameInfo.getEditText().getText().toString();
            mName.getEditText().setText(name);
            return true;
        } else {
            updateNameInfo.getEditText().setText(name);
            Toast.makeText(ProfileActivity.this, "Name is same as before", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void signOut() {
        if (acct != null) {
            firebaseAuth.signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Toast.makeText(getApplicationContext(), "Signed out from google", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            firebaseAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Toast.makeText(getApplicationContext(), "Signed out from firebase", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
    }

    private void showAllUserData() {

        if (acct != null) {
            updateButton.setVisibility(View.GONE);
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();

            userModelClass.setEmail(personEmail);
            userModelClass.setName(personName);
            userModelClass.setLatitude(lat);
            userModelClass.setLongitude(longi);

            mName.getEditText().setText(personName);
            mEmail.getEditText().setText(personEmail);
            boldName.setText(personName);
            latitudes.setText(lat);
            longitudes.setText(longi);

            mPhone.setVisibility(View.GONE);
            mPassword.setVisibility(View.GONE);

            firebaseAuth.fetchSignInMethodsForEmail(personEmail).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                    if (isNewUser) {
                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModelClass);
                    }
                }
            });

        } else {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                userModelClass.setEmail(email);
                userModelClass.setName(name);
                userModelClass.setPassword(password);
                userModelClass.setPhoneNo(phoneNo);
                userModelClass.setLatitude(lat);
                userModelClass.setLongitude(longi);

                mName.getEditText().setText(name);
                mEmail.getEditText().setText(email);
                boldName.setText(name);
                mPhone.getEditText().setText(phoneNo);
                mPassword.getEditText().setText(password);
                latitudes.setText(lat);
                longitudes.setText(longi);

                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModelClass);
            }
        }
    }
}