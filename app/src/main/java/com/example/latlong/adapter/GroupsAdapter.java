package com.example.latlong.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.latlong.R;
import com.example.latlong.listener.GroupListener;
import com.example.latlong.modelClass.GroupInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    public static final String TAG = "Adapter";
    Context context;
    List<GroupInformation> groups;
    GroupListener groupListener;

    public GroupsAdapter(Context context, List<GroupInformation> groups, GroupListener groupListener){
        this.context = context;
        this.groups = groups;
        this.groupListener = groupListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.groups_recyclerview, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, @SuppressLint("RecyclerView") int position) {

        GroupInformation info = groups.get(position);
        holder.groupNameText.setText(info.getGroupName());
        if(info.getGroupIcon() == null){
            holder.groupImage.setPadding(10, 10, 10, 10);
            holder.groupImage.setImageResource(R.drawable.groups);
        } else {
            Picasso.get().load(info.getGroupIcon()).placeholder(R.drawable.groups).into(holder.groupImage);
        }
        holder.memberCountText.setText(info.getMemberCount().toString() + " member(s)");

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupListener.onGroupClicked(groups.get(position), position);
            }
        });

//        holder.delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder{

        ImageView groupImage, delete;
        TextView groupNameText, memberCountText;
        RelativeLayout layout;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            groupImage = itemView.findViewById(R.id.groupIcon);
            groupNameText = itemView.findViewById(R.id.groupNameTextView);
            memberCountText = itemView.findViewById(R.id.memberCountTextView);
            layout = itemView.findViewById(R.id.parentLayout);
            delete = itemView.findViewById(R.id.deleteGroup);
        }
    }
}
