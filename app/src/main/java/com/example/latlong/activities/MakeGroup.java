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

    TextInputLayout groupName, enteredEmail, memberName, memberEmail;
    Button addEmail, addMember, done;
    ImageButton checkGroupName;
    LinearLayout parent, addedMembers, memberInfo, linearLayout, addEmailLayout;
    TextView memberNameInitial;
    DatabaseReference reference, reference2;
    String groupNameString, enteredEmailString, token, adminEmail, firstInitial, lastInitial, adminToken, id;
    Integer memberCount = 0, groupCount = 0, intentFromGroupChoice;
    MemberInformation memberInformation;
    ProgressBar progressBar;
    ImageView deleteMemberIcon;
    GoogleSignInAccount acct;
    View view;
    Boolean isPressed = false, isAvailable = true;

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
//        addEmailLayout = findViewById(R.id.emailLayout);
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

        String emailPattern = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";

        addEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPressed = true;
                progressBar.setVisibility(View.VISIBLE);

                groupNameString = groupName.getEditText().getText().toString();

                if (TextUtils.isEmpty(groupNameString)) {
                    groupName.setError("Required Field");
                    return;
                } else if (groupNameString.length() > 30) {
                    groupName.setError("Word limit should not exceed than 30");
                    return;
                } else {
                    groupName.setErrorEnabled(false);
                }

                enteredEmailString = enteredEmail.getEditText().getText().toString();

                memberInformation.setAdminToken(adminToken);

                if (TextUtils.isEmpty(enteredEmailString)) {
                    enteredEmail.setError("Required Field");
                    return;
                } else if (enteredEmailString.equals(adminEmail)) {
                    enteredEmail.setError("Admin can't be a member. Please enter another email.");
                    enteredEmail.getEditText().setText("");
                    return;
                } else if (!enteredEmailString.matches(emailPattern)) {
                    enteredEmail.setError("Invalid Format");
                    enteredEmail.getEditText().setText("");
                    return;
                } else {
                    enteredEmail.setErrorEnabled(false);
                }

//                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        ArrayList<String> emails = new ArrayList<>();
//                        if (snapshot.exists()) {
//                            for (DataSnapshot ds : snapshot.getChildren()) {
//                                memberEmailFromDb = ds.child(groupNameString).child("Members").child("email").getValue().toString();
//                                emails.add(memberEmailFromDb);
//                                for (int i = 0; i < emails.size(); i++) {
//                                    if (memberEmailFromDb.equals(emails.get(i))) {
//                                        isAvailable = true;
//                                        progressBar.setVisibility(View.GONE);
//                                        enteredEmail.setError("Email already added. Enter a different email.");
//                                        return;
//                                    } else {
//                                        isAvailable = false;
//                                        enteredEmail.setErrorEnabled(false);
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                firstInitial = String.valueOf(enteredEmailString.charAt(0));

                FirebaseAuth fAuth = FirebaseAuth.getInstance();

//                if(!isAvailable) {
                    fAuth.fetchSignInMethodsForEmail(enteredEmailString)
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (!task.isSuccessful()) {
                                        enteredEmail.setError("Email doesn't exist in our database");
                                        groupName.setErrorEnabled(false);
                                    } else {
                                        memberInformation.setMemberEmail(enteredEmailString);
                                        enteredEmail.setErrorEnabled(false);

                                        enteredEmail.getEditText().setText("");
                                        memberInfo.setVisibility(View.VISIBLE);
//                                  memberName.getEditText().setText();
                                        memberEmail.getEditText().setText(enteredEmailString);
                                        progressBar.setVisibility(View.GONE);
                                        groupName.setErrorEnabled(false);

                                    }
                                }
                            });
//                }
            }
        });

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.members_list, parent, false);
        linearLayout = new LinearLayout(getApplicationContext());

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedMembers.setVisibility(View.VISIBLE);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                reference2 = database.getReference("groups");
                reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).child("email").setValue(enteredEmailString);
                reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).child("admin_token").setValue(adminToken);

                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.members_list, parent, false);
                linearLayout = new LinearLayout(getApplicationContext());
                linearLayout.addView(view);
                parent.addView(linearLayout);

                memberNameInitial = view.findViewById(R.id.cardTextView);

                String initials = firstInitial.toUpperCase();
                memberNameInitial.setText(initials);

                memberInfo.setVisibility(View.GONE);
                memberEmail.getEditText().setText("");

                done.setVisibility(View.VISIBLE);
                done.setEnabled(true);
                memberCount++;
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
                intent.putExtra("groupName", groupNameString);
                startActivity(intent);
            }
        });


    }
}