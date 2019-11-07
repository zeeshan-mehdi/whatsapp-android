package com.shaikhutech.whatsapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.shaikhutech.whatsapp.MessageActivity;
import com.shaikhutech.whatsapp.Model.User;
import com.shaikhutech.whatsapp.PlaceCallActivity;
import com.shaikhutech.whatsapp.R;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.webrtc.ContextUtils.getApplicationContext;

public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.ViewHolder> {
    static ArrayList<User> users;
    static Context context;
    SinchClient sinchClient;


    public CallsAdapter(Context ctx, ArrayList<User> users){
        this.context = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public CallsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.call_row,viewGroup,false);

        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull CallsAdapter.ViewHolder holder, int i) {

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

            txtUserName = itemView.findViewById(R.id.txtUserName1);

            txtLastMessage = itemView.findViewById(R.id.txtLastMessage1);

            userProfilePic = itemView.findViewById(R.id.userProfilePic1);

            userOnline = itemView.findViewById(R.id.userOnline1);
            userOffline = itemView.findViewById(R.id.userOffline1);



             itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent(context, PlaceCallActivity.class);

                     intent.putExtra("user_id",users.get(getAdapterPosition()).id);


                     Log.e("ID",users.get(getAdapterPosition()).id);

                     context.startActivity(intent);

                 }
             });
        }
    }

//    private void openPlaceCallActivity(Context context) {
//        Intent mainActivity = new Intent(context, PlaceCallActivity.class);
//        context.startActivity(mainActivity);
//    }
//






}
