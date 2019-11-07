package com.shaikhutech.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shaikhutech.whatsapp.MessageActivity;
import com.shaikhutech.whatsapp.Model.User;
import com.shaikhutech.whatsapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    static ArrayList<User> users;
    static Context context;


    public UserAdapter(Context ctx, ArrayList<User> users){
        this.context = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,final int i) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.users_row,viewGroup,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int i) {

        User user = users.get(i);

        holder.txtUserName.setText(user.name);

        Glide.with(context).load(user.profilePicUrl).into(holder.userProfilePic);

        if(user.status.equals("online")){
            holder.userOnline.setVisibility(View.VISIBLE);
           holder.userOffline.setVisibility(View.GONE);
        }else if(user.status.toLowerCase().equals("offline")){
            holder.userOffline.setVisibility(View.VISIBLE);
            holder.userOnline.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    static class ViewHolder extends  RecyclerView.ViewHolder {

        TextView txtUserName,txtLastMessage;
        CircleImageView userProfilePic,userOnline,userOffline;

         ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUserName = itemView.findViewById(R.id.txtUserName);

            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);

            userProfilePic = itemView.findViewById(R.id.userProfilePic);

            userOnline = itemView.findViewById(R.id.userOnline);
            userOffline = itemView.findViewById(R.id.userOffline);



             itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent(context, MessageActivity.class);

                     intent.putExtra("userId",users.get(getAdapterPosition()).id);

                     context.startActivity(intent);

                 }
             });
        }
    }



}
