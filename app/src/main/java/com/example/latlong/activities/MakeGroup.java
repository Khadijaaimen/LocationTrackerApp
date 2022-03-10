package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
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

    TextInputLayout groupName, enteredEmail, memberName, memberEmail;
    Button addEmail, addMember, done;
    LinearLayout parent, addedMembers, memberInfo;
    TextView memberNameInitial;
    GoogleSignInAccount acct;
    DatabaseReference reference, reference2;
    String groupNameString, enteredEmailString, token, adminName, adminEmail, firstInitial, lastInitial, adminToken;
    Integer count = 0;
    AdminInformation adminInformation;
    MemberInformation memberInformation;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        groupName = findViewById(R.id.layout1);
        enteredEmail = findViewById(R.id.layout2);
//        memberName = findViewById(R.id.layout3);
        memberEmail = findViewById(R.id.layout4);

        addEmail = findViewById(R.id.addEmail);
        addMember = findViewById(R.id.addMember);
        done = findViewById(R.id.done);
        progressBar = findViewById(R.id.progressBarAddEmail);

        addedMembers = findViewById(R.id.addedMembersLayout);
        memberInfo = findViewById(R.id.memberInfoLayout);
        parent = findViewById(R.id.membersLayoutView);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        reference = FirebaseDatabase.getInstance().getReference("groups");

        adminInformation = new AdminInformation();
        memberInformation = new MemberInformation();

        assert acct != null;
        adminName = acct.getDisplayName();
        adminEmail = acct.getEmail();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    token = task.getResult();
                    adminToken = token;
                }
            }
        });

        adminInformation.setAdminName(adminName);
        adminInformation.setAdminEmail(adminEmail);

//        String[] a = adminName.split(" ");
//        String first = a[0];
//        firstInitial = String.valueOf(first.charAt(0));
//        String second = a[1];
//        lastInitial = String.valueOf(second.charAt(0));

        String emailPattern = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";

        addEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                groupNameString = groupName.getEditText().getText().toString();
                enteredEmailString = enteredEmail.getEditText().getText().toString();

                if (TextUtils.isEmpty(groupNameString)) {
                    groupName.setError("Required Field");
                    return;
                } else if (groupNameString.length() > 30) {
                    groupName.setError("Word limit should not exceed than 30");
                    return;
                } else {
                    groupName.setErrorEnabled(false);
                }
                adminInformation.setToken(adminToken);
                memberInformation.setAdminToken(adminToken);
                adminInformation.setGroupName(groupNameString);

                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Admin").child("name").setValue(adminInformation.getAdminName());
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Admin").child("email").setValue(adminInformation.getAdminEmail());
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Admin").child("token").setValue(adminInformation.getToken());
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Admin").child("group_name").setValue(adminInformation.getGroupName());

                if (TextUtils.isEmpty(enteredEmailString)) {
                    enteredEmail.setError("Required Field");
                    return;
                } else if (enteredEmailString.equals(adminEmail)) {
                    enteredEmail.setError("Admin can't be a member. Please enter another email.");
                    return;
                } else if (!enteredEmailString.matches(emailPattern)) {
                    enteredEmail.setError("Invalid Format");
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
                                } else {
                                    memberInformation.setMemberEmail(enteredEmailString);
                                    enteredEmail.setErrorEnabled(false);

                                    enteredEmail.getEditText().setText("");
                                    memberInfo.setVisibility(View.VISIBLE);
//                                  memberName.getEditText().setText();
                                    memberEmail.getEditText().setText(enteredEmailString);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedMembers.setVisibility(View.VISIBLE);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                reference2 = database.getReference("groups");
                reference2.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Members").child("Member "+count).child("email").setValue(enteredEmailString);
                reference2.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupNameString).child("Members").child("Member "+count).child("admin_token").setValue(adminToken);

                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.members_list, parent, false);
                LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                linearLayout.addView(view);
                parent.addView(linearLayout);

                memberNameInitial = view.findViewById(R.id.cardTextView);
                String initials = firstInitial.toUpperCase();
                memberNameInitial.setText(initials);

                memberInfo.setVisibility(View.GONE);
                memberEmail.getEditText().setText("");

                done.setVisibility(View.VISIBLE);
                done.setEnabled(true);
                count++;
            }
        });

        if(count>0){
            done.setVisibility(View.VISIBLE);
        }
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeGroup.this, MyGroups.class);
                intent.putExtra("groupName", groupNameString);
                startActivity(intent);
            }
        });


    }
}