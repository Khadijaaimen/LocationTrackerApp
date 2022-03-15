package com.example.latlong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.latlong.R;
import com.example.latlong.modelClass.GroupInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    public static final String TAG = "Adapter";
    Context context;
    ArrayList<String> nNames = new ArrayList<>();
    ArrayList<Integer> nCount = new ArrayList<>();

    public GroupsAdapter(Context context, ArrayList<String> nNames, ArrayList<Integer> nCount){
        this.context = context;
        this.nNames = nNames;
        this.nCount = nCount;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_recyclerview, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {

        holder.groupNameText.setText(nNames.get(position));
        holder.memberCountText.setText(nCount.get(position));

//        holder.delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return nNames.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder{

        ImageView groupImage;
        TextView groupNameText, memberCountText;
        Button delete;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            groupImage = itemView.findViewById(R.id.groupIcon);
            groupNameText = itemView.findViewById(R.id.groupNameTextView);
            memberCountText = itemView.findViewById(R.id.memberCountTextView);
            delete = itemView.findViewById(R.id.deleteGroup);
        }
    }
}
