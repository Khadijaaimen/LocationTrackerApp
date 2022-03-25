package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
import com.example.latlong.modelClass.GroupInformation;
import com.example.latlong.modelClass.MemberInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.messaging.Constants;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MakeGroup extends AppCompatActivity {

    TextInputLayout groupName, enteredEmail;
    Button addMember, done;
    LinearLayout parent, addedMembers, linearLayout;
    TextView memberNameInitial;
    DatabaseReference reference, reference2, reference3;
    String groupNameString, enteredEmailString, token, adminEmail, adminName, firstInitial, lastInitial, adminToken, id;
    Integer memberCount = 0, groupCount = 0;
    MemberInformation memberInformation;
    ProgressBar progressBar;
    ImageView deleteMemberIcon, addImage;
    GoogleSignInAccount acct;
    View view;
    com.example.latlong.modelClass.GroupInformation groups;
    ArrayList<String> emails, addedEmail;
    RelativeLayout cardView;
    Uri imageUri;
    StorageReference storageReference, fileReference;
    Boolean isUploaded = false, isClicked = false;
    AdminInformation adminInformation;

    public static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        groupName = findViewById(R.id.layout1);
        enteredEmail = findViewById(R.id.layout2);

        addMember = findViewById(R.id.addEmail);
        done = findViewById(R.id.done);
        cardView = findViewById(R.id.iconGroupLayout);
        addImage = findViewById(R.id.imageAddImage);

        progressBar = findViewById(R.id.progressBarAddEmail);

        addedMembers = findViewById(R.id.addedMembersLayout);
        parent = findViewById(R.id.membersLayoutView);

        emails = new ArrayList<>();
        addedEmail = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("groups");
        reference3 = FirebaseDatabase.getInstance().getReference("token");

        storageReference = FirebaseStorage.getInstance().getReference("groupUploads");

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        assert acct != null;
        adminName = acct.getDisplayName();
        adminEmail = acct.getEmail();

        adminInformation = new AdminInformation();

        adminInformation.setAdminName(adminName);
        adminInformation.setAdminEmail(adminEmail);

        memberInformation = new MemberInformation();
        groups = new GroupInformation();

        reference.child(id).child("Admin_Information").child("no_of_groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    progressBar.setVisibility(View.GONE);
                    int number = snapshot.getValue(Integer.class);
                    groupCount = number;
                } else{
                    progressBar.setVisibility(View.GONE);
                    groupCount = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                    adminToken = token;
                    adminInformation.setToken(adminToken);
                    reference3.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_token").setValue(token);
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

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                groups.setGroupName(groupNameString);

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

                                    isClicked = true;
                                    enteredEmail.setErrorEnabled(false);

                                    addedMembers.setVisibility(View.VISIBLE);

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

                                    groups.setMemberCount(memberCount);

                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("group_name").setValue(groupNameString);
                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("no_of_members").setValue(memberCount);
                                    reference2.child(id).child("Groups").child("Group " + groupCount).child("group_number").setValue(groupCount);
                                }
                            }
                        });
            }
        });

        if (memberCount > 0) {
            done.setVisibility(View.VISIBLE);
        }

        deleteMemberIcon = view.findViewById(R.id.crossIcon);
        deleteMemberIcon.bringToFront();
        deleteMemberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference2.child(id).child("Groups").child("Group " + groupCount).child("Member " + memberCount).removeValue();
                parent.removeView(view);
                memberCount--;
                groups.setMemberCount(memberCount);
                reference2.child(id).child("Groups").child("Group " + groupCount).child("no_of_members").setValue(memberCount);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MakeGroup.this, MyGroups.class);
                groupCount++;
                adminInformation.setNumberOfGroups(groupCount);

                progressBar.setVisibility(View.VISIBLE);
//                Toast.makeText(getApplicationContext(), "Please wait while data is being loaded", Toast.LENGTH_LONG).show();

                reference.child(id).child("Admin_Information").child("name").setValue(adminInformation.getAdminName());
                reference.child(id).child("Admin_Information").child("email").setValue(adminInformation.getAdminEmail());
                reference.child(id).child("Admin_Information").child("token").setValue(adminInformation.getToken());
                reference.child(id).child("Admin_Information").child("no_of_groups").setValue(adminInformation.getNumberOfGroups());

                intent.putExtra("groupCountFromMake", groupCount);
                startActivity(intent);
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
                            imageStore = FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + groupCount).child("imageURL");
                            groups.setGroupIcon(uri.toString());
                            imageStore.setValue(uri.toString());
                            isUploaded = true;

                            progressBar.setVisibility(View.GONE);
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
            progressBar.setVisibility(View.VISIBLE);
            imageUri = data.getData();
            addImage = findViewById(R.id.imageAddImage);
            addImage.setImageURI(imageUri);
            uploadFile();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MakeGroup.this, GroupChoice.class);
        reference.child(id).child("Groups").child("Group " + groupCount).removeValue();
        reference.child(id).child("Admin_Information").child("no_of_groups").setValue(groupCount);
        startActivity(intent);
        MakeGroup.this.finish();
    }
}