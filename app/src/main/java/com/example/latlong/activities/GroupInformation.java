package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupInformation extends AppCompatActivity {

    ImageView groupIcon, addMember;
    TextInputLayout enteredEmail;
    TextView groupName, groupMemberEmail, groupMemberStatus;
    LinearLayout membersLayout, newView, linearLayout;
    View view;
    Button addMemberBtn;
    Integer countMember = 0, countGroup = 0;
    DatabaseReference reference;
    GoogleSignInAccount acct;
    com.example.latlong.modelClass.GroupInformation availableGroup;
    CardView cardView;
    ArrayList<String> emails = new ArrayList<>();
    String memberEmail;
    Uri imageUri;
    ProgressBar progressBar;
    StorageReference storageReference, fileReference;
    Boolean isUploaded = false;
    com.example.latlong.modelClass.GroupInformation groups;
    String enteredEmailString, adminEmail, id, token, adminToken;

    public static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        groupIcon = findViewById(R.id.nav_header_view_profilePic);
        groupName = findViewById(R.id.groupNameTextView);
        cardView = findViewById(R.id.imageCardView);
        addMember = findViewById(R.id.addMemberImage);
        progressBar = findViewById(R.id.progressBarAddEmail);
        linearLayout = findViewById(R.id.emailLayout);
        addMemberBtn = findViewById(R.id.addEmail);
        enteredEmail = findViewById(R.id.layout2);

        countMember = getIntent().getIntExtra("memberCount", 0);
        countGroup = getIntent().getIntExtra("groupNumber", 0);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        groups = new com.example.latlong.modelClass.GroupInformation();

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        reference = FirebaseDatabase.getInstance().getReference("groups");

        assert acct != null;
        adminEmail = acct.getEmail();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                    adminToken = token;
                }
            }
        });
        membersLayout = findViewById(R.id.participantsLayout);

        newView = new LinearLayout(this);
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
        newView.addView(view);

        membersLayout.addView(newView);

        groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
        groupMemberStatus = view.findViewById(R.id.memberStatus);

        groupMemberEmail.setText(acct.getEmail().toString());
        groupMemberStatus.setText("Admin");

        availableGroup = new com.example.latlong.modelClass.GroupInformation();

        if (getIntent().getBooleanExtra("isViewUpdate", false)) {
            availableGroup = (com.example.latlong.modelClass.GroupInformation) getIntent().getSerializableExtra("note");
            setViewOrUpdateGroup();
        }

        groupName.setText(availableGroup.getGroupName());

        if(availableGroup.getGroupIcon() == null){
            groupIcon.setPadding(40, 40, 40, 40);
            groupIcon.setImageResource(R.drawable.groups);
        } else {
            Picasso.get().load(availableGroup.getGroupIcon()).into(groupIcon);
        }

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String emailPattern = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";
                enteredEmailString = enteredEmail.getEditText().getText().toString();

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
                } else if (emails.size() > 0) {
                    for (int i = 0; i < emails.size(); i++) {
                        if (enteredEmailString.equals(emails.get(i))) {
                            enteredEmail.setError("Member already added.");
                            enteredEmail.getEditText().setText("");
                            progressBar.setVisibility(View.GONE);
                            return;
                        } else{
                            enteredEmail.getEditText().setText("");
                            enteredEmail.setErrorEnabled(false);
                        }
                    }
                    emails.add(enteredEmailString);
                } else {
                    emails.add(enteredEmailString);
                    enteredEmail.setErrorEnabled(false);
                }

                FirebaseAuth fAuth = FirebaseAuth.getInstance();

                fAuth.fetchSignInMethodsForEmail(enteredEmailString)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (!task.isSuccessful()) {
                                    enteredEmail.setError("Email doesn't exist in our database");
                                    return;
                                } else {

                                    newView = new LinearLayout(GroupInformation.this);
                                    view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
                                    newView.addView(view);

                                    membersLayout.addView(newView);

                                    groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
                                    groupMemberStatus = view.findViewById(R.id.memberStatus);

                                    groupMemberStatus.setText("Member");
                                    groupMemberEmail.setText(enteredEmailString);

                                    enteredEmail.setErrorEnabled(false);
                                    enteredEmail.getEditText().setText("");

                                    reference.child(id).child("Groups").child("Group " + countGroup).child("Member " + countMember).child("email").setValue(enteredEmailString);
                                    reference.child(id).child("Groups").child("Group " + countGroup).child("Member " + countMember).child("admin_token").setValue(adminToken);
                                    reference.child(id).child("Groups").child("Group " + countGroup).child("Member " + countMember).child("group_name").setValue(availableGroup.getGroupName());

                                    countMember++;

                                    groups.setMemberCount(countMember);
                                    reference.child(id).child("Groups").child("Group " + countGroup).child("no_of_members").setValue(countMember);
                                    progressBar.setVisibility(View.GONE);
                                    linearLayout.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }

    private void setViewOrUpdateGroup() {

        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + countGroup)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (emails.size() < countMember) {
                                    memberEmail = ds.child("email").getValue().toString();
                                } else{
                                    return;
                                }
                                emails.add(memberEmail);
                                newView = new LinearLayout(GroupInformation.this);
                                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
                                newView.addView(view);

                                membersLayout.addView(newView);

                                groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
                                groupMemberStatus = view.findViewById(R.id.memberStatus);

                                groupMemberStatus.setText("Member");
                                groupMemberEmail.setText(memberEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private  String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (imageUri != null) {
            fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference imageStore;
                            imageStore = FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + countGroup).child("imageURL");
                            groups.setGroupIcon(uri.toString());
                            imageStore.setValue(uri.toString());
                            isUploaded = true;
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            groupIcon.setImageURI(imageUri);
            uploadFile();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GroupInformation.this, GroupChoice.class);
        startActivity(intent);
        GroupInformation.this.finish();
    }

}