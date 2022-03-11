package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.latlong.R;
import com.example.latlong.modelClass.MemberInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MakeGroup extends AppCompatActivity {

    TextInputLayout groupName, enteredEmail;
    Button addMember, done;
    LinearLayout parent, addedMembers, linearLayout;
    TextView memberNameInitial;
    DatabaseReference reference, reference2;
    String groupNameString, enteredEmailString, token, adminEmail, firstInitial, lastInitial, adminToken, id;
    Integer memberCount = 0, groupCount = 0, intentFromGroupChoice;
    MemberInformation memberInformation;
    ProgressBar progressBar;
    ImageView deleteMemberIcon;
    GoogleSignInAccount acct;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        groupName = findViewById(R.id.layout1);
        enteredEmail = findViewById(R.id.layout2);

        addMember = findViewById(R.id.addEmail);
        done = findViewById(R.id.done);

        progressBar = findViewById(R.id.progressBarAddEmail);

        addedMembers = findViewById(R.id.addedMembersLayout);
        parent = findViewById(R.id.membersLayoutView);

        intentFromGroupChoice = getIntent().getIntExtra("noOfGroups", 1);
        groupCount = intentFromGroupChoice;

        reference = FirebaseDatabase.getInstance().getReference("groups");

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        assert acct != null;
        adminEmail = acct.getEmail();

        memberInformation = new MemberInformation();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                    adminToken = token;
                }
            }
        });

//        String[] a = adminName.split(" ");
//        String first = a[0];
//        firstInitial = String.valueOf(first.charAt(0));
//        String second = a[1];
//        lastInitial = String.valueOf(second.charAt(0));

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.members_list, parent, false);
        linearLayout = new LinearLayout(getApplicationContext());

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedMembers.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.VISIBLE);

                groupNameString = groupName.getEditText().getText().toString();

                if (TextUtils.isEmpty(groupNameString)) {
                    groupName.setError("Required Field");
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (groupNameString.length() > 30) {
                    groupName.setError("Word limit should not exceed than 30");
                    progressBar.setVisibility(View.GONE);
                    return;
                } else {
                    groupName.setErrorEnabled(false);
                }

                String emailPattern = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";
                enteredEmailString = enteredEmail.getEditText().getText().toString();

                memberInformation.setAdminToken(adminToken);

                if (TextUtils.isEmpty(enteredEmailString)) {
                    enteredEmail.setError("Required Field");
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (enteredEmailString.equals(adminEmail)) {
                    enteredEmail.setError("Admin can't be a member. Please enter another email.");
                    enteredEmail.getEditText().setText("");
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (!enteredEmailString.matches(emailPattern)) {
                    enteredEmail.setError("Invalid Format");
                    enteredEmail.getEditText().setText("");
                    progressBar.setVisibility(View.GONE);
                    return;
                } else {
                    enteredEmail.setErrorEnabled(false);
                }

                firstInitial = String.valueOf(enteredEmailString.charAt(0));

                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                fAuth.fetchSignInMethodsForEmail(enteredEmailString)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (!task.isSuccessful()) {
                                    enteredEmail.setError("Email doesn't exist in our database");
                                    groupName.setErrorEnabled(false);
                                    return;
                                } else {
                                    enteredEmail.setErrorEnabled(false);

                                    enteredEmail.getEditText().setText("");
                                    progressBar.setVisibility(View.GONE);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    reference2 = database.getReference("groups");
                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).child("email").setValue(enteredEmailString);
                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).child("admin_token").setValue(adminToken);
                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).child("group_name").setValue(groupNameString);

                                    view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.members_list, parent, false);
                                    linearLayout = new LinearLayout(getApplicationContext());
                                    linearLayout.addView(view);
                                    parent.addView(linearLayout);

                                    memberNameInitial = view.findViewById(R.id.cardTextView);

                                    String initials = firstInitial.toUpperCase();
                                    memberNameInitial.setText(initials);

                                    done.setVisibility(View.VISIBLE);
                                    done.setEnabled(true);
                                    memberCount++;

                                }
                            }
                        });



            }
        });

        if (memberCount > 0) {
            done.setVisibility(View.VISIBLE);
        }

        deleteMemberIcon = view.findViewById(R.id.crossIcon);
        deleteMemberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).removeValue();
                parent.removeView(view);
                memberCount--;
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeGroup.this, MyGroups.class);
                intent.putExtra("groupCount", groupCount);
                startActivity(intent);
            }
        });


    }
}